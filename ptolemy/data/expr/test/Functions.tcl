# Tests for function evaluation in expressions
#
# @Author: Edward A. Lee and Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

proc evaluate {root} {
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]
    $evaluator evaluateParseTree $root
}

proc theTest {expression} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    [evaluate $root] toString
}

# Call ptclose on the results.
# Use this proc if the results are slightly different under Solaris 
# and Windows
proc theTestPtClose {expression results} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    ptclose [[evaluate $root] toString] $results
}

####################################################################
# acos

test Function-acos {Test acos} {
    list [theTestPtClose {acos(0.0)} 1.5707963267949] \
         [theTest {acos(1.0)}] \
         [theTest {acos(1)}] \
         [theTest {acos(1ub)}] \
         [theTestPtClose {acos(1.0+0.0i)} {0.0 + 0.0i}] \
     } {1 0.0 0.0 0.0 1}

####################################################################
# asin

test Function-asin {Test asin} {
    list [theTestPtClose {asin(1.0)} 1.5707963267949] \
         [theTest {asin(0.0)}] \
         [theTest {asin(0)}] \
         [theTest {asin(0ub)}] \
         [theTestPtClose {asin(1.0+0.0i)} {1.5707963267949 + 0.0i}] \
     } {1 0.0 0.0 0.0 1}

####################################################################
# atan

test Function-atan {Test atan} {
    list [theTestPtClose {atan(1.0)} 0.7853981633974] \
         [theTestPtClose {atan(-1)} -0.7853981633974] \
         [theTest {atan(0ub)}] \
         [theTestPtClose {atan(Infinity)} 1.5707963267949] \
         [theTestPtClose {atan(0.0+0.0i)} {0.0 + 0.0i}]
     } {1 1 0.0 1 1}

####################################################################
# atan2

test Function-atan2 {Test atan2} {
    list [theTestPtClose {atan2(1.0, 1.0)} 0.7853981633974] \
         [theTestPtClose {atan2(-1, 1)} -0.7853981633974] \
         [theTest {atan2(0ub, 1ub)}] \
         [theTestPtClose {atan2(Infinity, 1.0)} 1.5707963267949] \
     } {1 1 0.0 1}

####################################################################
# acosh

