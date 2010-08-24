# Common Tcl Procs to Test Publisher
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008-2009 The Regents of the University of California.
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

# Create a model with lots of Scale actors
proc createPublisher {e0 channel nameSuffix {factor 1.1} } {
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 "ramp_$nameSuffix"]
    set scale [java::new ptolemy.actor.lib.Scale $e0 "scale_$nameSuffix"]
    set publisher [java::new ptolemy.actor.lib.Publisher $e0 "publisher_$nameSuffix"]

    set globalParameter [getParameter $publisher global]
    $globalParameter setExpression true
    $publisher attributeChanged $globalParameter

    set factorParameter [getParameter $scale factor]
    $factorParameter setExpression $factor

    set channelParameter [getParameter $publisher channel]
    $channelParameter setExpression $channel
    $publisher attributeChanged $channelParameter

    $e0 connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]

    # Insert 25 scales
    for {set m 1} { $m <= 25} {incr m} {
	set scaleSuffix "scale_${nameSuffix}_$m"
	set otherScale [java::new ptolemy.actor.lib.Scale $e0 $scaleSuffix]
	if {$m == 1} {
	    $e0 connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale] output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $otherScale] input]
	} else {
	    $e0 connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $previousScale] output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $otherScale] input]
	}
	set previousScale $otherScale
    }

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer $previousScale] output] \
  	    [java::field $publisher input]
}

proc createSubscriber {e0 channel nameSuffix} {
    set subscriber [java::new ptolemy.actor.lib.Subscriber $e0 "subscriber_$nameSuffix"]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 "rec_$nameSuffix"]

    set channelParameter [getParameter $subscriber channel]
    $channelParameter setExpression $channel

    $subscriber attributeChanged $channelParameter

    $e0 connect \
            [java::field $subscriber output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    return $rec
}

proc nPubSubs {numberOfPubSubs {returnAll 1} {usePubSub 1}} {
    set e0 [createPubSubModel $numberOfPubSubs $returnAll $usePubSub]
    return [executeModel $numberOfPubSubs $e0 $returnAll]
}

proc createPubSubModel {numberOfPubSubs {returnAll 1} {usePubSub 1} {typedComposite "ptolemy.actor.TypedCompositeActor"}} {
    #puts "createPubSubModel $numberOfPubSubs $returnAll $usePubSub $typedComposite"
    set e0 [sdfModel 5]
    $e0 allowLevelCrossingConnect true
    set sdfDirector [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
    for {set i 1} { $i < $numberOfPubSubs} {incr i} {
        set rec [createSubscriber $e0 "PubSub20_$i" "PS20_$i"]
	set e1 [java::new $typedComposite $e0 "e1-$i"]

	$e1 allowLevelCrossingConnect true
	createPublisher $e1 "PubSub20_$i" "PS20_$i" [expr {($i + 0.0)/$numberOfPubSubs + 1.0}]

	if {$usePubSub == 0 } {
	    # Remove the pub sub actors and use levelxing links
	    set scale [java::cast ptolemy.actor.lib.Scale [$e1 getEntity "scale_PS20_$i"]]
	    [java::field [java::cast ptolemy.actor.lib.Transformer $scale] output] unlinkAll

	    set pub [java::cast ptolemy.actor.lib.Publisher [$e1 getEntity "publisher_PS20_$i"]]
	    $pub setContainer [java::null]

	    set rec [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "rec_PS20_$i"]]
	    [java::field [java::cast ptolemy.actor.lib.Sink $rec] input] unlinkAll

	    set sub [java::cast ptolemy.actor.lib.Subscriber [$e0 getEntity "subscriber_PS20_$i"]]
	    $sub setContainer [java::null]

	    set scale [java::cast ptolemy.actor.lib.Scale [$e1 getEntity "scale_PS20_$i"]]
	    set scale2 [java::new ptolemy.actor.lib.Scale $e1 "scale2_PS20_$i"]
	    set scale3 [java::new ptolemy.actor.lib.Scale $e0 "scale3_PS20_$i"]

	    $e1 connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale] output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale2] input]

            
	    set ar [$e1 newRelation [$e1 uniqueName "LevelXingRelation"]]
            [java::field [java::cast ptolemy.actor.lib.Transformer $scale2] output] liberalLink $ar
	    [java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input] liberalLink $ar


	    #$e0 connect \
	    #	[java::field [java::cast ptolemy.actor.lib.Transformer $scale2] output] \
	    #	[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input]

	    set rec [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "rec_PS20_$i"]]
	    $e0 connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] output] \
		[java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
	}

    }
    return $e0
}

