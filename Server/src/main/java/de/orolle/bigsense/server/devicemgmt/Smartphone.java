package de.orolle.bigsense.server.devicemgmt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.vertx.java.core.json.JsonArray;

import de.orolle.bigsense.server.Config;

/**
 * The Class Smartphone.
 */
public class Smartphone {

	/** The imei. */
	private String imei;
	
	/** The last contact. */
	private long lastContact;
	
	/** The real name. */
	private String realName;
	
	/** The battery level. */
	private int batteryLevel;
	
	/** The battery temperature. */
	private float batteryTemperature;
	
	/**
	 * Instantiates a new smartphone.
	 *
	 * @param imei the imei
	 * @param lastContact the last contact
	 * @param realName the real name
	 * @param batteryLevel the battery level
	 * @param batteryTemperature the battery temperature
	 */
	public Smartphone(String imei, long lastContact, String realName, int batteryLevel, float batteryTemperature) {
		super();
		this.imei = imei;
		this.lastContact = lastContact;
		this.realName = realName;
		this.batteryLevel = batteryLevel;
		this.batteryTemperature = batteryTemperature;
	}
	
	/**
	 * Instantiates a new smartphone.
	 *
	 * @param serialized the serialized
	 */
	public Smartphone(String serialized) {
		String[] values = serialized.split(";");
		this.imei = values[0];
		this.lastContact = Long.valueOf(values[1]);
		this.realName = values[2];
		this.batteryLevel = Integer.valueOf(values[3]);
		this.batteryTemperature = Float.valueOf(values[4]);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.imei + ";" + this.lastContact + ";" + this.realName + ";" + this.batteryLevel + ";" + this.batteryTemperature;
	}

	/**
	 * Gets the last contact.
	 *
	 * @return the last contact
	 */
	public long getLastContact() {
		return lastContact;
	}

	/**
	 * Sets the last contact.
	 *
	 * @param lastContact the new last contact
	 */
	public void setLastContact(long lastContact) {
		this.lastContact = lastContact;
	}

	/**
	 * Gets the real name.
	 *
	 * @return the real name
	 */
	public String getRealName() {
		return realName;
	}

	/**
	 * Sets the real name.
	 *
	 * @param realName the new real name
	 */
	public void setRealName(String realName) {
		this.realName = realName;
	}

	/**
	 * Gets the imei.
	 *
	 * @return the imei
	 */
	public String getImei() {
		return imei;
	}
	
	/**
	 * Sets the battery level.
	 *
	 * @param batteryLevel the new battery level
	 */
	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
	
	/**
	 * Gets the battery level.
	 *
	 * @return the battery level
	 */
	public int getBatteryLevel() {
		return batteryLevel;
	}
	
	/**
	 * Sets the battery temperature.
	 *
	 * @param batteryTemperature the new battery temperature
	 */
	public void setBatteryTemperature(float batteryTemperature) {
		this.batteryTemperature = batteryTemperature;
	}
	
	/**
	 * Gets the battery temperature.
	 *
	 * @return the battery temperature
	 */
	public float getBatteryTemperature() {
		return batteryTemperature;
	}

	/**
	 * This is used to print Info in cool format for the web interface
	 *
	 * @return All infos in one json array
	 */
	public JsonArray printPrittily() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");    
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		Date resultdate = new Date(lastContact);
		Date now = new Date();
		
		JsonArray outArray = new JsonArray();
		outArray.add("<h4>" + imei + "</h4>");
		
		String colorDate = "";
		if(resultdate.getTime()/1000 + Config.LIMIT_GOOD_LASTCONTACT > now.getTime()/1000) colorDate = "green";
		else if(resultdate.getTime()/1000 + Config.LIMIT_BAD_LASTCONTACT > now.getTime()/1000) colorDate = "orange";
		else colorDate = "red";
		outArray.add("<h4 style=\"color: " + colorDate + ";\">" + sdf.format(resultdate) + "</h4>");
		
		outArray.add("<h4>" + realName + "</h4>");
		
		String colorBattery = "";
		if(batteryLevel >= Config.LIMIT_GOOD_BATTERY) colorBattery = "green";
		else if(batteryLevel >= Config.LIMIT_BAD_BATTERY) colorBattery = "orange";
		else colorBattery = "red";
		outArray.add("<h4 style=\"color: " + colorBattery + ";\">" + batteryLevel + "%" + "</h4>");
		
		String colorTemperature = "";
		if(batteryTemperature <= Config.LIMIT_GOOD_TEMPERATURE) colorTemperature = "green";
		else if(batteryTemperature <= Config.LIMIT_BAD_TEMPERATURE) colorTemperature = "orange";
		else colorTemperature = "red";
		outArray.add("<h4 style=\"color: " + colorTemperature + ";\">" + batteryTemperature + "°C" + "</h4>");
		outArray.add("<h4><input type=\"button\" class=\"btn btn-default\" value=\"Change Name\" onclick=\"changePhoneName('" + getImei() + "','" + getRealName() + "')\"/></h4>");
		outArray.add("<h4><input type=\"button\" class=\"btn btn-default\" value=\"Delete\" onclick=\"deletePhone('" + getImei() + "')\"/></h4>");
		return outArray;
	}
}
