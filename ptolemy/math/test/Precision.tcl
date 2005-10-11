# Tests for the Precision class
#
# @Author: Mike Wirthlin
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


### Constructor tests test:
### - the constructor
### - getExponent
### - getNumberOfBits
### - getSign
test Precision-1.0 {constructors-string} {

    set p0 [java::new ptolemy.math.Precision "3.2"]
    set p1 [java::new ptolemy.math.Precision "0.7"]
    set p2 [java::new ptolemy.math.Precision "-2.12"]
    set p3 [java::new ptolemy.math.Precision "12.-4"]
    set p4 [java::new ptolemy.math.Precision "32.0"]
    set p5 [java::new ptolemy.math.Precision "U1.7"]

    set p6 [java::new ptolemy.math.Precision "5/3"]
    set p7 [java::new ptolemy.math.Precision "7/0"]
    set p8 [java::new ptolemy.math.Precision "10/-2"]
    set p9 [java::new ptolemy.math.Precision "8/12"]
    set p10 [java::new ptolemy.math.Precision "32/32"]
    set p11 [java::new ptolemy.math.Precision "U8/1"]

    set p12 [java::new ptolemy.math.Precision "2:-2"]
    set p13 [java::new ptolemy.math.Precision "-1:-7"]
    set p14 [java::new ptolemy.math.Precision "-3:-12"]
    set p15 [java::new ptolemy.math.Precision "11:4"]
    set p16 [java::new ptolemy.math.Precision "31:0"]
    set p17 [java::new ptolemy.math.Precision "U0:-7"]

    set p18 [java::new ptolemy.math.Precision "5e-2"]
    set p19 [java::new ptolemy.math.Precision "7e-7"]
    set p20 [java::new ptolemy.math.Precision "10e-12"]
    set p21 [java::new ptolemy.math.Precision "8e4"]
    set p22 [java::new ptolemy.math.Precision "32e0"]
    set p23 [java::new ptolemy.math.Precision "U8e-7"]


    list "
[$p0 toString] [$p0 getNumberOfBits] [$p0 getExponent] [$p0 getSign]
[$p1 toString] [$p1 getNumberOfBits] [$p1 getExponent] [$p1 getSign]
[$p2 toString] [$p2 getNumberOfBits] [$p2 getExponent] [$p2 getSign]
[$p3 toString] [$p3 getNumberOfBits] [$p3 getExponent] [$p3 getSign]
[$p4 toString] [$p4 getNumberOfBits] [$p4 getExponent] [$p4 getSign]
[$p5 toString] [$p5 getNumberOfBits] [$p5 getExponent] [$p5 getSign]
[$p6 toString] [$p6 getNumberOfBits] [$p6 getExponent] [$p6 getSign]
[$p7 toString] [$p7 getNumberOfBits] [$p7 getExponent] [$p7 getSign]
[$p8 toString] [$p8 getNumberOfBits] [$p8 getExponent] [$p8 getSign]
[$p9 toString] [$p9 getNumberOfBits] [$p9 getExponent] [$p9 getSign]
[$p10 toString] [$p10 getNumberOfBits] [$p10 getExponent] [$p10 getSign]
[$p11 toString] [$p11 getNumberOfBits] [$p11 getExponent] [$p11 getSign]
[$p12 toString] [$p12 getNumberOfBits] [$p12 getExponent] [$p12 getSign]
[$p13 toString] [$p13 getNumberOfBits] [$p13 getExponent] [$p13 getSign]
[$p14 toString] [$p14 getNumberOfBits] [$p14 getExponent] [$p14 getSign]
[$p15 toString] [$p15 getNumberOfBits] [$p15 getExponent] [$p15 getSign]
[$p16 toString] [$p16 getNumberOfBits] [$p16 getExponent] [$p16 getSign]
[$p17 toString] [$p17 getNumberOfBits] [$p17 getExponent] [$p17 getSign]
[$p18 toString] [$p18 getNumberOfBits] [$p18 getExponent] [$p18 getSign]
[$p19 toString] [$p19 getNumberOfBits] [$p19 getExponent] [$p19 getSign]
[$p20 toString] [$p20 getNumberOfBits] [$p20 getExponent] [$p20 getSign]
[$p21 toString] [$p21 getNumberOfBits] [$p21 getExponent] [$p21 getSign]
[$p22 toString] [$p22 getNumberOfBits] [$p22 getExponent] [$p22 getSign]
[$p23 toString] [$p23 getNumberOfBits] [$p23 getExponent] [$p23 getSign] "
} {{
(3.2) 5 -2 1
(0.7) 7 -7 1
(-2.12) 10 -12 1
(12.-4) 8 4 1
(32.0) 32 0 1
(U1.7) 8 -7 0
(5/3) 5 -2 1
(7/0) 7 -7 1
(10/-2) 10 -12 1
(8/12) 8 4 1
(32/32) 32 0 1
(U8/1) 8 -7 0
(2:-2) 5 -2 1
(-1:-7) 7 -7 1
(-3:-12) 10 -12 1
(11:4) 8 4 1
(31:0) 32 0 1
(U0:-7) 8 -7 0
(5e-2) 5 -2 1
(7e-7) 7 -7 1
(10e-12) 10 -12 1
(8e4) 8 4 1
(32e0) 32 0 1
(U8e-7) 8 -7 0 }}

