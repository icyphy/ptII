# Tests for the UserActorLibrary
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2009 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# The list of filters is static, so we reset it in case there
# filters were already added.
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

java::call ptolemy.moml.MoMLParser addMoMLFilters \
    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

# Remove ptolemy.vergil.icon.BoxedValueIcon
java::call ptolemy.moml.filter.RemoveGraphicalClasses initialize
set filter [java::new ptolemy.moml.filter.RemoveGraphicalClasses]
java::call ptolemy.moml.MoMLParser addMoMLFilter $filter



test UserActorLibrary-0.1 {Read in the configuration} { 
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]

    if {[info vars configuration] == ""} {
	set configuration [java::call ptolemy.actor.gui.MoMLApplication readConfiguration $configurationURL]
    }
    $configuration getFullName
} {.configuration}

#
# Test the UserActorLibrary.saveComponentInLibrary() method 
#
proc testSaveComponentInLibrary { modelFile configuration } { 
    # Set the user library to something temporary
    set userLibraryName testUserActorLibrary_OK_2_DELETE
    java::field ptolemy.actor.gui.UserActorLibrary \
    	USER_LIBRARY_NAME $userLibraryName
    set libraryName "[java::call ptolemy.util.StringUtilities preferencesDirectory]${userLibraryName}.xml"
    file delete -force $libraryName
    if [file exists $libraryName] {
	error "$libraryName exists"
    } 

    java::call ptolemy.actor.gui.UserActorLibrary openUserLibrary \
	$configuration

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    $parser purgeModelRecord $modelFile
    set entity [$parser parseFile $modelFile]

    java::call ptolemy.actor.gui.UserActorLibrary \
	saveComponentInLibrary \
	$configuration $entity


    # Save the library
    set libraryInstance [$configuration getEntity "actor library.$userLibraryName"]
    set libraryTableau [$configuration openModel $libraryInstance]
    set libraryEffigy [java::cast ptolemy.actor.gui.PtolemyEffigy \
    			   [$libraryTableau getContainer]]
    set file [$libraryEffigy getWritableFile]
    $libraryEffigy writeFile $file

    # Read in the library and check it
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser purgeModelRecord [$file toURL]
    set readbackEntity [$parser {parse java.net.URL java.net.URL} \
			    [java::null] [$file toURL]]
    return $readbackEntity
}

######################################################################
####
#
test UserActorLibrary-1.0 {} {
    [testSaveComponentInLibrary test.xml $configuration] exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="testUserActorLibrary_OK_2_DELETE" extends="ptolemy.moml.EntityLibrary">
    <configure>
        <group>
            <entity name="test" class="ptolemy.actor.TypedCompositeActor">
                <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
                </property>
                <doc>This test has no placeable elements, but writes to standard out.</doc>
                <property name="step" class="ptolemy.data.expr.Parameter" value="1">
                </property>
                <property name="director" class="ptolemy.domains.sdf.kernel.SDFDirector">
                    <property name="iterations" class="ptolemy.data.expr.Parameter" value="3">
                    </property>
                    <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
                    </property>
                </property>
                <entity name="ramp" class="ptolemy.actor.lib.Ramp">
                    <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
                    </property>
                    <property name="init" class="ptolemy.data.expr.Parameter" value="0">
                    </property>
                    <property name="step" class="ptolemy.actor.parameters.PortParameter" value="step">
                    </property>
                </entity>
                <entity name="rec" class="ptolemy.actor.lib.Recorder">
                    <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
                    </property>
                </entity>
                <relation name="r1" class="ptolemy.actor.TypedIORelation">
                    <property name="width" class="ptolemy.data.expr.Parameter" value="1">
                    </property>
                </relation>
                <link port="ramp.output" relation="r1"/>
                <link port="rec.input" relation="r1"/>
            </entity>
        </group>
    </configure>
</class>
}

######################################################################
####
#
test UserActorLibrary-1.2 {Sinewave, which is a class} {

    set parser [java::new ptolemy.moml.MoMLParser]

    set entityLibrary [java::cast ptolemy.moml.EntityLibrary \
			   [testSaveComponentInLibrary \
				../../lib/Sinewave.xml $configuration]]
    set restoredEntity [$entityLibrary getEntity Sinewave]
    

    set entity [$parser parseFile ../../lib/Sinewave.xml]
    set entityMoML [$entity exportMoML]

    # Get rid of the header
    # The nightly build changes the version number, so we use
    # ptFilterOutVersion
    set results [ptFilterOutVersion [string range $entityMoML 153 \
			       [string length $entityMoML]] \
		     [$restoredEntity exportMoML]]
    list $results	
} {0}


######################################################################
####
#
test UserActorLibrary-1.3 {model.xml, which has problems with hideName} {

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set entity [java::cast ptolemy.kernel.CompositeEntity \
		    [$parser parseFile hideNameTestModel.xml]]
    set entity2 [$entity getEntity CompositeActor]
    java::call ptolemy.actor.gui.UserActorLibrary \
	saveComponentInLibrary \
	$configuration $entity2

} {}

######################################################################
####
#
test UserActorLibrary-1.4 {Try to assign to a Singleton. ComponentEntity._checkContainer() was throwing an exception, which was masking the real error  } {

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set handler [java::new ptolemy.util.MessageHandler]
    java::call ptolemy.util.MessageHandler setMessageHandler $handler
    java::call ptolemy.actor.gui.UserActorLibrary  saveComponentInLibrary  $configuration $entity2
} {}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