test Function-acosh {Test acosh} {
    list [theTestPtClose {acosh(1.0)} 0.0] \
         [theTestPtClose {acosh(1)} 0.0] \
         [theTestPtClose {acosh(1ub)} 0.0] \
         [theTestPtClose {acosh(1.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

####################################################################
# asinh

test Function-asinh {Test asinh} {
    list [theTestPtClose {asinh(0.0)} 0.0] \
         [theTestPtClose {asinh(0)} 0.0] \
         [theTestPtClose {asinh(0ub)} 0.0] \
         [theTestPtClose {asinh(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

test Function-asinh-2 {Test asinh} {
    list [theTestPtClose {asinh(1+i)} \
	{1.0612750619050355 + 0.6662394324925153i}] \
         [theTestPtClose {asinh({1+i, 1-i})} \
	{{1.0612750619050355 + 0.6662394324925153i, 1.0612750619050355 - 0.6662394324925153i}}] \
	[theTestPtClose {asinh([1+i, 1-i])} \
	{[1.0612750619050357 + 0.6662394324925153i, 1.0612750619050357 - 0.6662394324925153i]}]
} {1 1 1}

####################################################################
# cos

test Function-cos {Test cos} {
    list [theTestPtClose {cos(0.0)} 1.0] \
         [theTestPtClose {cos(0)} 1.0] \
         [theTestPtClose {cos(0ub)} 1.0] \
         [theTestPtClose {cos(0.0+0.0i)} {1.0 + 0.0i}] \
     } {1 1 1 1}

test Function-cos-2 {Test cos} {
    list [theTest {cos(1+i)}] \
         [theTest {cos({1+i, 1-i})}] \
         [theTest {cos([1+i, 1-i])}]
} {{0.8337300251311491 - 0.9888977057628653i} {{0.8337300251311491 - 0.9888977057628653i, 0.8337300251311491 + 0.9888977057628653i}} {[0.8337300251311491 - 0.9888977057628653i, 0.8337300251311491 + 0.9888977057628653i]}}

####################################################################
# cosh

test Function-cosh {Test cosh} {
    list [theTestPtClose {cosh(0.0)} 1.0] \
         [theTestPtClose {cosh(0)} 1.0] \
         [theTestPtClose {cosh(0ub)} 1.0] \
         [theTestPtClose {cosh(0.0+0.0i)} {1.0 + 0.0i}] \
     } {1 1 1 1}

 test Function-cosh-2 {Test cosh} {
    list [theTest {cosh(1+i)}] \
         [theTest {cosh({1+i, 1-i})}] \
         [theTest {cosh([1+i, 1-i])}]
 } {{0.8337300251311491 + 0.9888977057628653i} {{0.8337300251311491 + 0.9888977057628653i, 0.8337300251311491 - 0.9888977057628653i}} {[0.8337300251311491 + 0.9888977057628653i, 0.8337300251311491 - 0.9888977057628653i]}}

####################################################################
# sin

test Function-sin {Test sin} {
    list [theTestPtClose {sin(0.0)} 0.0] \
         [theTestPtClose {sin(0)} 0.0] \
         [theTestPtClose {sin(0ub)} 0.0] \
         [theTestPtClose {sin(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

test Function-sin-2 {Test sin} {
    list [theTest {sin(1+i)}] \
         [theTest {sin({1+i, 1-i})}] \
         [theTest {sin([1+i, 1-i])}]
} {{1.2984575814159776 + 0.6349639147847362i} {{1.2984575814159776 + 0.6349639147847362i, 1.2984575814159776 - 0.6349639147847362i}} {[1.2984575814159776 + 0.6349639147847362i, 1.2984575814159776 - 0.6349639147847362i]}}

####################################################################
# sinh

test Function-sinh {Test sinh} {
    list [theTestPtClose {sinh(0.0)} 0.0] \
         [theTestPtClose {sinh(0)} 0.0] \
         [theTestPtClose {sinh(0ub)} 0.0] \
         [theTestPtClose {sinh(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

 test Function-sinh-2 {Test sinh} {
    list [theTest {sinh(1+i)}] \
         [theTest {sinh({1+i, 1-i})}] \
         [theTest {sinh([1+i, 1-i])}]
 } {{0.6349639147847362 + 1.2984575814159776i} {{0.6349639147847362 + 1.2984575814159776i, 0.6349639147847362 - 1.2984575814159776i}} {[0.6349639147847362 + 1.2984575814159776i, 0.6349639147847362 - 1.2984575814159776i]}}

####################################################################
# tan

test Function-tan {Test tan} {
    list [theTestPtClose {tan(0.0)} 0.0] \
         [theTestPtClose {tan(pi)} 0.0] \
         [theTestPtClose {tan(0)} 0.0] \
         [theTestPtClose {tan(0ub)} 0.0] \
         [theTestPtClose {tan(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1 1}

 test Function-tan-2 {Test tan} {
    list [theTest {tan(1+i)}] \
         [theTest {tan({1+i, 1-i})}] \
         [theTest {tan([1+i, 1-i])}]
 } {{0.27175258531951163 + 1.0839233273386946i} {{0.27175258531951163 + 1.0839233273386946i, 0.27175258531951163 - 1.0839233273386946i}} {[0.27175258531951163 + 1.0839233273386946i, 0.27175258531951163 - 1.0839233273386946i]}}

####################################################################
# tanh

test Function-tanh {Test tanh} {
    list [theTestPtClose {tanh(0.0)} 0.0] \
         [theTestPtClose {tanh(0)} 0.0] \
         [theTestPtClose {tanh(0ub)} 0.0] \
         [theTestPtClose {tanh(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

 test Function-tanh {Test tanh} {
    list [theTest {tanh(1+i)}] \
         [theTest {tanh({1+i, 1-i})}] \
         [theTest {tanh([1+i, 1-i])}]
 } {{1.0839233273386946 + 0.27175258531951163i} {{1.0839233273386946 + 0.27175258531951163i, 1.0839233273386946 - 0.27175258531951163i}} {[1.0839233273386946 + 0.27175258531951163i, 1.0839233273386946 - 0.27175258531951163i]}}

####################################################################
####################################################################
####################################################################
####################################################################
# New table in the docs:

####################################################################
# abs

test Function-abs {Test abs on scalars} {
    list [theTestPtClose {abs(1+i)} 1.4142135623731] \
         [theTest {abs(-1.0)}] \
         [theTest {abs(-1)}] \
         [theTest {abs(1ub)}] \
         [theTest {abs(-1L)}] \
         [theTest {abs(1ub)}] \
} {1 1.0 1 1 1L 1}

test Function-abs-2 {Test abs on arrays} {
    list [theTestPtClose {abs({1+i, 1-i})} {{1.4142135623731, 1.4142135623731}}] \
         [theTest {abs({-1.0, -2.0})}] \
         [theTest {abs({-1, -2})}] \
         [theTest {abs({-1L, -2L})}] \
     } {1 {{1.0, 2.0}} {{1, 2}} {{1L, 2L}}}

test Function-abs-3 {Test abs on matrices} {
    list [theTestPtClose {abs([1+i, 1-i])} \
                  {[1.4142135623731, 1.4142135623731]}] \
         [theTest {abs(-identityDouble(2))}] \
         [theTest {abs(-identityInt(2))}] \
         [theTest {abs(-identityLong(2))}] \
         [theTest {abs(-identityComplex(2))}] \
} {1 {[1.0, 0.0; 0.0, 1.0]} {[1, 0; 0, 1]} {[1L, 0L; 0L, 1L]} {[1.0, 0.0; 0.0, 1.0]}}

####################################################################
# angle

 test Function-angle {Test angle} {
    list [theTestPtClose {angle(1+i)} 0.7853981633974] \
         [theTestPtClose {angle({1+i, 1-i})} \
                 {{0.7853981633974, -0.7853981633974}}] \
         [theTestPtClose {angle([1+i, 1-i])} \
                 {[0.7853981633974, -0.7853981633974]}] \
         [theTestPtClose {angle(i)} {1.5707963267949}] \
         [theTest {angle(0.0i)}] \
 } {1 1 1 1 0.0}


####################################################################
# ceil

test Function-ceil {Test ceil} {
    list [theTest {ceil(-1.1)}] \
         [theTest {ceil(-1)}] \
         [theTest {ceil(1ub)}] \
         [theTest {ceil({1.1, 2.1})}] \
         [theTest {ceil([1.1, 2.1])}] \
     } {-1.0 -1.0 1.0 {{2.0, 3.0}} {[2.0, 3.0]}}

####################################################################
# compare

 test Function-compare {Test compare} {
    list [theTest {compare(1.0, 2.0)}] \
         [theTest {compare(1, 2)}] \
         [theTest {compare(1ub, 2ub)}] \
         [theTest {compare({1, 2}, {3, 4})}] \
         [theTest {compare([1, 2], [3, 4])}] \
     } {-1 -1 -1 {{-1, -1}} {[-1, -1]}}

####################################################################
# conjugate

 test Function-conjugate {Test conjugate} {
    list [theTest {conjugate(1+i)}] \
         [theTest {conjugate({1+i, 1-i})}] \
         [theTest {conjugate([1+i, 1-i])}] \
         [theTestPtClose {conjugate(1.0)} {1.0 + 0.0i}] \
         [theTestPtClose {conjugate(1)} {1.0 + 0.0i}] \
         [theTestPtClose {conjugate(1ub)} {1.0 + 0.0i}] \
 } {{1.0 - 1.0i} {{1.0 - 1.0i, 1.0 + 1.0i}} {[1.0 - 1.0i, 1.0 + 1.0i]} 1 1 1}

####################################################################
# exp

test Function-exp {Test exp} {
    list [theTestPtClose {exp(0.0)} 1.0] \
         [theTestPtClose {exp(-1.0)} 0.3678794411714] \
         [theTestPtClose {exp(0)} 1.0] \
         [theTestPtClose {exp(0ub)} 1.0] \
         [theTestPtClose {exp(0.0+0.0i)} {1.0 + 0.0i}] \
     } {1 1 1 1 1}

####################################################################
# floor

test Function-floor {Test floor} {
    list [theTest {floor(-1.1)}] \
         [theTest {floor(-1)}] \
         [theTest {floor(1ub)}] \
         [theTest {floor({1.1, 2.1})}] \
         [theTest {floor([1.1, 2.1])}] \
     } {-2.0 -1.0 1.0 {{1.0, 2.0}} {[1.0, 2.0]}}

####################################################################
# imag

 test Function-7.1 {Test imag} {
    list [theTest {imag(1+i)}] \
         [theTest {imag({1+i, 1-i})}] \
         [theTest {imag([1+i, 1-i])}] \
         [theTest {imag(1.0)}] \
         [theTest {imag(1)}] \
         [theTest {imag(1ub)}] \
     } {1.0 {{1.0, -1.0}} {[1.0, -1.0]} 0.0 0.0 0.0}

####################################################################
# isInfinite

test Function-isInfinite {Test isInfinite} {
    list [theTest {isInfinite(-1.1)}] \
         [theTest {isInfinite(Infinity)}] \
         [theTest {isInfinite(-Infinity)}] \
         [theTest {isInfinite(-1)}] \
         [theTest {isInfinite(1ub)}] \
         [theTest {isInfinite({1.1, Infinity})}] \
         [theTest {isInfinite([1.1, -Infinity])}] \
     } {false true true false false {{false, true}} {[false, true]}}

####################################################################
# isNaN

test Function-isNaN {Test isNaN} {
    list [theTest {isNaN(-1.1)}] \
         [theTest {isNaN(Infinity)}] \
         [theTest {isNaN(NaN)}] \
         [theTest {isNaN(-1)}] \
         [theTest {isNaN(1ub)}] \
         [theTest {isNaN({1.1, NaN})}] \
         [theTest {isNaN([1.1, -Infinity])}] \
     } {false false true false false {{false, true}} {[false, false]}}

####################################################################
# log

test Function-log {Test log} {
    list [theTestPtClose {log(e)} 1.0] \
         [theTest {log(0.0)}] \
         [theTest {log(1)}] \
         [theTest {log(1ub)}] \
         [theTestPtClose {log(1.0+0.0i)} {0.0 + 0.0i}] \
     } {1 -Infinity 0.0 0.0 1}

####################################################################
# log10

test Function-log10 {Test log10} {
    list [theTestPtClose {log10(10.0)} 1.0] \
         [theTest {log10(0.0)}] \
         [theTest {log10(1)}] \
         [theTest {log10(1ub)}] \
     } {1 -Infinity 0.0 0.0}

####################################################################
# log2

test Function-log2 {Test log2} {
    list [theTestPtClose {log2(2.0)} 1.0] \
         [theTest {log2(0.0)}] \
         [theTest {log2(1)}] \
         [theTest {log2(1ub)}] \
     } {1 -Infinity 0.0 0.0}

####################################################################
# max

test Function-max {Test max} {
    list [theTest {max(1.0, 2.0)}] \
         [theTest {max(0.0, -1.0)}] \
         [theTest {max(1, -1)}] \
         [theTest {max(1ub, 2ub)}] \
         [theTest {max(1L, -1L)}] \
         [theTest {max({1.0, 2.0})}] \
         [theTest {max({1, 2})}] \
         [theTest {max({1L, 2L})}] \
         [theTest {max({1ub, 2ub})}] \
     } {2.0 0.0 1 2ub 1L 2.0 2 2L 2ub}


####################################################################
# min

test Function-min {Test min} {
    list [theTest {min(1.0, 2.0)}] \
         [theTest {min(0.0, -1.0)}] \
         [theTest {min(1, -1)}] \
         [theTest {min(1ub, 2ub)}] \
         [theTest {min(1L, -1L)}] \
         [theTest {min({1.0, 2.0})}] \
         [theTest {min({1, 2})}] \
         [theTest {min({1L, 2L})}] \
         [theTest {min({1ub, 2ub})}] \
     } {1.0 -1.0 -1 1ub -1L 1.0 1 1L 1ub}

####################################################################
# pow

test Function-pow {Test pow} {
    list [theTestPtClose {pow(2.0, 0.0)} 1.0] \
         [theTestPtClose {pow(0.0, -1.0)} Infinity] \
         [theTestPtClose {pow(1, -1)} 1.0] \
         [theTestPtClose {pow(2ub, 2ub)} 4.0] \
         [theTestPtClose {pow({1.0, 2.0}, {-2, -2})} {{1.0, 0.25}}] \
         [theTestPtClose {pow({1, 2}, {2, 2})} {{1.0, 4.0}}] \
         [theTestPtClose {pow({1ub, 2ub}, {2ub, 2ub})} {{1.0, 4.0}}] \
         [theTestPtClose {pow([1.0, 2.0], [-2, -2])} {[1.0, 0.25]}] \
         [theTestPtClose {pow([1, 2], [2, 2])} {[1.0, 4.0]}] \
     } {1 1 1 1 1 1 1 1 1}

test Function-pow2 {Test pow on complex} {
    list [theTestPtClose {pow(i, 2)} {-1.0 + 0.0i}] \
         [theTestPtClose {pow(e, 2*pi*i)} {1.0 + 0.0i}] \
         [theTestPtClose {pow(e+0.0i, 2*pi*i)} {1.0 + 0.0i}] \
     } {1 1 1}

####################################################################
# random

# Tough to test?

####################################################################
# real

 test Function-15.1 {Test real} {
    list [theTest {real(1+i)}] \
         [theTest {real({1+i, 1-i})}] \
         [theTest {real([1+i, 1-i])}] \
         [theTest {real(1.0)}] \
         [theTest {real(1)}] \
         [theTest {real(1ub)}] \
     } {1.0 {{1.0, 1.0}} {[1.0, 1.0]} 1.0 1.0 1.0}

####################################################################
# remainder

test Function-remainder {Test remainder} {
    list [theTestPtClose {remainder(3.0, 2.0)} -1.0] \
         [theTestPtClose {remainder(2.0, 3.0)} -1.0] \
         [theTestPtClose {remainder(-1, 1)} 0.0] \
         [theTestPtClose {remainder(0ub, 1ub)} 0.0] \
         [theTestPtClose {remainder(2.5, 1.0)} 0.5] \
         [theTest {remainder({3, 2}, {2, 2})}] \
         [theTest {remainder([3, 2], [2, 2])}] \
     } {1 1 1 1 1 {{-1.0, 0.0}} {[-1.0, 0.0]}}

####################################################################
# round

test Function-round {Test round} {
    list [theTest {round(1.1)}] \
         [theTest {round(-1.1)}] \
         [theTest {round(NaN)}] \
         [string compare [theTest {round(Infinity)}] [theTest {MaxLong}]] \
         [string compare [theTest {round(-Infinity)}] [theTest {MinLong}]] \
     } {1L -1L 0L 0 0}

####################################################################
# roundToInt

test Function-roundToInt {Test roundToInt} {
    list [theTest {roundToInt(1.1)}] \
         [theTest {roundToInt(-1.1)}] \
         [theTest {roundToInt(NaN)}] \
         [string compare [theTest {roundToInt(Infinity)}] [theTest {MaxInt}]] \
         [string compare [theTest {roundToInt(-Infinity)}] [theTest {MinInt}]] \
     } {1 -1 0 0 0}

####################################################################
# sgn

test Function-sgn {Test sgn} {
    list [theTest {sgn(1.1)}] \
         [theTest {sgn(-1.1)}] \
         [theTest {sgn(0.0)}] \
     } {1 -1 1}

####################################################################
# sqrt

test Function-sqrt {Test sqrt} {
    list [theTest {sqrt(4.0)}] \
         [theTest {sqrt(-1.1)}] \
         [theTest {sqrt(0.0)}] \
         [theTest {sqrt(4.0 + 0.0i)}] \
     } {2.0 NaN 0.0 {2.0 + 0.0i}}


####################################################################
# toDegrees

test Function-toDegrees {Test toDegrees} {
    list [theTest {toDegrees(0.0)}] \
         [theTestPtClose {toDegrees(-pi)} -180] \
     } {0.0 1}

####################################################################
# toRadians

test Function-toRadians {Test toRadians} {
    list [theTest {toRadians(0.0)}] \
         [theTestPtClose {toRadians(-180)} [theTest {-pi}]] \
     } {0.0 1}

####################################################################
####################################################################
####################################################################
####################################################################
####################################################################

####################################################################
# conjugateTranspose

test Function-conjugateTranspose {Test conjugateTranspose} {
    list [theTest {conjugateTranspose([0, i; 0, 0])}] \
        } {{[0.0 + 0.0i, 0.0 + 0.0i; 0.0 - 1.0i, 0.0 + 0.0i]}}

####################################################################
# crop

test Function-crop {Test crop} {
    list [theTest {crop(identityDouble(3), 0, 1, 2, 2)}] \
         [theTest {crop(identityInt(3), 0, 1, 2, 2)}] \
         [theTest {crop(identityComplex(3), 0, 1, 2, 2)}] \
         [theTest {crop(identityLong(3), 0, 1, 2, 2)}] \
} {{[0.0, 0.0; 1.0, 0.0]} {[0, 0; 1, 0]} {[0.0 + 0.0i, 0.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]} {[0L, 0L; 1L, 0L]}}

####################################################################
# determinant

test Function-determinant {Test determinant} {
    list [theTest {determinant(identityDouble(3))}] \
         [theTest {determinant(identityComplex(3))}]
} {1.0 {1.0 + 0.0i}}

####################################################################
# diag

test Function-diag {Test diag} {
    list [theTest {diag({1, 2})}] \
         [theTest {diag({1.0, 2.0})}] \
         [theTest {diag({1.0 + 1.0i, 2.0 + 2.0i})}] \
         [theTest {diag({1L, 2L})}] \
} {{[1, 0; 0, 2]} {[1.0, 0.0; 0.0, 2.0]} {[1.0 + 1.0i, 0.0 + 0.0i; 0.0 + 0.0i, 2.0 + 2.0i]} {[1L, 0L; 0L, 2L]}}


####################################################################
####################################################################
####################################################################
####################################################################
####################################################################
# FIXME: Organize the following

####################################################################
# merge

test Function-merge {Test merge of records} {
    list [theTest {merge({a=1, b=2}, {a=3, c=3})}] \
        } {{{a=1, b=2, c=3}}}



test Function-23.6.5 {Test various function calls: createArray} {
    # FIXME: what about UnsignedByteArray and FixMatrixToken
    list \
	[theTest {createArray([true,false;true,true])}] \
	[theTest {createArray([1,2;3,4])}] \
	[theTest {createArray([1L,2L;3L,4L])}] \
	[theTest {createArray([1.0,2.0;3.0,4.0])}] \
	[theTest {createArray([1.0 + 0i, 2.0 + 1i; 3.0 - 1i, 4.0 + 4i])}]
} {{{true, false, true, true}} {{1, 2, 3, 4}} {{1L, 2L, 3L, 4L}} {{1.0, 2.0, 3.0, 4.0}} {{1.0 + 0.0i, 2.0 + 1.0i, 3.0 - 1.0i, 4.0 + 4.0i}}}


test Function-23.6.6.1 {Test various function calls: createMatrix} {
    # FIXME: what about UnsignedByteArray and FixMatrixToken
    list "[theTest {createMatrix({true,false,true,true,false,false}, 2, 3)}]\n \
	[theTest {createMatrix({1,2,3,4,5,6}, 2, 3)}]\n \
	[theTest {createMatrix({1L,2L,3L,4L,5L,6L}, 2, 3)}]\n \
	[theTest {createMatrix({1.0,2.0,3.0,4.0,5.0,6.0}, 2, 3)}]\n \
	[theTest {createMatrix({1.0 + 0i, 2.0 + 1i, 3.0 - 1i, 4.0 + 4i, 5.0 - 5i, 6.0 - 6.0i}, 2, 3)}]\n \
	[theTest {createMatrix({1ub,2,3.5,4,5,6}, 2, 3)}]"

} {{[true, false, true; true, false, false]
  [1, 2, 3; 4, 5, 6]
  [1L, 2L, 3L; 4L, 5L, 6L]
  [1.0, 2.0, 3.0; 4.0, 5.0, 6.0]
  [1.0 + 0.0i, 2.0 + 1.0i, 3.0 - 1.0i; 4.0 + 4.0i, 5.0 - 5.0i, 6.0 - 6.0i]
  [1.0, 2.0, 3.5; 4.0, 5.0, 6.0]}}

test Function-23.6.6.2 {Test various function calls: createMatrix with an array that is not big enough} {
    catch {theTest {createMatrix({1L,2L,3L,4L}, 2, 3)}} errMsg
    list $errMsg	
} {{ptolemy.kernel.util.IllegalActionException: Error invoking function public static ptolemy.data.MatrixToken ptolemy.data.MatrixToken.createMatrix(ptolemy.data.Token[],int,int) throws ptolemy.kernel.util.IllegalActionException

Because:
LongMatrixToken: The specified array is not of the correct length}}


test Function-23.6.7.1 {Test various function calls: createSequence} {
    # FIXME: what about UnsignedByteArray and FixMatrixToken
    list "[theTest {createSequence(false, false, 5)}]\n \
	[theTest {createSequence(false, true, 5)}]\n \
	[theTest {createSequence(true, false, 5)}]\n \
	[theTest {createSequence(true, true, 5)}]\n \
	[theTest {createSequence(-1, 1, 5)}]\n \
	[theTest {createSequence(-1L, 1L, 5)}]\n \
	[theTest {createSequence(-1.0, 1.0, 5)}]\n \
	[theTest {createSequence(-1.0 - 1i, 1.0 - 1i, 5)}]"
} {{{false, false, false, false, false}
  {false, true, true, true, true}
  {true, true, true, true, true}
  {true, true, true, true, true}
  {-1, 0, 1, 2, 3}
  {-1L, 0L, 1L, 2L, 3L}
  {-1.0, 0.0, 1.0, 2.0, 3.0}
  {-1.0 - 1.0i, 0.0 - 2.0i, 1.0 - 3.0i, 2.0 - 4.0i, 3.0 - 5.0i}}}



test Function-23.11 {Test various function calls} {
    list [theTest {magnitudeSquared(1+i)}] \
         [theTest {magnitudeSquared({1+i, 1-i})}] \
         [theTest {magnitudeSquared([1+i, 1-i])}]
} {2.0 {{2.0, 2.0}} {[2.0, 2.0]}}

test Function-23.13 {Test various function calls} {
    list [theTest {reciprocal(1+i)}] \
         [theTest {reciprocal({1+i, 1-i})}] \
         [theTest {reciprocal([1+i, 1-i])}]
} {{0.5 - 0.5i} {{0.5 - 0.5i, 0.5 + 0.5i}} {[0.5 - 0.5i, 0.5 + 0.5i]}}

test Function-23.14 {Test various function calls} {
    list [theTest {roots(1+i, 4)}] \
         [theTest {roots({1+i, 1-i}, 4)}]
} {{{1.0695539323639858 + 0.21274750472674303i, -0.21274750472674295 + 1.0695539323639858i, -1.0695539323639858 - 0.21274750472674314i, 0.2127475047267431 - 1.0695539323639858i}} {{{1.0695539323639858 + 0.21274750472674303i, -0.21274750472674295 + 1.0695539323639858i, -1.0695539323639858 - 0.21274750472674314i, 0.2127475047267431 - 1.0695539323639858i}, {1.0695539323639858 - 0.21274750472674303i, 0.21274750472674311 + 1.0695539323639858i, -1.0695539323639858 + 0.21274750472674342i, -0.21274750472674347 - 1.0695539323639858i}}}}

 

 test Function-23.19 {Test various function calls: sum is defined in DoubleArrayMath} {
    list [theTest {sum({0.1,0.2,0.3})}]
 } {0.6}

 test Function-23.20 {Test various function calls: sumOfSquares is defined in DoubleArrayMath} {
    list [theTest {sumOfSquares({1.0,2.0,3.0})}]
 } {14.0}
 

####################################################################
