# Tests for deep codegen
#
# @Author: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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
    set SEP [jdkClassPathSeparator]
    _stripEmpties [lsort [split [exec java -classpath "$PTII$SEP$PTII/lib/sootclasses.jar$SEP$PTII/lib/jasminclasses.jar" ptolemy.copernicus.java.test.TestSpecializeTypesMain "$className"] "\r\n"]]
} 
proc _doUnboxingTest {className} {
    global PTII
    set SEP [jdkClassPathSeparator]
    set outputDir [file join [pwd] testOutput]
    exec java -classpath "$PTII$SEP$PTII/lib/sootclasses.jar$SEP$PTII/lib/jasminclasses.jar" ptolemy.copernicus.java.test.TestUnboxingMain "$outputDir" "$className"
    set f [open [file join testOutput $className.jimple]]
    read $f
    close $f
}

proc _doExecuteTest {className} {
    global PTII
    set SEP [jdkClassPathSeparator]
    set outputDir [file join [pwd] testOutput]
    _stripEmpties [split [exec java -classpath "$PTII$SEP$outputDir" $className] "\r\n"]
}

proc _stripEmpties {x} {
    set i [lsearch $x ""]
    while {$i != -1} {
	set x [lreplace $x $i $i]
	set i [lsearch $x ""]
    }

    return $x
}

#First, do an SDF test just to be sure things are working
test TokenUnboxing-1.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing1
} {}
 
test TokenUnboxing-1.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing1
} {}

test TokenUnboxing-1.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing1
} {{token = 1}}

test TokenUnboxing-1b.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing1b
} {{(ArrayElementType({int}), int)} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing1b: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = r2}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}}}

test TokenUnboxing-1b.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing1b
} {}

test TokenUnboxing-1b.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing1b
} {{token = {1}}}

test TokenUnboxing-1c.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing1c
} {{(ArrayElementType({int}), int)} {(ArrayElementType({{int}}), {int})} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = int, depth = 1, associated object = r3}} {{VariableTerm: value = {int}, depth = 2, associated object = $r7}} {{VariableTerm: value = {int}, depth = 2, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = r4}} {{VariableTerm: value = {int}, depth = 2, associated object = r5}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r7.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r3)}} {{VariableTerm: value = {{int}}, depth = 3, associated object = $r8}} {{VariableTerm: value = {{int}}, depth = 3, associated object = r6}} {{VariableTerm: value = {{int}}, depth = 3, associated object = specialinvoke $r8.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r5)}}}

test TokenUnboxing-1c.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing1c
} {}

test TokenUnboxing-1c.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing1c
} {{token = {{1}}}}


test TokenUnboxing-1d.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing1d
} {{(ArrayElementType({int}), int)} {(ArrayElementType({{int}}), {int})} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing1d: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r10}} {{VariableTerm: value = {int}, depth = 2, associated object = $r4}} {{VariableTerm: value = {int}, depth = 2, associated object = $r6}} {{VariableTerm: value = {int}, depth = 2, associated object = $r7}} {{VariableTerm: value = {int}, depth = 2, associated object = $r8}} {{VariableTerm: value = {int}, depth = 2, associated object = <ptolemy.copernicus.java.test.Unboxing1d: ptolemy.data.ArrayToken intArrayToken>}} {{VariableTerm: value = {int}, depth = 2, associated object = <ptolemy.copernicus.java.test.Unboxing1d: ptolemy.data.Token[] tokens2>}} {{VariableTerm: value = {int}, depth = 2, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r4.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r5)}} {{VariableTerm: value = {{int}}, depth = 3, associated object = $r14}} {{VariableTerm: value = {{int}}, depth = 3, associated object = $r9}} {{VariableTerm: value = {{int}}, depth = 3, associated object = <ptolemy.copernicus.java.test.Unboxing1d: ptolemy.data.ArrayToken intArrayArrayToken>}} {{VariableTerm: value = {{int}}, depth = 3, associated object = specialinvoke $r9.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r10)}}}

test TokenUnboxing-1d.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing1d
} {}

test TokenUnboxing-1d.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing1d
} {{token = {{1}}}}

