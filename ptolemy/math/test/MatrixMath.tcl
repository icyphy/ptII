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

set complex2array [java::new {ptolemy.math.Complex[]} {2} \
	[list $complex1 $complex2]]
set complex4array [java::new {ptolemy.math.Complex[]} {4} \
	[list $complex1 $complex2 $complex3 $complex4]]

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
set double2array [java::new {double[]} {2} [list 2.0 -1.0]]
set double4array [java::new {double[]} {4} [list 2.0 -1.0 1.0 0.0]]

set double1_1 [java::new {double[][]} 1 [list [list 2.0]]]
set double2_2 [java::new {double[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 0.0]]]
# 2x2 array with non-zero elements
set double2_2nonzero [java::new {double[][]} {2 2} [list [list 2.0 -1.0] \
	                               [list 1.0 3.0]]]


set float1 2
set float2array [java::new {float[]} {2} [list 2.0 -1.0]]
set float4array [java::new {float[]} {4} [list 2.0 -1.0 1.0 0.0]]
set float1_1 [java::new {float[][]} 1 [list 2.0]]
set float2_2 [java::new {float[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 0.0]]]
# 2x2 array with non-zero elements
set float2_2nonzero [java::new {float[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 3.0]]]

set int1 2
set int2array [java::new {int[]} {2} [list 2 -1]]
set int4array [java::new {int[]} {4} [list 2 -1 1 0]]
set int1_1 [java::new {int[][]} 1 [list 2]]
set int2_2 [java::new {int[][]} {2 2} [list [list 2 -1] \
                                       [list 1 0]]]
# 2x2 array with non-zero elements
set int2_2nonzero [java::new {int[][]} {2 2} [list [list 2 -1] \
                                       [list 1 3]]]

# 2x2 matrix with power of two elements
set int2_2powerof2 [java::new {int[][]} {2 2} [list [list 32 16] \
                                       [list 8 4]]]

set long1 2
set long2array [java::new {long[]} {2} [list 2 -1]]
set long4array [java::new {long[]} {4} [list 2 -1 1 0]]
set long1_1 [java::new {long[][]} 1 [list 2]]
set long2_2 [java::new {long[][]} {2 2} [list [list 2 -1] \
                                       [list 1 0]]]

# 2x2 array with non-zero elements
set long2_2nonzero [java::new {long[][]} {2 2} [list [list 2 -1] \
                                       [list 1 3]]]

# 2x2 matrix with power of two elements
set long2_2powerof2 [java::new {long[][]} {2 2} [list [list 32 16] \
                                       [list 8 4]]]



# Test an operation that takes two arguments
# This proc is rather complex because it is fairly flexible, we usually
# call this proc from other wrapper procs below
#
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
#    opSignature - the signature of the op, usually something like
#                  {[list $op "$t\[\]\[\]" $t]}
#    arg1 - The first argument to pass to $op, for example: {[subst $$matrix]}
#
proc testMatrixMath {op types arraySize opSignature \
	arg1 arg2 {arg3 {}} {baseclass MatrixMath}} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test testMatrixMatrix-$op \
		"$m.MatrixMath.[subst $opSignature]" {
	    set matrix ${v}${arraySize}
	    set array ${v}[string range $arraySize 0 0]array
	    global $matrix $array ${v}1
	    if { $arg2 == {}} {
		set matrixResults [java::call \
			ptolemy.math.${m}${baseclass} \
			[subst $opSignature] [subst $arg1]]
	    } else {
		if { $arg3 == {}} {
		    set matrixResults [java::call \
			    ptolemy.math.${m}${baseclass} \
			    [subst $opSignature] [subst $arg1] [subst $arg2]]
		} else {
		    set matrixResults [java::call \
			    ptolemy.math.${m}${baseclass} \
			    [subst $opSignature] [subst $arg1] \
			    [subst $arg2] [subst $arg3]]
		}
	    }

	    if [catch {java::info dimensions $matrixResults}] {
		# Could be a number like 2.0
		set stringResults $matrixResults
	    } else {
		# Depending on the number of dimensions, call the
		# proper toString
		switch [java::info dimensions $matrixResults] {
		    0 {
			set stringResults [$matrixResults toString]
		    }
		    1 {
			set stringResults \
				[java::call ptolemy.math.${m}ArrayMath \
				toString $matrixResults]	
		    }
		    default {
			set stringResults \
				[java::call ptolemy.math.${m}MatrixMath \
				toString $matrixResults]	
		    }
		}
	    }
	    regsub -all {,} $stringResults {} stringAsList
	    puts "got: $stringAsList"
	    puts "expected: $expectedResults"
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}


