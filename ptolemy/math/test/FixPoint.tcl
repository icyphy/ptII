# Tests for the FixPoint Class
#
# @Author: Bart Kienhuis, Ed.Willink
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test FixPoint-1.0 {constructors-bigdecimal} {

    set ctor_bigdecimal {ptolemy.math.FixPoint java.math.BigDecimal ptolemy.math.Quantization}

    set bd1 [java::new java.math.BigDecimal "25.5734"]
    set bd2 [java::new java.math.BigDecimal "-18.23"]

    set q_6_2 [java::new ptolemy.math.FixPointQuantization "6.2,saturate,nearest"]
    set q_6_10 [java::new ptolemy.math.FixPointQuantization "6.10,saturate,nearest"]
    set q_6_20 [java::new ptolemy.math.FixPointQuantization "6.20,saturate,nearest"]
    set q_4_4 [java::new ptolemy.math.FixPointQuantization "4.4,saturate,nearest"]
    set q_4_8 [java::new ptolemy.math.FixPointQuantization "4.8,saturate,nearest"]

    set qu_6_2 [java::new ptolemy.math.FixPointQuantization "u6.2,saturate,nearest"]
    set qu_6_10 [java::new ptolemy.math.FixPointQuantization "u6.10,saturate,nearest"]
    set qu_6_20 [java::new ptolemy.math.FixPointQuantization "u6.20,saturate,nearest"]
    set qu_4_4 [java::new ptolemy.math.FixPointQuantization "u4.4,saturate,nearest"]

    # less than on both sides
    set c10 [java::new $ctor_bigdecimal $bd1 $q_6_2]
    set c11 [java::new $ctor_bigdecimal $bd1 $q_6_10]
    set c12 [java::new $ctor_bigdecimal $bd1 $q_6_20]
    set c13 [java::new $ctor_bigdecimal $bd1 $q_4_4]

    set c20 [java::new $ctor_bigdecimal $bd2 $q_6_2]
    set c21 [java::new $ctor_bigdecimal $bd2 $q_6_10]
    set c22 [java::new $ctor_bigdecimal $bd2 $q_6_20]
    set c23 [java::new $ctor_bigdecimal $bd2 $q_4_4]

    set c30 [java::new $ctor_bigdecimal $bd1 $qu_6_2]
    set c31 [java::new $ctor_bigdecimal $bd1 $qu_6_10]
    set c32 [java::new $ctor_bigdecimal $bd1 $qu_6_20]
    set c33 [java::new $ctor_bigdecimal $bd1 $qu_4_4]

    list "
[$bd1 toString] [ $c10 toStringPrecision ] 
[$bd1 toString] [ $c11 toStringPrecision ] 
[$bd1 toString] [ $c12 toStringPrecision ] 
[$bd1 toString] [ $c13 toStringPrecision ] 
[$bd1 toString] [ $c30 toStringPrecision ] 
[$bd1 toString] [ $c31 toStringPrecision ] 
[$bd1 toString] [ $c32 toStringPrecision ] 
[$bd1 toString] [ $c33 toStringPrecision ] 
[$bd2 toString] [ $c20 toStringPrecision ] 
[$bd2 toString] [ $c21 toStringPrecision ] 
[$bd2 toString] [ $c22 toStringPrecision ] 
[$bd2 toString] [ $c23 toStringPrecision ] 
"
} {{
25.5734 25.5(6.2) 
25.5734 25.5732421875(6.10) 
25.5734 25.57339954376220703125(6.20) 
25.5734 7.9375(4.4) 
25.5734 25.5(U6.2) 
25.5734 25.5732421875(U6.10) 
25.5734 25.57339954376220703125(U6.20) 
25.5734 15.9375(U4.4) 
-18.23 -18.25(6.2) 
-18.23 -18.23046875(6.10) 
-18.23 -18.229999542236328125(6.20) 
-18.23 -8.0(4.4) 
}}

####################################################################
test FixPoint-1.0a {constructors-bigdecimal-unsigned-error} {

    set qu_20_32 [java::new ptolemy.math.FixPointQuantization "u20.32,saturate,nearest"]

    catch { set c0 [java::new $ctor_bigdecimal $bd2 $qu_20_32] } msg
    list "
$msg "
} {{
java.lang.ArithmeticException: Attempting to create a unsigned FixPoint from a negative double:-18.23 }}

