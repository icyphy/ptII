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

proc evaluateTree {root} {
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]
    $evaluator evaluateParseTree $root
}

proc evaluate {expression} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    [evaluateTree $root] toString
}

# Call ptclose on the results.
# Use this proc if the results are slightly different under Solaris 
# and Windows
proc evaluatePtClose {expression results} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    ptclose [[evaluateTree $root] toString] $results
}

####################################################################
# acos

test Function-acos {Test acos} {
    list [evaluatePtClose {acos(0.0)} 1.5707963267949] \
         [evaluate {acos(1.0)}] \
         [evaluate {acos(1)}] \
         [evaluate {acos(1ub)}] \
         [evaluatePtClose {acos(1.0+0.0i)} {0.0 + 0.0i}] \
     } {1 0.0 0.0 0.0 1}

####################################################################
# asin

test Function-asin {Test asin} {
    list [evaluatePtClose {asin(1.0)} 1.5707963267949] \
         [evaluate {asin(0.0)}] \
         [evaluate {asin(0)}] \
         [evaluate {asin(0ub)}] \
         [evaluatePtClose {asin(1.0+0.0i)} {1.5707963267949 + 0.0i}] \
     } {1 0.0 0.0 0.0 1}

####################################################################
# atan

test Function-atan {Test atan} {
    list [evaluatePtClose {atan(1.0)} 0.7853981633974] \
         [evaluatePtClose {atan(-1)} -0.7853981633974] \
         [evaluate {atan(0ub)}] \
         [evaluatePtClose {atan(Infinity)} 1.5707963267949] \
         [evaluatePtClose {atan(0.0+0.0i)} {0.0 + 0.0i}]
     } {1 1 0.0 1 1}

####################################################################
# atan2

test Function-atan2 {Test atan2} {
    list [evaluatePtClose {atan2(1.0, 1.0)} 0.7853981633974] \
         [evaluatePtClose {atan2(-1, 1)} -0.7853981633974] \
         [evaluate {atan2(0ub, 1ub)}] \
         [evaluatePtClose {atan2(Infinity, 1.0)} 1.5707963267949] \
     } {1 1 0.0 1}

####################################################################
# acosh

