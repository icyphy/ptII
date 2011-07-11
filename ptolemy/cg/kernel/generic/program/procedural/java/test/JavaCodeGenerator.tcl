# Test JavaCodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2009-2011 The Regents of the University of California.
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
test JavaCodeGenerator-1.1 {Instantiate a JavaCodeGenerator, call a few methods} {
    set model [sdfModel]
    set codeGenerator \
	    [java::new ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator \
	    $model "myCodeGenerator"]

    # Call setCodeGenerator for complete code coverage, even though it
    # does not do anything.
    $codeGenerator setCodeGenerator $codeGenerator

    list \
	[$codeGenerator toString] \
	[$codeGenerator comment {This is a comment}]
} {{ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator {.top.myCodeGenerator}} {/* This is a comment */
}}

################################################
# Tests for splitLongBody and splitVariableDeclaration

# Create dummy code
proc makeCode {lines} {
    set results {line 1;}
    for {set i 2} {$i <= $lines} {incr i} {
	set results "$results\nline $i;"
    }
    return "$results\n";
}

# Return a two element array
proc testSplitLongBody {codeLines bodyLines} {
    global codeGenerator
    set code [makeCode $codeLines]
    return [$codeGenerator splitLongBody $bodyLines foo $code]
}

# Return a list of at least two Strings.
proc testSplitVariableDeclaration {codeLines bodyLines} {
    global codeGenerator
    set code [makeCode $codeLines]
    return [$codeGenerator splitVariableDeclaration $bodyLines foo $code]
}


#####
test JavaCodeGenerator-2.1 {splitLongBody no code} {
    set results [$codeGenerator splitLongBody 5 foo ""]
    $results getrange
} {{} {}}

#####
test JavaCodeGenerator-2.2 {splitLongBody code smaller than max body size} {
    set results [testSplitLongBody 4 5]
    $results getrange
} {{} {line 1;
line 2;
line 3;
line 4;
}}

#####
test JavaCodeGenerator-2.3 {splitLongBody code same size as max body size} {
    set results [testSplitLongBody 5 5]
    $results getrange
} {{} {line 1;
line 2;
line 3;
line 4;
line 5;
}}

#####
test JavaCodeGenerator-2.4 {splitLongBody code same size one over max body size} {
    set results [testSplitLongBody 6 5]
    list [$results get 0] [$results get 1]
} {{class foo {
class _fo_sL_0 {
_fo_sL_0() throws Exception {
line 1;
line 2;
line 3;
line 4;
line 5;
}
}
class _fo_sL_1 {
_fo_sL_1() throws Exception {
line 6;
}
}

void callAllfoo() throws Exception {
foo foo = new foo();
new _fo_sL_0();
new _fo_sL_1();
}
}
} {foo foo = new foo();
foo.callAllfoo();
}}


#####
test JavaCodeGenerator-2.5 {splitLongBody code same size one over max body size} {
    set results [testSplitLongBody 12 5]
    $results getrange
} {{class foo {
class _fo_sL_0 {
_fo_sL_0() throws Exception {
line 1;
line 2;
line 3;
line 4;
line 5;
}
}
class _fo_sL_1 {
_fo_sL_1() throws Exception {
line 6;
line 7;
line 8;
line 9;
line 10;
}
}
class _fo_sL_2 {
_fo_sL_2() throws Exception {
line 11;
line 12;
}
}

void callAllfoo() throws Exception {
foo foo = new foo();
new _fo_sL_0();
new _fo_sL_1();
new _fo_sL_2();
}
}
} {foo foo = new foo();
foo.callAllfoo();
}}

#####
test JavaCodeGenerator-2.6 {Don't split try catch blocks} {
    set code "try \{
line1;
line2;
line3.1;
line3.2;
line3.3;
line3.4;
line3.5;
\} catch (Exception ex) \{
    line4;
    line5;
    line6;
\}"

   set results [$codeGenerator splitLongBody 2 foo $code]
   $results getrange
} {{class foo {
class _fo_sL_0 {
_fo_sL_0() throws Exception {
try {
line1;
line2;
line3.1;
line3.2;
line3.3;
line3.4;
line3.5;
} catch (Exception ex) {
    line4;
    line5;
    line6;
}
}
}

void callAllfoo() throws Exception {
foo foo = new foo();
new _fo_sL_0();
}
}
} {foo foo = new foo();
foo.callAllfoo();
}}


