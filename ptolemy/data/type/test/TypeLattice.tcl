# Tests for TypeLattice
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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


######################################################################
####
# 
test TypeLattice-1.0 {compare} {
    set tokInt [java::new ptolemy.data.IntToken]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $tokInt $tokDou] [$lattice compare $tokDou $tokInt]
} {-1 1}

test TypeLattice-1.1 {compare} {
    set tokLong [java::new ptolemy.data.LongToken]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $tokLong $tokDou] [$lattice compare $tokDou $tokDou]
} {2 0}

test TypeLattice-1.2 {bounds} {
    set tokInt [java::new ptolemy.data.IntToken]
    set type1 [$tokInt getType]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set type2 [$tokDou getType]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {double double int int}

test TypeLattice-1.3 {bounds} {
    set tokLong [java::new ptolemy.data.LongToken]
    set type1 [$tokLong getType]
    set tokDou [java::new ptolemy.data.DoubleToken]
    set type2 [$tokDou getType]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {scalar scalar int int}

######################################################################
####
# 

test TypeLattice-2.0 {compare test token} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set doubleToken [java::new ptolemy.data.DoubleToken]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $testToken $doubleToken] [$lattice compare $doubleToken $testToken]
} {2 2}

test TypeLattice-2.1 {compare two user types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set testToken2 [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $testToken $testToken2] [$lattice compare $testToken2 $testToken]
} {0 0}

test TypeLattice-2.2 {compare user and unknown} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $testToken $unknownType] [$lattice compare $unknownType $testToken]
} {1 -1}

test TypeLattice-2.3 {compare user and general} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set unknownType [java::field ptolemy.data.type.BaseType GENERAL]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $testToken $unknownType] [$lattice compare $unknownType $testToken]
} {-1 1}

test TypeLattice-2.4 {compare user and structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set arrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $testToken $arrayType] [$lattice compare $doubleToken $arrayType]
} {2 -1}

test TypeLattice-2.5 {bounds test token} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set doubleToken [java::new ptolemy.data.DoubleToken]
    set type2 [$doubleToken getType]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general unknown unknown}

test TypeLattice-2.6 {bounds two user types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set testToken2 [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type2 [$testToken2 getType]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {test test test test}

test TypeLattice-2.7 {bounds user and unknown} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set type2 $unknownType
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {test test unknown unknown}

test TypeLattice-2.8 {bounds user and general} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set unknownType [java::field ptolemy.data.type.BaseType GENERAL]
    set type2 $unknownType
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general test test}

test TypeLattice-2.9 {bounds user and structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [$testToken getType]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set arrayType [java::new ptolemy.data.type.ArrayType $str]
    set type2 $arrayType
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
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
    list [$lattice compare $int $strArrayType] [$lattice compare $int $intArrayType]
} {-1 -1}

test TypeLattice-3.1 {compare structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $strArrayType $intArrayType] [$lattice compare $intArrayType $strArrayType]
} {1 -1}

test TypeLattice-3.2 {compare user and unknown} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set unknownType [java::field ptolemy.data.type.BaseType UNKNOWN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $intArrayType $unknownType] [$lattice compare $unknownType $intArrayType]
} {1 -1}

test TypeLattice-3.3 {compare user and general} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set unknownType [java::field ptolemy.data.type.BaseType GENERAL]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $intArrayType $unknownType] [$lattice compare $unknownType $intArrayType]
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
    list [$lattice compare $doubleArrayType $r] [$lattice compare $r $doubleArrayType]
} {2 2}

test TypeLattice-3.4.1 {array of strings is greater than a record type} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $strArrayType $r] [$lattice compare $r $strArrayType]
} {1 -1}

test TypeLattice-3.4.2 {string is greater than a record type} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $str $r] [$lattice compare $r $str]
} {1 -1}