proc executeModel {numberOfPubSubs e0 returnAll} {
    [$e0 getManager] execute
    if {$returnAll == 1} {
	set result {}
	for {set i 1} { $i < $numberOfPubSubs} {incr i} {
	    set rec [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "rec_PS20_$i"]]

	    lappend result [list [enumToTokenValues [$rec getRecord 0]]]
	}
    } else {
	set rec [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "rec_PS20_[expr {$numberOfPubSubs - 1}]"]]
	set result [enumToTokenValues [$rec getRecord 0]]
    }
    return [list $e0 $result]
}

proc timeStats {name data} {
    set sawFinished 0
    set r "<dataset name=\"$name\">"
    set op "m"
    foreach line $data {
	set nActors [lindex $line 0]
	if {[regexp finished $line]} {
            set time [lindex $line [expr {[llength $line] - 7}]]
	    puts "time: $time, $line"
        } else {
        set time [lindex $line 4]
        }
        set r "$r\n<$op x=\"$nActors\" y=\"$time\"/>"
	set op "p"
    }
    set r "$r\n</dataset>"
    return $r
}

proc memStats {name data} {
    set sawFinished 0
    set r "<dataset name=\"$name\">"
    set op "m"
    foreach line $data {
	set nActors [lindex $line 0]
	if {[regexp finished $line]} {
	    set mem [lindex $line [expr {[llength $line] - 4}]]
	    set free [lindex $line [expr {[llength $line] - 2}]]
	    puts "$mem, $free, $line"
        } else {
            set nActors [lindex $line 0]
	    set mem [lindex $line 7]
	    set free [lindex $line 9]
        }
        set mem [string range $mem 0 [expr {[string length $mem] - 2 } ]]
        set free [string range $free 0 [expr {[string length $free] - 2 } ]]
        set used [expr {($mem - $free)/10.0}]
	#puts "$mem $free $used: $line"
	set r "$r\n<$op x=\"$nActors\" y=\"$used\"/>"
	set op "p"
    }
    set r "$r\n</dataset>"
    return $r
}

proc plotStats {pubSubStats {levelxingStats {}}} {
    set plot [open stats.xml w] 
    puts $plot {<?xml version="1.0" standalone="yes"?>
<!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<!-- Ptolemy plot, version 5.7.1.devel , PlotML format. -->
<title>Number of actors vs time and memory</title>
<xLabel>Number of actors</xLabel>
<yLabel>Time in ms, Memory in 10k Bytes</yLabel>
	<default marks="bigdots"/>}

    puts $plot [timeStats {PubSub Time in ms.} $pubSubStats]
    puts $plot [memStats {PubSub Memory in 10K bytes} $pubSubStats]

    puts "PubSub results"
    puts $pubSubStats

    if { "$levelxingStats" != ""} {
	puts $plot [timeStats {Level Xing Time in ms.} $levelxingStats]
	puts $plot [memStats {Level Xing Memory in 10K bytes} $levelxingStats]
	puts "Level Crossing results"
	puts $levelxingStats
    }

    puts $plot {</plot>}
    close $plot

}

