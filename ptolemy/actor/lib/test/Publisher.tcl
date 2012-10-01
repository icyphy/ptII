# Test Publisher
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

proc readModel {modelPath} {
    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File $modelPath] toURL]
    $parser purgeModelRecord $url
    return [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
}

proc readModelWorkspace {workspace} {
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
    return $model
}



######################################################################
####
#
test Publisher-1.1 {Test class instantiation problem} {
    set model [readModel auto/PublisherSubscriber2.xml]

    #set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    #$model setManager $manager 
    #$manager execute
    list [$model getName] \
	[[$model getEntity CompositeActor] isClassDefinition]
} {PublisherSubscriber2 0}

test Publisher-1.2 {Convert CompositeActor to class, save the model for later use} {
    # Uses 1.1 above
    set compositeActor [$model getEntity CompositeActor]
    set workspace [$model workspace]
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
    #set moml "<group name=\"auto\"><entity name=\"InstanceOf[$compositeActor getName]\" class=\"[$compositeActor getName]\"/></group>"
    set moml "<entity name=\"InstanceOf[$compositeActor getName]\" class=\"[$compositeActor getName]\"/>"
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
    set model [readModelWorkspace $workspace]
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

#set fileWriter [java::new java.io.FileWriter foo2.xml]
#$model exportMoML $fileWriter 0 "[$model getName]"
#$fileWriter close

test Publisher-1.6 {Convert CompositeActor to a class } {
    # Uses 1.1, 1.2, 1.3

    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set model [readModelWorkspace $workspace]

    for {set x 0} {$x < 3} {incr x} {   

	# Convert to class, see vergil/actor/ActorInstanceController.java
	set compositeActor [$model getEntity CompositeActor]
	set moml "<class name=\"[$compositeActor getName]\"/>"
	set request [java::new ptolemy.moml.MoMLChangeRequest \
			 $workspace $model $moml]

	$model requestChange $request
	set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
	$model setManager $manager 
	$manager execute

	# Convert to instance, see vergil/actor/ClassDefinitionController.java
	set moml "<entity name=\"[$compositeActor getName]\"/>"
	set request [java::new ptolemy.moml.MoMLChangeRequest \
		     $workspace $model $moml]

	set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
	$model setManager $manager 
	$model requestChange $request

	# This used to cause
	#   ptolemy.kernel.util.IllegalActionException: 
	#   Subscriber has no matching Publisher.
	$manager execute
    }
} {}

######################################################################
####
#
test Publisher-2.1 {Instantiate twice a class that has a publisher} {

    # Having two publishers with the same channel name is an error,
    # and detecting it at run time is correct (detecting it at model
    # construction time would be wrong, since then you couldn't
    # actually create two instances).

    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "PublisherSubscriberInClass.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set compositeActor [$model getEntity CompositeActor]
    #set moml "<group name=\"auto\"><entity name=\"InstanceOf[$compositeActor getName]\" class=\"[$compositeActor getName]\"/></group>"
    set moml "<entity name=\"Instance1Of[$compositeActor getName]\" class=\"[$compositeActor getName]\"/>"
    set request [java::new ptolemy.moml.MoMLChangeRequest \
		     $workspace $model $moml]

    # This should not cause an error
    $model requestChange $request

    # Instantiate again.  This means we now have two publishers
    # with the same instance
    set moml "<entity name=\"Instance2Of[$compositeActor getName]\" class=\"[$compositeActor getName]\"/>"
    set request2 [java::new ptolemy.moml.MoMLChangeRequest \
		     $workspace $model $moml]
    $model requestChange $request2

    set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    $model setManager $manager 

    catch {$manager execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Can't link SubscriptionAggregatorPort with a PublisherPort.
  in .PublisherSubscriberInClass.Instance1OfCompositeActor.Subscriber.input
Because:
We have 2 ports with the name "channel1", which is not equal to 1.
 port: ptolemy.actor.PublisherPort {.PublisherSubscriberInClass.Instance1OfCompositeActor.Publisher.output}name: output channel: ptolemy.data.expr.StringParameter {.PublisherSubscriberInClass.Instance1OfCompositeActor.Publisher.output.channel} "channel1"
 port: ptolemy.actor.PublisherPort {.PublisherSubscriberInClass.Instance2OfCompositeActor.Publisher.output}name: output channel: ptolemy.data.expr.StringParameter {.PublisherSubscriberInClass.Instance2OfCompositeActor.Publisher.output.channel} "channel1"

  in .PublisherSubscriberInClass}}

######################################################################
####
#
test Publisher-2.0 {Test deletion of a Publisher} {
    set workspace [java::new ptolemy.kernel.util.Workspace "subAggPubDelWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
            [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
            ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "SubscriptionAggregatorPublisherDelete.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
                   [$parser {parse java.net.URL java.net.URL} \
                        [java::null] $url]]
    set manager [java::new ptolemy.actor.Manager $workspace "subAggPubDelManager"]
    $model setManager $manager
    # Success is not crashing
    $manager execute

    # Get the value of Recorder
    set recorder [$model getEntity "Recorder"]
    set r1 [[[java::cast ptolemy.actor.lib.Recorder $recorder] getLatest 0] toString]

    # Delete the second publisher
    set publisher2 [java::cast ptolemy.actor.lib.Publisher [$model getEntity "Publisher2"]]

    # listToFullNames is defined in enums.tcl
    set subscribersBeforeDeletion [listToFullNames [$publisher2 subscribers]]
    $publisher2 setContainer [java::null]

    # This should not crash.  We used to get: 
    # ptolemy.actor.sched.NotSchedulableException: Actors remain that
    # cannot be scheduled!
    # The fix was to add Publisher.setContainer().
    $manager execute

    set r2 [[[java::cast ptolemy.actor.lib.Recorder $recorder] getLatest 0] toString]
    list $r1 $r2 $subscribersBeforeDeletion
} {2 1 .SubscriptionAggregatorPublisherDelete.SubscriptionAggregator}


test Publisher-3.0 {Test no publisher} {
    set model [readModel "NoPublisher.xml"]

    set manager [java::new ptolemy.actor.Manager [$model workspace] "p30manager"]
    $model setManager $manager 
    catch {$manager execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: No Publishers were found adjacent to or below .NoPublisher.subagg
  in .NoPublisher.subagg}}


###############################################################
# Infrastructure for changing Publisher channel names

proc getPublisherChannel {model publisherName} {
    return [list $publisherName [[java::field [java::cast ptolemy.actor.lib.Publisher [$model getEntity $publisherName]] channel] getExpression]]
}
proc getSubscriberChannel {model subscriberName} {
    return [list $subscriberName [[java::field [java::cast ptolemy.actor.lib.Subscriber [$model getEntity $subscriberName]] channel] getExpression]]
}
proc getChannels {model} {
    return [list [getPublisherChannel $model Publisher] \
		[getPublisherChannel $model Publisher2] \
		[getSubscriberChannel $model Subscriber] \
		[getSubscriberChannel $model Subscriber2] \
		[getSubscriberChannel $model SubscriptionAggregator] \
		[getSubscriberChannel $model SubscriptionAggregator2]]
}

### publishers() and subscribers()
proc getPublishers {model subscriberName} {
    return [list $subscriberName [lsort [listToFullNames [[java::cast ptolemy.actor.lib.Subscriber [$model getEntity $subscriberName]] publishers]]]]
}
proc getSubscribers {model publisherName} {
    return [list $publisherName [lsort [listToFullNames [[java::cast ptolemy.actor.lib.Publisher [$model getEntity $publisherName]] subscribers]]]]
}
proc getPublishersAndSubscribers {model} {
    return [list [getSubscribers $model Publisher] "\n" \
		[getSubscribers $model Publisher2] "\n" \
		[getPublishers $model Subscriber] "\n" \
		[getPublishers $model Subscriber2] "\n" \
		[getPublishers $model SubscriptionAggregator] "\n" \
		[getPublishers $model SubscriptionAggregator2]]
}

test Publisher-4.0 {Channel Name Change: the initial values} {
    set model [readModel PublisherSubscriberChannelChange.xml] 

    set manager [java::new ptolemy.actor.Manager [$model workspace] "p4manager"]
    $model setManager $manager 
    $manager execute

    getChannels $model
} {{Publisher channel3} {Publisher2 foo} {Subscriber channel3} {Subscriber2 foo} {SubscriptionAggregator channel3} {SubscriptionAggregator2 channel.*}}


test Publisher-4.0.5 {Channel Name Change: the initial values} {
    # Uses 4.0 above
    getPublishersAndSubscribers $model
} {{Publisher {.PublisherSubscriberChannelChange.Subscriber .PublisherSubscriberChannelChange.SubscriptionAggregator .PublisherSubscriberChannelChange.SubscriptionAggregator2}} {
} {Publisher2 .PublisherSubscriberChannelChange.Subscriber2} {
} {Subscriber .PublisherSubscriberChannelChange.Publisher} {
} {Subscriber2 .PublisherSubscriberChannelChange.Publisher2} {
} {SubscriptionAggregator .PublisherSubscriberChannelChange.Publisher} {
} {SubscriptionAggregator2 .PublisherSubscriberChannelChange.Publisher}}

test Publisher-4.1 {Channel Name Change: change the Publisher name} {
    set model [readModel PublisherSubscriberChannelChange.xml] 

    # Change the channel of the publisher
    set publisher [java::cast ptolemy.actor.lib.Publisher [$model getEntity Publisher]]
    set channel [getParameter $publisher channel]
    $channel setExpression "channel4"
    $channel validate

    set manager [java::new ptolemy.actor.Manager [$model workspace] "p4manager"]
    $model setManager $manager 
    $manager execute

    # Test out the subscribers() method. listToFullNames is defined in enums.tcl
    set subscribers [listToFullNames [$publisher subscribers]]

    list "[getChannels $model]\n [lsort $subscribers]"
} {{{Publisher channel4} {Publisher2 foo} {Subscriber channel4} {Subscriber2 foo} {SubscriptionAggregator channel4} {SubscriptionAggregator2 channel.*}
 .PublisherSubscriberChannelChange.Subscriber .PublisherSubscriberChannelChange.SubscriptionAggregator .PublisherSubscriberChannelChange.SubscriptionAggregator2}}


test Publisher-4.1.5 {test out subscribers() and publishers()} {
    # Uses 4.1 above
    getPublishersAndSubscribers $model
} {{Publisher {.PublisherSubscriberChannelChange.Subscriber .PublisherSubscriberChannelChange.SubscriptionAggregator .PublisherSubscriberChannelChange.SubscriptionAggregator2}} {
} {Publisher2 .PublisherSubscriberChannelChange.Subscriber2} {
} {Subscriber .PublisherSubscriberChannelChange.Publisher} {
} {Subscriber2 .PublisherSubscriberChannelChange.Publisher2} {
} {SubscriptionAggregator .PublisherSubscriberChannelChange.Publisher} {
} {SubscriptionAggregator2 .PublisherSubscriberChannelChange.Publisher}}

test Publisher-4.2 {Channel Name Change: change the Publisher name in an Opaque model} {
    set model [readModel auto/PublisherSubscriberOpaque.xml]

    # Change the channel of the publisher
    set publisher [java::cast ptolemy.actor.lib.Publisher [$model getEntity CompositeActor.CompositeActor.Publisher]]
    set propagateNameChanges [getParameter $publisher propagateNameChanges]
    $propagateNameChanges setExpression true
    set channel [getParameter $publisher channel]
    $channel setExpression "channel4"
    $channel validate

    # Test out the subscribers() method. listToFullNames is defined in enums.tcl
    set subscribers [listToFullNames [$publisher subscribers]]

    list [getPublisherChannel $model CompositeActor.CompositeActor.Publisher] \
	[getSubscriberChannel $model CompositeActor.Subscriber] \
	$subscribers
} {{CompositeActor.CompositeActor.Publisher channel4} {CompositeActor.Subscriber channel4} .PublisherSubscriberOpaque.CompositeActor.Subscriber}

test Publisher-4.2.5 {test out subscribers() and publishers() on an opaque} {
    # Uses 4.2 above
    list \
	[getPublishers $model CompositeActor.Subscriber] "\n" \
	[getSubscribers $model CompositeActor.CompositeActor.Publisher]
} {{CompositeActor.Subscriber .PublisherSubscriberOpaque.CompositeActor.CompositeActor.Publisher} {
} {CompositeActor.CompositeActor.Publisher .PublisherSubscriberOpaque.CompositeActor.Subscriber}}

test Publisher-4.3 {Channel Name Change: change the Publisher name in an Opaque model} {
    # Run the model from 4.2
    set manager [java::new ptolemy.actor.Manager [$model workspace] "p4manager"]
    $model setManager $manager 
    $manager execute
} {}

test Publisher-4.4 {test out subscribers() and publishers() on a deep opaque} {
    set model4_4 [readModel auto/PublisherSubscriberOpaqueD3.xml]
    list \
	[getPublishers $model4_4 CompositeActor.Subscriber] "\n" \
	[getSubscribers $model4_4 CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher]
} {{CompositeActor.Subscriber .PublisherSubscriberOpaqueD3.CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher} {
} {CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher .PublisherSubscriberOpaqueD3.CompositeActor.Subscriber}}

test Publisher-4.5 {test out subscribers() and publishers() on a deep opaque} {
    set model4_5 [readModel auto/PublisherSubscriberOpaqueD3Reverse.xml]
    list \
	[getPublishers $model4_5 CompositeActor.CompositeActor.CompositeActor.CompositeActor.Subscriber] "\n" \
	[getSubscribers $model4_5 CompositeActor.Publisher] 
} {{CompositeActor.CompositeActor.CompositeActor.CompositeActor.Subscriber .PublisherSubscriberOpaqueD3Reverse.CompositeActor.Publisher} {
} {CompositeActor.Publisher .PublisherSubscriberOpaqueD3Reverse.CompositeActor.CompositeActor.CompositeActor.CompositeActor.Subscriber}}

test Publisher-4.6 {test out subscribers() and publishers() on a deep transparent} {
    set model4_6 [readModel auto/PublisherSubscriberD3.xml]
    list \
	[getPublishers $model4_6 CompositeActor.Subscriber] "\n" \
	[getSubscribers $model4_6 CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher]
} {{CompositeActor.Subscriber .PublisherSubscriberD3.CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher} {
} {CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher .PublisherSubscriberD3.CompositeActor.Subscriber}}

test Publisher-4.7 {test out subscribers() and publishers() on a deep transparent} {
    set model4_7 [readModel auto/PublisherSubscriberD3Reverse.xml]
    list \
	[getPublishers $model4_7 CompositeActor.CompositeActor.CompositeActor.CompositeActor.Subscriber] "\n" \
	[getSubscribers $model4_7 CompositeActor.Publisher]
} {{CompositeActor.CompositeActor.CompositeActor.CompositeActor.Subscriber .PublisherSubscriberD3Reverse.CompositeActor.Publisher} {
} {CompositeActor.Publisher .PublisherSubscriberD3Reverse.CompositeActor.CompositeActor.CompositeActor.CompositeActor.Subscriber}}

test Publisher-4.8 {test out subscribers() and publishers() on a deep transparent where the Pub and the Sub are adjacent} {
    set model4_8 [readModel auto/PublisherSubscriberAdjacent.xml]
    list \
	[getPublishers $model4_8 CompositeActor.CompositeActor2.CompositeActor.CompositeActor.Subscriber] "\n" \
	[getSubscribers $model4_8 CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher]
} {{CompositeActor.CompositeActor2.CompositeActor.CompositeActor.Subscriber .PublisherSubscriberAdjacent.CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher} {
} {CompositeActor.CompositeActor.CompositeActor.CompositeActor.Publisher .PublisherSubscriberAdjacent.CompositeActor.CompositeActor2.CompositeActor.CompositeActor.Subscriber}}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

