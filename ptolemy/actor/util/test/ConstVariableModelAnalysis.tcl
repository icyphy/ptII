# Tests for constant variable model analysis
#
# @Author: Steve Neuendorffer
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

if {[info procs test_clone] == "" } then { 
    source [file join $PTII util testsuite testParameters.tcl]
}

if {[info procs test_clone] == "" } then { 
    source [file join $PTII util testsuite testParameters.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

######################################################################
####
#

# First, do an SDF test just to be sure things are working
test ConstVariableModelAnalysis-1.1 {Test simple model} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]
    $step setExpression init

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    # Use lsort for platform independence
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {{} {} {firingCountLimit init step} {}}


test ConstVariableModelAnalysis-1.2 {Test hierarchical dependance} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    $p0 setExpression 5
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {p0 {} {firingCountLimit init step} {}}

test ConstVariableModelAnalysis-1.3 {Test flat dependance} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression 5
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression p1
    $step setExpression p0
    
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {{p0 p1} {} {firingCountLimit init step} {}}


test ConstVariableModelAnalysis-1.4 {Test unbound variables} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression a
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression p1
    $step setExpression p0

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {{} {a p0 p1} firingCountLimit {init step}}


test ConstVariableModelAnalysis-1.5 {Test expressions of unbound variables} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression a
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + 1"
    $step setExpression p0

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {{} {a p0 p1} firingCountLimit {init step}}

test ConstVariableModelAnalysis-1.6 {Test a model that is not correct (circular dependancy)} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression "5"
    $p1 setExpression "p0"
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + a"
    $step setExpression "p0"

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]]
} {{p0 p1} {firingCountLimit step}}

test ConstVariableModelAnalysis-1.7 {Test unbound variables} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression "p1"
    $p1 setExpression "5"
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + unbound"
    $step setExpression 5

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {{p0 p1} a {firingCountLimit init step} {}}

test ConstVariableModelAnalysis-1.8 {test scoping} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set cinit [java::new ptolemy.data.expr.Parameter $e0 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e0 step]
    $cinit setExpression a
    $cstep setExpression 1
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "step + 5"
    $step setExpression a

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {step {a init} firingCountLimit {init step}}

test ConstVariableModelAnalysis-1.9 {test scoping} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set cinit [java::new ptolemy.data.expr.Parameter $e0 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e0 step]
    $cinit setExpression 1
    $cstep setExpression a
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "init + 5"
    $step setExpression a

    set varSet [java::new java.util.HashSet]
    $varSet add $a
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0 $varSet]
    list [lsort [listToNames [$analysis getConstVariables $e0]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e0]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]]
} {init {a step} {firingCountLimit init} step}

test ConstVariableModelAnalysis-3.1 {test port parameters} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
 
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
   
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    
    set inport [java::new ptolemy.actor.parameters.PortParameter $e1 inport]
    [$e1 getPort inport] link $r1
    
    set ramp [java::new ptolemy.actor.lib.Ramp $e1 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "inport"
    $step setExpression "1"

    set repeat [java::new ptolemy.actor.TypedAtomicActor $e1 repeat]
    set blockSize [java::new ptolemy.data.expr.Parameter $repeat blockSize]
    $blockSize setExpression "inport"
    set numberOfTimes [java::new ptolemy.data.expr.Parameter $repeat numberOfTimes]
    $numberOfTimes setExpression "5"
    set repeat_input [java::new ptolemy.actor.TypedIOPort $repeat input]
    set repeat_output [java::new ptolemy.actor.TypedIOPort $repeat output]
    set repeat_input_tokenConsumptionRate [java::new ptolemy.data.expr.Parameter $repeat_input tokenConsumptionRate]
    $repeat_input_tokenConsumptionRate setExpression "blockSize"
    set repeat_output_tokenProductionRate [java::new ptolemy.data.expr.Parameter $repeat_output tokenProductionRate]
    $repeat_output_tokenProductionRate setExpression "blockSize * numberOfTimes"

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToNames [$analysis getNotConstVariables $e1]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $repeat]]] \
	[lsort [listToNames [$analysis getNotConstVariables [$repeat getPort input]]]]
} {inport init blockSize tokenConsumptionRate}

test ConstVariableModelAnalysis-3.2 {test port parameters} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
 
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    
    set inport [java::new ptolemy.actor.parameters.PortParameter $e1 inport]
    $inport setExpression "3"
   
    set ramp [java::new ptolemy.actor.lib.Ramp $e1 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "inport"
    $step setExpression "1"

    set repeat [java::new ptolemy.actor.TypedAtomicActor $e1 repeat]
    set blockSize [java::new ptolemy.data.expr.Parameter $repeat blockSize]
    $blockSize setExpression "inport"
    set numberOfTimes [java::new ptolemy.data.expr.Parameter $repeat numberOfTimes]
    $numberOfTimes setExpression "5"
    set repeat_input [java::new ptolemy.actor.TypedIOPort $repeat input]
    set repeat_output [java::new ptolemy.actor.TypedIOPort $repeat output]
    set repeat_input_tokenConsumptionRate [java::new ptolemy.data.expr.Parameter $repeat_input tokenConsumptionRate]
    $repeat_input_tokenConsumptionRate setExpression "blockSize"
    set repeat_output_tokenProductionRate [java::new ptolemy.data.expr.Parameter $repeat_output tokenProductionRate]
    $repeat_output_tokenProductionRate setExpression "blockSize * numberOfTimes"
  
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToNames [$analysis getConstVariables $e1]]] \
	[lsort [listToNames [$analysis getNotConstVariables $e1]]] \
	[lsort [listToNames [$analysis getConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getNotConstVariables $ramp]]] \
	[lsort [listToNames [$analysis getConstVariables $repeat]]] \
	[lsort [listToNames [$analysis getNotConstVariables $repeat]]] \
	[lsort [listToNames [$analysis getConstVariables [$repeat getPort input]]]] \
	     [lsort [listToNames [$analysis getNotConstVariables [$repeat getPort input]]]]
} {inport {} {firingCountLimit init step} {} {blockSize numberOfTimes} {} tokenConsumptionRate {}}

# Test change context
test ConstVariableModelAnalysis-3.3 {test port parameters} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
 
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
   
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    
    set inport [java::new ptolemy.actor.parameters.PortParameter $e1 inport]
    [$e1 getPort inport] link $r1
    
    set ramp [java::new ptolemy.actor.lib.Ramp $e1 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "inport"
    $step setExpression "1"

    set repeat [java::new ptolemy.actor.TypedAtomicActor $e1 repeat]
    set blockSize [java::new ptolemy.data.expr.Parameter $repeat blockSize]
    $blockSize setExpression "inport"
    set numberOfTimes [java::new ptolemy.data.expr.Parameter $repeat numberOfTimes]
    $numberOfTimes setExpression "5"
    set repeat_input [java::new ptolemy.actor.TypedIOPort $repeat input]
    set repeat_output [java::new ptolemy.actor.TypedIOPort $repeat output]
    set repeat_input_tokenConsumptionRate [java::new ptolemy.data.expr.Parameter $repeat_input tokenConsumptionRate]
    $repeat_input_tokenConsumptionRate setExpression "blockSize"
    set repeat_output_tokenProductionRate [java::new ptolemy.data.expr.Parameter $repeat_output tokenProductionRate]
    $repeat_output_tokenProductionRate setExpression "blockSize * numberOfTimes"
    
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [[$analysis getChangeContext $inport] getFullName] \
	[[$analysis getChangeContext $init] getFullName] \
	[java::isnull [$analysis getChangeContext $step]]

} {..e1 ..e1 1}