# Create a Pub/Sub model that is at the top.
# This proc is called only once.
proc _createHierarchichalPubSubModelTop {container numberOfPubSubsPerLevel levelNumber {returnAll 1} {usePubSub 1} {typedComposite "ptolemy.actor.TypedCompositeActor"} {opaque true}} {
    puts "_createHierarchicalPubSubModelTop $container $numberOfPubSubsPerLevel $levelNumber $returnAll $usePubSub  $typedComposite $opaque"
    if {$levelNumber == 1} {
	_createHierarchichalPubSubModel $container $numberOfPubSubsPerLevel $levelNumber $returnAll $usePubSub $typedComposite
	return
    }
    global pubCount
 	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
            set en [java::new $typedComposite $container "en-$n"]
	    if {$opaque} {
		set sdfDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector $en "SDFDirector"]  
		# [java::field $sdfDirector iterations] setExpression 1
		[java::field $sdfDirector allowDisconnectedGraphs] setExpression true
		if {$typedComposite == "ptolemy.cg.lib.ModularCodeGenTypedCompositeActor"} {
		    [java::field $en recompileHierarchy] setExpression true
		    [java::field $en recompileThisLevel] setExpression true
		}

	    }
 	    $en allowLevelCrossingConnect true
 	    set channel "PubSub_[$en getFullName]_[expr {$levelNumber - 1}]_$n"
 	    set channel2 "PubSub_[$container getFullName]_${levelNumber}_$n"

	    set nameSuffix [expr {$levelNumber - 1}]

	    # Only create a few ModalCodeGenTypedComposites at the top.
	    set innerTypedComposite ptolemy.actor.TypedCompositeActor
	    #set innerTypedComposite $typedComposite

   	    _createHierarchichalPubSubModel $en $numberOfPubSubsPerLevel $nameSuffix $returnAll $usePubSub $innerTypedComposite $opaque
	    #puts "---------[$en getFullName] $levelNumber $n\n[$en exportMoML]"

	    set p1 [java::new ptolemy.actor.TypedIOPort $en "p1" false true]

	    #pub_b
	    set innerPublisher [$en getEntity "pub_${nameSuffix}_${n}"]
	    set innerPublisherPort [$innerPublisher getPort "input"]
	    $innerPublisherPort unlinkAll

	    $innerPublisher setContainer [java::null]

	    set innerSubscriber [$en getEntity "sub_${nameSuffix}_${n}"]
	    set innerSubscriberPort [$innerSubscriber getPort "output"]
	    $innerSubscriberPort unlinkAll

	    set relation [$en connect \
			      $innerSubscriberPort \
			      $p1]

		#[java::field [java::cast ptolemy.actor.lib.Transformer $innerSubscriber] output]

	    [java::cast ptolemy.actor.IORelation $relation] setWidth -1

 	    #set subscriber [java::new ptolemy.actor.lib.Subscriber $container "sub_c_${levelNumber}_$n"]
 	    #set channelParameter [getParameter $subscriber channel]
 	    #$channelParameter setExpression $channel
 	    #$subscriber attributeChanged $channelParameter

	    #pub_c
 	    set publisher [java::new ptolemy.actor.lib.Publisher $container "pub_${levelNumber}_$n"]

	    #set globalParameter [getParameter $publisher global]
	    #$globalParameter setExpression true
	    #$publisher attributeChanged $globalParameter

 	    set channelParameter [getParameter $publisher channel]
 	    $channelParameter setExpression $channel2
 	    $publisher attributeChanged $channelParameter

 	    set relation [$container connect \
			      $p1 \
			      [java::field $publisher input]]

	        #[java::field $subscriber output]
	    [java::cast ptolemy.actor.IORelation $relation] setWidth -1
         } 
}

