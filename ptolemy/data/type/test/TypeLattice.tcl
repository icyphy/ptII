# Tests for TypeLattice
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
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
} {2 2}

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
} {2 2}

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
    set str [java::field ptolemy.data.type.BaseType STRING]
    set strArrayType [java::new ptolemy.data.type.ArrayType $str]
    set lattice [java::new ptolemy.data.type.TypeLattice]
    list [$lattice compare $strArrayType $r] [$lattice compare $r $strArrayType]
} {2 2}

test TypeLattice-3.5 {compare structured types and basic types} {
    set testToken [java::new ptolemy.data.type.test.TestToken [java::new java.lang.Object]]
    set type1 [java::field ptolemy.data.type.BaseType INT]
    set type2 [java::new ptolemy.data.type.ArrayType $int]
  
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]

} {general general unknown unknown}

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

test TypeLattice-3.9 {compare different structured types} {
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType DOUBLE]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set type1 [java::new {ptolemy.data.type.RecordType} $l $v]
    set str [java::field ptolemy.data.type.BaseType STRING]
    set type2 [java::new ptolemy.data.type.ArrayType $str]
    set lattice [[java::new ptolemy.data.type.TypeLattice] lattice]
    list [[$lattice leastUpperBound $type1 $type2] toString] [[$lattice leastUpperBound $type2 $type1] toString] [[$lattice greatestLowerBound $type1 $type2] toString] [[$lattice greatestLowerBound $type2 $type1] toString]

} {general general unknown unknown}






