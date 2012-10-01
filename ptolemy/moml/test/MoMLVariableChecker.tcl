# Tests for the MoMLVariableChecker class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007-2012 The Regents of the University of California.
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

if {[string compare sdfModel [info procs getParameterl]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

# Parse the moml in a workspace with the given name.
# Return the toplevel
proc parseMoML {moml workspaceName } {
    set w [java::new ptolemy.kernel.util.Workspace $workspaceName]
    set parser [java::new ptolemy.moml.MoMLParser $w]
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]


    set toplevel [java::cast ptolemy.actor.CompositeActor \
		      [$parser parse $moml]]
    return $toplevel
}

# Invoke a change request and get a token.
# This proc is used to test cut and paste.
proc changeAndGetToken {toplevel changeRequestString \
			    {entityName {Const}} {attributeName {value}}} {
    set changeRequest [java::new ptolemy.moml.MoMLChangeRequest \
			   $toplevel $toplevel $changeRequestString]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] \
		     "myManager"]
    $toplevel setManager $manager
    $manager requestChange $changeRequest
    set const [$toplevel getEntity $entityName]

    if [java::isnull $const] {
	puts "Warning: changeAndGetToken: $toplevel getEntity $entityName returned null?"
	return [java::null]
    }
    if [java::isnull [$const getAttribute $attributeName]] {
	puts "Warning: changeAndGetToken: $const getAttribute $attributeName returned null?"
	return [java::null]
    }
    set value [java::cast ptolemy.data.expr.Variable \
		   [$const getAttribute $attributeName]]
    return [$value getToken]
}


# Below are MoML fragments that are used for testing.

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set entityStart {
<entity name="paramCopy" class="ptolemy.actor.TypedCompositeActor">
}

set myParam {
    <property name="myParam" class="ptolemy.data.expr.Parameter" value="1">
    </property>
}

set paramCopyConst {
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="myParam">
        </property>
    </entity>
}

set baseModel2 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
</entity>
}

######################################################################
####
#
test MoMLVariableChecker-1.1 {copy a const that refers another parameter } {
    set toplevel1 [parseMoML $baseModel2 w1_1]

    # Create a change request: a const that refers to missing param 

    catch {changeAndGetToken $toplevel1 \
	       "<group name=\"auto\">$paramCopyConst</group>"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: myParam
  in .top.Const.value
Because:
The ID myParam is undefined.}}

######################################################################
####
#
test MoMLVariableChecker-1.2.1 {copy a const that refers another parameter } {
    set moml1_2 "$header $entityStart $myParam $paramCopyConst </entity>"
    set toplevel1_2 [parseMoML $moml1_2 w1_2]

    set variableChecker [java::new ptolemy.moml.MoMLVariableChecker]
    set copyMoML [$variableChecker checkCopy $paramCopyConst $toplevel1_2]
} {<property createIfNecessary="true" name="myParam" class="ptolemy.data.expr.Parameter" value="1">
</property>
}

######################################################################
####
#
test MoMLVariableChecker-1.2.2 {Simulate paste with the myParam variable defined in the cut buffer } {
    # no myParam here
    set moml1_2_2 "$header $entityStart</entity>"
    set toplevel1_2_2 [parseMoML $moml1_2_2 w1_2_2]

    # Uses copyMoML from 1.2.1 above
    set value [changeAndGetToken $toplevel1_2_2 "<group name=\"auto\">$copyMoML $paramCopyConst\n</group>"]
    list [$value toString]
} {1}


