# Tests for the DDEDirector class
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
# Global Variables 
set globalEndTimeRcvr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
set globalEndTime [java::field $globalEndTimeRcvr INACTIVE]


######################################################################
####
#
test DDEDirector-2.1 {Simple composite actor with ports contained in a composite actor} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    # $toplevel setName "toplevel"
    set wormhole [java::new ptolemy.actor.TypedCompositeActor $toplevel "wormhole"]
    set topdir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "topdir"]
    set wormdir [java::new ptolemy.domains.dde.kernel.DDEDirector $wormhole "wormdir"]
    
    # Assign Directors/Managers
    $toplevel setManager $mgr
    
    # Instantiate Actors
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThrough $wormhole "actorThru"]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]

    # Add Ports to the Wormhole
    set wormin [java::new ptolemy.actor.TypedIOPort $wormhole "input" true false]
    set wormout [java::new ptolemy.actor.TypedIOPort $wormhole "output" false true]
    
    # Get Ports for the Atomic Actors
    set ioprcvr [$actorRcvr getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set iopflowin [$actorThru getPort "input"]
    set iopflowout [$actorThru getPort "output"]
    
    # Connect Inside Wormhole Ports
    $wormhole connect $wormin $iopflowin
    $wormhole connect $wormout $iopflowout
    
    # Connect Other Ports
    $toplevel connect $iopsend $wormin
    $toplevel connect $wormout $ioprcvr

    # Specify Data 
    set tok1 [java::new ptolemy.data.Token]
    $actorSend setToken $tok1 5.0 0 
    $actorSend setToken $tok1 15.0 1
    $actorSend setToken $tok1 25.0 2

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 15.0 25.0}

######################################################################
####
#
test DDEDirector-3.1 {Portless composite actor contained in a composite actor} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set wormhole [java::new ptolemy.actor.TypedCompositeActor $toplevel "wormhole"]
    $toplevel setName "toplevel"
    set topleveldir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "topleveldir"]
    set wormholedir [java::new ptolemy.domains.dde.kernel.DDEDirector $wormhole "wormholedir"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    
    # $toplevel setDirector $topleveldir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $wormhole "actorRcvr" 3]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $wormhole "actorSend" 3]

    set tok1 [java::new ptolemy.data.Token]
    $actorSend setToken $tok1 5.0 0 
    $actorSend setToken $tok1 15.0 1
    $actorSend setToken $tok1 25.0 2

    set ioprcvr [$actorRcvr getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set rel [$wormhole connect $ioprcvr $iopsend "rel"]

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 15.0 25.0}

######################################################################
####
#
test DDEDirector-4.1 {Composite actor containing a closed feedback cycle} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set wormhole [java::new ptolemy.actor.TypedCompositeActor $toplevel "wormhole"]
    set topleveldir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "topleveldir"]
    set wormholedir [java::new ptolemy.domains.dde.kernel.DDEDirector $wormhole "wormholedir"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setManager $mgr
    
    # Set the stop time of the top level director
    set topleveldirStopTime [java::cast ptolemy.data.expr.Parameter [$topleveldir getAttribute stopTime]]
    $topleveldirStopTime setToken [java::new ptolemy.data.DoubleToken 25.0]
    
    # Instantiate the Clock actor and sets its output values
    set clock [java::new ptolemy.actor.lib.Clock $toplevel "clock"]
    set values [java::cast ptolemy.data.expr.Parameter [$clock getAttribute values]]
    $values setExpression {[1, 1]}
    set period [java::cast ptolemy.data.expr.Parameter [$clock getAttribute period]]
    $period setToken [java::new ptolemy.data.DoubleToken 20.0]
    set offsets [java::cast ptolemy.data.expr.Parameter [$clock getAttribute offsets]]
    $offsets setExpression {[5.0, 15.0]}
    set stopTime [java::cast ptolemy.data.expr.Parameter [$clock getAttribute stopTime]]
    $stopTime setToken [java::new ptolemy.data.DoubleToken 27.0]
    # set clock [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]
    # set tok1 [java::new ptolemy.data.Token]
    # $clock setToken $tok1 5.0 0 
    # $clock setToken $tok1 15.0 1
    # $clock setToken $tok1 25.0 2
    
    # Instantiate the other atomic actors
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set join [java::new ptolemy.domains.dde.kernel.test.FlowThrough $wormhole "join"]
    set fork [java::new ptolemy.domains.dde.kernel.test.TwoPut $wormhole "fork"]
    set fBack [java::new ptolemy.domains.dde.kernel.FeedBackDelay $wormhole "fBack"]
    set sink [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $wormhole "sink" 1]

    # Set the feedback delay parameter
    $fBack setDelay 4.0
    set realDelay [java::cast ptolemy.data.expr.Parameter [$fBack getAttribute realDelay]]
    $realDelay setToken [java::new ptolemy.data.BooleanToken true]

    # Instantiate the ports of the atomic actors
    set rcvrIn [$actorRcvr getPort "input"]
    set clockOut [$clock getPort "output"]
    set joinIn [$join getPort "input"]
    set joinOut [$join getPort "output"]
    set forkIn [$fork getPort "input"]
    set forkOut1 [$fork getPort "output1"]
    set forkOut2 [$fork getPort "output2"]
    set fBackIn [$fBack getPort "input"]
    set fBackOut [$fBack getPort "output"]
    set sinkIn [$sink getPort "input"]
    set clockOut [java::cast ptolemy.actor.TypedIOPort [$clock getPort "output"]]
    $clockOut setMultiport true
    
    # Add ports to the wormhole
    set wormin [java::new ptolemy.actor.TypedIOPort $wormhole "wormin" true false]
    set wormout [java::new ptolemy.actor.TypedIOPort $wormhole "wormout" false true]

    # Connect ports inside of the wormhole
    $wormhole connect $wormin $joinIn
    $wormhole connect $joinOut $forkIn
    $wormhole connect $fBackOut $joinIn
    $wormhole connect $fBackIn $forkOut1
    $wormhole connect $forkOut2 $sinkIn 
    $wormhole connect $forkOut1 $wormout
    
    # Connect ports outside of the wormhole
    $toplevel connect $clockOut $wormin
    $toplevel connect $wormout $rcvrIn

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 9.0 13.0}

