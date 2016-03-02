package fr.bmartel.protocol.http;

/**
 * Status return code object builder
 * 
 * @author Bertrand Martel
 * 
 */
public class StatusCodeObject {

	/** return code value */
	public int code = 0;

	/** return code phrase */
	public String reasonPhrase = "";

	/**
	 * Return code builder
	 * 
	 * @param code
	 *            return code value
	 * @param reasonPhrase
	 *            return code phrase
	 */
	public StatusCodeObject(int code, String reasonPhrase) {
		this.code = code;
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * Display status code object information like in http request according to
	 * RFC
	 */
	public String toString() {
		return (code + " " + reasonPhrase);
	}
}
