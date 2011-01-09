# Tests for the Time class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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
test Time-1.1 {Constructors} {
    set d1 [java::new ptolemy.actor.Director]
    $d1 setName D1
    set t1 [java::new ptolemy.actor.util.Time $d1]
    set t2 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 1.0]
    set t3 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director long} $d1 1]
    list [$t1 toString] [$t2 toString] [$t3 toString] [$d1 getTimeResolution]
} {0.0 1.0 1.0E-10 1e-10}

######################################################################
####
#
test Time-1.2 {Constructors: coverage} {
    set d2 [java::new ptolemy.actor.Director]
    $d2 setName D2
    set t4 [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 -1.0]
    catch {[java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 [java::field java.lang.Double NaN]]} errMsg
    set tPositiveInfinity [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 [java::field java.lang.Double POSITIVE_INFINITY]]
    set tNegativeInfinity [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d2 [java::field java.lang.Double NEGATIVE_INFINITY]]
    list [$t4 toString] $errMsg [$tPositiveInfinity toString] [$tNegativeInfinity toString]
} {-1.0 {java.lang.ArithmeticException: Time value can not be NaN.} Infinity -Infinity}

######################################################################
####
#
test Time-2.1 {compareTo} {
    #Uses 1.1 and 1.2 above
    list \
         [list [$t1 compareTo $t1] \
	       [$t1 compareTo $t2] \
	       [$t1 compareTo $t3] \
               [$t1 compareTo $t4] \
               [$t1 compareTo $tPositiveInfinity] \
               [$t1 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$t2 compareTo $t1] \
	       [$t2 compareTo $t2] \
	       [$t2 compareTo $t3] \
               [$t2 compareTo $t4] \
               [$t2 compareTo $tPositiveInfinity] \
              [$t2 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$t3 compareTo $t1] \
	       [$t3 compareTo $t2] \
	       [$t3 compareTo $t3] \
               [$t3 compareTo $t4] \
               [$t3 compareTo $tPositiveInfinity] \
              [$t3 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$t4 compareTo $t1] \
	       [$t4 compareTo $t2] \
	       [$t4 compareTo $t3] \
               [$t4 compareTo $t4] \
               [$t4 compareTo $tPositiveInfinity] \
              [$t4 compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$tPositiveInfinity compareTo $t1] \
	       [$tPositiveInfinity compareTo $t2] \
	       [$tPositiveInfinity compareTo $t3] \
               [$tPositiveInfinity compareTo $t4] \
               [$tPositiveInfinity compareTo $tPositiveInfinity] \
               [$tPositiveInfinity compareTo $tNegativeInfinity]] \
         "\n" \
         [list [$tNegativeInfinity compareTo $t1] \
	       [$tNegativeInfinity compareTo $t2] \
	       [$tNegativeInfinity compareTo $t3] \
               [$tNegativeInfinity compareTo $t4] \
               [$tNegativeInfinity compareTo $tPositiveInfinity] \
              [$tNegativeInfinity compareTo $tNegativeInfinity]]
} {{0 -1 -1 1 -1 1} {
} {1 0 1 1 -1 1} {
} {1 -1 0 1 -1 1} {
} {-1 -1 -1 0 -1 1} {
} {1 1 1 1 0 1} {
} {-1 -1 -1 -1 -1 0}}

######################################################################
####
#
test Time-2.2 {compareTo null} {
     # The Javadoc for java.lang.Comparable says: "Note that null
     # is not an instance of any class, and e.compareTo(null)
     # should throw a NullPointerException even though
     # e.equals(null) returns false."
     catch {$t1 compareTo [java::null]} errMsg
     list $errMsg
} {java.lang.NullPointerException}

######################################################################
####
#
test Time-3.1 {equals null} {
     # The Javadoc for java.lang.Comparable says: "Note that null
     # is not an instance of any class, and e.compareTo(null)
     # should throw a NullPointerException even though
     # e.equals(null) returns false."
     list [$t1 equals [java::null]]
} {0}

####
#
test Time-3.2 {equals a non-time} {
     # The Javadoc for java.lang.Comparable says: "Note that null
     # is not an instance of any class, and e.compareTo(null)
     # should throw a NullPointerException even though
     # e.equals(null) returns false."
     list [$t1 equals [java::new java.util.Date]]
} {0}

