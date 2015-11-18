package de.orolle.bigsense.server.ssh;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Makes synchronous SSHClient functionality available in asynchronous vert.x.
 * 
 * It can handle the flowing message:
 * {"action": "connect", "ip": "<REMOTE IP>", "port": <REMOTE PORT>, "user": "<SSH USER>", "password":"<SSH PASSWORD>"}
 * 
 * @author Oliver Rolle
 *
 */
public class SSHVerticle extends Verticle implements Handler<Message<JsonObject>> {
	
	/** Eventbus address to listen on. */
	private String address;

	/**
	 * Called by vert.x framework on deployment of this verticle
	 */
	public void start() {
		super.start();
		
		address = container.config().getString("address", getClass().getCanonicalName());

		this.vertx.eventBus().registerHandler(address, this);
	}

	/**
	 * Receives messages from eventbus.
	 *
	 * @param msg the msg
	 */
	@Override
	public void handle(final Message<JsonObject> msg) {
		final JsonObject body = msg.body();
		final String action = body.getString("action", "");
		//System.out.println("SSHVerticle recv: "+body.encode());
		switch (action) {
		case "connect":
			SSHClient ssh = createClient(body.getString("ip", ""), body.getInteger("port", 1), 
					body.getString("user", ""), body.getString("password", ""));
			
			JsonObject address = new JsonObject()
			.putString("write", ssh.writeAddress())
			.putString("read", ssh.readAddress());
			
			msg.reply(address);
			ssh.connect();
			break;

		default:
			break;
		}
	}

	/**
	 * Connects onto a ssh server. It delegates the ssh functionality to a SSHClient instance.
	 *
	 * @param ip 	remote ip
	 * @param port  remote port
	 * @param user  ssh user name
	 * @param password  ssh password
	 * @return the SSH client
	 */
	private SSHClient createClient(String ip, int port, String user, String password) {
		return new SSHClient(vertx, ip, port, user, password);
	}
}
