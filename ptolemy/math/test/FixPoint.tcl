# Tests for the FixPoint Class
#
# @Author: Bart Kienhuis, Ed.Willink
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

test FixPoint-1.0 {constructors-double} {
    set ctor_double {ptolemy.math.FixPoint double ptolemy.math.Quantization}
    set ctor_string {ptolemy.math.FixPoint String ptolemy.math.Quantization}
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

test FixPoint-1.1 {constructors-precision} {
    set q_20_1 [java::new ptolemy.math.FixPointQuantization "20.1,saturate,nearest"]
    set q_20_19 [java::new ptolemy.math.FixPointQuantization "20.19,saturate,nearest"]
    set q_20_20 [java::new ptolemy.math.FixPointQuantization "20.20,saturate,nearest"]
    set q_20_21 [java::new ptolemy.math.FixPointQuantization "20.21,saturate,nearest"]
    set q_20_22 [java::new ptolemy.math.FixPointQuantization "20.22,saturate,nearest"]
    set q_20_24 [java::new ptolemy.math.FixPointQuantization "20.24,saturate,nearest"]
    set q_20_25 [java::new ptolemy.math.FixPointQuantization "20.25,saturate,nearest"]
    set q_20_26 [java::new ptolemy.math.FixPointQuantization "20.26,saturate,nearest"]
    set q_20_27 [java::new ptolemy.math.FixPointQuantization "20.27,saturate,nearest"]
    set q_20_28 [java::new ptolemy.math.FixPointQuantization "20.28,saturate,nearest"]
    set q_20_32 [java::new ptolemy.math.FixPointQuantization "20.32,saturate,nearest"]
    set q_20_128 [java::new ptolemy.math.FixPointQuantization "20.128,saturate,nearest"]
    set c0 [java::new $ctor_double 5.0735 $q_20_32]
    set c1 [java::new $ctor_double 5.0735 $q_20_28]
    set c2 [java::new $ctor_double 5.0735 $q_20_27]
    set c3 [java::new $ctor_double 5.0735 $q_20_26]
    set c4 [java::new $ctor_double 5.0735 $q_20_25]
    set c5 [java::new $ctor_string 5.0735 $q_20_1]
    set c6 [java::new $ctor_string -14.23 $q_20_32]
    set c7 [java::new $ctor_string -14.23 $q_20_22]
    set c8 [java::new $ctor_string -14.23 $q_20_21]
    set c9 [java::new $ctor_string -14.23 $q_20_20]
    set c10 [java::new $ctor_string -14.23 $q_20_19]
    set c11 [java::new $ctor_string -14.23 $q_20_1]
    set c12 [java::new $ctor_string $PI $q_20_128]
    list "
[$c0 toBitString] [ $c0 toString ] 
[$c1 toBitString] [ $c1 toString ] 
[$c2 toBitString] [ $c2 toString ] 
[$c3 toBitString] [ $c3 toString ]
[$c4 toBitString] [ $c4 toString ] 
[$c5 toBitString] [ $c5 toString ] 
[$c6 toBitString] [ $c6 toString ] 
[$c7 toBitString] [ $c7 toString ] 
[$c8 toBitString] [ $c8 toString ] 
[$c9 toBitString] [ $c9 toString ]
[$c10 toBitString] [ $c10 toString ] 
[$c11 toBitString] [ $c11 toString ] 
[$c12 toBitString]
[ $c12 toString ] "
} {{
101.00010010110100001110010101100000 5.073499999940395355224609375 
101.0001001011010000111001010110 5.073499999940395355224609375 
101.000100101101000011100101011 5.073499999940395355224609375 
101.00010010110100001110010110 5.0735000073909759521484375
101.0001001011010000111001011 5.0735000073909759521484375 
101.0 5.0 
-1111.11000101000111101011100001010010 -14.2299999999813735485076904296875 
-1111.1100010100011110101110 -14.230000019073486328125 
-1111.110001010001111010111 -14.230000019073486328125 
-1111.11000101000111101100 -14.229999542236328125
-1111.1100010100011110110 -14.229999542236328125 
-1110.0 -14.0 
11.00100100001111110110101010001000100001011101110100111011100100000011110100001111001010110001110001011101100010111010010111010110
3.1415926535900000000000000000000000000004017912364873323939340034143393312954900984334226377026055843089125119149684906005859375 }}

test FixPoint-1.2 {constructors-string} {
    set q_20_1 [java::new ptolemy.math.FixPointQuantization "20.1,saturate,nearest"]
    set c0 [java::new $ctor_string "0.749999" $q_20_1]
    set c1 [java::new $ctor_string "7.5e-1" $q_20_1]
    set c2 [java::new $ctor_string "0.125e1" $q_20_1]
    set c3 [java::new $ctor_string "150e-2" $q_20_1]
    set c4 [java::new $ctor_string "1.2500001" $q_20_1]
    set c5 [java::new $ctor_string "1.74999999" $q_20_1]
    set c6 [java::new $ctor_string "1.750000" $q_20_1]
    set c7 [java::new $ctor_string "-0.24999999" $q_20_1]
    set c8 [java::new $ctor_string "-0.025e1" $q_20_1]
    set c9 [java::new $ctor_string "-2500.001e-4" $q_20_1]
    list "
[$c0 toBitString] [ $c0 toString ] 
[$c1 toBitString] [ $c1 toString ] 
[$c2 toBitString] [ $c2 toString ] 
[$c3 toBitString] [ $c3 toString ] 
[$c4 toBitString] [ $c4 toString ] 
[$c5 toBitString] [ $c5 toString ] 
[$c6 toBitString] [ $c6 toString ] 
[$c7 toBitString] [ $c7 toString ] 
[$c8 toBitString] [ $c8 toString ] 
[$c9 toBitString] [ $c9 toString ] "
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

test FixPoint-1.3 {constructors} {
    set q_20_0 [java::new ptolemy.math.FixPointQuantization "20.0,saturate,nearest"]
    set q_20_1 [java::new ptolemy.math.FixPointQuantization "20.1,saturate,nearest"]
    set c0 [java::new $ctor_double 1 $q_20_0]
    set c1 [java::new $ctor_double -1 $q_20_0]
    set c2 [java::new $ctor_double 0 $q_20_0]
    set c3 [java::new $ctor_double 1 $q_20_1]
    set c4 [java::new $ctor_double -1 $q_20_1]
    set c5 [java::new $ctor_double 0 $q_20_1]
    set c6 [java::new $ctor_double 1 $q_20_0]
    set c7 [java::new $ctor_double -1 $q_20_0]
    set c8 [java::new $ctor_double 0 $q_20_0]
    list "
[$c0 toBitString] [ $c0 toString ]
[$c1 toBitString] [ $c1 toString ] 
[$c2 toBitString] [ $c2 toString ]
[$c3 toBitString] [ $c3 toString ]
[$c4 toBitString] [ $c4 toString ] 
[$c5 toBitString] [ $c5 toString ]
[$c6 toBitString] [ $c6 toString ]
[$c7 toBitString] [ $c7 toString ] 
[$c8 toBitString] [ $c8 toString ] "
} {{
1 1
-1 -1 
0 0
1.0 1.0
-1.0 -1.0 
0.0 0.0
1 1
-1 -1 
0 0 }}

test FixPoint-1.4 {constructors-bad} {
    set q_20_1 [java::new ptolemy.math.FixPointQuantization "20.1,saturate,nearest"]
    catch { set c0 [java::new $ctor_string "1g6" $q_20_1] } msg
    list $msg
} {{java.lang.IllegalArgumentException: NumberFormatException while converting "1g6" to a FixPoint.}}


####################################################################
test FixPoint-1.5 {clone} {
    set clone [$c0 clone]
    list [$clone equals $c0]
} {1}

####################################################################
test FixPoint-1.7 {printFix} {
    jdkCapture {$c0 printFix} out
    list $out
} {{ unscale Value  (2) 1
 unscaled Value (10) 1
 scale Value (10) 1.0 Precision: (20.0)
 BitCount:   1
 BitLength   1
 ABS value   1
 ABS bit count:  1
 ABD bitLength:  1
 Max value:  524287.0
 Min value:  -524288.0
}}


####################################################################

test FixPoint-2.1 {add} {
    set q_20_9 [java::new ptolemy.math.FixPointQuantization "20.9,saturate,nearest"]
    set q_20_11 [java::new ptolemy.math.FixPointQuantization "20.11,saturate,nearest"]
    set q_20_12 [java::new ptolemy.math.FixPointQuantization "20.12,saturate,nearest"]
    set q_20_13 [java::new ptolemy.math.FixPointQuantization "20.13,saturate,nearest"]
    set add_1 [java::new $ctor_double 5.5734 $q_20_12]
    set add_2 [java::new $ctor_double -4.23 $q_20_12]
    set add_3 [$add_1 add $add_2]
    list "[$add_1 toString] [$add_2 toString] [$add_3 toString] [$add_3 toBitString] [[$add_3 getPrecision] toString]"
} {{5.573486328125 -4.22998046875 1.343505859375 1.010101111111 (20.12)}}


test FixPoint-2.2 {add} {
    set add_1 [java::new $ctor_double 5.5734 $q_20_12]
    set add_2 [java::new $ctor_double -4.23 $q_20_11]
    set add_3 [$add_1 add $add_2]
    list "[$add_1 toString] [$add_2 toString] [$add_3 toString] [$add_3 toBitString] [[$add_3 getPrecision] toString]"
} {{5.573486328125 -4.22998046875 1.343505859375 1.010101111111 (20.12)}}

test FixPoint-2.3 {add} {
    set add_1 [java::new $ctor_double 5.5734 $q_20_12]
    set add_2 [java::new $ctor_double -4.0 $q_20_13]
    set add_3 [$add_1 add $add_2]
    list "[$add_1 toString] [$add_2 toString] [$add_3 toString] [$add_3 toBitString] [[$add_3 getPrecision] toString]"
} {{5.573486328125 -4.0 1.573486328125 1.1001001011010 (20.13)}}

test FixPoint-2.4 {add} {
    set add_1 [java::new $ctor_double 5.5734 $q_20_12]
    set add_2 [java::new $ctor_double -4.231 $q_20_9]
    set add_3 [$add_1 add $add_2]
    list "[$add_1 toString] [$add_2 toString] [$add_3 toString] [$add_3 toBitString] [[$add_3 getPrecision] toString]"
} {{5.573486328125 -4.23046875 1.343017578125 1.010101111101 (20.12)}}

test FixPoint-2.5 {add} {
    set add_1 [java::new $ctor_double 5.5734 $q_20_12]
    set add_2 [java::new $ctor_double -4.0 $q_20_11]
    set add_3 [$add_1 add $add_2]
    list "[$add_1 toString] [$add_2 toString] [$add_3 toString] [$add_3 toBitString] [[$add_3 getPrecision] toString]"
} {{5.573486328125 -4.0 1.573486328125 1.100100101101 (20.12)}}

####################################################################

test FixPoint-3.1 {subtract} {
    set q_20_10 [java::new ptolemy.math.FixPointQuantization "20.10,saturate,nearest"]
    set q_20_12 [java::new ptolemy.math.FixPointQuantization "20.12,saturate,nearest"]
    set sub_1 [java::new $ctor_double 5.5734 $q_20_10]
    set sub_2 [java::new $ctor_double -4.23 $q_20_10]
    set sub_3 [$sub_1 subtract $sub_2]
    list "[$sub_1 toString] [$sub_2 toString] [$sub_3 toString] [$sub_3 toBitString] [[$sub_3 getPrecision] toString]"
} {{5.5732421875 -4.23046875 9.8037109375 1001.1100110111 (20.10)}}

test FixPoint-3.2 {subtract} {
    set sub_1 [java::new $ctor_double 5.5734 $q_20_10]
    set sub_2 [java::new $ctor_double -4.23 $q_20_12]
    set sub_3 [$sub_1 subtract $sub_2]
    list "[$sub_1 toString] [$sub_2 toString] [$sub_3 toString] [$sub_3 toBitString] [[$sub_3 getPrecision] toString]"
} {{5.5732421875 -4.22998046875 9.80322265625 1001.110011011010 (20.12)}}

####################################################################

test FixPoint-4.1 {multiply} {
    set q_5_10 [java::new ptolemy.math.FixPointQuantization "5.10,saturate,nearest"]
    set q_4_12 [java::new ptolemy.math.FixPointQuantization "4.12,saturate,nearest"]
    set mul_1 [java::new $ctor_double 5.5734 $q_5_10]
    set mul_2 [java::new $ctor_double 4.23 $q_5_10]
    set mul_3 [$mul_1 multiply $mul_2]
    list "[$mul_1 toString] [$mul_2 toString] [$mul_3 toString] [$mul_3 toBitString] [[$mul_3 getPrecision] toString]"
} {{5.5732421875 4.23046875 23.577426910400390625 10111.10010011110100100100 (10.20)}}

test FixPoint-4.2 {multiply} {
    set mul_1 [java::new $ctor_double 7.5734 $q_4_12]
    set mul_2 [java::new $ctor_double -7.23 $q_4_12]
    set mul_3 [$mul_1 multiply $mul_2]
    list "[$mul_1 toString] [$mul_2 toString] [$mul_3 toString] [$mul_3 toBitString] [[$mul_3 getPrecision] toString]"
} {{7.573486328125 -7.22998046875 -54.75615823268890380859375 -110111.001111100110110001101010 (8.24)}}

test FixPoint-4.3 {multiply} {
    set mul_1 [java::new $ctor_double 15.5734 $q_5_10]
    set mul_2 [java::new $ctor_double 7.23 $q_4_12]
    set mul_3 [$mul_1 multiply $mul_2]
    list "[$mul_1 toString] [$mul_2 toString] [$mul_3 toString] [$mul_3 toBitString] [[$mul_3 getPrecision] toString]"
} {{15.5732421875 7.22998046875 112.594236850738525390625 1110000.1001100000011111111010 (9.22)}}

test FixPoint-4.4 {multiply} {
    set mul_1 [java::new $ctor_double -16 $q_5_10]
    set mul_2 [java::new $ctor_double -16 $q_5_10]
    set mul_3 [$mul_1 multiply $mul_2]
    list "[$mul_1 toString] [$mul_2 toString] [$mul_3 toString] [$mul_3 toBitString] [[$mul_3 getPrecision] toString]"
} {{-16.0 -16.0 256.0 100000000.00000000000000000000 (10.20)}}

####################################################################

test FixPoint-5.1 {divide} {
    set q_20_2 [java::new ptolemy.math.FixPointQuantization "20.2,saturate,nearest"]
    set q_20_10 [java::new ptolemy.math.FixPointQuantization "20.10,saturate,nearest"]
    set q_20_12 [java::new ptolemy.math.FixPointQuantization "20.12,saturate,nearest"]
    set q_20_28 [java::new ptolemy.math.FixPointQuantization "20.28,saturate,nearest"]
    set div_1 [java::new $ctor_double 5.5734 $q_20_10]
    set div_2 [java::new $ctor_double 4.23 $q_20_10]
    set div_3 [$div_1 divide $div_2]
    set div_4 [$div_1 divide $div_2 $q_20_28]
    list "
[$div_1 toString] [$div_2 toString] [$div_3 toString] [$div_3 toBitString] [[$div_3 getPrecision] toString]
[$div_1 toString] [$div_2 toString] [$div_4 toString] [$div_4 toBitString] [[$div_4 getPrecision] toString]"
} {{
5.5732421875 4.23046875 1.3173828125 1.0101000101 (20.10)
5.5732421875 4.23046875 1.3174053542315959930419921875 1.0101000101000001011110100011 (20.28)}}

test FixPoint-5.2 {divide} {
    set div_1 [java::new $ctor_double 7.5734 $q_20_12]
    set div_2 [java::new $ctor_double -7.23 $q_20_12]
    set div_3 [$div_1 divide $div_2]
    list "[$div_1 toString] [$div_2 toString] [$div_3 toString] [$div_3 toBitString] [[$div_3 getPrecision] toString]"
} {{7.573486328125 -7.22998046875 -1.047607421875 -10.111100111101 (20.12)}}

test FixPoint-5.3 {divide} {
    set div_1 [java::new $ctor_double 7.5734 $q_20_28]
    set div_2 [java::new $ctor_double -7.23 $q_20_28]
    set div_3 [$div_1 divide $div_2]
    list "[$div_1 toString] [$div_2 toString] [$div_3 toString] [$div_3 toBitString] [[$div_3 getPrecision] toString]"
} {{7.573399998247623443603515625 -7.2300000004470348358154296875 -1.04749654233455657958984375 -10.1111001111010111010001000100 (20.28)}}

test FixPoint-5.4 {divide-by-zero} {
    set div_1 [java::new $ctor_double 1 $q_20_2]
    set div_2 [java::new $ctor_double 0 $q_20_2]
    set div_3 [java::new $ctor_double -1 $q_20_2]
    catch { set div_4 [$div_1 divide $div_2] } msg
    set div_5 [$div_1 divide $div_2 $q_20_28]
    set div_6 [$div_3 divide $div_2 $q_20_28]
    set div_7 [$div_2 divide $div_2 $q_20_28]
    list "
$msg
[$div_5 toString]
[$div_6 toString]
[$div_7 toString]"
} {{
java.lang.IllegalArgumentException: ArithmeticException while dividing 1.0 by 0.0.
524287.9999999962747097015380859375
-524288.0
524287.9999999962747097015380859375}}

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

test FixPoint-10.0 {quantize-saturate} {
    set q0 [java::new ptolemy.math.FixPointQuantization "20.10,grow" ]
    set q1 [java::new ptolemy.math.FixPointQuantization "3.1,saturate,nearest" ]
    set q_0 [java::new ptolemy.math.FixPointQuantization "20.0,grow" ]
    set q_2 [java::new ptolemy.math.FixPointQuantization "20.-2,grow" ]
    list "
 7.000 [[[java::new ptolemy.math.FixPoint "7.000" $q0] quantize $q1] toString]
 4.000 [[[java::new ptolemy.math.FixPoint "4.000" $q_2] quantize $q1] toString]
 3.250 [[[java::new ptolemy.math.FixPoint "3.250" $q0] quantize $q1] toString]
 3.249 [[[java::new ptolemy.math.FixPoint "3.249" $q0] quantize $q1] toString]
-3.250 [[[java::new ptolemy.math.FixPoint "-3.250" $q0] quantize $q1] toString]
-3.251 [[[java::new ptolemy.math.FixPoint "-3.251" $q0] quantize $q1] toString]
-4.000 [[[java::new ptolemy.math.FixPoint "-4.000" $q_0] quantize $q1] toString]
-7.000 [[[java::new ptolemy.math.FixPoint "-7.000" $q0] quantize $q1] toString] "
} {{
 7.000 3.5
 4.000 3.5
 3.250 3.5
 3.249 3.0
-3.250 -3.0
-3.251 -3.5
-4.000 -4.0
-7.000 -4.0 }}

test FixPoint-10.1 {quantize-modulo} {
    set q1 [java::new ptolemy.math.FixPointQuantization "3.1,modulo,nearest" ]
    list "
 7.000 [[[java::new ptolemy.math.FixPoint "7.000" $q0] quantize $q1] toString]
 4.000 [[[java::new ptolemy.math.FixPoint "4.000" $q0] quantize $q1] toString]
 3.250 [[[java::new ptolemy.math.FixPoint "3.250" $q0] quantize $q1] toString]
 3.249 [[[java::new ptolemy.math.FixPoint "3.249" $q0] quantize $q1] toString]
-3.250 [[[java::new ptolemy.math.FixPoint "-3.250" $q0] quantize $q1] toString]
-3.251 [[[java::new ptolemy.math.FixPoint "-3.251" $q0] quantize $q1] toString]
-4.000 [[[java::new ptolemy.math.FixPoint "-4.000" $q0] quantize $q1] toString]
-7.000 [[[java::new ptolemy.math.FixPoint "-7.000" $q0] quantize $q1] toString] "
} {{
 7.000 -1.0
 4.000 -4.0
 3.250 3.5
 3.249 3.0
-3.250 -3.0
-3.251 -3.5
-4.000 -4.0
-7.000 1.0 }}
