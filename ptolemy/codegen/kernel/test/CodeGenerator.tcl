# Test CodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2011 The Regents of the University of California.
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
test CodeGenerator-1.1 {Instantiate a CodeGenerator, call a few methods} {
    set model [sdfModel]
    set codeGenerator \
	    [java::new ptolemy.codegen.kernel.CodeGenerator \
	    $model "myCodeGenerator"]

    # Call setCodeGenerator for complete code coverage, even though it
    # does not do anything.
    $codeGenerator setCodeGenerator $codeGenerator
    $codeGenerator addInclude "-I/usr/local/lib"
    $codeGenerator addLibrary "-lm"

    list \
	[$codeGenerator toString] \
	[$codeGenerator comment {This is a comment}] \
} {{ptolemy.codegen.kernel.CodeGenerator {.top.myCodeGenerator}} {/* This is a comment */
}}

#####
test CodeGenerator-2.1 {generateMainEntryCode, generateMainExitCode} {
    list [$codeGenerator generateMainEntryCode] \
	[$codeGenerator generateMainExitCode] 
} {{/* main entry code */
} {/* main exit code */
}}

#####
test CodeGenerator-3.1 {getComponent()} {
    [$codeGenerator getComponent] toString
} {ptolemy.actor.TypedCompositeActor {.top}}

#####
test CodeGenerator-4.1 {setExecuteCommands, getExecuteCommands} {
    # Trivial test to increase code coverage.
    set executeCommands [java::new ptolemy.util.StreamExec]
    $codeGenerator setExecuteCommands $executeCommands
    set executeCommands2 [$codeGenerator getExecuteCommands]
    list [$executeCommands equals $executeCommands2]
} {1}

#####
test CodeGenerator-5.1 {getCodeFileName} {
    # Trivial test to increase code coverage.
    $codeGenerator getCodeFileName
} {}

#####
test CodeGenerator-6.1 {generateMainEntryCode, generateMainExitCode} {
    list [$codeGenerator generateMainEntryCode] \
	[$codeGenerator generateMainExitCode] 
} {{/* main entry code */
} {/* main exit code */
}}

#####
test CodeGenerator-7.1 {generateInitializeEntryCode, generateInitializeExitCode} {
    list [$codeGenerator generateInitializeEntryCode] \
	[$codeGenerator generateInitializeExitCode] \
	[$codeGenerator generateInitializeProcedureName] 
} {{/* initialization entry code */
} {/* initialization exit code */
} {}}

#####
test CodeGenerator-8.1 {generatePostfire* methods} {
    list [$codeGenerator generatePostfireEntryCode] \
	[$codeGenerator generatePostfireExitCode] \
	[$codeGenerator generatePostfireProcedureName] 
} {{/* postfire entry code */
} {/* postfire exit code */
} {}}

#####
test CodeGenerator-9.1 {generateTypeConvertCode} {
    list [$codeGenerator generateTypeConvertCode]
} {{}}

#####
test CodeGenerator-10.1 {generateWrapup* methods} {
    list [$codeGenerator generateWrapupEntryCode] \
	[$codeGenerator generateWrapupExitCode] \
	[$codeGenerator generateWrapupProcedureName] 
} {{/* wrapup entry code */
} {/* wrapup exit code */
} {}}

#####
test CodeGenerator-10.1 {generateWrapup* methods} {
    list [$codeGenerator generateWrapupEntryCode] \
	[$codeGenerator generateWrapupExitCode] \
	[$codeGenerator generateWrapupProcedureName] 
} {{/* wrapup entry code */
} {/* wrapup exit code */
} {}}

#####
test CodeGenerator-11.1 {getModifiedVariables methods} {
    list [java::isnull [$codeGenerator getModifiedVariables]]
} {0}

#####

# generateCode(StringBuffer) is also tested in
# $PTII/ptolemy/codegen/c/domains/sdf/kernel/test/SDFDirector.tcl
# because the test relies on SDFDirector being built. 

