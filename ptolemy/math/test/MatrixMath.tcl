# Tests for the *ArrayMath and *MatrixMath classes
#
# @Author: Christopher Hylands (tests only)
#
# @Version $Id$
#
# @Copyright (c) 2002-2008 The Regents of the University of California.
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

if {[string compare jdkStackTrace [info procs jdkStackTrace]] == 1} then { 
    source [file join $PTII util testsuite jdkTools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Complex numbers to be used
set complex0 [java::new ptolemy.math.Complex 0.0 0.0]
set complex1 [java::new ptolemy.math.Complex 2.0 -2.0]
set complex2 [java::new ptolemy.math.Complex 1.0 1.0]
set complex3 [java::new ptolemy.math.Complex -1.0 -1.0]
set complex4 [java::new ptolemy.math.Complex 0.0 0.0]
set complex5 [java::new ptolemy.math.Complex 3.0 -3.0]
set complex6 0
set complex7 1
set complex8 2

set complex0array [java::new {ptolemy.math.Complex[]} {0}]
set complex2array [java::new {ptolemy.math.Complex[]} {2} \
	[list $complex1 $complex2]]
set complex3array [java::new {ptolemy.math.Complex[]} {3} \
	[list $complex1 $complex2 $complex3]]
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

set complexBinaryOperation \
	[java::new ptolemy.math.test.TestComplexBinaryOperation]
set complexUnaryOperation \
	[java::new ptolemy.math.test.TestComplexUnaryOperation]


set double0 0.0 
set double1 2.0
set double2 4.0
set double6 0
set double7 1
set double8 2

set double0array [java::new {double[]} {0}]
set double2array [java::new {double[]} {2} [list 2.0 -1.0]]
set double3array [java::new {double[]} {3} [list 2.0 -1.0 1.0]]
set double4array [java::new {double[]} {4} [list 2.0 -1.0 1.0 0.0]]


set double1_1 [java::new {double[][]} 1 [list [list 2.0]]]
set double2_2 [java::new {double[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 0.0]]]
# 2x2 array with non-zero elements
set double2_2nonzero [java::new {double[][]} {2 2} [list [list 2.0 -1.0] \
	                               [list 1.0 3.0]]]

set doubleBinaryOperation \
	[java::new ptolemy.math.test.TestDoubleBinaryOperation]
set doubleUnaryOperation \
	[java::new ptolemy.math.test.TestDoubleUnaryOperation]



set float0 0.0
set float1 2.0
set float2 4.0
set float6 0
set float7 1
set float8 2

set float0array [java::new {float[]} {0}]
set float2array [java::new {float[]} {2} [list 2.0 -1.0]]
set float3array [java::new {float[]} {3} [list 2.0 -1.0 1.0]]
set float4array [java::new {float[]} {4} [list 2.0 -1.0 1.0 0.0]]
set float1_1 [java::new {float[][]} 1 [list 2.0]]
set float2_2 [java::new {float[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 0.0]]]
# 2x2 array with non-zero elements
set float2_2nonzero [java::new {float[][]} {2 2} [list [list 2.0 -1.0] \
                                       [list 1.0 3.0]]]

set floatBinaryOperation \
	[java::new ptolemy.math.test.TestFloatBinaryOperation]
set floatUnaryOperation \
	[java::new ptolemy.math.test.TestFloatUnaryOperation]

# Fraction numbers to be used
set fraction0 [java::new ptolemy.math.Fraction 0 1]
set fraction1 [java::new ptolemy.math.Fraction 2 1]
set fraction2 [java::new ptolemy.math.Fraction 1 2]
set fraction3 [java::new ptolemy.math.Fraction -1 -2]
set fraction4 [java::new ptolemy.math.Fraction 0 1]
set fraction2array [java::new {ptolemy.math.Fraction[]} {2} \
	[list $fraction1 $fraction2]]
set fraction2_2 [java::new {ptolemy.math.Fraction[][]} {2} [list \
	[list $fraction1 $fraction2] \
	[list $fraction3 $fraction4]]]


set int0 0
set int1 2
set int2 4
set int6 0
set int7 1
set int8 2

set int0array [java::new {int[]} {0}]
set int2array [java::new {int[]} {2} [list 2 -1]]
set int3array [java::new {int[]} {3} [list 2 -1 1]]
set int4array [java::new {int[]} {4} [list 2 -1 1 0]]
# array of length 2  with power of two elements
set int2powerof2array [java::new {int[]} {2} [list 32 16]]

set int1_1 [java::new {int[][]} 1 [list 2]]
set int2_2 [java::new {int[][]} {2 2} [list [list 2 -1] \
                                       [list 1 0]]]
# 2x2 array with non-zero elements
set int2_2nonzero [java::new {int[][]} {2 2} [list [list 2 -1] \
                                       [list 1 3]]]

# 2x2 matrix with power of two elements
set int2_2powerof2 [java::new {int[][]} {2 2} [list [list 32 16] \
                                       [list 8 4]]]

set intBinaryOperation \
	[java::new ptolemy.math.test.TestIntegerBinaryOperation]
set intUnaryOperation \
	[java::new ptolemy.math.test.TestIntegerUnaryOperation]

set long0 0
set long1 2
set long2 4
set long6 0
set long7 1
set long8 2

set long0array [java::new {long[]} {0}]
set long2array [java::new {long[]} {2} [list 2 -1]]
set long3array [java::new {long[]} {3} [list 2 -1 1]]
set long4array [java::new {long[]} {4} [list 2 -1 1 0]]
# array of length 2  with power of two elements
set long2powerof2array [java::new {long[]} {2} [list 32 16]]

set long1_1 [java::new {long[][]} 1 [list 2]]
set long2_2 [java::new {long[][]} {2 2} [list [list 2 -1] \
                                       [list 1 0]]]

# 2x2 array with non-zero elements
set long2_2nonzero [java::new {long[][]} {2 2} [list [list 2 -1] \
                                       [list 1 3]]]

# 2x2 matrix with power of two elements
set long2_2powerof2 [java::new {long[][]} {2 2} [list [list 32 16] \
                                       [list 8 4]]]

set longBinaryOperation \
	[java::new ptolemy.math.test.TestLongBinaryOperation]
set longUnaryOperation \
	[java::new ptolemy.math.test.TestLongUnaryOperation]


# Test an operation that takes two arguments
# This proc is rather complex because it is fairly flexible, we usually
# call this proc from other wrapper procs below.
#
# Arguments:
#    op - The operation to be tested, for example "add"
#    types - a list of lists of types, where each element of the list
#            contains four subelements: 
#              The base matrix type, which would go in 
#                    ptolemy.math.xxxMatrixMath
#              The base type, for example double or ptolemy.math.Complex
#              The base name of the variable to use, for example double 
#              The expected results
#    matrixSize - the suffix of the variable name that contains the
#                the test data. If matrixSize is 2_2, then long2_2, int2_2
#                etc. should exist
#    opSignature - the signature of the op, usually something like
#                  {[list $op "$t\[\]\[\]" $t]}
#    arg1 - The first argument to pass to $op, for example: {[subst $$matrix]}
#
proc testMatrixMath {op types matrixSize opSignature \
	arg1 arg2 {arg3 {}} {baseclass MatrixMath}} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test testMatrixMatrix-$op \
		"$m.MatrixMath.[subst $opSignature]" {
	    set matrix ${v}${matrixSize}

	    # Strip off the x_ from the matrixSize
	    set array ${v}[string range $matrixSize 2 end]array


	    set binaryOperation ${v}BinaryOperation
	    set unaryOperation ${v}UnaryOperation
	    global $matrix $array ${v}0 ${v}1 $binaryOperation $unaryOperation

	    # We could be smarter here and use Tcl's variable number
	    # of arguments facility, but the substitution is bad enough
	    # as it is, so we have three different branches depending
	    # on the number of args.
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
			    [subst $arg2] [subst $arg3] ]
		}
	    }


	    # Call the appropriate toString method
	    if [catch {java::info dimensions $matrixResults}] {
		# Could be a number like 2.0
		set stringResults $matrixResults
	    } else {
		# The basetype of the result.  If matrixResults is
		# double[], then this will be double.
		# If matrixResults is ptolemy.math.Complex[], then
		# this will be Complex
		set resultsBaseType [java::info baseclass $matrixResults]

		if {"$resultsBaseType" == "ptolemy.math.Complex" } {
		    set resultsBaseType Complex
		}

		if {"$resultsBaseType" == "ptolemy.math.Fraction" } {
		    set resultsBaseType Fraction
		}
		if {"$resultsBaseType" == "int" } {
		    set resultsBaseType Integer
		}

		# Capitalize
		set resultsBaseType "[string toupper [string range $resultsBaseType 0 0]][string range $resultsBaseType 1 end]"


		# Depending on the number of dimensions, call the
		# proper toString
		switch [java::info dimensions $matrixResults] {
		    0 {
			set stringResults [$matrixResults toString]
		    }
		    1 {
			set stringResults \
				[java::call ptolemy.math.${resultsBaseType}ArrayMath \
				toString $matrixResults]	
		    }
		    default {
			set stringResults \
				[java::call ptolemy.math.${resultsBaseType}MatrixMath \
				toString $matrixResults]	
		    }
		}
	    }
	    regsub -all {,} $stringResults {} stringAsList
	    #puts "got: $stringAsList"
	    #puts "expected: $expectedResults"
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
#    matrixSize - the suffix of the variable name that contains the
#                the test data. If matrixSize is 2_2, then long2_2, int2_2
#                etc. should exist

