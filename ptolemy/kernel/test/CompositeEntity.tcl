# Tests for the CompositeEntity class
#
# @Author: Edward A. Lee, Jie Liu, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

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
test CompositeEntity-2.0 {Construct CompositeEntities, call a few methods} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.CompositeEntity]
    $e2 setName A
    set e1contents [$e1 getEntities]
    list [$e1 getName] [$e2 getName] \
	    [$e1 getFullName] [$e2 getFullName] \
	    [$e1 isAtomic] [$e2 isAtomic] \
	    [ java::instanceof $e1contents java.util.Enumeration] \
	    [expr {[java::null] == [$e1 getContainer]}]
} {{} A . .A 0 0 1 1}

######################################################################
####
#
test CompositeEntity-2.1 {Create a 3 level deep tree using constructors} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $c D]
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {{B C} {} D}



######################################################################
####
#
test CompositeEntity-2.2 {Create a 3 level deep tree after construction} {
    # entity serving as a workspace
    set w [java::new ptolemy.kernel.CompositeEntity]
    set a [java::new ptolemy.kernel.CompositeEntity $w A]
    set b [java::new ptolemy.kernel.CompositeEntity $w B]
    set c [java::new ptolemy.kernel.CompositeEntity $w C]
    set d [java::new ptolemy.kernel.ComponentEntity $w D]
    $d setContainer $c
    $b setContainer $a
    $c setContainer $b
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {B C D}

######################################################################
####
#
test CompositeEntity-2.3 {test setName for NameDuplicationException} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $a D]
    catch {$d setName B} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Name duplication: B
  in .A}}

######################################################################
####
#
test CompositeEntity-3.1 {Test deepGetEntities} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $c D]
    list [enumToNames [$a deepGetEntities]] \
            [enumToNames [$c deepGetEntities]] \
} {{B D} D}

######################################################################
####
#
test CompositeEntity-3.2 {Test numEntities} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $c D]
    list [$a numEntities] [$b numEntities] [$c numEntities]
} {2 0 1}

######################################################################
####
#
test CompositeEntity-3.3 {Test getEntity by name} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $b C]
    set d [java::new ptolemy.kernel.ComponentEntity $c D]
    set e1 [$c getEntity D]
    set e2 [$b getEntity C.D]
    set e3 [$a getEntity B.C.D]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName]
} {.A.B.C.D .A.B.C.D .A.B.C.D}

######################################################################
####
#
test CompositeEntity-3.4 {Test entityList and entityList with a filter} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $a D]
    list [listToNames [$a entityList]] [listToNames [$a entityList [$a getClass]]]
} {{B C D} {B C}}

######################################################################
####
#
test CompositeEntity-4.1 {Test deepContains} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $c D]
    list [$a deepContains $d] [$a deepContains $a] [$c deepContains $a] \
            [$c deepContains $d] [$b deepContains $d]
} {1 0 0 1 0}

######################################################################
####
#
test CompositeEntity-5.1 {Test reparenting} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.ComponentEntity $c D]
    $c setContainer $b
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {B C D}

######################################################################
####
#
test CompositeEntity-5.2 {Test reparenting with an error} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.CompositeEntity $c D]
    catch {$c setContainer $d} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Attempt to construct recursive containment
  in .A.C and .A.C.D}}

######################################################################
####
#
test CompositeEntity-6.1 {Test removing entities} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $b C]
    set d [java::new ptolemy.kernel.CompositeEntity $c D]
    $b setContainer [java::null]
    $d setContainer [java::null]
    $c setContainer [java::null]
    enumMethodToNames getEntities $a $b $c $d
} {{} {} {} {}}

######################################################################
####
#
test CompositeEntity-6.3 {Test removing entities by name} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $b C]
    set d [java::new ptolemy.kernel.CompositeEntity $c D]
    [$a getEntity B] setContainer [java::null]
    enumMethodToNames getEntities $a $b $c $d
} {{} C D {}}

