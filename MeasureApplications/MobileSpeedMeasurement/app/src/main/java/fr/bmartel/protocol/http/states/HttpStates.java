package fr.bmartel.protocol.http.states;

/**
 * 
 * Different http states return by http frame decoder
 * 
 * @author Bertrand Martel
 *
 */
public enum HttpStates {

	MALFORMED_HTTP_FRAME, HTTP_FRAME_OK, HTTP_READING_ERROR, HTTP_WRONG_VERSION, HTTP_STATE_NONE, SOCKET_ERROR,HTTP_BODY_PARSE_ERROR

}
