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

# Generate code for all the xml files in a directory.
proc autoAppletCG {autoDirectory} {
    global KNOWN_FAILED	
    foreach file [glob $autoDirectory/*.xml] {
	if { [string last FileWriter2.xml $file] != -1 \
		|| [string last Legiotto.xml $file] != -1 \
		|| [string last ReadFile2.xml $file] != -1 } {
	    # Skip known failures
	    puts "Skipping Known Failure: $file"
	    incr KNOWN_FAILED
	    continue
	}
	puts "---- testing $file"
	test "Auto" "Automatic test in file $file" {
	    set elapsedTime [time {sootCodeGeneration $file Applet 1000}]
	    puts "soot took [expr {[lindex $elapsedTime 0] / 1000000.0}] seconds"
	    list {}
	} {{}}
	java::call System gc
    }
}

# Generate code all the demos where a demo model has the name 
# demo/Foo/Foo.xml thus avoiding running scripts like demo/Foo/xxx.xml
proc autoAppletDemoCG {autoDirectory} {
    global KNOWN_FAILED	
    set i 0
    foreach directory [glob $autoDirectory/*/demo/*] {
	set base [file tail $directory]
	set file [file join $directory $base.xml]
	if { [string last FileWriter2.xml $file] != -1 \
		|| [string last Legiotto.xml $file] != -1 \
		|| [string last ReadFile2.xml $file] != -1 } {
	    # Skip known failures
	    puts "Skipping Known Demo Failure: $file"
	    incr KNOWN_FAILED
	    continue
	}
	if [ file exists $file ] {
	    incr i
	    puts "---- $i testing $file"
	    test "Auto-$i" "Automatic test in file $file" {
	        set elapsedTime [time {sootCodeGeneration $file Applet 1000}]
	        puts "soot took [expr {[lindex $elapsedTime 0] / 1000000.0}] seconds"
	        list {}
	    } {{}}
	    java::call System gc
       }
    }
}

# Generate applets for all the domain demos
autoAppletDemoCG [file join $relativePathToPTII ptolemy domains]


# Now try to generate code for all the tests in the auto directories.
autoAppletCG [file join $relativePathToPTII ptolemy actor lib test auto]
autoAppletCG [file join $relativePathToPTII ptolemy actor lib conversions test auto]
autoAppletCG [file join $relativePathToPTII ptolemy actor lib javasound test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains ct lib test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains de lib test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains dt kernel test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains fsm kernel test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains fsm test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains giotto kernel test auto]
#autoAppletCG [file join $relativePathToPTII ptolemy domains hdf kernel test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains sdf kernel test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains sdf lib test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains sdf lib vq test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains sr kernel test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains sr lib test auto]

autoAppletCG [file join $relativePathToPTII ptolemy domains ct lib test auto]
autoAppletCG [file join $relativePathToPTII ptolemy domains wireless test auto]

# Print out stats
#doneTests


