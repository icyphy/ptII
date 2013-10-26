# Tests for TypeLattice
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2013 The Regents of the University of California.
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

# 
#

set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
set generalType [java::field ptolemy.data.type.BaseType GENERAL]
set generalArrayType [java::new ptolemy.data.type.ArrayType $generalType]
set generalSizedArrayType [java::new ptolemy.data.type.ArrayType $generalType 1]
set baseTypes [list \
                   [java::field ptolemy.data.type.BaseType ARRAY_BOTTOM] \
                   [java::field ptolemy.data.type.BaseType BOOLEAN] \
                   [java::field ptolemy.data.type.BaseType BOOLEAN_MATRIX] \
                   [java::field ptolemy.data.type.BaseType UNSIGNED_BYTE] \
                   [java::field ptolemy.data.type.BaseType COMPLEX] \
                   [java::field ptolemy.data.type.BaseType COMPLEX_MATRIX] \
                   [java::field ptolemy.data.type.BaseType DOUBLE] \
                   [java::field ptolemy.data.type.BaseType DOUBLE_MATRIX] \
                   [java::field ptolemy.data.type.BaseType FIX] \
                   [java::field ptolemy.data.type.BaseType UNSIZED_FIX] \
                   [java::field ptolemy.data.type.BaseType SIZED_FIX] \
                   [java::field ptolemy.data.type.BaseType FIX_MATRIX] \
                   [java::field ptolemy.data.type.BaseType INT] \
                   [java::field ptolemy.data.type.BaseType INT_MATRIX] \
                   [java::field ptolemy.data.type.BaseType LONG] \
                   [java::field ptolemy.data.type.BaseType LONG_MATRIX] \
                   [java::field ptolemy.data.type.BaseType OBJECT] \
                   [java::field ptolemy.data.type.BaseType XMLTOKEN] \
                   [java::field ptolemy.data.type.BaseType SCALAR] \
                   [java::field ptolemy.data.type.BaseType MATRIX] \
                   [java::field ptolemy.data.type.BaseType STRING] \
                   [java::field ptolemy.data.type.BaseType EVENT] \
                   [java::field ptolemy.data.type.BaseType PETITE] \
                   [java::field ptolemy.data.type.BaseType NIL] ]

set unsizedArrayTypes {}
foreach type $baseTypes {
    lappend unsizedArrayTypes [java::new ptolemy.data.type.ArrayType $type]
}

set lengthOneArrayTypes {}
foreach type $baseTypes {
    lappend lengthOneArrayTypes [java::new ptolemy.data.type.ArrayType $type 1]
}

set lengthTwoArrayTypes {}
foreach type $baseTypes {
    lappend lengthTwoArrayTypes [java::new ptolemy.data.type.ArrayType $type 2]
}

proc testInvariants {type1 type2} {
    set lattice [java::new ptolemy.data.type.TypeLattice]
    # LUB is commutative
    test TypeLattice-testInvariants-[$type1 toString]-[$type2 toString] {LUB commutative} {[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] equals [java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1]} {1}
    # < is antisymmetric, == and != are symmetric
    set cmp1 [java::call ptolemy.data.type.TypeLattice compare $type1 $type2]
    set cmp2 [java::call ptolemy.data.type.TypeLattice compare $type2 $type1]
    test TypeLattice-testInvariants-[$type1 toString]-[$type2 toString] {compare symmetry} {expr ( $cmp1 == 0 && $cmp2 == 0 ) || ( $cmp1 == 1 && $cmp2 == -1 ) || ( $cmp1 == -1 && $cmp2 == 1 ) || ( $cmp1 == 2 && $cmp2 == 2 )} {1}
    return 0
}

proc testTypeIsLessThan {type1 type2} {
    set lattice [java::new ptolemy.data.type.TypeLattice]
    testInvariants $type1 $type2

    test TypeLattice-testTypeIsLessThan-[$type1 toString]-[$type2 toString] {compare} {java::call ptolemy.data.type.TypeLattice compare $type1 $type2} {-1}
    test TypeLattice-testTypeIsLessThan-[$type1 toString]-[$type2 toString] {compareReverse} {java::call ptolemy.data.type.TypeLattice compare $type2 $type1} {1}

    set lub [java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] 
    test TypeLattice-testTypeIsLessThan-[$type1 toString]-[$type2 toString] {lubCompare1} {java::call ptolemy.data.type.TypeLattice compare $lub $type1} {1}
    test TypeLattice-testTypeIsLessThan-[$type1 toString]-[$type2 toString] {lubCompare2} {java::call ptolemy.data.type.TypeLattice compare $lub $type2} {0}
    test TypeLattice-testTypeIsLessThan-[$type1 toString]-[$type2 toString] {lubEquals1} {$lub equals $type1} {0}
    test TypeLattice-testTypeIsLessThan-[$type1 toString]-[$type2 toString] {lubEquals2} {$lub equals $type2} {1}
}        