proc testArrayMathArray {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]"]} \
	    {[subst $$array]} {} {} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, and an array
# like xxx[] add(xxx[], xxx[])
proc testArrayMathArrayArray {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]" "$t\[\]"]} \
	    {[subst $$array]} {[subst $$array]} {} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, an array and an array
# like boolean within(xxx[], xxx[], xxx[])
proc testArrayMathArrayArrayArray {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize \
	    {[list $op "$t\[\]" "$t\[\]" "$t\[\]"]} \
  	{[subst $$array]} {[subst $$array]} {[subst $$array]} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, an array and a scalar
# like boolean within(xxx[], xxx[], scalar)
proc testArrayMathArrayArrayScalar {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]" "$t\[\]" $t]} \
	    {[subst $$array]} {[subst $$array]} {[subst $${v}1]} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, and an int
# like xxx[] shiftArithmetic(xxx[] int)
proc testArrayMathArrayInt {op types {matrixSize 2_2} {intValue 1}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]" int]} \
	    {[subst $$array]} [list $intValue] {} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, and a scalar
# like xxx[] add(xxx[], xxx)
proc testArrayMathArrayScalar {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]" $t]} \
	    {[subst $$array]} {[subst $${v}1]} {} ArrayMath
}

# Test a *ArrayMath  operation that takes an array, a scalar and a scalar
# like xxx[] limit(xxx[], xxx, xxx)
proc testArrayMathArrayScalarScalar {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]" $t $t]} \
	    {[subst $$array]} {[subst $${v}0]} {[subst $${v}1]} ArrayMath
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
#    matrixSize - the suffix of the variable name that contains the
#                the test data. If matrixSize is 2_2, then long2_2, int2_2
#                etc. should exist
proc testArrayMatrix {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize \
	{[list $op "$t\[\]" "$t\[\]\[\]"]} \
	{[subst $$array]} {[subst $$matrix]}
}

