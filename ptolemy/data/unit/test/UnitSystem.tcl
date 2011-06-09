# Tests for UnitSystem
#
# @author: Christopher Hylands (tests only)
#
# @Version $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#


######################################################################
####
# 
test UnitSystem-1.0 {Construct an empty UnitSystem} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]

    java::call ptolemy.data.unit.UnitUtilities resetUnitCategories
    set myUnitSystem [java::new ptolemy.data.unit.UnitSystem \
	    $e0 "myUnitSystem"]

    java::call ptolemy.data.unit.UnitUtilities summarizeUnitCategories
} {The registered categories are: 0 []}

test UnitSystem-1.1 {Construct a UnitSystem add a UnitCategory twice} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]

    java::call ptolemy.data.unit.UnitUtilities resetUnitCategories
    set myUnitSystem [java::new ptolemy.data.unit.UnitSystem \
	    $e0 "myUnitSystem"]


    set meters [java::new \
	    ptolemy.data.unit.BaseUnit $myUnitSystem "meters"]
    $meters setExpression 1.0
    set length [java::new \
	    ptolemy.data.unit.UnitCategory $meters "length"]


    set index1 [java::call \
	    ptolemy.data.unit.UnitUtilities getUnitCategoryIndex "meters"]
    set name1 [java::call \
	    ptolemy.data.unit.UnitUtilities getBaseUnitName $index1]


    # When length gets created, we call UnitSystem.addUnitCategory()
    # but we call it here again so as to cover a basic block.

    java::call ptolemy.data.unit.UnitUtilities registerUnitCategory "meters"

    set index2 [java::call \
	    ptolemy.data.unit.UnitUtilities getUnitCategoryIndex "meters"]
    set name2 [java::call \
  	    ptolemy.data.unit.UnitUtilities getBaseUnitName $index2]
    list [expr {$index1 == $index2}] $name1 $name2 \
    [java::call ptolemy.data.unit.UnitUtilities summarizeUnitCategories]

} {1 meters meters {The registered categories are: 1 [meters]}}


test UnitSystem-1.2 {Add another UnitCategory twice} {
    # Uses setup in UnitSystem-1.0 above
    set seconds [java::new \
	    ptolemy.data.unit.BaseUnit $myUnitSystem "seconds"]
    $seconds setExpression 1.0
    set time [java::new \
	    ptolemy.data.unit.UnitCategory $seconds "time"]

    set index1 [java::call \
		    ptolemy.data.unit.UnitUtilities getUnitCategoryIndex "seconds"]
    set name1 [java::call \
	    ptolemy.data.unit.UnitUtilities getBaseUnitName $index1]

    java::call ptolemy.data.unit.UnitUtilities registerUnitCategory "seconds"

    set index2 [java::call \
	    ptolemy.data.unit.UnitUtilities getUnitCategoryIndex "seconds"]
    set name2 [java::call \
  	    ptolemy.data.unit.UnitUtilities getBaseUnitName $index2]
    list [expr {$index1 == $index2}] $name1 $name2 \
	[java::call ptolemy.data.unit.UnitUtilities summarizeUnitCategories]
} {1 seconds seconds {The registered categories are: 2 [meters, seconds]}}


######################################################################
####
# 
test UnitSystem-1.3 {Create Scalar tokens with units} {
    # uses 1.2 above
    set scalarTypes {ComplexToken DoubleToken FixToken IntToken LongToken UnsignedByteToken}
    set results {}
    foreach type $scalarTypes {
	set t [java::new ptolemy.data.$type]
	$t setUnitCategory 1
	lappend results [$t toString]
    }
    list $results
} {{{0.0 + 0.0i * seconds} {0.0 * seconds} {fix(0,2,2) * seconds} {0 * seconds} {0L * seconds} {0ub * seconds}}}