######################################################################
#### The methods below are for testing *ArrayMath methods

# Test a *ArrayMath  operation that takes an array
# like xxx[] bitwiseComplement(xxx[])
# Arguments:
#    op - The operation to be tested, for example "multiply"
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

proc testArrayMathArray {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]"} {[subst $$array]} {} {} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, and a scalar
# like xxx[] add(xxx[], xxx[])
proc testArrayMathArrayArray {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]" "$t\[\]"} {[subst $$array]} {[subst $$array]} {} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, and a scalar
# like xxx[] add(xxx[], xxx)
proc testArrayMathArrayScalar {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]" $t} {[subst $$array]} {[subst $${v}1]} {} ArrayMath
}


######################################################################
####
# The methods below are for testing *MatrixMath methods

# Test a *MatrixMath operation that takes a matrix and an array
# like multiply(xxx[][], xxx[])
# Arguments:
#    op - The operation to be tested, for example "multiply"
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
proc testArrayMatrix {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]" "$t\[\]\[\]"} {[subst $$array]} {[subst $$matrix]}
}

# Test a *MatrixMath operation that takes an array, an int and an int
# like xxx[][] toMatrixFromArray(xxx[], int, int)
proc testArrayIntInt {op types {arraySize 2_2} {intValue1 1} {intValue2 2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]" int int} {[subst $$array]} [list $intValue1] [list $intValue2]
}

# Test an operation that takes an int
# like xxx[][] identity(int)
proc testInt {op types {arraySize 2_2} {intValue 1}} {
    testMatrixMath $op $types $arraySize {[list $op int]} [list $intValue] {}
}

# Test an operation that takes a matrix
# like allocCopy(long[][])
proc testMatrix {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]\[\]"]} {[subst $$matrix]} {}
}

# Test an operation that takes a matrix and an array
# like multiply(xxx[][], xxx[])
proc testMatrixArray {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]\[\]" "$t\[\]"]} {[subst $$matrix]} {[subst $$array]}
}

# Test an operation that takes a matrix and an int
# like xxx[][] shiftArithmetic(xxx[][], int)
proc testMatrixInt {op types {arraySize 2_2} {intValue 1}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]\[\]" int]} {[subst $$matrix]} [list $intValue]
}

# Test an operation that takes a matrix and an int
# like xxx[] fromMatrixToArray(xxx[][], int, int)
proc testMatrixIntInt {op types {arraySize 2_2} {intValue1 1} {intValue2 2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]\[\]" int int]} {[subst $$matrix]} [list $intValue1] [list $intValue2]
}


# Test an operation that takes a matrix and a matrix
# like add(long[][], long[][])
proc testMatrixMatrix {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]\[\]" "$t\[\]\[\]"]} {[subst $$matrix]} {[subst $${v}${arraySize}]}
}

# Test an operation that takes a matrix and a scalar,
# like add(long[][], long)
proc testMatrixScalar {op types {arraySize 2_2}} {
    testMatrixMath $op $types $arraySize {[list $op "$t\[\]\[\]" $t]} {[subst $$matrix]} {[subst $${v}1]}
}


######################################################################
####
#  *ArrayMath Test out: xxx[] add(xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{4.0 - 4.0i 3.0 - 1.0i}}] \
	[list Double double double {{4.0 1.0}}] \
	[list Float float float {{4.0 1.0}}] \
	[list Integer int int {{4 1}}] \
	[list Long long long {{4 1}}]]


testArrayMathArrayScalar add $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] add(xxx[][], xxx)

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
#  *ArrayMath Test out: xxx[] add(xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{4.0 - 4.0i 2.0 + 2.0i}}] \
	[list Double double double {{4.0 -2.0}}] \
	[list Float float float {{4.0 -2.0}}] \
	[list Integer int int {{4 -2}}] \
	[list Long long long {{4 -2}}]]


