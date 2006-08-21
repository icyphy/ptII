# Tests for the CalendarQueue class
#
# @Author: Lukito Muliadi
#
# @Version: $Id$
#
# @Copyright (c) 1998-2006 The Regents of the University of California.
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
set comparator [java::new ptolemy.actor.util.test.DoubleCQComparator]

######################################################################
####
#
test CalendarQueue-2.1 {Construct an empty queue and check defaults} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    list [$queue size] [$queue isEmpty]
} {0 1}

######################################################################
####
#
test CalendarQueue-2.2 {Construct an empty queue and attempt a take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    catch {[$queue take]} msg1
    list $msg1
} {{ptolemy.kernel.util.InvalidStateException: Queue is empty.}}

######################################################################
######################################################################
# The following objects are used throughout the rest of the tests.
set p1 [java::new {Double double} 0.0 ]
set p2 [java::new {Double double} 0.1 ]
set p3 [java::new {Double double} 0.2 ]
set p4 [java::new {Double double} 3.0 ]
set p5 [java::new {Double double} 4.0 ]
set p6 [java::new {Double double} 7.6 ]
set p7 [java::new {Double double} 8.9 ]
set p8 [java::new {Double double} 50.0 ]
set p9 [java::new {Double double} 999.1 ]
set p10 [java::new {Double double} 999.3 ]
set p11 [java::new {Double double} 999.8 ]
set p12 [java::new {Double double} 1001.0 ]
set p13 [java::new {Double double} 1002.1 ]
set p14 [java::new {Double double} 1002.2 ]
set p15 [java::new {Double double} 1002.3 ]
set p16 [java::new {Double double} 1002.4 ]
set p1again [java::new {Double double} 0.0 ]

# DoubleCQComparator.getVirtualBinNumber returns Long.MAX_Value
# if the input == 666.0
set p666 [java::new {Double double} 666.0 ]


test CalendarQueue-2.0 {log} {
    catch {java::call ptolemy.actor.util.CalendarQueue log -1} errMsg1
    catch {java::call ptolemy.actor.util.CalendarQueue log 0} errMsg0
    set r1 [java::call ptolemy.actor.util.CalendarQueue log 1]
    set r2 [java::call ptolemy.actor.util.CalendarQueue log 2]
    set r3 [java::call ptolemy.actor.util.CalendarQueue log 10]
    list $errMsg1 $errMsg0 $r1 $r2 $r3
} {{java.lang.ArithmeticException: CalendarQueue: Cannot take the log of a non-positive number: -1} {java.lang.ArithmeticException: CalendarQueue: Cannot take the log of a non-positive number: 0} 0 1 4}

######################################################################
####
#
test CalendarQueue-3.0 {Put 4 entries in the queue and do a single take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p4
    $queue put $p2
    $queue put $p3
    $queue put $p1
    list [$queue get] \
	    [$queue take] \
            [$queue isEmpty] \
            [$queue size]
} {0.0 0.0 0 3}

######################################################################
####
#
test CalendarQueue-3.1 {Put 4 entries in the queue and take one by one} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p4
    $queue put $p2
    $queue put $p3
    $queue put $p1
    list [$queue take] \
	    [$queue take] \
	    [$queue take] \
	    [$queue take] \
	    [$queue isEmpty] \
            [$queue size]
} {0.0 0.1 0.2 3.0 1 0}

######################################################################
####
#
test CalendarQueue-3.3 {Test the resize method } {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p9
    $queue put $p5
    $queue put $p7
    $queue put $p2
    $queue put $p1
    # queue size should get doubled here, becomes 4
    $queue put $p10
    $queue put $p8
    $queue put $p6
    $queue put $p4
    # queue size should get doubled here, becomes 8
    $queue put $p1again
    $queue put $p3
    set result {}
    while {![$queue isEmpty]} {
        lappend result [$queue take]
    }
    list $result
} {{0.0 0.0 0.1 0.2 3.0 4.0 7.6 8.9 50.0 999.1 999.3}}

######################################################################
####
#
test CalendarQueue-3.5 {Tests interleaved put and take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]

    $queue put $p1
    $queue put $p1again
    $queue put $p5

    set mylist [list [$queue take] [$queue take]]

    $queue put $p2
    $queue put $p6
    $queue put $p4
    $queue put $p3
    $queue put $p9

    lappend mylist [$queue take] [$queue take] [$queue take] \
            [$queue take] [$queue take]

    $queue put $p7
    $queue put $p10
    $queue put $p8

    lappend mylist [$queue take] [$queue take] [$queue take]
} {0.0 0.0 0.1 0.2 3.0 4.0 7.6 8.9 50.0 999.1}

