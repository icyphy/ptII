# Tests for the PtParser class
#
# @Author: Neil Smyth
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

proc printTree {root} {
    set printer [java::new ptolemy.data.expr.ParseTreeWriter]
    $printer printParseTree $root
}

proc theTest {expression} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    printTree $root
}

######################################################################
####
# 
test ParseTreeWriter-2.2 {Construct a Parser, try simple integer expressions} {
    list [theTest "2 + 3 + 4"] [theTest "2 - 3 - 4"] [theTest "2 * 3 * 4"] [theTest "7 % 5"] [theTest "12 / 2 / 3"]
} {(2+3+4) (2-3-4) (2*3*4) (7%5) (12/2/3)}

######################################################################
####
# 
test ParseTreeWriter-2.3 {Construct a Parser, try complex integer expressions} {
    list [theTest "-(2 + (3) + 4*(3- 4 % 3)*(12/12))\n"]
} {-(2+3+(4*(3-(4%3))*(12/12)))}
######################################################################
####
# 
test ParseTreeWriter-2.4 {Construct a Parser, try simple double expressions} {
    list [theTest "2.0 + 3.5 + 4.2"] [theTest "2.2 - 3.6 - 4.2"] [theTest "2.0 * 3.5 * 4.2"] [theTest "7.1 % 5.5"] [theTest "12.0 / 2.4 / 2.5"]
} {(2.0+3.5+4.2) (2.2-3.6-4.2) (2.0*3.5*4.2) (7.1%5.5) (12.0/2.4/2.5)}

######################################################################
####
# 
test ParseTreeWriter-2.5 {Construct a Parser, try complex double expressions} {
    list [theTest "-(2.2 + (3.7%1.5) + 4.0*(3.2- 4.2 % 3.0)*(12.0/2.4/2.5/2.0))"]
} {-(2.2+(3.7%1.5)+(4.0*(3.2-(4.2%3.0))*(12.0/2.4/2.5/2.0)))}

######################################################################
####
# 
test ParseTreeWriter-2.6 {Construct a Parser, try creating complex numbers} {
    list [theTest "2i"] [theTest "3 + 2i"]
} {{0.0 + 2.0i} {(3+0.0 + 2.0i)}}

######################################################################
####
# 
test ParseTreeWriter-2.7 {Construct a Parser, try integer format specifiers} {
    list [theTest "29"] [theTest "035"] [theTest "0x1D"] [theTest "0X1d"] [theTest "0xbub"]
} {29 29 29 29 11ub}

######################################################################
####
# 
test ParseTreeWriter-2.8 {Construct a Parser, try long format specifiers} {
    list [theTest "29l"] [theTest "035L"] [theTest "0x1Dl"] [theTest "0X1dL"]
} {29L 29L 29L 29L}

######################################################################
####
# 
test ParseTreeWriter-2.9 {Construct a Parser, try floating point format specifiers} {
    list [theTest "1.8e1"] [theTest ".18E2"] [theTest "18.0f"] [theTest "18.0D"]
} {18.0 18.0 18.0 18.0}

######################################################################
####
# 
test ParseTreeWriter-3.0 {Construct a Parser,mixing doubles, strings and integers using arithmetic} {
    list [theTest "-(2*9.5 + (3.5/7) + 4/.5) +  \" hello \" + (3*5 -4)\n"]
} {{(-((2*9.5)+(3.5/7)+(4/0.5))+" hello "+((3*5)-4))}}

######################################################################
####
# 
test ParseTreeWriter-4.0 {Construct a Parser, try basic relational operators} {
    list [theTest "2<4"] [theTest "4<=4"] [theTest "4>=4"] [theTest "4>7"] [theTest "5==4"] [theTest "5!=4"]
} {(2<4) (4<=4) (4>=4) (4>7) (5==4) (5!=4)}

######################################################################
####
# 
test ParseTreeWriter-4.1 {Construct a Parser, try  relational operators with arithetic operators} {
    list [theTest "(2)<(4*5 -9)"] [theTest "4<=4*7"] [theTest "4-7>=4"]
} {(2<((4*5)-9)) (4<=(4*7)) ((4-7)>=4)}

