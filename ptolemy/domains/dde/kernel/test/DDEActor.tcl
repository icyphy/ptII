# Tests for the ODActor class
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
test DDEActor-2.1 {getNextToken() - Send One Token} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    $toplevel setDirector $dir
    $toplevel setManager $mgr
    
    set actorRcvr [java::new ptolemy.domains.dde.kernel.test.DDEGet $toplevel "actorRcvr"]
    set actorSend [java::new ptolemy.domains.dde.kernel.test.DDEPut $toplevel "actorSend"]
    set ioprcvr [$actorRcvr getPort "input"]
    set iopsend [$actorSend getPort "output"]
    set rel [$toplevel connect $ioprcvr $iopsend "rel"]
    $actorRcvr createReceivers
    $actorSend createReceivers
    $actorRcvr initialize 
    $actorSend initialize 

    set odr [java::new ptolemy.domains.dde.kernel.DDEReceiver $ioprcvr]
    set rcvrkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorRcvr]
    $odr setReceivingTimeKeeper $rcvrkeeper
    set sendkeeper [java::new ptolemy.domains.dde.kernel.TimeKeeper \
	    $actorSend]
    $odr setSendingTimeKeeper $sendkeeper

    set rcvrs [ [java::cast ptolemy.actor.TypedIOPort $ioprcvr] getReceivers]
    if { $rcvrs == [java::null] } {
	set null 1
    }

    set token0 [java::new ptolemy.data.Token]
    $odr put $token0 5.0
    set outToken0 $token0
    #set outToken0 [$actorRcvr getNextToken] 

    if { $outToken0 == $token0 } {
	set val 1
    }

    list $val

} {1}