testArrayMathArrayArray add $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] add(xxx[][], xxx[][])

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
#  *ArrayMath Test out: xxx[] append(xxx[], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i 2.0 - 2.0i 1.0 + 1.0i}}] \
	[list Double double double {{2.0 -1.0 2.0 -1.0}}] \
	[list Float float float {{2.0 -1.0 2.0 -1.0}}] \
	[list Integer int int {{2 -1 2 -1}}] \
	[list Long long long {{2 -1 2 -1}}]]
testArrayMathArrayArray append $types

######################################################################
####
#  FIXME: *ArrayMath xxx[] append(xxx[], int, int, xxx[], int, int )

######################################################################
####
#  FIXME: *ArrayMath applyBinaryOperation(XXXBinaryOperation, xxx, xxx[])

######################################################################
####
#  FIXME: *ArrayMath applyBinaryOperation(XXXBinaryOperation, xxx[], xxx[])

######################################################################
####
#  FIXME: *ArrayMath applyUnaryOperation(XXXUnaryOperation, xxx[])


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
#  *ArrayMath Test out: xxx[] bitwiseAnd(xxx[], xxx)

set types [list \
	[list Integer int int {{2 2}}] \
	[list Long long long {{2 2}}]]

testArrayMathArrayScalar bitwiseAnd $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] bitwiseAnd(xxx[][], xxx)

set types [list \
	[list Integer int int {{{2 2} {0 0}}}] \
	[list Long long long {{{2 2} {0 0}}}]]

testMatrixScalar bitwiseAnd $types

######################################################################
####
#  *ArrayMath Test out: xxx[] bitwiseAnd(xxx[], xxx[])

set types [list \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]]

testArrayMathArrayArray bitwiseAnd $types


######################################################################
####
#  *MatrixMath Test out: xxx[][] bitwiseAnd(xxx[][], xxx[][])

set types [list \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrixMatrix bitwiseAnd $types

######################################################################
####
#  *ArrayMath Test out: xxx[] bitwiseComplement(xxx[])

set types [list \
	[list Integer int int {{-3 0}}] \
	[list Long long long {{-3 0}}]]

testArrayMathArray bitwiseComplement $types


######################################################################
####
#  *MatrixMath Test out: xxx[][] bitwiseComplement(xxx[][])

set types [list \
	[list Integer int int {{{-3 0} {-2 -1}}}] \
	[list Long long long {{{-3 0} {-2 -1}}}]]

testMatrix bitwiseComplement $types

######################################################################
####
#  *ArrayMath Test out: xxx[] bitwiseOr(xxx[], xxx)

set types [list \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]] 

testArrayMathArrayScalar bitwiseOr $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] bitwiseOr(xxx[][], xxx)

set types [list \
	[list Integer int int {{{2 -1} {3 2}}}] \
	[list Long long long {{{2 -1} {3 2}}}]] 

testMatrixScalar bitwiseOr $types

######################################################################
####
#  *ArrayMath Test out: xxx[] bitwiseOr(xxx[], xxx[])

set types [list \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]] 

testArrayMathArrayArray bitwiseOr $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] bitwiseOr(xxx[][], xxx[][])

set types [list \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrixMatrix bitwiseOr $types

######################################################################
####
#  *ArrayMath Test out: xxx[] bitwiseXor(xxx[], xxx)

set types [list \
	[list Integer int int {{0 -3}}] \
	[list Long long long {{0 -3}}]] 

testArrayMathArrayScalar bitwiseXor $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] bitwiseXor(xxx[][], xxx)

set types [list \
	[list Integer int int {{{0 -3} {3 2}}}] \
	[list Long long long {{{0 -3} {3 2}}}]]

testMatrixScalar bitwiseXor $types

######################################################################
####
#  *ArrayMath Test out: xxx[] bitwiseXor(xxx[], xxx[])

set types [list \
	[list Integer int int {{0 0}}] \
	[list Long long long {{0 0}}]] 

testArrayMathArrayArray bitwiseXor $types

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
#  FIXME: *ArrayMath Test out: xxx[] divide(xxx[], xxx)

######################################################################
####
#  *ArrayMath Test out: xxx[] divide(xxx[], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{1.0 + 0.0i 1.0 + 0.0i}}] \
	[list Double double double {{1.0 1.0}}] \
	[list Float float float {{1.0 1.0}}] \
	[list Integer int int {{1 1}}] \
	[list Long long long {{1 1}}]]