# Create a Pub/Sub model that is not at the top.
# This proc is called multiple times.
proc _createHierarchichalPubSubModel {container numberOfPubSubsPerLevel levelNumber {returnAll 1} {usePubSub 1} {typedComposite "ptolemy.actor.TypedCompositeActor"} {opaque true}} {
    #puts "_createHierarchicalPubSubModel $container $numberOfPubSubsPerLevel $levelNumber $returnAll $usePubSub  $typedComposite"
    global pubCount
    if {$levelNumber == 1} {
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
            set en [java::new $typedComposite $container "en-$n"]
	    # FIXME: it seems like opaques don't work here?
	    if {$typedComposite != "ptolemy.actor.TypedCompositeActor"} {
		set sdfDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector $en "SDFDirector"]  
		#[java::field $sdfDirector iterations] setExpression 1
		[java::field $sdfDirector allowDisconnectedGraphs] setExpression true
	    }
	    #puts "_createHierarchicalPubSubModel 1 $typedComposite [$container getFullName] \n [$en exportMoML]"
	    $en allowLevelCrossingConnect true
	    #set channel "PubSub_[expr {$levelNumber - 1}]_$n"
	    #set channel2 "PubSub_${levelNumber}_$n"
	    set channel "PubSub_[$en getFullName]_[expr {$levelNumber - 1}]_$n"
	    set channel2 "PubSub_[$container getFullName]_${levelNumber}_$n"

	    incr pubCount
	    #set channel "PubSub_${pubCount}a"
	    #set channel2 "PubSub_${pubCount}b"

	    #set channel "PubSub_[expr {$levelNumber - 1}]_${n}_${pubCount}"
	    #set channel2 "PubSub_${levelNumber}_${n}_${pubCount}"

	    #pub_a
	    set nameSuffix "pub_[expr {$levelNumber - 1}]_$n"
	    createPublisher $en $channel "$nameSuffix" [expr {($n + 0.0)/$numberOfPubSubsPerLevel + 1.0}]

	    # Remove the inner publisher and add a port
# 	    set p1 [java::new ptolemy.actor.TypedIOPort $en "p1" false true]
# 	    set innerPublisher [$en getEntity "publisher_$nameSuffix"]
# 	    set innerPublisherPort [$innerPublisher getPort "input"]
# 	    $innerPublisherPort unlinkAll

# 	    $innerPublisher setContainer [java::null]

# 	    set innerScale [$en getEntity "scale_$nameSuffix"]
# 	    set innerScalePort [$innerScale getPort "output"]
# 	    $innerScalePort unlinkAll

# 	    $en connect \
# 		[java::field [java::cast ptolemy.actor.lib.Transformer $innerScale] output] \
# 		$p1

	    # Create the upper subscriber and set the channel 
	    set subscriber [java::new ptolemy.actor.lib.Subscriber $container "sub_${levelNumber}_$n"]
	    set channelParameter [getParameter $subscriber channel]
	    $channelParameter setExpression $channel
	    $subscriber attributeChanged $channelParameter

	    # Create the upper publisher and set the channel 
	    #pub_b
	    set publisher [java::new ptolemy.actor.lib.Publisher $container "pub_${levelNumber}_$n"]
	    # Needed for a 2_3 model
	    set globalParameter [getParameter $publisher global]
	    $globalParameter setExpression true
	    #$publisher attributeChanged $globalParameter

	    set channelParameter [getParameter $publisher channel]
	    $channelParameter setExpression $channel2
	    $publisher attributeChanged $channelParameter

	    $container connect \
		[java::field $subscriber output] \
		[java::field $publisher input]
        } 
    } else {
 	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
            set en [java::new $typedComposite $container "en-$n"]
	    if {$opaque} {
		set sdfDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector $en "SDFDirector"]  
		#[java::field $sdfDirector iterations] setExpression 1
		[java::field $sdfDirector allowDisconnectedGraphs] setExpression true
	    }
	    #puts "_createHierarchicalPubSubModel N $typedComposite [$en exportMoML]"
 	    $en allowLevelCrossingConnect true
 	    set channel "PubSub_[$en getFullName]_[expr {$levelNumber - 1}]_$n"
 	    set channel2 "PubSub_[$container getFullName]_${levelNumber}_$n"

	    set nameSuffix [expr {$levelNumber - 1}]
   	    _createHierarchichalPubSubModel $en $numberOfPubSubsPerLevel $nameSuffix $returnAll $usePubSub $typedComposite

	    #sub_c
 	    set subscriber [java::new ptolemy.actor.lib.Subscriber $container "sub_${levelNumber}_$n"]
 	    set channelParameter [getParameter $subscriber channel]
 	    $channelParameter setExpression $channel
 	    $subscriber attributeChanged $channelParameter

	    #pub_c
 	    set publisher [java::new ptolemy.actor.lib.Publisher $container "pub_${levelNumber}_$n"]
	    # Needed for 3_4 models
	    set globalParameter [getParameter $publisher global]
	    $globalParameter setExpression true
	    #$publisher attributeChanged $globalParameter

 	    set channelParameter [getParameter $publisher channel]
 	    $channelParameter setExpression $channel2
 	    $publisher attributeChanged $channelParameter

 	    $container connect \
	        [java::field $subscriber output] \
 		[java::field $publisher input]
         } 
    }
}