# Test a *MatrixMath operation that takes an array, an int and an int
# like xxx[][] toMatrixFromArray(xxx[], int, int)
proc testArrayIntInt {op types {matrixSize 2_2} {intValue1 1} {intValue2 2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]" int int]} {[subst $$array]} [list $intValue1] [list $intValue2]
}

# Test an operation that takes an int
# like xxx[][] identity(int)
proc testInt {op types {matrixSize 2_2} {intValue 1}} {
    testMatrixMath $op $types $matrixSize {[list $op int]} [list $intValue] {}
}

# Test an operation that takes a matrix
# like allocCopy(long[][])
proc testMatrix {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]"]} {[subst $$matrix]} {}
}

# Test an operation that takes a nonzero matrix
# like allocCopy(long[][])
proc testNonZeroMatrix {op types {matrixSize 2_2nonzero}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]"]} {[subst $$matrix]} {}
}

# Test an operation that takes a matrix and an array
# like multiply(xxx[][], xxx[])
proc testMatrixArray {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]" "$t\[\]"]} {[subst $$matrix]} {[subst $$array]}
}

# Test an operation that takes a matrix and an int
# like xxx[][] shiftArithmetic(xxx[][], int)
proc testMatrixInt {op types {matrixSize 2_2} {intValue 1}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]" int]} {[subst $$matrix]} [list $intValue]
}

# Test an operation that takes a matrix and a double
# like xxx[][] shiftArithmetic(xxx[][], double)
proc testMatrixDouble {op types {matrixSize 2_2} {doubleValue 2.0}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]" double]} {[subst $$matrix]} [list $doubleValue]
}

# Test an operation that takes a matrix and an int
# like xxx[] fromMatrixToArray(xxx[][], int, int)
proc testMatrixIntInt {op types {matrixSize 2_2} {intValue1 1} {intValue2 2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]" int int]} {[subst $$matrix]} [list $intValue1] [list $intValue2]
}


# Test an operation that takes a matrix and a matrix
# like add(long[][], long[][])
proc testMatrixMatrix {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]" "$t\[\]\[\]"]} {[subst $$matrix]} {[subst $${v}${matrixSize}]}
}


# Test an operation that takes a matrix, matrix and a matrix
# like boolean within(xxx[][], xxx[][], xxx[][])
proc testMatrixMatrixMatrix {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize \
	    {[list $op "$t\[\]\[\]" "$t\[\]\[\]" "$t\[\]\[\]"]} \
	    {[subst $$matrix]} {[subst $${v}${matrixSize}]} {[subst $$matrix]}
}

# Test an operation that takes a matrix, matrix and a scalar
# like boolean within(xxx[][], xxx[][], xxx)
proc testMatrixMatrixScalar {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize \
	    {[list $op "$t\[\]\[\]" "$t\[\]\[\]" $t]} \
	    {[subst $$matrix]} {[subst $${v}${matrixSize}]} \
	    {[subst $${v}1]}
}


# Test an operation that takes a matrix and a scalar,
# like add(long[][], long)
proc testMatrixScalar {op types {matrixSize 2_2}} {
    testMatrixMath $op $types $matrixSize {[list $op "$t\[\]\[\]" $t]} {[subst $$matrix]} {[subst $${v}1]}
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
#  *ArrayMath Test out: xxx[] add(xxx[], xxx[])

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
#  Test out: xxx[] allocCopy(xxx[])

set types [list \
	[list Double double double {{2.0 -1.0}}] \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}] \
        [list Fraction ptolemy.math.Fraction fraction {{2/1 1/2}}]]

testArrayMathArray allocCopy $types

######################################################################
####
#  *ArrayMath Test out: xxx[] append(xxx[], xxx[]) with 0 length array

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{}}] \
	[list Double double double {{}}] \
	[list Float float float {{}}] \
	[list Integer int int {{}}] \
	[list Long long long {{}}]]

testArrayMathArrayArray append $types 0_0

######################################################################
####
#  *ArrayMath Test out: xxx[] append(xxx[], xxx[]) with 0 length

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
#  FIXED (FIXME): *ArrayMath Test out: xxx[] append(xxx[], int, int, xxx[], int, int) - See append tests in ComplexArrayMath.tcl, DoubleArrayMath.tcl, FloatArrayMath.tcl, IntegerArrayMath.tcl, and LongArrayMath.tcl because the function takes 5 arguments


######################################################################
####
#  Test out *ArrayMath applyBinaryOperation(XXXBinaryOperation, xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 + 0.0i -1.0 + 3.0i}}] \
	[list Double double double {{0.0 -3.0}}] \
	[list Float float float {{0.0 -3.0}}] \
	[list Integer int int {{0 -3}}] \
	[list Long long long {{0 -3}}]]

