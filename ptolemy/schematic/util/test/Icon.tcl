# Tests for the Icon class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
test Icon-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.util.Icon]
    set e1 [java::new ptolemy.schematic.util.Icon "TestIcon"]
    list [$e0 toString] [$e1 toString]
} {Icon() TestIcon()}

test Icon-2.2 {setDescription, isDescription tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getDescription]
    $e0 setDescription {Oh what a tangled web we weave,}
    set r1 [$e0 getDescription]
    $e0 setDescription {when we practice to deceive.}
    set r2 [$e0 getDescription]
    list $r0 $r1 $r2
} {{} {Oh what a tangled web we weave,} {when we practice to deceive.}}

######################################################################
####
#
test Icon-3.1 {addGraphicElement} {
    set t1 [java::new ptolemy.schematic.util.GraphicElement GraphicElement1]
    set t2 [java::new ptolemy.schematic.util.GraphicElement GraphicElement2]
    $e0 addGraphicElement $t1
    $e0 toString
} {Icon(
....GraphicElement1())}

test Icon-3.2 {containsGraphicElement} {
    list [$e0 containsGraphicElement $t1] [$e0 containsGraphicElement $t2]
} {1 0}

test Icon-3.3 {GraphicElements} {
    $e0 addGraphicElement $t2
    set enum [$e0 graphicElements]
    set r1 [$enum hasMoreElements]
    set r2 [[$enum nextElement] toString]
    set r3 [$enum hasMoreElements]
    set r4 [[$enum nextElement] toString]
    set r5 [$enum hasMoreElements]
    list $r1 $r2 $r3 $r4 $r5
} {1 GraphicElement1() 1 GraphicElement2() 0}

test Icon-3.4 {removeGraphicElement} {
    $e0 removeGraphicElement $t1
    $e0 toString
} {Icon(
....GraphicElement2())}

