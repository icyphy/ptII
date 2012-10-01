# Tests for the PNDirector class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998-2012 The Regents of the University of California.
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
test PNDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.pn.kernel.PNDirector]
    $d1 setName D1
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set d2 [java::new ptolemy.domains.pn.kernel.PNDirector $w]
    set d3 [java::new ptolemy.domains.pn.kernel.PNDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 . .E0.D3}

######################################################################
####
#
test PNDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
set d4 [java::cast ptolemy.domains.pn.kernel.PNDirector [$d3 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .}


######################################################################
####
#
test PNDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    set d3 [java::new ptolemy.domains.pn.kernel.PNDirector $e0 D3]
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {. .E0}}

######################################################################
####
#
test PNDirector-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]
    set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]
    $a1 clear
    $manager run
    lsort [$a1 getRecord]
    
} {.E0.A1.fire .E0.A1.initialize .E0.A1.postfire .E0.A1.prefire .E0.A1.wrapup .E0.A2.fire .E0.A2.initialize .E0.A2.postfire .E0.A2.prefire .E0.A2.wrapup}

test PNDirector-5.2.1 {cover debug basic block} {
    # NOTE: Uses the setup above
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set debugListener \
	[java::new ptolemy.kernel.util.StreamListener $printStream]
    $d3 addDebugListener $debugListener

    $a1 clear
    $manager run
    $printStream flush
    $d3 removeDebugListener $debugListener

    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" debugOutput
    # Truncate debugging output because it varies
    #puts $debugOutput 	
    list [string range $debugOutput 0 594]
} {{.E0.D3 Preinitializing ...
attribute ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.D3._iconDescription} changed
attribute ptolemy.data.expr.Parameter {.E0.D3.startTime} value undefined changed
attribute ptolemy.data.expr.Parameter {.E0.D3.stopTime} value undefined changed
attribute ptolemy.data.expr.Parameter {.E0.D3.initialQueueCapacity} 1 changed
attribute ptolemy.data.expr.Parameter {.E0.D3.maximumQueueCapacity} 65536 changed
Invoking preinitialize():  .E0.A1
Invoking preinitialize():  .E0.A2
.E0.D3 Finished preinitialize().
Called initialize().
Invoking initialize():  .E0.A1}}

######################################################################
####
#
test PNDirector-5.2 {Test creation of a receiver} {
    set r1 [java::cast ptolemy.domains.pn.kernel.PNQueueReceiver \
	    [$d3 newReceiver]]
    set p1 [$d4 getAttribute "initialQueueCapacity"]
    _testSetToken $p1 [java::new {ptolemy.data.IntToken int} 5]
    set r2 [java::cast ptolemy.domains.pn.kernel.PNQueueReceiver \
	    [$d4 newReceiver]]
    list [$r1 getCapacity] [$r2 getCapacity]
} {1 5}

######################################################################
####
#
test PNDirector-7.1 {Test finishing methods} {
    set e71 [java::new ptolemy.actor.CompositeActor]
    $e71 setName E71
    set manager [java::new ptolemy.actor.Manager]
    $e71 setManager $manager
    set d71 [java::new ptolemy.domains.pn.kernel.PNDirector]
    $d71 setName D71    
    $e71 setDirector $d71
    set p1 [$d71 getAttribute "initialQueueCapacity"]
    _testSetToken $p1 [java::new {ptolemy.data.IntToken int} 5]
    set t1 [java::new ptolemy.domains.pn.kernel.test.TestDirector $e71 t1]
    set p1 [$t1 getPort input]
    set p2 [$t1 getPort output]
    $e71 connect $p1 $p2
    $manager run
    set prof [$t1 getProfile]
    #This tests that the application can run twice without recreating the 
    #model
    $t1 clearProfile
    $manager run
    list $prof [$t1 getProfile]
} {{broadcast new token 0
broadcast new token 1
received new token 0
received new token 1
broadcast new token 2
} {broadcast new token 0
broadcast new token 1
received new token 0
received new token 1
broadcast new token 2
}}

