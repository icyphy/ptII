# Tests for the Port class
#
# @Author: Christopher Hylands, Edward A. Lee, Jie Liu
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

######################################################################
####
#
test Port-2.1 {Construct Ports} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    set p2 [java::new ptolemy.kernel.Port $e1 "My Port"]
    list [$p1 getName] [$p2 getName] \
	    [$p1 numLinks] [$p2 numLinks] [$p1 isOpaque]
} {{} {My Port} 0 0 1}

######################################################################
####
#
test Port-3.1 {Test link with one port, one relation} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    $p1 setContainer $e1
    $p1 setName P1
    set r1 [java::new ptolemy.kernel.Relation R1]
    $p1 link $r1
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {R1 {}}

######################################################################
####
#
test Port-3.1.1 {Test link with one port, one relation twice} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation R1]
    $p1 link $r1
    $p1 link $r1
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {{R1 R1} {}}

######################################################################
####
#
test Port-3.1.2 {Test link with one port to a null relation} {
    set p1 [java::new ptolemy.kernel.Port]
    $p1 link [java::null]
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {{} {}}

######################################################################
####
#
test Port-3.2 {Test link with one port, two relations} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation R1]
    set r2 [java::new ptolemy.kernel.Relation R2]
    $p1 link $r1
    $p1 link $r2
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {{R1 R2} {}}

######################################################################
####
#
test Port-3.3 {Test link with two ports, one relation} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set p2 [java::new ptolemy.kernel.Port $e1 P2]
    set r1 [java::new ptolemy.kernel.Relation R1]
    $p1 link $r1
    $p2 link $r1
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {R1 P2}

######################################################################
####
#
test Port-3.4 {Test link with two ports, two relations} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set p2 [java::new ptolemy.kernel.Port $e1 P2]
    set r1 [java::new ptolemy.kernel.Relation R1]
    set r2 [java::new ptolemy.kernel.Relation R2]
    $p1 link $r1
    $p2 link $r1
    $p1 link $r2
    $p2 link $r2
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]] \
            [enumToNames [$p2 linkedRelations]] \
            [enumToNames [$p2 connectedPorts]] \
	    [$p1 numLinks] \
	    [$p2 numLinks]
} {{R1 R2} {P2 P2} {R1 R2} {P1 P1} 2 2}

######################################################################
####
#
test Port-4.1 {Test unlinkAll} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    $p1 setContainer $e1
    set p2 [java::new ptolemy.kernel.Port $e1 P2]
    set r1 [java::new ptolemy.kernel.Relation "relation1"]
    set r2 [java::new ptolemy.kernel.Relation "relation2"]
    $p1 link $r1
    $p2 link $r1
    $p1 link $r2
    $p2 link $r2
    $p1 unlinkAll
    set result1 [_testPortLinkedRelations $p1 $p2]
    # We call this twice to make sure that if there are no relations,
    # we don't cause an error.
    $p1 unlinkAll
    set result2 [_testPortLinkedRelations $p1 $p2]
    $p2 unlinkAll
    set result3 [_testPortLinkedRelations $p1 $p2]
   list "$result1\n$result2\n$result3"
} {{{} {relation1 relation2}
{} {relation1 relation2}
{} {}}}

######################################################################
####
#
test Port-5.1 {Test unlink (by relation)} {
    set p3 [java::new ptolemy.kernel.Port]
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    # FIXME: Bug in TclBlend: If p3 is set below instead of above,
    # TclBlend gives an error on Unix machines, but not on NT.
    # The error is:
    # wrong # args for calling constructor "ptolemy.kernel.Port"
    # set p3 [java::new ptolemy.kernel.Port]
    $p3 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation "relation1"]
    set r2 [java::new ptolemy.kernel.Relation "relation2"]
    $p1 link $r1
    $p3 link $r1
    $p1 link $r2
    $p3 link $r2
    $p1 unlink $r1
    set result1 [_testPortLinkedRelations $p1 $p3]
    $p3 unlink $r2
    set result2 [_testPortLinkedRelations $p1 $p3]
    $p3 unlink $r1
    set result3 [_testPortLinkedRelations $p1 $p3]

    # Call unlink on a relation that has already been disconnected.
    $p3 unlink $r1
    set result4 [expr {$result3 == [_testPortLinkedRelations $p1 $p3]}]

    $p1 unlink $r2
    set result5 [_testPortLinkedRelations $p1 $p3]

   list "$result1\n$result2\n$result3\n$result4\n$result5"
} {{relation2 {relation1 relation2}
relation2 relation1
relation2 {}
1
{} {}}}

