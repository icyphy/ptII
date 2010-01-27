# Tests for the BooleanMatrixToken class
#
# @Author: Yuhong Xiong
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
test BooleanMatrixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.BooleanMatrixToken]
    $p toString
} {[false]}

######################################################################
####
# 
test BooleanMatrixToken-1.1 {Create a non-empty instance from} {
    set a [java::new {boolean[][]} {2 2} {{true true} {false false}}]
    set p [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $a]
    $p toString
} {[true, true; false, false]}

######################################################################
####
# 
test BooleanMatrixToken-1.2 {Create a non-empty 1-D matrix} {
    set p [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, true\]"]
    $p toString
} {[true, true]}

######################################################################
####
# 
test BooleanMatrixToken-1.3 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, true; false, false\]"]
    list [$p toString] [$p getElementAt 0 0]
} {{[true, true; false, false]} 1}

######################################################################
####
# 
test BooleanMatrixToken-2.0 {Create a non-empty instance and query its value} {
    set res1 [$p booleanMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{1 1} {0 0}}

######################################################################
####
# 
test BooleanMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[false, false; false, false]}}

######################################################################
####
# 
test BooleanMatrixToken-2.5.1 {Test oneRight} {
    set token [$p oneRight] 
    list [$token toString]
} {{[true, false; false, true]}}

######################################################################
####
# 
test BooleanMatrixToken-3.0 {Test equals} {
    set p1 [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, true; false, false\]"]
    set p2 [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, true; false, false\]"]
    set p3 [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, false; false, false\]"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test BooleanMatrixToken-4.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, true; false, false\]"]
    set p2 [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, true; false, false\]"]
    set p3 [java::new {ptolemy.data.BooleanMatrixToken String} "\[true, false; false, false\]"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {2 2 1}

######################################################################
####
# 
test BooleanMatrixToken-2.7 {Test matrixToArray} {
    set array [java::call ptolemy.data.MatrixToken matrixToArray [java::cast ptolemy.data.MatrixToken [$p one]]]
    $array toString
} {{true, false, false, true}}