######################################################################
####
#
test MoMLVariableChecker-1.2.3 {Simulate paste with the myParam variable defined in the cut buffer and in the destination } {
    # myParam is defined to be 42
    set myParam42 {
	<property name="myParam" class="ptolemy.data.expr.Parameter" value="42">
	</property>
    }
    set moml1_2_3 "$header $entityStart $myParam42</entity>"
    set toplevel1_2_3 [parseMoML $moml1_2_3 w1_2_3]

    # Uses copyMoML from 1.2.1 above
    # Even though copyMoML sets myParam to 1, we ignore that and use
    # the prefined value of 42
    set value [changeAndGetToken $toplevel1_2_3 "<group name=\"auto\">$copyMoML $paramCopyConst\n</group>"]
    if [java::isnull $value] {
	list [$toplevel1_2_3 exportMoML] $copyMoML $paramCopyConst
    } else {
	list [$value toString]
    }
} {42}


######################################################################
####
# 

set myOtherParam1_3 {
    <property name="myOtherParam" class="ptolemy.data.expr.Parameter" value="2">
    </property>
}

set paramCopyConst1_3 {
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="myParam + myOtherParam">
        </property>
    </entity>
}

test MoMLVariableChecker-1.3.1 {copy a const that refers another parameter } {
    set moml1_3 "$header $entityStart $myParam $myOtherParam1_3 $paramCopyConst1_3 </entity>"
    set toplevel1_3 [parseMoML $moml1_3 w1_3]

    set variableChecker1_3 [java::new ptolemy.moml.MoMLVariableChecker]
    set copyMoML1_3 [$variableChecker checkCopy $paramCopyConst1_3 $toplevel1_3]
    list $copyMoML1_3
} {{<property createIfNecessary="true" name="myParam" class="ptolemy.data.expr.Parameter" value="1">
</property>
<property createIfNecessary="true" name="myOtherParam" class="ptolemy.data.expr.Parameter" value="2">
</property>
}}


######################################################################
####
#
test MoMLVariableChecker-1.3.2 {Simulate paste with the myParam variable defined} {
    set moml1_3_2 "$header $entityStart  </entity>"
    set toplevel1_3_2 [parseMoML $moml1_3_2 w1_3_2]

    # Uses copyMoML1_3 from 1.3.1 above
    set value [changeAndGetToken $toplevel1_3_2 \
		   "<group name=\"auto\">$copyMoML1_3 $paramCopyConst1_3\n</group>"]
    list [$value toString]
} {3}

######################################################################
####
# 

set innerEntity1_4 {
<entity name="innerParamCopy" class="ptolemy.actor.TypedCompositeActor">
}

set myOtherParam {
    <property name="myOtherParam" class="ptolemy.data.expr.Parameter" value="2">
    </property>
}

set paramCopyConst1_4 {
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="myParam + myOtherParam">
        </property>
    </entity>
}

test MoMLVariableChecker-1.4.1 {copy a const that refers two parameters, one of which is in the container} {
    set moml1_4 "$header $entityStart $myParam $innerEntity1_4 $myOtherParam $paramCopyConst1_4 </entity> </entity>"
    set toplevel1_4 [parseMoML $moml1_4 w1_4]

    set variableChecker1_4 [java::new ptolemy.moml.MoMLVariableChecker]
    set copyMoML1_4 [$variableChecker1_4 checkCopy \
		      "$innerEntity1_4 $paramCopyConst1_4 </entity>" $toplevel1_4]
    list $copyMoML1_4
} {{<property createIfNecessary="true" name="myParam" class="ptolemy.data.expr.Parameter" value="1">
</property>
<property createIfNecessary="true" name="myOtherParam" class="ptolemy.data.expr.Parameter" value="2">
</property>
}}

######################################################################
####
#
test MoMLVariableChecker-1.4.2 {Simulate paste with the myParam variable defined} {
    set myOtherParam42 {<property name="myOtherParam" class="ptolemy.data.expr.Parameter" value="42"/>}
    set moml1_4_2 "$header $entityStart  $myOtherParam42 </entity>"
    set toplevel1_4_2 [parseMoML $moml1_4_2 w1_4_2]

    # Uses copyMoML1_4 from 1.4.1 above
    set clipBoard  "<group name=\"auto\">$copyMoML1_4 $innerEntity1_4 $paramCopyConst1_4 </entity>\n</group>"
    set value [changeAndGetToken $toplevel1_4_2 \
		   $clipBoard \
		   innerParamCopy.Const]
	
    list [$value toString]
} {43}


