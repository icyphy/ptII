# Tests for the FloatMatrixMath Class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

set epsilon [java::field ptolemy.math.SignalProcessing EPSILON]

set a2 [java::new {float[]} 3 [list 4862.2 236.1 -36.25]]

set m3 [java::new {float[][]} 3 [list [list 3.7 -6.6 0.0003] \
                                       [list 4862.2 236.1 -36.25] \
                                       [list -56.4 -26.3 4.9]]]

set m23 [java::new {float[][]} 2 [list [list 3.7 -6.6 0.0003] \
 	                                [list 4862.2 236.1 -36.25]]]


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test FloatMatrixMath-0.5.1 {applyBinaryOperation FloatBinaryOperation float float[][]} {
    set dbo [java::new ptolemy.math.test.TestFloatBinaryOperation]
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    {applyBinaryOperation ptolemy.math.FloatBinaryOperation float float[][]} $dbo -2 $m3]

    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    regsub -all {,} $s {} stmp

    epsilonDiff $stmp {{{-5.7 4.6 -2.0003} {-4864.2 -238.1 34.25} {54.4 24.3 -6.9}}}
} {}

####################################################################
test FloatMatrixMath-0.5.2 {applyBinaryOperation FloatBinaryOperation float[][] float} {
    set dbo [java::new ptolemy.math.test.TestFloatBinaryOperation]
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    {applyBinaryOperation ptolemy.math.FloatBinaryOperation float[][] float} $dbo $m3 -2]

    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{5.7 -4.6 2.0003} {4864.2 238.1 -34.25} {-54.4 -24.3 6.9}}}
} {}

####################################################################
test FloatMatrixMath-0.5.3.1 {applyBinaryOperation FloatBinaryOperation float[][] float[][]} {
    set dbo [java::new ptolemy.math.test.TestFloatBinaryOperation]
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    {applyBinaryOperation ptolemy.math.FloatBinaryOperation float[][] float[][]} $dbo $m3 $m3]

    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{0 0 0} {0 0 0} {0 0 0}}}
} {}

####################################################################
test FloatMatrixMath-0.5.3.2 {applyBinaryOperation FloatBinaryOperation float[][] float[][] with matrices that are different sizes} {
    set dbo [java::new ptolemy.math.test.TestFloatBinaryOperation]
    catch { set mr [java::call ptolemy.math.FloatMatrixMath \
	    {applyBinaryOperation ptolemy.math.FloatBinaryOperation float[][] float[][]} $dbo $m3 $m23]} errMsg
    set errMsg
} {java.lang.IllegalArgumentException: ptolemy.math.FloatMatrixMath.applyBinaryOperation() : one matrix [3 x 3] is not the same size as another matrix [2 x 3].}

####################################################################
test FloatMatrixMath-0.6.1 {applyUnaryOperation FloatUnaryOperation float[][]} {
    set duo [java::new ptolemy.math.test.TestFloatUnaryOperation]
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    {applyUnaryOperation ptolemy.math.FloatUnaryOperation float[][] } $duo $m23]
    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-3.7 6.6 -3.0E-4} {-4862.2 -236.1 36.25}}}
} {}

####################################################################
test FloatMatrixMath-1.7.1 {diag float[]} {
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    diag $a2]
    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    epsilonDiff $s {{{4862.2, 0.0, 0.0}, {0.0, 236.1, 0.0}, {0.0, 0.0, -36.25}}}
} {}


####################################################################
test FloatMatrixMath-1.8 {divide float[][], float } {
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    divide $m23 1.0]
    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    epsilonDiff $s [java::call ptolemy.math.FloatMatrixMath toString $m23]
} {}

####################################################################
test FloatMatrixMath-1.8.1 {divide float[][], float } {
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    divide $m23 1.0]
    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    epsilonDiff $s {{{3.7, -6.6, 3.0E-4}, {4862.2, 236.1, -36.25}}}
} {}

####################################################################
test FloatMatrixMath-2.1 {determinant float[][] not square} {
    catch {set r [java::call ptolemy.math.FloatMatrixMath determinant $m23]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.FloatMatrixMath.determinant() : matrix argument [2 x 3] is not a square matrix.}}

