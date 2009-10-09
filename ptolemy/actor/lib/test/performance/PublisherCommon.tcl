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

    set factorParameter [getParameter $scale factor]
    $factorParameter setExpression $factor

    set channelParameter [getParameter $publisher channel]
    $channelParameter setExpression $channel

    $e0 connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]

    $publisher attributeChanged $channelParameter

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer $scale] output] \
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

proc createPubSubModel {numberOfPubSubs {returnAll 1} {usePubSub 1}} {
    set e0 [sdfModel 5]
    $e0 allowLevelCrossingConnect true
    set sdfDirector [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
    for {set i 1} { $i < $numberOfPubSubs} {incr i} {
        set rec [createSubscriber $e0 "PubSub20_$i" "PS20_$i"]
        set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 "e1-$i"]
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


proc createHierarchichalPubSubModel {container numberOfPubSubsPerLevel levelNumber {returnAll 1} {usePubSub 1}} {
    global pubCount
    if {$levelNumber == 1} {
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
            set en [java::new ptolemy.actor.TypedCompositeActor $container "en-$n"]
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

	    createPublisher $en $channel "pub_a_[expr {$levelNumber - 1}]_$n" [expr {($n + 0.0)/$numberOfPubSubsPerLevel + 1.0}]

	    # Get the upper subscriber and set the channel 
	    set subscriber [java::new ptolemy.actor.lib.Subscriber $container "sub_${levelNumber}_$n"]
	    set channelParameter [getParameter $subscriber channel]
	    $channelParameter setExpression $channel
	    $subscriber attributeChanged $channelParameter

	    # Get the upper subscriber and set the channel 
	    set publisher [java::new ptolemy.actor.lib.Publisher $container "pub_b_${levelNumber}_$n"]
	    set channelParameter [getParameter $publisher channel]
	    $channelParameter setExpression $channel2
	    $publisher attributeChanged $channelParameter

	    $container connect \
		[java::field $subscriber output] \
		[java::field $publisher input]
        } 
    } else {
 	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
             set en [java::new ptolemy.actor.TypedCompositeActor $container "en-$n"]
 	    $en allowLevelCrossingConnect true
# 	    #set channel "PubSub_${pubCount}_[expr {$levelNumber - 1}]_$n"
 	    #set channel2 "PubSub_${pubCount}_${levelNumber}_$n"
# 	    incr pubCount
 	    #set channel "PubSub_${pubCount}a"
 	    #set channel2 "PubSub_${pubCount}b"
 	    #set channel "PubSub_[expr {$levelNumber - 1}]_${n}"
 	    #set channel2 "PubSub_${levelNumber}_${n}"
 	    set channel "PubSub_[$en getFullName]_[expr {$levelNumber - 1}]_$n"
 	    set channel2 "PubSub_[$container getFullName]_${levelNumber}_$n"

   	    createHierarchichalPubSubModel $en $numberOfPubSubsPerLevel [expr {$levelNumber - 1}] $returnAll $usePubSub
 	    set subscriber [java::new ptolemy.actor.lib.Subscriber $container "sub_c_${levelNumber}_$n"]
 	    set channelParameter [getParameter $subscriber channel]
 	    $channelParameter setExpression $channel
 	    $subscriber attributeChanged $channelParameter

 	    set publisher [java::new ptolemy.actor.lib.Publisher $container "pub_c_${levelNumber}_$n"]
 	    set channelParameter [getParameter $publisher channel]
 	    $channelParameter setExpression $channel2
 	    $publisher attributeChanged $channelParameter

 	    $container connect \
 		[java::field $subscriber output] \
 		[java::field $publisher input]
         } 
    }
}

