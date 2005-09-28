# Tests for the FixPointQuantization Class
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

test FixPointQuantization-1.0 {constructor-string} {
    set q1 [java::new ptolemy.math.FixPointQuantization "1.15" ]
    set q2 [java::new ptolemy.math.FixPointQuantization "3/2" ]
    set q3 [java::new ptolemy.math.FixPointQuantization "4.-3,modulo" ]
    set q4 [java::new ptolemy.math.FixPointQuantization "3/4,grow" ]
    set q5 [java::new ptolemy.math.FixPointQuantization "-5.8,to_zero,up" ]
    set q6 [java::new ptolemy.math.FixPointQuantization "[ $q3 toString ]" ]
    set q7 [java::new ptolemy.math.FixPointQuantization "[string trim [ $q4 toString ] "()"]" ]
    list "
[ $q1 toString ]
[ $q2 toString ]
[ $q3 toString ]
[ $q4 toString ]
[ $q5 toString ]
[ $q6 toString ]
[ $q7 toString ] "
} {{
(1.15,saturate,half_ceiling)
(2.1,saturate,half_ceiling)
(4.-3,modulo,half_ceiling)
(4.-1,grow,half_ceiling)
(-5.8,to_zero,up)
(4.-3,modulo,half_ceiling)
(4.-1,grow,half_ceiling) }}

test FixPointQuantization-1.1 {constructor-bad} {
    #catch { set q [java::new ptolemy.math.FixPointQuantization "" ] } msg1
    #catch { set q [java::new ptolemy.math.FixPointQuantization "1" ] } msg2
    #catch { set q [java::new ptolemy.math.FixPointQuantization "1+2" ] } msg3
    #catch { set q [java::new ptolemy.math.FixPointQuantization "1.-2" ] } msg4
    catch { set q [java::new ptolemy.math.FixPointQuantization "1.1,zzz" ] } msg5
    catch { set q [java::new ptolemy.math.FixPointQuantization "1.1,clip,zzz" ] } msg6
    catch { set q [java::new ptolemy.math.FixPointQuantization "1.1,clip,up,zzz" ] } msg7
    list "
$msg5
$msg6
$msg7 "
} {{
java.lang.IllegalArgumentException: Unknown overflow strategy "zzz".
java.lang.IllegalArgumentException: Unknown rounding strategy "zzz".
java.lang.IllegalArgumentException: FixPointQuantization requires at most a precision overflow and rounding, }}

test FixPointQuantization-1.2 {constructor-fields} {
    set prec_1_15 [java::new ptolemy.math.Precision "1.15" ]
    set prec_9_3 [java::new ptolemy.math.Precision "9.3" ]
    set overflow_modulo [java::call ptolemy.math.Overflow forName "modulo"];
    set overflow_saturate [java::call ptolemy.math.Overflow forName "saturate"];
    set round_down [java::call ptolemy.math.Rounding forName "down"];
    set round_half_up [java::call ptolemy.math.Rounding forName "half_up"];
    set q1 [java::new ptolemy.math.FixPointQuantization $prec_1_15 $overflow_modulo $round_down ]
    set q2 [java::new ptolemy.math.FixPointQuantization $prec_9_3 $overflow_saturate $round_half_up ]
    list "
[ $q1 toString ]
[ $q2 toString ] "
} {{
(1.15,modulo,down)
(9.3,saturate,half_up) }}

####################################################################

test FixPointQuantization-2.0 {equals} {
    set q0 [java::new ptolemy.math.FixPointQuantization "1.15,clip,nearest" ]
    set q1 [java::new ptolemy.math.FixPointQuantization "1.15,saturate,half_ceiling" ]
    set q2 [$q0 setPrecision [java::new ptolemy.math.Precision "2.5" ]]
    set q3 [$q0 setOverflow [java::call ptolemy.math.Overflow forName "modulo"]]
    set q4 [$q0 setRounding [java::call ptolemy.math.Rounding forName "down"]]
    list "
[ $q0 toString ] [$q0 equals $q0 ]
[ $q1 toString ] [$q0 equals $q1 ]
[ $q2 toString ] [$q0 equals $q2 ]
[ $q3 toString ] [$q0 equals $q3 ]
[ $q4 toString ] [$q0 equals $q4 ] "
} {{
(1.15,saturate,half_ceiling) 1
(1.15,saturate,half_ceiling) 1
(2.5,saturate,half_ceiling) 0
(1.15,modulo,half_ceiling) 0
(1.15,saturate,down) 0 }}

