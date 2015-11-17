package de.orolle.bigsense.server.devicemgmt;

// TODO: Auto-generated Javadoc
/**
 * The Class State.
 */
public class State {
	
	/** The imei. */
	private String imei;
	
	//0: Smartphone runs this app actually, but it won't in the future
	//1: Smartphone has to install this app
	//2: Smartphone has to update the app
	/** The state. */
	//3: smartphone is up to date
	private SmartphoneState state;
	
	/** The last restart. */
	private long lastRestart;
	
	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(SmartphoneState state) {
		this.state = state;
	}

	/**
	 * Instantiates a new state.
	 *
	 * @param imei the imei
	 * @param state the state
	 * @param lastRestart the last restart
	 */
	public State(String imei, SmartphoneState state, long lastRestart) {
		super();
		this.imei = imei;
		this.state = state;
		this.lastRestart = lastRestart;
	}

	/**
	 * Gets the imei.
	 *
	 * @return the imei
	 */
	public String getImei() {
		return imei;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public SmartphoneState getState() {
		return state;
	}
	
	/**
	 * Gets the last restart.
	 *
	 * @return the last restart
	 */
	public long getLastRestart() {
		return lastRestart;
	}
}
