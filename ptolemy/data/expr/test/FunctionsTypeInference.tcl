# Test type inference on function calls.
#
# @Author: Steve Neuendorffer and Edward Lee
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

proc inferTypes {root} {
    set typeInference [java::new ptolemy.data.expr.ParseTreeTypeInference]
    $typeInference inferTypes $root
}
proc displayTree {root} {
    set display [java::new ptolemy.data.expr.ParseTreeDumper]
    $display displayParseTree $root
}
proc inferTypesScope {root scope} {
    set typeInference [java::new ptolemy.data.expr.ParseTreeTypeInference]
    $typeInference inferTypes $root $scope
}

# Return the type inferred on the specified expression.
proc inferType {expression} {
    set parser [java::new ptolemy.data.expr.PtParser]
    set root [ $parser {generateParseTree String} $expression]
    [inferTypes $root] toString
}

# Check that the type inferred on the specified expression
# matches the type of the result. Return 1 if they match,
# and 0 otherwise.
proc matchTypes {expression} {
    set parser [java::new ptolemy.data.expr.PtParser]
    set root [ $parser {generateParseTree String} $expression]
    set inferredType [inferTypes $root]
    set evaluateResultType [[ $root evaluateParseTree ] getType]
    return [$evaluateResultType equals $inferredType]
}

######################################################################
####
# 
test FunctionsTypeInference-acos {Test acos} {
    list [matchTypes {acos(0.0)}] \
         [matchTypes {acos(1.0)}] \
         [matchTypes {acos(1)}] \
         [matchTypes {acos(1ub)}] \
         [matchTypes {acos(1.0+0.0i)}] \
     } {1 1 1 1 1}

####################################################################
# asin

test Function-asin {Test asin} {
    list [matchTypes {asin(1.0)}] \
         [matchTypes {asin(0.0)}] \
         [matchTypes {asin(0)}] \
         [matchTypes {asin(0ub)}] \
         [matchTypes {asin(1.0+0.0i)}] \
     } {1 1 1 1 1}

####################################################################
# atan

test Function-atan {Test atan} {
    list [matchTypes {atan(1.0)}] \
         [matchTypes {atan(-1)}] \
         [matchTypes {atan(0ub)}] \
         [matchTypes {atan(Infinity)}] \
         [matchTypes {atan(0.0+0.0i)}]
     } {1 1 1 1 1}

####################################################################
# atan2

test Function-atan2 {Test atan2} {
    list [matchTypes {atan2(1.0, 1.0)}] \
         [matchTypes {atan2(-1, 1)}] \
         [matchTypes {atan2(0ub, 1ub)}] \
         [matchTypes {atan2(Infinity, 1.0)}] \
     } {1 1 1 1}

####################################################################
# acosh

