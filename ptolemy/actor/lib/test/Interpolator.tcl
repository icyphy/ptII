# Test Interpolator
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
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

# compare two lists of doubles
proc deltaCompare {list1 list2} {
    set delta 1e-6
    for {set i 0} {$i < [llength $list1]} {incr i} {
	set v1 [lindex $list1 $i]
	set v2 [lindex $list2 $i]
	set diff [expr abs($v1 - $v2)]

	if {$diff> $delta} {
	    return 0 
	}
    }
    return 1
}

######################################################################
####
#
test Interpolator-1.1 {test constructor and clone with default values} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set baseInterp [java::new ptolemy.actor.lib.Interpolator $e0 baseInterp]

    set interp [java::cast ptolemy.actor.lib.Interpolator [$baseInterp clone]]
    set values [getParameter $interp values]
    set valuesStr [[$values getToken] toString]
    set indexes [getParameter $interp indexes]
    set indexesStr [[$indexes getToken] toString]
    set period [getParameter $interp period]
    set periodStr [[$period getToken] toString]
    set order [getParameter $interp order]
    set orderStr [[$order getToken] toString]

    list $valuesStr $indexesStr $periodStr $orderStr
} {{[1.0, 0.0]} {[0, 1]} 2 0}

######################################################################
#### Test Interpolator in an SDF model
#
test Interpolator-2.1 {test with the default output values} {
    set e0 [sdfModel 8]
    $interp setContainer $e0

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $interp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0}

######################################################################
####
#
test Interpolator-2.2 {test 1st order} {
    $order setExpression 1
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0}

######################################################################
####
#
test Interpolator-2.3 {test 3rd order} {
    $order setExpression 3
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0}

######################################################################
####
#
test Interpolator-2.4 {test truncation} {
    $period setExpression 0
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0}

######################################################################
####
#
test Interpolator-3.1 {test using new values} {
    set dir [$e0 getDirector]
    set iteration [getParameter $dir iterations]
    $iteration setExpression 16

    $values setExpression {[7.0, 5.0, 3.0, 1.0]}
    $indexes setExpression {[0, 2, 4, 6]}
    $period setExpression 0
    $order setExpression 0
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {7.0 7.0 5.0 5.0 3.0 3.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0}

######################################################################
####
#
test Interpolator-3.2 {period=0, order=1} {
    $order setExpression 1
    [$e0 getManager] execute
    set result [enumToTokenValues [$rec getRecord 0]]
    deltaCompare $result {7.0 6.0 5.0 4.0 3.0 2.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0}
} {1}

######################################################################
####
#
test Interpolator-3.3 {period=0, order=3} {
    $order setExpression 3
    [$e0 getManager] execute
    set result [enumToTokenValues [$rec getRecord 0]]
    deltaCompare $result {7.0 7.0 5.0 4.0 3.0 2.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0}
} {1}

######################################################################
####
#
test Interpolator-3.4 {test period=8, order=0} {
    $period setExpression 8
    $order setExpression 0
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {7.0 7.0 5.0 5.0 3.0 3.0 1.0 1.0 7.0 7.0 5.0 5.0 3.0 3.0 1.0 1.0}

######################################################################
####
#
test Interpolator-3.5 {period=8, order=1} {
    $order setExpression 1
    [$e0 getManager] execute
    set result [enumToTokenValues [$rec getRecord 0]]
    deltaCompare $result {7.0 6.0 5.0 4.0 3.0 2.0 1.0 4.0 7.0 6.0 5.0 4.0 3.0 2.0 1.0 4.0}
} {1}


######################################################################
####
#
test Interpolator-3.6 {period=8, order=3} {
    $order setExpression 3
    [$e0 getManager] execute
    set result [enumToTokenValues [$rec getRecord 0]]
    deltaCompare $result {7.0 6.5 5.0 4.0 3.0 1.5 1.0 4.0 7.0 6.5 5.0 4.0 3.0 1.5 1.0 4.0}
} {1}