test Precision-1.1 {constructors-string-bad} {

    set ctor_int_int {ptolemy.math.Precision int int int}

    # Unrecognizable formats
    catch { set q [java::new ptolemy.math.Precision "1"] } msg1
    catch { set q [java::new ptolemy.math.Precision "1+2"] } msg2
    catch { set q [java::new ptolemy.math.Precision "1\\2"] } msg3
    # test illegal variations of known formats   
    catch { set q [java::new ptolemy.math.Precision "-22.12"] } msg4
    catch { set q [java::new ptolemy.math.Precision "3.-4"] } msg5
    catch { set q [java::new ptolemy.math.Precision "3.-3"] } msg6
    catch { set q [java::new ptolemy.math.Precision "0/2"] } msg7
    catch { set q [java::new ptolemy.math.Precision "-2:1"] } msg8
    catch { set q [java::new ptolemy.math.Precision "0:0"] } msg9
    catch { set q [java::new ptolemy.math.Precision "s0e2"] } msg10

    list "
$msg1
$msg2
$msg3
$msg4
$msg5
$msg6
$msg7
$msg8
$msg9
$msg10 "
} {{
java.lang.IllegalArgumentException: Unrecognized Precision String:1
java.lang.IllegalArgumentException: Unrecognized Precision String:1+2
java.lang.IllegalArgumentException: Unrecognized Precision String:1\2
java.lang.IllegalArgumentException: Precision format  must be at least 1 bit:-22.12
java.lang.IllegalArgumentException: Precision format  must be at least 1 bit:3.-4
java.lang.IllegalArgumentException: Precision format  must be at least 1 bit:3.-3
java.lang.IllegalArgumentException: Precision format must be at least 1 bit:0/2
java.lang.IllegalArgumentException: MSb of VHDL format must be greater than LSb:-2:1
java.lang.IllegalArgumentException: MSb of VHDL format must be greater than LSb:0:0
java.lang.IllegalArgumentException: Precision format must be at least 1 bit:s0e2 }}

