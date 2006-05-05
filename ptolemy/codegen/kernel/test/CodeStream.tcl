# Test CodeStream
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2006 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

#####
test CodeStream-1.1 {Constructor that takes a CodeGeneratorHelper} {
    set model [sdfModel]

    set cgHelper [java::new ptolemy.codegen.kernel.CodeGeneratorHelper \
		      $model]
    set codeStream [java::new ptolemy.codegen.kernel.CodeStream \
			$cgHelper]
    $codeStream append "Test 1.1"
    list [$codeStream toString] \
	[$codeStream description]
} {{Test 1.1} {}}

#####
test CodeStream-1.2 {Constructor that takes a String} {

    set codeStream2 [java::new ptolemy.codegen.kernel.CodeStream \
			"foo"]
    $codeStream2 append "Test 1.2"
    $codeStream2 toString
} {Test 1.2}

#####
#test CodeStream-2.1 {append() two CodeStreams} {
#    $codeStream append $codeStream2
#    $codeStream toString
#} {Test 1.1Test 1.2}

#####
test CodeStream-3.1 {appendCodeBlock} {
    $codeStream appendCodeBlock "foo" [java::new java.util.ArrayList] true
    catch {$codeStream appendCodeBlock "foo" [java::new java.util.ArrayList] false} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot find code block: foo().
}}

#####
test CodeStream-3.2 {appendCodeBlock} {
    set fileTestBlock [java::new java.io.File testCodeBlock.c]
    set codeStream3_2 [java::new ptolemy.codegen.kernel.CodeStream \
			[[$fileTestBlock toURL] toString]]

    set args [java::new java.util.ArrayList]
    $args add [java::call Integer toString 3]
    $codeStream3_2 appendCodeBlock "initBlock" $args false
    list [$codeStream3_2 toString] \
	[$codeStream3_2 description]
} {{
    if ($ref(input) != 3) {
        $ref(output) = 3;
    }
} {initBlock($arg):

    if ($ref(input) != $arg) {
        $ref(output) = $arg;
    }

-------------------------------

}}


#####
test CodeStream-3.3 {appendCodeBlock: wrong number of args} {
    set fileTestBlock [java::new java.io.File testCodeBlock.c]
    set codeStream3_3 [java::new ptolemy.codegen.kernel.CodeStream \
			[[$fileTestBlock toURL] toString]]

    set args [java::new java.util.ArrayList]
    $args add [java::call Integer toString 3]
    $args add [java::call Integer toString 4]
    catch {$codeStream3_3 appendCodeBlock "initBlock" $args false} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot find code block: initBlock($, $).
}}

#####
test CodeStream-3.4 {appendCodeBlock: arg name does not start with $} {
    set codeStream3_4 [java::new ptolemy.codegen.kernel.CodeStream \
		"file:./testCodeBlockBadArg.c"]

    set args [java::new java.util.ArrayList]
    $args add [java::call Integer toString 3]
    catch {$codeStream3_4 appendCodeBlock "initBlock" $args false} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: initBlock($) in file:./testCodeBlockBadArg.c problems replacing "doesNotStartWithDollar" with "3"
Because:
Parameter "doesNotStartWithDollar" is not well-formed.
Parameter name for code block needs to starts with '$'}}

#####
test CodeStream-3.5 {appendCodeBlock initBlock} {
    set fileTestBlock [java::new java.io.File testCodeBlock.c]
    set codeStream3_5 [java::new ptolemy.codegen.kernel.CodeStream \
			[[$fileTestBlock toURL] toString]]

    catch {$codeStream3_5 appendCodeBlock "initBlock"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: initBlock -- is a code block that is appended by default.}}

#####
test CodeStream-4.0 {appendCodeBlock(nameExpression)} {
    set codeStream4_0 [java::new ptolemy.codegen.kernel.CodeStream \
		"file:./testCodeBlockNoArg.c"]

    $codeStream4_0 appendCodeBlock "myBlock"
    # Increase code coverage of if (_declarations) block	
    $codeStream4_0 appendCodeBlock "myOtherBlock"
    list [$codeStream4_0 toString] \
	[$codeStream4_0 description]

} {{
    // myBlock

    // myOtherBlock
} {myBlock:

    // myBlock

-------------------------------

myOtherBlock:

    // myOtherBlock

-------------------------------

}}



#####
test CodeStream-10.0 {main} {
    set args [java::new {String[]} 1 "file:./testCodeBlock.c"]
    jdkCapture {
	java::call ptolemy.codegen.kernel.CodeStream main $args
    } results
    list $results
} {{
----------Result-----------------------

initBlock($arg):

    if ($ref(input) != $arg) {
        $ref(output) = $arg;
    }

-------------------------------



----------Result-----------------------


    if ($ref(input) != 3) {
        $ref(output) = 3;
    }

}}
