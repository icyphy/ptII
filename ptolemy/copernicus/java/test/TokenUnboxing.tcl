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
    exec java -classpath "$PTII;$PTII/lib/sootclasses.jar;$PTII/lib/jasminclasses.jar;." ptolemy.copernicus.java.test.TestSpecializeTypesMain "$className"    
} 
proc _doUnboxingTest {className} {
    global PTII
    set outputDir [file join [pwd] testOutput $className]
    exec java -classpath "$PTII;$PTII/lib/sootclasses.jar;$PTII/lib/jasminclasses.jar;." ptolemy.copernicus.java.test.TestUnboxingMain "$outputDir" "$className"    
    set f [open [file join testOutput $className $className.jimple]]
    read $f
    close $f
}

proc _doExecuteTest {className} {
    global PTII
    set outputDir [file join [pwd] testOutput $className]
    exec java -classpath "$PTII;$outputDir" $className
}

#First, do an SDF test just to be sure things are working
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
} {{VariableTerm: value = int, depth = 1, associated object = $r6}
{VariableTerm: value = {int}, depth = 2, associated object = r2}
{VariableTerm: value = {int}, depth = 2, associated object = $r5}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing1b: ptolemy.data.Token[] tokens>}
{VariableTerm: value = int, depth = 1, associated object = $r3}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
(ArrayElementType({int}), int)
{VariableTerm: value = int, depth = 1, associated object = $r4}}

test TokenUnboxing-1b.2 {} {
    _doUnboxingTest Unboxing1b
} {}

test TokenUnboxing-1b.3 {} {
    _doExecuteTest Unboxing1b
} {token = {1}}

test TokenUnboxing-1c.1 {} {
    _doSpecializedTypesTest Unboxing1c
} {{VariableTerm: value = {int}, depth = 2, associated object = $r7}
{VariableTerm: value = int, depth = 1, associated object = r3}
(ArrayElementType({int}), int)
{VariableTerm: value = {int}, depth = 2, associated object = r5}
(ArrayElementType({{int}}), {int})
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = {{int}}, depth = 3, associated object = specialinvoke $r8.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r5)}
{VariableTerm: value = {int}, depth = 2, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r7.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r3)}
{VariableTerm: value = {{int}}, depth = 3, associated object = $r8}
{VariableTerm: value = {int}, depth = 2, associated object = r4}
{VariableTerm: value = {{int}}, depth = 3, associated object = r6}}

test TokenUnboxing-1c.2 {} {
    _doUnboxingTest Unboxing1c
} {}

test TokenUnboxing-1c.3 {} {
    _doExecuteTest Unboxing1c
} {token = {{1}}}


test TokenUnboxing-1d.1 {} {
    _doSpecializedTypesTest Unboxing1d
} {{VariableTerm: value = {{int}}, depth = 3, associated object = <Unboxing1d: ptolemy.data.ArrayToken intArrayArrayToken>}
(ArrayElementType({int}), int)
{VariableTerm: value = {int}, depth = 2, associated object = $r4}
{VariableTerm: value = {{int}}, depth = 3, associated object = specialinvoke $r9.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r10)}
(ArrayElementType({{int}}), {int})
{VariableTerm: value = {int}, depth = 2, associated object = $r8}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing1d: ptolemy.data.Token[] tokens>}
{VariableTerm: value = int, depth = 1, associated object = $r3}
{VariableTerm: value = {{int}}, depth = 3, associated object = $r14}
{VariableTerm: value = {int}, depth = 2, associated object = $r7}
{VariableTerm: value = {int}, depth = 2, associated object = $r10}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r4.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r5)}
{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing1d: ptolemy.data.ArrayToken intArrayToken>}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r5}
{VariableTerm: value = {int}, depth = 2, associated object = $r6}
{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing1d: ptolemy.data.Token[] tokens2>}
{VariableTerm: value = {{int}}, depth = 3, associated object = $r9}
{VariableTerm: value = {int}, depth = 2, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r2}
}

test TokenUnboxing-1d.2 {} {
    _doUnboxingTest Unboxing1d
} {}

test TokenUnboxing-1d.3 {} {
    _doExecuteTest Unboxing1d
} {token = {{1}}}

test TokenUnboxing-2.1 {} {
    _doSpecializedTypesTest Unboxing2
} {{VariableTerm: value = general, depth = -2147483648, associated object = <Unboxing2: ptolemy.data.Token token>}
    {VariableTerm: value = general, depth = -2147483648, associated object = $r9}}

