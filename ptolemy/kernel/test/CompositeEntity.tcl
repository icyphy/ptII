# Tests for the CompositeEntity class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

if {[info procs enumToObjects] == "" } then { 
     source enums.tcl
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
test CompositeEntity-1.1 {Get information about an instance \
	of CompositeEntity} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.CompositeEntity]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.CompositeEntity
  fields:        
  methods:       {addEntity pt.kernel.ComponentEntity} {addPort pt.kerne
    l.Port} {addRelation pt.kernel.ComponentRelation} {alia
    s pt.kernel.ComponentPort pt.kernel.ComponentPort} {ali
    as pt.kernel.ComponentPort pt.kernel.ComponentPort java
    .lang.String} {connect pt.kernel.ComponentPort pt.kerne
    l.ComponentPort} {connect pt.kernel.ComponentPort pt.ke
    rnel.ComponentPort java.lang.String} {deepContains pt.k
    ernel.ComponentEntity} deepGetEntities {equals java.lan
    g.Object} getClass getConnectedEntities {getConnectedEn
    tities java.lang.String} {getConnectedEntities pt.kerne
    l.Port} getContainer getEntities {getEntity java.lang.S
    tring} getFullName getLinkedRelations {getLinkedRelatio
    ns java.lang.String} {getLinkedRelations pt.kernel.Port
    } getName {getPort java.lang.String} getPorts {getRelat
    ion java.lang.String} getRelations hashCode isAtomic {n
    ewPort java.lang.String} {newRelation java.lang.String}
     notify notifyAll numEntities numRelations removeAllEnt
    ities removeAllPorts removeAllRelations {removeEntity j
    ava.lang.String} {removeEntity pt.kernel.ComponentEntit
    y} {removePort java.lang.String} {removePort pt.kernel.
    Port} {removeRelation java.lang.String} {removeRelation
     pt.kernel.ComponentRelation} {setContainer pt.kernel.C
    ompositeEntity} {setName java.lang.String} toString wai
    t {wait long} {wait long int}
    
  constructors:  pt.kernel.CompositeEntity {pt.kernel.CompositeEntity ja
    va.lang.String} {pt.kernel.CompositeEntity pt.kernel.Co
    mpositeEntity java.lang.String}
    
  properties:    atomic class connectedEntities container entities fullN
    ame linkedRelations name ports relations
    
  superclass:    pt.kernel.ComponentEntity
    
}}

######################################################################
####
# 
test CompositeEntity-1.1 {Construct CompositeEntities, call a few methods} {
    set e1 [java::new pt.kernel.CompositeEntity]
    set e2 [java::new pt.kernel.CompositeEntity A]
    set e1contents [$e1 getEntities]
    list [$e1 getName] [$e2 getName] \
	    [$e1 getFullName] [$e2 getFullName] \
	    [$e1 isAtomic] [$e2 isAtomic] \
	    [ java::instanceof $e1contents java.util.Enumeration] \
	    [expr {[java::null] == [$e1 getContainer]}]
} {{} A {} A 0 0 1 1}

######################################################################
####
# 
test CompositeEntity-2.1 {Create a 3 level deep tree using constructors} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {{B C} {} D}

######################################################################
####
# 
test CompositeEntity-2.2 {Create a 3 level deep tree after construction} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity B]
    set c [java::new pt.kernel.CompositeEntity C]
    set d [java::new pt.kernel.ComponentEntity D]
    $c addEntity $d
    $a addEntity $b
    $b addEntity $c
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {B C D}

######################################################################
####
# 
test CompositeEntity-3.1 {Test deepGetEntities} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [enumToNames [$a deepGetEntities]] \
            [enumToNames [$c deepGetEntities]] \
} {{B D} D}

######################################################################
####
# 
test CompositeEntity-3.2 {Test numEntities} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [$a numEntities] [$b numEntities] [$c numEntities]
} {2 0 1}

######################################################################
####
# 
test CompositeEntity-3.3 {Test getEntity by name} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    set e [$c getEntity D]
    $e getFullName
} {A.B.C.D}

######################################################################
####
# 
test CompositeEntity-4.1 {Test deepContains} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [$a deepContains $d] [$a deepContains $a] [$c deepContains $a] \
            [$c deepContains $d] [$b deepContains $d]
} {1 0 0 1 0}

######################################################################
####
# 
test CompositeEntity-5.1 {Test reparenting} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    $c setContainer $b
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {B C D}

######################################################################
####
# 
test CompositeEntity-5.2 {Test reparenting with an error} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    catch {$c setContainer $d} msg
    list $msg
} {{pt.kernel.IllegalActionException: A.C and A.C.D: Attempt to construct recursive containment.}}

