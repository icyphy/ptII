# Tests for the DDEReceiver class
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
test DDEReceiver-2.1 {get(), single arg put(), check _rcvrTime and _lastTime} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor $toplevel "actorRcvr"]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor $toplevel "actorSend"]
    set ioprcvr [java::new ptolemy.actor.TypedIOPort $actorRcvr  "port"]
    set iopsend [java::new ptolemy.actor.TypedIOPort $actorSend "port"]
    $ioprcvr setInput true
    $iopsend setOutput true
    set rel [$toplevel connect $ioprcvr $iopsend "rel"]
    $actorRcvr createReceivers
    $actorSend createReceivers
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actorSend]

    set rcvrs [$ioprcvr getReceivers]
    set odr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]

    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    set token0 [java::new ptolemy.data.Token]
    set token1 [java::new ptolemy.data.Token]
    set token2 [java::new ptolemy.data.Token]

    $sendkeeper setCurrentTime 12.5
    $odr put $token0
    set rcvrTime0 [$odr getRcvrTime]
    set lastTime0 [$odr getLastTime]

    $sendkeeper setDelayTime 12.5
    $odr put $token1
    set rcvrTime1 [$odr getRcvrTime]
    set lastTime1 [$odr getLastTime]

    set outToken0 [$odr get] 
    set rcvrTime2 [$odr getRcvrTime]

    list $rcvrTime0 $lastTime0 $rcvrTime1 $lastTime1 $rcvrTime2
} {12.5 12.5 12.5 25.0 25.0}

######################################################################
####
#
test DDEReceiver-2.2 {Put delayed event into non-empty queue; \
	check rcvrTime and lastTime} {
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $odr put $t1 
    $odr put $t2 5.0
    list [$odr getRcvrTime] [$odr getLastTime]
} {0.0 5.0}

######################################################################
####
#
test DDEReceiver-3.1 {Set event time = -1.0  and place into \
	empty queue; check rcvrTime and lastTime} {
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    $sendkeeper setCurrentTime -1.0
    $sendkeeper setDelayTime 5.0

    $odr put $t1 
    list [$odr getRcvrTime] [$odr getLastTime]
} {-1.0 -1.0}

######################################################################
####
#
test DDEReceiver-3.2 {Set negative delay time} {
    set wkspc [java::new ptolemy.kernel.util.Workspace]
    set comp [java::new ptolemy.actor.TypedCompositeActor $wkspc]
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor \
	    $comp "Receiver"]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor \
	    $comp "Sender"]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    catch {$sendkeeper setDelayTime -1.0} msg
    list $msg 

} {{ptolemy.kernel.util.IllegalActionException: Sender - Attempt to set negative delay time.}}

######################################################################
####
#
test DDEReceiver-3.3 {Set negative current time} {
    set wkspc [java::new ptolemy.kernel.util.Workspace]
    set comp [java::new ptolemy.actor.TypedCompositeActor $wkspc]
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor \
	    $comp "Receiver"]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor \
	    $comp "Sender"]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    catch {$sendkeeper setCurrentTime -1.0} msg1
    catch {$sendkeeper setCurrentTime -3.0} msg2
    list $msg1 $msg2

} {{} {java.lang.IllegalArgumentException: Sender - Attempt to set current time in the past.}}

######################################################################
####
#
test DDEReceiver-4.1 {Three gets followed by three puts} {
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]
    $odr put $t1 0.5
    $odr put $t2 5.0
    $odr put $t3 12.5
    set t4 [$odr get]
    set t5 [$odr get]
    set t6 [$odr get]
    list [expr {$t1 == $t4} ] [expr {$t2 == $t5} ] [expr {$t3 == $t6} ] \
	    [$odr getLastTime] [$odr getRcvrTime]
} {1 1 1 12.5 12.5}

######################################################################
####
#
test DDEReceiver-4.2 {Insert events in wrong order} {
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    $odr put $t1 5.0
    catch {$odr put $t2 0.5} msg
    list $msg
} {{java.lang.IllegalArgumentException:  - Attempt to set current time in the past.}}

######################################################################
####
#
test DDEReceiver-5.1 {hasToken() - tokens available} {
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    set token [java::new ptolemy.data.Token]
    $odr put $token 5.0
    set hastoken [$odr hasToken]

    list $hastoken
} {1}

######################################################################
####
# FIXME
test DDEReceiver-5.1 {hasToken() - tokens available} {
    set actorRcvr [java::new ptolemy.actor.TypedAtomicActor]
    set actorSend [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actorRcvr \
	    "port"]
    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setReceivingTimeKeeper $rcvrkeeper
    $odr setSendingTimeKeeper $sendkeeper

    set token [java::new ptolemy.data.Token]
    $odr put $token 5.0
    set hastoken [$odr hasToken]

    list $hastoken
} {1}












