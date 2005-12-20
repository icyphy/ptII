# Tests for the Overflow Class
#
# @Author: Ed.Willink
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################

test Overflow-1.0 {names} {
    set overflow_clip [java::call ptolemy.math.Overflow forName "clip"];
    set overflow_grow [java::call ptolemy.math.Overflow forName "grow"];
    set overflow_modulo [java::call ptolemy.math.Overflow forName "modulo"];
    set overflow_saturate [java::call ptolemy.math.Overflow forName "saturate"];
    set overflow_throw [java::call ptolemy.math.Overflow forName "throw"];
    set overflow_to_zero [java::call ptolemy.math.Overflow getName "to_zero"];
    set overflow_trap [java::call ptolemy.math.Overflow getName "trap"];
    set overflow_wrap [java::call ptolemy.math.Overflow getName "wrap"];
    set overflow_minimize [java::call ptolemy.math.Overflow getName "minimize"];
    set overflow_shrink [java::call ptolemy.math.Overflow getName "shrink"];
    catch { set overflow_zzz [java::call ptolemy.math.Overflow getName "zzz"]; } msg
    list "
[ $overflow_clip toString ] 
[ $overflow_grow toString ] 
[ $overflow_modulo toString ] 
[ $overflow_saturate toString ] 
[ $overflow_throw toString ] 
[ $overflow_to_zero toString ] 
[ $overflow_trap toString ] 
[ $overflow_wrap toString ] 
[ $overflow_minimize toString ] 
[ $overflow_shrink toString ] 
$msg "
} {{
saturate 
grow 
modulo 
saturate 
trap 
to_zero 
trap 
modulo 
minimize 
minimize 
java.lang.IllegalArgumentException: Unknown overflow strategy "zzz". }}


####################################################################
test Overflow-1.1 {isOutOfRange} {

    set big_100 [java::new java.math.BigInteger "100" ]
    set big_99 [java::new java.math.BigInteger "99" ]
    set big_5 [java::new java.math.BigInteger "5" ]
    set big_4 [java::new java.math.BigInteger "4" ]
    set big_3 [java::new java.math.BigInteger "3" ]
    set big_1 [java::new java.math.BigInteger "1" ]
    set big_0 [java::new java.math.BigInteger "0" ]
    set big__1 [java::new java.math.BigInteger "-1" ]
    set big__3 [java::new java.math.BigInteger "-3" ]
    set big__4 [java::new java.math.BigInteger "-4" ]
    set big__5 [java::new java.math.BigInteger "-5" ]
    set big__99 [java::new java.math.BigInteger "-99" ]

    set p_s3_0 [java::new ptolemy.math.Precision "s3.0" ]
    set p_u3_0 [java::new ptolemy.math.Precision "u3.0" ]

    list \
	[$overflow_grow isOutOfRange $big_99 $p_s3_0] \
	[$overflow_grow isOutOfRange $big_5 $p_s3_0] \
	[$overflow_grow isOutOfRange $big_4 $p_s3_0] \
	[$overflow_grow isOutOfRange $big_3 $p_s3_0] \
	[$overflow_grow isOutOfRange $big_0 $p_s3_0] \
	[$overflow_grow isOutOfRange $big__3 $p_s3_0] \
	[$overflow_grow isOutOfRange $big__4 $p_s3_0] \
	[$overflow_grow isOutOfRange $big__5 $p_s3_0] \
	[$overflow_grow isOutOfRange $big__99 $p_s3_0] \
	[$overflow_grow isOutOfRange $big_99 $p_u3_0] \
	[$overflow_grow isOutOfRange $big_5 $p_u3_0] \
	[$overflow_grow isOutOfRange $big_4 $p_u3_0] \
	[$overflow_grow isOutOfRange $big_3 $p_u3_0] \
	[$overflow_grow isOutOfRange $big_0 $p_u3_0] \
	[$overflow_grow isOutOfRange $big__3 $p_u3_0] \
	[$overflow_grow isOutOfRange $big__4 $p_u3_0] \
	[$overflow_grow isOutOfRange $big__5 $p_u3_0] \
	[$overflow_grow isOutOfRange $big__99 $p_u3_0]
} {1 1 1 0 0 0 0 1 1 1 0 0 0 0 1 1 1 1}

