# Tests for UnitSystem
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
test UnitSystem-0.9 {Construct an empty UnitSystem} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]

    java::call ptolemy.data.unit.UnitSystem reset
    set myUnitSystem [java::new ptolemy.data.unit.UnitSystem \
	    $e0 "myUnitSystem"]

    $myUnitSystem toString

} {ptolemy.data.unit.UnitSystem {..myUnitSystem}
 contains 0 categories
[]}

test UnitSystem-1.0 {Construct a UnitSystem add a UnitCategory twice} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]

    java::call ptolemy.data.unit.UnitSystem reset
    set myUnitSystem [java::new ptolemy.data.unit.UnitSystem \
	    $e0 "myUnitSystem"]


    set meters [java::new \
	    ptolemy.data.unit.BaseUnit $myUnitSystem "meters"]
    $meters setExpression 1.0
    set length [java::new \
	    ptolemy.data.unit.UnitCategory $meters "length"]


    set index1 [java::call \
	    ptolemy.data.unit.UnitSystem getUnitCategoryIndex $length]
    set name1 [java::call \
	    ptolemy.data.unit.UnitSystem getBaseUnitName $index1]


    # When length gets created, we call UnitSystem.addUnitCategory()
    # but we call it here again so as to cover a basic block.

    java::call ptolemy.data.unit.UnitSystem addUnitCategory $length

    set index2 [java::call \
	    ptolemy.data.unit.UnitSystem getUnitCategoryIndex $length]
    set name2 [java::call \
  	    ptolemy.data.unit.UnitSystem getBaseUnitName $index2]
    list [expr {$index1 == $index2}] $name1 $name2 [$myUnitSystem toString]

} {1 meters meters {ptolemy.data.unit.UnitSystem {..myUnitSystem}
 contains 1 categories
[ptolemy.data.unit.UnitCategory {..myUnitSystem.meters.length}]}}


test UnitSystem-1.0 {Add another UnitCategory twice} {
    # Uses setup in UnitSystem-1.0 above
    set seconds [java::new \
	    ptolemy.data.unit.BaseUnit $myUnitSystem "seconds"]
    $seconds setExpression 1.0
    set time [java::new \
	    ptolemy.data.unit.UnitCategory $seconds "time"]

    set index1 [java::call \
	    ptolemy.data.unit.UnitSystem getUnitCategoryIndex $time]
    set name1 [java::call \
	    ptolemy.data.unit.UnitSystem getBaseUnitName $index1]

    java::call ptolemy.data.unit.UnitSystem addUnitCategory $time

    set index2 [java::call \
	    ptolemy.data.unit.UnitSystem getUnitCategoryIndex $time]
    set name2 [java::call \
  	    ptolemy.data.unit.UnitSystem getBaseUnitName $index2]
    list [expr {$index1 == $index2}] $name1 $name2 [$myUnitSystem toString]
} {1 seconds seconds {ptolemy.data.unit.UnitSystem {..myUnitSystem}
 contains 2 categories
[ptolemy.data.unit.UnitCategory {..myUnitSystem.meters.length}, ptolemy.data.unit.UnitCategory {..myUnitSystem.seconds.time}]}}
