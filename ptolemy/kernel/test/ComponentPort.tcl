# Tests for the ComponentPort class
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

# Load up Tcl procs to print out enums
if {[info procs _testEnums] == "" } then { 
    source testEnums.tcl
}

if {[info procs enumToFullNames] == "" } then { 
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
test ComponentPort-1.1 {Get information about an instance of ComponentPort} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.ComponentPort]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.ComponentPort
  fields:        
  methods:       {_checkRelation pt.kernel.Relation} {_outside pt.kernel
    .Nameable} deepGetConnectedPorts deepGetInsidePorts {de
    scription int} {equals java.lang.Object} getClass getCo
    nnectedPorts getContainer getFullName getInsidePorts ge
    tInsideRelations getLinkedRelations getName hashCode {l
    iberalLink pt.kernel.Relation} {link pt.kernel.Relation
    } notify notifyAll numInsideLinks numLinks {setContaine
    r pt.kernel.Entity} {setName java.lang.String} toString
     {unlink pt.kernel.Relation} unlinkAll wait {wait long}
     {wait long int} workspace
    
  constructors:  pt.kernel.ComponentPort {pt.kernel.ComponentPort pt.ker
    nel.ComponentEntity java.lang.String} {pt.kernel.Compon
    entPort pt.kernel.Workspace}
    
  properties:    class connectedPorts container fullName insidePorts ins
    ideRelations linkedRelations name
    
  superclass:    pt.kernel.Port
    
}}

