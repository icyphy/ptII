# Tests for the DoubleArrayMath Class
#
# @Author: Christopher Hylands, Jeff Tsay
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Double array of length 0
set a0 [java::new {double[]} 0]
set b0 [java::new {double[]} 0]

set a1 [java::new {double[]} 5 [list 3.7 -6.6 0.0003 -3829 -3.261]]
set a2 [java::new {double[]} 5 [list 4826.2 236.1 -36.21 5 65.4]]
set b1 [java::new {double[]} 3 [list -0.0000976 5832.61 -43.21]]

set e1 [java::new {double[]} 4 [list -62.3 0.332 5.22 -0.03]]

# ar is a double array used to store the results of tests

####################################################################
test DoubleArrayMath-1.1 {abs} {
    set ar [java::call ptolemy.math.DoubleArrayMath abs $a1]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    jdkPrintArray $ar
} {3.7 6.6 0.0003 3829.0 3.261}

####################################################################
test DoubleArrayMath-1.2 {abs with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath abs $a0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-2.1 {add} {
    set ar [java::call ptolemy.math.DoubleArrayMath add $a1 $a2]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    jdkPrintArray $ar
} {4829.9 229.5 -36.2097 -3824.0 62.139}

####################################################################
test DoubleArrayMath-2.2 {add with unequally sized arrays} {
    catch {set ar [java::call ptolemy.math.DoubleArrayMath add $a1 $b1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.add() : input arrays must have the same length, but the first array has length 5 and the second array has length 3.}}

####################################################################
test DoubleArrayMath-2.3 {add with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath add $a0 $b0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-3.1 {append with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a0 $b0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-3.2 {append with 1 empty array, one non-empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a0 $a1]
    epsilonDiff [$ar getrange 0] [$a1 getrange 0]
} {}

####################################################################
test DoubleArrayMath-3.3 {append with one non-empty array, 1 empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a1 $a0]
    epsilonDiff [$ar getrange 0] [$a1 getrange 0]
} {}

####################################################################
test DoubleArrayMath-3.4 {append with 2 non-empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a1 $a2]
    epsilonDiff [$ar getrange 0] \
    [list 3.7 -6.6 0.0003 -3829 -3.261 4826.2 236.1 -36.21 5 65.4]
} {}

####################################################################
test DoubleArrayMath-4.1 {append with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a0 -4 0 $b0 8 0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-4.2 {append with 1 empty array, one non-empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a0 -4 0 $a1 2 3]
    epsilonDiff [$ar getrange 0] [list 0.0003 -3829 -3.261]
} {}

####################################################################
test DoubleArrayMath-4.3 {append with one non-empty array, 1 empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a1 2 3 $a0 8 0]
    epsilonDiff [$ar getrange 0] [list 0.0003 -3829 -3.261]
} {}

####################################################################
test DoubleArrayMath-4.4 {append with 2 non-empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath append $a1 3 1 $a2 1 2]
    epsilonDiff [$ar getrange 0] \
    [list -3829 236.1 -36.21]
} {}

####################################################################
test DoubleArrayMath-11.1 {divide} {
    set ar [java::call ptolemy.math.DoubleArrayMath divide $a1 $a2]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    #jdkPrintArray $ar
    epsilonDiff [$ar getrange 0] [list 0.000766648709129 -0.0279542566709 \
     -8.2850041425e-06 -765.8 -0.0498623853211] 
} {}