######################################################################
####
#
test Time-4.1.1 {multiply Infinite DoubleTokens for comparison with Time} {
    set DoubleOne [java::new ptolemy.data.DoubleToken 1.0]
    set DoubleZero [java::new ptolemy.data.DoubleToken 0.0]
    set DoubleInfinity [java::cast ptolemy.data.DoubleToken [$DoubleOne divide $DoubleZero]]

    set DoubleOneMultiplyDoubleInfinity [$DoubleOne multiply $DoubleInfinity]
    set DoubleInfinityMultiplyDoubleOne [$DoubleInfinity multiply $DoubleOne]
    set DoubleInfinityMultiplyDoubleInfinity [$DoubleInfinity multiply $DoubleInfinity]

    set DoubleZeroMultiplyDoubleInfinity [$DoubleZero multiply $DoubleInfinity]
    set DoubleInfinityMultiplyDoubleZero [$DoubleInfinity multiply $DoubleZero]
    set DoubleInfinityMultiplyDoubleInfinity [$DoubleInfinity multiply $DoubleInfinity]


    list \
	[$DoubleOneMultiplyDoubleInfinity toString] \
	[$DoubleInfinityMultiplyDoubleInfinity toString ]\
	[$DoubleInfinityMultiplyDoubleOne toString] \
	[$DoubleZeroMultiplyDoubleInfinity toString] \
	[$DoubleInfinityMultiplyDoubleInfinity toString] \
	[$DoubleInfinityMultiplyDoubleZero toString]
} {Infinity Infinity Infinity NaN Infinity NaN}

######################################################################
####
#
test Time-4.1.2 {multiply compared with DoubleTokens} {
    set TimeOne [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 1.0]
    set TimeZero [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 0.0]

    set TimeOneMultiplyTimePositiveInfinity [$TimeOne multiply $tPositiveInfinity]
    set TimePositiveInfinityMultiplyTimeOne [$tPositiveInfinity multiply $TimeOne]
    set TimeOneMultiplyTimeNegativeInfinity [$TimeOne multiply $tNegativeInfinity]
    set TimeNegativeInfinityMultiplyTimeOne [$tNegativeInfinity multiply $TimeOne]

    list \
	[$TimeOneMultiplyTimePositiveInfinity toString] \
	[$TimeOneMultiplyTimeNegativeInfinity toString] \
	[$TimePositiveInfinityMultiplyTimeOne toString] \
	[$TimeNegativeInfinityMultiplyTimeOne toString]
} {Infinity -Infinity Infinity -Infinity}

# FIXME: do the same for Double.NEGATIVE_INFINITY


######################################################################
####
#
# uses 4.1.2 from above

test Time-4.1.3 {multiply} {
	catch {[$TimeZero multiply $tPositiveInfinity] } errMsg1
	catch {[$TimeZero multiply $tNegativeInfinity] } errMsg2
	catch {[$tPositiveInfinity multiply $TimeZero] } errMsg3
	catch {[$tNegativeInfinity multiply $TimeZero] } errMsg4
	list $errMsg1 "\n" $errMsg2 "\n" $errMsg3 "\n" $errMsg4
} {{java.lang.ArithmeticException: Time: multiply positive or negative infinity to 0.0 results in an invalid time.} {
} {java.lang.ArithmeticException: Time: multiply positive or negative infinity to 0.0 results in an invalid time.} {
} {java.lang.ArithmeticException: Time: multiply positive or negative infinity to 0.0 results in an invalid time.} {
} {java.lang.ArithmeticException: Time: multiply positive or negative infinity to 0.0 results in an invalid time.}}

######################################################################
####
#
test Time-4.1.4 {multiply2} {
    set TimeNegativeInfinityMultiplyTimePositiveInfinity [$tNegativeInfinity multiply $tPositiveInfinity]
    set TimePositiveInfinityMultiplyTimeNegativeInfinity [$tPositiveInfinity multiply $tNegativeInfinity]
    set TimeNegativeInfinityMultiplyTimeNegativeInfinity [$tNegativeInfinity multiply $tNegativeInfinity]
    set TimePositiveInfinityMultiplyTimePositiveInfinity [$tPositiveInfinity multiply $tPositiveInfinity]
    list \
    [$TimeNegativeInfinityMultiplyTimePositiveInfinity toString] \
    [$TimePositiveInfinityMultiplyTimeNegativeInfinity toString] \
    [$TimeNegativeInfinityMultiplyTimeNegativeInfinity toString] \
    [$TimePositiveInfinityMultiplyTimePositiveInfinity toString]
} {-Infinity -Infinity Infinity Infinity}

