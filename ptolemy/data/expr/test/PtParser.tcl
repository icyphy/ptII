# Tests for the PtParser class
#
# @Author: Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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
test PtParser-1.1 {Get information about an instance of PtParser} {
    # If anything changes, we want to know about it so we can write tests.
    set p [java::new pt.data.parser.PtParser]
    list [getJavaInfo $p]
} {{
  class:         pt.data.parser.PtParser
  fields:        lookingAhead nextToken token token_source
    
  methods:       {ReInit java.io.InputStream} {ReInit pt.data.parser.PtP
    arserTokenManager} bitwiseAnd bitwiseOr bitwiseXor disa
    ble_tracing element enable_tracing {equals java.lang.Ob
    ject} funcIf function generateParseException {generateP
    arseTree java.lang.String} {generateParseTree java.lang
    .String pt.kernel.NamedList} getClass getNextToken {get
    Token int} hashCode logicalAnd logicalEquals logicalOr 
    notify notifyAll relational start sum term toString una
    ry wait {wait long} {wait long int}
    
  constructors:  pt.data.parser.PtParser {pt.data.parser.PtParser java.i
    o.InputStream} {pt.data.parser.PtParser java.util.Obser
    ver} {pt.data.parser.PtParser pt.data.parser.PtParserTo
    kenManager}
    
  properties:    class nextToken token
    
  superclass:    java.lang.Object
    
}}


######################################################################
####
# 
test PtParser-2.1 {Construct Parse objects using different constructors} {
    set p1 [java::new pt.data.parser.PtParser]
    set e [java::new {pt.kernel.Entity String} parent]
    set tok1 [java::new  {pt.data.DoubleToken double} 4.5]
    set param1 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id1 $tok1]
    set p2 [java::new {pt.data.parser.PtParser java.util.Observer} $param1]
    set c1 [$p1 getClass]
    set c2 [$p2 getClass]
    list [ $c1 getName ] [$c2 getName] 
} {pt.data.parser.PtParser pt.data.parser.PtParser}

######################################################################
####
# 
test PtParser-2.2 {Construct a Parser, try simple integer expressions using arithmetic} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "2 + 3 + 4\n"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.IntToken 9}

######################################################################
####
# 
test PtParser-2.3 {Construct a Parser, try simple integer expressions using arithmetic} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "2 -3 +99\n"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.IntToken 98}

######################################################################
####
# 
test PtParser-2.4 {Construct a Parser, try simple integer expressions using arithmetic} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "2 + 3 + (79 -9999)\n"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.IntToken -9915}

######################################################################
####
# 
test PtParser-2.5 {Construct a Parser, try simple integer expressions using arithmetic} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "-(2 + (3) + 4*(3- 4 % 3))\n"]
    # Note that dividing an Int by an Int gives a Double, here I want all nodes 
    # the parse tree to have IntTokens
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.IntToken -13}

######################################################################
####
# 
test PtParser-3.0 {Construct a Parser, try simple double expressions using arithmetic} {
    set p [java::new pt.data.parser.PtParser]
    set root [ $p {generateParseTree String} "-(2*9.5 + (3.5/7) + 4/.5)\n" ]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.DoubleToken -27.5}
