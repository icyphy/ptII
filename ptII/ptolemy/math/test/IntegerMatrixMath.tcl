# Tests for the IntegerMatrixMath Class
#
# @Author: Christopher Hylands
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

set epsilon [java::field ptolemy.math.SignalProcessing EPSILON]


set m3 [java::new {int[][]} 3 [list [list 3 -6 0] \
                                       [list 4862 236 -36] \
                                       [list -56 -26 4]]]
set m23 [java::new {int[][]} 2 [list [list 3 -6 0] \
 	                                [list 4862 236 -36]]]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test IntegerMatrixMath-0.5.1 {applyBinaryOperation IntegerBinaryOperation int int[][]} {
    set dbo [java::new ptolemy.math.test.TestIntegerBinaryOperation]
    set mr [java::call ptolemy.math.IntegerMatrixMath \
	    {applyBinaryOperation ptolemy.math.IntegerBinaryOperation int int[][]} $dbo -2 $m3]

    set s [java::call ptolemy.math.IntegerMatrixMath toString $mr]
    regsub -all {,} $s {} stmp

    epsilonDiff $stmp {{{-5 4 -2} {-4864 -238 34} {54 24 -6}}}
} {}

####################################################################
test IntegerMatrixMath-0.5.2 {applyBinaryOperation IntegerBinaryOperation int[][] int} {
    set dbo [java::new ptolemy.math.test.TestIntegerBinaryOperation]
    set mr [java::call ptolemy.math.IntegerMatrixMath \
	    {applyBinaryOperation ptolemy.math.IntegerBinaryOperation int[][] int} $dbo $m3 -2]

    set s [java::call ptolemy.math.IntegerMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{5 -4 2} {4864 238 -34} {-54 -24 6}}}
} {}

####################################################################
test IntegerMatrixMath-0.5.3.1 {applyBinaryOperation IntegerBinaryOperation int[][] int[][]} {
    set dbo [java::new ptolemy.math.test.TestIntegerBinaryOperation]
    set mr [java::call ptolemy.math.IntegerMatrixMath \
	    {applyBinaryOperation ptolemy.math.IntegerBinaryOperation int[][] int[][]} $dbo $m3 $m3]

    set s [java::call ptolemy.math.IntegerMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{0 0 0} {0 0 0} {0 0 0}}}
} {}

####################################################################
test IntegerMatrixMath-0.5.3.2 {applyBinaryOperation IntegerBinaryOperation int[][] int[][] with matrices that are different sizes} {
    set dbo [java::new ptolemy.math.test.TestIntegerBinaryOperation]
    catch { set mr [java::call ptolemy.math.IntegerMatrixMath \
	    {applyBinaryOperation ptolemy.math.IntegerBinaryOperation int[][] int[][]} $dbo $m3 $m23]} errMsg
    set errMsg
} {java.lang.IllegalArgumentException: ptolemy.math.IntegerMatrixMath.applyBinaryOperation() : one matrix [3 x 3] is not the same size as another matrix [2 x 3].}

####################################################################
test IntegerMatrixMath-0.6.1 {applyUnaryOperation IntegerUnaryOperation int[][]} {
    set duo [java::new ptolemy.math.test.TestIntegerUnaryOperation]
    set mr [java::call ptolemy.math.IntegerMatrixMath \
	    {applyUnaryOperation ptolemy.math.IntegerUnaryOperation int[][] } $duo $m23]
    set s [java::call ptolemy.math.IntegerMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-3 6 0} {-4862 -236 36}}}
} {}


####################################################################
test IntegerMatrixMath-5.8.0 {matrixCopy([][], [][]) } {
    set m3_tmp [java::new {int[][]} 3 \
	    [list \
	    [list 0 0 0] \
	    [list 0 0 0] \
	    [list 0 0 0]]]

    java::call ptolemy.math.IntegerMatrixMath \
	    matrixCopy $m3 $m3_tmp
    set s [java::call ptolemy.math.IntegerMatrixMath toString $m3_tmp]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{3 -6 0} {4862 236 -36} {-56 -26 4}}}
} {}


####################################################################
test IntegerMatrixMath-5.8.1 {matrixCopy int[][] int int int[][] int int int int} {
    set m3_src [java::new {int[][]} 3 [list [list 0 -1 2] \
                                       [list 0 -1 -2] \
                                       [list 0 -1 -2]]]
    set m3_dest [java::new {int[][]} 3 [list [list 10 -11 12] \
                                       [list 10 -11 -12] \
                                       [list 10 -11 -12]]]

    java::call ptolemy.math.IntegerMatrixMath \
	    {matrixCopy} $m3_src 1 1 $m3_dest 0 1 1 2
    set s [java::call ptolemy.math.IntegerMatrixMath toString $m3_dest]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{10 -1 -2} {10 -11 -12} {10 -11 -12}}}
} {}

####################################################################
test IntegerMatrixMath-6.1 {within int[][] int[][] int corner case} {
   set m12_1 [java::new {int[][]} 1 [list [list 2 3]]]
   set m12_2 [java::new {int[][]} 1 [list [list 3 2]]] 
   set ok [java::call ptolemy.math.IntegerMatrixMath \
   {within int[][] int[][] int} $m12_1 $m12_2 1]
} {1}

####################################################################
test IntegerMatrixMath-6.2 {within int[][] int[][] int corner case} {
   set m12_1 [java::new {int[][]} 1 [list [list 2 2]]]
   set m12_2 [java::new {int[][]} 1 [list [list 2 2]]] 
   set ok [java::call ptolemy.math.IntegerMatrixMath \
   {within int[][] int[][] int} $m12_1 $m12_2 0]
} {1}

####################################################################
test IntegerMatrixMath-6.3 {within int[][] int[][] int[][] corner case} {
   set m13_1 [java::new {int[][]} 1 [list [list 2 3 4]]]
   set m13_2 [java::new {int[][]} 1 [list [list 3 2 4]]]
   set me [java::new {int[][]} 1 [list [list 1 1 0]]]
   set ok [java::call ptolemy.math.IntegerMatrixMath \
   {within int[][] int[][] int[][]} $m13_1 $m13_2 $me]
} {1}

####################################################################
test IntegerMatrixMath-6.4 {within int[][] int[][] int false case} {
   set m12_1 [java::new {int[][]} 1 [list [list 2 4]]]
   set m12_2 [java::new {int[][]} 1 [list [list 4 2]]] 
   set ok [java::call ptolemy.math.IntegerMatrixMath \
   {within int[][] int[][] int} $m12_1 $m12_2 1]
} {0}


