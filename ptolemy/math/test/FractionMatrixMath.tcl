# Tests for the FractionMatrixMath Class
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

# Fractions to be used
set c0 [java::new ptolemy.math.Fraction 1 2]
set c1 [java::new ptolemy.math.Fraction 3 2]
set c2 [java::new ptolemy.math.Fraction 4 -2]
set c3 [java::new ptolemy.math.Fraction -4 -6]
set d0 [java::new ptolemy.math.Fraction 2 1]
set d1 [java::new ptolemy.math.Fraction 2 3]
set d2 [java::new ptolemy.math.Fraction 1 -2]
set d3 [java::new ptolemy.math.Fraction 4 -6]


# Create three 2 by 2 Fraction matrices
set m [java::new {ptolemy.math.Fraction[][]} 2 [list [list $c0 $c1] [list $c2 $c3]]]
set n [java::new {ptolemy.math.Fraction[][]} 2 [list [list $d0 $d1] [list $d2 $d3]]]
set o [java::new {ptolemy.math.Fraction[][]} 2 [list [list $c0 $c1] [list $c2 $d2]]]

set fourxtwo [java::new {ptolemy.math.Fraction[][]} 2 \
		   [list [list $c0 $c1 $c2 $c3] \
			[list $d0 $d1 $d2 $d3]]]

set fourxfour [java::new {ptolemy.math.Fraction[][]} 4 \
		   [list [list $c0 $c1 $c2 $c3] \
			[list $d0 $d1 $d2 $d3] \
			[list $c0 $c1 $c2 $c3] \
			[list $d0 $d1 $d2 $d3]]]

#Create a length 2 Fraction array
set a [java::new {ptolemy.math.Fraction[]} 2 [list $c0 $c1]]

#Create a length 4 Fraction array
set b [java::new {ptolemy.math.Fraction[]} 4 [list $c0 $c1 $c2 $c3]]

####################################################################
test FractionMatrixMath-1 {add Fraction[][] 2x2 Fraction} {
	set m1 [java::call ptolemy.math.FractionMatrixMath add $m $c0]
	set s1 [java::call ptolemy.math.FractionMatrixMath toString $m1]
	set s1
} {{{1/1, 2/1}, {-3/2, 7/6}}}

####################################################################
test FractionMatrixMath-1.1 {add Fraction[][] with diff. size matrices} {
    catch {java::call ptolemy.math.FractionMatrixMath \
	       {add ptolemy.math.Fraction[][] ptolemy.math.Fraction[][]} \
	       $m $fourxfour} errMsg
        list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.FractionMatrixMath.add() : one matrix [2 x 2] is not the same size as another matrix [4 x 4].}}

####################################################################
test FractionMatrixMath-2 {add Fraction[][] 2x2 Fraction[][] 2x2} {
	set m2 [java::call ptolemy.math.FractionMatrixMath add $m $n]
	set s2 [java::call ptolemy.math.FractionMatrixMath toString $m2]
	set s2
} {{{5/2, 13/6}, {-5/2, 0/1}}}

####################################################################
test FractionMatrixMath-3 {allocCopy Fraction[][] 2x2} {
	set m3 [java::call ptolemy.math.FractionMatrixMath allocCopy $m]
	set s3 [java::call ptolemy.math.FractionMatrixMath toString $m3]
	set s3
} {{{1/2, 3/2}, {-2/1, 2/3}}}

####################################################################
test FractionMatrixMath-4 {crop Fraction[][] 2x2 int int int int} {
	set i0 0
	set i1 0
	set i2 1
	set i3 2
	set m4a [java::call ptolemy.math.FractionMatrixMath crop $m $i0 $i1 $i2 $i3]
	set m4b [java::call ptolemy.math.FractionMatrixMath crop $m $i0 $i1 $i3 $i2]
	set s4 [java::call ptolemy.math.FractionMatrixMath toString $m4a]
	append s4 [java::call ptolemy.math.FractionMatrixMath toString $m4b]
	set s4
} {{{1/2, 3/2}}{{1/2}, {-2/1}}}

####################################################################
test FractionMatrixMath-5 {diag Fraction[] 3} {
	set m5 [java::call ptolemy.math.FractionMatrixMath diag $a]
	set s5 [java::call ptolemy.math.FractionMatrixMath toString $m5]
	set s5
} {{{1/2, 0/1}, {0/1, 3/2}}}

####################################################################
test FractionMatrixMath-6 {divide Fraction[][] 2x2 Fraction} {
	set m6 [java::call ptolemy.math.FractionMatrixMath divide $m $d0]
	set s6 [java::call ptolemy.math.FractionMatrixMath toString $m6]
	set s6
} {{{1/4, 3/4}, {-1/1, 1/3}}}

####################################################################
test FractionMatrixMath-7 {divideElements Fraction[][] 2x2 Fraction[][] 2x2} {
	set m7 [java::call ptolemy.math.FractionMatrixMath divideElements $m $n]
	set s7 [java::call ptolemy.math.FractionMatrixMath toString $m7]
	set s7
} {{{1/4, 9/4}, {4/1, -1/1}}}

####################################################################
test FractionMatrixMath-8 {identity int} {
	set m8 [java::call ptolemy.math.FractionMatrixMath identity 3]
	set s8 [java::call ptolemy.math.FractionMatrixMath toString $m8]
	set s8
} {{{1/1, 0/1, 0/1}, {0/1, 1/1, 0/1}, {0/1, 0/1, 1/1}}}

