# Test Const.
#
# @Author: Edward A. Lee
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
#### Constructors and Clone
#

test Const-1.0 {test constructor and initial value} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    [$const getAttribute value] toString
} {ptolemy.data.expr.Parameter {.top.const.value} 1}

test Const-1.1 {test clone and initial value} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set newObject [java::cast ptolemy.actor.lib.Const \
		       [$const clone [$e0 workspace]]]
    $newObject setName new
    [$newObject getAttribute value] toString
} {ptolemy.data.expr.Parameter {.new.value} 1}

test Const-1.2 {change the original value and verify that the new remains} {
    set orgvalue [java::cast ptolemy.data.expr.Parameter \
            [$const getAttribute value]]
    $orgvalue setToken [java::new {ptolemy.data.DoubleToken double} 3.1]

    list [[$newObject getAttribute value] toString]  \
            [[$const getAttribute value] toString]
} {{ptolemy.data.expr.Parameter {.new.value} 1} {ptolemy.data.expr.Parameter {.top.const.value} 3.1}}

test Const-1.3 {Test clone of Source base class} {
    expr 0 != \ [string compare [$const getPort output] \
            [$newObject getPort output]]
} {1}

######################################################################
#### Test Const in an SDF model
#
test Const-2.1 {test with the default output value} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1}

test Const-2.1 {change output value and type and rerun} {
    set p [getParameter $const value]
    $p setExpression 3.0
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {3.0}

test Const-2.3 {change type to RecordToken} {
    # RecordToken is {name = "foo", value = 5}
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    # set new token
    $p setToken $r

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{name = "foo", value = 5}}}

test Const-2.4 {check types of the above model} {
    set constOut [java::field [java::cast ptolemy.actor.lib.Source $const] output]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    list [[$constOut getType] toString] [[$recIn getType] toString]
} {{{name = string, value = int}} {{name = string, value = int}}}

test Const-2.5 {test RecordToken containing ArrayToken} {
    # RecordToken is {name = "foo", value = 5, anArray = [1.5, 2.5]}
    set l [java::new {String[]} {3} {{name} {value} {anArray}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]

    set val0 [java::new {ptolemy.data.DoubleToken double} 1.5]
    set val1 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set v [java::new {ptolemy.data.Token[]} 3 [list $nt $vt $valToken]]

    set r [java::new {ptolemy.data.RecordToken} $l $v]

    # set new token
    $p setToken $r

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{anArray = {1.5, 2.5}, name = "foo", value = 5}}}

test Const-2.6 {check types of the above model} {
    list [[$constOut getType] toString] [[$recIn getType] toString]
} {{{anArray = {double}, name = string, value = int}} {{anArray = {double}, name = string, value = int}}}

test Const-2.7 {test an array of record} {
    # first record is {name = "foo", value = 5}
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} foo]
    set vt [java::new {ptolemy.data.IntToken int} 5]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r1 [java::new {ptolemy.data.RecordToken} $l $v]

    # second record is {name = "bar", value = 3}
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::new {ptolemy.data.StringToken String} bar]
    set vt [java::new {ptolemy.data.IntToken int} 3]
    set v [java::new {ptolemy.data.Token[]} 2 [list $nt $vt]]

    set r2 [java::new {ptolemy.data.RecordToken} $l $v]

    # construct the array token
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $r1 $r2]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    # set new token
    $p setToken $valToken

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{{name = "foo", value = 5}, {name = "bar", value = 3}}}}

test Const-2.8 {check types of the above model} {
    list [[$constOut getType] toString] [[$recIn getType] toString]
} {{{{name = string, value = int}}} {{{name = string, value = int}}}}


test Const-3.0 {check out ReadFile} {
    # Create a file Const.txt that contains the string "foo"
    set fd [open Const.txt w]
    puts $fd {"foo"}
    flush $fd
    close $fd
    
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set p [getParameter $const value]
    $p setExpression {eval(readFile("Const.txt"))}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"foo"}}

test Const-3.1 {check out ReadFile with a multiline file} {
    # Create a file Const.txt that contains the three lines
    set fd [open Const.txt w]
    puts $fd {"}
    puts $fd {bar}
    puts $fd {"}

    flush $fd
    close $fd
    
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set p [getParameter $const value]
    $p setExpression {eval(readFile("Const.txt"))}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    # This is sort of lame, the \n chars get converted to spaces in
    # PtParser.generateParseTree()
    enumToTokenValues [$rec getRecord 0]
} {{"
bar
"}}

# FIXME: Need a mechanism to test a change in parameter during a run.

test Const-4.1 {Check out Strings with double quotes in them} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set p [getParameter $const value]
    set nt [java::new ptolemy.data.StringToken \
	    "This has a double quote \" in it and a backslashed double quote \\\" in it"]
    $p setToken $nt
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"This has a double quote \" in it and a backslashed double quote \" in it"}}
