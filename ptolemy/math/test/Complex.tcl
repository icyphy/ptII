# Tests for the Complex Class
#
# @Author: Edward A. Lee, Christopher Hylands
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test Complex-1.1 {constructors} {
    set c0 [java::new ptolemy.math.Complex]
    set c1 [java::new ptolemy.math.Complex -0.5]
    set c2 [java::new ptolemy.math.Complex 2.0 -3.0]
    list "[$c0 toString]\n[$c1 toString]\n[$c2 toString]"
} {{0.0 + 0.0i
-0.5 + 0.0i
2.0 - 3.0i}}

####################################################################
test Complex-2.1 {mag} {
    list "[$c0 mag]\n[$c1 mag]\n[$c2 mag]"
} {{0.0
0.5
3.60555127546}}

####################################################################
test Complex-2.3 {add} {
    set c22 [java::new ptolemy.math.Complex -1.1 -1.1]
    set c23 [$c2 add $c22]
    set result [$c23 add $c22]
    $result toString
} {-0.20000000000000018 - 5.199999999999999i}

####################################################################
test Complex-2.4 {acos} {
    set c9 [$c2 acos]
    epsilonDiff [$c9 toString] {1.0001435424737994 + 1.983387029916533i}
} {}

####################################################################
test Complex-2.5 {acosh} {
    set c9 [$c2 acosh]
    $c9 toString
} {1.9833870299165355 - 1.0001435424737972i}

####################################################################
test Complex-3.1 {angle} {
    list "[$c0 angle]\n[$c1 angle]\n[$c2 angle]"
} {{0.0
3.14159265359
-0.982793723247}}

####################################################################
test Complex-3.1.1 {asin} {
    set c9 [$c2 asin]
    $c9 toString
} {0.5706527843210994 - 1.9833870299165355i}

####################################################################
test Complex-3.1.2 {asinh} {
    set c9 [$c2 asinh]
    epsilonDiff [$c9 toString] {1.9686379257930964 - 0.9646585044076028i}
} {}

####################################################################
test Complex-3.1.3 {atan} {
    set c9 [$c2 atan]
    $c9 toString
} {1.4099210495965755 - 0.2290726829685388i}

####################################################################
test Complex-3.1.4 {atanh} {
    set c9 [$c2 atanh]
    $c9 toString
} {0.14694666622552977 - 1.3389725222944935i}

####################################################################
test Complex-3.2 {csc} {
    set c9 [$c2 csc]
    epsilonDiff [$c9 toString] {0.09047320975320743 - 0.04120098628857413i}
} {}

####################################################################
test Complex-4.1 {conjugate} {
    set c0p [$c0 conjugate]
    set c1p [$c1 conjugate]
    set c2p [$c2 conjugate]
    list "[$c0p toString]\n[$c1p toString]\n[$c2p toString]"
} {{0.0 + 0.0i
-0.5 + 0.0i
2.0 + 3.0i}}

####################################################################
test Complex-5.1.0 {cot} {
    set c9 [$c2 cot]
    epsilonDiff [$c9 toString] {-0.0037397103763367905 + 0.9967577965693585i}
} {}

####################################################################
test Complex-5.1.1 {cos} {
    set c9 [$c2 cos]
    epsilonDiff [$c9 toString] {-4.189625690968808 + 9.109227893755339i}
} {}

####################################################################
test Complex-5.1.2 {cosh} {
    set c9 [$c2 cosh]
    $c9 toString
} {-3.724545504915323 - 0.5118225699873846i}

####################################################################
test Complex-5.2 {divide} {
    set div [$c2 divide $c2p]
    $div toString
} {-0.38461538461538464 - 0.9230769230769231i}

####################################################################
test Complex-5.2.5 {equals} {
    # Copy the values of c2
    set ct4 [java::new ptolemy.math.Complex \
	    [java::field $c2 real] \
	    [java::field $c2 imag]]
    list \
	    [$c0 {equals ptolemy.math.Complex} $c0] \
	    [$c1 {equals ptolemy.math.Complex} $c1] \
	    [$c2 {equals ptolemy.math.Complex} $c2] \
	    [$c2 {equals ptolemy.math.Complex} $c1] \
	    [$c2 {equals ptolemy.math.Complex} $ct4]
} {1 1 1 0 1}

####################################################################
test Complex-5.3.1 {exp} {
    set ec2 [$c2 exp]
    $ec2 toString
} {-7.315110094901103 - 1.0427436562359045i}

####################################################################
test Complex-5.4 {isNaN} {
    set inf [$c2 divide $c0]
    list [$c2 isNaN] [$inf isNaN]
} {0 1}