####################################################################
test FractionMatrixMath-9 {multiply Fraction[][] 2x2 Fraction} {
	set m9 [java::call ptolemy.math.FractionMatrixMath multiply $m $c0]
	set s9 [java::call ptolemy.math.FractionMatrixMath toString $m9]
	set s9
} {{{1/4, 3/4}, {-1/1, 1/3}}}

####################################################################
test FractionMatrixMath-9.1 {multiply Fraction[][] 2x2 Fraction[] 2} {
	set m9 [java::call ptolemy.math.FractionMatrixMath multiply $m $a]
	set s9 [java::call ptolemy.math.FractionArrayMath toString $m9]
	set s9
} {{5/2, 0/1}}

####################################################################
test FractionMatrixMath-9.2 {multiply Fraction[] 2 Fraction[][] 2x2} {
	set m9 [java::call ptolemy.math.FractionMatrixMath multiply $a $m]
	set s9 [java::call ptolemy.math.FractionArrayMath toString $m9]
	set s9
} {{-11/4, 7/4}}

####################################################################
test FractionMatrixMath-9.4 {multiply Fraction[][] fractions w/ diff. sizes} {
        catch {java::call ptolemy.math.FractionMatrixMath \
		   multiply \
		   $m $fourxfour} errMsg
        list $errMsg
} {{java.lang.ArithmeticException: Number of columns (2) of matrix1 does note equal number of rows (4) of matrix2.}}


####################################################################
test FractionMatrixMath-10 {multiply Fraction[][] 2x2 Fraction[][] 2x2} {
	set m10 [java::call ptolemy.math.FractionMatrixMath multiply $m $n]
	set s10 [java::call ptolemy.math.FractionMatrixMath toString $m10]
	set s10
} {{{1/4, -2/3}, {-13/3, -16/9}}}

####################################################################
test FractionMatrixMath-10.2 {multiply Fraction[][], Fraction [] w/ diff. sizes} {
        catch {java::call ptolemy.math.FractionMatrixMath \
		   multiply \
		   $m $b} errMsg
        list $errMsg
} {{java.lang.IllegalArgumentException: postMultiply() : array does not have the same number of elements (4) as the number of columns of the matrix (2)}}

####################################################################
test FractionMatrixMath-10.3 {multiply Fraction[][], Fraction [][] w/ diff. sizes} {
        catch {java::call ptolemy.math.FractionMatrixMath \
		   multiply \
		   $m $fourxfour} errMsg
        list $errMsg
} {{java.lang.ArithmeticException: Number of columns (2) of matrix1 does note equal number of rows (4) of matrix2.}}

####################################################################
test FractionMatrixMath-11 {negative Fraction[][] 2x2} {
	set m11 [java::call ptolemy.math.FractionMatrixMath negative $m]
	set s11 [java::call ptolemy.math.FractionMatrixMath toString $m11]
	set s11
} {{{-1/2, -3/2}, {2/1, -2/3}}}

####################################################################
test FractionMatrixMath-12 {subtract Fraction[][] 2x2 Fraction[][] 2x2} {
	set m12 [java::call ptolemy.math.FractionMatrixMath subtract $m $n]
	set s12 [java::call ptolemy.math.FractionMatrixMath toString $m12]
	set s12
} {{{-3/2, 5/6}, {-3/2, 4/3}}}

####################################################################
test FractionMatrixMath-13 {sum Fraction[][] 2x2} {
	set m13 [java::call ptolemy.math.FractionMatrixMath sum $m]
	list [$m13 toString]
} {2/3}

####################################################################
test FractionMatrixMath-14 {toDoubleMatrix Fraction[][] 2x2} {
	set m14 [java::call ptolemy.math.FractionMatrixMath toDoubleMatrix $o]
	set s14 [java::call ptolemy.math.DoubleMatrixMath toString $m14]
	set s14
} {{{0.5, 1.5}, {-2.0, -0.5}}}

####################################################################
test FractionMatrixMath-15 {toMatrixFromArray Fraction[][] 2x2 int int} {
	set m15 [java::call ptolemy.math.FractionMatrixMath toMatrixFromArray $b 2 2]
	set s15 [java::call ptolemy.math.FractionMatrixMath toString $m15]
	set s15
} {{{1/2, 3/2}, {-2/1, 2/3}}}

####################################################################
test FractionMatrixMath-16 {trace Fraction[][] 2x2} {
	set m16 [java::call ptolemy.math.FractionMatrixMath trace $m]
	list [$m16 toString]
} {7/6}

####################################################################
test FractionMatrixMath-16.2 {trace Fraction[][] 2x4} {
    catch {java::call ptolemy.math.FractionMatrixMath trace $fourxtwo] errMsg}
	list $errMsg
} {{java.lang.ArithmeticException: Number of columns (2) of matrix1 does note equal number of rows (4) of matrix2.}}

####################################################################
test FractionMatrixMath-17 {transpose Fraction[][] 2x2} {
	set m17 [java::call ptolemy.math.FractionMatrixMath transpose $m]
	set s17 [java::call ptolemy.math.FractionMatrixMath toString $m17]
	set s17
} {{{1/2, -2/1}, {3/2, 2/3}}}