####################################################################
test FixPoint-1.1 {constructors-fixpoint} {

    set ctor_fixpoint {ptolemy.math.FixPoint ptolemy.math.FixPoint ptolemy.math.Quantization}

    # case 1: 25.5734(6.10) with more bits on both ends
    set q_8_20 [java::new ptolemy.math.FixPointQuantization "8.20,saturate,nearest"]
    set fq1 [java::new $ctor_fixpoint $c21 $q_8_20]

    # case 2: 25.5734(6.10) with more bits on left, fewer bits on right
    set q_8_4 [java::new ptolemy.math.FixPointQuantization "8.4,saturate,nearest"]
    set fq2 [java::new $ctor_fixpoint $c21 $q_8_4]

    # case 3: 25.5734(6.10) with fewer bits on left, more bits on right
    set q_4_20 [java::new ptolemy.math.FixPointQuantization "4.20,saturate,nearest"]
    set fq3 [java::new $ctor_fixpoint $c21 $q_4_20]

    # case 4: 25.5734(6.10) with fewer bits on both sides
    set q_4_4 [java::new ptolemy.math.FixPointQuantization "4.4,saturate,nearest"]
    set fq4 [java::new $ctor_fixpoint $c21 $q_4_4]

    # Signed tests

    # case 1: 25.5734(6.10) with more bits on both ends
    set fq5 [java::new $ctor_fixpoint $c31 $q_8_20]

    # case 2: 25.5734(6.10) with more bits on left, fewer bits on right
    set fq6 [java::new $ctor_fixpoint $c31 $q_8_4]

    # case 3: 25.5734(6.10) with fewer bits on left, more bits on right
    set fq7 [java::new $ctor_fixpoint $c31 $q_4_20]

    # case 4: 25.5734(6.10) with fewer bits on both sides
    set fq8 [java::new $ctor_fixpoint $c31 $q_4_4]

    list "
[$c21 toStringPrecision] -> [$fq1 toStringPrecision]
[$c21 toStringPrecision] -> [$fq2 toStringPrecision]
[$c21 toStringPrecision] -> [$fq3 toStringPrecision]
[$c21 toStringPrecision] -> [$fq4 toStringPrecision]
[$c31 toStringPrecision] -> [$fq5 toStringPrecision]
[$c31 toStringPrecision] -> [$fq6 toStringPrecision]
[$c31 toStringPrecision] -> [$fq7 toStringPrecision]
[$c31 toStringPrecision] -> [$fq8 toStringPrecision]
"
} {{
-18.23046875(6.10) -> -18.23046875(8.20)
-18.23046875(6.10) -> -18.25(8.4)
-18.23046875(6.10) -> -8.0(4.20)
-18.23046875(6.10) -> -8.0(4.4)
25.5732421875(U6.10) -> 25.5732421875(8.20)
25.5732421875(U6.10) -> 25.5625(8.4)
25.5732421875(U6.10) -> 7.99999904632568359375(4.20)
25.5732421875(U6.10) -> 7.9375(4.4)
}}

####################################################################
test FixPoint-1.2 {constructors-double} {
    set ctor_double {ptolemy.math.FixPoint double ptolemy.math.Quantization}
    set q_20__1 [java::new ptolemy.math.FixPointQuantization "20.-1,saturate,nearest"]
    set q_20_12 [java::new ptolemy.math.FixPointQuantization "20.12,saturate,nearest"]
    set q_20_32 [java::new ptolemy.math.FixPointQuantization "20.32,saturate,nearest"]
    set c0 [java::new $ctor_double 5.5734 $q_20_12]
    set c1 [java::new $ctor_double 5.5734 $q_20_32]
    set c2 [java::new $ctor_double -4.23  $q_20_12]
    set c3 [java::new $ctor_double -4.23  $q_20_32]
    set c4 [java::new $ctor_double -4.23  $q_20__1]
    list "
[$c0 toBitString] [ $c0 toString ] 
[$c1 toBitString] [ $c1 toString ] 
[$c2 toBitString] [ $c2 toString ] 
[$c3 toBitString] [ $c3 toString ] 
[$c4 toBitString] [ $c4 toString ] "
} {{
101.100100101101 5.573486328125 
101.10010010110010100101011110101000 5.57340000011026859283447265625 
-101.110001010010 -4.22998046875 
-101.11000101000111101011100001010010 -4.2299999999813735485076904296875 
-100 -4 }}


####################################################################
test FixPoint-1.3 {constructors-string} {
    set ctor_string {ptolemy.math.FixPoint String ptolemy.math.Quantization}
    set q_20_1 [java::new ptolemy.math.FixPointQuantization "20.1,saturate,nearest"]
    set cSTR0 [java::new $ctor_string "0.749999" $q_20_1]
    set cSTR1 [java::new $ctor_string "7.5e-1" $q_20_1]
    set cSTR2 [java::new $ctor_string "0.125e1" $q_20_1]
    set cSTR3 [java::new $ctor_string "150e-2" $q_20_1]
    set cSTR4 [java::new $ctor_string "1.2500001" $q_20_1]
    set cSTR5 [java::new $ctor_string "1.74999999" $q_20_1]
    set cSTR6 [java::new $ctor_string "1.750000" $q_20_1]
    set cSTR7 [java::new $ctor_string "-0.24999999" $q_20_1]
    set cSTR8 [java::new $ctor_string "-0.025e1" $q_20_1]
    set cSTR9 [java::new $ctor_string "-2500.001e-4" $q_20_1]
    list "
[$cSTR0 toBitString] [ $cSTR0 toString ] 
[$cSTR1 toBitString] [ $cSTR1 toString ] 
[$cSTR2 toBitString] [ $cSTR2 toString ] 
[$cSTR3 toBitString] [ $cSTR3 toString ] 
[$cSTR4 toBitString] [ $cSTR4 toString ] 
[$cSTR5 toBitString] [ $cSTR5 toString ] 
[$cSTR6 toBitString] [ $cSTR6 toString ] 
[$cSTR7 toBitString] [ $cSTR7 toString ] 
[$cSTR8 toBitString] [ $cSTR8 toString ] 
[$cSTR9 toBitString] [ $cSTR9 toString ] "
} {{
0.1 0.5 
1.0 1.0 
1.1 1.5 
1.1 1.5 
1.1 1.5 
1.1 1.5 
10.0 2.0 
0.0 0.0 
0.0 0.0 
-1.1 -0.5 }}

####################################################################
test FixPoint-1.4 {constructors-int} {
    set ctor_int1 {ptolemy.math.FixPoint int ptolemy.math.Quantization}

    set q_10__2 [java::new ptolemy.math.FixPointQuantization "10.-2,saturate,nearest"]

    set i0 10123
    set c0 [java::new $ctor_int1 [expr $i0] $q_10__2]
    set i1 501
    set c1 [java::new $ctor_int1 [expr $i1] $q_10__2]
    set i2 256
    set c2 [java::new $ctor_int1 [expr $i2] $q_10__2]
    set i3 -9
    set c3 [java::new $ctor_int1 [expr $i3] $q_10__2]
    set i4 -260
    set c4 [java::new $ctor_int1 [expr $i4] $q_10__2]
    set i5 -934
    set c5 [java::new $ctor_int1 [expr $i5] $q_10__2]
    list "
$i0 [$c0 toStringPrecision]
$i1 [$c1 toStringPrecision]
$i2 [$c2 toStringPrecision]
$i3 [$c3 toStringPrecision]
$i4 [$c4 toStringPrecision]
$i5 [$c5 toStringPrecision] "
} {{
10123 508(10.-2)
501 500(10.-2)
256 256(10.-2)
-9 -8(10.-2)
-260 -260(10.-2)
-934 -512(10.-2) }}