testMatrixMath applyBinaryOperation $types 2_2 \
	{[list applyBinaryOperation ptolemy.math.${m}BinaryOperation $t\[\] $t]} \
	{[subst $$binaryOperation]} \
	{[subst $$array]} {[subst $${v}1]} ArrayMath

######################################################################
####
#  Test out *ArrayMath applyBinaryOperation(XXXBinaryOperation, xxx, xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 + 0.0i 1.0 - 3.0i}}] \
	[list Double double double {{0.0 -3.0}}] \
	[list Float float float {{0.0 -3.0}}] \
	[list Integer int int {{0 -3}}] \
	[list Long long long {{0 -3}}]]

testMatrixMath applyBinaryOperation $types 2_2 \
	{[list applyBinaryOperation ptolemy.math.${m}BinaryOperation $t $t\[\]]} \
	{[subst $$binaryOperation]} \
	{[subst $${v}1]} {[subst $$array]} ArrayMath

######################################################################
####
#  Test out: *ArrayMath applyBinaryOperation(XXXBinaryOperation, xxx[], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 + 0.0i 0.0 + 0.0i}}] \
	[list Double double double {{0.0 0.0}}] \
	[list Float float float {{0.0 0.0}}] \
	[list Integer int int {{0 0}}] \
	[list Long long long {{0 0}}]]

testMatrixMath applyBinaryOperation $types 2_2 \
	{[list applyBinaryOperation ptolemy.math.${m}BinaryOperation $t\[\] $t\[\]]} \
	{[subst $$binaryOperation]} \
	{[subst $$array]} {[subst $$array]} ArrayMath

######################################################################
####
#  Test out *ArrayMath applyUnaryOperation(XXXUnaryOperation, xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{-2.0 + 2.0i -1.0 - 1.0i}}] \
	[list Double double double {{-2.0 1.0}}] \
	[list Float float float {{-2.0 1.0}}] \
	[list Integer int int {{-2 1}}] \
	[list Long long long {{-2 1}}]]

testMatrixMath applyUnaryOperation $types 2_2 \
	{[list applyUnaryOperation ptolemy.math.${m}UnaryOperation $t\[\]]} \
	{[subst $$unaryOperation]} \
	{[subst $$array]} {} ArrayMath

######################################################################
####
#  Test out applyBinaryOperation(XXXBinaryOperation, xxx, xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 + 0.0i 1.0 - 3.0i} {3.0 - 1.0i 2.0 - 2.0i}}}] \
	[list Double double double {{{0.0 3.0} {1.0 2.0}}}] \
	[list Float float float {{{0.0 3.0} {1.0 2.0}}}] \
	[list Integer int int {{{0 3} {1 2}}}] \
	[list Long long long {{{0 3} {1 2}}}]]

testMatrixMath applyBinaryOperation $types 2_2 \
    {[list applyBinaryOperation ptolemy.math.${m}BinaryOperation $t $t\[\]\[\]]} \
	{[subst $$binaryOperation]} \
	{[subst $${v}1]} {[subst $$matrix]} MatrixMath

######################################################################
####
#  Test out applyBinaryOperation(XXXBinaryOperation, xxx[][], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 + 0.0i -1.0 + 3.0i} {-3.0 + 1.0i -2.0 + 2.0i}}}] \
	[list Double double double {{{0.0 -3.0} {-1.0 -2.0}}}] \
	[list Float float float {{{0.0 -3.0} {-1.0 -2.0}}}] \
	[list Integer int int {{{0 -3} {-1 -2}}}] \
	[list Long long long {{{0 -3} {-1 -2}}}]]

testMatrixMath applyBinaryOperation $types 2_2 \
	{[list applyBinaryOperation ptolemy.math.${m}BinaryOperation \
	      $t\[\]\[\] $t]} \
	{[subst $$binaryOperation]} \
        {[subst $$matrix]} {[subst $${v}1]} MatrixMath

######################################################################
####
#  Test out applyBinaryOperation(XXXBinaryOperation, xxx[][], xxx[][])

set types [list \
        [list Complex ptolemy.math.Complex complex \
	{{{0.0 + 0.0i 1.0 - 3.0i} {3.0 - 1.0i 2.0 - 2.0i}}}] \
	[list Double double double {{{0.0 3.0} {1.0 2.0}}}] \
	[list Float float float {{{0.0 3.0} {1.0 2.0}}}] \
	[list Integer int int {{{0 3} {1 2}}}] \
	[list Long long long {{{0 3} {1 2}}}]]

testMatrixMath applyBinaryOperation $types 2_2 \
	{[list applyBinaryOperation ptolemy.math.${m}BinaryOperation \
	      $t $t\[\]\[\]]} \
	{[subst $$binaryOperation]} \
	{[subst $${v}1]} {[subst $$matrix]} MatrixMath

######################################################################
####
#  Test out applyUnaryOperation(XXXUnaryOperation, xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{-2.0 + 2.0i -1.0 - 1.0i} {1.0 + 1.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{-2.0 1.0} {-1.0 0.0}}}] \
	[list Float float float {{{-2.0 1.0} {-1.0 0.0}}}] \
	[list Integer int int {{{-2 1} {-1 0}}}] \
	[list Long long long {{{-2 1} {-1 0}}}]]

