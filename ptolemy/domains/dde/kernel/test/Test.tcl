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
set globalNullTok [java::new ptolemy.domains.dde.kernel.NullToken]

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
    set actorThru [java::new ptolemy.domains.dde.kernel.test.FlowThru $toplevel "actorThru"]

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
    
    $toplevel connect $sendOutPort2 $thruInPort
    $toplevel connect $thruOutPort $rcvrInPort
    $toplevel connect $sendOutPort1 $thruInPort

    $mgr run
    
    set time0 [$actorRcvr getAfterTime 0]
    set time1 [$actorRcvr getAfterTime 1]
    set time2 [$actorRcvr getAfterTime 2]
    set time3 [$actorRcvr getAfterTime 3]
    set time4 [$actorRcvr getAfterTime 4]
    
    list $time0 $time1 $time2 $time3 $time4 
} {4.0 5.0 6.0 7.0 8.0}    















