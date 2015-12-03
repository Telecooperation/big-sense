package de.orolle.bigsense.server.devicemgmt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.vertx.java.core.json.JsonArray;

/**
 * The Class AppVersion.
 */
public class AppVersion {

	/** The dbID */
	private int id;

	/** The package name. */
	private String packageName;
	
	/** The file name. */
	private String fileName;
	
	/** config */
	private String config;
	
	/** The last change. */
	private long lastChange;
	
	/** The smartphones. */
	private List<State> smartphones;
	
	/** The groups. */
	private List<String> groups;
	
	/**
	 * Instantiates a new app version.
	 *
	 * @param id the dbID
	 * @param packageName the package name
	 * @param fileName the name of the file
	 * @param config config of this app
	 * @param lastChange the last change
	 * @param smartphones the smartphones
	 * @param groups the groups
	 */
	public AppVersion(int id, String packageName, String fileName, String config, long lastChange, List<State> smartphones, List<String> groups) {
		super();
		this.id = id;
		this.packageName = packageName;
		this.fileName = fileName;
		this.config = config.replaceAll("\"", "&quot;");
		this.lastChange = lastChange;
		this.smartphones = smartphones;
		this.groups = groups;
	}
	
	/**
	 * Instantiates a new app version.
	 *
	 * @param serialized the serialized
	 */
	public AppVersion(String serialized) {
		String[] parts = serialized.split(":");
		String[] values = parts[0].split(";");
		this.id = Integer.valueOf(values[0]);
		this.packageName = values[1];
		this.fileName = values[2];
		this.config = values[3].replaceAll("\"", "&quot;");
		this.lastChange = Long.valueOf(values[4]);
		this.smartphones = new ArrayList<>();
		for(int i = 5; i < values.length; i = i+3) {
			smartphones.add(new State(values[i], 
					SmartphoneState.valueOf(Integer.valueOf(values[i+1])), 
					Long.valueOf(values[i+2])));
		}
		this.groups = new ArrayList<>();
		if(parts.length > 1) {
			String[] values2 = parts[1].split(";");
			for(int i = 0; i < values2.length; i++) {
				if(values2[i].length() > 0) groups.add(values2[i]);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = this.id + ";" + this.packageName + ";" + this.fileName + ";" + this.config + ";" + this.lastChange;
		for(State state : smartphones) {
			output += ";" + state.getImei() + ";" + state.getState().getValue() + ";" + state.getLastRestart();
		}
		output += ":";
		for(int i = 0; i < groups.size(); i++) {
			output += groups.get(i);
			if(i < groups.size() - 1) output += ";";
		}
		return output;
	}

	/**
	 * Gets the package name.
	 *
	 * @return the package name
	 */
	public String getPackageName() {
		return packageName;
	}
	
	/**
	 * Gets the file name
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Returns the config
	 * @return config
	 */
	public String getConfig() {
		return config;
	}

	/**
	 * Gets the last change.
	 *
	 * @return the last change
	 */
	public long getLastChange() {
		return lastChange;
	}

	/**
	 * Gets the smartphones.
	 *
	 * @return the smartphones
	 */
	public List<State> getSmartphones() {
		return smartphones;
	}
	
	/**
	 * Gets the groups.
	 *
	 * @return the groups
	 */
	public List<String> getGroups() {
		return groups;
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
	 * Sets the last change.
	 *
	 * @param lastChange the new last change
	 */
	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}
	
	/**
	 * Sets the file name
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Sets the config
	 * @param config
	 */
	public void setConfig(String config) {
		this.config = config.replaceAll("\"", "&quot;");
	}

	/**
	 * Sets the smartphones.
	 *
	 * @param smartphones the new smartphones
	 */
	public void setSmartphones(List<State> smartphones) {
		this.smartphones = smartphones;
	}
	
	/**
	 * Sets the groups.
	 *
	 * @param groups the new groups
	 */
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	/**
	 * This is used to print this info for the web interface
	 * It shows a list of all groups (and their smartphones). Those, on which this app is deployed and all others
	 * It adds an deploy/undeploy-button for each group
	 *
	 * @param allGroups the all groups
	 * @param allPhones To get the real names of the phones
	 * @return all apps together in one array
	 */
	public JsonArray printPrittily(List<Group> allGroups, List<Smartphone> allPhones) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm"); 
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		Date resultdate = new Date(lastChange);
			
		JsonArray out = new JsonArray();
		out.add("<h4>" + packageName.replace(".", ". ") + "</h4>");
		out.add("<h4>" + sdf.format(resultdate) + "</h4>");
		
		List<State> phonesToShow = new ArrayList<>();
		String inAppGroups = "<h4>";
		String notInAppGroups = "<h4>";
		
		//show all involved groups
		for(int k = 0; k < groups.size(); k++) {
			inAppGroups += "(" + groups.get(k) + " <a style=\"color: red;\" href='#' onclick=undeployAppOnGroup('" + packageName + "','" + groups.get(k) + "')>Undeploy</a>), ";
			for(Group group : allGroups) {
				if(group.getName().equals(groups.get(k))) {
					for(int i = 0; i < smartphones.size(); i++) {
						if(group.getImeis().contains(smartphones.get(i).getImei())) {
							phonesToShow.add(smartphones.get(i));
						}
					}
				}
			}
		}
		
		//Now print all remaining groups
		for(int j = 0; j < allGroups.size(); j++) {			
			if(!groups.contains(allGroups.get(j).getName())) {
				notInAppGroups += "(" + allGroups.get(j).getName() + 
						" <a style=\"color: green;\" href='#' onclick=deployAppOnGroup('" + packageName + "','" + allGroups.get(j).getName()+ "')>Deploy</a>), ";
			}
		}
		
		if(inAppGroups.length()>4) inAppGroups = inAppGroups.substring(0, inAppGroups.length()-2);
		inAppGroups += "</h4>";
		out.add(inAppGroups);
		
		if(notInAppGroups.length()>4) notInAppGroups = notInAppGroups.substring(0, notInAppGroups.length()-2);
		notInAppGroups += "</h4>";
		out.add(notInAppGroups);
		
		//Print all smartphones, which aren't belonging to any group
		for(State phone : smartphones) {
			boolean phoneContainsToAnyGroup = false;
			for(String groupString : groups) {
				for (Group group : allGroups) {
					if(group.getName().equals(groupString) && group.getImeis().contains(phone.getImei())) 
						phoneContainsToAnyGroup = true;
				}
			}
			if(!phoneContainsToAnyGroup) phonesToShow.add(phone);
		}
		
		//add a own table for all involved phones
		String phonesTable = "<table width=\"100%\"> <thead><tr><th>Phone</th><th>Last Restart</th><th>Status</th></tr></thead><tbody>";
		for(State state : phonesToShow) {
			resultdate = new Date(state.getLastRestart());
			//get the right name of this phone
			String phoneName = "";
			for(Smartphone phone : allPhones) {
				if(phone.getImei().equals(state.getImei())) {
					phoneName = phone.getRealName();
					break;
				}
			}
			phonesTable += "<tr><td>" + phoneName + "</td><td>" + sdf.format(resultdate) + "</td><td>" + state.getState() + "</td></tr>";
		}
		phonesTable += "</tbody></table>";
		out.add(phonesTable);
		out.add("<input type=\"button\" class=\"btn btn-default\" value=\"Change Config\" onclick=\"changeConfig('" + packageName + "','" + config + "')\"/>");
		return out;
	}
}
