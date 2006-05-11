# Test Expression.
#
# @Author: Yuhong Xiong, Edward A. Lee
#
# @Version: $Id$
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
test Expression-1.1 {test clone} {
    set e0 [sdfModel 3]
    set exprmaster [java::new ptolemy.actor.lib.Expression $e0 expr]
    set expr [_testClone $exprmaster [$e0 workspace]]
    $exprmaster setContainer [java::null]
    $expr setContainer $e0
    $expr description 1
} {ptolemy.actor.lib.Expression}

test Expression-2.1 {run with default empty expression} {
    set in1 [java::new ptolemy.actor.TypedIOPort $expr in1 true false]
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1]
    $e0 connect \
            [java::field $expr output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    catch {$m execute} msg
    string range $msg 0 228

} {ptolemy.kernel.util.IllegalActionException:   in .top.<Unnamed Object>
Because:
Type resolution failed because of an error during type inference
  in .top
Because:
An error occurred during expression type inference
  in .top.expr}

test Expression-3.1 {run with a simple expression} {
    set expression [java::field $expr expression]
    $expression setExpression "iteration + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {6 7 8}

test Expression-3.2 {run with a simple expression} {
    set expression [java::field $expr expression]
    # In SDF, time is 0.0
    $expression setExpression "time + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {5.0 5.0 5.0}

test Expression-3.3 {run with a simple expression "time" in a DE model} {
    set e3 [deModel 5.0]
    set expression3 [java::new ptolemy.actor.lib.Expression $e3 expression3]
    set rec3 [java::new ptolemy.actor.lib.Recorder $e3 rec3]

    # This is DE, so we need a clock as a trigger
    set clock3 [java::new ptolemy.actor.lib.Clock $e3 clock3]   
    set in3 [java::new ptolemy.actor.TypedIOPort $expression3 in1 true false]
    set r3 [$e3 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock3] output] \
            $in3]
    $e3 connect \
            [java::field $expression3 output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec3] input]
    set expression3_3 [java::field $expression3 expression]
    $expression3_3 setExpression "time"
    set manager3 [$e3 getManager]
    $manager3 addExecutionListener \
            [java::new ptolemy.actor.StreamExecutionListener]
    $manager3 execute
    enumToTokenValues [$rec3 getRecord 0]
} {0.0 1.0 2.0 3.0 4.0 5.0}

# FIXME: The following test fails because of limitations in the
# type system.  When types can propagate through expressions.
# test Expression-4.1 {run with a simple expression} {
#     $expression setExpression "time + 5"
#     $m execute
#     enumToTokenValues [$rec getRecord 0]
# } {6 7 8}

test Expression-5.1 {run with a simple expression} {
    set rampinit [java::field $ramp1 init]
    $rampinit setExpression "0.0"
    $expression setExpression "time + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {5.0 5.0 5.0}

test Expression-6.1 {run with a simple expression} {
    set rampinit [java::field $ramp1 init]
    $expression setExpression "in1 + 5"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {5.0 6.0 7.0}

test Expression-7.1 {run with two inputs} {
    set ramp2 [java::new ptolemy.actor.lib.Ramp $e0 ramp2]   
    set in2 [java::new ptolemy.actor.TypedIOPort $expr in2 true false]
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
            $in2]
    $expression setExpression "in1 + in2"
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {0.0 2.0 4.0}

test Expression-8.1 {port named "time" is verboten} {
    set e8 [sdfModel 3]
    set ramp8 [java::new ptolemy.actor.lib.Ramp $e8 ramp8]   
    set expr8 [java::new ptolemy.actor.lib.Expression $e8 expr8]
    set time [java::new ptolemy.actor.TypedIOPort $expr8 time true false]
    set r1 [$e8 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp8] output] \
            $time]
    set expression [java::field $expr8 expression]
    $expression setExpression "time + time"
    set m8 [$e8 getManager]
    catch {$m8 execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: This actor has a port named "time", which will not be read, instead the variable "time" will be read.
  in .top.expr8}}


test Expression-8.2 {port named "iteration" is verboten} {
    set e8 [sdfModel 3]
    set ramp8 [java::new ptolemy.actor.lib.Ramp $e8 ramp8]   
    set expr8 [java::new ptolemy.actor.lib.Expression $e8 expr8]
    set iteration [java::new ptolemy.actor.TypedIOPort $expr8 iteration true false]
    set r1 [$e8 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp8] output] \
            $iteration]
    set expression [java::field $expr8 expression]
    $expression setExpression "iteration + iteration"
    set m8 [$e8 getManager]
    catch {$m8 execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: This actor has a port named "iteration", which will not be read, instead the variable "iteration" will be read.
  in .top.expr8}}



