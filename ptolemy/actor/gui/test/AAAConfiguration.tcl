# Tests Configuration
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2012 The Regents of the University of California.
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

# This file is called AAAConfiguration.tcl so that it runs first, before
# other classes start messing with the filters

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test Configuration-1.0 {Read in the configuration} { 
    global configuration
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]

    java::call ptolemy.moml.MoMLParser purgeModelRecord [$configurationURL toString]
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser resetAll

    if {[info vars configuration] == ""} {
	set configuration [java::call ptolemy.actor.gui.MoMLApplication readConfiguration $configurationURL]
    }
    $configuration getFullName
} {.configuration}

######################################################################
####
#
test UserActorLibrary-1.5 {check} {
#    $configuration check
} {}

######################################################################
####
#
test UserActorLibrary-2.0 {configurations} {
    set configurations [$configuration configurations]
    set firstConfiguration [java::cast ptolemy.actor.gui.Configuration \
				[$configurations get 0]]
    list [$configurations size] [$firstConfiguration getFullName]
} {1 .configuration}

######################################################################
####
#
test UserActorLibrary-3.0 {getDirectory} {
    list [[$configuration getDirectory] getFullName]
} {.configuration.directory}


######################################################################
####
#
test UserActorLibrary-4.0 {openModel, findEffigy, showAll} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    $parser purgeModelRecord test.xml
    set entity [java::cast ptolemy.kernel.CompositeEntity \
		    [$parser parseFile test.xml]]
    set tableau [$configuration openModel $entity]
    set effigy1 [$configuration findEffigy $entity]
    set effigy2 [$configuration getEffigy $entity]
    $entity setContainer [java::null]
    $configuration showAll
    list [$tableau getFullName] [$effigy1 getFullName] [$effigy2 getFullName]
} {.configuration.directory.test.simpleTableau .configuration.directory.test .configuration.directory.test}

######################################################################
####
#
test UserActorLibrary-5.0 {setContainer} {
    $configuration setContainer [java::null]
    catch {$configuration setContainer $entity} errMsg
    list [java::isnull [$configuration getContainer]] $errMsg
} {1 {ptolemy.kernel.util.IllegalActionException: Configuration can only be at the top level of a hierarchy.
  in .configuration}}

######################################################################
####
#
test UserActorLibrary-6.0 {showAll} {
    $configuration showAll	
} {}

######################################################################
####
#
test UserActorLibrary-7.0 {_effigyIdentifier} {
    # no _uri attribute
    set namedObj [java::new ptolemy.kernel.util.NamedObj "myNamedObj"]
    set tableau [$configuration openModel $namedObj]	
    set effigy1 [$configuration findEffigy $entity]
    set effigy2 [$configuration getEffigy $entity]
    set namedObj [java::null]	
    list [$tableau getFullName] [$effigy1 getFullName] [$effigy2 getFullName]
} {.configuration.directory.myNamedObj.simpleTableau .configuration.directory.test .configuration.directory.test}


######################################################################
####
#
test UserActorLibrary-8.0 {isModifiable on a model with spaces in the name} {
    # See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=153
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    $parser purgeModelRecord {model with spaces.xml}
    set tableau [$configuration \openModel [java::null] \
		     [java::new java.net.URL \
			  {file:./model%20with%20spaces.xml}] \
		     foo [java::null]]

    set modelDirectory [$configuration getDirectory]
    set effigy1 [$modelDirectory getEffigy foo]
    list [$effigy1 isModifiable]
} {1}

######################################################################
####
# 

set removeClassesMoML {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="removeClassesMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="StaticSchedulingCodeGenerator" class="ptolemy.codegen.kernel.StaticSchedulingCodeGenerator">        <property name="_location" class="ptolemy.kernel.util.Location" value="[220.0, 40.0]">
        </property>
    </property>
    <property name="MyParameter" class="ptolemy.data.expr.Parameter"/>
</entity>
}

test Configuration-9.0 {testConfiguration.xml has classesToRemove set to include removing StaticSchedulingCodeGenerator} {

    # Make sure we have a RemoveClasses element
    set sawRemoveClasses 0
    set momlFilters [java::call ptolemy.moml.MoMLParser getMoMLFilters]
    set filters [$momlFilters iterator]
    while {[$filters hasNext] == 1} {
	set filter [java::cast ptolemy.moml.MoMLFilter [$filters -noconvert next]]
	if [java::instanceof $filter ptolemy.moml.filter.RemoveClasses] {
	    set sawRemoveClasses 1
	}
    }
    if {$sawRemoveClasses != 1} {
	error "Failed to add RemoveClasses to MoMLFilters?"
    }

    set toplevel [$parser parse $removeClassesMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML

} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="removeClassesMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.0.beta">
    </property>
    <property name="MyParameter" class="ptolemy.data.expr.Parameter">
    </property>
</entity>
}}

test Configuration-9.1 {set _classesToRemove to something new} {
    set classesToRemove [java::field $configuration classesToRemove]
    $classesToRemove setExpression "{\"foo.bar\", \"bif.baz\", \"ptolemy.codegen.kernel.StaticSchedulingCodeGenerator\"}"
    
    # Make sure that Configuration.attributeChanged() is called
    
    $classesToRemove validate

    # Reparse our same old example
    set toplevel [$parser parse $removeClassesMoML]
    set newMoML [$toplevel exportMoML]
    # Note that we did not remove StateSchedulingCodeGenerator
    list $newMoML

} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="removeClassesMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <property name="MyParameter" class="ptolemy.data.expr.Parameter">
    </property>
</entity>
}}




set removeGraphicalClassesMoML {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="removeGraphicalClassesMoML" class="ptolemy.actor.TypedCompositeActor">
    <entity name="SequencePlotter"
	        class="ptolemy.actor.lib.gui.SequencePlotter"/>
</entity>
}

test Configuration-10.0 {testConfiguration.xml has removeGraphicalClasses set to true} {
    #[java::cast ptolemy.actor.TypedCompositeActor $toplevel] setContainer [java::null]
    $parser resetAll
    set toplevel2 [$parser parse $removeGraphicalClassesMoML]
    set newMoML [$toplevel2 exportMoML]
    # Note that we removed the SequenceActor is now a discard.
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="removeGraphicalClassesMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="11.0.devel">
    </property>
    <entity name="SequencePlotter" class="ptolemy.moml.filter.DiscardDoubles">
    </entity>
</entity>
}}

test Configuration-10.1 {testConfiguration.xml has removeGraphicalClasses set to true} {
    #[java::cast ptolemy.actor.TypedCompositeActor $toplevel] setContainer [java::null]
    set removeGraphicalClasses [java::field $configuration removeGraphicalClasses]
    $removeGraphicalClasses setExpression "false"
    
    # Make sure that Configuration.attributeChanged() is called
    
    $removeGraphicalClasses validate
    $parser resetAll
    set toplevel2 [$parser parse $removeGraphicalClassesMoML]
    set newMoML [$toplevel2 exportMoML]
    # Note that we removed the SequenceActor is now a discard.
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="removeGraphicalClassesMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.0.beta">
    </property>
    <entity name="SequencePlotter" class="ptolemy.actor.lib.gui.SequencePlotter">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute">
        </property>
    </entity>
</entity>
}}

