# Tests for Licensing problems
#
# @Author: Christopher Brooks
#
# $Id: Release.tcl 59969 2010-12-13 00:09:52Z bldmastr $
#
# @Copyright (c) 2011 The Regents of the University of California.
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

test license-1.1 {Check to see if BasicGraphFrame was checked in with itextpdf enabled} {
    set result {}
    set file $PTII/ptolemy/vergil/basic/BasicGraphFrame.java
    set fp [open $file]
    while 1 {
	gets $fp line
	if [eof $fp] {
	    break
     	}
	if [regexp {^ *String exportPDFActionClassName = "ptolemy.vergil.basic.itextpdf.ExportPDFAction"} $line] {
	    set result "$file contains \n$line\nWhich is not commented out!\nThis line should be commented out so that Export PDF is disabled.\nWe don't want it always enabled because ptiny, the applets and\nWeb Start should not included this AGPL'd piece of software"
        }
    }
    list $result
} {{}}
