# Tests for the IORelation class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test IORelation-2.1 {Construct Relations} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    set r1 [java::new ptolemy.actor.IORelation]
    set r2 [java::new ptolemy.actor.IORelation $e1 R2]
    list [$r1 getFullName] [$r2 getFullName]
} {. ..R2}

test IORelation-2.2 {Construct Relations} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set r2 [java::new ptolemy.actor.IORelation $e1 R2]
    list [$r1 getFullName] [$r2 getFullName]
} {.E1.R1 .E1.R2}

# NOTE: We assume that the deepReceivers method is tested in the
# IOPort tests.

######################################################################
####
#
test IORelation-3.1 {Test getWidth} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    $r1 getWidth
} {1}

test IORelation-3.2 {Test getWidth} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    $r1 setWidth 4
    $r1 getWidth
} {4}

test IORelation-3.3 {Test getWidth} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    $r1 setWidth 0
    $r1 getWidth
} {1}

test IORelation-3.4 {Test getWidth of a port} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    $p1 link $r1
    catch {$r1 setWidth 4} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .E1.R1 and .E1.E2.P1:
Cannot make bus because the relation is linked to a non-multiport.}}

test IORelation-3.4.1 {Test getWidth of a port} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    $p1 link $r1
    $p1 setMultiport true
    $r1 setWidth 4
    $p1 getWidth
} {4}

test IORelation-3.5 {Test getWidth of a port with unspecified relation width} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    $r1 setWidth 0
    catch {$p1 link $r1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .E1.E2.P1 and .E1.R1:
Attempt to link a bus relation to a single port.}}

test IORelation-3.6 {Test getWidth of a port with unspecified relation width} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setDirector $director
    $e1 setManager $manager
    $e1 setName E1
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    $r1 setWidth 0
    $p1 setMultiport true
    $p1 link $r1
    $p1 getWidth
} {1}

test IORelation-3.7 {Test getWidth of a port with inferred relation width} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2]
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    catch {$r1 setWidth 0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .E0.E1.R1 and .E0.E1.E2.P1:
Cannot make bus because the relation is linked to a non-multiport.}}

test IORelation-3.8 {Test getWidth of a port with inferred relation width} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2]
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p1 setMultiport true
    catch {$r1 setWidth 0} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .E0.E1.R1 and .E0.E1.P2:
Cannot make bus because the relation is linked to a non-multiport.}}

test IORelation-3.9 {Test getWidth of a port with inferred relation width} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2]
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p1 setMultiport true
    $p2 setMultiport true
    $r1 setWidth 0
    $r2 setWidth 4
    list [$p1 getWidth] [$r1 getWidth] [$p2 getWidth]
} {4 4 4}

test IORelation-3.10 {Test getWidth of a port with inferred relation width} {
    # NOTE: Append to previous design
    set r3 [java::new ptolemy.actor.IORelation $e1 R3]
    $p2 link $r3
    list [$p1 getWidth] [$r1 getWidth] [$p2 getWidth]
} {3 3 4}

test IORelation-3.11 {Test getWidth of a port with inferred relation width} {
    # NOTE: Append to previous design
    set r4 [java::new ptolemy.actor.IORelation $e1 R4]
    $r4 setWidth 0
    catch {$p2 link $r4} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .E0.E1.P2 and .E0.E1.R4:
Attempt to link a second bus relation with unspecified width to the inside of a port.}}

test IORelation-3.12 {Test getWidth of a port with inferred relation width} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 0
    set p1 [java::new ptolemy.actor.IOPort $e1 P1]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2]
    $p1 setMultiport true
    $p2 setMultiport true
    $p1 link $r1
    $p2 link $r1
    list [$p1 getWidth] [$r1 getWidth] [$p2 getWidth]
} {1 1 1}

test IORelation-3.13 {Resolve width through three levels} {
    # E0 contains E1 contains E2
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set p0 [java::new ptolemy.actor.IOPort $e0 P0]
    $p0 setMultiport true
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1]
    $p1 setMultiport true
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2]
    $p2 setMultiport true
    set r0 [java::new ptolemy.actor.IORelation]
    $r0 setName R0
    $r0 setWidth 0
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 0
    set r2 [java::new ptolemy.actor.IORelation $e1 R2]
    $r2 setWidth 0
    $p2 link $r2
    $p1 link $r1
    $p1 link $r2
    $p0 link $r1
    $p0 link $r0
    $r0 setWidth 3
    list [$p0 getWidth] [$p1 getWidth] [$p2 getWidth] \
         [$r0 getWidth] [$r1 getWidth] [$r2 getWidth]
} {3 3 3 3 3 3}

