# Tests for the PtParser class
#
# @Author: Neil Smyth
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
# 
test PtParser-2.1 {Construct Parser objects using different constructors} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set param1 [java::new {ptolemy.data.expr.Parameter ptolemy.kernel.util.NamedObj String ptolemy.data.Token} $e id1 $tok1]
    set p2 [java::new {ptolemy.data.expr.PtParser ptolemy.data.expr.Variable} $param1]

    set c1 [$p1 getClass]
    set c2 [$p2 getClass]

    list [ $c1 getName ] [$c2 getName] 
} {ptolemy.data.expr.PtParser ptolemy.data.expr.PtParser}

######################################################################
####
# 
test PtParser-2.2 {Construct a Parser, try simple integer expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "2 + 3 + 4"]
    set res  [ $root evaluateParseTree ]

    set root [ $p1 {generateParseTree String} "2 - 3 - 4"]
    set res1  [ $root evaluateParseTree ]

    set root [ $p1 {generateParseTree String} "2 * 3 * 4"]
    set res2  [ $root evaluateParseTree ]
    
    set root [ $p1 {generateParseTree String} "7 % 5"]
    set res3  [ $root evaluateParseTree ]
    
    set root [ $p1 {generateParseTree String} "12 / 2 / 3"]
    set res4  [ $root evaluateParseTree ]

    list [$res toString] [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {9 -5 24 2 2}

######################################################################
####
# 
test PtParser-2.3 {Construct a Parser, try complex integer expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "-(2 + (3) + 4*(3- 4 % 3)*(12/12))\n"]
    # Note that dividing an Int by an Int can give a Double, here I want 
    # all nodes the parse tree to have IntTokens
    set res  [ $root evaluateParseTree ]

    list [$res toString] 
} {-13}
######################################################################
####
# 
test PtParser-2.4 {Construct a Parser, try simple double expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "2.0 + 3.5 + 4.2"]
    set res  [ $root evaluateParseTree ]

    set root [ $p1 {generateParseTree String} "2.2 - 3.6 - 4.2"]
    set res1  [ $root evaluateParseTree ]

    set root [ $p1 {generateParseTree String} "2.0 * 3.5 * 4.2"]
    set res2  [ $root evaluateParseTree ]
    
    set root [ $p1 {generateParseTree String} "7.1 % 5.5"]
    set res3  [ $root evaluateParseTree ]
    
    set root [ $p1 {generateParseTree String} "12.0 / 2.4 / 2.5"]
    set res4  [ $root evaluateParseTree ]

    set reslist [list [$res stringValue] [$res1 stringValue] [$res2 stringValue] [$res3 stringValue] [$res4 stringValue]]
    ptclose $reslist {9.7 -5.6 29.4 1.6 2.0}
} {1}
######################################################################
####
# 
test PtParser-2.5 {Construct a Parser, try complex double expressions} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "-(2.2 + (3.7%1.5) + 4.0*(3.2- 4.2 % 3.0)*(12.0/2.4/2.5/2.0))" ]
    set res  [ $root evaluateParseTree ]

    list [$res toString] 
} {-10.9}
######################################################################
####
# 
test PtParser-2.6 {Construct a Parser, try creating complex numbers} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "2i"]
    set res1  [ $root evaluateParseTree ]

    set root [ $p {generateParseTree String} "3 + 2i"]
    set res2  [ $root evaluateParseTree ]

    list [$res1 toString] [$res2 toString]
} {{0.0 + 2.0i} {3.0 + 2.0i}}
######################################################################
####
# 
test PtParser-3.0 {Construct a Parser,mixing doubles, strings and integers using arithmetic} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root [ $p {generateParseTree String} "-(2*9.5 + (3.5/7) + 4/.5) +  \" hello \" + (3*5 -4)\n"]
    set res  [ $root evaluateParseTree ]

    list [$res toString]
    # for some reason TclBlend puts brackets around the result, it seems 
    # because there are spaces within the paranthesis???
} {{-27.5 hello 11}}
######################################################################
####
# 
test PtParser-4.0 {Construct a Parser, try basic relational operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "2<4"]
    set root2 [ $p {generateParseTree String} "4<=4"]
    set root3 [ $p {generateParseTree String} "4>=4"]
    set root4 [ $p {generateParseTree String} "4>7"]
    set root5 [ $p {generateParseTree String} "5==4"]
    set root6 [ $p {generateParseTree String} "5!=4"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]
    set res4  [ $root4 evaluateParseTree ]
    set res5  [ $root5 evaluateParseTree ]
    set res6  [ $root6 evaluateParseTree ]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] [$res5 toString] [$res6 toString] 
} {true true true false false true}
######################################################################
####
# 
test PtParser-4.1 {Construct a Parser, try  relational operators with arithetic operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "(2)<(4*5 -9)"]
    set root2 [ $p {generateParseTree String} "4<=4*7"]
    set root3 [ $p {generateParseTree String} "4-7>=4"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]

    list [$res1 toString] [$res2 toString] [$res3 toString]  
} {true true false}
######################################################################
####
# 
test PtParser-4.2 {Construct a Parser,test use of equality operator on strings} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "\"hello\" == \"hello\""]
    set root2 [ $p {generateParseTree String} "\"hello\" != \"hello\""]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
   
    list [$res1 toString] [$res2 toString]  
} {true false}
######################################################################
####
# 
test PtParser-5.0 {Construct a Parser, test use of logical operators} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "\"hello\" == \"hello\" && 5>=5"]
    set root2 [ $p {generateParseTree String} "\"hello\" != \"hello\" || 3<3"]
    set root3 [ $p {generateParseTree String} "(3<=5) && (56 == 56) || (\"foo\" != \"foo\")"]
    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]

    list [$res1 toString] [$res2 toString] [$res3 toString]  
} {true false true}
######################################################################
####
# 
test PtParser-5.1 {Construct a Parser, unary minus & unary logical not} {
    set p [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p {generateParseTree String} "!true"]
    set root2 [ $p {generateParseTree String} "-7"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]

    list [$res1 toString] [$res2 toString] 
} {false -7}
######################################################################
####
# 
test PtParser-6.0 {Construct a Parser, test use of params passed in a namedlist} {
    
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set tok2 [java::new  {ptolemy.data.DoubleToken double} 2.45]
    set tok3 [java::new  {ptolemy.data.IntToken int} 9]
    set tok4 [java::new  {ptolemy.data.StringToken String} { hello world}]
    set param1 [java::new {ptolemy.data.expr.Parameter ptolemy.kernel.util.NamedObj String ptolemy.data.Token} $e id1 $tok1]
    set param2 [java::new {ptolemy.data.expr.Parameter ptolemy.kernel.util.NamedObj String ptolemy.data.Token} $e id2 $tok2]
    set param3 [java::new {ptolemy.data.expr.Parameter ptolemy.kernel.util.NamedObj String ptolemy.data.Token} $e id3 $tok3]
    set param4 [java::new {ptolemy.data.expr.Parameter ptolemy.kernel.util.NamedObj String ptolemy.data.Token} $e id4 $tok4]

    set parser [java::new {ptolemy.data.expr.PtParser ptolemy.data.expr.Variable} $param1]
    $param1 setContainer $e
    $param2 setContainer $e
    $param3 setContainer $e
    $param4 setContainer $e

    set nl [$param1 getScope]

    set root1 [ $parser {generateParseTree String ptolemy.kernel.util.NamedList} "id2 + id3 + id4\n" $nl]
    set res1  [ $root1 evaluateParseTree ]
   
    list [$res1 toString] 
} {{11.45 hello world}}
######################################################################
####
# 
test PtParser-6.1 {Test reEvaluation of parse Tree} {
    
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set tok1 [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set tok2 [java::new  {ptolemy.data.DoubleToken double} 2.45]
    set tok3 [java::new  {ptolemy.data.IntToken int} 9]
    set tok4 [java::new  {ptolemy.data.StringToken String} { hello world}]
    set param1 [java::new ptolemy.data.expr.Parameter  $e id1 $tok1]
    set param2 [java::new ptolemy.data.expr.Parameter  $e id2 $tok2]
    set param3 [java::new ptolemy.data.expr.Parameter  $e id3 $tok3]
    set param4 [java::new ptolemy.data.expr.Parameter  $e id4 $tok4]

   
    set parser [java::new ptolemy.data.expr.PtParser]
    $param1 setContainer $e
    $param2 setContainer $e
    $param3 setContainer $e
    $param4 setContainer $e
    set nl [$param1 getScope]

    set root1 [ $parser {generateParseTree String ptolemy.kernel.util.NamedList} "id2 + id3 + id4\n" $nl]
    set res1  [ $root1 evaluateParseTree ]

    set newTok [java::new {ptolemy.data.DoubleToken double} 102.45]
    $param2 setToken $newTok
    set res2  [ $root1 {evaluateParseTree} ]
    
    list [$res1 toString] [$res2 toString] 
} {{11.45 hello world} {111.45 hello world}}
######################################################################
####
# 
test PtParser-7.0 {Construct a Parser, try simple functional if then else} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(true)?(7):(6)\n"]
    set res  [ $root evaluateParseTree ]

    list [$res toString] 
} {7}

