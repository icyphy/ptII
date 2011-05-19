# Tests for the ComplexMatrixMath Class
#
# @Author: Christopher Hylands, Jeff Tsay
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


# Complex numbers to be used
set c0 [java::new ptolemy.math.Complex 0.0 0.0]
set c1 [java::new ptolemy.math.Complex 1.0 2.0]
set c2 [java::new ptolemy.math.Complex 3.0 -4.0]
set c3 [java::new ptolemy.math.Complex -4.9 -6.0]
set c4 [java::new ptolemy.math.Complex -7.0 8.0]
set c5 [java::new ptolemy.math.Complex -0.25 0.4]
set c6 [java::new ptolemy.math.Complex -1.0 0.0]
set c7 [java::new ptolemy.math.Complex 9.0 9.0]

# Complex array of length 0
set ca0 [java::new {ptolemy.math.Complex[]} 0]

# Complex array of length 2
set m2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c2]]

# Complex matrices
set m3 [java::new {ptolemy.math.Complex[][]} 3 [list [list $c1 $c2 $c3] \
                                       [list $c4 $c5 $c3] \
                                       [list $c2 $c1 $c5]]]

set m22 [java::new {ptolemy.math.Complex[][]} 2 [list [list $c3 $c1] \
                                       [list $c5 $c4]]]

set m23 [java::new {ptolemy.math.Complex[][]} 2 [list [list $c3 $c1 $c2] \
                                       [list $c5 $c4 $c2]]]

set m23_0 [java::new {ptolemy.math.Complex[][]} 2 [list [list $c0 $c0 $c0] \
                                       [list $c0 $c0 $c0]]]


set m32 [java::new {ptolemy.math.Complex[][]} 3 [list [list $c3 $c1] \
                                       [list $c5 $c4] \
                                       [list $c2 $c5]]]

set m33 [java::new {ptolemy.math.Complex[][]} 3 [list [list $c3 $c1 $c4] \
                                       [list $c5 $c4 $c3] \
                                       [list $c2 $c5 $c1]]]



####################################################################
test ComplexMatrixMath-0.5.1.1 {applyBinaryOperation ComplexBinaryOperation Complex Complex[][] 3x3} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex ptolemy.math.Complex[][]} $dbo $c1 $m3]

    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp

    ptclose $stmp {{{0.0 + 0.0i -2.0 + 6.0i 5.9 + 8.0i} {8.0 - 6.0i 1.25 + 1.6i 5.9 + 8.0i} {-2.0 + 6.0i 0.0 + 0.0i 1.25 + 1.6i}}}
} {1}

####################################################################
test ComplexMatrixMath-0.5.1.2 {applyBinaryOperation ComplexBinaryOperation Complex Complex[][] 2x3} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex ptolemy.math.Complex[][]} $dbo $c1 $m23]

    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp

    ptclose $stmp {{{5.9 + 8.0i 0.0 + 0.0i -2.0 + 6.0i} {1.25 + 1.6i 8.0 - 6.0i -2.0 + 6.0i}}}
} {1}


####################################################################
test ComplexMatrixMath-0.5.1.3 {applyBinaryOperation ComplexBinaryOperation Complex Complex[][] 3x2} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex ptolemy.math.Complex[][]} $dbo $c1 $m32]

    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp

    ptclose $stmp {{{5.9 + 8.0i 0.0 + 0.0i} {1.25 + 1.6i 8.0 - 6.0i} {-2.0 + 6.0i 1.25 + 1.6i}}}
} {1}

####################################################################
test ComplexMatrixMath-0.5.2.1 {applyBinaryOperation ComplexBinaryOperation Complex[][] Complex 3x3} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex[][] ptolemy.math.Complex} $dbo $m3 $c1]

    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    ptclose $stmp {{{0.0 + 0.0i 2.0 - 6.0i -5.9 - 8.0i} {-8.0 + 6.0i -1.25 - 1.6i -5.9 - 8.0i} {2.0 - 6.0i 0.0 + 0.0i -1.25 - 1.6i}}}
} {1}

