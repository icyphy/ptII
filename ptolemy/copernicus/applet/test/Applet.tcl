# Tests for the Applet class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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

# Ptolemy II bed, see $PTII/doc/coding/testing.html for more information.

# Set the timeOut to two hours
set timeOutSeconds 12000

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs sootCodeGeneration] == "" } then { 
    source [file join $PTII util testsuite codegen.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

test Applet-1.1 {Compile and run the SDF IIR test} {
    set result [sootCodeGeneration   [file join $relativePathToPTII ptolemy actor lib test auto IIR.xml] Applet] 
    puts $result
    list {}
} {{}}

test Applet-1.2 {Create an applet for a graphical demo that uses diva.jar in a directory outside of the Ptolemy II tree} {
    # ptapplet must exist or else the defaults will be used.
    file delete -force /tmp/ptapplet	
    file mkdir /tmp/ptapplet
	
    set args [java::new {String[]} 11 \
	[list \
	    [file join $relativePathToPTII ptolemy domains fsm demo \
		MultipleRuns MultipleRuns.xml] \
	    "-ptIIUserDirectory" "/tmp/ptapplet" \
	    "-targetPath" "modelName" \
	    "-targetPackage" "modelName" \
	    "-codeGenerator" "applet" \
	    "-run" "false"] ]
    java::new ptolemy.copernicus.kernel.Copernicus $args	
    list {}
} {{}}


