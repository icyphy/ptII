# Tests for the DoubleArrayStat Class
#
# @Author: Jeff Tsay
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

proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set a2 [java::new {double[]} 5 [list 236.1 -36.21 4826.2 5.0 65.4]]
set a4 [java::new {double[]} 5 [list 23.7 -0.00367 4826.2 5.0 0.000654]]
set l [list 1 2 -3 4.1 0.0 -0.0 +0.0 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    [java::field java.lang.Double NEGATIVE_INFINITY] \
	    [java::field java.lang.Double NaN] \
	    [java::field java.lang.Double MIN_VALUE] \
	    [java::field java.lang.Double MAX_VALUE] \
	    ]
set a3 [java::new {double[]} [llength $l] $l]
set p1 [java::new {double[]} 4 [list 0.3 0.2 0.0 0.5]]
set p2 [java::new {double[]} 4 [list 0.7 0.1 0.0 0.2]]
set p3 [java::new {double[]} 4 [list 0.4 0.4 0.1 0.1]]
set badp3 [java::new {double[]} 4 [list 0.4 0.4 -0.1 0.2]]

####################################################################
test DoubleArrayStat-1.1 {entropy} {
    set r [java::call ptolemy.math.DoubleArrayStat entropy $p1]
    set br [java::call ptolemy.math.SignalProcessing close $r \
            1.485475297]
    list $br
} {1}

####################################################################
test DoubleArrayStat-1.1 {min} {
    set r [java::call ptolemy.math.DoubleArrayStat min $a2]
    list $r
} -36.21

####################################################################
test DoubleArrayStat-1.2 {min with weird array} {
    set r [java::call ptolemy.math.DoubleArrayStat min $a3]
    list $r
} -Infinity


####################################################################
test DoubleArrayStat-2.1 {max} {
    set r [java::call ptolemy.math.DoubleArrayStat max $a2]
    list $r
}  4826.2

####################################################################
test DoubleArrayStat-3.1 {mean} {
    set r [java::call ptolemy.math.DoubleArrayStat mean $a2]
    list $r
}  1019.298

####################################################################
test DoubleArrayStat-3.1 {productOfElements} {
    set r [java::call ptolemy.math.DoubleArrayStat productOfElements $a4]
    epsilonDiff [list $r] [list -1.37267422284600]
} {}

####################################################################
test DoubleArrayStat-1.1 {relativeEntropy} {
    set r [java::call ptolemy.math.DoubleArrayStat relativeEntropy $p1 \
           $p2]
    set br [java::call ptolemy.math.SignalProcessing close $r \
            0.49424632141]
    set rl [java::new {double[]} 1 [list $r]]
    epsilonDiff [list $r] [list 0.49424632141]
} {}

####################################################################
test DoubleArrayStat-3.1 {standardDeviation} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2]
    set er [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2 false]
    epsilonDiff [list $r] [list $er]
} {}

####################################################################
test DoubleArrayStat-3.1 {standardDeviation sample true} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2 true]
    epsilonDiff [list $r] {2130.652535614383}
} {}

####################################################################
test DoubleArrayStat-3.2 {standardDeviation sample false} {
    set r [java::call ptolemy.math.DoubleArrayStat standardDeviation $a2 true]
    epsilonDiff [list $r] {1905.713562426421}
} {}

####################################################################
test DoubleArrayStat-3.1 {sumOfElements} {
    set r [java::call ptolemy.math.DoubleArrayStat sumOfElements $a2]
    list $r
} 5096.49

####################################################################
test DoubleArrayStat-3.1 {sumOfSquares} {
    set r [java::call ptolemy.math.DoubleArrayStat sumOfSquares $a2]
    list $r
} 23353562.9741

####################################################################
test DoubleArrayStat-3.1 {variance sample} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a2]
    set er [java::call ptolemy.math.DoubleArrayStat variance $a2 false] 
    epsilonDiff [list $r] [list $er]
} {}

####################################################################
test DoubleArrayStat-3.1 {variance sample true} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a2 true]
    epsilonDiff [list $r] {4539680.22750001}
} {}

####################################################################
test DoubleArrayStat-3.1 {variance sample false} {
    set r [java::call ptolemy.math.DoubleArrayStat variance $a2 true]
    epsilonDiff [list $r] {3631744.182016002}
} {}


