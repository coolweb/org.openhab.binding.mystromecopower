/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystromecopower.internal.api;

import org.openhab.binding.mystromecopower.internal.api.model.*;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Manage Json Api call to mystrom Api.
 * @since 1.7.0-SNAPSHOT
 * @author Christophe
 *
 */
public class MystromClient implements IMystromClient{
	private Logger logger;
	private JsonParser jsonParser;
	private Gson gson;
	private String userName;
	private String password;
	private String authToken;
	private static final String API_URL = "https://mystrom.ch/mobile/";
	
	public MystromClient(String userName, String password, Logger logger)
	{
		this.gson = this.createGsonBuilder().create();
		this.logger = logger;
		this.userName = userName;
		this.password = password;
		this.jsonParser = new JsonParser();
	}
	
	/* (non-Javadoc)
	 * @see org.openhab.binding.mystromecopower.internal.api.IMystromClient#login()
	 */
	public Boolean login(){
		Reader reader = null;
		this.logger.info("Do login for user '{}'", this.userName);
		
		try {
			HttpURLConnection httpURLConnection;
			httpURLConnection = (HttpURLConnection) new URL(API_URL + "auth?email="
					+ this.userName
					+ "&password=" + this.password)
					.openConnection();
		
			InputStream inputStream = httpURLConnection.getInputStream();
			reader = new InputStreamReader(inputStream, "UTF-8");
			JsonObject jsonObject = (JsonObject) jsonParser.parse(reader);
			
			String status = jsonObject.get("status").getAsString();
			if(!status.equals("ok")){
				return false;
			}
			this.logger.info("Logon successfull");
			
			this.authToken = jsonObject.get("authToken").getAsString();
			
			return true;
		}
		catch(Exception ex)
		{
			this.logger.error("Error do logon: '{}'", ex.toString());
			return false;
		} 
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {

				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openhab.binding.mystromecopower.internal.api.IMystromClient#getDevices()
	 */
	public List<MystromDevice> getDevices()
	{
		Reader reader = null;
		
		this.logger.info("get devices...");
		
		try {
			HttpURLConnection httpURLConnection;
			httpURLConnection = (HttpURLConnection) new URL(API_URL + "devices" +
					"?authToken=" + this.authToken)
				.openConnection();
			
			httpURLConnection.connect();
			
			int responseCode = httpURLConnection.getResponseCode();
			
			if (responseCode != HttpURLConnection.HTTP_OK) {
				this.logger.error("Get devices http code: '{}'", responseCode);
				return new ArrayList<MystromDevice>();
			}
			
			InputStream inputStream = httpURLConnection.getInputStream();
			reader = new InputStreamReader(inputStream, "UTF-8");
			JsonObject jsonObject = (JsonObject) jsonParser.parse(reader);
			
			String status = jsonObject.get("status").getAsString();
			if(!status.equals("ok")){
				this.logger.error("Error while getting devices: '{}'", status);
				return new ArrayList<MystromDevice>();
			}
			
			GetDevicesResult result = gson.fromJson(jsonObject, GetDevicesResult.class);
			this.logger.info("Devices discovery sucessfull, found '{}' devices", result.devices.size());
			
			return result.devices;
		}
		catch(Exception ex){
			this.logger.error("Error getting devices: '{}'", ex.toString());
			return new ArrayList<MystromDevice>();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {

				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openhab.binding.mystromecopower.internal.api.IMystromClient#getDeviceInfo(java.lang.String)
	 */
	public MystromDevice getDeviceInfo(String deviceId){
		Reader reader = null;
		
		this.logger.info("get device info...");
		
		try {
			HttpURLConnection httpURLConnection;
			httpURLConnection = (HttpURLConnection) new URL(API_URL + "device" +
					"?authToken=" + this.authToken
					+ "&id=" + deviceId)
				.openConnection();
			
			httpURLConnection.connect();
			
			int responseCode = httpURLConnection.getResponseCode();
			
			if (responseCode != HttpURLConnection.HTTP_OK) {
				this.logger.error("Get device info http code: '{}'", responseCode);
				return null;
			}
		
			InputStream inputStream = httpURLConnection.getInputStream();
			reader = new InputStreamReader(inputStream, "UTF-8");
			JsonObject jsonObject = (JsonObject) jsonParser.parse(reader);
			
			String status = jsonObject.get("status").getAsString();
			if(!status.equals("ok")){
				this.logger.error("Error while getting device info id: '{}' status '{}'", deviceId, status);
				return null;
			}
			
			GetDeviceInfoResult result = gson.fromJson(jsonObject, GetDeviceInfoResult.class);
			
			return result.device;
		}
		catch(Exception ex){
			this.logger.error("Error getting device info  with id: '{}', detail '{}'", deviceId, ex.toString());
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {

				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openhab.binding.mystromecopower.internal.api.IMystromClient#ChangeState(java.lang.String, java.lang.Boolean)
	 */
	@Override
	public Boolean ChangeState(String deviceId, Boolean newStateIsOn) {
		Reader reader = null;
		this.logger.info(
				"Change state for device id '{}', new state is on: '{}'",
				deviceId,
				newStateIsOn);
		
		try {
			HttpURLConnection httpURLConnection;
			httpURLConnection = (HttpURLConnection) new URL(API_URL + "device/switch" +
					"?authToken=" + this.authToken
					+ "&id=" + deviceId
					+ "&on=" + newStateIsOn.toString())
				.openConnection();
		
			InputStream inputStream = httpURLConnection.getInputStream();
			reader = new InputStreamReader(inputStream, "UTF-8");
			JsonObject jsonObject = (JsonObject) jsonParser.parse(reader);
			
			String status = jsonObject.get("status").getAsString();
			if(!status.equals("ok")){
				String error = jsonObject.get("error").getAsString();
				this.logger.error("Unable to switch state for device '{}' error '{}'", deviceId, error);
				return false;
			}
			
			String newState = jsonObject.get("state").getAsString();
			this.logger.info(
					"Switch state for device '{}' successfull, state is '{}'",
					deviceId,
					newState);
			
			return true;
		}
		catch(Exception ex)
		{
			this.logger.error("Error set state: '{}'", ex.toString());
			return false;
		} 
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {

				}
			}
		}
	}
	
	private GsonBuilder createGsonBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		return gsonBuilder;
	}
}
