# Tests for the TimedEvent class
#
# @Author: Christopher Hylands.  Based on CalendarQueue.tcl by Lukito Muliadi
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
# TimedEvent$TimeComparator is an inner class
set d1 [java::new ptolemy.actor.Director]
$d1 setName D1

set comparator [java::new {ptolemy.actor.util.TimedEvent$TimeComparator}]

######################################################################
####
#
test TimedEvent-2.1 {Construct an empty queue and check defaults} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    list [$queue size] [$queue isEmpty]
} {0 1}

######################################################################
####
#
test TimedEvent-2.2 {Construct an empty queue and attempt a take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    catch {[$queue take]} msg1
    list $msg1
} {{ptolemy.kernel.util.InvalidStateException: Queue is empty.}}

######################################################################
######################################################################
# The following objects are used throughout the rest of the tests.
set p1 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 0.0] [java::new String "<0.0>"]]

set p2 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 0.1] [java::new String "<0.1>"]]

set p3 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 0.2] [java::new String "<0.2>"]]

set p4 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 3.0] [java::new String "<3.0>"]]

set p5 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 4.0] [java::new String "<4.0>"]]

set p6 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 7.6] [java::new String "<7.6>"]]

set p7 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 8.9] [java::new String "<8.9>"]]

set p8 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 50.0] [java::new String "<50.0>"]]

set p9 [java::new ptolemy.actor.util.TimedEvent \
	    [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		 $d1 999.1] [java::new String "<999.1>"]]

set p10 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 999.3] [java::new String "<999.3>"]]

set p11 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 999.8] [java::new String "<999.8>"]]

set p12 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 1001.0] [java::new String "<1001.0>"]]

set p13 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 1002.1] [java::new String "<1002.1>"]]

set p14 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 1002.2] [java::new String "<1002.2>"]]

set p15 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 1002.3] [java::new String "<1002.3>"]]

set p16 [java::new ptolemy.actor.util.TimedEvent\
	     [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double}\
		  $d1 1002.4] [java::new String "<2.4>"]]

set p1again [java::new ptolemy.actor.util.TimedEvent \
		 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director \
				 double} $d1 0.0] [java::new String "<0.0>"]]


######################################################################
####
#
test TimedEvent-3.0 {Put 4 entries in the queue and do a single take} {
    set queue [java::new ptolemy.actor.util.CalendarQueue $comparator]
    $queue put $p4
    $queue put $p2
    $queue put $p3
    $queue put $p1
    set g [java::cast ptolemy.actor.util.TimedEvent [$queue get]]
    set t [java::cast ptolemy.actor.util.TimedEvent [$queue take]]
    list [[java::field $g timeStamp] getDoubleValue] \
	[java::field $g contents] \
	[[java::field $t timeStamp] getDoubleValue] \
	[java::field $t contents] \
	[$queue isEmpty] \
	[$queue size]
} {0.0 <0.0> 0.0 <0.0> 0 3}


######################################################################
####
#
test TimedEvent-3.3 {Test the resize method } {
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
	set t [java::cast ptolemy.actor.util.TimedEvent [$queue take]]
        lappend result [list\
			    [[java::field $t timeStamp] getDoubleValue] \
				 [java::field $t contents]]
    }
    list $result
} {{{0.0 <0.0>} {0.0 <0.0>} {0.1 <0.1>} {0.2 <0.2>} {3.0 <3.0>} {4.0 <4.0>} {7.6 <7.6>} {8.9 <8.9>} {50.0 <50.0>} {999.1 <999.1>} {999.3 <999.3>}}}