proc testTypesEqual {type1 type2} {
    set lattice [java::new ptolemy.data.type.TypeLattice]
    testInvariants $type1 $type2

    test TypeLattice-testTypesEqual-[$type1 toString]-[$type2 toString] {compare} {java::call ptolemy.data.type.TypeLattice compare $type1 $type2} {0}
    test TypeLattice-testTypesEqual-[$type1 toString]-[$type2 toString] {compare} {java::call ptolemy.data.type.TypeLattice compare $type2 $type1} {0}

    set lub [java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] 
    test TypeLattice-testTypesEqual-[$type1 toString]-[$type2 toString] {lubCompare1} {java::call ptolemy.data.type.TypeLattice compare $lub $type1} {0}
    test TypeLattice-testTypesEqual-[$type1 toString]-[$type2 toString] {lubCompare2} {java::call ptolemy.data.type.TypeLattice compare $lub $type2} {0}
    test TypeLattice-testTypesEqual-[$type1 toString]-[$type2 toString] {lubEquals1} {$lub equals $type1} {1}
    test TypeLattice-testTypesEqual-[$type1 toString]-[$type2 toString] {lubEquals2} {$lub equals $type2} {1}
}        

proc testTypesIncomparable {type1 type2} {
    set lattice [java::new ptolemy.data.type.TypeLattice]
    testInvariants $type1 $type2
    if {[$type1 toString] == "arrayBottom" \
	&& [$type2 toString] == "arrayType(arrayBottom,2)"} {
        test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {compare} {java::call ptolemy.data.type.TypeLattice compare $type1 $type2} {-1}
    } else {
        test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {compare} {java::call ptolemy.data.type.TypeLattice compare $type1 $type2} {2}
    }

    set lub [java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] 

    if {[$type1 toString] == "arrayBottom" \
	&& [$type2 toString] == "arrayType(arrayBottom,2)"} {
        test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {lubCompare2} {java::call ptolemy.data.type.TypeLattice compare $lub $type2} {0}
    } else {
        test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {lubCompare2} {java::call ptolemy.data.type.TypeLattice compare $lub $type2} {1}
    }

    test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {lubEquals1} {$lub equals $type1} {0}

    if {[$type1 toString] == "arrayBottom" \
	&& [$type2 toString] == "arrayType(arrayBottom,2)"} {
		test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {lubEquals2} {$lub equals $type2} {1}
    } else {
		test TypeLattice-testTypesIncomparable-[$type1 toString]-[$type2 toString] {lubEquals2} {$lub equals $type2} {0}
    }
}

# Ensure that arrayBottom is less than an array of anything.
set arrayBottom [java::field ptolemy.data.type.BaseType ARRAY_BOTTOM]
foreach type $baseTypes {
	set arrayType [java::new ptolemy.data.type.ArrayType $type]
	testTypeIsLessThan $arrayBottom $arrayType
}

foreach type [concat $baseTypes $unsizedArrayTypes $lengthOneArrayTypes $lengthTwoArrayTypes] {
    testTypeIsLessThan $type $generalType
}

foreach type [concat $baseTypes $unsizedArrayTypes $lengthOneArrayTypes $lengthTwoArrayTypes] {
    testTypeIsLessThan $unknownType $type
}
           
foreach type [concat $baseTypes $unsizedArrayTypes $lengthOneArrayTypes $lengthTwoArrayTypes] {
    testTypesEqual $type $type
}

foreach type $baseTypes {
    set arrayType [java::new ptolemy.data.type.ArrayType $type]
    set sized1ArrayType [java::new ptolemy.data.type.ArrayType $type 1]
    set sized2ArrayType [java::new ptolemy.data.type.ArrayType $type 2]

    testTypeIsLessThan $type $generalArrayType
    testTypeIsLessThan $type $generalSizedArrayType
    testTypeIsLessThan $type $arrayType
    testTypeIsLessThan $type $sized1ArrayType
    testTypesIncomparable $type $sized2ArrayType
    testTypeIsLessThan $sized1ArrayType $arrayType
    testTypeIsLessThan $sized2ArrayType $arrayType
    testTypesIncomparable $sized1ArrayType $sized2ArrayType
}
testTypeIsLessThan $generalArrayType $generalType
testTypeIsLessThan $generalSizedArrayType $generalType

           
        