######################################################################
####
#
test Time-5.1 {divide} {
	#set tNaNDouble [java::new java.lang.Double NaN]
	catch {[$tPositiveInfinity divide $tPositiveInfinity] } errMsg1
	catch {[$tPositiveInfinity divide $tNegativeInfinity] } errMsg2
	catch {[$tNegativeInfinity divide $tPositiveInfinity] } errMsg3
	catch {[$tNegativeInfinity divide $tNegativeInfinity] } errMsg4
	catch {[$TimeZero divide $TimeZero] } errMsg5
	catch {[$TimeOne divide $TimeZero] } errMsg6
	list $errMsg1 "\n" $errMsg2 "\n" $errMsg3 "\n" $errMsg4 "\n" $errMsg5 "\n" \
	$errMsg6
} {{java.lang.ArithmeticException: Time: Divide a positive/negative infinity by another positive/negative infinity results in an invalid time.} {
} {java.lang.ArithmeticException: Time: Divide a positive/negative infinity by another positive/negative infinity results in an invalid time.} {
} {java.lang.ArithmeticException: Time: Divide a positive/negative infinity by another positive/negative infinity results in an invalid time.} {
} {java.lang.ArithmeticException: Time: Divide a positive/negative infinity by another positive/negative infinity results in an invalid time.} {
} {java.lang.ArithmeticException: Time: Divide a zero by results in an invalid time.} {
} {java.lang.ArithmeticException: Time: Divide a zero by results in an invalid time.}}

######################################################################
####
#
test Time-5.1 {divide} {
    set TimeTwo [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 2.0]
    set TimeThree [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 3.0]
    set TimeFour [java::new {ptolemy.actor.util.Time ptolemy.actor.Director double} $d1 4.0]
    
    set fraction0 [$TimeOne divide $TimeOne]
    set fraction1 [$TimeOne divide $TimeTwo]
    set fraction2 [$TimeOne divide $TimeThree]
    set fraction3 [$TimeOne divide $TimeFour]
    set fraction4 [$TimeTwo divide $TimeOne]
    set fraction5 [$TimeTwo divide $TimeTwo]
    set fraction6 [$TimeTwo divide $TimeThree]
    set fraction7 [$TimeTwo divide $TimeFour]
    set fraction8 [$TimeThree divide $TimeOne]
    set fraction9 [$TimeThree divide $TimeTwo]
    set fraction10 [$TimeThree divide $TimeThree]
    set fraction11 [$TimeThree divide $TimeFour]
    set fraction12 [$TimeFour divide $TimeOne]
    set fraction13 [$TimeFour divide $TimeTwo]
    set fraction14 [$TimeFour divide $TimeThree]
    set fraction15 [$TimeFour divide $TimeFour]
	list \
	[$fraction0 getDoubleValue] \
	[$fraction1 getDoubleValue] \
	[$fraction2 getDoubleValue] \
	[$fraction3 getDoubleValue] \
	[$fraction4 getDoubleValue] \
	[$fraction5 getDoubleValue] \
	[$fraction6 getDoubleValue] \
	[$fraction7 getDoubleValue] \
	[$fraction8 getDoubleValue] \
	[$fraction9 getDoubleValue] \
	[$fraction10 getDoubleValue] \
	[$fraction11 getDoubleValue] \
	[$fraction12 getDoubleValue] \
	[$fraction13 getDoubleValue] \
	[$fraction14 getDoubleValue] \
	[$fraction15 getDoubleValue] \
} {1.0 0.5 0.3333333333 0.25\
   2.0 1.0 0.6666666666 0.5\
   3.0 1.5 1.0 0.75\
   4.0 2.0 1.3333333333 1.0}

