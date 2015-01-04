# Tests for configurations
#
# @Author: Steve Neuendorffer, Contributor: Christopher Hylands
#
# $Id$
#
# @Copyright (c) 2000-2009 The Regents of the University of California.
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

cd $PTII/ptolemy/configs
set configs [glob */*configuration*.xml]
cd ../../doc/test

foreach i $configs {

    
    if {[regexp "jxta/" $i] == 1} {
	puts "Skipping $i, running vergil -jxta brings up a window"
	continue
    }

    if {[regexp "viptos/" $i] == 1} {
        if {[java::call System getenv TOSROOT] == ""} {
            puts "Skipping viptos because TOSROOT is not set in the environment, so we are assuming that TinyOS is not installed."
            continue
        }
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
    $inputFileNamesToSkip add "/attributes/decorative.xml"
    $inputFileNamesToSkip add "/chic/chic.xml"
    $inputFileNamesToSkip add "/codegen.xml"
    $inputFileNamesToSkip add "/configs/ellipse.xml"
    $inputFileNamesToSkip add "/gr.xml"
    $inputFileNamesToSkip add "/io/comm/comm.xml"
    $inputFileNamesToSkip add "/image.xml"
    #$inputFileNamesToSkip add "/experimentalDirectors.xml"
    $inputFileNamesToSkip add "/lib/interactive.xml"
    $inputFileNamesToSkip add "/line.xml"
    $inputFileNamesToSkip add "/jai/jai.xml"
    $inputFileNamesToSkip add "/jmf/jmf.xml"
    $inputFileNamesToSkip add "/joystick/jstick.xml"
    $inputFileNamesToSkip add "/jxta/jxta.xml"
    $inputFileNamesToSkip add "/ptinyos/lib/lib-composite.xml"
    $inputFileNamesToSkip add "/rectangle.xml"
    $inputFileNamesToSkip add "TOSIndex.xml"
    $inputFileNamesToSkip add "/quicktime.xml"
    $inputFileNamesToSkip add "/matlab.xml"
    $inputFileNamesToSkip add "/x10/x10.xml"
    $inputFileNamesToSkip add "utilityIDAttribute.xml"

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
    removeGraphicalClasses $parser

    set loader [[$parser getClass] getClassLoader]
    
    set URL [$loader getResource ptolemy/configs/$i]
    set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
    set configuration [java::cast ptolemy.kernel.CompositeEntity $object]

    test "$i-6.1" "Test docClassNameToURL" {
	# In general, if we call getName on a public field in an actor,
	# then the name that is returned should be the same as the name
	# of the field.
	set entityList [$configuration allAtomicEntityList]
	set results {}
	for {set iterator [$entityList iterator]} \
	    {[$iterator hasNext] == 1} {} {
		set entity [$iterator next]
		if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
		    set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		    set className [$actor getClassName]
		    set url [java::call ptolemy.vergil.actor.DocManager \
				 docClassNameToURL \
				 $configuration $className \
				 true false false false]
		    if [java::isnull $url] {
			lappend results "\nFailed to find ptdoc for $className.  Try doing (cd $PTII/doc; rm codeDoc/ptolemy/actor/lib/Ramp.xml codeDoc/ptolemy/actor/lib/RampIdx.xml; make docs)"
		    }


		    set url [java::call ptolemy.vergil.actor.DocManager \
				 docClassNameToURL \
				 $configuration $className \
				 false true false false]
		    if [java::isnull $url] {
			lappend results "\nFailed to find javadoc for $className"
		    }

		    set url [java::call ptolemy.vergil.actor.DocManager \
				 docClassNameToURL \
				 $configuration $className \
				 false true false false]
		    if [java::isnull $url] {
			lappend results "\nFailed to find source for $className"
		    }


		    # For actor index, it is ok to return null
		    set url [java::call ptolemy.vergil.actor.DocManager \
				 docClassNameToURL \
				 $configuration $className \
				 false false false true]
		}
	    }
    list $results	
    } {{}}
}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