######################################################################
####
#
test CompositeEntity-7.1 {Add relations} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new ptolemy.kernel.ComponentRelation $a R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $a R2]
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
#
test CompositeEntity-7.2 {Add relations after creation} {
    # Workspace entity
    set w [java::new ptolemy.kernel.CompositeEntity]
    set a [java::new ptolemy.kernel.CompositeEntity $w A]
    set r1 [java::new ptolemy.kernel.ComponentRelation $w R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $w R2]
    $r1 setContainer $a
    $r2 setContainer $a
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
#
test CompositeEntity-7.3 {Get relations by name} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new ptolemy.kernel.ComponentRelation $a R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $a R2]
    set r [$a getRelation R1]
    $r getFullName
} {.A.R1}

######################################################################
####
#
test CompositeEntity-7.4 {Add relations using newRelation} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set r1 [$a newRelation R1]
    set r2 [$a newRelation R2]
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
#
test CompositeEntity-8.1 {Remove relations} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new ptolemy.kernel.ComponentRelation $a R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $a R2]
    $r1 setContainer [java::null]
    enumToNames [$a getRelations]
} {R2}

######################################################################
####
#
test CompositeEntity-8.3 {Remove relations by name} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new ptolemy.kernel.ComponentRelation $a R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $a R2]
    [$a getRelation R2] setContainer [java::null]
    enumToNames [$a getRelations]
} {R1}

######################################################################
####
#
test CompositeEntity-8.5 {Test removing all entities} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set c [java::new ptolemy.kernel.CompositeEntity $a C]
    set d [java::new ptolemy.kernel.CompositeEntity $a D]
    $a removeAllEntities
    enumToNames [$a getEntities]
} {}

######################################################################
####
#
test CompositeEntity-8.6 {Remove all relations} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new ptolemy.kernel.ComponentRelation $a R1]
    set r2 [java::new ptolemy.kernel.ComponentRelation $a R2]
    $a removeAllRelations
    enumToNames [$a getRelations]
} {}

######################################################################
####
#
test CompositeEntity-9.1 {Test transparent port} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    set p1 [java::new ptolemy.kernel.ComponentPort $a P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $b P2]
    $a connect $p2 $p1
    set result {}
    foreach ar [enumToObjects [$p1 insideRelations]] {
        lappend result [enumToFullNames \
                [[java::cast ptolemy.kernel.Relation $ar] linkedPorts $p1]]
    }
    list $result
} {.A.B.P2}

######################################################################
####
#
test CompositeEntity-10.1 {Test multiple relation naming} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentEntity $a B]
    set c [java::new ptolemy.kernel.ComponentEntity $a C]
    set p1 [java::new ptolemy.kernel.ComponentPort $a P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $b P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $c P3]
    set p4 [java::new ptolemy.kernel.ComponentPort $a P4]
    set ar1 [$a connect $p2 $p1]
    set ar2 [$a connect $p3 $p4]
    list [$ar1 getFullName] [$ar2 getFullName]
} {.A._R .A._R2}

######################################################################
####
#
test CompositeEntity-10.3 {Create and then remove a transparent port} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentEntity $a B]
    set c [java::new ptolemy.kernel.ComponentEntity $a C]
    set p1 [java::new ptolemy.kernel.ComponentPort $a P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $b P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $c P3]
    set p4 [java::new ptolemy.kernel.ComponentPort $a P4]
    set ar1 [$a connect $p2 $p1]
    set ar2 [$a connect $p3 $p4]
    set result {}
    lappend result [$ar1 getFullName] [$ar2 getFullName]
    $ar2 setContainer [java::null]
    lappend result [$p4 numInsideLinks]
} {.A._R .A._R2 0}

######################################################################
####
#
test CompositeEntity-10.4 {Create and then remove ports with given names} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.ComponentEntity $a B]
    set c [java::new ptolemy.kernel.ComponentEntity $a C]
    set p1 [java::new ptolemy.kernel.ComponentPort $a P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $b P2]
    set p3 [java::new ptolemy.kernel.ComponentPort $c P3]
    set p4 [java::new ptolemy.kernel.ComponentPort $a P4]
    set ar1 [$a connect $p2 $p1 AR1]
    set ar2 [$a connect $p3 $p4 AR2]
    set result {}
    lappend result [$ar1 getFullName] [$ar2 getFullName]
    $ar2 setContainer [java::null]
    lappend result [$p4 numInsideLinks]
} {.A.AR1 .A.AR2 0}

