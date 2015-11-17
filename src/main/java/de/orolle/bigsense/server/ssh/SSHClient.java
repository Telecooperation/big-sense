package de.orolle.bigsense.server.ssh;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.impl.JsonObjectMessage;
import org.vertx.java.core.json.JsonObject;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

/**
 * SSHClient which connects to Androids SSH-Server.
 *
 * @author Oliver Rolle
 */
@SuppressWarnings("unused")
public class SSHClient {
	
	/** Vertx instance. */
	private final Vertx vertx;
	
	/** Ip of ssh server. */
	private final String ip;
	
	/** port of ssh server. */
	private final int port;
	
	/** username for ssh server. */
	private final String user;
	
	/** password for ssh server. */
	private final String password;

	/** Event bus address to listen on for ssh commands. */
	private final String sshWriteAddress;
	
	/** Event bus address to write responses to. */
	private final String sshReadAddress;

	/** SSH connection. */
	private Connection conn;
	
	/** SSH session. */
	private Session sess;

	/** Command line channel stdin. */
	private WritableByteChannel sshWrite;
	
	/** Command line channel stdout. */
	private ReadableByteChannel  sshRead;
	
	/** Read on channel. */
	private Reader reader;
	
	/** Write on channel. */
	private Writer writer;

	/** SCP client for file transfer. */
	private SCPClient scp;
	
	/** SSH client logged into server. */
	private boolean isAuthenticated = false;

	// Thread
	/** Execution thread for ssh connection. */
	private boolean thread_isexec = true;
	
	/** List of update commands. */
	private final List<Message<JsonObject>> cmdMsgs = Collections.synchronizedList(new LinkedList<Message<JsonObject>>());

	/** Handler to queue incoming update commands and notify ssh client on new commands. */
	private Handler<Message<JsonObject>> listen = new Handler<Message<JsonObject>>() {
		@Override
		public synchronized void handle(Message<JsonObject> msg) {
			if(!sshRead.isOpen() || !sshWrite.isOpen()) {
				shutdown();
				return;
			}

			synchronized (cmdMsgs) {
				//System.out.println("Schedule: "+msg.body().encode());
				cmdMsgs.add(msg);
				cmdMsgs.notify();
			}
		}
	};

	/**
	 * Instantiates an SSH-Client which is used to connect android.
	 *
	 * @param vertx 	Vert.x instance to access eventbus API
	 * @param ip 	IP to connect to
	 * @param port 	Port to connect to
	 * @param user  Username for SSH authentification
	 * @param password  Password for SSH authentification
	 */
	public SSHClient(Vertx vertx, String ip, int port, String user, String password) {
		super();
		this.vertx = vertx;
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.password = password;

		UUID id = UUID.randomUUID();
		sshWriteAddress = "SSHClient."+id.toString()+".write";
		sshReadAddress =  "SSHClient."+id.toString()+".read";
		
		cmdMsgs.add(new JsonObjectMessage(true, null, new JsonObject().putString("cmd", "su")));
		vertx.eventBus().registerHandler(sshWriteAddress, listen);
	}

	/**
	 * eventbus address to write commands to ssh server.
	 *
	 * @return the string
	 */
	public String writeAddress() {
		return sshWriteAddress;
	}

	/**
	 * eventbus address to receive repsonses from ssh server.
	 *
	 * @return the string
	 */
	public String readAddress() {
		return sshReadAddress;
	}

