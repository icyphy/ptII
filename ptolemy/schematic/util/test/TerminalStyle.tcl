# Tests for the TerminalStyle class
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
test TerminalStyle-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.util.TerminalStyle]
    set e1 [java::new ptolemy.schematic.util.TerminalStyle "TestTerminalStyle"]
    list [$e0 toString] [$e1 toString]
} {TerminalStyle() TestTerminalStyle()}

test TerminalStyle-2.2 {setDescription, isDescription tests} {
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
test TerminalStyle-3.1 {addTerminal} {
    set t1 [java::new ptolemy.schematic.util.Terminal Terminal1]
    $t1 setInput 1
    set t2 [java::new ptolemy.schematic.util.Terminal Terminal2]
    $t2 setOutput 1    
    $e0 addTerminal $t1
    $e0 toString
} {TerminalStyle(Terminal1((0.0, 0.0), Input))}

test TerminalStyle-3.2 {containsTerminal} {
    list [$e0 containsTerminal $t1] [$e0 containsTerminal $t2]
} {1 0}

test TerminalStyle-3.3 {terminals} {
    $e0 addTerminal $t2
    _testEnums terminals $e0
} {{Terminal1 Terminal2}}

test TerminalStyle-3.4 {removeTerminal} {
    $e0 removeTerminal $t1
    $e0 toString
} {TerminalStyle(Terminal2((0.0, 0.0), Output))}

