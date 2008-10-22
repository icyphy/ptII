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

proc plotStats {pubSubStats levelxingStats} {
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
}


set pubSubStats {{45 pubs and subs 25 ms. Memory: 31360K Free: 16644K (53%)
} {95 pubs and subs 51 ms. Memory: 56320K Free: 32022K (57%)
} {245 pubs and subs 243 ms. Memory: 80640K Free: 66925K (83%)
} {495 pubs and subs 208 ms. Memory: 112832K Free: 36921K (33%)
} {995 pubs and subs 637 ms. Memory: 200896K Free: 163735K (82%)
} {1495 pubs and subs 626 ms. Memory: 292480K Free: 64365K (22%)
} {1995 pubs and subs 1196 ms. Memory: 409280K Free: 167431K (41%)
} {2495 pubs and subs 2056 ms. Memory: 621120K Free: 212842K (34%)
} {2995 pubs and subs 3083 ms. Memory: 849856K Free: 368024K (43%)
} {3495 pubs and subs 4153 ms. Memory: 1210048K Free: 786758K (65%)
} {3995 pubs and subs 4209 ms. Memory: 1447040K Free: 655967K (45%)
} {4495 pubs and subs 7199 ms. Memory: 1435776K Free: 812667K (57%)
} {4995 pubs and subs 8375 ms. Memory: 1727360K Free: 1012097K (59%)
} {7495 pubs and subs 14838 ms. Memory: 1750528K Free: 301986K (17%)
} {9995 pubs and subs 29479 ms. Memory: 1929216K Free: 558617K (29%)
} {14995 pubs and subs preinitialize() finished: 60703 ms. Memory: 2206784K Free: 1057615K (48%)
Manager.initialize() finished: 60744 ms. Memory: 2206784K Free: 1037009K (47%)
60959 ms. Memory: 2206784K Free: 1037009K (47%)
} {24995 pubs and subs preinitialize() finished: 154321 ms. Memory: 2925632K Free: 1195173K (41%)
Manager.initialize() finished: 154390 ms. Memory: 2925632K Free: 1168804K (40%)
154756 ms. Memory: 2925632K Free: 1168804K (40%)
} }

set levelxingStats {{45 pubs and subs 29 ms. Memory: 31360K Free: 2109K (7%)
} {95 pubs and subs 92 ms. Memory: 56320K Free: 1886K (3%)
} {245 pubs and subs 178 ms. Memory: 101440K Free: 70064K (69%)
} {495 pubs and subs 207 ms. Memory: 136256K Free: 42760K (31%)
} {995 pubs and subs 465 ms. Memory: 245184K Free: 185295K (76%)
} {1495 pubs and subs 714 ms. Memory: 345984K Free: 142922K (41%)
} {1995 pubs and subs 1043 ms. Memory: 490816K Free: 178930K (36%)
} {2495 pubs and subs 1672 ms. Memory: 728896K Free: 212458K (29%)
} {2995 pubs and subs 2343 ms. Memory: 979584K Free: 383797K (39%)
} {3495 pubs and subs 3043 ms. Memory: 1300672K Free: 101315K (8%)
} {3995 pubs and subs 4237 ms. Memory: 1378816K Free: 486045K (35%)
} {4495 pubs and subs 7161 ms. Memory: 1629376K Free: 1097324K (67%)
} {4995 pubs and subs 6171 ms. Memory: 1747840K Free: 227272K (13%)
} {7495 pubs and subs 16861 ms. Memory: 1869696K Free: 781608K (42%)
} {9995 pubs and subs 25499 ms. Memory: 2107648K Free: 962237K (46%)
} {14995 pubs and subs preinitialize() finished: 60282 ms. Memory: 2436224K Free: 256640K (11%)
Manager.initialize() finished: 60322 ms. Memory: 2436224K Free: 256640K (11%)
60561 ms. Memory: 2436224K Free: 234842K (10%)
} {24995 pubs and subs preinitialize() finished: 154758 ms. Memory: 3225216K Free: 283676K (9%)
Manager.initialize() finished: 154826 ms. Memory: 3225216K Free: 283676K (9%)
155225 ms. Memory: 3225216K Free: 283676K (9%)
} }

#plotStats $pubSubStats $levelxingStats


test PubSub-4.1 {create many} {
    set pubSubStats {}
    set levelxingStats {}
    foreach n {10 20 50 100 200 300 400 500 600 700 800 900 1000} {
	puts "\n======"
	puts "Running model with $n pub/subs"
	jdkCapture {
	    set r [nPubSubs $n 0]
        } pubSubStat
	puts "pubsub $pubSubStat"
        lappend pubSubStats $pubSubStat
	set toplevel [lindex $r 0]
	set filename "pubsub$n.xml"
	set fd [open $filename w]
	puts $fd [$toplevel exportMoML]
	close $fd 
	puts "Wrote $filename"

	puts "\nRunning model with $n level crossing links"
	jdkCapture {
	    set r2 [nPubSubs $n 0 0]
        } levelxingStat
	puts "levelxing $levelxingStat"
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
    #plotStats $pubSubStats $levelxingStats

} {}