####################################################################
test Overflow-1.1 {isOverflow} {

    list \
	[$overflow_grow isOverflow $big_99 $p_s3_0] \
	[$overflow_grow isOverflow $big_5 $p_s3_0] \
	[$overflow_grow isOverflow $big_4 $p_s3_0] \
	[$overflow_grow isOverflow $big_3 $p_s3_0] \
	[$overflow_grow isOverflow $big_0 $p_s3_0] \
	[$overflow_grow isOverflow $big__3 $p_s3_0] \
	[$overflow_grow isOverflow $big__4 $p_s3_0] \
	[$overflow_grow isOverflow $big__5 $p_s3_0] \
	[$overflow_grow isOverflow $big__99 $p_s3_0] \
	[$overflow_grow isOverflow $big_99 $p_u3_0] \
	[$overflow_grow isOverflow $big_5 $p_u3_0] \
	[$overflow_grow isOverflow $big_4 $p_u3_0] \
	[$overflow_grow isOverflow $big_3 $p_u3_0] \
	[$overflow_grow isOverflow $big_0 $p_u3_0] \
	[$overflow_grow isOverflow $big__3 $p_u3_0] \
	[$overflow_grow isOverflow $big__4 $p_u3_0] \
	[$overflow_grow isOverflow $big__5 $p_u3_0] \
	[$overflow_grow isOverflow $big__99 $p_u3_0]
} {1 1 1 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0}

####################################################################
test Overflow-1.2 {isUnderflow} {

    list \
	[$overflow_grow isUnderflow $big_99 $p_s3_0] \
	[$overflow_grow isUnderflow $big_5 $p_s3_0] \
	[$overflow_grow isUnderflow $big_4 $p_s3_0] \
	[$overflow_grow isUnderflow $big_3 $p_s3_0] \
	[$overflow_grow isUnderflow $big_0 $p_s3_0] \
	[$overflow_grow isUnderflow $big__3 $p_s3_0] \
	[$overflow_grow isUnderflow $big__4 $p_s3_0] \
	[$overflow_grow isUnderflow $big__5 $p_s3_0] \
	[$overflow_grow isUnderflow $big__99 $p_s3_0] \
	[$overflow_grow isUnderflow $big_99 $p_u3_0] \
	[$overflow_grow isUnderflow $big_5 $p_u3_0] \
	[$overflow_grow isUnderflow $big_4 $p_u3_0] \
	[$overflow_grow isUnderflow $big_3 $p_u3_0] \
	[$overflow_grow isUnderflow $big_0 $p_u3_0] \
	[$overflow_grow isUnderflow $big__3 $p_u3_0] \
	[$overflow_grow isUnderflow $big__4 $p_u3_0] \
	[$overflow_grow isUnderflow $big__5 $p_u3_0] \
	[$overflow_grow isUnderflow $big__99 $p_u3_0]
} {0 0 0 0 0 0 0 1 1 0 0 0 0 0 1 1 1 1}

####################################################################
test Overflow-1.5 {clone} {
    set clone [$overflow_grow clone]
    list \
	[$clone equals $overflow_grow] \
	[expr {[$clone hashCode] == [$overflow_grow hashCode]}] \
	[$clone equals $overflow_trap] \
	[expr {[$clone hashCode] == [$overflow_trap hashCode]}]
} {1 1 0 0}

####################################################################
test Overflow-2.0 {quantizeGrow} {

    catch { [$overflow_grow quantizeGrow $big__3 $p_u3_0] toStringPrecision]; } msg1
    catch { [$overflow_grow quantizeGrow $big__4 $p_u3_0] toStringPrecision]; } msg2
    catch { [$overflow_grow quantizeGrow $big__5 $p_u3_0] toStringPrecision]; } msg3
    catch { [$overflow_grow quantizeGrow $big__99 $p_u3_0] toStringPrecision]; } msg4

    list "
