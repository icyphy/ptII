# Tests for the BoundaryDetector class
#
# @Author: John S. Davis II
#
# @Version: : BoundaryDetector.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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
test BoundaryDetector-2.1 {Check is...Boundary() for single layer boundary} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set wormhole [java::new ptolemy.actor.TypedCompositeActor $toplevel "wormhole"]
    set topdir [java::new ptolemy.actor.Director $toplevel "topdirector"]
    set wormdir [java::new ptolemy.actor.Director $wormhole "wormdirector"]


    # Assign Directors/Managers
    $toplevel setManager $mgr
    $toplevel setDirector $topdir
    $wormhole setDirector $wormdir


    # Instantiate Actors
    set a1 [java::new ptolemy.actor.process.test.TypedTestProcessActor $toplevel "a1"]
    set a2 [java::new ptolemy.actor.process.test.TypedTestProcessActor $toplevel "a2"]

    set b1 [java::new ptolemy.actor.process.test.TypedTestProcessActor $wormhole "b1"]
    set b2 [java::new ptolemy.actor.process.test.TypedTestProcessActor $wormhole "b2"]


    # Add Ports to the Wormhole
    set wormInPort [java::new ptolemy.actor.TypedIOPort $wormhole "input" true false]
    set wormOutPort [java::new ptolemy.actor.TypedIOPort $wormhole "output" false true]


    # Add Ports to the other Actors
    set a1OutPort [java::new ptolemy.actor.TypedIOPort $a1 "output" false true]
    set a2InPort [java::new ptolemy.actor.TypedIOPort $a2 "input" true false]

    set b1InPort [java::new ptolemy.actor.TypedIOPort $b1 "input" true false]
    set b1OutPort [java::new ptolemy.actor.TypedIOPort $b1 "output" false true]
    set b2InPort [java::new ptolemy.actor.TypedIOPort $b2 "input" true false]

    $b1OutPort setMultiport true


    # Connect Inside Wormhole Ports
    $wormhole connect $wormInPort $b1InPort
    $wormhole connect $b1OutPort $b2InPort
    $wormhole connect $b1OutPort $wormOutPort 


    # Connect Outer Ports
    $toplevel connect $a1OutPort $wormInPort
    $toplevel connect $wormOutPort $a2InPort


    # Create Receivers
    $toplevel preinitialize

    set b1InIOPort [java::cast ptolemy.actor.TypedIOPort $b1InPort]
    set rcvrs [$b1InIOPort getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 1 && $valc == 0 && $vald == 0 } {
        set val1 1
    } else {
        set val1 0
    }

    set b2InIOPort [java::cast ptolemy.actor.TypedIOPort $b2InPort]
    set rcvrs [$b2InIOPort getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 0 && $valc == 0 && $vald == 0 } {
        set val2 1
    } else {
        set val2 0
    }

    set a2InIOPort [java::cast ptolemy.actor.TypedIOPort $a2InPort]
    set rcvrs [$a2InIOPort  getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 0 && $valc == 1 && $vald == 0 } {
        set val3 1
    } else {
        set val3 0
    }

    set wormOutIOPort [java::cast ptolemy.actor.TypedIOPort $wormOutPort]
    set rcvrs [$wormOutIOPort getInsideReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 1  && $valb == 0 && $valc == 0 && $vald == 0 } {
        set val4 1
    } else {
        set val4 0
    }

    set wormInIOPort [java::cast ptolemy.actor.TypedIOPort $wormInPort]
    set rcvrs [$wormInIOPort getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 0 && $valc == 0 && $vald == 1 } {
        set val5 1
    } else {
        set val5 0
    }

    list $val1 $val2 $val3 $val4 $val5

} {1 1 1 1 1}