testMatrixMath applyUnaryOperation $types 2_2 \
    {[list applyUnaryOperation ptolemy.math.${m}UnaryOperation $t\[\]\[\]]} \
	{[subst $$unaryOperation]} \
	{[subst $$matrix]} {} MatrixMath

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
#  FIXED (FIXME): crop(final Complex[][] matrix, final int rowStart, final int colStart,final int rowSpan, final int colSpan) - See ComplexMatrixMath.tcl, DoubleMatrixMath.tcl, FloatMatrixMath.tcl, IntegerMatrixMath.tcl, and LongMatrixMath.tcl for tests.

######################################################################
####
#  *ArrayMath Test out: xxx[] divide(xxx[], xxx)

set types [list \
        [list Complex ptolemy.math.Complex complex \
	{{1.0 + 0.0i 0.0 + 0.5i}}] \
	[list Double double double {{1.0 -0.5}}] \
	[list Float float float {{1.0 -0.5}}] \
	[list Integer int int {{1 0}}] \
	[list Long long long {{1 0}}]]

testArrayMathArrayScalar divide $types

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

testArrayMathArrayArray divideElements $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] divide(xxx[][], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{1.0 + 0.0i 0.0 + 0.5i} {0.0 - 0.5i 0.0 + 0.0i}}}] \
	[list Double double double {{{1.0 -0.5} {0.5 0.0}}}] \
	[list Float float float {{{1.0 -0.5} {0.5 0.0}}}] \
	[list Integer int int {{{1 0} {0 0}}}] \
	[list Long long long {{{1 0} {0 0}}}]]

testMatrixScalar divide $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] divide(xxx[][], xxx[][])

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
	[list Long long long {{2 -1 1 0}}] \
        [list Fraction ptolemy.math.Fraction fraction {{2/1 1/2 1/2 0/1}}]]

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
	[list Long long long {{2 -1}}] \
        [list Fraction ptolemy.math.Fraction fraction {{2/1 1/2}}]]

testMatrixIntInt fromMatrixToArray $types 2_2 1 2

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i -1.0 - 1.0i}}] \
	[list Double double double {{2.0 1.0}}] \
	[list Float float float {{2.0 1.0}}] \
        [list Integer int int {{2 1}}] \
	[list Long long long {{2 1}}] \
        [list Fraction ptolemy.math.Fraction fraction {{2/1 1/2}}]]


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
#  *ArrayMath Test out: xxx l2norm(xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex {3.46410161514}] \
	[list Double double double {2.44948974278}] \
	[list Float float float {2.44948983192}]]

testArrayMathArray l2norm $types 4_4

######################################################################
####
#  *ArrayMath Test out: xxx[] limit(xxx[], int, int)

# Note: Complex[] limit(Complex[], Complex, Complex), Added to 
# ComplexArrayMath.tcl because the test below will use
# complex0 and complex1 which will trigger an exception:
# "Complex.limit requires that bottom lie below and to the left of top."

set types [list \
	[list Double double double {{2.0 0.0 1.0 0.0}}] \
	[list Float float float {{2.0 0.0 1.0 0.0}}] \
	[list Integer int int {{2 0 1 0}}] \
	[list Long long long {{2 0 1 0}}]] \

testArrayMathArrayScalarScalar limit $types 4_4

######################################################################
####
#  void matrixCopy(xxx[][], xxx[][]); Tests are written in the
#  5 test files for each data type.

######################################################################
####
#  void matrixCopy(xxx[][], xxx[][], ...); Tests are written in the
#  5 test files for each data type.

######################################################################
####
#  *ArrayMath Test out: xxx[] modulo(xxx[], xxx)

set types [list \
	[list Integer int int {{0 -1}}] \
	[list Long long long {{0 -1}}]]

testArrayMathArrayScalar modulo $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] modulo(xxx[][], xxx)

set types [list \
	[list Integer int int {{{0 -1} {1 1}}} ] \
	[list Long long long {{{0 -1} {1 1}}}] \
	[list Double double double {{{0.0 -1.0} {1.0 1.0}}}]]

testMatrixScalar modulo $types 2_2nonzero


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
#  *ArrayMath Test out: xxx[] multiply(xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 - 8.0i 4.0 + 0.0i}}] \
	[list Double double double {{4.0 -2.0}}] \
	[list Float float float {{4.0 -2.0}}] \
	[list Integer int int {{4 -2}}] \
	[list Long long long {{4 -2}}]]

testArrayMathArrayScalar multiply $types

######################################################################
####
#  *MatrixMath Test out: xxx[][] multiply(xxx[][], xxx)

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
#  *MatrixMath Test out: xxx[][] multiply(xxx[][], double)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{4.0 - 4.0i 2.0 + 2.0i} {-2.0 - 2.0i 0.0 + 0.0i}}}]]

testMatrixDouble multiply $types

######################################################################
####
#  *ArrayMath Test out: xxx[] multiply(xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 - 8.0i 0.0 + 2.0i}}] \
	[list Double double double {{4.0 1.0}}] \
	[list Float float float {{4.0 1.0}}] \
	[list Integer int int {{4 1}}] \
	[list Long long long {{4 1}}]]

testArrayMathArrayArray multiply $types

######################################################################
####
#  *MatrixMath Test out: xxx[] multiply(xxx[][], xxx[])

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
#  Test out: xxx[][] multiplyElements(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.0 - 8.0i 0.0 + 2.0i} {0.0 + 2.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{4.0 1.0} {1.0 0.0}}}] \
	[list Float float float {{{4.0 1.0} {1.0 0.0}}}] \
	[list Integer int int {{{4 1} {1 0}}}] \
	[list Long long long {{{4 1} {1 0}}}] \
        [list Fraction ptolemy.math.Fraction fraction {{{4/1 1/4} {1/4 0/1}}}]]

