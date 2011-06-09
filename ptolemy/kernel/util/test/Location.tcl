# Tests for the Location class
#
# @Author: Christopher Hylands (tests only)
#
# @Version: $Id$ 
#
# @Copyright (c) 2002-2007 The Regents of the University of California.
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
    # Note that when we later call getExpression(), the value that is 
    # returned is not parseable by setExpression() in other classes
    # because it does not have a leading { and a closing }.
    $s2_1 setExpression "3.0, 4.0"

    # For Locations, calling validate notifies the listeners.
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
} {{(ptolemy.kernel.util.Location, Location = {1.0, 2.0}) changed, new expression: {1.0, 2.0}
(ptolemy.kernel.util.Location, Location = 3.0, 4.0) changed, new expression: 3.0, 4.0
}}

test Location-2.1 {clone with an empty location} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set s2 [$s1 clone]
    $s2 toString
} {(ptolemy.kernel.util.Location, Location = {0.0, 0.0})}

test Location-2.2 {clone with a non-empty location} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set locations [java::new {double[]} {2} {1.0 2.0}]
    $s1 setLocation $locations

    set s2 [$s1 clone]
    $s2 toString
} {(ptolemy.kernel.util.Location, Location = {1.0, 2.0})}

test Location-3.1 {exportMoML} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj3_1"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location3_1"]
    set result1 [$s1 exportMoML]
    list $result1	
} {{<property name="my Location3_1" class="ptolemy.kernel.util.Location" value="{0.0, 0.0}">
</property>
}}

test Location-3.2 {exportMoML, expression is the empty string} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj3_2"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location3_2"]
    $s1 setExpression "" 	 
    set result1 [$s1 exportMoML]
    list $result1
} {{<property name="my Location3_2" class="ptolemy.kernel.util.Location">
</property>
}}

test Location-4.1 {getDefaultExpression} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj4_1"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location4_1"]
    set result1 [$s1 getDefaultExpression]
    set result2 [$s1 getExpression]
    $s1 setExpression "a string 4_1"
    set result3 [$s1 getDefaultExpression]
    set result4 [$s1 getExpression]

    list $result1 $result2 $result3 $result4 [$s1 getValueAsString]
} {{} {{0.0, 0.0}} {a string 4_1} {a string 4_1} {a string 4_1}}

test Location-5.1 {getDisplayName, getName} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set result1 [$s1 getDisplayName]
    set result2 [$s1 getName]
    $s1 setName "myName"
    set result3 [$s1 getDisplayName]
    set result4 [$s1 getName]
    list $result1 $result2 $result3 $result4
} {{my Location} {my Location} myName myName}

test Location-7.1 {getExpression} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    # Increase code coverage
    $s1 setExpression ""
    set result1 [$s1 getExpression]
    list $result1
} {{}}

test Location-6.1 {getVisibility, setVisibility} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set full [java::field ptolemy.kernel.util.Settable NONE]
    set r1 [expr {[$s1 getVisibility] == $full}]

    set none [java::field ptolemy.kernel.util.Settable FULL]
    $s1 setVisibility $none

    list $r1 [expr {[$s1 getVisibility] == $none}]
} {1 1}

test Location-7.1 {setLocation: three dimensional location} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    set locations [java::new {double[]} {3} [list -1.0 2.0 3.0]]
    # The Z axis is Infinity
    $locations set 2 [java::field java.lang.Double POSITIVE_INFINITY]
    $s1 setLocation $locations

    set s2 [$s1 clone]
    $s2 toString
} {(ptolemy.kernel.util.Location, Location = {-1.0, 2.0, Infinity})}

test Location-7.2 {setLocation on a Location with no container} {
    set w [java::new ptolemy.kernel.util.Workspace "my Workspace"]
    set s1 [java::new ptolemy.kernel.util.Location $w]
    set locations [java::new {double[]} {2} [list -7.2 7.2]]
    $s1 setLocation $locations
    $s1 toString
} {(ptolemy.kernel.util.Location, Location = {-7.2, 7.2})}

test Location-10.1 {validate} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s1 [java::new ptolemy.kernel.util.Location $n "my Location"]
    $s1 setExpression "{-10.1,10.1}"
    set result1 [$s1 getExpression] 
    # Need to call validate to get the new expression	
    set result2 [[$s1 getLocation] getrange]
    $s1 validate
    set result3 [$s1 getExpression] 
    set result4 [[$s1 getLocation] getrange]
    $s1 setExpression [java::null]
    set result5 [$s1 getExpression] 

    # Note that the old location is still set to -10.1, 10.1
    set result6 [[$s1 getLocation] getrange]

    # Calling validate changes the location to 0.0, 0.0
    $s1 validate
    set result7 [$s1 getExpression] 
    set result8 [[$s1 getLocation] getrange]
    list $result1 $result2 $result3 $result4 $result5 $result6 $result7 $result8
} {{{-10.1,10.1}} {0.0 0.0} {{-10.1,10.1}} {-10.1 10.1} {} {-10.1 10.1} {} {0.0 0.0}}
