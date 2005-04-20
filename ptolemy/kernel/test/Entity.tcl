# Tests for the Entity class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkStackTrace] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Entity-2.1 {Construct Entities} {
    set e1 [java::new ptolemy.kernel.Entity]
    set e2 [java::new ptolemy.kernel.Entity "My Entity"]
    list [$e1 getName] [$e2 getName]
} {{} {My Entity}}

######################################################################
####
#
test Entity-2.2 {Construct Entities, call getPorts} {
    set e1 [java::new ptolemy.kernel.Entity]
    set e2 [java::new ptolemy.kernel.Entity "My Entity"]
    list [java::instanceof [$e1 getPorts] java.util.Enumeration]
} {1}

######################################################################
####
#
test Entity-4.0 {Connect Entities} {
    # Create objects
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set print [java::new ptolemy.kernel.Entity "Print"]
    set out [java::new ptolemy.kernel.Port $ramp "Ramp out"]
    set in [java::new ptolemy.kernel.Port $print "Print in"]
    set arc [java::new ptolemy.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    _testEntityLinkedRelations $ramp
} {Arc}

######################################################################
####
#
test Entity-5.0 {move port from one entity to another} {
    # Workspace
    set w [java::new ptolemy.kernel.util.Workspace]
    set old [java::new ptolemy.kernel.Entity $w "Old"]
    set ramp [java::new ptolemy.kernel.Entity $w "Ramp"]
    set a [java::new ptolemy.kernel.Port $old foo]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    list [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $old] \
            [[$a getContainer] getName] \
            [[$b getContainer] getName]
} {{{a b}} {{}} Ramp Ramp}

######################################################################
####
#
test Entity-5.1 {move port without a name from one entity to another} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set ramp [java::new ptolemy.kernel.Entity $w "Ramp"]
    set old [java::new ptolemy.kernel.Entity $w "Old"]
    set a [java::new ptolemy.kernel.Port $old {}]
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    _testEntityGetPorts $ramp
} {{{} b}}

######################################################################
####
#
test Entity-5.2 {move port twice} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set ramp [java::new ptolemy.kernel.Entity $w "Ramp"]
    set old [java::new ptolemy.kernel.Entity $w "Old"]
    set a [java::new ptolemy.kernel.Port $old "Port"]
    $a setContainer $ramp
    $a setContainer $ramp
    list [enumToFullNames [$ramp getPorts]] [enumToFullNames [$old getPorts]]
} {.Ramp.Port {}}

######################################################################
####
#
test Entity-6.0 {remove port by name} {
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set a [java::new ptolemy.kernel.Port]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    [$ramp getPort a] setContainer [java::null]
    list [_testEntityGetPorts $ramp] \
            [expr { [$a getContainer] == [java::null] }] \
            [[$b getContainer] getName]
} {b 1 Ramp}

######################################################################
####
#
test Entity-6.2 {remove port by reference} {
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set a [java::new ptolemy.kernel.Port]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    $a setContainer [java::null]

    list [$ramp description 15] \
            [expr { [$a getContainer] == [java::null] }] \
            [[$b getContainer] getName]
} {{ptolemy.kernel.Entity {.Ramp} ports {
    {ptolemy.kernel.Port {.Ramp.b} links {
    }}
}} 1 Ramp}

######################################################################
####
#
test Entity-6.6 {remove port twice by reference, then check state} {
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set a [java::new ptolemy.kernel.Port]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    $a setContainer [java::null]
    list [_testEntityGetPorts $ramp] \
            [expr { [$a getContainer] == [java::null] }]
} {b 1}

######################################################################
####
#
test Entity-6.7 {set the name of a port to null, then check state} {
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set a [java::new ptolemy.kernel.Port]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    $a setName [java::null]
    list [_testEntityGetPorts $ramp] \
            [[$a getContainer] getName]
} {{{{} b}} Ramp}

######################################################################
#
test Entity-6.8 {remove all ports} {
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set a [java::new ptolemy.kernel.Port]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
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
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set a [java::new ptolemy.kernel.Port]
    $a setName a
    $a setContainer $ramp
    set b [java::new ptolemy.kernel.Port $ramp b]
    $b setContainer [java::null]
    list [_testEntityGetPorts $ramp] \
            [expr { [$b getContainer] == [java::null] }] \
            [[$a getContainer] getName]
} {a 1 Ramp}

