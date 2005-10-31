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

# ${1#-} will not work under Solaris 8
#while [ "${1#-}" != "$1" ]; do
# jode takes a -d argument, which causes problems unless we use "x..."
while [ "x$1" != "x" -a  "x`echo $1 | egrep '^-'`" = "x$1" -a $# -gt 0 ]; do
    case $1 in
        -h|-help|--help)
	    shift    
	    echo "$0: Usage: $0 -zip"
	    echo "    Zip up the opts and opts-nolocalincludes files from the "
            echo "    tinyos-1.x directory.  These files contain the necessary"
            echo "    compiler options for running nc2moml and ncapp2moml."
	    echo "    Flags:"
	    echo "        -zip Zip up the files."
            echo ""
            echo "    Warning: this will delete and overwrite "
            echo "    $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz"
	    exit 0
            ;;

        -zip)
            shift
            ZIP=1
            continue
            ;;
	*)
	    echo "$0: Error: Don't understand '$1' argument, run with -h for help"
	    exit 3
	    ;;
    esac
done

if [ -z $ZIP ]; then
    echo " Did nothing."
    echo "   Run this command with the -h flag to see the options."
    exit 0
fi

cd $SRC_DIR

# WARNING: This deletes the existing file.
echo "/bin/rm -f $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz"
/bin/rm -f $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz

find . -name opts -printf "%p "| xargs tar -cvf $OUTPUT_DIR/$OUTPUT_FILENAME.tar

# Add the hacky files that are used to avoid including the current directory.
find . -name opts-nolocalincludes -printf "%p "| xargs tar -rvf $OUTPUT_DIR/$OUTPUT_FILENAME.tar

gzip $OUTPUT_DIR/$OUTPUT_FILENAME.tar

echo "Created $OUTPUT_DIR/$OUTPUT_FILENAME.tar.gz"
