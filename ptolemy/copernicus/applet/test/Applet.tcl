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
    foreach file [glob $autoDirectory/*.xml] {
	if { [string last FileWriter2.xml $file] != -1 \
		|| [string last ReadFile2.xml $file] != -1 } {
		next
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

# Generate code for all the xml files in subdirectories
proc autoAppletDemoCG {autoDirectory} {
    set i 0
    foreach file [glob $autoDirectory/*/demo/*/*.xml] {
	puts "---- testing $file"
	incr i
	test "Auto-$i" "Automatic test in file $file" {
	    set elapsedTime [time {sootCodeGeneration $file Applet 1000}]
	    puts "soot took [expr {[lindex $elapsedTime 0] / 1000000.0}] seconds"
	    list {}
	} {{}}
	java::call System gc
    }
}

######################################################################
####
#


#test Applet-1.1 {Compile and run the Orthocomm test} {
#    set result [sootAppletCodeGeneration \
#  	    [file join $relativePathToPTII ptolemy actor lib test auto \
#	    RecordUpdater.xml]]
#ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom]
#    lrange $result 0 9
#} {2 4 6 8 10 12 14 16 18 20}


# Do a SDF and a DE test just be sure things are working
test Applet-1.2 {Compile and run the SDF IIR test} {
    set result [sootCodeGeneration   [file join $relativePathToPTII ptolemy actor lib test auto IIR.xml] Applet] 
    puts $result
    list {}
} {{}}


test Applet-1.3 {Compile and run the DE Counter test} {
    set result [sootCodeGeneration \
	    [file join $relativePathToPTII ptolemy actor lib test auto \
		 Counter.xml] Applet]
    puts $result
    list {}
} {{}}
 
test Applet-1.3 {Compile and run the MathFunction test, which tends to hang} {
    set result [sootCodeGeneration \
	    [file join $relativePathToPTII ptolemy actor lib test auto \
	    MathFunction.xml] Applet] 
    puts $result
    list {}
} {{}}


# Generate applets for all the demos
# Note that this currently fails in the nightly build if the demo
# has a Display Actor
#autoAppletDemoCG [file join $relativePathToPTII ptolemy domains]


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

# Print out stats
#doneTests


