package de.orolle.bigsense.server.http;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Appends a JsonObject contained in HttpServerRequest on a Mongodb collection
 * It is used by NetworkEndpoint.apk Android App
 * 
 * @author Oliver Rolle
 *
 */
public class NetworkEndpoint {

	/**
	 * Handles JsonObject within HttpServerRequest.
	 *
	 * @param vertx 	Vert.x instance to access eventbus
	 * @param req 	A HttpServerRequest which contains the JsonObject
	 */
	public NetworkEndpoint(final Vertx vertx, final HttpServerRequest req) {
		final Buffer data = new Buffer();
		
		req.dataHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer in) {
				data.appendBuffer(in);
			}
		})
		
		.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void event) {
				try {
					JsonObject o = new JsonObject(data.toString());
					save(o);
					req.response().setStatusCode(200).end();
				} catch (Exception e) {
					req.response().setStatusCode(500).end();
					e.printStackTrace();
				}
			}

			private void save(JsonObject in) {
				String collection = in.getString("collection", "default");
				JsonObject data = in.getObject("data", new JsonObject())
						.putNumber("servertime", System.currentTimeMillis());
				vertx.eventBus().publish("web.out.listen."+collection, data); // notify user interface
			}
		});
	}

}
