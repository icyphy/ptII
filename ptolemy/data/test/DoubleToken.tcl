# Tests for the DoubleToken class
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
test DoubleToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.DoubleToken]
    $p toString
} {0.0}

######################################################################
####
# 
test DoubleToken-1.1 {Create a non-empty instance from an double} {
    set p [java::new {ptolemy.data.DoubleToken double} 5.5]
    $p toString
} {5.5}

######################################################################
####
# 
test DoubleToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.DoubleToken String} "7.77"]
    $p toString
} {7.77}

######################################################################
####
# 
test DoubleToken-2.0 {Create a non-empty instance and query its value as a Complex} {
    set p [java::new {ptolemy.data.DoubleToken double} 3.3]
    set res [$p complexValue]
    list [$res toString]
} {{3.3 + 0.0i}}

######################################################################
####
# 
test DoubleToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.DoubleToken double} 3.3]
    set res1 [$p doubleValue]
    list $res1
} {3.3}

######################################################################
####
# 
test DoubleToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.DoubleToken double} 12]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.DoubleToken to an int losslessly.}}

######################################################################
####
# 
test DoubleToken-2.3 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.DoubleToken double} 12]
   catch {$p longValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.DoubleToken to a long losslessly.}}

######################################################################
####
# 
test DoubleToken-2.4 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.DoubleToken double} 12.2]
    $p stringValue
} {12.2}

######################################################################
####
# 
test DoubleToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.DoubleToken double} 12.2]
    set token [$p zero]

    list [$token toString]
} {0.0}
######################################################################
####
# 
test DoubleToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.DoubleToken double} 12.2]
    set token [$p one]

    list [$token toString]
} {1.0}

######################################################################
####
# Test addition of doubles to Token types below it in the lossless 
# type hierarchy, and with other doubles.
test DoubleToken-3.0 {Test adding doubles.} {
    set p [java::new {ptolemy.data.DoubleToken double} 12.2]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {24.4 24.4}
######################################################################
####
# 
test DoubleToken-3.1 {Test adding doubles and ints.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {14.2 14.2 14.2}

######################################################################
####
# Test division of doubles with Token types below it in the lossless 
# type hierarchy, and with other doubles.
test DoubleToken-4.0 {Test dividing doubles.} {
    set p [java::new {ptolemy.data.DoubleToken double} 12.2]
    set res1 [$p divide $p]
    set res2 [$p divideReverse $p]

    list [$res1 toString] [$res2 toString]
} {1.0 1.0}
######################################################################
####
# 
test DoubleToken-4.1 {Test dividing doubles and ints.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 divide $tok2]
    set res2 [$tok1 divideReverse $tok2]

    set res3 [$tok2 divide $tok1]
 
    list [$res1 toString] [$res2 toString] [$res3 toString]
} {6.1 0.164 0.164}

######################################################################
####
# Test isEqualTo operator applied to other doubles and Tokens types 
# below it in the lossless type hierarchy.
test DoubleToken-5.0 {Test equality between doubles.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.DoubleToken double} 2.2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}
######################################################################
####
# 
test DoubleToken-5.1 {Test equality between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]
    set tok3 [java::new {ptolemy.data.DoubleToken double} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isEqualTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# Test modulo operator between doubles and ints.
test DoubleToken-6.0 {Test modulo between doubles.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.DoubleToken double} 2.2]

    set res1 [$tok1 modulo $tok1]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {0.0 2.2}
######################################################################
####
# 
test DoubleToken-6.1 {Test modulo operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    set res3 [$tok2 modulo $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {0.2 3.0 3.0}

######################################################################
####
# Test multiply operator between doubles and ints.
test DoubleToken-7.0 {Test multiply operator between doubles.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.DoubleToken double} 2.2]

    set res1 [$tok1 multiply $tok1]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {148.84 26.84}
######################################################################
####
# 
test DoubleToken-7.1 {Test multiply operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    set res3 [$tok2 multiply $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {36.6 36.6 36.6}


######################################################################
####
# Test subtract operator between doubles and ints.
test DoubleToken-8.0 {Test subtract operator between doubles.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.DoubleToken double} 2.2]

    set res1 [$tok1 subtract $tok1]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {0.0 -10.0}
######################################################################
####
# 
test DoubleToken-8.1 {Test subtract operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.DoubleToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    set res3 [$tok2 subtract $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {9.2 -9.2 -9.2}
