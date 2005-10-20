#!/bin/sh

# This script will generate nesc-dump.jar, assuming the nesc src
# directory contains both the .java and .class files.  If using
# Eclipse, you should set up the nesc project to use the source
# directory as the output directory.

###########################################################################
#   SETTINGS
###########################################################################

# Source directory for the nesc java tools.
SRC_DIR=/home/celaine/tinyos/nesc/tools/java

# Note: if using the nc2moml install instructions, use this SRC_DIR instead.
#SRC_DIR=$PTII/vendors/ptinyos/nesc/tools/java

# Temporary directory for storing the jar sources.
# Warning!!! This directory will be automatically deleted!!!
TEMP_DIR=/tmp/nesc-dump

# Output directory for the jar file.
OUTPUT_DIR=$PTII/ptolemy/domains/ptinyos/lib

# Name of the jar file, without suffix.
OUTPUT_FILENAME=nesc-dump

###########################################################################

# Delete the output directory so that we can start from scratch, in
# case it already exists.
/bin/rm -rf $TEMP_DIR

# Create the output directory.
mkdir $TEMP_DIR

# Archive the files, but not CVS directories or files with pattern *~
gtar -C $SRC_DIR --exclude=CVS --exclude=*\~ -czf $TEMP_DIR/$OUTPUT_FILENAME.tgz net
echo "Created $OUTPUT_FILENAME.tgz in $TEMP_DIR"

# Extract the files.
cd $TEMP_DIR
gtar -xzf $OUTPUT_FILENAME.tgz
echo "Extracted $OUTPUT_FILENAME.tgz in $TEMP_DIR"

# Jar the files.
jar cvf $OUTPUT_DIR/$OUTPUT_FILENAME.jar net
echo "Created $OUTPUT_FILENAME.jar in $OUTPUT_DIR"

# Delete the $TEMP_DIR
#/bin/rm -rf $TEMP_DIR
#echo "Deleted $TEMP_DIR"
