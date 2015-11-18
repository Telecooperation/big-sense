package de.orolle.bigsense.server.devicemgmt;

import java.util.List;

import de.orolle.bigsense.server.database.MySQL;

/**
 * The Class VersionManagement.
 */
public class VersionManagement {
	
	/** The version management. */
	private static VersionManagement versionManagement;
	
	/**
	 * Gets the single instance of VersionManagement.
	 *
	 * @return single instance of VersionManagement
	 */
	public static VersionManagement getInstance() {
		if(versionManagement == null) versionManagement = new VersionManagement();
		return versionManagement;
	}
	
	/** The smartphones. */
	private List<Smartphone> smartphones;
	
	/** The apps. */
	private List<AppVersion> apps;
	
	/** The groups. */
	private List<Group> groups;
	
	/** SQL Connection */
	private MySQL sqlConnection;
	
	/**
	 * Instantiates a new version management.
	 */
	public VersionManagement() {
		this.sqlConnection = MySQL.getInstance();
		
		smartphones = sqlConnection.getAllPhones();
		apps = sqlConnection.getAllAppVersions();
		groups = sqlConnection.getAllGroups();
	}
	
	/**
	 * Save.
	 */
	public void save() {
		sqlConnection.setAllPhones(smartphones);
		smartphones = sqlConnection.getAllPhones();
		
		sqlConnection.setAllAppVersions(apps);
		apps = sqlConnection.getAllAppVersions();
		
		sqlConnection.setAllGroups(groups);
		groups = sqlConnection.getAllGroups();
	}

	/**
	 * Gets the smartphones.
	 *
	 * @return the smartphones
	 */
	public List<Smartphone> getSmartphones() {
		return smartphones;
	}

	/**
	 * Sets the smartphones.
	 *
	 * @param smartphones the new smartphones
	 */
	public void setSmartphones(List<Smartphone> smartphones) {
		this.smartphones = smartphones;
		save();
	}

	/**
	 * Gets the apps.
	 *
	 * @return the apps
	 */
	public List<AppVersion> getApps() {
		return apps;
	}

	/**
	 * Sets the apps.
	 *
	 * @param apps the new apps
	 */
	public void setApps(List<AppVersion> apps) {
		this.apps = apps;
		save();
	}
	
	/**
	 * Gets the groups.
	 *
	 * @return the groups
	 */
	public List<Group> getGroups() {
		return groups;
	}

	/**
	 * Sets the groups.
	 *
	 * @param groups the new groups
	 */
	public void setGroups(List<Group> groups) {
		this.groups = groups;
		save();
	}
}