######################################################################
####
#
# use fraction2: 1/3; fraction3: 1/4; fraction6: 2/3; fraction9: 3/2 fraction14: 4/3
test Time-5.1 {multiply fractions} {
    set MultDiv0 [$fraction2 multiply $TimeOne]
    set MultDiv1 [$fraction2 multiply $TimeTwo]
    set MultDiv2 [$fraction2 multiply $TimeThree]
    set MultDiv3 [$fraction2 multiply $TimeFour]
    set MultDiv4 [$fraction3 multiply $TimeOne]
    set MultDiv5 [$fraction3 multiply $TimeTwo]
    set MultDiv6 [$fraction3 multiply $TimeThree]
    set MultDiv7 [$fraction3 multiply $TimeFour]
    set MultDiv8 [$fraction6 multiply $TimeOne]
    set MultDiv9 [$fraction6 multiply $TimeTwo]
    set MultDiv10 [$fraction6 multiply $TimeThree]
    set MultDiv11 [$fraction6 multiply $TimeFour]
    set MultDiv12 [$fraction9 multiply $TimeOne]
    set MultDiv13 [$fraction9 multiply $TimeTwo]
    set MultDiv14 [$fraction9 multiply $TimeThree]
    set MultDiv15 [$fraction9 multiply $TimeFour]
    set MultDiv16 [$fraction14 multiply $TimeOne]
    set MultDiv17 [$fraction14 multiply $TimeTwo]
    set MultDiv18 [$fraction14 multiply $TimeThree]
    set MultDiv19 [$fraction14 multiply $TimeFour]
        set MultDiv20 [$fraction2 multiply $fraction2]
    set MultDiv21 [$fraction2 multiply $fraction3]
    set MultDiv22 [$fraction2 multiply $fraction6]
    set MultDiv23 [$fraction2 multiply $fraction9]
    set MultDiv24 [$fraction2 multiply $fraction14]
    set MultDiv25 [$fraction3 multiply $fraction2]
    set MultDiv26 [$fraction3 multiply $fraction3]
    set MultDiv27 [$fraction3 multiply $fraction6]
    set MultDiv28 [$fraction3 multiply $fraction9]
    set MultDiv29 [$fraction3 multiply $fraction14]
    set MultDiv30 [$fraction6 multiply $fraction2]
    set MultDiv31 [$fraction6 multiply $fraction3]
    set MultDiv32 [$fraction6 multiply $fraction6]
    set MultDiv33 [$fraction6 multiply $fraction9]
    set MultDiv34 [$fraction6 multiply $fraction14]
    set MultDiv35 [$fraction9 multiply $fraction2]
    set MultDiv36 [$fraction9 multiply $fraction3]
    set MultDiv37 [$fraction9 multiply $fraction6]
    set MultDiv38 [$fraction9 multiply $fraction9]
    set MultDiv39 [$fraction9 multiply $fraction14]
    set MultDiv40 [$fraction14 multiply $fraction2]
    set MultDiv41 [$fraction14 multiply $fraction3]
    set MultDiv42 [$fraction14 multiply $fraction6]
    set MultDiv43 [$fraction14 multiply $fraction9]
    set MultDiv44 [$fraction14 multiply $fraction14]
	list \
	[$MultDiv0 getDoubleValue] \
	[$MultDiv1 getDoubleValue] \
	[$MultDiv2 getDoubleValue] \
	[$MultDiv3 getDoubleValue] \
	[$MultDiv4 getDoubleValue] \
	[$MultDiv5 getDoubleValue] \
	[$MultDiv6 getDoubleValue] \
	[$MultDiv7 getDoubleValue] \
	[$MultDiv8 getDoubleValue] \
	[$MultDiv9 getDoubleValue] \
	[$MultDiv10 getDoubleValue] \
	[$MultDiv11 getDoubleValue] \
	[$MultDiv12 getDoubleValue] \
	[$MultDiv13 getDoubleValue] \
	[$MultDiv14 getDoubleValue] \
	[$MultDiv15 getDoubleValue] \
	[$MultDiv16 getDoubleValue] \
	[$MultDiv17 getDoubleValue] \
	[$MultDiv18 getDoubleValue] \
	[$MultDiv19 getDoubleValue] \
	[$MultDiv20 getDoubleValue] \
	[$MultDiv21 getDoubleValue] \
	[$MultDiv22 getDoubleValue] \
	[$MultDiv23 getDoubleValue] \
	[$MultDiv24 getDoubleValue] \
	[$MultDiv25 getDoubleValue] \
	[$MultDiv26 getDoubleValue] \
	[$MultDiv27 getDoubleValue] \
	[$MultDiv28 getDoubleValue] \
	[$MultDiv29 getDoubleValue] \
	[$MultDiv30 getDoubleValue] \
	[$MultDiv31 getDoubleValue] \
	[$MultDiv32 getDoubleValue] \
	[$MultDiv33 getDoubleValue] \
	[$MultDiv34 getDoubleValue] \
	[$MultDiv35 getDoubleValue] \
	[$MultDiv36 getDoubleValue] \
	[$MultDiv37 getDoubleValue] \
	[$MultDiv38 getDoubleValue] \
	[$MultDiv39 getDoubleValue] \
	[$MultDiv40 getDoubleValue] \
	[$MultDiv41 getDoubleValue] \
	[$MultDiv42 getDoubleValue] \
	[$MultDiv43 getDoubleValue] \
	[$MultDiv44 getDoubleValue] \
} {0.3333333333 0.6666666666 1.0 1.3333333333\
   0.25 0.5 0.75 1.0\
   0.6666666666 1.3333333333 2.0 2.6666666666\
   1.5 3.0 4.5 6.0\
   1.3333333333 2.6666666666 4.0 5.3333333333\
   0.1111111111 0.0833333333 0.2222222222 0.5 0.4444444444\
   0.0833333333 0.0625 0.1666666666 0.375 0.3333333333\
   0.2222222222 0.1666666666 0.4444444444 1.0 0.8888888888\
   0.5 0.375 1.0 2.25 2.0\
   0.4444444444 0.3333333333 0.8888888888 2.0 1.7777777777}

