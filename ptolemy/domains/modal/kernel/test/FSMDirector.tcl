# Tests for the FSMDirector class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 1999-2011 The Regents of the University of California.
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
    set dir1 [java::new ptolemy.domains.modal.kernel.FSMDirector]
    set dir2 [java::new ptolemy.domains.modal.kernel.FSMDirector $w]
    set e0 [java::new ptolemy.actor.CompositeActor]
    set dir3 [java::new ptolemy.domains.modal.kernel.FSMDirector $e0 dir]
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
    set dir [java::new ptolemy.domains.modal.kernel.FSMDirector $e0 dir]
    set v0 [java::field $dir controllerName]
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e0 fsm]
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
test FSMDirector-3.1 {test getNextIterationTime} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.domains.de.kernel.DEDirector $e0 dir]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    set fsmDir [java::new ptolemy.domains.modal.kernel.FSMDirector $e1 fsmDir]
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e1 fsm]
    [java::field $fsmDir controllerName] setExpression fsm
    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    [java::field $fsm initialStateName] setExpression s0
    [java::field $s0 refinementName] setExpression e2
    set e2 [java::new ptolemy.actor.TypedCompositeActor $e1 e2]
    set dir1 [java::new ptolemy.domains.de.kernel.DEDirector $e2 dir1]
    $dir preinitialize
    $dir initialize
    set time [java::new ptolemy.actor.util.Time $dir]
    $dir1 setModelTime [$time {add double} 3.0]
    $fsmDir setModelTime [$time {add double} 2.0]
    set re0 [$fsmDir getNextIterationTime]
    $e0 setDirector [java::null]
    set re1 [$fsmDir getNextIterationTime]
    list $re0 $re1
} {Infinity 2.0}

