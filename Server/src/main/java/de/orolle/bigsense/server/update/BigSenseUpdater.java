package de.orolle.bigsense.server.update;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import de.orolle.bigsense.server.Config;
import de.orolle.bigsense.server.database.MySQL;
import de.orolle.bigsense.server.devicemgmt.AppVersion;
import de.orolle.bigsense.server.devicemgmt.Smartphone;
import de.orolle.bigsense.server.devicemgmt.SmartphoneState;
import de.orolle.bigsense.server.devicemgmt.State;
import de.orolle.bigsense.server.devicemgmt.VersionManagement;
import de.orolle.bigsense.server.webui.DecodeAndroidManifest;

/**
 * Manages Update Process for Android Smartphones.
 *
 * @author Oliver Rolle, Martin Hellwig
 */
public class BigSenseUpdater {
	
	/** Vertx instance. */
	private final Vertx vertx;
	
	/** Folder in which app-apks are stored. */
	public final String apkFolder;
	
	/** Eventbus address to write commands to. */
	private final String sshWriteAddress;
	
	/** Eventbus address to read responses. */
	private final String sshReadAddress;
	
	/** List of update commands. */
	private final LinkedList<SSH> cmds = new LinkedList<>();
	
	/** IMEI of connected smart phone. */
	private String imei = "";
	
	/** battery of the phone */
	private int batteryLevel;
	
	/** Temperature of the phone */
	private float batteryTemperature;
	
	/** Indicates if the phone just started */
	private boolean justStarted;
	
	/** The version management. */
	private VersionManagement versionManagement;
	
	/** The to uninstall. */
	private List<String> toUninstall;
	
	/** The to upload. */
	private List<String> toUpload;
	
	/** The to install and start. */
	private List<String> toInstallAndStart;
	
	/** The to start. */
	private List<String> toStart;
	
	/** The to close. */
	private List<String> toClose;
	
	/** This string will be filled with logs and then stored in db */
	private String log = "";

