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
set globalEndTimeRcvr [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver]
set globalEndTime [java::field $globalEndTimeRcvr INACTIVE]


######################################################################
####
#
test DDEDirector-2.1 {Check compositionality} {
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

