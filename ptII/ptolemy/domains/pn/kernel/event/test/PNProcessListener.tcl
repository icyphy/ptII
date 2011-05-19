# Tests for the PNProcessListener class
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
test PNProcessListener-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    set d3 [java::new ptolemy.domains.pn.kernel.PNDirector $e0 D3]
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]
    #set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]

    # No listeners have been added, and no scheduler is present
    set listener \
	[java::new ptolemy.domains.pn.kernel.event.test.StringPNListener]

    # Try remove when there are no ScheduleListeners added yet
    $d3 removeProcessListener $listener

    $d3 addProcessListener $listener

    # Try adding it twice
    $d3 addProcessListener $listener
    set r1 [$listener getProfile]
    
    set pnProcessEvent \
	[java::new {ptolemy.domains.pn.kernel.event.PNProcessEvent \
			ptolemy.actor.Actor int} \
	     $a1 \
	     [java::field ptolemy.domains.pn.kernel.event.PNProcessEvent \
		  PROCESS_RUNNING]]

    $listener processStateChanged $pnProcessEvent

    set pnProcessEvent2 \
	[java::new {ptolemy.domains.pn.kernel.event.PNProcessEvent \
			ptolemy.actor.Actor int} \
	     $a1 \
	     [java::field ptolemy.domains.pn.kernel.event.PNProcessEvent \
		  PROCESS_FINISHED]]

    $listener processFinished $pnProcessEvent2

    # Remove the listener
    $d3 removeProcessListener $listener
    # $listener processFinished

    $a1 clear
    $manager run

    set r2 [$listener getProfile]
    list $r1 $r2
} {{} {State of .E0.A1 is PROCESS_RUNNING
State of .E0.A1 is PROCESS_FINISHED and the cause = FINISHED_CAUSE_UNKNOWN
}}
