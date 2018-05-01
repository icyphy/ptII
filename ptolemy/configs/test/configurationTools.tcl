# Tests for configurations
#
# @Author: Steve Neuendorffer, Contributor: Christopher Hylands
#
# @Copyright (c) 2000-2018 The Regents of the University of California.
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


proc parseConfiguration {configurationFile parser} {

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

    # set osName [java::call System getProperty {os.name}]
    # set osNameStartsWith [string range $osName 0 5]
    #if {$osNameStartsWith == "Mac OS"} {
    #   puts "Skipping backtrack.xml because Backtracking has problems on the Mac"
    #   $inputFileNamesToSkip add "/backtrack.xml"
    #}
    
    # Tell the parser to skip inputting the above files
    java::field $parser inputFileNamesToSkip $inputFileNamesToSkip 

    # Filter out graphical classes while inside MoMLParser
    # See ptII/util/testsuite/removeGraphicalClasses.tcl
    # removeGraphicalClasses $parser

    set loader [[$parser getClass] getClassLoader]
    
    set URL [$loader getResource ptolemy/configs/$configurationFile]
    if {[java::isnull $URL]} {
	error "Could not get the  ptolemy/configs/$configurationFile resources"
    }
    set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
    set configuration [java::cast ptolemy.kernel.CompositeEntity $object]
    return $configuration
}


proc checkConstraints {configurationFile} {

    if {[regexp "jxta/" configurationFile] == 1} {
	puts "Skipping configurationFile, running vergil -jxta brings up a window"
	continue
    }

    if {[regexp "viptos/" configurationFile] == 1} {
	puts "Skipping configurationFile, PtinyOS is not typically installed."
	continue
    }

    puts " Force everything to get expanded ptolemy/configs/configurationFile"
    puts "    (Skipping certain optional packages, jxta and viptos)"

    set parser [java::new ptolemy.moml.MoMLParser]

    set configuration [parseConfiguration $configurationFile $parser]
    
    test "configurationFile-3.1" "Test to see if configurationFile contains any actors whose type constraints don't clone" {
            set results [[java::cast ptolemy.actor.gui.Configuration $configuration] check]
            # FIXME: Need to call this twice to find problems with RecordAssembler.
            puts "---- Second call to Configuration.check"
            set results2 [[java::cast ptolemy.actor.gui.Configuration $configuration] check]
            # Don't call return as the last line of a test proc, since return
            # throws an exception.
            list $results $results2
    } {{} {}}

    puts "[java::call ptolemy.actor.Manager timeAndMemory 0]"

    puts "Setting containers of atomic entities to null"
    set entityList [$configuration allAtomicEntityList]
    for {set iterator [$entityList iterator]} {[$iterator hasNext] == 1} {} {
        set entity [$iterator next]
        [java::cast ptolemy.kernel.ComponentEntity $entity] setContainer [java::null]
    }

    $configuration setContainer [java::null]

    set $configuration [java::null]

    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    $parser resetAll
    set $parser [java::null]

    #java::call System gc
    #puts "[java::call ptolemy.actor.Manager timeAndMemory 0]"

    #puts "######## sleeping"
    #java::call Thread sleep 1000000
    #sleep 10000 0
}    
