# Tests for the ParseTreeEvaluator class
#
# @Author: Steve Neuendorffer, Edward A. Lee
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
    set value [$evaluator evaluateParseTree $root]
    set typeInference [java::new ptolemy.data.expr.ParseTreeTypeInference]
    set type [$typeInference inferTypes $root]
    if [$type equals [$value getType]] then {
	return [$value toString]
    } else {
	return "[$value toString] Warning: inferredType [$type toString] not consistent"
    }
}

proc evaluate {expression} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    evaluateTree $root
}

# Call ptclose on the results.
# Use this proc if the results are slightly different under Solaris 
# and Windows
proc evaluatePtClose {expression results} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    ptclose [evaluateTree $root] $results
}

######################################################################
####
#
test ParseTreeEvaluator-2.2 {Test simple integer expressions} {
    list [evaluate "2 + 3 + 4"] \
            [evaluate "2 - 3 - 4"] \
            [evaluate "2 * 3 * 4"] \
            [evaluate "7 % 5"] \
            [evaluate "12 / 2 / 3"]
} {9 -5 24 2 2}

test ParseTreeEvaluator-2.2.1 {Test simple unsigned byte expressions} {
    list [evaluate "2ub + 3ub + 4ub "] \
            [evaluate "2ub - 3ub - 4ub"] \
            [evaluate "2ub * 3ub * 4ub"] \
            [evaluate "7ub % 5ub"] \
            [evaluate "12ub / 2ub / 3ub"]
} {9ub 251ub 24ub 2ub 2ub}

######################################################################
####
# 
test ParseTreeEvaluator-2.3 {Test complex integer expressions} {
    list [evaluate "-(2 + (3) + 4*(3- 4 % 3)*(12/12))\n"]
} {-13}

######################################################################
####
# 
test ParseTreeEvaluator-2.4 {Test simple double expressions} {
    list [evaluate "2.0 + 3.5 + 4.2"] \
            [evaluate "2.2 - 3.6 - 4.2"] \
            [evaluate "2.0 * 3.5 * 4.2"] \
            [evaluate "7.1 % 5.5"] \
            [evaluate "12.0 / 2.4 / 2.5"]
} {9.7 -5.6 29.4 1.6 2.0}


######################################################################
####
# 
test ParseTreeEvaluator-2.4.1 {Test out power op '^' on doubles} {
    list [evaluate "3.0 ^ -3"] \
	[evaluate "pow(3.0, -3.0)"] \
	[evaluate "3.0 ^ 0"] \
	[evaluate "3.0 ^ 0ub"] \
	[evaluate "3.0 ^ 3"] \
	[evaluate "3.0 ^ 3ub"] \
	[evaluate "(3 + 0i) ^ 3ub"] \
	[evaluate "(3 + 2i) ^ 3ub"] \
	[evaluate "(3 + 0i) ^ -3"] 
} {0.037037037037 0.037037037037 1.0 1.0 27.0 27.0 {27.0 + 0.0i} {-9.0 + 46.0i} {0.037037037037037035 + 0.0i}}

######################################################################
####
# 
test ParseTreeEvaluator-2.4.2 {Test out power op '^' on matrices} {
    catch {evaluate {[1,2;3,4] ^ -1}} errMsg
    list "$errMsg\n \
	[evaluate {[1,2;3,4] ^ 0}]\n \
	[evaluate {[1,2;3,4] ^ 1}]\n \
	[evaluate {[1,2;3,4] ^ 2}]\n \
	[evaluate {[1,2;3,4] ^ 2ub}]"
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.IntMatrixToken '[1, 0; 0, 1]' and ptolemy.data.IntMatrixToken '[1, 2; 3, 4]'
  [1, 0; 0, 1]
  [1, 2; 3, 4]
  [7, 10; 15, 22]
  [7, 10; 15, 22]}}

######################################################################
####
# 
test ParseTreeEvaluator-2.5 {Test complex double expressions} {
    list [evaluate "-(2.2 + (3.7%1.5) + 4.0*(3.2- 4.2 % 3.0)*(12.0/2.4/2.5/2.0))"]
} {-10.9}

######################################################################
####
# 
test ParseTreeEvaluator-2.6 {Test creating complex numbers} {
    list [evaluate "2i"] \
            [evaluate "3 + 2i"]
} {{0.0 + 2.0i} {3.0 + 2.0i}}

