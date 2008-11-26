# Test Publisher
#
# @Author: Christopher Brooks
#
# @Version: $Id: Publisher.tcl 47596 2007-12-18 00:51:11Z cxh $
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

# Create a model with lots of Scale actors
proc createPublisher {e0 channel nameSuffix {factor 1.1} } {
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 "ramp_$nameSuffix"]
    set scale [java::new ptolemy.actor.lib.Scale $e0 "scale_$nameSuffix"]
    set publisher [java::new ptolemy.actor.lib.Publisher $e0 "publisher_$nameSuffix"]

    set factorParameter [getParameter $scale factor]
    $factorParameter setExpression $factor

    set channelParameter [getParameter $publisher channel]
    $channelParameter setExpression $channel

    $publisher attributeChanged $channelParameter

    $e0 connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $scale] input]

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

test PubSub-1.0 {One pub, one sub} {
    set e0 [sdfModel 5]
    set sdfDirector [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
    createPublisher $e0 PubSub10 PS10
    set rec [createSubscriber $e0 PubSub10 PS10]
    [$e0 getManager] execute
    list [enumToTokenValues [$rec getRecord 0]]
} {{0.0 1.1 2.2 3.3 4.4}}

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

test PubSub-2.0 {5 pubs and subs} {
    set r [lindex [nPubSubs 5 0] 1]
    list $r
} {{0.0 1.8 3.6 5.4 7.2}}

test PubSub-2.1 {5 pubs and subs} {
    set r [lindex [nPubSubs 5 1] 1]
    list $r
} {{{{0.0 1.2 2.4 3.6 4.8}} {{0.0 1.4 2.8 4.2 5.6}} {{0.0 1.6 3.2 4.8 6.4}} {{0.0 1.8 3.6 5.4 7.2}}}}

test PubSub-3.1 {50 pubs and subs} {
    puts "[time {set r [lindex [nPubSubs 50 0] 1]}] to create 50"
    list $r
} {{0.0 1.98 3.96 5.94 7.92}}

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

test PubSub-4.1 {create many} {
    set pubSubStats {}
    set levelxingStats {}
    set runtime [java::call Runtime getRuntime]
    foreach n {10 20 50 100 200 300 400 500 600 700 800 900 1000 2000 5000 10000 20000} {
	puts "\n======"
	java::call System gc
	puts "After gc: Memory: [expr {[$runtime totalMemory]/1000}] K Free: [expr {[$runtime freeMemory]/1000}] K"
        set e0 [createPubSubModel $n 0 1]
	set filename "pubsub$n.xml"
	set fd [open $filename w]
	puts $fd [$e0 exportMoML]
	close $fd 
	puts "Wrote $filename"

#	puts "Running model with $n pub/subs"
#	jdkCapture {
# 	    #exec java -classpath $PTII ptolemy.actor.gui.MoMLSimpleStatisticalApplication $filename
#	    set r [executeModel $n $e0 0]
#        } pubSubStat
#	puts "pubsub $pubSubStat"
#        lappend pubSubStats $pubSubStat
#	$e0 setContainer [java::null]

# 	java::call System gc
#         set e0 [createPubSubModel $n 0 0]
#  	set filename "levelxing$n.xml"
#  	set fd [open $filename w]
#  	puts $fd [$e0 exportMoML]
#  	close $fd 
#  	puts "Created $filename"

#  	puts "\nRunning model with $n level crossing links"
#  	jdkCapture {
# 	    # MoML does not work with level crossing links, see
# 	    # https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=217
#  	    #exec java -classpath $PTII ptolemy.actor.gui.MoMLSimpleStatisticalApplication $filename
#  	    set r2 [executeModel $n $e0 0]
#          } levelxingStat
# 	 $e0 setContainer [java::null]
#          puts "levelxing $levelxingStat"
#          lappend levelxingStats $levelxingStat

	#if { [lindex $r 1] != [lindex $r2 1] } {
	#    error "Results $r and $r2 are not equal"
	#}
    }
    #plotStats $pubSubStats $levelxingStats
#    plotStats $pubSubStats

} {}



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
	    set subscriber [java::new ptolemy.actor.lib.Subscriber $container "sub_${levelNumber}_$n"]

	    set channelParameter [getParameter $subscriber channel]
	    $channelParameter setExpression $channel
	    $subscriber attributeChanged $channelParameter

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

	    #set channel "PubSub_${pubCount}_[expr {$levelNumber - 1}]_$n"
	    #set channel2 "PubSub_${pubCount}_${levelNumber}_$n"

	    incr pubCount
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


proc pubSubAggModel {numberOfSubsPerLevel levels} {
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
    set filename "pubSubAgg_${numberOfSubsPerLevel}_${levels}.xml"
    set fd [open $filename w]
    puts $fd [$e0 exportMoML]
    close $fd
    puts "Created $filename, containing [[$e0 deepOpaqueEntityList] size] actors"
    
    [$e0 getManager] execute

    return [list [enumToTokenValues [$recorder getRecord 0]]]
} 

test PubSub-5.2.1 {Use Hierarchy} {
    pubSubAggModel 2 1
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSub-5.2.2 {Use Hierarchy} {
    pubSubAggModel 2 2
} {{0.0 3.5 7.0 10.5 14.0}}

test PubSub-5.3.1 {Use Hierarchy} {
    pubSubAggModel 3 1
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSub-5.3.2 {Use Hierarchy} {
    pubSubAggModel 3 2
} {{0.0 5.0 10.0 15.0 20.0}}

test PubSub-5.3.3 {Use Hierarchy} {
    pubSubAggModel 3 3
} {{0.0 5.0 10.0 15.0 20.0}}