######################################################################
####
# NOTE:  The setup constructed in this test is used in the subsequent
# tests.
test CompositeEntity-11.1 {Test deepLinkedEntities on component relations} {
    # This structure is the example in the kernel design document.

    # See ExampleSystem.tcl for a standalone file containing these commands

    # Create composite entities
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e3 [java::new ptolemy.kernel.CompositeEntity $e0 E3]
    set e4 [java::new ptolemy.kernel.CompositeEntity $e3 E4]
    set e7 [java::new ptolemy.kernel.CompositeEntity $e0 E7]
    set e10 [java::new ptolemy.kernel.CompositeEntity $e0 E10]

    # Create component entities.
    set e1 [java::new ptolemy.kernel.ComponentEntity $e4 E1]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e4 E2]
    set e5 [java::new ptolemy.kernel.ComponentEntity $e3 E5]
    set e6 [java::new ptolemy.kernel.ComponentEntity $e3 E6]
    set e8 [java::new ptolemy.kernel.ComponentEntity $e7 E8]
    set e9 [java::new ptolemy.kernel.ComponentEntity $e10 E9]

    # Create ports.
    set p0 [java::cast ptolemy.kernel.ComponentPort [$e4 newPort P0]]
    set p1 [java::cast ptolemy.kernel.ComponentPort [$e1 newPort P1]]
    set p2 [java::cast ptolemy.kernel.ComponentPort [$e2 newPort P2]]
    set p3 [java::cast ptolemy.kernel.ComponentPort [$e2 newPort P3]]
    set p4 [java::cast ptolemy.kernel.ComponentPort [$e4 newPort P4]]
    set p5 [java::cast ptolemy.kernel.ComponentPort [$e5 newPort P5]]
    set p6 [java::cast ptolemy.kernel.ComponentPort [$e6 newPort P6]]
    set p7 [java::cast ptolemy.kernel.ComponentPort [$e3 newPort P7]]
    set p8 [java::cast ptolemy.kernel.ComponentPort [$e7 newPort P8]]
    set p9 [java::cast ptolemy.kernel.ComponentPort [$e8 newPort P9]]
    set p10 [java::cast ptolemy.kernel.ComponentPort [$e8 newPort P10]]
    set p11 [java::cast ptolemy.kernel.ComponentPort [$e7 newPort P11]]
    set p12 [java::cast ptolemy.kernel.ComponentPort [$e10 newPort P12]]
    set p13 [java::cast ptolemy.kernel.ComponentPort [$e10 newPort P13]]
    set p14 [java::cast ptolemy.kernel.ComponentPort [$e9 newPort P14]]

    # Create links
    set r1 [$e4 connect $p1 $p0 R1]
    set r2 [$e4 connect $p1 $p4 R2]
    $p3 link $r2
    set r3 [$e4 connect $p1 $p2 R3]
    set r4 [$e3 connect $p4 $p7 R4]
    set r5 [$e3 connect $p4 $p5 R5]
    $e3 allowLevelCrossingConnect true
    set r6 [$e3 connect $p3 $p6 R6]
    set r7 [$e0 connect $p7 $p13 R7]
    set r8 [$e7 connect $p9 $p8 R8]
    set r9 [$e7 connect $p10 $p11 R9]
    set r10 [$e0 connect $p8 $p12 R10]
    set r11 [$e10 connect $p12 $p13 R11]
    set r12 [$e10 connect $p14 $p13 R12]
    $p11 link $r7

    enumMethodToNames deepLinkedPorts $r1 $r2 $r3 $r4 $r5 $r6 $r7 $r8 $r9 \
            $r10 $r11 $r12
} {P1 {P1 P9 P14 P10 P5 P3} {P1 P2} {P1 P3 P9 P14 P10} {P1 P3 P5} {P3 P6} {P1 P3 P9 P14 P10} {P9 P1 P3 P10} {P10 P1 P3 P9 P14} {P9 P1 P3 P10} {P9 P1 P3 P10} {P14 P1 P3 P10}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.2 {Test linkedPorts on relations} {
    enumMethodToNames linkedPorts $r1 $r2 $r3 $r4 $r5 $r6 $r7 $r8 $r9 \
            $r10 $r11 $r12
} {{P1 P0} {P1 P4 P3} {P1 P2} {P4 P7} {P4 P5} {P3 P6} {P7 P13 P11} {P9 P8} {P10 P11} {P8 P12} {P12 P13} {P14 P13}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.3 {Test deepConnectedPorts on ports} {
    enumMethodToNames deepConnectedPorts $p0 $p1 $p2 $p3 $p4 $p5 $p6 \
            $p7 $p8 $p9 $p10 $p11 $p12 $p13 $p14
} {{} {P9 P14 P10 P5 P3 P2} P1 {P1 P9 P14 P10 P5 P6} {P9 P14 P10 P5} {P1 P3} P3 {P9 P14 P10} {P1 P3 P10} {P1 P3 P10} {P1 P3 P9 P14} {P1 P3 P9 P14} P9 {P1 P3 P10} {P1 P3 P10}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.4 {Test connectedPorts on ports} {
    enumMethodToNames connectedPorts $p0 $p1 $p2 $p3 $p4 $p5 $p6 \
            $p7 $p8 $p9 $p10 $p11 $p12 $p13 $p14
} {{} {P0 P4 P3 P2} P1 {P1 P4 P6} {P7 P5} P4 P3 {P13 P11} P12 P8 P11 {P7 P13} P8 {P7 P11} P13}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.5 {Test NAME and CLASS description} {
    $e0 description 3
} {ptolemy.kernel.CompositeEntity {.E0}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.6 {Test full description} {
    $e0 description [java::field ptolemy.kernel.util.NamedObj COMPLETE]
} {ptolemy.kernel.CompositeEntity {.E0} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0._iconDescription} attributes {
    }}
} ports {
} classes {
} entities {
    {ptolemy.kernel.CompositeEntity {.E0.E3} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3._iconDescription} attributes {
        }}
    } ports {
        {ptolemy.kernel.ComponentPort {.E0.E3.P7} attributes {
        } links {
            {ptolemy.kernel.ComponentRelation {.E0.R7} attributes {
            }}
        } insidelinks {
            {ptolemy.kernel.ComponentRelation {.E0.E3.R4} attributes {
            }}
        }}
    } classes {
    } entities {
        {ptolemy.kernel.CompositeEntity {.E0.E3.E4} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3.E4._iconDescription} attributes {
            }}
        } ports {
            {ptolemy.kernel.ComponentPort {.E0.E3.E4.P0} attributes {
            } links {
            } insidelinks {
                {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R1} attributes {
                }}
            }}
            {ptolemy.kernel.ComponentPort {.E0.E3.E4.P4} attributes {
            } links {
                {ptolemy.kernel.ComponentRelation {.E0.E3.R4} attributes {
                }}
                {ptolemy.kernel.ComponentRelation {.E0.E3.R5} attributes {
                }}
            } insidelinks {
                {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R2} attributes {
                }}
            }}
        } classes {
        } entities {
            {ptolemy.kernel.ComponentEntity {.E0.E3.E4.E1} attributes {
                {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3.E4.E1._iconDescription} attributes {
                }}
            } ports {
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E1.P1} attributes {
                } links {
                    {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R1} attributes {
                    }}
                    {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R2} attributes {
                    }}
                    {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R3} attributes {
                    }}
                } insidelinks {
                }}
            }}
            {ptolemy.kernel.ComponentEntity {.E0.E3.E4.E2} attributes {
                {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3.E4.E2._iconDescription} attributes {
                }}
            } ports {
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E2.P2} attributes {
                } links {
                    {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R3} attributes {
                    }}
                } insidelinks {
                }}
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E2.P3} attributes {
                } links {
                    {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R2} attributes {
                    }}
                    {ptolemy.kernel.ComponentRelation {.E0.E3.R6} attributes {
                    }}
                } insidelinks {
                }}
            }}
        } relations {
            {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R1} attributes {
            } links {
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E1.P1} attributes {
                }}
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.P0} attributes {
                }}
            }}
            {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R2} attributes {
            } links {
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E1.P1} attributes {
                }}
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.P4} attributes {
                }}
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E2.P3} attributes {
                }}
            }}
            {ptolemy.kernel.ComponentRelation {.E0.E3.E4.R3} attributes {
            } links {
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E1.P1} attributes {
                }}
                {ptolemy.kernel.ComponentPort {.E0.E3.E4.E2.P2} attributes {
                }}
            }}
        }}
        {ptolemy.kernel.ComponentEntity {.E0.E3.E5} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3.E5._iconDescription} attributes {
            }}
        } ports {
            {ptolemy.kernel.ComponentPort {.E0.E3.E5.P5} attributes {
            } links {
                {ptolemy.kernel.ComponentRelation {.E0.E3.R5} attributes {
                }}
            } insidelinks {
            }}
        }}
        {ptolemy.kernel.ComponentEntity {.E0.E3.E6} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E3.E6._iconDescription} attributes {
            }}
        } ports {
            {ptolemy.kernel.ComponentPort {.E0.E3.E6.P6} attributes {
            } links {
                {ptolemy.kernel.ComponentRelation {.E0.E3.R6} attributes {
                }}
            } insidelinks {
            }}
        }}
    } relations {
        {ptolemy.kernel.ComponentRelation {.E0.E3.R4} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E3.E4.P4} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E3.P7} attributes {
            }}
        }}
        {ptolemy.kernel.ComponentRelation {.E0.E3.R5} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E3.E4.P4} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E3.E5.P5} attributes {
            }}
        }}
        {ptolemy.kernel.ComponentRelation {.E0.E3.R6} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E3.E4.E2.P3} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E3.E6.P6} attributes {
            }}
        }}
    }}
    {ptolemy.kernel.CompositeEntity {.E0.E7} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E7._iconDescription} attributes {
        }}
    } ports {
        {ptolemy.kernel.ComponentPort {.E0.E7.P8} attributes {
        } links {
            {ptolemy.kernel.ComponentRelation {.E0.R10} attributes {
            }}
        } insidelinks {
            {ptolemy.kernel.ComponentRelation {.E0.E7.R8} attributes {
            }}
        }}
        {ptolemy.kernel.ComponentPort {.E0.E7.P11} attributes {
        } links {
            {ptolemy.kernel.ComponentRelation {.E0.R7} attributes {
            }}
        } insidelinks {
            {ptolemy.kernel.ComponentRelation {.E0.E7.R9} attributes {
            }}
        }}
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.E0.E7.E8} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E7.E8._iconDescription} attributes {
            }}
        } ports {
            {ptolemy.kernel.ComponentPort {.E0.E7.E8.P9} attributes {
            } links {
                {ptolemy.kernel.ComponentRelation {.E0.E7.R8} attributes {
                }}
            } insidelinks {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E7.E8.P10} attributes {
            } links {
                {ptolemy.kernel.ComponentRelation {.E0.E7.R9} attributes {
                }}
            } insidelinks {
            }}
        }}
    } relations {
        {ptolemy.kernel.ComponentRelation {.E0.E7.R8} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E7.E8.P9} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E7.P8} attributes {
            }}
        }}
        {ptolemy.kernel.ComponentRelation {.E0.E7.R9} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E7.E8.P10} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E7.P11} attributes {
            }}
        }}
    }}
    {ptolemy.kernel.CompositeEntity {.E0.E10} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E10._iconDescription} attributes {
        }}
    } ports {
        {ptolemy.kernel.ComponentPort {.E0.E10.P12} attributes {
        } links {
            {ptolemy.kernel.ComponentRelation {.E0.R10} attributes {
            }}
        } insidelinks {
            {ptolemy.kernel.ComponentRelation {.E0.E10.R11} attributes {
            }}
        }}
        {ptolemy.kernel.ComponentPort {.E0.E10.P13} attributes {
        } links {
            {ptolemy.kernel.ComponentRelation {.E0.R7} attributes {
            }}
        } insidelinks {
            {ptolemy.kernel.ComponentRelation {.E0.E10.R11} attributes {
            }}
            {ptolemy.kernel.ComponentRelation {.E0.E10.R12} attributes {
            }}
        }}
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.E0.E10.E9} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.E0.E10.E9._iconDescription} attributes {
            }}
        } ports {
            {ptolemy.kernel.ComponentPort {.E0.E10.E9.P14} attributes {
            } links {
                {ptolemy.kernel.ComponentRelation {.E0.E10.R12} attributes {
                }}
            } insidelinks {
            }}
        }}
    } relations {
        {ptolemy.kernel.ComponentRelation {.E0.E10.R11} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E10.P12} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E10.P13} attributes {
            }}
        }}
        {ptolemy.kernel.ComponentRelation {.E0.E10.R12} attributes {
        } links {
            {ptolemy.kernel.ComponentPort {.E0.E10.E9.P14} attributes {
            }}
            {ptolemy.kernel.ComponentPort {.E0.E10.P13} attributes {
            }}
        }}
    }}
} relations {
    {ptolemy.kernel.ComponentRelation {.E0.R7} attributes {
    } links {
        {ptolemy.kernel.ComponentPort {.E0.E3.P7} attributes {
        }}
        {ptolemy.kernel.ComponentPort {.E0.E10.P13} attributes {
        }}
        {ptolemy.kernel.ComponentPort {.E0.E7.P11} attributes {
        }}
    }}
    {ptolemy.kernel.ComponentRelation {.E0.R10} attributes {
    } links {
        {ptolemy.kernel.ComponentPort {.E0.E7.P8} attributes {
        }}
        {ptolemy.kernel.ComponentPort {.E0.E10.P12} attributes {
        }}
    }}
}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
# FIXME: description2TclBlend needs fixing.
# test CompositeEntity-11.7 {Generate a description, then regenerate it} {
#     set desc0 [description2TclBlend [$e0 description \
# 	    [java::field ptolemy.kernel.Nameable PRETTYPRINT]]]
#     eval $desc0
#
#     # Note that description2TclBlend uses the names of entities
#     # as variables, so what was $e0 in 11.1 is $E0
#     set desc1 [description2TclBlend [$E0 description \
# 	    [java::field ptolemy.kernel.Nameable PRETTYPRINT]]]
#     list [expr {"$desc0" != ""}] [expr {"$desc0" == "$desc1"}]
# } {1 1}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.8 {Test that clone fails with level-cross xsitions} {
    catch {set ne0 [$e0 clone]} msg
    list $msg
} {{java.lang.CloneNotSupportedException: Cannot clone a CompositeEntity with level crossing transitions.}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.9 {Test clone} {
    set ne7 [java::cast ptolemy.kernel.CompositeEntity [$e7 clone]]
    $ne7 description 31
} {ptolemy.kernel.CompositeEntity {.E7} ports {
    {ptolemy.kernel.ComponentPort {.E7.P8} links {
    } insidelinks {
        {ptolemy.kernel.ComponentRelation {.E7.R8}}
    }}
    {ptolemy.kernel.ComponentPort {.E7.P11} links {
    } insidelinks {
        {ptolemy.kernel.ComponentRelation {.E7.R9}}
    }}
} classes {
} entities {
    {ptolemy.kernel.ComponentEntity {.E7.E8} ports {
        {ptolemy.kernel.ComponentPort {.E7.E8.P9} links {
            {ptolemy.kernel.ComponentRelation {.E7.R8}}
        } insidelinks {
        }}
        {ptolemy.kernel.ComponentPort {.E7.E8.P10} links {
            {ptolemy.kernel.ComponentRelation {.E7.R9}}
        } insidelinks {
        }}
    }}
} relations {
    {ptolemy.kernel.ComponentRelation {.E7.R8} links {
        {ptolemy.kernel.ComponentPort {.E7.E8.P9}}
        {ptolemy.kernel.ComponentPort {.E7.P8}}
    }}
    {ptolemy.kernel.ComponentRelation {.E7.R9} links {
        {ptolemy.kernel.ComponentPort {.E7.E8.P10}}
        {ptolemy.kernel.ComponentPort {.E7.P11}}
    }}
}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.10 {Test numRelations} {
    list [$e0 numRelations] [$e3 numRelations] [$e4 numRelations] \
	    [$e7 numRelations] [$e10 numRelations]
} {2 3 3 2 2}

######################################################################
####
# Test connections.
test CompositeEntity-12.1 {Test connect} {
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new ptolemy.kernel.ComponentEntity $e0 E1]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e0 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set r1 [$e0 connect $p1 $p2]
    enumToNames [$r1 linkedPorts]
} {P1 P2}

######################################################################
####
# Test connections.
test CompositeEntity-12.2 {Test connect} {
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new ptolemy.kernel.ComponentEntity $e0 E1]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e0 E2]
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 P1]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 P2]
    set r1 [$e0 connect $p1 $p2 R1]
    enumToNames [[$e0 getRelation R1] linkedPorts ]
} {P1 P2}