######################################################################
####
#
test CalendarQueue-3.6 {Test toArray} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p9
    $queue put $p5
    $queue put $p7
    $queue put $p2
    $queue put $p1
    arrayToStrings [$queue toArray]
} {0.0 0.1 4.0 8.9 999.1}

######################################################################
####
#
test CalendarQueue-3.7 {Test toArray} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p666
    $queue put $p666
    $queue put $p7
    arrayToStrings [$queue toArray]
} {8.9 666.0 666.0}

######################################################################
####
#
test CalendarQueue-5.1 {Test remove and includes methods} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    set r1 [$queue remove $p1]
    $queue put $p1
    $queue put $p2
    set mylist [list [$queue get] \
	    [$queue remove $p1] \
	    [$queue get] \
	    [$queue take]]

    $queue put $p2
    $queue put $p5
    $queue put $p3
    lappend mylist [$queue remove $p2] \
	    [$queue remove $p2] \
	    [$queue remove $p3]
    $queue put $p2
    lappend mylist [$queue includes $p2] \
            [$queue take] \
            [$queue take]
    catch {[$queue take]} msg1
    catch {[$queue get]} msg2
    list $r1 $mylist $msg1 $msg2
} {0 {0.0 1 0.1 0.1 1 0 1 1 0.1 4.0} {ptolemy.kernel.util.InvalidStateException: Queue is empty.} {ptolemy.kernel.util.InvalidStateException: Queue is empty.}}

######################################################################
####
#
test CalendarQueue-5.2 {Comprehensive tests of everything} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    catch {[$queue get]} msg1
    catch {[$queue take]} msg2
    catch {[$queue get]} msg3
    list $msg1 $msg2 $msg3
} {{ptolemy.kernel.util.InvalidStateException: Queue is empty.} {ptolemy.kernel.util.InvalidStateException: Queue is empty.} {ptolemy.kernel.util.InvalidStateException: Queue is empty.}}

test CalendarQueue-5.3 {Comprehensive tests of everything} {

    $queue put $p1
    $queue get
} {0.0}

test CalendarQueue-5.4 {Comprehensive tests of everything} {

    # Note that due to implementation, this will come before n1
    $queue put $p1again
    set mylist [list [$queue get]]

    $queue put $p5
    lappend mylist [$queue get] \
	    [$queue remove $p5]
} {0.0 0.0 1}

test CalendarQueue-5.5 {Comprehensive tests of everything} {
    set mylist [list [$queue get] [$queue take] [$queue take] [$queue size]]
} {0.0 0.0 0.0 0}

######################################################################
####
#
test CalendarQueue-6.1 {Test identical entry} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p1
    $queue put $p1

    set mylist [list [$queue get] \
	    [$queue remove $p1] \
	    [$queue get] \
            [$queue take]]

    $queue put $p1
    $queue put $p1
    $queue put $p1
    lappend mylist [$queue remove $p2] \
	    [$queue remove $p2] \
	    [$queue remove $p3]
    $queue put $p1
    lappend mylist [$queue includes $p2] [$queue take] [$queue take] \
        [$queue take] [$queue take]
    catch {[$queue get]} msg1
    lappend mylist $msg1

} {0.0 1 0.0 0.0 0 0 0 0 0.0 0.0 0.0 0.0 {ptolemy.kernel.util.InvalidStateException: Queue is empty.}}

######################################################################
####
#
test CalendarQueue-7.1 {Test the clear method} {

    $queue put $p1
    $queue take
    $queue put $p2
    $queue put $p3
    $queue put $p1
    $queue put $p1
    $queue take

    $queue clear
    $queue put $p1
    $queue put $p1

    set mylist [list [$queue take] \
            [$queue remove $p1] \
            [$queue size]]

    $queue put $p1
    $queue put $p1
    $queue put $p1
    lappend mylist [$queue remove $p2] \
	    [$queue remove $p2] \
	    [$queue remove $p3]
    $queue put $p1
    lappend mylist [$queue includes $p2] [$queue take] [$queue take] \
        [$queue take] [$queue take]
    catch {$queue get} msg1
    lappend mylist $msg1

} {0.0 1 0 0 0 0 0 0.0 0.0 0.0 0.0 {ptolemy.kernel.util.InvalidStateException: Queue is empty.}}

