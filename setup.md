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

## Setup

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
    USER_CREDENTIALS=user,$2a$10$WUJevKFYLHfIheVZ3yv7J.7uIHeoPV8fAb9wFqdW50kFD8O4EWJ4u,USER;
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
