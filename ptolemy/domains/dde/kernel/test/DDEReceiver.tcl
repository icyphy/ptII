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
# Global Variables 
set globalEndTimeRcvr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver]
set globalEndTime [java::field $globalEndTimeRcvr INACTIVE]
set globalIgnoreTimeRcvr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver]
set globalIgnoreTime -1.0
# set globalIgnoreTime [java::field $globalIgnoreTimeRcvr IGNORE]
set globalNullTok [java::new ptolemy.domains.dde.kernel.NullToken]

######################################################################
####
#
test DDEReceiver-2.1 {Send three tokens between two actors} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]

    set tok1 [java::new ptolemy.data.Token]
    $actorSend setToken $tok1 5.0 0 
    $actorSend setToken $tok1 15.0 1
    $actorSend setToken $tok1 25.0 2

    set ioprcvr [$actorRcvr getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set rel [$toplevel connect $ioprcvr $iopsend "rel"]

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]

    list $time0 $time1 $time2

} {5.0 15.0 25.0}

######################################################################
####
#
test DDEReceiver-2.2 {Send a real token, an ignore token and a real token.} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 2]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]

    set tok1 [java::new ptolemy.data.Token]
    $actorSend setToken $tok1 5.0 0 
    $actorSend setToken $tok1 $globalIgnoreTime 1
    $actorSend setToken $tok1 25.0 2

    set ioprcvr [$actorRcvr getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set rel [$toplevel connect $ioprcvr $iopsend "rel"]

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]

    list $time0 $time1

} {5.0 25.0}

######################################################################
####
#
test DDEReceiver-2.3 {Send NullTokens through FlowThrough.} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 6]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "actorThru"]

    set tok1 [java::new ptolemy.data.Token]

    $actorSend setToken $globalNullTok 5.0 0 
    $actorSend setToken $tok1 7.0 1
    $actorSend setToken $globalNullTok 8.0 2 
    $actorSend setToken $globalNullTok 8.0 3 
    $actorSend setToken $globalNullTok 9.0 4 
    $actorSend setToken $tok1 9.5 5

    set rcvrInPort [$actorRcvr getPort "input"]
    set sendOutPort [$actorSend getPort "output"]
    set thruInPort [$actorThru getPort "input"]
    set thruOutPort [$actorThru getPort "output"]

    $toplevel connect $sendOutPort $thruInPort "rel1"
    $toplevel connect $thruOutPort $rcvrInPort "rel2"

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]

    list $time0 $time1 $time2

} {7.0 9.5 -1.0}

######################################################################
####
#
test DDEReceiver-2.4 {Send Ignore and Real through multiport.} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 5]
    set actorSend1 [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend1" 3]
    set actorSend2 [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend2" 3]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "actorThru"]

    set tok1 [java::new ptolemy.data.Token]
    
    $actorSend1 setToken $tok1 $globalIgnoreTime 0 
    $actorSend1 setToken $tok1 5.0 1 
    $actorSend1 setToken $tok1 7.0 2 
    
    $actorSend2 setToken $tok1 4.0 0 
    $actorSend2 setToken $tok1 6.0 1 
    $actorSend2 setToken $tok1 8.0 2 
    
    set rcvrInPort [$actorRcvr getPort "input"]
    set sendOutPort1 [$actorSend1 getPort "output"]
    set sendOutPort2 [$actorSend2 getPort "output"]
    set thruInPort [$actorThru getPort "input"]
    set thruOutPort [$actorThru getPort "output"]
    
    $toplevel connect $sendOutPort1 $thruInPort
    $toplevel connect $sendOutPort2 $thruInPort
    $toplevel connect $thruOutPort $rcvrInPort

    $mgr run
    
    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]
    set time3 [$actorRcvr getAfterTime 3]
    set time4 [$actorRcvr getAfterTime 4]
    
    list $time0 $time1 $time2 $time3 $time4 
} {4.0 5.0 6.0 7.0 8.0}    