# use fraction2: 1/3; fraction3: 1/4; fraction6: 2/3; fraction9: 3/2 fraction14: 4/3
######################################################################
####
#
# use fraction2: 1/3; fraction3: 1/4; fraction6: 2/3; fraction9: 3/2 fraction14: 4/3
test Time-5.1 {add fractions} {
    set AddDiv0 [$fraction2 add $TimeOne]
    set AddDiv1 [$fraction2 add $TimeTwo]
    set AddDiv2 [$fraction2 add $TimeThree]
    set AddDiv3 [$fraction2 add $TimeFour]
    set AddDiv4 [$fraction3 add $TimeOne]
    set AddDiv5 [$fraction3 add $TimeTwo]
    set AddDiv6 [$fraction3 add $TimeThree]
    set AddDiv7 [$fraction3 add $TimeFour]
    set AddDiv8 [$fraction6 add $TimeOne]
    set AddDiv9 [$fraction6 add $TimeTwo]
    set AddDiv10 [$fraction6 add $TimeThree]
    set AddDiv11 [$fraction6 add $TimeFour]
    set AddDiv12 [$fraction9 add $TimeOne]
    set AddDiv13 [$fraction9 add $TimeTwo]
    set AddDiv14 [$fraction9 add $TimeThree]
    set AddDiv15 [$fraction9 add $TimeFour]
    set AddDiv16 [$fraction14 add $TimeOne]
    set AddDiv17 [$fraction14 add $TimeTwo]
    set AddDiv18 [$fraction14 add $TimeThree]
    set AddDiv19 [$fraction14 add $TimeFour]
    set AddDiv20 [$fraction2 add $fraction2]
    set AddDiv21 [$fraction2 add $fraction3]
    set AddDiv22 [$fraction2 add $fraction6]
    set AddDiv23 [$fraction2 add $fraction9]
    set AddDiv24 [$fraction2 add $fraction14]
    set AddDiv25 [$fraction3 add $fraction2]
    set AddDiv26 [$fraction3 add $fraction3]
    set AddDiv27 [$fraction3 add $fraction6]
    set AddDiv28 [$fraction3 add $fraction9]
    set AddDiv29 [$fraction3 add $fraction14]
    set AddDiv30 [$fraction6 add $fraction2]
    set AddDiv31 [$fraction6 add $fraction3]
    set AddDiv32 [$fraction6 add $fraction6]
    set AddDiv33 [$fraction6 add $fraction9]
    set AddDiv34 [$fraction6 add $fraction14]
    set AddDiv35 [$fraction9 add $fraction2]
    set AddDiv36 [$fraction9 add $fraction3]
    set AddDiv37 [$fraction9 add $fraction6]
    set AddDiv38 [$fraction9 add $fraction9]
    set AddDiv39 [$fraction9 add $fraction14]
    set AddDiv40 [$fraction14 add $fraction2]
    set AddDiv41 [$fraction14 add $fraction3]
    set AddDiv42 [$fraction14 add $fraction6]
    set AddDiv43 [$fraction14 add $fraction9]
    set AddDiv44 [$fraction14 add $fraction14]
	list \
	[$AddDiv0 getDoubleValue] \
	[$AddDiv1 getDoubleValue] \
	[$AddDiv2 getDoubleValue] \
	[$AddDiv3 getDoubleValue] \
	[$AddDiv4 getDoubleValue] \
	[$AddDiv5 getDoubleValue] \
	[$AddDiv6 getDoubleValue] \
	[$AddDiv7 getDoubleValue] \
	[$AddDiv8 getDoubleValue] \
	[$AddDiv9 getDoubleValue] \
	[$AddDiv10 getDoubleValue] \
	[$AddDiv11 getDoubleValue] \
	[$AddDiv12 getDoubleValue] \
	[$AddDiv13 getDoubleValue] \
	[$AddDiv14 getDoubleValue] \
	[$AddDiv15 getDoubleValue] \
	[$AddDiv16 getDoubleValue] \
	[$AddDiv17 getDoubleValue] \
	[$AddDiv18 getDoubleValue] \
	[$AddDiv19 getDoubleValue] \
	[$AddDiv20 getDoubleValue] \
	[$AddDiv21 getDoubleValue] \
	[$AddDiv22 getDoubleValue] \
	[$AddDiv23 getDoubleValue] \
	[$AddDiv24 getDoubleValue] \
	[$AddDiv25 getDoubleValue] \
	[$AddDiv26 getDoubleValue] \
	[$AddDiv27 getDoubleValue] \
	[$AddDiv28 getDoubleValue] \
	[$AddDiv29 getDoubleValue] \
	[$AddDiv30 getDoubleValue] \
	[$AddDiv31 getDoubleValue] \
	[$AddDiv32 getDoubleValue] \
	[$AddDiv33 getDoubleValue] \
	[$AddDiv34 getDoubleValue] \
	[$AddDiv35 getDoubleValue] \
	[$AddDiv36 getDoubleValue] \
	[$AddDiv37 getDoubleValue] \
	[$AddDiv38 getDoubleValue] \
	[$AddDiv39 getDoubleValue] \
	[$AddDiv40 getDoubleValue] \
	[$AddDiv41 getDoubleValue] \
	[$AddDiv42 getDoubleValue] \
	[$AddDiv43 getDoubleValue] \
	[$AddDiv44 getDoubleValue] \
} {1.3333333333 2.3333333333 3.3333333333 4.3333333333\
   1.25 2.25 3.25 4.25\
   1.6666666666 2.6666666666 3.6666666666 4.6666666666\
   2.5 3.5 4.5 5.5\
   2.3333333333 3.3333333333 4.3333333333 5.3333333333\
   0.6666666666 0.5833333333 1.0 1.8333333333 1.6666666666\
   0.5833333333 0.5 0.9166666666 1.75 1.5833333333\
   1.0 0.9166666666 1.3333333333 2.1666666666 2.0\
   1.8333333333 1.75 2.1666666666 3.0 2.8333333333\
   1.6666666666 1.5833333333 2.0 2.8333333333 2.6666666666}