test CodeGenerator-12.1 {generateCode using actor/TypedCompositeActor.java} {

    ###################################################################
    # NOTE: DO NOT REMOVE THIS TEST
    # If you remove this test, then the corresponding method WILL NOT
    # be covered in the nightly build.
    # Yes, it is annoying to update the output because this checks
    # the output of the codegenerator against known good results.
    # However, if this test is not here, then the method in the base
    # class is not ever run, which is a no-no
    # An alternative design would be to make this class abstract,
    # which would make it slightly more difficult to test.	
    # -cxh 11/07
    ###################################################################

    # Set the generatorPackage parameter so we look in
    # $PTII/ptolemy/codegen/kernel/test/actor for TypedCompositeActor.java
    set generatorPackageParameter [java::cast  ptolemy.data.expr.StringParameter [$codeGenerator getAttribute generatorPackage]]
    $generatorPackageParameter setExpression ptolemy.codegen.kernel.test

    set code [java::new StringBuffer]
    $codeGenerator generateCode $code
    list [$code toString]
} {{/* Generated by Ptolemy II (http://ptolemy.eecs.berkeley.edu)

Copyright (c) 2005-2011 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
 */
/* end shared code */
/* end preinitialize code */
/* before appending fireFunctionCode */

void top() {
}
/* after appending fireFunctionCode */
/* initialization entry code */
/* initialization exit code */
/* wrapup entry code */
/* wrapup exit code */
/* main entry code */
/* The firing of the director. */
/* The postfire of the director. */
/* closing exit code */
/* main exit code */
}}

# main(String[]) is tested in
# $PTII/ptolemy/codegen/c/actor/lib/test/CodeGenerator2.tcl
# because the test relies on SDFDirector being built and actors being present.
test CodeGenerator-13.1 {main -help} {

    set previousDoNotExit \
	[java::call ptolemy.util.StringUtilities getProperty \
	     ptolemy.ptII.doNotExit]

    set previousExitAfterWrapup \
	[java::call ptolemy.util.StringUtilities getProperty \
	     ptolemy.ptII.exitAfterWrapup]

    set args [java::new {String[]} 1  {-help}]
    java::call System clearProperty ptolemy.ptII.doNotExit
    java::call System setProperty ptolemy.ptII.exitAfterWrapup false
    jdkCapture {
	catch {
	    java::call ptolemy.codegen.kernel.CodeGenerator main $args
	} errMsg
    } result1
    set args [java::new {String[]} 1  {-version}]
    jdkCapture {
	catch {
	    java::call ptolemy.codegen.kernel.CodeGenerator main $args
	} errMsg2
    } result2

    java::call System setProperty ptolemy.ptII.exitAfterWrapup \
	$previousExitAfterWrapup
    java::call System setProperty ptolemy.ptII.doNotExit $previousDoNotExit

    list $errMsg $result1 $errMsg2 [string range $result2 0 6]
} {{java.lang.Exception: Failed to parse "-help"} {Usage: ptcg [ options ] [file ...]

Options that take values:
 -allowDynamicMultiportReferences         true|false (default: false)
 -codeDirectory <directory in which to put code (default: $HOME/codegen. Other values: $CWD, $HOME, $PTII, $TMPDIR)>
 -compile            true|false (default: true)
 -compileTarget      <target to be run, defaults to empty string>
 -generateComment    true|false (default: true)
 -generatorPackage   <Java package of code generator, defaults to ptolemy.codegen.c>
 -inline             true|false (default: false)
 -measureTime        true|false (default: false)
 -overwriteFiles     true|false (default: true)
 -padBuffers         true|false (default: true)
 -run                true|false (default: true)
 -sourceLineBinding  true|false (default: false)
 -target             <target name, defaults to false>
 -<parameter name>   <parameter value>

Boolean flags:
 -help
 -version

} {java.lang.Exception: Failed to parse "-version"} Version}