####################################################################
test FixPoint-1.5 {clone} {
    set clone [$c0 clone]
    list [$clone equals $c0]
} {1}

####################################################################
test FixPoint-1.7 {printFix} {
    jdkCapture {$c0 printFix} out
    list $out
} {{ unscale Value  (2) 1111111
 unscaled Value (10) 127
 scale Value (10) 508.0 Precision: (10.-2)
 BitCount:   7
 BitLength   7
 ABS value   1111111
 ABS bit count:  7
 ABD bitLength:  7
 Max value:  508.0
 Min value:  -512.0
}}

####################################################################
test FixPoint-1.8 {bigDecimalValue} {
    list "
[$cSTR0 toStringPrecision] [[$cSTR0 bigDecimalValue] toString]
[$cSTR1 toStringPrecision] [[$cSTR1 bigDecimalValue] toString]
[$cSTR2 toStringPrecision] [[$cSTR2 bigDecimalValue] toString]
[$cSTR3 toStringPrecision] [[$cSTR3 bigDecimalValue] toString]
[$cSTR4 toStringPrecision] [[$cSTR4 bigDecimalValue] toString]
[$cSTR5 toStringPrecision] [[$cSTR5 bigDecimalValue] toString]
[$cSTR6 toStringPrecision] [[$cSTR6 bigDecimalValue] toString]
[$cSTR7 toStringPrecision] [[$cSTR7 bigDecimalValue] toString]
[$cSTR8 toStringPrecision] [[$cSTR8 bigDecimalValue] toString]
[$cSTR9 toStringPrecision] [[$cSTR9 bigDecimalValue] toString]
"
} {{
0.5(20.1) 0.5
1.0(20.1) 1.0
1.5(20.1) 1.5
1.5(20.1) 1.5
1.5(20.1) 1.5
1.5(20.1) 1.5
2.0(20.1) 2.0
0.0(20.1) 0.0
0.0(20.1) 0.0
-0.5(20.1) -0.5
}}

####################################################################
test FixPoint-1.9 {minimumQuantization} {

    # no changes
    set mquant0 [java::new $ctor_string "7.0625" $q_4_4]
    # lsb requant
    set mquant1 [java::new $ctor_string "7.25" $q_4_4]
    # msb requant
    set mquant2 [java::new $ctor_string "3.0625" $q_4_4]
    # both
    set mquant3 [java::new $ctor_string "3.25" $q_4_4]

    list "
[$mquant0 toStringPrecision] [[$mquant0 minimumQuantization] toStringPrecision]
[$mquant1 toStringPrecision] [[$mquant1 minimumQuantization] toStringPrecision]
[$mquant2 toStringPrecision] [[$mquant2 minimumQuantization] toStringPrecision]
[$mquant3 toStringPrecision] [[$mquant3 minimumQuantization] toStringPrecision]
"
} {{
7.0625(4.4) 7.0625(4.4)
7.25(4.4) 7.25(4.2)
3.0625(4.4) 3.0625(3.4)
3.25(4.4) 3.25(3.2)
}}


####################################################################
# Abs
####################################################################

test FixPoint-2.0 {abs} {

    set a1 [java::new $ctor_string "5.5734" $q_6_10]
    set abs_1 [$a1 abs]
    set a2 [java::new $ctor_string "5.5734" $qu_6_10]
    set abs_2 [$a2 abs]
    set a3 [java::new $ctor_string "-18.23" $q_6_10]
    set abs_3 [$a3 abs]
    set a4 [java::new $ctor_string "-4.9814" $q_6_10]
    set abs_4 [$a4 abs]
    list "
[$a1 toStringPrecision] [$abs_1 toStringPrecision]
[$a2 toStringPrecision] [$abs_2 toStringPrecision]
[$a3 toStringPrecision] [$abs_3 toStringPrecision]
[$a4 toStringPrecision] [$abs_4 toStringPrecision]
"
} {{
5.5732421875(6.10) 5.5732421875(6.10)
5.5732421875(U6.10) 5.5732421875(U6.10)
-18.23046875(6.10) 18.23046875(6.10)
-4.9814453125(6.10) 4.9814453125(6.10)
}}

####################################################################
# Add
####################################################################

test FixPoint-2.1 {add-result} {

    set add_11 [java::new $ctor_double "5.5734" $q_6_10]
    set add_12 [java::new $ctor_double "-4.23" $q_6_10]
    set add_13 [$add_11 add $add_12]

    set add_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set add_22 [java::new $ctor_double "4.23" $q_6_10]
    set add_23 [$add_21 add $add_22]

    set add_31 [java::new $ctor_double "-5.5734" $q_6_10]
    set add_32 [java::new $ctor_double "-4.23" $q_6_10]
    set add_33 [$add_31 add $add_32]

    set add_41 [java::new $ctor_double "5.5734" $q_6_10]
    set add_42 [java::new $ctor_double "4.23" $q_6_10]
    set add_43 [$add_41 add $add_42]

    list "
[$add_11 toStringPrecision] + [$add_12 toStringPrecision] = [$add_13 toStringPrecision]
[$add_21 toStringPrecision] + [$add_22 toStringPrecision] = [$add_23 toStringPrecision]
[$add_31 toStringPrecision] + [$add_32 toStringPrecision] = [$add_33 toStringPrecision]
[$add_41 toStringPrecision] + [$add_42 toStringPrecision] = [$add_43 toStringPrecision]
"
} {{
5.5732421875(6.10) + -4.23046875(6.10) = 1.3427734375(7.10)
-5.5732421875(6.10) + 4.23046875(6.10) = -1.3427734375(7.10)
-5.5732421875(6.10) + -4.23046875(6.10) = -9.8037109375(7.10)
5.5732421875(6.10) + 4.23046875(6.10) = 9.8037109375(7.10)
}}

