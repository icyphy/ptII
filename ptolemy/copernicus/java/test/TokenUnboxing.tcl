# Tests for deep codegen
#
# @Author: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2004 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkClassPathSeparator] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

if {[info procs sootCodeGeneration] == "" } then { 
    source [file join $PTII util testsuite codegen.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#

proc _doTest {className} {
    global PTII
    java::call soot.G reset
    set args [java::new {java.lang.String[]} 3 "-f dava $className"]
    [java::call soot.Main v] run $args
    set f [open sootOutput/dava/src/$className.java]
    read $f
}

proc _doSpecializedTypesTest {className} {
    global PTII
    java::call soot.G reset
    java::call ptolemy.copernicus.kernel.PtolemyUtilities loadSootReferences
    set theClass [[java::call soot.Scene v] loadClassAndSupport $className]
    $theClass setApplicationClass
    set analysis [java::new ptolemy.copernicus.java.TypeSpecializerAnalysis $theClass [java::new java.util.HashSet]]
    lsort [objectsToStrings [iterToObjects [$analysis getSolverVariables]]]
}

proc _doUnboxingTest {className} {
    global PTII
    java::call soot.G reset
    java::call ptolemy.copernicus.kernel.PtolemyUtilities loadSootReferences
    set pack [[java::call soot.PackManager v] getPack wjtp]
    set main [java::new ptolemy.copernicus.java.test.TestUnboxingMain]
    $main setOutputDirectory [file join testOutput $className]
    $main addTransforms
    set theClass [[java::call soot.Scene v] loadClassAndSupport $className]
    $theClass setApplicationClass
    set args [java::new {java.lang.String[]} 0]
    $main generateCode $args
    set f [open [file join testOutput $className $className.jimple]]
    read $f
}

proc _doExecuteTest {className} {
    global PTII
    set outputDir [file join [pwd] testOutput $className]
    puts "$PTII;$outputDir"
    exec java -classpath "$PTII;$outputDir" $className
}


# First, do an SDF test just to be sure things are working
test TokenUnboxing-1.1 {} {
    _doSpecializedTypesTest Unboxing1
} {}
 
test TokenUnboxing-1.2 {} {
    _doUnboxingTest Unboxing1
} {}

test TokenUnboxing-1.3 {} {
    _doExecuteTest Unboxing1
} {token = 1}

test TokenUnboxing-1b.1 {} {
    _doSpecializedTypesTest Unboxing1b
} {\{ArrayElementType(\{int\}),\ int) {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing1b: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = r1}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}}}

test TokenUnboxing-1b.2 {} {
    _doUnboxingTest Unboxing1b
} {}

test TokenUnboxing-1b.3 {} {
    _doExecuteTest Unboxing1b
} {token = {1}}

test TokenUnboxing-1c.1 {} {
    _doSpecializedTypesTest Unboxing1c
} {\{ArrayElementType(\{int\}),\ int) \{ArrayElementType(\{\{int\}\}),\ \{int\}) {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = int, depth = 1, associated object = r3}} {{VariableTerm: value = {int}, depth = 2, associated object = $r7}} {{VariableTerm: value = {int}, depth = 2, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = r4}} {{VariableTerm: value = {int}, depth = 2, associated object = r5}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r7.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r3)}} {{VariableTerm: value = {{int}}, depth = 3, associated object = $r8}} {{VariableTerm: value = {{int}}, depth = 3, associated object = r6}} {{VariableTerm: value = {{int}}, depth = 3, associated object = specialinvoke $r8.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r5)}}}

test TokenUnboxing-1c.2 {} {
    _doUnboxingTest Unboxing1c
} {}

test TokenUnboxing-1c.3 {} {
    _doExecuteTest Unboxing1c
} {token = {{1}}}


test TokenUnboxing-1d.1 {} {
    _doSpecializedTypesTest Unboxing1d
} {\{ArrayElementType(\{int\}),\ int) \{ArrayElementType(\{\{int\}\}),\ \{int\}) {{VariableTerm: value = int, depth = 1, associated object = $r1}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing1c: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r10}} {{VariableTerm: value = {int}, depth = 2, associated object = $r4}} {{VariableTerm: value = {int}, depth = 2, associated object = $r6}} {{VariableTerm: value = {int}, depth = 2, associated object = $r7}} {{VariableTerm: value = {int}, depth = 2, associated object = $r8}} {{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing1c: ptolemy.data.ArrayToken intArrayToken>}} {{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing1c: ptolemy.data.Token[] tokens2>}} {{VariableTerm: value = {int}, depth = 2, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r4.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r5)}} {{VariableTerm: value = {{int}}, depth = 3, associated object = $r14}} {{VariableTerm: value = {{int}}, depth = 3, associated object = $r9}} {{VariableTerm: value = {{int}}, depth = 3, associated object = <Unboxing1c: ptolemy.data.ArrayToken intArrayArrayToken>}} {{VariableTerm: value = {{int}}, depth = 3, associated object = specialinvoke $r9.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r10)}}}