test Function-acosh {Test acosh} {
    list [evaluatePtClose {acosh(1.0)} 0.0] \
         [evaluatePtClose {acosh(1)} 0.0] \
         [evaluatePtClose {acosh(1ub)} 0.0] \
         [evaluatePtClose {acosh(1.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

####################################################################
# asinh

test Function-asinh {Test asinh} {
    list [evaluatePtClose {asinh(0.0)} 0.0] \
         [evaluatePtClose {asinh(0)} 0.0] \
         [evaluatePtClose {asinh(0ub)} 0.0] \
         [evaluatePtClose {asinh(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

test Function-asinh-2 {Test asinh} {
    list [evaluatePtClose {asinh(1+i)} \
	{1.0612750619050355 + 0.6662394324925153i}] \
         [evaluatePtClose {asinh({1+i, 1-i})} \
	{{1.0612750619050355 + 0.6662394324925153i, 1.0612750619050355 - 0.6662394324925153i}}] \
	[evaluatePtClose {asinh([1+i, 1-i])} \
	{[1.0612750619050357 + 0.6662394324925153i, 1.0612750619050357 - 0.6662394324925153i]}]
} {1 1 1}

####################################################################
# cos

test Function-cos {Test cos} {
    list [evaluatePtClose {cos(0.0)} 1.0] \
         [evaluatePtClose {cos(0)} 1.0] \
         [evaluatePtClose {cos(0ub)} 1.0] \
         [evaluatePtClose {cos(0.0+0.0i)} {1.0 + 0.0i}] \
     } {1 1 1 1}

test Function-cos-2 {Test cos} {
    list [evaluate {cos(1+i)}] \
         [evaluate {cos({1+i, 1-i})}] \
         [evaluate {cos([1+i, 1-i])}]
} {{0.8337300251311491 - 0.9888977057628653i} {{0.8337300251311491 - 0.9888977057628653i, 0.8337300251311491 + 0.9888977057628653i}} {[0.8337300251311491 - 0.9888977057628653i, 0.8337300251311491 + 0.9888977057628653i]}}

####################################################################
# cosh

test Function-cosh {Test cosh} {
    list [evaluatePtClose {cosh(0.0)} 1.0] \
         [evaluatePtClose {cosh(0)} 1.0] \
         [evaluatePtClose {cosh(0ub)} 1.0] \
         [evaluatePtClose {cosh(0.0+0.0i)} {1.0 + 0.0i}] \
     } {1 1 1 1}

 test Function-cosh-2 {Test cosh} {
    list [evaluate {cosh(1+i)}] \
         [evaluate {cosh({1+i, 1-i})}] \
         [evaluate {cosh([1+i, 1-i])}]
 } {{0.8337300251311491 + 0.9888977057628653i} {{0.8337300251311491 + 0.9888977057628653i, 0.8337300251311491 - 0.9888977057628653i}} {[0.8337300251311491 + 0.9888977057628653i, 0.8337300251311491 - 0.9888977057628653i]}}

####################################################################
# sin

test Function-sin {Test sin} {
    list [evaluatePtClose {sin(0.0)} 0.0] \
         [evaluatePtClose {sin(0)} 0.0] \
         [evaluatePtClose {sin(0ub)} 0.0] \
         [evaluatePtClose {sin(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

test Function-sin-2 {Test sin} {
    list [evaluate {sin(1+i)}] \
         [evaluate {sin({1+i, 1-i})}] \
         [evaluate {sin([1+i, 1-i])}]
} {{1.2984575814159776 + 0.6349639147847362i} {{1.2984575814159776 + 0.6349639147847362i, 1.2984575814159776 - 0.6349639147847362i}} {[1.2984575814159776 + 0.6349639147847362i, 1.2984575814159776 - 0.6349639147847362i]}}

####################################################################
# sinh

test Function-sinh {Test sinh} {
    list [evaluatePtClose {sinh(0.0)} 0.0] \
         [evaluatePtClose {sinh(0)} 0.0] \
         [evaluatePtClose {sinh(0ub)} 0.0] \
         [evaluatePtClose {sinh(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

 test Function-sinh-2 {Test sinh} {
    list [evaluate {sinh(1+i)}] \
         [evaluate {sinh({1+i, 1-i})}] \
         [evaluate {sinh([1+i, 1-i])}]
 } {{0.6349639147847362 + 1.2984575814159776i} {{0.6349639147847362 + 1.2984575814159776i, 0.6349639147847362 - 1.2984575814159776i}} {[0.6349639147847362 + 1.2984575814159776i, 0.6349639147847362 - 1.2984575814159776i]}}

####################################################################
# tan

test Function-tan {Test tan} {
    list [evaluatePtClose {tan(0.0)} 0.0] \
         [evaluatePtClose {tan(pi)} 0.0] \
         [evaluatePtClose {tan(0)} 0.0] \
         [evaluatePtClose {tan(0ub)} 0.0] \
         [evaluatePtClose {tan(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1 1}

 test Function-tan-2 {Test tan} {
    list [evaluate {tan(1+i)}] \
         [evaluate {tan({1+i, 1-i})}] \
         [evaluate {tan([1+i, 1-i])}]
 } {{0.27175258531951163 + 1.0839233273386946i} {{0.27175258531951163 + 1.0839233273386946i, 0.27175258531951163 - 1.0839233273386946i}} {[0.27175258531951163 + 1.0839233273386946i, 0.27175258531951163 - 1.0839233273386946i]}}

####################################################################
# tanh

test Function-tanh {Test tanh} {
    list [evaluatePtClose {tanh(0.0)} 0.0] \
         [evaluatePtClose {tanh(0)} 0.0] \
         [evaluatePtClose {tanh(0ub)} 0.0] \
         [evaluatePtClose {tanh(0.0+0.0i)} {0.0 + 0.0i}] \
     } {1 1 1 1}

 test Function-tanh {Test tanh} {
    list [evaluate {tanh(1+i)}] \
         [evaluate {tanh({1+i, 1-i})}] \
         [evaluate {tanh([1+i, 1-i])}]
 } {{1.0839233273386946 + 0.27175258531951163i} {{1.0839233273386946 + 0.27175258531951163i, 1.0839233273386946 - 0.27175258531951163i}} {[1.0839233273386946 + 0.27175258531951163i, 1.0839233273386946 - 0.27175258531951163i]}}

####################################################################
####################################################################
####################################################################
####################################################################
# New table in the docs:

####################################################################
# abs

test Function-abs {Test abs on scalars} {
    list [evaluatePtClose {abs(1+i)} 1.4142135623731] \
         [evaluate {abs(-1.0)}] \
         [evaluate {abs(-1)}] \
         [evaluate {abs(1ub)}] \
         [evaluate {abs(-1L)}] \
         [evaluate {abs(1ub)}] \
} {1 1.0 1 1 1L 1}

test Function-abs-2 {Test abs on arrays} {
    list [evaluatePtClose {abs({1+i, 1-i})} {{1.4142135623731, 1.4142135623731}}] \
         [evaluate {abs({-1.0, -2.0})}] \
         [evaluate {abs({-1, -2})}] \
         [evaluate {abs({-1L, -2L})}] \
     } {1 {{1.0, 2.0}} {{1, 2}} {{1L, 2L}}}

test Function-abs-3 {Test abs on matrices} {
    list [evaluatePtClose {abs([1+i, 1-i])} \
                  {[1.4142135623731, 1.4142135623731]}] \
         [evaluate {abs(-identityDouble(2))}] \
         [evaluate {abs(-identityInt(2))}] \
         [evaluate {abs(-identityLong(2))}] \
         [evaluate {abs(-identityComplex(2))}] \
} {1 {[1.0, 0.0; 0.0, 1.0]} {[1, 0; 0, 1]} {[1L, 0L; 0L, 1L]} {[1.0, 0.0; 0.0, 1.0]}}

####################################################################
# angle

 test Function-angle {Test angle} {
    list [evaluatePtClose {angle(1+i)} 0.7853981633974] \
         [evaluatePtClose {angle({1+i, 1-i})} \
                 {{0.7853981633974, -0.7853981633974}}] \
         [evaluatePtClose {angle([1+i, 1-i])} \
                 {[0.7853981633974, -0.7853981633974]}] \
         [evaluatePtClose {angle(i)} {1.5707963267949}] \
         [evaluate {angle(0.0i)}] \
 } {1 1 1 1 0.0}


####################################################################
# ceil

test Function-ceil {Test ceil} {
    list [evaluate {ceil(-1.1)}] \
         [evaluate {ceil(-1)}] \
         [evaluate {ceil(1ub)}] \
         [evaluate {ceil({1.1, 2.1})}] \
         [evaluate {ceil([1.1, 2.1])}] \
     } {-1.0 -1.0 1.0 {{2.0, 3.0}} {[2.0, 3.0]}}

####################################################################
# compare

 test Function-compare {Test compare} {
    list [evaluate {compare(1.0, 2.0)}] \
         [evaluate {compare(1, 2)}] \
         [evaluate {compare(1ub, 2ub)}] \
         [evaluate {compare({1, 2}, {3, 4})}] \
         [evaluate {compare([1, 2], [3, 4])}] \
         [evaluate {compare([1, 2], [1.0, 2.0])}] \
     } {-1 -1 -1 {{-1, -1}} {[-1, -1]} {[0, 0]}}

####################################################################
# conjugate

 test Function-conjugate {Test conjugate} {
    list [evaluate {conjugate(1+i)}] \
         [evaluate {conjugate({1+i, 1-i})}] \
         [evaluate {conjugate([1+i, 1-i])}] \
         [evaluatePtClose {conjugate(1.0)} {1.0 + 0.0i}] \
         [evaluatePtClose {conjugate(1)} {1.0 + 0.0i}] \
         [evaluatePtClose {conjugate(1ub)} {1.0 + 0.0i}] \
 } {{1.0 - 1.0i} {{1.0 - 1.0i, 1.0 + 1.0i}} {[1.0 - 1.0i, 1.0 + 1.0i]} 1 1 1}

####################################################################
# exp

test Function-exp {Test exp} {
    list [evaluatePtClose {exp(0.0)} 1.0] \
         [evaluatePtClose {exp(-1.0)} 0.3678794411714] \
         [evaluatePtClose {exp(0)} 1.0] \
         [evaluatePtClose {exp(0ub)} 1.0] \
         [evaluatePtClose {exp(0.0+0.0i)} {1.0 + 0.0i}] \
     } {1 1 1 1 1}

####################################################################
# floor

test Function-floor {Test floor} {
    list [evaluate {floor(-1.1)}] \
         [evaluate {floor(-1)}] \
         [evaluate {floor(1ub)}] \
         [evaluate {floor({1.1, 2.1})}] \
         [evaluate {floor([1.1, 2.1])}] \
     } {-2.0 -1.0 1.0 {{1.0, 2.0}} {[1.0, 2.0]}}

####################################################################
# imag

 test Function-7.1 {Test imag} {
    list [evaluate {imag(1+i)}] \
         [evaluate {imag({1+i, 1-i})}] \
         [evaluate {imag([1+i, 1-i])}] \
         [evaluate {imag(1.0)}] \
         [evaluate {imag(1)}] \
         [evaluate {imag(1ub)}] \
     } {1.0 {{1.0, -1.0}} {[1.0, -1.0]} 0.0 0.0 0.0}

####################################################################
# isInfinite

test Function-isInfinite {Test isInfinite} {
    list [evaluate {isInfinite(-1.1)}] \
         [evaluate {isInfinite(Infinity)}] \
         [evaluate {isInfinite(-Infinity)}] \
         [evaluate {isInfinite(-1)}] \
         [evaluate {isInfinite(1ub)}] \
         [evaluate {isInfinite({1.1, Infinity})}] \
         [evaluate {isInfinite([1.1, -Infinity])}] \
     } {false true true false false {{false, true}} {[false, true]}}

####################################################################
# isNaN

test Function-isNaN {Test isNaN} {
    list [evaluate {isNaN(-1.1)}] \
         [evaluate {isNaN(Infinity)}] \
         [evaluate {isNaN(NaN)}] \
         [evaluate {isNaN(-1)}] \
         [evaluate {isNaN(1ub)}] \
         [evaluate {isNaN({1.1, NaN})}] \
         [evaluate {isNaN([1.1, -Infinity])}] \
     } {false false true false false {{false, true}} {[false, false]}}

####################################################################
# log

test Function-log {Test log} {
    list [evaluatePtClose {log(e)} 1.0] \
         [evaluate {log(0.0)}] \
         [evaluate {log(1)}] \
         [evaluate {log(1ub)}] \
         [evaluatePtClose {log(1.0+0.0i)} {0.0 + 0.0i}] \
     } {1 -Infinity 0.0 0.0 1}

####################################################################
# log10

test Function-log10 {Test log10} {
    list [evaluatePtClose {log10(10.0)} 1.0] \
         [evaluate {log10(0.0)}] \
         [evaluate {log10(1)}] \
         [evaluate {log10(1ub)}] \
     } {1 -Infinity 0.0 0.0}

####################################################################
# log2

test Function-log2 {Test log2} {
    list [evaluatePtClose {log2(2.0)} 1.0] \
         [evaluate {log2(0.0)}] \
         [evaluate {log2(1)}] \
         [evaluate {log2(1ub)}] \
     } {1 -Infinity 0.0 0.0}

####################################################################
# max

test Function-max {Test max} {
    list [evaluate {max(1.0, 2.0)}] \
         [evaluate {max(0.0, -1.0)}] \
         [evaluate {max(1, -1)}] \
         [evaluate {max(1ub, 2ub)}] \
         [evaluate {max(1L, -1L)}] \
         [evaluate {max({1.0, 2.0})}] \
         [evaluate {max({1, 2})}] \
         [evaluate {max({1L, 2L})}] \
         [evaluate {max({1ub, 2ub})}] \
     } {2.0 0.0 1 2ub 1L 2.0 2 2L 2ub}


####################################################################
# min

test Function-min {Test min} {
    list [evaluate {min(1.0, 2.0)}] \
         [evaluate {min(0.0, -1.0)}] \
         [evaluate {min(1, -1)}] \
         [evaluate {min(1ub, 2ub)}] \
         [evaluate {min(1L, -1L)}] \
         [evaluate {min({1.0, 2.0})}] \
         [evaluate {min({1, 2})}] \
         [evaluate {min({1L, 2L})}] \
         [evaluate {min({1ub, 2ub})}] \
     } {1.0 -1.0 -1 1ub -1L 1.0 1 1L 1ub}

####################################################################
# within

test Function-within-double {Test within on doubles} {
   list [evaluate {within (1.0, 1.1, 0.1)}] \
        [evaluate {within (-1.0, -0.9, 0.1)}] \
        [evaluate {within (1.0, 1.1, 0.0)}] \
        [evaluate {within (1.0, 1.0, 0.0)}] \
        [evaluate {within (1.0, 1.1, 0.05)}]
    } {true true false true false}

# NOTE: Couldn't find a way to make complex as precise as double.
test Function-within-complex {Test within on complex} {
   list [evaluate {within (1.0i, 1.1i, 0.10001)}] \
        [evaluate {within (1.0i, 1.1i, 0.0)}] \
        [evaluate {within (1.0i, 1.0i, 0.0)}] \
        [evaluate {within (1.0, 1.1i, 0.05)}]
    } {true false true false}

test Function-within-int {Test within on ints} {
   list [evaluate {within (1, 2, 1)}] \
        [evaluate {within (1, 2, 0)}] \
        [evaluate {within (1, 1, 0)}] \
        [evaluate {within (1, 3, 1)}] \
        [evaluate {within (-1, -2, 1)}] \
        [evaluate {within (-2, -1, 1)}] \
        [evaluate {within (-1, -3, 1)}] \
        [evaluate {within (-1, -3, 1)}]
    } {true false true false true true false false}

test Function-within-long {Test within on longs} {
   list [evaluate {within (1L, 2L, 1)}] \
        [evaluate {within (1L, 2L, 0)}] \
        [evaluate {within (1L, 1L, 0)}] \
        [evaluate {within (1L, 3L, 1)}]
    } {true false true false}

test Function-within-ub {Test within on unsigned bytes} {
   list [evaluate {within (1ub, 2ub, 1)}] \
        [evaluate {within (1ub, 2ub, 0)}] \
        [evaluate {within (1ub, 1ub, 0)}] \
        [evaluate {within (1ub, 3ub, 1)}]
    } {true false true false}

test Function-within-fix {Test within on fixed point} {
   list [evaluate {within (fix(1,8,4), fix(2,8,4), 1)}] \
        [evaluate {within (fix(1,8,4), fix(2,8,4), 0)}] \
        [evaluate {within (fix(1,8,4), fix(1,8,4), 0)}] \
        [evaluate {within (fix(1,8,4), fix(3,8,4), 1)}]
    } {true false true false}

test Function-within-array {Test within on arrays} {
   list [evaluate {within ({1.0, -1.0}, {1.1, -0.9}, 0.1)}] \
        [evaluate {within ({1.0, 1.0}, {1.1, 1.0}, 0.0)}] \
        [evaluate {within ({1.0, -2.0}, {1.0, -2.0}, 0.0)}] \
        [evaluate {within ({1.0, -1.0}, {1.1, -1.1}, 0.05)}]
    } {true false true false}

test Function-within-matrix {Test within on matrices} {
   list [evaluate {within ([1.0, -1.0], [1.1, -0.9], 0.1)}] \
        [evaluate {within ([1.0, 1.0], [1.1, 1.0], 0.0)}] \
        [evaluate {within ([1.0, -2.0], [1.0, -2.0], 0.0)}] \
        [evaluate {within ([1.0, -1.0], [1.1, -1.1], 0.05)}]
    } {true false true false}

test Function-within-array {Test within on records} {
   list [evaluate {within ({a=1.0, b=-1.0}, {a=1.1, b=-0.9}, 0.1)}] \
        [evaluate {within ({a=1.0, b=1.0}, {a=1.1, b=1.0}, 0.0)}] \
        [evaluate {within ({a=1.0, b=-2.0}, {a=1.0, b=-2.0}, 0.0)}] \
        [evaluate {within ({a=1.0, b=-1.0}, {a=1.1, b=-1.1}, 0.05)}]
    } {true false true false}

test Function-within-misc {Test within on misc} {
   list [evaluate {within ("a", "b", 0.0)}] \
        [evaluate {within("a", "a", 0.0)}] \
        [evaluate {within({a=1}, {b=1}, 1.0)}] \
        [evaluate {within({a=1}, {a=1, b=1}, 1.0)}] \
        [evaluate {within([1], [1, 2], 1.0)}] \
        [evaluate {within({1}, {1, 2}, 1.0)}]
} {false true false false false false}

####################################################################
# pow

test Function-pow {Test pow} {
    list [evaluatePtClose {pow(2.0, 0.0)} 1.0] \
         [evaluatePtClose {pow(0.0, -1.0)} Infinity] \
         [evaluatePtClose {pow(1, -1)} 1.0] \
         [evaluatePtClose {pow(2ub, 2ub)} 4.0] \
         [evaluatePtClose {pow({1.0, 2.0}, {-2, -2})} {{1.0, 0.25}}] \
         [evaluatePtClose {pow({1, 2}, {2, 2})} {{1.0, 4.0}}] \
         [evaluatePtClose {pow({1ub, 2ub}, {2ub, 2ub})} {{1.0, 4.0}}] \
         [evaluatePtClose {pow([1.0, 2.0], [-2, -2])} {[1.0, 0.25]}] \
         [evaluatePtClose {pow([1, 2], [2, 2])} {[1.0, 4.0]}] \
     } {1 1 1 1 1 1 1 1 1}

test Function-pow2 {Test pow on complex} {
    list [evaluatePtClose {pow(i, 2)} {-1.0 + 0.0i}] \
         [evaluatePtClose {pow(e, 2*pi*i)} {1.0 + 0.0i}] \
         [evaluatePtClose {pow(e+0.0i, 2*pi*i)} {1.0 + 0.0i}] \
     } {1 1 1}

####################################################################
# random

# Tough to test?

####################################################################
# real

 test Function-15.1 {Test real} {
    list [evaluate {real(1+i)}] \
         [evaluate {real({1+i, 1-i})}] \
         [evaluate {real([1+i, 1-i])}] \
         [evaluate {real(1.0)}] \
         [evaluate {real(1)}] \
         [evaluate {real(1ub)}] \
     } {1.0 {{1.0, 1.0}} {[1.0, 1.0]} 1.0 1.0 1.0}

####################################################################
# remainder

test Function-remainder {Test remainder} {
    list [evaluatePtClose {remainder(3.0, 2.0)} -1.0] \
         [evaluatePtClose {remainder(2.0, 3.0)} -1.0] \
         [evaluatePtClose {remainder(-1, 1)} 0.0] \
         [evaluatePtClose {remainder(0ub, 1ub)} 0.0] \
         [evaluatePtClose {remainder(2.5, 1.0)} 0.5] \
         [evaluate {remainder({3, 2}, {2, 2})}] \
         [evaluate {remainder([3, 2], [2, 2])}] \
     } {1 1 1 1 1 {{-1.0, 0.0}} {[-1.0, 0.0]}}

####################################################################
# round

test Function-round {Test round} {
    list [evaluate {round(1.1)}] \
         [evaluate {round(-1.1)}] \
         [evaluate {round(NaN)}] \
         [string compare [evaluate {round(Infinity)}] [evaluate {MaxLong}]] \
         [string compare [evaluate {round(-Infinity)}] [evaluate {MinLong}]] \
     } {1L -1L 0L 0 0}

####################################################################
# roundToInt

test Function-roundToInt {Test roundToInt} {
    list [evaluate {roundToInt(1.1)}] \
         [evaluate {roundToInt(-1.1)}] \
         [evaluate {roundToInt(NaN)}] \
         [string compare [evaluate {roundToInt(Infinity)}] [evaluate {MaxInt}]] \
         [string compare [evaluate {roundToInt(-Infinity)}] [evaluate {MinInt}]] \
     } {1 -1 0 0 0}

####################################################################
# sgn

test Function-sgn {Test sgn} {
    list [evaluate {sgn(1.1)}] \
         [evaluate {sgn(-1.1)}] \
         [evaluate {sgn(0.0)}] \
     } {1 -1 1}

####################################################################
# sqrt

test Function-sqrt {Test sqrt} {
    list [evaluate {sqrt(4.0)}] \
         [evaluate {sqrt(-1.1)}] \
         [evaluate {sqrt(0.0)}] \
         [evaluate {sqrt(4.0 + 0.0i)}] \
     } {2.0 NaN 0.0 {2.0 + 0.0i}}


####################################################################
# toDegrees

test Function-toDegrees {Test toDegrees} {
    list [evaluate {toDegrees(0.0)}] \
         [evaluatePtClose {toDegrees(-pi)} -180] \
     } {0.0 1}

####################################################################
# toRadians

test Function-toRadians {Test toRadians} {
    list [evaluate {toRadians(0.0)}] \
         [evaluatePtClose {toRadians(-180)} [evaluate {-pi}]] \
     } {0.0 1}

####################################################################
####################################################################
####################################################################
####################################################################
####################################################################

####################################################################
# arrayToMatrix

test Function-arrayToMatrix {Test arrayToMatrix} {
    list [evaluate {arrayToMatrix({true,false,true,true,false,false}, 2, 3)}] \
	[evaluate {arrayToMatrix({1,2,3,4,5,6}, 2, 3)}] \
	[evaluate {arrayToMatrix({1L,2L,3L,4L,5L,6L}, 2, 3)}] \
	[evaluate {arrayToMatrix({1.0,2.0,3.0,4.0,5.0,6.0}, 2, 3)}] \
	[evaluate {arrayToMatrix({1.0 + 0i, 2.0 + 1i, 3.0 - 1i, 4.0 + 4i, 5.0 - 5i, 6.0 - 6.0i}, 2, 3)}] \
	[evaluate {arrayToMatrix({1ub,2,3.5,4,5,6}, 2, 3)}]
} {{[true, false, true; true, false, false]} {[1, 2, 3; 4, 5, 6]} {[1L, 2L, 3L; 4L, 5L, 6L]} {[1.0, 2.0, 3.0; 4.0, 5.0, 6.0]} {[1.0 + 0.0i, 2.0 + 1.0i, 3.0 - 1.0i; 4.0 + 4.0i, 5.0 - 5.0i, 6.0 - 6.0i]} {[1.0, 2.0, 3.5; 4.0, 5.0, 6.0]}}

test Function-arrayToMatrix {Test arrayToMatrix with an array that is not big enough} {
    catch {evaluate {arrayToMatrix({1L,2L,3L,4L}, 2, 3)}} errMsg
    list $errMsg	
} {{ptolemy.kernel.util.IllegalActionException: Error invoking function public static ptolemy.data.MatrixToken ptolemy.data.MatrixToken.arrayToMatrix(ptolemy.data.Token[],int,int) throws ptolemy.kernel.util.IllegalActionException

Because:
LongMatrixToken: The specified array is not of the correct length}}


####################################################################
# conjugateTranspose

test Function-conjugateTranspose {Test conjugateTranspose} {
    list [evaluate {conjugateTranspose([0, i; 0, 0])}] \
        } {{[0.0 + 0.0i, 0.0 + 0.0i; 0.0 - 1.0i, 0.0 + 0.0i]}}

####################################################################
# createSequence

test Function-createSequence {Test createSequence} {
    list "[evaluate {createSequence(false, false, 5)}]\n \
	[evaluate {createSequence(false, true, 5)}]\n \
	[evaluate {createSequence(true, false, 5)}]\n \
	[evaluate {createSequence(true, true, 5)}]\n \
	[evaluate {createSequence(-1, 1, 5)}]\n \
	[evaluate {createSequence(-1L, 1L, 5)}]\n \
	[evaluate {createSequence(-1.0, 1.0, 5)}]\n \
	[evaluate {createSequence(-1.0 - 1i, 1.0 - 1i, 5)}]"
} {{{false, false, false, false, false}
  {false, true, true, true, true}
  {true, true, true, true, true}
  {true, true, true, true, true}
  {-1, 0, 1, 2, 3}
  {-1L, 0L, 1L, 2L, 3L}
  {-1.0, 0.0, 1.0, 2.0, 3.0}
  {-1.0 - 1.0i, 0.0 - 2.0i, 1.0 - 3.0i, 2.0 - 4.0i, 3.0 - 5.0i}}}

####################################################################
# crop

test Function-crop {Test crop} {
    list [evaluate {crop(identityDouble(3), 0, 1, 2, 2)}] \
         [evaluate {crop(identityInt(3), 0, 1, 2, 2)}] \
         [evaluate {crop(identityComplex(3), 0, 1, 2, 2)}] \
         [evaluate {crop(identityLong(3), 0, 1, 2, 2)}] \
} {{[0.0, 0.0; 1.0, 0.0]} {[0, 0; 1, 0]} {[0.0 + 0.0i, 0.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]} {[0L, 0L; 1L, 0L]}}

####################################################################
# determinant

test Function-determinant {Test determinant} {
    list [evaluate {determinant(identityDouble(3))}] \
         [evaluate {determinant(identityInt(3))}] \
         [evaluate {determinant(identityComplex(3))}]
} {1.0 1.0 {1.0 + 0.0i}}

####################################################################
# diag

test Function-diag {Test diag} {
    list [evaluate {diag({1, 2})}] \
         [evaluate {diag({1.0, 2.0})}] \
         [evaluate {diag({1.0 + 1.0i, 2.0 + 2.0i})}] \
         [evaluate {diag({1L, 2L})}] \
} {{[1, 0; 0, 2]} {[1.0, 0.0; 0.0, 2.0]} {[1.0 + 1.0i, 0.0 + 0.0i; 0.0 + 0.0i, 2.0 + 2.0i]} {[1L, 0L; 0L, 2L]}}

####################################################################
# divideElements

test Function-divideElements {Test elementwise divide on matrices} {
    list [evaluate {divideElements([1.0, 2.0], [2.0, 2.0])}] \
         [evaluate {divideElements([1, 2], [2, 2])}] \
         [evaluate {divideElements([1, 2], [2.0, 2])}] \
         [evaluate {divideElements([1.0, 2], [2, 2])}] \
         [evaluate {divideElements([i, 2], [2.0 + 0.0*i, 2])}] \
         [evaluate {divideElements([2.0, 1.0], [2, 2])}]
 } {{[0.5, 1.0]} {[0, 1]} {[0.5, 1.0]} {[0.5, 1.0]} {[0.0 + 0.5i, 1.0 + 0.0i]} {[1.0, 0.5]}}

####################################################################
# hilbert

test Function-hilbert {Test hilbert} {
    list [evaluate {abs(determinant(hilbert(3))) < 0.001}]
} {true}

####################################################################
# identity

test Function-identity {Test identity} {
    list [evaluate {identityComplex(2)}] \
         [evaluate {identityDouble(2)}] \
         [evaluate {identityInt(2)}] \
         [evaluate {identityLong(2)}]
} {{[1.0 + 0.0i, 0.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]} {[1.0, 0.0; 0.0, 1.0]} {[1, 0; 0, 1]} {[1L, 0L; 0L, 1L]}}

####################################################################
# inverse and within

test Function-inverse {Test inverse} {
    list [evaluate {within(inverse(hilbert(3))*hilbert(3), identityDouble(3), 1.0e-6)}]
} {true}

####################################################################
# matrixToArray

test Function-matrixToArray {Test matrixToArray} {
    list \
	[evaluate {matrixToArray([true,false;true,true])}] \
	[evaluate {matrixToArray([1,2;3,4])}] \
	[evaluate {matrixToArray([1L,2L;3L,4L])}] \
	[evaluate {matrixToArray([1.0,2.0;3.0,4.0])}] \
	[evaluate {matrixToArray([1.0 + 0i, 2.0 + 1i; 3.0 - 1i, 4.0 + 4i])}]
} {{{true, false, true, true}} {{1, 2, 3, 4}} {{1L, 2L, 3L, 4L}} {{1.0, 2.0, 3.0, 4.0}} {{1.0 + 0.0i, 2.0 + 1.0i, 3.0 - 1.0i, 4.0 + 4.0i}}}

####################################################################
# merge

test Function-merge {Test merge of records} {
    list [evaluate {merge({a=1, b=2}, {a=3, c=3})}] \
        } {{{a=1, b=2, c=3}}}

####################################################################
# multiplyElements

test Function-multiplyElements {Test elementwise multiply on matrices} {
    list [evaluate {multiplyElements([1.0, 2.0], [2.0, 2.0])}] \
         [evaluate {multiplyElements([1, 2], [2, 2])}] \
         [evaluate {multiplyElements([i, 2], [2.0 + 0.0*i, 2])}]
        } {{[2.0, 4.0]} {[2, 4]} {[0.0 + 2.0i, 4.0 + 0.0i]}}

####################################################################
# orthogonalizeColumns

test Function-orthogonalizeColumns {Test orthogonalizeColumns} {
    list [evaluate {within(orthogonalizeColumns(identityDouble(3)), identityDouble(3), 1.0e-6)}] \
         [evaluate {within(orthogonalizeColumns(identityComplex(3)), identityComplex(3), 1.0e-6)}]
        } {true true}

####################################################################
# orthogonalizeRows

test Function-orthogonalizeRows {Test orthogonalizeRows} {
    list [evaluate {within(orthogonalizeRows(identityDouble(3)), identityDouble(3), 1.0e-6)}] \
         [evaluate {within(orthogonalizeRows(identityComplex(3)), identityComplex(3), 1.0e-6)}]
        } {true true}

####################################################################
# orthonormalizeColumns

test Function-orthonormalizeColumns {Test orthonormalizeColumns} {
    list [evaluate {within(orthonormalizeColumns(identityDouble(3)), identityDouble(3), 1.0e-6)}] \
         [evaluate {within(orthonormalizeColumns(identityComplex(3)), identityComplex(3), 1.0e-6)}]
        } {true true}

####################################################################
# orthonormalizeRows

test Function-orthonormalizeRows {Test orthonormalizeRows} {
    list [evaluate {within(orthonormalizeRows(identityDouble(3)), identityDouble(3), 1.0e-6)}] \
         [evaluate {within(orthonormalizeRows(identityComplex(3)), identityComplex(3), 1.0e-6)}]
        } {true true}

####################################################################
# repeat

test Function-repeat {Test repeat} {
    list [evaluate {repeat(2, 1)}] \
         [evaluate {repeat(2, 1.0)}] \
         [evaluate {repeat(2, i)}] \
         [evaluate {repeat(2, 1L)}] \
         [evaluate {repeat(2, [1])}] \
         [evaluate {repeat(2, {1})}] \
        } {{{1, 1}} {{1.0, 1.0}} {{0.0 + 1.0i, 0.0 + 1.0i}} {{1L, 1L}} {{[1], [1]}} {{{1}, {1}}}}

####################################################################
# sum

test Function-sum {Test sum} {
    list [evaluate {sum({1, 2})}] \
         [evaluate {sum({{1, 2}, {2, 3}})}] \
         [evaluate {sum({1.0, 2.0})}] \
         [evaluate {sum({1.0 + 1.0i, 2.0 + 2.0i})}] \
         [evaluate {sum({1L, 2L})}] \
         [evaluate {sum([1, 2; 3, 4])}] \
         [evaluate {sum([1.0, 2.0; 3.0, 4.0])}] \
         [evaluate {sum([1.0i, 2.0i; 3.0, 4.0])}] \
         [evaluate {sum([1L, 2L; 3L, 4L])}] \
     } {3 {{3, 5}} 3.0 {3.0 + 3.0i} 3L 10 10.0 {7.0 + 3.0i} 10L}

####################################################################
# trace

test Function-trace {Test trace} {
    list [evaluate {trace(identityComplex(3))}] \
         [evaluate {trace(identityDouble(3))}] \
         [evaluate {trace(identityInt(3))}] \
         [evaluate {trace(identityLong(3))}] \
        } {{3.0 + 0.0i} 3.0 3 3L}

####################################################################
# transpose

test Function-transpose {Test transpose} {
    list [evaluate {identityComplex(3)==transpose(identityComplex(3))}] \
         [evaluate {identityDouble(3)==transpose(identityDouble(3))}] \
         [evaluate {identityInt(3)==transpose(identityInt(3))}] \
         [evaluate {identityLong(3)==transpose(identityLong(3))}] \
        } {true true true true}

##### within is tested above.

####################################################################
####################################################################
####################################################################
####################################################################
####################################################################

####################################################################
# set

test Function-set {Test set} {
    list [evaluate {set("foo", 10)}] \
         [evaluate {foo*10}] \
     } {10 100}


####################################################################
####################################################################
####################################################################
####################################################################
####################################################################
# FIXME: Organize the following


test Function-23.11 {Test various function calls} {
    list [evaluate {magnitudeSquared(1+i)}] \
         [evaluate {magnitudeSquared({1+i, 1-i})}] \
         [evaluate {magnitudeSquared([1+i, 1-i])}]
} {2.0 {{2.0, 2.0}} {[2.0, 2.0]}}

test Function-23.13 {Test various function calls} {
    list [evaluate {reciprocal(1+i)}] \
         [evaluate {reciprocal({1+i, 1-i})}] \
         [evaluate {reciprocal([1+i, 1-i])}]
} {{0.5 - 0.5i} {{0.5 - 0.5i, 0.5 + 0.5i}} {[0.5 - 0.5i, 0.5 + 0.5i]}}

test Function-23.14 {Test various function calls} {
    list [evaluate {roots(1+i, 4)}] \
         [evaluate {roots({1+i, 1-i}, 4)}]
} {{{1.0695539323639858 + 0.21274750472674303i, -0.21274750472674295 + 1.0695539323639858i, -1.0695539323639858 - 0.21274750472674314i, 0.2127475047267431 - 1.0695539323639858i}} {{{1.0695539323639858 + 0.21274750472674303i, -0.21274750472674295 + 1.0695539323639858i, -1.0695539323639858 - 0.21274750472674314i, 0.2127475047267431 - 1.0695539323639858i}, {1.0695539323639858 - 0.21274750472674303i, 0.21274750472674311 + 1.0695539323639858i, -1.0695539323639858 + 0.21274750472674342i, -0.21274750472674347 - 1.0695539323639858i}}}}

 
 test Function-23.20 {Test various function calls: sumOfSquares is defined in DoubleArrayMath} {
    list [evaluate {sumOfSquares({1.0,2.0,3.0})}]
 } {14.0}
 

####################################################################
