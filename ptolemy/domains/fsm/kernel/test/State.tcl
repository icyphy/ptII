# Tests for the State class
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
test State-1.1 {test creating a state} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set p0 [$s0 getPort incomingPort]
    set p1 [$s0 getPort outgoingPort]
    list [$s0 getName] [$s0 getFullName] [$p0 getFullName] [$p1 getFullName]
} {s0 .e0.fsm.s0 .e0.fsm.s0.incomingPort .e0.fsm.s0.outgoingPort}

test State-1.2 {container of a state must be instance of FSMActor or null} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    $s0 setContainer [java::null]
    set msg0 [$s0 getFullName]
    catch {$s0 setContainer $e0} msg1
    list $msg0 $msg1
} {.s0 {ptolemy.kernel.util.IllegalActionException: .e0 and .s0:
State can only be contained by instances of FSMActor.}}

######################################################################
####
#
test State-2.1 {test listing preemptive and non-preemptive transitions} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    set t1 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t1]
    [java::field $s0 outgoingPort] link $t1
    [java::field $s1 incomingPort] link $t1
    $t1 setPreemptive true
    set ls1 [listToNames [$s0 preemptiveTransitionList]]
    set ls2 [listToNames [$s0 nonpreemptiveTransitionList]]
    $t1 setPreemptive false
    set ls3 [listToNames [$s0 preemptiveTransitionList]]
    set ls4 [listToNames [$s0 nonpreemptiveTransitionList]]
    $t0 setPreemptive true
    $t1 setPreemptive true
    set ls5 [listToNames [$s0 preemptiveTransitionList]]
    set ls6 [listToNames [$s0 nonpreemptiveTransitionList]]
    list $ls1 $ls2 $ls3 $ls4 $ls5 $ls6
} {t1 t0 {} {t0 t1} {t0 t1} {}}

######################################################################
####
#
test State-3.1 {test setting refinement} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 e1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 e2]
    set re0 [java::isnull [$s0 getRefinement]]
    set tok [java::new ptolemy.data.StringToken e1]
    [java::field $s0 refinementName] setToken $tok
    set ref0 [java::cast ptolemy.kernel.Entity [$s0 getRefinement]]
    set re1 [$ref0 getFullName]
    set tok [java::new ptolemy.data.StringToken e2]
    [java::field $s0 refinementName] setToken $tok
    set ref1 [java::cast ptolemy.kernel.Entity [$s0 getRefinement]]
    set re2 [$ref1 getFullName]
    set tok [java::new ptolemy.data.StringToken e3]
    [java::field $s0 refinementName] setToken $tok
    catch {$s0 getRefinement} msg
    list $re0 $re1 $re2 $msg
} {1 .e0.e1 .e0.e2 {ptolemy.kernel.util.IllegalActionException: .e0.fsm.s0:
Cannot find refinement with name "e3" in .e0}}
