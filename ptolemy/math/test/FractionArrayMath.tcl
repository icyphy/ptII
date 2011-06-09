# Tests for the FractionArrayMath Class
#
# @Author: Adam Cataldo
#
# @Copyright (c) 1998-2007 The Regents of the University of California.
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

# Make Fractions
set a0 [java::new ptolemy.math.Fraction -3 2]
set a1 [java::new ptolemy.math.Fraction 3 6]
set a2 [java::new ptolemy.math.Fraction 7 1]
set a3 [java::new ptolemy.math.Fraction -6 6]
set b0 [java::new ptolemy.math.Fraction 1 4]
set b1 [java::new ptolemy.math.Fraction 3 8]
set b2 [java::new ptolemy.math.Fraction 3 4]
set b3 [java::new ptolemy.math.Fraction -2 1]
set c0 [java::new ptolemy.math.Fraction 2 3]

#Make Arrays
set a [java::new {ptolemy.math.Fraction[]} 4 [list $a0 $a1 $a2 $a3]]
#set a2 [java::new {ptolemy.math.Fraction[]} 5 [list 4826.2 236.1 -36.21 5 65.4]]
#set a3 [java::new {ptolemy.math.Fraction[]} 5 [list 236.1 -36.21 4826.2 5.0 65.4]]
set b [java::new {ptolemy.math.Fraction[]} 4 [list $b0 $b1 $b2 $b3]]
set c [java::new {ptolemy.math.Fraction[]} 0]

#set e1 [java::new {ptolemy.math.Fraction[]} 4 [list -62.3 0.332 5.22 -0.03]]

# ar is a ptolemy.math.Fraction array used to store the results of tests

####################################################################
test FractionArrayMath-2.1 {add} {
    set ar [java::call ptolemy.math.FractionArrayMath add $a $b]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-5/4, 7/8, 31/4, -3/1}}

####################################################################
test FractionArrayMath-2.1.1 {add, first arg is null} {
    catch {java::call ptolemy.math.FractionArrayMath \
	       {add ptolemy.math.Fraction[] ptolemy.math.Fraction[]} \
	       [java::null] $b} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.FractionArrayMath.add() : first input array is null.}}

####################################################################
test FractionArrayMath-2.1.2 {add, second arg is null} {
    catch {java::call ptolemy.math.FractionArrayMath \
	       {add ptolemy.math.Fraction[] ptolemy.math.Fraction[]} \
	       $a [java::null]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.FractionArrayMath.add() : second input array is null.}}

####################################################################
test FractionArrayMath-2.1.3 {add, different length args} {
    catch {java::call ptolemy.math.FractionArrayMath \
	       add $a $c} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.FractionArrayMath.add() : input arrays must have the same length, but the first array has length 4 and the second array has length 0.}}

####################################################################
test FractionArrayMath-2.1.4 {add, zero length arrays} {
    set ar [java::call ptolemy.math.FractionArrayMath add $c $c]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{}}

####################################################################
test FractionArrayMath-2.2 {add scalar} {
    set ar [java::call ptolemy.math.FractionArrayMath add $a $c0]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-5/6, 7/6, 23/3, -1/3}}

####################################################################
test FractionArrayMath-2.3 {allocCopy} {
    set ar [java::call ptolemy.math.FractionArrayMath allocCopy $a]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-3/2, 1/2, 7/1, -1/1}}

####################################################################
test FractionArrayMath-3.1 {append with two empty arrays} {
    set ar [java::call ptolemy.math.FractionArrayMath append $c $c]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{}}

####################################################################
test FractionArrayMath-3.2 {append with 1 empty array, one non-empty array} {
    set ar [java::call ptolemy.math.FractionArrayMath append $c $a]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-3/2, 1/2, 7/1, -1/1}}

####################################################################
test FractionArrayMath-3.3 {append with one non-empty array, 1 empty array} {
    set ar [java::call ptolemy.math.FractionArrayMath append $a $c]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-3/2, 1/2, 7/1, -1/1}}

####################################################################
test FractionArrayMath-3.4 {append with 2 non-empty arrays} {
    set ar [java::call ptolemy.math.FractionArrayMath append $a $b]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-3/2, 1/2, 7/1, -1/1, 1/4, 3/8, 3/4, -2/1}}

####################################################################
test FractionArrayMath-11.1 {divide} {
    set ar [java::call ptolemy.math.FractionArrayMath divide $a $b]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-6/1, 4/3, 28/3, 1/2}}

####################################################################
test FractionArrayMath-11.2 {dotProduct} {
    set r [java::call ptolemy.math.FractionArrayMath dotProduct $a $b]
    set s [$r toString]
} {113/16} 

####################################################################
test FractionArrayMath-11.2.5 {negative} {
    set ar [java::call ptolemy.math.FractionArrayMath negative $a]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{3/2, -1/2, -7/1, 1/1}}

####################################################################
test FractionArrayMath-11.3 {multiply} {
    set ar [java::call ptolemy.math.FractionArrayMath multiply $a $b]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-3/8, 3/16, 21/4, 2/1}}

####################################################################
test FractionArrayMath-11.4 {multiply scalar} {
    set ar [java::call ptolemy.math.FractionArrayMath multiply $a $c0]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-1/1, 1/3, 14/3, -2/3}}

####################################################################
test FractionArrayMath-12.1 {subtract} {
    set ar [java::call ptolemy.math.FractionArrayMath subtract $a $b]
    set s [java::call ptolemy.math.FractionArrayMath toString $ar]
} {{-7/4, 1/8, 25/4, 1/1}}

####################################################################
test FractionArrayMath-14.3.1 {sum} {
    set r [java::call ptolemy.math.FractionArrayMath sum $a]
    set s [$r toString]
} {5/1}

####################################################################
test FractionArrayMath-14.3.2 {equals} {
	set b1 [java::call ptolemy.math.FractionArrayMath equals $a $a]
	set b2 [java::call ptolemy.math.FractionArrayMath equals $a $b]
	set b3 [java::call ptolemy.math.FractionArrayMath equals $a $c]
	concat $b1 $b2 $b3
} {1 0 0}

####################################################################
test FractionArrayMath-15.1 {toDoubleArray} {
    set doubleArray [java::call ptolemy.math.FractionArrayMath toDoubleArray $a]
    set emptyArray [java::call ptolemy.math.FractionArrayMath toDoubleArray $c]
    list [jdkPrintArray $doubleArray] \
	[jdkPrintArray $emptyArray]
} {{-1.5 0.5 7.0 -1.0} {}}
