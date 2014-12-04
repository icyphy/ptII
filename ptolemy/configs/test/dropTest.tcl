# Tests for configurations
#
# @Author: Steve Neuendorffer, Contributor: Christopher Hylands
#
# $Id: allConfigs.tcl 64822 2012-10-19 04:40:11Z hudson $
#
# @Copyright (c) 2000-2012 The Regents of the University of California.
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


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


# Common code for drop test, used to test drag-n-drop for Actors and Attributes.
proc _dropTest {toplevel namedObj cloneConfiguration stream printStream isAttribute} {
    set results ""

    set fullName [[java::cast ptolemy.kernel.util.NamedObj $namedObj] getName $cloneConfiguration]

    set className [[$namedObj getClass] getName]
    #if [regexp {\.gui\.} $className] {
    #	puts "Skipping $className, it contains .gui., which causes problems in a headless environment"
    #	return
    #}
    # Check for attributes that contain attributes that fail when
    # put into an unnamed top level.
    # ptolemy.cg.kernel.generic.GenericCodeGenerator
    # failed here.
    regsub -all {\.} $fullName {_}  safeName
    set toplevel1_1 [java::new ptolemy.actor.TypedCompositeActor]
    set newNamedObj [java::new $className $toplevel1_1 $safeName]
    set newNamedObjs [$newNamedObj attributeList]
    puts $fullName
    for {set iterator3 [$newNamedObjs iterator]} {[$iterator3 hasNext] == 1} {} {
	set innerAttribute [java::cast ptolemy.kernel.util.Attribute [$iterator3 next]]
	if [catch {$newNamedObj attributeChanged $innerAttribute} errMsg] {
	    puts "_dropTest: $errMsg"
	    lappend results "Calling [$newNamedObj getFullName] attributeChanged [$innerAttribute getName] failed:\n$errMsg\n[jdkStackTrace]]" 
	}
    }

    if {$isAttribute} {
	$newNamedObj {setContainer {ptolemy.kernel.util.NamedObj}} [java::null]
	set clone [java::cast ptolemy.kernel.util.Attribute \
		       [$cloneConfiguration getAttribute $fullName]]
    } else {	
	[java::cast ptolemy.kernel.ComponentEntity $newNamedObj] {setContainer {ptolemy.kernel.CompositeEntity}} [java::null]
	set clone [java::cast ptolemy.actor.TypedAtomicActor \
		       [$cloneConfiguration getEntity $fullName]]
    }
    if {![java::isnull $clone]} {
	set moml [java::new StringBuffer]
	# Simulate vergil.basic.EditorDropTarget.drop()
	$moml append "<group>"
	$moml append "<group name=\"auto\">"
	$moml append [$clone exportMoML "dropped_[$nameObj getName]"]
	$moml append "</group>"
	$moml append "</group>"
	
	# The context of the ChangeRequest is the container
	# so that we properly evaluate atomic actors in
	# composite actors like MaximumEntropySpectrum
	set changeRequest [java::new ptolemy.moml.MoMLChangeRequest \
			       $toplevel [$clone getContainer] \
			       [$moml toString]]
	if [catch {$toplevel requestChange $changeRequest} errMsg] {
	    # Note that the changeRequest will likely never
	    # throw an error that will get us to here, we use
	    # a StreamChangeListener instead
	    set msg "\n\nIn '$fullName'\n\
                            the ChangeRequest:\n\
                            [$moml toString]\n\
                            failed:\n\
                            $errMsg\n\
	                    [jdkStackTrace]\n\
                            Perhaps there is a typo in the initial\n\
                            value of a parameter?\n"
	    puts $msg
	    lappend results $msg
	}
	
	# Flush the listener
	$printStream flush
	regsub -all [java::call System getProperty "line.separator"] \
	    [$stream toString] "\n" output
	if {[string first "StreamChangeRequest.changeFailed():" \
		 $output] != -1 } {
	    # If the listener starts with changedFailed, then we
	    # have an error
	    lappend results $output
	    puts $output
	}
	$stream reset
    }
    return $results
}

