# Run tests on CCodeGenerator class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

test CCodeGenerator-1.1 {Instantiate a CodeGenerator} {
    set model [sdfModel]
    set codeGenerator \
	    [java::new ptolemy.codegen.c.kernel.CCodeGenerator \
	    $model "myCodeGenerator"]
    set generatorPackageParameter [java::cast  ptolemy.data.expr.StringParameter [$codeGenerator getAttribute generatorPackage]]

    list [$generatorPackageParameter getExpression]
} {ptolemy.codegen.c}

#####
test CodeGenerator-2.1 {splitLongBody no code} {
    set results [$codeGenerator splitLongBody 5 foo ""]
    $results getrange
} {{} {}}

#####
test CCodeGenerator-2.2 {splitLongBody code smaller than max body size} {
    set results [testSplitLongBody 4 5]
    $results getrange
} {{} {line 1;
line 2;
line 3;
line 4;
}}

#####
test CCodeGenerator-2.3 {splitLongBody code same size as max body size} {
    set results [testSplitLongBody 5 5]
    $results getrange
} {{} {line 1;
line 2;
line 3;
line 4;
line 5;
}}

#####
test CCodeGenerator-2.4 {splitLongBody code same size one over max body size} {
    set results [testSplitLongBody 6 5]
    $results getrange
} {{void foo_0(void) {
line 1;
line 2;
line 3;
line 4;
line 5;
}
void foo_1(void) {
line 6;
}
} {foo_0();
foo_1();
}}

#####
test CCodeGenerator-2.5 {splitLongBody code same size one over max body size} {
    set results [testSplitLongBody 12 5]
    $results getrange
} {{void foo_0(void) {
line 1;
line 2;
line 3;
line 4;
line 5;
}
void foo_1(void) {
line 6;
line 7;
line 8;
line 9;
line 10;
}
void foo_2(void) {
line 11;
line 12;
}
} {foo_0();
foo_1();
foo_2();
}}
