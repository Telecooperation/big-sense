package fr.bmartel.protocol.http.inter;

import java.util.HashMap;

import fr.bmartel.protocol.http.HttpVersion;
import fr.bmartel.protocol.http.StatusCodeObject;

/**
 * 
 * Http response frame interface
 * 
 * @author Bertrand Martel
 * 
 */
public interface IHttpResponseFrame {

	/** return code for response frame */
	public StatusCodeObject getReturnCode();

	/** http version for response frame */
	public HttpVersion getHttpVersion();

	/** headers for response frame */
	public HashMap<String, String> getHeaders();
}
