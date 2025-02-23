LabCAS Backend
==============

Repository containing back-end services and configuration for executing EDRN LabCAS data processing workflows.


Documentation
-------------

See the `docs/documentation.pdf` file.


Development
-----------

To build locally, maybe try:

    mkdir /tmp/labcas
    export "JAVA_HOME=`/usr/libexec/java_home --version 1.8.0`"
    export LABCAS_HOME=/tmp/labcas
    export PATH=${JAVA_HOME}/bin:$PATH
    mvn clean install





# LabCAS Back-End: Installation from Docker
This page explains how to install and run an instance of the LabCAS back-end services as a Docker container. Because of its simplicity, this is the recommended way to run LabCAS.
## Pre-Requisites

You must have Docker Engine installed on your personal system (Mac OSX, Windows or Linux), a basic understanding of how Docker works, and preferably you should have run a test command to verify that your Docker installation does indeed work. For example, you can run the following command to verify that your Docker installation works:

    $ docker run ubuntu /bin/echo hello world
**References:**

Docker installation: https://docs.docker.com/engine/installation/ Docker MacOSX tutorial: https://docs.docker.com/docker-for-mac/ Docker Windows tutorial: https://docs.docker.com/docker-for-windows/ Docker Linux tutorial: https://docs.docker.com/engine/installation/linux/
## Installation and Operation

Start a Docker terminal on your system 
Download the image:

    $ docker pull edrn/labcas-backend:latest

Start a container based on the previous image. Give the container a unique name ("mylabcas") and make the standard ports available to clients. The command will also create a terminal connection to the container, and log you in as the default user "labadmin" (which is a user who is authorized to run the LabCAS services):

    $ docker run -p 8983:8983 -p 9001:9001 --name mylabcas -ti edrn/labcas-backend    
Once inside the container, start the LabCAS back-end services. Keep this terminal open to view the server-side logs.

    [labadmin@3c283eb1b827]$ $LABCAS_HOME/start.sh

Start another Docker terminal where you will be issuing client-side commands. Connect the second Docker terminal to the same running LabCAS container (by referencing the "mylabcas" name):

    $ docker exec -it mylabcas /bin/bash

Submit a test workflow:

    [labadmin@3c283eb1b827]$ cd $LABCAS_HOME/cas-workflow/bin
    [labadmin@3c283eb1b827]$ ./wmgr-client --url http://localhost:9001 --operation --sendEvent --eventName labcas-test --metaData --key experiment 11 --key species snakes

Verify that products were generated and ingested by using your browser to query the standard Solr URL.

    $ wget 'http://localhost:8983/solr/oodt-fm/select?q=*%3A*&wt=json&indent=true'

Stop the container (and the services inside the container). In the Docker terminal where you first started the container, type "exit". This will also disconnect the second terminal window.

    [labadmin@3c283eb1b827]$ exit
Restart the container. Connect again to that container via a shell, then restart the services. Verify that the published data (generated by the test workflow) is preserved across starting/stopping a container:

    $ docker start mylabcas
    $ docker exec -it mylabcas /bin/bash
    [labadmin@3c283eb1b827]$ $LABCAS_HOME/start.sh
    $ wget 'http://localhost:8983/solr/oodt-fm/select?q=*%3A*&wt=json&indent=true'
To create a new container from the same image, in a clean state (i.e. no data is restored), first delete the previous container, then restart a new one with the same name (or, create a new container with a different name). Then proceed as before to start all the services.

    $ docker rm mylabcas
    $ docker run -p 8983:8983 -p 9001:9001 --name mylabcas -ti edrn/labcas-backend
    [labadmin@3c283eb1b827]$ $LABCAS_HOME/start.sh
To start or connect to a container as "root" user, use the -u option to override the default image user:

    $ docker run -p 8983:8983 -p 9001:9001 --name mylabcas -ti -u root edrn/labcas-backend
    $ docker exec -it mylabcas -u root /bin/bash


