# Tests for the FloatMatrixMath Class
#
# @Author: Christopher Hylands
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

set epsilon [java::field ptolemy.math.SignalProcessing epsilon]


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

