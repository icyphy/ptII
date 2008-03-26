# Tests Configuration
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
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


######################################################################
####
#
test Configuration-1.0 {Read in the configuration} { 
    global configuration
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]

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
