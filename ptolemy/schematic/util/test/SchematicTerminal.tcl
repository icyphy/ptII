# Tests for the SchematicTerminal class
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
test SchematicTerminal-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.util.SchematicTerminal "TestTerminal"]
    set e1 [java::new ptolemy.schematic.util.SchematicTerminal "TestTerminal" $e0]
    list [$e0 description] [$e1 description]
} {{ptolemy.schematic.util.SchematicTerminal {TestTerminal} parameters {
} template {
    null
} X {0.0} Y {0.0}} {ptolemy.schematic.util.SchematicTerminal {TestTerminal} parameters {
} template {
    ptolemy.schematic.util.SchematicTerminal {TestTerminal} parameters {
    } template {
        null
    } X {0.0} Y {0.0}
} X {0.0} Y {0.0}}}

test SchematicTerminal-2.2 {setDescription, isDocumentation tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getDocumentation]
    $e0 setDocumentation {Oh what a tangled web we weave,}
    set r1 [$e0 getDocumentation]
    $e0 setDocumentation {when we practice to deceive.}
    set r2 [$e0 getDocumentation]
    list $r0 $r1 $r2
} {{} {Oh what a tangled web we weave,} {when we practice to deceive.}}

######################################################################
####
#
test SchematicTerminal-3.1 {setX, getX tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getX]
    $e0 setX 1.0
    set r1 [$e0 getX]
    $e0 setX 0.2
    set r2 [$e0 getX]
    list $r0 $r1 $r2
} {0.0 1.0 0.2}

test SchematicTerminal-3.2 {setY, getY tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getY]
    $e0 setY 1.0
    set r1 [$e0 getY]
    $e0 setY 0.2
    set r2 [$e0 getY]
    list $r0 $r1 $r2
} {0.0 1.0 0.2}

test SchematicTerminal-3.7 {description} {
    $e0 setX 1.1
    $e0 setY 2.4
    $e0 description
} {ptolemy.schematic.util.SchematicTerminal {TestTerminal} parameters {
} template {
    null
} X {1.1} Y {2.4}}

