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
  methods:       {_checkRelation pt.kernel.Relation} deepGetConnectedEnt
    ities deepGetConnectedPorts deepGetDownPorts deepGetLin
    kedRelations {equals java.lang.Object} getClass getCont
    ainer getDownAlias getFullName getLinkedRelations getNa
    me hashCode {link pt.kernel.Relation} notify notifyAll 
    numLinks {setContainer pt.kernel.Entity} {setDownAlias 
    pt.kernel.AliasRelation} {setName java.lang.String} toS
    tring {unlink pt.kernel.Relation} unlinkAll wait {wait 
    long} {wait long int}
    
  constructors:  pt.kernel.ComponentPort {pt.kernel.ComponentPort pt.ker
    nel.ComponentEntity java.lang.String}
    
  properties:    class container downAlias fullName linkedRelations name
    
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
} {{} .P2}

######################################################################
####
# 
test ComponentPort-2.2 {Construct Ports} {
    set e1 [java::new pt.kernel.ComponentEntity E1]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {E1.P1 E1.P2}

######################################################################
####
# 
test ComponentPort-3.1 {Construct aliases} {
    set e1 [java::new pt.kernel.CompositeEntity E1]
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set a1 [java::new pt.kernel.AliasRelation $e1 A1]
    $p2 link $a1
    enumToFullNames [$a1 getLinkedPorts]
} {E1.E2.P2}

######################################################################
####
# 
test ComponentPort-3.2 {Make multiple aliases and test deepGetDownPorts} {
    set e1 [java::new pt.kernel.CompositeEntity E1]
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e2 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set p3 [java::new pt.kernel.ComponentPort $e1 P3]
    set a1 [java::new pt.kernel.AliasRelation $e1 A1]
    $p1 link $a1
    $p2 link $a1
    $a1 setUpAlias $p3
    enumToFullNames [$p3 deepGetDownPorts]
} {E1.E2.P1 E1.E2.P2}

######################################################################
####
# 
test ComponentPort-3.3 {test getDownAlias of port and getUpAlias of relation} {
    set e1 [java::new pt.kernel.CompositeEntity E1]
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set a1 [java::new pt.kernel.AliasRelation $e1 A1]
    $p2 link $a1
    $a1 setUpAlias $p1
    list [[$p1 getDownAlias] getFullName] [[$a1 getUpAlias] getFullName]
} {E1.A1 E1.P1}

######################################################################
####
# 
test ComponentPort-3.4 {Construct aliases in the wrong direction} {
    set e1 [java::new pt.kernel.CompositeEntity E1]
    set e2 [java::new pt.kernel.ComponentEntity $e1 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set a1 [java::new pt.kernel.AliasRelation $e1 A1]
    catch {$p1 link $a1} msg
    list $msg
} {{pt.kernel.IllegalActionException: E1.P1 and E1.A1: Link crosses levels of the hierarchy}}

######################################################################
####
#
test ComponentPort-3.5 {Construct aliases, with level error} {
    set e1 [java::new pt.kernel.CompositeEntity E1]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e1 P2]
    set a1 [java::new pt.kernel.AliasRelation $e1 A1]
    catch {$p1 link $a1} msg
    list $msg
} {{pt.kernel.IllegalActionException: E1.P1 and E1.A1: Link crosses levels of the hierarchy}}

######################################################################
####
# 
test ComponentPort-3.6 {Construct aliases, then modify them} {
    set e0 [java::new pt.kernel.CompositeEntity E0]
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set e4 [java::new pt.kernel.ComponentEntity $e0 E4]

    set p1 [java::new pt.kernel.ComponentPort $e0 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set p3 [java::new pt.kernel.ComponentPort $e0 P3]
    set p4 [java::new pt.kernel.ComponentPort $e4 P4]

    set a1 [java::new pt.kernel.AliasRelation $e0 A1]
    set a2 [java::new pt.kernel.AliasRelation $e0 A2]

    $p2 link $a1
    $p4 link $a2
    $p1 setDownAlias $a1
    $p3 setDownAlias $a2

    set result {}
    foreach obj [list $p1 $p2 $p3 $p4] {
        set dp [$obj getDownAlias]
        if {$dp != [java::null]} {
            lappend result [$dp getFullName]
        } else {
            lappend result {}
        }
    }
    foreach obj [list $a1 $a2] {
        lappend result [enumToFullNames [$obj getLinkedPorts]]
    }

    # Now the modification
    $p4 unlink $a2
    $p2 link $a2
    $p3 setDownAlias $a1

    foreach obj [list $p1 $p2 $p3 $p4] {
        set dp [$obj getDownAlias]
        if {$dp != [java::null]} {
            lappend result [$dp getFullName]
        } else {
            lappend result {}
        }
    }
    foreach obj [list $a1 $a2] {
        lappend result [enumToFullNames [$obj getLinkedPorts]]
    }
    list $result
} {{E0.A1 {} E0.A2 {} E0.E2.P2 E0.E4.P4 {} {} E0.A1 {} E0.E2.P2 E0.E2.P2}}