# Create a top level model with an appropriate number of subscribers and levels
# that has a Recorder. 
proc _pubSubAggBase {numberOfSubsPerLevel levels {typedComposite "ptolemy.actor.TypedCompositeActor"} {opaque true}} {
    # This proc is only called from pubSubAggModel, which is the main entry point

    puts "_pubSubAggBase $numberOfSubsPerLevel $levels $typedComposite"
    global pubCount 
    set pubCount 0
    set e0 [sdfModel 5]
    $e0 allowLevelCrossingConnect true
    # This is the top level, so we always set allowDisconnectedGraphs to true.
    set sdfDirector [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
    set returnAll 1
    set usePubSub 1
    _createHierarchichalPubSubModelTop $e0 $numberOfSubsPerLevel $levels $returnAll $usePubSub $typedComposite $opaque

    set subscriptionAggregator [java::new ptolemy.actor.lib.SubscriptionAggregator $e0 "subscriptionAggregator"]
    set recorder [java::new ptolemy.actor.lib.Recorder $e0 "recorder"]

    #set channel "PubSub_${levels}_\[0-9\]"
    set channel "PubSub_[$e0 getFullName]_${levels}_\[0-9\]*"
    #set channel2 "PubSub_[$ getFullName]_${levelNumber}_$n"

    set channelParameter [getParameter $subscriptionAggregator channel]
    $channelParameter setExpression $channel
    $subscriptionAggregator attributeChanged $channelParameter

    $e0 connect \
	[java::field [java::cast ptolemy.actor.lib.Subscriber $subscriptionAggregator] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $recorder] input]
    return $e0
}

# Create a Publisher/Subscriber model with a certain number of Subscribers and levels
# This is the main entry point for creating Pub/Sub models.
proc pubSubAggModel {numberOfSubsPerLevel levels {typedComposite "ptolemy.actor.TypedCompositeActor"} {opaque true}} {
    puts "pubSubAggModel $numberOfSubsPerLevel $levels $typedComposite $opaque"
    set e0 [_pubSubAggBase $numberOfSubsPerLevel $levels $typedComposite $opaque]
    set modelType ""
    if {$typedComposite != "ptolemy.actor.TypedCompositeActor"} {
	if {$typedComposite == "ptolemy.cg.lib.ModularCodeGenTypedCompositeActor"} {
	    set modelType "MCG"
	} else { 
	    if {$typedComposite == "ptolemy.actor.LazyTypedCompositeActor"} {
	    set modelType "Lazy"
	    } else {
		set modelType "Other"
	    }
	}
    }
    if {$opaque == "true"} {
	set modelType "${modelType}Opaque"
    } else {
	set modelType "${modelType}Transparent"
    }
    set baseName "pubSubAgg${modelType}_${numberOfSubsPerLevel}_${levels}"
    set fileName ${baseName}.xml
    set fd [open $fileName w]
    $e0 setName $baseName
    puts $fd [$e0 exportMoML]
    close $fd
    puts "Created $fileName, containing [[$e0 deepEntityList] size] actors"
    $e0 setContainer [java::null]

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser resetAll
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $fileName]]

    set baseName "pubSubAgg${modelType}_${numberOfSubsPerLevel}_${levels}-clean"
    set fileName ${baseName}.xml
    set fd [open $fileName w]
    $e0 setName $baseName
    puts $fd [$toplevel exportMoML]
    close $fd
    $toplevel setContainer [java::null]

    $parser resetAll
    puts "Parsing $fileName"
    set toplevel2 [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $fileName]]
    puts "BUG: need to expand lazy composites: [$toplevel2 deepGetEntities]"
    set manager [java::new ptolemy.actor.Manager [$toplevel2 workspace] "manager"]
    $toplevel2 setManager $manager

    catch {$manager execute} errMsg
    $manager execute

    set recorder [java::cast ptolemy.actor.lib.Recorder [$toplevel2 getEntity "recorder"]]
    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 