######################################################################
####
# 
test PtParser-7.1 {Construct a Parser, try harder if then else} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(false) ? (3/.5*4) : (\"hello\")"]
    set res  [ $root evaluateParseTree ]

    list [$res toString] 
} {hello}
######################################################################
####
# 
test PtParser-7.2 {Test complicated expression within boolean test condition} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "((3<5) && (\"test\" == \"test\")) ? (3/.5*4) : (\"hello\")"]
    set res  [ $root evaluateParseTree ]

    list [$res toString] 
} {24.0}
######################################################################
####
# 
test PtParser-7.3 {Test nested if then elses} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(true ? false: true ) ? (3/.5*4) : (\"hello\")"]
    set res  [ $root evaluateParseTree ]

    list [$res toString] 
} {hello}
######################################################################
####
# 
test PtParser-7.4 {Test many levels of parenthesis nesting} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "(true ? false: true ) ? (((((3/.5*4))))) : ((((((\"hello\"))))))"]
    set res  [ $root evaluateParseTree ]

    list  [$res toString] 
} {hello}
######################################################################
####
# 
test PtParser-8.0 {Test method calls on PtTokens} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "(4.0).add(3.0)"]
    set res1  [ $root1 evaluateParseTree ]

    set nl [java::new ptolemy.kernel.util.NamedList]
    set v1 [java::new ptolemy.data.expr.Variable]
    $v1 setName "v1"
    $v1 setToken [java::new {ptolemy.data.IntToken int} 1]
    set v2 [java::new ptolemy.data.expr.Variable]
    $v2 setName "v2"
    $v2 setToken [java::new {ptolemy.data.DoubleToken double} 1.0]
    $nl prepend $v1
    $nl prepend $v2
    set root2 [ $p1 generateParseTree "v1.add(v2)" $nl]
    set res2 [ $root2 evaluateParseTree ]

    list [$res1 toString] [$res2 toString]
} {7.0 2.0}
######################################################################
####
# 
test PtParser-8.1 {Test bitwise operators} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "5 & 2"]
    set root2 [ $p1 {generateParseTree String} "5 | 2"]
    set root3 [ $p1 {generateParseTree String} "5 # 4"]
    set root4 [ $p1 {generateParseTree String} "~5"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]
    set res4  [ $root4 evaluateParseTree ]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {0 7 1 -6}