######################################################################
####
#
test CompositeEntity-13.1 {test set name to itself} {
    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName A
    set b [java::new ptolemy.kernel.CompositeEntity $a B]
    $b setName B
    $b getFullName
} {.A.B}


######################################################################
####
#
test CompositeEntity-14.1 {Test uniqueName} {
    set a [java::new ptolemy.kernel.test.TestCompositeEntity]
    $a setName A
    set r1 [list [$a getFullName] [$a uniqueName _E]]
    java::new ptolemy.kernel.ComponentRelation $a _E
    set r2 [list [$a uniqueName _E] [$a getFullName]]
    list $r1 $r2
} {{.A _E} {_E2 .A}}

######################################################################
####
#
test CompositeEntity-15.0 {test getAttribute} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.kernel.util.Attribute $e2 "a"]
    set r [$e1 getAttribute e2.a]
    $r getFullName
} {.e1.e2.a}

test CompositeEntity-15.1 {test getAttribute} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set e2 [java::new ptolemy.kernel.ComponentRelation $e1 "e2"]
    set a [java::new ptolemy.kernel.util.Attribute $e2 "a"]
    set r [$e1 getAttribute e2.a]
    $r getFullName
} {.e1.e2.a}

######################################################################
####
#
test CompositeEntity-16.0 {test getRelation} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set e2 [java::new ptolemy.kernel.CompositeEntity $e1 "e2"]
    set a [java::new ptolemy.kernel.ComponentRelation $e2 "a"]
    set r [$e1 getRelation e2.a]
    $r getFullName
} {.e1.e2.a}

