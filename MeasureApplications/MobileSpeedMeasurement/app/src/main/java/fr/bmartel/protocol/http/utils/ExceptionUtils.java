package fr.bmartel.protocol.http.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ExceptionUtils {
	/**
	 * Retrieve exception stack message
	 * 
	 * @param e
	 *            Exception e
	 * @return excpetion message
	 */
	public static String getExceptionMessage(Exception e) {
		final StringBuilder sb = new StringBuilder();
		e.printStackTrace(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				sb.append((char) b);
			}
		}));
		return sb.toString();
	}
}
