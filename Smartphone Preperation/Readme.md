# Prepare the smartphone for BigSense
First you need to root your phone. Simple search via google how to do this. Now you have several things to do:

## Install/Configure SSH Server
- Install the application "com.icecoldapps.sshserver.apk" on your phone
- Add a new ssh-server:
--> Username: bigsense (or whatever you want, you have to change it also in the Config.java of the server)
--> Password: password (or whatever you want, you have to change it also in the Config.java of the server)
--> Port: 33822
--> This server starts together with the app itself

## Reboot if power is empty
Method 1:
- Copy file "lpm" on to your phone in the folder /sytem/bin/
- Do the following per ssh (or terminal on phone)
--> mount -o remount,rw /system
--> mv /system/bin/lpm /system/bin/lpm.orig
--> cp /sdcard/Downloads/lpm /system/bin/lpm
--> chmod 755 /system/bin/lpm
--> chown root.shell /system/bin/lpm
--> mount -o ro,remount /system

Method 2:
Connect the phone via USB Debug and run the following command via console: fastboot oem off-mode-charge 0

## Miscellaneous
- Set high GPS accuracy
- Install GPlayServices (these are used to retrieve sensor data)
- Deactivate Sim-Pin request