######################################################################
####
#
# Removed this test, which is too bizarre for words.
# It has a refinement that is an actor that feeds data to the controller.
# We are not interested in topologies like this.  EAL 10/10/12
#
# test FSMDirector-4.1 {test action methods} {
#     set e0 [deModel 3.5]
#     set clk [java::new ptolemy.actor.lib.Clock $e0 clk]
#     set src [java::new ptolemy.actor.lib.Ramp $e0 src]
#     set r0 [java::new ptolemy.actor.TypedIORelation $e0 r0]
#     [java::field [java::cast ptolemy.actor.lib.Source $clk] output] link $r0
#     [java::field [java::cast ptolemy.actor.lib.Source $src] trigger] link $r0
#     [java::field $src step] setExpression "3"
#     set e1 [java::new ptolemy.domains.modal.modal.ModalModel $e0 e1]
# 	[java::field $e1 directorClass] setExpression "ptolemy.domains.modal.kernel.FSMDirector"
# 	[java::field $e1 directorClass] validate
# #    The following commented statements were the way to construct
# #    a modal model before the ModalModel class was implemented.
# #    We use ModalModel class instead to avoid GraphConstructionException
# #    when constructing an IODependency of a modal model.
# #    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
# #    set dir [java::new ptolemy.domains.modal.kernel.FSMDirector $e1 dir]
#     set e2 [java::new ptolemy.actor.lib.Const $e1 e2]
#     set tok [java::new {ptolemy.data.IntToken int} 6]
#     [java::field $e2 value] setToken $tok
#     set fsm [$e1 getController]
# #    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e1 fsm]
#     set p0 [java::new ptolemy.actor.TypedIOPort $e1 p0]
#     $p0 setInput true
#     set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
#     [java::field [java::cast ptolemy.actor.lib.Source $src] output] link $r1
#     $p0 link $r1
#     #set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1]
#     set p1 [java::cast ptolemy.actor.TypedIOPort [$fsm newPort p1]]
#     $p1 setInput true
#     #set p2 [java::new ptolemy.actor.TypedIOPort $fsm p2]
#     set p2 [java::cast ptolemy.actor.TypedIOPort [$fsm newPort p2]]
#     $p2 setOutput true
#     $p2 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
#     #set r2 [java::new ptolemy.actor.TypedIORelation $e1 r2]
#     set lrl1 [$p1 linkedRelationList]
#     set r2 [$lrl1 get 0]
#     $p0 link $r2
#     #$p1 link $r2
#     [java::field [java::cast ptolemy.actor.lib.Source $e2] output] link $r2
#     set p3 [java::new ptolemy.actor.TypedIOPort $e1 p3]
#     $p3 setOutput true
#     $p3 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
#     #set r3 [java::new ptolemy.actor.TypedIORelation $e1 r3]
#     set lrl2 [$p2 linkedRelationList]
#     set r3 [$lrl2 get 0]
# 
#     #$p2 link $r3
#     $p3 link $r3
#     set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
#     set r4 [java::new ptolemy.actor.TypedIORelation $e0 r4]
#     $p3 link $r4
#     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input] link $r4
# 
#     set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
#     set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
#     set t0 [java::new ptolemy.domains.modal.kernel.Transition $fsm t0]
#     set t1 [java::new ptolemy.domains.modal.kernel.Transition $fsm t1]
#     set t2 [java::new ptolemy.domains.modal.kernel.Transition $fsm t2]
#     [java::field $s0 outgoingPort] link $t0
#     [java::field $s1 incomingPort] link $t0
#     [java::field $s1 outgoingPort] link $t1
#     [java::field $s0 incomingPort] link $t1
#     [java::field $s0 outgoingPort] link $t2
#     [java::field $s1 incomingPort] link $t2
#     [java::field $fsm initialStateName] setExpression s0
#     [java::field $s0 refinementName] setExpression e2
#     [java::field $s1 refinementName] setExpression e2
#     $t0 setGuardExpression "p1_isPresent && p1 > 5"
#     [java::field $t1 preemptive] setExpression "true"
#     $t1 setGuardExpression "p1_isPresent && p1 > 0"
#     [java::field $t2 preemptive] setExpression "true"
#     $t2 setGuardExpression "p1_isPresent && p1 > 5"
#     set act0 [java::field $t0 outputActions]
#     $act0 setExpression "p2 = 1"
#     set act1 [java::field $t1 outputActions]
#     $act1 setExpression "p2 = p1"
#     set act2 [java::field $t2 outputActions]
#     $act2 setExpression "p2 = 0"
# 
#     [$e0 getManager] execute
#     # The same test in fsm has the result {1 3 1 6 1 9 1}
#     # See also modal/kernel/test/auto/bizarre.xml and the 
# 
#     # This test is really a weird one. The output of the
#     # refinement of the fsm, called e2, connects to the input of the
#     # fsm (p1) rather than the output (p2). Therefore, the simulation
#     # results (logic) are shown as follow.
#     # 
#     #	state	input(p1)	|	output(p2)		next state
#     #	s0		0				(no output)		s0
#     #	s0		6(from e2)		1				s1
#     #	s1		3, 6 (one external and one from e2, in this order)
#     #	(continuing ...)		3				s0
#     # 	s0		6				1				s1
#     #	s1		6, 6			6				s0
#     #	s0		6				1				s1
#     # 	... (omitted)
# 
#     listToStrings [$rec getHistory 0]
# } {1 3 1 6 1 9 1}

