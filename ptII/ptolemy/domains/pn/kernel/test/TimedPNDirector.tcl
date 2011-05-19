# Tests for the TimedPNDirector class
#
# @Author: Mudit Goel
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
test TimedPNDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.pn.kernel.TimedPNDirector]
    $d1 setName D1
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set d2 [java::new ptolemy.domains.pn.kernel.TimedPNDirector $w]
    set d3 [java::new ptolemy.domains.pn.kernel.TimedPNDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 . .E0.D3}

######################################################################
####
#
test TimedPNDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.domains.pn.kernel.TimedPNDirector [$d3 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .}


######################################################################
####
#
test TimedPNDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    set d3 [java::new ptolemy.domains.pn.kernel.TimedPNDirector $e0 D3]
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {. .E0}}

######################################################################
####
#
test TimedPNDirector-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]
    set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]
    $a1 clear
    $manager run
    lsort [$a1 getRecord]
} {.E0.A1.fire .E0.A1.initialize .E0.A1.postfire .E0.A1.prefire .E0.A1.wrapup .E0.A2.fire .E0.A2.initialize .E0.A2.postfire .E0.A2.prefire .E0.A2.wrapup}

######################################################################
####
#
test TimedPNDirector-5.2 {Test creation of a receiver} {
    set r1 [java::cast ptolemy.domains.pn.kernel.PNQueueReceiver \
	    [$d3 newReceiver]]
    #FIXME: Check if this is correct!
    set p1 [$d4 getAttribute "initialQueueCapacity"]

    # _testSetToken is defined in $PTII/util/testsuite/testParams.tcl
    _testSetToken $p1 [java::new {ptolemy.data.IntToken int} 5]

    set r2 [java::cast ptolemy.domains.pn.kernel.PNQueueReceiver \
	    [$d4 newReceiver]]

    list [$r1 getCapacity] [$r2 getCapacity]
} {1 5}

######################################################################
####
#
test TimedPNDirector-6.1 {Test an application} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    set director [java::new ptolemy.domains.pn.kernel.TimedPNDirector]
    $e0 setDirector $director
    $e0 setName top
    $e0 setManager $manager

    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set param [java::cast ptolemy.data.expr.Parameter \
	  [$ramp getAttribute firingCountLimit]]
    $param setExpression 20

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
	  [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
	  [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]

} {0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19}

