# Tests for the CalendarQueue class
#
# @Author: Lukito Muliadi
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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
#test CalendarQueue-1.1 {Get class information} {
#    # If anything changes, we want to know about it so we can write tests.
#    set n [java::new ptolemy.actor.util.CalendarQueue]
#    list [getJavaInfo $n]
#} {{
#  class:         ptolemy.actor.util.CalendarQueue
#  fields:
#  methods:       {equals java.lang.Object} getClass getNextKey getP
#    reviousPriority hashCode {includes java.lang.Objec
#    t ptolemy.actor.util.Sortable} notify notifyAll {put java.lang.Ob
#    ject ptolemy.actor.util.Sortable} {remove java.lang.Object
#    ptolemy.actor.util.Sortable} size take toString wait {wait long}
#    {wait long int}
#
#  constructors:  ptolemy.actor.util.CalendarQueue
#
#  properties:    class nextPriority previousPriority
#
#  superclass:    java.lang.Object
#
#}   }
######################################################################
####
#
set comparator [java::new ptolemy.actor.util.DoubleCQComparator]

######################################################################
####
#
test CalendarQueue-2.1 {Construct an empty queue and check defaults} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    list [$queue size]
} {0}

######################################################################
####
#
test CalendarQueue-2.2 {Construct an empty queue and attempt a take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    catch {[$queue take]} msg1
    list $msg1
} {{java.lang.IllegalAccessException: Invoking take() on empty queue is not allowed.}}

######################################################################
######################################################################
# The following named objects are used throughout the rest of the tests.
set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
set n4 [java::new ptolemy.kernel.util.NamedObj "n4"]
set n5 [java::new ptolemy.kernel.util.NamedObj "n5"]
set n6 [java::new ptolemy.kernel.util.NamedObj "n6"]
set n7 [java::new ptolemy.kernel.util.NamedObj "n7"]
set n8 [java::new ptolemy.kernel.util.NamedObj "n8"]
set n9 [java::new ptolemy.kernel.util.NamedObj "n9"]
set n10 [java::new ptolemy.kernel.util.NamedObj "n10"]
set n11 [java::new ptolemy.kernel.util.NamedObj "n11"]
set n12 [java::new ptolemy.kernel.util.NamedObj "n12"]
set n13 [java::new ptolemy.kernel.util.NamedObj "n13"]
set n14 [java::new ptolemy.kernel.util.NamedObj "n14"]
set n15 [java::new ptolemy.kernel.util.NamedObj "n15"]
set n16 [java::new ptolemy.kernel.util.NamedObj "n16"]
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

######################################################################
####
#
test CalendarQueue-3.0 {Put 4 datas in the queue and do a single take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p4 $n1
    $queue put $p2 $n2
    $queue put $p3 $n3
    $queue put $p1 $n4
    list [$queue getNextKey] \
	   [[$queue take] getName] \
	    [$queue getPreviousKey]
} {0.0 n4 0.0}

