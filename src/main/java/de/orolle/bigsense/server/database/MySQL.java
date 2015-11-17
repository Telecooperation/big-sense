package de.orolle.bigsense.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.vertx.java.core.json.JsonArray;

import de.orolle.bigsense.server.devicemgmt.AppVersion;
import de.orolle.bigsense.server.devicemgmt.Group;
import de.orolle.bigsense.server.devicemgmt.Smartphone;
import de.orolle.bigsense.server.devicemgmt.SmartphoneState;
import de.orolle.bigsense.server.devicemgmt.State;

public class MySQL {
	
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	private static String user;
	private static String password;
	private static MySQL instance;

	/**
	 * Singleton for this SQLServer-Connection
	 * @return
	 */
	public static MySQL getInstance() {
		if(instance == null) {
			instance = new MySQL();
		}
		return instance;
	}
	
	public MySQL() {
		if(user != null && password != null && user.length() > 0) connect();
	}
	
	/**
	 * Sets credentials to connect to database
	 * @param user user-account
	 * @param password password for user-account
	 */
	public static void setUser(String user, String password) {
		MySQL.user = user;
		MySQL.password = password;
	}
	

	/**
	 * Starts the connection to the MySQL database
	 */
	private void connect() {
	    try {
	    	// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
		    connect = DriverManager.getConnection("jdbc:mysql://localhost/BigSense?" + "user=" + user + "&password=" + password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private void close() {
		try {
			if (resultSet != null) {
		        resultSet.close();
		    }
	
		    if (statement != null) {
		        statement.close();
		    }
		 } catch (Exception e) {
		 }
	}
	
	
	/**
	 * Retrieves all saved phones from database 
	 * @return all phones together in one ArrayList
	 */
	public List<Smartphone> getAllPhones() {
		ArrayList<Smartphone> out = new ArrayList<>();
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
			resultSet = statement.executeQuery("Select * from BigSense.Phones");
			while (resultSet.next()) {
				out.add(new Smartphone(resultSet.getString("imei"), 
						Long.parseLong(resultSet.getString("lastcontact")), 
						resultSet.getString("realname"), 
						resultSet.getInt("batterylevel"), 
						Float.valueOf(resultSet.getString("batterytemperature"))));
			 }
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return out;
	}
	
	/**
	 * Sets the given phones in db; updates existing ones, adding new ones and deletes old ones
	 * @param phones
	 */
	public void setAllPhones(List<Smartphone> phones) {
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
		    
		    for(int i = 0; i < phones.size(); i++) {
		    	resultSet = statement.executeQuery("Select * from BigSense.Phones Where imei='" + phones.get(i).getImei() + "'");
		    	if(resultSet.next()) { //this phone is already in db, only update 
		    	    preparedStatement = connect.prepareStatement("Update BigSense.Phones SET lastcontact=?, realname=?, batterylevel=?, batterytemperature=? Where imei=" + phones.get(i).getImei());
		    	    preparedStatement.setString(1, "" + phones.get(i).getLastContact());
		    	    preparedStatement.setString(2, phones.get(i).getRealName());
		    	    preparedStatement.setInt(3, phones.get(i).getBatteryLevel());
		    	    preparedStatement.setString(4, "" + phones.get(i).getBatteryTemperature());
		    	    preparedStatement.executeUpdate();
		    	}
		    	else { //insert new one
		    	    preparedStatement = connect.prepareStatement("Insert into BigSense.Phones values (default, ?, ?, ?, ?, ?)");
		    	    preparedStatement.setString(1, phones.get(i).getImei());
		    	    preparedStatement.setString(2, "" + phones.get(i).getLastContact());
		    	    preparedStatement.setString(3, phones.get(i).getRealName());
		    	    preparedStatement.setInt(4, phones.get(i).getBatteryLevel());
		    	    preparedStatement.setString(5, "" + phones.get(i).getBatteryTemperature());
		    	    preparedStatement.executeUpdate();
		    	}
			}
		    
		    //now delete all phones in db, which aren't existing in program anymore
		    resultSet = statement.executeQuery("Select * from BigSense.Phones");
			while (resultSet.next()) {
				boolean delete = true;
				String imei = resultSet.getString("imei");
				for(int i = 0; i < phones.size(); i++) {
					if(phones.get(i).getImei().equals(imei)) {
						delete = false;
						break;
					}
				}
				if(delete) {
					preparedStatement = connect.prepareStatement("Delete from BigSense.Phones Where imei=" + imei);
					preparedStatement.executeUpdate();
				}
			 }
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	/**
	 * Searches all groups and its corresponding phones in db
	 * @return
	 */
	public List<Group> getAllGroups() {
		ArrayList<Group> out = new ArrayList<>();
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
			resultSet = statement.executeQuery("Select * from BigSense.Groups");
			ArrayList<Integer> groupIDs = new ArrayList<>();
			while (resultSet.next()) {
				groupIDs.add(resultSet.getInt("ID"));
			}
			for(int i = 0; i < groupIDs.size(); i++) {
				//get name of this group
				resultSet = statement.executeQuery("Select name from BigSense.Groups Where ID='" + groupIDs.get(i) + "'");
				resultSet.next();
				String name = resultSet.getString("name");
				
				//get all phones of this group
				ArrayList<String> phonesImeis = new ArrayList<>();
				statement = connect.createStatement();
				resultSet = statement.executeQuery("Select P.imei From Phones P, Group_Phones GP Where P.ID = GP.PhoneID AND GP.GroupID = '" + groupIDs.get(i) + "'");
				while (resultSet.next()) {
					phonesImeis.add(resultSet.getString("imei"));
				}
				out.add(new Group(groupIDs.get(i), name, phonesImeis)); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return out;
	}
	
	/**
	 * Sets the given groups in db; updates existing ones, adding new ones and deletes old ones
	 * @param phones
	 */
	public void setAllGroups(List<Group> groups) {
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
		    
		    for(int i = 0; i < groups.size(); i++) {
		    	resultSet = statement.executeQuery("Select * from BigSense.Groups Where ID='" + groups.get(i).getID() + "'");
		    	if(resultSet.next()) { //this group is already in db, only update 
		    	    preparedStatement = connect.prepareStatement("Update BigSense.Groups SET name=? Where ID=" + groups.get(i).getID());
		    	    preparedStatement.setString(1, groups.get(i).getName());
		    	    preparedStatement.executeUpdate();
		    	    
		    	    //now delete all group-connected phones and add the new ones
		    	    preparedStatement = connect.prepareStatement("Delete from BigSense.Group_Phones Where GroupID=" + groups.get(i).getID());
					preparedStatement.executeUpdate();
					
		    	    for(int j = 0; j < groups.get(i).getImeis().size(); j++) {
		    	    	resultSet = statement.executeQuery("Select ID from BigSense.Phones Where imei='" + groups.get(i).getImeis().get(j) + "'");
			    	    if(resultSet.next()) {
				    	    String dbPhoneID = resultSet.getString("ID");
			    	    	
			    	    	preparedStatement = connect.prepareStatement("Insert into BigSense.Group_Phones values (default, ?, ?)");
				    	    preparedStatement.setString(1, "" + groups.get(i).getID());
				    	    preparedStatement.setString(2, dbPhoneID);
				    	    preparedStatement.executeUpdate();
			    	    }
		    	    }
		    	}
		    	else { //insert new one
		    	    preparedStatement = connect.prepareStatement("Insert into BigSense.Groups values (default, ?)");
		    	    preparedStatement.setString(1, groups.get(i).getName());
		    	    preparedStatement.executeUpdate();
		    	    
		    	    resultSet = statement.executeQuery("Select ID from BigSense.Groups Where name='" + groups.get(i).getName() + "'");
		    	    resultSet.next();
		    	    String dbGroupID = resultSet.getString("ID");
		    	    for(int j = 0; j < groups.get(i).getImeis().size(); j++) {
		    	    	//get dbID of phone
		    	    	resultSet = statement.executeQuery("Select ID from BigSense.Phones Where imei='" + groups.get(i).getImeis().get(j) + "'");
			    	    if(resultSet.next()) {
				    	    String dbPhoneID = resultSet.getString("ID");
			    	    	
			    	    	preparedStatement = connect.prepareStatement("Insert into BigSense.Group_Phones values (default, ?, ?)");
				    	    preparedStatement.setString(1, dbGroupID);
				    	    preparedStatement.setString(2, dbPhoneID);
				    	    preparedStatement.executeUpdate();
			    	    }
		    	    }
		    	}
			}
		    
		    //now delete all groups in db, which aren't existing in program anymore
		    resultSet = statement.executeQuery("Select * from BigSense.Groups");
			while (resultSet.next()) {
				boolean delete = true;
				int id = resultSet.getInt("ID");
				String name = resultSet.getString("name");
				for(int i = 0; i < groups.size(); i++) {
					if(groups.get(i).getName().equals(name)) {
						delete = false;
						break;
					}
				}
				if(delete) {
					preparedStatement = connect.prepareStatement("Delete from BigSense.Groups Where ID=" + id);
					preparedStatement.executeUpdate();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	/**
	 * Retrieves all saved appversions from database 
	 * @return all apps together in one ArrayList
	 */
	public List<AppVersion> getAllAppVersions() {
		ArrayList<AppVersion> out = new ArrayList<>();
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
			resultSet = statement.executeQuery("Select * from BigSense.AppVersion");
			ArrayList<Integer> appIDs = new ArrayList<>();
			while (resultSet.next()) {
				appIDs.add(resultSet.getInt("ID"));
			}
			for(int i = 0; i < appIDs.size(); i++) {
				//get name and lastChange of this app
				resultSet = statement.executeQuery("Select packagename, filename, config, lastchange from BigSense.AppVersion Where ID='" + appIDs.get(i) + "'");
				resultSet.next();
				String packagename = resultSet.getString("packagename");
				String filename = resultSet.getString("filename");
				String config = resultSet.getString("config");
				long lastChange = resultSet.getLong("lastchange");
				
				//get all phones, which run this app
				ArrayList<State> phoneStates = new ArrayList<>();
				statement = connect.createStatement();
				resultSet = statement.executeQuery("Select imei, lastrestart, state From AppPhoneStates Where AVID = '" + appIDs.get(i) + "'");
				while (resultSet.next()) {
					phoneStates.add(new State(resultSet.getString("imei"), 
							SmartphoneState.valueOf(resultSet.getInt("state")), 
							resultSet.getLong("lastrestart")));
				}
				
				//get all groups, which run this app
				ArrayList<String> groupNames = new ArrayList<>();
				statement = connect.createStatement();
				resultSet = statement.executeQuery("Select G.name From Groups G, AppVersion_Groups AVG Where G.ID = AVG.GroupID AND AVG.AVID='" + appIDs.get(i) + "'");
				while (resultSet.next()) {
					groupNames.add(resultSet.getString("name"));
				}
				
				out.add(new AppVersion(appIDs.get(i), packagename, filename, config, lastChange, phoneStates, groupNames)); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return out;
	}
	
	/**
	 * Sets the given apps in db; updates existing ones, adding new ones and deletes old ones
	 * @param apps
	 */
	public void setAllAppVersions(List<AppVersion> apps) {
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
		    
		    for(int i = 0; i < apps.size(); i++) {
		    	resultSet = statement.executeQuery("Select * from BigSense.AppVersion Where ID='" + apps.get(i).getID() + "'");
		    	if(resultSet.next()) { //this app is already in db, only update 
		    	    preparedStatement = connect.prepareStatement("Update BigSense.AppVersion SET packagename=?, lastchange=?, filename=?, config=? Where ID=" + apps.get(i).getID());
		    	    preparedStatement.setString(1, apps.get(i).getPackageName());
		    	    preparedStatement.setLong(2, apps.get(i).getLastChange());
		    	    preparedStatement.setString(3, apps.get(i).getFileName());
		    	    preparedStatement.setString(4, apps.get(i).getConfig());
		    	    preparedStatement.executeUpdate();
		    	    
		    	    //now delete all app-connected groups and add the new ones
		    	    preparedStatement = connect.prepareStatement("Delete from BigSense.AppVersion_Groups Where AVID=" + apps.get(i).getID());
					preparedStatement.executeUpdate();
					
		    	    for(int j = 0; j < apps.get(i).getGroups().size(); j++) {
		    	    	resultSet = statement.executeQuery("Select ID from BigSense.Groups Where name='" + apps.get(i).getGroups().get(j) + "'");
			    	    if(resultSet.next()) {
				    	    String dbGroupID = resultSet.getString("ID");
			    	    	
			    	    	preparedStatement = connect.prepareStatement("Insert into BigSense.AppVersion_Groups values (default, ?, ?)");
				    	    preparedStatement.setString(1, "" + apps.get(i).getID());
				    	    preparedStatement.setString(2, dbGroupID);
				    	    preparedStatement.executeUpdate();
			    	    }
		    	    }
		    	    
		    	    //now delete all app-connected phones and add the new ones
		    	    preparedStatement = connect.prepareStatement("Delete from BigSense.AppPhoneStates Where AVID=" + apps.get(i).getID());
					preparedStatement.executeUpdate();
					
		    	    for(int j = 0; j < apps.get(i).getSmartphones().size(); j++) {
		    	    	preparedStatement = connect.prepareStatement("Insert into BigSense.AppPhoneStates values (default, ?, ?, ?, ?)");
			    	    preparedStatement.setString(1, "" + apps.get(i).getID());
			    	    preparedStatement.setString(2, apps.get(i).getSmartphones().get(j).getImei());
			    	    preparedStatement.setString(3, "" + apps.get(i).getSmartphones().get(j).getLastRestart());
			    	    preparedStatement.setInt(4, apps.get(i).getSmartphones().get(j).getState().getValue());
			    	    preparedStatement.executeUpdate();
		    	    }
		    	}
		    	else { //insert new one
		    	    preparedStatement = connect.prepareStatement("Insert into BigSense.AppVersion values (default, ?, ?, ?, ?)");
		    	    preparedStatement.setString(1, apps.get(i).getPackageName());
		    	    preparedStatement.setString(2, "" + apps.get(i).getLastChange());
		    	    preparedStatement.setString(3, apps.get(i).getFileName());
		    	    preparedStatement.setString(4, apps.get(i).getConfig());
		    	    preparedStatement.executeUpdate();
		    	    
		    	    resultSet = statement.executeQuery("Select ID from BigSense.AppVersion Where packagename='" + apps.get(i).getPackageName() + "'");
		    	    resultSet.next();
		    	    String dbAppID = resultSet.getString("ID");
		    	    
		    	    //Add phonestates
		    	    for(int j = 0; j < apps.get(i).getSmartphones().size(); j++) {
		    	    	preparedStatement = connect.prepareStatement("Insert into BigSense.AppVersion_Groups values (default, ?, ?, ?, ?)");
			    	    preparedStatement.setString(1, "" + apps.get(i).getID());
			    	    preparedStatement.setString(2, apps.get(i).getSmartphones().get(j).getImei());
			    	    preparedStatement.setString(3, "" + apps.get(i).getSmartphones().get(j).getLastRestart());
			    	    preparedStatement.setInt(4, apps.get(i).getSmartphones().get(j).getState().getValue());
			    	    preparedStatement.executeUpdate();
		    	    }
		    	    
		    	    //Add group-connections
		    	    for(int j = 0; j < apps.get(i).getGroups().size(); j++) {
		    	    	resultSet = statement.executeQuery("Select ID from BigSense.Groups Where name='" + apps.get(i).getGroups().get(j) + "'");
			    	    resultSet.next();
			    	    String dbGroupID = resultSet.getString("ID");
		    	    	
		    	    	preparedStatement = connect.prepareStatement("Insert into BigSense.AppVersion_Groups values (default, ?, ?)");
			    	    preparedStatement.setString(1, dbAppID);
			    	    preparedStatement.setString(2, dbGroupID);
			    	    preparedStatement.executeUpdate();
		    	    }
		    	}
			}
		    
		    //now delete all apps in db, which aren't existing in program anymore
		    resultSet = statement.executeQuery("Select * from BigSense.AppVersion");
			while (resultSet.next()) {
				boolean delete = true;
				int id = resultSet.getInt("ID");
				String packagename = resultSet.getString("packagename");
				for(int i = 0; i < apps.size(); i++) {
					if(apps.get(i).getPackageName().equals(packagename)) {
						delete = false;
						break;
					}
				}
				if(delete) {
					preparedStatement = connect.prepareStatement("Delete from BigSense.AppVersion Where ID=" + id);
					preparedStatement.executeUpdate();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	/**
	 * Stores the log entry together with phoneID and timestamp in db
	 * @param imei
	 * @param log
	 */
	public void addLog(String imei, String log) {
		Date now = new Date();
		
		try {
			//get dbID of phone
			statement = connect.createStatement();
	    	resultSet = statement.executeQuery("Select ID from BigSense.Phones Where imei='" + imei + "'");
		    resultSet.next();
		    String dbPhoneID = resultSet.getString("ID");
	    	
	    	preparedStatement = connect.prepareStatement("Insert into BigSense.Log_Connections values (default, ?, ?, ?)");
		    preparedStatement.setString(1, dbPhoneID);
		    preparedStatement.setString(2, "" + now.getTime());
		    preparedStatement.setString(3, log);
		    preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	public JsonArray getLogs() {
		JsonArray out = new JsonArray();
		try {
			// Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
			resultSet = statement.executeQuery("Select timestamp, log from BigSense.Log_Connections Order By ID DESC Limit 10");
			while (resultSet.next()) {
				JsonArray oneLog = new JsonArray();
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");    
				sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				Date resultdate = new Date(Long.parseLong(resultSet.getString("timestamp")));
				oneLog.add(sdf.format(resultdate));
				oneLog.add(resultSet.getString("log"));
				out.add(oneLog);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return out;
	}
}