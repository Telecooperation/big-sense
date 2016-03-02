package fr.bmartel.protocol.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import fr.bmartel.protocol.http.constants.HttpHeader;
import fr.bmartel.protocol.http.utils.IByteList;

/**
 * Http request builder
 * 
 * @author Bertrand Martel
 * 
 */
public class HttpBuilder {

	/**
	 * Send an httpRequest with content length dynamic
	 * 
	 * @param method
	 *            request method
	 * @param uri
	 *            request uri
	 * @param content
	 *            request body content
	 * @param headers
	 *            hashmap containing all http headers for this request
	 *            (content-length will be added if not here and only if body
	 *            length not 0)
	 * @return request in String format
	 * @throws UnsupportedEncodingException
	 */
	public static String httpRequest(String method, String uri, IByteList content,
			HashMap<String, String> headers)
			throws UnsupportedEncodingException {
		HttpVersion version = new HttpVersion(1, 1);
		if (!headers.containsKey(HttpHeader.CONTENT_LENGTH)
				&& content.getBytes().length != 0) {
			headers.put(HttpHeader.CONTENT_LENGTH,
					String.valueOf(content.getBytes().length));
		}
		HttpFrame request = new HttpFrame(method, version,
				headers, uri, content );
		return request.toString();
	}
}
