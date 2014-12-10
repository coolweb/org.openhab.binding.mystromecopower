package org.openhab.binding.mystromecopower.internal.api.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mystromecopower.internal.api.IMystromClient;
import org.openhab.binding.mystromecopower.internal.api.model.MystromDevice;

public class MockMystromClient implements IMystromClient{
	private Map<String, MystromDevice> devices = new HashMap<String, MystromDevice>();

	@Override
	public Boolean login() {
		return true;
	}

	@Override
	public List<MystromDevice> getDevices() {
		ArrayList<MystromDevice> foundDevices = new ArrayList<MystromDevice>();
		this.devices.clear();
		
		MystromDevice device1 = new MystromDevice();
		device1.name = "Halogène/Multi prises";
		device1.id = "1";
		device1.state = "on";
		devices.put(device1.id, device1);
		foundDevices.add(device1);
		
		
		MystromDevice device2 = new MystromDevice();
		device2.name = "Tv/HomeCinéma";
		device2.id = "2";
		device2.state = "off";
		devices.put(device2.id, device2);
		foundDevices.add(device2);
		
		return foundDevices;
	}

	@Override
	public MystromDevice getDeviceInfo(String deviceId) {
		MystromDevice device = this.devices.get(deviceId);
		device.power = Double.toString(Math.random() * 100);
		return device;
	}

	@Override
	public void ChangeState(String deviceId, Boolean newStateIsOn) {
		this.devices.get(deviceId).state = newStateIsOn ? "on" : "off";
	}

}