proc lazyStats {e0} {
    set lazyTypedCompositeCount 0
    set entityList [$e0 allCompositeEntityList] 
    set iterator [$entityList iterator]
    while {[$iterator hasNext]} {
	set entity [java::cast ptolemy.kernel.util.NamedObj [$iterator next]]
	#puts "[$entity getFullName] [$entity getClassName] [[$entity getClass] getName]"
	if {[java::instanceof $entity ptolemy.actor.LazyTypedCompositeActor]} {
	    incr lazyTypedCompositeCount
	}
    }
    return "containing [[$e0 deepOpaqueEntityList] size] actors, $lazyTypedCompositeCount LazyTypedCompositeActors"
}

proc pubSubAggLazyModel {numberOfSubsPerLevel levels} {
    set e0 [pubSubAggBase $numberOfSubsPerLevel $levels]
    set fileName "pubSubAgg_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $fileName w]
    puts $fd [$e0 exportMoML]
    close $fd
    puts "Created $fileName, [lazyStats $e0]"

    
    jdkCapture {
	java::new ptolemy.moml.ConvertToLazy $fileName 0
    } moml2

    set fileName "pubSubAggLazy_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $fileName w]
    puts $fd $moml2
    close $fd

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser resetAll

    set e1 [java::cast ptolemy.actor.TypedCompositeActor [$parser parse $moml2]]

    puts "Created $fileName, [lazyStats $e1]"

    set manager [java::new ptolemy.actor.Manager [$e1 workspace] "myManager"]
    $e1 setManager $manager
    [$e1 getManager] execute
    set recorder [java::cast ptolemy.actor.lib.Recorder [$e1 getEntity "recorder"]]
    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 

##########################################################################

# Below here are commands used to build composites that don't have pub/sub.
# See README.txt

# Create the second level composites that may have a ModalCodeGenTypedComposite
proc _createHierarchichalModelSecondLevel {container numberOfPubSubsPerLevel levelNumber {typedComposite "ptolemy.actor.TypedCompositeActor"}} {
    #puts "_createHierarchicalModelSecondLevel $numberOfPubSubsPerLevel $levelNumber $typedComposite"
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
	    set en [java::new $typedComposite $container "en-$n"]

	    # Only create a few ModalCodeGenTypedComposites at the top.
	    #set innerTypedComposite ptolemy.actor.TypedCompositeActor
	    set innerTypedComposite $typedComposite

   	    _createHierarchichalModel $en $numberOfPubSubsPerLevel [expr {$levelNumber - 1}] $innerTypedComposite

	    set upperNameSuffix "${levelNumber}_$n"

	    set p1 [java::new ptolemy.actor.TypedIOPort $en "p1_$upperNameSuffix" true false]
	    set r2 [java::new ptolemy.actor.TypedIORelation $en R2]
	    $p1 link $r2

	    # Collect all the outputs
	    set addSub [java::new ptolemy.actor.lib.AddSubtract $en addSub]
	    for {set m 1} { $m <= $numberOfPubSubsPerLevel} {incr m} {
		set enInner [java::cast $innerTypedComposite [$en getEntity "en-$m"]]

		set p1InnerName "p1_[expr {${levelNumber} -1}]_$m"
		set p1Inner [$enInner getPort $p1InnerName]
		if {[java::isnull $p1Inner]} {
		    error "levelNumber=$levelNumber, n=$n, m=$m: Could not get port $p1InnerName from $en. [$en exportMoML]"
		}
		$p1Inner link $r2
		#$en connect $p1 $p1Inner

		set p2InnerName "p2_[expr {${levelNumber} -1}]_$m"
		set p2Inner [$enInner getPort $p2InnerName]
		if {[java::isnull $p2Inner]} {
		    error "levelNumber=$levelNumber, n=$n, m=$m: Could not get port $p2InnerName from $en. [$en exportMoML]"
		}
		$en connect $p2Inner [java::field $addSub plus] "R_m$m"
	    }
	    set scale3 [java::new ptolemy.actor.lib.Scale $en "scale3_$upperNameSuffix"]

	    $en connect \
		[java::field $addSub output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input]

	    set p2 [java::new ptolemy.actor.TypedIOPort $en "p2_$upperNameSuffix" false true]
	    #puts "p2: levelNumber: $levelNumber, n: $n: p2: p2_$upperNameSuffix"
	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] output] \
		$p2
	}
    }


