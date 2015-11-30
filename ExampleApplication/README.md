# Example Application
BigSense provides two types of Android applications. The one (mostly used one) is an Android service, like this example application. Another possible type is an android activity, which shows info on the screen. But only one of those screen-activities can be used on each phone (If more than one are used, only the last started one is visible).
This Android app shows you how simple it is to make a standard Android app compatible with BigSense. In the only Java-class (StartService.java) you will get the config (which can be specified via the webinterface) and then do whatever you want. In this case we simply make a toast,w hich outputs a config parameter.

# How To add BigSense functionality to your Service-app
You need to change the following things

## Manifest
If you already have a service-class, which does all important things, simply name this service "StartService". If you start with a complete new app, add a service in manifest with the following code:
```
<service
	android:name=".StartService"
	android:enabled="true"
	android:exported="false" >
	<intent-filter>
		<category android:name="android.intent.category.DEFAULT" />
	</intent-filter>
</service>
```

## Config:
Add config.json in assets and fill it with the config you need (in json-format). Example: {"username":"testuser"}


## StartService.java
In this class you need this code to get the config parameters:

	//This one is needed to prevent to close the app from android system (you have to choose a different number than 1337)
	Notification notification = new NotificationCompat.Builder(this).build();
	startForeground(1337, notification);
		
	if(intent.getExtras() != null) {
		Object extra_conf = intent.getExtras().get("config");
	    JSONObject config = new JSONObject();
	
		if (extra_conf instanceof JSONObject) {
			config = (JSONObject) extra_conf;
		} else if (extra_conf instanceof String) {
			String str = (String) extra_conf;
			try {
				config = new JSONObject(str);
			} catch (JSONException e) {
			}
		} else {
			throw new IllegalStateException("Type of intent extra not a config: "+extra_conf.getClass().getCanonicalName());
		}
		
		try {
			long username = config.getLong("username");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	
# How To add BigSense functionality to your Activity-app
You need to change the following things

## Manifest
If you already have a activity-class, which does all important things, simply name this activity "StartActivity". If you start with a complete new app, add a activity in manifest with the following code:
```
<activity
	android:name=".StartActivity"
	android:configChanges="orientation|keyboardHidden|screenSize"
	android:label="@string/app_name"
	android:theme="@style/FullscreenTheme">
	<intent-filter>
		<action android:name="android.intent.action.MAIN" />
		<category android:name="android.intent.category.LAUNCHER" />
	</intent-filter>
</activity>
```

If the activity should be displayed in fullscreen, you also have to add these permissions:
```
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
```

## Config:
Add activity.json in assets. This is only needed to identify the application as an activity. At this point, we can't send configs to an activity.

## StartActivity.java
In this class you need this code to start the app in fullscreen (also unlocks the phone):
	
	PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
    wakeLock.acquire();
    KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
    keyguardLock.disableKeyguard();