######################################################################
####
# 
test PtParser-8.2 {Test more complicated bitwise operations, and bitwise ops on booleans} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "~(5 & 2 | 4)"]
    set root2 [ $p1 {generateParseTree String} "(5>4) & (2==2)"]
    set root3 [ $p1 {generateParseTree String} "(false) | (2!=2)"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {-5 true false}

######################################################################
####
# Need to test that the tree can be reevaluated an arbitrary number of times
test PtParser-9.0 {Check that evaluation of the parse tree does not change the parse tree} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "2+3"]
    set root2 [ $p1 {generateParseTree String} "2-3"]
    set root3 [ $p1 {generateParseTree String} "2*3"]
    set root4 [ $p1 {generateParseTree String} "2/4"]
    set root5 [ $p1 {generateParseTree String} "11 % 3"]
    
    set res1a [ $root1 evaluateParseTree ]
    set res1b [ $root1 evaluateParseTree ]

    set res2a [ $root2 evaluateParseTree ]
    set res2b [ $root2 evaluateParseTree ]

    set res3a [ $root3 evaluateParseTree ]
    set res3b [ $root3 evaluateParseTree ]

    set res4a [ $root4 evaluateParseTree ]
    set res4b [ $root4 evaluateParseTree ]

    set res5a [ $root5 evaluateParseTree ]
    set res5b [ $root5 evaluateParseTree ]

    list [$res1a stringValue] [$res1b stringValue] [$res2a stringValue] [$res2b stringValue] [$res3a stringValue] [$res3b stringValue] [$res4a stringValue] [$res4b stringValue] [$res5a stringValue] [$res5b stringValue]
} {5 5 -1 -1 6 6 0 0 2 2}
######################################################################
####
# Need to test that constants can be registered and recognized by the parser.
test PtParser-10.0 {Test that constants can be registered and recognized by the parser} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "PI + E"]
    set res1 [ $root1 evaluateParseTree ]
    set value1 [$res1 toString]

    $p1 registerConstant "neil" "neil"
    $p1 registerConstant "one" [java::new {Integer int} 1]
    $p1 registerConstant "half" [java::new {Double double} "0.5"]
    $p1 registerConstant "long" [java::new {Long long} "1000"]
    $p1 registerConstant "boolean" [java::new {Boolean boolean} "true"]

    set root1 [ $p1 {generateParseTree String} "half + one + neil"]
    set res1 [ $root1 evaluateParseTree ]
    set value2 [$res1 toString]

    set root1 [ $p1 {generateParseTree String} "boolean == true"]
    set res1 [ $root1 evaluateParseTree ]
    set value3 [$res1 toString]

    set root1 [ $p1 {generateParseTree String} "long"]
    set res1 [ $root1 evaluateParseTree ]
    set value4 [$res1 toString]
    list $value1 $value2 $value3 $value4
} {5.86 1.5neil true 1000}
######################################################################
####
# Need to test that functions can access methods registered in the 
# search path of the parser. can be registered and recognized by the parser.
# FIXME: this test is not finished.
test PtParser-10.1 {Test that functions can access registered classes.
} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "min(1,3)"]
    set res1 [ $root1 evaluateParseTree ]
    set value1 [$res1 toString]

    #$p1 registerClass "ptolemy.data.expr.UtilityFunctions"
    
    set root1 [ $p1 {generateParseTree String} "sin(30*PI/180)"]
    set res1 [ $root1 evaluateParseTree ]
    set value2 [$res1 toString]

    
    list $value1 $value2
} {1 0.5}