######################################################################
####
# 
test ParseTreeWriter-4.2 {Construct a Parser,test use of equality operator on strings} {
    list [theTest "\"hello\" == \"hello\""] [theTest "\"hello\" != \"hello\""]
} {(\"hello\"==\"hello\") (\"hello\"!=\"hello\")}

######################################################################
####
test ParseTreeWriter-4.3 {Construct a Parser,test shift operators} {
    list [theTest "2 << 2"] [theTest "-4 >> 1"] [theTest "-4L >>> 1"] [theTest "4UB >> 2"]
} {(2<<2) (-4>>1) (-4L>>>1) (4ub>>2)}

######################################################################
####
# 
test ParseTreeWriter-5.0 {Construct a Parser, test use of logical operators} {
    list [theTest "\"hello\" == \"hello\" && 5>=5"] [theTest "\"hello\" != \"hello\" || 3<3"] [theTest "(3<=5) && (56 == 56) || (\"foo\" != \"foo\")"]
} {((\"hello\"==\"hello\")&&(5>=5)) ((\"hello\"!=\"hello\")||(3<3)) (((3<=5)&&(56==56))||(\"foo\"!=\"foo\"))}

######################################################################
####
# 
test ParseTreeWriter-5.1 {Construct a Parser, unary minus & unary logical not} {
    list [theTest "!true"] [theTest "-7"]
} {!true -7}


######################################################################
####
# 
test ParseTreeWriter-7.0 {Construct a Parser, try simple functional if then else} {
    list [theTest "(true)?(7):(6)\n"] [theTest "(true)?(7):(6.0)\n"]
} {(true?7:6) (true?7:6.0)}

######################################################################
####
# 
test ParseTreeWriter-7.1 {Construct a Parser, try harder if then else} {
    list [theTest "(false) ? (3/.5*4) : (pow(3.0,2.0))"]
} {{(false?(3/0.5*4):pow(3.0, 2.0))}}

######################################################################
####
# 
test ParseTreeWriter-7.2 {Test complicated expression within boolean test condition} {
    list [theTest "((3<5) && (\"test\" == \"test\")) ? (3/.5*4) : (pow(3.0,2.0))"]

} {{(((3<5)&&("test"=="test"))?(3/0.5*4):pow(3.0, 2.0))}}

######################################################################
####
# 
test ParseTreeWriter-7.3 {Test nested if then elses} {
    list [theTest "(true ? false: true ) ? (3/.5*4) : (pow(3.0,2.0))"]
} {{((true?false:true)?(3/0.5*4):pow(3.0, 2.0))}}

######################################################################
####
# 
test ParseTreeWriter-7.4 {Test many levels of parenthesis nesting} {
    list [theTest "(true ? false: true ) ? (((((3/.5*4))))) : ((((((pow(3.0,2.0)))))))"]
   
} {{((true?false:true)?(3/0.5*4):pow(3.0, 2.0))}}

######################################################################
####
# 
test ParseTreeWriter-8.1 {Test bitwise operators} {
    list [theTest "5&2"] [theTest "5|2"] [theTest "5\#4"] [theTest "~5"]
} {(5&2) (5|2) (5#4) ~5}

######################################################################
####
# 
test ParseTreeWriter-8.2 {Test more complicated bitwise operations, and bitwise ops on booleans} {
    list [theTest "~(5 & 2 | 4)"] [theTest "(5>4) & (2==2)"] [theTest "(false) | (2!=2)"]
} {~((5&2)|4) ((5>4)&(2==2)) (false|(2!=2))}

######################################################################
####
# Need to test that the tree can be reevaluated an arbitrary number of times
test ParseTreeWriter-9.0 {Check that evaluation of the parse tree does not change the parse tree} {
    list [theTest "2+3"] [theTest "2-3"] [theTest "2*3"] [theTest "2/4"] [theTest "11 % 3"]
  
} {(2+3) (2-3) (2*3) (2/4) (11%3)}

######################################################################
####
# Need to test that constants can be registered and recognized by the parser.
test ParseTreeWriter-10.0 {Test that constants can be registered and recognized by the parser} {
    list [theTest "half + one + neil"] [theTest "boolean == true"] [theTest "long"]
} {(half+one+neil) (boolean==true) long}

######################################################################
####
# Need to test that functions can access methods registered in the 
# search path of the parser. can be registered and recognized by the parser.
# FIXME: this test is not finished.
test ParseTreeWriter-10.1 {Test that functions can access registered classes.} {
    list [theTest "min(1,3)"] [theTest "sin(30*PI/180)"]
} {{min(1, 3)} sin((30*PI/180))}

#

######################################################################
####
# Test matrix construction, when term types are identical.
test ParseTreeWriter-12.1 {Test basic matrix construction.} {
    list [theTest "\[1,2,3;4,5,6;7,8,9\]" ]
} {{[1, 2, 3; 4, 5, 6; 7, 8, 9]}}

# Test matrix construction, when term types are heterogeneous.
test ParseTreeWriter-12.2 {Test matrix construction.} {
    list [theTest "\[1.0;2;3j\]" ]
} {{[1.0; 2; 0.0 + 3.0i]}}

######################################################################
####
# Test array reference.
test ParseTreeWriter-13.0 {Test array reference.} {
    list [theTest "v1(0+1,2)+v1(0, v2-1)"] [theTest "cast(complex,v1(0+1,2)+v1(0, v2-1).add(v2))"]
} {{(v1((0+1), 2)+v1(0, (v2-1)))} {cast(complex, (v1((0+1), 2)+v1(0, (v2-1)).add(v2)))}}

test ParseTreeWriter-13.1 {Test array method calls.} {
    list [theTest "cast(int, {1, 2, 3}.getElement(1))"]
} {{cast(int, {1, 2, 3}.getElement(1))}}

# Test record construction,
test ParseTreeWriter-13.2 {Test record construction.} {
    list [theTest "{a=1,b=2.4}" ]
} {{{a=1, b=2.4}}}

######################################################################
####
# Test eval
test ParseTreeWriter-14.0 {Test eval inference.} {
    list [theTest "eval(\"1+1\")" ]
} {eval(\"1+1\")}

######################################################################
####
# 
test ParseTreeWriter-16.0 {Test method calls on arrays, matrices, etc.} {
    list [theTest "cast({int},{1,2,3}.add({3,4,5}))"] [theTest "cast({int},{{a=1,b=2},{a=3,b=4},{a=5,b=6}}.get(\"a\"))"] [theTest "cast(\[int\],create({1,2,3,4,5,6},2,3))"] [theTest "cast({int},{1,1,1,1}.leftShift({1,2,3,4}))"]
} {{cast({int}, {1, 2, 3}.add({3, 4, 5}))} {cast({int}, {{a=1, b=2}, {a=3, b=4}, {a=5, b=6}}.get("a"))} {cast([int], create({1, 2, 3, 4, 5, 6}, 2, 3))} {cast({int}, {1, 1, 1, 1}.leftShift({1, 2, 3, 4}))}}

test ParseTreeWriter-16.2 {Test record indexing} {
    list [theTest "true ? 2 : ({a={0,0,0}}.a).length()"] [theTest "false ? 2 : ({a={0,0,0}}.a).length()"]
} {{(true?2:{a={0, 0, 0}}.a().length())} {(false?2:{a={0, 0, 0}}.a().length())}}

test ParseTreeWriter-16.3 {Test property} {
    list [theTest "getProperty(\"ptolemy.ptII.dir\")"] [theTest "property(\"ptolemy.ptII.dir\") + \"foo\""]
} {getProperty(\"ptolemy.ptII.dir\") (property(\"ptolemy.ptII.dir\")+\"foo\")}


test ParseTreeWriter-17.1 {Test correct scoping in function definitions.} {
    list [theTest "function(x) x + p3"] [theTest "4 + p1(6)"]
} {{(function(x) (x+p3))} (4+p1(6))}

test ParseTreeWriter-17.2 {Test nested function definitions.} {
    list [theTest "function (y) function(x) x + y + p3"] [theTest "p1(6)"] [theTest "p2(4)"]
} {{(function(y) (function(x) (x+y+p3)))} p1(6) p2(4)}

test ParseTreeWriter-17.2 {Test nested function definitions.} {
    list [theTest "function (y) function(x) double + y + p3"] [theTest "p1(6)"] [theTest "p2(4)"]
} {{(function(y) (function(x) (double+y+p3)))} p1(6) p2(4)}

