# Tests for the FixToken class
#
# @Author: Bart Kienhuis, Christopher Hylands, Ed Willink
#
# @Version $Id$
#
# @Copyright (c) 1999-2007 The Regents of the University of California.
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
#set VERBOSE 1

# 
#

######################################################################
test FixToken-1.0 {Create an empty instance} {
    set sat [java::call ptolemy.math.Overflow getName "saturate"]
    set stz [java::call ptolemy.math.Overflow getName "to_zero"]
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    0.0 $p0]
    set p [java::new ptolemy.data.FixToken $c0 ]
    $p toString
} {fix(0.0,16,4)}

test FixToken-1.1 {Create a non-empty instance from two strings} {
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set p [java::new ptolemy.data.FixToken $c1 ]
    $p toString
} {fix(5.573486328125,16,4)}

test FixToken-1.2 {Create a non-empty instance from two strings} {
    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } 5.5734 $p1 ]
    set p [java::new ptolemy.data.FixToken $c2 ]
    $p toString
} {fix(5.573399998247623443603515625,32,4)}

test FixToken-1.3 {Create a non-empty instance from an String} {
    set p1 [java::new ptolemy.math.Precision "(4.12)" ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } 7.7734 $p1 ]
    set p [java::new ptolemy.data.FixToken $c3 ]
    $p toString
} {fix(7.7734375,16,4)}

test FixToken-1.4 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.FixToken String} "fix(2.5, 6, 3)" ]
    $p toString
} {fix(2.5,6,3)}

######################################################################

test FixToken-2.1 {Test additive identity} {	    
    set c21 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set p [java::new ptolemy.data.FixToken $c21 ]
    set token [$p zero]
    list [$token toString]
} {fix(0.0,16,4)}

test FixToken-2.2 {Test multiplicative identity} { 
    set c22 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } 12.2 $p0 ]
    set p [java::new ptolemy.data.FixToken $c22 ]
    set token [$p one]
    list [$token toString]
} {fix(1.0,16,4)}

######################################################################

test FixToken-3.1 {Test Addition} {
    set c31 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } 3.2334454232 $p0 ]
    set c32 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } -1.5454325   $p0 ]
    set pa [java::new ptolemy.data.FixToken $c31 ]
    set pb [java::new ptolemy.data.FixToken $c32 ]
    set res [$pa add $pb]    

    list [$pa toString] [$pb toString] [$res toString]

} {fix(3.2333984375,16,4) fix(-1.54541015625,16,4) fix(1.68798828125,17,5)}

test FixToken-3.2 {Test Subtraction} {
    set res [$pa subtract $pb]   

    list [$res toString]
} {fix(4.77880859375,17,5)}

test FixToken-3.3 {Test Multiply} {
    set res [$pa multiply $pb]   

    list [$res toString]
} {fix(-4.996926784515380859375,32,8)}

test FixToken-3.4 {Test Divide} {    
   set res [$pa divide $pb]    	

   list [$res toString]
} {fix(-2.092254638671875,34,18)}

test FixToken-3.5 {Test Addition with different precision} {
    set p2 [java::new ptolemy.math.Precision "(16/8)" ]
    set c31 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } 3.2334454232 $p0 ]
    set c32 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } -1.5454325   $p2 ]
    set pa [java::new ptolemy.data.FixToken $c31 ]
    set pb [java::new ptolemy.data.FixToken $c32 ]
    set res [$pa add $pb]    

    list [$pa toString] [$pb toString] [$res toString]

} {fix(3.2333984375,16,4) fix(-1.546875,16,8) fix(1.6865234375,21,9)}

test FixToken-3.6 {Test Subtraction with different precision} {
    set res [$pa subtract $pb]   

    list [$res toString]
} {fix(4.7802734375,21,9)}

test FixToken-3.7 {Test Multiply with different precision} {
    set res [$pa multiply $pb]   

    list [$res toString]
} {fix(-5.0016632080078125,32,12)}

test FixToken-3.8 {Test Divide with different precisions} {    
   set res [$pa divide $pb]    	

   list [$res toString]
} {fix(-2.09027767181396484375,34,14)}

######################################################################

test FixToken-4.0 {Test fixValue} {
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } 5.5734 $p0 ]
    set p  [java::new ptolemy.data.FixToken $c1 ]
    set res1 [$p fixValue]
    list [$res1 toBitString]
} {101.100100101101}

