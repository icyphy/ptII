# Test DiscreteRandomSource.
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Constructors and Clone
#
test DiscreteRandomSource-1.0 {test constructor and initial value} {
    set e0 [sdfModel 4]
    set randommaster [java::new ptolemy.actor.lib.DiscreteRandomSource $e0 random]
    [$randommaster getAttribute values] toString
} {ptolemy.data.expr.Parameter {.top.random.values} {0, 1}}

test DiscreteRandomSource-1.1 {test clone and initial value} {
    set random [java::cast ptolemy.actor.lib.DiscreteRandomSource \
		    [$randommaster clone [$e0 workspace]]]
    $randommaster setContainer [java::null]
    $random setContainer $e0
    [$random getAttribute values] toString
} {ptolemy.data.expr.Parameter {.top.random.values} {0, 1}}

######################################################################
#### Test DiscreteRandomSource in a SDF model
#
# FIXME: since the actor output is random, the test is commented out.
# test DiscreteRandomSource-2.1 {test with the default output value} {
#     set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
#     $e0 connect \
#             [java::field [java::cast ptolemy.actor.lib.Source $random] output] \
#             [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
#     set manager [$e0 getManager]
#     $manager addExecutionListener \
#             [java::new ptolemy.actor.StreamExecutionListener]
#     $manager execute
#     enumToTokenValues [$rec getRecord 0]
# } {1 0 1 0 1}

######################################################################
#### Test DiscreteRandomSource in a SDF model
#
test DiscreteRandomSource-2.2 {check type with default value} {
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set out [java::field [java::cast ptolemy.actor.lib.Source $random] output]
    set in [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    $e0 connect $out $in
    set manager [$e0 getManager]
    $manager addExecutionListener \
            [java::new ptolemy.actor.StreamExecutionListener]
    $manager execute
    list [[$out getType] toString] [[$in getType] toString]
} {int int}

######################################################################
#### Change type
#
test DiscreteRandomSource-2.3 {change type to double} {
    set v [getParameter $random values]
    $v setExpression {{0.5, 0.5}} 
    $manager execute
    list [[$out getType] toString] [[$in getType] toString]
} {double double}

######################################################################
#### Change type
#
test DiscreteRandomSource-2.4 {change type to string} {
    $v setExpression {{"foo", "bar"}} 
    $manager execute
    list [[$out getType] toString] [[$in getType] toString]
} {string string}

######################################################################
#### Change type
#
test DiscreteRandomSource-2.5 {change type to array} {
    $v setExpression {{{0}, {1}}} 
    $manager execute
    list [[$out getType] toString] [[$in getType] toString]
} {{{int}} {{int}}}

######################################################################
#### Change type
#
test DiscreteRandomSource-2.6 {change type to record} {
    $v setExpression {{{a = 0.7, b = 0.8}, {a = 1.7, b = 1.8}}} 
    $manager execute
    list [[$out getType] toString] [[$in getType] toString]
} {{{a = double, b = double}} {{a = double, b = double}}}

