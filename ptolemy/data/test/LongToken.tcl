# Tests for the LongToken class
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
test LongToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.LongToken]
    $p toString
} {0}

######################################################################
####
# 
test LongToken-1.1 {Create an instance with a value} {
    set token [java::new {ptolemy.data.LongToken long} 7]
    $token toString
} {7}

######################################################################
####
# 
test LongToken-1.2 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.LongToken String} "5"]
    $token toString
} {5}

######################################################################
####
# 
test LongToken-2.0 {Create a non-empty instance and query its value as an long} {
    set p [java::new {ptolemy.data.LongToken long} 3]
    set res1 [$p longValue]
    list $res1
} {3}

######################################################################
####
# 
test LongToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p doubleValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.LongToken to a double losslessly.}}

######################################################################
####
# 
test LongToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.LongToken to an int losslessly.}}

######################################################################
####
# 
test LongToken-2.3 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    $p stringValue
} {12}

######################################################################
####
# 
test LongToken-2.4 {Create a non-empty instance and query its value as a complex#} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p complexValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.LongToken to a Complex losslessly.}}

######################################################################
####
# 
test LongToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.LongToken long} 7]
    set token [$p zero]

    list [$token toString]
} {0}
######################################################################
####
# 
test LongToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.LongToken long} 7]
    set token [$p one]

    list [$token toString]
} {1}

######################################################################
####
# Test addition of longs to Token types below it in the lossless 
# type hierarchy, and with other longs.
test LongToken-3.0 {Test adding longs.} {
    set p [java::new {ptolemy.data.LongToken long} 7]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {14 14}
######################################################################
####
# 
test LongToken-3.1 {Test adding longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {9 9 9}

######################################################################
####
# Test division of longs with Token types below it in the lossless 
# type hierarchy, and with other ints.
test LongToken-4.0 {Test dividing longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 5]
    set tok2 [java::new {ptolemy.data.LongToken long} 12]
 
    set res1 [$tok1 divide $tok1]
    set res2 [$tok1 divideReverse $tok1]

    set res3 [$tok1 divide $tok2]
    set res4 [$tok1 divideReverse $tok2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {1 1 0 2}

######################################################################
####
# 
test LongToken-4.1 {Test dividing longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 divide $tok2]
    set res2 [$tok1 divideReverse $tok2]

    set res3 [$tok2 divide $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {3 0 0}

######################################################################
####
# Test isEqualTo operator applied to other longs and Tokens types 
# below it in the lossless type hierarchy.
test LongToken-5.0 {Test equality between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}
######################################################################
####
# 
test LongToken-5.1 {Test equality between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 12]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]
    set tok3 [java::new {ptolemy.data.LongToken long} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isEqualTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# Test modulo operator between longs and ints.
test LongToken-6.0 {Test modulo between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {1 2}
######################################################################
####
# 
test LongToken-6.1 {Test modulo operator between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    set res3 [$tok2 modulo $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {1 3 3}

######################################################################
####
# Test multiply operator between longs and ints.
test longToken-7.0 {Test multiply operator between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {14 14}
######################################################################
####
# 
test LongToken-7.1 {Test multiply operator between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    set res3 [$tok2 multiply $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {21 21 21}


######################################################################
####
# Test subtract operator between longs and ints.
test LongToken-8.0 {Test subtract operator between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {5 -5}
######################################################################
####
# 
test LongToken-8.1 {Test subtract operator between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    set res3 [$tok2 subtract $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {4 -4 -4}

######################################################################
####
# Do not really need this test, but leave in for now.
test LongToken-9.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.LongToken long} 23]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{"value is 2323....."}}
