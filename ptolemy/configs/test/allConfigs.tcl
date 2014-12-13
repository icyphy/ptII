# Tests for configurations
#
# @Author: Steve Neuendorffer, Contributor: Christopher Hylands
#
# $Id$
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

# Test the suggestedModalModelDirectors() method by constructing each
# element of the array.
proc testSuggestedModalModelDirectors {director} {
    set compositeEntity [java::new ptolemy.kernel.CompositeEntity]
    set directors [$director suggestedModalModelDirectors]
    for {set i 0} {$i < [$directors length]} {incr i} {
	set directorName [$directors -noconvert get $i]
        set directorClass [java::call Class forName $directorName]
        set args [java::new {Class[]} {2} \
	    [list \
		[java::call Class forName ptolemy.kernel.CompositeEntity] \
		[java::call Class forName java.lang.String]]]
	set constructor [$directorClass getConstructor $args]
	set shortDirectorName [$directorName substring \
		[expr {[$directorName lastIndexOf "."] + 1}]]
	set a [list $compositeEntity $shortDirectorName] 
	set initArgs [java::new {java.lang.Object[]} {2} $a]
	set instance [$constructor newInstance $initArgs]
	if {![java::instanceof $instance ptolemy.actor.Director]} {
	    error "$directorClass is not a Director?"
	}
    }
}

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