test Precision-1.2 {constructors-int-int-int} {

    set ctor_int_int {ptolemy.math.Precision int int int}

    set p23 [java::new $ctor_int_int 1 5 -2]
    set p24 [java::new $ctor_int_int 1 7 -7]
    set p25 [java::new $ctor_int_int 1 10 -12]
    set p26 [java::new $ctor_int_int 1 8 4]
    set p27 [java::new $ctor_int_int 1 32 0]
    set p28 [java::new $ctor_int_int 0 8 -7]

    list "
[$p23 toString] [$p23 getNumberOfBits] [$p23 getExponent] [$p23 getSign]
[$p24 toString] [$p24 getNumberOfBits] [$p24 getExponent] [$p24 getSign]
[$p25 toString] [$p25 getNumberOfBits] [$p25 getExponent] [$p25 getSign]
[$p26 toString] [$p26 getNumberOfBits] [$p26 getExponent] [$p26 getSign]
[$p27 toString] [$p27 getNumberOfBits] [$p27 getExponent] [$p27 getSign]
[$p28 toString] [$p28 getNumberOfBits] [$p28 getExponent] [$p28 getSign] "
} {{
(3.2) 5 -2 1
(0.7) 7 -7 1
(-2.12) 10 -12 1
(12.-4) 8 4 1
(32.0) 32 0 1
(U1.7) 8 -7 0 }}

test Precision-1.3 {constructors-int-int} {

    set ctor_int_int {ptolemy.math.Precision int int}

    set p29 [java::new $ctor_int_int 5 3]
    set p30 [java::new $ctor_int_int 7 0]
    set p31 [java::new $ctor_int_int 10 -2]
    set p32 [java::new $ctor_int_int 8 12]
    set p33 [java::new $ctor_int_int 32 32]
    set p34 [java::new $ctor_int_int 8 1]

    list "
[$p29 toString] [$p29 getNumberOfBits] [$p29 getExponent] [$p29 getSign]
[$p30 toString] [$p30 getNumberOfBits] [$p30 getExponent] [$p30 getSign]
[$p31 toString] [$p31 getNumberOfBits] [$p31 getExponent] [$p31 getSign]
[$p32 toString] [$p32 getNumberOfBits] [$p32 getExponent] [$p32 getSign]
[$p33 toString] [$p33 getNumberOfBits] [$p33 getExponent] [$p33 getSign]
[$p34 toString] [$p34 getNumberOfBits] [$p34 getExponent] [$p34 getSign] "
} {{
(3.2) 5 -2 1
(0.7) 7 -7 1
(-2.12) 10 -12 1
(12.-4) 8 4 1
(32.0) 32 0 1
(1.7) 8 -7 1 }}


test Precision-2.0 {findMaximum-findMinimum} {

    list "
[$p0 toString] [[$p0 findMaximum] toString ] [[$p0 findMinimum] toString]
[$p6 toString] [[$p6 findMaximum] toString ] [[$p6 findMinimum] toString]
[$p12 toString] [[$p12 findMaximum] toString ] [[$p12 findMinimum] toString]
[$p18 toString] [[$p18 findMaximum] toString ] [[$p18 findMinimum] toString] "
} {{
(3.2) 3.75 -4
(5/3) 3.75 -4
(2:-2) 3.75 -4
(5e-2) 3.75 -4 }}

test Precision-3.0 {getEpsilon-getNumberOfLevels} {

    list "
[$p1 toString] [[$p1 getEpsilon] toString ] [$p1 getNumberOfLevels]
[$p7 toString] [[$p7 getEpsilon] toString ] [$p7 getNumberOfLevels]
[$p13 toString] [[$p13 getEpsilon] toString ] [$p13 getNumberOfLevels]
[$p19 toString] [[$p19 getEpsilon] toString ] [$p19 getNumberOfLevels] "
} {{
(0.7) 0.0078125 128.0
(7/0) 0.0078125 128.0
(-1:-7) 0.0078125 128.0
(7e-7) 0.0078125 128.0 }}


test Precision-4.0 {getFractionalBitLength-getIntegerBitLength} {

    list "
[$p2 toString] [$p2 getIntegerBitLength].[$p2 getFractionBitLength]
[$p8 toString] [$p8 getIntegerBitLength].[$p8 getFractionBitLength]
[$p14 toString] [$p14 getIntegerBitLength].[$p14 getFractionBitLength]
[$p20 toString] [$p20 getIntegerBitLength].[$p20 getFractionBitLength] "
} {{
(-2.12) -2.12
(10/-2) -2.12
(-3:-12) -2.12
(10e-12) -2.12 }}

