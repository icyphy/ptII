# Tests for the FmvAutomatonclass
#
# @Author: Christopher Brooks, based on IA tests by Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 2000-2008 The Regents of the University of California.
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
    set fmv [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton $e0 fmv]

    set v0 [java::field [java::cast ptolemy.domains.fsm.kernel.FSMActor $fmv] \
        initialStateName]

    set fmv1 [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton]
    set ws [java::new ptolemy.kernel.util.Workspace]
    set fmv2 [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton $ws]
    list [$fmv getFullName] [$v0 getFullName] [$fmv1 getFullName] \
            [$fmv2 getFullName]
} {..fmv ..fmv.initialStateName . .}

test InterfaceAutomaton-1.2 {container must be TypedCompositeActor or null} {
    $fmv setContainer [java::null]
    set re0 [$fmv getFullName]
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setName testContainer
    $fmv setContainer $e1
    list $re0 [[$fmv getContainer] getFullName]
} {.fmv .testContainer}

######################################################################
####
#
test InterfaceAutomaton-2.1 {test getDirector} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fmv [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton $e0 fmv]
    set re0 [expr {[$fmv getDirector] == $dir}]
    set re1 [expr {[$fmv getExecutiveDirector] == $dir}]
    $fmv setContainer [java::null]
    set re2 [expr {[$fmv getDirector] == [java::null]}]
    list $re0 $re1 $re2
} {1 1 1}

test InterfaceAutomaton-2.2 {test getManager} {
    set mag [java::new ptolemy.actor.Manager]
    $e0 setManager $mag
    set re1 [expr {[$fmv getManager] == [java::null]}]
    $fmv setContainer $e0
    set re2 [expr {[$fmv getManager] == $mag}]
    list $re1 $re2
} {1 1}

######################################################################
####
#
test InterfaceAutomaton-3.1 {test listing input and output ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fmv [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton $e0 fmv]
    set p0 [java::new ptolemy.actor.TypedIOPort $fmv p0]
    set p1 [java::new ptolemy.actor.TypedIOPort $fmv p1 true true]
    set p2 [java::new ptolemy.actor.TypedIOPort $fmv p2 true false]
    set p3 [java::new ptolemy.actor.TypedIOPort $fmv p3 false true]
    list [listToFullNames [$fmv inputPortList]] \
            [listToFullNames [$fmv outputPortList]]
} {{..fmv.p1 ..fmv.p2} {..fmv.p1 ..fmv.p3}}

test InterfaceAutomaton-3.2 {test newPort} {
    set p4 [$fmv newPort p4]
    list [java::instanceof $p4 ptolemy.actor.TypedIOPort] \
            [listToFullNames [$fmv portList]]
} {1 {..fmv.p0 ..fmv.p1 ..fmv.p2 ..fmv.p3 ..fmv.p4}}

test InterfaceAutomaton-3.3 {test newReceiver} {
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set r [$fmv newReceiver]
    set tok [java::new ptolemy.data.StringToken foo]
    $r put $tok
    set received [java::cast ptolemy.data.StringToken [$r get]]
    $received stringValue
} {foo}

######################################################################
####
#
test InterfaceAutomaton-4.1 {test setting initfmvl state} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set fmv [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton $e0 fmv]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fmv s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $fmv s1]
    set p [java::field [java::cast \
        ptolemy.domains.fsm.kernel.FSMActor $fmv] initialStateName]
    $p setExpression s0
    set re0 [expr {[$fmv getInitialState] == $s0}]
    $p setExpression s1
    set re1 [expr {[$fmv getInitialState] == $s1}]
    $p setExpression s2
    catch {$fmv getInitialState} msg
    list $re0 $re1 $msg
} {1 1 {ptolemy.kernel.util.IllegalActionException: Cannot find initial state with name "s2".
  in .<Unnamed Object>.fmv}}

######################################################################
####
#
test InterfaceAutomaton-6.1 {test newRelation} {
    set fmv [java::new ptolemy.domains.fsm.kernel.fmv.FmvAutomaton]
    set r0 [$fmv newRelation r0]
    set re0 [java::instanceof $r0 \
        ptolemy.domains.fsm.kernel.Transition]
    catch {$fmv newRelation r0} msg
    list $re0 [$r0 getFullName] $msg
} {1 ..r0 {ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "r0" into container named ".<Unnamed Object>", which already contains an object with that name.}}