testArrayMathArrayArray divide $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] divideElements(xxx[][], xxx[][])

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
#  *ArrayMath Test out: xxx[] dotProduct(xxx[], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{10.0 + 0.0i}] \
	[list Double double double {5.0}] \
	[list Float float float {5.0}] \
	[list Integer int int {5}] \
	[list Long long long {5}]]

testArrayMathArrayArray dotProduct $types


######################################################################
####
#  Test out: xxx[] fromMatrixToArray(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i -1.0 - 1.0i 0.0 + 0.0i}}] \
	[list Double double double {{2.0 -1.0 1.0 0.0}}] \
	[list Float float float {{2.0 -1.0 1.0 0.0}}] \
	[list Integer int int {{2 -1 1 0}}] \
	[list Long long long {{2 -1 1 0}}]]

testMatrix fromMatrixToArray $types

######################################################################
####
#  Test out: xxx[] fromMatrixToArray(xxx[][], int, int)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i}}] \
	[list Double double double {{2.0 -1.0}}] \
	[list Float float float {{2.0 -1.0}}] \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]]

testMatrixIntInt fromMatrixToArray $types 2_2 1 2

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i -1.0 - 1.0i}}] \
	[list Double double double {{2.0 1.0}}] \
	[list Float float float {{2.0 1.0}}] \
	[list Integer int int {{2 1}}] \
	[list Long long long {{2 1}}]]

testMatrixIntInt fromMatrixToArray $types 2_2 2 1

######################################################################
####
#  Test out: identity(int)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{}}] \
	[list Double double double {{}}] \
	[list Float float float {{}}] \
	[list Integer int int {{}}] \
	[list Long long long {{}}]]

testInt identity $types 2_2 0

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{1.0 + 0.0i}}}] \
	[list Double double double {{{1.0}}}] \
	[list Float float float {{{1.0}}}] \
	[list Integer int int {{{1}}}] \
	[list Long long long {{{1}}}]]

testInt identity $types 2_2 1

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{1.0 + 0.0i 0.0 + 0.0i} {0.0 + 0.0i 1.0 + 0.0i}}}] \
	[list Double double double {{{1.0 0.0} {0.0 1.0}}}] \
	[list Float float float {{{1.0 0.0} {0.0 1.0}}}] \
	[list Integer int int {{{1 0} {0 1}}}] \
	[list Long long long {{{1 0} {0 1}}}]]

testInt identity $types 2_2 2

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{1.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i} {0.0 + 0.0i 1.0 + 0.0i 0.0 + 0.0i} {0.0 + 0.0i 0.0 + 0.0i 1.0 + 0.0i}}}] \
	[list Double double double {{{1.0 0.0 0.0} {0.0 1.0 0.0} {0.0 0.0 1.0}}}] \
	[list Float float float {{{1.0 0.0 0.0} {0.0 1.0 0.0} {0.0 0.0 1.0}}}] \
	[list Integer int int {{{1 0 0} {0 1 0} {0 0 1}}}] \
	[list Long long long {{{1 0 0} {0 1 0} {0 0 1}}}]]

testInt identity $types 2_2 3

######################################################################
####
#  FIXME: *ArrayMath Test out: xxx[] limit(xxx[], int int)


######################################################################
####
#  FIXME: void matrixCopy(xxx[][], xxx[][])


######################################################################
####
#  FIXME: void matrixCopy(xxx[][], xxx[][], ...)

######################################################################
####
#  *ArrayMath Test out: xxx[] modulo(xxx[], xxx)

set types [list \
	[list Integer int int {{0 -1}}] \
	[list Long long long {{0 -1}}]]

testArrayMathArrayScalar modulo $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] moduloElements(xxx[][], xxx)

set types [list \
	[list Integer int int {{{0 -1} {1 1}}} ] \
	[list Long long long {{{0 -1} {1 1}}}]]


testMatrixScalar moduloElements $types 2_2nonzero


######################################################################
####
#  *ArrayMath Test out: xxx[] modulo(xxx[], xxx[])

set types [list \
	[list Integer int int {{0 0}}] \
	[list Long long long {{0 0}}]]