######################################################################
####
#
test CompositeEntity-17.0 {test getPort} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.kernel.ComponentPort $e2 "a"]
    set r [$e1 getPort e2.a]
    $r getFullName
} {.e1.e2.a}

######################################################################
####
#
test CompositeEntity-18.0 {test exportMoML} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "p1"]
    set a1 [java::new ptolemy.kernel.util.Attribute $e1 "a1"]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 "p2"]
    set a2 [java::new ptolemy.kernel.util.Attribute $e2 "a2"]
    set r [java::new ptolemy.kernel.ComponentRelation $e1 "r"]
    $p1 link $r
    $p2 link $r
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <property name="a1" class="ptolemy.kernel.util.Attribute">
    </property>
    <port name="p1" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        <property name="a2" class="ptolemy.kernel.util.Attribute">
        </property>
        <port name="p2" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p1" relation="r"/>
    <link port="e2.p2" relation="r"/>
</entity>
}

######################################################################
####
#
test CompositeEntity-20.0 {test exportMoML with link indexing, inside links} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "p1"]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 "p2"]
    set r [java::new ptolemy.kernel.ComponentRelation $e1 "r"]
    $p1 insertLink 0 $r
    $p2 insertLink 0 $r
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="p1" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        <port name="p2" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p1" relation="r"/>
    <link port="e2.p2" relation="r"/>
