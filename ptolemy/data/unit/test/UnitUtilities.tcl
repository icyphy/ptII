# Tests for UnitUtilities
#
# @author: Christopher Hylands (tests only)
#
# @Version $Id$
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
test UnitUtilities-1.0 { call newUnitArrayInCategory} {
    # I have no idea why this method is here, but we test it anyway
    set unitArray0 [java::call \
	    ptolemy.data.unit.UnitUtilities newUnitArrayInCategory 0]
    set unitArray1 [java::call \
	    ptolemy.data.unit.UnitUtilities newUnitArrayInCategory 1]
    list [$unitArray0 getrange] [$unitArray1 getrange]
} {1 {0 1}}

######################################################################
####
# 
test UnitUtilities-2.0 {call unitsStrings with a unitless arg} {
    java::call ptolemy.data.unit.UnitUtilities resetUnitCategories
    set unitless [java::new {int[]} {5} {0 0 0 0 0 0}]
    java::call ptolemy.data.unit.UnitUtilities unitsString $unitless
} {}

######################################################################
####
# 
test UnitUtilities-2.1 {call unitsStrings an array of length 1} {

    java::call ptolemy.data.unit.UnitUtilities resetUnitCategories

    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set myUnitSystem [java::new ptolemy.data.unit.UnitSystem \
	    $e0 "myUnitSystem"]


    set meters2 [java::new \
	    ptolemy.data.unit.BaseUnit $myUnitSystem "meters"]
    $meters2 setExpression 1.0
    set length2 [java::new \
 	    ptolemy.data.unit.UnitCategory $meters2 "length"]

    set seconds [java::new \
	    ptolemy.data.unit.BaseUnit $myUnitSystem "seconds"]
    $seconds setExpression 1.0
    set time [java::new \
	    ptolemy.data.unit.UnitCategory $seconds "time"]


    set m [java::new {int[]} {1} {1}]
    set oneOverMeters [java::new {int[]} {1} {-1}]
    set metersOverSeconds [java::new {int[]} {2} {1 1}]
    set a [java::new {int[]} {2} {0 1}]
    set b [java::new {int[]} {2} {1 0}]
    set c [java::new {int[]} {2} {-1 1}]
    set d [java::new {int[]} {2} {1 -1}]
    set e [java::new {int[]} {2} {-1 -1}]
    set f [java::new {int[]} {2} {-1 2}]
    list \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $m] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString \
	    $oneOverMeters] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString \
	    $metersOverSeconds] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $a] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $b] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $c] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $d] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $e] \
	    [java::call ptolemy.data.unit.UnitUtilities unitsString $f]

} {meters {1 / meters} {(meters * seconds)} seconds meters {seconds / meters} {meters / seconds} {1 / (meters * seconds)} {(seconds * seconds) / meters}}


