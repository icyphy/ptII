# Tests for the ComplexMatrixMath Class
#
# @Author: Christopher Hylands, Jeff Tsay
#
# @Version: $Id$
#
# @Copyright (c) 1998-2002 The Regents of the University of California.
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
set c1 [java::new ptolemy.math.Complex 1.0 2.0]
set c2 [java::new ptolemy.math.Complex 3.0 -4.0]
set c3 [java::new ptolemy.math.Complex -4.9 -6.0]
set c4 [java::new ptolemy.math.Complex -7.0 8.0]
set c5 [java::new ptolemy.math.Complex -0.25 0.4]

# Complex array of length 0
set ca0 [java::new {ptolemy.math.Complex[]} 0]

# Complex array
set m3 [java::new {ptolemy.math.Complex[][]} 3 [list [list $c1 $c2 $c3] \
                                       [list $c4 $c5 $c3] \
                                       [list $c2 $c1 $c5]]]

set m23 [java::new {ptolemy.math.Complex[][]} 2 [list [list $c3 $c1 $c2] \
                                       [list $c5 $c4 $c2]]]


set m32 [java::new {ptolemy.math.Complex[][]} 3 [list [list $c3 $c1] \
                                       [list $c5 $c4] \
                                       [list $c2 $c5]]]



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