</entity>
}

######################################################################
####
#
test CompositeEntity-20.1 {test exportMoML with link indexing, inside links} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "p1"]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 "p2"]
    set r [java::new ptolemy.kernel.ComponentRelation $e1 "r"]
    $p1 insertLink 1 $r
    $p2 insertLink 1 $r
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="p1" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        <port name="p2" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p1" insertAt="1" relation="r"/>
    <link port="e2.p2" insertAt="1" relation="r"/>
</entity>
}

######################################################################
####
#
test CompositeEntity-20.2 {test exportMoML with link indexing, inside links} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "p1"]
    set r1 [java::new ptolemy.kernel.ComponentRelation $e1 "r1"]
    set r2 [java::new ptolemy.kernel.ComponentRelation $e1 "r2"]
    $p1 insertLink 1 $r1
    $p1 insertLink 0 $r2
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="p1" class="ptolemy.kernel.ComponentPort">
    </port>
    <relation name="r1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="r2" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p1" relation="r2"/>
    <link port="p1" insertAt="2" relation="r1"/>
</entity>
}

######################################################################
####
#
test CompositeEntity-20.3 {test exportMoML with link indexing, inside links} {
    # NOTE: This builds on the previous test.
    $p1 {unlinkInside int} 0
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="p1" class="ptolemy.kernel.ComponentPort">
    </port>
    <relation name="r1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="r2" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p1" insertAt="1" relation="r1"/>
</entity>
}

