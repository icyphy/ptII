# Tests for the ODIOPort class
#
# @Author: John S. Davis II
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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
test DDEIOPort-2.1 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d1 [java::new ptolemy.domains.dde.kernel.DDEIOPort]
    $d1 setName D1
    set e1 [java::new ptolemy.actor.TypedAtomicActor $w]
    $e1 setName E1
    set d3 [java::new ptolemy.domains.dde.kernel.DDEIOPort $e1 D3]
    set d4 [java::new ptolemy.domains.dde.kernel.DDEIOPort $e1 D4 true false]
    list [$d1 getFullName] [$d3 getFullName] [$d4 getFullName]
} {.D1 .E1.D3 .E1.D4}


######################################################################
####
#
test DDEIOPort-2.2 {Container must be atomic} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    catch {set d3 [java::new ptolemy.domains.dde.kernel.DDEIOPort \
		       $e0 D3]} errMsg3
    catch {set d4 [java::new ptolemy.domains.dde.kernel.DDEIOPort \
		       $e0 D4 true false]} errMsg4
    list $errMsg3 $errMsg4
} {{ptolemy.kernel.util.IllegalActionException: A DDEIOPort can not be contained by a composite actor.
  in .E0 and .E0.D3} {ptolemy.kernel.util.IllegalActionException: A DDEIOPort can not be contained by a composite actor.
  in .E0 and .E0.D4}}

######################################################################
####
#
test DDEIOPort-3.1 {setContainer: Container must be atomic} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d1 [java::new ptolemy.domains.dde.kernel.DDEIOPort]
    catch {$d1 setContainer $e0} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: A DDEIOPort can not be contained by a composite actor.
  in .E0 and .<Unnamed Object>}}


test DDEIOPort-3.2 {setContainer} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e1 [java::new ptolemy.actor.TypedAtomicActor $w]
    $e1 setName E1
    set d2 [java::new ptolemy.domains.dde.kernel.DDEIOPort $e1 D2]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $w]
    $e2 setName E2

    $d2 setContainer $e2
    list [$d2 getFullName]
} {.E2.D2}


######################################################################
####
#
test DDEIOPort-4.0 {Check send()} {
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
    set rel [java::cast ptolemy.actor.IORelation [$topLevel connect $outPort $inPort "rel"]]
    $rel setWidth 1
    

    set tok [java::new ptolemy.data.Token]
    set tokType [$tok getType]

    $outPort setTypeEquals $tokType
    $inPort setTypeEquals $tokType

    $inPort createReceivers

    set rcvrs [$inPort getReceivers]
    set rcvr [java::cast ptolemy.domains.dde.kernel.PrioritizedTimedQueue [$rcvrs get {0 0}]]

    $rcvr setCapacity 1

    set hasRoom [$rcvr hasRoom]
    set time [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $dir 5.0]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $outPort {send int ptolemy.data.Token ptolemy.actor.util.Time}  0 $tok $time
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

    set rel2 [java::cast ptolemy.actor.IORelation [$topLevel connect $outPort $inPort2 "rel2"]]
    $rel2 setWidth 1
    set rel3 [java::cast ptolemy.actor.IORelation [$topLevel connect $outPort $inPort3 "rel3"]]
		$rel3 setWidth 1

    set tok [java::new ptolemy.data.Token]
    set tokType [$tok getType]

    $outPort setTypeEquals $tokType
    $inPort2 setTypeEquals $tokType
    $inPort3 setTypeEquals $tokType

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

    set time [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $dir 5.0]
    $outPort {broadcast ptolemy.data.Token ptolemy.actor.util.Time} $tok $time

    if { [$rcvr2 hasRoom] != 0 } {
	set val 0
    }
    if { [$rcvr3 hasRoom] != 0 } {
	set val 0
    }

    list $val 

} {1}
