# Test TemplateParser
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2011 The Regents of the University of California.
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
test TemplateParser-1.1 {Test processCode with $country once} {
    set code {new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    $templateParser processCode $code
} {new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");}

#####
test TemplateParser-1.1.2 {Test processCode on $actorSymbol(step) once} {
    set code {$actorSymbol(step)}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    set toplevel1_1_2 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp [java::cast ptolemy.kernel.CompositeEntity $toplevel1_1_2] MyRamp]
    set codeGeneratorAdapter [java::new ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter $ramp]
		       
    $templateParser init $ramp $codeGeneratorAdapter
    $templateParser setCodeGenerator \
	[java::new ptolemy.cg.kernel.generic.program.ProgramCodeGenerator \
	     $toplevel1_1_2 myCodeGenerator .j .j]
    $templateParser processCode $code
} {_MyRamp__step}

#####
test TemplateParser-1.1.3.1 {Test processCode with $country twice} {
    set code {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    $templateParser processCode $code
} {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}

#####
test TemplateParser-1.1.3.2 {Test processCode with $country three times} {
    set code {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");
new ptolemy.actor.TypeAttribute("$country_c_*_*_c_p_p", "inputType");}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    $templateParser processCode $code
} {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");
new ptolemy.actor.TypeAttribute("$country_c_*_*_c_p_p", "inputType");}

#####
test TemplateParser-1.1.3.5 {Test processCode on $country, $actorSymbol, $country} {
    set code {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
$actorSymbol(step)
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    set toplevel1_1_2 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp [java::cast ptolemy.kernel.CompositeEntity $toplevel1_1_2] MyRamp]
    set codeGeneratorAdapter [java::new ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter $ramp]
		       
    $templateParser init $ramp $codeGeneratorAdapter
    $templateParser setCodeGenerator \
	[java::new ptolemy.cg.kernel.generic.program.ProgramCodeGenerator \
	     $toplevel1_1_2 myCodeGenerator .j .j]
    $templateParser processCode $code
} {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
_MyRamp__step
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}

#####
test TemplateParser-1.1.4 {Test processCode on $country, ${foo}, $country} {
    set code {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
${fooParameter}
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    set workspace1_1_3 [java::new ptolemy.kernel.util.Workspace]
    set toplevel1_1_3 [java::new ptolemy.actor.TypedCompositeActor $workspace1_1_3]
    set fooParameter [java::new ptolemy.data.expr.Parameter $toplevel1_1_3 {fooParameter}]
    $fooParameter setExpression 42
    set ramp [java::new ptolemy.actor.lib.Ramp [java::cast ptolemy.kernel.CompositeEntity $toplevel1_1_3] MyRamp]
    set codeGeneratorAdapter [java::new ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter $ramp]
		       
    $templateParser init $ramp $codeGeneratorAdapter
    $templateParser setCodeGenerator \
	[java::new ptolemy.cg.kernel.generic.program.ProgramCodeGenerator \
	     $toplevel1_1_3 myCodeGenerator .j .j]
    $templateParser processCode $code
} {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
42
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}

#####
test TemplateParser-1.1.5 {Test processCode on $country, \$, $country} {
    set code {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
String bar = "\$foo";
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    set workspace1_1_3 [java::new ptolemy.kernel.util.Workspace]
    set toplevel1_1_3 [java::new ptolemy.actor.TypedCompositeActor $workspace1_1_3]
    set ramp [java::new ptolemy.actor.lib.Ramp [java::cast ptolemy.kernel.CompositeEntity $toplevel1_1_3] MyRamp]
    set codeGeneratorAdapter [java::new ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter $ramp]
		       
    $templateParser init $ramp $codeGeneratorAdapter
    $templateParser setCodeGenerator \
	[java::new ptolemy.cg.kernel.generic.program.ProgramCodeGenerator \
	     $toplevel1_1_3 myCodeGenerator .j .j]
    $templateParser processCode $code
} {
new ptolemy.actor.TypeAttribute("$country_a_*_*_a_p_p", "inputType");
String bar = "\$foo";
new ptolemy.actor.TypeAttribute("$country_b_*_*_b_p_p", "inputType");}

#####
test TemplateParser-2.1 {Test processCode on ::} {
    set code {
Token Scale_scaleOnRight(Token input, double factor) {
return $tokenFunc(input::multiply($new(Double(factor))));
}
}
    set templateParser [java::new ptolemy.cg.kernel.generic.program.TemplateParser]
    set workspace1_1_3 [java::new ptolemy.kernel.util.Workspace]
    set toplevel1_1_3 [java::new ptolemy.actor.TypedCompositeActor $workspace1_1_3]
    set ramp [java::new ptolemy.actor.lib.Ramp [java::cast ptolemy.kernel.CompositeEntity $toplevel1_1_3] MyRamp]
    set codeGeneratorAdapter [java::new ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter $ramp]
		       
    $templateParser init $ramp $codeGeneratorAdapter
    $templateParser setCodeGenerator \
	[java::new ptolemy.cg.kernel.generic.program.ProgramCodeGenerator \
	     $toplevel1_1_3 myCodeGenerator .j .j]
    $templateParser processCode $code
} {
Token Scale_scaleOnRight(Token input, double factor) {
return functionTable[(int)input.type][FUNC_multiply](input, null);
}
}