######################################################################
####
#
test DDEDirector-4.1 {Composite actor containing a non-closed feedback cycle} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set wormhole [java::new ptolemy.actor.TypedCompositeActor $toplevel "wormhole"]
    set topleveldir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "topleveldir"]
    set wormholedir [java::new ptolemy.domains.dde.kernel.DDEDirector $wormhole "wormholedir"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setManager $mgr
    
    # Set the stop time of the top level director
    set topleveldirStopTime [java::cast ptolemy.data.expr.Parameter [$topleveldir getAttribute stopTime]]
    $topleveldirStopTime setToken [java::new ptolemy.data.DoubleToken 25.0]
    
    # Instantiate the Clock actor and sets its output values
    set clock [java::new ptolemy.actor.lib.Clock $toplevel "clock"]
    set values [java::cast ptolemy.data.expr.Parameter [$clock getAttribute values]]
    $values setExpression {[1, 1]}
    set period [java::cast ptolemy.data.expr.Parameter [$clock getAttribute period]]
    $period setToken [java::new ptolemy.data.DoubleToken 20.0]
    set offsets [java::cast ptolemy.data.expr.Parameter [$clock getAttribute offsets]]
    $offsets setExpression {[5.0, 15.0]}
    set stopTime [java::cast ptolemy.data.expr.Parameter [$clock getAttribute stopTime]]
    $stopTime setToken [java::new ptolemy.data.DoubleToken 27.0]
    
    # Instantiate the other atomic actors
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set join [java::new ptolemy.domains.dde.kernel.test.FlowThrough $wormhole "join"]
    set fork [java::new ptolemy.domains.dde.kernel.test.TwoPut $wormhole "fork"]
    set fBack [java::new ptolemy.domains.dde.kernel.FeedBackDelay $toplevel "fBack"]

    # Set the feedback delay parameter
    $fBack setDelay 4.0
    set realDelay [java::cast ptolemy.data.expr.Parameter [$fBack getAttribute realDelay]]
    $realDelay setToken [java::new ptolemy.data.BooleanToken true]

    # Instantiate the ports of the atomic actors
    set rcvrIn [$actorRcvr getPort "input"]
    set clockOut [$clock getPort "output"]
    set joinIn [$join getPort "input"]
    set joinOut [$join getPort "output"]
    set forkIn [$fork getPort "input"]
    set forkOut1 [$fork getPort "output1"]
    # set forkOut2 [$fork getPort "output2"]
    set fBackIn [$fBack getPort "input"]
    set fBackOut [$fBack getPort "output"]
    # set clockOut [java::cast ptolemy.actor.TypedIOPort [$clock getPort "output"]]
    # $clockOut setMultiport true
    
    # Add ports to the wormhole
    set worminA [java::new ptolemy.actor.TypedIOPort $wormhole "worminA" true false]
    set worminB [java::new ptolemy.actor.TypedIOPort $wormhole "worminB" true false]
    set wormoutA [java::new ptolemy.actor.TypedIOPort $wormhole "wormoutA" false true]
    set wormoutB [java::new ptolemy.actor.TypedIOPort $wormhole "wormoutB" false true]

    # Connect ports inside of the wormhole
    $wormhole connect $worminA $joinIn
    $wormhole connect $worminB $joinIn
    $wormhole connect $joinOut $forkIn
    $wormhole connect $forkOut1 $wormoutA
    $wormhole connect $forkOut1 $wormoutB

    # Connect ports outside of the wormhole
    $toplevel connect $clockOut $worminA
    $toplevel connect $wormoutA $rcvrIn
    $toplevel connect $fBackOut $worminB
    $toplevel connect $fBackIn $wormoutB

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 9.0 13.0}