	/**
	 * Starts connect process. 
	 *
	 * @return the SSH client
	 */
	public SSHClient connect() {
		try
		{
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if(!isAuthenticated) {
						vertx.eventBus().publish(sshReadAddress, new JsonObject().putString("result", "Could not authenticated within 60 seconds."));
						shutdown();
					}
				}
			}, 60000);
			/* Create a connection instance */

			conn = new Connection(ip, port);

			/* Now connect */
			conn.connect();

			/* Authenticate */

			isAuthenticated = conn.authenticateWithPassword(user, password);

			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			/* Create SCP Client */
			scp = conn.createSCPClient();

			/* Create a session */
			sess = conn.openSession();

			sess.requestPTY("dump");
			sess.startShell();

			sshWrite = Channels.newChannel(sess.getStdin());
			sshRead = Channels.newChannel(sess.getStdout());

			reader = Channels.newReader(sshRead, "UTF-8");
			writer = Channels.newWriter(sshWrite, "UTF-8");

			reader.read(CharBuffer.allocate(1024));

			/**
			 * Execute one command after the other 
			 */
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (sshWrite.isOpen() && sshRead.isOpen() && thread_isexec) {
						try {
							synchronized (cmdMsgs) {
								if(cmdMsgs.isEmpty())
									cmdMsgs.wait();

								exectuteCmd();
							}
						} catch (Exception e) {
							e.printStackTrace();
							shutdown();
						}
					}
				}
			}).start();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			shutdown();
		}

		return this;
	}

	/**
	 * Execute a single command with synchronous behavior.
	 * 2 types of commands: cmd and put
	 * cmd executes a commandline command.
	 * put copies a local file onto the ssh server.
	 *
	 * @throws Exception the exception
	 */
	private void exectuteCmd() throws Exception{
		if(cmdMsgs.isEmpty() || !thread_isexec) {
			//System.out.println("Commands are emptry");
			return;
		}
		Message<JsonObject> msg = cmdMsgs.remove(0);
		String cmd = msg.body().getString("cmd", "");
		JsonObject put = msg.body().getObject("put", null);
		
		//System.out.println("COMMAND: "+msg.body().encode());

		if(!cmd.equals("")) {
			cmd = cmd.equals("exit")? "exit\nexit\n" : cmd;
			// double exit because of 1 super user, 2 user shell

			int bytes = -1;
			try{
				bytes = sshWrite.write(ByteBuffer.wrap((cmd+"\n").getBytes()));
			}catch(Exception e) {
				msg.reply(new JsonObject().putString("result", e.toString()));
				this.shutdown();
				return;
			}
			//System.out.println("SSHClient: send["+bytes+"]: "+cmd);

			final StringBuffer result = new StringBuffer();
			final CharBuffer buffer = CharBuffer.allocate(1024);
			int read = 0;

			try {
				while((read = reader.read(buffer)) >= 0) {
					StringBuffer line = new StringBuffer();
					for(int i=0; i < read; i++) {
						line.append(buffer.get(i));
					}

					result.append(line);
					buffer.clear();
					//System.out.println("SSHClient: recv["+read+"]: "+line);

					// is command executed than prompt appears
					if(line.toString().endsWith(" # ") || line.toString().endsWith(" $ ")) {
						String data = result.toString();
						String[] rows = data.split("\n");

						StringBuffer cuted = new StringBuffer();
						for(int i = 1; i < rows.length-1; i++) {
							cuted.append(rows[i]);
							cuted.append("\n");
						}

						msg.reply(new JsonObject().putString("result", cuted.toString()));
						break;
					}

					Thread.sleep(10);
				}
			} catch(Exception e) {
				msg.reply(new JsonObject().putString("result", e.toString()));
				this.shutdown();
				return;
			}

			if(cmd.equals("exit\nexit\n")) {
				shutdown();
			}
		} else if(put != null ) {
			String localFile = put.getString("localFile", "");
			String remoteDir = put.getString("remoteDir", "");

			if(!localFile.equals("") && !remoteDir.equals("")) {
				try {
					scp.put(localFile, remoteDir);
				} catch (Exception e) {
					e.printStackTrace();
					msg.reply(new JsonObject().putString("result", "SSH PUT FAILED "+put.encode()));
					
					return;
				}
				msg.reply(new JsonObject().putString("result", "SSH PUT COMPLETE "+put.encode()));
			}else {
				msg.reply(new JsonObject().putString("result", "SSH PUT FAILED "+put.encode()));
			}
		}
	}

	/**
	 * Closes ssh connection and removes eventbus listener.
	 * It should be always called after finishing job or failure!
	 *
	 * @return the SSH client
	 */
	private SSHClient shutdown() {
		vertx.eventBus().unregisterHandler(sshWriteAddress, listen);
		vertx.eventBus().publish(sshReadAddress, new JsonObject().putString("result", "exit"));
		
		/* Close the Shell streams*/
		try {
			sshRead.close();
		}catch(Exception e) {}
		try {
			sshWrite.close();
		}catch(Exception e) {}


		/* Close this session */
		try { 
			sess.close();
		}catch(Exception e) {}

		/* Close the connection */
		try {
			conn.close();
		}catch(Exception e) {}

		/* Shut down thread */
		thread_isexec = false;
		cmdMsgs.clear();
		synchronized (cmdMsgs) {
			cmdMsgs.notify();
		}

		return this;
	}
}
