# Tests for the Pxgraph class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

########################################
#### pxgraphFiles
# Create two test files pxgraphfile1 pxgraphfile2
#
proc pxgraphFiles {} {
    global pxgraphfile1 pxgraphfile2
    set pxgraphfile1 /tmp/pxgrapfile1_[pid].plt
    set pxgraphfile2 /tmp/pxgrapfile2_[pid].plt
    set fd [open $pxgraphfile1 w]
    puts $fd "0  0\n1	1 \n2,2\n3 -0.2" 
    close $fd

    set fd [open $pxgraphfile2 w]
    puts $fd "0 1\n		1 2\nmove:2 2.5\n 3 1"
    close $fd
}
pxgraphFiles

########################################
#### pxgraphTest
# Pass arguments to Pxgraph, run it, sleep, then return
#
proc pxgraphTest { args } {
    set jargs [java::new {String[]} [llength $args] $args ]
    set pxgraph [java::new ptolemy.plot.Pxgraph $jargs]
    set thread [java::call Thread currentThread ]
    # sleep 2 seconds
    $thread sleep 2000
    $pxgraph dispose
}
######################################################################
####
#
test Pxgraph-1.1 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 '' -binary ../demo/data/bin.plt
    pxgraphTest  $pxgraphfile1 $pxgraphfile2
    pxgraphTest  -0 "first data set" -1 "second data set" \
	    $pxgraphfile1 $pxgraphfile2
    pxgraphTest  -0 "first data set" $pxgraphfile1 $pxgraphfile2
    pxgraphTest  -1 "second data set" $pxgraphfile1 $pxgraphfile2
    pxgraphTest  -2 "second data set" $pxgraphfile1 $pxgraphfile2
    pxgraphTest  -0 "zero" -binary ../demo/data/bin.plt


    pxgraphTest  -bar -0 "first data set" -1 "second data set" \
	    $pxgraphfile1 $pxgraphfile2
} {}

######################################################################
####
#
test Pxgraph-2.1 {Test out Flags in order} {
    pxgraphTest  $pxgraphfile1
    pxgraphTest  -bar $pxgraphfile1
    pxgraphTest  -bb $pxgraphfile1
    pxgraphTest  -bigendian ../demo/data/bin.plt
    pxgraphTest  -binary ../demo/data/bin.plt
    pxgraphTest  -bar -binary ../demo/data/bin.plt
    pxgraphTest  -db $pxgraphfile1
    #pxgraphTest -help $pxgraphfile1
    pxgraphTest  -littleendian ../demo/data/bin.plt
    pxgraphTest  -lnx $pxgraphfile1
    pxgraphTest  -lny $pxgraphfile1
    pxgraphTest  -m $pxgraphfile1
    pxgraphTest  -M $pxgraphfile1
    pxgraphTest  -nl $pxgraphfile1
    pxgraphTest  -p $pxgraphfile1
    pxgraphTest  -p -nl $pxgraphfile1
    pxgraphTest  -P $pxgraphfile1
    pxgraphTest  -P -nl -binary ../demo/data/bin.plt
    pxgraphTest  -P -nl $pxgraphfile1
    pxgraphTest  -rv $pxgraphfile1
    pxgraphTest  -tk $pxgraphfile1
    pxgraphTest  -v $pxgraphfile1
} {}

######################################################################
####
#
test Pxgraph-3.1 {Test out Options in order} {
    pxgraphTest  -bd blue $pxgraphfile1
    pxgraphTest  -bg red $pxgraphfile1
    pxgraphTest  -brb 1.0 -bar $pxgraphfile1
    pxgraphTest  -brw 0.8 -bar $pxgraphfile1
    pxgraphTest  -fg green $pxgraphfile1
    pxgraphTest  -gw 10 $pxgraphfile1
    pxgraphTest  -lf helvetica-ITALIC-20 $pxgraphfile1
    pxgraphTest  -lx 0.5,1.5 $pxgraphfile1
    pxgraphTest  -ly 0.5,1.5 $pxgraphfile1
    pxgraphTest  -lx 0.5,1.5 -ly 0.5,1.5 $pxgraphfile1
    pxgraphTest  -t "This is the Title" $pxgraphfile1
    pxgraphTest  -tf Courier-BOLD-16 $pxgraphfile1
    pxgraphTest  -x Years -y "$ Profit" $pxgraphfile1
    pxgraphTest  -zg Yellow $pxgraphfile1
    pxgraphTest  -zw 5 $pxgraphfile1

    # Test out stdin
    #pxgraphTest  < ../demo/data.plt
} {}
######################################################################
####
#
test Pxgraph-4.1 {Test out file args} {
    # Test out file args
    pxgraphTest  ../demo/bargraph.plt
    pxgraphTest  http://ptolemy.eecs.berkeley.edu/java/ptplot/demo/data.plt
} {}

######################################################################
####
#
test Pxgraph-5.1 {Ptolemy Example} {
    pxgraphTest  -binary -t "Integrator Demo" -P \
	    -x n =800x400+0+0 -1 control -0 final \
	    ../demo/data/integrator1.plt demo/data/integrator2.plt
} {}

file delete -force pxgraphfile1 pxgraphfile2
