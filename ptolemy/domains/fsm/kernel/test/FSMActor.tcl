# Tests for the FSMActor class
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
test FSMActor-1.1 {test creating an FSMActor} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set v0 [java::field $fsm initialStateName]
    set fsm1 [java::new ptolemy.domains.fsm.kernel.FSMActor]
    set ws [java::new ptolemy.kernel.util.Workspace]
    set fsm2 [java::new ptolemy.domains.fsm.kernel.FSMActor $ws]
    list [$fsm getFullName] [$v0 getFullName] [$fsm1 getFullName] \
            [$fsm2 getFullName]
} {..fsm ..fsm.initialStateName . .}

test FSMActor-1.2 {container must be TypedCompositeActor or null} {
    $fsm setContainer [java::null]
    set re0 [$fsm getFullName]
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setName testContainer
    $fsm setContainer $e1
    list $re0 [[$fsm getContainer] getFullName]
} {.fsm .testContainer}

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
    set received [java::cast ptolemy.data.StringToken [$r get]]
    $received stringValue
} {foo}

######################################################################
####
#
test FSMActor-4.1 {test setting initial state} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set p [java::field $fsm initialStateName]
    $p setExpression s0
    set re0 [expr {[$fsm getInitialState] == $s0}]
    $p setExpression s1
    set re1 [expr {[$fsm getInitialState] == $s1}]
    $p setExpression s2
    catch {$fsm getInitialState} msg
    list $re0 $re1 $msg
} {1 1 {ptolemy.kernel.util.IllegalActionException: Cannot find initial state with name "s2".
  in .<Unnamed Object>.fsm}}

######################################################################
####
#
test FSMActor-5.1 {test creating input variables} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
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
    listToNames [$fsm attributeList]
} {_iconDescription initialStateName finalStateNames _nonStrictMarker p0_isPresent p0 p0Array p1_0_isPresent p1_0 p1_0Array p1_1_isPresent p1_1 p1_1Array _IODependence}

test FSMActor-5.2 {test handling port name change} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    [java::field $fsm initialStateName] setExpression s0
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s0 incomingPort] link $t0
    $t0 setGuardExpression "p1 > 5"
    set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1]
    $p1 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
    set p2 [java::field [java::cast ptolemy.actor.lib.Source $ramp] output]
    $p2 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
    $p1 setInput true
    $e0 connect $p1 $p2
    $dir preinitialize
    $dir initialize
    $dir iterate 1
    $dir wrapup
    $p1 setName p0
    $dir preinitialize
    $dir initialize
    catch {$dir iterate 1} msg
    $dir wrapup
    $p1 setName pp
    $t0 setGuardExpression "pp > 5"
    $dir preinitialize
    $dir initialize
    $dir iterate 1
    $dir wrapup
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: p1 > 5
  in .<Unnamed Object>.fsm.t0._guard
Because:
The ID p1 is undefined.}}
    
######################################################################
####
#
test FSMActor-6.1 {test action methods} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]

    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    [java::field $fsm initialStateName] setExpression s0
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    $t0 setGuardExpression "p1_0_isPresent && p1_0 > 5"
    set act0 [java::field $t0 outputActions]
    $act0 setExpression "p2 = p1_0 + 1"
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

    set re0 [[$fsm currentState] getFullName]
    $fsm prefire
    $fsm fire
    set v0 [java::cast ptolemy.data.expr.Variable [$fsm getAttribute p1_0_isPresent]]
    set v1 [java::cast ptolemy.data.expr.Variable [$fsm getAttribute p1_0]]
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
} {..fsm.s0 false 1 true 0 true 6 ..fsm.s1 ..fsm.s0 {{"_"} {"_"} 7} {{"_"} {"_"} 7}}

######################################################################
####
#
test FSMActor-7.1 {test exception when multiple transitions enabled} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    [java::field $fsm initialStateName] setExpression s0
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
} {{ptolemy.domains.fsm.kernel.MultipleEnabledTransitionsException: Multiple enabled transitions: t0 and t1.
  in .<Unnamed Object>.fsm.s0}}

######################################################################
####
#
test FSMActor-8.1 {test newRelation} {
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor]
    set r0 [$fsm newRelation r0]
    set re0 [java::instanceof $r0 ptolemy.domains.fsm.kernel.Transition]
    catch {$fsm newRelation r0} msg
    list $re0 [$r0 getFullName] $msg
} {1 ..r0 {ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "r0" into container named ".<Unnamed Object>", which already contains an object with that name.}}

