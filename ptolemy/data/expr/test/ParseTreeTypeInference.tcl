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

proc theTest {expression} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} $expression]
    [inferTypes $root] toString
}

######################################################################
####
# 
test ParseTreeTypeInference-2.2 {Construct a Parser, try simple integer expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "2 + 3 + 4"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

    set root [ $p1 {generateParseTree String} "2 - 3 - 4"]
    set res1  [[ $root evaluateParseTree ] getType]
    set type1 [inferTypes $root]

    set root [ $p1 {generateParseTree String} "2 * 3 * 4"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]

    set root [ $p1 {generateParseTree String} "7 % 5"]
    set res3  [[ $root evaluateParseTree ] getType]
    set type3 [inferTypes $root]

    set root [ $p1 {generateParseTree String} "12 / 2 / 3"]
    set res4  [[ $root evaluateParseTree ] getType]
    set type4 [inferTypes $root]

    list [$res equals $type] [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-2.3 {Construct a Parser, try complex integer expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "-(2 + (3) + 4*(3- 4 % 3)*(12/12))\n"]
    # Note that dividing an Int by an Int can give a Double, here I want 
    # all nodes the parse tree to have IntTokens
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

    list [$res equals $type]
} {1}
######################################################################
####
# 
test ParseTreeTypeInference-2.4 {Construct a Parser, try simple double expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "2.0 + 3.5 + 4.2"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

    set root [ $p1 {generateParseTree String} "2.2 - 3.6 - 4.2"]
    set res1  [[ $root evaluateParseTree ] getType]
    set type1 [inferTypes $root]

    set root [ $p1 {generateParseTree String} "2.0 * 3.5 * 4.2"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]
    
    set root [ $p1 {generateParseTree String} "7.1 % 5.5"]
    set res3  [[ $root evaluateParseTree ] getType]
    set type3 [inferTypes $root]
    
    set root [ $p1 {generateParseTree String} "12.0 / 2.4 / 2.5"]
    set res4  [[ $root evaluateParseTree ] getType]
    set type4 [inferTypes $root]

    list [$res equals $type] [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-2.5 {Construct a Parser, try complex double expressions} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "-(2.2 + (3.7%1.5) + 4.0*(3.2- 4.2 % 3.0)*(12.0/2.4/2.5/2.0))" ]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

    list [$res equals $type]
} {1}
######################################################################
####
# 
test ParseTreeTypeInference-2.6 {Construct a Parser, try creating complex numbers} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "2i"]
    set res1  [[ $root evaluateParseTree ] getType]
    set type1 [inferTypes $root]

    set root [ $p {generateParseTree String} "3 + 2i"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]

    list [$res equals $type] [$res2 equals $type2]
} {1 1}

######################################################################
####
# 
test ParseTreeTypeInference-2.7 {Construct a Parser, try integer format specifiers} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "29"]
    set res1  [[ $root evaluateParseTree ] getType]
    set type1 [inferTypes $root]

    set root [ $p {generateParseTree String} "035"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]
 
    set root [ $p {generateParseTree String} "0x1D"]
    set res3  [[ $root evaluateParseTree ] getType]
    set type3 [inferTypes $root]
  
    set root [ $p {generateParseTree String} "0X1d"]
    set res4  [[ $root evaluateParseTree ] getType]
    set type4 [inferTypes $root]

    set root [ $p {generateParseTree String} "0xbub"]
    set res5  [[ $root evaluateParseTree ] getType]
    set type5 [inferTypes $root]

    list [$res equals $type] [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-2.8 {Construct a Parser, try long format specifiers} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "29l"]
    set res1  [[ $root evaluateParseTree ] getType]
    set type1 [inferTypes $root]

    set root [ $p {generateParseTree String} "035L"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]

    set root [ $p {generateParseTree String} "0x1Dl"]
    set res3  [[ $root evaluateParseTree ] getType]
    set type3 [inferTypes $root]
  
    set root [ $p {generateParseTree String} "0X1dL"]
    set res4  [[ $root evaluateParseTree ] getType]
    set type4 [inferTypes $root]

     list [$res equals $type] [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3]
} {1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-2.9 {Construct a Parser, try floating point format specifiers} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "1.8e1"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]

    set root [ $p {generateParseTree String} ".18E2"]
    set res3  [[ $root evaluateParseTree ] getType]
    set type3 [inferTypes $root]
  
    set root [ $p {generateParseTree String} "18.0f"]
    set res4  [[ $root evaluateParseTree ] getType]
    set type4 [inferTypes $root]

    set root [ $p {generateParseTree String} "18.0D"]
    set res5  [[ $root evaluateParseTree ] getType]
    set type5 [inferTypes $root]

    list [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] [$res5 equals $type5] 
} {1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-3.0 {Construct a Parser,mixing doubles, strings and integers using arithmetic} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "-(2*9.5 + (3.5/7) + 4/.5) +  \" hello \" + (3*5 -4)\n"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

    list [$res equals $type]
} {1}