####################################################################
test ComplexMatrixMath-0.5.2.2 {applyBinaryOperation ComplexBinaryOperation Complex[][] Complex 2x3} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex[][] ptolemy.math.Complex} $dbo $m23 $c1]

    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    ptclose $stmp {{{-5.9 - 8.0i 0.0 + 0.0i 2.0 - 6.0i} {-1.25 - 1.6i -8.0 + 6.0i 2.0 - 6.0i}}}
} {1}

####################################################################
test ComplexMatrixMath-0.5.3.1 {applyBinaryOperation ComplexBinaryOperation Complex[][] Complex[][]} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex[][] ptolemy.math.Complex[][]} $dbo $m3 $m3]

    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i} {0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i} {0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i}}}
} {}

####################################################################
test ComplexMatrixMath-0.5.3.2 {applyBinaryOperation ComplexBinaryOperation Complex[][] Complex[][] with matrices that are different sizes} {
    set dbo [java::new ptolemy.math.test.TestComplexBinaryOperation]
    catch { set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyBinaryOperation ptolemy.math.ComplexBinaryOperation ptolemy.math.Complex[][] ptolemy.math.Complex[][]} $dbo $m3 $m23]} errMsg
    set errMsg
} {java.lang.IllegalArgumentException: ptolemy.math.ComplexMatrixMath.applyBinaryOperation() : one matrix [3 x 3] is not the same size as another matrix [2 x 3].}

####################################################################
test ComplexMatrixMath-0.6.1 {applyUnaryOperation ComplexUnaryOperation Complex[][]} {
    set duo [java::new ptolemy.math.test.TestComplexUnaryOperation]
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    {applyUnaryOperation ptolemy.math.ComplexUnaryOperation ptolemy.math.Complex[][] } $duo $m23]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{4.9 + 6.0i -1.0 - 2.0i -3.0 + 4.0i} {0.25 - 0.4i 7.0 - 8.0i -3.0 + 4.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.8.0 {matrixCopy([][], [][]) } {
    set m3_tmp [java::new {ptolemy.math.Complex[][]} 3 \
	    [list \
	    [list $c1 $c1 $c1] \
	    [list $c1 $c1 $c1] \
	    [list $c1 $c1 $c1]]]

    java::call ptolemy.math.ComplexMatrixMath \
	    matrixCopy $m3 $m3_tmp
    set s [java::call ptolemy.math.ComplexMatrixMath toString $m3_tmp]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{1.0 + 2.0i 3.0 - 4.0i -4.9 - 6.0i} {-7.0 + 8.0i -0.25 + 0.4i -4.9 - 6.0i} {3.0 - 4.0i 1.0 + 2.0i -0.25 + 0.4i}}}
} {}

####################################################################
test ComplexMatrixMath-5.8.1 {matrixCopy([][], [][]) } {
    set m23_tmp [java::new {ptolemy.math.Complex[][]} 2 \
	    [list \
	    [list $c1 $c1 $c1] \
	    [list $c1 $c1 $c1]]]

    java::call ptolemy.math.ComplexMatrixMath \
	    matrixCopy $m23 $m23_tmp
    set s [java::call ptolemy.math.ComplexMatrixMath toString $m23_tmp]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 - 6.0i 1.0 + 2.0i 3.0 - 4.0i} {-0.25 + 0.4i -7.0 + 8.0i 3.0 - 4.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.8.2 {matrixCopy([][], [][]) } {
    set m32_tmp [java::new {ptolemy.math.Complex[][]} 3 \
	    [list \
	    [list $c1 $c1] \
	    [list $c1 $c1] \
            [list $c1 $c1]]]

    java::call ptolemy.math.ComplexMatrixMath \
	    matrixCopy $m32 $m32_tmp
    set s [java::call ptolemy.math.ComplexMatrixMath toString $m32_tmp]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 - 6.0i 1.0 + 2.0i} {-0.25 + 0.4i -7.0 + 8.0i} {3.0 - 4.0i -0.25 + 0.4i}}}
} {}


