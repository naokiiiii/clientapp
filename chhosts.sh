#!/bin/bash
SDK_HOME=/Users/naoki/Library/Android/sdk
SERVERAPP_IP=192.168.59.103

${SDK_HOME}/platform-tools/adb shell mount -o rw,remount /dev/block/mtdblock0 /system
${SDK_HOME}/platform-tools/adb pull /system/etc/hosts .
echo "$SERVERAPP_IP		mobilenurse.t4j.com" >> hosts
${SDK_HOME}/platform-tools/adb push hosts /system/etc/hosts

echo "edit Andoroid hosts"
cat hosts
rm hosts