######################################################################
####
# 
test ParseTreeTypeInference-4.0 {Construct a Parser, try basic relational operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "2<4"]
    set root2 [ $p {generateParseTree String} "4<=4"]
    set root3 [ $p {generateParseTree String} "4>=4"]
    set root4 [ $p {generateParseTree String} "4>7"]
    set root5 [ $p {generateParseTree String} "5==4"]
    set root6 [ $p {generateParseTree String} "5!=4"]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
    set res3  [[ $root3 evaluateParseTree ] getType]
    set type3 [inferTypes $root3]
    set res4  [[ $root4 evaluateParseTree ] getType]
    set type4 [inferTypes $root4]
    set res5  [[ $root5 evaluateParseTree ] getType]
    set type5 [inferTypes $root5]
    set res6  [[ $root6 evaluateParseTree ] getType]
    set type6 [inferTypes $root6]

    list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4]  [$res5 equals $type5] [$res6 equals $type6]
} {1 1 1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-4.1 {Construct a Parser, try  relational operators with arithetic operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "(2)<(4*5 -9)"]
    set root2 [ $p {generateParseTree String} "4<=4*7"]
    set root3 [ $p {generateParseTree String} "4-7>=4"]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
    set res3  [[ $root3 evaluateParseTree ] getType]
    set type3 [inferTypes $root3]

    list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] 
} {1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-4.2 {Construct a Parser,test use of equality operator on strings} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "\"hello\" == \"hello\""]
    set root2 [ $p {generateParseTree String} "\"hello\" != \"hello\""]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
   
    list [$res1 equals $type1] [$res2 equals $type2] 
} {1 1}


######################################################################
####
test ParseTreeTypeInference-4.3 {Construct a Parser,test shift operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "2 << 2"]
    set root2 [ $p {generateParseTree String} "-4 >> 1"]
    set root3 [ $p {generateParseTree String} "-4L >>> 1"]
    set root4 [ $p {generateParseTree String} "4UB >> 2"]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
    set res3  [[ $root3 evaluateParseTree ] getType]
    set type3 [inferTypes $root3]
    set res4  [[ $root4 evaluateParseTree ] getType]
    set type4 [inferTypes $root4]
   
   list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-5.0 {Construct a Parser, test use of logical operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "\"hello\" == \"hello\" && 5>=5"]
    set root2 [ $p {generateParseTree String} "\"hello\" != \"hello\" || 3<3"]
    set root3 [ $p {generateParseTree String} "(3<=5) && (56 == 56) || (\"foo\" != \"foo\")"]
    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
    set res3  [[ $root3 evaluateParseTree ] getType]
    set type3 [inferTypes $root3]

   list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3]
} {1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-5.1 {Construct a Parser, unary minus & unary logical not} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "!true"]
    set root2 [ $p {generateParseTree String} "-7"]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]

   list [$res1 equals $type1] [$res2 equals $type2]
} {1 1}


######################################################################
####
# 
test ParseTreeTypeInference-7.0 {Construct a Parser, try simple functional if then else} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(true)?(7):(6)\n"]
    set res1  [[ $root evaluateParseTree ] getType]
    set type1 [inferTypes $root]
    set root [ $p1 {generateParseTree String} "(true)?(7):(6.0)\n"]
    set res2  [[ $root evaluateParseTree ] getType]
    set type2 [inferTypes $root]

   list [$res1 equals $type1] [$res2 equals $type2]
} {1 1}