######################################################################
####
#
test CalendarQueue-3.1 {Put 4 datas in the queue and take one by one} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p4 $n1
    $queue put $p2 $n2
    $queue put $p3 $n3
    $queue put $p1 $n4
    list [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {n4 0.0 n2 0.1 n3 0.2 n1 3.0}

######################################################################
####
#
test CalendarQueue-3.2 {Put 5 datas in the queue and take one by one} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p5 $n1
    $queue put $p3 $n2
    $queue put $p4 $n3
    $queue put $p2 $n4
    $queue put $p1 $n5
    list [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {n5 0.0 n4 0.1 n2 0.2 n3 3.0 n1 4.0}

######################################################################
####
#
test CalendarQueue-3.3 {Tests the resize method and the direct search } {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p9 $n1
    $queue put $p5 $n2
    $queue put $p7 $n3
    $queue put $p2 $n4
    $queue put $p1 $n5
    # queue size should get doubled here, becomes 4
    $queue put $p10 $n16
    $queue put $p8 $n7
    $queue put $p6 $n8
    $queue put $p4 $n9
    # queue size should get doubled here, becomes 8
    $queue put $p1again $n10
    # We want FIFO operation, i.e. p1 should be before p1again
    $queue put $p3 $n11
    # This sequence of take would half the queue size to 4
    list [[$queue take] getName] \
	    [$queue getPreviousKey]\
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {n5 0.0 n10 0.0 n4 0.1 n11 0.2 n9 3.0 n2 4.0 n8 7.6 n3 8.9 n7 50.0 n1 999.1}


######################################################################
####
#
test CalendarQueue-3.4 {Again tests the direct search and more complicated put, take sequences} {
    # We still have $n16 $p10
    $queue put $p4 $n2
    $queue put $p3 $n3
    $queue put $p2 $n4
    $queue put $p11 $n5
    $queue put $p1 $n6
    $queue put $p9 $n7
    $queue put $p8 $n8
    $queue put $p7 $n9
    # queue size should get doubled here, becomes 8
    # This sequence of take would half the queue size to 4
    list [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {n6 0.0 n4 0.1 n3 0.2 n2 3.0 n9 8.9 n8 50.0 n7 999.1 n16 999.3 n5 999.8}

######################################################################
####
#
test CalendarQueue-3.5 {Tests interleaved put and take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]

    $queue put $p1 $n5
    # queue size should get doubled here, becomes 4
    $queue put $p1again $n10
    # Want FIFO behaviour, i.e. p1 before p1again
    $queue put $p5 $n2

    # This sequence of take would half the queue size to 4
    set mylist [list [[$queue take] getName] \
	    [$queue getPreviousKey]\
	    [[$queue take] getName] \
	    [$queue getPreviousKey]]

    $queue put $p2 $n4
    $queue put $p6 $n8
    $queue put $p4 $n9
    # queue size should get doubled here, becomes 8
    $queue put $p3 $n11
    $queue put $p9 $n1

    lappend mylist [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]

    $queue put $p7 $n3
    $queue put $p10 $n16
    $queue put $p8 $n7

    lappend mylist [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {n5 0.0 n10 0.0 n4 0.1 n11 0.2 n9 3.0 n2 4.0 n8 7.6 n3 8.9 n7 50.0 n1 999.1}

######################################################################
####
#
test CalendarQueue-4.1 {Tests the getNextKey method} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    catch {[$queue getNextKey]} msg1
    set mylist $msg1

    $queue put $p1 $n5
    lappend mylist [$queue getNextKey]

    $queue put $p1again $n10
    # FIFO behaviour: p1 before p1again
    lappend mylist [$queue getNextKey]

    $queue put $p5 $n2
    lappend mylist [$queue getNextKey]

    # This sequence of take would half the queue size to 4
    lappend mylist [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]

    $queue put $p2 $n4
    $queue put $p6 $n8
    $queue put $p4 $n9
    # queue size should get doubled here, becomes 8
    $queue put $p3 $n11
    $queue put $p9 $n1

    lappend mylist [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]

    $queue put $p7 $n3
    $queue put $p10 $n16
    $queue put $p8 $n7

    lappend mylist [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {java.lang.IllegalAccessException: Invoking getNextKey() on empty queue is not allowed. 0.0 0.0 0.0 0.0 n5 0.0 0.0 n10 0.0 n4 0.1 n11 0.2 n9 3.0 n2 4.0 n8 7.6 8.9 n3 8.9 50.0 n7 50.0 999.1 n1 999.1}



######################################################################
####
#
test CalendarQueue-5.1 {Tests remove and includes method} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p1 $n5
    $queue put $p2 $n4
    set mylist [list [$queue getNextKey] \
	    [$queue remove $p1 $n5] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]]

    $queue put $p2 $n3
    $queue put $p5 $n7
    $queue put $p3 $n8
    lappend mylist [$queue remove $p2 $n3] \
	    [$queue remove $p2 $n3] \
	    [$queue remove $p3 $n7]
    $queue put $p2 $n3
    lappend mylist [$queue includes $p2 $n3] \
	    [[$queue take] getName] \
	    [[$queue take] getName] \
	    [[$queue take] getName]
    catch {[$queue take]} msg1
    catch {[$queue getNextKey]} msg2
    lappend mylist $msg1 $msg2




} {0.0 1 0.1 n4 0.1 1 0 0 1 n3 n8 n7 {java.lang.IllegalAccessException: Invoking take() on empty queue is not allowed.} {java.lang.IllegalAccessException: Invoking getNextKey() on empty queue is not allowed.}}

######################################################################
####
#
test CalendarQueue-5.2 {Comprehensive tests of everything} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    catch {[$queue getNextKey]} msg1
    catch {[$queue take]} msg2
    catch {[$queue getPreviousKey]} msg3
    set mylist [list $msg1 $msg2 $msg3]


    $queue put $p1 $n5;# queue size should get doubled here, becomes 4
    lappend mylist [$queue getNextKey]

    $queue put $p1again $n10;# Note that due to implementation, this will come before n1
    lappend mylist [$queue getNextKey]

    $queue put $p5 $n2
    lappend mylist [$queue getNextKey] \
	    [$queue remove $p5 $n2]

    # This sequence of take would half the queue size to 4
    lappend mylist [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue size]
    catch {[$queue take]} msg1
    catch {[$queue getNextKey]} msg2
    lappend mylist $msg1 $msg2 \
	    [$queue remove $p2 $n4]

    $queue put $p2 $n4
    $queue put $p6 $n8
    $queue put $p4 $n9 ;# queue size should get doubled here, becomes 8
    $queue put $p3 $n11
    $queue put $p9 $n1

    lappend mylist [$queue remove $p3 $n4] \
	    [$queue includes $p2 $n4] \
	    [$queue remove $p6 $n8] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
    catch {[$queue take]} msg1
    catch {[$queue getPreviousKey]} msg2
    lappend mylist $msg1 $msg2

    $queue put $p7 $n3
    $queue put $p10 $n16
    $queue put $p8 $n7


    lappend mylist [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]
} {{java.lang.IllegalAccessException: Invoking getNextKey() on empty queue is not allowed.} {java.lang.IllegalAccessException: Invoking take() on empty queue is not allowed.} {java.lang.IllegalAccessException: No take() or valid take() precedes this operation} 0.0 0.0 0.0 1 0.0 n5 0.0 0.0 n10 0.0 0 {java.lang.IllegalAccessException: Invoking take() on empty queue is not allowed.} {java.lang.IllegalAccessException: Invoking getNextKey() on empty queue is not allowed.} 0 0 1 1 n4 0.1 n11 0.2 n9 3.0 n1 999.1 {java.lang.IllegalAccessException: Invoking take() on empty queue is not allowed.} {java.lang.IllegalAccessException: No take() or valid take() precedes this operation} 8.9 n3 8.9 50.0 n7 50.0 999.3 n16 999.3}


######################################################################
####
#
test CalendarQueue-6.1 {Tests identical entry} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p1 $n5
    $queue put $p1 $n5

    set mylist [list [$queue getNextKey] \
	    [$queue remove $p1 $n5] \
	    [$queue getNextKey] \
	    [[$queue take] getName] \
	    [$queue getPreviousKey]]

    $queue put $p1 $n5
    $queue put $p1 $n5
    $queue put $p1 $n5
    lappend mylist [$queue remove $p2 $n3] \
	    [$queue remove $p2 $n3] \
	    [$queue remove $p3 $n7]
    $queue put $p1 $n5
    lappend mylist [$queue includes $p2 $n3] \
	    [[$queue take] getName] \
	    [[$queue take] getName] \
	    [[$queue take] getName] \
	    [[$queue take] getName]
    catch {[$queue getNextKey]} msg1
    lappend mylist $msg1

} {0.0 1 0.0 n5 0.0 0 0 0 0 n5 n5 n5 n5 {java.lang.IllegalAccessException: Invoking getNextKey() on empty queue is not allowed.}}

######################################################################
####
#
