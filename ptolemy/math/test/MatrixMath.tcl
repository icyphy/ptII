# Tests for the *MatrixMath classes
#
# @Author: Christopher Hylands (tests only)
#
# @Version $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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

if {[string compare jdkStackTrace [info procs jdkStackTrace]] == 1} then { 
    source [file join $PTII util testsuite jdkTools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Complex numbers to be used
set complex1 [java::new ptolemy.math.Complex 2.0 -2.0]
set complex2 [java::new ptolemy.math.Complex 1.0 1.0]
set complex3 [java::new ptolemy.math.Complex -1.0 -1.0]
set complex4 [java::new ptolemy.math.Complex 0.0 0.0]
set complex5 [java::new ptolemy.math.Complex 3.0 -3.0]

set complex1_1 [java::new {ptolemy.math.Complex[][]} 1 [list \
	[list $complex1]]]

set complex2_2 [java::new {ptolemy.math.Complex[][]} {2} [list \
	[list $complex1 $complex2] \
	[list $complex3 $complex4]]]
# 2x2 array with non-zero elements
set complex2_2nonzero [java::new {ptolemy.math.Complex[][]} {2} [list \
	[list $complex1 $complex2] \
	[list $complex3 $complex5]]]

set double1 2
set double1_1 [java::new {double[][]} 1 [list [list 2.0]]]
set double2_2 [java::new {double[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 0.0]]]
# 2x2 array with non-zero elements
set double2_2nonzero [java::new {double[][]} {2 2} [list [list 2.0 -1.0] \
	                               [list 1.0 3.0]]]


set float1 2
set float1_1 [java::new {float[][]} 1 [list 2.0]]
set float2_2 [java::new {float[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 0.0]]]
# 2x2 array with non-zero elements
set float2_2nonzero [java::new {float[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 3.0]]]

set int1 2
set int1_1 [java::new {int[][]} 1 [list 2]]
set int2_2 [java::new {int[][]} {2 2} [list [list 2 -1] \
                                       [list 1 0]]]
# 2x2 array with non-zero elements
set int2_2nonzero [java::new {int[][]} {2 2} [list [list 2 -1] \
                                       [list 1 3]]]

set long1 2
set long1_1 [java::new {long[][]} 1 [list 2]]
set long2_2 [java::new {long[][]} {2 2} [list [list 2 -1] \
                                       [list 1 0]]]

# 2x2 array with non-zero elements
set long2_2nonzero [java::new {long[][]} {2 2} [list [list 2 -1] \
                                       [list 1 3]]]


# Test an operation that takes a matrix and a scalar,
# like add(long[][], long)
# Arguments:
#    op - The operation to be tested, for example "add"
#    types - a list of lists of types, where each element of the list
#            contains four subelements: 
#              The base matrix type, which would go in 
#                    ptolemy.math.xxxMatrixMath
#              The base type, for example double or Complex
#              The base name of the variable to use, for example double 
#              The expected results
#    arraySize - the suffix of the variable name that contains the
#                the test data. If arraySize is 2_2, then long2_2, int2_2
#                etc. should exist
proc testMatrixScalar {op types {arraySize 2_2}} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test testMatrixScalar-$op "$m.MatrixMath.$op\($t\[\]\[\], $t\)" {
	    set matrix ${v}${arraySize}
	    global $matrix ${v}1
	    set matrixResults [java::call ptolemy.math.${m}MatrixMath \
		    [list $op "$t\[\]\[\]" $t] [subst $$matrix] [subst $${v}1]]
	    set stringResults [java::call ptolemy.math.${m}MatrixMath \
		    toString $matrixResults]	
	    regsub -all {,} $stringResults {} stringAsList
	    #puts "got: $stringAsList"
	    #puts "expected: $expectedResults"
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}

# Test an operation that takes a matrix and a matrix
# like add(long[][], long[][])
# Arguments:
#    op - The operation to be tested, for example "add"
#    types - a list of lists of types, where each element of the list
#            contains four subelements: 
#              The base matrix type, which would go in 
#                    ptolemy.math.xxxMatrixMath
#              The base type, for example double or Complex
#              The base name of the variable to use, for example double 
#              The expected results
#    arraySize - the suffix of the variable name that contains the
#                the test data. If arraySize is 2_2, then long2_2, int2_2
#                etc. should exist
proc testMatrixMatrix {op types {arraySize 2_2}} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test testMatrixMatrix-$op \
		"$m.MatrixMath.$op\($t\[\]\[\], $t\[\]\[\]\)" {
	    set matrix ${v}${arraySize}
	    global $matrix ${v}1
	    set matrixResults [java::call ptolemy.math.${m}MatrixMath \
		    [list $op "$t\[\]\[\]" $t\[\]\[\]] [subst $$matrix] [subst $${v}${arraySize}]]
	    set stringResults [java::call ptolemy.math.${m}MatrixMath \
		    toString $matrixResults]	
	    regsub -all {,} $stringResults {} stringAsList
	    puts "got: $stringAsList"
	    puts "expected: $expectedResults"
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}


# Test an operation that takes a matrix
# like allocCopy(long[][])
# Arguments:
#    op - The operation to be tested, for example "add"
#    types - a list of lists of types, where each element of the list
#            contains four subelements: 
#              The base matrix type, which would go in 
#                    ptolemy.math.xxxMatrixMath
#              The base type, for example double or Complex
#              The base name of the variable to use, for example double 
#              The expected results
#    arraySize - the suffix of the variable name that contains the
#                the test data. If arraySize is 2_2, then long2_2, int2_2
#                etc. should exist
proc testMatrix2 {op types {arraySize 2_2}} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test testMatrixMatrix-$op \
		"$m.MatrixMath.$op\($t\[\]\[\]\)" {
	    set matrix ${v}${arraySize}
	    global $matrix ${v}1
	    set matrixResults [java::call ptolemy.math.${m}MatrixMath \
		    [list $op "$t\[\]\[\]"] [subst $$matrix]]
	    set stringResults [java::call ptolemy.math.${m}MatrixMath \
		    toString $matrixResults]	
	    regsub -all {,} $stringResults {} stringAsList
	    puts "got: $stringAsList"
	    puts "expected: $expectedResults"
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}
proc testMatrixMath {op types {arraySize 2_2} matrixArguments} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test testMatrixMatrix-$op \
		"$m.MatrixMath.$op\($t\[\]\[\]\)" {
	    set matrix ${v}${arraySize}
	    global $matrix ${v}1
	    set matrixResults [java::call ptolemy.math.${m}MatrixMath \
		    $matrixArguments]
	    set stringResults [java::call ptolemy.math.${m}MatrixMath \
		    toString $matrixResults]	
	    regsub -all {,} $stringResults {} stringAsList
	    puts "got: $stringAsList"
	    puts "expected: $expectedResults"
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}

proc testMatrix {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {$op [subst $$matrix]}
}
######################################################################
####
#  Test out: xxx[][] add(xxx[][], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{4.0 - 4.0i 3.0 - 1.0i} {1.0 - 3.0i 2.0 - 2.0i}}}] \
	[list Double double double {{{4.0 1.0} {3.0 2.0}}}] \
	[list Float float float {{{4.0 1.0} {3.0 2.0}}}] \
	[list Integer int int {{{4 1} {3 2}}}] \
	[list Long long long {{{4 1} {3 2}}}]]


testMatrixScalar add $types


######################################################################
####
#  Test out: xxx[][] add(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{4.0 - 4.0i 2.0 + 2.0i} {-2.0 - 2.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{4.0 -2.0} {2.0 0.0}}}] \
	[list Float float float {{{4.0 -2.0} {2.0 0.0}}}] \
	[list Integer int int {{{4 -2} {2 0}}}] \
	[list Long long long {{{4 -2} {2 0}}}]]