testMatrixMatrix multiplyElements $types

######################################################################
####
##  *ArrayMath Test out: xxx[] negative(xxx[])

set types [list \
        [list Complex ptolemy.math.Complex complex \
	{{-2.0 + 2.0i -1.0 - 1.0i}} ] \
	[list Double double double {{-2.0 1.0}}] \
	[list Float float float {{-2.0 1.0}}] \
	[list Integer int int {{-2 1}}] \
	[list Long long long {{-2 1}}] \
        [list Fraction ptolemy.math.Fraction fraction {{-2/1 -1/2}}]]

testArrayMathArray negative $types

######################################################################
####
##  *MatrixMath Test out: xxx[][] negative(xxx[][])

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
#  *ArrayMath Test out: xxx[] normalize(xxx[])

# FIXME: Missing Complex[] normalize(Complex[])

set types [list \
	[list Double double double \
	{{0.8164965809277261 -0.4082482904638631 0.4082482904638631 0.0}}] \
	[list Float float float \
	{{0.81649655 -0.40824828 0.40824828 0.0}}]]

testArrayMathArray normalize $types 4_4

######################################################################
####
##  *MatrixMath Test out: xxx[][] orthogonalizeColumns(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 0.2 + 0.2i} {-1.0 - 1.0i -0.4 + 0.4i}}}] \
	[list Double double double {{{2.0 -0.2} {1.0 0.4}}}] \
	[list Float float float {{{2.0 -0.2} {1.0 0.4}}}]]


testMatrix orthogonalizeColumns $types

######################################################################
####
##  *MatrixMath Test out: xxx[][] orthonormalizeColumns(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 0.2 + 0.2i} {-1.0 - 1.0i -0.4 + 0.4i}}}] \
	[list Double double double {{{2.0 -0.2} {1.0 0.4}}}] \
	[list Float float float {{{2.0 -0.2} {1.0 0.4}}}]]


testMatrix orthonormalizeColumns $types

######################################################################
####
##  *MatrixMath Test out: xxx[][] orthogonalizeRows(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i} {-0.2 - 0.2i -0.4 + 0.4i}}}] \
	[list Double double double {{{2.0 -1.0} {0.2 0.4}}}] \
	[list Float float float {{{2.0 -1.0} {0.2 0.4}}}]]


testMatrix orthogonalizeRows $types

######################################################################
####
##  *MatrixMath Test out: xxx[][] orthonormalizeRows(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{0.6324555320336759 - 0.6324555320336759i 0.31622776601683794 + 0.31622776601683794i} {-0.31622776601683783 - 0.31622776601683783i -0.6324555320336759 + 0.6324555320336759i}}}] \
	[list Double double double {{{0.8944271909999159 -0.4472135954999579} {0.44721359549995787 0.894427190999916}}}] \
	[list Float float float {{{0.8944272 -0.4472136} {0.4472136 0.89442724}}}]]


testMatrix orthonormalizeRows $types

######################################################################
####
##  *ArrayMath Test out: xxx[] padMiddle(xxx[], int)

# First, pad arrays of length 2 out to length 6
set types [list \
	[list Complex ptolemy.math.Complex complex {{2.0 - 2.0i 0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i 0.0 + 0.0i 1.0 + 1.0i}}] \
	[list Double double double {{2.0 0.0 0.0 0.0 0.0 -1.0}}] \
	[list Float float float {{2.0 0.0 0.0 0.0 0.0 -1.0}}] \
	[list Integer int int {{2 0 0 0 0 -1}}] \
	[list Long long long {{2 0 0 0 0 -1}}]]

testArrayMathArrayInt padMiddle $types 2_2 6


# Then pad arrays of length 3 out to length 6
set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i 0.0 + 0.0i 0.0 + 0.0i 1.0 + 1.0i -1.0 - 1.0i}}] \
	[list Double double double {{2.0 -1.0 0.0 0.0 -1.0 1.0}}] \
	[list Float float float {{2.0 -1.0 0.0 0.0 -1.0 1.0}}] \
	[list Integer int int {{2 -1 0 0 -1 1}}] \
	[list Long long long {{2 -1 0 0 -1 1}}]]

testArrayMathArrayInt padMiddle $types 2_3 6


# Then pad arrays of length 3 down to length 3, which is really a resize.
set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i -1.0 - 1.0i}}] \
	[list Double double double {{2.0 -1.0 1.0}}] \
	[list Float float float {{2.0 -1.0 1.0}}] \
	[list Integer int int {{2 -1 1}}] \
	[list Long long long {{2 -1 1}}]]

testArrayMathArrayInt padMiddle $types 2_3 3

######################################################################
####
##  *ArrayMath Test out: xxx[] resize(xxx[], int)

# resize a 2 element array to 4 elements
set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i 0.0 + 0.0i 0.0 + 0.0i}}] \
	[list Double double double {{2.0 -1.0 0.0 0.0}}] \
	[list Float float float {{2.0 -1.0 0.0 0.0}}] \
	[list Integer int int {{2 -1 0 0}}] \
	[list Long long long {{2 -1 0 0}}]]

testArrayMathArrayInt resize $types 2_2 4

# resize a 4 element array to 2 elements
set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{2.0 - 2.0i 1.0 + 1.0i}}] \
	[list Double double double {{2.0 -1.0}}] \
	[list Float float float {{2.0 -1.0}}] \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]]