test TokenUnboxing-2.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing2
} {{{VariableTerm: value = general, depth = -2147483648, associated object = $r9}} {{VariableTerm: value = general, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing2: ptolemy.data.Token token>}}}

# test TokenUnboxing-2.2 {} {
#     _doUnboxingTest ptolemy.copernicus.java.test.Unboxing2
# } {}

# test TokenUnboxing-2.3 {} {
#     _doExecuteTest ptolemy.copernicus.java.test.Unboxing2
# } {token = 1}

test TokenUnboxing-3.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing3
} {{{VariableTerm: value = double, depth = 1, associated object = $r3}} {{VariableTerm: value = double, depth = 1, associated object = $r7}} {{VariableTerm: value = double, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing3: ptolemy.data.Token token>}}}

test TokenUnboxing-3.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing3
} {}

test TokenUnboxing-3.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing3
} {{token = 3.0}}

test TokenUnboxing-4.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing4
} {{(ArrayElementType({int}), int)} {{VariableTerm: value = general, depth = -2147483648, associated object = $r11}} {{VariableTerm: value = general, depth = -2147483648, associated object = $r15}} {{VariableTerm: value = general, depth = -2147483648, associated object = $r5}} {{VariableTerm: value = general, depth = -2147483648, associated object = $r7}} {{VariableTerm: value = general, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing4: ptolemy.data.ArrayToken arraytoken>}} {{VariableTerm: value = general, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing4: ptolemy.data.Token token>}} {{VariableTerm: value = general, depth = -2147483648, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing4: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

# test TokenUnboxing-4.2 {} {
#     _doUnboxingTest ptolemy.copernicus.java.test.Unboxing4
# } {}

# test TokenUnboxing-4.3 {} {
#     _doExecuteTest ptolemy.copernicus.java.test.Unboxing4
# } {token = 1}

test TokenUnboxing-5.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing5
} {{(ArrayElementType({int}), int)} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing5: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = r2}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}}}

test TokenUnboxing-5.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing5
} {}

test TokenUnboxing-5.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing5
} {{token = {1}}}

test TokenUnboxing-6.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing6
} {{{VariableTerm: value = int, depth = 1, associated object = $r11}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing6: ptolemy.data.Token castToken>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing6: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing6: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

test TokenUnboxing-6.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing6
} {}

test TokenUnboxing-6.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing6
} {{token = 1}}

test TokenUnboxing-7.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing7
} {{{VariableTerm: value = int, depth = 1, associated object = $r13}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = $r7}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing7: ptolemy.data.Token castToken>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing7: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing7: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

test TokenUnboxing-7.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing7
} {}

test TokenUnboxing-7.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing7
} {{token = 1}}

test TokenUnboxing-8.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing8
} {{{VariableTerm: value = int, depth = 1, associated object = $r10}} {{VariableTerm: value = int, depth = 1, associated object = $r2}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing8: ptolemy.data.Token token>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing8: ptolemy.data.Token[] tokens2>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing8: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}}}

test TokenUnboxing-8.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing8
} {}

test TokenUnboxing-8.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing8
} {{token = 1}}


test TokenUnboxing-9.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing9
} {{(ArrayElementType({int}), int)} {(ArrayElementType({unknown}), unknown)} {{VariableTerm: value = double, depth = 1, associated object = r7}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = r1}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = r3}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = $r8}} {{VariableTerm: value = {int}, depth = 2, associated object = <ptolemy.copernicus.java.test.Unboxing9: ptolemy.data.ArrayToken arraytoken>}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r1)}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r10}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r11}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r15}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r5}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r6}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r9}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = <ptolemy.copernicus.java.test.Unboxing9: ptolemy.data.ArrayToken arraytoken2>}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = <ptolemy.copernicus.java.test.Unboxing9: ptolemy.data.ArrayToken initialOutputs_CGToken>}} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = virtualinvoke $r5.<ptolemy.data.ArrayToken: ptolemy.data.Token[] arrayValue()>()}}} {Known Failure: cases where the type inference engine fails, but they do not come up in practice}

