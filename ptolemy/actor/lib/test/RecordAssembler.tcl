# Test RecordAssembler.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test RecordAssembler-1.1 {test clone} {
    set e0 [sdfModel 3]
    set recAsmMaster [java::new ptolemy.actor.lib.RecordAssembler $e0 assembler]
    set assembler [_testClone $recAsmMaster [$e0 workspace]]
    $recAsmMaster setContainer [java::null]
    $assembler setContainer $e0
    $assembler description 1
} {ptolemy.actor.lib.RecordAssembler}

test RecordAssembler-2.1 {run with one input port, test prefire} {
    set fromRamp [java::new ptolemy.actor.TypedIOPort $assembler fromRamp \
                                                                 true false]
    set output [java::field $assembler output]

    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]   
    set rampOut [java::field [java::cast ptolemy.actor.lib.Source $ramp] output]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set recIn [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    set r1 [$e0 connect $rampOut $fromRamp]
    $e0 connect $output $recIn

    set m [$e0 getManager]
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {{{fromRamp = 0}} {{fromRamp = 1}} {{fromRamp = 2}}}

test RecordAssembler-2.3 {check types} {
    list [[$rampOut getType] toString] [[$fromRamp getType] toString] \
         [[$output getType] toString] [[$recIn getType] toString]
} {int int {{fromRamp = int}} {{fromRamp = int}}}

test RecordAssembler-2.4 {run with two input port} {
    set fromConst [java::new ptolemy.actor.TypedIOPort $assembler fromConst \
                                                               true false]
    set const [java::new ptolemy.actor.lib.Const $e0 const]   
    set constOut [java::field [java::cast ptolemy.actor.lib.Source $const] \
     								output]

    $e0 connect $constOut $fromConst

    set m [$e0 getManager]
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {{{fromConst = 1, fromRamp = 0}} {{fromConst = 1, fromRamp = 1}} {{fromConst = 1, fromRamp = 2}}}

test RecordAssembler-2.5 {check types} {
    list [[$rampOut getType] toString] [[$constOut getType] toString] \
    	[[$fromRamp getType] toString] [[$fromConst getType] toString] \
	[[$output getType] toString] [[$recIn getType] toString]
} {int int int int {{fromConst = int, fromRamp = int}} {{fromConst = int, fromRamp = int}}}