# test TokenUnboxing-2.2 {} {
#     _doUnboxingTest Unboxing2
# } {}

# test TokenUnboxing-2.3 {} {
#     _doExecuteTest Unboxing2
# } {token = 1}

test TokenUnboxing-3.1 {} {
    _doSpecializedTypesTest Unboxing3
} {{VariableTerm: value = double, depth = 1, associated object = <Unboxing3: ptolemy.data.Token token>}
{VariableTerm: value = double, depth = 1, associated object = $r7}
{VariableTerm: value = double, depth = 1, associated object = $r3}}

test TokenUnboxing-3.2 {} {
    _doUnboxingTest Unboxing3
} {}

test TokenUnboxing-3.3 {} {
    _doExecuteTest Unboxing3
} {token = 3.0}

test TokenUnboxing-4.1 {} {
    _doSpecializedTypesTest Unboxing4
} {{VariableTerm: value = general, depth = -2147483648, associated object = <Unboxing4: ptolemy.data.Token token>}
{VariableTerm: value = general, depth = -2147483648, associated object = $r11}
{VariableTerm: value = general, depth = -2147483648, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r4}
{VariableTerm: value = general, depth = -2147483648, associated object = $r7}
{VariableTerm: value = general, depth = -2147483648, associated object = $r5}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing4: ptolemy.data.Token[] tokens>}
{VariableTerm: value = general, depth = -2147483648, associated object = $r15}
{VariableTerm: value = general, depth = -2147483648, associated object = <Unboxing4: ptolemy.data.ArrayToken arraytoken>}
(ArrayElementType({int}), int)
{VariableTerm: value = int, depth = 1, associated object = $r6}
{VariableTerm: value = int, depth = 1, associated object = $r3}}

# test TokenUnboxing-4.2 {} {
#     _doUnboxingTest Unboxing4
# } {}

# test TokenUnboxing-4.3 {} {
#     _doExecuteTest Unboxing4
# } {token = 1}

test TokenUnboxing-5.1 {} {
    _doSpecializedTypesTest Unboxing5
} {{VariableTerm: value = {int}, depth = 2, associated object = $r5}
{VariableTerm: value = {int}, depth = 2, associated object = r2}
(ArrayElementType({int}), int)
{VariableTerm: value = int, depth = 1, associated object = $r4}
{VariableTerm: value = int, depth = 1, associated object = $r3}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r6}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r6)}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing5: ptolemy.data.Token[] tokens>}}

test TokenUnboxing-5.2 {} {
    _doUnboxingTest Unboxing5
} {}

test TokenUnboxing-5.3 {} {
    _doExecuteTest Unboxing5
} {token = {1}}

test TokenUnboxing-6.1 {} {
    _doSpecializedTypesTest Unboxing6
} {{VariableTerm: value = int, depth = 1, associated object = $r3}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing6: ptolemy.data.Token[] tokens>}
{VariableTerm: value = int, depth = 1, associated object = $r4}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r11}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing6: ptolemy.data.Token castToken>}
{VariableTerm: value = int, depth = 1, associated object = $r5}
{VariableTerm: value = int, depth = 1, associated object = $r6}
{VariableTerm: value = int, depth = 1, associated object = $r2}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing6: ptolemy.data.Token token>}}

test TokenUnboxing-6.2 {} {
    _doUnboxingTest Unboxing6
} {}

test TokenUnboxing-6.3 {} {
    _doExecuteTest Unboxing6
} {token = 1}

test TokenUnboxing-7.1 {} {
    _doSpecializedTypesTest Unboxing7
} {{VariableTerm: value = int, depth = 1, associated object = $r6}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing7: ptolemy.data.Token token>}
{VariableTerm: value = int, depth = 1, associated object = $r3}
{VariableTerm: value = int, depth = 1, associated object = $r13}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing7: ptolemy.data.Token castToken>}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r5}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing7: ptolemy.data.Token[] tokens>}
{VariableTerm: value = int, depth = 1, associated object = $r2}
{VariableTerm: value = int, depth = 1, associated object = $r7}
{VariableTerm: value = int, depth = 1, associated object = $r4}}

test TokenUnboxing-7.2 {} {
    _doUnboxingTest Unboxing7
} {}

test TokenUnboxing-7.3 {} {
    _doExecuteTest Unboxing7
} {token = 1}