######################################################################
####
#
test CalendarQueue-8.0 {check out debugListener} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $queue addDebugListener $listener
    $queue put $p4

    # Call addDebugListener again with the same listener so as
    # to increase basic block coverage
    $queue addDebugListener $listener

    # Try adding another listener so as to increase coverage	
    set listener2 [java::new ptolemy.kernel.util.RecorderListener]
    $queue addDebugListener $listener2

    $queue put $p2
    # Remove and then readd the listener
    $queue removeDebugListener $listener
    # Call removeDebugListener twice so as to increase coverage
    $queue removeDebugListener $listener
    # Remove the other listener so as to increase coverage
    $queue removeDebugListener $listener2

    $queue put $p3
    $queue addDebugListener $listener

    $queue put $p1
    # Listener output should not include the value of p3 (0.2) being added
    list [arrayToStrings [$queue toArray]] \
	 [$listener getMessages]

} {{0.0 0.1 0.2 3.0} {+++ putting in queue: 3.0
+++ putting in queue: 0.1
+++ putting in queue: 0.0
}}


######################################################################
####
#
test CalendarQueue-8.1 {Test the resize method with debugging } {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]

    # Add a listener so that we can get some of the debug basic blocks.
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $queue addDebugListener $listener

    for {set a 0} {$a < 2} {incr a} {
	for {set b 0} {$b < 2} {incr b} {
		$queue put $p9
		$queue put $p5
		$queue put $p7
		$queue put $p2
		$queue put $p1
		# queue size should get doubled here, becomes 4
		$queue put $p10
		$queue put $p8
		$queue put $p6
		$queue put $p4
		# queue size should get doubled here, becomes 8
		$queue put $p1again
		$queue put $p3
	}
    }

    set size [$queue size]

    set result {}
    while {![$queue isEmpty]} {
        lappend result [$queue take]
    }
    list $size $result \
	    [$listener getMessages]
} {44 {0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.1 0.1 0.1 0.1 0.2 0.2 0.2 0.2 3.0 3.0 3.0 3.0 4.0 4.0 4.0 4.0 7.6 7.6 7.6 7.6 8.9 8.9 8.9 8.9 50.0 50.0 50.0 50.0 999.1 999.1 999.1 999.1 999.3 999.3 999.3 999.3} {+++ putting in queue: 999.1
+++ putting in queue: 4.0
+++ putting in queue: 8.9
+++ putting in queue: 0.1
+++ putting in queue: 0.0
+++ putting in queue: 999.3
+++ putting in queue: 50.0
+++ putting in queue: 7.6
+++ putting in queue: 3.0
+++ putting in queue: 0.0
+++ putting in queue: 0.2
+++ putting in queue: 999.1
+++ putting in queue: 4.0
+++ putting in queue: 8.9
+++ putting in queue: 0.1
+++ putting in queue: 0.0
+++ putting in queue: 999.3
+++ putting in queue: 50.0
+++ putting in queue: 7.6
+++ putting in queue: 3.0
+++ putting in queue: 0.0
+++ putting in queue: 0.2
+++ putting in queue: 999.1
+++ putting in queue: 4.0
+++ putting in queue: 8.9
+++ putting in queue: 0.1
+++ putting in queue: 0.0
+++ putting in queue: 999.3
+++ putting in queue: 50.0
+++ putting in queue: 7.6
+++ putting in queue: 3.0
+++ putting in queue: 0.0
+++ putting in queue: 0.2
+++ putting in queue: 999.1
+++ putting in queue: 4.0
+++ putting in queue: 8.9
+++ putting in queue: 0.1
>>>>>> increasing number of buckets to: 4
+++ putting in queue: 0.0
+++ putting in queue: 999.3
+++ putting in queue: 50.0
+++ putting in queue: 7.6
+++ putting in queue: 3.0
+++ putting in queue: 0.0
+++ putting in queue: 0.2
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.0
--- taking from queue: 0.1
--- taking from queue: 0.1
--- taking from queue: 0.1
--- taking from queue: 0.1
--- taking from queue: 0.2
--- taking from queue: 0.2
--- taking from queue: 0.2
--- taking from queue: 0.2
--- taking from queue: 3.0
--- taking from queue: 3.0
--- taking from queue: 3.0
--- taking from queue: 3.0
--- taking from queue: 4.0
--- taking from queue: 4.0
--- taking from queue: 4.0
--- taking from queue: 4.0
--- taking from queue: 7.6
--- taking from queue: 7.6
--- taking from queue: 7.6
--- taking from queue: 7.6
--- taking from queue: 8.9
--- taking from queue: 8.9
--- taking from queue: 8.9
--- taking from queue: 8.9
--- taking from queue: 50.0
--- taking from queue: 50.0
--- taking from queue: 50.0
--- taking from queue: 50.0
--- taking from queue: 999.1
--- taking from queue: 999.1
--- taking from queue: 999.1
--- taking from queue: 999.1
--- taking from queue: 999.3
--- taking from queue: 999.3
--- taking from queue: 999.3
--- taking from queue: 999.3
}}