testMatrixMatrix add $types

######################################################################
####
#  Test out: xxx[][] allocCopy(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i} {-1.0 - 1.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Float float float {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrix allocCopy $types

######################################################################
####
#  FIXME: Test out applyBinaryOperation(XXXBinaryOperation, xxx, xxx[][])

######################################################################
####
#  FIXME: Test out applyBinaryOperation(XXXBinaryOperation, xxx[][], xxx)

######################################################################
####
#  FIXME: Test out applyBinaryOperation(XXXBinaryOperation, xxx[][], xxx[][])

######################################################################
####
#  FIXME: Test out applyUnaryOperation(XXXUnaryOperation, xxx[][])

######################################################################
####
#  Test out: xxx[][] bitwiseAnd(xxx[][], xxx)

set types [list \
	[list Integer int int {{{2 2} {0 0}}}] \
	[list Long long long {{{2 2} {0 0}}}]]

testMatrixScalar bitwiseAnd $types

######################################################################
####
#  Test out: xxx[][] bitwiseAnd(xxx[][], xxx[][])

set types [list \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrixMatrix bitwiseAnd $types

######################################################################
####
#  Test out: xxx[][] bitwiseComplement(xxx[][])

set types [list \
	[list Integer int int {{{-3 0} {-2 -1}}}] \
	[list Long long long {{{-3 0} {-2 -1}}}]]

testMatrix bitwiseComplement $types

######################################################################
####
#  Test out: xxx[][] bitwiseOr(xxx[][], xxx)

set types [list \
	[list Integer int int {{{2 -1} {3 2}}}] \
	[list Long long long {{{2 -1} {3 2}}}]] 

testMatrixScalar bitwiseOr $types

######################################################################
####
#  Test out: xxx[][] bitwiseOr(xxx[][], xxx[][])

set types [list \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrixMatrix bitwiseOr $types

######################################################################
####
#  Test out: xxx[][] bitwiseXor(xxx[][], xxx)

set types [list \
	[list Integer int int {{{0 -3} {3 2}}}] \
	[list Long long long {{{0 -3} {3 2}}}]]

testMatrixScalar bitwiseXor $types

######################################################################
####
#  Test out: xxx[] bitwiseXor(xxx[][], xxx[][])

set types [list \
	[list Integer int int {{{0 0} {0 0}}}] \
	[list Long long long {{{0 0} {0 0}}}]]

testMatrixMatrix bitwiseXor $types


######################################################################
####
#  FIXME: Integer, Long   Test out    crop . . .


######################################################################
####
#  Test out: xxx[][] divideElements(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{1.0 + 0.0i 1.0 + 0.0i} {1.0 + 0.0i 1.0 + 0.0i}}}] \
	[list Double double double {{{1.0 1.0} {1.0 1.0}}}] \
	[list Float float float {{{1.0 1.0} {1.0 1.0}}} ] \
	[list Integer int int {{{1 1} {1 1}}}] \
	[list Long long long {{{1 1} {1 1}}}]]

testMatrixMatrix divideElements $types 2_2nonzero

######################################################################
####
#  FIXME: xxx[] fromMatrixToArray(xxx[][])

######################################################################
####
#  FIXME: xxx[] fromMatrixToArray(xxx[][], int, int)

######################################################################
####
#  FIXME: identity(int)

######################################################################
####
#  Test out void matrixCopy(xxx[][], xxx[][])


######################################################################
####
#  Test out void matrixCopy(xxx[][], xxx[][], ...)

######################################################################
####
#  Test out: xxx[][] moduloElements(xxx[][], xxx)

set types [list \
	[list Integer int int {{{0 -1} {1 1}}} ] \
	[list Long long long {{{0 -1} {1 1}}}]]


testMatrixScalar moduloElements $types 2_2nonzero


######################################################################
####
#  Test out: xxx[][] modulo(xxx[][], xxx[][])

set types [list \
	[list Integer int int {{{0 0} {0 0}}}] \
	[list Long long long {{{0 0} {0 0}}}]]


testMatrixMatrix modulo $types 2_2nonzero

######################################################################
####
#  Test out: xxx[][] multiply(xxx[][], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 - 8.0i 4.0 + 0.0i} {-4.0 + 0.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{4.0 -2.0} {2.0 0.0}}}] \
	[list Float float float {{{4.0 -2.0} {2.0 0.0}}}] \
	[list Integer int int {{{4 -2} {2 0}}}] \
	[list Long long long {{{4 -2} {2 0}}}]]