test FixToken-4.1 {Test doubleValue} {
    catch {$p doubleValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(5.573486328125,16,4)' to the type double.}}

test FixToken-4.2 {Test intValue} {
    catch {$p intValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(5.573486328125,16,4)' to the type int.}}

test FixToken-4.3 {Test longValue} {
    catch {$p longValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(5.573486328125,16,4)' to the type long.}}

test FixToken-4.4 {Test toString} {
    $p toString
} {fix(5.573486328125,16,4)}

######################################################################

######################################################################
# Test isEqualTo operator applied to other Complex and Tokens types 
# below it in the lossless type hierarchy.
test FixToken-5.0 {Test equality between FixTokens} {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.375 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/2)" ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.375 $p2 ]
    set r2 [java::new ptolemy.data.FixToken $c2 ]

    set p3 [java::new ptolemy.math.Precision "(32/6)" ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.375 $p3 ]
    set r3 [java::new ptolemy.data.FixToken $c3 ]

    set res1 [$r1 {isEqualTo ptolemy.data.Token} $r1]
    set res2 [$r1 {isEqualTo ptolemy.data.Token} $r2]
    set res3 [$r1 {isEqualTo ptolemy.data.Token} $r3]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

test FixToken-5.1 {Test equality between FixToken and IntToken} {

    set i1 [java::new {ptolemy.data.IntToken int} 8]
    set i2 [java::new {ptolemy.data.IntToken int} 4]

        catch { [$r1 {isEqualTo ptolemy.data.Token} $i1] } msg
	list $msg

} {{ptolemy.kernel.util.IllegalActionException: isEqualTo method not supported between ptolemy.data.FixToken 'fix(5.375,32,4)' and ptolemy.data.IntToken '8' because the types are incomparable.}}


# Helper proc: given a FixPoint, print out a string like
# FixToken.toString()
proc FixedPointToString { fixpoint } {
    set precision [$fixpoint getPrecision]
    return "fix([$fixpoint doubleValue],[$precision getNumberOfBits],[$precision getIntegerBitLength])"
}
######################################################################
# Test round with a given Precision



test FixToken-6.0 {Test round with a Precision, 5.375, Saturate  }  {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.375 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]
    set p3 [java::new ptolemy.math.Precision "(32/3)" ]
    set p4 [java::new ptolemy.math.Precision "(32/2)" ]
    set p5 [java::new ptolemy.math.Precision "(32/1)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $sat ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $sat ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $sat ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $sat ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4]

} {fix(5.375,32,4) fix(3.99999999814,32,3) fix(1.99999999907,32,2) fix(0.999999999534,32,1)}

test FixToken-6.1 {Test round with a Precision, 5.375, Saturate}  {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.375 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $sat ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $sat ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $sat ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $sat ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4]

} {fix(-5.375,32,4) fix(-4.0,32,3) fix(-2.0,32,2) fix(-1.0,32,1)}

test FixToken-6.2 {Test round with a Precision, -5.375, Saturate}  {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.375 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]
    set p3 [java::new ptolemy.math.Precision "(32/3)" ]
    set p4 [java::new ptolemy.math.Precision "(32/2)" ]
    set p5 [java::new ptolemy.math.Precision "(32/1)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $stz ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $stz ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $stz ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $stz ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4]

} {fix(-5.375,32,4) fix(0.0,32,3) fix(0.0,32,2) fix(0.0,32,1)}

test FixToken-6.3 {Test round with a Precision, -5.375, ZeroSaturate}  {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.375 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]
    set p3 [java::new ptolemy.math.Precision "(32/3)" ]
    set p4 [java::new ptolemy.math.Precision "(32/2)" ]
    set p5 [java::new ptolemy.math.Precision "(32/1)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $stz ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $stz ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $stz ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $stz ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4]

} {fix(-5.375,32,4) fix(0.0,32,3) fix(0.0,32,2) fix(0.0,32,1)}



test FixToken-6.4 {Test round with a Precision, 5.97534, Saturate}  {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.97534 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]
    set p3 [java::new ptolemy.math.Precision "(12/4)" ]
    set p4 [java::new ptolemy.math.Precision "(10/4)" ]
    set p5 [java::new ptolemy.math.Precision "(8/6)" ]
    set p6 [java::new ptolemy.math.Precision "(6/4)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $sat ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $sat ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $sat ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $sat ]
    set res5 [java::call ptolemy.math.Quantizer round  $v1 $p6 $sat ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4] \
	    [FixedPointToString $res5]
} {fix(5.97534000129,32,4) fix(5.9765625,12,4) fix(5.96875,10,4) fix(6.0,8,6) fix(6.0,6,4)}

test FixToken-6.5 {Test round with a Precision, -5.97534, Saturate}  {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.97534 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]
    set p3 [java::new ptolemy.math.Precision "(12/4)" ]
    set p4 [java::new ptolemy.math.Precision "(10/4)" ]
    set p5 [java::new ptolemy.math.Precision "(8/6)" ]
    set p6 [java::new ptolemy.math.Precision "(6/4)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $sat ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $sat ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $sat ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $sat ]
    set res5 [java::call ptolemy.math.Quantizer round  $v1 $p6 $sat ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4] \
	    [FixedPointToString $res5]
} {fix(-5.97534000129,32,4) fix(-5.9765625,12,4) fix(-5.96875,10,4) fix(-6.0,8,6) fix(-6.0,6,4)}

test FixToken-6.6 {Test round with a Precision, 5.97534, ZeroSaturate} {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.97534 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]

    set p3 [java::new ptolemy.math.Precision "(12/4)" ]
    set p4 [java::new ptolemy.math.Precision "(10/4)" ]
    set p5 [java::new ptolemy.math.Precision "(8/6)" ]
    set p6 [java::new ptolemy.math.Precision "(6/4)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $stz ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $stz ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $stz ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $stz ]
    set res5 [java::call ptolemy.math.Quantizer round $v1 $p6 $stz ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4] \
	    [FixedPointToString $res5]

} {fix(5.97534000129,32,4) fix(5.9765625,12,4) fix(5.96875,10,4) fix(6.0,8,6) fix(6.0,6,4)}

test FixToken-6.7 {Test round with a Precision, -5.97534, ZeroSaturate} {

    set p1 [java::new ptolemy.math.Precision "(32/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.97534 $p1 ]
    set r1 [java::new ptolemy.data.FixToken $c1 ]

    set p2 [java::new ptolemy.math.Precision "(32/4)" ]
    set p3 [java::new ptolemy.math.Precision "(12/4)" ]
    set p4 [java::new ptolemy.math.Precision "(10/4)" ]
    set p5 [java::new ptolemy.math.Precision "(8/6)" ]
    set p6 [java::new ptolemy.math.Precision "(6/4)" ]

    set v1 [$r1 fixValue]
    set res1 [java::call ptolemy.math.Quantizer round $v1 $p2 $stz ]
    set res2 [java::call ptolemy.math.Quantizer round $v1 $p3 $stz ]
    set res3 [java::call ptolemy.math.Quantizer round $v1 $p4 $stz ]
    set res4 [java::call ptolemy.math.Quantizer round $v1 $p5 $stz ]
    set res5 [java::call ptolemy.math.Quantizer round $v1 $p6 $stz ]

    list [FixedPointToString $res1] \
	    [FixedPointToString $res2] \
	    [FixedPointToString $res3] \
	    [FixedPointToString $res4] \
	    [FixedPointToString $res5]

} {fix(-5.97534000129,32,4) fix(-5.9765625,12,4) fix(-5.96875,10,4) fix(-6.0,8,6) fix(-6.0,6,4)}

######################################################################
####
# test isLessThan
test FixToken-7.0 {test isLessThan} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    2.0 $p0]
    set f2 [java::new ptolemy.data.FixToken $c0 ]

    set p1 [java::new ptolemy.math.Precision "(16/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    2.0 $p1]
    set ff2 [java::new ptolemy.data.FixToken $c1 ]

    set p3 [java::new ptolemy.math.Precision "(16/4)" ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    3.0 $p3]
    set f3 [java::new ptolemy.data.FixToken $c3 ]

    list [[$f2 isLessThan $ff2] booleanValue] \
         [[$f2 isLessThan $f3] booleanValue] \
	 [[$f3 isLessThan $f2] booleanValue]
} {0 1 0}

######################################################################
####
# 
test FixToken-8.0 {Test equals} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    1.0 $p0]
    set p1 [java::new ptolemy.data.FixToken $c1 ]

    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    1.0 $p0]
    set p2 [java::new ptolemy.data.FixToken $c2 ]

    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    4.0 $p0]
    set p3 [java::new ptolemy.data.FixToken $c3 ]

    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test FixToken-5.0 {Test hashCode} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    1.0 $p0]
    set p1 [java::new ptolemy.data.FixToken $c1 ]

    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    1.0 $p0]
    set p2 [java::new ptolemy.data.FixToken $c2 ]

    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    4.0 $p0]
    set p3 [java::new ptolemy.data.FixToken $c3 ]

    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {4096 4096 16384}

