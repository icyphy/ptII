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

	    $e0 connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale2] output] \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] input]

	    set rec [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "rec_PS20_$i"]]
	    $e0 connect \
		[java::field [java::cast ptolemy.actor.lib.Transformer $scale3] output] \
		[java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
	}

    }
    set n [[$e0 deepEntityList] size]

    puts -nonewline "$n pubs and subs "

    [$e0 getManager] execute
    if {$returnAll == 1} {
	set result {}
	for {set i 1} { $i < $numberOfPubSubs} {incr i} {
	    set rec [java::cast ptolemy.actor.lib.Recorder [$e0 getEntity "rec_PS20_$i"]]

	    lappend result [list [enumToTokenValues [$rec getRecord 0]]]
	}
    } else {
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
    set r "<dataset name=\"$name\">"
    set op "m"
    foreach line $data {
	set nActors [lindex $line 0]
	set time [lindex $line 4]
	set r "$r\n<$op x=\"$nActors\" y=\"$time\"/>"
	set op "p"
    }
    set r "$r\n</dataset>"
    return $r
}

proc memStats {name data} {
    set r "<dataset name=\"$name\">"
    set op "m"
    foreach line $data {
	set nActors [lindex $line 0]
	set mem [lindex $line 7]
	set mem [string range $mem 0 [expr {[string length $mem] - 2 } ]]
	set free [lindex $line 9]
	set free [string range $free 0 [expr {[string length $free] - 2 } ]]
	set used [expr {($mem - $free)/10.0}]
	#puts "$mem $free $used: $line"
	set r "$r\n<$op x=\"$nActors\" y=\"$used\"/>"
	set op "p"
    }
    set r "$r\n</dataset>"
    return $r
}

test PubSub-4.1 {create many} {
    set pubSubStats {}
    set levelxingStats {}
    foreach n {10 20 50 100 200 300 400 500 600 700 800 900 1000 1500 2000 3000 5000 6000 10000 15000 20000} { 
	jdkCapture {
	    set r [nPubSubs $n 0]
        } pubSubStat
        lappend pubSubStats $pubSubStat
	set toplevel [lindex $r 0]
	set filename "pubsub$n.xml"
	set fd [open $filename w]
	puts $fd [$toplevel exportMoML]
	close $fd 
	puts "Created $filename"

	jdkCapture {
	    set r2 [nPubSubs $n 0 0]
        } levelxingStat
        lappend levelxingStats $levelxingStat

	set toplevel [lindex $r 0]
	set filename "levelxing$n.xml"
	set fd [open $filename w]
	puts $fd [$toplevel exportMoML]
	close $fd 
	puts "Created $filename"

	if { [lindex $r 1] != [lindex $r2 1] } {
	    error "Results $r and $r2 are not equal"
	}
    }

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

    puts $plot [timeStats {Level Xing Time in ms.} $levelxingStats]
    puts $plot [memStats {Level Xing Memory in 10K bytes} $levelxingStats]


    puts $plot {</plot>}
    close $plot

    puts "PubSub results"
    puts $pubSubStats
    puts "Level Crossing results"
    puts $levelxingStats
} {}

