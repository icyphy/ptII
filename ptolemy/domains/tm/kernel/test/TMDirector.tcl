# Tests for the TMDirector class
#
# @Author: Christopher Hylands, based on SDFDirector.tcl by Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2003 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test TMDirector-2.1 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d1 [java::new ptolemy.domains.tm.kernel.TMDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.tm.kernel.TMDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.tm.kernel.TMDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test TMDirector-3.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d4 [java::cast ptolemy.domains.tm.kernel.TMDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.Manager}

# Get a parameter by name, properly cast to Parameter.
#
proc getParameter {namedobj paramname} {
    set p [$namedobj getAttribute $paramname]
    return [java::cast ptolemy.data.expr.Parameter $p]
}

# Create an TM model with no actors in it and return it.
# The optional argument sets the stop time for the execution.
# It defaults to 1.0.
#
proc tmModel {{stopTime 1.}} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    $e0 setName top
    $e0 setManager $manager
    set director \
            [java::new ptolemy.domains.tm.kernel.TMDirector $e0 TMDirector]

    set stopparam [getParameter $director stopTime]
    $stopparam setToken [java::new ptolemy.data.DoubleToken $stopTime];

    return $e0
}



######################################################################
####
#
test TMDirector-4.1 {addScheduleListener} {
    set e0 [tmModel]
    set d4 [java::cast ptolemy.domains.tm.kernel.TMDirector [$e0 getDirector]]

    # No listeners have been added, and no scheduler is present
    set listener [java::new ptolemy.domains.tm.kernel.test.TestScheduleListener]
    # Try remove when there are no ScheduleListeners added yet
    $d4 removeScheduleListener $listener

    $d4 addScheduleListener $listener

    # Try adding it twice
    $d4 addScheduleListener $listener
    set r1 [$listener getEvents]
    $listener event "foo" 1.0 1
    
    # Remove the listener
    $d4 removeScheduleListener $listener
    $listener event "bar" 2.0 2

    set r2 [$listener getEvents]
    list $r1 $r2
} {{} {foo	1.0	1
bar	2.0	2
}}

######################################################################
####
#
test TMDirector-5.1 {run a simple model } {
    set e0 [tmModel 2.0]
    set d5 [java::cast ptolemy.domains.tm.kernel.TMDirector [$e0 getDirector]]

    set scheduleListener \
	[java::new ptolemy.domains.tm.kernel.test.TestScheduleListener]
    $d5 addScheduleListener $scheduleListener

    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]

    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]
    set priorityRamp1 [java::new ptolemy.data.expr.Parameter \
			   $ramp1 "priority" \
			   [java::new ptolemy.data.IntToken 4]]


    set ramp2 [java::new ptolemy.actor.lib.Ramp $e0 ramp2]
    set priorityRamp2 [java::new ptolemy.data.expr.Parameter \
			   $ramp2 "priority" \
			   [java::new ptolemy.data.IntToken 3]]

    set rec1 [java::new ptolemy.actor.lib.Recorder $e0 rec1]
    set priorityRec1 [java::new ptolemy.data.expr.Parameter \
			   $rec1 "priority" \
			   [java::new ptolemy.data.IntToken 2]]

    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]
    set priorityRec2 [java::new ptolemy.data.expr.Parameter \
			   $rec2 "priority" \
			   [java::new ptolemy.data.IntToken 1]]



    set relation [java::new ptolemy.actor.TypedIORelation $e0 relation]

    [$clock getPort output] link $relation

    [java::field [java::cast ptolemy.actor.lib.Source $ramp1] trigger] \
	link $relation

    [java::field [java::cast ptolemy.actor.lib.Source $ramp2] trigger] \
	link $relation

    $e0 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]

    $e0 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]

    # cover debug() clauses
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set debugListener [java::new ptolemy.kernel.util.StreamListener $printStream]

    $d5 addDebugListener $debugListener

    [$e0 getManager] execute

    $printStream flush
    $d5 removeDebugListener $debugListener

    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" debugOutput
    # Only get the first 18 chars
    set shortDebugOutput [string range "$debugOutput" 0 18]
    list \
	[enumToTokenValues [$rec1 getRecord 0]] \
	[enumToTokenValues [$rec2 getRecord 0]] \
	[$scheduleListener getEvents] \
	$shortDebugOutput
} {{0 1 2} {0 1 2} {	0.0	-1
ramp2	0.0	1
ramp1	0.0	2
rec2	0.0	3
rec2	0.0	1
ramp1	0.0	3
ramp1	0.0	1
rec1	0.0	3
rec1	0.0	1
ramp2	1.0	1
ramp1	1.0	2
rec2	1.0	3
rec2	1.0	1
ramp1	1.0	3
ramp1	1.0	1
rec1	1.0	3
rec1	1.0	1
ramp2	2.0	1
ramp1	2.0	2
rec2	2.0	3
rec2	2.0	1
ramp1	2.0	3
ramp1	2.0	1
rec1	2.0	3
rec1	2.0	1
} {Updating TMDirector}}
test TMDirector-5.2 {run a simple model with different priorities } {
    # Uses test 5.1 above
    $priorityRamp1 setToken [java::new ptolemy.data.IntToken 2]

    [$e0 getManager] execute

    # Note that ramp1 gets fired before ramp2 here
    list \
	[enumToTokenValues [$rec1 getRecord 0]] \
	[enumToTokenValues [$rec2 getRecord 0]] \
	[$scheduleListener getEvents]
} {{0 1 2} {0 1 2} {	0.0	-1
ramp1	0.0	1
ramp2	0.0	2
rec1	0.0	3
rec1	0.0	1
ramp2	0.0	3
ramp2	0.0	1
rec2	0.0	3
rec2	0.0	1
ramp1	1.0	1
ramp2	1.0	2
rec1	1.0	3
rec1	1.0	1
ramp2	1.0	3
ramp2	1.0	1
rec2	1.0	3
rec2	1.0	1
ramp1	2.0	1
ramp2	2.0	2
rec1	2.0	3
rec1	2.0	1
ramp2	2.0	3
ramp2	2.0	1
rec2	2.0	3
rec2	2.0	1
}}