####################################################################
test FixPoint-2.2 {add-sign} {

    set add_11 [java::new $ctor_double "5.5734" $qu_6_10]
    set add_12 [java::new $ctor_double "-4.23" $q_6_10]
    set add_13 [$add_11 add $add_12]

    set add_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set add_22 [java::new $ctor_double "4.23" $qu_6_10]
    set add_23 [$add_21 add $add_22]

    set add_41 [java::new $ctor_double "5.5734" $qu_6_10]
    set add_42 [java::new $ctor_double "4.23" $qu_6_10]
    set add_43 [$add_41 add $add_42]

    list "
[$add_11 toStringPrecision] + [$add_12 toStringPrecision] = [$add_13 toStringPrecision]
[$add_21 toStringPrecision] + [$add_22 toStringPrecision] = [$add_23 toStringPrecision]
[$add_41 toStringPrecision] + [$add_42 toStringPrecision] = [$add_43 toStringPrecision]
"
} {{
5.5732421875(U6.10) + -4.23046875(6.10) = 1.3427734375(8.10)
-5.5732421875(6.10) + 4.23046875(U6.10) = -1.3427734375(8.10)
5.5732421875(U6.10) + 4.23046875(U6.10) = 9.8037109375(U7.10)
}}

####################################################################
test FixPoint-2.3 {add-growth} {


    set add_11 [java::new $ctor_double "5.5734" $q_4_4]
    set add_12 [java::new $ctor_double "4.23" $q_4_4]
    set add_13 [$add_11 add $add_12]

    set add_31 [java::new $ctor_double "-5.5734" $q_4_4]
    set add_32 [java::new $ctor_double "-4.23" $q_4_4]
    set add_33 [$add_31 add $add_32]

    set q_6__2 [java::new ptolemy.math.FixPointQuantization "6.-2,saturate,nearest"]
    set q_2_3 [java::new ptolemy.math.FixPointQuantization "2.3,saturate,nearest"]

    set add_41 [java::new $ctor_double "23.1231" $q_6__2]
    set add_42 [java::new $ctor_double "3.131223" $q_2_3]
    set add_43 [$add_41 add $add_42]


    list "
[$add_11 toStringPrecision] + [$add_12 toStringPrecision] = [$add_13 toStringPrecision]
[$add_31 toStringPrecision] + [$add_32 toStringPrecision] = [$add_33 toStringPrecision]
[$add_41 toStringPrecision] + [$add_42 toStringPrecision] = [$add_43 toStringPrecision]
"
} {{
5.5625(4.4) + 4.25(4.4) = 9.8125(5.4)
-5.5625(4.4) + -4.25(4.4) = -9.8125(5.4)
24(6.-2) + 1.875(2.3) = 25.875(7.3)
}}

####################################################################
test FixPoint-2.4 {add-quant} {

    set add_11 [java::new $ctor_double "5.5734" $q_4_4]
    set add_12 [java::new $ctor_double "4.23" $q_4_4]
    set add_13 [$add_11 add $add_12 $q_4_4]

    set add_31 [java::new $ctor_double "-5.5734" $q_4_4]
    set add_32 [java::new $ctor_double "-4.23" $q_4_4]
    set add_33 [$add_31 add $add_32 $q_4_4]

    set q_5_2 [java::new ptolemy.math.FixPointQuantization "5.2,saturate,nearest"]

    set add_41 [java::new $ctor_double "13.1231" $q_6__2]
    set add_42 [java::new $ctor_double "3.131223" $q_2_3]
    set add_43 [$add_41 add $add_42 $q_5_2]


    list "
[$add_11 toStringPrecision] + [$add_12 toStringPrecision] = [$add_13 toStringPrecision]
[$add_31 toStringPrecision] + [$add_32 toStringPrecision] = [$add_33 toStringPrecision]
[$add_41 toStringPrecision] + [$add_42 toStringPrecision] = [$add_43 toStringPrecision]
"
} {{
5.5625(4.4) + 4.25(4.4) = 7.9375(4.4)
-5.5625(4.4) + -4.25(4.4) = -8.0(4.4)
12(6.-2) + 1.875(2.3) = 14.0(5.2)
}}

####################################################################
# Subtract
####################################################################