######################################################################
####
# 


set paramCopySubscriber1_5 {
    <entity name="Subscriber" class="ptolemy.actor.lib.Subscriber">
        <property name="channel" class="ptolemy.data.expr.StringParameter" value="$myParam">
        </property>
    </entity>
}

test MoMLVariableChecker-1.5.1 {copy a Subscriber that refers to a channel via a var} {
    set moml1_5 "$header $entityStart $myParam $paramCopySubscriber1_5 </entity>"
    set toplevel1_5 [parseMoML $moml1_5 w1_5]

    set variableChecker1_5 [java::new ptolemy.moml.MoMLVariableChecker] 
    set copyMoML1_5 [$variableChecker1_5 checkCopy \
		      "$paramCopySubscriber1_5" $toplevel1_5]
    list $copyMoML1_5
} {{<property createIfNecessary="true" name="myParam" class="ptolemy.data.expr.Parameter" value="1">
</property>
}}

######################################################################
####
#
test MoMLVariableChecker-1.5.2 {Simulate paste with the myParam variable defined} {
    set myParam1_5_2 {
	<property name="myParam" class="ptolemy.data.expr.Parameter" value="152">
	</property>
    }  
    set moml1_5_2 "$header $entityStart $myParam1_5_2 </entity>"
    set toplevel1_5_2 [parseMoML $moml1_5_2 w1_5_2]

    # Uses copyMoML1_5 from 1.4.1 above
    set clipBoard  "<group name=\"auto\">$copyMoML1_5 $paramCopySubscriber1_5\n</group>"
    set value [changeAndGetToken $toplevel1_5_2 \
		   $clipBoard  Subscriber channel]
	
    list [$value toString]
} {{"152"}}

######################################################################
####
# 
test MoMLVariableChecker-2.0 {Try to paste a class that does not exist} {
    set instance2_0 {
	<entity name="ConstClassInstance" class="ConstClassDefinition">
	</entity>
    }
    set toplevel2_0b [parseMoML $baseModel2 w2_0b]

    # This fails because there is no actorclass AddSubtractClassClassDefinition
    catch {changeAndGetToken $toplevel2_0b \
	       "<group name=\"auto\">$instance2_0</group>"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.InternalErrorException: ChangeRequest failed (NOTE: there is no ChangeListener):
<group name="auto">
	<entity name="ConstClassInstance" class="ConstClassDefinition">
	</entity>
    </group>
  in .top
Because:
Attempt to extend an entity that is not a class: .ConstClassDefinition className: ConstClassDefinition entityName: ConstClassInstance source: null in [external stream] at line 2 and column 54}}

######################################################################
####
# 
test MoMLVariableChecker-2.1 {copy a class that _does_ exist} {
    # Uses instance2_0 from above
    set w [java::new ptolemy.kernel.util.Workspace w2_1a]
    set parser [java::new ptolemy.moml.MoMLParser $w]

    set toplevelClassDefinition2_1 [java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile ConstClassDefinition.xml]]

    set moml2_1b "$header $entityStart $myParam $paramCopyConst </entity>"
    set toplevel2_1b [parseMoML $moml2_1b w2_1b]

    # This works because the class is defined in $toplevelClassDefinition2_1
    set results [changeAndGetToken $toplevelClassDefinition2_1 \
	       "<group name=\"auto\">$instance2_0</group>" \
	       ConstClassDefinition.Const]
    list [$results toString]
} {4242}

