# Tests for the Backtracking
#
# @Author: Christopher Brooks
#
# @Version: $Id$
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
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

#
# Invoke the main method of className and return the results of stdout.
proc backtrack {className} {
    jdkCapture {    	
        if [catch {
		java::call ptolemy.backtrack.test.$className main \
		    [java::new {String[]} 0]} errMsg] {
	    puts $errMsg
	}
    } results
    return $results
}

puts "Running 'make backtrack' to run Transformer"
if [catch {set results [exec -stderrok make backtrack]} errMsg] {
    error $errMsg	
} else {
    puts $results	
}


######################################################################
####
#
test Backtrack-1.1 {Test1Main} {
    backtrack Test1Main
} {0 1 2 3 3 2 1 0 
}

######################################################################
####
#
test Backtrack-2.1 {Test2Main} {
    backtrack Test2Main
} {1 1 2 2 3 3 28 28 37 37 6 6 7 7 
}
