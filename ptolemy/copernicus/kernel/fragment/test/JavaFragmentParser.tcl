# Tests for the JavaFragmentParser
#
# @Author: Stephen Neuendorffer
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

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

proc doSimpleParseTest {string} {
    set voidType [java::call soot.VoidType v]
    set intType [java::call soot.VoidType v]
    set jimple [java::call soot.jimple.Jimple v]
    set theClass [java::new soot.SootClass foo 0]
    set theMethod [java::new soot.SootMethod bar [java::new java.util.LinkedList] $voidType 0]
    $theClass addMethod $theMethod
    set staticMethod [java::new soot.SootMethod bars [java::new java.util.LinkedList] $voidType [java::field soot.Modifier STATIC]]
    $theClass addMethod $staticMethod

    set theField [java::new soot.SootField baz [java::call soot.RefType v $theClass]]
    $theClass addField $theField
    set staticField [java::new soot.SootField bazs [java::call soot.RefType v $theClass] [java::field soot.Modifier STATIC]]
    $theClass addField $staticField

    set theBody [$jimple newBody $theMethod]
    $theBody insertIdentityStmts
    $theMethod setActiveBody $theBody
    java::call ptolemy.copernicus.kernel.JavaFragmentParser add $theBody $string
    [java::call soot.jimple.toolkits.scalar.NopEliminator v] transform $theBody "test"
    list [[$theBody getUnits] toString] [[java::cast java.lang.Object [$theBody getLocals]] toString] [[java::cast java.lang.Object [$theBody getTraps]] toString]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test JavaFragmentParser-1.0 {Simple statement} {
    doSimpleParseTest ";"
} {{[this := @this: foo]} {[this]} {[]}}

test JavaFragmentParser-1.1 {Simple statement} {
    doSimpleParseTest "{int a = 5;}"
} {{[this := @this: foo, a = 5]} {[this, a]} {[]}}

test JavaFragmentParser-1.2 {Simple statement} {
    doSimpleParseTest "{boolean a = 5 > 7;}"
} {{[this := @this: foo, $gt = 5 > 7, a = $gt]} {[this, a, $gt]} {[]}}

test JavaFragmentParser-1.3 {Simple statement} {
    doSimpleParseTest "{boolean a = 5 < 7;}"
} {{[this := @this: foo, $lt = 5 < 7, a = $lt]} {[this, a, $lt]} {[]}}

test JavaFragmentParser-1.4 {Simple statement} {
    doSimpleParseTest "{boolean a = 5 <= 7;}"
} {{[this := @this: foo, $lte = 5 <= 7, a = $lte]} {[this, a, $lte]} {[]}}

test JavaFragmentParser-1.5 {Simple statement} {
    doSimpleParseTest "{boolean a = 5 >= 7;}"
} {{[this := @this: foo, $gte = 5 >= 7, a = $gte]} {[this, a, $gte]} {[]}}

test JavaFragmentParser-1.6 {Simple statement} {
    doSimpleParseTest "{int a = 5 + 7;}"
} {{[this := @this: foo, $add = 5 + 7, a = $add]} {[this, a, $add]} {[]}}

test JavaFragmentParser-1.7 {Simple statement} {
    doSimpleParseTest "{int a; double b = 5 * 7;}"
} {{[this := @this: foo, $multiply = 5 * 7, b = $multiply]} {[this, a, b, $multiply]} {[]}}

test JavaFragmentParser-1.8 {Simple statement} {
    doSimpleParseTest "{int a = 5 / 7; float b = a;}"
} {{[this := @this: foo, $divide = 5 / 7, a = $divide, b = a]} {[this, a, $divide, b]} {[]}}

test JavaFragmentParser-1.9 {Simple statement} {
    doSimpleParseTest "{int a = 5 > 7 ? 3 :4 ; float b = a;}"
} {{[this := @this: foo, $gt = 5 > 7, if $gt == 0 goto $result = 4, $result = 3, goto [?= a = $result], $result = 4, a = $result, b = a]} {[this, a, $gt, $result, b]} {[]}}

test JavaFragmentParser-1.10 {Simple statement} {
    doSimpleParseTest "{int a = 5 > 7;  int b; if(a) {b=2*a;} else {b=3*a;} int c=1;}"
} {{[this := @this: foo, $gt = 5 > 7, a = $gt, if a == 0 goto $multiply = 3 * a, $multiply = 2 * a, b = $multiply, goto [?= c = 1], $multiply = 3 * a, b = $multiply, c = 1]} {[this, a, $gt, b, $multiply, $multiply, c]} {[]}}

test JavaFragmentParser-1.11 {Simple statement} {
    doSimpleParseTest "{int a = 5; a++; int b = a--; int c = ++a; int d = --a;}"
} {{[this := @this: foo, a = 5, a = a + 1, $copy = a, $decrement = a + -1, a = $decrement, b = $copy, $increment = a + 1, a = $increment, c = a, $decrement = a + -1, a = $decrement, d = a]} {[this, a, b, $copy, $decrement, c, $increment, d, $decrement]} {[]}}


test JavaFragmentParser-2.1 {For statement} {
    doSimpleParseTest "{int a = 5; for(int i =0; i<10; i++) {a=2*a;} int c=1;}"
} {{[this := @this: foo, a = 5, i = 0, $lt = i < 10, if $lt == 0 goto c = 1, goto [?= $multiply = 2 * a], i = i + 1, goto [?= $lt = i < 10], $multiply = 2 * a, a = $multiply, goto [?= i = i + 1], c = 1]} {[this, a, i, $lt, $multiply, c]} {[]}}

test JavaFragmentParser-2.2 {Do statement} {
    doSimpleParseTest "{int a = 5; do {a=2*a;} while(a <50); int c=1;}"
} {{[this := @this: foo, a = 5, $multiply = 2 * a, a = $multiply, $lt = a < 50, if $lt != 0 goto $multiply = 2 * a, c = 1]} {[this, a, $multiply, $lt, c]} {[]}}

test JavaFragmentParser-2.3 {While statement} {
    doSimpleParseTest "{int a = 5; while(a <50) {a=2*a;} int c=1;}"
} {{[this := @this: foo, a = 5, $lt = a < 50, if $lt == 0 goto c = 1, $multiply = 2 * a, a = $multiply, goto [?= $lt = a < 50], c = 1]} {[this, a, $lt, $multiply, c]} {[]}}

test JavaFragmentParser-2.4 {Throw statement} {
    doSimpleParseTest "{throw new java.lang.RuntimeException(\"hello\");}"
} {{[this := @this: foo, $new = new java.lang.RuntimeException, specialinvoke $new.<java.lang.RuntimeException: void <init>(java.lang.String)>("hello"), throw $new]} {[this, $new]} {[]}}

test JavaFragmentParser-2.5 {Switch statement} {
    doSimpleParseTest "{int a = 5; int b; switch(a) {case 3:b=1; break; case 5:b=2; default:b=3;} int c=1;}"
} {{[this := @this: foo, a = 5, goto [?= (branch)], b = 1, goto [?= c = 1], b = 2, b = 3, goto [?= c = 1], lookupswitch(a) {     case 3: goto b = 1;     case 5: goto b = 2;     default: goto b = 3; }, c = 1]} {[this, a, b, c]} {[]}}

test JavaFragmentParser-2.6 {try statement} {
    doSimpleParseTest "{int a = 5; try { int b=3;} catch (java.lang.RuntimeException ex) { int c = 4;} int d = 5;}"
} {{[this := @this: foo, a = 5, b = 3, goto [?= d = 5], c = 4, d = 5]} {[this, a, b, c, d]} {[Trap :
begin  : b = 3
end    : goto [?= d = 5]
handler: c = 4]}}


test JavaFragmentParser-3.1 {Allocation statement} {
    doSimpleParseTest "{int a\[\] = new int\[3\];}"
} {{[this := @this: foo, a = newarray (int)[3]]} {[this, a]} {[]}}

test JavaFragmentParser-3.2 {Allocation statement} {
    doSimpleParseTest "{java.lang.Object a = new java.lang.Object();}"
} {{[this := @this: foo, $new = new java.lang.Object, specialinvoke $new.<java.lang.Object: void <init>()>(), a = $new]} {[this, a, $new]} {[]}}

test JavaFragmentParser-3.3 {Allocation statement} {
    doSimpleParseTest "{java.lang.Object a\[\] = new java.lang.Object\[3\];}"
} {{[this := @this: foo, a = newarray (java.lang.Object)[3]]} {[this, a]} {[]}}

test JavaFragmentParser-3.4 {Allocation statement} {
    doSimpleParseTest "{int a\[\]\[\] = new int\[3\]\[\];}"
} {{[this := @this: foo, a = newarray (int[])[3]]} {[this, a]} {[]}}

test JavaFragmentParser-3.5 {Allocation statement} {
    doSimpleParseTest "{int a\[\]\[\] = new int\[3\]\[2\];}"
} {{[this := @this: foo, a = newmultiarray (int)[3][2]]} {[this, a]} {[]}}

test JavaFragmentParser-3.6 {Allocation statement} {
    doSimpleParseTest "{long a\[\]\[\]\[\]\[\] = new int\[3\]\[2\]\[\]\[\];}"
} {{[this := @this: foo, a = newmultiarray (int)[3][2][][]]} {[this, a]} {[]}}


test JavaFragmentParser-4.1 {.class syntax} {
    doSimpleParseTest "{java.lang.Class theClass = int.class;}"
} {{[this := @this: foo, theClass = java.lang.Int.TYPE]} {[this, theClass]} {[]}}

test JavaFragmentParser-4.2 {.class syntax} {
    doSimpleParseTest "{java.lang.Class theClass = java.lang.Object.class;}"
} {{[this := @this: foo, theClass = java.lang.Class.forName("java.lang.Object")]} {[this, theClass]} {[]}}

test JavaFragmentParser-4.3 {this} {
    doSimpleParseTest "{java.lang.Object object = this;}"
} {{[this := @this: foo, object = this]} {[this, object]} {[]}}

test JavaFragmentParser-4.4 {this method} {
    doSimpleParseTest "{this.bar();}"
} {{[this := @this: foo, virtualinvoke this.<foo: void bar()>()]} {[this]} {[]}}

test JavaFragmentParser-4.5 {plain instance method} {
    doSimpleParseTest "{bar();}"
} {{[this := @this: foo, virtualinvoke this.<foo: void bar()>()]} {[this]} {[]}}
test JavaFragmentParser-4.6 {this field} {
    doSimpleParseTest "{java.lang.Object object = this.baz;}"
} {{[this := @this: foo, object = this.<foo: foo baz>]} {[this, object]} {[]}}

test JavaFragmentParser-4.7 {plain instance field} {
    doSimpleParseTest "{java.lang.Object object = baz;}"
} {{[this := @this: foo, object = this.<foo: foo baz>]} {[this, object]} {[]}}

test JavaFragmentParser-4.8 {instance field} {
    doSimpleParseTest "{java.lang.Object object = baz.baz.baz;}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, $deref = $deref.<foo: foo baz>, object = $deref.<foo: foo baz>]} {[this, object, $deref, $deref]} {[]}}

test JavaFragmentParser-4.9 {instance field and method} {
    doSimpleParseTest "{java.lang.Object object = baz.bar();}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, object = virtualinvoke $deref.<foo: void bar()>()]} {[this, object, $deref]} {[]}}

test JavaFragmentParser-4.10 {instance field and field} {
    doSimpleParseTest "{java.lang.Object object = this.baz.baz;}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, object = $deref.<foo: foo baz>]} {[this, object, $deref]} {[]}}

test JavaFragmentParser-4.11 {instance field and field and method} {
    doSimpleParseTest "{java.lang.Object object = this.baz.baz.bar();}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, $deref = $deref.<foo: foo baz>, object = virtualinvoke $deref.<foo: void bar()>()]} {[this, object, $deref, $deref]} {[]}}

test JavaFragmentParser-4.12 {plain static method} {
    doSimpleParseTest "{bars();}"
} {{[this := @this: foo, staticinvoke <foo: void bars()>()]} {[this]} {[]}}

test JavaFragmentParser-4.13 {static method} {
    doSimpleParseTest "{this.bars();}"
} {{[this := @this: foo, staticinvoke <foo: void bars()>()]} {[this]} {[]}}

test JavaFragmentParser-4.14 {static method} {
    doSimpleParseTest "{this.baz.bars();}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, staticinvoke <foo: void bars()>()]} {[this, $deref]} {[]}}

test JavaFragmentParser-4.15 {static method} {
    doSimpleParseTest "{baz.bars();}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, staticinvoke <foo: void bars()>()]} {[this, $deref]} {[]}}


test JavaFragmentParser-4.16 {plain static field} {
    doSimpleParseTest "{bazs.bar();}"
} {{[this := @this: foo, $deref = <foo: foo bazs>, virtualinvoke $deref.<foo: void bar()>()]} {[this, $deref]} {[]}}

test JavaFragmentParser-4.17 {static field} {
    doSimpleParseTest "{this.bazs.bar();}"
} {{[this := @this: foo, $deref = <foo: foo bazs>, virtualinvoke $deref.<foo: void bar()>()]} {[this, $deref]} {[]}}

test JavaFragmentParser-4.18 {static field} {
    doSimpleParseTest "{this.baz.bazs.bar();}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, $deref = <foo: foo bazs>, virtualinvoke $deref.<foo: void bar()>()]} {[this, $deref, $deref]} {[]}}

test JavaFragmentParser-4.19 {static field} {
    doSimpleParseTest "{baz.bazs.baz.bar();}"
} {{[this := @this: foo, $deref = this.<foo: foo baz>, $deref = <foo: foo bazs>, $deref = $deref.<foo: foo baz>, virtualinvoke $deref.<foo: void bar()>()]} {[this, $deref, $deref, $deref]} {[]}}