proc pubSubAggBase {numberOfSubsPerLevel levels} {

    global pubCount 
    set pubCount 0
    set e0 [sdfModel 5]
    $e0 allowLevelCrossingConnect true
    set sdfDirector [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
    createHierarchichalPubSubModel $e0 $numberOfSubsPerLevel $levels

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

proc pubSubAggModel {numberOfSubsPerLevel levels} {
    set e0 [pubSubAggBase $numberOfSubsPerLevel $levels]
    set filename "pubSubAgg_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $filename w]
    puts $fd [$e0 exportMoML]
    close $fd
    puts "Created $filename, containing [[$e0 deepOpaqueEntityList] size] actors"
    
    [$e0 getManager] execute
    set recorder [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "recorder"]]
    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 


proc pubSubAggLazyModel {numberOfSubsPerLevel levels} {
    set e0 [pubSubAggBase $numberOfSubsPerLevel $levels]
    set filename "pubSubAgg_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $filename w]
    puts $fd [$e0 exportMoML]
    close $fd
    puts "Created $filename, containing [[$e0 deepOpaqueEntityList] size] actors"
    
    jdkCapture {
	java::new ptolemy.moml.ConvertToLazy $filename 0
    } moml2

    set filename "pubSubAggLazy_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $filename w]
    puts $fd $moml2
    close $fd

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser resetAll

    set e1 [java::cast ptolemy.actor.TypedCompositeActor [$parser parse $moml2]]

    

    set manager [java::new ptolemy.actor.Manager [$e1 workspace] "myManager"]
    $e1 setManager $manager
    [$e1 getManager] execute
    set recorder [java::cast ptolemy.actor.lib.Recorder [$e1 getEntity "recorder"]]
    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 



proc createHierarchichalModel {container numberOfPubSubsPerLevel levelNumber} {
    global pubCount
    if {$levelNumber == 1} {
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
            set en [java::new ptolemy.actor.TypedCompositeActor $container "en-$n"]
	    #set en $container
	    incr pubCount
	    # createPublisher creates a ramp, scale and publisher,
	    # so we create a ramp, scale and another scale connected to a port
	    #createPublisher $en $channel "pub_a_[expr {$levelNumber - 1}]_$n" [expr {($n + 0.0)/$numberOfPubSubsPerLevel + 1.0}]
	    set nameSuffix "[expr {$levelNumber}]_$n"

	    set ramp [java::new ptolemy.actor.lib.Ramp $en "ramp_$nameSuffix"]
	    set scale [java::new ptolemy.actor.lib.Scale $en "scale_$nameSuffix"]

	    set factor  [expr {($n + 0.0)/$numberOfPubSubsPerLevel + 1.0}]
	    set factorParameter [getParameter $scale factor]
	    $factorParameter setExpression $factor

	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]

	    # scale2 is like the publisher that is created in createPublisher for pubsub
	    set scale2 [java::new ptolemy.actor.lib.Scale $en "scale2_$nameSuffix"]
	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale] output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale2] input]

	    set port1 [java::new ptolemy.actor.TypedIOPort $en "port1_$nameSuffix" false true]
	    puts "port1: levelNumber: $levelNumber, n: $n: port1: port1_$nameSuffix"
	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale2] output] \
		$port1

	    ##set r1 [java::new ptolemy.actor.TypedIORelation $en R1]
	    ##$port1 link $r1
	    #set port2 [java::new ptolemy.actor.TypedIOPort $container "port2_$nameSuffix" true false]
	    #$port2 link $r1
	    #set upperNameSuffix "${levelNumber}_$n"
	    #set scale3 [java::new ptolemy.actor.lib.Scale $container "scale3_$upperNameSuffix"]
	    #$container connect 
	    #	$port2 
	    #	[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input]

	    #set pUpper [java::cast ptolemy.actor.TypedIOPort \
	    #		    [$container getPort "port2_$upperNameSuffix"]]
	    #set scale4 [java::new ptolemy.actor.lib.Scale $container "scale4_$upperNameSuffix"]
	    #$container connect \
	    #	[java::field [java::cast ptolemy.actor.lib.Transformer $scale4] output] \
	    #	$pUpper

        } 
    } else {
	for {set n 1} { $n <= $numberOfPubSubsPerLevel} {incr n} {
	    set en [java::new ptolemy.actor.TypedCompositeActor $container "en-$n"]
   	    createHierarchichalModel $en $numberOfPubSubsPerLevel [expr {$levelNumber - 1}]

	    set addSub [java::new ptolemy.actor.lib.AddSubtract $en addSub]
	    for {set m 1} { $m <= $numberOfPubSubsPerLevel} {incr m} {
		set enInner [java::cast ptolemy.actor.TypedCompositeActor [$en getEntity "en-$m"]]
		set port1Name "port1_[expr {${levelNumber} -1}]_$m"
		set port1 [$enInner getPort $port1Name]
		if {[java::isnull $port1]} {
		    error "levelNumber=$levelNumber, n=$n, m=$m: Could not get port $port1Name from $en. [$en exportMoML]"
		}
		$en connect $port1 [java::field $addSub plus] "R_m$m"
	    }
	    set upperNameSuffix "${levelNumber}_$n"
	    set scale3 [java::new ptolemy.actor.lib.Scale $en "scale3_$upperNameSuffix"]

	    $en connect \
		[java::field $addSub output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input]

	    set port2 [java::new ptolemy.actor.TypedIOPort $en "port1_$upperNameSuffix" false true]
	    puts "port2: levelNumber: $levelNumber, n: $n: port1: port1_$upperNameSuffix"
	    $en connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] output] \
		$port2
	}
    }
}

proc noPubSubAggModel {numberOfSubsPerLevel levels} {
    global pubCount 
    global e0
    set pubCount 0
    set e0 [sdfModel 5]
    createHierarchichalModel $e0 $numberOfSubsPerLevel $levels

    # Connect ports
    set addSub [java::new ptolemy.actor.lib.AddSubtract $e0 addSub]
    for {set n 1} { $n <= $numberOfSubsPerLevel} {incr n} {
        set nameSuffix "${levels}_$n"
	set en [java::cast ptolemy.actor.TypedCompositeActor [$e0 getEntity "en-$n"]]
        set port1Name "port1_[expr {${levels}}]_$n"
	set port1 [$en getPort $port1Name]
	if {[java::isnull $port1]} {
	    error "Could not get port $port1Name from $en\n[$en exportMoML]" 
        }
	$e0 connect $port1 [java::field $addSub plus] "R_$n"
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
    set filename "hierarchicalModel_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $filename w]
    puts $fd [$e0 exportMoML]
    close $fd
    puts "Created $filename, containing [[$e0 deepOpaqueEntityList] size] actors"
    
    [$e0 getManager] execute

    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 
