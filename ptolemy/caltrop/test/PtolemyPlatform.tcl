# Tests for the IntegerList class
#
# @Author: 
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

######################################################################
####
#

proc testCalExpr {name docString expression expectedValue} {
    test $name $docString {
	set platform [java::new ptolemy.caltrop.PtolemyPlatform]
	set context [$platform context]
	set globalEnv [$platform createGlobalEnvironment]
	set evaluator [java::new caltrop.interpreter.ExprEvaluator \
                                 $context $globalEnv]
	set expr [java::call caltrop.interpreter.util.SourceReader \
                             readExpr $expression] 
	set value [$evaluator evaluate $expr]
	$value toString
    } $expectedValue
}

######################################################################
####
#

####### basic stuff

testCalExpr {PtolemyPlatform-1.0} {Simple expression.} {
    1 + 2
} {3}

testCalExpr {PtolemyPlatform-1.1} {Operator precedences.} {
    [3 + 4 * 5, 4 * 5 + 3]
} {object([23, 23])} 


####### functions, closures etc.

testCalExpr {PtolemyPlatform-2.0} {Applying a function.} {
    lambda (x) : x * x end (5)
} {25}

testCalExpr {PtolemyPlatform-2.1} {Method closures.} {
    let m = [7, 19] . get :
        m(1)
    end
} {19}

testCalExpr {PtolemyPlatform-2.2} {Class/constructor closures.} {
    let c = "abc".getClass() :
        c() . length()
    end
} {0}




####### comprehensions, collection stuff

testCalExpr {PtolemyPlatform-3.0} {Simple comprehension.} {
    [a : for a in Integers(0, 10), a mod 2 = 0]
    .equals( [0, 2, 4, 6, 8, 10] )  
} {true}
  # FIXME: Equality test on ObjectTokens currently broken.

testCalExpr {PtolemyPlatform-3.1} {Map domain.} {
    map {a + 1 -> a * a : for a in Integers(0, 10), a mod 2 = 0}
    .keySet()
    .equals({1, 3, 5, 7, 9, 11})
} {true}
  # FIXME: dom/rng operators 


####### variable declarations

testCalExpr {PtolemyPlatform-4.0} {Variable declaration sorting.} {
    let a = 11 :
        let a = 6, b = a + 1 : a * b end
        =
        let b = a + 1, a = 6 : a * b end
    end
} {true}

testCalExpr {PtolemyPlatform-4.1} {Mutual recursion.} {
    let 
       function f (x) : g (x) end,
       function g (x) :
           if x < 2 then 1 else x * f(x - 1) end
       end
    :
       f(10)
    end
} {3628800}


####### Java interface

testCalExpr {PtolemyPlatform-5.0} {Calling Java method.} {
    ["abc", "def"] . size()
} {2}

testCalExpr {PtolemyPlatform-5.1} {Calling Java method.} {
    let a = ["abc", "def"], b = a . add ("xyz") : 
       // NOTE: wrapping problem in a
       a . size() 
    end
} {3}














