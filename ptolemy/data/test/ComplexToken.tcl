# Tests for the ComplexToken class
#
# @Author: Yuhong Xiong
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
test ComplexToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.ComplexToken]
    $p toString
} {0.0 + 0.0i}

######################################################################
####
# 
test ComplexToken-1.1 {Create a non-empty instance from a Complex} {
    set c [java::new {ptolemy.math.Complex double double} 1.1 2.2]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    $p toString
} {1.1 + 2.2i}

######################################################################
####
# 
#test ComplexToken-1.2 {Create a non-empty instance from an String} {
#    set p [java::new {ptolemy.data.ComplexToken String} "7.77"]
#    $p toString
#} {7.77}

######################################################################
####
# 
test ComplexToken-2.0 {Test complexValue} {
    set c [java::new {ptolemy.math.Complex double double} 3.3 4.4]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set res1 [$p complexValue]
    list [$res1 toString]
} {{3.3 + 4.4i}}

######################################################################
####
# 
test ComplexToken-2.1 {Test doubleValue} {
    # use the Complex above
    catch {$p doubleValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.ComplexToken to a double losslessly.}}

######################################################################
####
# 
test ComplexToken-2.2 {Test intValue} {
    # use the Complex above
    catch {$p intValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.ComplexToken to an int losslessly.}}

######################################################################
####
# 
test ComplexToken-2.3 {Test longValue} {
    # use the Complex above
    catch {$p longValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Cannot convert the value in ptolemy.data.ComplexToken to a long losslessly.}}

######################################################################
####
# 
test ComplexToken-2.4 {Test stringValue} {
    # use the Complex above
    $p stringValue
} {3.3 + 4.4i}

######################################################################
####
# 
test ComplexToken-2.5 {Test additive identity} {
    set token [$p zero]

    list [$token toString]
} {{0.0 + 0.0i}}

######################################################################
####
# 
test ComplexToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.ComplexToken}]
    set token [$p one]

    list [$token toString]
} {{1.0 + 0.0i}}

######################################################################
####
# Test addition of Complex to Token types below it in the lossless 
# type hierarchy, and with other Complex.
test ComplexToken-3.0 {Test adding Complex} {
    set c [java::new {ptolemy.math.Complex double double} 5.5 6.6]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set res [$p add $p]

    list [$res toString]
} {{11.0 + 13.2i}}

######################################################################
####
# 
test ComplexToken-3.1 {Test adding Complex and double} {
    # use the Complex above
    set d [java::new {ptolemy.data.DoubleToken double} 2.5]
    set res1 [$p add $d]
    set res2 [$p addReverse $d]

    set res3 [$d add $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{8.0 + 6.6i} {8.0 + 6.6i} {8.0 + 6.6i}}

######################################################################
####
# 
test ComplexToken-3.2 {Test adding Complex and int} {
    # use the Complex above
    set i [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p add $i]
    set res2 [$p addReverse $i]

    set res3 [$i add $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{7.5 + 6.6i} {7.5 + 6.6i} {7.5 + 6.6i}}

######################################################################
####
# Test division of Complex with Token types below it in the lossless 
# type hierarchy, and with other Complex.
test ComplexToken-4.0 {Test dividing Complex} {
    set c [java::new {ptolemy.math.Complex double double} 7.7 8.8]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set res [$p divide $p]

    list [$res toString]
} {{1.0 + 0.0i}}

######################################################################
####
# 
test ComplexToken-4.1 {Test dividing Complex and double} {
    set c [java::new {ptolemy.math.Complex double double} 8.0 4.0]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set d [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p divide $d]
    set res2 [$p divideReverse $d]

    list [$res1 toString] [$res2 toString]
} {{4.0 + 2.0i} {0.2 - 0.1i}}

######################################################################
####
# 
test ComplexToken-4.2 {Test dividing Complex and int} {
    # use the Complex above
    set i [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p divide $i]
    set res2 [$p divideReverse $i]

    set res3 [$i divide $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{4.0 + 2.0i} {0.2 - 0.1i} {0.2 - 0.1i}}

######################################################################
####
# Test isEqualTo operator applied to other Complex and Tokens types 
# below it in the lossless type hierarchy.
test ComplexToken-5.0 {Test equality between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 7.7 8.8]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 0.5 0.6]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 {isEqualTo ptolemy.data.Token} $p1]
    set res2 [$p1 {isEqualTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test ComplexToken-5.1 {Test equality between Complex and double} {
    set c1 [java::new {ptolemy.math.Complex double double} 8.0 0.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 4.0 0.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set d1 [java::new {ptolemy.data.DoubleToken double} 8]
    set d2 [java::new {ptolemy.data.DoubleToken double} 4]

    set res1 [$p1 {isEqualTo ptolemy.data.Token} $d1]
    set res2 [$p1 {isEqualTo ptolemy.data.Token} $d2]

    set res3 [$d1 {isEqualTo ptolemy.data.Token} $p1]
    set res4 [$d1 {isEqualTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true false}

######################################################################
####
# 
test ComplexToken-5.2 {Test equality between Complex and int} {
    # use the Complex above

    set i1 [java::new {ptolemy.data.IntToken int} 8]
    set i2 [java::new {ptolemy.data.IntToken int} 4]

    set res1 [$p1 {isEqualTo ptolemy.data.Token} $i1]
    set res2 [$p1 {isEqualTo ptolemy.data.Token} $i2]

    set res3 [$i1 {isEqualTo ptolemy.data.Token} $p1]
    set res4 [$i1 {isEqualTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true false}

######################################################################
####
#
test ComplexToken-6.0 {Test modulo} {
    # use the Complex  above
    catch {res [$p1 modulo $p1]} msg

    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Modulo operation not supported: ptolemy.data.ComplexToken modulo ptolemy.data.ComplexToken.}}

######################################################################
####
# Test multiply
test ComplexToken-7.0 {Test multiply operator between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 3.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 4.0 5.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 multiply $p2]

    list [$res1 toString]
} {{-7.0 + 22.0i}}

######################################################################
####
#
test ComplexToken-7.1 {Test multiply between Complex and double} {
    # use the Complex above
    set d [java::new {ptolemy.data.DoubleToken double} 6]

    set res1 [$p1 multiply $d]
    set res2 [$p1 multiplyReverse $d]
    set res3 [$d multiply $p1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{12.0 + 18.0i} {12.0 + 18.0i} {12.0 + 18.0i}}

######################################################################
####
# 
test ComplexToken-7.2 {Test multiply between Complex and int} {
    # use the Complex above
    set i [java::new {ptolemy.data.IntToken int} 6]

    set res1 [$p1 multiply $i]
    set res2 [$p1 multiplyReverse $i]
    set res3 [$i multiply $p1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{12.0 + 18.0i} {12.0 + 18.0i} {12.0 + 18.0i}}

######################################################################
####
# Test subtract operator between complex
test ComplexToken-8.0 {Test subtract between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 8.0 9.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 subtract $p2]

    list [$res1 toString]
} {{-2.0 - 2.0i}}

######################################################################
####
# 
test ComplexToken-8.1 {Test subtract operator between Complex and double} {
    # use the complex above
    set d [java::new {ptolemy.data.DoubleToken double} 12.0]

    set res1 [$p1 subtract $d]
    set res2 [$p1 subtractReverse $d]

    set res3 [$d subtract $p1]

    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {{-6.0 + 7.0i} {6.0 - 7.0i} {6.0 - 7.0i}}
