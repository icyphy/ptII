# Tests for the Transition class
#
# @Author: Xiaojun Liu
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
    set v0 [java::field $t0 guardExpression]
    set v1 [java::field $t0 triggerExpression]
    set v2 [java::field $t0 preemptive]
    list [$t0 getFullName] [$v0 getFullName] [$v1 getFullName] \
            [$v2 getFullName]
} {.e0.fsm.t0 .e0.fsm.t0.guardExpression .e0.fsm.t0.triggerExpression .e0.fsm.t0.preemptive}

test Transition-1.2 {container of a transition must be an FSMActor or null} {
    $t0 setContainer [java::null]
    set re [$t0 getFullName]
    catch {$t0 setContainer $e0} msg
    list $re $msg
} {.t0 {ptolemy.kernel.util.IllegalActionException: Transition can only be contained by instances of FSMActor.
  in .e0 and .t0}}

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
} {{ptolemy.kernel.util.IllegalActionException: Transition can only be linked to incoming or outgoing port of State.
  in .<Unnamed Object>.fsm.t0 and .<Unnamed Object>.fsm.s0}}

test Transition-2.2 {transition can link to at most one incoming port} {
    set inport [java::field $s0 incomingPort]
    $inport link $t0
    catch {$inport link $t0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Transition can only have one source and one destination.
  in .<Unnamed Object>.fsm.t0}}

test Transition-2.3 {transition can link to at most one outgoing port} {
    set outport [java::field $s0 outgoingPort]
    $outport link $t0
    $inport unlink $t0
    catch {$outport link $t0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Transition can only have one source and one destination.
  in .<Unnamed Object>.fsm.t0}}

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
    [java::field $fsm initialStateName] setExpression s0
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
    set scope1 [[$fsm getPortScope] identifierSet]
    list [lsort [listToStrings $scope1]]
} {{p0 p0Array p0_0 p0_0Array p0_0_isPresent p0_isPresent p1 p1Array p1_0 p1_0Array p1_0_isPresent p1_1 p1_1Array p1_1_isPresent p1_isPresent}}

test Transition-3.2 {test setting guard and trigger expression} {
    $t0 setGuardExpression "p0 > 0"
    $t0 setTriggerExpression "p0 > 5"
    list [$t0 getGuardExpression] [$t0 getTriggerExpression]
} {{p0 > 0} {p0 > 5}}

######################################################################
####
#
test Transition-4.1 {test setting a transition preemptive or non-preemptive} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set re0 [$t0 isPreemptive]
    [java::field $t0 preemptive] setExpression "true"
    set re1 [$t0 isPreemptive]
    [java::field $t0 preemptive] setExpression "false"
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
    list $re0 $re1
} {outputActions setActions}

