# Tests for the ArrayMath Class
#
# @Author: Christopher Hylands
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}




# Complex numbers to be used
set c1 [java::new ptolemy.math.Complex 1 2]
set c2 [java::new ptolemy.math.Complex 3 -4]
set c3 [java::new ptolemy.math.Complex -4.9 -6]
set c4 [java::new ptolemy.math.Complex -7 8]
set c5 [java::new ptolemy.math.Complex -0.25 +0.4]

# Complex array of length 0
set ca0 [java::new {ptolemy.math.Complex[]} 0]

# Complex array
set ca1 [java::new {ptolemy.math.Complex[]} 4 [list $c1 $c2 $c3 $c4]]

# ca2 is a Complex array used to store the results of tests


####################################################################
test ArrayMath-1.1 {add} {
    set ca2 [java::call ptolemy.math.ArrayMath add $ca0 $c1]
    javaPrintArray $ca2
} {}

####################################################################
test ArrayMath-1.2 {add} {
    set ca2 [java::call ptolemy.math.ArrayMath add $ca1 $c5]
    javaPrintArray $ca2
} {{0.75 + 2.4i} {2.75 - 3.6i} {-5.15 - 5.6i} {-7.25 + 8.4i}}

####################################################################
test ArrayMath-2.1 {addR} {
    java::call ptolemy.math.ArrayMath addR $ca0 $c5
    javaPrintArray $ca0
} {}

####################################################################
test ArrayMath-2.2 {addR} {
    java::call ptolemy.math.ArrayMath addR $ca2 $c5
    epsilonDiff [javaPrintArray $ca2] \
	    {{0.5 + 2.8i} {2.5 - 3.2i} {-5.4 - 5.2} {-7.5 + 8.8i}}
} {}

####################################################################
test ArrayMath-3.1 {conjugate} {
    set ca2 [java::call ptolemy.math.ArrayMath conjugate $ca0]
    javaPrintArray $ca2
} {}

####################################################################
test ArrayMath-3.2 {conjugate} {
    set ca2 [java::call ptolemy.math.ArrayMath conjugate $ca1]
    epsilonDiff [javaPrintArray $ca2] {{1.0 - 2.0i} {3.0 + 4.0i} {-4.9 + 6.0i} {-7.0 - 8.0i}}
} {}

####################################################################
test ArrayMath-4.1 {conjugateR} {
    java::call ptolemy.math.ArrayMath conjugateR $ca0
    javaPrintArray $ca0
} {}

####################################################################
test ArrayMath-4.2 {conjugateR} {
    set ca2 [java::call ptolemy.math.ArrayMath add $ca1 $c5]
    java::call ptolemy.math.ArrayMath conjugateR $ca2
    epsilonDiff [javaPrintArray $ca2] {{0.75 - 2.4i} {2.75 + 3.6i} {-5.15 + 5.6i} {-7.25 - 8.4i}}
} {}

####################################################################
test ArrayMath-5.1 {convolve Complex} {
    set ca2 [java::call ptolemy.math.ArrayMath \
	    {convolve ptolemy.math.Complex[] ptolemy.math.Complex[]} $ca0 $ca0]
    javaPrintArray $ca2
} {}

####################################################################
test ArrayMath-5.2 {convolve Complex} {
    set ca2 [java::call ptolemy.math.ArrayMath \
	    {convolve ptolemy.math.Complex[] ptolemy.math.Complex[]} $ca1 $ca1]
    epsilonDiff [javaPrintArray $ca2] \
	    {{-3.0 + 4.0i} {22.0 + 4.0i} {7.2 - 55.6i} {-123.4 - 8.8} \
	    {10.01 + 162.8i} {164.6 + 5.6i} {-15.0 - 112.0i}}
} {}



####################################################################
test ArrayMath-6.1 {convolve double: empty array} {
    set da0 [java::new {double[]} 0]
    set da2 [java::call ptolemy.math.ArrayMath \
	    {convolve double[] double[]} $da0 $da0]
    $da2 getrange 0
} {}

####################################################################
test ArrayMath-6.2 {convolve double} {
    set da1 [java::new {double[]} 4 {1 2 -3 4.1}]
    set da2 [java::call ptolemy.math.ArrayMath \
	    {convolve double[] double[]} $da1 $da1]
    epsilonDiff [$da2 getrange 0] {1.0 4.0 -2.0 -3.8 25.4 -24.6 16.81}
} {}

####################################################################
test ArrayMath-7.1 {limit: empty array} {
    set da0 [java::new {double[]} 0]
    set da2 [java::call ptolemy.math.ArrayMath limit $da0 0 0]
    $da2 getrange 0
} {}

####################################################################
test ArrayMath-7.2 {limit} {
    set l [list 1 2 -3 4.1 0.0 -0.0 +0.0 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    [java::field java.lang.Double NEGATIVE_INFINITY] \
	    [java::field java.lang.Double NaN] \
	    [java::field java.lang.Double MIN_VALUE] \
	    [java::field java.lang.Double MAX_VALUE] \
	    ]
    set da3 [java::new {double[]} [llength $l] $l]

    set da2 [java::call ptolemy.math.ArrayMath limit $da3 -0.5 1.25]
    $da2 getrange 0
} {1.0 1.25 -0.5 1.25 0.0 -0.0 0.0 1.25 -0.5 NaN 4.94065645841e-324 1.25}