test TokenUnboxing-1d.2 {} {
    _doUnboxingTest Unboxing1d
} {}

test TokenUnboxing-1d.3 {} {
    _doExecuteTest Unboxing1d
} {token = {{1}}}

test TokenUnboxing-2.1 {} {
    _doSpecializedTypesTest Unboxing2
} {{{VariableTerm: value = general, depth = 1, associated object = $r9}} {{VariableTerm: value = general, depth = 1, associated object = <Unboxing2: ptolemy.data.Token token>}}}

test TokenUnboxing-2.2 {} {
    _doUnboxingTest Unboxing2
} {}

test TokenUnboxing-2.3 {} {
    _doExecuteTest Unboxing2
} {token = 1}

test TokenUnboxing-3.1 {} {
    _doSpecializedTypesTest Unboxing3
} {}

test TokenUnboxing-3.2 {} {
    _doUnboxingTest Unboxing3
} {}

test TokenUnboxing-3.3 {} {
    _doExecuteTest Unboxing3
} {token = 3.0}

test TokenUnboxing-4.1 {} {
    _doSpecializedTypesTest Unboxing4
} {\{ArrayElementType(\{int\}),\ int) {{VariableTerm: value = general, depth = -2147483648, associated object = $r15}} {{VariableTerm: value = general, depth = -2147483648, associated object = <Unboxing4: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing4: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r11}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = $r7}} {{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing4: ptolemy.data.ArrayToken arraytoken>}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}}}

test TokenUnboxing-4.2 {} {
    _doUnboxingTest Unboxing4
} {}

test TokenUnboxing-4.3 {} {
    _doExecuteTest Unboxing4
} {token = 1}

test TokenUnboxing-5.1 {} {
    _doSpecializedTypesTest Unboxing5
} {\{ArrayElementType(\{int\}),\ int) {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing5: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = r1}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}}}

test TokenUnboxing-5.2 {} {
    _doUnboxingTest Unboxing5
} {}

test TokenUnboxing-5.3 {} {
    _doExecuteTest Unboxing5
} {token = {1}}

test TokenUnboxing-6.1 {} {
    _doSpecializedTypesTest Unboxing6
} {{{VariableTerm: value = int, depth = 1, associated object = $r11}} {{VariableTerm: value = int, depth = 1, associated object = $r1}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing6: ptolemy.data.Token castToken>}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing6: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing6: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

test TokenUnboxing-6.2 {} {
    _doUnboxingTest Unboxing6
} {}

test TokenUnboxing-6.3 {} {
    _doExecuteTest Unboxing6
} {token = 1}

test TokenUnboxing-7.1 {} {
    _doSpecializedTypesTest Unboxing7
} {{{VariableTerm: value = int, depth = 1, associated object = $r13}} {{VariableTerm: value = int, depth = 1, associated object = $r1}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = $r7}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing7: ptolemy.data.Token castToken>}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing7: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing7: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

test TokenUnboxing-7.2 {} {
    _doUnboxingTest Unboxing7
} {}

test TokenUnboxing-7.3 {} {
    _doExecuteTest Unboxing7
} {token = 1}

test TokenUnboxing-8.1 {} {
    _doSpecializedTypesTest Unboxing8
} {{{VariableTerm: value = int, depth = 1, associated object = $r10}} {{VariableTerm: value = int, depth = 1, associated object = $r1}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing8: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing8: ptolemy.data.Token[] tokens2>}} {{VariableTerm: value = int, depth = 1, associated object = <Unboxing8: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

test TokenUnboxing-8.2 {} {
    _doUnboxingTest Unboxing8
} {}

test TokenUnboxing-8.3 {} {
    _doExecuteTest Unboxing8
} {token = 1}


test TokenUnboxing-9.1 {} {
    _doSpecializedTypesTest Unboxing9
} {}

test TokenUnboxing-9.2 {} {
    _doUnboxingTest Unboxing9
} {}

test TokenUnboxing-9.3 {} {
    _doExecuteTest Unboxing9
} {token = 0.0}


test TokenUnboxing-10.1 {} {
    _doSpecializedTypesTest Unboxing10
} {}

test TokenUnboxing-10.2 {} {
    _doUnboxingTest Unboxing10
} {}

test TokenUnboxing-10.3 {} {
    _doExecuteTest Unboxing10
} {token = 0.0}


test TokenUnboxing-11.1 {} {
    _doSpecializedTypesTest Unboxing11
} {}

test TokenUnboxing-11.2 {} {
    _doUnboxingTest Unboxing11
} {}

test TokenUnboxing-11.3 {} {
    _doExecuteTest Unboxing11
} {token = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}}