######################################################################
####
# 
test ParseTreeEvaluator-2.7 {Test integer format specifiers} {
    list [evaluate "29"] \
            [evaluate "035"] \
            [evaluate "0x1D"] \
            [evaluate "0X1d"] \
            [evaluate "0xbub"]
} {29 29 29 29 11ub}

######################################################################
####
# 
test ParseTreeEvaluator-2.8 {Test long format specifiers} {
    list [evaluate "29l"] \
            [evaluate "035L"] \
            [evaluate "0x1Dl"] \
            [evaluate "0X1dL"]
} {29L 29L 29L 29L}

######################################################################
####
# 
test ParseTreeEvaluator-2.9 {Test floating point format specifiers} {
    list [evaluate "1.8e1"] \
            [evaluate ".18E2"] \
            [evaluate "18.0f"] \
            [evaluate "18.0D"]
} {18.0 18.0 18.0 18.0}

######################################################################
####
# 
test ParseTreeEvaluator-3.0 {Test mixing doubles, strings and integers using arithmetic} {
    list [evaluate "-(2*9.5 + (3.5/7) + 4/.5) +  \" hello \" + (3*5 -4)\n"]
} {{"-27.5 hello 11"}}

######################################################################
####
# 
test ParseTreeEvaluator-4.0 {Test relational operators} {
    list [evaluate "2<4"] \
            [evaluate "4<=4"] \
            [evaluate "4>=4"] \
            [evaluate "4>7"] \
            [evaluate "5==4"] \
            [evaluate "5!=4"]
} {true true true false false true}

test ParseTreeEvaluator-4.0.1 {Test relational operators on distinct types} {
    list [evaluate "2.0<4"] \
            [evaluate "4<=4.0"] \
            [evaluate "4L>=4"] \
            [evaluate "4>7L"] \
            [evaluate "5.0+0.0i==4"] \
            [evaluate "5!=4.0+0.0i"]
} {true true true false false true}

######################################################################
####
# 
test ParseTreeEvaluator-4.1 {Test relational operators with arithetic} {
    list [evaluate "(2)<(4*5 -9)"] \
            [evaluate "4<=4*7"] \
            [evaluate "4-7>=4"]
} {true true false}

######################################################################
####
# 
test ParseTreeEvaluator-4.2 {Test use of equality operator on strings} {
    list [evaluate "\"hello\" == \"hello\""] \
            [evaluate "\"hello\" != \"hello\""]
} {true false}

######################################################################
####
test ParseTreeEvaluator-4.3 {Test shift operators} {
    list [evaluate "2 << 2"] \
            [evaluate "-4 >> 1"] \
            [evaluate "-4L >>> 1ub"] \
            [evaluate "4UB >> 2ub"] \
            [evaluate "-4L >>> 1"] \
            [evaluate "4UB >> 2"]
} {8 -2 9223372036854775806L 1ub 9223372036854775806L 1ub}

######################################################################
####
# 
test ParseTreeEvaluator-5.0 {Test logical operators} {
    list [evaluate "\"hello\" == \"hello\" && 5>=5"] \
            [evaluate "\"hello\" != \"hello\" || 3<3"] \
            [evaluate "(3<=5) && (56 == 56) || (\"foo\" != \"foo\")"]
} {true false true}

######################################################################
####
# 
test ParseTreeEvaluator-5.1 {unary minus & unary logical not} {
    list [evaluate "!true"] \
            [evaluate "-7"]
} {false -7}


######################################################################
####
# 
test ParseTreeEvaluator-6.1 {check overflow} {
    list [evaluate "MaxUnsignedByte + 1ub"] \
	[evaluate "MaxInt + 1"] \
	[evaluate "MaxLong + 1L"] \
	[evaluate "MaxDouble + 1.0"] \
	[evaluate "MaxDouble * 2.0"]
} {0ub -2147483648 -9223372036854775808L 1.7976931348623E308 Infinity}


######################################################################
####
# 
test ParseTreeEvaluator-6.2 {check underflow} {
    list [evaluate "MinUnsignedByte - 1ub"] \
	[evaluate "MinInt - 1"] \
	[evaluate "MinLong - 1L"] \
	[evaluate "MinDouble - 1.0"] \
	[evaluate "MinDouble * 2.0"]
} {255ub 2147483647 9223372036854775807L -1.0 1.0E-323}