######################################################################
####
# 
test MoMLVariableChecker-2.2 {copy a class that does not exist} {
    # Uses instance2_0 from 2.0 above
    set w [java::new ptolemy.kernel.util.Workspace w2_2a]
    set parser [java::new ptolemy.moml.MoMLParser $w]

    # We copy from here
    set toplevelClassDefinition2_2 [java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile ConstClassDefinition.xml]]

    
    # We paste to here, which does not have the class definition
    set moml2_2b "$header $entityStart $myParam $paramCopyConst </entity>"
    set toplevel2_2b [parseMoML $moml2_2b w2_2b]

    set variableChecker2_2 [java::new ptolemy.moml.MoMLVariableChecker]

    # We did not find the missing class, so copyMoML is empty
    set copyMoML [$variableChecker2_2 checkCopy "<group name=\"auto\">$instance2_0</group>" $toplevel2_2b]
} {}

######################################################################
####
# 
test MoMLVariableChecker-2.3 {copy a class that does exist} {
    # Uses instance2_0 from 2.0 above
    set w [java::new ptolemy.kernel.util.Workspace w2_3a]
    set parser [java::new ptolemy.moml.MoMLParser $w]

    # We copy from here
    set toplevelClassDefinition2_3 [java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile ConstClassDefinition.xml]]

    # We paste to here, which does not have the class definition
    set moml2_3b "$header $entityStart $myParam $paramCopyConst </entity>"
    set toplevel2_3b [parseMoML $moml2_3b w2_3b]

    set variableChecker2_3 [java::new ptolemy.moml.MoMLVariableChecker]

    # We _did_ find the missing class!
    set copyMoML [$variableChecker2_3 checkCopy "<group name=\"auto\">$instance2_0</group>" $toplevelClassDefinition2_3]
} {<class createIfNecessary="true" name="ConstClassDefinition" extends="ptolemy.actor.TypedCompositeActor">
    <property name="_location" class="ptolemy.kernel.util.Location" value="{185, 90}">
    </property>
    <port name="port" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[515.0, 160.0]">
        </property>
    </port>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="4242">
        </property>
        <doc>Create a constant sequence.</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{200, 150}">
        </property>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="port" relation="relation"/>
    <link port="Const.output" relation="relation"/>
</class>
}


######################################################################
####
# 
test MoMLVariableChecker-2.3.2 {copy a class that does exist to a top level that  does not have the class} {
    set moml2_3_2 "$header $entityStart</entity>"
    set toplevel2_3_2 [parseMoML $moml2_3_2 w2_3_2]

    # Uses instance2_0 from 2.0 above and copyMoML from 2.3
    set results [changeAndGetToken $toplevel2_3_2 \
	       "<group name=\"auto\">$copyMoML $instance2_0</group>" \
	       ConstClassDefinition.Const]
    list [$results toString]
} {4242}



######################################################################
####
# 
test MoMLVariableChecker-2.3.3 {copy a class that does exist to a top level that _does_ have the class} {

    # Uses instance2_0 from 2.0 above
    set w [java::new ptolemy.kernel.util.Workspace w2_3a]
    set parser [java::new ptolemy.moml.MoMLParser $w]
    java::call ptolemy.moml.MoMLParser purgeModelRecord ConstClassDefinition.xml

    #set moml2_3_3 "$header $entityStart</entity>"
    #set toplevel2_3_3 [parseMoML $moml2_3_3 w2_3_3]

    set toplevelClassDefinition2_3_3 [java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile ConstClassDefinition.xml]]

    # Change ConstClassDefinition.Const to 666
    set const [$toplevelClassDefinition2_3_3 getEntity \
		   ConstClassDefinition.Const]
    set value [getParameter $const value]
    $value setExpression 666


    # Uses instance2_0 from 2.0 above and copyMoML from 2.3
    set results [changeAndGetToken $toplevelClassDefinition2_3_3 \
	       "<group name=\"auto\">$copyMoML $instance2_0</group>" \
	       ConstClassDefinition.Const]

    java::call ptolemy.moml.MoMLParser purgeModelRecord ConstClassDefinition.xml

    list [$results toString]
} {666}


