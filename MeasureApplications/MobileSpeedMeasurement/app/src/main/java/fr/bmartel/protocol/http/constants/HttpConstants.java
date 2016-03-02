package fr.bmartel.protocol.http.constants;

/**
 * <b>Define HTTP constants</b>
 * 
 * @author Bertrand Martel
 * 
 */
public class HttpConstants {
	/**
	 * HTTP line delimiter
	 */
	public static final String HEADER_DELEMITER = "\r\n";
	/**
	 * HTTP header delimiter
	 */
	public static final String HEADER_TERMINATOR = "\r\n" + "\r\n";

	/**
	 * HTTP header delimiter.
	 */
	public static final String HEADER_VALUE_DELIMITER = ": ";

	/**
	 * NOT FOUND RESPONSE String definition
	 */
	public final static String NOT_FOUND = "HTTP/1.1 404 Not Found\r\n"
			+ "Content-Length: 22\r\n" + "Content-Type: text/html\r\n\r\n"
			+ "<h1>404 Not Found</h1>";

	/**
	 * SERVER ERROR RESPONSE String definition
	 */
	public final static String BAD_REQUEST_ERROR = "HTTP/1.1 400 Bad Request\r\n"
			+ "Content-Length: 53\r\n" + "Content-Type: text/html\r\n\r\n"
			+ "<h1>400 Bad Request</h1><p>Malformed HTTP request</p>";

	/**
	 * SERVER ERROR RESPONSE String definition
	 */
	public final static String INTERNAL_SERVER_ERROR = "HTTP/1.1 500 Internal Server Error\r\n"
			+ "Content-Length: 34\r\n"
			+ "Content-Type: text/html\r\n\r\n"
			+ "<h1>500 Internal Server Error</h1>";

	/**
	 * OK RESPONSE String definition
	 */
	public final static String OK = "HTTP/1.1 200 OK\r\n" + "Content-Length: 18\r\n"
			+ "Content-Type: text/html\r\n\r\n" + "<h1>200 Found</h1>";

	/**
	 * REDIRECTION
	 */
	public final static String REDIRECTION = "HTTP/1.1 301 Moved Permanently\r\n"
			+ "Content-Length: 22\r\n" + "Content-Type: text/html\r\n\r\n"
			+ "<h1>301     Found</h1>";
}
