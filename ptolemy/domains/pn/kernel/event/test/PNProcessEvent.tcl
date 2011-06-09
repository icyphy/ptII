# Tests for the PNProcesseEvent class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]

######################################################################
####
#
test PNProcessEvent-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    set d3 [java::new ptolemy.domains.pn.kernel.PNDirector $e0 D3]
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]

    # No listeners have been added, and no scheduler is present
    set listener \
	[java::new ptolemy.domains.pn.kernel.event.test.StringPNListener]

    $d3 addProcessListener $listener

    set states [list PROCESS_BLOCKED PROCESS_FINISHED PROCESS_PAUSED \
		    PROCESS_RUNNING]
    foreach state $states {
	set pnProcessEvent \
	    [java::new {ptolemy.domains.pn.kernel.event.PNProcessEvent \
			    ptolemy.actor.Actor int} \
		 $a1 \
		 [java::field ptolemy.domains.pn.kernel.event.PNProcessEvent \
		      $state]]
	$listener processStateChanged $pnProcessEvent
    }

    set causes [list BLOCKED_ON_DELAY BLOCKED_ON_MUTATION BLOCKED_ON_READ \
		    BLOCKED_ON_WRITE FINISHED_ABRUPTLY FINISHED_PROPERLY \
		    FINISHED_WITH_EXCEPTION]
    foreach cause $causes { 
	foreach state $states {
	    set pnProcessEvent \
		[java::new {ptolemy.domains.pn.kernel.event.PNProcessEvent \
				ptolemy.actor.Actor int int} \
		     $a1 \
		     [java::field \
			  ptolemy.domains.pn.kernel.event.PNProcessEvent \
			  $state] \
		     [java::field \
			  ptolemy.domains.pn.kernel.event.PNProcessEvent \
			  $cause]]
	    $listener processStateChanged $pnProcessEvent
	}
    }

    set exception3 [java::new ptolemy.kernel.util.IllegalActionException \
		       $a1 "This is a test exception"]

    set pnProcessEvent3 \
		[java::new {ptolemy.domains.pn.kernel.event.PNProcessEvent \
				ptolemy.actor.Actor Exception} \
		     $a1 $exception3]

    $listener processFinished $pnProcessEvent3

    $a1 clear
    $manager run

    set r2 [$listener getProfile]

    list $r2
} {{State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKING_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKED_ON_DELAY
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKED_ON_MUTATION
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKED_ON_READ
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKED_ON_WRITE
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKING_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_ABRUPTLY
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKING_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_PROPERLY
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_BLOCKED and the cause = BLOCKING_CAUSE_UNKNOWN
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_WITH_EXCEPTION with null exception
State of .E0.A1 is PROCESS_PAUSED
State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_WITH_EXCEPTION with exception ptolemy.kernel.util.IllegalActionException: This is a test exception
  in .E0.A1
}}

test PNProcessEvent-2.2 {get Methods} {
 
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0

    set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]

    set exception3 [java::new ptolemy.kernel.util.IllegalActionException \
		       $a2 "This is a test exception"]

    set p3 \
		[java::new {ptolemy.domains.pn.kernel.event.PNProcessEvent \
				ptolemy.actor.Actor Exception} \
		     $a1 $exception3]

    list [[java::cast ptolemy.kernel.util.NamedObj [$p3 getActor]] \
	      getFullName] \
	[$p3 getBlockingCause] [$p3 getCurrentState] \
	[[$p3 getException] toString] \
	[$p3 getFinishingCause] \
} {.E0.A1 736 368 {ptolemy.kernel.util.IllegalActionException: This is a test exception
  in .E0.A2} 736}