######################################################################
####
#
test CompositeEntity-20.4 {test unlinkInside by relation, inside links} {
    # NOTE: This builds on the previous test.
    $p1 unlinkInside $r1
    $e1 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="e1" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.0-devel">
    </property>
    <port name="p1" class="ptolemy.kernel.ComponentPort">
    </port>
    <relation name="r1" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="r2" class="ptolemy.kernel.ComponentRelation">
    </relation>
</entity>
}

######################################################################
####
#
test CompositeEntity-21.0 {test exportLinks with filtering} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "p1"]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 "p2"]
    set r [java::new ptolemy.kernel.ComponentRelation $e1 "r"]
    $p1 insertLink 0 $r
    $p2 insertLink 0 $r
    $e1 exportLinks 0 [java::null]
} {<link port="p1" relation="r"/>
<link port="e2.p2" relation="r"/>
}

######################################################################
####
#
test CompositeEntity-21.1 {test exportLinks with filtering} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity $w]
    $e1 setName "e1"
    set p1 [java::new ptolemy.kernel.ComponentPort $e1 "p1"]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set p2 [java::new ptolemy.kernel.ComponentPort $e2 "p2"]
    set r [java::new ptolemy.kernel.ComponentRelation $e1 "r"]
    $p1 insertLink 0 $r
    $p2 insertLink 0 $r
    $e1 exportLinks 0 [java::null]
} {<link port="p1" relation="r"/>
<link port="e2.p2" relation="r"/>
}

######################################################################
####
#
test CompositeEntity-21.2 {test exportLinks with filtering} {
    set filter [java::new java.util.HashSet]
    $filter add $p1
    $filter add $r
    $e1 exportLinks 0 $filter
} {<link port="p1" relation="r"/>
}

######################################################################
####
#
test CompositeEntity-21.3 {test exportLinks with filtering} {
    set filter [java::new java.util.HashSet]
    $filter add $p2
    $filter add $r
    $e1 exportLinks 0 $filter
} {<link port="e2.p2" relation="r"/>
}

######################################################################
####
#
test CompositeEntity-21.4 {test exportLinks with filtering} {
    set filter [java::new java.util.HashSet]
    $filter add $e2
    $filter add $r
    $e1 exportLinks 0 $filter
} {<link port="e2.p2" relation="r"/>
}

######################################################################
####
#
test CompositeEntity-21.5 {test exportLinks with filtering} {
    set filter [java::new java.util.HashSet]
    $e1 exportLinks 0 $filter
} {}
