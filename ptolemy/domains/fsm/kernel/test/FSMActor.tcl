# Tests for the FSMActor class
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
test FSMActor-1.1 {test creating an FSMActor} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set v0 [java::field $fsm initialStateName]
    list [$fsm getFullName] [$v0 getFullName]
} {..fsm ..fsm.initialStateName}

test FSMActor-1.2 {container must be TypedCompositeActor or null} {
    $fsm setContainer [java::null]
    set re0 [$fsm getFullName]
    set e1 [java::new ptolemy.actor.CompositeActor]
    catch {$fsm setContainer $e1} msg
    list $re0 $msg
} {.fsm {ptolemy.kernel.util.IllegalActionException: . and .fsm:
FSMActor can only be contained by instances of TypedCompositeActor.}}

######################################################################
####
#
test FSMActor-2.1 {test getDirector} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set re0 [expr {[$fsm getDirector] == $dir}]
    set re1 [expr {[$fsm getExecutiveDirector] == $dir}]
    $fsm setContainer [java::null]
    set re2 [expr {[$fsm getDirector] == [java::null]}]
    list $re0 $re1 $re2
} {1 1 1}

test FSMActor-2.2 {test getManager} {
    set mag [java::new ptolemy.actor.Manager]
    $e0 setManager $mag
    set re1 [expr {[$fsm getManager] == [java::null]}]
    $fsm setContainer $e0
    set re2 [expr {[$fsm getManager] == $mag}]
    list $re1 $re2
} {1 1}

######################################################################
####
#
test FSMActor-3.1 {test listing input and output ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set p0 [java::new ptolemy.actor.TypedIOPort $fsm p0]
    set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1 true true]
    set p2 [java::new ptolemy.actor.TypedIOPort $fsm p2 true false]
    set p3 [java::new ptolemy.actor.TypedIOPort $fsm p3 false true]
    list [listToFullNames [$fsm inputPortList]] \
            [listToFullNames [$fsm outputPortList]]
} {{..fsm.p1 ..fsm.p2} {..fsm.p1 ..fsm.p3}}

test FSMActor-3.2 {test newPort} {
    set p4 [$fsm newPort p4]
    list [java::instanceof $p4 ptolemy.actor.TypedIOPort] \
            [listToFullNames [$fsm portList]]
} {1 {..fsm.p0 ..fsm.p1 ..fsm.p2 ..fsm.p3 ..fsm.p4}}

test FSMActor-3.3 {test newReceiver} {
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set r [$fsm newReceiver]
    set tok [java::new ptolemy.data.StringToken foo]
    $r put $tok
    set received [$r get]
    $received toString
} {foo}

######################################################################
####
#
test FSMActor-4.1 {test setting initial state} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set tok [java::new ptolemy.data.StringToken s0]
    set p [java::field $fsm initialStateName]
    $p setToken $tok
    set re0 [expr {[$fsm getInitialState] == $s0}]
    set tok [java::new ptolemy.data.StringToken s1]
    $p setToken $tok
    set re1 [expr {[$fsm getInitialState] == $s1}]
    set tok [java::new ptolemy.data.StringToken s2]
    $p setToken $tok
    catch {$fsm getInitialState} msg
    list $re0 $re1 $msg
} {1 1 {ptolemy.kernel.util.IllegalActionException: ..fsm:
Cannot find initial state with name "s2".}}

######################################################################
####
#
test FSMActor-5.1 {test creating input variables} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
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
    listToNames [$fsm attributeList]
} {initialStateName p0_S p0_V p1_0_S p1_0_V p1_1_S p1_1_V}

######################################################################
####
#
test FSMActor-6.1 {test action methods} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]

    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set tok [java::new ptolemy.data.StringToken s0]
    [java::field $fsm initialStateName] setToken $tok
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    $t0 setGuardExpression "(p1_0_S ? p1_0_V : 0) > 5"
    set act0 [java::new ptolemy.domains.fsm.kernel.BroadcastOutput $t0 act0]
    set tok [java::new ptolemy.data.StringToken "p2"]
    [java::field $act0 portName] setToken $tok
    set tok [java::new ptolemy.data.StringToken "p1_0_V + 1"]
    [java::field $act0 expression] setToken $tok
    set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1]
    $p1 setInput true
    $p1 setMultiport true
    set p2 [java::new ptolemy.actor.TypedIOPort $fsm p2]
    $p2 setOutput true
    $p2 setMultiport true
    $p2 setTypeEquals [java::field ptolemy.data.type.BaseType INT]

    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 e1]
    set p3 [java::new ptolemy.actor.TypedIOPort $e1 p3]
    $p3 setOutput true
    $p3 setMultiport true
    $p3 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
    set r0 [java::new ptolemy.actor.TypedIORelation $e0 r0]
    $r0 setWidth 2
    $p1 link $r0
    $p3 link $r0

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
    $r1 setWidth 2
    $p2 link $r1
    [java::field [java::cast ptolemy.actor.lib.Sink $rec] input] link $r1

    $dir preinitialize
    $dir initialize

    set v0 [java::cast ptolemy.data.expr.Variable [$fsm getAttribute p1_0_S]]
    set v1 [java::cast ptolemy.data.expr.Variable [$fsm getAttribute p1_0_V]]
    set re0 [[$fsm currentState] getFullName]
    $fsm prefire
    $fsm fire
    set re1 [[$v0 getToken] toString]
    set re2 [expr {[$v1 getToken] == [java::null]}]
    $fsm postfire
    $rec prefire
    $rec fire
    $rec postfire

    set tok [java::new {ptolemy.data.IntToken int} 0]
    $p3 broadcast $tok
    $fsm prefire
    $fsm fire
    set re3 [[$v0 getToken] toString]
    set re4 [[$v1 getToken] toString]
    $fsm postfire
    $rec prefire
    $rec fire
    $rec postfire

    set tok [java::new {ptolemy.data.IntToken int} 6]
    $p3 broadcast $tok
    $fsm prefire
    $fsm fire
    set re5 [[$v0 getToken] toString]
    set re6 [[$v1 getToken] toString]
    $fsm postfire
    set re7 [[$fsm currentState] getFullName]
    $rec prefire
    $rec fire
    $rec postfire
    set ls0 [$rec getHistory 0]
    set ls1 [$rec getHistory 1]

    $dir wrapup
    $dir terminate

    $fsm reset
    set re8 [[$fsm currentState] getFullName]

    list $re0 $re1 $re2 $re3 $re4 $re5 $re6 $re7 $re8 [listToStrings $ls0] \
            [listToStrings $ls1]
} {..fsm.s0 false 1 true 0 true 6 ..fsm.s1 ..fsm.s0 {_ _ 7} {_ _ 7}}

######################################################################
####
#
test FSMActor-7.1 {test exception when multiple transitions enabled} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set tok [java::new ptolemy.data.StringToken s0]
    [java::field $fsm initialStateName] setToken $tok
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    $t0 setGuardExpression "v0 > 5"
    set t1 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t1]
    [java::field $s0 outgoingPort] link $t1
    [java::field $s1 incomingPort] link $t1
    $t1 setGuardExpression "v0 > 5"
    set v0 [java::new ptolemy.data.expr.Variable $fsm v0]
    set tok [java::new {ptolemy.data.IntToken int} 6]
    $v0 setToken $tok
    $dir preinitialize
    $dir initialize
    $dir prefire
    catch {$dir fire} msg
    $dir terminate
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ..fsm and ..fsm.s0:
Multiple enabled transitions.}}
