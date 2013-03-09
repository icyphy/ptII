# Test the G4 Linear Temporal Logic facility.
#
# @Author: Christopher
#
# @Version: $Id: Ramp.tcl 64744 2012-10-01 22:51:43Z cxh $
#
# @Copyright (c) 2013 The Regents of the University of California.
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

if {[string compare test [info procs jdkCaptureErr]] == 1} then {
   source [file join $PTII util testsuite jdktools.tcl]
} {}

proc generateMoML {ltlFileName context {optionTechnique 0} {unrollSteps 1} {findStrategy true} } {
    set ltlFile [java::new java.io.File $ltlFileName]
    set results [java::call ptolemy.vergil.basic.imprt.g4ltl.G4LTL generateMoML \
		 $ltlFile $optionTechnique $unrollSteps $findStrategy $context]
    return $results
}

proc synthesizeFromFile {ltlFileName {optionTechnique 0} {unrollSteps 1} {findStrategy true} } {
    set solver [java::new g4ltl.SolverUtility]
    set ltlFile [java::new java.io.File $ltlFileName]
    set results [java::call ptolemy.vergil.basic.imprt.g4ltl.G4LTL synthesizeFromFile \
		 $solver $ltlFile $optionTechnique $unrollSteps $findStrategy]
    return $results
}

set parser [java::new ptolemy.moml.MoMLParser]


## HOW TO ADD A TEST for G4LTL
## The g4ltl facility adds a FSM actor to the model. 
## 1. Open the model in vergil, import the ltl file.
## 2. Replace the Display actors with Test actors
## 3. Connect up the FSM actor to the Test actors
## 4. Train the model by setting the training parameter in a Test actor, running the model
## and then unsetting the training parameter.
## 5. Save a version with the FSM Actor in it.
## 6. Delete the FSM Actor
## 7. Save the model in this directory.  I suggest adding "Test" to the name: FooTest.xml
## 8. Diff the version with the FSMActor and the version without.  The portion that adds relations
## and links relations and ports goes inside the test. 
## See G4LTL-2.1 as an example.

######################################################################
####
#
test G4LTL-1.1 {Synthesize from ArbitorLTL.txt} {
    jdkCaptureOutAndErr { 
	set moml [synthesizeFromFile $PTII/ptolemy/vergil/basic/imprt/g4ltl/demo/Arbitor/ArbitorLTL.txt]
    } outputMsg errMsg


    $parser resetAll
    set model [java::cast ptolemy.domains.modal.kernel.FSMActor [$parser parse $moml]]

    # Check that there are some ports present
    set req1 [[$model getPort req1] getName]
    set req2 [[$model getPort req2] getName]
    set grant1 [[$model getPort grant1] getName]
    set grant2 [[$model getPort grant2] getName]
    list $req1 $req2 $grant1 $grant2 [string range $outputMsg 1 195]
} {req1 req2 grant1 grant2 {ewritten as       : ( ( true ) U ( ( ( "req1" ) /\ ( ( false ) V ( ! ( "grant1" ) ) ) ) \/ ( ( ( "grant1" ) /\ ( "grant2" ) ) \/ ( ( "req2" ) /\ ( ( false ) V ( ! ( "grant2" ) ) ) ) ) ) )

******}}


######################################################################
####
#
test G4LTL-2.1 {Synthesize from ArbitorLTL.txt and merge it into the ArbitorTest.xml} {
    $parser resetAll
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile ArbitorTest.xml]]
    set result [generateMoML $PTII/ptolemy/vergil/basic/imprt/g4ltl/demo/Arbitor/ArbitorLTL.txt $toplevel]

    set request [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
      <entity name=".ArbitorTest" class="ptolemy.actor.TypedCompositeActor">
	<relation name="relation4" class="ptolemy.actor.TypedIORelation">
	</relation>
	<relation name="relation5" class="ptolemy.actor.TypedIORelation">
	</relation>
	<link port="model1.req1" relation="relation2"/>
	<link port="model1.req2" relation="relation"/>
	<link port="model1.grant1" relation="relation4"/>
	<link port="model1.grant2" relation="relation5"/>
	<link port="Grant1.input" relation="relation4"/>
	<link port="Grant2.input" relation="relation5"/>
      </entity>
    }]
    $toplevel requestChange $request
    createBasicModelErrorHandler $toplevel
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] myManager]
    $toplevel setManager $manager
    [$toplevel getManager] execute
} {}