test PtParser-10.2 {Test for reasonable error messages on type problems} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set v1 [java::new ptolemy.data.expr.Variable $e v1]
    $v1 setExpression "cos(1)"
    catch {$v1 getToken} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ASTFunction Function cos( 1 ) cannot be executed with given arguments.}}

######################################################################
####
test PtParser-11.0 {Test constants} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set v1 [java::new ptolemy.data.expr.Variable $e v1]
    $v1 setExpression i
    set v2 [java::new ptolemy.data.expr.Variable $e v2]
    $v2 setExpression {v1*j}
    [$v2 getToken] stringValue
} {-1.0 + 0.0i}
######################################################################
####
# Test matrix construction, when term types are identical.
test PtParser-12.1 {Test basic matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1,2,3;4,5,6;7,8,9\]" ]
    set res1 [java::cast ptolemy.data.IntMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set value1 [ $res1 getElementAt 0 0 ]
    set value2 [ $res1 getElementAt 2 2 ]

    list $col $row $value1 $value2
} {3 3 1 9}

# Test matrix construction, when term types are heterogeneous.
test PtParser-12.2 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1.0;2;3j\]" ]
    set res1 [java::cast ptolemy.data.ComplexMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set value1 [ [ $res1 getElementAt 0 0 ] toString ]
    set value2 [ [ $res1 getElementAt 2 0 ] toString ]

    list $col $row $value1 $value2
} {1 3 {1.0 + 0.0i} {0.0 + 3.0i}}
# Test matrix construction
test PtParser-12.3 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 generateParseTree "\[0.5, -0.5\]" ]
    set res1 [java::cast ptolemy.data.DoubleMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set value1 [ $res1 getElementAt 0 0 ]
    set value2 [ $res1 getElementAt 0 1 ]

    list $col $row $value1 $value2
} {2 1 0.5 -0.5}