testArrayMathArrayInt resize $types 2_4 2

# resize a 4 element array to 0 elements
set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{}}] \
	[list Double double double {{}}] \
	[list Float float float {{}}] \
	[list Integer int int {{}}] \
	[list Long long long {{}}]]

testArrayMathArrayInt resize $types 2_4 0

# resize a 0 element array to 0 elements
set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{}}] \
	[list Double double double {{}}] \
	[list Float float float {{}}] \
	[list Integer int int {{}}] \
	[list Long long long {{}}]]

testArrayMathArrayInt resize $types 2_0 0


######################################################################
####
##  *ArrayMath Test out: xxx[] scale(xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 - 8.0i 4.0 + 0.0i}}] \
	[list Double double double {{4.0 -2.0}}] \
	[list Float float float {{4.0 -2.0}}] \
	[list Integer int int {{4 -2}}] \
	[list Long long long {{4 -2}}]]

testArrayMathArrayScalar scale $types

######################################################################
####
##  *ArrayMath Test out: xxx[] shiftArithmetic(xxx[], int)

set types [list \
	[list Integer int int {{4 -2}}] \
	[list Long long long {{4 -2}}]]

testArrayMathArrayInt shiftArithmetic $types 2_2 1

set types [list \
	[list Integer int int {{8 -4}}] \
	[list Long long long {{8 -4}}]]

testArrayMathArrayInt shiftArithmetic $types 2_2 2

set types [list \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]]

testArrayMathArrayInt shiftArithmetic $types 2_2 0

set types [list \
	[list Integer int int {{1 2147483647}}]]

testArrayMathArrayInt shiftArithmetic $types 2_2 -1

set types [list \
	[list Integer int int {{8 4}}] \
	[list Long long long {{8 4}}]] 

testArrayMathArrayInt shiftArithmetic $types 2_2powerof2 -2

######################################################################
####
##  *MatrixMath Test out: xxx[][] shiftArithmetic(xxx[][], int)

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
##  *ArrayMath Test out: xxx[] shiftLogical(xxx[], int)

set types [list \
	[list Integer int int {{4 -2}}] \
	[list Long long long {{4 -2}}]]

testArrayMathArrayInt shiftLogical $types 2_2 1

set types [list \
	[list Integer int int {{8 -4}}] \
	[list Long long long {{8 -4}}]]

testArrayMathArrayInt shiftLogical $types 2_2 2

set types [list \
	[list Integer int int {{2 -1}}] \
	[list Long long long {{2 -1}}]]

testArrayMathArrayInt shiftLogical $types 2_2 0

set types [list \
	[list Integer int int {{1 -1}}]]

testArrayMathArrayInt shiftLogical $types 2_2 -1

set types [list \
	[list Integer int int {{0 -1}}] \
	[list Long long long {{0 -1}}]] 

testArrayMathArrayInt shiftLogical $types 2_2 -2

######################################################################
####
##  *MatrixMath Test out: xxx[][] shiftLogical(xxx[][], int)

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
#  *ArrayMath Test out: xxx[] subtract(xxx[], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{0.0 + 0.0i 0.0 + 0.0i}}] \
	[list Double double double {{0.0 0.0}}] \
	[list Float float float {{0.0 0.0}}] \
	[list Integer int int {{0 0}}] \
	[list Long long long {{0 0}}]]

testArrayMathArrayArray subtract $types

######################################################################
####
#  *MatrixMath Test out subtract(xxx[][], xxx[][])

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
#  *ArrayMath Test out sumOfSquares(xxx[])

# FIXME: no Complex sumOfSquares(Complex [])

set types [list \
	[list Double double double {{6.0}}] \
	[list Float float float {{6.0}}] \
	[list Integer int int {{6}}] \
	[list Long long long {{6}}]]

testArrayMathArray sumOfSquares $types 4_4

######################################################################
####
##  (NEW) *ArrayMath Test out Complex[] toComplexArray(xxx[])

set types [list \
	[list Double double double {{2.0 + 0.0i -1.0 + 0.0i}}] \
	[list Float float float {{2.0 + 0.0i -1.0 + 0.0i}}] \
	[list Integer int int {{2.0 + 0.0i -1.0 + 0.0i}}] \
	[list Long long long {{2.0 + 0.0i -1.0 + 0.0i}}]]

testArrayMathArray toComplexArray $types

######################################################################
####
##  (NEW) *ArrayMath Test out Complex[] toComplexMatrix(xxx[][])

set types [list \
	[list Double double double {{{2.0 + 0.0i -1.0 + 0.0i} {1.0 + 0.0i 0.0 + 0.0i}}}] \
	[list Float float float {{{2.0 + 0.0i -1.0 + 0.0i} {1.0 + 0.0i 0.0 + 0.0i}}}] \
	[list Integer int int {{{2.0 + 0.0i -1.0 + 0.0i} {1.0 + 0.0i 0.0 + 0.0i}}}] \
	[list Long long long {{{2.0 + 0.0i -1.0 + 0.0i} {1.0 + 0.0i 0.0 + 0.0i}}}]]

testMatrix toComplexMatrix $types

######################################################################
####
##  *ArrayMath Test out double[] toDoubleArray(xxx[])

set types [list \
	[list Float float float {{2.0 -1.0}}] \
	[list Integer int int {{2.0 -1.0}}] \
	[list Long long long {{2.0 -1.0}}]]

