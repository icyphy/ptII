# Tests for the Transition class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 2000 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Transition-1.1 {test creating a transition} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set v0 [java::field $t0 guard]
    set v1 [java::field $t0 trigger]
    list [$t0 getFullName] [$v0 getFullName] [$v1 getFullName]
} {.e0.fsm.t0 .e0.fsm.t0.guard .e0.fsm.t0.trigger}

test Transition-1.2 {container of a transition must be an FSMActor or null} {
    $t0 setContainer [java::null]
    set re [$t0 getFullName]
    catch {$t0 setContainer $e0} msg
    list $re $msg
} {.t0 {ptolemy.kernel.util.IllegalActionException: .e0 and .t0:
Transition can only be contained by instances of FSMActor.}}

######################################################################
####
#
test Transition-2.1 {transition can only link to incoming or outgoing port} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set p0 [java::new ptolemy.kernel.ComponentPort $s0 p0]
    catch {$p0 link $t0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ..fsm.t0 and ..fsm.s0:
Transition can only be linked to incoming or outgoing port of State.}}

test Transition-2.2 {transition can link to at most one incoming port} {
    set inport [java::field $s0 incomingPort]
    $inport link $t0
    catch {$inport link $t0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ..fsm.t0:
Transition can only have one source and one destination.}}

test Transition-2.3 {transition can link to at most one outgoing port} {
    set outport [java::field $s0 outgoingPort]
    $outport link $t0
    $inport unlink $t0
    catch {$outport link $t0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ..fsm.t0:
Transition can only have one source and one destination.}}

test Transition-2.4 {test tracking source and destination state} {
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set inport1 [java::field $s1 incomingPort]
    set outport1 [java::field $s1 outgoingPort]
    $inport1 link $t0
    set re0 [[$t0 sourceState] getFullName]
    set re1 [[$t0 destinationState] getFullName]
    $inport1 unlink $t0
    $outport unlink $t0
    $outport1 link $t0
    $inport link $t0
    set re2 [[$t0 sourceState] getFullName]
    set re3 [[$t0 destinationState] getFullName]
    list $re0 $re1 $re2 $re3
} {..fsm.s0 ..fsm.s1 ..fsm.s1 ..fsm.s0}

######################################################################
####
#
test Transition-3.1 {test scope of guard and trigger expressions} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    set tok [java::new ptolemy.data.StringToken s0]
    [java::field $fsm initialStateName] setToken $tok
    set p0 [java::new ptolemy.actor.TypedIOPort $fsm p0]
    $p0 setInput true
    set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1]
    $p1 setInput true
    $p1 setMultiport true
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 e2]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 p2]
    $p2 setMultiport true
    set r0 [java::new ptolemy.actor.TypedIORelation $e0 r0]
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
    $r1 setWidth 2
    $p0 link $r0
    $p1 link $r1
    $p2 link $r0
    $p2 link $r1
    $dir preinitialize
    set guard [java::field $t0 guard]
    set trigger [java::field $t0 trigger]
    set scope1 [[$guard getScope] elementList]
    set scope2 [[$trigger getScope] elementList]
    list [listToNames $scope1] [listToNames $scope2]
} {{trigger initialStateName p0_S p0_V p1_0_S p1_0_V p1_1_S p1_1_V} {guard initialStateName p0_S p0_V p1_0_S p1_0_V p1_1_S p1_1_V}}

test Transition-3.2 {test setting guard and trigger expression} {
    $t0 setGuardExpression "p0_V > 0"
    $t0 setTriggerExpression "p0_V > 5"
    list [$t0 getGuardExpression] [$t0 getTriggerExpression]
} {{p0_V > 0} {p0_V > 5}}

test Transition-3.3 {test guard and trigger evaluation} {
    set tok [java::new {ptolemy.data.IntToken int} 10]
    set v0 [java::cast ptolemy.data.expr.Variable [$fsm getAttribute p0_V]]
    $v0 setToken $tok
    set re0 [$t0 isEnabled]
    set re1 [$t0 isTriggered]
    set tok [java::new {ptolemy.data.IntToken int} -1]
    $v0 setToken $tok
    set re2 [$t0 isEnabled]
    set re3 [$t0 isTriggered]
    list $re0 $re1 $re2 $re3
} {1 1 0 0}

test Transition-3.4 {guard must be true whenever trigger is true} {
    $t0 setTriggerExpression "p0_V > -5"
    catch {$t0 isTriggered} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ..fsm.t0:
The trigger: p0_V > -5 is true but the guard: p0_V > 0 is false.}}

######################################################################
####
#
test Transition-4.1 {test setting a transition preemptive or non-preemptive} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set re0 [$t0 isPreemptive]
    $t0 setPreemptive true
    set re1 [$t0 isPreemptive]
    $t0 setPreemptive false
    set re2 [$t0 isPreemptive]
    list $re0 $re1 $re2
} {0 1 0}

######################################################################
####
#
test Transition-5.1 {test listing choice and commit actions} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set re0 [listToNames [$t0 choiceActionList]]
    set re1 [listToNames [$t0 commitActionList]]
    set act0 [java::new ptolemy.domains.fsm.kernel.BroadcastOutput $t0 act0]
    set act1 [java::new ptolemy.domains.fsm.kernel.BroadcastOutput $t0 act1]
    set act2 [java::new ptolemy.domains.fsm.kernel.SetVariable $t0 act2]
    set re2 [listToNames [$t0 choiceActionList]]
    set re3 [listToNames [$t0 commitActionList]]
    $act0 setContainer [java::null]
    set re4 [listToNames [$t0 choiceActionList]]
    list $re0 $re1 $re2 $re3 $re4
} {{} {} {act0 act1} act2 act1}
