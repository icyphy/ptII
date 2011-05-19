# Test CodeGeneratorHelper
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2009 The Regents of the University of California.
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

#####
test CodeGeneratorHelper-1.1 {Constructor} {
    set model [sdfModel]

    set cgHelper [java::new ptolemy.codegen.kernel.CodeGeneratorHelper \
		      $model]
    set parseTree [$cgHelper getParseTreeCodeGenerator]
    list \
	[[$parseTree evaluateParseTree [java::null] [java::null]] toString]\
	[$parseTree generateFireCode]
} {present {/* ParseTreeCodeGenerator.generateFireCode() not implemented in codegen.kernel.CodeGenerator */}}

#####
test CodeGeneratorHelper-2.1 {VariableScope methods} {
    set testCodeGeneratorHelper \
	[java::new ptolemy.codegen.kernel.test.TestCodeGeneratorHelper \
	     $model]
    set variableScope [java::field $testCodeGeneratorHelper variableScope]
    set msg1 [$variableScope getType "foo"] 
    set msg2 [$variableScope getTypeTerm "bar"] 
    set msg3 [$variableScope identifierSet]
    list $msg1 $msg2 $msg3
} {java0x0 java0x0 java0x0}


#####
test CodeGeneratorHelper-3.1 {parseTreeCodeGenerator coverage } {
    # Uses 1.1 above
    # Do nothing, we just want good coverage
    [$cgHelper getParseTreeCodeGenerator] escapeForTargetLanguage "foobar"
} {foobar}

#####
test CodeGeneratorHelper-4.1 {generateVariableDeclaration} {
    # Uses 1.1 above
    # Do nothing, we just want good coverage
    list [$cgHelper generateVariableDeclaration]
} {{}}

#####
test CodeGeneratorHelper-5.1 {getSourceChannel} {
    # Uses 1.1 above
    # Add a ramp and a recorder
    set ramp [java::new ptolemy.actor.lib.Ramp $model ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $model rec]
    $model connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    set outputPort [$ramp getPort output]
    set inputPort [$rec getPort input]

	# Create the receivers explicitly.
	[java::cast ptolemy.actor.IOPort $outputPort] createReceivers
	[java::cast ptolemy.actor.IOPort $inputPort] createReceivers
    
    catch {$cgHelper getSourceChannel $inputPort 1} errMsg
    set channel [$cgHelper getSourceChannel $inputPort 0]
    
    list $errMsg [$channel toString]
} {{java.lang.ArrayIndexOutOfBoundsException: 1} output_0}


