# Deploying CTT on a server

## Table of Content

* [Software Requirements](#Software-Requirements)
* [Setup](#Setup)
* [Configuration](#Configuration)
* [Container Architecture](#Container-Architecture)

## Software Requirements  

CTT only requires Docker and Docker-Compose to be installed on the server. Provided links are for an Ubuntu installation.

* Docker: <https://docs.docker.com/engine/install/ubuntu/>
* Docker-Compose: <https://docs.docker.com/compose/install/#install-compose-on-linux-systems>

## Detailed Docker Setup for Windows

### Installing Docker Desktop
1. Installation 
To download Docker Desktop (4.3.2), use the following link (01/2022):
[Download Docker Desktop](https://hub.docker.com/editions/community/docker-ce-desktop-windows/)

Follow the instructions.
Make sure the checkbox to install required Windows components for WSL 2 is checked:
![image](https://user-images.githubusercontent.com/63195459/151388075-a8efa128-80d7-43dc-a75e-61e30ebc0799.png)

In case the WSL 2 installation is incomplete, follow the following steps:
![image](https://user-images.githubusercontent.com/63195459/151388138-86a987c8-b631-4ebf-8f42-62393f4258dc.png)

1.	Click on the link in the menu or [here](https://docs.microsoft.com/de-de/windows/wsl/install-manual#step-4---download-the-linux-kernel-update-package)
2.	Download the recent update package for the WSL2 Linux kernel for your computer (Step 4)
3.	After installing the Linux kernel, click on ‘Restart’ to restart Docker Desktop.
4.	Either step through the Docker tutorial or skip it.

### Installing Ubuntu
Go to the Microsoft Store and install ‘Ubuntu 20.04 LTS’

### Activating WSL2
1. Check if Linux subsystems are activated. To do so navigate to the Windows features: Windows search bar -> Control Panel -> Programs -> Turn Windows features on or off
2. Check if ‘Virtual Machine Platform’ and ‘Windows Subsystem for Linux’ are checked: 
![image](https://user-images.githubusercontent.com/63195459/151388427-52f802dc-99d0-4be4-b37a-208c20c43a36.png)

In case ‘Virtual Machine Platform’ is not displayed, go to Task Manger -> Performance  CPU and check if Virtualization is enabled. 
If this is the Case, continue follwing this Docker Setup (3. Start your Windows Command Terminal)
If this is not the case, enable Virtualization by following online instructions. 


3. Start your Windows Command Terminal (enter cmd in the Windows search bar).
4. Enter following code in the command line:
	wsl --set-default-version 2
    wsl --list -v

![image](https://user-images.githubusercontent.com/63195459/151388524-847a4a05-23dc-4d35-bee9-5c7090d6eeb1.png)

Then, change Ubuntu-20.04 to version 2:
    ``` conf
    wsl --set-version Ubuntu-20.04 2
    ```

To check if it succeeded enter: 
    ``` conf
    wsl --list -v
    ```
    
![image](https://user-images.githubusercontent.com/63195459/151388596-6a0eb82b-0cba-4131-bcbc-b1e34fe648ad.png)


### Enable WSL2 in Docker Desktop

![image](https://user-images.githubusercontent.com/63195459/151388684-6ead7bc8-eb0b-46e5-811a-48f792a59476.png)

Go to Settings (1) -> Resources (2) -> WSL Integration (3) -> enable Ubuntu-20.04 (4)
Then, apply and restart Docker Desktop.

### GitHub login in Docker

1.	Generate a personal access token: 
Github -> Settings -> Developer settings -> Personal access tokens -> Generate new Token
Tick public_repo and click ‘Generate Token’. This token is per default available for 30 days.
2.	Open Ubuntu 20.04.
3.	and enter following code:
a.	docker login docker.pkg.github.com -u <your-github-username>
b.	As password, enter your generated personal access token

### Check if Docker is working
1.	Open Ubuntu 20.04.
2.	Enter following code: docker-compose
If an error occurs, please check the steps above and redo the instructions.

### CTT Setup
1.	Create a directory, preferably with permissions restricted to the root user. For our server we use /var/local/ctt-app, but any location is valid. All further instructions will be relative to this folder.

2.	Create a certs directory and copy your key, certificate, and optionally a file with the password for the key from our [repository](https://github.com/informatik-mannheim/HSMA-CTT/tree/dev/certs) into the certs directory. The files should be named app.key and app.pem respectively. The file with the password must be linked in [nginx.conf](https://github.com/informatik-mannheim/HSMA-CTT/blob/dev/nginx.conf) under server -> ssl_password_file. Make sure the key can only be accessed by the root user!

3.	Create a file named .env. Enter following code (deployment specific configuration):

    
``` conf
    # Ensure the server is set to production mode
    SERVER_ENV=production
    # Override the URL used to construct absolute URLs
    URL_OVERRIDE=https://your.domain.com/
    # Override the public default database credentials
    DB_USER=XXbPWmwdRW
    DB_PW=grEtwOBI4E
    # Add the user credentials for the site, see docker-compose for more information
    # Note that user accounts are recreated on every startup from this variable and are not persisted anywhere else.
    USER_CREDENTIALS=admin,$2a$10$BMCXL.xl/nHYAZWHsXNS8u6pOIlIYUU.8kJWD7raecbz/8rKoeRvC,ADMIN;
    # AES Secret used to encrypt personal data in the database
    DB_ENCRYPTION_SECRET=corona-ctt-20201
```	
    
4.	Navigate to the created repository in step ‘CTT Setup, 2.’ (/ctt-app).
a.	cd ../../mnt
b.	Example to navigate to /ctt-app:  
cd ./c/Users/username/Desktop/CTT_Docker_App/var/local/ctt-app



5.	Copy the [docker-compose.yml](https://github.com/informatik-mannheim/HSMA-CTT/blob/dev/docker-compose.yml) and [nginx.conf](https://github.com/informatik-mannheim/HSMA-CTT/blob/dev/nginx.conf) files from the repository into the /ct-app repository. Neither should require further customization. If further customization is required, check the .env file for more information. 

6.	Run docker-compose pull && docker-compose up -d. This will ensure that the newest images are pulled and containers restarted, if necessary. The containers are configured to restart if anything breaks (cf. docker-compose.yml).

7.	Now open any Browser (preferably FireFox) and enter localhost:80
8.	To stop the server again use docker-compose down



## Docker Setup

1. Add the current user to the `docker` group to use docker commands without sudo.
1. Create a directory, preferably with permissions restricted to the root user. For our server we use `/var/local/ctt-app`, but any location is valid. All further instructions will be relative to this folder.  
1. Create a certs directory and copy your key, certificate, and optionally a file with the password for the key into the certs directory. The files should be named `app.key` and `app.pem` respectively. The file with the password must be linked in nginx.conf under server -> ssl_password_file. Make sure the key can only be accessed by the root user!
1. Create a file named `.env`. It will contain deployment specific configuration:

    ``` conf
    # Ensure the server is set to production mode
    SERVER_ENV=production
    # Override the URL used to construct absolute URLs
    URL_OVERRIDE=https://your.domain.com/
    # Override the public default database credentials
    DB_USER=XXbPWmwdRW
    DB_PW=grEtwOBI4E
    # Add the user credentials for the site, see docker-compose for more information
    # Note that user accounts are recreated on every startup from this variable and are not persisted anywhere else.
    USER_CREDENTIALS=admin,$2a$10$BMCXL.xl/nHYAZWHsXNS8u6pOIlIYUU.8kJWD7raecbz/8rKoeRvC,ADMIN;
    # AES Secret used to encrypt personal data in the database
    DB_ENCRYPTION_SECRET=corona-ctt-20201
    ```

1. Copy the `docker-compose.yml` and `nginx.conf` files from the repository. Neither should require further customization, if possible use the `.env` for this purpose.
1. Run `docker-compose pull && docker-compose up -d`. This will ensure that the newest images are pulled and containers restarted, if necessary. The containers are configured to restart if anything breaks (cf. docker-compose.yml).
1. To stop the server again use `docker-compose down`.

## Configuration

### Default passwords

If you haven't made changes to the password hashes provided in `docker-compose.yml`, the default login `admin` with password `admin` can be used.
Make sure to use safe passwords when deploying this app publicly!

### Import a room list

To configure the rooms available in the building(s) upload a `.csv` file containing a list of rooms to <https://your.domain.com/r/import>. The CSV is semicolon delimited with Windows (CLRF) line-endings with three columns: Building letter; room name; room capacity.  
An example row for room 'A008' in building 'A' :

``` csv
A;A008;20
```

### Printing room postings

The page containing the check-in QR code can be generated as a `.docx` document for each building at <https://your.domain.com/printout/rooms>.

## Container Architecture

``` diagramm
                 |          |                                                                        
                 |          |                                                                        
+----------------|----------|-------------------------------------+                                  
| Port 443(https)|          | Port 80 (http)        Ubuntu Server |                                  
| Proxied to App |          | Forwarded to https                  |                                  
|                |          |                                     |                                  
|          +-----|----------|-----+                               |                                  
|          | nginx - Reverse Proxy|                               |                                  
|          +-----------|----------+                               |                                  
|                      |                                          |                                  
|                      |                                          |                                  
| via http on port 8080|                                          |                                  
|                      |                                          |                                  
|            +---------|--------+ Port 5432 +------------------+  |                                  
|            |  CTT Java App    |-----------| Postgres DB      |  |                                  
|            +------------------+           +------------------+  |                                  
|                                                                 |                                  
|                                                                 |                                  
|                                                                 |                                  
+-----------------------------------------------------------------+                                  
```

### Components

#### Postgres DB

Provides persistance for the Java App. The Java App connects to the database via the internal docker network. The database is *not* reachable from the outside. An alternative database can be configured with the `SPRING_DATASOURCE_*` Java environment variables.

### Nginx Reverse Proxy

Handles encryption for https and upgrading unsecured http requests. It is the only component reachable from an external network. Https requests are internally proxied to the Java App.