######################################################################
####
#
test BoundaryDetector-2.2 {Check is...Boundary() for multilayered boundaries} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set mgr [java::new ptolemy.actor.Manager $wspc "manager"]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set outerworm [java::new ptolemy.actor.TypedCompositeActor $toplevel "outerworm"]
    set innerworm [java::new ptolemy.actor.TypedCompositeActor $outerworm "innerworm"]
    set topdir [java::new ptolemy.actor.Director $toplevel "topdirector"]
    set outerwormdir [java::new ptolemy.actor.Director $outerworm "outerwormdirector"]
    set innerwormdir [java::new ptolemy.actor.Director $innerworm "innerwormdirector"]

    # Assign Directors/Managers
    $toplevel setManager $mgr
    $toplevel setDirector $topdir
    $outerworm setDirector $outerwormdir
    $innerworm setDirector $innerwormdir

    # Instantiate Atomic Actors
    set a1 [java::new ptolemy.actor.process.test.TypedTestProcessActor $toplevel "a1"]
    set a2 [java::new ptolemy.actor.process.test.TypedTestProcessActor $toplevel "a2"]

    set b1 [java::new ptolemy.actor.process.test.TypedTestProcessActor $innerworm "b1"]
    set b2 [java::new ptolemy.actor.process.test.TypedTestProcessActor $innerworm "b2"]


    # Add Ports to the Inner Wormhole
    set innerwormInPort [java::new ptolemy.actor.TypedIOPort $innerworm "input" true false]
    set innerwormOutPort [java::new ptolemy.actor.TypedIOPort $innerworm "output" false true]

    # Add Ports to the Outer Wormhole
    set outerwormInPort [java::new ptolemy.actor.TypedIOPort $outerworm "input" true false]
    set outerwormOutPort [java::new ptolemy.actor.TypedIOPort $outerworm "output" false true]

    # Add Ports to the Atomic Actors
    set a1OutPort [java::new ptolemy.actor.TypedIOPort $a1 "output" false true]
    set a2InPort [java::new ptolemy.actor.TypedIOPort $a2 "input" true false]

    set b1InPort [java::new ptolemy.actor.TypedIOPort $b1 "input" true false]
    set b1OutPort [java::new ptolemy.actor.TypedIOPort $b1 "output" false true]
    set b2InPort [java::new ptolemy.actor.TypedIOPort $b2 "input" true false]

    $b1OutPort setMultiport true


    # Connect Interior Inner Wormhole Ports
    $innerworm connect $innerwormInPort $b1InPort
    $innerworm connect $b1OutPort $b2InPort 
    $innerworm connect $b1OutPort $innerwormOutPort


    # Connect Interior Outer Wormhole Ports
    $outerworm connect $outerwormInPort $innerwormInPort 
    $outerworm connect $innerwormOutPort $outerwormOutPort 


    # Connect Interior Top Level Ports
    $toplevel connect $a1OutPort $outerwormInPort
    $toplevel connect $outerwormOutPort $a2InPort


    # Create Receivers
    $toplevel preinitialize

    set innerwormOutIOPort [java::cast ptolemy.actor.TypedIOPort $innerwormOutPort]
    set rcvrs [$innerwormOutIOPort getInsideReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 1  && $valb == 0 && $valc == 0 && $vald == 0 } {
        set val1 1
    } else {
        set val1 0
    }

    set innerwormInIOPort [java::cast ptolemy.actor.TypedIOPort $innerwormInPort]
    set rcvrs [$innerwormInIOPort getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 1 && $valc == 0 && $vald == 1 } {
        set val2 1
    } else {
        set val2 0
    }

    set outerwormOutIOPort [java::cast ptolemy.actor.TypedIOPort $outerwormOutPort]
    set rcvrs [$outerwormOutIOPort getInsideReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 1  && $valb == 0 && $valc == 0 && $vald == 0 } {
        set val3 1
    } else {
        set val3 0
    }

    set outerwormInIOPort [java::cast ptolemy.actor.TypedIOPort $outerwormInPort]
    set rcvrs [$outerwormInIOPort getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 0 && $valc == 0 && $vald == 1 } {
        set val4 1
    } else {
        set val4 0
    }

    set a2InIOPort [java::cast ptolemy.actor.TypedIOPort $a2InPort]
    set rcvrs [$a2InIOPort  getReceivers]
    set rcvr [java::cast ptolemy.actor.Mailbox [$rcvrs get {0 0}]]
    set detector [java::new ptolemy.actor.process.BoundaryDetector $rcvr]
    set vala [$detector isInsideBoundary]
    set valb [$detector isConnectedToBoundaryInside]
    set valc [$detector isConnectedToBoundaryOutside]
    set vald [$detector isOutsideBoundary]
    if { $vala == 0  && $valb == 0 && $valc == 1 && $vald == 0 } {
        set val5 1
    } else {
        set val5 0
    }

    list $val1 $val2 $val3 $val4 $val5

} {1 1 1 1 1}
