# Tests for the FunctionToken class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
test FunctionToken-1.0 {Create an empty instance} {
    set r [java::new {ptolemy.data.FunctionToken} "function() 4"]
    list [$r toString] [[$r getType] toString]
} {{(function() 4)} {(function() int)}}

######################################################################
####
# 
test FunctionToken-1.1 {Create a non-empty instance} {
    set r [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"]
    list [$r toString] [[$r getType] toString]
} {{(function(x, y) (4+x+y))} {(function(a0:general, a1:general) general)}}

test FunctionToken-1.2 {Create a non-empty instance} {
    set r [java::new {ptolemy.data.FunctionToken} "function(x:int,y:double) 4+x+y"]
    list [$r toString] [[$r getType] toString]
} {{(function(x:int, y:double) (4+x+y))} {(function(a0:int, a1:double) double)}}

test FunctionToken-1.3 {Create an empty instance} {
    set r [java::new {ptolemy.data.FunctionToken} "function(x,y) 4"]
    list [$r toString] [[$r getType] toString]
} {{(function(x, y) 4)} {(function(a0:general, a1:general) int)}}

test FunctionToken-1.4 {Create an empty instance} {
    set r [java::new {ptolemy.data.FunctionToken} "function(x:long) function(y:double) 4"]
    list [$r toString] [[$r getType] toString]
} {{(function(x:long) (function(y:double) 4))} {(function(a0:long) (function(a0:double) int))}}

test FunctionToken-1.5 {Create an empty instance} {
    set r [java::new {ptolemy.data.FunctionToken} "function(x:(function(y:double) long)) 4"]
    list [$r toString] [[$r getType] toString]
} {{(function(x:(function(a0:double) long)) 4)} {(function(a0:(function(a0:double) long)) int)}}

######################################################################
####
# 
test FunctionToken-2.1 {Test add} {
    # first record is {name="foo", value=1, extra1=2}
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(a,b) 8+2"]

    [$r1 add $r2] toString
} {{(function(x, y) (function(x,y) 4+x+y)(x,y) + (function(a,b) 8+2)(x,y)}} {add not implemented}

######################################################################
####
# 
test FunctionToken-10.0 {test equals} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4+a+b"]
    
    list [$r1 equals $r1] [$r1 equals $r2] [$r1 equals $r3]
} {1 0 0}

######################################################################
####
# 

test FunctionToken-11.0 {test isCloseTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4+a+b"]
    
    list [[$r1 isCloseTo $r1] toString] [[$r1 isCloseTo $r2] toString] [[$r1 isCloseTo $r3] toString]
} {true true false}

test FunctionToken-11.1 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x+y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4+a+b"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true true false}

test FunctionToken-11.2 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x) 4+5"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b,c) 4+5"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.3 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+5"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+x"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4+5"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.4 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4+5"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4-x"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4-5"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.5 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4*5"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4*5"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4/5"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true true false}

test FunctionToken-11.6 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4^5"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4^5"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4^5"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true true false}

test FunctionToken-11.7 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4&5"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) 4|5"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) 4&5"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.8 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) x>y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) x<y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) a>b"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.9 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) x&&y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) x||y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) a&&b"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.10 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) {x,y}"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) {x,y,5}"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) {a,b}"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.11 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) {foo=x,bar=y}"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) {foo=x,baz=y}"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) {bar=b,foo=a}"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.12 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) x>>y"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) x<<y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) a>>b"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.13 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x,y) -x"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) -y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a,b) -a"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}

test FunctionToken-11.14 {test isEqualTo} {
    set r1 [java::new {ptolemy.data.FunctionToken} "function(x:int,y:int) function(z:int) x+y+z"] 
    set r2 [java::new {ptolemy.data.FunctionToken} "function(x,y) -y"] 
    set r3 [java::new {ptolemy.data.FunctionToken} "function(a:int,b:int) function(c:int) a+b+c"]
    
    list [[$r1 isEqualTo $r1] toString] [[$r1 isEqualTo $r2] toString] [[$r1 isEqualTo $r3] toString]
} {true false false}
