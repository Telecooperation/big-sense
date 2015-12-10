package de.orolle.bigsense.server.webui;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Base64;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.orolle.bigsense.server.StartCloud;
import de.orolle.bigsense.server.database.MySQL;
import de.orolle.bigsense.server.devicemgmt.AppVersion;
import de.orolle.bigsense.server.devicemgmt.Group;
import de.orolle.bigsense.server.devicemgmt.Smartphone;
import de.orolle.bigsense.server.devicemgmt.SmartphoneState;
import de.orolle.bigsense.server.devicemgmt.State;
import de.orolle.bigsense.server.devicemgmt.VersionManagement;

/**
 * Manages APK upload and delete for web-interface.
 * Notifies web-interfaces about apk changes.
 * 
 * @author Oliver Rolle, Martin Hellwig
 *
 */
public class APKManager {
	
	/** Vertx instance. */
	public final Vertx vertx;
	
	/** Folder in which app-apks are stored. */
	public final String apkFolder;

	/** Cache for app upload from web ui. */
	private final HashMap<String, String[]> files = new HashMap<>();
	
	/** Timer to cleanup the cache. */
	private final HashMap<String, Long> timers = new HashMap<>();
	
	/** The version mgmt. */
	private VersionManagement versionMgmt;

	/**
	 * Constructs APKManager.
	 *
	 * @param v 	Vert.x instance for FileSystem and Eventbus API
	 * @param a 	Folder to store APKs in
	 */
	public APKManager(Vertx v, String a) {
		super();
		this.vertx = v;
		this.apkFolder = a;
		this.versionMgmt = VersionManagement.getInstance();

		/*
		 * User uploads apk
		 */
		vertx.eventBus().registerHandler("web.in.apkupload", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				final String filename = msg.body().getString("filename", null);
				int part = msg.body().getInteger("part", -1);
				int totalParts = msg.body().getInteger("parts", -1);
				String base64 = msg.body().getString("base64", null);

				if(!filename.endsWith(".apk"))
					return;

				if(files.get(filename)==null) {
					files.put(filename, new String[totalParts]);
				}

				if(timers.get(filename)==null) {
					long id = vertx.setTimer(5*60*1000, new Handler<Long>() { // Upload is cleaned up after 5 minutes
						@Override
						public void handle(Long event) {
							files.remove(filename);
							timers.remove(filename);
						}
					});
					timers.put(filename, id);
				}

				files.get(filename)[part] = base64;

				if(isTotal(files.get(filename))) {
					writeFile(filename);
				}
			}
		});

		/*
		 * Update web-interface on a browser connection
		 */
		vertx.eventBus().registerHandler("web.in.refresh", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				notifyWeb();
			}
		});

		//Get all events from web interface
		vertx.eventBus().registerHandler("web.in.apk", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				// Delete apk
				if(msg.body().getString("action", "").equals("delete")) {
					String name = msg.body().getString("filename", "");
					if(name.endsWith(".apk")) {					
						//The app is now removed, so set states of all phones to UNINSTALL
						//If there are no devices left, delete this app from app-list
						String packageName = readApkPackage(apkFolder+""+name);
						List<AppVersion> newApps = new ArrayList<>();
						for(AppVersion app : versionMgmt.getApps()) {
							if(app.getPackageName().equals(packageName)) {
								List<State> phones = new ArrayList<>();
								for(State phone : app.getSmartphones()) {
									if(!phone.getState().equals(SmartphoneState.INSTALL)) 
										phones.add(new State(phone.getImei(), SmartphoneState.UNINSTALL, phone.getLastRestart()));
								}
								app.setSmartphones(phones);
								app.setGroups(new ArrayList<String>());
								if(app.getSmartphones().size() > 0) {
									newApps.add(app);
								}
								else {
									vertx.fileSystem().deleteSync(apkFolder+name);
								}
							}
							else newApps.add(app);
						}
						versionMgmt.setApps(newApps);
						
						StartCloud.log.info("APK remove: "+apkFolder+name);
						
						apkFilesChanged();
					}
				}
				//Deploy App on group
				if(msg.body().getString("action", "").equals("deploy")) {
					String appPackage = msg.body().getString("package", "");
					String groupName = msg.body().getString("group", "");
					
					List<AppVersion> newApps = new ArrayList<>();
					for(AppVersion app : versionMgmt.getApps()) {
						if(app.getPackageName().equals(appPackage)) {
							List<String> groups = app.getGroups();
							groups.add(groupName);
							
							for(Group group : versionMgmt.getGroups()) {
								if(group.getName().equals(groupName)) {
									List<State> newPhones = new ArrayList<>();
									for(State phone : app.getSmartphones()) {
										if(!group.getImeis().contains(phone.getImei())) {
											newPhones.add(phone);
										} 
										else newPhones.add(new State(phone.getImei(), SmartphoneState.UPDATE, System.currentTimeMillis()));
									}
									for(String imei : group.getImeis()) {
										boolean isAlreadyInList = false;
										for(State phone : app.getSmartphones()) {
											if(phone.getImei().equals(imei)) isAlreadyInList = true;
										}
										if(!isAlreadyInList) newPhones.add(new State(imei, SmartphoneState.INSTALL, System.currentTimeMillis()));
									}
									app.setSmartphones(newPhones);
								}
							}
							
							newApps.add(app);
						}
						else newApps.add(app);
					}
					versionMgmt.setApps(newApps);
					
					notifyWeb();
				}
				//undeploy app on group
				if(msg.body().getString("action", "").equals("undeploy")) {
					String appPackage = msg.body().getString("package", "");
					String groupName = msg.body().getString("group", "");
					
					List<AppVersion> newApps = new ArrayList<>();
					for(AppVersion app : versionMgmt.getApps()) {
						if(app.getPackageName().equals(appPackage)) {
							List<String> groups = app.getGroups();
							groups.remove(groupName);
							
							for(Group group : versionMgmt.getGroups()) {
								if(group.getName().equals(groupName)) {
									List<State> phones = new ArrayList<>();
									for(State phone : app.getSmartphones()) {
										if(group.getImeis().contains(phone.getImei())) {
											if(phone.getState() == SmartphoneState.UNINSTALL || 
													phone.getState() == SmartphoneState.UPDATE || 
													phone.getState() == SmartphoneState.UPTODATE)
												phones.add(new State(phone.getImei(), SmartphoneState.UNINSTALL, phone.getLastRestart()));
										}
										else phones.add(phone);
									}
									newApps.add(new AppVersion(app.getID(), appPackage, app.getFileName(), app.getConfig(), app.getLastChange(), phones, groups));
								}
							}							
						}
						else newApps.add(app);
					}
					versionMgmt.setApps(newApps);
					
					notifyWeb();
				}
				//add phone to a group
				if(msg.body().getString("action", "").equals("addPhoneToGroup")) {
					String groupName = msg.body().getString("group", "");
					String imei = msg.body().getString("imei", "");
					
					List<Group> newGroups = new ArrayList<>();
					for(Group group : versionMgmt.getGroups()) {
						if(group.getName().equals(groupName)) {
							List<String> imeis = group.getImeis();
							imeis.add(imei);
							newGroups.add(group);
						}
						else newGroups.add(group);
					}
					versionMgmt.setGroups(newGroups);
					
					List<AppVersion> newApps = new ArrayList<>();
					for(AppVersion app : versionMgmt.getApps()) {
						if(app.getGroups().contains(groupName)) {
							List<State> newPhones = new ArrayList<>();
							boolean foundPhoneAlready = false;
							for(State phone : app.getSmartphones()) {
								if(phone.getImei().equals(imei)) {
									newPhones.add(new State(imei, SmartphoneState.UPDATE, System.currentTimeMillis()));
									foundPhoneAlready = true;
								}
								else newPhones.add(phone);
							}
							
							if(!foundPhoneAlready) newPhones.add(new State(imei, SmartphoneState.INSTALL, System.currentTimeMillis()));
							app.setSmartphones(newPhones);
							newApps.add(app);
						}
						else newApps.add(app);
					}
					versionMgmt.setApps(newApps);
					
					notifyWeb();
				}
				//delete phone from a group
				if(msg.body().getString("action", "").equals("deletePhoneFromGroup")) {
					String groupName = msg.body().getString("group", "");
					String imei = msg.body().getString("imei", "");
					
					List<Group> newGroups = new ArrayList<>();
					for(Group group : versionMgmt.getGroups()) {
						if(group.getName().equals(groupName)) {
							List<String> imeis = new ArrayList<>();
							for(String oldImei : group.getImeis()) {
								if(!oldImei.equals(imei)) imeis.add(oldImei);
							}
							newGroups.add(new Group(group.getID(), group.getName(), imeis));
						}
						else newGroups.add(group);
					}
					versionMgmt.setGroups(newGroups);
					
					List<AppVersion> newApps = new ArrayList<>();
					for(AppVersion app : versionMgmt.getApps()) {
						if(app.getGroups().contains(groupName)) {
							List<State> newPhones = new ArrayList<>();
							for(State phone : app.getSmartphones()) {
								if(phone.getImei().equals(imei)) {
									if(phone.getState() != SmartphoneState.INSTALL) newPhones.add(new State(imei, SmartphoneState.UNINSTALL, System.currentTimeMillis()));
								}
								else newPhones.add(phone);
							}
							app.setSmartphones(newPhones);
							newApps.add(app);
						}
						else newApps.add(app);
					}
					versionMgmt.setApps(newApps);
					
					notifyWeb();
				}
				//delete group
				if(msg.body().getString("action", "").equals("deleteGroup")) {
					String groupName = msg.body().getString("group", "");
					
					List<AppVersion> newApps = new ArrayList<>();
					for(AppVersion app : versionMgmt.getApps()) {
						if(app.getGroups().contains(groupName)) {
							List<State> newPhones = new ArrayList<>();
							for(State phone : app.getSmartphones()) {
								boolean foundPhoneInDeletingGroup = false;
								for(Group group : versionMgmt.getGroups()) {
									if(group.getName().equals(groupName) && group.getImeis().contains(phone.getImei())) {
										foundPhoneInDeletingGroup = true;
										if(phone.getState() != SmartphoneState.INSTALL) newPhones.add(new State(phone.getImei(), 
												SmartphoneState.UNINSTALL, 
												System.currentTimeMillis()));
									}
								}
								if(!foundPhoneInDeletingGroup) newPhones.add(phone);
							}
							app.setSmartphones(newPhones);
							List<String> groups = app.getGroups();
							groups.remove(groupName);
							newApps.add(app);
						}
						else newApps.add(app);
					}
					versionMgmt.setApps(newApps);
					
					List<Group> newGroups = new ArrayList<>();
					for(Group group : versionMgmt.getGroups()) {
						if(!group.getName().equals(groupName)) {
							newGroups.add(group);
						}
					}
					versionMgmt.setGroups(newGroups);
					
					notifyWeb();
				}
				//add new group
				if(msg.body().getString("action", "").equals("addGroup")) {
					String groupName = msg.body().getString("group", "");
					
					//Check if no group with this name already exists
					List<Group> newGroups = versionMgmt.getGroups();
					boolean nameAlreadyExists = false;
					for(Group group : newGroups) {
						if(group.getName().equals(groupName)) nameAlreadyExists = true;
					}
					if(!nameAlreadyExists && 
							groupName.length() > 0 &&
							!groupName.contains(";") &&
							!groupName.contains(":")) newGroups.add(new Group(0, groupName, new ArrayList<String>()));
					versionMgmt.setGroups(newGroups);
					
					notifyWeb();
				}
				//change name of a phone
				if(msg.body().getString("action", "").equals("changePhoneName")) {
					String imei = msg.body().getString("imei", "");
					String name = msg.body().getString("name", "");
					
					List<Smartphone> oldPhones = versionMgmt.getSmartphones();
					List<Smartphone> newPhones = new ArrayList<>();
					for(Smartphone phone : oldPhones) {
						if(phone.getImei().equals(imei)) {
							Smartphone tempPhone = phone;
							tempPhone.setRealName(name);
							newPhones.add(tempPhone);
						}
						else newPhones.add(phone);
					}
					versionMgmt.setSmartphones(newPhones);
					
					notifyWeb();
				}
				//delete a phone
				if(msg.body().getString("action", "").equals("deletePhone")) {
					String imei = msg.body().getString("imei", "");
					
					List<Smartphone> oldPhones = versionMgmt.getSmartphones();
					List<Smartphone> newPhones = new ArrayList<>();
					for(Smartphone phone : oldPhones) {
						if(!phone.getImei().equals(imei)) newPhones.add(phone);
					}
					versionMgmt.setSmartphones(newPhones);
					
					List<AppVersion> oldApps = versionMgmt.getApps();
					List<AppVersion> newApps = new ArrayList<>();
					for (AppVersion app : oldApps) {
						boolean changePhones = false;
						int removeIndex = 0;
						List<State> imeis = app.getSmartphones();
						for(int i = 0; i < imeis.size(); i++) {
							if(imeis.get(i).getImei().equals(imei)) {
								changePhones = true;
								removeIndex = i;
								break;
							}
						}
						if(changePhones) {
							imeis.remove(removeIndex);
							app.setSmartphones(imeis);
							newApps.add(app);
						}
						else newApps.add(app);
					}
					versionMgmt.setApps(newApps);
					
					notifyWeb();
				}
				//change config of an app
				if(msg.body().getString("action", "").equals("changeConfig")) {
					String packageName = msg.body().getString("packageName", "");
					String config = msg.body().getString("config", "{}");
					
					//set all involved phones to status "Update" to use the new config
					List<AppVersion> oldApps = versionMgmt.getApps();
					List<AppVersion> newApps = new ArrayList<>();
					for (AppVersion app : oldApps) {
						if(app.getPackageName().equals(packageName)) {
							AppVersion tempApp = app;
							tempApp.setConfig(config);
							Date now = new Date();
							tempApp.setLastChange(now.getTime());
							ArrayList<State> newPhones = new ArrayList<>();
							for(State phone : tempApp.getSmartphones()) {
								if(phone.getState().equals(SmartphoneState.UPTODATE)) {
									newPhones.add(new State(phone.getImei(), SmartphoneState.UPDATE, phone.getLastRestart()));
								}
								else newPhones.add(phone);
							}
							tempApp.setSmartphones(newPhones);
							newApps.add(tempApp);
						}
						else {
							newApps.add(app);
						}
					}
					versionMgmt.setApps(newApps);
					
					notifyWeb();
				}
			}
		});
	}

	/**
	 * Reads the App package name (the name space of the apk).
	 * @param apk
	 * 	APK file
	 * @return
	 * 	Package name
	 */
	private String readApkPackage(String apk) {
		try {
			String raw = DecodeAndroidManifest.extractManifest(apk);
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(raw)));
			String appName = d.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getNodeValue();
			return appName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Handles changes of APKs. Notifies Update Process than web-interface.
	 */
	private void apkFilesChanged() {
		vertx.fileSystem().mkdirSync(apkFolder, true);

		notifyWeb();
	}

	/**
	 * Notify web-interface about current apks.
	 */
	private void notifyWeb() {
		// Notify about files
		JsonArray files = new JsonArray();
		for (AppVersion app : versionMgmt.getApps()) {
			files.add(new JsonObject().putString("filename", app.getFileName()));
		}
		vertx.eventBus().publish("web.out.apk", new JsonObject().putString("action", "files changed").putArray("files", files));

		// Notify about saved apps (not apks)
		JsonArray apps = new JsonArray();
		for (AppVersion app : versionMgmt.getApps()) {
			apps.add(app.printPrittily(versionMgmt.getGroups(), versionMgmt.getSmartphones()));
		}
		vertx.eventBus().publish("web.out.modules", new JsonObject().putArray("apps", apps));
		
		// Notify about phones
		JsonArray phones = new JsonArray();
		for (Smartphone phone : versionMgmt.getSmartphones()) {
			phones.add(phone.printPrittily());
		}
		vertx.eventBus().publish("web.out.phones", new JsonObject().putArray("phones", phones));
		
		// Notify about groups
		JsonArray groups = new JsonArray();
		for (Group group : versionMgmt.getGroups()) {
			groups.add(group.printPrittily(versionMgmt.getSmartphones(), versionMgmt.getGroups()));
		}
		vertx.eventBus().publish("web.out.groups", new JsonObject().putArray("groups", groups));
		
		// Notify about logs
		MySQL sqlConnection = MySQL.getInstance();
		JsonArray logs = sqlConnection.getLogs();
		vertx.eventBus().publish("web.out.logs", new JsonObject().putArray("logs", logs));
	}
		

	/**
	 * Writes an APK file to disk which is received via websocket encoded as base64.
	 *
	 * @param filename the filename
	 */
	private void writeFile(String filename) {
		vertx.cancelTimer(timers.remove(filename));
		String[] strings = files.remove(filename);

		StringBuffer buf = new StringBuffer();
		for (String s : strings) {
			buf.append(s);
		}

		byte[] data = Base64.decode(buf.toString());

		vertx.fileSystem().mkdirSync(apkFolder, true);

		vertx.fileSystem().writeFileSync(apkFolder+filename, new Buffer(data));

		StartCloud.log.info("APK add: "+apkFolder+filename);
		
		//check if its a real bigsense-app
		String config = DecodeAndroidManifest.extractFile(apkFolder + filename, "assets/config.json");
		String activity = DecodeAndroidManifest.extractFile(apkFolder + filename, "assets/activity.json");
		if((config != null && config != "") || (activity != null && activity.equals(""))) {
			//If this is the case, this application is an activity and has no config
			if(activity != null && activity.equals("")) config = "";
		
			String packageName = readApkPackage(apkFolder+filename);
			List<AppVersion> newApps = new ArrayList<>();
			List<AppVersion> apps = versionMgmt.getApps();
			boolean foundExistingApp = false;
			for(AppVersion app : apps) {
				if(app.getPackageName().equals(packageName)) {
					foundExistingApp = true;
					//Update app
					AppVersion tempApp = app;
					
					//delete old file, if its not already overwritten
					if(!tempApp.getFileName().equals(filename)) {
						vertx.fileSystem().deleteSync(apkFolder+tempApp.getFileName());
					}
					tempApp.setFileName(filename);
					tempApp.setLastChange(System.currentTimeMillis());
					
					List<State> phones = new ArrayList<>();
					for(State phone : app.getSmartphones()) {
						if(phone.getState() == SmartphoneState.UPTODATE) 
							phones.add(new State(phone.getImei(), SmartphoneState.UPDATE, phone.getLastRestart()));
						else phones.add(phone);
					}
					
					tempApp.setConfig(config);
					tempApp.setSmartphones(phones);
					newApps.add(tempApp);
				}
				else newApps.add(app);
			}
			
			if(!foundExistingApp) {
				newApps.add(new AppVersion(0, packageName, filename, config, System.currentTimeMillis(), new ArrayList<State>(), new ArrayList<String>()));
			}
			versionMgmt.setApps(newApps);
	
			apkFilesChanged();
		}
		else {
			//delete the file
			vertx.fileSystem().deleteSync(apkFolder+filename);
		}
	}

	/**
	 * Counts the received parts of an uploaded APK file via websocket.
	 *
	 * @param strings 	parts of the APK
	 * @return 	number of received APKs
	 */
	private boolean isTotal(String[] strings) {
		boolean ret = true;

		for (String s : strings) {
			ret &= s != null;
		}

		return ret;
	}
}