######################################################################
####
# 
test CompositeEntity-6.1 {Test removing entities} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    $a {removeEntity pt.kernel.ComponentEntity} $b
    $c {removeEntity pt.kernel.ComponentEntity} $d
    $b {removeEntity pt.kernel.ComponentEntity} $c
    enumMethodToNames getEntities $a $b $c $d
} {{} {} {} {}}

######################################################################
####
# 
test CompositeEntity-6.2 {Test removing entities with an error} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    catch {$a {removeEntity pt.kernel.ComponentEntity} $d} msg
    list $msg
} {{pt.kernel.IllegalActionException: A and A.B.C.D: Attempt to remove an entity from a container that does not contain it.}}

######################################################################
####
# 
test CompositeEntity-6.3 {Test removing entities by name} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    $a {removeEntity String} B
    enumMethodToNames getEntities $a $b $c $d
} {{} C D {}}

######################################################################
####
# 
test CompositeEntity-6.4 {Test removing entities by name with an error} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    catch {$a {removeEntity String} D} msg
    list $msg
} {{pt.kernel.NoSuchItemException: A: Attempt to remove a nonexistent entity: D}}

######################################################################
####
# 
test CompositeEntity-7.1 {Add relations} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
# 
test CompositeEntity-7.2 {Add relations after creation} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation R1]
    set r2 [java::new pt.kernel.ComponentRelation R2]
    $a addRelation $r1
    $a addRelation $r2
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
# 
test CompositeEntity-7.3 {Get relations by name} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    set r [$a getRelation R1]
    $r getFullName
} {A.R1}

######################################################################
####
# 
test CompositeEntity-7.4 {Add relations using newRelation} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [$a newRelation R1]
    set r2 [$a newRelation R2]
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
# 
test CompositeEntity-8.1 {Remove relations} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    $a {removeRelation pt.kernel.ComponentRelation} $r1
    enumToNames [$a getRelations]
} {R2}

######################################################################
####
# 
test CompositeEntity-8.2 {Remove relations with an error} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    catch {$a {removeRelation pt.kernel.ComponentRelation} $r1} msg
    list $msg
} {{pt.kernel.IllegalActionException: A and R1: Attempt to remove a relation from a container that does not contain it.}}

######################################################################
####
# 
test CompositeEntity-8.3 {Remove relations by name} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    $a {removeRelation String} R2
    enumToNames [$a getRelations]
} {R1}

######################################################################
####
# 
test CompositeEntity-8.4 {Remove relations by name with an error} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    catch {$a {removeRelation String} R1} msg
    list $msg
} {{pt.kernel.NoSuchItemException: A: Attempt to remove a nonexistent relation: R1}}

######################################################################
####
# 
test CompositeEntity-8.5 {Test removing all entities} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.CompositeEntity $a D]
    $a removeAllEntities
    enumToNames [$a getEntities]
} {}

######################################################################
####
# 
test CompositeEntity-8.6 {Remove all relations} {
    set a [java::new pt.kernel.CompositeEntity A]
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    $a removeAllRelations
    enumToNames [$a getRelations]
} {}

######################################################################
####
# 
test CompositeEntity-9.1 {Test alias} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.CompositeEntity $a B]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    $a alias $p2 $p1
    set ar [$p1 getDownAlias]
    enumToFullNames [$ar getLinkedPorts]
} {A.B.P2}

######################################################################
####
# 
test CompositeEntity-10.1 {Test multiple alias relation naming} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    set ar1 [$a alias $p2 $p1]
    set ar2 [$a alias $p3 $p4]
    list [$ar1 getFullName] [$ar2 getFullName]
} {A._R0 A._R1}

######################################################################
####
# 
test CompositeEntity-10.2 {Test multiple alias relation naming} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    $a alias $p2 $p1
    $a alias $p3 $p4
    set ar1 [$p1 getDownAlias]
    set ar2 [$p4 getDownAlias]
    list [$ar1 getFullName] [$ar2 getFullName]
} {A._R0 A._R1}

######################################################################
####
# 
test CompositeEntity-10.3 {Create and then remove an alias} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    set ar1 [$a alias $p2 $p1]
    set ar2 [$a alias $p3 $p4]
    set result {}
    lappend result [$ar1 getFullName] [$ar2 getFullName]
    $a {removeRelation pt.kernel.ComponentRelation} $ar2
    lappend result [expr { [$p4 getDownAlias] == [java::null] } ]
} {A._R0 A._R1 1}

######################################################################
####
# 
test CompositeEntity-10.4 {Create and then remove aliases with given names} {
    set a [java::new pt.kernel.CompositeEntity A]
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    set ar1 [$a alias $p2 $p1 AR1]
    set ar2 [$a alias $p3 $p4 AR2]
    set result {}
    lappend result [$ar1 getFullName] [$ar2 getFullName]
    $a {removeRelation pt.kernel.ComponentRelation} $ar2
    lappend result [expr { [$p4 getDownAlias] == [java::null] } ]
} {A.AR1 A.AR2 1}

