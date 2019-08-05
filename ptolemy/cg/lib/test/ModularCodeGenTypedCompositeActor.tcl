# Test ModularCodeGenTypedCompositeActor
#
# @Author: Christopher Brooks
#
#
# @Copyright (c) 2010-2019 The Regents of the University of California.
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}



#####
test ModularCodeGenTypedCompositeActor-1.1 {Run a Modular Code Generator model, verify that it creates the .class files} {
    if {[catch {file delete -force $env(HOME)/cg} errMsg]} {
	puts "Warning: failed to delete $env(HOME)/cg: $errMsg"
    }
    set r1 [list \
		[file exists $env(HOME)/cg/ModularCodeGenPubSub3_A1.class] \
		[file exists $env(HOME)/cg/ModularCodeGenPubSub3_A1_A2.class]]
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
		      [$parser parseFile ModularCodeGenTest.xml]]

    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]

    $toplevel setManager $manager

    # If this test hangs, then it probably happens here because
    # we are writing to stderr
    #jdkCapture {$manager execute} firstRun
    $manager execute

    # Run it twice
    jdkCapture {$manager execute} secondRun

    if [expr {[string length $secondRun] > 80}] {
	puts "SecondRun output:\n$secondRun"
	error "The second run produced output longer than 80 chars? which indicates that the recompilation occurred"
    }

    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "Recorder"]]

    set r2 [list \
		[file exists $env(HOME)/cg/ModularCodeGenPubSub3_A1.class] \
		[file exists $env(HOME)/cg/ModularCodeGenPubSub3_A1_A2.class]]
    list $r1 \
	[enumToTokenValues [$recorder getRecord 0]] \
	$r2
} {{0 0} {2 2} {0 0}}


test ModularCodeGenTypedCompositeActor-1.2 {change the value of a parameter in a compiled composite} {
    set scale [java::cast ptolemy.actor.lib.Scale \
            [$toplevel getEntity "A1.A2.Scale"]]
    set factor [getParameter $scale factor]
    $factor setExpression {10}

    # FIXME: If a parameter is changed, then the container should be marked as modified.
    set scaleContainer [java::cast ptolemy.cg.lib.ModularCodeGenTypedCompositeActor [$scale getContainer]]
    set recompileThisLevel [getParameter $scaleContainer recompileThisLevel]
    $recompileThisLevel setExpression {true}

    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "Recorder"]]
    list [enumToTokenValues [$recorder getRecord 0]]
} {{20 20}}

test ModularCodeGenTypedCompositeActor-1.3 {reparse the model and rerun} {
    if {[catch {file delete -force $env(HOME)/cg} errMsg]} {
	puts "Warning: failed to delete $env(HOME)/cg: $errMsg"
    }
    set r1 [list \
		[file exists $env(HOME)/cg/ModularCodeGenPubSub3_A1.class] \
		[file exists $env(HOME)/cg/ModularCodeGenPubSub3_A1_A2.class]]
    
    
    # Note that not calling anything or calling parser reset here fails, but if we 
    # call parser resetAll, then it works
    #$parser reset
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
		      [$parser parseFile ModularCodeGenTest.xml]]

    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager

    $manager execute
} {}


