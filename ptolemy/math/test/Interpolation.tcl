# Tests for the Interpolation Class
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

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
test Interpolation-1.1 {interpolate using default} {
    set interp [java::new ptolemy.math.Interpolation]
    list [$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5]
} {1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0}

######################################################################
####
#
test Interpolation-1.2 {test 1st order} {
    $interp setOrder 1
    list [$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5]
} {1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0}

######################################################################
####
#
test Interpolation-1.3 {test 3rd order} {
    $interp setOrder 3
    list [$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5]
} {1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0}

######################################################################
####
#
test Interpolation-1.4 {test truncation} {
    $interp setPeriod 0
    list [$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5]
} {0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0}

######################################################################
####
#
test Interpolation-2.1 {test using new values} {
    set values [java::new {double[]} 4 [list 7.0 5.0 3.0 1.0]]
    set indexes [java::new {int[]} 4 [list 0 2 4 6]]
    $interp setValues $values
    $interp setIndexes $indexes
    $interp setPeriod 0
    $interp setOrder 0

    set gv [$interp getValues]
    set gi [$interp getIndexes]

    list [$gv length] [$gi length] [$interp getPeriod] \
	[$interp getOrder]
} {4 4 0 0}


######################################################################
####
#
test Interpolation-2.2 {test period=0, order=0} {
    list [$interp interpolate -4] [$interp interpolate -3] \
	[$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5] \
	[$interp interpolate 6] [$interp interpolate 7] \
	[$interp interpolate 8] [$interp interpolate 9] \
	[$interp interpolate 10] [$interp interpolate 11]
} {0.0 0.0 0.0 0.0 7.0 7.0 5.0 5.0 3.0 3.0 1.0 0.0 0.0 0.0 0.0 0.0}

######################################################################
####
#
test Interpolation-2.3 {period=0, order=1} {
    $interp setOrder 1
    set result [list [$interp interpolate -4] [$interp interpolate -3] \
	[$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5] \
	[$interp interpolate 6] [$interp interpolate 7] \
	[$interp interpolate 8] [$interp interpolate 9] \
	[$interp interpolate 10] [$interp interpolate 11]]
    deltaCompare $result {0.0 0.0 0.0 0.0 7.0 6.0 5.0 4.0 3.0 2.0 1.0 0.0 0.0 0.0 0.0 0.0}
} {1}

######################################################################
####
#
test Interpolation-2.4 {period=0, order=3} {
    $interp setOrder 3
    set result [list [$interp interpolate -4] [$interp interpolate -3] \
	[$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5] \
	[$interp interpolate 6] [$interp interpolate 7] \
	[$interp interpolate 8] [$interp interpolate 9] \
	[$interp interpolate 10] [$interp interpolate 11]]
    deltaCompare $result {0.0 0.0 0.0 0.0 7.0 7.0 5.0 4.0 3.0 2.0 1.0 0.0 0.0 0.0 0.0 0.0}
} {1}

######################################################################
####
#
test Interpolation-2.5 {test period=8, order=0} {
    $interp setPeriod 8
    $interp setOrder 0
    list [$interp interpolate -4] [$interp interpolate -3] \
	[$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5] \
	[$interp interpolate 6] [$interp interpolate 7] \
	[$interp interpolate 8] [$interp interpolate 9] \
	[$interp interpolate 10] [$interp interpolate 11]
} {3.0 3.0 1.0 1.0 7.0 7.0 5.0 5.0 3.0 3.0 1.0 1.0 7.0 7.0 5.0 5.0}

######################################################################
####
#
test Interpolation-2.6 {period=8, order=1} {
    $interp setOrder 1
    set result [list [$interp interpolate -4] [$interp interpolate -3] \
	[$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5] \
	[$interp interpolate 6] [$interp interpolate 7] \
	[$interp interpolate 8] [$interp interpolate 9] \
	[$interp interpolate 10] [$interp interpolate 11]]
    deltaCompare $result {3.0 2.0 1.0 4.0 7.0 6.0 5.0 4.0 3.0 2.0 1.0 4.0 7.0 6.0 5.0 4.0}
} {1}

######################################################################
####
#
test Interpolation-2.7 {period=8, order=3} {
    $interp setOrder 3
    set result [list [$interp interpolate -4] [$interp interpolate -3] \
	[$interp interpolate -2] [$interp interpolate -1] \
	[$interp interpolate 0] [$interp interpolate 1] \
	[$interp interpolate 2] [$interp interpolate 3] \
	[$interp interpolate 4] [$interp interpolate 5] \
	[$interp interpolate 6] [$interp interpolate 7] \
	[$interp interpolate 8] [$interp interpolate 9] \
	[$interp interpolate 10] [$interp interpolate 11]]
    deltaCompare $result {3.0 1.5 1.0 4.0 7.0 6.5 5.0 4.0 3.0 1.5 1.0 4.0 7.0 6.5 5.0 4.0}
} {1}