test TokenUnboxing-9.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing9
} {}

test TokenUnboxing-9.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing9
} {{token = {0}} {token = 0.0}}


test TokenUnboxing-10.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing10
} {{(ArrayElementType({int}), int)} {(ArrayElementType({int}), int)} {(ArrayElementType({int}), int)} {(ArrayElementType({int}), int)} {{VariableTerm: value = general, depth = -2147483648, associated object = r1}} {{VariableTerm: value = general, depth = -2147483648, associated object = r2}} {{VariableTerm: value = int, depth = 1, associated object = $r10}} {{VariableTerm: value = int, depth = 1, associated object = $r11}} {{VariableTerm: value = int, depth = 1, associated object = r4}} {{VariableTerm: value = {int}, depth = 2, associated object = $r12}} {{VariableTerm: value = {int}, depth = 2, associated object = $r13}} {{VariableTerm: value = {int}, depth = 2, associated object = $r17}} {{VariableTerm: value = {int}, depth = 2, associated object = $r4}} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}} {{VariableTerm: value = {int}, depth = 2, associated object = $r6}} {{VariableTerm: value = {int}, depth = 2, associated object = $r9}} {{VariableTerm: value = {int}, depth = 2, associated object = <ptolemy.copernicus.java.test.Unboxing10: ptolemy.data.ArrayToken arraytoken2>}} {{VariableTerm: value = {int}, depth = 2, associated object = <ptolemy.copernicus.java.test.Unboxing10: ptolemy.data.ArrayToken arraytoken>}} {{VariableTerm: value = {int}, depth = 2, associated object = <ptolemy.copernicus.java.test.Unboxing10: ptolemy.data.ArrayToken initialOutputs_CGToken>}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r12.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r5)}} {{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r6.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r1)}} {{VariableTerm: value = {int}, depth = 2, associated object = virtualinvoke $r4.<ptolemy.data.ArrayToken: ptolemy.data.Token[] arrayValue()>()}} {{VariableTerm: value = {int}, depth = 2, associated object = virtualinvoke $r9.<ptolemy.data.ArrayToken: ptolemy.data.Token[] arrayValue()>()}}}

test TokenUnboxing-10.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing10
} {}

test TokenUnboxing-10.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing10
} {{token = {0}} {token = 0.0}}


test TokenUnboxing-11.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing11
} {{(ArrayElementType({double}), double)} {{VariableTerm: value = boolean, depth = 1, associated object = r39}} {{VariableTerm: value = boolean, depth = 1, associated object = r43}} {{VariableTerm: value = double, depth = 1, associated object = $r10}} {{VariableTerm: value = double, depth = 1, associated object = $r11}} {{VariableTerm: value = double, depth = 1, associated object = $r12}} {{VariableTerm: value = double, depth = 1, associated object = $r13}} {{VariableTerm: value = double, depth = 1, associated object = $r14}} {{VariableTerm: value = double, depth = 1, associated object = $r15}} {{VariableTerm: value = double, depth = 1, associated object = $r16}} {{VariableTerm: value = double, depth = 1, associated object = $r17}} {{VariableTerm: value = double, depth = 1, associated object = $r18}} {{VariableTerm: value = double, depth = 1, associated object = $r20}} {{VariableTerm: value = double, depth = 1, associated object = $r21}} {{VariableTerm: value = double, depth = 1, associated object = $r21}} {{VariableTerm: value = double, depth = 1, associated object = $r22}} {{VariableTerm: value = double, depth = 1, associated object = $r24}} {{VariableTerm: value = double, depth = 1, associated object = $r26}} {{VariableTerm: value = double, depth = 1, associated object = $r37}} {{VariableTerm: value = double, depth = 1, associated object = $r3}} {{VariableTerm: value = double, depth = 1, associated object = $r56}} {{VariableTerm: value = double, depth = 1, associated object = $r5}} {{VariableTerm: value = double, depth = 1, associated object = $r6}} {{VariableTerm: value = double, depth = 1, associated object = $r7}} {{VariableTerm: value = double, depth = 1, associated object = $r8}} {{VariableTerm: value = double, depth = 1, associated object = $r9}} {{VariableTerm: value = double, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing11: ptolemy.data.Token token>}} {{VariableTerm: value = double, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing11: ptolemy.data.Token token_4_>}} {{VariableTerm: value = double, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing11: ptolemy.data.Token[] _outputs>}} {{VariableTerm: value = double, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing11: ptolemy.data.Token[] tokens>}} {{VariableTerm: value = double, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing11: ptolemy.data.Token[] tokens_6_>}} {{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[$i19]}} {{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[$i29]}} {{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[$i9]}} {{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[i0]}} {{VariableTerm: value = double, depth = 1, associated object = r23}} {{VariableTerm: value = double, depth = 1, associated object = r36}} {{VariableTerm: value = double, depth = 1, associated object = r55}} {{VariableTerm: value = double, depth = 1, associated object = r5}} {{VariableTerm: value = int, depth = 1, associated object = r3}} {{VariableTerm: value = int, depth = 1, associated object = r6}} {{VariableTerm: value = {double}, depth = 2, associated object = $r25}} {{VariableTerm: value = {double}, depth = 2, associated object = r27}} {{VariableTerm: value = {double}, depth = 2, associated object = specialinvoke $r25.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r26)}}}

