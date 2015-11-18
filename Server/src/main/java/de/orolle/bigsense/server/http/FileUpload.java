package de.orolle.bigsense.server.http;

import java.math.BigInteger;
import java.util.Random;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Writes a File from an HttpServerRequest to Disk.
 * It is used by FileEndpoint.apk Android App.
 * 
 * @author Oliver Rolle
 *
 */
public class FileUpload {
	
	/**
	 * Handles FileUpload initiated by HttpServerRequest.
	 *
	 * @param vertx 	Vert.x instance to access FileSystem API
	 * @param req 	A HttpServerRequest which contains a File
	 * @param fileWriterPath 	Path to a directory to write the file into.
	 */
	public FileUpload(final Vertx vertx, final HttpServerRequest req,
			final String fileWriterPath) {
		req.expectMultiPart(true);
		
		req.uploadHandler(new Handler<HttpServerFileUpload>() {
			
			@Override
			public void handle(HttpServerFileUpload upload) {
				String rand = new BigInteger(30, new Random()).toString(32)+"-"; // safe file names
				
				upload.streamToFileSystem(fileWriterPath+rand+upload.name());
				
				upload.endHandler(new Handler<Void>() {
					@Override
					public void handle(Void event) {
						req.response().setStatusCode(200).end();
					}
				});
				
				upload.exceptionHandler(new Handler<Throwable>() {
					@Override
					public void handle(Throwable event) {
						req.response().setStatusCode(404).end();
						System.out.println(event.toString());
					}
				});
			}
		});
	}
}
