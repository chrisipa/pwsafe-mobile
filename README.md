pwsafe-mobile
=======

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=pwsafe-mobile)](https://papke.it/jenkins/job/pwsafe-mobile/)
[![Code Analysis](https://img.shields.io/badge/code%20analysis-available-blue.svg)](https://papke.it/sonar/overview?id=1)

Overview
-------------
This is a alternative web frontend based on JQuery Mobile for the original webpasswordsafe created by Josh Drummond.

![Screenshot](https://raw.githubusercontent.com/chrisipa/pwsafe-mobile/master/public/screenshot_login.png)

Features
-------------
* Web frontend optimized for smartphones and tablets
* 2-factor authentication with google authenticator
* Multi language support (i18n)

Prerequisites
-------------
* [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or higher must be installed
* Web application server (e.g. Tomcat) must be installed
* [webpasswordsafe](https://github.com/chrisipa/webpasswordsafe) must be installed

Installation
-------------
* Download and extract the war file to web apps folder:
```
cd /opt/tomcat/webapps
wget https://papke.it/jenkins/job/pwsafe-mobile/lastStableBuild/de.papke%24pwsafe-mobile/artifact/de.papke/pwsafe-mobile/1.0.7/pwsafe-mobile-1.0.7.war -O pwsafe-mobile.war
mkdir pwsafe-mobile 
unzip pwsafe-mobile.war -d pwsafe-mobile 
rm pwsafe-mobile.war
```
* Optional: Change web service url of webpasswordsafe application:
```
nano /opt/tomcat/webapps/pwsafe-mobile/WEB-INF/classes/application.properties

# Web service rest url for web password safe
pwsafe.webservice.url=http://localhost:8080/webpasswordsafe/rest
```
* Optional: Activate 2-factor authentication:
```
nano /opt/tomcat/webapps/pwsafe-mobile/WEB-INF/classes/application.properties

# Use 2-factor authentication with google authenticator
google.authenticator.enabled=true
```
* Change secret for 2-factor authentication and your username:
```
# Secrets for google authenticator: You must change these values in a production environment !!!
admin=6ITCMBNB2L6AHRJT
```
