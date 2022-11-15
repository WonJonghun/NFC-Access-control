#!/bin/sh
cd /home/pi/main/
sudo java -Dsun.security.smartcardio.library=/usr/lib/arm-linux-gnueabihf/libpcsclite.so.1 -cp "/opt/pi4j/lib/*:/opt/kafka/libs/*":. NFCreader