######################################################################
####
#
test Port-5.1.1 {Test unlink (by index)} {
    set p3 [java::new ptolemy.kernel.Port]
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    # FIXME: Bug in TclBlend: If p3 is set below instead of above,
    # TclBlend gives an error on Unix machines, but not on NT.
    # The error is:
    # wrong # args for calling constructor "ptolemy.kernel.Port"
    # set p3 [java::new ptolemy.kernel.Port]
    $p3 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation "relation1"]
    set r2 [java::new ptolemy.kernel.Relation "relation2"]
    $p1 link $r1
    $p3 link $r1
    $p1 link $r2
    $p3 link $r2
    $p1 {unlink int} 1
    set result1 [_testPortLinkedRelations $p1 $p3]
    $p3 {unlink int} 0
    set result2 [_testPortLinkedRelations $p1 $p3]
    $p3 {unlink int} 0
    set result3 [_testPortLinkedRelations $p1 $p3]

    # Call unlink on a relation that has already been disconnected.
    $p3 {unlink int} 1
    set result4 [expr {$result3 == [_testPortLinkedRelations $p1 $p3]}]

    $p1 {unlink int} 0
    set result5 [_testPortLinkedRelations $p1 $p3]

   list "$result1\n$result2\n$result3\n$result4\n$result5"
} {{relation1 {relation1 relation2}
relation1 relation2
relation1 {}
1
{} {}}}

######################################################################
####
#
test Port-5.2 {Test unlink on a relation we are not connected to} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation "relation1"]
    set r2 [java::new ptolemy.kernel.Relation "relation2"]
    $p1 link $r1
    $p1 unlink $r2
    list [_testPortLinkedRelations $p1]
} {relation1}

######################################################################
####
#
test Port-5.3 {Test unlink of port connected multiple times to same relation.} {
    set p3 [java::new ptolemy.kernel.Port]
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    # FIXME: Bug in TclBlend: If p3 is set below instead of above,
    # TclBlend gives an error on Unix machines, but not on NT.
    # The error is:
    # wrong # args for calling constructor "ptolemy.kernel.Port"
    # set p3 [java::new ptolemy.kernel.Port]
    $p3 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation "relation1"]
    set r2 [java::new ptolemy.kernel.Relation "relation2"]
    $p1 link $r1
    $p3 link $r1
    $p1 link $r2
    $p3 link $r2
    $p1 link $r1
    $p3 link $r1
    $p1 unlink $r1
    set result1 [_testPortLinkedRelations $p1 $p3]
    $p3 unlink $r2
    set result2 [_testPortLinkedRelations $p1 $p3]
    $p3 unlink $r1
    set result3 [_testPortLinkedRelations $p1 $p3]

    # Call unlink on a relation that has already been disconnected.
    $p3 unlink $r2
    set result4 [expr {$result3 == [_testPortLinkedRelations $p1 $p3]}]

    $p1 unlink $r2
    set result5 [_testPortLinkedRelations $p1 $p3]

   list "$result1\n$result2\n$result3\n$result4\n$result5"
} {{{relation2 relation1} {relation1 relation2 relation1}
{relation2 relation1} {relation1 relation1}
{relation2 relation1} relation1
1
relation1 relation1}}