test TokenUnboxing-11.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing11
} {}

test TokenUnboxing-11.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing11
} {{token = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}}}

test TokenUnboxing-12.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing12
} {{{VariableTerm: value = double, depth = 1, associated object = r7}} {{VariableTerm: value = int, depth = 1, associated object = r15}} {{VariableTerm: value = int, depth = 1, associated object = r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r1}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r9}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing12: ptolemy.data.Token _stateToken>}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing12: ptolemy.data.Token[] _resultArray>}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = newarray (ptolemy.data.Token)[1]}}} {Known Failure: cases where the type inference engine fails, but they do not come up in practice}

test TokenUnboxing-12.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing12
} {}

test TokenUnboxing-12.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing12
} {{token = 0.0} {token = 1.0}}


test TokenUnboxing-13.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing13
} {{{VariableTerm: value = double, depth = 1, associated object = r7}} {{VariableTerm: value = int, depth = 1, associated object = r15}} {{VariableTerm: value = int, depth = 1, associated object = r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r1}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r9}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing13: ptolemy.data.Token _stateToken>}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = <ptolemy.copernicus.java.test.Unboxing13: ptolemy.data.Token[] _resultArray>}} {{VariableTerm: value = unknown, depth = -2147483648, associated object = newarray (ptolemy.data.Token)[1]}}} {Known Failure: cases where the type inference engine fails, but they do not come up in practice}

test TokenUnboxing-13.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing13
} {} {Known Failure: cases where the type inference engine fails, but they do not come up in practice}

test TokenUnboxing-13.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing13
} {{token = 0.0} {token = 1.0}} {Known Failure: cases where the type inference engine fails, but they do not come up in practice}

test TokenUnboxing-14.1 {} {
    _doSpecializedTypesTest ptolemy.copernicus.java.test.Unboxing14
} {{{VariableTerm: value = double, depth = 1, associated object = r2}} {{VariableTerm: value = int, depth = 1, associated object = $r1}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r3}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r4}} {{VariableTerm: value = int, depth = 1, associated object = $r5}} {{VariableTerm: value = int, depth = 1, associated object = $r6}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing14: ptolemy.data.Token _stateToken>}} {{VariableTerm: value = int, depth = 1, associated object = <ptolemy.copernicus.java.test.Unboxing14: ptolemy.data.Token[] _resultArray>}} {{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}} {{VariableTerm: value = int, depth = 1, associated object = r15}} {{VariableTerm: value = int, depth = 1, associated object = r3}} {{VariableTerm: value = int, depth = 1, associated object = r5}}}

test TokenUnboxing-14.2 {} {
    _doUnboxingTest ptolemy.copernicus.java.test.Unboxing14
} {}

test TokenUnboxing-14.3 {} {
    _doExecuteTest ptolemy.copernicus.java.test.Unboxing14
} {{token = 0.0} {token = 1.0}}