######################################################################
####
# 
test ParseTreeTypeInference-7.1 {Construct a Parser, try harder if then else} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(false) ? (3/.5*4) : (pow(3.0,2.0))"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

   list [$res equals $type]
} {1}
######################################################################
####
# 
test ParseTreeTypeInference-7.2 {Test complicated expression within boolean test condition} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "((3<5) && (\"test\" == \"test\")) ? (3/.5*4) : (pow(3.0,2.0))"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

   list [$res equals $type]
} {1}
######################################################################
####
# 
test ParseTreeTypeInference-7.3 {Test nested if then elses} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(true ? false: true ) ? (3/.5*4) : (pow(3.0,2.0))"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

   list [$res equals $type]
} {1}
######################################################################
####
# 
test ParseTreeTypeInference-7.4 {Test many levels of parenthesis nesting} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(true ? false: true ) ? (((((3/.5*4))))) : ((((((pow(3.0,2.0)))))))"]
    set res  [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]

    list [$res equals $type]
} {1}

######################################################################
####
# 
test ParseTreeTypeInference-8.1 {Test bitwise operators} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "5&2"]
    set root2 [ $p1 {generateParseTree String} "5|2"]
    set root3 [ $p1 {generateParseTree String} "5\#4"]
    set root4 [ $p1 {generateParseTree String} "~5"]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
    set res3  [[ $root3 evaluateParseTree ] getType]
    set type3 [inferTypes $root3]
    set res4  [[ $root4 evaluateParseTree ] getType]
    set type4 [inferTypes $root4]

   list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4]
} {1 1 1 1}

######################################################################
####
# 
test ParseTreeTypeInference-8.2 {Test more complicated bitwise operations, and bitwise ops on booleans} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "~(5 & 2 | 4)"]
    set root2 [ $p1 {generateParseTree String} "(5>4) & (2==2)"]
    set root3 [ $p1 {generateParseTree String} "(false) | (2!=2)"]

    set res1  [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]
    set res2  [[ $root2 evaluateParseTree ] getType]
    set type2 [inferTypes $root2]
    set res3  [[ $root3 evaluateParseTree ] getType]
    set type3 [inferTypes $root3]

   list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3]
} {1 1 1}

######################################################################
####
# Need to test that the tree can be reevaluated an arbitrary number of times
test ParseTreeTypeInference-9.0 {Check that evaluation of the parse tree does not change the parse tree} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "2+3"]
    set root2 [ $p1 {generateParseTree String} "2-3"]
    set root3 [ $p1 {generateParseTree String} "2*3"]
    set root4 [ $p1 {generateParseTree String} "2/4"]
    set root5 [ $p1 {generateParseTree String} "11 % 3"]
    
    set res1a [[ $root1 evaluateParseTree ] getType]
    set type1a [inferTypes $root1]
    set res1b [[ $root1 evaluateParseTree ] getType]
    set type1b [inferTypes $root1]

    set res2a [[ $root2 evaluateParseTree ] getType]
    set type2a [inferTypes $root2]
    set res2b [[ $root2 evaluateParseTree ] getType]
    set type2b [inferTypes $root2]

    set res3a [[ $root3 evaluateParseTree ] getType]
    set type3a [inferTypes $root3]
    set res3b [[ $root3 evaluateParseTree ] getType]
    set type3b [inferTypes $root3]

    set res4a [[ $root4 evaluateParseTree ] getType]
    set type4a [inferTypes $root4]
    set res4b [[ $root4 evaluateParseTree ] getType]
    set type4b [inferTypes $root4]

    set res5a [[ $root5 evaluateParseTree ] getType]
    set type5a [inferTypes $root5]
    set res5b [[ $root5 evaluateParseTree ] getType]
    set type5b [inferTypes $root5]

   list [$res1a equals $type1a] [$res2a equals $type2a] [$res3a equals $type3a] [$res4a equals $type4a] [$res5a equals $type5a] [$res1b equals $type1b] [$res2b equals $type2b] [$res3b equals $type3b] [$res4b equals $type4b] [$res5b equals $type5b]
} {1 1 1 1 1 1 1 1 1 1}
######################################################################
####
# Need to test that constants can be registered and recognized by the parser.
test ParseTreeTypeInference-10.0 {Test that constants can be registered and recognized by the parser} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "PI + E"]
    set res1 [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]

    $p1 registerConstant "neil" "neil"
    $p1 registerConstant "one" [java::new {Integer int} 1]
    $p1 registerConstant "half" [java::new {Double double} "0.5"]
    $p1 registerConstant "long" [java::new {Long long} "1000"]
    $p1 registerConstant "boolean" [java::new {Boolean boolean} "true"]

    set root1 [ $p1 {generateParseTree String} "half + one + neil"]
    set res2 [[ $root1 evaluateParseTree ] getType]
    set type2 [inferTypes $root1]

    set root1 [ $p1 {generateParseTree String} "boolean == true"]
    set res3 [[ $root1 evaluateParseTree ] getType]
    set type3 [inferTypes $root1]

    set root1 [ $p1 {generateParseTree String} "long"]
    set res4 [[ $root1 evaluateParseTree ] getType]
    set type4 [inferTypes $root1]
  
    list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1}
