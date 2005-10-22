#!/bin/sh

# FIXME: move this file to tinyos-1.x?

# This script will generate opts.tar.gz, assuming the tinyos-1.x src
# directory contains files named "opts" containing necessary ncc
# compiler options for the subdirectories where the file is located.

###########################################################################
#   SETTINGS
###########################################################################

# Source directory for the opts files (root of tinyos-1.x directory)
SRC_DIR=$PTII/vendors/ptinyos/tinyos-1.x

# Directory in which to generate .tar.gz file.
OUTPUT_DIR=$PTII/vendors/ptinyos/tinyos-1.x/contrib/ptII

# Name of the .tar.gz file to generate, without suffix.
OUTPUT_FILENAME=opts

###########################################################################

cd $SRC_DIR

find . -name opts -printf "%p "| xargs gtar -czvf $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz

echo "Created $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz"