[ [ $overflow_grow quantizeGrow $big_99 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_5 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_4 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_3 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_0 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big__3 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big__4 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big__5 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big__99 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_99 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_5 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_4 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_3 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeGrow $big_0 $p_u3_0] toStringPrecision ] 
$msg1
$msg2
$msg3
$msg4 "
} {{
99(8.0) 
5(4.0) 
4(4.0) 
3(3.0) 
0(3.0) 
-3(3.0) 
-4(3.0) 
-5(4.0) 
-99(8.0) 
99(U7.0) 
5(U3.0) 
4(U3.0) 
3(U3.0) 
0(U3.0) 
java.lang.ArithmeticException: Precision (U2.0) not sufficient to represent -3
java.lang.ArithmeticException: Precision (U2.0) not sufficient to represent -4
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -5
java.lang.ArithmeticException: Precision (U7.0) not sufficient to represent -99 }}

####################################################################
test Overflow-2.1 {quantizeMinimum} {

    catch { [$overflow_grow quantizeMinimum $big__3 $p_u3_0] toStringPrecision]; } msg1
    catch { [$overflow_grow quantizeMinimum $big__4 $p_u3_0] toStringPrecision]; } msg2
    catch { [$overflow_grow quantizeMinimum $big__5 $p_u3_0] toStringPrecision]; } msg3
    catch { [$overflow_grow quantizeMinimum $big__99 $p_u3_0] toStringPrecision]; } msg4
    catch { [$overflow_grow quantizeMinimum $big__1 $p_u3_0] toStringPrecision]; } msg5

    list "
[ [ $overflow_grow quantizeMinimum $big_99 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_5 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_4 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_3 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_1 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_0 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big__1 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big__3 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big__4 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big__5 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big__99 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_99 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_5 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_4 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_3 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_1 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeMinimum $big_0 $p_u3_0] toStringPrecision ] 
$msg1
$msg2
$msg3
$msg4
$msg5 "
} {{
99(8.0) 
5(4.0) 
4(4.0) 
3(3.0) 
1(2.0) 
0(2.0) 
-1(2.0) 
-3(3.0) 
-4(3.0) 
-5(4.0) 
-99(8.0) 
99(U7.0) 
5(U3.0) 
4(U3.0) 
3(U2.0) 
1(U1.0) 
0(U1.0) 
java.lang.ArithmeticException: Precision (U2.0) not sufficient to represent -3
java.lang.ArithmeticException: Precision (U2.0) not sufficient to represent -4
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -5
java.lang.ArithmeticException: Precision (U7.0) not sufficient to represent -99
java.lang.ArithmeticException: Precision (U1.0) not sufficient to represent -1 }}

####################################################################
test Overflow-2.2 {quantizeModulo} {

    catch { [$overflow_grow quantizeModulo $big__3 $p_u3_0] toStringPrecision]; } msg1
    catch { [$overflow_grow quantizeModulo $big__4 $p_u3_0] toStringPrecision]; } msg2
    catch { [$overflow_grow quantizeModulo $big__5 $p_u3_0] toStringPrecision]; } msg3
    catch { [$overflow_grow quantizeModulo $big__99 $p_u3_0] toStringPrecision]; } msg4
    catch { [$overflow_grow quantizeModulo $big__1 $p_u3_0] toStringPrecision]; } msg5

    list "
