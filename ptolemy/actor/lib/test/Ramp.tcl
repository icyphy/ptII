# Test Ramp.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test Ramp-1.1 {test clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setToken [java::new ptolemy.data.DoubleToken 2.5]
    $step setExpression init

    set newObject [java::cast ptolemy.actor.lib.Ramp \
		       [$ramp clone [$e0 workspace]]]
    set newInit [getParameter $newObject init]
    set newStep [getParameter $newObject step]
    set initVal [[$newInit getToken] toString]
    set stepVal [[$newStep getToken] toString]

    list $initVal $stepVal
} {2.5 2.5}

test Ramp-1.2 {test clone} {
    $init setExpression 5.5
    set stepValue [[$step getToken] toString]
    set newStepValue [[$newStep getToken] toString]

    list $stepValue $newStepValue
} {5.5 2.5}

######################################################################
#### Test Ramp in an SDF model
#
test Ramp-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 1 2 3 4}

test Ramp-2.1 {test with strings} {
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {"a"}
    $step setExpression {"b"}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"a"} {"ab"} {"abb"} {"abbb"} {"abbbb"}}

test Ramp-2.2 {test with record} {
    # first record is {name = "a", value = 1, extra1 = 2}
    # Old, very labor intensive way to do this.
    #     set l1 [java::new {String[]} {3} {{name} {value} {extra1}}]
    # 
    #     set nt1 [java::new {ptolemy.data.StringToken String} a]
    #     set vt1 [java::new {ptolemy.data.IntToken int} 1]
    #     set et1 [java::new {ptolemy.data.IntToken int} 2]
    #     set v1 [java::new {ptolemy.data.Token[]} 3 [list $nt1 $vt1 $et1]]
    # 
    #     set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name = "b", value = 2.5}
    # Old, very labor intensive way to do this.
    # Actually, this would no longer work, since setToken on step
    # doesn't do the same thing as setExpression.
    #     set l2 [java::new {String[]} {2} {{name} {value}}]
    # 
    #     set nt2 [java::new {ptolemy.data.StringToken String} b]
    #     set vt2 [java::new {ptolemy.data.DoubleToken double} 2.5]
    #     set v2 [java::new {ptolemy.data.Token[]} 2 [list $nt2 $vt2]]
    # 
    #     set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    # set Ramp parameters
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "{name = \"a\", value = 1, extra1 = 2}"
    $step setExpression "{name = \"b\", value = 2.5}"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{name = "a", value = 1.0}} {{name = "ab", value = 3.5}} {{name = "abb", value = 6.0}} {{name = "abbb", value = 8.5}} {{name = "abbbb", value = 11.0}}}

test Ramp-2.3 {check types of the above model} {
    set constOut [java::field [java::cast ptolemy.actor.lib.Source $ramp] output]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    list [[$constOut getType] toString] [[$recIn getType] toString]
} {{{name = string, value = double}} {{name = string, value = double}}}


test Ramp-2.4 {test with record containing array} {
    # first record is {name = "a", value = 1, anArray = {1.5, 2.5}, extra1 = 2}
    # Old, very labor intensive way to do this.
    #     set l1 [java::new {String[]} {4} {{name} {value} {anArray} {extra1}}]
    # 
    #     set nt1 [java::new {ptolemy.data.StringToken String} a]
    #     set vt1 [java::new {ptolemy.data.IntToken int} 1]
    # 
    #     set val0 [java::new {ptolemy.data.DoubleToken double} 1.5]
    #     set val1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    #     set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    #     set valToken [java::new {ptolemy.data.ArrayToken} $valArray]
    # 
    #     set et1 [java::new {ptolemy.data.IntToken int} 2]
    #     set v1 [java::new {ptolemy.data.Token[]} 4 [list $nt1 $vt1 $valToken $et1]]
    # 
    #     set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]

    # second record is {name = "b", value = 2.5, anArray = {10, 20}}
    # Old, very labor intensive way to do this.
    #     set l2 [java::new {String[]} {3} {{name} {value} {anArray}}]
    # 
    #     set nt2 [java::new {ptolemy.data.StringToken String} b]
    #     set vt2 [java::new {ptolemy.data.DoubleToken double} 2.5]
    # 
    #     set val0_2 [java::new {ptolemy.data.IntToken int} 10]
    #     set val1_2 [java::new {ptolemy.data.IntToken int} 20]
    #     set valArray_2 [java::new {ptolemy.data.Token[]} 2 [list $val0_2 $val1_2]]
    #     set valToken_2 [java::new {ptolemy.data.ArrayToken} $valArray_2]
    # 
    # 
    #     set v2 [java::new {ptolemy.data.Token[]} 3 [list $nt2 $vt2 $valToken_2]]
    # 
    #     set r2 [java::new {ptolemy.data.RecordToken} $l2 $v2]

    # set Ramp parameters
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "{name = \"a\", value = 1, anArray = {1.5, 2.5}, extra1 = 2}"
    $step setExpression "{name = \"b\", value = 2.5, anArray = {10, 20}}"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{anArray = {1.5, 2.5}, name = "a", value = 1.0}} {{anArray = {11.5, 22.5}, name = "ab", value = 3.5}} {{anArray = {21.5, 42.5}, name = "abb", value = 6.0}} {{anArray = {31.5, 62.5}, name = "abbb", value = 8.5}} {{anArray = {41.5, 82.5}, name = "abbbb", value = 11.0}}}