# Test matrix construction, using regularly spaced vector as row.
test PtParser-12.3 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1:1:10\]" ]
    set res1 [java::cast ptolemy.data.IntMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set v1 [ $res1 getElementAt 0 0 ]
    set v2 [ $res1 getElementAt 0 9 ]

    list $col $row $v1 $v2
} {10 1 1 10}

# Test matrix construction, using regularly spaced vector as row.
test PtParser-12.4 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1 : -1 : 1\]" ]
    set res1 [java::cast ptolemy.data.IntMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]

    list $col $row
} {1 1}

# Test matrix construction, using regularly spaced vector as row.
test PtParser-12.5 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1 : -1 : 2\]" ]
    set res1 [java::cast ptolemy.data.IntMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]

    list $col $row
} {0 1}

# Test matrix construction, using regularly spaced vector as row.
test PtParser-12.6 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1L : -1 : 0;0:1:1\]" ]
    set res1 [java::cast ptolemy.data.LongMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set v1 [$res1 getElementAt 1 1]

    list $col $row $v1
} {2 2 1}

# Test matrix construction, using regularly spaced vector as row.
test PtParser-12.7 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 {generateParseTree String} "\[1.0 : 1 : 3;0:1:2\]" ]
    set res1 [java::cast ptolemy.data.DoubleMatrixToken [ $root1 evaluateParseTree ]]
    set col [ $res1 getColumnCount ]
    set row [ $res1 getRowCount ]
    set v1 [$res1 getElementAt 1 1]

    list $col $row $v1
} {3 2 1.0}

# Test matrix construction, using regularly spaced vector as row.
test PtParser-12.8 {Test matrix construction.} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root1 [ $p1 generateParseTree "\[1.0 : j : 3;0:1:2\]" ]
    catch {$root1 evaluateParseTree} msg

    list $msg
} {{ptolemy.kernel.util.IllegalActionException: The LUB of the types of the terms of a regularly-spaced-vector matrix construction is not supported: complex}}

######################################################################
####
# Test array reference.
test PtParser-13.0 {Test array reference.} {
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

    set root1 [ $p1 generateParseTree "v1(0+1,2)+v1(0, v2-1)" $nl]
    set res1 [ [ $root1 evaluateParseTree ] toString ]
    set root2 [ $p1 generateParseTree "v1(0+1,2)+v1(0, v2-1).add(v2)" $nl]
    set res2 [ [ $root2 evaluateParseTree ] toString ]
    list $res1 $res2
} {{1.0 + 6.0i} {2.0 + 6.0i}}
######################################################################
####
# Test that constant expressions are evaluated only once.
test PtParser-14.0 {Test constant expressions.} {
    set p [java::new ptolemy.data.expr.PtParser]
    set ra [ $p generateParseTree "1+2*3" ]
    set ta [ $ra evaluateParseTree ]
    set tb [ $ra evaluateParseTree ]
    set va [ $ta equals $tb ]
    set vb [ [ $ta isEqualTo $tb ] stringValue ]

    set nl [java::new ptolemy.kernel.util.NamedList]
    set vara [java::new ptolemy.data.expr.Variable]
    $vara setName "vara"
    $vara setExpression "1"
    $nl prepend $vara
    set rb [ $p generateParseTree "vara-1" $nl]
    set ta [ $rb evaluateParseTree ]
    set tb [ $rb evaluateParseTree ]
    set vc [ $ta equals $tb ]
    set vd [ [ $ta isEqualTo $tb ] stringValue ]

    set rc [ $p generateParseTree "eval(\"1+1\")" ]
    set ta [ $rc evaluateParseTree ]
    set tb [ $rc evaluateParseTree ]
    set ve [ $ta equals $tb ]
    set vf [ [ $ta isEqualTo $tb ] stringValue ]

    list $va $vb $vc $vd $ve $vf
} {1 true 0 true 0 true}