######################################################################
####
# NOTE:  The setup constructed in this test is used in the subsequent
# tests.
test CompositeEntity-11.1 {Test deepGetLinkedEntities on component relations} {
    # This structure is the example in the kernel design document.

    # Create composite entities
    set e0 [java::new pt.kernel.CompositeEntity E0]
    set e4 [java::new pt.kernel.CompositeEntity $e0 E4]
    set e7 [java::new pt.kernel.CompositeEntity $e0 E7]
    set e3 [java::new pt.kernel.CompositeEntity $e4 E3]

    # Create component entities.
    set e1 [java::new pt.kernel.ComponentEntity $e3 E1]
    set e2 [java::new pt.kernel.ComponentEntity $e3 E2]
    set e5 [java::new pt.kernel.ComponentEntity $e4 E5]
    set e6 [java::new pt.kernel.ComponentEntity $e4 E6]
    set e8 [java::new pt.kernel.ComponentEntity $e7 E8]

    # Create ports.
    set p1 [$e1 newPort P1]
    set p2 [$e2 newPort P2]
    set p3 [$e3 newPort P3]
    set p4 [$e4 newPort P4]
    set p5 [$e5 newPort P5]
    set p6 [$e6 newPort P6]
    set p7 [$e7 newPort P7]
    set p8 [$e8 newPort P8]
    set p9 [$e8 newPort P9]
    set p10 [$e3 newPort P10]
    set p11 [$e7 newPort P11]
    set p12 [$e2 newPort P12]

    # Create links
    set a1 [$e3 alias $p1 $p10 A1]
    set a2 [$e3 alias $p1 $p3 A2]
    set r1 [$e3 connect $p1 $p2 R1]
    set a3 [$e4 alias $p3 $p4 A3]
    set r2 [$e4 connect $p3 $p5 R2]
    set r3 [$e4 connect $p3 $p6 R3]
    set r4 [$e0 connect $p4 $p7 R4]
    set a4 [$e7 alias $p8 $p7 A4]
    set a5 [$e7 alias $p9 $p11 A5]
    $p11 link $r4
    $p12 link $a2

    enumMethodToNames deepGetLinkedEntities $r1 $r2 $r3 $r4
} {{E1 E2} {E1 E2 E5} {E1 E2 E6} {E1 E2 E8 E8}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.2 {Test deepGetLinkedEntities on alias relations} {
    enumMethodToNames deepGetLinkedEntities $a1 $a2 $a3 $a4 $a5
} {E1 {E1 E2} {E1 E2} E8 E8}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.3 {Test deepGetLinkedPorts on normal relations} {
    enumMethodToNames deepGetLinkedPorts $r1 $r2 $r3 $r4
} {{P1 P2} {P1 P12 P5} {P1 P12 P6} {P1 P12 P8 P9}}

# FIXME: test deepGetLinkedPortsExcept

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.5 {Test deepGetLinkedRelations on ports} {
    enumMethodToNames deepGetLinkedRelations $p1 $p2 $p3 $p4 $p5 $p6 $p7 $p8 $p9 $p10 $p11
} {{R4 R2 R3 R1} R1 {R4 R2 R3} R4 R2 R3 R4 R4 R4 {} R4}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.6 {Test deepGetConnectedPorts on ports} {
    enumMethodToNames deepGetConnectedPorts $p1 $p2 $p3 $p4 $p5 $p6 $p7 $p8 $p9 $p10 $p11 $p12
} {{P12 P8 P9 P5 P6 P2} P1 {P8 P9 P5 P6} {P8 P9} {P1 P12} {P1 P12} {P1 P12 P9} {P1 P12 P9} {P1 P12 P8} {} {P1 P12 P8} {P1 P8 P9 P5 P6}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.7 {Test deepGetConnectedEntities on ports} {
    enumMethodToNames deepGetConnectedEntities $p1 $p2 $p3 $p4 $p5 $p6 $p7 $p8 $p9 $p10 $p11 $p12
} {{E2 E8 E8 E5 E6 E2} E1 {E8 E8 E5 E6} {E8 E8} {E1 E2} {E1 E2} {E1 E2 E8} {E1 E2 E8} {E1 E2 E8} {} {E1 E2 E8} {E1 E8 E8 E5 E6}}


######################################################################
####
# Test connections.
test CompositeEntity-12.1 {Test connect} {
    set e0 [java::new pt.kernel.CompositeEntity E0]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set r1 [$e0 connect $p1 $p2]
    enumToNames [$r1 getLinkedPorts]
} {P1 P2}

######################################################################
####
# Test connections.
test CompositeEntity-12.2 {Test connect} {
    set e0 [java::new pt.kernel.CompositeEntity E0]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set r1 [$e0 connect $p1 $p2 R1]
    enumToNames [[$e0 getRelation R1] getLinkedPorts ]
} {P1 P2}

