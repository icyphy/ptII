# Tests for ant
#
# @Author: Christopher Brooks
#
# $Id: Release.tcl 62883 2012-01-27 22:44:23Z bldmastr $
#
# @Copyright (c) 2012 The Regents of the University of California.
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

# Get rid of any previous lists of .java files etc.
exec make clean

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

set dir [pwd]
test ant-1.1 {Check the build.default.xml file} {
    cd $PTII
    if {[file exists build.xml]} {
	exec cp build.xml build.xml.bak
    }
    puts "# Copying build.default.xml to build.xml"
    exec cp build.default.xml build.xml
    puts "# Running ant clean"
    puts [exec ant clean]
    puts "# Running ant" 
    puts [exec ant]

} {}

cd $dir

test ant-1.2 {Rebuild using build.xml file} {
    cd $PTII
    if {[file exists build.xml.bak]} {
	exec mv build.xml.bak build.xml
	puts "# Running ant" 
	puts [exec ant]
    }
} {}

cd $dir