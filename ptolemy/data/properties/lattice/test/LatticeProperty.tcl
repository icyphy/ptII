# Tests for LatticeProperty
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
}

if {[string compare lcompare [info procs lcompare]] == 1} then {
    source [file join $PTII util testsuite lcompare.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#

######################################################################
####
#
test LatticeProperty-1.1 {construct a lattice property} {
    set nullaryPropertyLattice [java::new ptolemy.data.properties.lattice.PropertyLattice]

    set propertyLattice1a [java::new ptolemy.data.properties.lattice.PropertyLattice]
    set latticeProperty1a [java::new ptolemy.data.properties.lattice.LatticeProperty \
			       $propertyLattice1a "latticeProperty1a"]

    set propertyLattice1b [java::new ptolemy.data.properties.lattice.PropertyLattice]
    set latticeProperty1b [java::new ptolemy.data.properties.lattice.LatticeProperty \
			       $propertyLattice1b "latticeProperty1b"]

    set propertyLattice2 [java::new ptolemy.data.properties.lattice.PropertyLattice]
    $propertyLattice2 addNodeWeight $propertyLattice1a
    $propertyLattice2 addNodeWeight $propertyLattice1b
    list [$nullaryPropertyLattice toString] \
	[$propertyLattice2 toString] \
	[$propertyLattice2 getName] \
} {lattice lattice}

######################################################################
####
#
test LatticeProperty-2.1 {test PropertyLattice equals and hashCode} {

    # Create a propertyLattice that is like the one created in test 1.1 above.

    set propertyLattice1a_21 [java::new ptolemy.data.properties.lattice.PropertyLattice]
    set latticeProperty1a_21 [java::new ptolemy.data.properties.lattice.LatticeProperty \
			       $propertyLattice1a_21 "latticeProperty1a"]

    set propertyLattice1b_21 [java::new ptolemy.data.properties.lattice.PropertyLattice]
    set latticeProperty1b_21 [java::new ptolemy.data.properties.lattice.LatticeProperty \
			       $propertyLattice1b_21 "latticeProperty1b"]

    set propertyLattice2_21 [java::new ptolemy.data.properties.lattice.PropertyLattice]
    $propertyLattice2_21 addNodeWeight $propertyLattice1a_21
    $propertyLattice2_21 addNodeWeight $propertyLattice1b_21

    # These are not equal because the nodeWeights are different objects
    list [$propertyLattice2 equals $propertyLattice2_21]
} {0} 

######################################################################
####
#
test LatticeProperty-2.2 {test PropertyLattice equals and hashCode} {

    # Create a propertyLattice that is like the one created in test 1.1 above.
    set propertyLattice2_22 [java::new ptolemy.data.properties.lattice.PropertyLattice]
    $propertyLattice2_22 addNodeWeight $propertyLattice1a
    $propertyLattice2_22 addNodeWeight $propertyLattice1b

    # These are equal because the nodeWeights are the same objects
    list [$propertyLattice2 equals $propertyLattice2_22]
} {1} 


######################################################################
####
#
test LatticeProperty-2.3 {test equals and hashCode} {
    # Uses 2.1 and 2.2 above.
    list [$latticeProperty1a equals $latticeProperty1a_21] \
	[expr {[$latticeProperty1a hashCode] == [$latticeProperty1a_21 hashCode]}] \
	[$latticeProperty1a equals $latticeProperty1b] \
	[expr {[$latticeProperty1a hashCode] == [$latticeProperty1b hashCode]}]
} {1 1 0 1}