####################################################################

test FixToken-6.0 {quantize-saturate} {
    set p0 [java::new ptolemy.math.Precision "20.10" ]
    set q1 [java::new ptolemy.math.FixPointQuantization "3.1,saturate,nearest" ]
    list "
 7.000 [[[java::new ptolemy.data.FixToken 7.000 $p0] quantize $q1] toString]
 4.000 [[[java::new ptolemy.data.FixToken 4.000 $p0] quantize $q1] toString]
 3.250 [[[java::new ptolemy.data.FixToken 3.250 $p0] quantize $q1] toString]
 3.249 [[[java::new ptolemy.data.FixToken 3.249 $p0] quantize $q1] toString]
-3.250 [[[java::new ptolemy.data.FixToken -3.250 $p0] quantize $q1] toString]
-3.251 [[[java::new ptolemy.data.FixToken -3.251 $p0] quantize $q1] toString]
-4.000 [[[java::new ptolemy.data.FixToken -4.000 $p0] quantize $q1] toString]
-7.000 [[[java::new ptolemy.data.FixToken -7.000 $p0] quantize $q1] toString] "
} {{
 7.000 fix(3.5,4,3)
 4.000 fix(3.5,4,3)
 3.250 fix(3.5,4,3)
 3.249 fix(3.0,4,3)
-3.250 fix(-3.0,4,3)
-3.251 fix(-3.5,4,3)
-4.000 fix(-4.0,4,3)
-7.000 fix(-4.0,4,3) }}