####################################################################
test FloatMatrixMath-2.2 {determinant float[][]} {
   set r [java::call ptolemy.math.FloatMatrixMath determinant $m3]
   set ok [java::call ptolemy.math.SignalProcessing close $r 144468.484375]
} {1}

####################################################################
test FloatMatrixMath-3.8.1 {hilbert} {
    set mr [java::call ptolemy.math.FloatMatrixMath \
	    hilbert 4]
    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{1.0 0.5 0.3333333333333333 0.25} {0.5 0.3333333333333333 0.25 0.2} {0.3333333333333333 0.25 0.2 0.16666666666666666} {0.25 0.2 0.16666666666666666 0.14285714285714285}}}
} {}

####################################################################
test FloatMatrixMath-5.1 {inverse float[][] not square} {
    catch {set r [java::call ptolemy.math.FloatMatrixMath inverse $m23]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.FloatMatrixMath.inverse() : matrix argument [2 x 3] is not a square matrix.}}

####################################################################
test FloatMatrixMath-5.2 {inverse float[][]} {
    set mr [java::call ptolemy.math.FloatMatrixMath inverse $m3]
    set s [java::call ptolemy.math.FloatMatrixMath toString $mr]
    # Get rid of trailing ,
    regsub -all {,} $s {} stmp
    ptclose $stmp {{{0.00140871553556869 0.000223800435617599 0.00165558024009741} {-0.150761461342092 0.000125611616474079 0.000938499905222039} {-0.79297446471244 0.00325018981267364 0.228174953683435}}}
} {1} 

####################################################################
test FloatMatrixMath-5.8.0 {matrixCopy([][], [][]) } {
    set m3_tmp [java::new {float[][]} 3 \
	    [list \
	    [list 0.0 0.0 0.0] \
	    [list 0.0 0.0 0.0] \
	    [list 0.0 0.0 0.0]]]

    java::call ptolemy.math.FloatMatrixMath \
	    matrixCopy $m3 $m3_tmp
    set s [java::call ptolemy.math.FloatMatrixMath toString $m3_tmp]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{3.7 -6.6 3.0E-4} {4862.2 236.1 -36.25} {-56.4 -26.3 4.9}}}
} {}

####################################################################
test FloatMatrixMath-5.8.1 {matrixCopy float[][] int int float[][] int int int int} {
    set m3_src [java::new {float[][]} 3 [list [list 0.0 -1.0 2.0] \
                                       [list 0.1 -1.1 -2.1] \
                                       [list 0.2 -1.2 -2.2]]]
    set m3_dest [java::new {float[][]} 3 [list [list 10.0 -11.0 12.0] \
                                       [list 10.1 -11.1 -12.1] \
                                       [list 10.2 -11.2 -12.2]]]

    java::call ptolemy.math.FloatMatrixMath \
	    {matrixCopy} $m3_src 1 1 $m3_dest 0 1 1 2
    set s [java::call ptolemy.math.FloatMatrixMath toString $m3_dest]

    epsilonDiff $s {{{10.0, -1.1, -2.1}, {10.1, -11.1, -12.1}, {10.2, -11.2, -12.2}}}
} {}


####################################################################
test FloatMatrixMath-7.9 {qr float[][]} {
    # Result is a float[][][]
    set mr [java::call ptolemy.math.FloatMatrixMath qr $m3]

    set s0 [java::call ptolemy.math.FloatMatrixMath toString [$mr get 0]]
    set s1 [java::call ptolemy.math.FloatMatrixMath toString [$mr get 1]]
    # Get rid of trailing ,
    regsub -all {,} [list $s0 $s1] {} stmp
    epsilonDiff $stmp {{{{7.60921E-4 -0.27655035 -0.96098757} {0.99993247 -0.010937519 0.0039348574} {-0.011598904 -0.9609372 0.2765635}}} {{{4862.5283 236.38411 -36.304386} {0.0 24.515532 -4.3121905} {0.0 0.0 1.2122343}}}}
} {} 

####################################################################
test FloatMatrixMath-8.0 {sum float[][]} {
    set s [java::call ptolemy.math.FloatMatrixMath sum $m3]
    epsilonDiff $s 4981.35058594
} {}
