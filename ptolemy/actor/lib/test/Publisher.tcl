# Test Publisher
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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


######################################################################
####
#
test Publisher-1.1 {Test class instantiation problem} {
    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "auto/PublisherSubscriber2.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    #set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    #$model setManager $manager 
    #$manager execute
    list [$model getName] \
	[[$model getEntity CompositeActor] isClassDefinition]
} {PublisherSubscriber2 0}

test Publisher-1.2 {Convert CompositeActor to class, save the model for later use} {
    # Uses 1.1 above
    set compositeActor [$model getEntity CompositeActor]
    set request [java::new ptolemy.moml.MoMLChangeRequest \
		     $workspace $model \
		     "<class name=\"[$compositeActor getName]\"/>"]
    $model requestChange $request

    # Save the model
    set fileWriter [java::new java.io.FileWriter PublisherSubscriber2class.xml]
    $model exportMoML $fileWriter 0 "[$model getName]class"
    $fileWriter close

    #$manager execute
    list [$model getName] \
	[[$model getEntity CompositeActor] isClassDefinition]
} {PublisherSubscriber2 1}


test Publisher-1.3 {Convert CompositeActor of our first model to an instance } {
    # Uses 1.1 and 1.2 above

    # This is a test of class instantiation problems with Publishers and
    # Subscribers.
    # If we have a model (auto/PublisherSubscriber2.xml) that has a 
    # CompositeActor that contains a Subscriber, and we:
    # 1. Convert the CompositeActor to a class (see test 1.2)
    # 2. Instantiate the CompositeActor (which is now a class) (this test (1.3)
    # Then we get an error.
    # However, if we _save_ the model after 1.2 and then instantiate
    # (see test 1.5) then things work?

    set compositeActor [$model getEntity CompositeActor]
    set moml "<group name=\"auto\"><entity name=\"InstanceOf[$compositeActor getName]\" class=\"[$compositeActor getName]\"/></group>"
    set request [java::new ptolemy.moml.MoMLChangeRequest \
		     $workspace $model $moml]

    # This should not cause an error
    $model requestChange $request
    set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    $model setManager $manager 
    $manager execute
    list [$model getName] \
	[[$model getEntity CompositeActor] isClassDefinition]

} {PublisherSubscriber2 1}

test Publisher-1.4 {Read in the model created in 1.2 with the class definition} {
    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "PublisherSubscriber2class.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    list [$model getName] \
	[[$model getEntity CompositeActor] isClassDefinition]
} {PublisherSubscriber2class 1}

test Publisher-1.5 {Convert CompositeActor to an instance } {
    # Uses 1.1 and 1.2 above
    set compositeActor [$model getEntity CompositeActor]
    set moml "<group name=\"auto\"><entity name=\"InstanceOf[$compositeActor getName]\" class=\"[$compositeActor getName]\"/></group>"
    set request [java::new ptolemy.moml.MoMLChangeRequest \
		     $workspace $model $moml]

    $model requestChange $request
    set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    $model setManager $manager 
    $manager execute
} {}
