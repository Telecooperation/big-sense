package de.orolle.bigsense.server.devicemgmt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.vertx.java.core.json.JsonArray;

/**
 * The Class Group.
 */
public class Group {
	
	/** The dbID */
	private int id;
	
	/** The name. */
	private String name;
	
	/** The imeis. */
	private List<String> imeis;
	
	/**
	 * Instantiates a new group.
	 *
	 * @param integer the dbID
	 * @param name the name
	 * @param imeis the imeis
	 */
	public Group(int integer, String name, List<String> imeis) {
		super();
		this.id = integer;
		this.name = name;
		this.imeis = imeis;
	}
	
	/**
	 * Instantiates a new group.
	 *
	 * @param serialized the serialized
	 */
	public Group(String serialized) {
		String[] values = serialized.split(";");
		this.id = Integer.valueOf(values[0]);
		this.name = values[1];
		this.imeis = new ArrayList<>();
		for(int i = 2; i < values.length; i++) {
			imeis.add(values[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = this.id + ";" + this.name;
		for(String imei : imeis) {
			output += ";" + imei;
		}
		return output;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the dbID.
	 *
	 * @param id the new dbID
	 */
	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the dbID.
	 *
	 * @return the dbID
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the imeis.
	 *
	 * @return the imeis
	 */
	public List<String> getImeis() {
		return imeis;
	}
	
	/**
	 * Sets the imeis.
	 *
	 * @param imeis the new imeis
	 */
	public void setImeis(List<String> imeis) {
		this.imeis = imeis;
	}
	
	/**
	 * This is used to print this info for the web interface
	 * It shows the name of this group together with all containing phones and all phones without a group
	 * It adds an add/delete-button for each device
	 *
	 * @param allPhones the all phones
	 * @param allGroups the all groups
	 * @return the array with all information insight
	 */
	public JsonArray printPrittily(List<Smartphone> allPhones, List<Group> allGroups) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm"); 
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		
		JsonArray outArray = new JsonArray();
		outArray.add("<h4>" + name + "</h4>");
		
		String inGroupPhones = "<h4>";
		String notInGroupPhones = "<h4>";
		
		for(int j = 0; j < allPhones.size(); j++) {
			boolean foundPhoneInThisGroup = false;
			boolean foundPhoneInOtherGroup = false;
			
			
			for(int i = 0; i < imeis.size(); i++) {
				if(imeis.get(i).equals(allPhones.get(j).getImei())) {
					foundPhoneInThisGroup = true;
					inGroupPhones += "(" + allPhones.get(j).getRealName() + 
						" <a style=\"color: red;\" href='#' onclick=deletePhoneFromGroup('" + name + "','" + allPhones.get(j).getImei() + "')>Delete</a>), ";
				}
			}			
			
			if(!foundPhoneInThisGroup) {
				for(Group group : allGroups) {
					for(int i = 0; i < group.getImeis().size(); i++) {
						if(group.getImeis().get(i).equals(allPhones.get(j).getImei())) {
							foundPhoneInOtherGroup = true;
						}
					}
				}
				
				
				if(!foundPhoneInOtherGroup) 
					notInGroupPhones += "(" + allPhones.get(j).getRealName() + 
						" <a style=\"color: green;\" href='#' onclick=addPhoneToGroup('" + name + "','" + allPhones.get(j).getImei() + "')>Add</a>), ";
			}
		}
		
		if(inGroupPhones.length()>4) inGroupPhones = inGroupPhones.substring(0, inGroupPhones.length()-2);
		inGroupPhones += "</h4>";
		outArray.add(inGroupPhones);
		
		if(notInGroupPhones.length()>4) notInGroupPhones = notInGroupPhones.substring(0, notInGroupPhones.length()-2);
		notInGroupPhones += "</h4>";
		outArray.add(notInGroupPhones);
		
		outArray.add("<input type=\"button\" class=\"btn btn-default\" value=\"Delete Group\" onclick=\"deleteGroup('" + name + "')\"/>");
		return outArray;
	}
}
