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

test FilterBackwardCompatibility-1.1 {Const} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterBackwardCompatibility]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    set toplevel [$parser parse $constMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityConst" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel"/>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="value">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="40">
            </property>
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
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $mathFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityMathFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel"/>
    <entity name="MathFunction" class="ptolemy.actor.lib.MathFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="exp">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="function">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="6">
            </property>
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
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $scaleMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityScale" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel"/>
    <entity name="Scale" class="ptolemy.actor.lib.Scale">
        <property name="factor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="scaleOnLeft" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="factor">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="6">
            </property>
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
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $trigFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="FilterBackwardCompatibilityTrigFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel"/>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="sin">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="function">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="6">
            </property>
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


test FilterBackwardCompatiblity-10.1 {Try running old models, first check that the makefile created the compat/ directory} { 
    if {! [file exists compat]} {
	error "compat directory does not exist.  This could happen\
		If you do not have access to old Ptolemy II tests"
    } else {
	list 1
    }
} {1}

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