testMatrixScalar multiply $types


######################################################################
####
#  FIXME:  xxx[] multiply(xxx[][], xxx[])


######################################################################
####
#  FIXME:  xxx[] multiply(xxx[], xxx[][])

######################################################################
####
#  Test out: xxx[][] multiply(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 - 10.0i 4.0 + 0.0i} {-4.0 + 0.0i 0.0 - 2.0i}}}] \
	[list Double double double {{{3.0 -2.0} {2.0 -1.0}}}] \
	[list Float float float {{{3.0 -2.0} {2.0 -1.0}}}] \
	[list Integer int int {{{3 -2} {2 -1}}}] \
	[list Long long long {{{3 -2} {2 -1}}}]]


testMatrixMatrix multiply $types


######################################################################
####
#  Test out: xxx[][] multiply(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 - 8.0i 0.0 + 2.0i} {0.0 + 2.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{4.0 1.0} {1.0 0.0}}}] \
	[list Float float float {{{4.0 1.0} {1.0 0.0}}}] \
	[list Integer int int {{{4 1} {1 0}}}] \
	[list Long long long {{{4 1} {1 0}}}]]


testMatrixMatrix multiplyElements $types

######################################################################
####
##  Test out: xxx[][] negative(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{-2.0 + 2.0i -1.0 - 1.0i} {1.0 + 1.0i 0.0 + 0.0i}}} ] \
	[list Double double double {{{-2.0 1.0} {-1.0 -0.0}}}] \
	[list Float float float {{{-2.0 1.0} {-1.0 -0.0}}}] \
	[list Integer int int {{{-2 1} {-1 0}}}] \
	[list Long long long {{{-2 1} {-1 0}}}]]