######################################################################
####
# 
test PtParser-3.1 {Construct a Parser,mixing doubles, strings and integers using arithmetic} {
    set p [java::new pt.data.parser.PtParser]
    set root [ $p {generateParseTree String} "-(2*9.5 + (3.5/7) + 4/.5) +  \" hello \" + (3*5 -4)\n"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.StringToken {-27.5 hello 11}}
######################################################################
####
# 
test PtParser-4.0 {Construct a Parser, try basic relational operators} {
    set p [java::new pt.data.parser.PtParser]
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

    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
    set c3 [$res3 getClass]
    set c4 [$res4 getClass]
    set c5 [$res5 getClass]
    set c6 [$res6 getClass]
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] [$c3 getName ] [$res3 toString]  [$c4 getName ] [$res4 toString] [$c5 getName ] [$res5 toString] [$c6 getName ] [$res6 toString] 
} {pt.data.BooleanToken true pt.data.BooleanToken true pt.data.BooleanToken true pt.data.BooleanToken false pt.data.BooleanToken false pt.data.BooleanToken true}
######################################################################
####
# 
test PtParser-4.1 {Construct a Parser, try  relational operators with arithetic operators} {
    set p [java::new pt.data.parser.PtParser]
    set root1 [ $p {generateParseTree String} "(2)<(4*5 -9)"]
    set root2 [ $p {generateParseTree String} "4<=4*7"]
    set root3 [ $p {generateParseTree String} "4-7>=4"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]

    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
    set c3 [$res3 getClass]
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] [$c3 getName ] [$res3 toString]  
} {pt.data.BooleanToken true pt.data.BooleanToken true pt.data.BooleanToken false}
######################################################################
####
# 
test PtParser-4.2 {Construct a Parser,test use of equality operator on strings} {
    set p [java::new pt.data.parser.PtParser]
    set root1 [ $p {generateParseTree String} "\"hello\" == \"hello\""]
    set root2 [ $p {generateParseTree String} "\"hello\" != \"hello\""]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    
    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
    
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString]  
} {pt.data.BooleanToken true pt.data.BooleanToken false}
######################################################################
####
# 
test PtParser-5.0 {Construct a Parser, test use of logical operators} {
    set p [java::new pt.data.parser.PtParser]
    set root1 [ $p {generateParseTree String} "\"hello\" == \"hello\" && 5>=5"]
    set root2 [ $p {generateParseTree String} "\"hello\" != \"hello\" || 3<3"]
    set root3 [ $p {generateParseTree String} "(3<=5) && (56 == 56) || (\"foo\" != \"foo\")"]
    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]

    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
    set c3 [$res3 getClass]
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] [$c3 getName ] [$res3 toString]  
} {pt.data.BooleanToken true pt.data.BooleanToken false pt.data.BooleanToken true}
######################################################################
####
# 
test PtParser-5.1 {Construct a Parser, unary minus & unary logical not} {
    set p [java::new pt.data.parser.PtParser]
    set root1 [ $p {generateParseTree String} "!true"]
    set root2 [ $p {generateParseTree String} "-7"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
   
    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
   
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] 
} {pt.data.BooleanToken false pt.data.IntToken -7}
######################################################################
####
# 
test PtParser-6.0 {Construct a Parser, test use of params passed in a namedlist} {
    
    set e [java::new {pt.kernel.Entity String} parent]
    set tok1 [java::new  {pt.data.DoubleToken double} 4.5]
    set tok2 [java::new  {pt.data.DoubleToken double} 2.45]
    set tok3 [java::new  {pt.data.IntToken int} 9]
    set tok4 [java::new  {pt.data.StringToken String} { hello world }]
    set param1 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id1 $tok1]
    set param2 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id2 $tok2]
    set param3 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id3 $tok3]
    set param4 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id4 $tok4]

    set parser [java::new {pt.data.parser.PtParser java.util.Observer} $param1]
    $e {addParam pt.data.Param} $param1
    $e {addParam pt.data.Param} $param2
    $e {addParam pt.data.Param} $param3
    $e {addParam pt.data.Param} $param4

    set nl [$param1 getScope]

    set root1 [ $parser {generateParseTree String pt.kernel.NamedList} "id2 + id3 + id4\n" $nl]
    set res1  [ $root1 evaluateParseTree ]

    set c1 [$res1 getClass]
    list [$c1 getName ] [$res1 toString] 
} {pt.data.StringToken {11.45 hello world }}
######################################################################
####
# 
test PtParser-6.1 {Test reEvaluation of parse Tree} {
    
    set e [java::new {pt.kernel.Entity String} parent]
    set tok1 [java::new  {pt.data.DoubleToken double} 4.5]
    set tok2 [java::new  {pt.data.DoubleToken double} 2.45]
    set tok3 [java::new  {pt.data.IntToken int} 9]
    set tok4 [java::new  {pt.data.StringToken String} { hello world }]
    set param1 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id1 $tok1]
    set param2 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id2 $tok2]
    set param3 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id3 $tok3]
    set param4 [java::new {pt.data.Param pt.kernel.NamedObj String pt.data.Token} $e id4 $tok4]

    $e {addParam pt.data.Param} $param1
    $e {addParam pt.data.Param} $param2
    $e {addParam pt.data.Param} $param3
    $e {addParam pt.data.Param} $param4

    set parser [java::new pt.data.parser.PtParser]

    set nl [$param1 getScope]

    set root1 [ $parser {generateParseTree String pt.kernel.NamedList} "id2 + id3 + id4\n" $nl]
    set res1  [ $root1 evaluateParseTree ]
    set c1 [$res1 getClass]

    $tok2 {setValue double} 102.45
    set res2  [ $root1 {evaluateParseTree} ]
    
    set c2 [$res2 getClass]

    list [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] 
} {pt.data.StringToken {11.45 hello world } pt.data.StringToken {111.45 hello world }}
######################################################################
####
# 
test PtParser-7.0 {Construct a Parser, try simple functional if then else} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "(true)?(7):(6)\n"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.IntToken 7}