####################################################################
test Complex-5.4.1 {log} {
    set lc2 [$c2 log]
    $lc2 toString
} {1.2824746787307684 - 0.982793723247329i}

####################################################################
test Complex-5.5 {multiply} {
    set c10 [java::new ptolemy.math.Complex -1.1 -1.1]
    set c8 [$c2 multiply $c10]
    $c8 toString
} {-5.5 + 1.1i}

####################################################################
test Complex-6.1 {negate} {
    set c0p [$c0 negate]
    set c1p [$c1 negate]
    set c2p [$c2 negate]
    list "[$c0p toString]\n[$c1p toString]\n[$c2p toString]"
} {{0.0 + 0.0i
0.5 + 0.0i
-2.0 + 3.0i}}

####################################################################
test Complex-8.1 {polarToComplex} {
    set c3 [java::call ptolemy.math.Complex polarToComplex 1.0 [expr $PI/2]]
    set c4 [java::call ptolemy.math.Complex polarToComplex 0.0 [expr $PI/2]]
    set c5 [java::call ptolemy.math.Complex polarToComplex -1.0 [expr -$PI]]
    list [epsilonDiff [$c3 toString] {-5.1034119692792285E-12 + 1.0i}] \
	    [epsilonDiff [$c4 toString] {0.0 + 0.0i}] \
	    [epsilonDiff [$c5 toString] {1.0 - 2.0694557179012918E-13i}]
} {{} {} {}}

####################################################################
test Complex-9.1 {pow} {
    set c6 [java::new ptolemy.math.Complex -0.5 0.9]
    set c7 [$c2 pow $c6]
    $c7 toString
} {-0.09534790752229648 + 1.2718528818533663i}

####################################################################
test Complex-10.1 {reciprocal} {
    set c8 [$c2 reciprocal]
    $c8 toString
} {0.15384615384615385 + 0.23076923076923078i}

####################################################################
test Complex-11.1 {scale} {
    set c8 [$c2 scale 2.5]
    $c8 toString
} {5.0 - 7.5i}

####################################################################
test Complex-11.2.0 {sec} {
    set c9 [$c2 sec]
    epsilonDiff [$c9 toString] {-0.04167496441114425 - 0.09061113719623758i}
} {}

####################################################################
test Complex-11.2.1 {sin} {
    set c9 [$c2 sin]
    epsilonDiff [$c9 toString] {9.15449914691143 + 4.168906959966566i} 
} {}

####################################################################
test Complex-11.2.2 {sinh} {
    set c9 [$c2 sinh]
    $c9 toString
} {-3.59056458998578 - 0.5309210862485199i}

####################################################################
test Complex-11.3 {sqrt} {
    set c8 [$c2 sqrt]
    $c8 toString
} {1.6741492280355401 - 0.895977476129838i}

####################################################################
test Complex-12.1 {subtract} {
    set c22 [java::new ptolemy.math.Complex -1.1 -1.1]
    set c23 [$c2 subtract $c22]
    $c23 toString
} {3.1 - 1.9i}

####################################################################
test Complex-13.1 {tan} {
    set c9 [$c2 tan]
    epsilonDiff [$c9 toString] {-0.0037640256415040793 - 1.0032386273536096i}
} {}

####################################################################
test Complex-14.1 {tanh} {
    set c9 [$c2 tanh]
    $c9 toString
} {0.965385879022133 + 0.009884375038322507i}

####################################################################
test Complex-15.1 {isInfinite} {
    set negativeInfinity [java::field java.lang.Double NEGATIVE_INFINITY]
    set complexNegativeInfinity \
	    [java::new ptolemy.math.Complex $negativeInfinity]
    set complexImaginaryNegativeInfinity \
	    [java::new ptolemy.math.Complex 0.0 $negativeInfinity]

    set positiveInfinity [java::field java.lang.Double POSITIVE_INFINITY]
    set complexPositiveInfinity \
	    [java::new ptolemy.math.Complex $positiveInfinity]

    set complexImaginaryPositiveInfinity \
	    [java::new ptolemy.math.Complex 0.0 $positiveInfinity]

    set minValue [java::field java.lang.Double MIN_VALUE]
    set complexMinValue \
	    [java::new ptolemy.math.Complex $minValue]

    set maxValue [java::field java.lang.Double MAX_VALUE]
    set complexMaxValue \
	    [java::new ptolemy.math.Complex $maxValue]


    list [$complexNegativeInfinity isInfinite] \
	    [$complexImaginaryNegativeInfinity isInfinite] \
	    [$complexPositiveInfinity isInfinite] \
	    [$complexImaginaryPositiveInfinity isInfinite] \
	    [$complexMinValue isInfinite] \
	    [$complexMaxValue isInfinite]


} {1 1 1 1 0 0}
