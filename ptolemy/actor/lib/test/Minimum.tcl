# Test Minimum.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

######################################################################
####
#
test Minimum-1.1 {test constructor and clone} {
    set e0 [sdfModel 5]
    set baseobj [java::new ptolemy.actor.lib.Minimum $e0 baseobj]
    set minimum [java::cast ptolemy.actor.lib.Minimum \
		     [$baseobj clone [$e0 workspace]]]
    $minimum setName minimum
    $minimum setContainer $e0
    $baseobj setContainer [java::null]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Minimum in an SDF model
#
test Minimum-2.1 {test Minimum} {
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set values [getParameter $pulse values]
    set indexes [getParameter $pulse indexes]
    $values setExpression {{-2, -1, 0, 1, 2}}
    $indexes setExpression {{0, 1, 2, 3, 4}}
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set value [getParameter $const value]
    $value setExpression {0.0}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set input [java::field $minimum input]
    [java::cast ptolemy.actor.IORelation [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
       $input]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
       $input]] setWidth 1
    [java::cast ptolemy.actor.IORelation [$e0 connect \
       [java::field $minimum minimumValue] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]] setWidth 1
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-2.0 -1.0 0.0 0.0 0.0}

######################################################################
#### Check types of above model
#
test Minimum-2.2 {check types} {
    set pulseOut [java::field [java::cast ptolemy.actor.lib.Source $pulse] \
	output]
    set constOut [java::field [java::cast ptolemy.actor.lib.Source $const] \
	output]
    set minimumIn [java::field $minimum input]
    set minimumOut [java::field $minimum minimumValue]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$minimumIn getType] toString] [[$minimumOut getType] toString] \
	[[$recIn getType] toString]
# NOTE: first element would be double if bidirectional type inference were 
# enabled
} {int double double double double}

######################################################################
#### Test integer input
#
test Minimum-2.3 {test integer} {
    $values setExpression {{-2, -1, 0, 1, 2}}
    $value setExpression {0}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-2 -1 0 0 0}

######################################################################
#### Check types of above model
#
test Minimum-2.4 {check types} {
    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$minimumIn getType] toString] [[$minimumOut getType] toString] \
	[[$recIn getType] toString]
} {int int int int int}

######################################################################
#### Test long input
#
test Minimum-2.5 {test long} {
    $values setExpression {{-2l, -1l, 0l, 1l, 2l}}
    $value setExpression {0}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {-2L -1L 0L 0L 0L}

######################################################################
#### Check types of above model
#
test Minimum-2.6 {check types} {
    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$minimumIn getType] toString] [[$minimumOut getType] toString] \
	[[$recIn getType] toString]
# NOTE: second element would be long if bidirectional type inference were 
# enabled
} {long int long long long}

######################################################################
#### Test complex input
#
test Minimum-2.7 {test complex} {
    $values setExpression {{-2+i, -1-i, 0+0i, 3+3i, 5+4i}}
    $value setExpression {4}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{-2.0 + 1.0i} {-1.0 - 1.0i} {0.0 + 0.0i} {4.0 + 0.0i} {4.0 + 0.0i}}

######################################################################
#### Check types of above model
#
test Minimum-2.8 {check types} {
    list [[$pulseOut getType] toString] [[$constOut getType] toString] \
	[[$minimumIn getType] toString] [[$minimumOut getType] toString] \
	[[$recIn getType] toString]
# NOTE: second element would be complex if bidirectional type inference were 
# enabled
} {complex int complex complex complex}

