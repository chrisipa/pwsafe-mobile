PWSafe Mobile
=============

[![Build Status](https://papke.it/jenkins/buildStatus/icon?job=pwsafe-mobile)](https://papke.it/jenkins/job/pwsafe-mobile/)
[![Code Analysis](https://img.shields.io/badge/code%20analysis-available-blue.svg)](https://papke.it/sonar/overview?id=1)

# Overview

This is a alternative web frontend based on JQuery Mobile for the original webpasswordsafe created by Josh Drummond.

![Screenshot](https://raw.githubusercontent.com/chrisipa/pwsafe-mobile/master/public/screenshot_login.png)

# Features

* Web frontend optimized for smartphones and tablets
* 2-factor authentication with google authenticator
* Multi language support (i18n)

# Installation

* Manual installation instructions can be found [here](INSTALLATION.md)

# Docker

The Web Password safe docker image is based on Debian Jessie, Oracle JDK 8 and Apache Tomcat 7.

## Description

This password safe docker image contains the following software components:

 - [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 - [Apache Tomcat 7](http://tomcat.apache.org)
 - [webpasswordsafe](https://github.com/chrisipa/webpasswordsafe)
 - [pwsafe-mobile](https://github.com/chrisipa/pwsafe-mobile)

For data storage you will need a relational database. At the moment, these databases are supported:

 - [HSQLDB](http://hsqldb.org/)
 - [MySQL](http://www.mysql.com/)
 - [PostgreSQL](http://www.postgresql.org/)

### Ports

Both tomcat http ports are exposed:

 - 8080 (http)
 - 8443 (https)

## How to run the container

### Environment variables

When you start the password safe container, you can adjust the configuration by passing one or more environment variables on the `docker run` command line:

#### `PASSPHRASE`

 - The passphrase for jasypt encryptor
 - Please specify a strong password here
 - You will not be able to retrieve old passwords when you change it after initial configuration
 - Default value: `w3bp@$$w0rd$@f3k3y`

#### `DB_TYPE`

 - The database type to use
 - Possible values: `hsqldb`, `mysql`, `postgresql`
 - Default value: `hsqldb`

#### `DB_HOST`

 - The database hostname or ip address as string
 - Default value: `$MYSQL_PORT_3306_TCP_ADDR` or `webpasswordsafe-mysql`

#### `DB_PORT`

 - The database port as a numeric value
 - Default value: `$MYSQL_PORT_3306_TCP_PORT` or `3306`

#### `DB_NAME`

 - The database name as string
 - Default value: `$MYSQL_ENV_MYSQL_DATABASE` or `webpasswordsafe`

#### `DB_USER`

 - The database user as string
 - Default value: `$MYSQL_ENV_MYSQL_USER` or `webpasswordsafe`

#### `DB_PASS`

 - The database password as string
 - Default value: `$MYSQL_ENV_MYSQL_PASSWORD` or `my-password`

### Using docker

#### Example 1: Evaluation usage without persistent data storage

* Run password safe container in foreground with this command:
  ```
  docker run --rm -p 8080:8080 -p 8443:8443 chrisipa/pwsafe-mobile
  ```

#### Example 2: MySQL server on external host with default port

1. Make sure that your mysql database server allows [external access](http://www.cyberciti.biz/tips/how-do-i-enable-remote-access-to-mysql-database-server.html)

2. Create a database with name `webpasswordsafe` and allow user `webpasswordsafe` to access it

3. Run the password safe container with the following command:
  ```
  docker run --name webpasswordsafe-tomcat -d -p 8080:8080 -p 8443:8443 -e PASSPHRASE=my-passphrase -e DB_HOST=192.168.0.1 -e DB_PASS=my-password chrisipa/pwsafe-mobile
  ```

#### Example 3: MySQL server as docker container on the same docker host

1. Run mysql container with this command:
  ```
  docker run --name webpasswordsafe-mysql -d -e MYSQL_ROOT_PASSWORD=my-root-password -e MYSQL_DATABASE=webpasswordsafe -e MYSQL_USER=webpasswordsafe -e MYSQL_PASSWORD=my-password -v /opt/docker/webpasswordsafe/mysql:/var/lib/mysql mysql:latest
  ```

2. Run password safe container by linking to the newly created mysql container:
  ```
  docker run --name webpasswordsafe-tomcat --link webpasswordsafe-mysql:mysql -d -p 8080:8080 -p 8443:8443 -e PASSPHRASE=my-passphrase chrisipa/pwsafe-mobile
  ```

#### Example 4: Running docker containers with compose

1. Create docker compose file `docker-compose.yml` with your configuration data:
  ```yml
  mysql:
    image: mysql
    volumes:
      - /opt/docker/webpasswordsafe/mysql:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=my-root-password
      - MYSQL_DATABASE=webpasswordsafe
      - MYSQL_USER=webpasswordsafe
      - MYSQL_PASSWORD=my-password

  tomcat:
    image: chrisipa/pwsafe-mobile
    links:
      - mysql:mysql
    ports:
      - 8080:8080
      - 8443:8443
    environment:
      - PASSPHRASE=my-passphrase
  ```

2. Run docker containers with docker compose:
  ```
  docker-compose up -d
  ```

### Advanced topics

#### Use your own SSL certificates

See parent image: [chrisipa/tomcat](https://github.com/chrisipa/docker-library/tree/master/debian-pom/java-pom/tomcat-pom/tomcat#use-your-own-ssl-certificates)

#### Accept self signed SSL certificates from Jenkins JRE

See parent image: [chrisipa/jdk](https://github.com/chrisipa/docker-library/tree/master/debian-pom/java-pom/jdk#accept-self-signed-ssl-certificates-from-jre)

### Caveats

1. Access the GWT version of the password safe to configure your settings (default username: admin, default password: admin) [https://localhost:8443/webpasswordsafe](https://localhost:8443/webpasswordsafe)

2. Access the mobile version of the password safe on your smartphone or tablet: [https://localhost:8443/pwsafe-mobile](https://localhost:8443/pwsafe-mobile)