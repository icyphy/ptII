# Tests for the UserActorLibrary
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

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



# Set the user library to something temporary
set userLibraryName testUserActorLibrary_OK_2_DELETE



set parser [java::new ptolemy.moml.MoMLParser]
$parser reset

# Set the error handler so that we can query for errors
set errorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
$parser setErrorHandler [java::null]

# Set the message handler so that we can determine if the change request succeeded
set messageHandler [java::new ptolemy.util.test.RecorderMessageHandler]
java::call ptolemy.util.MessageHandler setMessageHandler $messageHandler



test UserActorLibrary-0.1 {Read in the configuration} { 
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]

    
    if {[info vars configuration] != ""} {
	# StringUtilities.exit() checks to see if this property is has a length > 0
	java::call System setProperty ptolemy.ptII.doNotExit {}
	#java::call System clearProperty ptolemy.ptII.doNotExit

	# The Manager uses this property to help us test the Exit actor.
	java::call System setProperty ptolemy.ptII.exitAfterWrapup true

	$configuration setContainer [java::null]

	# Reset the property
	java::call System setProperty ptolemy.ptII.doNotExit true

    }
    set configuration [java::call ptolemy.actor.gui.MoMLApplication readConfiguration $configurationURL]
    # Open one model so that we don't exit.
    $parser purgeModelRecord test.xml
    set entity [java::cast ptolemy.kernel.CompositeEntity \
		    [$parser parseFile test.xml]]
    set tableau [$configuration openModel $entity]

    $configuration getFullName
} {.configuration}



#
# Test the UserActorLibrary.saveComponentInLibrary() method by saving a file. 
#
proc testSaveFileInLibrary { modelFile configuration } { 
    global userLibraryName

    resetUserLibrary $configuration $userLibraryName

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    $parser purgeModelRecord $modelFile
    set entity [$parser parseFile $modelFile]

    return [testSaveComponentInLibrary $entity $configuration $userLibraryName]
}

#
# Test the UserActorLibrary.saveComponentInLibrary() method by saving an entity.
#
proc testSaveComponentInLibrary {entity configuration userLibraryName} {
    #java::call ptolemy.actor.gui.UserActorLibrary openUserLibrary \
    #	$configuration

    java::call ptolemy.actor.gui.UserActorLibrary \
	saveComponentInLibrary \
	$configuration $entity


    # Save the library
    set libraryInstance [$configuration getEntity "actor library.${userLibraryName}"]
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

#
# Reset the user library.
#
proc resetUserLibrary {configuration userLibraryName } {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    $parser purgeAllModelRecords

    # Delete the file in ~/.ptolemyII
    java::field ptolemy.actor.gui.UserActorLibrary \
    	USER_LIBRARY_NAME $userLibraryName
    set libraryName "[java::call ptolemy.util.StringUtilities preferencesDirectory]${userLibraryName}.xml"
    file delete -force $libraryName
    if [file exists $libraryName] {
	error "$libraryName exists"
    } 

    set libraryInstance [$configuration getEntity "actor library.${userLibraryName}"]

    set libraryTableau [java::null]
    catch {set libraryTableau [$configuration openModel $libraryInstance]}
    if {![java::isnull $libraryTableau]} {
	set libraryEffigy [java::cast ptolemy.actor.gui.PtolemyEffigy \
			       [$libraryTableau getContainer]]
	set file [$libraryEffigy getWritableFile]
	set directory [$configuration getDirectory]

	set fileURL [[[java::new java.io.File $libraryName] toURI] toURL]
	set libraryEffigy2 [$directory getEffigy [$fileURL toExternalForm]]
	#set libraryEffigy [java::call ptolemy.actor.gui.Configuration findEffigy $libraryInstance]
	$libraryEffigy2 setContainer [java::null]
	$libraryInstance setContainer [java::null]

	if {![java::isnull $file]} {
	    set parser [java::new ptolemy.moml.MoMLParser]
	    $parser purgeModelRecord [$file toURL]
	}
    }

    # Reopen the user library named by UserActorLibrary.USER_LIBRARY
    java::call ptolemy.actor.gui.UserActorLibrary openUserLibrary \
	$configuration
}

######################################################################
####
#
test UserActorLibrary-1.0 {Test saving test.xml in the User Actor Library} {
    resetUserLibrary $configuration $userLibraryName

    [testSaveFileInLibrary test.xml $configuration] exportMoML
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
                    <property name="init" class="ptolemy.actor.parameters.PortParameter" value="0">
                    </property>
                    <property name="step" class="ptolemy.actor.parameters.PortParameter" value="step">
                    </property>
                    <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
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
			   [testSaveFileInLibrary \
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

    resetUserLibrary $configuration $userLibraryName

    set handler [java::new ptolemy.util.MessageHandler]
    java::call ptolemy.util.MessageHandler setMessageHandler $handler

    java::call ptolemy.actor.gui.UserActorLibrary openUserLibrary \
    	$configuration

    java::call ptolemy.actor.gui.UserActorLibrary saveComponentInLibrary  $configuration $entity2
} {}

######################################################################
####
#
test UserActorLibrary-2.0 {A PortParameter in an unnamed entity} {
    resetUserLibrary $configuration $userLibraryName

    set toplevel [java::new ptolemy.actor.TypedCompositeActor]
    set portParameter [java::new ptolemy.actor.parameters.PortParameter $toplevel myPortParameter]
    java::call ptolemy.actor.gui.UserActorLibrary \
	saveComponentInLibrary \
	$configuration $toplevel
    list [$errorHandler getMessages] [$messageHandler getMessages]
} {{} {}}


######################################################################
####
#
test UserActorLibrary-2.1 {A Ramp in an unnamed entity} {
    resetUserLibrary $configuration $userLibraryName

    set toplevel2_1 [java::new ptolemy.actor.TypedCompositeActor]
    set ramp [java::new ptolemy.actor.lib.Ramp $toplevel2_1 myRamp]
    java::call ptolemy.actor.gui.UserActorLibrary \
	saveComponentInLibrary \
	$configuration $toplevel2_1
    list [$errorHandler getMessages] [$messageHandler getMessages]
} {{} {}}

resetUserLibrary $configuration $userLibraryName

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