testArrayMathArray toDoubleArray $types

######################################################################
####
##  *MatrixMath Test out double[][] toDoubleMatrix(xxx[][])

set types [list \
	[list Float float float {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Integer int int {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Long long long {{{2.0 -1.0} {1.0 0.0}}}]]

testMatrix toDoubleMatrix $types

######################################################################
####
##  *ArrayMath Test out: float[] toFloatArray(xxx[])

set types [list \
	[list Double double double {{2.0 -1.0}}] \
	[list Integer int int {{2.0 -1.0}}] \
	[list Long long long {{2.0 -1.0}}]]

testArrayMathArray toFloatArray $types

######################################################################
####
##  *MatrixMath Test out: float[][] toFloatMatrix(xxx[][])

set types [list \
	[list Double double double {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Integer int int {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Long long long {{{2.0 -1.0} {1.0 0.0}}}]]

testMatrix toFloatMatrix $types

######################################################################
####
##  *ArrayMath Test out int[] toIntegerArray(xxx[])

set types [list \
	[list Double double double {{2 -1}}] \
	[list Float float float {{2 -1}}] \
	[list Long long long {{2 -1}}]]

testArrayMathArray toIntegerArray $types

######################################################################
####
##  *MatrixMath: int[][] toIntegerMatrix(xxx[][])

set types [list \
	[list Double double double {{{2 -1} {1 0}}}] \
	[list Float float float {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrix toIntegerMatrix $types

######################################################################
####
##  *ArrayMath Test out long[] toLongArray(xxx[])

set types [list \
	[list Double double double {{2 -1}}] \
	[list Float float float {{2 -1}}] \
	[list Integer int int {{2 -1}}]]

testArrayMathArray toLongArray $types


######################################################################
####
##  *MatrixMath: long[][] toLongMatrix(xxx[][])

set types [list \
	[list Double double double {{{2 -1} {1 0}}}] \
	[list Float float float {{{2 -1} {1 0}}}] \
	[list Integer int int {{{2 -1} {1 0}}}]]

testMatrix toLongMatrix $types

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

# The x_ is stripped off
testArrayIntInt toMatrixFromArray $types x_4 2 2

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i} {1.0 + 1.0i}}}] \
	[list Double double double {{{2.0} {-1.0}}}] \
	[list Float float float {{{2.0} {-1.0}}}] \
	[list Integer int int {{{2} {-1}}}] \
	[list Long long long {{{2} {-1}}}]]

# The x_ is stripped off
testArrayIntInt toMatrixFromArray $types x_4 2 1

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i}}}] \
	[list Double double double {{{2.0 -1.0}}}] \
	[list Float float float {{{2.0 -1.0}}}] \
	[list Integer int int {{{2 -1}}}] \
	[list Long long long {{{2 -1}}}]]

# The x_ is stripped off
testArrayIntInt toMatrixFromArray $types x_4 1 2

######################################################################
####
##  *MatrixMath: toString(xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i} {-1.0 - 1.0i 0.0 + 0.0i}}}] \
	[list Double double double {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Float float float {{{2.0 -1.0} {1.0 0.0}}}] \
	[list Integer int int {{{2 -1} {1 0}}}] \
	[list Long long long {{{2 -1} {1 0}}}]]

testMatrix toString $types

######################################################################
####
##  FIXED (FIXME): toString(xxx[][], ArrayStringFormat)
##  Call toString(xxx[][]), which in turn calls toString(array, ArrayStringFormat.javaASFormat).

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 - 2.0i 1.0 + 1.0i} {-1.0 - 1.0i 3.0 - 3.0i}}}] \
	[list Double double double {{{2.0 -1.0} {1.0 3.0}}}] \
	[list Float float float {{{2.0 -1.0} {1.0 3.0}}}] \
	[list Integer int int {{{2 -1} {1 3}}}] \
	[list Long long long {{{2 -1} {1 3}}}]]

testNonZeroMatrix toString $types

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
##  *ArrayMath Test out boolean within(xxx[], xxx[], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex {1}] \
	[list Double double double {1}] \
	[list Float float float {1}] \
	[list Integer int int {1}] \
	[list Long long long {1}]]

testArrayMathArrayArrayScalar within $types

######################################################################
####
##  *MatrixMath Test out boolean within(xxx[][], xxx[][], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex {1}] \
	[list Double double double {1}] \
	[list Float float float {1}] \
	[list Integer int int {1}] \
	[list Long long long {1}]]

testMatrixMatrixScalar within $types

######################################################################
####
##  *ArrayMath Test out boolean within(xxx[], xxx[], xxx[])

# Note that it is ok to have negative values for complex numbers
# but not the others
set types [list \
        [list Complex ptolemy.math.Complex complex {1}] \
  	[list Double double double {0}] \
  	[list Float float float {0}] \
  	[list Integer int int {0}] \
  	[list Long long long {0}]]

testArrayMathArrayArrayArray within $types

######################################################################
####
##  *MatrixMath Test out boolean within(xxx[][], xxx[][], xxx[][])

# The matrices contain negative elements, which will cause
# this to return within(xxx[][], xxx[][], xxx[][]) to return false

# Note that it is ok to have negative values for complex numbers
# but not the others

set types [list \
	[list Complex ptolemy.math.Complex complex {1}] \
	[list Double double double {0}] \
	[list Float float float {0}] \
	[list Integer int int {0}] \
	[list Long long long {0}]]

testMatrixMatrixMatrix within $types