######################################################################
####
# 
test ComponentPort-2.1 {Construct Ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    set p1 [java::new pt.kernel.ComponentPort]
    set p2 [java::new pt.kernel.ComponentPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..P2}

######################################################################
####
# 
test ComponentPort-2.2 {Construct Ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {.E1.P1 .E1.P2}

######################################################################
####
# 
test ComponentPort-2.3 {Check getInsidePorts on opaque ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    enumToFullNames [$p1 getInsidePorts]
} {}

######################################################################
####
# 
test ComponentPort-2.3 {Check deepGetInsidePorts on opaque ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    enumToFullNames [$p1 deepGetInsidePorts]
} {.E1.P1}

######################################################################
####
# 
test ComponentPort-3.1 {Make transparent port} {
    set e1 [java::new pt.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set a1 [java::new pt.kernel.ComponentRelation $e1 A1]
    $p2 link $a1
    $p1 link $a1
    enumToFullNames [$a1 getLinkedPorts]
} {.E1.E2.P2 .E1.P1}

######################################################################
####
# 
test ComponentPort-3.2 {Make multiple aliases and test deepGetInsidePorts} {
    set e1 [java::new pt.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e2 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set p3 [java::new pt.kernel.ComponentPort $e1 P3]
    set a1 [java::new pt.kernel.ComponentRelation $e1 A1]
    $p1 link $a1
    $p2 link $a1
    $p3 link $a1
    list [enumToFullNames [$p3 deepGetInsidePorts]] \
            [enumToFullNames [$p3 getInsidePorts]]
} {{.E1.E2.P1 .E1.E2.P2} {.E1.E2.P1 .E1.E2.P2}}

######################################################################
####
# 
test ComponentPort-3.3 {test getConnectedPorts} {
    set e1 [java::new pt.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e2 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set p3 [java::new pt.kernel.ComponentPort $e1 P3]
    set a1 [java::new pt.kernel.ComponentRelation $e1 A1]
    $p1 link $a1
    $p2 link $a1
    $p3 link $a1
    list [enumToNames [$p3 getConnectedPorts]] \
            [enumToNames [$p2 getConnectedPorts]] \
            [enumToNames [$p1 getConnectedPorts]]
} {{} {P1 P3} {P2 P3}}

######################################################################
####
# 
test ComponentPort-3.4 {Level-crossing link error} {
    set e1 [java::new pt.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new pt.kernel.CompositeEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set a1 [java::new pt.kernel.ComponentRelation $e2 A1]
    catch {$p1 link $a1} msg
    list $msg
} {{pt.kernel.IllegalActionException: .E1.P1 and .E1.E2.A1: Link crosses levels of the hierarchy}}

######################################################################
####
# 
test ComponentPort-3.5 {Level-crossing link} {
    set e1 [java::new pt.kernel.CompositeEntity]
    $e1 setName E1
    set e2 [java::new pt.kernel.CompositeEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set a1 [java::new pt.kernel.ComponentRelation $e2 A1]
    $p1 liberalLink $a1
    enumToNames [$a1 getLinkedPorts]
} {P1}

######################################################################
####
# 
test ComponentPort-3.6 {Construct aliases, then modify them} {
    set e0 [java::new pt.kernel.CompositeEntity]
    $e1 setName E0
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set e4 [java::new pt.kernel.ComponentEntity $e0 E4]

    set p1 [java::new pt.kernel.ComponentPort $e0 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set p3 [java::new pt.kernel.ComponentPort $e0 P3]
    set p4 [java::new pt.kernel.ComponentPort $e4 P4]

    set a1 [java::new pt.kernel.ComponentRelation $e0 A1]
    set a2 [java::new pt.kernel.ComponentRelation $e0 A2]

    $p2 link $a1
    $p4 link $a2
    $p1 link $a1
    $p3 link $a2

    set result {}
    foreach obj [list $p1 $p2 $p3 $p4] {
        lappend result [enumToNames [$obj getInsidePorts]]
    }
    foreach obj [list $a1 $a2] {
        lappend result [enumToNames [$obj getLinkedPorts]]
    }

    # Now the modification
    $p4 unlink $a2
    $p2 link $a2
    $p3 link $a1

    foreach obj [list $p1 $p2 $p3 $p4] {
        lappend result [enumToNames [$obj getInsidePorts]]
    }
    foreach obj [list $a1 $a2] {
        lappend result [enumToNames [$obj getLinkedPorts]]
    }
    list $result
} {{P2 {} P4 {} {P2 P1} {P4 P3} {P2 P3} {} {P2 P2 P1} {} {P2 P1 P3} {P3 P2}}}

######################################################################
#### 
#
test ComponentPort-4.1 {Cross Level Link} {
    # Create objects
    set e0 [java::new pt.kernel.CompositeEntity]
    $e1 setName E0
    set e1 [java::new pt.kernel.CompositeEntity $e0 "E1"]
    set e2 [java::new pt.kernel.ComponentEntity $e1 "E2"]
    set e3 [java::new pt.kernel.ComponentEntity $e0 "E3"]
    set p1 [java::new pt.kernel.ComponentPort $e2 "P1"]
    set p2 [java::new pt.kernel.ComponentPort $e3 "P2"]
    set r1 [java::new pt.kernel.ComponentRelation $e0 "R1"]

    # Connect
    $p1 liberalLink $r1
    $p2 link $r1

    list [enumToNames [$p1 getLinkedRelations]] \
            [enumToNames [$p2 getLinkedRelations]]
} {R1 R1}

######################################################################
#### 
# Example from figure of design document.
test ComponentPort-5.1 {Transparent entity} {
    # Create objects
    set e0 [java::new pt.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new pt.kernel.ComponentEntity $e0 "E1"]
    set e2 [java::new pt.kernel.CompositeEntity $e0 "E2"]
    set e3 [java::new pt.kernel.ComponentEntity $e2 "E3"]
    set e4 [java::new pt.kernel.ComponentEntity $e0 "E4"]
    set p1 [java::new pt.kernel.ComponentPort $e1 "P1"]
    set p2 [java::new pt.kernel.ComponentPort $e2 "P2"]
    set p3 [java::new pt.kernel.ComponentPort $e3 "P3"]
    set p4 [java::new pt.kernel.ComponentPort $e2 "P4"]
    set p5 [java::new pt.kernel.ComponentPort $e4 "P5"]
    set r1 [java::new pt.kernel.ComponentRelation $e0 "R1"]
    set r2 [java::new pt.kernel.ComponentRelation $e2 "R2"]
    set r3 [java::new pt.kernel.ComponentRelation $e0 "R3"]

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    $p4 link $r2
    $p4 link $r3
    $p5 link $r3

    list [enumToNames [$p1 deepGetConnectedPorts]] \
            [enumToNames [$p2 deepGetConnectedPorts]] \
            [enumToNames [$p3 deepGetConnectedPorts]] \
            [enumToNames [$p4 deepGetConnectedPorts]] \
            [enumToNames [$p5 deepGetConnectedPorts]]
} {{P3 P5} P1 {P1 P5} P5 {P1 P3}}

######################################################################
#### 
# NOTE: Uses topology built in 5.1
test ComponentPort-5.2 {numInsideLinks} {
    list [$p1 numInsideLinks] \
            [$p2 numInsideLinks] \
            [$p3 numInsideLinks] \
            [$p4 numInsideLinks] \
            [$p5 numInsideLinks]
} {0 1 0 1 0}

######################################################################
#### 
# NOTE: Uses topology built in 5.1
test ComponentPort-5.3 {unlinkAll} {
    $p2 unlinkAll
    list [enumToNames [$p1 deepGetConnectedPorts]] \
            [enumToNames [$p2 deepGetConnectedPorts]] \
            [enumToNames [$p3 deepGetConnectedPorts]] \
            [enumToNames [$p4 deepGetConnectedPorts]] \
            [enumToNames [$p5 deepGetConnectedPorts]]
} {{} {} P5 P5 P3}
