# Tests for the FSMDirector class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 1999-2005 The Regents of the University of California.
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
test FSMDirector-1.1 {test constructors} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set dir1 [java::new ptolemy.domains.fsm.kernel.FSMDirector]
    set dir2 [java::new ptolemy.domains.fsm.kernel.FSMDirector $w]
    set e0 [java::new ptolemy.actor.CompositeActor]
    set dir3 [java::new ptolemy.domains.fsm.kernel.FSMDirector $e0 dir]
    list [$dir1 getFullName] [[java::field $dir1 controllerName] getFullName] \
            [$dir2 getFullName] \
            [[java::field $dir2 controllerName] getFullName] \
            [$dir3 getFullName] \
            [[java::field $dir3 controllerName] getFullName]
} {. ..controllerName . ..controllerName ..dir ..dir.controllerName}

######################################################################
####
#
test FSMDirector-2.1 {test setting controller} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.domains.fsm.kernel.FSMDirector $e0 dir]
    set v0 [java::field $dir controllerName]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    $v0 setExpression fsm
    set re0 [expr {[$dir getController] == $fsm}]
    $fsm setContainer [java::null]
    catch {$dir getController} msg0
    $fsm setContainer $e0
    $fsm getContainer
    $v0 setExpression foo
    catch {$dir getController} msg1
    list $re0 $msg0 $msg1
} {1 {ptolemy.kernel.util.IllegalActionException: No controller found with name fsm
  in .<Unnamed Object>.dir} {ptolemy.kernel.util.IllegalActionException: No controller found with name foo
  in .<Unnamed Object>.dir}}

######################################################################
####
#
test FSMDirector-6.1 {test transferInputs} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set d0 [java::new ptolemy.actor.Director $e0 d0]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    set dir [java::new ptolemy.domains.fsm.kernel.FSMDirector $e1 dir]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e1 e2]
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e1 e3]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e1 fsm]
    [java::field $dir controllerName] setExpression fsm
    set p0 [java::new ptolemy.actor.TypedIOPort $e1 p0]
    $p0 setInput true
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
    set e4 [java::new ptolemy.actor.TypedAtomicActor $e0 e4]
    set p00 [java::new ptolemy.actor.TypedIOPort $e4 p00]
    $p00 setOutput true
    $p00 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
    $p0 link $r1
    $p00 link $r1
    set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1]
    $p1 setInput true
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 p2]
    $p2 setInput true
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 p3]
    $p3 setInput true
    set r2 [java::new ptolemy.actor.TypedIORelation $e1 r2]
    $p0 link $r2
    $p1 link $r2
    $p2 link $r2
    $p3 link $r2

    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set t1 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t1]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    [java::field $s1 outgoingPort] link $t1
    [java::field $s0 incomingPort] link $t1
    [java::field $fsm initialStateName] setExpression s0
    [java::field $s0 refinementName] setExpression e2
    [java::field $s1 refinementName] setExpression e3
    $t0 setGuardExpression "p1 > 5"
    [java::field $t1 preemptive] setExpression "true"
    $t1 setGuardExpression "p1 > 5"

    $d0 preinitialize
    $d0 initialize
    set tok [java::new {ptolemy.data.IntToken int} 6]
    $p00 broadcast $tok
    $e1 prefire
    $e1 fire
    set re0 [[$p2 get 0] toString]
    set re1 [$p3 hasToken 0]
    $e1 postfire
    $p00 broadcast $tok
    $e1 prefire
    $e1 fire
    set re2 [$p2 hasToken 0]
    set re3 [[$p3 get 0] toString]
    $e1 postfire
    set re4 [[$fsm currentState] getFullName]
    $d0 terminate
    list $re0 $re1 $re2 $re3 $re4
} {6 0 1 6 ..e1.fsm.s0}