####################################################################
test DoubleArrayMath-11.2 {divide with unequally sized arrays} {
    catch {set ar [java::call ptolemy.math.DoubleArrayMath divide $a1 $b1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.divide() : input arrays must have the same length, but the first array has length 5 and the second array has length 3.}}

####################################################################
test DoubleArrayMath-12.3 {divide with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath divide $a0 $b0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-11.1 {dotProduct} {
    set r [java::call ptolemy.math.DoubleArrayMath dotProduct $a1 $a2]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    list $r
} -3059.600263  

####################################################################
test DoubleArrayMath-11.2 {dotProduct with unequally sized arrays} {
    catch {set r [java::call ptolemy.math.DoubleArrayMath dotProduct $a1 $b1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.dotProduct() : input arrays must have the same length, but the first array has length 5 and the second array has length 3.}}

####################################################################
test DoubleArrayMath-12.3 {dotProduct with two empty arrays} {
    set r [java::call ptolemy.math.DoubleArrayMath dotProduct $a0 $b0]
    list $r
} 0.0

####################################################################
test DoubleArrayMath-7.1 {limit: empty array} {
    set da0 [java::new {double[]} 0]
    set da2 [java::call ptolemy.math.DoubleArrayMath limit $da0 0 0]
    $da2 getrange 0
} {}

####################################################################
test DoubleArrayMath-7.2 {limit} {
    set l [list 1 2 -3 4.1 0.0 -0.0 +0.0 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    [java::field java.lang.Double NEGATIVE_INFINITY] \
	    [java::field java.lang.Double NaN] \
	    [java::field java.lang.Double MIN_VALUE] \
	    [java::field java.lang.Double MAX_VALUE] \
	    ]
    set da3 [java::new {double[]} [llength $l] $l]

    set da2 [java::call ptolemy.math.DoubleArrayMath limit $da3 -0.5 1.25]
    $da2 getrange 0
} {1.0 1.25 -0.5 1.25 0.0 -0.0 0.0 1.25 -0.5 NaN 4.94065645841e-324 1.25}

####################################################################
test DoubleArrayMath-7.3 {limit: no bottom} {
    set da2 [java::call ptolemy.math.DoubleArrayMath limit \
	    $da3 \
	    [java::field java.lang.Double NEGATIVE_INFINITY] \
	    1.25]
    $da2 getrange 0
} {1.0 1.25 -3.0 1.25 0.0 -0.0 0.0 1.25 -Infinity NaN 4.94065645841e-324 1.25}


####################################################################
test DoubleArrayMath-7.4 {limit: no top} {
    set da2 [java::call ptolemy.math.DoubleArrayMath limit \
	    $da3 \
	    -0.5 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    ]
    $da2 getrange 0
} {1.0 2.0 -0.5 4.1 0.0 -0.0 0.0 Infinity -0.5 NaN 4.94065645841e-324 1.79769313486e+308}

####################################################################
test DoubleArrayMath-7.5 {limit: bottom greater than top} {
    set da2 [java::call ptolemy.math.DoubleArrayMath limit \
	    $da3 \
	    1.25 \
            -0.5 \
	    ]
    $da2 getrange 0
} {-0.5 -0.5 1.25 -0.5 -0.5 -0.5 -0.5 -0.5 1.25 NaN -0.5 -0.5}

####################################################################
test DoubleArrayMath-7.6 {limit: Infinity top} {
    set da2 [java::call ptolemy.math.DoubleArrayMath limit \
	    $da3 \
	    -0.5 \
	    [java::field java.lang.Double POSITIVE_INFINITY] \
	    ]
    $da2 getrange 0
} {1.0 2.0 -0.5 4.1 0.0 -0.0 0.0 Infinity -0.5 NaN 4.94065645841e-324 1.79769313486e+308}

####################################################################
test DoubleArrayMath-11.1 {multiply} {
    set ar [java::call ptolemy.math.DoubleArrayMath multiply $a1 $a2]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    jdkPrintArray $ar
} {17856.94 -1558.26 -0.010863 -19145.0 -213.2694}

####################################################################
test DoubleArrayMath-11.2 {multiply with unequally sized arrays} {
    catch {set ar [java::call ptolemy.math.DoubleArrayMath multiply $a1 $b1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.multiply() : input arrays must have the same length, but the first array has length 5 and the second array has length 3.}}

####################################################################
test DoubleArrayMath-12.3 {multiply with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath multiply $a0 $b0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-13.1 {padMiddle of empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $a0 5]
    jdkPrintArray $ar
} {0.0 0.0 0.0 0.0 0.0}

####################################################################
test DoubleArrayMath-13.2 {padMiddle to smaller size} {
    catch {set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $a1 4]} \
    errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.padMiddle() : newLength must be >= length of array.}}

####################################################################
test DoubleArrayMath-13.3 {padMiddle to same size} {
    set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $a1 5]
    epsilonDiff [$ar getrange 0] {3.7 -6.6 0.0003 -3829 -3.261}
} {}


####################################################################
test DoubleArrayMath-13.4 {padMiddle odd -> even} {
    set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $a1 8]
    epsilonDiff [$ar getrange 0] {3.7 -6.6 0.0003 0.0 0.0 0.0003 -3829 -3.261}
} {}

