# Test JavaCodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2009-2010 The Regents of the University of California.
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
# Tests for splitLongBody

proc makeCode {lines} {
    set results {line 1;}
    for {set i 2} {$i <= $lines} {incr i} {
	set results "$results\nline $i;"
    }
    return $results;
}

proc testSplitLongBody {codeLines bodyLines} {
    global codeGenerator
    set code [makeCode $codeLines]
    return [$codeGenerator splitLongBody $bodyLines foo $code]
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
} {{public class foo {
void foo_0() {
line 1;
line 2;
line 3;
line 4;
line 5;
}
void foo_1() {
line 6;
}

void callAllfoo() {
foo foo = new foo();
foo.foo_0();
foo.foo_1();
}
}
} {foo foo = new foo();
foo.callAllfoo();
}}

#####
test JavaCodeGenerator-2.5 {splitLongBody code same size one over max body size} {
    set results [testSplitLongBody 12 5]
    $results getrange
} {{public class foo {
void foo_0() {
line 1;
line 2;
line 3;
line 4;
line 5;
}
void foo_1() {
line 6;
line 7;
line 8;
line 9;
line 10;
}
void foo_2() {
line 11;
line 12;
}

void callAllfoo() {
foo foo = new foo();
foo.foo_0();
foo.foo_1();
foo.foo_2();
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
} {{public class foo {
void foo_0() {
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

void callAllfoo() {
foo foo = new foo();
foo.foo_0();
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
} {{public class foo {
void foo_0() {
line 1;
}
void foo_1() {
line 2;
}
void foo_2() {
line 3;
}

void callAllfoo() {
foo foo = new foo();
foo.foo_0();
foo.foo_1();
foo.foo_2();
}
}
} {foo foo = new foo();
foo.callAllfoo();
}}

