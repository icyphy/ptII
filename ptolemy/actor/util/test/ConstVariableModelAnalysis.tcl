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
    list [listToObjects [$analysis getConstVariableNames $e0]] \
	[listToObjects [$analysis getNotConstVariableNames $e0]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
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
    list [listToObjects [$analysis getConstVariableNames $e0]] \
	[listToObjects [$analysis getNotConstVariableNames $e0]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
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
    list [listToObjects [$analysis getConstVariableNames $e0]] \
	[listToObjects [$analysis getNotConstVariableNames $e0]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
} {{p1 p0} {} {firingCountLimit init step} {}}


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

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToObjects [$analysis getConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
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

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToObjects [$analysis getConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
} {{} {a p0 p1} firingCountLimit {init step}}

test ConstVariableModelAnalysis-1.6 {Test a model that is not correct (circular dependancy)} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p1
    $p1 setExpression p0
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + a"
    $step setExpression p0

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [listToObjects [$analysis getConstVariableNames $e0]] \
	[listToObjects [$analysis getConstVariableNames $ramp]]
} {{p1 p0} {firingCountLimit step}}

test ConstVariableModelAnalysis-1.7 {Test unbound variables} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set p0 [java::new ptolemy.data.expr.Parameter $e0 p0]
    set p1 [java::new ptolemy.data.expr.Parameter $e0 p1]
    $p0 setExpression p0
    $p1 setExpression p1
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "p1 + unbound"
    $step setExpression 5

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToObjects [$analysis getConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
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

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToObjects [$analysis getConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
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

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToObjects [$analysis getConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getConstVariableNames $ramp]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $ramp]]]
} {init {a step} {firingCountLimit init} step}

# FSM tests.
test ConstVariableModelAnalysis-2.0 {test fsms.} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set cinit [java::new ptolemy.data.expr.Parameter $e0 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e0 step]
    $cinit setExpression 1
    $cstep setExpression a
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set p1 [java::new ptolemy.data.expr.Parameter $fsm p1]
    set p2 [java::new ptolemy.data.expr.Parameter $fsm p2]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set s2 [java::new ptolemy.domains.fsm.kernel.State $fsm s2]
    set s1_incomingPort [$s1 getPort incomingPort]
    set s2_incomingPort [$s2 getPort incomingPort]
    set s1_outgoingPort [$s1 getPort outgoingPort]
    set s2_outgoingPort [$s2 getPort outgoingPort]
  
    set t1 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t1]
    set t2 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t2]
    
    $s1_incomingPort link $t1
    $s2_outgoingPort link $t1
    $s2_incomingPort link $t2
    $s1_outgoingPort link $t2

    set t1_action [getSettable $t1 setActions]
    set t2_action [getSettable $t2 setActions]
  
    $p1 setExpression "1"
    $p2 setExpression "2"
    $t1_action setExpression "p1=1"
    $t2_action setExpression "p1=2"

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [lsort [listToObjects [$analysis getConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $e0]]] \
	[lsort [listToObjects [$analysis getConstVariableNames $fsm]]] \
	[lsort [listToObjects [$analysis getNotConstVariableNames $fsm]]]
} {init {a step} p2 p1}

test ConstVariableModelAnalysis-2.2 {test modal model.} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set a [java::new ptolemy.data.expr.Parameter $e0 a]
    set cinit [java::new ptolemy.data.expr.Parameter $e0 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e0 step]
    $cinit setExpression 1
    $cstep setExpression a
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    set cinit [java::new ptolemy.data.expr.Parameter $e1 init]
    set cstep [java::new ptolemy.data.expr.Parameter $e1 step]
    $cinit setExpression a
    $cstep setExpression 1

    set ramp [java::new ptolemy.actor.lib.Ramp $e1 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "5"
    $step setExpression "1"

    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e1 fsm]
    set p1 [java::new ptolemy.data.expr.Parameter $fsm p1]
    set p2 [java::new ptolemy.data.expr.Parameter $fsm p2]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set s2 [java::new ptolemy.domains.fsm.kernel.State $fsm s2]
    set s1_incomingPort [$s1 getPort incomingPort]
    set s2_incomingPort [$s2 getPort incomingPort]
    set s1_outgoingPort [$s1 getPort outgoingPort]
    set s2_outgoingPort [$s2 getPort outgoingPort]
  
    set t1 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t1]
    set t2 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t2]
    
    $s1_incomingPort link $t1
    $s2_outgoingPort link $t1
    $s2_incomingPort link $t2
    $s1_outgoingPort link $t2

    set t1_action [getSettable $t1 setActions]
    set t2_action [getSettable $t2 setActions]
  
    $p1 setExpression "1"
    $p2 setExpression "2"
    $t1_action setExpression "p1=1"
    $t2_action setExpression "ramp.step=2"

    set dynamicVariableSet [java::new java.util.HashSet]
    $dynamicVariableSet add $a

    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [listToObjects [$analysis getConstVariableNames $e0]] \
	[listToObjects [$analysis getConstVariableNames $e1]] \
	[listToObjects [$analysis getConstVariableNames $ramp]] \
	[listToObjects [$analysis getConstVariableNames $fsm]]
} {init step {init firingCountLimit} p2}

test ConstVariableModelAnalysis-2.3 {test port parameters} {
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

    set repeat [java::new ptolemy.domains.sdf.lib.Repeat $e1 repeat]
    set blockSize [getParameter $repeat blockSize]
    $blockSize setExpression "inport"
  
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [listToObjects [$analysis getNotConstVariableNames $e1]] \
	[listToObjects [$analysis getNotConstVariableNames $ramp]] \
	[listToObjects [$analysis getNotConstVariableNames $repeat]] \
	[listToObjects [$analysis getNotConstVariableNames [$repeat getPort input]]]
} {inport init blockSize tokenConsumptionRate}

test ConstVariableModelAnalysis-2.4 {test port parameters} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
 
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    
    set inport [java::new ptolemy.actor.parameters.PortParameter $e1 inport]
    $inport setExpression "3"
   
    set ramp [java::new ptolemy.actor.lib.Ramp $e1 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "inport"
    $step setExpression "1"

    set repeat [java::new ptolemy.domains.sdf.lib.Repeat $e1 repeat]
    set blockSize [getParameter $repeat blockSize]
    $blockSize setExpression "inport"
  
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [listToObjects [$analysis getConstVariableNames $e1]] \
	[listToObjects [$analysis getNotConstVariableNames $e1]] \
	[listToObjects [$analysis getConstVariableNames $ramp]] \
	[listToObjects [$analysis getNotConstVariableNames $ramp]] \
	[listToObjects [$analysis getConstVariableNames $repeat]] \
	[listToObjects [$analysis getNotConstVariableNames $repeat]] \
	[listToObjects [$analysis getConstVariableNames [$repeat getPort input]]] \
	[listToObjects [$analysis getNotConstVariableNames [$repeat getPort input]]]
} {inport {} {init firingCountLimit step} {} {blockSize numberOfTimes} {} tokenConsumptionRate {}}

# Test change context
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

    set repeat [java::new ptolemy.domains.sdf.lib.Repeat $e1 repeat]
    set blockSize [getParameter $repeat blockSize]
    $blockSize setExpression "inport"
    
    set analysis [java::new ptolemy.actor.util.ConstVariableModelAnalysis $e0]
    list [[$analysis getChangeContext $inport] getFullName] \
	[[$analysis getChangeContext $init] getFullName] \
	[java::isnull [$analysis getChangeContext $step]]

} {..e1 ..e1 1}