#####
test JavaCodeGenerator-2.7 {one line per method start with a comment} {
    set code "     /* This is a comment*/"

   set results [$codeGenerator splitLongBody 1 foo $code]
   $results getrange
} {{} {     /* This is a comment*/
}}

#####
test JavaCodeGenerator-2.8 {one line per method} {
    set results [testSplitLongBody 3 1]
   $results getrange
} {{class foo {
class _fo_sL_0 {
_fo_sL_0() throws Exception {
line 1;
}
}
class _fo_sL_1 {
_fo_sL_1() throws Exception {
line 2;
}
}
class _fo_sL_2 {
_fo_sL_2() throws Exception {
line 3;
}
}

void callAllfoo() throws Exception {
foo foo = new foo();
new _fo_sL_0();
new _fo_sL_1();
new _fo_sL_2();
}
}
} {foo foo = new foo();
foo.callAllfoo();
}}

#################################################################
#### splitVariableDeclaration
test JavaCodeGenerator-3.1 {splitVariableDeclaration no code} {
    set results [$codeGenerator splitVariableDeclaration 5 foo ""]
    # listToStrings is defined in $PTII/util/testsuite/enums.tcl
    listToStrings $results
} {{} {}}

#####
test JavaCodeGenerator-3.2 {splitVariableDeclaration code smaller than max body size} {
    set results [testSplitVariableDeclaration 4 5]
    listToStrings $results
} {{} {line 1;
line 2;
line 3;
line 4;
}}

#####
test JavaCodeGenerator-3.3 {splitVariableDeclaration code same size as max body size} {
    set results [testSplitVariableDeclaration 5 5]
    listToStrings $results
} {{} {line 1;
line 2;
line 3;
line 4;
line 5;
}}

#####
test JavaCodeGenerator-3.4 {splitVariableDeclaration code same size one over max body size} {
    set results [testSplitVariableDeclaration 6 5]
    listToStrings $results
} {{import foo.Token;
import static foo.class0.*;
import static foo.class1.*;
} {package foo;
import foo.Token;

public class class0 { 
line 1;
line 2;
line 3;
line 4;
line 5;
}
} {package foo;
import foo.Token;

public class class1 { 
line 6;
}
}}

#####
test JavaCodeGenerator-3.5 {splitVariableDeclaration code same size one over max body size} {
    set results [testSplitVariableDeclaration 12 5]
    listToStrings $results
} {{import foo.Token;
import static foo.class0.*;
import static foo.class1.*;
import static foo.class2.*;
} {package foo;
import foo.Token;

public class class0 { 
line 1;
line 2;
line 3;
line 4;
line 5;
}
} {package foo;
import foo.Token;

public class class1 { 
line 6;
line 7;
line 8;
line 9;
line 10;
}
} {package foo;
import foo.Token;

public class class2 { 
line 11;
line 12;
}
}}

#####
test JavaCodeGenerator-3.7 {one line per method start with a comment} {
    set code "     /* This is a comment*/"

    set results [$codeGenerator splitVariableDeclaration 1 foo $code]
    listToStrings $results
} {{} {     /* This is a comment*/}}

#####
test JavaCodeGenerator-3.8 {one line per method} {
    set results [testSplitVariableDeclaration 3 1]
    listToStrings $results
} {{import foo.Token;
import static foo.class0.*;
import static foo.class1.*;
import static foo.class2.*;
} {package foo;
import foo.Token;

public class class0 { 
line 1;
}
} {package foo;
import foo.Token;

public class class1 { 
line 2;
}
} {package foo;
import foo.Token;

public class class2 { 
line 3;
}
}}

#####
test JavaCodeGenerator-4.0 {Token.java not being written to the write location} {
    set args [java::new {String[]} 7  \
		  {{-language} {java} \
		       {-inline} {false} \
		       {-maximumLinesPerBlock} {2} \
		       {auto/Repeat.xml}} ]
    java::call ptolemy.cg.kernel.generic.test.TestGenericCodeGenerator main $args
} {}