######################################################################
####
# Need to test that functions can access methods registered in the 
# search path of the parser. can be registered and recognized by the parser.
# FIXME: this test is not finished.
test ParseTreeTypeInference-10.1 {Test that functions can access registered classes.
} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "min(1,3)"]
    set res1 [[ $root1 evaluateParseTree ] getType]
    set type1 [inferTypes $root1]

    #$p1 registerClass "ptolemy.data.expr.UtilityFunctions"
    
    set root1 [ $p1 {generateParseTree String} "sin(30*PI/180)"]
    set res2 [[ $root1 evaluateParseTree ] getType]
    set type2 [inferTypes $root1]

    
    list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1}
#

######################################################################
####
# Test matrix construction, when term types are identical.
test ParseTreeTypeInference-12.1 {Test basic matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1,2,3;4,5,6;7,8,9\]" ]
    set res1 [java::cast ptolemy.data.IntMatrixToken [ $root1 evaluateParseTree ]]
    set type [inferTypes $root1]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set value1 [ $res1 getElementAt 0 0 ]
    set value2 [ $res1 getElementAt 2 2 ]

    list $col $row $value1 $value2 [$type toString]
} {3 3 1 9 {[int]}}

# Test matrix construction, when term types are heterogeneous.
test ParseTreeTypeInference-12.2 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1.0;2;3j\]" ]
    set res1 [java::cast ptolemy.data.ComplexMatrixToken [ $root1 evaluateParseTree ]]
    set type [inferTypes $root1]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set value1 [ [ $res1 getElementAt 0 0 ] toString ]
    set value2 [ [ $res1 getElementAt 2 0 ] toString ]

    list $col $row $value1 $value2 [$type toString]
} {1 3 {1.0 + 0.0i} {0.0 + 3.0i} {[complex]}}

######################################################################
####
# Test array reference.
test ParseTreeTypeInference-13.0 {Test array reference.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set nl [java::new ptolemy.kernel.util.NamedList]
    set v1 [java::new ptolemy.data.expr.Variable]
    $v1 setName "v1"
    $v1 setExpression "\[ 1, 2.0, 3; 4, 5, 6j \]"
    $nl prepend $v1
    set v2 [java::new ptolemy.data.expr.Variable]
    $v2 setName "v2"
    $v2 setToken [java::new {ptolemy.data.IntToken int} 1]
    $nl prepend $v2

    set scope [java::new ptolemy.data.expr.ExplicitScope $nl]    
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    set root1 [ $p1 generateParseTree "v1(0+1,2)+v1(0, v2-1)"]
    set res1 [ [ $evaluator evaluateParseTree $root1 $scope] getType ]
    set type1 [inferTypesScope $root1 $scope]
    
    set root2 [ $p1 generateParseTree "cast(complex,v1(0+1,2)+v1(0, v2-1).add(v2))"]
    set res2 [ [ $evaluator evaluateParseTree $root2 $scope] getType ]
    set type2 [inferTypesScope $root2 $scope]
    
    list [$res1 equals $type1] [$res2 equals $type2]
} {1 1}