######################################################################
####
#
test Port-6.1 {Test linkedRelations} {
    set p1 [java::new ptolemy.kernel.Port]
    set enum [$p1 linkedRelations]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
#
test Port-7.1 {Test getContainer on a Port that has no container } {
    set p1 [java::new ptolemy.kernel.Port]
    list [expr { [java::null] == [$p1 getContainer] } ]
} {1}

######################################################################
####
#
test Port-7.2 {Test getContainer on a Port that has a container } {
    set p1 [java::new ptolemy.kernel.Port]
    set e1 [java::new ptolemy.kernel.Entity "entity1"]
    $p1 setContainer $e1
    list [expr { $e1 == \
	    [java::cast ptolemy.kernel.Entity [$p1 getContainer]] } ]
} {1}

######################################################################
####
#
test Port-8.1 {Build a topology consiting of a Ramp and a Print Entity} {
    # Create objects
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set print [java::new ptolemy.kernel.Entity "Print"]
    set out [java::new ptolemy.kernel.Port $ramp "Ramp out"]
    set in [java::new ptolemy.kernel.Port $print "Print in"]
    set arc [java::new ptolemy.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    # Note that we are not getting all the information we could
    list [_testPortLinkedRelations $out $in] \
            [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $print]
} {{Arc Arc} {{{Ramp out}}} {{{Print in}}}}

######################################################################
####
#
test Port-9.1 {Remove a port from its container} {
    # Create objects
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set print [java::new ptolemy.kernel.Entity "Print"]
    set out [java::new ptolemy.kernel.Port $ramp "Ramp out"]
    set in [java::new ptolemy.kernel.Port $print "Print in"]
    set arc [java::new ptolemy.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    $out setContainer [java::null]

    # Note that we are not getting all the information we could
    list [_testPortLinkedRelations $out $in] \
            [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $print]
} {{{} Arc} {{}} {{{Print in}}}}

######################################################################
####
#
test Port-10.1 {Reassign a port to a new container} {
    # Create objects
    set ramp [java::new ptolemy.kernel.Entity "Ramp"]
    set print [java::new ptolemy.kernel.Entity "Print"]
    set out [java::new ptolemy.kernel.Port $ramp "Ramp out"]
    set in [java::new ptolemy.kernel.Port $print "Print in"]
    set arc [java::new ptolemy.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    $out setContainer $print

    # Note that we are not getting all the information we could
    list [_testPortLinkedRelations $out $in] \
            [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $print]
} {{Arc Arc} {{}} {{{Print in} {Ramp out}}}}

######################################################################
####
#
test Port-11.1 {Move Port in and out of the workspace} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.Entity $w E1]
    set p1 [java::new ptolemy.kernel.Port $w]
    $p1 setName P1
    set p2 [java::new ptolemy.kernel.Port $e1 P2]
    set p3 [java::new ptolemy.kernel.Port $e1 P3]
    set r1 [enumToFullNames [$w directory]]
    set r2 [enumToFullNames [$e1 getPorts]]
    $p2 setContainer [java::null]
    $p3 setContainer [java::null]
    set r3 [enumToFullNames [$w directory]]
    set r4 [enumToFullNames [$e1 getPorts]]
    list $r1 $r2 $r3 $r4
} {{.E1 .P1} {.E1.P2 .E1.P3} {.E1 .P1} {}}

######################################################################
####
#
test Port-12.1 {Test description} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.Entity $w E1]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set r1 [java::new ptolemy.kernel.Relation $w R1]
    set r2 [java::new ptolemy.kernel.Relation $w R2]
    $p1 description 7
} {ptolemy.kernel.Port {.E1.P1} links {
}}

test Port-12.2 {Test description} {
    # NOTE: Builds on previous example.
    $p1 link $r1
    $p1 link $r2
    $p1 description 7
} {ptolemy.kernel.Port {.E1.P1} links {
    {ptolemy.kernel.Relation {.R1}}
    {ptolemy.kernel.Relation {.R2}}
}}