######################################################################
####
#
test DDEReceiver-3.1 {Check is...Boundary() for single layer boundary} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set wormhole [java::new ptolemy.actor.TypedCompositeActor $toplevel "wormhole"]
    set topdir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "topdirector"]
    set wormdir [java::new ptolemy.domains.dde.kernel.DDEDirector $wormhole "wormdirector"]
    
    # Assign Directors/Managers
    $toplevel setManager $mgr
    $toplevel setDirector $topdir
    $wormhole setDirector $wormdir
    
    # Instantiate Actors
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThrough $wormhole "actorThru"]
    set actorInner [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $wormhole "actorInner" 3]

    # Add Ports to the Wormhole
    set wormin [java::new ptolemy.actor.TypedIOPort $wormhole "input" true false]
    set wormout [java::new ptolemy.actor.TypedIOPort $wormhole "output" false true]
    
    # Get Ports for the Atomic Actors
    set rcvrPort [$actorRcvr getPort "input"]
    set sendPort [$actorSend getPort "output"]
    set flowinPort [$actorThru getPort "input"]
    set flowoutPort [$actorThru getPort "output"]
    set innerPort [$actorInner getPort "input"]
    
    # Connect Inside Wormhole Ports
    $wormhole connect $wormin $flowinPort
    $wormhole connect $innerPort $flowoutPort 
    $wormhole connect $flowoutPort $wormout 

    # Connect Outer Ports
    $toplevel connect $sendPort $wormin
    $toplevel connect $wormout $rcvrPort
    
    # Create Receivers
    $toplevel initialize
    
    set iopFlowin [java::cast ptolemy.actor.TypedIOPort $flowinPort]
    set rcvrs [$iopFlowin getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val1a [$rcvr isInsideBoundary]
    set val1b [$rcvr isConnectedToBoundary]
    set val1c [$rcvr isOutsideBoundary]
    
    set iopInnerPort [java::cast ptolemy.actor.TypedIOPort $innerPort]
    set rcvrs [$iopInnerPort getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val2a [$rcvr isInsideBoundary]
    set val2b [$rcvr isConnectedToBoundary]
    set val2c [$rcvr isOutsideBoundary]
    
    set iopRcvrPort [java::cast ptolemy.actor.TypedIOPort $rcvrPort]
    set rcvrs [$iopRcvrPort getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val3a [$rcvr isInsideBoundary]
    set val3b [$rcvr isConnectedToBoundary]
    set val3c [$rcvr isOutsideBoundary]
    
    set iopWormOut [java::cast ptolemy.actor.TypedIOPort $wormout]
    set rcvrs [$iopWormOut getInsideReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]

    set val4a [$rcvr isInsideBoundary]
    set val4b [$rcvr isConnectedToBoundary]
    set val4c [$rcvr isOutsideBoundary]
    
    set iopWormIn [java::cast ptolemy.actor.TypedIOPort $wormin]
    set rcvrs [$iopWormIn getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val5a [$rcvr isInsideBoundary]
    set val5b [$rcvr isConnectedToBoundary]
    set val5c [$rcvr isOutsideBoundary]
    
    list $val1a $val1b $val1c $val2a $val2b $val2c $val3a $val3b $val3c $val4a $val4b $val4c $val5a $val5b $val5c
    
} {0 1 0 0 0 0 0 0 0 1 0 0 0 0 1}

######################################################################
####
#
test DDEReceiver-3.2 {Check is...Boundary() for multilayered boundaries} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set outerworm [java::new ptolemy.actor.TypedCompositeActor $toplevel "outerworm"]
    set innerworm [java::new ptolemy.actor.TypedCompositeActor $outerworm "innerworm"]
    set topdir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "topdirector"]
    set outerwormdir [java::new ptolemy.domains.dde.kernel.DDEDirector $outerworm "outerwormdirector"]
    set innerwormdir [java::new ptolemy.domains.dde.kernel.DDEDirector $innerworm "innerwormdirector"]
    
    # Assign Directors/Managers
    $toplevel setManager $mgr
    $toplevel setDirector $topdir
    $outerworm setDirector $outerwormdir
    $innerworm setDirector $innerwormdir
    
    # Instantiate Atomic Actors
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 3]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThrough $innerworm "actorThru"]
    set actorInner [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $innerworm "actorInner" 3]

    # Add Ports to the Inner Wormhole
    set innerwormin [java::new ptolemy.actor.TypedIOPort $innerworm "input" true false]
    set innerwormout [java::new ptolemy.actor.TypedIOPort $innerworm "output" false true]
    
    # Add Ports to the Outer Wormhole
    set outerwormin [java::new ptolemy.actor.TypedIOPort $outerworm "input" true false]
    set outerwormout [java::new ptolemy.actor.TypedIOPort $outerworm "output" false true]
    
    # Get Ports for the Atomic Actors
    set rcvrPort [$actorRcvr getPort "input"]
    set sendPort [$actorSend getPort "output"]
    set flowinPort [$actorThru getPort "input"]
    set flowoutPort [$actorThru getPort "output"]
    set innerPort [$actorInner getPort "input"]
    
    # Connect Interior Inner Wormhole Ports
    $innerworm connect $innerwormin $flowinPort
    $innerworm connect $innerPort $flowoutPort 
    $innerworm connect $flowoutPort $innerwormout 

    # Connect Interior Outer Wormhole Ports
    $outerworm connect $outerwormin $innerwormin 
    $outerworm connect $innerwormout $outerwormout 
    
    # Connect Interior Top Level Ports
    $toplevel connect $sendPort $outerwormin
    $toplevel connect $outerwormout $rcvrPort
    
    # Create Receivers
    $toplevel initialize
    
    set iopInnerWormOut [java::cast ptolemy.actor.TypedIOPort $innerwormout]
    set rcvrs [$iopInnerWormOut getInsideReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val4a [$rcvr isInsideBoundary]
    set val4b [$rcvr isConnectedToBoundary]
    set val4c [$rcvr isOutsideBoundary]
    
    set iopInnerWormIn [java::cast ptolemy.actor.TypedIOPort $innerwormin]
    set rcvrs [$iopInnerWormIn getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val5a [$rcvr isInsideBoundary]
    set val5b [$rcvr isConnectedToBoundary]
    set val5c [$rcvr isOutsideBoundary]
    
    set iopOuterWormOut [java::cast ptolemy.actor.TypedIOPort $outerwormout]
    set rcvrs [$iopOuterWormOut getInsideReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val6a [$rcvr isInsideBoundary]
    set val6b [$rcvr isConnectedToBoundary]
    set val6c [$rcvr isOutsideBoundary]
    
    set iopOuterWormIn [java::cast ptolemy.actor.TypedIOPort $outerwormin]
    set rcvrs [$iopOuterWormIn getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$rcvrs get {0 0}]]
    set val7a [$rcvr isInsideBoundary]
    set val7b [$rcvr isConnectedToBoundary]
    set val7c [$rcvr isOutsideBoundary]
    
    list $val4a $val4b $val4c $val5a $val5b $val5c $val6a $val6b $val6c $val7a $val7b $val7c 
    
} {1 0 0 0 1 1 1 0 0 0 0 1}


















