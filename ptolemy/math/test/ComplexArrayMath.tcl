# Tests for the ComplexArrayMath Class
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

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


# Complex numbers to be used
set c1 [java::new ptolemy.math.Complex 1.0 2.0]
set c2 [java::new ptolemy.math.Complex 3.0 -4.0]
set c3 [java::new ptolemy.math.Complex -4.9 -6.0]
set c4 [java::new ptolemy.math.Complex -7.0 8.0]
set c5 [java::new ptolemy.math.Complex -0.25 0.4]

# Complex array of length 0
set ca0 [java::new {ptolemy.math.Complex[]} 0]

# Complex array
set ca1 [java::new {ptolemy.math.Complex[]} 4 [list $c1 $c2 $c3 $c4]]
set ca3 [java::new {ptolemy.math.Complex[]} 4 [list $c3 $c2 $c4 $c1]]
set ca5 [java::new {ptolemy.math.Complex[]} 5 [list $c1 $c2 $c3 $c4 $c5]]
# ca2 is a Complex array used to store the results of tests


# Double array
set da0 [java::new {double[]} 0]
set da1 [java::new {double[]} 4 [list 1.0 3.0 -4.9 -7.0]]
set da2 [java::new {double[]} 4 [list 2.0 -4.0 -6.0 8.0]]
set da5 [java::new {double[]} 5 [list 0.0 0.1 0.2 0.3 0.4]]

####################################################################
test ComplexArrayMath-1.1 {add} {
    set ca2 [java::call ptolemy.math.ComplexArrayMath add $ca0 $c1]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    jdkPrintArray $ca2
} {}

####################################################################
test ComplexArrayMath-1.2 {add} {
    set ca2 [java::call ptolemy.math.ComplexArrayMath add $ca1 $ca3]
    epsilonDiff [jdkPrintArray $ca2] {{-3.9 - 4.0i} {6.0 - 8.0i} {-11.9 + 2.0i} {-6.0 + 10.0i}}
} {}

####################################################################
test ComplexArrayMath-3.1 {append with two empty arrays} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca0 $ca0]
    jdkPrintArray $ar
} {}

####################################################################
test ComplexArrayMath-3.2 {append with 1 empty array, one non-empty array} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca0 $ca1]
    epsilonDiff [jdkPrintArray $ar] [jdkPrintArray $ca1]
} {}

####################################################################
test ComplexArrayMath-3.3 {append with one non-empty array, 1 empty array} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca1 $ca0]
    epsilonDiff [jdkPrintArray $ar] [jdkPrintArray $ca1]
} {}

####################################################################
test ComplexArrayMath-3.4 {append with 2 non-empty arrays} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca1 $ca3]
    epsilonDiff [jdkPrintArray $ar] \
    {{1.0 + 2.0i} {3.0 - 4.0000i} {-4.9 - 6.0i} {-7.0 + 8.0i} {-4.9 - 6.0i} {3.0 - 4.0i} {-7.0 + 8.0i} {1.0 + 2.0i}}
} {}

####################################################################
test ComplexArrayMath-4.1 {append with two empty arrays} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca0 -4 0 $ca0 8 0]
    jdkPrintArray $ar
} {}

####################################################################
test ComplexArrayMath-4.2 {append with 1 empty array, one non-empty array} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca0 -4 0 $ca1 1 3]
    epsilonDiff [jdkPrintArray $ar] {{3.0 - 4.0i} {-4.9 - 6.0i} {-7.0 + 8.0i}}
} {}

####################################################################
test ComplexArrayMath-4.3 {append with one non-empty array, 1 empty array} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca1 1 3 $ca0 8 0]
    epsilonDiff [jdkPrintArray $ar] {{3.0 - 4.0i} {-4.9 - 6.0i} {-7.0 + 8.0i}} 
} {}