####################################################################

test FixPointQuantization-3.0 {quantize-saturate} {
    set q0 [java::new ptolemy.math.FixPointQuantization "2.1,saturate,half_up" ]
    list "
 7 0.000 [[$q0 quantize [java::new java.math.BigInteger "7" ] 0.000] toString]
 3 0.000 [[$q0 quantize [java::new java.math.BigInteger "3" ] 0.000] toString]
 2 0.500 [[$q0 quantize [java::new java.math.BigInteger "2" ] 0.500] toString]
 2 0.499 [[$q0 quantize [java::new java.math.BigInteger "2" ] 0.499] toString]
-3 0.999 [[$q0 quantize [java::new java.math.BigInteger "-3" ] 0.999] toString]
-3 0.001 [[$q0 quantize [java::new java.math.BigInteger "-3" ] 0.001] toString]
-4 0.000 [[$q0 quantize [java::new java.math.BigInteger "-4" ] 0.000] toString]
-7 0.000 [[$q0 quantize [java::new java.math.BigInteger "-7" ] 0.000] toString] "
} {{
 7 0.000 3
 3 0.000 3
 2 0.500 3
 2 0.499 2
-3 0.999 -2
-3 0.001 -3
-4 0.000 -4
-7 0.000 -4 }}

test FixPointQuantization-3.1 {quantize-modulo} {
    set q0 [java::new ptolemy.math.FixPointQuantization "2.1,modulo,nearest" ]
    list "
 7 0.000 [[$q0 quantize [java::new java.math.BigInteger "7" ] 0.000] toString]
 4 0.000 [[$q0 quantize [java::new java.math.BigInteger "4" ] 0.000] toString]
 3 0.499 [[$q0 quantize [java::new java.math.BigInteger "3" ] 0.499] toString]
 3 0.001 [[$q0 quantize [java::new java.math.BigInteger "3" ] 0.001] toString]
-3 0.500 [[$q0 quantize [java::new java.math.BigInteger "-3" ] 0.500] toString]
-3 0.499 [[$q0 quantize [java::new java.math.BigInteger "-3" ] 0.499] toString]
-4 0.000 [[$q0 quantize [java::new java.math.BigInteger "-4" ] 0.000] toString]
-7 0.000 [[$q0 quantize [java::new java.math.BigInteger "-7" ] 0.000] toString] "
} {{
 7 0.000 -1
 4 0.000 -4
 3 0.499 3
 3 0.001 3
-3 0.500 -2
-3 0.499 -3
-4 0.000 -4
-7 0.000 1 }}

test FixPointQuantization-4.1 {getMaximumUnscaledValue} {

    # With a fix format of 0 integer and 8 fractional bits, the
    # maximum value
    # is 0.01111111 (.49609375) rather than 0.11111111 (.99609375).

    # 8 bits, 0 integer bits
    #set precision [java::new ptolemy.math.Precision 8 0]
    #set	q8 [java::new ptolemy.math.FixPointQuantization \
    #    $precision \
    #	[java::field ptolemy.math.Overflow SATURATE] \
    #	[java::field ptolemy.math.Rounding TRUNCATE]]

    set q8 [java::new ptolemy.math.FixPointQuantization "0.8" ]
    set fixedPoint [java::new ptolemy.math.FixPoint [expr {7.0/8}] $q8]

    list [$fixedPoint toString] [$fixedPoint toBitString]
} {0.49609375 0.01111111}