######################################################################
####
# 
test PtParser-7.1 {Construct a Parser, try harder if then else} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "(false) ? (3/.5*4) : (\"hello\")"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.StringToken hello}
######################################################################
####
# 
test PtParser-7.2 {Test complicated expression within boolean test condition} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "((3<5) && (\"test\" == \"test\")) ? (3/.5*4) : (\"hello\")"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.DoubleToken 24.0}
######################################################################
####
# 
test PtParser-7.3 {Test nested if then elses} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "(true ? false: true ) ? (3/.5*4) : (\"hello\")"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.StringToken hello}
######################################################################
####
# 
test PtParser-7.4 {Test many levels of parenthesis nesting} {
    set p1 [java::new pt.data.parser.PtParser]
    set root [ $p1 {generateParseTree String} "(true ? false: true ) ? (((((3/.5*4))))) : ((((((\"hello\"))))))"]
    set res  [ $root evaluateParseTree ]

    set c1 [$res getClass]
    list  [$c1 getName ] [$res toString] 
} {pt.data.StringToken hello}
######################################################################
####
# 
test PtParser-8.0 {Test method calls on PtTokens} {
    set p1 [java::new pt.data.parser.PtParser]
    set root1 [ $p1 {generateParseTree String} "(4.0).add(3.0)"]
    set res1  [ $root1 evaluateParseTree ]

    set c1 [$res1 getClass]
    list  [$c1 getName ] [$res1 toString] 
} {pt.data.DoubleToken 7.0}
######################################################################
####
# 
test PtParser-8.1 {Test bitwise operators} {
    set p1 [java::new pt.data.parser.PtParser]
    set root1 [ $p1 {generateParseTree String} "5 & 2"]
    set root2 [ $p1 {generateParseTree String} "5 | 2"]
    set root3 [ $p1 {generateParseTree String} "5 ^ 4"]
    set root4 [ $p1 {generateParseTree String} "~5"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]
    set res4  [ $root4 evaluateParseTree ]


    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
    set c3 [$res3 getClass]
    set c4 [$res4 getClass]
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] [$c3 getName ] [$res3 toString] [$c4 getName ] [$res4 toString] 
} {pt.data.IntToken 0 pt.data.IntToken 7 pt.data.IntToken 1 pt.data.IntToken -6}
######################################################################
####
# 
test PtParser-8.2 {Test more complicated bitwise operations, and bitwise ops on booleans} {
    set p1 [java::new pt.data.parser.PtParser]
    set root1 [ $p1 {generateParseTree String} "~(5 & 2 | 4)"]
    set root2 [ $p1 {generateParseTree String} "(5>4) & (2==2)"]
    set root3 [ $p1 {generateParseTree String} "(false) | (2!=2)"]

    set res1  [ $root1 evaluateParseTree ]
    set res2  [ $root2 evaluateParseTree ]
    set res3  [ $root3 evaluateParseTree ]

    set c1 [$res1 getClass]
    set c2 [$res2 getClass]
    set c3 [$res3 getClass]
    list  [$c1 getName ] [$res1 toString] [$c2 getName ] [$res2 toString] [$c3 getName ] [$res3 toString]
} {pt.data.IntToken -5 pt.data.BooleanToken true pt.data.BooleanToken false}

######################################################################
####
# Need to test that the tree can be reevaluated an arbitrary number of times
test PtParser-9.0 {Check that evaluation of the parse tree does not change the parse tree} {
    set p1 [java::new pt.data.parser.PtParser]
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

    list [$res1a getValue] [$res1b getValue] [$res2a getValue] [$res2b getValue] [$res3a getValue] [$res3b getValue] [$res4a getValue] [$res4b getValue] [$res5a getValue] [$res5b getValue]
} {5 5 -1 -1 6 6 0.5 0.5 2 2}
