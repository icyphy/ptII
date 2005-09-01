# Tests for the DDEReceiver class
#
# @Author: John S. Davis II
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
# Global Variables 
set globalEndTimeReceiver [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
#set globalEndTime [java::field $globalEndTimeReceiver INACTIVE]
set globalEndTime -2.0
set globalIgnoreTimeReceiver [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
set globalIgnoreTime -1.0
# set globalIgnoreTime [java::field $globalIgnoreTimeReceiver IGNORE]
set globalNullTok [java::new ptolemy.domains.dde.kernel.NullToken]

# Call the various boundary* methods on the receiver
proc describeBoundary {receiver} {
    return "[$receiver isConnectedToBoundary] [$receiver isConnectedToBoundaryInside] [$receiver isConnectedToBoundaryOutside] [$receiver isProducerReceiver] [$receiver isReadBlocked] [$receiver isWriteBlocked] [$receiver hasRoom 1]"


}
######################################################################
####
#
test DDEReceiver-1.1 {Constructors} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set ddeActor [java::new ptolemy.domains.dde.kernel.DDEActor $toplevel "DDEActor"]
    set ioPort [java::new ptolemy.domains.dde.kernel.DDEIOPort $ddeActor "IOPort"]
    $ioPort setName IOPort1
    set r2 [java::new ptolemy.domains.dde.kernel.DDEReceiver $ioPort]
    set r3 [java::new ptolemy.domains.dde.kernel.DDEReceiver $ioPort 1]
    list [[$r2 getContainer] toString] [describeBoundary $r2] \
	[[$r3 getContainer] toString] [describeBoundary $r3] \
} {{ptolemy.domains.dde.kernel.DDEIOPort {..DDEActor.IOPort1}} {0 0 0 0 0 0 1} {ptolemy.domains.dde.kernel.DDEIOPort {..DDEActor.IOPort1}} {0 0 0 0 0 0 1}}

test DDEReceiver-1.2 {Constructors: make sure that the priorities are used} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set ddeActor [java::new ptolemy.domains.dde.kernel.DDEActor $toplevel "DDEActor"]
    set ioPort [java::new ptolemy.domains.dde.kernel.DDEIOPort $ddeActor "IOPort1"]

    set r3 [java::new ptolemy.domains.dde.kernel.DDEReceiver $ioPort 1]
    set r4 [java::new ptolemy.domains.dde.kernel.DDEReceiver $ioPort 2]

    set timeKeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $ddeActor]
    set receiverComparator \
	[java::new ptolemy.domains.dde.kernel.ReceiverComparator $timeKeeper]
    list \
	[$receiverComparator compare $r4 $r3] \
	[$receiverComparator compare $r3 $r3] \
	[$receiverComparator compare $r3 $r4] 
} {-1 0 1}

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

    set actorReceiver [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorReceiver" 3]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]

    set tok1 [java::new ptolemy.data.Token]
    $actorSend setToken $tok1 5.0 0 
    $actorSend setToken $tok1 15.0 1
    $actorSend setToken $tok1 25.0 2

    set ioprcvr [$actorReceiver getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set rel [$toplevel connect $ioprcvr $iopsend "rel"]

    $mgr run

    set time0 [$actorReceiver getAfterTime 0]
    set time1 [$actorReceiver getAfterTime 1]
    set time2 [$actorReceiver getAfterTime 2]

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

    set actorReceiver [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorReceiver" 2]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]

    set tok1 [java::new ptolemy.data.Token]
    $actorSend setToken $tok1 5.0 0 
    $actorSend setToken $tok1 $globalIgnoreTime 1
    $actorSend setToken $tok1 25.0 2

    set ioprcvr [$actorReceiver getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set rel [$toplevel connect $ioprcvr $iopsend "rel"]

    $mgr run

    set time0 [$actorReceiver getAfterTime 0]
    set time1 [$actorReceiver getAfterTime 1]

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

    set actorReceiver [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorReceiver" 3]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 6]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThrough $toplevel "actorThru"]

    set tok1 [java::new ptolemy.data.Token]

    $actorSend setToken $globalNullTok 5.0 0 
    $actorSend setToken $tok1 7.0 1
    $actorSend setToken $globalNullTok 8.0 2 
    $actorSend setToken $globalNullTok 8.0 3 
    $actorSend setToken $globalNullTok 9.0 4 
    $actorSend setToken $tok1 9.5 5

    set rcvrInPort [$actorReceiver getPort "input"]
    set sendOutPort [$actorSend getPort "output"]
    set thruInPort [$actorThru getPort "input"]
    set thruOutPort [$actorThru getPort "output"]

    $toplevel connect $sendOutPort $thruInPort "rel1"
    $toplevel connect $thruOutPort $rcvrInPort "rel2"

    $mgr run

    set time0 [$actorReceiver getAfterTime 0]
    set time1 [$actorReceiver getAfterTime 1]
    set time2 [$actorReceiver getAfterTime 2]

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

    set actorReceiver [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorReceiver" 5]
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

    set rcvrInPort [$actorReceiver getPort "input"]
    set sendOutPort1 [$actorSend1 getPort "output"]
    set sendOutPort2 [$actorSend2 getPort "output"]
    set thruInPort [$actorThru getPort "input"]
    set thruOutPort [$actorThru getPort "output"]

    $toplevel connect $sendOutPort1 $thruInPort
    $toplevel connect $sendOutPort2 $thruInPort
    $toplevel connect $thruOutPort $rcvrInPort

    $mgr run

    set time0 [$actorReceiver getAfterTime 0]
    set time1 [$actorReceiver getAfterTime 1]
    set time2 [$actorReceiver getAfterTime 2]
    set time3 [$actorReceiver getAfterTime 3]
    set time4 [$actorReceiver getAfterTime 4]

    list $time0 $time1 $time2 $time3 $time4 
} {4.0 5.0 6.0 7.0 8.0}    

######################################################################
####
#
test DDEReceiver-2.5 {Check hasToken() cache.} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set actor [java::new ptolemy.actor.TypedCompositeActor $toplevel "actor"]
    set port [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set rcvr [java::new ptolemy.domains.dde.kernel.DDEReceiver $port]

    $toplevel setName "toplevel"

    catch {$rcvr get} msg

    list $msg
} {{ptolemy.actor.NoTokenException: Attempt to get token that does not have have the earliest time stamp.
  in .toplevel.actor.port}}