######################################################################
####
# 
test MoMLVariableChecker-2.3.4 {copy a class that does exist to a top level that _does_ have the class, but with a different definition} {

    # Uses instance2_0 from 2.0 above
    set w [java::new ptolemy.kernel.util.Workspace w2_3a]
    set parser [java::new ptolemy.moml.MoMLParser $w]
    java::call ptolemy.moml.MoMLParser purgeModelRecord ConstClassDefinition.xml

    #set moml2_3_4 "$header $entityStart</entity>"
    #set toplevel2_3_4 [parseMoML $moml2_3_4 w2_3_4]

    set toplevelClassDefinition2_3_4 [java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile ConstClassDefinition.xml]]

    # Change ConstClassDefinition.Const to 666
    set const [$toplevelClassDefinition2_3_4 getEntity \
		   ConstClassDefinition.Const]
    set value [getParameter $const value]
    $value setExpression 666

    # Reread the class
    $parser reset
    java::call ptolemy.moml.MoMLParser purgeModelRecord ConstClassDefinition.xml
    set toplevelClassDefinition2_3_4b \
	[java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile ConstClassDefinition.xml]]

    # Change ConstClassDefinition.Const to 777
    set const [$toplevelClassDefinition2_3_4b getEntity \
		   ConstClassDefinition.Const]
    set value [getParameter $const value]
    $value setExpression 777

    # Uses instance2_0 from 2.0 above and copyMoML from 2.3
    set results [changeAndGetToken $toplevelClassDefinition2_3_4b  \
	       "<group name=\"auto\">$copyMoML $instance2_0</group>" \
	       ConstClassDefinition.Const]

    # Make sure that we have only one class definition 
    set classDefinitions [$toplevelClassDefinition2_3_4b classDefinitionList]

    java::call ptolemy.moml.MoMLParser purgeModelRecord ConstClassDefinition.xml

    list [$classDefinitions size] [$results toString]
} {1 777}

######################################################################
####
# 
test MoMLVariableChecker-3.0 {copy a composite that has an expression that refers to a top-level parameter} {
    set w [java::new ptolemy.kernel.util.Workspace w3_0]
    set parser [java::new ptolemy.moml.MoMLParser $w]
    java::call ptolemy.moml.MoMLParser purgeModelRecord CompositeCopyAndPasteTest.xml

    set toplevel3_0 [java::cast ptolemy.actor.CompositeActor \
			 [$parser parseFile CompositeCopyAndPasteTest.xml]]
    set compositeActorA [$toplevel3_0 getEntity CompositeActorA]

    set compositeActorAMoML [$compositeActorA exportMoML]
    set variableChecker3_0 [java::new ptolemy.moml.MoMLVariableChecker]
    set copyMoML3_0 [$variableChecker3_0 checkCopy $compositeActorAMoML $toplevel3_0]
  
} {<property createIfNecessary="true" name="ParameterP" class="ptolemy.data.expr.Parameter" value="42">
    <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
    </property>
    <property name="_icon" class="ptolemy.kernel.util.Attribute">
        <property name="_color" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 1.0, 1.0}">
        </property>
    </property>
    <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
        <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-P-</text>
      </svg>
    </configure>
    </property>
    <property name="_editorFactory" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="_location" class="ptolemy.kernel.util.Location" value="[150.0, 80.0]">
    </property>
</property>
}