cd ..
set configs [glob */*configuration*.xml]
#set configs full/configuration.xml
#set configs hyvisual/configuration.xml
cd test

foreach i $configs {

    
    if {[regexp "jxta/" $i] == 1} {
	puts "Skipping $i, running vergil -jxta brings up a window"
	continue
    }

    puts " Force everything to get expanded ptolemy/configs/$i"
    puts "    (Skipping certain optional packages)"

    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    # Add backward compatibility filters
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    # Add optional .xml files to be skipped to this list.
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

    # Filter out graphical classes while inside MoMLParser
    # See ptII/util/testsuite/removeGraphicalClasses.tcl
    # removeGraphicalClasses $parser

    set loader [[$parser getClass] getClassLoader]
    
    set URL [$loader getResource ptolemy/configs/$i]
    if {[java::isnull $URL]} {
	error "Could not get the  ptolemy/configs/$i resources"
    }
    set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
    set configuration [java::cast ptolemy.kernel.CompositeEntity $object]
    
    # The configuration has a removeGraphicalClasses parameter that
    # defaults to false so we set it to true.
    #set removeGraphicalClasses [java::field [java::cast ptolemy.actor.gui.Configuration $configuration] removeGraphicalClasses]
    #$removeGraphicalClasses setExpression "true"


    test "$i-1.1" "Test to see if $i contains any bad XML" {
	# force everything to get expanded
	expr [string length [$configuration description]] > 0
    } {1}

    test "$i-2.1" "Test to see if $i has fields with names that are wrong" {
	# In general, if we call getName on a public field in an actor,
	# then the name that is returned should be the same as the name
	# of the field.
	puts "-------> Before clone"
 	set cloneConfiguration [java::cast ptolemy.kernel.CompositeEntity [$configuration clone [java::new ptolemy.kernel.util.Workspace {clonedWorkspace}]]]
	puts "-------> after clone"
	set entityList [$configuration allAtomicEntityList]
	set results {}
	set logfile [open logfile2-1 "w"]
	for {set iterator [$entityList iterator]} \
		{[$iterator hasNext] == 1} {} {
	    set entity [$iterator next]
	    if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
		set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		set className [$actor getClassName]
		if [java::instanceof $entity $className] {
		    set realActor [java::cast $className $entity]
		    set fields [java::info fields $realActor]
		    # This puts seems to be necessary, or else we get
		    # field being set to 'tcl.lang.FieldSig@2b6fc7'
		    # instead of 'factor'
  		    puts $logfile "actor: $className fields: $fields"
		    foreach field $fields {
			# If the field is actually defined in the parent class
			# then java::field will not find the field
			set fieldObj [java::null]
			catch {
			    # We use -noconvert here in case there is a public
			    # int or double. hde.ArrayMem has a public int.
			    set fieldObj [java::field -noconvert \
				    $realActor $field]
			}
			if {![java::isnull $fieldObj]} {
			    if [catch {set dottedName [$fieldObj getName $entity]} errMsg] {
				set msg "\n\nIn '$className'\n\
					On the field '$field'\n\
					The getName() method failed:\n\
					$errMsg\n\
					Perhaps the field is a basic type?\n"
				lappend results $msg
			    } else {
				set sanitizedName [java::call ptolemy.util.StringUtilities sanitizeName $dottedName]
				if {"$sanitizedName" != "$field"} {
				    if { "${sanitizedName}Port" == "$field"} {
					# FileReader needs this:
					puts "\nWarning: In '$className'\n \
					    The getName() method returns\n  \
					    '[$fieldObj getName]' but the \
					    field is named\n   '$field'\n  \
					    This is technically a violation \
					    of the coding standard,\n  \
					    but permissible because the name \
					    ends in 'Port'"

				    } else {
					set msg "\n\nIn '$className'\n\
					    The getName() method returns\n \
					    '$sanitizedName' != '$field' \
					    '[$fieldObj getName]' but the \
					    field is named\n  '$field'.\n \
                                            Perhaps you should use an \
                                            underscore followed by the field \
                                            name\n, see how SDFTransformer \
                                            keeps a reference to objecs that \
                                            are not directly contained."
				lappend results $msg
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	close $logfile
	file delete logfile2-1
	list $results
    } {{}}

    test "$i-3.1" "Test to see if $i contains any actors whose type constraints don't clone" {
	    set results [[java::cast ptolemy.actor.gui.Configuration $configuration] check]
	    # FIXME: Need to call this twice to find problems with RecordAssembler.
	    puts "---- Second call to Configuration.check"
	    set results2 [[java::cast ptolemy.actor.gui.Configuration $configuration] check]
   	    # Don't call return as the last line of a test proc, since return
	    # throws an exception.
	    list $results $results2
    } {{} {}}


    test "$i-4.1" "Test to see if $i contains any actors that might not drag and drop properly by creating ChangeRequests " {

	# This test caught a problem with AudioReader, where the initial
	# default source URL parameter had a bogus value.

	# Create a base model.
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
	set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
	$toplevel setManager $manager
	

	# Set up a StreamChangeListener to listen for errors
	set stream [java::new java.io.ByteArrayOutputStream]
	set printStream [java::new \
		     {java.io.PrintStream java.io.OutputStream} $stream]
	set listener [java::new ptolemy.kernel.util.StreamChangeListener \
			  $printStream]
	$toplevel addChangeListener $listener

	set cloneConfiguration \
	    [java::cast ptolemy.kernel.CompositeEntity [$configuration clone]]

	set entityList [$configuration allAtomicEntityList]
	set results {}
	for {set iterator [$entityList iterator]} \
	    {[$iterator hasNext] == 1} {} {
   	    set entity [$iterator next]
	    
	    #puts [$entity toString]
	    #if [java::instanceof $entity ptolemy.kernel.util.NamedObj] {
	    #	puts [[java::cast ptolemy.kernel.util.NamedObj $entity] \
	    #		  getName]
	    #}

	    if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
		set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		#puts "Actor: [$actor getFullName]"
		set actorName [$actor getFullName]
                if [regexp {StateSpaceModel} $actorName] {
                    puts "Skipping StateSpaceModel as this test iterates through the attributes and calls attributeChanged, which triggers a ConcurrentModificationException.  See org/ptolemy/ssm/test/SSMTest.java"
                }  {
		    if [catch {set r [_dropTest $toplevel $actor $cloneConfiguration $stream $printStream 0]} errMsg] {
			lappend results "Drag and Drop test of actor: [$actor getFullName] failed:\n$errMsg\n[jdkStackTrace]"
		    }
		}
		if {[llength $r] != 0} {
		    lappend results $r
		}
	    }

        }
	list $results
    } {{}}

    test "$i-4.2" "Test to see if $i contains any attributes that might not drag and drop properly by creating ChangeRequests " {

	# Use the baseModel from 4.1 above

	# Set up a StreamChangeListener to listen for errors
	set stream [java::new java.io.ByteArrayOutputStream]
	set printStream [java::new \
		     {java.io.PrintStream java.io.OutputStream} $stream]
	set listener [java::new ptolemy.kernel.util.StreamChangeListener \
			  $printStream]
	$toplevel addChangeListener $listener

	set cloneConfiguration \
	    [java::cast ptolemy.kernel.CompositeEntity [$configuration clone]]

	set entityList [$configuration deepNamedObjList]
	set results {}
	set count 0
	for {set iterator [$entityList iterator]} \
	    {[$iterator hasNext] == 1} {} {
		set object [$iterator next]
		#puts "6Attribute: [$object toString]"
		if [java::instanceof $object ptolemy.moml.EntityLibrary] {
		    #puts "---------- [$object toString]"

		    # FIXME: I don't understand why I need to expand the entityLibraries
		    # and get the attributes?
		    set entityLibrary [java::cast ptolemy.moml.EntityLibrary $object]
		    #puts [$entityLibrary exportMoML]
		    set attributes [$entityLibrary attributeList]
		    #puts [listToFullNames $attributes]
		    for {set iterator2 [$attributes iterator]} {[$iterator2 hasNext] == 1} {} {
			set attr [$iterator2 next]
			#puts "attr: [java::instanceof $attr ptolemy.kernel.util.Attribute] [$attr toString] "
			
			set attribute [java::cast ptolemy.kernel.util.Attribute $attr]

			if [java::instanceof $attribute ptolemy.domains.tm.kernel.SchedulePlotter] {
			    puts "Skipping drop test of tm.kernel.SchedulePlotter because it must be dropped into a container that has a TMDirector"
			    continue
			}

			set r [_dropTest $toplevel $attribute $cloneConfiguration $stream $printStream 1]
			if {[llength $r] != 0} {
			    lappend results $r
			}
		    }
		}
	    }
	list $results
    } {{}}


    test "$i-5.1" "Test directors in $i " {
	#set entityList [$configuration allAtomicEntityList]
	set actorLibrary [java::cast ptolemy.kernel.CompositeEntity \
		[$configuration getEntity {actor library}]]
	set results {}
	if [ java::isnull $actorLibrary] {
	    puts "Warning: $i has no 'actor library'?  (this is ok for ptinyViewer)"
	} else {
	    set directors [java::cast ptolemy.kernel.CompositeEntity \
			       [$actorLibrary getEntity {Directors}]]
	    if [java::isnull $directors] {
		puts "Warning: $i has no 'actor library.Directors'? (this is ok for dsp, viptos)"
	    } else {
		set attributeList [$directors attributeList]
		set allDirectors [java::new java.util.LinkedList $attributeList]
		set experimentalDirectors \
		    [$directors getEntity ExperimentalDirectors]
	
		if {![java::isnull $experimentalDirectors]} {
		    set moreDirectors [$experimentalDirectors attributeList]
		    $allDirectors addAll $moreDirectors
		}

		#puts "Testing as many as [$allDirectors size] directors in $i"
		for {set iterator [$allDirectors iterator]} \
		    {[$iterator hasNext] == 1} {} {
			set entity [$iterator next]

			# Call all the suggestedModalModelDirectors methods
			# and instantiate each director that is returned.
			if [java::instanceof $entity ptolemy.actor.Director] {
			    set director [java::cast ptolemy.actor.Director $entity]
			    #puts "testing director [$director getName]"
			    set msg {}
			    catch {testSuggestedModalModelDirectors $director} msg
			    if {"$msg" != ""} {
				lappend results $msg
			    }
			}
		    }
	    }
	}
	list $results
    } {{}}

    test "$i-6.1" "Test that clone(Workspace) works on a new Actor.  Creating kars in Kepler does this." {
	# In general, if we call getName on a public field in an actor,
	# then the name that is returned should be the same as the name
	# of the field.
	puts "-------> Before clone"
 	set cloneConfiguration [java::cast ptolemy.kernel.CompositeEntity [$configuration clone [java::new ptolemy.kernel.util.Workspace {clonedWorkspace}]]]
	puts "-------> after clone"
	set workspace61 [java::new ptolemy.kernel.util.Workspace "workspace61"]
	set compositeEntity61 [java::new ptolemy.kernel.CompositeEntity $workspace61]
	set workspace61Clone [java::new ptolemy.kernel.util.Workspace "workspace61Clone"]
	set entityList [$configuration allAtomicEntityList]
	set results {}
	for {set iterator [$entityList iterator]} \
		{[$iterator hasNext] == 1} {} {
	    set entity [$iterator next]
	    if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
		set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		set className [$actor getClassName]
		# Create a new actor
		set newActor [java::new $className $compositeEntity61 [$compositeEntity61 uniqueName [split $className . ]]]
		# Clone it.
		if [catch {set clonedActor [$newActor clone $workspace61Clone]} errMsg] {
		    lappend $results "Cloning $className failed:\n$errMsg:\n[jdkStackTrace]\n"
		}
		[java::cast ptolemy.kernel.ComponentEntity $clonedActor] setContainer [java::null]
	    }
	}
	[java::cast ptolemy.kernel.CompositeEntity $compositeEntity61] setContainer [java::null]
	$cloneConfiguration setContainer [java::null]
	list $results
    } {{}}
}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