test FixPoint-3.1 {subtract-result} {

    set sub_11 [java::new $ctor_double "5.5734" $q_6_10]
    set sub_12 [java::new $ctor_double "-4.23" $q_6_10]
    set sub_13 [$sub_11 subtract $sub_12]

    set sub_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set sub_22 [java::new $ctor_double "4.23" $q_6_10]
    set sub_23 [$sub_21 subtract $sub_22]

    set sub_31 [java::new $ctor_double "-5.5734" $q_6_10]
    set sub_32 [java::new $ctor_double "-4.23" $q_6_10]
    set sub_33 [$sub_31 subtract $sub_32]

    set sub_41 [java::new $ctor_double "5.5734" $q_6_10]
    set sub_42 [java::new $ctor_double "4.23" $q_6_10]
    set sub_43 [$sub_41 subtract $sub_42]

    list "
[$sub_11 toStringPrecision] - [$sub_12 toStringPrecision] = [$sub_13 toStringPrecision]
[$sub_21 toStringPrecision] - [$sub_22 toStringPrecision] = [$sub_23 toStringPrecision]
[$sub_31 toStringPrecision] - [$sub_32 toStringPrecision] = [$sub_33 toStringPrecision]
[$sub_41 toStringPrecision] - [$sub_42 toStringPrecision] = [$sub_43 toStringPrecision]
"
} {{
5.5732421875(6.10) - -4.23046875(6.10) = 9.8037109375(7.10)
-5.5732421875(6.10) - 4.23046875(6.10) = -9.8037109375(7.10)
-5.5732421875(6.10) - -4.23046875(6.10) = -1.3427734375(7.10)
5.5732421875(6.10) - 4.23046875(6.10) = 1.3427734375(7.10)
}}

####################################################################
test FixPoint-3.2 {subtract-sign} {

    set sub_11 [java::new $ctor_double "5.5734" $qu_6_10]
    set sub_12 [java::new $ctor_double "-4.23" $q_6_10]
    set sub_13 [$sub_11 subtract $sub_12]

    set sub_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set sub_22 [java::new $ctor_double "4.23" $qu_6_10]
    set sub_23 [$sub_21 subtract $sub_22]

    set sub_31 [java::new $ctor_double "5.5734" $qu_6_10]
    set sub_32 [java::new $ctor_double "4.23" $qu_6_10]
    set sub_33 [$sub_31 subtract $sub_32]

    set sub_43 [$sub_32 subtract $sub_31]

    list "
[$sub_11 toStringPrecision] - [$sub_12 toStringPrecision] = [$sub_13 toStringPrecision]
[$sub_21 toStringPrecision] - [$sub_22 toStringPrecision] = [$sub_23 toStringPrecision]
[$sub_31 toStringPrecision] - [$sub_32 toStringPrecision] = [$sub_33 toStringPrecision]
[$sub_32 toStringPrecision] - [$sub_31 toStringPrecision] = [$sub_43 toStringPrecision]
"
} {{
5.5732421875(U6.10) - -4.23046875(6.10) = 9.8037109375(8.10)
-5.5732421875(6.10) - 4.23046875(U6.10) = -9.8037109375(8.10)
5.5732421875(U6.10) - 4.23046875(U6.10) = 1.3427734375(7.10)
4.23046875(U6.10) - 5.5732421875(U6.10) = -1.3427734375(7.10)
}}

####################################################################
test FixPoint-3.3 {subtract-growth} {


    set sub_11 [java::new $ctor_double "5.5734" $q_4_4]
    set sub_12 [java::new $ctor_double "-4.23" $q_4_4]
    set sub_13 [$sub_11 subtract $sub_12]

    set sub_31 [java::new $ctor_double "-5.5734" $q_4_4]
    set sub_32 [java::new $ctor_double "4.23" $q_4_4]
    set sub_33 [$sub_31 subtract $sub_32]

    set q_6__2 [java::new ptolemy.math.FixPointQuantization "6.-2,saturate,nearest"]
    set q_2_3 [java::new ptolemy.math.FixPointQuantization "2.3,saturate,nearest"]

    set sub_41 [java::new $ctor_double "-23.1231" $q_6__2]
    set sub_42 [java::new $ctor_double "3.131223" $q_2_3]
    set sub_43 [$sub_41 subtract $sub_42]


    list "
[$sub_11 toStringPrecision] - [$sub_12 toStringPrecision] = [$sub_13 toStringPrecision]
[$sub_31 toStringPrecision] - [$sub_32 toStringPrecision] = [$sub_33 toStringPrecision]
[$sub_41 toStringPrecision] - [$sub_42 toStringPrecision] = [$sub_43 toStringPrecision]
"
} {{
5.5625(4.4) - -4.25(4.4) = 9.8125(5.4)
-5.5625(4.4) - 4.25(4.4) = -9.8125(5.4)
-24(6.-2) - 1.875(2.3) = -25.875(7.3)
}}

####################################################################
test FixPoint-3.4 {subtract-quant} {

    set sub_11 [java::new $ctor_double "5.5734" $q_4_4]
    set sub_12 [java::new $ctor_double "-4.23" $q_4_4]
    set sub_13 [$sub_11 subtract $sub_12 $q_4_4]

    set sub_31 [java::new $ctor_double "-5.5734" $q_4_4]
    set sub_32 [java::new $ctor_double "4.23" $q_4_4]
    set sub_33 [$sub_31 subtract $sub_32 $q_4_4]

    set q_5_2 [java::new ptolemy.math.FixPointQuantization "5.2,saturate,nearest"]

    set sub_41 [java::new $ctor_double "-13.1231" $q_6__2]
    set sub_42 [java::new $ctor_double "3.131223" $q_2_3]
    set sub_43 [$sub_41 subtract $sub_42 $q_5_2]


    list "
[$sub_11 toStringPrecision] - [$sub_12 toStringPrecision] = [$sub_13 toStringPrecision]
[$sub_31 toStringPrecision] - [$sub_32 toStringPrecision] = [$sub_33 toStringPrecision]
[$sub_41 toStringPrecision] - [$sub_42 toStringPrecision] = [$sub_43 toStringPrecision]
"
} {{
5.5625(4.4) - -4.25(4.4) = 7.9375(4.4)
-5.5625(4.4) - 4.25(4.4) = -8.0(4.4)
-12(6.-2) - 1.875(2.3) = -13.75(5.2)
}}


####################################################################
# Multiplication
####################################################################

