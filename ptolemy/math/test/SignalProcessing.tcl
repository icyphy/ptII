# Tests for the SignalProcessing Class
#
# @Author: Edward A. Lee, Christopher Hylands, Jeff Tsay
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set PI [java::field java.lang.Math PI]

# Complex numbers to be used
set c1 [java::new ptolemy.math.Complex 1 2]
set c2 [java::new ptolemy.math.Complex 3 -4]
set c3 [java::new ptolemy.math.Complex -4.9 -6]
set c4 [java::new ptolemy.math.Complex -7 8]
set c5 [java::new ptolemy.math.Complex -0.25 +0.4]

set tc1 [java::new ptolemy.math.Complex 1.9580988213655477 1.9473493067241758]
set tc2 [java::new ptolemy.math.Complex 7.735387325913223 8.080714526375889]
set tc3 [java::new ptolemy.math.Complex -0.7311713808028095 7.704518832373466]
set tc4 [java::new ptolemy.math.Complex 9.283834783364014 3.157439394273176]
set tc5 [java::new ptolemy.math.Complex -2.9952657755544854 1.4300091256061105]
set tc6 [java::new ptolemy.math.Complex 9.037531049701858 9.785385282418467]
set tc7 [java::new ptolemy.math.Complex -8.173396227661598 1.2484787363532277]
set tc8 [java::new ptolemy.math.Complex 5.248333404047374 -0.1488041700540812]
set tc9 [java::new ptolemy.math.Complex 3.2599473937803847 -8.578564929357185]
set tc10 [java::new ptolemy.math.Complex -8.734318755327266 -9.234363319734305]
set tc11 [java::new ptolemy.math.Complex -1.01917647513811 -6.352732529324509]
set tc12 [java::new ptolemy.math.Complex -4.805316651645855 1.4291899399670633]
set tc13 [java::new ptolemy.math.Complex 1.2314786402975493 -1.0738755427305513]
set tc14 [java::new ptolemy.math.Complex 7.3758036581360855 5.365263047124664]
set tc15 [java::new ptolemy.math.Complex 9.683296291468398 -2.009423412382101]
set tc16 [java::new ptolemy.math.Complex 6.829045613048127 3.554929133420334] 

# Complex array of length 0
set ca0 [java::new {ptolemy.math.Complex[]} 0]

# Complex array of length 1
set ca1 [java::new {ptolemy.math.Complex[]} 1 [list $c1]]

# Complex array
set ca4 [java::new {ptolemy.math.Complex[]} 4 [list $c1 $c2 $c3 $c4]]
set cat1 [java::new {ptolemy.math.Complex[]} 16 [list $tc1 $tc2 $tc3 $tc4 $tc5 $tc6 $tc7 $tc8 $tc9 $tc10 $tc11 $tc12 $tc13 $tc14 $tc15 $tc16]]

# Double arrays
set a0 [java::new {double[]} 0]
set a1 [java::new {double[]} 1 [list -36.32]]
set a2 [java::new {double[]} 2 [list 48.21 -2.62]]
set rf1 [java::new {double[]} 16 [list 35.18411171499244 58.597309396491724 -8.538697482870184 1.2562064613276824 9.69895211060134 -9.075655753742792 1.1140913592386354 -13.76214746925425 -28.756489139482685 -6.045434825499838 26.727826141831585 -10.040825463925147 -2.309538366555107 -38.6680744335618 8.62411338341144 7.323833508845723]]
set if1 [java::new {double[]} 16 [list 16.30551342105384 32.95091818451263 1.5948145331090942 13.888295409268732 -5.724429796387775 -23.565125711136343 19.801562101512367 -37.125875283435164 -27.673994246528572 36.672861270367434 -9.049017104630792 8.283334138653242 -8.007417537167292 12.95198086398985 -40.29675635202494 40.150925016430506]]

proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test SignalProcessing-1.1 {close} {
    set epsilon [java::field ptolemy.math.SignalProcessing epsilon]
    set testpairslist [list \
	    [list 1 1] \
	    [list -1 [expr {-1 + $epsilon/2}]] \
	    [list -1 [expr {-1 - $epsilon/2}]] \
	    [list [expr {-1 + $epsilon/2}] -1] \
	    [list [expr {-1 - $epsilon/2}] -1] \
	    [list 1 2] \
	    [list -1 2] \
	    [list 1 -2] \
	    [list -1 -2] \
	    [list [java::field java.lang.Double POSITIVE_INFINITY] 1] \
	    [list [java::field java.lang.Double NEGATIVE_INFINITY] 1] \
	    [list [java::field java.lang.Double NaN] 1]\
	    [list [java::field java.lang.Double MIN_VALUE] 1] \
	    [list [java::field java.lang.Double MAX_VALUE] 1] \
	    [list [java::field java.lang.Double POSITIVE_INFINITY] \
	          [java::field java.lang.Double POSITIVE_INFINITY]] \
	    [list [java::field java.lang.Double NEGATIVE_INFINITY] \
	          [java::field java.lang.Double NEGATIVE_INFINITY]] \
	    [list [java::field java.lang.Double NaN] \
	          [java::field java.lang.Double NaN]] \
	    [list [java::field java.lang.Double MIN_VALUE] \
	          [java::field java.lang.Double MIN_VALUE]] \
	    [list [java::field java.lang.Double MAX_VALUE] \
	          [java::field java.lang.Double MAX_VALUE]] \
	    ]

    set results {}

    foreach testpair $testpairslist {
	set a [lindex $testpair 0]
	set b [lindex $testpair 1]
	if [catch {set callresults \
		[java::call ptolemy.math.SignalProcessing close  $a $b]} \
		errmsg] {
	    lappend results $errmsg
	} else {
	    lappend results $callresults
	}
    }
    list $results
} {{1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1}}

####################################################################
test SignalProcessing-2.1 {convolve double: empty array} {
    set da0 [java::new {double[]} 0]
    set da2 [java::call ptolemy.math.SignalProcessing \
	    {convolve double[] double[]} $da0 $da0]
    $da2 getrange 0
} {}

####################################################################
test SignalProcessing-2.2 {convolve double} {
    set da1 [java::new {double[]} 4 {1 2 -3 4.1}]
    set da2 [java::call ptolemy.math.SignalProcessing \
	    {convolve double[] double[]} $da1 $da1]
    epsilonDiff [$da2 getrange 0] {1.0 4.0 -2.0 -3.8 25.4 -24.6 16.81}
} {}

####################################################################
test SignalProcessing-3.1 {convolve Complex} {
    set ca2 [java::call ptolemy.math.SignalProcessing \
	    {convolve ptolemy.math.Complex[] ptolemy.math.Complex[]} $ca0 $ca0]
    jdkPrintArray $ca2
} {}

####################################################################
test SignalProcessing-3.2 {convolve Complex} {
    set ca2 [java::call ptolemy.math.SignalProcessing \
	    {convolve ptolemy.math.Complex[] ptolemy.math.Complex[]} $ca4 $ca4]
    epsilonDiff [jdkPrintArray $ca2] \
	    {{-3.0 + 4.0i} {22.0 + 4.0i} {7.2 - 55.6i} {-123.4 - 8.8} \
	    {10.01 + 162.8i} {164.6 + 5.6i} {-15.0 - 112.0i}}
} {}

####################################################################
test SignalProcessing-2.1 {decibel} {
    epsilonDiff \
	    [list \
	    [java::call ptolemy.math.SignalProcessing {decibel double} -10.0] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 0.0] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 0.1] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 1.0] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 10.0] \
	    ] {-Infinity -Infinity -106.03796221 0.0 106.03796221}
} {}


####################################################################
test SignalProcessing-3.1 {decibel array: empty array} {
    set dbresults [java::call ptolemy.math.SignalProcessing \
	    {decibel double[]} $a0]
    $dbresults getrange 0
} {}