####################################################################
test ComplexArrayMath-4.4 {append with 2 non-empty arrays} {
    set ar [java::call ptolemy.math.ComplexArrayMath append $ca1 3 1 $ca3 1 2]
    epsilonDiff [jdkPrintArray $ar] \
     {{-7.0 + 8.0i} {3.0 - 4.0i} {-7.0 + 8.0i}}    
} {}

####################################################################
test ComplexArrayMath-3.1 {conjugate empty argument} {
    set ca2 [java::call ptolemy.math.ComplexArrayMath conjugate $ca0]
    jdkPrintArray $ca2
} {}

####################################################################
test ComplexArrayMath-3.2 {conjugate} {
    set ca2 [java::call ptolemy.math.ComplexArrayMath conjugate $ca1]
    epsilonDiff [jdkPrintArray $ca2] {{1.0 - 2.0i} {3.0 + 4.0i} {-4.9 + 6.0i} {-7.0 - 8.0i}}
} {}

####################################################################
test ComplexArrayMath-4.1 {formComplexArray empty arguments} {
    set car [java::call ptolemy.math.ComplexArrayMath formComplexArray $da0 \
     $da0]
    jdkPrintArray $car
} {}

####################################################################
test ComplexArrayMath-4.2 {formComplexArray unequally sized arguments} {
    catch {set car [java::call ptolemy.math.ComplexArrayMath formComplexArray \
     $da1 $da5]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.ComplexArrayMath.formComplexArray() : input arrays must have the same length, but the first array has length 4 and the second array has length 5.}}

####################################################################
test ComplexArrayMath-4.3 {formComplexArray normal arguments} {
    set car [java::call ptolemy.math.ComplexArrayMath formComplexArray $da1 \
     $da2]
    epsilonDiff [jdkPrintArray $ca1] [jdkPrintArray $car]
} {}

####################################################################
test ComplexArrayMath-5.1 {imagParts empty array} {
    set result [java::call ptolemy.math.ComplexArrayMath imagParts $ca0]
    jdkPrintArray $result
} {} 

####################################################################
test ComplexArrayMath-5.2 {imagParts} {
    set result [java::call ptolemy.math.ComplexArrayMath imagParts $ca1]
    epsilonDiff [$result getrange 0] [$da2 getrange 0]
} {} 

####################################################################
test ComplexArrayMath-13.1 {padMiddle of empty array} {
    set ar [java::call ptolemy.math.ComplexArrayMath padMiddle $ca0 5]
    jdkPrintArray $ar
} {{0.0 + 0.0i} {0.0 + 0.0i} {0.0 + 0.0i} {0.0 + 0.0i} {0.0 + 0.0i}}

####################################################################
test ComplexArrayMath-13.2 {padMiddle to smaller size} {
    catch {set ar [java::call ptolemy.math.ComplexArrayMath padMiddle $ca1 3]}\
    errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.ComplexArrayMath.padMiddle() : newLength must be >= length of array.}}

####################################################################
test ComplexArrayMath-13.3 {padMiddle to same size} {
    set ar [java::call ptolemy.math.ComplexArrayMath padMiddle $ca1 4]
    epsilonDiff [$ar getrange 0] [$ca1 getrange 0]
} {}


####################################################################
test ComplexArrayMath-13.4 {padMiddle odd -> even} {
    set ar [java::call ptolemy.math.ComplexArrayMath padMiddle $ca5 8]
    epsilonDiff [jdkPrintArray $ar] {{1.0 + 2.0i} {3.0 - 4.0i} {-4.9 - 6.0i} \
    {0.0 + 0.0i} {0.0 + 0.0i} {-4.9 - 6.0i} {-7.0 + 8.0i} {-0.25 + 0.4i}}
} {}

####################################################################
test ComplexArrayMath-13.5 {padMiddle even -> even} {
    set ar [java::call ptolemy.math.ComplexArrayMath padMiddle $ca1 8]
    epsilonDiff [jdkPrintArray $ar] {{1.0 + 2.0i} {3.0 - 4.0i} {0.0 + 0.0i} \
    {0.0 + 0.0i} {0.0 + 0.0i} {0.0 + 0.0i} {-4.9 - 6.0i} {-7.0 + 8.0i}}    
} {}


