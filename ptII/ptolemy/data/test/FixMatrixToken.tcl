# Tests for the FixMatrixToken class
#
# @Author: Yuhong Xiong, Elaine Cheong
#
# @Version $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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
test FixMatrixToken-1.1 {Create a non-empty instance from a String} {
    set p [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.0, 4.0\], 8, 4)"]
    $p toString
} {[fix(1.0,8,4), fix(2.0,8,4); fix(3.0,8,4), fix(4.0,8,4)]}

######################################################################
####
# 
test FixMatrixToken-2.0 {Test equals} {
    set p1 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.5\], 8, 4)"]
    set p2 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.5\], 8, 4)"]
    set p3 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.0, 4.0\], 8, 4)"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

test FixMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[fix(0.0,8,4), fix(0.0,8,4); fix(0.0,8,4), fix(0.0,8,4)]}}

test FixMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[fix(1.0,8,4), fix(0.0,8,4); fix(0.0,8,4), fix(1.0,8,4)]}}

######################################################################
####
# 
test FixMatrixToken-2.7 {Test matrixToArray} {
    set array [java::call ptolemy.data.MatrixToken matrixToArray [java::cast ptolemy.data.MatrixToken [$p one]]]
    $array toString
} {{fix(1.0,8,4), fix(0.0,8,4), fix(0.0,8,4), fix(1.0,8,4)}}

test FixMatrixToken-2.8 {Test arrayToMatrix} {
    set token [java::call ptolemy.data.MatrixToken arrayToMatrix [$array arrayValue] 2 2]
    $token toString
} {[fix(1.0,8,4), fix(0.0,8,4); fix(0.0,8,4), fix(1.0,8,4)]}

######################################################################
####
# 
test FixMatrixToken-3.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.2\], 8, 4)"]
    set p2 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.5, 4.2\], 8, 4)"]
    set p3 [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.0, 6.0\], 8, 4)"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {10 10 12}

######################################################################
####
test FixMatrixToken-3.3 {Test adding FixMatrixToken to FixMatrixToken.} {
   set q [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 1.0; 3.0, 4.0\], 8, 4)"]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {{[fix(2.0,9,5), fix(3.0,9,5); fix(6.0,9,5), fix(8.0,9,5)]} {[fix(2.0,9,5), fix(3.0,9,5); fix(6.0,9,5), fix(8.0,9,5)]} {[fix(2.0,9,5), fix(3.0,9,5); fix(6.0,9,5), fix(8.0,9,5)]} {[fix(2.0,9,5), fix(3.0,9,5); fix(6.0,9,5), fix(8.0,9,5)]}}

test FixMatrixToken-3.7 {Test adding FixMatrixToken to FixToken.} {
    set r [java::new {ptolemy.data.FixToken String} "fix(2.0, 8, 4)"]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[fix(3.0,9,5), fix(4.0,9,5); fix(5.0,9,5), fix(6.0,9,5)]} {[fix(3.0,9,5), fix(4.0,9,5); fix(5.0,9,5), fix(6.0,9,5)]} {[fix(3.0,9,5), fix(4.0,9,5); fix(5.0,9,5), fix(6.0,9,5)]} {[fix(3.0,9,5), fix(4.0,9,5); fix(5.0,9,5), fix(6.0,9,5)]}}

######################################################################
####

test FixMatrixToken-7.3 {Test multiplying FixMatrixToken to FixMatrixToken.} {
    set p [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 2.0; 3.0, 4.0\], 8, 4)"]
   set q [java::new {ptolemy.data.FixMatrixToken String} "fix(\[1.0, 1.0; 3.0, 4.0\], 8, 4)"]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[fix(7.0,17,9), fix(9.0,17,9); fix(15.0,17,9), fix(19.0,17,9)]} {[fix(4.0,17,9), fix(6.0,17,9); fix(15.0,17,9), fix(22.0,17,9)]} {[fix(4.0,17,9), fix(6.0,17,9); fix(15.0,17,9), fix(22.0,17,9)]} {[fix(7.0,17,9), fix(9.0,17,9); fix(15.0,17,9), fix(19.0,17,9)]}}

test FixMatrixToken-7.3.1 {Test multiply operator between FixMatrixToken and FixMatrixToken of different dimensions} {
    set q [java::new {ptolemy.data.FixMatrixToken String} "fix(\[2.0, 1.0, 3.0; 3.0, 1.0, 6.0\], 8, 4)"]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[fix(8.0,17,9), fix(3.0,17,9), fix(15.0,17,9); fix(18.0,17,9), fix(7.0,17,9), fix(33.0,17,9)]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.FixMatrixToken '[fix(2.0,8,4), fix(1.0,8,4), fix(3.0,8,4); fix(3.0,8,4), fix(1.0,8,4), fix(6.0,8,4)]' and ptolemy.data.FixMatrixToken '[fix(1.0,8,4), fix(2.0,8,4); fix(3.0,8,4), fix(4.0,8,4)]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.FixMatrixToken '[fix(2.0,8,4), fix(1.0,8,4), fix(3.0,8,4); fix(3.0,8,4), fix(1.0,8,4), fix(6.0,8,4)]' and ptolemy.data.FixMatrixToken '[fix(1.0,8,4), fix(2.0,8,4); fix(3.0,8,4), fix(4.0,8,4)]' because the matrices have incompatible dimensions.} {[fix(8.0,17,9), fix(3.0,17,9), fix(15.0,17,9); fix(18.0,17,9), fix(7.0,17,9), fix(33.0,17,9)]}}

test FixMatrixToken-7.7 {Test multiplying FixMatrixToken to FixToken.} {
    set r [java::new {ptolemy.data.FixToken String} "fix(2.0, 8, 4)"]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {{[fix(2.0,16,8), fix(4.0,16,8); fix(6.0,16,8), fix(8.0,16,8)]} {[fix(2.0,16,8), fix(4.0,16,8); fix(6.0,16,8), fix(8.0,16,8)]} {[fix(2.0,16,8), fix(4.0,16,8); fix(6.0,16,8), fix(8.0,16,8)]} {[fix(2.0,16,8), fix(4.0,16,8); fix(6.0,16,8), fix(8.0,16,8)]}}
