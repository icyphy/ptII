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
set globalIgnoreTime [java::field $globalIgnoreTimeRcvr IGNORE]

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
test DDEReceiver-2.3 {Send a NullTokens through FlowThru.} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGetNToken $toplevel "actorRcvr" 2]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPutToken $toplevel "actorSend" 3]
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThru $toplevel "actorThru"]

    set tok1 [java::new ptolemy.data.Token]
    set nullTok [java::new ptolemy.domains.dde.kernel.NullToken]

    $actorSend setToken $nullTok 5.0 0 
    $actorSend setToken $tok1 7.0 1
    $actorSend setToken $tok1 9.5 2

    set rcvrInPort [$actorRcvr getPort "input"]
    set sendOutPort [$actorSend getPort "output"]
    set thruInPort [$actorThru getPort "input"]
    set thruOutPort [$actorThru getPort "output"]

    $toplevel connect $sendOutPort $thruInPort "rel1"
    $toplevel connect $thruOutPort $rcvrInPort "rel2"

    $mgr run

    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]

    list $time0 $time1

} {7.0 9.5}















