#!/bin/sh

# FIXME: move this file to tinyos-1.x?

# This script will unzip the opts.tar.gz file into the tinyos-1.x
# directory.  The opts.tar.gz file contains files named "opts"
# containing necessary ncc compiler options for the subdirectories
# where the opts files are located.

###########################################################################
#   SETTINGS
###########################################################################

# Name of the .tar.gz file to open
SRC_FILENAME=opts.tar.gz

# Directory in which the .tar.gz file is located.
SRC_DIR=$PTII/vendors/ptinyos/tinyos-1.x/contrib/ptII

# Output directory (root of tinyos-1.x directory).
OUTPUT_DIR=$PTII/vendors/ptinyos/tinyos-1.x

###########################################################################

gtar -C $OUTPUT_DIR -xzvf $SRC_DIR/$SRC_FILENAME

echo "Extracted $SRC_DIR/$SRC_FILENAME"