test Function-acosh {Test acosh} {
    list [matchTypes {acosh(1.0)}] \
         [matchTypes {acosh(1)}] \
         [matchTypes {acosh(1ub)}] \
         [matchTypes {acosh(1.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
# asinh

test Function-asinh {Test asinh} {
    list [matchTypes {asinh(0.0)}] \
         [matchTypes {asinh(0)}] \
         [matchTypes {asinh(0ub)}] \
         [matchTypes {asinh(0.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
# cos

test Function-cos {Test cos} {
    list [matchTypes {cos(0.0)}] \
         [matchTypes {cos(0)}] \
         [matchTypes {cos(0ub)}] \
         [matchTypes {cos(0.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
# cosh

test Function-cosh {Test cosh} {
    list [matchTypes {cosh(0.0)}] \
         [matchTypes {cosh(0)}] \
         [matchTypes {cosh(0ub)}] \
         [matchTypes {cosh(0.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
# sin

test Function-sin {Test sin} {
    list [matchTypes {sin(0.0)}] \
         [matchTypes {sin(0)}] \
         [matchTypes {sin(0ub)}] \
         [matchTypes {sin(0.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
# sinh

test Function-sinh {Test sinh} {
    list [matchTypes {sinh(0.0)}] \
         [matchTypes {sinh(0)}] \
         [matchTypes {sinh(0ub)}] \
         [matchTypes {sinh(0.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
# tan

test Function-tan {Test tan} {
    list [matchTypes {tan(0.0)}] \
         [matchTypes {tan(pi)}] \
         [matchTypes {tan(0)}] \
         [matchTypes {tan(0ub)}] \
         [matchTypes {tan(0.0+0.0i)}] \
     } {1 1 1 1 1}

####################################################################
# tanh

test Function-tanh {Test tanh} {
    list [matchTypes {tanh(0.0)}] \
         [matchTypes {tanh(0)}] \
         [matchTypes {tanh(0ub)}] \
         [matchTypes {tanh(0.0+0.0i)}] \
     } {1 1 1 1}

####################################################################
####################################################################
####################################################################
####################################################################
# New table in the docs:

####################################################################
# abs

test Function-abs {Test abs on scalars} {
    list [matchTypes {abs(1+i)}] \
         [matchTypes {abs(-1.0)}] \
         [matchTypes {abs(-1)}] \
         [matchTypes {abs(1ub)}] \
         [matchTypes {abs(-1L)}] \
         [matchTypes {abs(1ub)}] \
     } {1 1 1 1 1 1}

# FIXME: Failures:
# test Function-abs-2 {Test abs on arrays} {
#     list [matchTypes {abs({1+i, 1-i})}] \
#          [matchTypes {abs({-1.0, -2.0})}] \
#          [matchTypes {abs({-1, -2})}] \
#          [matchTypes {abs({-1L, -2L})}] \
#      } {1 1 1 1}
# 
# test Function-abs-3 {Test abs on matrices} {
#     list [matchTypes {abs([1+i, 1-i])}] \
#          [matchTypes {abs(-identityDouble(2))}] \
#          [matchTypes {abs(-identityInt(2))}] \
#          [matchTypes {abs(-identityLong(2))}] \
#          [matchTypes {abs(-identityComplex(2))}] \
#      } {1 1 1 1 1}

####################################################################
# angle

# FIXME
#  test Function-angle {Test angle} {
#     list [matchTypes {angle(1+i)}] \
#          [matchTypes {angle({1+i, 1-i})}] \
#          [matchTypes {angle([1+i, 1-i])}] \
#          [matchTypes {angle(i)}] \
#          [matchTypes {angle(0.0i)}] \
#      } {1 1 1 1 1}


####################################################################
# ceil

test Function-ceil {Test ceil on scalars} {
    list [matchTypes {ceil(-1.1)}] \
         [matchTypes {ceil(-1)}] \
         [matchTypes {ceil(1ub)}] \
# FIXME
#          [matchTypes {ceil({1.1, 2.1})}] \
#          [matchTypes {ceil([1.1, 2.1])}] \
     } {1 1 1 1 1}

####################################################################
# compare

 test Function-compare {Test compare} {
    list [matchTypes {compare(1.0, 2.0)}] \
         [matchTypes {compare(1, 2)}] \
         [matchTypes {compare(1ub, 2ub)}] \
# FIXME
#          [matchTypes {compare({1, 2}, {3, 4})}] \
#          [matchTypes {compare([1, 2], [3, 4])}] \
     } {1 1 1 1 1}

####################################################################
# conjugate

 test Function-conjugate {Test conjugate} {
    list [matchTypes {conjugate(1+i)}] \
# FIXME
#          [matchTypes {conjugate({1+i, 1-i})}] \
#          [matchTypes {conjugate([1+i, 1-i])}] \
     } {1 1 1}


# FIXME: Much missing

####################################################################
# random

# FIXME: test fails... Need similar test for Gaussian.
 test Function-random {Test random} {
    list [matchTypes {random()}] \
         [matchTypes {random(2)}] \
         [matchTypes {random(2, 2)}] \
     } {1 1 1}