test Ramp-2.5 {check types of the above model} {
    list [[$constOut getType] toString] [[$recIn getType] toString]
} {{{anArray = {double}, name = string, value = double}} {{anArray = {double}, name = string, value = double}}}

test Ramp-2.6 {test an array of record} {
    # init is {{name = "a", value = 1}, {name = "b", value = 2}}
    # Old, very labor intensive way to do this.
    #     set l1 [java::new {String[]} {2} {{name} {value}}]
    #     set nt1 [java::new {ptolemy.data.StringToken String} a]
    #     set vt1 [java::new {ptolemy.data.IntToken int} 1]
    #     set v1 [java::new {ptolemy.data.Token[]} 2 [list $nt1 $vt1]]
    #     set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]
    # 
    #     set nt2 [java::new {ptolemy.data.StringToken String} b]
    #     set vt2 [java::new {ptolemy.data.IntToken int} 2]
    #     set v2 [java::new {ptolemy.data.Token[]} 2 [list $nt2 $vt2]]
    #     set r2 [java::new {ptolemy.data.RecordToken} $l1 $v2]
    # 
    #     set valArray [java::new {ptolemy.data.Token[]} 2 [list $r1 $r2]]
    #     set initToken [java::new {ptolemy.data.ArrayToken} $valArray]

    # step is {{name = "c", value = 1.5}, {name = "d", value = 2.5}}
    #     set nt1 [java::new {ptolemy.data.StringToken String} c]
    #     set vt1 [java::new {ptolemy.data.DoubleToken double} 1.5]
    #     set v1 [java::new {ptolemy.data.Token[]} 2 [list $nt1 $vt1]]
    #     set r1 [java::new {ptolemy.data.RecordToken} $l1 $v1]
    # 
    #     set nt2 [java::new {ptolemy.data.StringToken String} d]
    #     set vt2 [java::new {ptolemy.data.DoubleToken double} 2.5]
    #     set v2 [java::new {ptolemy.data.Token[]} 2 [list $nt2 $vt2]]
    #     set r2 [java::new {ptolemy.data.RecordToken} $l1 $v2]
    # 
    #     set valArray [java::new {ptolemy.data.Token[]} 2 [list $r1 $r2]]
    #     set stepToken [java::new {ptolemy.data.ArrayToken} $valArray]

    # set Ramp parameters
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression "{{name = \"a\", value = 1}, {name = \"b\", value = 2}}"
    $step setExpression "{{name = \"c\", value = 1.5}, {name = \"d\", value = 2.5}}"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{{name = "a", value = 1.0}, {name = "b", value = 2.0}}} {{{name = "ac", value = 2.5}, {name = "bd", value = 4.5}}} {{{name = "acc", value = 4.0}, {name = "bdd", value = 7.0}}} {{{name = "accc", value = 5.5}, {name = "bddd", value = 9.5}}} {{{name = "acccc", value = 7.0}, {name = "bdddd", value = 12.0}}}}

test Ramp-2.7 {check types of the above model} {
    list [[$constOut getType] toString] [[$recIn getType] toString]
} {{{{name = string, value = double}}} {{{name = string, value = double}}}}


test Ramp-3.1 {Run a CT model which will detect errors in scheduling} {
    set e0 [ctModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.actor.sched.NotSchedulableException: ramp is a SequenceActor, which cannot be a source actor in the CT domain.}}


