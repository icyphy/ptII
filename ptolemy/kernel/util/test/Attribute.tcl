# Tests for the Attribute class
#
# @Author: Edward A. Lee
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
    set n [java::new pt.kernel.util.Attribute]
    $n setName "A"
    $n getName
} {A}

######################################################################
####
#
test Attribute-2.2 {Create a Attribute with a container} {
    set n [java::new pt.kernel.util.NamedObj]
    $n setName N
    set p [java::new pt.kernel.util.Attribute $n P]
    $p getFullName
} {.N.P}

######################################################################
####
#
test Attribute-6.2 {Test description} {
    set w [java::new pt.kernel.util.Workspace W]
    set n [java::new pt.kernel.util.NamedObj N]
    set a [java::new pt.kernel.util.Attribute]
    set b [java::new pt.kernel.util.Attribute $w]
    set c [java::new pt.kernel.util.Attribute $n C]
    # Test with DEEP bit not set
    set detail [expr "[java::field pt.kernel.util.NamedObj COMPLETE] & \
            ~[java::field pt.kernel.util.NamedObj DEEP]"]
    list [$a description $detail] \
	    [$b description $detail] \
	    [$c description $detail] \
	    [$n description $detail]
} {{pt.kernel.util.Attribute {.} attributes {
}} {pt.kernel.util.Attribute {W.} attributes {
}} {pt.kernel.util.Attribute {.N.C} attributes {
}} {pt.kernel.util.NamedObj {.N} attributes {
    {pt.kernel.util.Attribute {.N.C}}
}}}

######################################################################
####
#
test Attribute-7.1 {Test clone into a new workspace} {
    # NOTE: Builds on previous test.
    set x [java::new pt.kernel.util.Workspace X]
    set ax [$a clone $x]
    set aw [$a clone]
    set bx [$b clone $x]
    set bw [$b clone]
    set cx [$c clone $x]
    set cw [$c clone]
    list [$ax getFullName] \
            [$aw getFullName] \
            [$bx getFullName] \
            [$bw getFullName] \
            [$cx getFullName] \
            [$cw getFullName]
} {X. . X. W. X.C .C}

test Attribute-7.2 {Test cloning of NamedObj with attributes} {
    # NOTE: Builds on previous test.
    set nx [$n clone $x]
    set nw [$n clone]
    list [$nx description $detail] [$nw description $detail]
} {{pt.kernel.util.NamedObj {X.N} attributes {
    {pt.kernel.util.Attribute {X.N.C}}
}} {pt.kernel.util.NamedObj {.N} attributes {
    {pt.kernel.util.Attribute {.N.C}}
}}}