####################################################################
test SignalProcessing-3.2 {decibel array} {
    set dbarray [java::new {double[]} 5 {-10.0 0.0 0.1 1.0 10.0}]
    set dbresults [java::call ptolemy.math.SignalProcessing \
	    {decibel double[]} $dbarray]
    epsilonDiff [$dbresults getrange 0] \
	    {-Infinity -Infinity -106.03796221 0.0 106.03796221}
} {}

####################################################################
test SignalProcessing-3.1 {DCT double[] empty array} {
    catch {set eres [java::call ptolemy.math.SignalProcessing \
	    {DCT double[]} $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.SignalProcessing : size of transform must be positive.}}

####################################################################
test SignalProcessing-3.2 {DCT double[] order 1} {
    set dctresult [java::call ptolemy.math.SignalProcessing {DCT double[]} $a1]
    # this is -36.32 / sqrt(2)
    set eresult [java::new {double[]} 1 [list -25.68211829]]
    set b [java::call ptolemy.math.DoubleArrayMath \
    	   {within double[] double[] double} $eresult $dctresult 0.00001]
    list $b
} {1}

####################################################################
test SignalProcessing-3.3 {DCT double[] order 2} {
    set dctresult [java::call ptolemy.math.SignalProcessing {DCT double[]} $a2]
    set eresult [java::new {double[]} 2 [list 32.236998154294 35.942237687712]]
    set b [java::call ptolemy.math.DoubleArrayMath \
	   {within double[] double[] double} $eresult $dctresult 0.00001]
    list $b
} {1}

####################################################################
test SignalProcessing-4.2 {FFTComplexOut Complex[] : empty array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[]} $ca0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.SignalProcessing : size of transform must be positive.}}

####################################################################
test SignalProcessing-4.2 {FFTComplexOut Complex[]} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca5 [java::new {ptolemy.math.Complex[]} 5 [list $c1 $c0 $c0 $c0 $c0]]

    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[]} $ca5]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.3 {FFTComplexOut Complex[] order 0} {
    # NOTE: uses setup from 6.3 above
    set result [java::call ptolemy.math.SignalProcessing \
	       {FFTComplexOut ptolemy.math.Complex[]} $ca1]
    epsilonDiff [javaPrintArray $result] {{1.0 + 2.0i}}
} {}

####################################################################
test SignalProcessing-5.4 {FFTComplexOut Complex[] order 1} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[] int } $ca2 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.5 {FFTComplexOut Complex[] order 2} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca4t [java::new {ptolemy.math.Complex[]} 4 [list $c1 $c0 $c0 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[] int } $ca4t 2]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}


####################################################################
test SignalProcessing-5.6 {FFTComplexOut, order 1, w/ larger array} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array of size 3, or a order 1 fft, the size should be 2
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[] int } $ca3 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.7 {FFTComplexOut, order 2, smaller array} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array of size 2, hopefully fft will pad
    set ca2t [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[] int } $ca2t 2]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.8 {FFTComplexOut, order 4} {
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut ptolemy.math.Complex[] int } $cat1 4]
    set efftr [java::call ptolemy.math.ComplexArrayMath {formArray double[] double[] } $rf1 $if1]
    set close [java::call ptolemy.math.ComplexArrayMath {within ptolemy.math.Complex[] ptolemy.math.Complex[] double } $result $efftr 0.00001]
    list $close
} {1}

####################################################################
test SignalProcessing-6.1 {FFTComplexOut double[] : empty array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut double[]} $a0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.SignalProcessing : size of transform must be positive.}}

####################################################################
test SignalProcessing-6.3 {FFTComplexOut double[]} {
    # Real array
    set impulse [java::new {double[]} 5 [list 1.0 0.0 0.0 0.0 0.0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut double[]} $impulse]
    epsilonDiff [javaPrintArray $result] {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}
} {}