test FixPoint-4.1 {mult-result} {

    set mult_11 [java::new $ctor_double "5.5734" $q_6_10]
    set mult_12 [java::new $ctor_double ".901231" $q_6_10]
    set mult_13 [$mult_11 multiply $mult_12]

    set mult_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set mult_22 [java::new $ctor_double "4.23" $q_6_10]
    set mult_23 [$mult_21 multiply $mult_22]

    set mult_31 [java::new $ctor_double "-5.5734" $q_6_10]
    set mult_32 [java::new $ctor_double "-4.23" $q_6_10]
    set mult_33 [$mult_31 multiply $mult_32]

    set mult_41 [java::new $ctor_double "5.5734" $q_6_10]
    set mult_42 [java::new $ctor_double "4.23" $q_6_10]
    set mult_43 [$mult_41 multiply $mult_42]

    list "
[$mult_11 toStringPrecision] X [$mult_12 toStringPrecision] = [$mult_13 toStringPrecision]
[$mult_21 toStringPrecision] X [$mult_22 toStringPrecision] = [$mult_23 toStringPrecision]
[$mult_31 toStringPrecision] X [$mult_32 toStringPrecision] = [$mult_33 toStringPrecision]
[$mult_41 toStringPrecision] X [$mult_42 toStringPrecision] = [$mult_43 toStringPrecision]
"
} {{
5.5732421875(6.10) X 0.9013671875(6.10) = 5.02353763580322265625(12.20)
-5.5732421875(6.10) X 4.23046875(6.10) = -23.577426910400390625(12.20)
-5.5732421875(6.10) X -4.23046875(6.10) = 23.577426910400390625(12.20)
5.5732421875(6.10) X 4.23046875(6.10) = 23.577426910400390625(12.20)
}}

####################################################################
test FixPoint-4.2 {mult-sign} {

    set mult_11 [java::new $ctor_double "5.5734" $qu_6_10]
    set mult_12 [java::new $ctor_double "-4.23" $q_6_10]
    set mult_13 [$mult_11 multiply $mult_12]

    set mult_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set mult_22 [java::new $ctor_double "4.23" $qu_6_10]
    set mult_23 [$mult_21 multiply $mult_22]

    set mult_31 [java::new $ctor_double "5.5734" $qu_6_10]
    set mult_32 [java::new $ctor_double "4.23" $qu_6_10]
    set mult_33 [$mult_31 multiply $mult_32]



    list "
[$mult_11 toStringPrecision] X [$mult_12 toStringPrecision] = [$mult_13 toStringPrecision]
[$mult_21 toStringPrecision] X [$mult_22 toStringPrecision] = [$mult_23 toStringPrecision]
[$mult_31 toStringPrecision] X [$mult_32 toStringPrecision] = [$mult_33 toStringPrecision]
"
} {{
5.5732421875(U6.10) X -4.23046875(6.10) = -23.577426910400390625(12.20)
-5.5732421875(6.10) X 4.23046875(U6.10) = -23.577426910400390625(12.20)
5.5732421875(U6.10) X 4.23046875(U6.10) = 23.577426910400390625(U12.20)
}}

####################################################################
test FixPoint-4.3 {mult-growth} {

    set mult_fp1 [java::new $ctor_double "2.25" $q_4_4]
    set mult_fp2 [java::new $ctor_double "0.25" $q_4_4]
    set mult_fp3 [java::new $ctor_double "0.3" $q_4_4]
    set mult_fp4 [java::new $ctor_double "6.0" $q_4_4]
    set mult_fp5 [java::new $ctor_double "6.3" $q_4_4]

    # no growth
    set mult_p1 [$mult_fp1 multiply $mult_fp1]
    set mult_p2 [$mult_fp1 multiply $mult_fp2]

    # growth in lsb
    set mult_p3 [$mult_fp1 multiply $mult_fp3]

    # growth in msb
    set mult_p4 [$mult_fp1 multiply $mult_fp4]

    # growth in both
    set mult_p5 [$mult_fp1 multiply $mult_fp5]

    list "
[$mult_fp1 toStringPrecision] X [$mult_fp1 toStringPrecision] = [$mult_p1 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp2 toStringPrecision] = [$mult_p2 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp3 toStringPrecision] = [$mult_p3 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp4 toStringPrecision] = [$mult_p4 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp5 toStringPrecision] = [$mult_p5 toStringPrecision]
"
} {{
2.25(4.4) X 2.25(4.4) = 5.0625(8.8)
2.25(4.4) X 0.25(4.4) = 0.5625(8.8)
2.25(4.4) X 0.3125(4.4) = 0.703125(8.8)
2.25(4.4) X 6.0(4.4) = 13.5(8.8)
2.25(4.4) X 6.3125(4.4) = 14.203125(8.8)
}}

####################################################################
test FixPoint-4.4 {mult-quant} {

    set mult_p1 [$mult_fp1 multiply $mult_fp1 $q_4_4]
    set mult_p2 [$mult_fp1 multiply $mult_fp2 $q_4_4]
    set mult_p3 [$mult_fp1 multiply $mult_fp3 $q_4_4]
    set mult_p4 [$mult_fp1 multiply $mult_fp4 $q_4_4]
    set mult_p5 [$mult_fp1 multiply $mult_fp5 $q_4_4]


    list "
[$mult_fp1 toStringPrecision] X [$mult_fp1 toStringPrecision] = [$mult_p1 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp2 toStringPrecision] = [$mult_p2 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp3 toStringPrecision] = [$mult_p3 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp4 toStringPrecision] = [$mult_p4 toStringPrecision]
[$mult_fp1 toStringPrecision] X [$mult_fp5 toStringPrecision] = [$mult_p5 toStringPrecision]
"
} {{
2.25(4.4) X 2.25(4.4) = 5.0625(4.4)
2.25(4.4) X 0.25(4.4) = 0.5625(4.4)
2.25(4.4) X 0.3125(4.4) = 0.6875(4.4)
2.25(4.4) X 6.0(4.4) = 7.9375(4.4)
2.25(4.4) X 6.3125(4.4) = 7.9375(4.4)
}}


