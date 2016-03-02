package fr.bmartel.protocol.http;

/**
 * Http version builder and parser
 * 
 * @author Bertrand Martel
 * 
 */
public class HttpVersion {

	/** version first digit (before ".") */
	public int versionDigit1;

	/** version second digit */
	public int versionDigit2;

	/**
	 * Http version builder according to two digit parameters (RFC)
	 * 
	 * @param versionDigit1
	 * @param versionDigit2
	 */
	public HttpVersion(int versionDigit1, int versionDigit2) {
		this.versionDigit1 = versionDigit1;
		this.versionDigit2 = versionDigit2;
	}

	/**
	 * Parser for http version
	 * 
	 * @param stringToParse
	 *            http request to parse
	 */
	public HttpVersion(String stringToParse) {
		if (stringToParse.startsWith("HTTP/")
				&& stringToParse.length() > "HTTP/".length() + 2) {
			int version1 = Integer.parseInt(stringToParse.substring(
					stringToParse.indexOf('/') + 1,
					stringToParse.indexOf('/') + 2));
			int version2 = Integer.parseInt(stringToParse.substring(
					stringToParse.indexOf('/') + 3,
					stringToParse.indexOf('/') + 4));
			this.versionDigit1 = version1;
			this.versionDigit2 = version2;
		}
	}

	/**
	 * format http version according to RFC
	 */
	public String toString() {
		return "HTTP/" + this.versionDigit1 + "." + this.versionDigit2;
	}
}
