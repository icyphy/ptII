# Tests for the PrioritizedTimedQueue class
#
# @Author: John S. Davis II
#
# @Version: $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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
#
test PrioritizedTimedQueue-2.1 {Check IOPort container in new receiver} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $toplevel "director"]

    set actor [java::new ptolemy.actor.TypedAtomicActor $toplevel "actor"]
    set iop [java::new ptolemy.actor.TypedIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    set container [java::cast ptolemy.actor.TypedIOPort [$tqr getContainer]]
    list [expr { $iop == $container } ]
    #this test fails for a very strange reason... 
    #and the reason is that a java object may have several 
    #different jacl objects associated with it, depending how the 
    #java object is referred. 
    #for example, if the $iop is casted as an NamedObj, the test fails.
} {1}

######################################################################
####
#
test PrioritizedTimedQueue-3.1 {Check hasToken(), hasRoom(), rcvrTime
and lastTime for empty queue} {
    set hasToken [$tqr hasToken]
    set hasRoom [$tqr hasRoom]
    set rcvrTime [[$tqr getReceiverTime] getDoubleValue]
    set lastTime [[$tqr getLastTime] getDoubleValue]
    list $hasToken $hasRoom $rcvrTime $lastTime
} {0 1 0.0 0.0}

######################################################################
####
#
test PrioritizedTimedQueue-3.2 {Check hasToken() after putting token} {
    set time [java::new {ptolemy.actor.util.Time ptolemy.actor.Director} $dir]
    set time1 [$time {add double} 5.0]
    set time2 [$time {add double} 15.0]
    set time3 [$time {add double} 10.0]
    set time4 [$time {add double} 3.7]
    set time5 [$time {add double} 7.0]
    set time6 [$time {add double} -1.5]

    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 $time1
    set hasToken [$tqr hasToken]
    set rcvrTime1 [[$tqr getReceiverTime] getDoubleValue]
    set lastTime1 [[$tqr getLastTime] getDoubleValue]
    $tqr put $t1 $time2
    set rcvrTime2 [[$tqr getReceiverTime] getDoubleValue]
    set lastTime2 [[$tqr getLastTime] getDoubleValue]
    list $hasToken $rcvrTime1 $lastTime1 $rcvrTime2 $lastTime2
} {1 5.0 5.0 5.0 15.0}

######################################################################
####
#
test PrioritizedTimedQueue-4.1 {Check hasRoom() for full queue} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr setCapacity 2
    $tqr put $t1 $time1
    $tqr put $t2 $time1
    catch {$tqr hasRoom 0} msg
    list $msg [$tqr hasRoom] [$tqr hasRoom 1]
} {{java.lang.IllegalArgumentException: hasRoom() requires a positive argument.} 0 0}


######################################################################
####
# Continued from above
test PrioritizedTimedQueue-4.2 {Check exception message for full queue} {
    set t3 [java::new ptolemy.data.Token]
    set cap [$tqr getCapacity]
    catch {$tqr put $t3 $time3} msg
    list $cap $msg
} {2 {ptolemy.actor.NoRoomException: Queue is at capacity. Cannot insert token.
  in .<Unnamed Object>.actor.port}}
######################################################################
####
# Continued from above
test PrioritizedTimedQueue-4.3 {Check hasRoom() for infinite capacity queue} {
    set t4 [java::new ptolemy.data.Token]
    catch {$tqr put $t4 $time4} msg
    list [string range $msg 0 82]
} {{java.lang.IllegalArgumentException: actor - Attempt to set current time to the past}}

######################################################################
####
#
test PrioritizedTimedQueue-5.1 {Check get(), put(), _rcvrTime and _lastTime} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]

    set t0 [java::new ptolemy.data.Token]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]

    $tqr put $t0 $time1
    $tqr put $t1 $time5

    set rcvrTime0 [[$tqr getReceiverTime] getDoubleValue]
    set lastTime0 [[$tqr getLastTime] getDoubleValue]

    set outToken0 [$tqr get]
    set rslt0 [expr { $outToken0 == $t0 } ]

    $tqr put $t2 $time2

    set rcvrTime1 [[$tqr getReceiverTime] getDoubleValue]
    set lastTime1 [[$tqr getLastTime] getDoubleValue]

    set outToken1 [$tqr get]
    set rslt1 [expr { $outToken1 == $t1 } ]

    set rcvrTime3 [[$tqr getReceiverTime] getDoubleValue]

    $tqr get

    catch {$tqr hasToken 0} msg
    set rcvrTime4 [[$tqr getReceiverTime] getDoubleValue]
    set rcvrTime5 [[$tqr getReceiverTime] getDoubleValue]

    list $rcvrTime0 $lastTime0 $rslt0 $rcvrTime1 $lastTime1 $rslt1 $rcvrTime3 $rcvrTime4 $rcvrTime5 [$tqr hasToken] [$tqr hasToken 1] $msg
} {5.0 7.0 1 7.0 15.0 1 15.0 15.0 15.0 0 0 {java.lang.IllegalArgumentException: hasToken() requires a positive argument.}}

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
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    set t1 [java::new ptolemy.data.Token]

    catch {$tqr put $t1 $time6} msg
    list [string range $msg 0 82]
} {{java.lang.IllegalArgumentException: actor - Attempt to set current time to the past}}


######################################################################
####
#
test PrioritizedTimedQueue-7.3 {Attempt to put with no time} {
    set tqr [java::new ptolemy.domains.dde.kernel.PrioritizedTimedQueue $iop]
    set t1 [java::new ptolemy.data.Token]

    catch {$tqr put $t1 } msg
    list $msg
} {{ptolemy.actor.NoRoomException: put(Token) is not used in the DDE domain.}}