set parser [java::new ptolemy.moml.MoMLParser]

set inputFileNamesToSkip [java::new java.util.LinkedList]
# Alphabetical please
$inputFileNamesToSkip add "/apps/apps.xml"
$inputFileNamesToSkip add "/apps/superb/superb.xml"
#$inputFileNamesToSkip add "/attributes/decorative.xml"
$inputFileNamesToSkip add "/chic/chic.xml"
#$inputFileNamesToSkip add "/codegen.xml"
#$inputFileNamesToSkip add "/configs/ellipse.xml"
#$inputFileNamesToSkip add "/gr.xml"
$inputFileNamesToSkip add "/io/comm/comm.xml"
#$inputFileNamesToSkip add "/image.xml"
#$inputFileNamesToSkip add "/experimentalDirectors.xml"
#$inputFileNamesToSkip add "/lib/interactive.xml"
#$inputFileNamesToSkip add "/line.xml"
$inputFileNamesToSkip add "/jai/jai.xml"
$inputFileNamesToSkip add "/jmf/jmf.xml"
$inputFileNamesToSkip add "/joystick/jstick.xml"
$inputFileNamesToSkip add "/jxta/jxta.xml"
$inputFileNamesToSkip add "/ptinyos/lib/lib-composite.xml"
#$inputFileNamesToSkip add "/rectangle.xml"
$inputFileNamesToSkip add "TOSIndex.xml"
$inputFileNamesToSkip add "/quicktime.xml"
$inputFileNamesToSkip add "/matlab.xml"
#$inputFileNamesToSkip add "/x10/x10.xml"
#$inputFileNamesToSkip add "utilityIDAttribute.xml"

set osName [java::call System getProperty {os.name}]

set osNameStartsWith [string range $osName 0 5]

if {$osNameStartsWith == "Mac OS"} {
    puts "Skipping backtrack.xml because Backtracking has problems on the Mac"
    $inputFileNamesToSkip add "/backtrack.xml"
}
# Tell the parser to skip inputting the above files
java::field $parser inputFileNamesToSkip $inputFileNamesToSkip 


set loader [[$parser getClass] getClassLoader]

#set URL [$loader getResource ptolemy/configs/full/configuration.xml]
set URL [$loader getResource ptolemy/configs/test/ssm.xml]
if {[java::isnull $URL]} {
    error "Could not get the  $URL resources"
}
set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
set configuration [java::cast ptolemy.kernel.CompositeEntity $object]


set cloneConfiguration \
    [java::cast ptolemy.kernel.CompositeEntity [$configuration clone]]


set baseModel {<?xml version="1.0" standalone="no"?>
    <!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
    <entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="2"/>
    </property>
    </entity>
}

set parser [java::new ptolemy.moml.MoMLParser]
$parser reset
set toplevel [java::cast ptolemy.actor.CompositeActor \
		  [$parser parse $baseModel]]

set stream [java::new java.io.ByteArrayOutputStream]
set printStream [java::new \
		     {java.io.PrintStream java.io.OutputStream} $stream]

set listener [java::new ptolemy.kernel.util.StreamChangeListener \
		  $printStream]
$toplevel addChangeListener $listener

# 
#set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
set class [java::call Class forName org.ptolemy.ssm.StateSpaceModel]

set entityList [$configuration allAtomicEntityList]
set results {}
for {set iterator [$entityList iterator]} \
        {[$iterator hasNext] == 1} {} {
	set entity [$iterator next]

	if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
	    set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
	    #puts "Actor: [$actor getFullName]"
	    if [catch {set r [_dropTest $toplevel $actor $cloneConfiguration $stream $printStream 0]} errMsg] {
		lappend results "Drag and Drop test of actor: [$actor getFullName] failed:\n$errMsg\n[jdkStackTrace]"
	    }
	    if {[llength $r] != 0} {
		lappend results $r
	    }
	 }
	}

