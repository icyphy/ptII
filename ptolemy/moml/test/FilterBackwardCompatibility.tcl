# Tests for the FilterBackwardCompatibility class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set constMoml  "$header 
<entity name=\"FilterBackwardCompatibilityConst\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">
  </entity>
</entity>"

######################################################################
####
#
test FilterBackwardCompatibility-1.1 {Const: added an _icon} { 
    # This test is sort of pointless, since we add the Const _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build
    
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterBackwardCompatibility]

    #$parser addMoMLFilter [java::new ptolemy.moml.filter.AddEditorFactory]
    #$parser addMoMLFilter [java::new ptolemy.moml.filter.AddIcon]
    #$parser addMoMLFilter [java::new ptolemy.moml.filter.PortNameChanges]
    #$parser addMoMLFilter [java::new ptolemy.moml.filter.PropertyClassChanges]

    $parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    set toplevel [$parser parse $constMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityConst" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
</entity>
}}

set mathFunctionMoml  "$header 
<entity name=\"FilterBackwardCompatibilityMathFunction\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"MathFunction\" class=\"ptolemy.actor.lib.MathFunction\">
  </entity>
</entity>"

test FilterBackwardCompatibility-3.1 {MathFunction} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $mathFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityMathFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <entity name="MathFunction" class="ptolemy.actor.lib.MathFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="exp">
        </property>
        <port name="firstOperand" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}


set scaleMoml  "$header 
<entity name=\"FilterBackwardCompatibilityScale\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Scale\" class=\"ptolemy.actor.lib.Scale\">
  </entity>
</entity>"

test FilterBackwardCompatibility-4.1 {Scale} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $scaleMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityScale" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <entity name="Scale" class="ptolemy.actor.lib.Scale">
        <property name="factor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="scaleOnLeft" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}

set trigFunctionMoml  "$header 
<entity name=\"FilterBackwardCompatibilityTrigFunction\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"TrigFunction\" class=\"ptolemy.actor.lib.TrigFunction\">
  </entity>
</entity>"

test FilterBackwardCompatibility-5.1 {TrigFunction} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $trigFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityTrigFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="sin">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}


######################################################################
####
#

set complexToCartesianMoml  "$header 
<entity name=\"FilterBackwardCompatibilityComplextToCartesian\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"ComplexToCartesian1\" class=\"ptolemy.actor.lib.conversions.ComplexToCartesian\">
        <port name=\"input\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"real\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"imag\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
    </entity>
    <entity name=\"CartesianToComplex2\" class=\"ptolemy.actor.lib.conversions.CartesianToComplex\">
        <port name=\"real\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"imag\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
    </entity>
</entity>"

test FilterBackwardCompatibility-6.1 {ComplexToCartesian: port name change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $complexToCartesianMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityComplextToCartesian" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <entity name="ComplexToCartesian1" class="ptolemy.actor.lib.conversions.ComplexToCartesian">
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="x" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="y" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <entity name="CartesianToComplex2" class="ptolemy.actor.lib.conversions.CartesianToComplex">
        <port name="x" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="y" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}


######################################################################
####
#

set htvqEncodeMoml  "$header 
<entity name=\"FilterBackwardCompatibilityComplextToCartesian\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"HTVQEncode1\" class=\"ptolemy.domains.sdf.lib.vq.HTVQEncode\">
        <property name=\"codeBook\" class=\"ptolemy.data.expr.Parameter\" value=\"&quot;/ptolemy/domains/sdf/lib/vq/data/usc_hvq_s5.dat&quot;\">
        </property>
        <property name=\"blockCount\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
        </property>
        <property name=\"blockWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"4\">
        </property>
        <property name=\"blockHeight\" class=\"ptolemy.data.expr.Parameter\" value=\"2\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"244.0, 124.0\">
        </property>
        <port name=\"input\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"input\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
        </port>
        <port name=\"output\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"output\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
        </port>
    </entity>
</entity>"

test FilterBackwardCompatibility-7.1 {HTVQEncode: Property Class Change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $htvqEncodeMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityComplextToCartesian" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <entity name="HTVQEncode1" class="ptolemy.domains.sdf.lib.vq.HTVQEncode">
        <property name="codeBook" class="ptolemy.data.expr.Parameter" value="&quot;/ptolemy/domains/sdf/lib/vq/data/usc_hvq_s5.dat&quot;">
        </property>
        <property name="blockCount" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="blockWidth" class="ptolemy.data.expr.Parameter" value="4">
        </property>
        <property name="blockHeight" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="_location" class="ptolemy.moml.Location" value="244.0, 124.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
    </entity>
</entity>
}}



######################################################################
####
#

set editorFactoryMoml  "$header 
<entity name=\"FilterBackwardCompatibilityEditorFactor\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"lambda\" class=\"ptolemy.data.expr.Parameter\" value=\"25.0\">
        <property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">
        </property>
        <property name=\"lambda\" class=\"ptolemy.vergil.icon.ValueIcon\">
        </property>
        <property name=\"_smallIconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
      <svg>
        <text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">-P-</text>
      </svg>
    </configure>
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"355.0, 200.0\">
        </property>
    </property>