######################################################################
####
# 
test TypeLattice-1.0 {compare} {
    set tokInt [java::new ptolemy.data.IntToken]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $tokInt $tokDou] [java::call ptolemy.data.type.TypeLattice compare $tokDou $tokInt]
} {-1 1}

test TypeLattice-1.1 {compare} {
    set tokLong [java::new ptolemy.data.LongToken]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $tokLong $tokDou] [java::call ptolemy.data.type.TypeLattice compare $tokDou $tokDou]
} {2 0}

test TypeLattice-1.2 {bounds} {
    set tokInt [java::new ptolemy.data.IntToken]
    set type1 [$tokInt getType]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set type2 [$tokDou getType]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {double double int int}

test TypeLattice-1.3 {bounds} {
    set tokLong [java::new ptolemy.data.LongToken]
    set type1 [$tokLong getType]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set type2 [$tokDou getType]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {scalar scalar int int}

test TypeLattice-1.4 {lub with arrayBottom} {
    set tokDou [java::new ptolemy.data.DoubleToken]
    set double [$tokDou getType]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $arrayBottom $double] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $double $arrayBottom] toString]
} {arrayType(double) arrayType(double)}

test TypeLattice-1.5 {compare double with arrayBottom} {
    set tokDou [java::new ptolemy.data.DoubleToken]
    set double [$tokDou getType]
    list [java::call ptolemy.data.type.TypeLattice compare $arrayBottom $double] [java::call ptolemy.data.type.TypeLattice compare $double $arrayBottom]
} {2 2}

test TypeLattice-1.5 {compare {double} with arrayBottom} {
    set tokDou [java::new ptolemy.data.DoubleToken]
    set double [$tokDou getType]
    set arrayType [java::new ptolemy.data.type.ArrayType $double]
    list [java::call ptolemy.data.type.TypeLattice compare $arrayBottom $arrayType] [java::call ptolemy.data.type.TypeLattice compare $arrayType $arrayBottom]
} {-1 1}


######################################################################
####
# 

test TypeLattice-2.0 {compare test token} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set doubleToken [java::new ptolemy.data.DoubleToken]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $testToken $doubleToken] [java::call ptolemy.data.type.TypeLattice compare $doubleToken $testToken]
} {2 2}

test TypeLattice-2.1 {compare two user types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set testToken2 [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $testToken $testToken2] [java::call ptolemy.data.type.TypeLattice compare $testToken2 $testToken]
} {0 0}

test TypeLattice-2.2 {compare user and unknown} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $testToken $unknownType] [java::call ptolemy.data.type.TypeLattice compare $unknownType $testToken]
} {1 -1}

test TypeLattice-2.3 {compare user and general} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set unknownType [java::field ptolemy.data.type.BaseType GENERAL]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $testToken $unknownType] [java::call ptolemy.data.type.TypeLattice compare $unknownType $testToken]
} {-1 1}

test TypeLattice-2.4 {compare user and structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set arrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $testToken $arrayType] [java::call ptolemy.data.type.TypeLattice compare $doubleToken $arrayType]
} {2 -1}

test TypeLattice-2.5 {bounds test token} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set doubleToken [java::new ptolemy.data.DoubleToken]
    set type2 [$doubleToken getType]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general unknown unknown}

test TypeLattice-2.6 {bounds two user types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set testToken2 [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type2 [$testToken2 getType]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {test test test test}

test TypeLattice-2.7 {bounds user and unknown} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set type2 $unknownType
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {test test unknown unknown}

test TypeLattice-2.8 {bounds user and general} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set unknownType [java::field ptolemy.data.type.BaseType GENERAL]
    set type2 $unknownType
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general test test}

test TypeLattice-2.9 {bounds user and structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set arrayType [java::new ptolemy.data.type.ArrayType $str]
    set type2 $arrayType
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general unknown unknown}

######################################################################
####
# 

test TypeLattice-3.0 {compare structured types and basic types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $int $strArrayType] [java::call ptolemy.data.type.TypeLattice compare $int $intArrayType]
} {-1 -1}

test TypeLattice-3.1 {compare structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $strArrayType $intArrayType] [java::call ptolemy.data.type.TypeLattice compare $intArrayType $strArrayType]
} {1 -1}