[$big_100 toString 2] [ [ $overflow_grow quantizeModulo $big_100 $p_s3_0] toStringPrecision ] 
[$big_99 toString 2] [ [ $overflow_grow quantizeModulo $big_99 $p_s3_0] toStringPrecision ] 
[$big_5 toString 2] [ [ $overflow_grow quantizeModulo $big_5 $p_s3_0] toStringPrecision ] 
[$big_4 toString 2] [ [ $overflow_grow quantizeModulo $big_4 $p_s3_0] toStringPrecision ] 
[$big_3 toString 2] [ [ $overflow_grow quantizeModulo $big_3 $p_s3_0] toStringPrecision ] 
[$big_1 toString 2] [ [ $overflow_grow quantizeModulo $big_1 $p_s3_0] toStringPrecision ] 
[$big_0 toString 2] [ [ $overflow_grow quantizeModulo $big_0 $p_s3_0] toStringPrecision ] 
[$big__1 toString 2] [ [ $overflow_grow quantizeModulo $big__1 $p_s3_0] toStringPrecision ] 
[$big__3 toString 2] [ [ $overflow_grow quantizeModulo $big__3 $p_s3_0] toStringPrecision ] 
[$big__4 toString 2] [ [ $overflow_grow quantizeModulo $big__4 $p_s3_0] toStringPrecision ] 
[$big__5 toString 2] [ [ $overflow_grow quantizeModulo $big__5 $p_s3_0] toStringPrecision ] 
[$big__99 toString 2] [ [ $overflow_grow quantizeModulo $big__99 $p_s3_0] toStringPrecision ] 
[$big_100 toString 2] [ [ $overflow_grow quantizeModulo $big_100 $p_u3_0] toStringPrecision ] 
[$big_99 toString 2] [ [ $overflow_grow quantizeModulo $big_99 $p_u3_0] toStringPrecision ] 
[$big_5 toString 2] [ [ $overflow_grow quantizeModulo $big_5 $p_u3_0] toStringPrecision ] 
[$big_4 toString 2] [ [ $overflow_grow quantizeModulo $big_4 $p_u3_0] toStringPrecision ] 
[$big_3 toString 2] [ [ $overflow_grow quantizeModulo $big_3 $p_u3_0] toStringPrecision ] 
[$big_1 toString 2] [ [ $overflow_grow quantizeModulo $big_1 $p_u3_0] toStringPrecision ] 
[$big_0 toString 2] [ [ $overflow_grow quantizeModulo $big_0 $p_u3_0] toStringPrecision ] 
$msg1
$msg2
$msg3
$msg4
$msg5 "
} {{
1100100 -4(3.0) 
1100011 3(3.0) 
101 -3(3.0) 
100 -4(3.0) 
11 3(3.0) 
1 1(3.0) 
0 0(3.0) 
-1 -1(3.0) 
-11 -3(3.0) 
-100 -4(3.0) 
-101 3(3.0) 
-1100011 -3(3.0) 
1100100 4(U3.0) 
1100011 3(U3.0) 
101 5(U3.0) 
100 4(U3.0) 
11 3(U3.0) 
1 1(U3.0) 
0 0(U3.0) 
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -3
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -4
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -5
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -3
java.lang.ArithmeticException: Precision (U3.0) not sufficient to represent -1 }}


####################################################################
test Overflow-2.3 {quantizeSaturate} {

    list "
[ [ $overflow_grow quantizeSaturate $big_100 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_99 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_5 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_4 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_3 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_1 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_0 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__1 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__3 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__4 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__5 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__99 $p_s3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_100 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_99 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_5 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_4 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_3 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_1 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big_0 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__1 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__3 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__4 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__5 $p_u3_0] toStringPrecision ] 
[ [ $overflow_grow quantizeSaturate $big__99 $p_u3_0] toStringPrecision ] "
} {{
3(3.0) 
3(3.0) 
3(3.0) 
3(3.0) 
3(3.0) 
1(3.0) 
0(3.0) 
-1(3.0) 
-3(3.0) 
-4(3.0) 
-4(3.0) 
-4(3.0) 
7(U3.0) 
7(U3.0) 
5(U3.0) 
4(U3.0) 
3(U3.0) 
1(U3.0) 
0(U3.0) 
0(U3.0) 
0(U3.0) 
0(U3.0) 
0(U3.0) 
0(U3.0) }}

####################################################################
test Overflow-2.4 {quantizeToZero} {

    catch { [$overflow_grow quantizeToZero $big_99 $p_u3_0] toStringPrecision]; } msg1
    catch { [$overflow_grow quantizeToZero $big_5 $p_u3_0] toStringPrecision]; } msg2
    catch { [$overflow_grow quantizeToZero $big_4 $p_u3_0] toStringPrecision]; } msg3
    catch { [$overflow_grow quantizeToZero $big__5 $p_u3_0] toStringPrecision]; } msg4
    catch { [$overflow_grow quantizeToZero $big__99 $p_u3_0] toStringPrecision]; } msg5

    list "