######################################################################
####
# Build the elaborate test system from the design doc.
#
test IORelation-4.1 {Elaborate test system} {
    # Top container
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    # First level of the hierarchy
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2]
    $p2 setMultiport true
    set p3 [java::new ptolemy.actor.IOPort $e1 P3]
    $p3 setMultiport true
    set p4 [java::new ptolemy.actor.IOPort $e1 P4]
    $p4 setMultiport true

    set e3 [java::new ptolemy.actor.CompositeActor $e0 E3]
    set p5 [java::new ptolemy.actor.IOPort $e3 P5]
    $p5 setMultiport true
    set p6 [java::new ptolemy.actor.IOPort $e3 P6]

    set e6 [java::new ptolemy.actor.AtomicActor $e0 E6]
    set p7 [java::new ptolemy.actor.IOPort $e6 P7]
    $p7 setMultiport true
    $p7 setInput true

    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $r2 setWidth 3
    set r3 [java::new ptolemy.actor.IORelation $e0 R3]
    set r4 [java::new ptolemy.actor.IORelation $e0 R4]
    $r4 setWidth 2

    $p2 link $r2
    $p2 link $r3
    $p3 link $r4
    $p5 link $r2
    $p5 link $r3
    $p6 link $r3
    $p7 link $r4

    # Inside E1
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1]
    $p1 setMultiport true
    $p1 setOutput true
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    $r1 setWidth 0
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1
    $p4 link $r1

    # Inside E3
    set e4 [java::new ptolemy.actor.AtomicActor $e3 E4]
    set p8 [java::new ptolemy.actor.IOPort $e4 P8]
    $p8 setMultiport true
    $p8 setInput true
    set e5 [java::new ptolemy.actor.AtomicActor $e3 E5]
    set p9 [java::new ptolemy.actor.IOPort $e5 P9]
    $p9 setMultiport true
    $p9 setInput true
    set r5 [java::new ptolemy.actor.IORelation $e3 R5]
    $r5 setWidth 0
    set r6 [java::new ptolemy.actor.IORelation $e3 R6]
    set r7 [java::new ptolemy.actor.IORelation $e3 R7]
    $p5 link $r5
    $p5 link $r6
    $p6 link $r7
    $p8 link $r5
    $p9 link $r7
    $p9 link $r6

    # Read back widths
    list [$p1 getWidth] \
            [$r1 getWidth] \
            [$p2 getWidth] \
            [$p3 getWidth] \
            [$p4 getWidth] \
            [$r2 getWidth] \
            [$r3 getWidth] \
            [$r4 getWidth] \
            [$p5 getWidth] \
            [$p6 getWidth] \
            [$p7 getWidth] \
            [$r5 getWidth] \
            [$r6 getWidth] \
            [$r7 getWidth] \
            [$p8 getWidth] \
            [$p9 getWidth]
} {4 4 4 2 0 3 1 2 4 1 2 3 1 1 3 2}

######################################################################
####
# Test linkedDestinationPorts
# NOTE: These build on system above...
#
test IORelation-5.1 {Test linkedDestinationPorts} {
    list \
            [enumToFullNames [$r1 linkedDestinationPorts]] \
            [enumToFullNames [$r1 linkedDestinationPorts $p4]]
} {{.E0.E1.P2 .E0.E1.P3 .E0.E1.P4} {.E0.E1.P2 .E0.E1.P3}}

test IORelation-5.2 {Test linkedDestinationPorts} {
    list \
            [enumToFullNames [$r2 linkedDestinationPorts]] \
            [enumToFullNames [$r2 linkedDestinationPorts $p4]]
} {{.E0.E1.P2 .E0.E3.P5} {.E0.E1.P2 .E0.E3.P5}}

test IORelation-5.3 {Test linkedDestinationPorts} {
    list \
            [enumToFullNames [$r3 linkedDestinationPorts]] \
            [enumToFullNames [$r3 linkedDestinationPorts $p5]]
} {{.E0.E1.P2 .E0.E3.P5 .E0.E3.P6} {.E0.E1.P2 .E0.E3.P6}}

test IORelation-5.4 {Test linkedDestinationPorts} {
    list \
            [enumToFullNames [$r4 linkedDestinationPorts]] \
            [enumToFullNames [$r5 linkedDestinationPorts]] \
            [enumToFullNames [$r6 linkedDestinationPorts]] \
            [enumToFullNames [$r7 linkedDestinationPorts]]
} {{.E0.E1.P3 .E0.E6.P7} .E0.E3.E4.P8 .E0.E3.E5.P9 .E0.E3.E5.P9}


