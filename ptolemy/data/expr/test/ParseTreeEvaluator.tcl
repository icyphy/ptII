# Tests for the ParseTreeEvaluator class
#
# @Author: Steve Neuendorffer
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

######################################################################
####
# 
test ParseTreeEvaluator-2.2 {Construct a Parser, try simple integer expressions} {
    list [theTest "2 + 3 + 4"] [theTest "2 - 3 - 4"] [theTest "2 * 3 * 4"] [theTest "7 % 5"] [theTest "12 / 2 / 3"]
} {9 -5 24 2 2}

######################################################################
####
# 
test ParseTreeEvaluator-2.3 {Construct a Parser, try complex integer expressions} {
    list [theTest "-(2 + (3) + 4*(3- 4 % 3)*(12/12))\n"]
} {-13}
######################################################################
####
# 
test ParseTreeEvaluator-2.4 {Construct a Parser, try simple double expressions} {
    list [theTest "2.0 + 3.5 + 4.2"] [theTest "2.2 - 3.6 - 4.2"] [theTest "2.0 * 3.5 * 4.2"] [theTest "7.1 % 5.5"] [theTest "12.0 / 2.4 / 2.5"]
} {9.7 -5.6 29.4 1.6 2.0}

######################################################################
####
# 
test ParseTreeEvaluator-2.5 {Construct a Parser, try complex double expressions} {
    list [theTest "-(2.2 + (3.7%1.5) + 4.0*(3.2- 4.2 % 3.0)*(12.0/2.4/2.5/2.0))"]
} {-10.9}

######################################################################
####
# 
test ParseTreeEvaluator-2.6 {Construct a Parser, try creating complex numbers} {
    list [theTest "2i"] [theTest "3 + 2i"]
} {{0.0 + 2.0i} {3.0 + 2.0i}}

######################################################################
####
# 
test ParseTreeEvaluator-2.7 {Construct a Parser, try integer format specifiers} {
    list [theTest "29"] [theTest "035"] [theTest "0x1D"] [theTest "0X1d"] [theTest "0xbub"]
} {29 29 29 29 11ub}

######################################################################
####
# 
test ParseTreeEvaluator-2.8 {Construct a Parser, try long format specifiers} {
    list [theTest "29l"] [theTest "035L"] [theTest "0x1Dl"] [theTest "0X1dL"]
} {29l 29l 29l 29l} {Longs are not preserved}

######################################################################
####
# 
test ParseTreeEvaluator-2.9 {Construct a Parser, try floating point format specifiers} {
    list [theTest "1.8e1"] [theTest ".18E2"] [theTest "18.0f"] [theTest "18.0D"]
} {18.0 18.0 18.0 18.0}

######################################################################
####
# 
test ParseTreeEvaluator-3.0 {Construct a Parser,mixing doubles, strings and integers using arithmetic} {
    list [theTest "-(2*9.5 + (3.5/7) + 4/.5) +  \" hello \" + (3*5 -4)\n"]
} {{"-27.5 hello 11"}}

######################################################################
####
# 
test ParseTreeEvaluator-4.0 {Construct a Parser, try basic relational operators} {
    list [theTest "2<4"] [theTest "4<=4"] [theTest "4>=4"] [theTest "4>7"] [theTest "5==4"] [theTest "5!=4"]
} {true true true false false true}

######################################################################
####
# 
test ParseTreeEvaluator-4.1 {Construct a Parser, try  relational operators with arithetic operators} {
    list [theTest "(2)<(4*5 -9)"] [theTest "4<=4*7"] [theTest "4-7>=4"]
} {true true false}

######################################################################
####
# 
test ParseTreeEvaluator-4.2 {Construct a Parser,test use of equality operator on strings} {
    list [theTest "\"hello\" == \"hello\""] [theTest "\"hello\" != \"hello\""]
} {true false}

######################################################################
####
test ParseTreeEvaluator-4.3 {Construct a Parser,test shift operators} {
    list [theTest "2 << 2"] [theTest "-4 >> 1"] [theTest "-4L >>> 1"] [theTest "4UB >> 2"]
} {8 -2 9223372036854775806 1ub}

######################################################################
####
# 
test ParseTreeEvaluator-5.0 {Construct a Parser, test use of logical operators} {
    list [theTest "\"hello\" == \"hello\" && 5>=5"] [theTest "\"hello\" != \"hello\" || 3<3"] [theTest "(3<=5) && (56 == 56) || (\"foo\" != \"foo\")"]
} {true false true}

######################################################################
####
# 
test ParseTreeEvaluator-5.1 {Construct a Parser, unary minus & unary logical not} {
    list [theTest "!true"] [theTest "-7"]
} {false -7}


