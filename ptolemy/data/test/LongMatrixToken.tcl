# Tests for the LongMatrixToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#

######################################################################
####
# 
test LongMatrixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.LongMatrixToken]
    $p toString
} {[0L]}

######################################################################
####
# 
test LongMatrixToken-1.1 {Create a non-empty instance from an int} {
    set a [java::new {long[][]} {2 2} {{5 4} {3 2}}]
    set p [java::new {ptolemy.data.LongMatrixToken long[][]} $a]
    $p toString
} {[5L, 4L; 3L, 2L]}

######################################################################
####
# 
test LongMatrixToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.LongMatrixToken String} "\[5L, 4; 3, 2\]"]
    $p toString
} {[5L, 4L; 3L, 2L]}

######################################################################
####
# 
test LongMatrixToken-2.0 {Create a non-empty instance and query its value as an int} {
    catch {$p intMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongMatrixToken '[5L, 4L; 3L, 2L]' to the type int matrix.}}

######################################################################
####
# 
test LongMatrixToken-2.1 {Create a non-empty instance and query its value as a double} {
    catch {$p doubleMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongMatrixToken '[5L, 4L; 3L, 2L]' to the type double matrix.}}

######################################################################
####
# 
test LongMatrixToken-2.2 {Create a non-empty instance and query its value as a long} {
    set res1 [$p longMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5 4} {3 2}}

######################################################################
####
# 
test LongMatrixToken-2.3 {Create a non-empty instance and query its value as a string} {
    set res1 [$p toString]
    list $res1
} {{[5L, 4L; 3L, 2L]}}

######################################################################
####
#
test LongMatrixToken-2.4 {Create a non-empty instance and query its value as a complex} {
    catch {$p complexMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongMatrixToken '[5L, 4L; 3L, 2L]' to the type complex matrix.}}

######################################################################
####
# 
test LongMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[0L, 0L; 0L, 0L]}}

######################################################################
####
# 
test LongMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[1L, 0L; 0L, 1L]}}

######################################################################
####
# 
test LongMatrixToken-2.7 {Test matrixToArray} {
    set array [java::call ptolemy.data.MatrixToken matrixToArray [$p one]]
    $array toString
} {{1L, 0L, 0L, 1L}}

######################################################################
####
# Test addition of longs to Token types below it in the lossless 
# type hierarchy, and with other longs.
test LongMatrixToken-3.0 {Test adding longs.} {
    set b [java::new {long[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    set res1 [$p add $q]

    list [$res1 toString] 
} {{[7L, 5L; 6L, 3L]}}

test LongMatrixToken-3.4 {Test adding LongMatrixToken to LongToken.} {
    set r [java::new {ptolemy.data.LongToken long} 2]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7L, 6L; 5L, 4L]} {[7L, 6L; 5L, 4L]} {[7L, 6L; 5L, 4L]} {[7L, 6L; 5L, 4L]}}

######################################################################
####
# Test division of longs with Token types below it in the lossless 
# type hierarchy, and with other longs. Note that dividing longs could 
# give a double.
test LongMatrixToken-4.0 {Test dividing longs.} {
    set b [java::new {long[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.LongMatrixToken '[5L, 4L; 3L, 2L]' and ptolemy.data.LongMatrixToken '[2L, 1L; 3L, 1L]'}}

######################################################################
####
# Test equals operator applied to other longs and Tokens types 
# below it in the lossless type hierarchy.
test LongMatrixToken-5.0 {Test equality between longs.} {
    set q2 [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    set res1 [$q isEqualTo $q2]
    set res2 [$q isEqualTo $p]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# Test modulo operator between longs and longs.
test LongMatrixToken-6.0 {Test modulo between longs.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.LongMatrixToken '[5L, 4L; 3L, 2L]' and ptolemy.data.LongMatrixToken '[2L, 1L; 3L, 1L]'}}

######################################################################
####
# Test multiply operator between longs and longs.
test LongMatrixToken-7.0 {Test multiply operator between longs.} {
    set b3 [java::new {long[][]} {2 3} {{2 1 3} {3 1 6}}]
    set q3 [java::new {ptolemy.data.LongMatrixToken long[][]} $b3]
    set res1 [$p multiply $q]
    set res2 [$p multiply $q3]
    catch {$q3 multiply $p} res3

    list [$res1 toString] [$res2 toString] $res3
} {{[22L, 9L; 12L, 5L]} {[22L, 9L, 39L; 12L, 5L, 21L]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.LongMatrixToken '[2L, 1L, 3L; 3L, 1L, 6L]' and ptolemy.data.LongMatrixToken '[5L, 4L; 3L, 2L]' because the matrices have incompatible dimensions.}}

test LongMatrixToken-7.4 {Test multiplying LongMatrixToken to LongToken.} {
    set r [java::new {ptolemy.data.LongToken long} 2]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10L, 8L; 6L, 4L]} {[10L, 8L; 6L, 4L]} {[10L, 8L; 6L, 4L]} {[10L, 8L; 6L, 4L]}}


######################################################################
####
# Test subtract operator between longs and longs.
test LongMatrixToken-8.0 {Test subtract operator between longs.} {
    set b [java::new {long[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    set res1 [$p subtract $q]

    list [$res1 toString] 
} {{[3L, 3L; 0L, 1L]}}

test LongMatrixToken-8.4 {Test subtracting LongMatrixToken to LongToken.} {
    set r [java::new {ptolemy.data.LongToken long} 2]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3L, 2L; 1L, 0L]} {[-3L, -2L; -1L, 0L]} {[-3L, -2L; -1L, 0L]} {[3L, 2L; 1L, 0L]}}

######################################################################
####
# 
test LongMatrixToken-9.0 {Test equals} {
    set p1 [java::new {ptolemy.data.LongMatrixToken String} "\[1L, 2L; 3L, 4L\]"]
    set p2 [java::new {ptolemy.data.LongMatrixToken String} "\[1L, 2L; 3L, 4L\]"]
    set p3 [java::new {ptolemy.data.LongMatrixToken String} "\[9L, 8L; 7L, 6L\]"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test LongMatrixToken-10.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.LongMatrixToken String} "\[1L, 2L; 3L, 4L\]"]
    set p2 [java::new {ptolemy.data.LongMatrixToken String} "\[1L, 2L; 3L, 4L\]"]
    set p3 [java::new {ptolemy.data.LongMatrixToken String} "\[9L, 8L; 7L, 6L\]"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {10 10 30}

