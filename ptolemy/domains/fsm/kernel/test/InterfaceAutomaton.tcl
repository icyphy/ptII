# Tests for the InterfaceAutomaton class
#
# @Author: Yuhong Xiong
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
test InterfaceAutomaton-1.1 {test creating an InterfaceAutomaton} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ia [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton $e0 ia]

    set v0 [java::field [java::cast ptolemy.domains.fsm.kernel.FSMActor $ia] \
        initialStateName]

    set ia1 [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton]
    set ws [java::new ptolemy.kernel.util.Workspace]
    set ia2 [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton $ws]
    list [$ia getFullName] [$v0 getFullName] [$ia1 getFullName] \
            [$ia2 getFullName]
} {..ia ..ia.initialStateName . .}

test InterfaceAutomaton-1.2 {container must be TypedCompositeActor or null} {
    $ia setContainer [java::null]
    set re0 [$ia getFullName]
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setName testContainer
    $ia setContainer $e1
    list $re0 [[$ia getContainer] getFullName]
} {.ia .testContainer}

######################################################################
####
#
test InterfaceAutomaton-2.1 {test getDirector} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set ia [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton $e0 ia]
    set re0 [expr {[$ia getDirector] == $dir}]
    set re1 [expr {[$ia getExecutiveDirector] == $dir}]
    $ia setContainer [java::null]
    set re2 [expr {[$ia getDirector] == [java::null]}]
    list $re0 $re1 $re2
} {1 1 1}

test InterfaceAutomaton-2.2 {test getManager} {
    set mag [java::new ptolemy.actor.Manager]
    $e0 setManager $mag
    set re1 [expr {[$ia getManager] == [java::null]}]
    $ia setContainer $e0
    set re2 [expr {[$ia getManager] == $mag}]
    list $re1 $re2
} {1 1}

######################################################################
####
#
test InterfaceAutomaton-3.1 {test listing input and output ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ia [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton $e0 ia]
    set p0 [java::new ptolemy.actor.TypedIOPort $ia p0]
    set p1 [java::new ptolemy.actor.TypedIOPort $ia p1 true true]
    set p2 [java::new ptolemy.actor.TypedIOPort $ia p2 true false]
    set p3 [java::new ptolemy.actor.TypedIOPort $ia p3 false true]
    list [listToFullNames [$ia inputPortList]] \
            [listToFullNames [$ia outputPortList]]
} {{..ia.p1 ..ia.p2} {..ia.p1 ..ia.p3}}

test InterfaceAutomaton-3.2 {test newPort} {
    set p4 [$ia newPort p4]
    list [java::instanceof $p4 ptolemy.actor.TypedIOPort] \
            [listToFullNames [$ia portList]]
} {1 {..ia.p0 ..ia.p1 ..ia.p2 ..ia.p3 ..ia.p4}}

test InterfaceAutomaton-3.3 {test newReceiver} {
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set r [$ia newReceiver]
    set tok [java::new ptolemy.data.StringToken foo]
    $r put $tok
    set received [java::cast ptolemy.data.StringToken [$r get]]
    $received stringValue
} {foo}

######################################################################
####
#
test InterfaceAutomaton-4.1 {test setting initial state} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ia [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton $e0 ia]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $ia s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $ia s1]
    set p [java::field [java::cast \
        ptolemy.domains.fsm.kernel.FSMActor $ia] initialStateName]
    $p setExpression s0
    set re0 [expr {[$ia getInitialState] == $s0}]
    $p setExpression s1
    set re1 [expr {[$ia getInitialState] == $s1}]
    $p setExpression s2
    catch {$ia getInitialState} msg
    list $re0 $re1 $msg
} {1 1 {ptolemy.kernel.util.IllegalActionException: Cannot find initial state with name "s2".
  in .<Unnamed Object>.ia}}

######################################################################
####
#
test InterfaceAutomaton-6.1 {test newRelation} {
    set ia [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton]
    set r0 [$ia newRelation r0]
    set re0 [java::instanceof $r0 \
        ptolemy.domains.fsm.kernel.InterfaceAutomatonTransition]
    catch {$ia newRelation r0} msg
    list $re0 [$r0 getFullName] $msg
} {1 ..r0 {ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "r0" into container named ".<Unnamed Object>", which already contains an object with that name.}}

######################################################################
####
#
test InterfaceAutomaton-7.1 {test setting input transition} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]

    set ia [java::new ptolemy.domains.fsm.kernel.InterfaceAutomaton $e0 ia]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $ia s0]
    [java::field [java::cast ptolemy.domains.fsm.kernel.FSMActor $ia] \
      initialStateName] setExpression s0
    set s1 [java::new ptolemy.domains.fsm.kernel.State $ia s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.InterfaceAutomatonTransition \
      $ia t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0

    set lab [java::field $t0 label]
    $lab setExpression a?
    set act [java::field \
        [java::cast ptolemy.domains.fsm.kernel.Transition $t0] outputActions]
    list [$t0 getType] [$t0 getGuardExpression] [$act getExpression] 
} {0 a_isPresent {}}

test InterfaceAutomaton-7.2 {test setting output transition} {
    $lab setExpression b!
    list [$t0 getType] [$t0 getGuardExpression] [$act getExpression] 
} {1 true b=true}

test InterfaceAutomaton-7.3 {test setting internal transition} {
    $lab setExpression "c;"
    list [$t0 getType] [$t0 getGuardExpression] [$act getExpression] 
} {2 true c=true}

######################################################################
####
#
test InterfaceAutomaton-8.1 {test generating moml} {
    $ia exportMoML
} {<entity name="ia" class="ptolemy.domains.fsm.kernel.InterfaceAutomaton">
    <property name="initialStateName" class="ptolemy.kernel.util.StringAttribute" value="s0">
    </property>
    <property name="finalStateNames" class="ptolemy.kernel.util.StringAttribute">
    </property>
    <property name="_nonStrictMarker" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="c" class="ptolemy.data.expr.Parameter">
    </property>
    <entity name="s0" class="ptolemy.domains.fsm.kernel.State">
        <property name="refinementName" class="ptolemy.kernel.util.StringAttribute">
        </property>
        <port name="incomingPort" class="ptolemy.kernel.ComponentPort">
        </port>
        <port name="outgoingPort" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <entity name="s1" class="ptolemy.domains.fsm.kernel.State">
        <property name="refinementName" class="ptolemy.kernel.util.StringAttribute">
        </property>
        <port name="incomingPort" class="ptolemy.kernel.ComponentPort">
        </port>
        <port name="outgoingPort" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="t0" class="ptolemy.domains.fsm.kernel.InterfaceAutomatonTransition">
        <property name="guardExpression" class="ptolemy.kernel.util.StringAttribute" value="true">
        </property>
        <property name="outputActions" class="ptolemy.domains.fsm.kernel.OutputActionsAttribute" value="c=true">
        </property>
        <property name="setActions" class="ptolemy.domains.fsm.kernel.CommitActionsAttribute">
        </property>
        <property name="exitAngle" class="ptolemy.data.expr.Parameter" value="PI/5.0">
        </property>
        <property name="gamma" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="reset" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="preemptive" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="triggerExpression" class="ptolemy.kernel.util.StringAttribute">
        </property>
        <property name="refinementName" class="ptolemy.kernel.util.StringAttribute">
        </property>
        <property name="label" class="ptolemy.kernel.util.StringAttribute" value="c;">
        </property>
    </relation>
    <link port="s0.outgoingPort" relation="t0"/>
    <link port="s1.incomingPort" relation="t0"/>
</entity>
}


