# Check that $PTII/.classpath.default refers to jar files that exist.
#
# @Author: Christopher Brooks
#
# $Id: Release.tcl 63463 2012-05-02 02:47:37Z hudson $
#
# @Copyright (c) 2016-2017 The Regents of the University of California.
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
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

#set VERBOSE 1
# Get rid of any previous lists of .java files etc.
exec make clean

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Load up the test definitions.
if {[string compare test [info procs nightlyMake]] == 1} then {
    source ../installers2/test/nightlyMake.tcl
} {}


set startingDirectory [pwd]
set gendir $PTII

test checkEclipseUpdate-1.0 {Check that $PTII/.classpath.default refers to jar files that exist.} {
    # If this fails, it probably means that $PTII/.classpath.default
    # was not updated after jar files in $PTII/lib were updated.
    set results [exec make --no-print-directory --silent checkClasspathDefault]
} {}

test checkEclipseUpdate-2.0 {Check that $PTII/.classpath.default refers to jar files in $PTII/lib.} {
    # If this fails, it probably means that $PTII/.classpath.default
    # was not updated after jar files in $PTII/lib were updated.
    set results [exec make --no-print-directory --silent checkClasspathDefaultMissing]
} {Below are jar files in $PTII/lib that are not in .classpath.default:
lib/jna-4.1.0-variadic.jar
lib/nrjavaserial-3.11.0.devel.debug.jar}


