# Tests for the Event class
#
# @Author: John S. Davis II
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test TimedQueueReceiver-2.1 {Check IOPort container in new receiver} {
    set iop [java::new ptolemy.actor.IOPort]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    list [expr { $iop == [$tqr getContainer] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-3.1 {Check that hasToken() works for empty queue} {
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver]
    set actor [java::new ptolemy.domains.dde.kernel.DDEActor]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]
    $tqr setReceivingTimeKeeper $keeper
    list [expr { 0 == [$tqr hasToken] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-3.2 {Check hasToken() for non-empty queue} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]
    $tqr setReceivingTimeKeeper $keeper

    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 0.0
    $tqr put $t2 5.0
    list [expr { 1 == [$tqr hasToken] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-4.1 {Check hasRoom() for finite, empty queue} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]
    $tqr setReceivingTimeKeeper $keeper

    $tqr setCapacity 10
    list [expr { 1 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-4.2 {Check that hasRoom() works for full queue} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]
    $tqr setReceivingTimeKeeper $keeper
    $tqr setCapacity 2

    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 10.0
    $tqr put $t2 12.5
    list [expr { 0 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-4.3 {Check hasRoom() for infinite capacity queue} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]
    $tqr setReceivingTimeKeeper $keeper

    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]
    $tqr put $t1 3.5
    $tqr put $t2 3.6
    $tqr put $t3 3.7
    list [expr { 1 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-5.1 {get(), put(), check _rcvrTime and _lastTime} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]
    $tqr setReceivingTimeKeeper $keeper

    set token0 [java::new ptolemy.data.Token]
    set token1 [java::new ptolemy.data.Token]
    set token2 [java::new ptolemy.data.Token]

    $tqr put $token0 5.0
    set rcvrTime0 [$tqr getRcvrTime]
    set lastTime0 [$tqr getLastTime]

    $tqr put $token1 7.0
    set rcvrTime1 [$tqr getRcvrTime]
    set lastTime1 [$tqr getLastTime]

    set outToken0 [$tqr get]
    set rcvrTime2 [$tqr getRcvrTime]
    set lastTime2 [$tqr getLastTime]

    $tqr put $token2 15.0
    set rcvrTime3 [$tqr getRcvrTime]
    set lastTime3 [$tqr getLastTime]

    list $rcvrTime0 $lastTime0 $rcvrTime1 $lastTime1 $rcvrTime2 \
	    $lastTime2 $rcvrTime3 $lastTime3
} {5.0 5.0 5.0 7.0 7.0 7.0 7.0 15.0}

######################################################################
####
#
test TimedQueueReceiver-6.1 {Check for exception with put() given \
	full queue} {
    set wkspc [java::new ptolemy.kernel.util.Workspace]
    set comp [java::new ptolemy.actor.TypedCompositeActor $wkspc]
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor \
	    $comp "Receiver"]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor \
	    $comp "Sender"]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set rkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actorRcvr]
    set skeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actorSend]

    $tqr setReceivingTimeKeeper $rkeeper
    $tqr setSendingTimeKeeper $skeeper
    $tqr setCapacity 0
    set token [java::new ptolemy.data.Token]

    catch {$tqr put $token 0.0} msg
    list $msg
} {{ptolemy.actor.NoRoomException: ..Receiver.port: Queue is at capacity. Cannot insert token.}}

######################################################################
####
#
test TimedQueueReceiver-6.2 {Check for exception with get() given empty \
	queue} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop]
    set t [java::new ptolemy.data.Token]
    catch {$tqr get} msg 
    list $msg
} {{java.util.NoSuchElementException: The FIFOQueue is empty!}}















