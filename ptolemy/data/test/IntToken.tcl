# Tests for the IntToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
test IntToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.IntToken]
    $p toString
} {0}

######################################################################
####
# 
test IntToken-1.1 {Create a non-empty instance from an int} {
    set p [java::new {ptolemy.data.IntToken int} 5]
    $p toString
} {5}

######################################################################
####
# 
test IntToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.IntToken String} "7"]
    $p toString
} {7}

######################################################################
####
# 
test IntToken-2.0 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.IntToken int} 3]
    set res1 [$p intValue]
    list $res1
} {3}

######################################################################
####
# 
test IntToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p doubleValue
} {12.0}

######################################################################
####
# 
test IntToken-2.2 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p longValue
} {12}

######################################################################
####
# 
test IntToken-2.3 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p stringValue
} {12}

######################################################################
####
#
test IntToken-2.4 {Create a non-empty instance and query its value as a complex#} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    [$p complexValue] toString
} {12.0 + 0.0i}

######################################################################
####
# 
test IntToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.IntToken int} 7]
    set token [$p zero]

    list [$token toString]
} {0}
######################################################################
####
# 
test IntToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.IntToken int} 7]
    set token [$p one]

    list [$token toString]
} {1}

######################################################################
####
# Test addition of ints to Token types below it in the lossless 
# type hierarchy, and with other ints.
test IntToken-3.0 {Test adding ints.} {
    set p [java::new {ptolemy.data.IntToken int} 7]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {14 14}

######################################################################
####
# Test division of ints with Token types below it in the lossless 
# type hierarchy, and with other ints. Note that dividing ints could 
# give a double.
test IntToken-4.0 {Test dividing ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 14]
 
    set res1 [$tok1 divide $tok1]
    set res2 [$tok1 divideReverse $tok1]

    set res3 [$tok1 divide $tok2]
    set res4 [$tok1 divideReverse $tok2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {1 1 0 2}

######################################################################
####
# Test isEqualTo operator applied to other ints and Tokens types 
# below it in the lossless type hierarchy.
test IntToken-5.0 {Test equality between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 4]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# Test modulo operator between ints and ints.
test IntToken-6.0 {Test modulo between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {1 2}

######################################################################
####
# Test multiply operator between ints and ints.
test intToken-7.0 {Test multiply operator between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {14 14}

######################################################################
####
# Test subtract operator between ints and ints.
test IntToken-8.0 {Test subtract operator between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {5 -5}

######################################################################
####
# Do not really need this test, but leave in for now.
test IntToken-9.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.IntToken int} 23]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{value is 2323.....}}
