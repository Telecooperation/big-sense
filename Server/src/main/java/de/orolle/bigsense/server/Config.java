package de.orolle.bigsense.server;

public class Config {

	/** The Constant MYSQL_SERVER_USER. */
	public static final String MYSQL_SERVER_USER = "MYSQL_USER";
	
	/** The Constant MYSQL_SERVER_PWD. */
	public static final String MYSQL_SERVER_PWD = "SQL_PASSWORD";
	
	/** The Constant SSH_SERVER_USER. */
	public static final String SSH_SERVER_USER = "LINUX_USER";
	
	/** The Constant SSH_SERVER_PWD. */
	public static final String SSH_SERVER_PWD = "USER_PASSWORD";
	
	/** The Constant WEB_USER. */
	public static final String WEB_USER = "WEBINTERFACE_USER";
	
	/** The Constant WEB_USER_PWD. */
	public static final String WEB_USER_PWD = "WEBINTERFACE_PW";
	
	/** The Constant HTTP_INTERNAL_PORT. */
	public static final int HTTP_INTERNAL_PORT = 33824;
	
	/** The Constant HTTP_EXTERNAL_PORT. */
	public static final int HTTP_EXTERNAL_PORT = 80;
	
	/** The Constant SSH_SERVER_PORT. */
	public static final int SSH_SERVER_PORT = 33822;
	
	/** The Constant LISTEN_PORT_UPPER. */
	public static final int LISTEN_PORT_UPPER = 33821;
	
	/** Sets the value for good battery to 95 to 100% */
	public static final int LIMIT_GOOD_BATTERY = 95;
	
	/** Sets the value for bad battery to 0 to 60% */
	public static final int LIMIT_BAD_BATTERY = 60;
	
	/** Sets the value for good temperature till 40 degree */
	public static final float LIMIT_GOOD_TEMPERATURE = 40.0f;
	
	/** Sets the value for bad temperature from 50 degree on */
	public static final float LIMIT_BAD_TEMPERATURE = 50.0f;
	
	/** Sets the value for a good last contact to a maximum of 3 hours (3600 seconds * 3) */
	public static final int LIMIT_GOOD_LASTCONTACT = 10800;
	
	/** Sets the value for a bad last contact to a minimum of 24 hours (3600 seconds * 24) */
	public static final int LIMIT_BAD_LASTCONTACT = 86400;
	
	/** The Constant USER_HOME_PATH. */
	public static final String USER_HOME_PATH = "/home/bigsense/";
	
	/** The Constant RESTART_APP_INTERVAL_MILLISECOND. */
	public static final int RESTART_APP_INTERVAL_MILLISECOND = 21500000; //Every 6 hours (6 hours minus 100 sec)
}