######################################################################
####
#
# use fraction2: 1/3; fraction3: 1/4; fraction6: 2/3; fraction9: 3/2 fraction14: 4/3
test Time-5.1 {subtract fractions} {
    set SubDiv0 [$fraction2 subtract $TimeOne]
    set SubDiv1 [$fraction2 subtract $TimeTwo]
    set SubDiv2 [$fraction2 subtract $TimeThree]
    set SubDiv3 [$fraction2 subtract $TimeFour]
    set SubDiv4 [$fraction3 subtract $TimeOne]
    set SubDiv5 [$fraction3 subtract $TimeTwo]
    set SubDiv6 [$fraction3 subtract $TimeThree]
    set SubDiv7 [$fraction3 subtract $TimeFour]
    set SubDiv8 [$fraction6 subtract $TimeOne]
    set SubDiv9 [$fraction6 subtract $TimeTwo]
    set SubDiv10 [$fraction6 subtract $TimeThree]
    set SubDiv11 [$fraction6 subtract $TimeFour]
    set SubDiv12 [$fraction9 subtract $TimeOne]
    set SubDiv13 [$fraction9 subtract $TimeTwo]
    set SubDiv14 [$fraction9 subtract $TimeThree]
    set SubDiv15 [$fraction9 subtract $TimeFour]
    set SubDiv16 [$fraction14 subtract $TimeOne]
    set SubDiv17 [$fraction14 subtract $TimeTwo]
    set SubDiv18 [$fraction14 subtract $TimeThree]
    set SubDiv19 [$fraction14 subtract $TimeFour]
    set SubDiv20 [$fraction2 subtract $fraction2]
    set SubDiv21 [$fraction2 subtract $fraction3]
    set SubDiv22 [$fraction2 subtract $fraction6]
    set SubDiv23 [$fraction2 subtract $fraction9]
    set SubDiv24 [$fraction2 subtract $fraction14]
    set SubDiv25 [$fraction3 subtract $fraction2]
    set SubDiv26 [$fraction3 subtract $fraction3]
    set SubDiv27 [$fraction3 subtract $fraction6]
    set SubDiv28 [$fraction3 subtract $fraction9]
    set SubDiv29 [$fraction3 subtract $fraction14]
    set SubDiv30 [$fraction6 subtract $fraction2]
    set SubDiv31 [$fraction6 subtract $fraction3]
    set SubDiv32 [$fraction6 subtract $fraction6]
    set SubDiv33 [$fraction6 subtract $fraction9]
    set SubDiv34 [$fraction6 subtract $fraction14]
    set SubDiv35 [$fraction9 subtract $fraction2]
    set SubDiv36 [$fraction9 subtract $fraction3]
    set SubDiv37 [$fraction9 subtract $fraction6]
    set SubDiv38 [$fraction9 subtract $fraction9]
    set SubDiv39 [$fraction9 subtract $fraction14]
    set SubDiv40 [$fraction14 subtract $fraction2]
    set SubDiv41 [$fraction14 subtract $fraction3]
    set SubDiv42 [$fraction14 subtract $fraction6]
    set SubDiv43 [$fraction14 subtract $fraction9]
    set SubDiv44 [$fraction14 subtract $fraction14]
	list \
	[$SubDiv0 getDoubleValue] \
	[$SubDiv1 getDoubleValue] \
	[$SubDiv2 getDoubleValue] \
	[$SubDiv3 getDoubleValue] \
	[$SubDiv4 getDoubleValue] \
	[$SubDiv5 getDoubleValue] \
	[$SubDiv6 getDoubleValue] \
	[$SubDiv7 getDoubleValue] \
	[$SubDiv8 getDoubleValue] \
	[$SubDiv9 getDoubleValue] \
	[$SubDiv10 getDoubleValue] \
	[$SubDiv11 getDoubleValue] \
	[$SubDiv12 getDoubleValue] \
	[$SubDiv13 getDoubleValue] \
	[$SubDiv14 getDoubleValue] \
	[$SubDiv15 getDoubleValue] \
	[$SubDiv16 getDoubleValue] \
	[$SubDiv17 getDoubleValue] \
	[$SubDiv18 getDoubleValue] \
	[$SubDiv19 getDoubleValue] \
	[$SubDiv20 getDoubleValue] \
	[$SubDiv21 getDoubleValue] \
	[$SubDiv22 getDoubleValue] \
	[$SubDiv23 getDoubleValue] \
	[$SubDiv24 getDoubleValue] \
	[$SubDiv25 getDoubleValue] \
	[$SubDiv26 getDoubleValue] \
	[$SubDiv27 getDoubleValue] \
	[$SubDiv28 getDoubleValue] \
	[$SubDiv29 getDoubleValue] \
	[$SubDiv30 getDoubleValue] \
	[$SubDiv31 getDoubleValue] \
	[$SubDiv32 getDoubleValue] \
	[$SubDiv33 getDoubleValue] \
	[$SubDiv34 getDoubleValue] \
	[$SubDiv35 getDoubleValue] \
	[$SubDiv36 getDoubleValue] \
	[$SubDiv37 getDoubleValue] \
	[$SubDiv38 getDoubleValue] \
	[$SubDiv39 getDoubleValue] \
	[$SubDiv40 getDoubleValue] \
	[$SubDiv41 getDoubleValue] \
	[$SubDiv42 getDoubleValue] \
	[$SubDiv43 getDoubleValue] \
	[$SubDiv44 getDoubleValue] \
} {-0.6666666667 -1.6666666667 -2.6666666667 -3.6666666667\
   -0.75 -1.75 -2.75 -3.75\
   -0.3333333334 -1.3333333334 -2.3333333334 -3.3333333334\
   0.5 -0.5 -1.5 -2.5\
   0.3333333333 -0.6666666667 -1.6666666667 -2.6666666667\
   0.0 0.0833333333 -0.3333333334 -1.1666666667 -1.0\
   -0.0833333334 0.0 -0.4166666667 -1.25 -1.0833333334\
   0.3333333333 0.4166666666 0.0 -0.8333333334 -0.6666666667\
   1.1666666666 1.25 0.8333333333 0.0 0.1666666666\
   1.0 1.0833333333 0.6666666666 -0.1666666667 0.0}


