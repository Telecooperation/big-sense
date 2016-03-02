package fr.bmartel.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * Http reader : read http frame separated by \r\n from inputstream
 * 
 * @author Bertrand Martel
 * 
 */
public class HttpReader {

	/**
	 * <p>
	 * Read a character sequence from the inpustream. Return the character
	 * sequence as String object and null if no bytes can be read from the
	 * inputstream. Data is stored in string buffer object and retruned by this
	 * function
	 * </p>
	 * 
	 * @return The next line in the input stream or null for EOF
	 * @throws IOException
	 *             on I/O error
	 * @param in
	 *            socket inputStream
	 * @return parsed line terminated by \r\n
	 * @throws IOException
	 */
	public String readLine(InputStream in) throws IOException {
		try {
			String buffer = "";
			int bytesRead = 0;
			int i = 0;
			boolean stop = false;
			if (in != null) {
				while (stop == false && (i = in.read()) >= 0) {
					bytesRead++;
					if ('\n' == (char) i) {
						stop = true;
					} else if ('\r' != (char) i) {
						buffer = buffer + ((char) i);
					}
				}
			}
			if (bytesRead == 0) {
				return null;
			}
			return buffer;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
	}
}