test TokenUnboxing-8.1 {} {
    _doSpecializedTypesTest Unboxing8
} {{VariableTerm: value = int, depth = 1, associated object = $r4}
{VariableTerm: value = int, depth = 1, associated object = $r10}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing8: ptolemy.data.Token token>}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing8: ptolemy.data.Token[] tokens2>}
{VariableTerm: value = int, depth = 1, associated object = $r3}
{VariableTerm: value = int, depth = 1, associated object = $r2}
{VariableTerm: value = int, depth = 1, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = int, depth = 1, associated object = $r5}
{VariableTerm: value = int, depth = 1, associated object = $r6}
{VariableTerm: value = int, depth = 1, associated object = <Unboxing8: ptolemy.data.Token[] tokens>}}

test TokenUnboxing-8.2 {} {
    _doUnboxingTest Unboxing8
} {}

test TokenUnboxing-8.3 {} {
    _doExecuteTest Unboxing8
} {token = 1}


test TokenUnboxing-9.1 {} {
    _doSpecializedTypesTest Unboxing9
} {{VariableTerm: value = {unknown}, depth = -2147483647, associated object = <Unboxing9: ptolemy.data.ArrayToken arraytoken2>}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = <Unboxing9: ptolemy.data.ArrayToken initialOutputs_CGToken>}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r6}
(ArrayElementType({unknown}), unknown)
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r11}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r9}
{VariableTerm: value = {int}, depth = 2, associated object = $r8}
{VariableTerm: value = double, depth = 1, associated object = r7}
{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing9: ptolemy.data.ArrayToken arraytoken>}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = virtualinvoke $r5.<ptolemy.data.ArrayToken: ptolemy.data.Token[] arrayValue()>()}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r5.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r1)}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r5}
{VariableTerm: value = unknown, depth = -2147483648, associated object = r1}
{VariableTerm: value = unknown, depth = -2147483648, associated object = r3}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r15}
(ArrayElementType({int}), int)
{VariableTerm: value = {int}, depth = 2, associated object = $r5}
{VariableTerm: value = {unknown}, depth = -2147483647, associated object = $r10}}

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
} {{VariableTerm: value = {int}, depth = 2, associated object = $r13}
(ArrayElementType({int}), int)
(ArrayElementType({int}), int)
{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing10: ptolemy.data.ArrayToken arraytoken2>}
{VariableTerm: value = {int}, depth = 2, associated object = virtualinvoke $r9.<ptolemy.data.ArrayToken: ptolemy.data.Token[] arrayValue()>()}
{VariableTerm: value = {int}, depth = 2, associated object = $r17}
{VariableTerm: value = general, depth = -2147483648, associated object = r2}
{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing10: ptolemy.data.ArrayToken arraytoken>}
{VariableTerm: value = {int}, depth = 2, associated object = <Unboxing10: ptolemy.data.ArrayToken initialOutputs_CGToken>}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r12.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r5)}
{VariableTerm: value = {int}, depth = 2, associated object = specialinvoke $r6.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>(r1)}
{VariableTerm: value = {int}, depth = 2, associated object = $r5}
(ArrayElementType({int}), int)
{VariableTerm: value = {int}, depth = 2, associated object = $r9}
{VariableTerm: value = general, depth = -2147483648, associated object = r1}
{VariableTerm: value = int, depth = 1, associated object = r4}
{VariableTerm: value = int, depth = 1, associated object = $r11}
{VariableTerm: value = {int}, depth = 2, associated object = $r6}
{VariableTerm: value = {int}, depth = 2, associated object = virtualinvoke $r4.<ptolemy.data.ArrayToken: ptolemy.data.Token[] arrayValue()>()}
{VariableTerm: value = {int}, depth = 2, associated object = $r12}
{VariableTerm: value = int, depth = 1, associated object = $r10}
{VariableTerm: value = {int}, depth = 2, associated object = $r4}
(ArrayElementType({int}), int)
---- Result should have been:

---- TokenUnboxing-10.1 FAILED


==== TokenUnboxing-10.3 
==== Contents of test case:

    _doExecuteTest Unboxing10

==== Result was:
token = {0}
token = 0.0
---- Result should have been:
token = 0.0
---- TokenUnboxing-10.3 FAILED


==== TokenUnboxing-11.1 
==== Contents of test case:

    _doSpecializedTypesTest Unboxing11

