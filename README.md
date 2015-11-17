# Build this project
This project uses vert.x framework and its maven extension to build and install this project. Therefore take a look at http://vertx.io/maven_dev.html to get in introduction.

First clone this project via git ("git clone https://github.com/Telecooperation/big-sense.git") or via the zip-file. Extract the project if you have chosen the zip-file.

Second import this project into Eclipse

Third build and install project locally with "mvn install".


# Run this project
You need a Linux machine, where you do the following things (only one time):
- Create a local account with the username "bigsense" and the password "masterthesisdbsystel". Both values can be changed, but you have to change those also in the configs stated in Config.java
- Install JDK with "apt-get install openjdk-7-jdk"
- Install Vert.X:
	- wget http://dl.bintray.com/vertx/downloads/vert.x-2.1.5.tar.gz
	- tar -zxf vert.x-2.1.5.tar.gz
	- mv vert.x-2.1.5 /usr/local/share
	- ln -s /usr/local/share/vert.x-2.1.5/ /usr/local/share/vertx
	- ln -s /usr/local/share/vertx/bin/vertx /usr/bin/vertx
- Create folder "/home/bigsense/BigSense"
- Create folder "apk" in "/home/bigsense/BigSense/"
- Change rights of folder "/home/bigsense/BigSense/" recursive to 777
- Open the lib-file "ganymed-ssh2-build210.jar" located in libs-folder with 7Zip/WinRar; Copy the folder "ch" to /home/bigsense/BigSense/"
- Open the lib-file "mysql-connector-java-5.1.37-bin.jar" located in libs-folder with 7Zip/WinRar; Copy the folder "ch" to /home/bigsense/BigSense/"

- apt-get install php5-common libapache2-mod-php5 php5-cli
- Change Port in /etc/apache2/ports.conf and /etc/apache2/sites-available/default from 80 to 8080
- apt-get install mysql-server mysql-client
--> Create a SQL-Root password
- apt-get install libapache2-mod-auth-mysql phpmyadmin
- Go to phpmyadmin and create the databank "BigSense"
- Upload BigSense.sql in phpmyadmin and execute it

With the following instructions you can then execute the project if you changed something
- Export project as Jar-File (not runnable Jar-file)
- Copy the "de" and "web" folders out of the jar-file into "/home/bigsense/BigSense/"
- Start the whole thing with "vertx run de.orolle.bigsense.server.StartCloud"