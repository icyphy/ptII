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
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    $rcvr1 put $tok 0.0

    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    $rcvr2 put $tok 0.5

    set rcvr3 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 3]
    $rcvr3 put $tok 2.5

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 
    $keeper updateRcvrList $rcvr2
    $keeper updateRcvrList $rcvr3

    list [$keeper getCurrentTime] [$keeper hasMinRcvrTime]

} {0.0 1}

######################################################################
####
#
test TimeKeeper-2.2 {hasMinRcvrTime - Simultaneous Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    $rcvr1 put $tok 0.0

    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    $rcvr2 put $tok 0.0

    set rcvr3 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 3]
    $rcvr3 put $tok 2.5
    
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2
    $keeper updateRcvrList $rcvr3

    list [$keeper getCurrentTime] [$keeper hasMinRcvrTime]

} {0.0 0}

######################################################################
####
#
test TimeKeeper-2.3 {hasMinRcvrTime - Negative Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    $rcvr1 put $tok -1.0

    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    $rcvr2 put $tok 5.0

    set rcvr3 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 3]
    $rcvr3 put $tok 18.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    set time1 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr2
    set time2 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr3
    set time3 [$keeper getNextTime]

    list [$keeper hasMinRcvrTime] $time1 $time2 $time3

} {1 -1.0 5.0 5.0}

######################################################################
####
#
test TimeKeeper-3.1 {hasMinRcvrTime, getNextTime - With Negative Events} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    $rcvr1 put $tok -1.0
    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    $rcvr2 put $tok 5.0
    set rcvr3 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 3]
    $rcvr3 put $tok 18.0
    set rcvr4 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 4]
    $rcvr4 put $tok 18.0
    set rcvr5 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 5]
    $rcvr5 put $tok 18.0
    set rcvr6 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 6]
    $rcvr6 put $tok 28.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    set time1 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr2
    set time2 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr3
    set time3 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr4
    set time4 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr5
    set time5 [$keeper getNextTime]
    $keeper updateRcvrList $rcvr6
    set time6 [$keeper getNextTime]

    list [$keeper hasMinRcvrTime] $time1 $time2 $time3 $time4 $time5 $time6

} {1 -1.0 5.0 5.0 5.0 5.0 5.0}

######################################################################
####
#
test TimeKeeper-4.1 {getNextTime()} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop 2]
    $rcvr1 put $tok 15.0
    set rcvr2 [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop 1]
    $rcvr2 put $tok 5.0
    set rcvr3 [java::new ptolemy.domains.dde.kernel.DDEReceiver $iop 3]
    $rcvr3 put $tok 6.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2
    $keeper updateRcvrList $rcvr3
    set newrcvr [java::cast ptolemy.domains.dde.kernel.DDEReceiver [$keeper getFirstRcvr]]

    list [$keeper getNextTime] [expr {$rcvr2 == $newrcvr} ]

} {5.0 1}

######################################################################
####
#
test TimeKeeper-5.1 {Call Methods On Uninitialized TimeKeeper} {
    
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

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
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    $rcvr1 put $tok 5.0
    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    $rcvr2 put $tok 5.0
    set rcvr3 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 3]
    $rcvr3 put $tok 6.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1 
    $keeper updateRcvrList $rcvr2 
    $keeper updateRcvrList $rcvr3
    set newrcvr [$keeper getHighestPriorityReceiver]

    list [$keeper getCurrentTime] [expr {$rcvr2 == $newrcvr} ]

} {0.0 1}

######################################################################
####
#
test TimeKeeper-6.2 {getHighestPriorityTriple - No Simultaneous Events} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set tok [java::new ptolemy.data.Token]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    $rcvr1 put $tok 15.0
    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    $rcvr2 put $tok 5.0

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    $keeper updateRcvrList $rcvr1
    $keeper updateRcvrList $rcvr2
    set newrcvr [$keeper getHighestPriorityReceiver]

    list [$keeper getCurrentTime] [expr {$rcvr2 == $newrcvr} ]

} {0.0 1}

######################################################################
####
#
test TimeKeeper-6.3 {Check resortList} {
    set actor [java::new ptolemy.actor.TypedAtomicActor] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 1]
    set rcvr2 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 2]
    set rcvr3 [java::new ptolemy.domains.dde.kernel.TimedQueueReceiver $iop 3]

    set $tok [java::new ptolemy.data.Token]

    $rcvr1 put $tok 15.0
    $rcvr2 put $tok 5.0
    $rcvr3 put $tok 7.0

    $keeper updateRcvrList $rcvr1 
    $keeper updateRcvrList $rcvr2 
    $keeper updateRcvrList $rcvr3 

    set val 1
    if {[$keeper getFirstRcvr] != $rcvr2 } {
	set val 0
    }

    $rcvr2 get
    $rcvr2 put $tok 8.0

    $keeper resortRcvrList

    if { [$keeper getFirstRcvr] != $rcvr3 } {
	set val 0
    }

    list $val
} {1}

######################################################################
####
#
test TimeKeeper-7.1 {Check sendOutNullTokens} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $wspc "director"]
    set act1 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act1"] 
    set act2 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act2"] 

    $topLevel setDirector $dir

    set outPort [java::new ptolemy.actor.TypedIOPort $act1 "output" false true]
    set inPort [java::new ptolemy.actor.TypedIOPort $act2 "input" true false]
    set rel [$topLevel connect $outPort $inPort "rel"]

    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $act1]
    $inPort createReceivers

    set rcvrs [$inPort getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.TimedQueueReceiver [$rcvrs get {0 0}]]

    $rcvr setCapacity 1

    set hasRoom [$rcvr hasRoom]
    $keeper sendOutNullTokens
    set noRoom [$rcvr hasRoom]

    set val [$rcvr getRcvrTime]
    
    list $val $hasRoom $noRoom
} {0.0 1 0}