####################################################################
test ComplexArrayMath-9.0 {polynomial: null array} {
    set ca2 [java::call ptolemy.math.ComplexArrayMath polynomial $ca0]
    jdkPrintArray $ca2
} {{1.0 + 0.0i}}

####################################################################
test ComplexArrayMath-9.1 {polynomial } {
    # FIXME: we need some better input data here
    set ca2 [java::call ptolemy.math.ComplexArrayMath polynomial $ca1]
    epsilonDiff [jdkPrintArray $ca2] \
	    {{1.0 + 0.0i} {7.9 + 0.0i} {49.7 + 36.6i} \
	    {-199.9 + 155.2i} {899.7 + 195.4i}}

} {}

####################################################################
test ComplexArrayMath-9.1 {polynomial: array of length 1 } {
    set ca2 [java::new {ptolemy.math.Complex[]} 1 [list $c1]]
    epsilonDiff [jdkPrintArray $ca2] {{1.0 + 2.0i}}
} {}

####################################################################
test ComplexArrayMath-10.1 {product: empty array} {
    set result [java::call ptolemy.math.ComplexArrayMath product $ca0]
    $result toString
} {0.0 + 0.0i}

####################################################################
test ComplexArrayMath-11.2 {product} {
    set result [java::call ptolemy.math.ComplexArrayMath product $ca1]
    epsilonDiff [$result toString] {899.7 + 195.4i}
} {} 

####################################################################
test ComplexArrayMath-14.1 {realParts empty array} {
    set result [java::call ptolemy.math.ComplexArrayMath realParts $ca0]
    jdkPrintArray $result
} {} 

####################################################################
test ComplexArrayMath-14.2 {realParts} {
    set result [java::call ptolemy.math.ComplexArrayMath realParts $ca1]
    epsilonDiff [$result getrange 0] [$da1 getrange 0]
} {} 

####################################################################
test ComplexArrayMath-15.1 {scale empty array} {
    set result [java::call ptolemy.math.ComplexArrayMath scale $ca0 -3.2]
    jdkPrintArray $result
} {}

####################################################################
test ComplexArrayMath-15.1 {scale normal array} {
    set result [java::call ptolemy.math.ComplexArrayMath scale $ca1 3.2]
    epsilonDiff [jdkPrintArray $result] {{3.2 + 6.4i} {9.6 - 12.80i} {-15.68 - 19.2i} {-22.4 + 25.6i}}
} {}

####################################################################
test ComplexArrayMath-12.2 {subtract} {
    set ca2 [java::call ptolemy.math.ComplexArrayMath subtract $ca1 $ca3]
    epsilonDiff [jdkPrintArray $ca2] {{5.9 + 8.0i} {0.0 + 0.0i} {2.1 - 14.0i} {-8.0 + 6.0i}}
} {}

####################################################################
test ComplexArrayMath-13.1 {magnitude: empty array} {
    set da2 [java::call ptolemy.math.ComplexArrayMath magnitude $ca0]
    $da2 getrange 0 
} {}

####################################################################
test ComplexArrayMath-13.2 {magnitude} {
    set da2 [java::call ptolemy.math.ComplexArrayMath magnitude $ca1]
    epsilonDiff [$da2 getrange 0] \
	    {2.2360679775 5.0 7.74661216275 10.6301458127}
} {}

####################################################################
test ComplexArrayMath-14.1 {phase: empty array} {
    set da2 [java::call ptolemy.math.ComplexArrayMath phase $ca0]
    $da2 getrange 0 
} {}

####################################################################
test ComplexArrayMath-14.2 {phase} {
    set da2 [java::call ptolemy.math.ComplexArrayMath phase $ca1]
    epsilonDiff [$da2 getrange 0] \
	    {1.10714871779 -0.927295218002 -2.25561757274 2.28962632642}
} {}