test FixToken-6.1 {quantize-modulo} {
    set q1 [java::new ptolemy.math.FixPointQuantization "3.1,modulo,nearest" ]
    list "
 7.000 [[[java::new ptolemy.data.FixToken 7.000 $p0] quantize $q1] toString]
 4.000 [[[java::new ptolemy.data.FixToken 4.000 $p0] quantize $q1] toString]
 3.250 [[[java::new ptolemy.data.FixToken 3.250 $p0] quantize $q1] toString]
 3.249 [[[java::new ptolemy.data.FixToken 3.249 $p0] quantize $q1] toString]
-3.250 [[[java::new ptolemy.data.FixToken -3.250 $p0] quantize $q1] toString]
-3.251 [[[java::new ptolemy.data.FixToken -3.251 $p0] quantize $q1] toString]
-4.000 [[[java::new ptolemy.data.FixToken -4.000 $p0] quantize $q1] toString]
-7.000 [[[java::new ptolemy.data.FixToken -7.000 $p0] quantize $q1] toString] "
} {{
 7.000 fix(-1.0,4,3)
 4.000 fix(-4.0,4,3)
 3.250 fix(3.5,4,3)
 3.249 fix(3.0,4,3)
-3.250 fix(-3.0,4,3)
-3.251 fix(-3.5,4,3)
-4.000 fix(-4.0,4,3)
-7.000 fix(1.0,4,3) }}

######################################################################
####
# 
test FixToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type fix because the type of the token is higher or incomparable with the given type.}}

test FixToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.UnsignedByteToken '1ub' to the type fix because the type of the token is higher or incomparable with the given type.}}

test FixToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type fix because the type of the token is higher or incomparable with the given type.}}

test FixToken-13.3 {Test convert from DoubleToken} {
    set t [java::new {ptolemy.data.DoubleToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.DoubleToken '1.0' to the type fix because the type of the token is higher or incomparable with the given type.}}

test FixToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {fix(1.0,8,4)}

test FixToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '1' to the type fix because the type of the token is higher or incomparable with the given type.}}

test FixToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type fix because the type of the token is higher or incomparable with the given type.}}

test FixToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FixToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type fix because the type of the token is higher or incomparable with the given type.}}
    
test FixToken-14.0 {Test getType} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[$t getType] toString]} msg
    list $msg
} {fixedpoint(8,4)}


test FixToken-15.0 {Create an array} {
    set t1 [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set t2 [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $t1 $t2]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]
    $valToken toString
} {{fix(1.0,8,4), fix(1.0,8,4)}}

test FixToken-15.1 {Create an unsigned FixToken} {
    set p0 [java::new ptolemy.math.Precision "U1.0" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision } \
	    0.0 $p0]
    set p [java::new ptolemy.data.FixToken $c0 ]
   $p toString
} {fix(0,1,1)}

test FixToken-15.2 {Create an array of unsigned FixTokens using the single arg ctor w/o specifying the type} {
    # Used 15.1 above
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $p $p]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]
    $valToken toString
} {{fix(0,1,1), fix(0,1,1)}}

test FixToken-15.3 {Create an array of unsigned FixTokens using the two arg ctor } {
    # Used 15.1 above
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $p $p]]
    set valToken [java::new \
		      {ptolemy.data.ArrayToken ptolemy.data.type.Type ptolemy.data.Token[]} \
		      [$p getType] $valArray]
    $valToken toString
} {{fix(0,1,1), fix(0,1,1)}}
    