test TypeLattice-3.5 {compare array types and basic types} {
    set type1 [java::field ptolemy.data.type.BaseType INT]
    set type2 [java::new ptolemy.data.type.ArrayType $int]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {{{int}} {{int}} int int}

test TypeLattice-3.5.1 {compare array types and basic types} {
    set type1 [java::field ptolemy.data.type.BaseType INT]
    set double [java::field ptolemy.data.type.BaseType DOUBLE]
    set type2 [java::new ptolemy.data.type.ArrayType $double]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {{{double}} {{double}} int int}

test TypeLattice-3.5.2 {compare array types and basic types} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set unk [java::field ptolemy.data.type.BaseType UNKNOWN]
    set unkArrayType [java::new ptolemy.data.type.ArrayType $unk]
    list [[$lattice leastUpperBound $int $unkArrayType] toString] [[$lattice leastUpperBound $unkArrayType $int] toString]
} {{{int}} {{int}}}

test TypeLattice-3.6 {compare structured types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set int [java::field ptolemy.data.type.BaseType INT]
    set type1 [java::new ptolemy.data.type.ArrayType $int]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set type2 [java::new ptolemy.data.type.ArrayType $str]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]

} {{{string}} {{string}} {{int}} {{int}}}

test TypeLattice-3.7 {compare user and unknown} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set type1 [java::new ptolemy.data.type.ArrayType $int]
    set type2 [java::field ptolemy.data.type.BaseType UNKNOWN]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]

} {{{int}} {{int}} unknown unknown}

test TypeLattice-3.8  {compare user and general} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set type1 [java::new ptolemy.data.type.ArrayType $int]
    set type2 [java::field ptolemy.data.type.BaseType GENERAL]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]
} {general general {{int}} {{int}}}

test TypeLattice-3.9 {LUB of record and array of strings is array of strings} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set record [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set arrayStrings [java::new ptolemy.data.type.ArrayType $str]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $record $arrayStrings] toString] [[$lattice leastUpperBound $arrayStrings $record] toString] [[$lattice greatestLowerBound $record $arrayStrings] toString] [[$lattice greatestLowerBound $arrayStrings $record] toString]
} {{{string}} {{string}} {{name = string, value = double}} {{name = string, value = double}}}

test TypeLattice-3.9.1 {LUB of record and array of doubles is array of strings} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set string [java::field ptolemy.data.type.BaseType STRING]
    set double [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $string $double]]

    set record [java::new {ptolemy.data.type.RecordType} $l $v]
    set arrayDoubles [java::new ptolemy.data.type.ArrayType $double]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $record $arrayDoubles] toString] [[$lattice leastUpperBound $arrayDoubles $record] toString] [[$lattice greatestLowerBound $record $arrayDoubles] toString] [[$lattice greatestLowerBound $arrayDoubles $record] toString]
} {{{string}} {{string}} unknown unknown}

test TypeLattice-4.0 {compare scalar and array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $intArrayType $int] [$lattice compare $int $intArrayType]
} {1 -1}

test TypeLattice-4.1 {compare scalar and array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [[$lattice leastUpperBound $intArrayType $int] toString] [[$lattice leastUpperBound $int $intArrayType] toString]
} {{{int}} {{int}}}

test TypeLattice-4.2 {compare scalar and array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set boolean [java::field ptolemy.data.type.BaseType BOOLEAN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $intArrayType $boolean] [$lattice compare $boolean $intArrayType]
} {2 2}

test TypeLattice-4.3 {compare boolean and integer array} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set intArrayType [java::new ptolemy.data.type.ArrayType $int]
    set boolean [java::field ptolemy.data.type.BaseType BOOLEAN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [[$lattice leastUpperBound $intArrayType $boolean] toString] [[$lattice leastUpperBound $boolean $intArrayType] toString]
} {{{scalar}} {{scalar}}}

test TypeLattice-4.4 {compare boolean and int} {
    set int [java::field ptolemy.data.type.BaseType INT]
    set boolean [java::field ptolemy.data.type.BaseType BOOLEAN]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [[$lattice leastUpperBound $int $boolean] toString] [[$lattice leastUpperBound $boolean $int] toString]
} {scalar scalar}