####################################################################
# Division 
####################################################################

test FixPoint-5.1 {div-result} {

    set div_11 [java::new $ctor_double "5.5734" $q_4_4]
    set div_12 [java::new $ctor_double ".901231" $q_4_4]
    set div_13 [$div_11 divide $div_12 $q_4_8]

    set div_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set div_22 [java::new $ctor_double "4.23" $q_6_10]
    set div_23 [$div_21 divide $div_22 $q_6_20]

    set div_31 [java::new $ctor_double "-5.5734" $q_6_10]
    set div_32 [java::new $ctor_double "-4.23" $q_6_10]
    set div_33 [$div_31 divide $div_32 $q_6_20]

    set div_41 [java::new $ctor_double "5.5734" $q_6_10]
    set div_42 [java::new $ctor_double "4.23" $q_6_10]
    set div_43 [$div_41 divide $div_42 $q_6_20]

    list "
[$div_11 toStringValuePrecision] / [$div_12 toStringValuePrecision] = [$div_13 toStringValuePrecision]
[$div_21 toStringValuePrecision] / [$div_22 toStringValuePrecision] = [$div_23 toStringValuePrecision]
[$div_31 toStringValuePrecision] / [$div_32 toStringValuePrecision] = [$div_33 toStringValuePrecision]
[$div_41 toStringValuePrecision] / [$div_42 toStringValuePrecision] = [$div_43 toStringValuePrecision]
"
} {{
5.5625 [(4.4)=89] / 0.875 [(4.4)=14] = 6.35546875 [(4.8)=1627]
-5.5732421875 [(6.10)=-5707] / 4.23046875 [(6.10)=4332] = -1.31740570068359375 [(6.20)=-1381400]
-5.5732421875 [(6.10)=-5707] / -4.23046875 [(6.10)=-4332] = 1.31740570068359375 [(6.20)=1381400]
5.5732421875 [(6.10)=5707] / 4.23046875 [(6.10)=4332] = 1.31740570068359375 [(6.20)=1381400]
}}

####################################################################
test FixPoint-5.2 {div-sign} {

    set div_11 [java::new $ctor_double "5.5734" $qu_6_10]
    set div_12 [java::new $ctor_double "-4.23" $q_6_10]
    set div_13 [$div_11 divide $div_12 $q_6_10]

    set div_21 [java::new $ctor_double "-5.5734" $q_6_10]
    set div_22 [java::new $ctor_double "4.23" $qu_6_10]
    set div_23 [$div_21 multiply $div_22 $q_6_10]

    set div_31 [java::new $ctor_double "5.5734" $qu_6_10]
    set div_32 [java::new $ctor_double "4.23" $qu_6_10]
    set div_33 [$div_31 multiply $div_32 $qu_6_10]



    list "
[$div_11 toStringPrecision] / [$div_12 toStringPrecision] = [$div_13 toStringPrecision]
[$div_21 toStringPrecision] / [$div_22 toStringPrecision] = [$div_23 toStringPrecision]
[$div_31 toStringPrecision] / [$div_32 toStringPrecision] = [$div_33 toStringPrecision]
"
} {{
5.5732421875(U6.10) / -4.23046875(6.10) = -1.3173828125(6.10)
-5.5732421875(6.10) / 4.23046875(U6.10) = -23.5771484375(6.10)
5.5732421875(U6.10) / 4.23046875(U6.10) = 23.5771484375(U6.10)
}}


####################################################################

test FixPoint-6.1 {equals} {
    set q_20_26 [java::new ptolemy.math.FixPointQuantization "20.26,saturate,nearest"]
    set q_20_27 [java::new ptolemy.math.FixPointQuantization "20.27,saturate,nearest"]
    set q_20_28 [java::new ptolemy.math.FixPointQuantization "20.28,saturate,nearest"]
    set eq_1 [java::new $ctor_double 7.5734 $q_20_28]	
    set eq_2 [java::new $ctor_double 7.5734 $q_20_27]
    set eq_3 [java::new $ctor_double 7.5734 $q_20_28]
    set eq_4 [java::new $ctor_double 7.5734 $q_20_26]
    list "
[$eq_1 toString] [$eq_1 toString] [$eq_1 {equals ptolemy.math.FixPoint} $eq_1 ] 
[$eq_1 toString] [$eq_2 toString] [$eq_1 {equals ptolemy.math.FixPoint} $eq_2 ] 
[$eq_1 toString] [$eq_3 toString] [$eq_1 {equals ptolemy.math.FixPoint} $eq_3 ] 
[$eq_1 toString] [$eq_4 toString] [$eq_1 {equals ptolemy.math.FixPoint} $eq_4 ] 
[$eq_2 toString] [$eq_1 toString] [$eq_2 {equals ptolemy.math.FixPoint} $eq_1 ] "
} {{
7.573399998247623443603515625 7.573399998247623443603515625 1 
7.573399998247623443603515625 7.573399998247623443603515625 1 
7.573399998247623443603515625 7.573399998247623443603515625 1 
7.573399998247623443603515625 7.57340000569820404052734375 0 
7.573399998247623443603515625 7.573399998247623443603515625 1 }}

####################################################################
test FixPoint-6.5 {getError is deprecated, but we call it for completeness } {
    set err [$c0 getError]
    list [$err getDescription]
} {{ Overflow status is no longer tracked.}}

####################################################################

