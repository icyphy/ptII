# Tests for the ODIOPort class
#
# @Author: John S. Davis II
#
# @Version: %W%	%G%
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
test DDEIOPort-2.1 {Check send()} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set man [java::new ptolemy.actor.Manager $wspc "manager"]
    $topLevel setManager $man
    $topLevel setDirector $dir

    set act1 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act1"] 
    set act2 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act2"] 

    set outPort [java::new ptolemy.domains.dde.kernel.DDEIOPort $act1 "output" false true]
    set inPort [java::new ptolemy.domains.dde.kernel.DDEIOPort $act2 "input" true false]
    set rel [$topLevel connect $outPort $inPort "rel"]

    set tok [java::new ptolemy.data.Token]
    set tokClass [$tok getClass]

    $outPort setTypeEquals $tokClass
    $inPort setTypeEquals $tokClass

    $inPort createReceivers

    set rcvrs [$inPort getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.PrioritizedTimedQueue [$rcvrs get {0 0}]]

    $rcvr setCapacity 1

    set hasRoom [$rcvr hasRoom]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $outPort {send int ptolemy.data.Token double}  0 $tok 5.0
    set noRoom [$rcvr hasRoom]

    list $hasRoom $noRoom

} {1 0}

######################################################################
####
#
test DDEIOPort-3.1 {Broadcast tokens to two different actors.} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set dir [java::new ptolemy.domains.dde.kernel.DDEDirector $topLevel "director"]
    set act1 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act1"] 
    set act2 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act2"] 
    set act3 [java::new ptolemy.actor.TypedAtomicActor $topLevel "act3"] 


    set outPort [java::new ptolemy.domains.dde.kernel.DDEIOPort $act1 "output" false true]
    set inPort2 [java::new ptolemy.domains.dde.kernel.DDEIOPort $act2 "input" true false]
    set inPort3 [java::new ptolemy.domains.dde.kernel.DDEIOPort $act3 "input" true false]
    $outPort setMultiport true

    set rel2 [$topLevel connect $outPort $inPort2 "rel2"]
    set rel3 [$topLevel connect $outPort $inPort3 "rel3"]

    set tok [java::new ptolemy.data.Token]
    set tokClass [$tok getClass]

    $outPort setTypeEquals $tokClass
    $inPort2 setTypeEquals $tokClass
    $inPort3 setTypeEquals $tokClass

    $dir preinitialize

    set rcvrs2 [$inPort2 getReceivers]
    set rcvr2 [java::cast ptolemy.domains.dde.kernel.PrioritizedTimedQueue [$rcvrs2 get {0 0}]]
    set rcvrs3 [$inPort3 getReceivers]
    set rcvr3 [java::cast ptolemy.domains.dde.kernel.PrioritizedTimedQueue [$rcvrs3 get {0 0}]]

    $rcvr2 setCapacity 1
    $rcvr3 setCapacity 1

    set val 1

    if { [$rcvr2 hasRoom] != 1 } {
	set val 0
    }
    if { [$rcvr3 hasRoom] != 1 } {
	set val 0
    }

    $outPort broadcast $tok 5.0

    if { [$rcvr2 hasRoom] != 0 } {
	set val 0
    }
    if { [$rcvr3 hasRoom] != 0 } {
	set val 0
    }

    list $val 

} {1}