####################################################################
test DoubleArrayMath-13.5 {padMiddle odd -> odd} {
    set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $a1 9]
    epsilonDiff [$ar getrange 0] {3.7 -6.6 0.0003 0.0 0.0 0.0 0.0003 -3829 -3.261}
} {}

####################################################################
test DoubleArrayMath-13.6 {padMiddle even -> even} {
    set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $e1 8]
    epsilonDiff [$ar getrange 0] {-62.3 0.332 0.0 0.0 0.0 0.0 5.22 -0.03}
} {}

####################################################################
test DoubleArrayMath-13.7 {padMiddle even -> odd} {
    set ar [java::call ptolemy.math.DoubleArrayMath padMiddle $e1 9]
    epsilonDiff [$ar getrange 0] {-62.3 0.332 0.0 0.0 0.0 0.0 0.0 5.22 -0.03}
} {}

####################################################################
test DoubleArrayMath-13.1 {resize int of empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a0 5]
    jdkPrintArray $ar
} {0.0 0.0 0.0 0.0 0.0}

####################################################################
test DoubleArrayMath-13.2 {resize int truncation of end of array} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a1 3]
    epsilonDiff [$ar getrange 0] {3.7 -6.6 0.0003}
} {}

####################################################################
test DoubleArrayMath-13.3 {resize int padding of end of array} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a1 7]
    epsilonDiff [$ar getrange 0] {3.7 -6.6 0.0003 -3829 -3.261 0.0 0.0}
} {}

####################################################################
test DoubleArrayMath-13.4 {resize int empty output} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a1 0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-14.1 {resize int int of empty array} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a0 5 4]
    jdkPrintArray $ar
} {0.0 0.0 0.0 0.0 0.0}

####################################################################
test DoubleArrayMath-14.2 {resize int int truncation of end of array} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a1 3 1]
    epsilonDiff [$ar getrange 0] {-6.6 0.0003 -3829}
} {}

####################################################################
test DoubleArrayMath-14.3 {resize int int begin trunc padding of end} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a1 7 2]
    epsilonDiff [$ar getrange 0] {0.0003 -3829 -3.261 0.0 0.0 0.0 0.0}
} {}

####################################################################
test DoubleArrayMath-13.4 {resize int int empty output} {
    set ar [java::call ptolemy.math.DoubleArrayMath resize $a1 0 -3]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-12.1 {subtract} {
    set ar [java::call ptolemy.math.DoubleArrayMath subtract $a1 $a2]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    jdkPrintArray $ar
} {-4822.5 -242.7 36.2103 -3834.0 -68.661}


####################################################################
test DoubleArrayMath-12.2 {subtract with unequally sized arrays} {
    catch {set ar [java::call ptolemy.math.DoubleArrayMath subtract $a1 $b1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.subtract() : input arrays must have the same length, but the first array has length 5 and the second array has length 3.}}

####################################################################
test DoubleArrayMath-12.3 {subtract with two empty arrays} {
    set ar [java::call ptolemy.math.DoubleArrayMath subtract $a0 $b0]
    jdkPrintArray $ar
} {}

####################################################################
test DoubleArrayMath-13.1 {toString with two empty array} {
    set s [java::call ptolemy.math.DoubleArrayMath toString $a0]
    list $s
} {{{}}}

####################################################################
test DoubleArrayMath-13.2 {toString with non-empty array} {
    set s [java::call ptolemy.math.DoubleArrayMath toString $a1]
    list $s
} {{{3.7, -6.6, 3.0E-4, -3829.0, -3.261}}}

####################################################################
test DoubleArrayMath-13.1 {within true} {
    set ar [java::new {double[]} 5 [list 3.702 -6.6005 0.0003 -3829.0015 -3.261999]]
    set br [java::call ptolemy.math.DoubleArrayMath {within double[] double[] double} $a1 $ar 0.002]
    list $br
} {1}

####################################################################
test DoubleArrayMath-13.2 {within with unequally sized arrays} {
    catch {set br [java::call ptolemy.math.DoubleArrayMath {within double[] double[] double} $b1 $a1 0.002]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.DoubleArrayMath.within() : input arrays must have the same length, but the first array has length 3 and the second array has length 5.}}
