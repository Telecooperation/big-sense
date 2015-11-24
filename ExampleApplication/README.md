# Example Application
This Android app shows you how simple it is to make a standard Android app compatible with BigSense. In the only Java-class (StartService.java) you will get the config (which can be specified via the webinterface) and then do whatever you want. In this case we simply make a toast,w hich outputs a config parameter.

# How To add BigSense functionality to your app
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

