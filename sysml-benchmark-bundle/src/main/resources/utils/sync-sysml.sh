#!/bin/bash

SCRIPTPATH=$( cd "$(dirname "$0")" ; pwd )
PEELROOT=$( cd $SCRIPTPATH; cd ..; pwd )

# Github data
USER=stratosphere
REPO=incubator-systemml
BRANCH=flink-lr

ARCHIVE_NAME=$BRANCH.zip
URL="https://github.com/$USER/$REPO/archive/$ARCHIVE_NAME"
FOLDER_NAME=$REPO-$BRANCH

# download archive
echo "Downloading branch $BRANCH from repository $USER/$REPO..."
cd "$PEELROOT/downloads"
if [ -f $ARCHIVE_NAME ]; 
then 
	echo "Removing $ARCHIVE_NAME..."
	rm -rf $ARCHIVE_NAME 
fi
wget $URL

# unzip archive
if [ -f $FOLDER_NAME ]; 
then 
	echo "Removing $FOLDER_NAME..."
	rm -rf $FOLDER_NAME 
fi
unzip $ARCHIVE_NAME

# build systemml
echo "Building SystemML..."
cd $FOLDER_NAME
mvn -DskipTests -Dhadoop.version=2.7.1 -T1.0C clean package

# copy SystemML.jar and scripts to apps

mkdir "$PEELROOT/apps"
cp "target/SystemML.jar" "$PEELROOT/apps/"
cp "target/lib/hadoop-mapreduce-client-jobclient-2.7.1.jar" "$PEELROOT/apps/"
cp -r "scripts" "$PEELROOT/apps/"


