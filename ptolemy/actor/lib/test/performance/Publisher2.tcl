# Test Publisher
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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
if {[string compare test [info procs createPublisher]] == 1} then {
    source PublisherCommon.tcl
} {}

test PubSub-1.0 {One pub, one sub} {
    set e0 [sdfModel 5]
    set sdfDirector [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
    [java::field $sdfDirector allowDisconnectedGraphs] setExpression true
    createPublisher $e0 PubSub10 PS10
    set rec [createSubscriber $e0 PubSub10 PS10]
    [$e0 getManager] execute
    list [enumToTokenValues [$rec getRecord 0]]
} {{0.0 1.1 2.2 3.3 4.4}}

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

