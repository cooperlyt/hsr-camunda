#!/usr/bin/env bash


#[ -z $CONSULSERVER_HOST ] && CONSULSERVER_HOST=$(cat /opt/config/ip)
#echo "********************************************************"
#echo "Waiting for the consul server to start on port $CONSULSERVER_HOST : $CONSULSERVER_PORT"
#echo "********************************************************"
#./wait-for-it.sh -t 0 $CONSULSERVER_HOST:$CONSULSERVER_PORT -- echo "******* Consul Server has started"

echo "********************************************************"
echo "Starting @project.build.finalName@ Service with $(hostname -I | cut -d' ' -f1):$SERVER_PORT - $PROFILE"
echo "********************************************************"
java -Djava.security.egd=file:/dev/./urandom -Dserver.port=$SERVER_PORT   \
     -Dspring.cloud.consul.port=$CONSULSERVER_PORT  \
     -Dspring.cloud.consul.host=$CONSULSERVER_HOST               \
     -Dspring.profiles.active=$PROFILE                          \
     -Dspring.datasource.url=$DB_URL \
     -Dspring.datasource.username=$DB_USERNAME \
     -Dspring.datasource.password=$DB_PWD \
     -jar /usr/local/app/@project.build.finalName@.jar