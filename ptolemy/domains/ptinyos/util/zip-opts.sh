#!/bin/sh
# @Version: $Id$
# @Author: Elaine Cheong
#
# @Copyright (c) 2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY


# FIXME: move this file to tinyos-1.x?

# This script will generate opts.tar.gz, assuming the tinyos-1.x src
# directory contains files named "opts" containing necessary ncc
# compiler options for the subdirectories where the file is located.
# WARNING: This deletes/overwrites the existing file.

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

# WARNING: This deletes the existing file.
echo "/bin/rm -f $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz"
/bin/rm -f $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz

find . -name opts -printf "%p "| xargs tar -cvf $OUTPUT_DIR/$OUTPUT_FILENAME.tar

# Add the hacky files that are used to avoid including the current directory.
find . -name opts-kludge* -printf "%p "| xargs tar -rvf $OUTPUT_DIR/$OUTPUT_FILENAME.tar

gzip $OUTPUT_DIR/$OUTPUT_FILENAME.tar

echo "Created $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz"