######################################################################
####
# 
test ParseTreeEvaluator-7.0 {Test simple functional if then else} {
    list [evaluate "(true)?(7):(6)\n"] \
            [evaluate "(true)?(7):(6.0)\n"]
} {7 7.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.1 {Test harder if then else} {
    list [evaluate "(false) ? (3/.5*4) : (pow(3.0,2.0))"]
} {9.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.2 {Test complicated expression within boolean test condition} {
    list [evaluate "((3<5) && (\"test\" == \"test\")) ? (3/.5*4) : (pow(3.0,2.0))"]

} {24.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.3 {Test nested if then elses} {
    list [evaluate "(true ? false: true ) ? (3/.5*4) : (pow(3.0,2.0))"]
} {9.0}

######################################################################
####
# 
test ParseTreeEvaluator-7.4 {Test many levels of parenthesis nesting} {
    list [evaluate "(true ? false: true ) ? (((((3/.5*4))))) : ((((((pow(3.0,2.0)))))))"]
   
} {9.0}

######################################################################
####
# 
test ParseTreeEvaluator-8.1.1 {Test bitwise operators} {
    list [evaluate "false&false"] \
            [evaluate "false|false"] \
            [evaluate "false\#false"] \
            [evaluate "~false"] \
            [evaluate "false&true"] \
            [evaluate "false|true"] \
            [evaluate "false\#true"] \
            [evaluate "true&false"] \
            [evaluate "true|false"] \
            [evaluate "true\#false"] \
            [evaluate "true&true"] \
            [evaluate "true|true"] \
            [evaluate "true\#true"] \
            [evaluate "~true"] \
            [evaluate "5&2"] \
            [evaluate "5|2"] \
            [evaluate "5\#4"] \
            [evaluate "~5"] \
            [evaluate "5ub&2ub"] \
            [evaluate "5ub|2ub"] \
            [evaluate "5ub\#4ub"] \
            [evaluate "~5ub"] \
            [evaluate "5L&2L"] \
            [evaluate "5L|2L"] \
            [evaluate "5L\#4L"] \
            [evaluate "~5L"]
} {false false false true false true true false true true true true true false 0 7 1 -6 0ub 7ub 1ub 250ub 0L 7L 1L -6L}

test ParseTreeEvaluator-8.1.2 {Test bitwise operators on doubles} {
    catch {evaluate "5.0&2.0"} errMsg1
    catch {evaluate "5.0|2.0"} errMsg2
    catch {evaluate "5.0\#4.0"} errMsg3
    catch {evaluate "~5.0"} errMsg4
    list " $errMsg1\n $errMsg2\n $errMsg\n $errMsg4" 
	
} {{ ptolemy.kernel.util.IllegalActionException: bitwiseAnd operation not supported between ptolemy.data.DoubleToken '5.0' and ptolemy.data.DoubleToken '2.0'
 ptolemy.kernel.util.IllegalActionException: bitwiseOr operation not supported between ptolemy.data.DoubleToken '5.0' and ptolemy.data.DoubleToken '2.0'
 ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.IntMatrixToken '[1, 0; 0, 1]' and ptolemy.data.IntMatrixToken '[1, 2; 3, 4]'
 ptolemy.kernel.util.IllegalActionException: bitwiseNot operation not supported between ptolemy.data.DoubleToken '5.0' and ptolemy.data.DoubleToken '5.0'}}

test ParseTreeEvaluator-8.1.3 {Test bitwise operators on mixed types} {
    list [evaluate "5L&2"] \
            [evaluate "5|2L"] \
            [evaluate "5ub\#4"] \
            [evaluate "~5L"] \
            [evaluate "5L&2ub"] \
            [evaluate "5ub|2"] \
            [evaluate "5ub\#4ub"] \
            [evaluate "~5ub"]\
            [evaluate "5L&2L"]
} {0L 7L 1 -6L 0L 7 1ub 250ub 0L}

######################################################################
####
# 
test ParseTreeEvaluator-8.2 {Test more complicated bitwise operations, and bitwise ops on booleans} {
    list [evaluate "~(5 & 2 | 4)"] \
            [evaluate "(5>4) & (2==2)"] \
            [evaluate "(false) | (2!=2)"]
} {-5 true false}

######################################################################
####
# Need to test that the tree can be reevaluated an arbitrary number of times
test ParseTreeEvaluator-9.0 {Check that evaluateuation of the parse tree does not change the parse tree} {
    list [evaluate "2+3"] \
            [evaluate "2-3"] \
            [evaluate "2*3"] \
            [evaluate "2/4"] \
            [evaluate "11 % 3"]
} {5 -1 6 0 2}

######################################################################
####
# Test matrix construction, when term types are identical.
test ParseTreeEvaluator-12.1 {Test basic matrix construction.} {
    list [evaluate "\[1,2,3;4,5,6;7,8,9\]" ]
} {{[1, 2, 3; 4, 5, 6; 7, 8, 9]}}

# Test matrix construction, when term types are heterogeneous.
test ParseTreeEvaluator-12.2 {Test matrix construction.} {
    list [evaluate "\[1.0;2;3j\]" ]
} {{[1.0 + 0.0i; 2.0 + 0.0i; 0.0 + 3.0i]}}

# Test matrix construction with ranges.
test ParseTreeEvaluator-12.3 {Test matrix construction.} {
    list [evaluate {[1:2:9; 2:2:10]} ]
} {{[1, 3, 5, 7, 9; 2, 4, 6, 8, 10]}}

######################################################################
####

test ParseTreeEvaluator-13.1 {Test array method calls.} {
    list [evaluate "cast(int, {1, 2, 3}.getElement(1))"]
} {2}

# Test record construction,
test ParseTreeEvaluator-13.2 {Test record construction.} {
    list [evaluate {{a=1,b=2.4}} ] \
         [evaluate {{b=1,a=2.4}} ] \
         [evaluate {{a=1, b=2}=={b=2, a=1}}]
 } {{{a=1, b=2.4}} {{a=2.4, b=1}} true}

######################################################################
####
# Test evaluate
test ParseTreeEvaluator-14.0 {Test eval().} {
    list [evaluate "cast(int,eval(\"1+1\"))" ] \
	[evaluate {cast([double],eval("[1.0, 2.0; 3.0, 4.0]"))}]
 } {2 {[1.0, 2.0; 3.0, 4.0]}}

######################################################################
####
# 
test ParseTreeEvaluator-16.0 {Test method calls on arrays, matrices, etc.} {
    list [evaluate "cast({int},{1,2,3}.add({3,4,5}))"] \
            [evaluate "cast({int},{{a=1,b=2},{a=3,b=4},{a=5,b=6}}.get(\"a\"))"] \
            [evaluate "cast(\[int\],create({1,2,3,4,5,6},2,3))"] \
            [evaluate "cast({int},{1,1,1,1}.leftShift({1,2,3,4}))"]
} {{{4, 6, 8}} {{1, 3, 5}} {[1, 2, 3; 4, 5, 6]} {{2, 4, 8, 16}}}

test ParseTreeFreeVariableCollector-16.2 {Test record indexing} {
    list [evaluate "true ? 2 : ({a={0,0,0}}.a).length()"] \
            [evaluate "false ? 2 : ({a={0,0,0}}.a).length()"]
} {2 3}

####################################################################

test ParseTreeEvaluator-17.1 {Test correct scoping in function definitions.} {
    list [evaluate "function(x) x + 3.0"] [evaluate "function(x:int) x + 3.0"]
} {{(function(x) (x+3.0))} {(function(x:int) (x+3.0))}}

test ParseTreeEvaluator-17.2 {Test function in function.} {
    list [evaluate "function(x) min({x,3.0})"] [evaluate "function(x:int) min({x, 3.0})"]
} {{(function(x) min({x, 3.0}))} {(function(x:int) min({x, 3.0}))}}

test ParseTreeEvaluator-17.3 {Test record in function.} {
    list [evaluate "function(x:{a=int,b=int}) x.a+x.b"] [evaluate "function(x:{a=int,b=int}) x.a"]
} {{(function(x:{a=int, b=int}) (x.a()+x.b()))} {(function(x:{a=int, b=int}) x.a())}}

test ParseTreeEvaluator-17.4 {Test double in function.} {
    list [evaluate "function(x:double) double"]
} {{(function(x:double) double)}}

####################################################################

test ParseTreeEvaluator-18.1 {Test Matrix Scalar Multiplication} {
    list [evaluate {[1,2]*1}] \
         [evaluate {1*[1,2]}] \
         [evaluate {[1,2]*1.0}] \
         [evaluate {1.0*[1,2]}] \
         [evaluate {j*[1.0,2.0]}] \
         [evaluate {[1.0,2.0]*j}] \
         [evaluate {j*[1,2]}] \
         [evaluate {[1,2]*j}] \
         [evaluate {[j, 2*j]*2}] \
         [evaluate {3*[j, 2*j]}]
 } {{[1, 2]} {[1, 2]} {[1.0, 2.0]} {[1.0, 2.0]} {[0.0 + 1.0i, 0.0 + 2.0i]} {[0.0 + 1.0i, 0.0 + 2.0i]} {[0.0 + 1.0i, 0.0 + 2.0i]} {[0.0 + 1.0i, 0.0 + 2.0i]} {[0.0 + 2.0i, 0.0 + 4.0i]} {[0.0 + 3.0i, 0.0 + 6.0i]}}

####################################################################
  
test ParseTreeEvaluator-18.2 {Test various matrix ops} {
    list [evaluate {[1, 2; 3, 4]*[2, 2; 2, 2]}] \
         [evaluate {[1, 2; 3, 4]+[2, 2; 2, 2]}] \
         [evaluate {[1, 2; 3, 4]-[2, 2; 2, 2]}] \
         [evaluate {[1, 2; 3, 4]^2}] \
         [evaluate {[1, 2; 3, 4]==[2, 2; 2, 2]}] \
         [evaluate {[1, 2; 3, 4]!=[2, 2; 2, 2]}]
} {{[6, 6; 14, 14]} {[3, 4; 5, 6]} {[-1, 0; 1, 2]} {[7, 10; 15, 22]} false true}

test ParseTreeEvaluator-18.3 {Test various matrix ops on mixed types} {
    list [evaluate {[1, 2; 3, 4]*[2.0, 2.0; 2.0, 2.0]}] \
         [evaluate {[1, 2; 3, 4]+[2L, 2L; 2L, 2L]}] \
         [evaluate {[1.0, 2.0; 3.0, 4.0]-[2, 2; 2, 2]}] \
         [evaluate {[1, 2; 3, 4]==[1.0, 2.0; 3.0, 4.0]}] \
         [evaluate {[1.0, 2.0; 3.0, 4.0]!=[2, 2; 2, 2]}]
} {{[6.0, 6.0; 14.0, 14.0]} {[3L, 4L; 5L, 6L]} {[-1.0, 0.0; 1.0, 2.0]} true true}

####################################################################
  
test ParseTreeEvaluator-19.1 {Test various array ops} {
    list [evaluate {{1, 2}*{2, 2}}] \
         [evaluate {{1, 2}+{2, 2}}] \
         [evaluate {{1, 2}-{2, 2}}] \
         [evaluate {{1, 2}^2}] \
         [evaluate {{1, 2}=={2, 2}}] \
         [evaluate {{1, 2}!={2, 2}}] \
         [evaluate {{1, 2}%{2, 2}}]
 } {{{2, 4}} {{3, 4}} {{-1, 0}} {{1, 4}} false true {{1, 0}}}

test ParseTreeEvaluator-19.2 {Test various array ops on mixed types} {
    list [evaluate {{1, 2}*{2, 2}}] \
         [evaluate {{1, 2}+{2, 2}}] \
         [evaluate {{1, 2}-{2, 2}}] \
         [evaluate {{1, 2}^2}] \
         [evaluate {{1, 2}=={2, 2}}] \
         [evaluate {{1, 2}!={2, 2}}] \
         [evaluate {{1, 2}%{2, 2}}]
 } {{{2, 4}} {{3, 4}} {{-1, 0}} {{1, 4}} false true {{1, 0}}}

####################################################################

test ParseTreeEvaluator-20.1 {Test Addition between incomparable data types} {
    catch {list [evaluate {1.0+1L}]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert token 1.0 to type scalar, because scalar is not a concrete type.}}

####################################################################

test ParseTreeEvaluator-20.2 {Test Matrix Scalar Addition} {
    list [evaluate {[1,2]+1}] \
         [evaluate {1+[1,2]}] \
         [evaluate {[1,2]+1.0}] \
         [evaluate {1.0+[1,2]}] \
         [evaluate {j+[1.0,2.0]}] \
         [evaluate {[1.0,2.0]+j}] \
         [evaluate {j+[1,2]}] \
         [evaluate {[1,2]+j}] \
         [evaluate {[j, 2*j]+1}] \
         [evaluate {1+[j, 2*j]}]
 } {{[2, 3]} {[2, 3]} {[2.0, 3.0]} {[2.0, 3.0]} {[1.0 + 1.0i, 2.0 + 1.0i]} {[1.0 + 1.0i, 2.0 + 1.0i]} {[1.0 + 1.0i, 2.0 + 1.0i]} {[1.0 + 1.0i, 2.0 + 1.0i]} {[1.0 + 1.0i, 1.0 + 2.0i]} {[1.0 + 1.0i, 1.0 + 2.0i]}}

####################################################################

test ParseTreeEvaluator-21.1 {Test Matrix Scalar Subtraction} {
    list [evaluate {[1,2]-1}] \
         [evaluate {1-[1,2]}] \
         [evaluate {[1,2]-1.0}] \
         [evaluate {1.0-[1,2]}] \
         [evaluate {j-[1.0,2.0]}] \
         [evaluate {[1.0,2.0]-j}] \
         [evaluate {j-[1,2]}] \
         [evaluate {[1,2]-j}] \
         [evaluate {[j, 2*j]-1}] \
         [evaluate {1-[j, 2*j]}]
 } {{[0, 1]} {[0, -1]} {[0.0, 1.0]} {[-0.0, -1.0]} {[-1.0 + 1.0i, -2.0 + 1.0i]} {[1.0 - 1.0i, 2.0 - 1.0i]} {[-1.0 + 1.0i, -2.0 + 1.0i]} {[1.0 - 1.0i, 2.0 - 1.0i]} {[-1.0 + 1.0i, -1.0 + 2.0i]} {[1.0 - 1.0i, 1.0 - 2.0i]}} 

####################################################################

test ParseTreeEvaluator-22.2 {Test Matrix Scalar Modulo} {
    list [evaluate {[2,3]%4}] \
	 [evaluate {[5,4]%1.5}] \
	 [evaluate {[2,3]%2L}] \
	 [evaluate {[2L,3L]%2}] \
	 [evaluate {[2L,3L]%3L}] \
	 [evaluate {[2.5,1.7]%2}] \
	 [evaluate {[2.5,1.7]%1.3}]
} {{[2, 3]} {[0.5, 1.0]} {[0L, 1L]} {[0L, 1L]} {[2L, 0L]} {[0.5, 1.7]} {[1.2, 0.4]}}

####################################################################

test ParseTreeEvaluator-22.3 {Test Matrix Scalar Division} {
    list [evaluate {[1,2]/1}] \
         [evaluate {[1,2]/1.0}] \
         [evaluate {[1.0,2.0]/j}] \
         [evaluate {[j,2*j]/2}] \
         [evaluate {[1,2]/j}] \
     } {{[1, 2]} {[1.0, 2.0]} {[0.0 - 1.0i, 0.0 - 2.0i]} {[0.0 + 0.5i, 0.0 + 1.0i]} {[0.0 - 1.0i, 0.0 - 2.0i]}}


####################################################################

test ParseTreeEvaluator-24.1 {Test Function closures} {
    list [evaluate {function(x:double) x*5.0}] \
         [evaluate {(function(x:double) x*5.0)(10)}] \
     } {{(function(x:double) (x*5.0))} 50.0}

####################################################################

test ParseTreeEvaluator-25.1 {Test remainder operator} {
    list [evaluate {3.0 % 2.0}] \
         [evaluate {-3.0 % 2.0}] \
         [evaluate {-3.0 % -2.0}] \
         [evaluate {3.0 % -2.0}] \
     } {1.0 -1.0 -1.0 1.0}

####################################################################

test ParseTreeEvaluator-26.0 {Test record operations} {
    list [evaluate {{a=1, b=2}=={a=1.0, b=2.0+0.0i}}] \
         [evaluate {{a=1, b=2}.equals({a=1.0, b=2.0+0.0i})}] \
         [evaluate {{a=1, b=2}.equals({b=2, a=1})}]
 } {true false true}

test ParseTreeEvaluator-26.1 {Test record operations} {
    list [evaluate {{a=1,b=2}.a}] \
         [evaluate {{a=1,b=2}.a()}] \
         [evaluate {{foodCost=40, hotelCost=100} + {foodCost=20, taxiCost=20}}]
 } {1 1 {{foodCost=60}}}

####################################################################

test ParseTreeEvaluator-27.0 {Test Error message} {
    catch {list [evaluate {1.0+im}]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: The ID im is undefined.}}