[ $big_3 toString] [ [ $overflow_grow quantizeToZero $big_3 $p_s3_0] toStringPrecision ] 
[ $big_1 toString] [ [ $overflow_grow quantizeToZero $big_1 $p_s3_0] toStringPrecision ] 
[ $big_0 toString] [ [ $overflow_grow quantizeToZero $big_0 $p_s3_0] toStringPrecision ] 
[ $big__1 toString] [ [ $overflow_grow quantizeToZero $big__1 $p_s3_0] toStringPrecision ] 
[ $big__3 toString] [ [ $overflow_grow quantizeToZero $big__3 $p_s3_0] toStringPrecision ] 
[ $big__4 toString] [ [ $overflow_grow quantizeToZero $big__4 $p_s3_0] toStringPrecision ] 
[ $big_99 toString] [ [ $overflow_grow quantizeToZero $big_99 $p_u3_0] toStringPrecision ] 
[ $big_5 toString] [ [ $overflow_grow quantizeToZero $big_5 $p_u3_0] toStringPrecision ] 
[ $big_4 toString] [ [ $overflow_grow quantizeToZero $big_4 $p_u3_0] toStringPrecision ] 
[ $big_3 toString] [ [ $overflow_grow quantizeToZero $big_3 $p_u3_0] toStringPrecision ] 
[ $big_1 toString] [ [ $overflow_grow quantizeToZero $big_1 $p_u3_0] toStringPrecision ] 
[ $big_0 toString] [ [ $overflow_grow quantizeToZero $big_0 $p_u3_0] toStringPrecision ] 
[ $big__1 toString] [ [ $overflow_grow quantizeToZero $big__1 $p_u3_0] toStringPrecision ] 
[ $big__3 toString] [ [ $overflow_grow quantizeToZero $big__3 $p_u3_0] toStringPrecision ] 
[ $big__4 toString] [ [ $overflow_grow quantizeToZero $big__4 $p_u3_0] toStringPrecision ] 
[ $big__5 toString] [ [ $overflow_grow quantizeToZero $big__5 $p_u3_0] toStringPrecision ] 
[ $big__99 toString] [ [ $overflow_grow quantizeToZero $big__99 $p_u3_0] toStringPrecision ] "
} {{
3 3(3.0) 
1 1(3.0) 
0 0(3.0) 
-1 -1(3.0) 
-3 -3(3.0) 
-4 -4(3.0) 
99 0(U3.0) 
5 5(U3.0) 
4 4(U3.0) 
3 3(U3.0) 
1 1(U3.0) 
0 0(U3.0) 
-1 0(U3.0) 
-3 0(U3.0) 
-4 0(U3.0) 
-5 0(U3.0) 
-99 0(U3.0) }}

####################################################################
test Overflow-2.4 {trap} {

    catch { [$overflow_trap quantize $big_99 $p_s3_0] toStringPrecision]; } msg1
    catch { [$overflow_trap quantize $big_5 $p_s3_0] toStringPrecision]; } msg2
    catch { [$overflow_trap quantize $big_4 $p_s3_0] toStringPrecision]; } msg3
    catch { [$overflow_trap quantize $big__4 $p_s3_0] toStringPrecision]; } msg4
    catch { [$overflow_trap quantize $big__5 $p_s3_0] toStringPrecision]; } msg5
    catch { [$overflow_trap quantize $big__99 $p_s3_0] toStringPrecision]; } msg6
    catch { [$overflow_trap quantize $big_99 $p_u3_0] toStringPrecision]; } msg7
    catch { [$overflow_trap quantize $big__1 $p_u3_0] toStringPrecision]; } msg8
    catch { [$overflow_trap quantize $big__3 $p_u3_0] toStringPrecision]; } msg9
    catch { [$overflow_trap quantize $big__4 $p_u3_0] toStringPrecision]; } msg10
    catch { [$overflow_trap quantize $big__5 $p_u3_0] toStringPrecision]; } msg11
    catch { [$overflow_trap quantize $big__99 $p_u3_0] toStringPrecision]; } msg12

    list "