	/**
	 * Constructs BigSenseUpdater which is responsible for update of
	 * exactly one Android Smartphone.
	 *
	 * @param vertx 	Vert.x instance
	 * @param write 	Address to write commands to the SSHClient
	 * @param read 	Address to read SSHClient responses from
	 */
	public BigSenseUpdater(final Vertx vertx, String write, String apkFolder,
			String read) {
		super();
		this.vertx = vertx;
		this.apkFolder = apkFolder;
		this.sshWriteAddress = write;
		this.sshReadAddress = read;
		
		versionManagement = VersionManagement.getInstance();

		vertx.eventBus().registerHandler(sshReadAddress, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				//System.out.println(sshReadAddress+" :: "+msg.body().encode());
				if(msg.body().getString("result","").equals("exit")) {
					vertx.eventBus().unregisterHandler(sshReadAddress, this);
				}
			}
		});
		updateLogic();
	}

	/**
	 * Builds and than execute update process.
	 */
	private void updateLogic() {
		/*
		 * BEGIN BUILDING OF SSH COMMANDs 
		 */
		// Log Device & Check Versions
		logDeviceAndCheckVersionBeforeUpdate();
		
		// Remove Big Sense Apps & APKs
		removeBigSenseModuleApps();
		
		/*
		 * END BUILDING SSH COMMANDs
		 */

		// execute ssh commands
		execute();
	}
	
	/**
	 * Log IMEI and check app versions.
	 */
	private void logDeviceAndCheckVersionBeforeUpdate() {
		//The first command is for normal devices, the second one for battery and the third one for the imei of Phones with Android 5 or higher; the forth one to find out how logn the device is alive
		cmds.add(new Command("dumpsys iphonesubinfo && dumpsys battery && cat /sdcard/Android/data/de.orolle.bigsense/files/imei && uptime && echo -e \"\\n\"", new Handler<String>() {
			@Override
			public void handle(String out) {
				Pattern p = Pattern.compile("\\d{15}");
				Matcher m = p.matcher(out);
				while (m.find()) {
					imei = m.group();
				}
				
				Pattern pattern = Pattern.compile("level:\\s(\\d{1,3})");
				Matcher matcher = pattern.matcher(out);
				while(matcher.find()) {
					batteryLevel = Integer.valueOf(matcher.group(1));
				}
				
				pattern = Pattern.compile("temperature:\\s(\\d{1,4})");
				matcher = pattern.matcher(out);
				while(matcher.find()) {
					batteryTemperature = Float.valueOf(matcher.group(1));
				}
				
				//If we not find the right pattern, the phone has a uptime more than one day
				justStarted = false;
				pattern = Pattern.compile("up\\stime:\\s(\\d{2}:\\d{2})");
				matcher = pattern.matcher(out);
				while(matcher.find()) {
					String temp = matcher.group(1);
					int hours = Integer.valueOf(temp.substring(0, 2));
					int minutes = Integer.valueOf(temp.substring(3, 5));
					if(hours == 0 && minutes < 5) justStarted = true;
				}
				
				if(imei != null && !imei.equals("")) {
					//Watch, which apps can be removed, updated etc.
					toUninstall = new ArrayList<>();
					toUpload = new ArrayList<>();
					toInstallAndStart = new ArrayList<>();
					toStart = new ArrayList<>();
					toClose = new ArrayList<>();
					for(AppVersion app : versionManagement.getApps()) {
						for(State smatphoneState : app.getSmartphones()) {
							if(smatphoneState.getImei().equals(imei)) {
								switch(smatphoneState.getState()) {
									case UNINSTALL:
										toUninstall.add(app.getPackageName());
										break;
									case INSTALL:
										toUpload.add(app.getPackageName());
										toInstallAndStart.add(app.getPackageName());
										break;
									case UPDATE:
										toUninstall.add(app.getPackageName());
										toUpload.add(app.getPackageName());
										toInstallAndStart.add(app.getPackageName());
										break;
									case UPTODATE: 
										if(justStarted || (System.currentTimeMillis() - smatphoneState.getLastRestart()) > 
												Config.RESTART_APP_INTERVAL_MILLISECOND) {
											toUninstall.add(app.getPackageName());
											toInstallAndStart.add(app.getPackageName());
										}
										break;
								}
							}
						}
					}
				}
				
				//Nothing Changed? Exit here!
				if(toUninstall.isEmpty() && toUpload.isEmpty() && 
						toInstallAndStart.isEmpty() && toClose.isEmpty() && 
						toStart.isEmpty()) {
					updateSmartphoneInfo();
					vertx.eventBus().publish("web.in.refresh", new JsonObject().putString("action", "update"));
					
					System.out.println("Newest Version already installed");
					cmds.clear();
					
					//add log to db
					MySQL sqlConnection = MySQL.getInstance();
					sqlConnection.addLog(imei, log);
					log = "";
					exit();
				}
				
				if(imei == null || imei.equals("")) {
					System.out.println("Failure while reading imei");
					cmds.clear();
					
					//add log to db
					MySQL sqlConnection = MySQL.getInstance();
					sqlConnection.addLog(imei, log);
					log = "";
					exit();
				}
				
				// Put Android Apps on Smartphone
				putApks();
				
				// Install Android Apps
				installApks();
				
				// Stop Modules 
				stopModules();
				
				// Start ModuleLoader Service of installed Apps
				startModules();
				
				// Close
				exit();
			}
		}));
	}
	
	/**
	 * Removes all BigSenseModule-Apps and creates /sdcard/BigSense on smartphone.
	 */
	private void removeBigSenseModuleApps() {
		cmds.add(new Command("date", new Handler<String>() {
			@Override
			public void handle(String event) {
				for(String packageName : toUninstall) {
					cmds.addFirst(new Command("pm uninstall "+packageName));
				}
			}
		}));

		//cmds.add(new Command("rm -rf /sdcard/BigSense"));
		cmds.add(new Command("mkdir /sdcard/BigSense"));
	}
	
	/**
	 * Put Android App Apks on the smartphones.
	 */
	private void putApks() {
		for (AppVersion app : versionManagement.getApps()) {
			try {
				String appPackageName = app.getPackageName();
				boolean contains = false;
				for(String packageName : toUpload) {
					if(packageName.equals(appPackageName)) contains = true;
				}
				if(contains) cmds.add(new Put(apkFolder+app.getFileName()));
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Install Android App on the smartphone.
	 */
	private void installApks() {
		for (AppVersion app : versionManagement.getApps()) {
			try {
				String appPackageName = app.getPackageName();
				boolean contains = false;
				for(String packageName : toInstallAndStart) {
					if(packageName.equals(appPackageName)) contains = true;
				}
				if(contains) cmds.add(new Command("pm install -r /sdcard/BigSense/"+app.getFileName()));
			}
			catch (Exception e) {
			}
		}
	}
	
	/**
	 * Stop ModuleLoader-Service on the smartphone.
	 */
	private void stopModules() {
		for (AppVersion app : versionManagement.getApps()) {
			String appPackageName = app.getPackageName();
			boolean contains = false;
			for(String packageName : toUpload) {
				if(packageName.equals(appPackageName)) contains = true;
			}
			if(contains) {
				String cmd = "am force-stop "+appPackageName;
				cmds.add(new Command(cmd));
			}			
		}
	}

	/**
	 * Start ModuleLoader-Service on the smartphone with its config.
	 */
	private void startModules() {
		for (AppVersion app : versionManagement.getApps()) {
			boolean contains = false;
			for(String packageName : toInstallAndStart) {
				if(packageName.equals(app.getPackageName())) contains = true;
			}
			for(String packageName : toStart) {
				if(packageName.equals(app.getPackageName())) contains = true;
			}
			if(contains) {
				String activity = DecodeAndroidManifest.extractFile(apkFolder + app.getFileName(), "assets/activity.json");
				String cmd = "";
				if(activity != null && activity.equals("")) {
					cmd = "am start -n "+ app.getPackageName() +"/.StartActivity";
				}
				else {
				cmd = "am startservice --es config '"+ app.getConfig().replaceAll("&quot;", "\"") +"' "
						+ "-n "+ app.getPackageName() +"/.StartService";
				}
				cmds.add(new Command(cmd));
			}			
		}
	}

	/**
	 * close connection.
	 */
	private void exit() {
		cmds.add(new Command("exit"));
	}
	
	/**
	 * Changes smartphone-data in db
	 */
	private void updateSmartphoneInfo() {
		int index = -1;
		List<Smartphone> smartphones = versionManagement.getSmartphones();
		for(int i = 0; i < smartphones.size(); i++) {
			if(smartphones.get(i).getImei().equals(imei)) {
				index = i;
			}
		}
		if(index != -1) smartphones.set(index, 
				new Smartphone(imei,
						System.currentTimeMillis(), 
						smartphones.get(index).getRealName(), 
						Integer.valueOf(batteryLevel), 
						Float.valueOf(batteryTemperature)/10));
		else smartphones.add(new Smartphone(imei, 
						System.currentTimeMillis(), 
						"", 
						Integer.valueOf(batteryLevel),
						Float.valueOf(batteryTemperature)/10));
		versionManagement.setSmartphones(smartphones);
	}
	
	/**
	 * Execute ssh commands one by one.
	 */
	private void execute() {
		if(cmds.isEmpty()) {
			//add log to db
			MySQL sqlConnection = MySQL.getInstance();
			sqlConnection.addLog(imei, log);
			log = "";
			
			updateSmartphoneInfo();
			
			//Update appInfo of this phone
			List<AppVersion> apps = new ArrayList<>();
			for(AppVersion app : versionManagement.getApps()) {
                List<State> smartphones = new ArrayList<>();
                     
                for(State smatphoneState : app.getSmartphones()) {
                    if(smatphoneState.getImei().equals(imei)) {
                        boolean add = true;
                        switch(smatphoneState.getState()) {
                            case UNINSTALL:
                                add = false;
                                break;
                            case INSTALL:
                                break;
                            case UPDATE:
                                break;
                            case UPTODATE: 
                                break;
                        }
                        if(add) smartphones.add(new State(imei, SmartphoneState.UPTODATE, System.currentTimeMillis()));
                    }
                    else smartphones.add(smatphoneState);
                }
                apps.add(new AppVersion(app.getID(), app.getPackageName(), app.getFileName(), app.getConfig(), app.getLastChange(), smartphones, app.getGroups()));
            }
            versionManagement.setApps(apps);
			
			vertx.eventBus().publish("web.in.refresh", new JsonObject().putString("action", "update"));
			return;
		}
		
		final SSH ssh = cmds.remove(0);
		vertx.eventBus().send(sshWriteAddress, ssh.toJson(), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				String command = "";
				String result = msg.body().getString("result");

				if (ssh instanceof Command) {
					Command cmd = (Command) ssh;
					command = cmd.command;
				}else if (ssh instanceof Put) {
					Put put = (Put) ssh;
					command = put.toJson().encode();
				}

				System.out.println(command);
				System.out.println(result);
				
				//log things in db
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");    
				sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				Date now = new Date();
				log += sdf.format(now) + ": [\"Command\":\"" + command + "\", \"Result\":\"" + result + "\"]\n";
				
				ssh.handle(result);
						
				// Force wait, especially on Put. 
				// Increases stability of update process
				long wait = ssh instanceof Put? 5000 : 300;
				vertx.setTimer(wait, new Handler<Long>() {
					@Override
					public void handle(Long event) {
						execute();
					}
				});
			}
		});
	}


	/**
	 * Helper Interface for commands.
	 *
	 * @author Oliver Rolle
	 */
	interface SSH extends Handler<String>{
		
		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type();
		
		/**
		 * To json.
		 *
		 * @return the json object
		 */
		public JsonObject toJson();
	}

	/**
	 * Commandline Command.
	 *
	 * @author Oliver Rolle
	 */
	class Command implements SSH{
		
		/** The command. */
		public final String command;
		
		/** The result. */
		public final Handler<String> result;

		/**
		 * Instantiates a new command.
		 *
		 * @param command the command
		 * @param result the result
		 */
		public Command(String command, Handler<String> result) {
			super();
			this.command = command;
			this.result = result;
		}

		/**
		 * Instantiates a new command.
		 *
		 * @param command the command
		 */
		public Command(String command) {
			this(command, null);
		}

		/* (non-Javadoc)
		 * @see org.vertx.java.core.Handler#handle(java.lang.Object)
		 */
		@Override
		public void handle(String event) {
			if(result != null) {
				result.handle(event);
			}
		}

		/* (non-Javadoc)
		 * @see de.orolle.bigsense.server.update.BigSenseUpdater.SSH#type()
		 */
		@Override
		public String type() {
			return "cmd";
		}

		/* (non-Javadoc)
		 * @see de.orolle.bigsense.server.update.BigSenseUpdater.SSH#toJson()
		 */
		@Override
		public JsonObject toJson() {
			return new JsonObject().putString("cmd", command);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return toJson().encode();
		}
	}

	/**
	 * SCP Put File Command.
	 *
	 * @author Oliver Rolle
	 */
	class Put implements SSH{
		
		/** The file. */
		public final String file;
		
		/** The result. */
		public final Handler<String> result;

		/**
		 * Instantiates a new put.
		 *
		 * @param file the file
		 * @param result the result
		 */
		public Put(String file, Handler<String> result) {
			super();
			this.file = file;
			this.result = result;
		}

		/**
		 * Instantiates a new put.
		 *
		 * @param file the file
		 */
		public Put(String file) {
			this(file, null);
		}

		/* (non-Javadoc)
		 * @see org.vertx.java.core.Handler#handle(java.lang.Object)
		 */
		@Override
		public void handle(String event) {
			if(result != null) {
				result.handle(event);
			}
		}

		/* (non-Javadoc)
		 * @see de.orolle.bigsense.server.update.BigSenseUpdater.SSH#type()
		 */
		@Override
		public String type() {
			return "put";
		}

		/* (non-Javadoc)
		 * @see de.orolle.bigsense.server.update.BigSenseUpdater.SSH#toJson()
		 */
		@Override
		public JsonObject toJson() {
			JsonObject put = new JsonObject().putString("localFile", file)
					.putString("remoteDir", "/sdcard/BigSense/");
			return new JsonObject().putObject("put", put);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return toJson().encode();
		}
	}
}
