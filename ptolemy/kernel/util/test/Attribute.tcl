# Tests for the Attribute class
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
test Attribute-2.1 {Create a Attribute} {
    set n [java::new ptolemy.kernel.util.Attribute]
    $n setName "A"
    $n getName
} {A}

######################################################################
####
#
test Attribute-2.2 {Create a Attribute with a container} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p [java::new ptolemy.kernel.util.Attribute $n P]
    $p getFullName
} {.N.P}

######################################################################
####
#
test Attribute-3.1 {Test for NameDuplicationException on constructor} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p1 [java::new ptolemy.kernel.util.Attribute $n P]
    catch {[java::new ptolemy.kernel.util.Attribute $n P]} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Attempt to insert object named "P" into a container that already contains an object with that name.}}
######################################################################
####
#
test Attribute-3.2 {Test for NameDuplicationException on setName} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p1 [java::new ptolemy.kernel.util.Attribute $n P1]
    set p2 [java::new ptolemy.kernel.util.Attribute $n P2]
    catch {$p2 setName P1} msg
    list $msg
} {{ptolemy.kernel.util.NameDuplicationException: Name duplication: P1
  in .N}}

######################################################################
####
#
test Attribute-3.3 {set an Attribute to its own name} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    $n setName N
    set p1 [java::new ptolemy.kernel.util.Attribute $n P1]
    $p1 setName P1
    $p1 getFullName
} {.N.P1}
######################################################################
####
#
test Attribute-6.2 {Test description} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set n [java::new ptolemy.kernel.util.NamedObj N]
    set a [java::new ptolemy.kernel.util.Attribute]
    set b [java::new ptolemy.kernel.util.Attribute $w]
    set c [java::new ptolemy.kernel.util.Attribute $n C]
    # Test with DEEP bit not set
    set detail [expr "[java::field ptolemy.kernel.util.NamedObj COMPLETE] & \
            ~[java::field ptolemy.kernel.util.NamedObj DEEP]"]
    list [$a description $detail] \
	    [$b description $detail] \
	    [$c description $detail] \
	    [$n description $detail]
} {{ptolemy.kernel.util.Attribute {.} attributes {
}} {ptolemy.kernel.util.Attribute {.} attributes {
}} {ptolemy.kernel.util.Attribute {.N.C} attributes {
}} {ptolemy.kernel.util.NamedObj {.N} attributes {
    {ptolemy.kernel.util.Attribute {.N.C}}
}}}

######################################################################
####
#
test Attribute-7.1 {Test clone into a new workspace} {
    # NOTE: Builds on previous test.
    set x [java::new ptolemy.kernel.util.Workspace X]
    set ax [java::cast ptolemy.kernel.util.Attribute [$a clone $x]]
    set aw [java::cast ptolemy.kernel.util.Attribute [$a clone]]
    set bx [java::cast ptolemy.kernel.util.Attribute [$b clone $x]]
    set bw [java::cast ptolemy.kernel.util.Attribute [$b clone]]
    set cx [java::cast ptolemy.kernel.util.Attribute [$c clone $x]]
    set cw [java::cast ptolemy.kernel.util.Attribute [$c clone]]
    list [$ax getFullName] \
            [$aw getFullName] \
            [$bx getFullName] \
            [$bw getFullName] \
            [$cx getFullName] \
            [$cw getFullName]
} {. . . . .C .C}

test Attribute-7.2 {Test cloning of NamedObj with attributes} {
    # NOTE: Builds on previous test.
    set nx [java::cast ptolemy.kernel.util.NamedObj [$n clone $x]]
    set nw [java::cast ptolemy.kernel.util.NamedObj [$n clone]]
    list [$nx description $detail] [$nw description $detail]
} {{ptolemy.kernel.util.NamedObj {.N} attributes {
    {ptolemy.kernel.util.Attribute {.N.C}}
}} {ptolemy.kernel.util.NamedObj {.N} attributes {
    {ptolemy.kernel.util.Attribute {.N.C}}
}}}

######################################################################
####
#
test Attribute-8.1 {setContainer} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj N]
    set a [java::new ptolemy.kernel.util.Attribute]
    set b [java::new ptolemy.kernel.util.Attribute $w]
    set c [java::new ptolemy.kernel.util.Attribute $n C]
    set d [java::new ptolemy.kernel.util.Attribute $n D]
    $a setContainer $c
    $a description
} {ptolemy.kernel.util.Attribute {.N.C.} attributes {
}}

test Attribute-8.2 {setContainer, different workspace} {
    # Builds on 8.1 above
    catch {$b setContainer $c} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot set container because workspaces are different.
  in .<Unnamed Object> and .N.C}}

test Attribute-8.3 {setContainer, then setContainer again} {
    # Builds on 8.1 above
    # Note that this calls NamedObj _removeAttribute()
    $a setContainer $c
    $a setContainer $d
    $a description
} {ptolemy.kernel.util.Attribute {.N.D.} attributes {
}}

test Attribute-8.4 {Construct an Attribute in an unnamed NamedObj} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj]
    set c [java::new ptolemy.kernel.util.Attribute $n C]
    $c getFullName
} {..C}

test Attribute-8.5 {setContainer to an unnamed NamedObj} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj]
    set c [java::new ptolemy.kernel.util.Attribute]
    $c setContainer $n
    $c getFullName
} {..}

test Attribute-8.6 {setContainer to an unnamed NamedObj} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n [java::new ptolemy.kernel.util.NamedObj]
    set c [java::new ptolemy.kernel.util.Attribute]
    set d [java::new ptolemy.kernel.util.Attribute]
    $c setContainer $n
    $d setContainer $c
    $d getFullName
} {...}
