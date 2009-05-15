# Tests for the MoMLCommandLineApplication class 
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1



######################################################################
####
#
test MoMLCommandLineLocation-1.1 {} {
    # $makeArguments is set in ptolemy/util/test/testDefs.tcl
    # Use "make $makeArguments" so that commands are not printed as they
    # are executed
    if {$makeArguments == {}} {
	set results [exec make MoMLCommandLineApplication1]
    } else {
	set results [exec make $makeArguments MoMLCommandLineApplication1]
    }
    regsub -all [java::call System getProperty "line.separator"] \
	        $results "\n" results2

    string range $results2 0 7
} {A String}

test MoMLCommandLineLocation-1.2 {Set the parameter to 2} {
    if {$makeArguments == {}} {
	set results [exec make MoMLCommandLineApplication2]
    } else {
	set results [exec make $makeArguments MoMLCommandLineApplication2]
    }
    regsub -all [java::call System getProperty "line.separator"] \
	        $results "\n" results2
    string range $results2 0 1
} {2
}

test MoMLCommandLineLocation-1.3 {Set the parameter to a string} {
    if {$makeArguments == {}} {
	set results [exec make MoMLCommandLineApplication3]
    } else {
	set results [exec make $makeArguments MoMLCommandLineApplication3]
    }
    regsub -all [java::call System getProperty "line.separator"] \
	        $results "\n" results2
    string range $results2 0 11
} {Hello, World}

test MoMLCommandLineLocation-1.4 {Set the director iterations parameter to a 2} {
    if {$makeArguments == {}} {
	set results [exec make MoMLCommandLineApplication4]
    } else {
	set results [exec make $makeArguments MoMLCommandLineApplication4]
    }
    regsub -all [java::call System getProperty "line.separator"] \
	        $results "\n" results2
    string range $results2 0 16
} {A String
A String}