####################################################################
test ComplexMatrixMath-5.8.3 {matrixCopy Complex[][] int int Complex[][] int int int int} {
    set m3_src [java::new {ptolemy.math.Complex[][]} 3 \
	    [list \
	    [list $c1 $c2] \
	    [list $c3 $c4] \
            [list $c5 $c1]]]
    set m3_dest [java::new {ptolemy.math.Complex[][]} 3 [list [list $c3 $c1] \
                                       [list $c5 $c4] \
                                       [list $c2 $c5]]]

    java::call ptolemy.math.ComplexMatrixMath \
	    {matrixCopy} $m3_src 0 0 $m3_dest 1 0 2 2
    set s [java::call ptolemy.math.ComplexMatrixMath toString $m3_dest]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 - 6.0i 1.0 + 2.0i} {1.0 + 2.0i 3.0 - 4.0i} {-4.9 - 6.0i -7.0 + 8.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.9.0 {conjugate } {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
		    conjugate $m3]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{1.0 - 2.0i 3.0 + 4.0i -4.9 + 6.0i} {-7.0 - 8.0i -0.25 - 0.4i -4.9 + 6.0i} {3.0 + 4.0i 1.0 - 2.0i -0.25 - 0.4i}}}
} {}

####################################################################
test ComplexMatrixMath-5.9.1 {conjugate with 0x0 matrix} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
		    conjugate $m23]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 + 6.0i 1.0 - 2.0i 3.0 + 4.0i} {-0.25 - 0.4i -7.0 - 8.0i 3.0 + 4.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.9.2 {conjugate } {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
		    conjugate $m32]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 + 6.0i 1.0 - 2.0i} {-0.25 - 0.4i -7.0 - 8.0i} {3.0 + 4.0i -0.25 - 0.4i}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.1 {zero} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    zero 2 3]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i} {0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.2 {realParts} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    realParts $m23]
    set s [java::call ptolemy.math.DoubleMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 1.0 3.0} {-0.25 -7.0 3.0}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.3 {inverse} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    inverse $m22]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-0.08054623357401358 + 0.09878325227792009i -0.012816950866797521 - 0.023549260257783887i} {0.0028290022510533727 - 0.004897469784379781i -0.061218731725691374 - 0.06985561701254343i}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.4 {imagParts} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    imagParts $m23]
    set s [java::call ptolemy.math.DoubleMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-6.0 2.0 -4.0} {0.4 8.0 -4.0}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.5 {diag} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    diag $m2]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{1.0 + 2.0i 0.0 + 0.0i} {0.0 + 0.0i 3.0 - 4.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.6 {determinant} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    determinant $m22]
    set s [java::call ptolemy.math.Complex toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {83.35 + 2.9i}
} {}

####################################################################
test ComplexMatrixMath-5.1.7 {crop} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    crop $m33 1 1 2 2]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-7.0 + 8.0i -4.9 - 6.0i} {-0.25 + 0.4i 1.0 + 2.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.1.8 {conjugateTranspose} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    conjugateTranspose $m33]
    set s [java::call ptolemy.math.ComplexMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-4.9 + 6.0i -0.25 - 0.4i 3.0 + 4.0i} {1.0 - 2.0i -7.0 - 8.0i -0.25 - 0.4i} {-7.0 - 8.0i -4.9 + 6.0i 1.0 - 2.0i}}}
} {}

####################################################################
test ComplexMatrixMath-5.4.2 {within} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    within $m3 $m33 $c0]
    epsilonDiff $mr 0
} {}

####################################################################
test ComplexMatrixMath-5.4.3 {within} {
    set mr [java::call ptolemy.math.ComplexMatrixMath \
	    within $m3 $m33 $c7]
    epsilonDiff $mr 0
} {} 