######################################################################
####
# Test linkedDestinationPortList
# NOTE: These build on system above...
#

test IORelation-6.1 {Test linkedDestinationPortList} {
    list \
            [listToFullNames [$r1 linkedDestinationPortList]] \
            [listToFullNames [$r1 linkedDestinationPortList $p4]]
} {{.E0.E1.P2 .E0.E1.P3 .E0.E1.P4} {.E0.E1.P2 .E0.E1.P3}}

test IORelation-6.2 {Test linkedDestinationPortList} {
    list \
            [listToFullNames [$r2 linkedDestinationPortList]] \
            [listToFullNames [$r2 linkedDestinationPortList $p4]]
} {{.E0.E1.P2 .E0.E3.P5} {.E0.E1.P2 .E0.E3.P5}}

test IORelation-6.3 {Test linkedDestinationPortList} {
    list \
            [listToFullNames [$r3 linkedDestinationPortList]] \
            [listToFullNames [$r3 linkedDestinationPortList $p5]]
} {{.E0.E1.P2 .E0.E3.P5 .E0.E3.P6} {.E0.E1.P2 .E0.E3.P6}}

test IORelation-6.4 {Test linkedDestinationPortList} {
    list \
            [listToFullNames [$r4 linkedDestinationPortList]] \
            [listToFullNames [$r5 linkedDestinationPortList]] \
            [listToFullNames [$r6 linkedDestinationPortList]] \
            [listToFullNames [$r7 linkedDestinationPortList]]
} {{.E0.E1.P3 .E0.E6.P7} .E0.E3.E4.P8 .E0.E3.E5.P9 .E0.E3.E5.P9}

######################################################################
####
# Test linkedSourcePorts
# NOTE: These build on system above...
#
test IORelation-7.1 {Test linkedSourcePorts} {
    list \
            [enumToFullNames [$r1 linkedSourcePorts]] \
            [enumToFullNames [$r1 linkedSourcePorts $p4]]
} {{.E0.E1.E2.P1 .E0.E1.P2 .E0.E1.P3 .E0.E1.P4} {.E0.E1.E2.P1 .E0.E1.P2 .E0.E1.P3}}

test IORelation-7.2 {Test linkedSourcePorts} {
    list \
            [enumToFullNames [$r2 linkedSourcePorts]] \
            [enumToFullNames [$r2 linkedSourcePorts $p4]]
} {.E0.E1.P2 .E0.E1.P2}

test IORelation-7.3 {Test linkedSourcePorts} {
    list \
            [enumToFullNames [$r3 linkedSourcePorts]] \
            [enumToFullNames [$r3 linkedSourcePorts $p2]]
} {.E0.E1.P2 {}}

test IORelation-7.4 {Test linkedSourcePorts} {
    list \
            [enumToFullNames [$r4 linkedSourcePorts]] \
            [enumToFullNames [$r5 linkedSourcePorts]] \
            [enumToFullNames [$r6 linkedSourcePorts]] \
            [enumToFullNames [$r7 linkedSourcePorts]]
} {.E0.E1.P3 .E0.E3.P5 .E0.E3.P5 .E0.E3.P6}

######################################################################
####
# Test isWidthFixed
# NOTE: These build on system above...
#
test IORelation-8.1 {Test isWidthFixed} {
    list \
            [$r1 isWidthFixed] \
            [$r2 isWidthFixed] \
            [$r3 isWidthFixed] \
            [$r4 isWidthFixed] \
            [$r5 isWidthFixed] \
            [$r6 isWidthFixed] \
            [$r7 isWidthFixed]
} {0 1 1 1 0 1 1}

######################################################################
####
# Test description
# NOTE: These build on system above...

test IORelation-9.1 {Test description} {
    set configuration [java::field ptolemy.actor.IORelation CONFIGURATION]
    $r1 description $configuration
} {configuration {width 4}}

test IORelation-9.2 {Test description} {
    set configuration [java::field ptolemy.actor.IORelation CONFIGURATION]
    $r2 description $configuration
} {configuration {width 3 fixed}}

test IORelation-9.3 {Test description} {
    set configuration [java::field ptolemy.actor.IORelation CONFIGURATION]
    $r3 description $configuration
} {configuration {width 1 fixed}}

######################################################################
####
# Test clone
# NOTE: These build on system above...

test IORelation-10.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set r8 [java::cast ptolemy.actor.IORelation [$r1 clone $w]]
    $r8 description $configuration
} {configuration {width 1}}

test IORelation-10.2 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set r9 [java::cast ptolemy.actor.IORelation [$r2 clone $w]]
    $r9 description $configuration
} {configuration {width 3 fixed}}


