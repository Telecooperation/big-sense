package de.orolle.bigsense.server.devicemgmt;

/**
 * The Enum SmartphoneState.
 */
public enum SmartphoneState {
	
	/** The uninstall. */
	UNINSTALL(0), 
 /** The install. */
 INSTALL(1), 
 /** The update. */
 UPDATE(2), 
 /** The uptodate. */
 UPTODATE(3);
	
	/** The value. */
	private final int value;

    /**
     * Instantiates a new smartphone state.
     *
     * @param value the value
     */
    SmartphoneState(int value) {
        this.value = value;
    }
    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
    	return this.value;
    }

	/**
	 * Value of.
	 *
	 * @param value the value
	 * @return the smartphone state
	 */
	public static SmartphoneState valueOf(Integer value) {
		switch(value) {
		case 0:
			return UNINSTALL;
		case 1:
			return INSTALL;
		case 2:
			return UPDATE;
		case 3:
			return UPTODATE;
		}
		return UNINSTALL;
	}
};