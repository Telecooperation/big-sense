package de.orolle.bigsense.server;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

import de.orolle.bigsense.server.database.MySQL;
import de.orolle.bigsense.server.ssh.SSHVerticle;
import de.orolle.bigsense.server.update.BigSenseUpdater;
import de.orolle.bigsense.server.util.Parallel;
import de.orolle.bigsense.server.webui.WebinterfaceManager;

/**
 * Main for BigSenseWeb. It is executed as Vert.x has loaded this class and than instantiates it.
 * 
 * 
 * @author Oliver Rolle, Martin Hellwig
 *
 */
public class StartCloud extends Verticle {
	/**
	 * Vert.x logger
	 */
	public static Logger log;
	
	/** File System Folders for apk files. */
	private String apkWriterPath = null;
	
	/** Event bus addresses for authentication. */
	private String authAddress = null;
	
	/** Event bus addresses for ssh client. */
	private String sshClientAddress = null;

	/** The Constant SSH_SERVER_ADDRESS. */
	private static final String SSH_SERVER_ADDRESS = "127.0.0.1";
	
	/** The Constant WEB_ADDRESS. */
	private static final String WEB_ADDRESS = "0.0.0.0";
	
	
	/** Session id of logged in user. */
	private String webAuthSSID = null;
	
	/** Latest time to close a reverse connection. */
	private final long reverse_connection_timeout = 58*60*1000;
	
	/** Used Server Ports. */
	private HashMap<Integer, Boolean> listenPorts = new HashMap<>();
	
