# Tests for the *MatrixMath classes
#
# @Author: Christopher Hylands (tests only)
#
# @Version $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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

if {[string compare jdkStackTrace [info procs jdkStackTrace]] == 1} then { 
    source [file join $PTII util testsuite jdkTools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1



######################################################################
####
# 
proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}




# Complex numbers to be used
set complex1 [java::new ptolemy.math.Complex 1.0 2.0]
set complex2 [java::new ptolemy.math.Complex 3.0 -4.0]
set complex3 [java::new ptolemy.math.Complex -4.9 -6.0]
set complex4 [java::new ptolemy.math.Complex -7.0 8.0]
set complex5 [java::new ptolemy.math.Complex -0.25 0.4]

set complex1_1 [java::new {ptolemy.math.Complex[][]} 1 [list \
	[list $complex1]]]

set complex2_2 [java::new {ptolemy.math.Complex[][]} {2} [list \
	[list $complex1 $complex2] \
	[list $complex3 $complex4]]]

set double1 44.5
set double1_1 [java::new {double[][]} 1 [list [list -56.4]]]
set double2_2 [java::new {double[][]} {2 2} [list [list 3.7 -6.6] \
                                       [list 4862.2 236.1]]]

set float1 44.5
set float1_1 [java::new {float[][]} 1 [list -56.4]]
set float2_2 [java::new {float[][]} {2 2} [list [list 3.7 -6.6] \
                                       [list 4862.2 236.1]]]
set int1 -56
set int1_1 [java::new {int[][]} 1 [list -56]]
set int2_2 [java::new {int[][]} {2 2} [list [list 4 -7] \
                                       [list 4862 236]]]

set long1 -56
set long1_1 [java::new {long[][]} 1 [list -56]]
set long2_2 [java::new {long[][]} {2 2} [list [list 4 -7] \
                                       [list 4862 236]]]


# Test an operation that takes a matrix and a scalar,
# like add(long[][], long)
# Arguments:
#    op - The operation to be tested, for example "add"
#    types - a list of lists of types, where each element of the list
#            contains four subelements: 
#              The base matrix type, which would go in 
#                    ptolemy.math.xxxMatrixMath
#              The base type, for example double or Complex
#              The base name of the variable to use, for example double 
#              The expected results
proc testMatrixScalar {op types} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test $op "$m.MatrixMath.$op\($t\[\]\[\], $t\)" {
	    set matrix ${v}2_2
	    global $matrix ${v}1
	    set matrixResults [java::call ptolemy.math.${m}MatrixMath \
		    [list $op "$t\[\]\[\]" $t] [subst $$matrix] [subst $${v}1]]
	    set stringResults [java::call ptolemy.math.${m}MatrixMath \
		    toString $matrixResults]	
	    regsub -all {,} $stringResults {} stringAsList
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}

# Test an operation that takes a matrix and a matrix
# like add(long[][], long[][]
# Arguments:
#    op - The operation to be tested, for example "add"
#    types - a list of lists of types, where each element of the list
#            contains four subelements: 
#              The base matrix type, which would go in 
#                    ptolemy.math.xxxMatrixMath
#              The base type, for example double or Complex
#              The base name of the variable to use, for example double 
#              The expected results
proc testMatrixMatrix {op types} {
    foreach typeList $types {
	set m [lindex $typeList 0]
	set t [lindex $typeList 1]
	set v [lindex $typeList 2]
	set expectedResults [lindex $typeList 3]
	
	test $op "$m.MatrixMath.$op\($t\[\]\[\], $t\)" {
	    set matrix ${v}2_2
	    global $matrix ${v}1
	    set matrixResults [java::call ptolemy.math.${m}MatrixMath \
		    [list $op "$t\[\]\[\]" $t\[\]\[\]] [subst $$matrix] [subst $${v}2_2]]
	    set stringResults [java::call ptolemy.math.${m}MatrixMath \
		    toString $matrixResults]	
	    regsub -all {,} $stringResults {} stringAsList
	    epsilonDiff $stringAsList $expectedResults
	} {}
    }
}

######################################################################
####
#  Test out add(xxx[][], xxx)

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 + 4.0i 4.0 - 2.0i} {-3.9 - 4.0i -6.0 + 10.0i}}}] \
	[list Double double double {{{48.2 37.9} {4906.7 280.6}}}] \
	[list Float float float {{{48.2 37.9} {4906.7 280.6}}}] \
	[list Integer int int {{{-52 -63} {4806 180}}}] \
	[list Long long long {{{-52 -63} {4806 180}}}] ]


testMatrixScalar add $types


######################################################################
####
#  Test out add(xxx[][], xxx[][])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{2.0 + 4.0i 6.0 - 8.0i} {-9.8 - 12.0i -14.0 + 16.0i}}} ] \
	[list Double double double {{{7.4 -13.2} {9724.4 472.2}}} ] \
	[list Float float float {{{7.4 -13.2} {9724.4 472.2}}} ] \
	[list Integer int int {{{8 -14} {9724 472}}}] \
	[list Long long long {{{8 -14} {9724 472}}}]]  


testMatrixMatrix add $types


######################################################################
####
#  Test out multiply(xxx[][], xxx[])

set types [list \
	[list Complex ptolemy.math.Complex complex \
	{{{-3.0 + 4.0i 11.0 + 2.0i} {7.1 - 15.8i -23.0 - 6.0i}}}] \
	[list Double double double \
	{{{164.65 -293.7} {216367.9 10506.449999999999}}}] \
	[list Float float float \
	{{{164.65001 -293.69998} {216367.9 10506.45}}}] \
	[list Integer int int {{{-224 392} {-272272 -13216}}}] \
	[list Long long long {{{-224 392} {-272272 -13216}}} ]]


testMatrixScalar multiply $types