######################################################################
####
#
# use fraction2: 1/3; fraction3: 1/4; fraction6: 2/3; fraction9: 3/2 fraction14: 4/3
test Time-5.1 {divide by fractions} {
    set DivDiv0 [$fraction2 divide $TimeOne]
    set DivDiv1 [$fraction2 divide $TimeTwo]
    set DivDiv2 [$fraction2 divide $TimeThree]
    set DivDiv3 [$fraction2 divide $TimeFour]
    set DivDiv4 [$fraction3 divide $TimeOne]
    set DivDiv5 [$fraction3 divide $TimeTwo]
    set DivDiv6 [$fraction3 divide $TimeThree]
    set DivDiv7 [$fraction3 divide $TimeFour]
    set DivDiv8 [$fraction6 divide $TimeOne]
    set DivDiv9 [$fraction6 divide $TimeTwo]
    set DivDiv10 [$fraction6 divide $TimeThree]
    set DivDiv11 [$fraction6 divide $TimeFour]
    set DivDiv12 [$fraction9 divide $TimeOne]
    set DivDiv13 [$fraction9 divide $TimeTwo]
    set DivDiv14 [$fraction9 divide $TimeThree]
    set DivDiv15 [$fraction9 divide $TimeFour]
    set DivDiv16 [$fraction14 divide $TimeOne]
    set DivDiv17 [$fraction14 divide $TimeTwo]
    set DivDiv18 [$fraction14 divide $TimeThree]
    set DivDiv19 [$fraction14 divide $TimeFour]
    set DivDiv20 [$fraction2 divide $fraction2]
    set DivDiv21 [$fraction2 divide $fraction3]
    set DivDiv22 [$fraction2 divide $fraction6]
    set DivDiv23 [$fraction2 divide $fraction9]
    set DivDiv24 [$fraction2 divide $fraction14]
    set DivDiv25 [$fraction3 divide $fraction2]
    set DivDiv26 [$fraction3 divide $fraction3]
    set DivDiv27 [$fraction3 divide $fraction6]
    set DivDiv28 [$fraction3 divide $fraction9]
    set DivDiv29 [$fraction3 divide $fraction14]
    set DivDiv30 [$fraction6 divide $fraction2]
    set DivDiv31 [$fraction6 divide $fraction3]
    set DivDiv32 [$fraction6 divide $fraction6]
    set DivDiv33 [$fraction6 divide $fraction9]
    set DivDiv34 [$fraction6 divide $fraction14]
    set DivDiv35 [$fraction9 divide $fraction2]
    set DivDiv36 [$fraction9 divide $fraction3]
    set DivDiv37 [$fraction9 divide $fraction6]
    set DivDiv38 [$fraction9 divide $fraction9]
    set DivDiv39 [$fraction9 divide $fraction14]
    set DivDiv40 [$fraction14 divide $fraction2]
    set DivDiv41 [$fraction14 divide $fraction3]
    set DivDiv42 [$fraction14 divide $fraction6]
    set DivDiv43 [$fraction14 divide $fraction9]
    set DivDiv44 [$fraction14 divide $fraction14]
	list \
	[$DivDiv0 getDoubleValue] \
	[$DivDiv1 getDoubleValue] \
	[$DivDiv2 getDoubleValue] \
	[$DivDiv3 getDoubleValue] \
	[$DivDiv4 getDoubleValue] \
	[$DivDiv5 getDoubleValue] \
	[$DivDiv6 getDoubleValue] \
	[$DivDiv7 getDoubleValue] \
	[$DivDiv8 getDoubleValue] \
	[$DivDiv9 getDoubleValue] \
	[$DivDiv10 getDoubleValue] \
	[$DivDiv11 getDoubleValue] \
	[$DivDiv12 getDoubleValue] \
	[$DivDiv13 getDoubleValue] \
	[$DivDiv14 getDoubleValue] \
	[$DivDiv15 getDoubleValue] \
	[$DivDiv16 getDoubleValue] \
	[$DivDiv17 getDoubleValue] \
	[$DivDiv18 getDoubleValue] \
	[$DivDiv19 getDoubleValue] \
	[$DivDiv20 getDoubleValue] \
	[$DivDiv21 getDoubleValue] \
	[$DivDiv22 getDoubleValue] \
	[$DivDiv23 getDoubleValue] \
	[$DivDiv24 getDoubleValue] \
	[$DivDiv25 getDoubleValue] \
	[$DivDiv26 getDoubleValue] \
	[$DivDiv27 getDoubleValue] \
	[$DivDiv28 getDoubleValue] \
	[$DivDiv29 getDoubleValue] \
	[$DivDiv30 getDoubleValue] \
	[$DivDiv31 getDoubleValue] \
	[$DivDiv32 getDoubleValue] \
	[$DivDiv33 getDoubleValue] \
	[$DivDiv34 getDoubleValue] \
	[$DivDiv35 getDoubleValue] \
	[$DivDiv36 getDoubleValue] \
	[$DivDiv37 getDoubleValue] \
	[$DivDiv38 getDoubleValue] \
	[$DivDiv39 getDoubleValue] \
	[$DivDiv40 getDoubleValue] \
	[$DivDiv41 getDoubleValue] \
	[$DivDiv42 getDoubleValue] \
	[$DivDiv43 getDoubleValue] \
	[$DivDiv44 getDoubleValue] \
} {0.3333333333 0.1666666666 0.1111111111 0.0833333333\
   0.25 0.125 0.0833333333 0.0625\
   0.6666666666 0.3333333333 0.2222222222 0.1666666666\
   1.5 0.75 0.5 0.375\
   1.3333333333 0.6666666666 0.4444444444 0.3333333333\
   1.0 1.3333333333 0.5 0.2222222222 0.25\
   0.75 1.0 0.375 0.1666666666 0.1875\
   2.0 2.6666666666 1.0 0.4444444444 0.5\
   4.5 6.0 2.25 1.0 1.125\
   4.0 5.3333333333 2.0 0.8888888888 1.0}
# use fraction2: 1/3; fraction3: 1/4; fraction6: 2/3; fraction9: 3/2 fraction14: 4/3