######################################################################
####
# 
test MoMLVariableChecker-3.1 {copy a composite that has an expression that refers to a top-level parameter} {
    set w [java::new ptolemy.kernel.util.Workspace w3_1]
    set parser [java::new ptolemy.moml.MoMLParser $w]
    java::call ptolemy.moml.MoMLParser purgeModelRecord ComplexCompositeCopyAndPasteTest.xml

    set toplevel3_1 [java::cast ptolemy.actor.CompositeActor \
			 [$parser parseFile \
			      ComplexCompositeCopyAndPasteTest.xml]]
    set compositeActorA [java::cast ptolemy.actor.CompositeActor [$toplevel3_1 getEntity CompositeActorA]]
    set compositeActorAB [$compositeActorA getEntity CompositeActorAB]

    set compositeActorABMoML [$compositeActorAB exportMoML]
    set variableChecker3_1 [java::new ptolemy.moml.MoMLVariableChecker]
    set copyMoML3_1 [$variableChecker3_1 checkCopy $compositeActorABMoML $compositeActorA]
  
    set moml3_1  "$header $entityStart $copyMoML3_1 </entity>"
    set copy [parseMoML $moml3_1 w3_1b]
    list \
	[[$copy getAttribute ParameterP] getName] \
	[[$copy getAttribute ParameterAP] getName] \
	[[$copy getAttribute ParameterP2] getName] \
	[[$copy getAttribute ParameterP3] getName]

} {ParameterP ParameterAP ParameterP2 ParameterP3}


######################################################################
####
# 
test MoMLVariableChecker-3.2 {copy an expression in an opaque composite to an adjacent opaque composite} {
    set w [java::new ptolemy.kernel.util.Workspace w3_2]
    set parser [java::new ptolemy.moml.MoMLParser $w]
    java::call ptolemy.moml.MoMLParser purgeModelRecord CompositeOpaqueCopyAndPasteTest.xml

    set toplevel3_2 [java::cast ptolemy.actor.CompositeActor \
			 [$parser parseFile \
			      CompositeOpaqueCopyAndPasteTest.xml]]
    set compositeActorA [java::cast ptolemy.actor.CompositeActor [$toplevel3_2 getEntity CompositeActorA]]
    set expressionA [$compositeActorA getEntity Expression]

    # Copy
    set expressionAMoML [$expressionA exportMoML]
    set variableChecker3_2 [java::new ptolemy.moml.MoMLVariableChecker]
    set copyMoML3_2 [$variableChecker3_2 checkCopy $expressionAMoML $compositeActorA]
    #puts "copyMoML3_2:"
    #puts "$copyMoML3_2"
    
    # Paste.
    # The bug was that when do the paste in an Opaque Composite,
    # if createIfNecessary is true, then we should look up the hierarchy
    # for values declared outside the container.
    set compositeActorA2 [java::cast ptolemy.actor.CompositeActor [$toplevel3_2 getEntity CompositeActorA2]]
    set moml3_2  "$header <group name=\"auto\">
         $copyMoML3_2
         $expressionAMoML
</group>" 
    set changeRequest [java::new ptolemy.moml.MoMLChangeRequest \
			   $compositeActorA2 $compositeActorA2 $moml3_2]
    set manager [java::new ptolemy.actor.Manager [$toplevel3_2 workspace] \
		     "myManager"]
    $toplevel3_2 setManager $manager
    $compositeActorA2 requestChange $changeRequest

    # Link the new Expression actor
    set r10 [$compositeActorA2 connect \
		 [$compositeActorA2 getPort port] \
		 [[$compositeActorA2 getEntity Expression] getPort input]]
    set r11 [$compositeActorA2 connect \
		 [$compositeActorA2 getPort port2] \
		 [[$compositeActorA2 getEntity Expression] getPort output]]

    # Change the top parameter.  CompositeActorA2 should not have
    # a copy of the parameters so the values going to the Test actor from
    # the composites should be the same for both composites.
    set topParameterLetter [java::cast ptolemy.data.expr.Parameter [$toplevel3_2 getAttribute ParameterEndsInLetter]]
    $topParameterLetter setExpression 50

    set topParameterNumber [java::cast ptolemy.data.expr.Parameter [$toplevel3_2 getAttribute ParameterEndsInNumber1]]
    $topParameterNumber setExpression 30

    $manager execute
} {}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
