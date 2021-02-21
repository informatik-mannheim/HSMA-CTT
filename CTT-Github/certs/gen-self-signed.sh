#!/bin/sh
openssl req -x509 -newkey rsa:4096 -keyout app.key -out app.pem -subj '/CN=localhost' -nodes -days 365