######################################################################
####
#
test FSMDirector-5.1 {test fireAt} {
    set e0 [deModel 3.0]
    set e1 [java::new ptolemy.domains.modal.modal.ModalModel $e0 e1]
    [java::field $e1 directorClass] setExpression "ptolemy.domains.modal.kernel.FSMDirector"
    # The above won't take hold until parameters are validated.
    # This is a bit hard to do, since we have to defer change requests,
    # then execute them.
    $e0 setDeferringChangeRequests true
    $e0 validateSettables
    $e0 executeChangeRequests
    set dir [java::cast ptolemy.domains.modal.kernel.FSMDirector [$e1 getDirector]]
    set fsm [$e1 getController]
    #set p2 [java::new ptolemy.actor.TypedIOPort $fsm p2]
    set p2 [java::cast ptolemy.actor.TypedIOPort [$fsm newPort p2]]
    $p2 setOutput true
    $p2 setTypeEquals [java::field ptolemy.data.type.BaseType INT]

    #set p3 [java::new ptolemy.actor.TypedIOPort $e1 p3]
    set p3 [java::cast ptolemy.actor.TypedIOPort [$fsm newPort p3]]
    $p3 setOutput true
    $p3 setTypeEquals [java::field ptolemy.data.type.BaseType INT]

    #set r3 [java::new ptolemy.actor.TypedIORelation $e1 r3]
    #$p2 link $r3
    #$p3 link $r3
    set lrl2 [$p2 linkedRelationList]
    set r3 [$lrl2 get 0]

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    #set r4 [java::new ptolemy.actor.TypedIORelation $e0 r4]
    #$p3 link $r4
    #set lrl3 [$p3 linkedRelationList]
    #set r4 [$lrl3 get 0]
    [java::field [java::cast ptolemy.actor.lib.Sink $rec] input] link $r3

    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
    [java::field $fsm initialStateName] setExpression s0
    set t0 [java::new ptolemy.domains.modal.kernel.Transition $fsm t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    $t0 setGuardExpression "true"
    set act0 [java::field $t0 outputActions]
    $act0 setExpression "p2 = -1000"

    set mag [$e0 getManager]
    $mag initialize
    set time [java::new ptolemy.actor.util.Time $dir]
    $dir fireAt $fsm [$time {add double} 1.111] 0
    # With DE change of 6/11, have to iterate twice to get to microstep 1.
    $mag iterate
    $mag iterate
    $mag wrapup
    # In fsm, the results would be {1.111 -1000}
    # We get 0.0 because calling fireAt on the controller of the ModalModel
    # does not result in firing the Modal model.
    list [listToStrings [$rec getTimeHistory]] \
            [listToStrings [$rec getHistory 0]]
} {0.0 -1000}

######################################################################
####
#
test FSMDirector-6.1 {test transferInputs} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setManager [java::new ptolemy.actor.Manager]    
    set d0 [java::new ptolemy.domains.de.kernel.DEDirector $e0 d0]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 e1]
    set dir [java::new ptolemy.domains.modal.kernel.FSMDirector $e1 dir]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e1 e2]
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e1 e3]
    set fsm [java::new ptolemy.domains.modal.kernel.FSMActor $e1 fsm]
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

    set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
    set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
    set t0 [java::new ptolemy.domains.modal.kernel.Transition $fsm t0]
    set t1 [java::new ptolemy.domains.modal.kernel.Transition $fsm t1]
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
    # This used to work in fsm, but does not in modal.
    catch  {set re1 [$p3 hasToken 0]} errMsg
    list $errMsg
#    $e1 postfire
#    $p00 broadcast $tok
#    $e1 prefire
#    $e1 fire
#    set re2 [$p2 hasToken 0]
#    set re3 [[$p3 get 0] toString]
#    $e1 postfire
#    set re4 [[$fsm currentState] getFullName]
#    $d0 terminate
#    list $re0 $re1 $re2 $re3 $re4
#  was {6 0 1 6 ..e1.fsm.s0}
} {{ptolemy.kernel.util.InternalErrorException: Receiver status is not known.
  in .<Unnamed Object>.e1.e3.p3}}