######################################################################
####
#
test FSMActor-9.1 {test working with MoML} {
# MoML description of an AMI (Alternating Mark 1) model
    set model {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="14">
        </property>
    </property>
    <entity name="fsm" class="ptolemy.domains.fsm.kernel.FSMActor">
        <property name="initialStateName" class="ptolemy.kernel.util.StringAttribute" value="plusOne">
        </property>
        <port name="in" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="out" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <entity name="plusOne" class="ptolemy.domains.fsm.kernel.State">
            <property name="refinementName" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <port name="incomingPort" class="ptolemy.kernel.ComponentPort">
            </port>
            <port name="outgoingPort" class="ptolemy.kernel.ComponentPort">
            </port>
        </entity>
        <entity name="minusOne" class="ptolemy.domains.fsm.kernel.State">
            <property name="refinementName" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <port name="incomingPort" class="ptolemy.kernel.ComponentPort">
            </port>
            <port name="outgoingPort" class="ptolemy.kernel.ComponentPort">
            </port>
        </entity>
        <relation name="t0" class="ptolemy.domains.fsm.kernel.Transition">
            <property name="guardExpression" class="ptolemy.kernel.util.StringAttribute" value="in == 1">
            </property>
            <property name="preemptive" class="ptolemy.data.expr.Parameter" value="false">
            </property>
            <property name="triggerExpression" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="outputActions" class="ptolemy.domains.fsm.kernel.OutputActionsAttribute" value="out = 1">
            </property>
        </relation>
        <relation name="t1" class="ptolemy.domains.fsm.kernel.Transition">
            <property name="guardExpression" class="ptolemy.kernel.util.StringAttribute" value="in == 0">
            </property>
            <property name="preemptive" class="ptolemy.data.expr.Parameter" value="false">
            </property>
            <property name="triggerExpression" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="outputActions" class="ptolemy.domains.fsm.kernel.OutputActionsAttribute" value="out = 0">
            </property>
        </relation>
        <relation name="t2" class="ptolemy.domains.fsm.kernel.Transition">
            <property name="guardExpression" class="ptolemy.kernel.util.StringAttribute" value="in == 1">
            </property>
            <property name="preemptive" class="ptolemy.data.expr.Parameter" value="false">
            </property>
            <property name="triggerExpression" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="outputActions" class="ptolemy.domains.fsm.kernel.OutputActionsAttribute" value="out = -1">
            </property>
        </relation>
        <relation name="t3" class="ptolemy.domains.fsm.kernel.Transition">
            <property name="guardExpression" class="ptolemy.kernel.util.StringAttribute" value="in == 0">
            </property>
            <property name="preemptive" class="ptolemy.data.expr.Parameter" value="false">
            </property>
            <property name="triggerExpression" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="outputActions" class="ptolemy.domains.fsm.kernel.OutputActionsAttribute" value="out = 0">
            </property>
        </relation>
        <link port="plusOne.incomingPort" relation="t2"/>
        <link port="plusOne.incomingPort" relation="t3"/>
        <link port="plusOne.outgoingPort" relation="t0"/>
        <link port="plusOne.outgoingPort" relation="t3"/>
        <link port="minusOne.incomingPort" relation="t0"/>
        <link port="minusOne.incomingPort" relation="t1"/>
        <link port="minusOne.outgoingPort" relation="t1"/>
        <link port="minusOne.outgoingPort" relation="t2"/>
    </entity>
    <entity name="src" class="ptolemy.domains.fsm.kernel.test.ZeroOneSource">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <relation name="r0" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="r1" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="fsm.in" relation="r0"/>
    <link port="fsm.out" relation="r1"/>
    <link port="src.output" relation="r0"/>
    <link port="rec.input" relation="r1"/>
</entity>}

    set par [java::new ptolemy.moml.MoMLParser]
    set top [java::cast ptolemy.actor.TypedCompositeActor [$par parse $model]]
    set mag [java::new ptolemy.actor.Manager [$top workspace] mag]
    $top setManager $mag
    $mag execute
    set rec [java::cast ptolemy.actor.lib.Recorder [$top getEntity rec]]
    listToStrings [$rec getHistory 0]
} {0 1 -1 1 0 -1 0 1 -1 0 1 -1 0 0}