testMatrix negative $types

######################################################################
####
##  FIXME: xxx[][] shiftArithmetic(xxx[][], int)

######################################################################
####
##  FIXME: xxx[][] shiftLogical(xxx[][], int)

######################################################################
####
#  Test out subtract(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 - 0.0i 0.0 + 0.0i} {0.0 + 0.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{0.0 0.0} {0.0 0.0}}}] \
	[list Float float float {{{0.0 0.0} {0.0 0.0}}}] \
	[list Integer int int {{{0 0} {0 0}}}] \
	[list Long long long {{{0 0} {0 0}}}]]

testMatrixMatrix subtract $types

######################################################################
####
##  FIXME: double[][] toDoubleMatrix(xxx[][])

######################################################################
####
##  FIXME: float[][] toFloatMatrix(xxx[][])

######################################################################
####
##  FIXME: int[][] toIntegerMatrix(xxx[][])

######################################################################
####
##  FIXME: xxx[][] toMatrixFromArray(xxx[], int, int)

######################################################################
####
##  FIXME: toString(xxx[][])

######################################################################
####
##  FIXME: toString(xxx[][], ArrayStringFormat)

######################################################################
####
##  FIXME: xxx trace(xxx[][])

######################################################################
####
##  Test out: xxx[][] transpose(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i -1.0 - 1.0i} {1.0 + 1.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{2.0 1.0} {-1.0 0.0}}}] \
	[list Float float float {{{2.0 1.0} {-1.0 0.0}}}] \
	[list Integer int int {{{2.0 1.0} {-1.0 0.0}}}] \
	[list Long long long {{{2 1} {-1 0}}}]]


testMatrix transpose $types

######################################################################
####
##  FIXME: within(xxx[][], xxx[][], xxx)


######################################################################
####
##  FIXME: within(xxx[][], xxx[][], xxx[][])
