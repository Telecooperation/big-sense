package de.orolle.bigsense.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BootReceiver is executed after the android system has booted.
 * It receives a BOOT_COMPLETED event from android and is registered
 * on this event in the manifest.
 * 
 * @author Oliver Rolle
 *
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent bootintent) {
		context.startService(new Intent(context, UpdateService.class));
	}
}