</entity>"

test FilterBackwardCompatibility-8.1 {Is a parameter, does not have _editorFactory} { 

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $editorFactoryMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityEditorFactor" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <property name="lambda" class="ptolemy.data.expr.Parameter" value="25.0">
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="lambda" class="ptolemy.kernel.util.Attribute">
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-P-</text>
      </svg>
    </configure>
        </property>
        <property name="_location" class="ptolemy.moml.Location" value="355.0, 200.0">
        </property>
        <property name="_editorFactory" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</entity>
}}

set annotationMoml  "$header 
<entity name=\"FilterBackwardCompatibilityEditorFactor\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"0:annotation1\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"426.0, 80.0\">
        </property>
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
           <svg>
           <text x=\"20\" style=\"font-size:14; font-family:sanserif; fill:blue\" y=\"20\">This model shows a nonlinear feedback
system that exhibits chaotic behavior.
It is modeled in continuous time. The
CT director uses a sophisticated
ordinary differential equation solver
to execute the model. This particular
model is known as a Lorenz attractor.</text>
           </svg>
           </configure>
        </property>
    </property>
</entity>
"

test FilterBackwardCompatibility-9.1 {annotation named annotation1 without a _hideName} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $annotationMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityEditorFactor" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <property name="0:annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_location" class="ptolemy.moml.Location" value="426.0, 80.0">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
           <svg>
           <text x="20" style="font-size:14; font-family:sanserif; fill:blue" y="20">This model shows a nonlinear feedback
system that exhibits chaotic behavior.
It is modeled in continuous time. The
CT director uses a sophisticated
ordinary differential equation solver
to execute the model. This particular
model is known as a Lorenz attractor.</text>
           </svg>
           </configure>
        </property>
        <property name="_hideName" class="ptolemy.data.expr.Parameter">
        </property>
    </property>
</entity>
}}

test FilterBackwardCompatiblity-10.1 {Try running old models, first check that the makefile created the compat/ directory} { 
    if {! [file exists compat]} {
	error "compat directory does not exist.  This could happen\
		If you do not have access to old Ptolemy II tests"
    } else {
	list 1
    }
} {1} KNOWN_FAILURE

if {[info procs jdkStackTrace] == 1} then {
    source [file join $PTII util testsuite jdkTools.tcl]
}
# createAndExecute a file with a MoMLFilter
proc createAndExecute {file} {
    global KNOWN_FAILED
    if { "$file" == "compat/testAudioReaderAudioPlayer.xml" \
	    || "$file" == "compat/testAudioReader.xml" \
	    || "$file" == "compat/testAudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture_AudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture.xml" \
	    || "$file" == "compat/MaximumEntropySpectrum.xml" \
	    || "$file" == "compat/ArrayAppend.xml" } {
	puts "$file: Skipping Known Failure"
	incr KNOWN_FAILED
	return
    }

    
    #java::new ptolemy.actor.gui.MoMLSimpleApplication $file
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterBackwardCompatibility]
    set namedObj [$parser parseFile $file]
    set toplevel [java::cast ptolemy.actor.CompositeActor $namedObj]

    # DT is a mess, don't bother testing it
    set compositeActor [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$compositeActor getDirector]
    if [java::instanceof \
	    $director ptolemy.domains.dt.kernel.DTDirector] {
	puts "$file: Skipping DT tests, marking as Known Failure"
	incr KNOWN_FAILED
	return
    }

    # Look for comp
    set deepEntityList [$compositeActor deepEntityList]
    for {set i 0} {$i < [$deepEntityList size]} {incr i} {
	set containedActor [$deepEntityList get $i]
	if [java::instanceof $containedActor \
		ptolemy.actor.TypedCompositeActor] {
	    set compositeActor [java::cast ptolemy.actor.CompositeActor \
		    $containedActor]
	    set director [$compositeActor getDirector]
	    if [java::instanceof \
		    $director ptolemy.domains.dt.kernel.DTDirector] {
		puts "$file: Skipping tests with DT inside, marking as Known Failure"
		incr KNOWN_FAILED
		return
	    }
	}
    }


    #set newMoML [$toplevel exportMoML]
    #puts $newMoML

    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "compatibilityChecking"]
    
    $toplevel setManager $manager
    $manager execute

}


# Find all the files in the compat directory


#foreach file [list compat/ComplexToCartesianAndBack.xml compat/testAudioReaderAudioPlayer.xml compat/test1.xml compat/FIR1.xml] {
foreach file [lsort [glob compat/*.xml]] {
    puts "------------------ testing $file"
    test "Auto" "Automatic test in file $file" {
        set application [createAndExecute $file]
        list {}
    } {{}}
    #test "Auto-rerun" "Automatic test rerun in file $file" {
    #	$application rerun
    #	list {}
    #} {{}}
}
#doneTests