######################################################################
####
# 
test ParseTreeEvaluator-7.0 {Construct a Parser, try simple functional if then else} {
    list [theTest "(true)?(7):(6)\n"] [theTest "(true)?(7):(6.0)\n"]
} {7 7.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.1 {Construct a Parser, try harder if then else} {
    list [theTest "(false) ? (3/.5*4) : (pow(3.0,2.0))"]
} {9.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.2 {Test complicated expression within boolean test condition} {
    list [theTest "((3<5) && (\"test\" == \"test\")) ? (3/.5*4) : (pow(3.0,2.0))"]

} {24.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.3 {Test nested if then elses} {
    list [theTest "(true ? false: true ) ? (3/.5*4) : (pow(3.0,2.0))"]
} {9.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.4 {Test many levels of parenthesis nesting} {
    list [theTest "(true ? false: true ) ? (((((3/.5*4))))) : ((((((pow(3.0,2.0)))))))"]
   
} {9.0}

######################################################################
####
# 
test ParseTreeEvaluator-8.1 {Test bitwise operators} {
    list [theTest "5&2"] [theTest "5|2"] [theTest "5\#4"] [theTest "~5"]
} {0 7 1 -6}

######################################################################
####
# 
test ParseTreeEvaluator-8.2 {Test more complicated bitwise operations, and bitwise ops on booleans} {
    list [theTest "~(5 & 2 | 4)"] [theTest "(5>4) & (2==2)"] [theTest "(false) | (2!=2)"]
} {-5 true false}

######################################################################
####
# Need to test that the tree can be reevaluated an arbitrary number of times
test ParseTreeEvaluator-9.0 {Check that evaluation of the parse tree does not change the parse tree} {
    list [theTest "2+3"] [theTest "2-3"] [theTest "2*3"] [theTest "2/4"] [theTest "11 % 3"]
  
} {5 -1 6 0 2}

######################################################################
####
# Need to test that functions can access methods registered in the 
# search path of the parser. can be registered and recognized by the parser.
# FIXME: this test is not finished.
test ParseTreeEvaluator-10.1 {Test that functions can access registered classes.
} {
    list [theTest "min(1,3)"] [theTest "sin(30*PI/180)"]
} {1 0.5}

#

######################################################################
####
# Test matrix construction, when term types are identical.
test ParseTreeEvaluator-12.1 {Test basic matrix construction.} {
    list [theTest "\[1,2,3;4,5,6;7,8,9\]" ]
} {{[1, 2, 3; 4, 5, 6; 7, 8, 9]}}

# Test matrix construction, when term types are heterogeneous.
test ParseTreeEvaluator-12.2 {Test matrix construction.} {
    list [theTest "\[1.0;2;3j\]" ]
} {{[1.0 + 0.0i; 2.0 + 0.0i; 0.0 + 3.0i]}}

######################################################################
####

test ParseTreeEvaluator-13.1 {Test array method calls.} {
    list [theTest "cast(int, {1, 2, 3}.getElement(1))"]
} {2}

# Test record construction,
test ParseTreeEvaluator-13.2 {Test record construction.} {
    list [theTest "{a=1,b=2.4}" ]
} {{{a=1, b=2.4}}}

######################################################################
####
# Test eval
test ParseTreeEvaluator-14.0 {Test eval inference.} {
    list [theTest "eval(\"1+1\")" ]
} {2}

######################################################################
####
# 
test ParseTreeEvaluator-16.0 {Test method calls on arrays, matrices, etc.} {
    list [theTest "cast({int},{1,2,3}.add({3,4,5}))"] [theTest "cast({int},{{a=1,b=2},{a=3,b=4},{a=5,b=6}}.get(\"a\"))"] [theTest "cast(\[int\],create({1,2,3,4,5,6},2,3))"] [theTest "cast({int},{1,1,1,1}.leftShift({1,2,3,4}))"]
} {{{4, 6, 8}} {{1, 3, 5}} {[1, 2, 3; 4, 5, 6]} {{2, 4, 8, 16}}}

test ParseTreeFreeVariableCollector-16.2 {Test record indexing} {
    list [theTest "true ? 2 : ({a={0,0,0}}.a).length()"] [theTest "false ? 2 : ({a={0,0,0}}.a).length()"]
} {2 3}

# NOTE: The following is not a reasonable test, since it's user dependent.
# test ParseTreeEvaluator-16.3 {Test property} {
#     list [theTest "getProperty(\"ptolemy.ptII.dir\")"] [theTest "property(\"ptolemy.ptII.dir\") + \"foo\""]
# } {{"c:/users/neuendor/ptII"} {"c:/users/neuendor/ptIIfoo"}}

####################################################################

test ParseTreeEvaluator-17.1 {Test correct scoping in function definitions.} {
    list [theTest "function(x) x + 3.0"] [theTest "function(x:int) x + 3.0"]
} {{(function(x) (x+3.0))} {(function(x:int) (x+3.0))}}

####################################################################

test ParseTreeEvaluator-18.1 {Test complex multiplied by [double].} {
    list [theTest "(1+i)*\[1, 2\]"] \
         [theTest "\[1, 2\]*(1+i)"]
 } {{[1.0 + 1.0i, 2.0 + 2.0i]} {[1.0 + 1.0i, 2.0 + 2.0i]}}

test ParseTreeEvaluator-18.2 {Test various matrix ops} {
    list [theTest {[1, 2; 3, 4]*[2, 2; 2, 2]}] \
         [theTest {[1, 2; 3, 4]+[2, 2; 2, 2]}] \
         [theTest {[1, 2; 3, 4]-[2, 2; 2, 2]}] \
         [theTest {[1, 2; 3, 4]^2}] \
         [theTest {[1, 2; 3, 4]==[2, 2; 2, 2]}]
} {{[6, 6; 14, 14]} {[3, 4; 5, 6]} {[-1, 0; 1, 2]} {[7, 10; 15, 22]} false}