####################################################################
test SignalProcessing-7.3 {FFTComplexOut double[] order 0} {
    # NOTE: uses setup from 6.3 above
    set result [java::call ptolemy.math.SignalProcessing \
	       {FFTComplexOut double[] int} $a1 0]
    set im [java::new {double[]} 1 [list 0.0]]
    set eresult [java::call ptolemy.math.ComplexArrayMath \
	         {formArray double[] double[]} $a1 $im]
    set br [java::call ptolemy.math.ComplexArrayMath \
            {within ptolemy.math.Complex[] ptolemy.math.Complex[] double} \
             $result $eresult 0.00001] 
    list $br
} {1}

####################################################################
test SignalProcessing-7.4 {FFTComplexOut double, order 1} {
    # NOTE: uses setup from 6.3 above
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut double[] int} $impulse 1 ]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-7.5 {FFTComplexOut double, order 3} {
    # NOTE: uses setup from 6.3 above
    # The input array is length 5.
    set result [java::call ptolemy.math.SignalProcessing \
	    {FFTComplexOut double[] int} $impulse 3 ]
    epsilonDiff [javaPrintArray $result] {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}
} {}

####################################################################
test SignalProcessing-9.1 {IFFTComplexOut Complex: empty array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.SignalProcessing : size of transform must be positive.}}

####################################################################
test SignalProcessing-9.3 {IFFTComplexOut Complex : order 1} {
    # The inverse of test 5.4 above
    set ca2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c1 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca2]
    javaPrintArray $result
} {{1.0 + 0.0i} {0.0 + 0.0i}}

####################################################################
test SignalProcessing-9.4 {IFFTComplexOut Complex: array that is not a power of two in length} {
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca3]
    javaPrintArray $result
} {{0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i}}

####################################################################
test SignalProcessing-10.3 {IFFTComplexOut: order 0} {
    set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca1]
    epsilonDiff [javaPrintArray $result] {{1.0 + 2.0i}}
} {}

####################################################################
test SignalProcessing-10.4 {IFFTComplexOut Complex: order 1} {
    # The inverse of test 5.4 above
    set ca2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c1]]
    set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca2 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {0.0 + 0.0i}}

####################################################################
test SignalProcessing-10.5 {IFFTComplexOut Complex: array that is not a power of two in length} {
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca3 2]
    epsilonDiff [javaPrintArray $result] {{0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i}}
} {}

####################################################################
test SignalProcessing-10.6 {IFFTComplexOut Complex: array is longer than order} {
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    IFFTComplexOut $ca3 1]
    javaPrintArray $result
} {{0.5 + 0.0i} {0.5 + 0.0i}}

####################################################################
test SignalProcessing-11.1 {poleZeroToFreq:} {
    list "We need tests for poleZeroToFreq with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-12.1  {powerOfTwo: check range} {
    set negative [catch {[java::call \
	    ptolemy.math.SignalProcessing powerOfTwo -0.01]} errMsg]
    set zero [catch {[java::call \
	    ptolemy.math.SignalProcessing powerOfTwo 0.01]} errMsg]
    set positive [java::call \
	    ptolemy.math.SignalProcessing powerOfTwo 2.1]
    set anotherpositive [java::call \
	    ptolemy.math.SignalProcessing powerOfTwo 10.0]
    list $negative $zero $positive $anotherpositive
} {1 1 4 16}

####################################################################
test SignalProcessing-13.1 {raisedCosine} {
    list "We need tests for raisedCosine with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-14.1 {raisedCosinePulse} {
    list "We need tests for raisedCosinePulse with realistic input data"
} {1} {KNOW_ERROR}


