# Tests for the SignalProcessing Class
#
# @Author: Edward A. Lee, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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
    set emptyarray [java::new {double[]} 0]
    set dbresults [java::call ptolemy.math.SignalProcessing \
	    {decibel double[]} $emptyarray]
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
test SignalProcessing-4.1 {fft Complex: null argument} {
    # Real array
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[]} [java::null]]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-4.2 {fft Complex: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[]} $ca0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}


####################################################################
test SignalProcessing-4.2 {fft Complex} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca1 [java::new {ptolemy.math.Complex[]} 5 [list $c1 $c0 $c0 $c0 $c0]]

    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[]} $ca1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.1 {fft Complex, order: null argument} {
    # Real array
    catch {java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int} [java::null] 1} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-5.2 {fft Complex, order: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    catch {java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int} $ca0 1} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-5.3 {fft Complex, order 0} {
    catch {java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 0} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: order argument must be positive.}}

####################################################################
test SignalProcessing-5.4 {fft Complex, order 1} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca1 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.5 {fft Complex, order 2} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca1 [java::new {ptolemy.math.Complex[]} 4 [list $c1 $c0 $c0 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 2]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}


####################################################################
test SignalProcessing-5.6 {fft Complex, order 1, w/ larger array} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array of size 3, or a order 1 fft, the size should be 2
    # FIXME: This test should return an order 1 fft, instead 
    # the code throws an ArrayIndexOutOfBoundsException
    set ca1 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}} \
	{KNOWN_FAILURE}

####################################################################
test SignalProcessing-5.7 {fft Complex, order 2, smaller array} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array of size 2, hopefully fft will pad
    set ca1 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 2]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-2.1 {fft} {
    # Real array
    set impulse [java::new {double[]} 5 [list 1.0 0.0 0.0 0.0 0.0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[]} $impulse]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}