test Port-12.3 {Test description} {
    # NOTE: Builds on previous example.
    $p1 description 6
} {{.E1.P1} links {
    {{.R1}}
    {{.R2}}
}}

test Port-12.4 {Test description on workspace} {
    # NOTE: Builds on previous example.
    # Test that links show inside an entity.
    $w description 15
} {ptolemy.kernel.util.Workspace {} directory {
    {ptolemy.kernel.Entity {.E1} ports {
        {ptolemy.kernel.Port {.E1.P1} links {
            {ptolemy.kernel.Relation {.R1}}
            {ptolemy.kernel.Relation {.R2}}
        }}
    }}
    {ptolemy.kernel.Relation {.R1} links {
        {ptolemy.kernel.Port {.E1.P1}}
    }}
    {ptolemy.kernel.Relation {.R2} links {
        {ptolemy.kernel.Port {.E1.P1}}
    }}
}}

######################################################################
####
#
test Port-13.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.kernel.Entity $w E1]
    set p1 [java::new ptolemy.kernel.Port $e1 P1]
    set r1 [java::new ptolemy.kernel.Relation $w R1]
    $p1 link $r1
    set p2 [java::cast ptolemy.kernel.Port [$p1 clone]]
    $p2 description 7
} {ptolemy.kernel.Port {.P1} links {
}}

######################################################################
####
#
test Port-14.1 {Test double link with one port, one relation} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new ptolemy.kernel.Relation R1]
    $p1 link $r1
    $p1 link $r1
    $e1 description [java::field ptolemy.kernel.util.NamedObj COMPLETE]
} {ptolemy.kernel.Entity {.} attributes {
} ports {
    {ptolemy.kernel.Port {..} attributes {
    } links {
        {ptolemy.kernel.Relation {.R1} attributes {
        }}
        {ptolemy.kernel.Relation {.R1} attributes {
        }}
    }}
}}

######################################################################
####
#
test Port-15.1 {Test for NameDuplicationException on constructor} {
    set a [java::new ptolemy.kernel.Entity]
    $a setName A
    set b [java::new ptolemy.kernel.Port $a B]
    catch {[java::new ptolemy.kernel.Port $a B]} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "B" into container named ".A", which already contains an object with that name.}}

######################################################################
####
#
test Port-15.2 {Test for NameDuplicationException on setName} {
    set a [java::new ptolemy.kernel.Entity]
    $a setName A
    set p1 [java::new ptolemy.kernel.Port $a P1]
    set p2 [java::new ptolemy.kernel.Port $a P2]
    catch {$p2 setName P1} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: .A:
already contains a port with the name P1.}}

######################################################################
####
#
test Port-15.3 {Test for setting name back} {
    set a [java::new ptolemy.kernel.Entity]
    $a setName A
    set p1 [java::new ptolemy.kernel.Port $a P1]
    $p1 setName P1
    $p1 getFullName
} {.A.P1}

######################################################################
####
#
test Port-16.0 {Test exportMoML} {
    set a [java::new ptolemy.kernel.Entity]
    $a setName A
    set p1 [java::new ptolemy.kernel.Port $a P1]
    $a exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="A" class="ptolemy.kernel.Entity">
    <port name="P1" class="ptolemy.kernel.Port">
    </port>
</model>
}

######################################################################
####
#
test Port-17.0 {Test insertLink} {
    set e1 [java::new ptolemy.kernel.Entity]
    $e1 setName "E1"
    set p1 [java::new ptolemy.kernel.Port $e1 "P1"]
    set r1 [java::new ptolemy.kernel.Relation "R1"]
    set r2 [java::new ptolemy.kernel.Relation "R2"]
    $p1 insertLink 1 $r1
    $p1 description
} {ptolemy.kernel.Port {.E1.P1} attributes {
} links {
    null
    {ptolemy.kernel.Relation {.R1} attributes {
    }}
}}