test Precision-5.0 {getMSB-getLSB} {

    list "
[$p3 toString] [$p3 getMostSignificantBitPosition]:[$p3 getLeastSignificantBitPosition]
[$p9 toString] [$p9 getMostSignificantBitPosition]:[$p9 getLeastSignificantBitPosition]
[$p15 toString] [$p15 getMostSignificantBitPosition]:[$p15 getLeastSignificantBitPosition]
[$p21 toString] [$p21 getMostSignificantBitPosition]:[$p21 getLeastSignificantBitPosition] "
} {{
(12.-4) 11:4
(8/12) 11:4
(11:4) 11:4
(8e4) 11:4 }}

test Precision-5.0 {getUnscaledValue} {

    list "
[$p4 toString] [[$p4 getMaximumUnscaledValue] toString] >= x >= [[$p4 getMinimumUnscaledValue] toString]
[$p10 toString] [[$p10 getMaximumUnscaledValue] toString] >= x >= [[$p10 getMinimumUnscaledValue] toString]
[$p16 toString] [[$p16 getMaximumUnscaledValue] toString] >= x >= [[$p16 getMinimumUnscaledValue] toString]
[$p22 toString] [[$p22 getMaximumUnscaledValue] toString] >= x >= [[$p22 getMinimumUnscaledValue] toString] "
} {{
(32.0) 2147483647 >= x >= -2147483648
(32/32) 2147483647 >= x >= -2147483648
(31:0) 2147483647 >= x >= -2147483648
(32e0) 2147483647 >= x >= -2147483648 }}


test Precision-6.0 {equals} {

    list "
[$p0 toString] equals [$p6 toString] [$p0 equals $p6]
[$p6 toString] equals [$p12 toString] [$p6 equals $p12]
[$p12 toString] equals [$p18 toString] [$p12 equals $p18]
[$p18 toString] equals [$p0 toString] [$p18 equals $p0]
[$p18 toString] equals [$p1 toString] [$p18 equals $p1] "
} {{
(3.2) equals (5/3) 1
(5/3) equals (2:-2) 1
(2:-2) equals (5e-2) 1
(5e-2) equals (3.2) 1
(5e-2) equals (0.7) 0 }}

test Precision-7.0 {matchThePoint} {

    set matchThePoint {ptolemy.math.Precision matchThePoint}

    list "
[$p0 toString] [$p1 toString] [[$p0 matchThePoint $p0 $p1] toString]
[$p1 toString] [$p2 toString] [[$p1 matchThePoint $p1 $p2] toString]
[$p2 toString] [$p3 toString] [[$p2 matchThePoint $p2 $p3] toString]
[$p3 toString] [$p4 toString] [[$p3 matchThePoint $p3 $p4] toString]
[$p4 toString] [$p5 toString] [[$p4 matchThePoint $p4 $p5] toString]
[$p5 toString] [$p6 toString] [[$p5 matchThePoint $p5 $p6] toString]
[$p6 toString] [$p7 toString] [[$p6 matchThePoint $p6 $p7] toString]
[$p5 toString] [$p11 toString] [[$p5 matchThePoint $p5 $p11] toString]
[$p4 toString] [$p4 toString] [[$p4 matchThePoint $p4 $p4] toString] "
} {{
(3.2) (0.7) (3.7)
(0.7) (-2.12) (0.12)
(-2.12) (12.-4) (12.12)
(12.-4) (32.0) (32.0)
(32.0) (U1.7) (32.7)
(U1.7) (5/3) (3.7)
(5/3) (7/0) (3.7)
(U1.7) (U8/1) (U1.7)
(32.0) (32.0) (32.0) }}