# Create the inner composites of a model that does not use Publisher/Subscriber
# This proc usually calls itself multiple times.
proc _createHierarchichalModel {container numberOfPubSubsPerLevel levelNumber {typedComposite "ptolemy.actor.TypedCompositeActor"}} {
    #puts "_createHierarchicalModel $numberOfPubSubsPerLevel $levelNumber $typedComposite"
    global pubCount
    if {$levelNumber == 1} {
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
            set en [java::new $typedComposite $container "en-$n"]

	    #if {$typedComposite != "ptolemy.actor.TypedCompositeActor"} {
		#set sdfDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector $en "SDFDirector"]  
		#[java::field $sdfDirector allowDisconnectedGraphs] setExpression true
		#if {$typedComposite == "ptolemy.cg.lib.ModularCodeGenTypedCompositeActor"} {
		#    [java::field $en recompileHierarchy] setExpression false
		#    [java::field $en recompileThisLevel] setExpression false
		#}
	    #}

	    set nameSuffix "[expr {$levelNumber}]_$n"

	    set scale [java::new ptolemy.actor.lib.Scale $en "scale_$nameSuffix"]

	    set factor  [expr {($n + 0.0)/$numberOfPubSubsPerLevel + 1.0}]
	    set factorParameter [getParameter $scale factor]
	    $factorParameter setExpression $factor

	    set p1 [java::new ptolemy.actor.TypedIOPort $en "p1_$nameSuffix" true false]

	    $en connect \
		$p1 \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]

	    # Insert 25 scales
	    for {set m 1} { $m <= 25} {incr m} {
		set scaleSuffix "scale_${nameSuffix}_$m"
		set otherScale [java::new ptolemy.actor.lib.Scale $en $scaleSuffix]
		if {$m == 1} {
		    $en connect \
			[java::field [java::cast ptolemy.actor.lib.Transformer $scale] output] \
			[java::field [java::cast ptolemy.actor.lib.Transformer $otherScale] input]
		} else {
		    $en connect \
			[java::field [java::cast ptolemy.actor.lib.Transformer $previousScale] output] \
			[java::field [java::cast ptolemy.actor.lib.Transformer $otherScale] input]
		}
		set previousScale $otherScale
	    }

	    set p2 [java::new ptolemy.actor.TypedIOPort $en "p2_$nameSuffix" false true]
	    #puts "p2: levelNumber: $levelNumber, n: $n: p2: p2_$nameSuffix"
	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $otherScale] output] \
		$p2
        } 
    } else {
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
	    set en [java::new $typedComposite $container "en-$n"]
   	    _createHierarchichalModel $en $numberOfPubSubsPerLevel [expr {$levelNumber - 1}] $typedComposite

	    set upperNameSuffix "${levelNumber}_$n"

	    set p1 [java::new ptolemy.actor.TypedIOPort $en "p1_$upperNameSuffix" true false]
	    set r2 [java::new ptolemy.actor.TypedIORelation $en R2]
	    $p1 link $r2

	    # Collect all the outputs
	    set addSub [java::new ptolemy.actor.lib.AddSubtract $en addSub]
	    for {set m 1} { $m <= $numberOfPubSubsPerLevel} {incr m} {
		set enInner [java::cast $typedComposite [$en getEntity "en-$m"]]

		set p1InnerName "p1_[expr {${levelNumber} -1}]_$m"
		set p1Inner [$enInner getPort $p1InnerName]
		if {[java::isnull $p1Inner]} {
		    error "levelNumber=$levelNumber, n=$n, m=$m: Could not get port $p1InnerName from $en. [$en exportMoML]"
		}
		$p1Inner link $r2
		#$en connect $p1 $p1Inner

		set p2InnerName "p2_[expr {${levelNumber} -1}]_$m"
		set p2Inner [$enInner getPort $p2InnerName]
		if {[java::isnull $p2Inner]} {
		    error "levelNumber=$levelNumber, n=$n, m=$m: Could not get port $p2InnerName from $en. [$en exportMoML]"
		}
		$en connect $p2Inner [java::field $addSub plus] "R_m$m"
	    }
	    set scale3 [java::new ptolemy.actor.lib.Scale $en "scale3_$upperNameSuffix"]

	    $en connect \
		[java::field $addSub output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input]

	    set p2 [java::new ptolemy.actor.TypedIOPort $en "p2_$upperNameSuffix" false true]
	    #puts "p2: levelNumber: $levelNumber, n: $n: p2: p2_$upperNameSuffix"
	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] output] \
		$p2
	}
    }
}