######################################################################
####
#
#
# Removed this test, which is too bizarre for words.
# It has a refinement that is an actor that feeds data to the controller.
# We are not interested in topologies like this.  EAL 10/10/12
#
# test FSMDirector-7.1 {test clone a modal model} {
#     set e0 [deModel 3.5]
#     set clk [java::new ptolemy.actor.lib.Clock $e0 clk]
#     set src [java::new ptolemy.actor.lib.Ramp $e0 src]
#     set r0 [java::new ptolemy.actor.TypedIORelation $e0 r0]
#     [java::field [java::cast ptolemy.actor.lib.Source $clk] output] link $r0
#     [java::field [java::cast ptolemy.actor.lib.Source $src] trigger] link $r0
#     [java::field $src step] setExpression "3"
# 
#     set e1 [java::new ptolemy.domains.modal.modal.ModalModel $e0 e1]
# 	[java::field $e1 directorClass] setExpression "ptolemy.domains.modal.kernel.FSMDirector"
#     set e2 [java::new ptolemy.actor.lib.Const $e1 e2]
#     set tok [java::new {ptolemy.data.IntToken int} 6]
#     [java::field $e2 value] setToken $tok
#     set fsm [$e1 getController]
#     set p0 [java::new ptolemy.actor.TypedIOPort $e1 p0]
#     $p0 setInput true
#     
#     #set p1 [java::new ptolemy.actor.TypedIOPort $fsm p1]
#     set p1 [java::cast ptolemy.actor.TypedIOPort [$fsm newPort p1]]
#     $p1 setInput true
# 
#     #set p2 [java::new ptolemy.actor.TypedIOPort $fsm p2]
#     set p2 [java::cast ptolemy.actor.TypedIOPort [$fsm newPort p2]]
#     $p2 setOutput true
#     $p2 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
# 
#     #set r2 [java::new ptolemy.actor.TypedIORelation $e1 r2]
#     #$p0 link $r2
#     #$p1 link $r2
#     set lrl1 [$p1 linkedRelationList]
#     set r2 [$lrl1 get 0]
#     [java::field [java::cast ptolemy.actor.lib.Source $e2] output] link $r2
# 
#     #set p3 [java::new ptolemy.actor.TypedIOPort $e1 p3]
#     #$p3 setOutput true
#     #$p3 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
#     #set r3 [java::new ptolemy.actor.TypedIORelation $e1 r3]
#     #$p2 link $r3
#     set lrl2 [$p2 linkedRelationList]
#     set r3 [java::cast ptolemy.kernel.Relation [$lrl2 get 0]]
#     set pl [$r3 linkedPortList [java::null]]
#     set p3 [$pl get 1]
#     #$p3 link $r3
# 
#     set s0 [java::new ptolemy.domains.modal.kernel.State $fsm s0]
#     set s1 [java::new ptolemy.domains.modal.kernel.State $fsm s1]
#     set t0 [java::new ptolemy.domains.modal.kernel.Transition $fsm t0]
#     set t1 [java::new ptolemy.domains.modal.kernel.Transition $fsm t1]
#     set t2 [java::new ptolemy.domains.modal.kernel.Transition $fsm t2]
#     [java::field $s0 outgoingPort] link $t0
#     [java::field $s1 incomingPort] link $t0
#     [java::field $s1 outgoingPort] link $t1
#     [java::field $s0 incomingPort] link $t1
#     [java::field $s0 outgoingPort] link $t2
#     [java::field $s1 incomingPort] link $t2
#     [java::field $fsm initialStateName] setExpression s0
#     [java::field $s0 refinementName] setExpression e2
#     [java::field $s1 refinementName] setExpression e2
#     $t0 setGuardExpression "p1_isPresent && p1 > 5"
#     [java::field $t1 preemptive] setExpression "true"
#     $t1 setGuardExpression "p1_isPresent && p1 > 0"
#     [java::field $t2 preemptive] setExpression "true"
#     $t2 setGuardExpression "p1_isPresent && p1 > 5"
#     set act0 [java::field $t0 outputActions]
#     $act0 setExpression "p2 = 1"
#     set act1 [java::field $t1 outputActions]
#     $act1 setExpression "p2 = p1"
#     set act2 [java::field $t2 outputActions]
#     $act2 setExpression "p2 = 0"
# 
#     set e1clone [java::cast ptolemy.domains.modal.modal.ModalModel \
#             [$e1 clone]]
#     
#     $e1clone setName e1clone
#     puts {####################################################################################}
#     # FIXME: The following setContainer causes a concurrent modification exception.
#     # presumably this is because queued change requests get executed at that point.
#     # why doesn't the above call to executeChangeRequests take care of that?
#     $e1clone setContainer $e0
#     set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
#     [java::field [java::cast ptolemy.actor.lib.Source $src] output] link $r1
#     [$e1clone getPort p1] link $r1
#     set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
#     set r4 [java::new ptolemy.actor.TypedIORelation $e0 r4]
#     [$e1clone getPort p2] link $r4
#     [java::field [java::cast ptolemy.actor.lib.Sink $rec] input] link $r4
# 
#     [$e0 getManager] execute
#     # In FSM, we get: {1 3 1 6 1 9 1}
#     listToStrings [$rec getHistory 0]
# } {1 3 1 6 1 9 1}
# 