test TMDirector-5.3 {run a simple model with preemptive scheduling } {
    set stopparam [getParameter $d5 stopTime]
    $stopparam setToken [java::new ptolemy.data.DoubleToken 5.0];

    set preemptiveParameter [getParameter $d5 preemptive]
    $preemptiveParameter setToken [java::new ptolemy.data.BooleanToken true];

    set defaultTaskExecutionTimeParameter \
	[getParameter $d5 defaultTaskExecutionTime]
    $defaultTaskExecutionTimeParameter setToken [java::new ptolemy.data.DoubleToken 1.0];

    [$e0 getManager] execute

    # Note that ramp1 gets fired before ramp2 here
    list \
	[enumToTokenValues [$rec1 getRecord 0]] \
	[enumToTokenValues [$rec2 getRecord 0]] \
	[$scheduleListener getEvents]
} {{0 1} {} {	0.0	-1
FIXME: KNOWN FAILURE: how come rec2 never fires?
ramp2	0.0	2
ramp1	0.0	3
ramp1	1.0	1
ramp2	1.0	2
rec1	1.0	3
ramp2	1.0	2
ramp2	1.0	2
ramp1	1.0	2
rec1	1.0	3
rec1	2.0	1
ramp2	2.0	2
ramp2	2.0	2
ramp1	2.0	3
ramp2	2.0	2
ramp2	2.0	2
ramp2	2.0	2
ramp1	2.0	2
ramp1	2.0	3
ramp1	3.0	1
ramp2	3.0	2
ramp2	3.0	2
ramp2	3.0	2
rec1	3.0	2
ramp1	3.0	3
ramp2	3.0	2
ramp2	3.0	2
ramp2	3.0	2
ramp2	3.0	2
ramp1	3.0	2
rec1	3.0	2
ramp1	3.0	3
ramp1	4.0	1
ramp2	4.0	2
ramp2	4.0	2
ramp2	4.0	2
ramp2	4.0	2
rec1	4.0	2
ramp1	4.0	2
rec1	4.0	3
ramp2	4.0	2
ramp2	4.0	2
ramp2	4.0	2
ramp2	4.0	2
ramp2	4.0	2
ramp1	4.0	2
rec1	4.0	2
ramp1	4.0	2
rec1	4.0	3
rec1	5.0	1
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp1	5.0	2
rec1	5.0	2
ramp1	5.0	3
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp2	5.0	2
ramp1	5.0	2
ramp1	5.0	2
rec1	5.0	2
ramp1	5.0	3
}} {KNOWN FAILURE: how come rec2 never fires?}