test FixPoint-7.1 {absolute} {
    set q_20_12 [java::new ptolemy.math.FixPointQuantization "20.12,saturate,nearest"]
    set q_20_28 [java::new ptolemy.math.FixPointQuantization "20.28,saturate,nearest"]
    set abs_0 [java::new $ctor_double 5.5734 $q_20_12]
    set abs_1 [java::new $ctor_double -4.23  $q_20_12]
    set abs_2 [java::new $ctor_double 7.5734 $q_20_28]	
    set abs_3 [java::new $ctor_double -8.0 $q_20_28]	
    list "
[$abs_0 toBitString] [[$abs_0 abs] toBitString] [[$abs_0 getPrecision] toString] 
[$abs_1 toBitString] [[$abs_1 abs] toBitString] [[$abs_1 getPrecision] toString] 
[$abs_2 toBitString] [[$abs_2 abs] toBitString] [[$abs_2 getPrecision] toString] 
[$abs_3 toBitString] [[$abs_3 abs] toBitString] [[$abs_3 getPrecision] toString] "
} {{
101.100100101101 101.100100101101 (20.12) 
-101.110001010010 100.001110101110 (20.12) 
111.1001001011001010010101111010 111.1001001011001010010101111010 (20.28) 
-1000.0000000000000000000000000000 1000.0000000000000000000000000000 (20.28) }}
####################################################################

test FixPoint-8.1 {doubleValue} {
    set q_20__5 [java::new ptolemy.math.FixPointQuantization "20.-5,saturate,nearest"]
    set q_20_0 [java::new ptolemy.math.FixPointQuantization "20.0,saturate,nearest"]
    set q_20_12 [java::new ptolemy.math.FixPointQuantization "20.12,saturate,nearest"]
    set q_20_28 [java::new ptolemy.math.FixPointQuantization "20.28,saturate,nearest"]
    set q_20_128 [java::new ptolemy.math.FixPointQuantization "20.128,saturate,nearest"]
    set dV_0 [java::new $ctor_double 5.5735 $q_20_12]
    set dV_1 [java::new $ctor_double -4.23  $q_20_12]
    set dV_2 [java::new $ctor_double 7.5734 $q_20_28]	
    set dV_3 [java::new $ctor_double -8.0 $q_20_28]	
    set dV_4 [java::new $ctor_double -8.0 $q_20_0]	
    set dV_5 [java::new $ctor_double 3192 $q_20__5]	
    set dV_6 [java::new $ctor_double $PI $q_20_128]	
    list "
[$dV_0 toBitString] [$dV_0 doubleValue] [[$dV_0 getPrecision] toString] 
[$dV_1 toBitString] [$dV_1 doubleValue] [[$dV_1 getPrecision] toString] 
[$dV_2 toBitString] [$dV_2 doubleValue] [[$dV_2 getPrecision] toString] 
[$dV_3 toBitString] [$dV_3 doubleValue] [[$dV_3 getPrecision] toString] 
[$dV_4 toBitString] [$dV_4 doubleValue] [[$dV_4 getPrecision] toString] 
[$dV_5 toBitString] [$dV_5 doubleValue] 
[$dV_6 toBitString] [$dV_6 doubleValue] [[$dV_6 getPrecision] toString] "
} {{
101.100100101101 5.57348632812 (20.12) 
-101.110001010010 -4.22998046875 (20.12) 
111.1001001011001010010101111010 7.57339999825 (20.28) 
-1000.0000000000000000000000000000 -8.0 (20.28) 
-1000 -8.0 (20.0) 
110010000000 3200.0 
11.00100100001111110110101010001000100001011010001100000000000000000000000000000000000000000000000000000000000000000000000000000000 3.14159265359 (20.128) }}

test FixPoint-9.1 {bigDecimalValue} {
    set dV_0 [java::new $ctor_double 5.5735 $q_20_12]
    set dV_1 [java::new $ctor_double -4.23  $q_20_12]
    set dV_2 [java::new $ctor_double 7.5734 $q_20_28]	
    set dV_3 [java::new $ctor_double -8.0 $q_20_28]	
    set dV_4 [java::new $ctor_double -8.0 $q_20_0]	
    set dV_5 [java::new $ctor_double 3192 $q_20__5]	
    set dV_6 [java::new $ctor_double $PI $q_20_128]	
    list "
[$dV_0 toBitString] [[$dV_0 bigDecimalValue] toString] [[$dV_0 getPrecision] toString] 
[$dV_1 toBitString] [[$dV_1 bigDecimalValue] toString] [[$dV_1 getPrecision] toString] 
[$dV_2 toBitString] [[$dV_2 bigDecimalValue] toString] [[$dV_2 getPrecision] toString] 
[$dV_3 toBitString] [[$dV_3 bigDecimalValue] toString] [[$dV_3 getPrecision] toString] 
[$dV_4 toBitString] [[$dV_4 bigDecimalValue] toString] [[$dV_4 getPrecision] toString] 
[$dV_5 toBitString] [[$dV_5 bigDecimalValue] toString] 
[$dV_6 toBitString] 
[[$dV_6 bigDecimalValue] toString] [[$dV_6 getPrecision] toString] "
} {{
101.100100101101 5.573486328125 (20.12) 
-101.110001010010 -4.229980468750 (20.12) 
111.1001001011001010010101111010 7.5733999982476234436035156250 (20.28) 
-1000.0000000000000000000000000000 -8.0000000000000000000000000000 (20.28) 
-1000 -8 (20.0) 
110010000000 3200 
11.00100100001111110110101010001000100001011010001100000000000000000000000000000000000000000000000000000000000000000000000000000000 
3.14159265358979311599796346854418516159057617187500000000000000000000000000000000000000000000000000000000000000000000000000000000 (20.128) }}

####################################################################

