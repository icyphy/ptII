# Tests for the PrioritizedTimedQueue class
#
# @Author: John S. Davis II
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
test PrioritizedTimedQueue-2.1 {Check IOPort container in new receiver} {
    set iop [java::new ptolemy.actor.IOPort]
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    list [expr { $iop == [$tqr getContainer] } ]
} {1}

######################################################################
####
#
test PrioritizedTimedQueue-3.1 {Check hasToken(), hasRoom(), rcvrTime
and lastTime for empty queue} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
    set hasToken [$tqr hasToken]
    set hasRoom [$tqr hasRoom]
    set rcvrTime [$tqr getRcvrTime]
    set lastTime [$tqr getLastTime]
    list $hasToken $hasRoom $rcvrTime $lastTime
} {0 1 0.0 0.0}

######################################################################
####
#
test PrioritizedTimedQueue-3.2 {Check hasToken() after putting token} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 5.0
    set hasToken [$tqr hasToken]
    set rcvrTime1 [$tqr getRcvrTime]
    set lastTime1 [$tqr getLastTime]
    $tqr put $t1 15.0
    set rcvrTime2 [$tqr getRcvrTime]
    set lastTime2 [$tqr getLastTime]
    list $hasToken $rcvrTime1 $lastTime1 $rcvrTime2 $lastTime2
} {1 5.0 5.0 5.0 15.0}

######################################################################
####
#
test PrioritizedTimedQueue-4.1 {Check hasRoom() for full queue} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr setCapacity 2
    $tqr put $t1 5.0
    $tqr put $t2 5.0
    list [expr { 0 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
# Continued from above
test PrioritizedTimedQueue-4.2 {Check exception message for full queue} {
    set t3 [java::new ptolemy.data.Token]
    set cap [$tqr getCapacity]
    catch {$tqr put $t3 10.0} msg
    list $cap $msg
} {2 {ptolemy.actor.NoRoomException: : Queue is at capacity. Cannot insert token.}}

######################################################################
####
# Continued from above
test PrioritizedTimedQueue-4.3 {Check hasRoom() for infinite capacity queue} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    $tqr setContainer $iop
    set t4 [java::new ptolemy.data.Token]
    catch {$tqr put $t4 3.7} msg
    list $msg
} {{java.lang.IllegalArgumentException:  - Attempt to set current time to the past; time = 3.7. The _lastTime was 5.0}}

######################################################################
####
#
test PrioritizedTimedQueue-5.1 {Check get(), put(), _rcvrTime and _lastTime} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]

    set t0 [java::new ptolemy.data.Token]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]

    $tqr put $t0 5.0
    $tqr put $t1 7.0

    set rcvrTime0 [$tqr getRcvrTime]
    set lastTime0 [$tqr getLastTime]

    set outToken0 [$tqr get]
    set rslt0 [expr { $outToken0 == $t0 } ]

    $tqr put $t2 15.0

    set rcvrTime1 [$tqr getRcvrTime]
    set lastTime1 [$tqr getLastTime]

    set outToken1 [$tqr get]
    set rslt1 [expr { $outToken1 == $t1 } ]

    set rcvrTime3 [$tqr getRcvrTime]

    $tqr get

    set empty [expr { [$tqr hasToken] == 0 } ]

    set rcvrTime4 [$tqr getRcvrTime]
    set rcvrTime5 [$tqr getRcvrTime]

    list $rcvrTime0 $lastTime0 $rslt0 $rcvrTime1 $lastTime1 $rslt1 $rcvrTime3 $rcvrTime4 $rcvrTime5 $empty
} {5.0 7.0 1 7.0 15.0 1 15.0 15.0 15.0 1}

######################################################################
####
# Continued from above
test PrioritizedTimedQueue-6.2 {Check for exception with get() given empty \
	queue} {
    catch {$tqr get} msg 
    list $msg
} {{java.util.NoSuchElementException: The FIFOQueue is empty!}}

######################################################################
####
#
test PrioritizedTimedQueue-7.2 {Attempt to put negative time stamps} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    set t1 [java::new ptolemy.data.Token]

    catch {$tqr put $t1 -1.5} msg
    list $msg
} {{java.lang.IllegalArgumentException:  - Attempt to set current time to the past; time = -1.5. The _lastTime was 0.0}}
