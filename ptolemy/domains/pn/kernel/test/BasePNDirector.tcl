# Tests for the BasePNDirector class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]


######################################################################
####
#
test BasePNDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.pn.kernel.BasePNDirector]
    $d1 setName D1
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set d2 [java::new ptolemy.domains.pn.kernel.BasePNDirector $w]
    set d3 [java::new ptolemy.domains.pn.kernel.BasePNDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 . .E0.D3}


######################################################################
####
#
test BasePNDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.domains.pn.kernel.BasePNDirector [$d3 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .}


######################################################################
####
#
test BasePNDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    set d3 [java::new ptolemy.domains.pn.kernel.BasePNDirector $e0 D3]
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {. .E0}}

######################################################################
####
#
test BasePNDirector-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]
    set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]
    $a1 clear
    $manager run
    # run the application again
    $manager run
    lsort [$a1 getRecord]
} {.E0.A1.fire .E0.A1.fire .E0.A1.initialize .E0.A1.initialize .E0.A1.postfire .E0.A1.postfire .E0.A1.prefire .E0.A1.prefire .E0.A1.wrapup .E0.A1.wrapup .E0.A2.fire .E0.A2.fire .E0.A2.initialize .E0.A2.initialize .E0.A2.postfire .E0.A2.postfire .E0.A2.prefire .E0.A2.prefire .E0.A2.wrapup .E0.A2.wrapup}

######################################################################
####
#
test BasePNDirector-5.2 {Test creation of a receiver} {
    set r1 [java::cast ptolemy.domains.pn.kernel.PNQueueReceiver \
	    [$d3 newReceiver]]
    #FIXME: Check if this is correct!
    set p1 [$d4 getAttribute "Initial_queue_capacity"]
    #$p1 setToken [java::new {ptolemy.data.IntToken int} 5]

    # _testSetToken is defined in $PTII/util/testsuite/testParams.tcl
    _testSetToken $p1 [java::new {ptolemy.data.IntToken int} 5]

    set r2 [java::cast ptolemy.domains.pn.kernel.PNQueueReceiver \
	    [$d4 newReceiver]]
    list [$r1 getCapacity] [$r2 getCapacity]
} {1 5}


######################################################################
####
#
test BasePNDirector-7.1 {Test finishing methods} {
    set e71 [java::new ptolemy.actor.CompositeActor]
    $e71 setName E71
    set manager [java::new ptolemy.actor.Manager]
    $e71 setManager $manager
    set d71 [java::new ptolemy.domains.pn.kernel.BasePNDirector]
    $d71 setName D71    
    $e71 setDirector $d71
    set p1 [$d71 getAttribute "Initial_queue_capacity"]
    _testSetToken $p1 [java::new {ptolemy.data.IntToken int} 5]
    set t1 [java::new ptolemy.domains.pn.kernel.test.TestDirector $e71 t1]
    set p1 [$t1 getPort input]
    set p2 [$t1 getPort output]
    $e71 connect $p1 $p2
    set lis [java::new ptolemy.domains.pn.kernel.event.test.StringPNListener]
    $d71 addProcessListener $lis
    $manager run
    set prof [$t1 getProfile]
    #Remove listener and run it again to confirm the action of removelistener
    #It also tests that the application can run twice without recreating the 
    #model
    $d71 removeProcessListener $lis
    $t1 clearProfile
    $manager run
    list $prof [$t1 getProfile] [$lis getProfile]
} {{broadcast new token 0
broadcast new token 1
received new token 0
received new token 1
} {broadcast new token 0
broadcast new token 1
received new token 0
received new token 1
} {State of .E71.t1 is PROCESS_FINISHED and the cause = FINISHED_PROPERLY
}}

######################################################################
####
#
test BasePNDirector-7.2 {Test artificial deadlock detection} {
    set e72 [java::new ptolemy.actor.CompositeActor]
    $e72 setName E72
    set manager [java::new ptolemy.actor.Manager]
    $e72 setManager $manager
    set d72 [java::new ptolemy.domains.pn.kernel.BasePNDirector]
    $d72 setName D72    
    $e72 setDirector $d72
    set p1 [$d72 getAttribute "Initial_queue_capacity"]
    _testSetToken $p1 [java::new {ptolemy.data.IntToken int} 0]
    set t1 [java::new ptolemy.domains.pn.kernel.test.TestDirector $e72 t1]
    set p1 [$t1 getPort input]
    set p2 [$t1 getPort output]
    $e72 connect $p1 $p2
    set lis [java::new ptolemy.domains.pn.kernel.event.test.StringPNListener]
    $d72 addProcessListener $lis
    $manager run
    list [$t1 getProfile] [$lis getProfile]
} {{broadcast new token 0
broadcast new token 1
received new token 0
received new token 1
} {State of .E72.t1 is PROCESS_BLOCKED and the cause = BLOCKED_ON_WRITE
State of .E72.t1 is PROCESS_RUNNING
State of .E72.t1 is PROCESS_BLOCKED and the cause = BLOCKED_ON_WRITE
State of .E72.t1 is PROCESS_RUNNING
State of .E72.t1 is PROCESS_FINISHED and the cause = FINISHED_PROPERLY
}}
