# Tests for the Port class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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
if {[info procs _testPortEnumRelations] == "" } then { 
    source testEnums.tcl
}

# Load up Tcl procs to print out enums
if {[info procs enumToNames] == "" } then { 
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
test Port-1.1 {Get information about an instance of Port} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.Port]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.Port
  fields:        
  methods:       {addParameter pt.data.Parameter} clone {clone pt.kernel
    .Workspace} connectedPorts {description int} {equals ja
    va.lang.Object} getClass getContainer getFullName getNa
    me {getParameter java.lang.String} getParameters hashCo
    de {isLinked pt.kernel.Relation} isOpaque {link pt.kern
    el.Relation} linkedRelations notify notifyAll numLinks 
    {removeParameter java.lang.String} {setContainer pt.ker
    nel.Entity} {setName java.lang.String} toString {unlink
     pt.kernel.Relation} unlinkAll wait {wait long} {wait l
    ong int} workspace
    
  constructors:  pt.kernel.Port {pt.kernel.Port pt.kernel.Entity java.la
    ng.String} {pt.kernel.Port pt.kernel.Workspace}
    
  properties:    class container fullName name opaque parameters
    
  superclass:    pt.kernel.NamedObj
    
}}

######################################################################
####
# 
test Port-2.1 {Construct Ports} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    set p2 [java::new pt.kernel.Port $e1 "My Port"]
    list [$p1 getName] [$p2 getName] \
	    [$p1 numLinks] [$p2 numLinks]
} {{} {My Port} 0 0}

######################################################################
####
# 
test Port-3.1 {Test link with one port, one relation} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    $p1 setName P1
    set r1 [java::new pt.kernel.Relation R1]
    $p1 link $r1
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {R1 {}}

######################################################################
####
# 
test Port-3.1.1 {Test link with one port, one relation twice} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation R1]
    $p1 link $r1
    $p1 link $r1
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {{R1 R1} {}}

######################################################################
####
# 
test Port-3.1.2 {Test link with one port to a null relation} {
    set p1 [java::new pt.kernel.Port]
    $p1 link [java::null]
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {{} {}}

######################################################################
####
# 
test Port-3.2 {Test link with one port, two relations} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation R1]
    set r2 [java::new pt.kernel.Relation R2]
    $p1 link $r1
    $p1 link $r2
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {{R1 R2} {}}

######################################################################
####
# 
test Port-3.3 {Test link with two ports, one relation} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set p2 [java::new pt.kernel.Port $e1 P2]
    set r1 [java::new pt.kernel.Relation R1]
    $p1 link $r1
    $p2 link $r1
    list [enumToNames [$p1 linkedRelations]] \
            [enumToNames [$p1 connectedPorts]]
} {R1 P2}

######################################################################
####
# 
test Port-3.4 {Test link with two ports, two relations} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set p2 [java::new pt.kernel.Port $e1 P2]
    set r1 [java::new pt.kernel.Relation R1]
    set r2 [java::new pt.kernel.Relation R2]
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
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set p2 [java::new pt.kernel.Port $e1 P2]
    set r1 [java::new pt.kernel.Relation "relation1"]
    set r2 [java::new pt.kernel.Relation "relation2"]
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
test Port-5.1 {Test unlink} {
    set p3 [java::new pt.kernel.Port]
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    # FIXME: Bug in TclBlend: If p3 is set below instead of above,
    # TclBlend gives an error on Unix machines, but not on NT.
    # The error is: 
    # wrong # args for calling constructor "pt.kernel.Port"
    # set p3 [java::new pt.kernel.Port]
    $p3 setContainer $e1
    set r1 [java::new pt.kernel.Relation "relation1"]
    set r2 [java::new pt.kernel.Relation "relation2"]
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
test Port-5.2 {Test unlink on a relation we are not connected to} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation "relation1"]
    set r2 [java::new pt.kernel.Relation "relation2"]
    $p1 link $r1
    $p1 unlink $r2
    list [_testPortLinkedRelations $p1]
} {relation1}

######################################################################
####
# 
test Port-6.1 {Test linkedRelations} {
    set p1 [java::new pt.kernel.Port]
    set enum [$p1 linkedRelations]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Port-7.1 {Test getContainer on a Port that has no container } {
    set p1 [java::new pt.kernel.Port]
    list [expr { [java::null] == [$p1 getContainer] } ]
} {1}

######################################################################
####
# 
test Port-7.2 {Test getContainer on a Port that has a container } {
    set p1 [java::new pt.kernel.Port]
    set e1 [java::new pt.kernel.Entity "entity1"]
    $p1 setContainer $e1
    list [expr { $e1 == [$p1 getContainer] } ]
} {1}

######################################################################
####
# 
test Port-8.1 {Build a topology consiting of a Ramp and a Print Entity} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

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
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

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
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

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
    set w [java::new pt.kernel.Workspace]
    set e1 [java::new pt.kernel.Entity $w E1]
    set p1 [java::new pt.kernel.Port $w]
    $p1 setName P1
    set p2 [java::new pt.kernel.Port $e1 P2]
    set p3 [java::new pt.kernel.Port $e1 P3]
    set r1 [enumToFullNames [$w elements]]
    set r2 [enumToFullNames [$e1 getPorts]]
    $p2 setContainer [java::null]
    $p3 setContainer [java::null]
    set r3 [enumToFullNames [$w elements]]
    set r4 [enumToFullNames [$e1 getPorts]]
    list $r1 $r2 $r3 $r4
} {{.E1 .P1} {.E1.P2 .E1.P3} {.E1 .P1 .P2 .P3} {}}

######################################################################
####
# 
test Port-12.1 {Test description} {
    set w [java::new pt.kernel.Workspace]
    set e1 [java::new pt.kernel.Entity $w E1]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set r1 [java::new pt.kernel.Relation $w R1]
    set r2 [java::new pt.kernel.Relation $w R2]
    $p1 description 7
} {pt.kernel.Port {.E1.P1} links {
}}

test Port-12.2 {Test description} {
    # NOTE: Builds on previous example.
    $p1 link $r1
    $p1 link $r2
    $p1 description 7
} {pt.kernel.Port {.E1.P1} links {
pt.kernel.Relation {.R1}
pt.kernel.Relation {.R2}
}}

test Port-12.3 {Test description} {
    # NOTE: Builds on previous example.
    $p1 description 6
} {{.E1.P1} links {
{.R1}
{.R2}
}}

test Port-12.4 {Test description on workspace} {
    # NOTE: Builds on previous example.
    $w description 15
} {pt.kernel.Workspace {} elements {
pt.kernel.Entity {.E1}
pt.kernel.Relation {.R1} links {
pt.kernel.Port {.E1.P1}
}
pt.kernel.Relation {.R2} links {
pt.kernel.Port {.E1.P1}
}
}}

######################################################################
####
# 
test Port-13.1 {Test clone} {
    set w [java::new pt.kernel.Workspace]
    set e1 [java::new pt.kernel.Entity $w E1]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set r1 [java::new pt.kernel.Relation $w R1]
    $p1 link $r1
    set p2 [$p1 clone]
    $p2 description 7
} {pt.kernel.Port {.P1} links {
}}