# Used to test sawtooth, square and triangle
proc _testSignalProcessingFunction { function period phase \
	starttime endtime steptime} {
    set plot 0
    if {$plot} {
	global plotfilenumber
	if ![info exists plotfilenumber] {
	    set plotfilenumber 1
	} else {
	    incr plotfilenumber
	}
	set plotfile /tmp/sp$plotfilenumber.plt
	set fd [open $plotfile "w"]
	puts $fd "TitleText: $function period=$period phase=$phase $starttime <= t <= $endtime by $steptime"

    } 
    set results {}
    for {set time $starttime} \
	    {$time < $endtime} \
	    {set time [expr {$time + $steptime}]} {
	set value  [java::call \
		ptolemy.math.SignalProcessing $function $period $phase $time]
	lappend results $value
	if {$plot} {
	    puts $fd "$time $value"
	}
    }
    if {$plot} {
	close $fd
	exec pxgraph $plotfile &
    }
    return $results
}

####################################################################
test SignalProcessing-15.1 {sawtooth} {
    _testSignalProcessingFunction sawtooth 1.0 0.0 -1.0 2.0 0.2
} {-2.0 -1.6 -1.2 -0.8 -0.4 0.0 0.4 0.8 -0.8 -0.4 0.0 0.4 0.8 -0.8 -0.4}

####################################################################
test SignalProcessing-15.2 {sawtooth: negative period} {
    # FIXME, some of the results are less than -1.0?
    _testSignalProcessingFunction sawtooth -1.0 0.5 -1.0 2.0 0.2
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-15.3 {sawtooth: negative phase} {
    # FIXME, some of the results are less than -1.0?
    _testSignalProcessingFunction sawtooth 1.0 -0.5 -1.0 2.0 0.2
    #{-1.0 0.6 0.2 -0.2 -0.6 -1.0 0.6 0.2 -0.2 -0.6 -1.0 -1.4 -1.8 -2.2 -2.6}
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-16.1 {square} {
    # FIXME, should these vary more at the beginning
    _testSignalProcessingFunction square 1.0 0.5 -1.0 2.0 0.2
    #1.0 1.0 1.0 1.0 1.0 -1.0 -1.0 -1.0 1.0 1.0 -1.0 -1.0 -1.0 1.0 1.0
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-16.2 {square: negative period} {
    # FIXME, the value goes to -1 and stays there?
    _testSignalProcessingFunction square -1.0 0.5 -1.0 2.0 0.2
    #-1.0 -1.0 -1.0 1.0 1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-16.3 {square: negative phase} {
    # FIXME, the value is always -1?
    _testSignalProcessingFunction square -1.0 -0.5 -1.0 2.0 0.2
    #-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0
} {} {KNOWN_ERROR}


####################################################################
test SignalProcessing-17.1 {sqrtRaisedCosine} {
    list "We need tests for sqrtRaisedCosine with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-18.1 {sqrtRaisedCosinePulse} {
    list "We need tests for sqrtRaisedCosinePulse with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-19.1 {triangle} {
    # FIXME: Does not look very triangular to me
    _testSignalProcessingFunction triangle 1.0 0.5 -1.0 2.0 0.2
    #-2.0 -1.2 -0.4 0.4 0.8 0.0 -0.8 -0.4 0.4 0.8 0.0 -0.8 -0.4 0.4 0.8
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-19.2 {triangle: negative period} {
    # FIXME: values are less than -1.0
    _testSignalProcessingFunction triangle -1.0 0.5 -1.0 2.0 0.2
    #0.0 0.8 0.4 -0.4 -0.8 0.0 0.8 0.4 -0.4 -1.2 -2.0 -2.8 -3.6 -4.4 -1.2
} {} {KNOW_ERROR}

####################################################################
test SignalProcessing-19.3 {triangle: negative phase} {
    # FIXME: values are less than -1.0
    _testSignalProcessingFunction triangle -1.0 -0.5 -1.0 2.0 0.2
    #0.0 0.8 0.4 -0.4 -1.2 -2.0 -2.8 -3.6 -4.4 -1.2 -2.0 -2.8 -3.6 -4.4 -1.2
} {} {KNOW_ERROR}

####################################################################
test SignalProcessing-20.1 {unwrap} {
    list "We need tests for unwrap with realistic input data"
} {1} {KNOW_ERROR}
