package org.openhab.binding.mystromecopower.internal.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.openhab.binding.mystromecopower.internal.api.model.MystromDevice;

/**
 * @author Jordens Christophe
 * Interface for mystrom client API.
 */
public interface IMystromClient {
	/**
	 * Do login.
	 * @return True if login succeeded else false.
	 */
	public Boolean login();
	
	/**
	 * Search all devices on the connected account, needs to
	 * call logon method before.
	 * @return List of devices.
	 */
	public List<MystromDevice> getDevices();
	
	/**
	 * Returns information about  a device by the id.
	 * @param deviceId The id of the device on the mystrom SRS server.
	 * @return The device information.
	 */
	public MystromDevice getDeviceInfo(String deviceId);
	
	/**
	 * Change the state of a device on or off.
	 * @param deviceId The id of the device for which to change state.
	 * @param newStateIsOn Indicates if new state must be on or off.
	 * @return True is change is successful else False.
	 */
	public Boolean ChangeState(String deviceId, Boolean newStateIsOn);
}