######################################################################
####
#
test Entity-7.0 {Connect Entities, then remove a port} {
    # Create objects
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set print [java::new ptolemy.kernel.Entity "Print"]
    set out [java::new ptolemy.kernel.Port $ramp "Ramp out"]
    set in [java::new ptolemy.kernel.Port $print "Print in"]
    set arc [java::new ptolemy.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    # Remove a port
    [$ramp getPort "Ramp out"] setContainer [java::null]

    list [_testEntityLinkedRelations $ramp] \
            [_testRelationLinkedPorts $arc]
} {{{}} {{{Print in}}}}

######################################################################
####
#
test Entity-8.0 {Create new ports} {
    set e1 [java::new ptolemy.kernel.Entity X]
    set p1 [$e1 newPort A]
    set p2 [$e1 newPort B]
    list [$p1 getFullName] [$p2 getFullName] [_testEntityGetPorts $e1]
} {.X.A .X.B {{A B}}}

######################################################################
####
#
test Entity-9.0 {Test description} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e1 [java::new ptolemy.kernel.Entity $w E1]
    set p1 [$e1 newPort P1]
    set p2 [$e1 newPort P2]
    set r1 [java::new ptolemy.kernel.Relation $w R1]
    $p1 link $r1
    $p2 link $r1
    $w description 31
} {ptolemy.kernel.util.Workspace {W} directory {
    {ptolemy.kernel.Entity {.E1} ports {
        {ptolemy.kernel.Port {.E1.P1} links {
            {ptolemy.kernel.Relation {.R1}}
        }}
        {ptolemy.kernel.Port {.E1.P2} links {
            {ptolemy.kernel.Relation {.R1}}
        }}
    }}
    {ptolemy.kernel.Relation {.R1} links {
        {ptolemy.kernel.Port {.E1.P1}}
        {ptolemy.kernel.Port {.E1.P2}}
    }}
}}

######################################################################
####
#
test Entity-9.1 {Test cloning} {
    # NOTE: Uses the setup constructed in 9.0
    set e2 [java::cast ptolemy.kernel.Entity [$e1 clone]]
    $e2 description 15
} {ptolemy.kernel.Entity {.E1} ports {
    {ptolemy.kernel.Port {.E1.P1} links {
    }}
    {ptolemy.kernel.Port {.E1.P2} links {
    }}
}}


######################################################################
####
#
test Entity-9.3 {Test clone with badly named ports} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set portNameProblem [java::new ptolemy.kernel.test.PortNameProblem \
			     $w E1]
    catch {$portNameProblem clone} errMsg
    set exception [lindex $errorCode 1]
    set cause [$exception getCause]
    list [$cause toString]
} {{ptolemy.kernel.util.IllegalActionException: Could not find a port named 'poorlyNamedInput' or 'poorlyNamedInputPort'. This can occur when the name of the variable does not match the name passed to the constructor of the actor.
Right:
    poorlyNamedInput = new TypedIOPort(this, "poorlyNamedInput", true, false);
Right:
    poorlyNamedInput = new TypedIOPort(this, "poorlyNamedInputPort", true, false);
Wrong:
    poorlyNamedInput = new TypedIOPort(this, "foo", true, false);
  in .E1}}

######################################################################
####
#
test Entity-9.4 {Test clone with port that has no container } {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set portNameProblem [java::new ptolemy.kernel.test.PortHasNoContainer \
			     $w E1]
    catch {$portNameProblem clone} errMsg
    set exception [lindex $errorCode 1]
    set cause [$exception getCause]
    list [$cause toString]
} {}

######################################################################
####
#
test Entity-9.5 {Test connectedPorts} {
    # NOTE: Uses the setup constructed in 9.0
    enumToNames [$e1 connectedPorts]
} {P2 P1}


######################################################################
####
#
test Entity-10.0 {test getAttribute} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e [java::new ptolemy.kernel.Entity $w "e"]
    set p [java::new ptolemy.kernel.Port $e "p"]
    set a [java::new ptolemy.kernel.util.Attribute $p "a"]
    set r [$e getAttribute p.a]
    $r getFullName
} {.e.p.a}

test Entity-10.1 {test getAttribute} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e [java::new ptolemy.kernel.Entity $w "e"]
    set p [java::new ptolemy.kernel.util.Attribute $e "p"]
    set a [java::new ptolemy.kernel.util.Attribute $p "a"]
    set r [$e getAttribute p.a]
    $r getFullName
} {.e.p.a}

######################################################################
####
#
test Entity-11.1 {uniqueName} {
    set e1 [java::new ptolemy.kernel.Entity]
    set r1 [list [$e1 uniqueName [java::null]] \
	    [$e1 uniqueName ""] \
	    [$e1 uniqueName "myPrefix"]]
    java::new ptolemy.kernel.Port $e1 myPrefix
    set r2 [list [$e1 uniqueName [java::null]] \
	    [$e1 uniqueName ""] \
	    [$e1 uniqueName "myPrefix"]]
    list $r1 $r2
} {{null {} myPrefix} {null {} myPrefix2}}