test TypeLattice-3.2 {compare user and unknown} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $intArrayType $unknownType] [java::call ptolemy.data.type.TypeLattice compare $unknownType $intArrayType]
} {1 -1}

test TypeLattice-3.3 {compare user and general} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set unknownType [java::field ptolemy.data.type.BaseType GENERAL]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $intArrayType $unknownType] [java::call ptolemy.data.type.TypeLattice compare $unknownType $intArrayType]
} {-1 1}

test TypeLattice-3.4 {compare different structured types} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set double [java::field ptolemy.data.type.BaseType DOUBLE]
    set doubleArrayType [java::new ptolemy.data.type.ArrayType $double]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $doubleArrayType $r] [java::call ptolemy.data.type.TypeLattice compare $r $doubleArrayType]
} {2 2}

test TypeLattice-3.4.1 {array of strings is incomparable with a record type} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::new ptolemy.data.type.ArrayType $str]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $strArrayType $r] [java::call ptolemy.data.type.TypeLattice compare $r $strArrayType]
} {2 2}

test TypeLattice-3.4.2 {string is incomparable with a record type} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $str $r] [java::call ptolemy.data.type.TypeLattice compare $r $str]
} {2 2}

test TypeLattice-3.5 {compare array types and basic types} {
    set type1 [java::field ptolemy.data.type.BaseType INT]
    set type2 [java::new ptolemy.data.type.ArrayType $int]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {arrayType(int) arrayType(int) int int}

test TypeLattice-3.5.1 {compare array types and basic types} {
    set type1 [java::field ptolemy.data.type.BaseType INT]
    set double [java::field ptolemy.data.type.BaseType DOUBLE]
    set type2 [java::new ptolemy.data.type.ArrayType $double]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {arrayType(double) arrayType(double) int int}

test TypeLattice-3.5.2 {compare array types and basic types} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set unk [java::field ptolemy.data.type.BaseType UNKNOWN]
    set unkArrayType [java::new ptolemy.data.type.ArrayType $unk]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $int $unkArrayType] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $unkArrayType $int] toString]
} {arrayType(int) arrayType(int)}

test TypeLattice-3.6 {compare structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set int [java::field ptolemy.data.type.BaseType INT]
    set type1 [java::new ptolemy.data.type.ArrayType $int]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set type2 [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]

} {arrayType(string) arrayType(string) arrayType(int) arrayType(int)}

test TypeLattice-3.7 {compare user and unknown} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set type1 [java::new ptolemy.data.type.ArrayType $int]
    set type2 [java::field ptolemy.data.type.BaseType UNKNOWN]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]

} {arrayType(int) arrayType(int) unknown unknown}

test TypeLattice-3.8  {compare user and general} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set type1 [java::new ptolemy.data.type.ArrayType $int]
    set type2 [java::field ptolemy.data.type.BaseType GENERAL]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type1 $type2] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general arrayType(int) arrayType(int)}

test TypeLattice-3.9 {records and arrays are incomparable} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set record [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set arrayStrings [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::call ptolemy.data.type.TypeLattice lattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $record $arrayStrings] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $arrayStrings $record] toString] [[$lattice greatestLowerBound $record $arrayStrings] toString] [[$lattice greatestLowerBound $arrayStrings $record] toString]
} {arrayType(general) arrayType(general) unknown unknown}

test TypeLattice-4.0 {compare scalar and array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $intArrayType $int] [java::call ptolemy.data.type.TypeLattice compare $int $intArrayType]
} {1 -1}

test TypeLattice-4.1 {compare scalar and array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $intArrayType $int] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $int $intArrayType] toString]
} {arrayType(int) arrayType(int)}

test TypeLattice-4.2 {compare scalar and array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set boolean [java::field ptolemy.data.type.BaseType BOOLEAN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [java::call ptolemy.data.type.TypeLattice compare $intArrayType $boolean] [java::call ptolemy.data.type.TypeLattice compare $boolean $intArrayType]
} {2 2}

test TypeLattice-4.3 {compare boolean and integer array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set boolean [java::field ptolemy.data.type.BaseType BOOLEAN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $intArrayType $boolean] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $boolean $intArrayType] toString]
} {arrayType(scalar) arrayType(scalar)}

test TypeLattice-4.4 {compare boolean and int} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set boolean [java::field ptolemy.data.type.BaseType BOOLEAN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [[java::call ptolemy.data.type.TypeLattice leastUpperBound $int $boolean] toString] [[java::call ptolemy.data.type.TypeLattice leastUpperBound $boolean $int] toString]
} {scalar scalar}