testArrayMathArrayArray modulo $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] modulo(xxx[][], xxx[][])

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
#  Test out: xxx[] multiply(xxx[][], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 - 10.0i 4.0 + 0.0i}}] \
	[list Double double double {{3.0 -2.0}}] \
	[list Float float float {{3.0 -2.0}}] \
	[list Integer int int {{3 -2}}] \
	[list Long long long {{3 -2}}]]

testMatrixArray multiply $types

######################################################################
####
#  Test out: xxx[] multiply(xxx[], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 - 6.0i -4.0 + 0.0i}}] \
	[list Double double double {{5.0 2.0}}] \
	[list Float float float {{5.0 2.0}}] \
	[list Integer int int {{5 2}}] \
	[list Long long long {{5 2}}]]

testArrayMatrix multiply $types

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
##  Test out: xxx[][] shiftArithmetic(xxx[][], int)

set types [list \
	[list Integer int int {{{4 -2} {2 0}}}] \
	[list Long long long {{{4 -2} {2 0}}}]]

testMatrixInt shiftArithmetic $types 2_2 1

set types [list \
	[list Integer int int {{{8 -4} {4 0}}}] \
	[list Long long long {{{8 -4} {4 0}}}]]

testMatrixInt shiftArithmetic $types 2_2 2

set types [list \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrixInt shiftArithmetic $types 2_2 0

set types [list \
	[list Integer int int {{{1 2147483647} {0 0}}}]]

testMatrixInt shiftArithmetic $types 2_2 -1

set types [list \
	[list Integer int int {{{8 4} {2 1}}}] \
	[list Long long long {{{8 4} {2 1}}}]] 

testMatrixInt shiftArithmetic $types 2_2powerof2 -2

######################################################################
####
##  Test out: xxx[][] shiftLogical(xxx[][], int)

set types [list \
	[list Integer int int {{{4 -2} {2 0}}}] \
	[list Long long long {{{4 -2} {2 0}}}]]
testMatrixInt shiftLogical $types

set types [list \
	[list Integer int int {{{8 -4} {4 0}}}] \
	[list Long long long {{{8 -4} {4 0}}}]]
testMatrixInt shiftLogical $types 2_2 2

set types [list \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]
testMatrixInt shiftLogical $types 2_2 0

set types [list \
	[list Integer int int {{{1 -1} {0 0}}}] \
	[list Long long long {{{1 -1} {0 0}}}]]
testMatrixInt shiftLogical $types 2_2 -1

set types [list \
	[list Integer int int {{{0 -1} {0 0}}}] \
	[list Long long long {{{0 -1} {0 0}}}]]
testMatrixInt shiftLogical $types 2_2 -2

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
##  Test out: xxx[][] toMatrixFromArray(xxx[], int, int)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i} {-1.0 - 1.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Float float float {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testArrayIntInt toMatrixFromArray $types 4 2 2

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i} {1.0 + 1.0i}}}] \
	[list Double double double {{{2.0} {-1.0}}}] \
	[list Float float float {{{2.0} {-1.0}}}] \
	[list Integer int int {{{2} {-1}}}] \
	[list Long long long {{{2} {-1}}}]]

testArrayIntInt toMatrixFromArray $types 4 2 1

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i}}}] \
	[list Double double double {{{2.0 -1.0}}}] \
	[list Float float float {{{2.0 -1.0}}}] \
	[list Integer int int {{{2 -1}}}] \
	[list Long long long {{{2 -1}}}]]

testArrayIntInt toMatrixFromArray $types 4 1 2

######################################################################
####
##  FIXME: toString(xxx[][])

######################################################################
####
##  FIXME: toString(xxx[][], ArrayStringFormat)

######################################################################
####
##  Test out: xxx trace(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{2.0 - 2.0i}] \
	[list Double double double {2.0}] \
	[list Float float float {2.0}] \
	[list Integer int int {2}] \
	[list Long long long {2}]]

testMatrix trace $types

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{5.0 - 5.0i}] \
	[list Double double double {5.0}] \
	[list Float float float {5.0}] \
	[list Integer int int {5}] \
	[list Long long long {5}]]

testMatrix trace $types 2_2nonzero

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
##  FIXME: boolean within(xxx[][], xxx[][], xxx)


######################################################################
####
##  FIXME: boolean within(xxx[][], xxx[][], xxx[][])