####################################################################
test ArrayMath-7.3 {limit: no bottom} {
    set da2 [java::call ptolemy.math.ArrayMath limit \
	    $da3 \
	    [java::field java.lang.Double MIN_VALUE] \
	    1.25]
    $da2 getrange 0
} {1.0 1.25 4.94065645841e-324 1.25 4.94065645841e-324 4.94065645841e-324 4.94065645841e-324 1.25 4.94065645841e-324 NaN 4.94065645841e-324 1.25}


####################################################################
test ArrayMath-7.4 {limit: no top} {
    set da2 [java::call ptolemy.math.ArrayMath limit \
	    $da3 \
	    -0.5 \
	    [java::field java.lang.Double MAX_VALUE] \
	    ]
    $da2 getrange 0
} {1.0 2.0 -0.5 4.1 0.0 -0.0 0.0 1.79769313486e+308 -0.5 NaN 4.94065645841e-324 1.79769313486e+308}

####################################################################
test ArrayMath-7.5 {limit: bottom greater than top} {
    set da2 [java::call ptolemy.math.ArrayMath limit \
	    $da3 \
	    1.25 \
            -0.5 \
	    ]
    $da2 getrange 0
} {-0.5 -0.5 1.25 -0.5 -0.5 -0.5 -0.5 -0.5 1.25 NaN -0.5 -0.5}

####################################################################
test ArrayMath-7.6 {limit: Infinity top} {
    set da2 [java::call ptolemy.math.ArrayMath limit \
	    $da3 \
	    -0.5 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    ]
    $da2 getrange 0
} {1.0 2.0 -0.5 4.1 0.0 -0.0 0.0 Infinity -0.5 NaN 4.94065645841e-324 1.79769313486e+308}


####################################################################
test ArrayMath-8.1 {limitR: empty array} {
    set da0tmp [java::new {double[]} 0]
    java::call ptolemy.math.ArrayMath limitR $da0tmp 0 0
    $da0tmp getrange 0
} {}

####################################################################
test ArrayMath-8.2 {limitR} {
    set l [list 1 2 -3 4.1 0.0 -0.0 +0.0 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    [java::field java.lang.Double NEGATIVE_INFINITY] \
	    [java::field java.lang.Double NaN] \
	    [java::field java.lang.Double MIN_VALUE] \
	    [java::field java.lang.Double MAX_VALUE] \
	    ]
    set da3tmp [java::new {double[]} [llength $l] $l]

    java::call ptolemy.math.ArrayMath limitR $da3tmp -0.5 1.25
    $da3tmp getrange 0
} {1.0 1.25 -0.5 1.25 0.0 -0.0 0.0 1.25 -0.5 NaN 4.94065645841e-324 1.25}

####################################################################
test ArrayMath-9.0 {polynomial: null array} {
    set ca2 [java::call ptolemy.math.ArrayMath polynomial $ca0]
    javaPrintArray $ca2
} {{1.0 + 0.0i}}

####################################################################
test ArrayMath-9.1 {polynomial } {
    # FIXME: we need some better input data here
    set ca2 [java::call ptolemy.math.ArrayMath polynomial $ca1]
    epsilonDiff [javaPrintArray $ca2] \
	    {{1.0 + 0.0i} {7.9 + 0.0i} {49.7 + 36.6i} \
	    {-199.9 + 155.2i} {899.7 + 195.4i}}

} {}

####################################################################
test ArrayMath-9.1 {polynomial: array of length 1 } {
    set ca3 [java::new {ptolemy.math.Complex[]} 1 [list $c1]]
    epsilonDiff [javaPrintArray $ca3] {{1.0 + 2.0i}}
} {}

####################################################################
test ArrayMath-10.1 {product: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    set result [java::call ptolemy.math.ArrayMath product $ca0]
    $result toString
} {}

####################################################################
test ArrayMath-11.2 {product} {
    set result [java::call ptolemy.math.ArrayMath product $ca1]
    epsilonDiff [$result toString] {899.7 + 195.4i}
} {} 

####################################################################
test ArrayMath-12.1 {subtract} {
    set ca2 [java::call ptolemy.math.ArrayMath subtract $ca0 $c1]
    javaPrintArray $ca2
} {}

####################################################################
test ArrayMath-12.2 {subtract} {
    set ca2 [java::call ptolemy.math.ArrayMath subtract $ca1 $c5]
    javaPrintArray $ca2
} {{1.25 + 1.6i} {3.25 - 4.4i} {-4.65 - 6.4i} {-6.75 + 7.6i}}

####################################################################
test ArrayMath-13.1 {subtractR} {
    java::call ptolemy.math.ArrayMath subtractR $ca0 $c5
    javaPrintArray $ca0
} {}

####################################################################
test ArrayMath-13.2 {subtractR} {
    java::call ptolemy.math.ArrayMath subtractR $ca2 $c5
    epsilonDiff [javaPrintArray $ca2] \
	    {{1.5 + 1.2i} {3.5 - 4.8i} {-4.4 - 6.8} {-6.5 + 7.2i}}
} {}