# Create a model that does not use publisher and subscribers
proc modularCodeGenModel {numberOfSubsPerLevel levels {typedComposite "ptolemy.actor.TypedCompositeActor"} {opaque true}} {
    global pubCount 
    global e0
    set pubCount 0
    set e0 [sdfModel 365]
    _createHierarchichalModelSecondLevel $e0 $numberOfSubsPerLevel $levels $typedComposite


    set ramp [java::new ptolemy.actor.lib.Ramp $e0 "Ramp"]
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 R1]
    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] link $r1

    # Connect ports
    set addSub [java::new ptolemy.actor.lib.AddSubtract $e0 addSub]
    for {set n 1} { $n <= $numberOfSubsPerLevel} {incr n} {
        set nameSuffix "${levels}_$n"
	set en [java::cast $typedComposite [$e0 getEntity "en-$n"]]

	if {$opaque} {
	    set sdfDirector [java::new ptolemy.domains.sdf.kernel.SDFDirector $en "SDFDirector"]  
	    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
	}
	#[java::field $sdfDirector allowDisconnectedGraphs] setExpression true
	#if {$typedComposite == "ptolemy.cg.lib.ModularCodeGenTypedCompositeActor"} {
	#    [java::field $en recompileHierarchy] setExpression false
	#    [java::field $en recompileThisLevel] setExpression false
	#}
        set p1Name "p1_[expr {${levels}}]_$n"
	set p1 [$en getPort $p1Name]
	if {[java::isnull $p1]} {
	    error "Could not get port $p1Name from $en\n[$en exportMoML]" 
        }
	$p1 link $r1

        set p2Name "p2_[expr {${levels}}]_$n"
	set p2 [$en getPort $p2Name]
	if {[java::isnull $p2]} {
	    error "Could not get port $p2Name from $en\n[$en exportMoML]" 
        }
	$e0 connect $p2 [java::field $addSub plus] "R_$n"
        #set port2Name "port2_[expr {${levels}}]_$n"
	#set port2 [$en getPort $port2Name]
	#if {[java::isnull $port2]} {
	#    error "Could not get port $port2Name from $en\n[$en exportMoML]" 
        #}
	#$e0 connect $port2 [java::field $addSub plus] "R_$n"
    }
    set recorder [java::new ptolemy.actor.lib.Recorder $e0 "recorder"]

    $e0 connect \
        [java::field $addSub output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $recorder] input]

    # Save the model
    set modelType ""
    if {$typedComposite != "ptolemy.actor.TypedCompositeActor"} {
	if {$typedComposite == "ptolemy.cg.lib.ModularCodeGenTypedCompositeActor"} {
	    set modelType "MCG"
	} else { 
	    if {$typedComposite == "ptolemy.actor.LazyTypedCompositeActor"} {
		set modelType "Lazy"
	    } else {
		set modelType "Other"
	    }
	}
    }
    if {$opaque != "true"} {
	set modelType "${modelType}Transparent"
    }
    # Change the name so that each model generates code with a unique name
    set basename "hierarchicalModel${modelType}_${numberOfSubsPerLevel}_${levels}"
    $e0 setName $basename

    set manager [java::new ptolemy.actor.Manager [$e0 workspace] "manager"]
    $e0 setManager $manager
    # Run the model first so that the saved models don't regenerate code
    $manager execute

    set fileName "${basename}.xml"
    set fd [open $fileName w]
    puts $fd [$e0 exportMoML]
    close $fd


    puts "Created $fileName, containing [[$e0 deepOpaqueEntityList] size] actors"


    # Read in the model so that we generate code with the right names.
    # Otherwise, code for different models will be shared
    #set parser [java::new ptolemy.moml.MoMLParser]
    #$parser resetAll
    #set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile $fileName]]
    #set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "manager"]
    #$toplevel setManager $manager
    #$manager execute
    #set recorder2 [java::cast ptolemy.actor.lib.Recorder [$toplevel getEntity "recorder"]]

    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 

