/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystromecopower.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mystromecopower.MyStromEcoPowerBindingProvider;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.binding.mystromecopower.internal.api.*;
import org.openhab.binding.mystromecopower.internal.api.mock.MockMystromClient;
import org.openhab.binding.mystromecopower.internal.api.model.MystromDevice;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	

/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 * 
 * @author Jordens Christophe
 * @since 1.7.0-SNAPSHOT
 */
public class MyStromEcoPowerBinding extends AbstractActiveBinding<MyStromEcoPowerBindingProvider> implements ManagedService {
	private Boolean devMode = true;
	private String userName;
	private String password;
	private IMystromClient mystromClient;
	
	private static final Logger logger = 
		LoggerFactory.getLogger(MyStromEcoPowerBinding.class);

	// List of discovered devices with their names and id.
	protected Map<String, String> devicesMap = new HashMap<String, String>();
	
	/** 
	 * the refresh interval which is used to poll values from the MyStromEcoPower
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;
	
	
	public MyStromEcoPowerBinding() {
	}
		
	
	public void activate() {
	}
	
	public void deactivate() {
		// deallocate resources here that are no longer needed and 
		// should be reset when activating this binding again
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "MyStromEcoPower Refresh Service";
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		logger.debug("execute() method is called!");
		
		for (MyStromEcoPowerBindingProvider provider : providers) {
			for (String itemName : provider.getItemNames()) {
				logger.debug("Mystrom eco power switch '{}' state will be updated", itemName);
				
				String friendlyName = provider.getMystromFriendlyName(itemName);
				String id = this.devicesMap.get(friendlyName);
				
				if(id != null){
					MystromDevice device;
					device = this.mystromClient.getDeviceInfo(id);
					
					if(device != null){
						if(provider.getIsSwitch(itemName)){
							State state = device.state.equals("on") ? OnOffType.ON : OnOffType.OFF;
							eventPublisher.postUpdate(itemName,  state);
						}
						
						if(provider.getIsStringItem(itemName)){
							// publish state of device, on/off/offline
							eventPublisher.postUpdate(itemName,  new StringType(device.state));
						}
						
						if(provider.getIsNumberItem(itemName)){
							eventPublisher.postUpdate(itemName, new DecimalType(device.power));
						}
					}
				} else {
					logger.warn("The device itemName '{}' not found on discovery verify device is not offline",  itemName);
				}
			}
		}
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand() is called!");
		String deviceId = null;
		
		for (MyStromEcoPowerBindingProvider provider : providers) {
			String switchFriendlyName = provider.getMystromFriendlyName(itemName);
			deviceId = this.devicesMap.get(switchFriendlyName);
		    logger.debug("item '{}' is configured as '{}'",itemName, switchFriendlyName);
		    
		    if(deviceId != null)
			{
		    	if(provider.getIsSwitch(itemName))
		    	{
					try {
						logger.info("Command '{}' is about to be send to item '{}'",command, itemName );
						
						boolean onOff = OnOffType.ON.equals(command);
						logger.debug("command '{}' transformed to '{}'", command, onOff);
						
						if(!this.mystromClient.ChangeState(deviceId, onOff))
						{
							// Unsuccessful state change, inform bus that the good 
							// state is the old one.
							eventPublisher.postUpdate(itemName,  onOff ? OnOffType.OFF : OnOffType.ON);
						}
						
					} catch (Exception e) {
						logger.error("Failed to send {} command", command, e);
					}
		    	}
			} else {
				logger.error("Unable to send command to '{}' device is not in discovery table", itemName);
			}
		}
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand() is called!");
	}
		
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {
			
			// to override the default refresh interval one has to add a 
			// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}
			
			// read further config parameters here ...
			// read user name
			String userName = (String) config.get("userName");
			if(StringUtils.isNotBlank(userName)) {
				this.userName = userName;
			} else {
				throw new ConfigurationException("userName", "The userName to connect to myStrom must be specified in config file");
			}
			
			String password = (String) config.get("password");
			if(StringUtils.isNotBlank(password)) {
				this.password = password;
			} else {
				throw new ConfigurationException("password", "The password to connect to myStrom must be specified in config file");
			}
			
			if(this.devMode){
				this.mystromClient = new MockMystromClient();
			} else {
				this.mystromClient = new MystromClient(this.userName, this.password, logger);
			}
			
			setProperlyConfigured(true);
			
			// do a discovery of all mystrom eco power to get id of devices
			try {
				this.mystromDiscovery();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void mystromDiscovery() throws MalformedURLException, IOException{
		List<MystromDevice> devices;
		logger.info("Do mystrom discovery");
		
		this.devicesMap.clear();
		
		if(this.mystromClient.login() == false)
		{
			logger.info("Invalid user or password");
			return;
		}
		
		devices = this.mystromClient.getDevices();
		
		for (MystromDevice mystromDevice : devices) {
			this.devicesMap.put(mystromDevice.name, mystromDevice.id);
		}
	}
}
