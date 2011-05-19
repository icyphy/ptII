# Tests for the ReceiverComparator class
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
set globalIgnoreTime -1

######################################################################
####
#
test ReceiverComparator-2.1 {compareTo() on times, same priorities} {

    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set actor [java::new ptolemy.actor.TypedAtomicActor $topLevel "actor"] 
    set iop [java::new ptolemy.actor.TypedIOPort $actor "IOPort"]
    set tok [java::new ptolemy.data.Token]
    set keeper [java::new ptolemy.domains.dde.kernel.TimeKeeper $actor]

    set cmp [java::new ptolemy.domains.dde.kernel.ReceiverComparator $keeper]
    set time [java::new {ptolemy.actor.util.Time ptolemy.actor.Director} $dir]
    set time1 [$time {add double} 0.5]
    set time2 [$time {add double} 2.5]
    set time3 [$time {add double} $globalIgnoreTime]
    set time4 [$time {add double} $globalEndTime]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr1 put $tok $time

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr2 put $tok $time1

    set rcvr3 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr3 put $tok $time2

    set rcvr4 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr4 put $tok $time3

    set rcvr5 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr5 put $tok $time4

    set rcvr6 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr6 put $tok $time1

    set testA [$cmp compare $rcvr1 $rcvr2]
    set testB [$cmp compare $rcvr4 $rcvr1]
    set testC [$cmp compare $rcvr4 $rcvr5]
    set testD [$cmp compare $rcvr4 $rcvr4]
    set testE [$cmp compare $rcvr5 $rcvr5]
    set testF [$cmp compare $rcvr3 $rcvr5]
    set testG [$cmp compare $rcvr2 $rcvr6]

    list $testA $testB $testC $testD $testE $testF $testG

} {-1 1 -1 0 0 -1 0}

######################################################################
####
# Continued from above...
test ReceiverComparator-2.2 {compareTo() on times and priorities} {

    set cmp [java::new ptolemy.domains.dde.kernel.ReceiverComparator $keeper]

    set rcvr1 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr1 put $tok $time

    set rcvr2 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 2]
    $rcvr2 put $tok $time

    set rcvr3 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 3]
    $rcvr3 put $tok $time3

    set rcvr4 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 4]
    $rcvr4 put $tok $time3

    set rcvr5 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 5]
    $rcvr5 put $tok $time4

    set rcvr6 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 6]
    $rcvr6 put $tok $time4

    set rcvr7 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 1]
    $rcvr7 put $tok $time

    set rcvr8 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 3]
    $rcvr8 put $tok $time3

    set rcvr9 [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop 5]
    $rcvr9 put $tok $time4

    set testA [$cmp compare $rcvr1 $rcvr2]
    set testB [$cmp compare $rcvr3 $rcvr4]
    set testC [$cmp compare $rcvr5 $rcvr6]
    set testD [$cmp compare $rcvr1 $rcvr3]
    set testE [$cmp compare $rcvr1 $rcvr6]
    set testF [$cmp compare $rcvr3 $rcvr6]
    set testG [$cmp compare $rcvr1 $rcvr7]
    set testH [$cmp compare $rcvr3 $rcvr8]
    set testI [$cmp compare $rcvr5 $rcvr9]

    list $testA $testB $testC $testD $testE $testF $testG $testH $testI

} {1 1 1 -1 -1 -1 0 0 0}
