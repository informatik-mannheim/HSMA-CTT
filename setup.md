# Deploying CTT on a server

## Software Requirements  

CTT only requires Docker and Docker-Compose to be installed on the server. Provided links are for an Ubuntu installations.

* Docker: <https://docs.docker.com/engine/install/ubuntu/>
* Docker-Compose: <https://docs.docker.com/compose/install/#install-compose-on-linux-systems>

## Setup

1. Add the current user to the `docker` group to use docker commands without sudo.
1. Create a directory, preferably with permissions restricted to the root user. For our server we use `/var/local/ctt-app`, but any location is valid. All further instructions will be relative to this folder.  
1. Create a certs directory and copy your key and certificate into the certs directory. The files should be named `app.key` and `app.pem` respectively. Make sure the key can only be accessed by the root user!
1. Create a file named `.env`. It will contain deployment specific configuration:

    ``` conf
    hostname=www.your-domain.com
    # Override the public default credentials
    POSTGRES_USER=XXbPWmwdRW
    POSTGRES_PASSWORD=grEtwOBI4E
    SPRING_DATASOURCE_USERNAME=XXbPWmwdRW
    SPRING_DATASOURCE_PASSWORD=grEtwOBI4E
    ```

1. Copy the `docker-compose.yml` and `nginx.conf` files from the repository. Neither should require further customization, if possible use the `.env` for this purpose.
1. Run `docker-compose pull && docker-compose up -d`. This will ensure that the newest images are pulled and containers restarted, if necessary.

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