==== Result was:
{VariableTerm: value = double, depth = 1, associated object = $r12}
{VariableTerm: value = double, depth = 1, associated object = r55}
(ArrayElementType({double}), double)
{VariableTerm: value = double, depth = 1, associated object = <Unboxing11: ptolemy.data.Token[] tokens_6_>}
{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[$i9]}
{VariableTerm: value = double, depth = 1, associated object = $r6}
{VariableTerm: value = double, depth = 1, associated object = r36}
{VariableTerm: value = double, depth = 1, associated object = $r21}
{VariableTerm: value = {double}, depth = 2, associated object = r27}
{VariableTerm: value = double, depth = 1, associated object = r5}
{VariableTerm: value = {double}, depth = 2, associated object = specialinvoke $r25.<ptolemy.data.ArrayToken: void <init>(ptolemy.data.Token[])>($r26)}
{VariableTerm: value = double, depth = 1, associated object = $r17}
{VariableTerm: value = double, depth = 1, associated object = $r21}
{VariableTerm: value = double, depth = 1, associated object = r23}
{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[i0]}
{VariableTerm: value = double, depth = 1, associated object = <Unboxing11: ptolemy.data.Token[] tokens>}
{VariableTerm: value = double, depth = 1, associated object = <Unboxing11: ptolemy.data.Token token_4_>}
{VariableTerm: value = double, depth = 1, associated object = <Unboxing11: ptolemy.data.Token token>}
{VariableTerm: value = double, depth = 1, associated object = $r3}
{VariableTerm: value = double, depth = 1, associated object = $r22}
{VariableTerm: value = double, depth = 1, associated object = $r11}
{VariableTerm: value = double, depth = 1, associated object = $r15}
{VariableTerm: value = double, depth = 1, associated object = $r7}
{VariableTerm: value = double, depth = 1, associated object = $r18}
{VariableTerm: value = double, depth = 1, associated object = $r56}
{VariableTerm: value = int, depth = 1, associated object = r6}
{VariableTerm: value = double, depth = 1, associated object = $r37}
{VariableTerm: value = double, depth = 1, associated object = $r16}
{VariableTerm: value = double, depth = 1, associated object = $r24}
{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[$i19]}
{VariableTerm: value = double, depth = 1, associated object = <Unboxing11: ptolemy.data.Token[] _outputs>}
{VariableTerm: value = double, depth = 1, associated object = $r10}
{VariableTerm: value = boolean, depth = 1, associated object = r39}
{VariableTerm: value = double, depth = 1, associated object = newarray (ptolemy.data.Token)[$i29]}
{VariableTerm: value = double, depth = 1, associated object = $r20}
{VariableTerm: value = double, depth = 1, associated object = $r9}
{VariableTerm: value = double, depth = 1, associated object = $r13}
{VariableTerm: value = {double}, depth = 2, associated object = $r25}
{VariableTerm: value = double, depth = 1, associated object = $r26}
{VariableTerm: value = double, depth = 1, associated object = $r8}
{VariableTerm: value = double, depth = 1, associated object = $r14}
{VariableTerm: value = boolean, depth = 1, associated object = r43}
{VariableTerm: value = int, depth = 1, associated object = r3}
{VariableTerm: value = double, depth = 1, associated object = $r5}}

test TokenUnboxing-11.2 {} {
    _doUnboxingTest Unboxing11
} {}

test TokenUnboxing-11.3 {} {
    _doExecuteTest Unboxing11
} {token = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}}

test TokenUnboxing-12.1 {} {
    _doSpecializedTypesTest Unboxing12
} {{VariableTerm: value = unknown, depth = -2147483648, associated object = $r9}
{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}
{VariableTerm: value = int, depth = 1, associated object = r3}
{VariableTerm: value = unknown, depth = -2147483648, associated object = newarray (ptolemy.data.Token)[1]}
{VariableTerm: value = unknown, depth = -2147483648, associated object = <Unboxing12: ptolemy.data.Token[] _resultArray>}
{VariableTerm: value = unknown, depth = -2147483648, associated object = $r1}
{VariableTerm: value = unknown, depth = -2147483648, associated object = <Unboxing12: ptolemy.data.Token _stateToken>}
{VariableTerm: value = int, depth = 1, associated object = r15}
{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}
{VariableTerm: value = double, depth = 1, associated object = r7}
{VariableTerm: value = unknown, depth = -2147483648, associated object = $r3}}

test TokenUnboxing-12.2 {} {
    _doUnboxingTest Unboxing12
} {}

test TokenUnboxing-12.3 {} {
    _doExecuteTest Unboxing12
} {}