	/**
	 * Initalizes and start all components of BigSenseWeb.
	 *
	 * @param startedResult the started result
	 */
	@Override
	public void start(final Future<Void> startedResult) {
		super.start(startedResult);
		log = this.container.logger();
		
		//connect to sql-database
		MySQL.setUser(Config.MYSQL_SERVER_USER, Config.MYSQL_SERVER_PWD);
		
		authAddress = "vertx.basicauthmanager";
		sshClientAddress = "ssh.client."+UUID.randomUUID();
		
		apkWriterPath = container.config().getString("apkdata_folder", Config.USER_HOME_PATH + "BigSense/apk/");
		
		/*
		 * Runs after everything is deployed
		 * Reports start success or failure to its vert.x container 
		 */
		final Parallel<AsyncResult<? extends Object>> deploy = Parallel.Completed(new Handler<HashMap<String,AsyncResult<? extends Object>>>() {
			@Override
			public void handle(HashMap<String, AsyncResult<? extends Object>> res) {
				boolean succeeded = true;
				for(Entry<String, AsyncResult<? extends Object>> e : res.entrySet()) {
					succeeded &= e.getValue().succeeded();
				}
				
				if(succeeded) {
					System.out.println("BigSenseWeb start complete. All sub services started successfully!");
					startedResult.setResult(null);
				} else {
					startedResult.setFailure(new Exception("Deployment not successful"));
				}
			}
		});

		/*
		 * BigSense Web UI
		 */
		// Config
		JsonArray inbound = new JsonArray();
		inbound.add(new JsonObject().putString("address_re", "web.in\\..+").putBoolean("requires_auth", false));
		inbound.add(new JsonObject().putString("address", authAddress+".login"));
		JsonArray outbound = new JsonArray();
		outbound.add(new JsonObject().putString("address_re", "web.out\\..+").putBoolean("requires_auth", false));
		JsonObject ui_config = new JsonObject()
		.putString("web_root", "web")
		.putString("host", WEB_ADDRESS)
		.putNumber("port", Config.HTTP_EXTERNAL_PORT)
		.putBoolean("static_files", true)
		.putBoolean("bridge", true)
		.putArray("inbound_permitted", inbound)
		.putArray("outbound_permitted", outbound)
		.putString("auth_address", authAddress)
		.putObject("sjs_config", new JsonObject().putString("prefix", "/vertxbus"));
		// Deployment
		final Handler<AsyncResult<? extends Object>> uiWeb = deploy.handler();
		container.deployModule("io.vertx~mod-web-server~2.0.0-final", ui_config, new Handler<AsyncResult<String>>() {
			@Override
			public void handle(AsyncResult<String> event) {
				uiWeb.handle(event);
			}
		});
		
		/*
		 * Web UI Functionality
		 * Intermediation between UI and apk, app deployment / updates
		 */
		new WebinterfaceManager(getVertx(), apkWriterPath);
		
		/*
		 * BigSense internal webserver Deployment
		 * data collector for smartphones
		 */
		final Handler<AsyncResult<? extends Object>> internalWeb = deploy.handler();
		vertx.createHttpServer()
		.listen(Config.HTTP_INTERNAL_PORT, new Handler<AsyncResult<HttpServer>>() {
			@Override
			public void handle(AsyncResult<HttpServer> event) {
				internalWeb.handle(event);
			}
		});
		
		/*
		 * Reverse Connection Server for SSH for Android Devices 
		 */
		final Handler<AsyncResult<? extends Object>> ssh = deploy.handler();
		vertx.createNetServer() // First server
		.connectHandler(new Handler<NetSocket>() {
			@Override
			public void handle(final NetSocket androidSSH) {
				final Buffer storeBuffer = new Buffer();
				androidSSH.dataHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer event) {
						storeBuffer.appendBuffer(event);
					}
				});
				
				makeReverseConnectionServer(androidSSH, Config.LISTEN_PORT_UPPER, storeBuffer); // Second server
			}
		}).listen(Config.SSH_SERVER_PORT, new Handler<AsyncResult<NetServer>>() {
			@Override
			public void handle(AsyncResult<NetServer> event) {
				ssh.handle(event);
			}
		});

		/*
		 * Deploy web authentication (but it is not used)
		 */
		deployWebAuthentication(deploy.handler());
		
		/*
		 * Deploy SSH functionality for SSH connections to Android Smartphone
		 */
		deploySSH(deploy.handler());
	}

	/**
	 * Deploys the SSHVerticle as worker in Vert.x environment, therefore providing
	 * SSH functionality for the update process by BigSenseUpdater.
	 * 
	 * @param handler
	 * 	Handler is called after deployment
	 */
	private void deploySSH(final Handler<AsyncResult<? extends Object>> handler) {
		JsonObject sshConf = new JsonObject()
		.putString("address", sshClientAddress);

		this.container.deployWorkerVerticle(SSHVerticle.class.getCanonicalName(), 
				sshConf, 
				1,
				false,
				new Handler<AsyncResult<String>>() {
			@Override
			public void handle(AsyncResult<String> event) {
				if(event.succeeded()) {
					handler.handle(event);
				} else {
					handler.handle(event);
				}
			}
		});
	}

	/**
	 * Deploys Authentication Service for web-interface.
	 * (IT IS NOT USED BY WEB)
	 * @param handler
	 * 	Handler is called after deployment
	 */
	private void deployWebAuthentication(final Handler<AsyncResult<? extends Object>> handler) {
		vertx.eventBus().registerHandler(authAddress+".authorise", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				String user = container.config().getString("web_user", Config.WEB_USER);
				
				if(msg.body().getString("sessionID", "").equals(webAuthSSID)) {
					msg.reply(new JsonObject().putString("status", "ok").putString("user", user));
				} else {
					msg.reply(new JsonObject().putString("status", "denied"));
				}
			}
		});
		
		vertx.eventBus().registerHandler(authAddress+".login", new Handler<Message<JsonObject>>() {
			String user = container.config().getString("web_user", Config.WEB_USER);
			String pwd = container.config().getString("web_password", Config.WEB_USER_PWD);

			@Override
			public void handle(Message<JsonObject> msg) {
				JsonObject o = msg.body();
				if((webAuthSSID != null && webAuthSSID.equals(o.getString("sessionID", ""))) ||
						(user.equals(o.getString("username", "")) &&
						pwd.equals(o.getString("password", "")))) {
					if(webAuthSSID == null)
						webAuthSSID = UUID.randomUUID().toString();

					msg.reply(new JsonObject().putString("status", "ok")
							.putString("sessionid", webAuthSSID));

					vertx.setTimer(200, new Handler<Long>() {
						@Override
						public void handle(Long event) {
							vertx.eventBus().publish("web.update.all", new JsonObject().putString("action", "update"));
						}
					});
				} else {
					msg.reply(new JsonObject().putString("status", "denied"));
				}
			}
		}, new Handler<AsyncResult<Void>>() {
			@Override
			public void handle(AsyncResult<Void> event) {
				handler.handle(event);
			}
		});
	}

	/**
	 * Creates a ReverseConnectionServer which waits on SSHClient to connect.
	 * The data which received on this server is passed to the TCP connection made by the 
	 * Android Smartphone.
	 *
	 * @param androidSSH 	Android Connection
	 * @param remotePort 	Port to listen on for SSHClient connection
	 * @param storeBuffer the store buffer
	 */
	private void makeReverseConnectionServer(final NetSocket androidSSH, final int remotePort, final Buffer storeBuffer) {
		System.out.println(androidSSH.remoteAddress().toString()+": Reverse server try to bind port ("+remotePort+")");
		if(listenPorts.containsKey(remotePort)) {
			makeReverseConnectionServer(androidSSH, remotePort - 1, storeBuffer);
			return ;
		}
		
		listenPorts.put(remotePort, true);

		@SuppressWarnings("rawtypes")
		final Handler[] close = {null};
		final NetServer[] instance = {null};
		instance[0] = vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
			Pump p1, p2;
			NetSocket remoteSSH;
			boolean closed = false;
			long timer;

			@Override
			public void handle(final NetSocket ns) {
				close[0] = new Handler<Void>() {
					@Override
					public void handle(Void event) {
						close();
					}
				};
				remoteSSH = ns;

				androidSSH.closeHandler(new Handler<Void>() {
					@Override
					public void handle(Void event) {
						close();
					}
				});

				remoteSSH.closeHandler(new Handler<Void>() {
					@Override
					public void handle(Void event) {
						close();
					}
				});

				// Cleanup after 59 min
				timer = vertx.setTimer(reverse_connection_timeout, new Handler<Long>() {
					@Override
					public void handle(Long event) {
						close();
					}
				});

				remoteSSH.write(storeBuffer);

				//System.out.println("Create Pumps: "+androidSSH.remoteAddress().getAddress().getHostAddress()+" <-> "+remoteSSH.remoteAddress().getAddress().getHostAddress());
				p1 = Pump.createPump(remoteSSH, androidSSH).start();
				p2 = Pump.createPump(androidSSH, remoteSSH).start();
			}

			private synchronized void close(){
				if(!closed) {
					System.out.println(androidSSH.remoteAddress().toString()+": Close connection of port ("+remotePort+").");
					closed = true;
					
					try {
						vertx.cancelTimer(timer);
					}catch(Exception e) {}
					try {
						androidSSH.close();
					}catch(Exception e) {}
					try {
						remoteSSH.close();
					}catch(Exception e) {}
					try {
						instance[0].close();
						instance[0] = null;
					}catch(Exception e) {}
					try {
						p1.stop();
					}catch(Exception e) {}
					try {
						p2.stop();
					}catch(Exception e) {}

					listenPorts.remove(remotePort);
				}
			}
		}).listen(remotePort, new Handler<AsyncResult<NetServer>>() {
			boolean isShutdown = false;
			@Override
			public void handle(AsyncResult<NetServer> res) {
				
				if(res.succeeded()) {
					System.out.println(androidSSH.remoteAddress().toString()+": Reverse server started on port ("+remotePort+")");
					// Close reverse connection after timeout
					vertx.setTimer(reverse_connection_timeout, new Handler<Long>() {
						@Override
						public void handle(Long event) {
							shutdown();
						}
					});
					
					JsonObject connect = new JsonObject()
					.putString("action", "connect")
					.putString("ip", SSH_SERVER_ADDRESS)
					.putNumber("port", res.result().port())
					.putString("user", Config.SSH_SERVER_USER)
					.putString("password", Config.SSH_SERVER_PWD);

					vertx.eventBus().send(sshClientAddress, connect, new Handler<Message<JsonObject>>() {
						@Override
						public void handle(final Message<JsonObject> msg) {
							vertx.eventBus().registerHandler(msg.body().getString("read"), new Handler<Message<JsonObject>>() {
								@Override
								public void handle(Message<JsonObject> event) {
									shutdown();
								}
							});
							
							new BigSenseUpdater(vertx, msg.body().getString("write"), apkWriterPath, msg.body().getString("read"));
						}
					});
					
				} else {
					System.out.println(androidSSH.remoteAddress().toString()+": Reverse server could not bind port("+remotePort+"): "+res.cause().toString());
					
					try {
						instance[0].close();
					}catch(Exception e) {}
					instance[0] = null;
					
					listenPorts.remove(remotePort);
					makeReverseConnectionServer(androidSSH, remotePort - 1, storeBuffer);
				}
			}
			
			@SuppressWarnings("unchecked")
			private void shutdown() {
				if(isShutdown)
					return;
				
				isShutdown = true;
				try {
					androidSSH.close();
				}catch(Exception e) {}
				try {
					instance[0].close();
					instance[0] = null;
				}catch(Exception e) {}
				try {
					close[0].handle(null);
				}catch(Exception e) {}
				
				listenPorts.remove(remotePort);
			}
		});
	}
}
