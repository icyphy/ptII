# Test Maximum.
#
# @Author: Edward A. Lee
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

######################################################################
####
#
test Maximum-1.1 {test constructor and clone} {
    set e0 [sdfModel 5]
    set baseobj [java::new ptolemy.actor.lib.Maximum $e0 baseobj]
    set maximum [java::cast ptolemy.actor.lib.Maximum [$baseobj clone]]
    $maximum setName maximum
    $maximum setContainer $e0
    $baseobj setContainer [java::null]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Maximum in an SDF model
#
test Maximum-2.1 {test maximum} {
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set values [getParameter $pulse values]
    set indexes [getParameter $pulse indexes]
    $values setExpression {[-2, -1, 0, 1, 2]}
    $indexes setExpression {[0, 1, 2, 3, 4]}
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set value [getParameter $const value]
    $value setExpression {0.0}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set input [java::field [java::cast ptolemy.actor.lib.Transformer \
            $maximum] input]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
       $input]
    set r2 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
       $input]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Transformer \
            $maximum] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.0 0.0 0.0 1.0 2.0}

######################################################################
#### Check types of above model
#
test Maximum-2.2 {check types} {
    set pulseOut [java::field [java::cast ptolemy.actor.lib.Source $pulse] \
	output]
    set constOut [java::field [java::cast ptolemy.actor.lib.Source $const] \
	output]
    set maximumIn [java::field [java::cast ptolemy.actor.lib.Transformer \
	$maximum] input]
    set maximumOut [java::field [java::cast ptolemy.actor.lib.Transformer \
	$maximum] output]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$maximumIn getType] toString] [[$maximumOut getType] toString] \
	[[$recIn getType] toString]
} {int double double double general}

######################################################################
#### Test integer input
#
test Maximum-2.3 {test integer} {
    $values setExpression {[-2, -1, 0, 1, 2]}
    $value setExpression {0}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 0 0 1 2}

######################################################################
#### Check types of above model
#
test Maximum-2.4 {check types} {
    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$maximumIn getType] toString] [[$maximumOut getType] toString] \
	[[$recIn getType] toString]
} {int int int int general}

######################################################################
#### Test long input
#
test Maximum-2.5 {test long} {
    $values setExpression {[-2l, -1, 0, 1, 2]}
    $value setExpression {0}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 0 0 1 2}

######################################################################
#### Check types of above model
#
test Maximum-2.6 {check types} {
    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$maximumIn getType] toString] [[$maximumOut getType] toString] \
	[[$recIn getType] toString]
} {long int long long general}

######################################################################
#### Test complex input
#
test Maximum-2.7 {test complex} {
    $values setExpression {[-2+i, -1-i, 0, 3+3i, 5+4i]}
    $value setExpression {4}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{4.0 + 0.0i} {4.0 + 0.0i} {4.0 + 0.0i} {3.0 + 3.0i} {5.0 + 4.0i}}

######################################################################
#### Check types of above model
#
test Maximum-2.8 {check types} {
    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$maximumIn getType] toString] [[$maximumOut getType] toString] \
	[[$recIn getType] toString]
} {complex int complex complex general}
