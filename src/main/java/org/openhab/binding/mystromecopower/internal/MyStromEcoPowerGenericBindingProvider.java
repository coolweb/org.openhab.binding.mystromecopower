/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystromecopower.internal;

import org.openhab.binding.mystromecopower.MyStromEcoPowerBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Jordens Christophe
 * @since 1.7.0-SNAPSHOT
 */
public class MyStromEcoPowerGenericBindingProvider extends AbstractGenericBindingProvider implements MyStromEcoPowerBindingProvider {
	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "mystromecopower";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof SwitchItem || item instanceof NumberItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only Switch or Number is allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		MyStromEcoPowerBindingConfig config = new MyStromEcoPowerBindingConfig();
		
		//parse bindingconfig here ...	
		config.mystromFriendlyName = bindingConfig;
		config.isSwitch = item instanceof SwitchItem;
		config.isNumberItem = item instanceof NumberItem;
		addBindingConfig(item, config);		
	}
	
	public String getMystromFriendlyName(String itemName) {
		MyStromEcoPowerBindingConfig config = (MyStromEcoPowerBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.mystromFriendlyName : null;
	}
	
	
	class MyStromEcoPowerBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values
		public String mystromFriendlyName;
		
		public Boolean isSwitch = false;
		
		public Boolean isNumberItem = false;
	}


	@Override
	public Boolean getIsSwitch(String itemName) {
		MyStromEcoPowerBindingConfig config = (MyStromEcoPowerBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.isSwitch : false;
	}

	@Override
	public Boolean getIsNumberItem(String itemName) {
		MyStromEcoPowerBindingConfig config = (MyStromEcoPowerBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.isNumberItem : false;
	}
}
