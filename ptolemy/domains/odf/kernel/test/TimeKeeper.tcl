# Tests for the TimeKeeper class
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
test TimeKeeper-2.1 {hasMinRcvrTime - No simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]

    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 0.0 1
    $keeper updateRcvrList $rcvr2 0.5 2
    $keeper updateRcvrList $rcvr3 2.5 3

    list [$keeper getCurrentTime] [$keeper hasMinRcvrTime]

} {0.0 1}

######################################################################
####
#
test TimeKeeper-2.2 {hasMinRcvrTime - Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    
    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 5.0 1
    $keeper updateRcvrList $rcvr2 5.0 2
    $keeper updateRcvrList $rcvr3 18.0 3

    list [$keeper getCurrentTime] [$keeper hasMinRcvrTime]

} {0.0 0}

######################################################################
####
#
test TimeKeeper-2.3 {hasMinRcvrTime - Negative Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]

    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 -1.0 2
    set time1 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr2 5.0 1
    set time2 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr3 18.0 3
    set time3 [$keeper getNextTime]

    list [$keeper hasMinRcvrTime] $time1 $time2 $time3

} {1 -1.0 5.0 5.0}

######################################################################
####
#
test TimeKeeper-3.1 {hasMinRcvrTime, getNextTime - With Negative Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr4 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr5 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr6 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]

    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 -1.0 2
    set time1 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr2 5.0 1
    set time2 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr3 18.0 3
    set time3 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr4 18.0 4
    set time4 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr5 18.0 5
    set time5 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr6 28.0 6
    set time6 [$keeper getNextTime]

    list [$keeper hasMinRcvrTime] $time1 $time2 $time3 $time4 $time5 $time6

} {1 -1.0 5.0 5.0 5.0 5.0 5.0}

######################################################################
####
#
test TimeKeeper-4.1 {getNextTime()} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set port [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set rcvr1 [java::new ptolemy.domains.odf.kernel.ODFReceiver $port]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.ODFReceiver $port]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.ODFReceiver $port]

    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 15.0 2
    $keeper updateRcvrList $rcvr2 5.0 1
    $keeper updateRcvrList $rcvr3 6.0 3
    set newrcvr [$keeper getFirstRcvr]

    list [$keeper getNextTime] [expr {$rcvr2 == $newrcvr} ]

} {5.0 1}

######################################################################
####
#
test TimeKeeper-5.1 {Call Methods On Uninitialized TimeKeeper} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    set val 1
    if { ![java::isnull [$keeper getHighestPriorityReceiver]] } {
	set val 0
    }
    if { [$keeper getCurrentTime] != 0.0 } {
	set val 0
    }
    if { [$keeper getNextTime] != 0.0 } {
	set val 0
    }
    if { ![java::isnull [$keeper getFirstRcvr]] } {
	set val 0
    }
    if { [$keeper hasMinRcvrTime] == 0 } {
	set val 0
    }

    list $val;

} {1}

######################################################################
####
#
test TimeKeeper-6.1 {getHighestPriorityTriple - Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]
    set rcvr3 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver]

    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 5.0 1
    $keeper updateRcvrList $rcvr2 5.0 2
    $keeper updateRcvrList $rcvr3 6.0 3
    set newrcvr [$keeper getHighestPriorityReceiver]

    list [$keeper getCurrentTime] [expr {$rcvr2 == $newrcvr} ]

} {0.0 1}

######################################################################
####
#
test TimeKeeper-6.2 {getHighestPriorityTriple - No Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set port [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set rcvr1 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $port]
    set rcvr2 [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $port]

    set keeper [java::new ptolemy.domains.odf.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 15.0 1
    $keeper updateRcvrList $rcvr2 5.0 2
    set newrcvr [$keeper getHighestPriorityReceiver]

    list [$keeper getCurrentTime] [expr {$rcvr2 == $newrcvr} ]

} {0.0 1}