test ParseTreeTypeInference-13.1 {Test array method calls.} {
    set p1 [java::new ptolemy.data.expr.PtParser]    
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    set root [ $p1 generateParseTree "cast(int, {1, 2, 3}.getElement(1))"]
    set res [ [ $evaluator evaluateParseTree $root] getType]
    set type [inferTypes $root]
    list [$res equals $type]
} {1}


# Test record construction,
test ParseTreeTypeInference-13.2 {Test record construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "{a=1,b=2.4}" ]
    set res [[ $root evaluateParseTree ] getType]
    set type [inferTypes $root]
    list [$res equals $type]
} {1}

######################################################################
####
# Test eval
test ParseTreeTypeInference-14.0 {Test eval inference.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "eval(\"1+1\")" ]
    set res [java::field ptolemy.data.type.BaseType GENERAL]
    set type [inferTypes $root]
    list [$res equals $type]
} {1}


######################################################################
####
# 
test ParseTreeTypeInference-16.0 {Test method calls on arrays, matrices, etc.} {
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "cast({int},{1,2,3}.add({3,4,5}))"]
    set res1  [[ $evaluator evaluateParseTree $root] getType]
    set type1 [inferTypes $root]

    set root [ $p1 {generateParseTree String} "cast({int},{{a=1,b=2},{a=3,b=4},{a=5,b=6}}.get(\"a\"))"]
    set res2  [[ $evaluator evaluateParseTree $root] getType]
    set type2 [inferTypes $root]
    
    set root [ $p1 {generateParseTree String} "cast(\[int\],create({1,2,3,4,5,6},2,3))"]
    set res3 [[ $evaluator evaluateParseTree $root] getType]
    set type3 [inferTypes $root]
    
    set root [ $p1 {generateParseTree String} "cast({int},{1,1,1,1}.leftShift({1,2,3,4}))"]
    set res4 [[ $evaluator evaluateParseTree $root] getType]
    set type4 [inferTypes $root]

    list [$res1 equals $type1] [$res2 equals $type2] [$res3 equals $type3] [$res4 equals $type4] 
} {1 1 1 1}

test ParseTreeTypeInference-16.2 {Test record indexing} {
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "true ? 2 : ({a={0,0,0}}.a).length()"]
    set res1 [[ $evaluator evaluateParseTree $root] getType]
    set type1 [inferTypes $root]
   
    set root [ $p {generateParseTree String} "false ? 2 : ({a={0,0,0}}.a).length()"]
    set res2  [[ $evaluator evaluateParseTree $root] getType]
    set type2 [inferTypes $root]
    list [$res1 equals $type1] [$res2 equals $type2]
} {1 1}

test ParseTreeTypeInference-16.3 {Test property} {
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "getProperty(\"ptolemy.ptII.dir\")"]
    set res1 [[ $evaluator evaluateParseTree $root] getType]
    set type1 [inferTypes $root]

    set root [ $p {generateParseTree String} "property(\"ptolemy.ptII.dir\") + \"foo\""]
    set res2 [[ $evaluator evaluateParseTree $root] getType]
    set type2 [inferTypes $root]
    list [$res1 equals $type1]  [$res2 equals $type2]
} {1 1}


####################################################################

test ParseTreeTypeInference-17.1 {Test correct scoping in function definitions.} {
    list [theTest "function(x) x + 3.0"] [theTest "function(x:int) x + 3.0"]
} {{(function(a0:general) general)} {(function(a0:int) double)}}

test ParseTreeTypeInference-17.2 {Test nested function definitions.} {
    list [theTest "function (y) function(x) x + y + 3"] [theTest "function (y:int) function(x) x + y + 3"] [theTest "function (y) function(x:int) x + y + 3"] [theTest "function (y:double) function(x:long) x + y + 3"] 
} {{(function(a0:general) (function(a0:general) general))} {(function(a0:int) (function(a0:general) general))} {(function(a0:general) (function(a0:int) general))} {(function(a0:double) (function(a0:long) scalar))}}

test ParseTreeTypeInference-17.3 {Test nested function definitions.} {
    list [theTest "cast(function(x:int) double, function(x) x + 3.0)"] 
} {{(function(a0:int) double)}}

####################################################################

test ParseTreeTypeInference-27.0 {Test Error message} {
    catch {list [evaluate {1.0+im}]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: The ID im is undefined.}}

