# Tests for the Entity class
#
# @Author: Christopher Hylands
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
if {[info procs _testEntityGetLinkedRelations] == "" } then { 
    source testEnums.tcl
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
test Entity-1.1 {Get information about an instance of Entity} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.Entity]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.Entity
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait getContainer getFullName getName {setName java.lang.String} {addPort pt.kernel.Port} getConnectedEntities {getConnectedEntities pt.kernel.Port} {getConnectedEntities java.lang.String} getLinkedRelations {getLinkedRelations pt.kernel.Port} {getLinkedRelations java.lang.String} {getPort java.lang.String} getPorts {newPort java.lang.String} removeAllPorts {removePort pt.kernel.Port} {removePort java.lang.String}
  constructors:  pt.kernel.Entity {pt.kernel.Entity java.lang.String}
  properties:    fullName connectedEntities class ports name container linkedRelations
  superclass:    pt.kernel.NamedObj
}}


######################################################################
####
# 
test Entity-2.1 {Construct Entities} {
    set e1 [java::new pt.kernel.Entity]
    set e2 [java::new pt.kernel.Entity "My Entity"]
    list [$e1 getName] [$e2 getName] 
} {{} {My Entity}}

######################################################################
####
# 
test Entity-2.2 {Construct Entities, call getPorts} {
    set e1 [java::new pt.kernel.Entity]
    set e2 [java::new pt.kernel.Entity "My Entity"]
    list [java::instanceof [$e1 getPorts] java.util.Enumeration]
} {1}

######################################################################
####
# 
test Entity-3.1 {Test getConnectedEntities} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    set enum [$e1 getConnectedEntities]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Entity-3.2 {Test getConnectedEntities on an invalid port name} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    catch {set enum [$e1 {getConnectedEntities String} "Not a Port"]} errmsg
    list $errmsg
} {{pt.kernel.NoSuchItemException: My Entity: Attempt to get the connected entities of a non-existent port: Not a Port}}

######################################################################
####
# 
test Entity-3.3 {Test getLinkedRelations when there are none} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    set enum [$e1 getLinkedRelations]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Entity-3.4 {Test getLinkedRelations on an invalid port name} {
    set e1 [java::new pt.kernel.Entity "My Entity"]
    catch {set enum [$e1 {getLinkedRelations String} "Not a PortName"]} errmsg
    list $errmsg
} {{pt.kernel.NoSuchItemException: My Entity: Attempt to get the connected entities of a non-existent port: Not a PortName}}

######################################################################
####
# 
test Entity-4.0 {Connect Entities} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    _testEntityGetLinkedRelations $ramp
} {Arc}

######################################################################
####
# 
test Entity-5.0 {add ports after construction} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    list [_testEntityGetPorts $ramp] \
            [[$a getContainer] getName] \
            [[$b getContainer] getName]
} {{{a b}} Ramp Ramp}

######################################################################
####
# 
test Entity-5.1 {add port without a name after construction} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    _testEntityGetPorts $ramp
} {{{} b}}

######################################################################
####
# 
test Entity-5.2 {add port without a name twice after construction} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    catch {$ramp addPort $a} msg
    list $msg
} {{pt.kernel.NameDuplicationException: Attempt to insert object named "<Unnamed Object>" into container named "Ramp", which already contains an object with that name.}}

######################################################################
####
# 
test Entity-5.3 {add port with a name twice after construction} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    catch {$ramp addPort $b} msg
    list $msg
} {{pt.kernel.NameDuplicationException: Attempt to insert object named "b" into container named "Ramp", which already contains an object with that name.}}

######################################################################
####
# 
test Entity-6.0 {remove port by name} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    $ramp {removePort String} a
    list [_testEntityGetPorts $ramp] \
            [expr { [$a getContainer] == [java::null] }] \
            [[$b getContainer] getName]
} {b 1 Ramp}

######################################################################
####
# 
test Entity-6.1 {remove port twice by name} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    $ramp {removePort String} a
    catch {$ramp {removePort String} a} msg
    list $msg
} {{pt.kernel.NoSuchItemException: Ramp: Attempt to remove a nonexistent port: a}}

######################################################################
####
# 
test Entity-6.2 {remove port by reference} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    $ramp {removePort pt.kernel.Port} $a
    list [_testEntityGetPorts $ramp] \
            [expr { [$a getContainer] == [java::null] }] \
            [[$b getContainer] getName]
} {b 1 Ramp}

######################################################################
####
# 
test Entity-6.3 {remove port twice by reference} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    $ramp {removePort pt.kernel.Port} $a
    catch {$ramp {removePort String} a} msg
    list $msg
} {{pt.kernel.NoSuchItemException: Ramp: Attempt to remove a nonexistent port: a}}

######################################################################
####
# 
test Entity-6.4 {remove an invalid port with no container} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    set b [java::new pt.kernel.Port $ramp b]
    catch {$ramp {removePort pt.kernel.Port} $a} msg
    list $msg
} {{pt.kernel.IllegalActionException: Ramp and a: Attempt to remove a port with no container.}}

######################################################################
####
# 
test Entity-6.5 {remove an invalid port with no container and no name} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    set b [java::new pt.kernel.Port $ramp b]
    catch {$ramp {removePort pt.kernel.Port} $a} msg
    list $msg
} {{pt.kernel.IllegalActionException: Ramp and <Unnamed Object>: Attempt to remove a port with no container.}}

######################################################################
####
# 
test Entity-6.6 {remove port twice by reference, then check state} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    $ramp {removePort pt.kernel.Port} $a
    catch {$ramp {removePort String} a} msg
    list [_testEntityGetPorts $ramp] \
            [expr { [$a getContainer] == [java::null] }]
} {b 1}

######################################################################
####
# 
test Entity-6.7 {set the name of a port to null, then check state} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    catch {$a setName [java::null]}
    list [_testEntityGetPorts $ramp] \
            [[$a getContainer] getName]
} {{{a b}} Ramp}

######################################################################
# 
test Entity-6.8 {remove all ports} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    set result1 [_testEntityGetPorts $ramp]
    $ramp removeAllPorts
    list $result1 [_testEntityGetPorts $ramp] \
            [expr { [$a getContainer] == [java::null] }] \
            [expr { [$b getContainer] == [java::null] }]
} {{{a b}} {{}} 1 1}

######################################################################
####
# 
test Entity-6.9 {remove port set in the constructor by reference} {
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set a [java::new pt.kernel.Port]
    $a setName a
    $ramp addPort $a
    set b [java::new pt.kernel.Port $ramp b]
    $ramp {removePort pt.kernel.Port} $b
    list [_testEntityGetPorts $ramp] \
            [expr { [$b getContainer] == [java::null] }] \
            [[$a getContainer] getName]
} {a 1 Ramp}

######################################################################
####
# 
test Entity-7.0 {Connect Entities, then remove a port} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    # Remove a port
    $ramp {removePort String} "Ramp out"

    list [_testEntityGetLinkedRelations $ramp] \
            [_testRelationGetLinkedPorts $arc]
} {{{}} {{{Print in}}}}

######################################################################
####
# 
test Entity-8.0 {Create new ports} {
    set e1 [java::new pt.kernel.Entity X]
    set p1 [$e1 newPort A]
    set p2 [$e1 newPort B]
    list [$p1 getFullName] [$p2 getFullName] [_testEntityGetPorts $e1]
} {X.A X.B {{A B}}}