[ $big_3 toString] [ [ $overflow_trap quantize $big_3 $p_s3_0] toStringPrecision ] 
[ $big_1 toString] [ [ $overflow_trap quantize $big_1 $p_s3_0] toStringPrecision ] 
[ $big_0 toString] [ [ $overflow_trap quantize $big_0 $p_s3_0] toStringPrecision ] 
[ $big__1 toString] [ [ $overflow_trap quantize $big__1 $p_s3_0] toStringPrecision ] 
[ $big__3 toString] [ [ $overflow_trap quantize $big__3 $p_s3_0] toStringPrecision ] 
[ $big_5 toString] [ [ $overflow_trap quantize $big_5 $p_u3_0] toStringPrecision ] 
[ $big_4 toString] [ [ $overflow_trap quantize $big_4 $p_u3_0] toStringPrecision ] 
[ $big_3 toString] [ [ $overflow_trap quantize $big_3 $p_u3_0] toStringPrecision ] 
[ $big_1 toString] [ [ $overflow_trap quantize $big_1 $p_u3_0] toStringPrecision ] 
[ $big_0 toString] [ [ $overflow_trap quantize $big_0 $p_u3_0] toStringPrecision ] 
$msg1
$msg2
$msg3
$msg4
$msg5
$msg6
$msg7
$msg8
$msg9
$msg10
$msg11
$msg12 "
} {{
3 3(3.0) 
1 1(3.0) 
0 0(3.0) 
-1 -1(3.0) 
-3 -3(3.0) 
5 5(U3.0) 
4 4(U3.0) 
3 3(U3.0) 
1 1(U3.0) 
0 0(U3.0) 
java.lang.ArithmeticException: Maximum overflow threshold of 3 exceeded with value 99
java.lang.ArithmeticException: Maximum overflow threshold of 3 exceeded with value 5
java.lang.ArithmeticException: Maximum overflow threshold of 3 exceeded with value 4
no such method "toStringPrecision]" in class ptolemy.math.FixPoint
java.lang.ArithmeticException: Minimum overflow threshold of -4 exceeded with value -5
java.lang.ArithmeticException: Minimum overflow threshold of -4 exceeded with value -99
java.lang.ArithmeticException: Maximum overflow threshold of 7 exceeded with value 99
java.lang.ArithmeticException: Minimum overflow threshold of 0 exceeded with value -1
java.lang.ArithmeticException: Minimum overflow threshold of 0 exceeded with value -3
java.lang.ArithmeticException: Minimum overflow threshold of 0 exceeded with value -4
java.lang.ArithmeticException: Minimum overflow threshold of 0 exceeded with value -5
java.lang.ArithmeticException: Minimum overflow threshold of 0 exceeded with value -99 }}


####################################################################
test Overflow-2.2.5x {minusInfinity} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,to_zero,unnecessary" ]
    list "
[[ $overflow_clip minusInfinity $quant_3_0 ] toString]
[ $overflow_grow minusInfinity $quant_3_0 ]
[ $overflow_modulo minusInfinity $quant_3_0 ]
[[ $overflow_saturate minusInfinity $quant_3_0 ] toString]
[ $overflow_throw minusInfinity $quant_3_0 ]
[[ $overflow_to_zero minusInfinity $quant_3_0 ] toString]
[ $overflow_trap minusInfinity $quant_3_0 ]
[ $overflow_wrap minusInfinity $quant_3_0 ]
"
} {{
-4
java0x0
java0x0
-4
java0x0
0
java0x0
java0x0
}}


####################################################################
test Overflow-2.2.5x {plusInfinity} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,to_zero,unnecessary" ]
    list "
[[ $overflow_clip plusInfinity $quant_3_0 ] toString]
[ $overflow_grow plusInfinity $quant_3_0 ]
[ $overflow_modulo plusInfinity $quant_3_0 ]
[[ $overflow_saturate plusInfinity $quant_3_0 ] toString]
[ $overflow_throw plusInfinity $quant_3_0 ]
[[ $overflow_to_zero plusInfinity $quant_3_0 ] toString]
[ $overflow_trap plusInfinity $quant_3_0 ]
[ $overflow_wrap plusInfinity $quant_3_0 ]
"
} {{
3
java0x0
java0x0
3
java0x0
0
java0x0
java0x0
}}

