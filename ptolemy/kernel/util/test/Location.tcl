# Tests for the Location class
#
# @Author: Christopher Hylands (tests only)
#
# @Version: $Id$ 
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.htm for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#


test Location-2.1 {addValueListener} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s2_1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.test.StreamValueListener \
	    $printStream]

    # Try removing the listener before adding it.
    $s2_1 removeValueListener $listener

    $s2_1 addValueListener $listener

    # Add the listener twice to get coverage of a basic block.
    $s2_1 addValueListener $listener

    #$s2_1 setExpression "a string"

    set locations [java::new {double[]} {2} {1.0 2.0}]
    $s2_1 setLocation $locations
    
    # Set locations again to cover a basic block.
    $s2_1 setLocation $locations

    # Set locations with different locations to cover a basic block.
    $s2_1 setExpression "3.0, 4.0"

    # For Locations, calling validate notifies the listeners
    $s2_1 validate

    # Call setExpression with the same expression and call validate()
    # again.  The listener should not be notified again.
    $s2_1 setExpression "3.0, 4.0"
    $s2_1 validate

    # Remove the listener and verify that we are not updating it.
    $s2_1 removeValueListener $listener

    set locations [java::new {double[]} {2} {5.0 6.0}]
    $s2_1 setLocation $locations

    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output
} {{(ptolemy.kernel.util.Location, Location = (1.0, 2.0)) changed, new expression: 1.0, 2.0
(ptolemy.kernel.util.Location, Location = (3.0, 4.0)) changed, new expression: 3.0, 4.0
}}


test Location-3.1 {getVisibility, setVisibility} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set full [java::field ptolemy.kernel.util.Settable NONE]
    set r1 [expr {[$s1 getVisibility] == $full}]

    set none [java::field ptolemy.kernel.util.Settable FULL]
    $s1 setVisibility $none

    list $r1 [expr {[$s1 getVisibility] == $none}]
} {1 1}


test Location-4.1 {clone with an empty location} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set s2 [$s1 clone]
    $s2 toString
} {(ptolemy.kernel.util.Location, Location = (0.0, 0.0))}

test Location-4.2 {clone with a non-empty location} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set locations [java::new {double[]} {2} {1.0 2.0}]
    $s1 setLocation $locations

    set s2 [$s1 clone]
    $s2 toString
} {(ptolemy.kernel.util.Location, Location = (1.0, 2.0))}
