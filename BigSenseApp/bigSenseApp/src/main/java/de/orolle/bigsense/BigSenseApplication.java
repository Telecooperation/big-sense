package de.orolle.bigsense;

import android.app.Application;
import android.content.Intent;
import de.orolle.bigsense.update.UpdateService;

/**
 * BigSenseApplication is executed at the beginning of an app start.
 * It starts the UpdateService.
 * 
 * @author Martin Hellwig
 *
 */
public final class BigSenseApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		startService(new Intent(this, UpdateService.class));		
	}
}
