# Tests for the Overflow Class
#
# @Author: Ed.Willink
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
    catch { set overflow_zzz [java::call ptolemy.math.Overflow getName "zzz"]; } msg
    set big_99 [java::new java.math.BigInteger "99" ]
    set big_5 [java::new java.math.BigInteger "5" ]
    set big_4 [java::new java.math.BigInteger "4" ]
    set big_3 [java::new java.math.BigInteger "3" ]
    set big_2 [java::new java.math.BigInteger "2" ]
    set big_1 [java::new java.math.BigInteger "1" ]
    set big_0 [java::new java.math.BigInteger "0" ]
    set big__1 [java::new java.math.BigInteger "-1" ]
    set big__2 [java::new java.math.BigInteger "-2" ]
    set big__3 [java::new java.math.BigInteger "-3" ]
    set big__4 [java::new java.math.BigInteger "-4" ]
    set big__5 [java::new java.math.BigInteger "-5" ]
    set big__99 [java::new java.math.BigInteger "-99" ]
    list "
[ $overflow_clip toString ] 
[ $overflow_grow toString ] 
[ $overflow_modulo toString ] 
[ $overflow_saturate toString ] 
[ $overflow_throw toString ] 
[ $overflow_to_zero toString ] 
[ $overflow_trap toString ] 
[ $overflow_wrap toString ] 
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
java.lang.IllegalArgumentException: Unknown overflow strategy "zzz". }}

####################################################################
test Overflow-2.5 {clone} {
    set clone [$overflow_grow clone]
    list \
	[$clone equals $overflow_grow] \
	[expr {[$clone hashCode] == [$overflow_grow hashCode]}] \
	[$clone equals $overflow_trap] \
	[expr {[$clone hashCode] == [$overflow_trap hashCode]}]
} {1 1 0 0}



####################################################################

test Overflow-2.0 {grow} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,grow,unnecessary" ]
    list "
[ [ $overflow_grow quantize $big_99 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big_5 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big_4 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big_3 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big_2 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big_1 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big_0 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big__1 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big__2 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big__3 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big__4 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big__5 $quant_3_0] toString ] 
[ [ $overflow_grow quantize $big__99 $quant_3_0] toString ] "
} {{
99 
5 
4 
3 
2 
1 
0 
-1 
-2 
-3 
-4 
-5 
-99 }}

test Overflow-2.1 {modulo} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,modulo,unnecessary" ]
    list "
[ [ $overflow_modulo quantize $big_99 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big_5 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big_4 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big_3 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big_2 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big_1 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big_0 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big__1 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big__2 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big__3 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big__4 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big__5 $quant_3_0] toString ] 
[ [ $overflow_modulo quantize $big__99 $quant_3_0] toString ] "
} {{
3 
-3 
-4 
3 
2 
1 
0 
-1 
-2 
-3 
-4 
3 
-3 }}

test Overflow-2.2 {saturate} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,saturate,unnecessary" ]
    list "
[ [ $overflow_saturate quantize $big_99 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big_5 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big_4 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big_3 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big_2 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big_1 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big_0 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big__1 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big__2 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big__3 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big__4 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big__5 $quant_3_0] toString ] 
[ [ $overflow_saturate quantize $big__99 $quant_3_0] toString ] "
} {{
3 
3 
3 
3 
2 
1 
0 
-1 
-2 
-3 
-4 
-4 
-4 }}

test Overflow-2.3 {overflow_to_zero} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,to_zero,unnecessary" ]
    list "
[ [ $overflow_to_zero quantize $big_99 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big_5 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big_4 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big_3 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big_2 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big_1 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big_0 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big__1 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big__2 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big__3 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big__4 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big__5 $quant_3_0] toString ] 
[ [ $overflow_to_zero quantize $big__99 $quant_3_0] toString ] "
} {{
0 
0 
0 
3 
2 
1 
0 
-1 
-2 
-3 
-4 
0 
0 }}

test Overflow-2.4 {trap} {
    set quant_3_0 [java::new ptolemy.math.FixPointQuantization "3.0,trap,unnecessary" ]
    catch { [ [ $overflow_trap quantize $big_99 $quant_3_0] toString ] } msg_99
    catch { [ [ $overflow_trap quantize $big_5 $quant_3_0] toString ] } msg_5
    catch { [ [ $overflow_trap quantize $big_4 $quant_3_0] toString ] } msg_4
    catch { [ [ $overflow_trap quantize $big__5 $quant_3_0] toString ] } msg__5
    catch { [ [ $overflow_trap quantize $big__99 $quant_3_0] toString ] } msg__99
    list "
$msg_99 
$msg_5 
$msg_4 
[ [ $overflow_trap quantize $big_3 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big_2 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big_1 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big_0 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big__1 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big__2 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big__3 $quant_3_0] toString ] 
[ [ $overflow_trap quantize $big__4 $quant_3_0] toString ] 
$msg__5 
$msg__99 "
} {{
java.lang.ArithmeticException: Maximum overflow threshold exceeded. 
java.lang.ArithmeticException: Maximum overflow threshold exceeded. 
java.lang.ArithmeticException: Maximum overflow threshold exceeded. 
3 
2 
1 
0 
-1 
-2 
-3 
-4 
java.lang.ArithmeticException: Minimum overflow threshold exceeded. 
java.lang.ArithmeticException: Minimum overflow threshold exceeded. }}
