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
# Pass arguments to Pxgraph, run it, write the output to
# a variable, sleep, dispose of the Pxgraph, then return the results
# 
#
proc pxgraphTest { args } {
    set jargs [java::new {String[]} [llength $args] $args ]
    set pxgraph [java::new ptolemy.plot.Pxgraph $jargs]
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    $pxgraph write $printStream
    $printStream flush
    set results [$stream toString]
    set thread [java::call Thread currentThread ]
    # sleep 10 seconds
    $thread sleep 10000
    $pxgraph dispose
    return $results
}

########################################
#### pxgraphTest
# Test out set labeling
#
proc Pxgraph-1.x {} {
test Pxgraph-1.1 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 '' -binary ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
DataSet: ''
move: 0.0, -2.0
1.0, 2.0
2.0, 0.0
3.0, 1.0
4.0, 2.0
}

test Pxgraph-1.2 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  $pxgraphfile1 $pxgraphfile2
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
DataSet: Set 1
move: 0.0, 1.0
1.0, 2.0
move: 2.0, 2.5
3.0, 1.0
}

test Pxgraph-1.3 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 "first data set" -1 "second data set" \
	    $pxgraphfile1 $pxgraphfile2
} {# Ptolemy plot, version 2.0
DataSet: first data set
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
DataSet: second data set
move: 0.0, 1.0
1.0, 2.0
move: 2.0, 2.5
3.0, 1.0
}

test Pxgraph-1.4 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 "first data set" $pxgraphfile1 $pxgraphfile2
} {# Ptolemy plot, version 2.0
DataSet: first data set
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
DataSet: Set 1
move: 0.0, 1.0
1.0, 2.0
move: 2.0, 2.5
3.0, 1.0
}

test Pxgraph-1.5 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -1 "second data set" $pxgraphfile1 $pxgraphfile2
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
DataSet: second data set
move: 0.0, 1.0
1.0, 2.0
move: 2.0, 2.5
3.0, 1.0
}

test Pxgraph-1.6 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -2 "second data set" $pxgraphfile1 $pxgraphfile2
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
DataSet: Set 1
move: 0.0, 1.0
1.0, 2.0
move: 2.0, 2.5
3.0, 1.0
}

test Pxgraph-1.7 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 "zero" -binary ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
DataSet: zero
move: 0.0, -2.0
1.0, 2.0
2.0, 0.0
3.0, 1.0
4.0, 2.0
}

test Pxgraph-1.8 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -bar -0 "first data set" -1 "second data set" \
	    $pxgraphfile1 $pxgraphfile2
} {# Ptolemy plot, version 2.0
Lines: off
Bars: 0.5, 0.05
DataSet: first data set
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
DataSet: second data set
move: 0.0, 1.0
move: 1.0, 2.0
move: 2.0, 2.5
move: 3.0, 1.0
}

######################################################################
####
#
test Pxgraph-2.1 {Test out Flags in order} {
    global pxgraphfile1
    pxgraphTest  $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.2 {Flags: -bar} {
    global pxgraphfile1
    pxgraphTest  -bar $pxgraphfile1
} {# Ptolemy plot, version 2.0
Lines: off
Bars: 0.5, 0.05
DataSet: Set 0
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
}

test Pxgraph-2.3 {Flags: -bb (Ignored)} {
    global pxgraphfile1
    pxgraphTest  -bb $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.4 {Flags: -bigendian} {
    global pxgraphfile1
    pxgraphTest  -bigendian ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, -2.0
1.0, 2.0
2.0, 0.0
3.0, 1.0
4.0, 2.0
}

test Pxgraph-2.5 {Flags: -binary} {
    global pxgraphfile1
    pxgraphTest  -binary ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, -2.0
1.0, 2.0
2.0, 0.0
3.0, 1.0
4.0, 2.0
}

test Pxgraph-2.6 {Flags: -bar -binary} {
    global pxgraphfile1
    pxgraphTest  -bar -binary ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
Lines: off
Bars: 0.5, 0.05
DataSet: Set 0
move: 0.0, -2.0
move: 1.0, 2.0
move: 2.0, 0.0
move: 3.0, 1.0
move: 4.0, 2.0
}

test Pxgraph-2.7 {Flags: -db (turn on debugging)} { 
    global pxgraphfile1
    pxgraphTest  -db $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}
test Pxgraph-2.7.5 {Flags: -debug 20 (turn on debugging)} { 
    global pxgraphfile1
    pxgraphTest  -debug 20 $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.8 {Flags: -help} {
    global pxgraphfile1
    # FIXME: need to capture the output here
    #pxgraphTest -help $pxgraphfile1
} {}

test Pxgraph-2.9 {Flags: -littleendian} {
    global pxgraphfile1
    pxgraphTest  -littleendian ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, -2.0
1.0, 2.0
2.0, 0.0
3.0, 1.0
4.0, 2.0
}

test Pxgraph-2.10 {Flags: -lnx (Log X axis)} {
    global pxgraphfile1
    pxgraphTest  -lnx $pxgraphfile1
} {# Ptolemy plot, version 2.0
XLog: on
DataSet: Set 0
0.30102999566398114, 2.0
0.4771212547196623, -0.2
}

test Pxgraph-2.11 {Flags: -lny (Log Y axis)} {
    global pxgraphfile1
    pxgraphTest  -lny $pxgraphfile1
} {# Ptolemy plot, version 2.0
YLog: on
DataSet: Set 0
2.0, 0.30102999566398114
}

test Pxgraph-2.12 {Flags: -m} {
    global pxgraphfile1
    pxgraphTest  -m $pxgraphfile1
} {# Ptolemy plot, version 2.0
Marks: various
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.13 {Flags -M (StyleMarkers)} {
    global pxgraphfile1
    pxgraphTest  -M $pxgraphfile1
} {# Ptolemy plot, version 2.0
Marks: various
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.14 {Flags: -nl (No Lines)} {
    global pxgraphfile1
    pxgraphTest  -nl $pxgraphfile1
} {# Ptolemy plot, version 2.0
Lines: off
DataSet: Set 0
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
}

test Pxgraph-2.15 {Flags: -p (PixelMarkers) } {
    global pxgraphfile1
    pxgraphTest  -p $pxgraphfile1
} {# Ptolemy plot, version 2.0
Marks: points
Marks: dots
Marks: various
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.16 {Flags: -p (PixelsMarkers) -nl } {
    global pxgraphfile1
    pxgraphTest  -p -nl $pxgraphfile1
} {# Ptolemy plot, version 2.0
Marks: points
Marks: dots
Marks: various
Lines: off
DataSet: Set 0
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
}

test Pxgraph-2.17 {Flags: -P (LargePixels) } {
    global pxgraphfile1
    pxgraphTest  -P $pxgraphfile1
} {# Ptolemy plot, version 2.0
Marks: dots
Marks: various
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.18 {Flags: -p -nl -binary} {
    pxgraphTest  -P -nl -binary ../demo/data/bin.plt
} {# Ptolemy plot, version 2.0
Marks: dots
Marks: various
Lines: off
DataSet: Set 0
move: 0.0, -2.0
move: 1.0, 2.0
move: 2.0, 0.0
move: 3.0, 1.0
move: 4.0, 2.0
}

test Pxgraph-2.19 {Flags -p -nl} {
    global pxgraphfile1
    pxgraphTest  -P -nl $pxgraphfile1
} {# Ptolemy plot, version 2.0
Marks: dots
Marks: various
Lines: off
DataSet: Set 0
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
}

test Pxgraph-2.20 {Flags: -rv (Reverse Video)} {
    global pxgraphfile1
    # FIXME: The write output does not capture -rv
    pxgraphTest  -rv $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-2.21 {Flags: -tk (Ticks)} {
    global pxgraphfile1
    pxgraphTest  -tk $pxgraphfile1
} {}

test Pxgraph-2.22 {Flags: -v (Version)} {
    global pxgraphfile1
    pxgraphTest  -v $pxgraphfile1
} {}
}
set VERBOSE 1
######################################################################
####
#
test Pxgraph-3.1 {Options: -bd <color> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -bd blue $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.2 {Options: -bg <color> } {
    global $pxgraphfile1
    #FIXME: the background is not written out
    pxgraphTest  -bg red $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}


test Pxgraph-3.3 {Options: -brb <base> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -brb 1.0 -bar $pxgraphfile1
} {# Ptolemy plot, version 2.0
Lines: off
Bars: 0.5, 0.05
DataSet: Set 0
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
}

test Pxgraph-3.4 { Options -brw <width> } {
    global $pxgraphfile1
    pxgraphTest  -brw 0.8 -bar $pxgraphfile1
} {# Ptolemy plot, version 2.0
Lines: off
Bars: 0.8, 0.0
DataSet: Set 0
move: 0.0, 0.0
move: 2.0, 2.0
move: 3.0, -0.2
}

test Pxgraph-3.5 {Options:  -fg <color> } {
    global $pxgraphfile1
    #FIXME: the foreground is not written out
    pxgraphTest  -fg green $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.6 {Options:  -gw <pixels> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -gw 10 $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.7 {Options:  -lf <label fontname> } {
    global $pxgraphfile1
    # FIXME: the label font is not stored
    pxgraphTest  -lf helvetica-ITALIC-20 $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.8 {Options:  -lx <xl,xh>} {
    global $pxgraphfile1
    pxgraphTest  -lx 0.5,1.5 $pxgraphfile1
} {# Ptolemy plot, version 2.0
XRange: 0.5, 1.5
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.9 {Options:  -ly <yl,yh>} {
    global $pxgraphfile1
    pxgraphTest  -ly 0.5,1.5 $pxgraphfile1
} {# Ptolemy plot, version 2.0
YRange: 0.5, 1.5
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.10 {Options:  -lx <xl,xh>  -ly <yl,yh> } {
    global $pxgraphfile1
    pxgraphTest  -lx 0.5,1.5 -ly 0.5,1.5 $pxgraphfile1
} {# Ptolemy plot, version 2.0
XRange: 0.5, 1.5
YRange: 0.5, 1.5
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.11 {Options: -t <title> } {
    global $pxgraphfile1
    pxgraphTest  -t "This is the Title" $pxgraphfile1
} {# Ptolemy plot, version 2.0
TitleText: This is the Title
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.12 {Options: -tf <fontname> } {
    global $pxgraphfile1
    # FIXME: the title font is not written out
    pxgraphTest  -tf Courier-BOLD-16 $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.13 {Options: -x -y} {
    global $pxgraphfile1
    pxgraphTest  -x Years -y "$ Profit" $pxgraphfile1
} {# Ptolemy plot, version 2.0
XLabel: Years
YLabel: $ Profit
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.14 {Option: -zg <color> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -zg Yellow $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.15 {Option: -zw <width> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -zw 5 $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

test Pxgraph-3.16 {Option: =WxH+X+Y} {
    global $pxgraphfile1
    pxgraphTest  =200x250+300+350 $pxgraphfile1
} {# Ptolemy plot, version 2.0
DataSet: Set 0
move: 0.0, 0.0
2.0, 2.0
3.0, -0.2
}

    # Test out stdin
    #pxgraphTest  < ../demo/data.plt

######################################################################
####
#
test Pxgraph-4.1 {Test out file args} {
    # Test out file args
    pxgraphTest  ../demo/bargraph.plt

} {# Ptolemy plot, version 2.0
TitleText: Software Downloads
XLabel: Year
YLabel: Downloads
XRange: 0.0, 10.0
YRange: 0.0, 10000.0
XTicks: "1993" 0.0, "1994" 1.0, "1995" 2.0, "1996" 3.0, "1997" 4.0, "1998" 5.0, "1999" 6.0, "2000" 7.0, "2001" 8.0, "2002" 9.0, "2003" 10.0
Lines: off
Bars: 0.5, 0.2
DataSet: program a
move: 0.0, 100.0
move: 1.0, 300.0
move: 2.0, 600.0
move: 3.0, 1000.0
move: 4.0, 4000.0
move: 5.0, 6000.0
move: 6.0, 3000.0
move: 7.0, 1000.0
move: 8.0, 400.0
DataSet: program b
move: 2.0, 50.0
move: 3.0, 100.0
move: 4.0, 800.0
move: 5.0, 400.0
move: 6.0, 1000.0
move: 7.0, 5000.0
move: 8.0, 2000.0
move: 9.0, 300.0
move: 10.0, 0.0
DataSet: program c
move: 3.0, 10.0
move: 4.0, 100.0
move: 5.0, 400.0
move: 6.0, 2000.0
move: 7.0, 5000.0
move: 8.0, 9000.0
move: 9.0, 7000.0
move: 10.0, 1000.0
}

test Pxgraph-4.1 {Test out file args} {
    # Test out file args
    pxgraphTest  http://ptolemy.eecs.berkeley.edu/java/ptplot/demo/data.plt
} {# Ptolemy plot, version 2.0
TitleText: My Plot
XLabel: X Axis
YLabel: Y Axis
XTicks: "zero" 0.0, "one" 1.0, "two" 2.0, "three" 3.0, "four" 4.0, "five" 5.0
Grid: off
Color: off
Marks: various
Lines: off
DataSet: dot
move: 0.0, -4.0
1.0, -3.0
2.0, -2.0
3.0, -1.0
4.0, 0.0
DataSet: cross
move: 0.0, -3.5
1.0, -2.5
2.0, -1.5
3.0, -0.5
4.0, 0.5
DataSet: square
move: 0.0, -3.0
move: 1.0, -2.0
move: 2.0, -1.0
move: 3.0, 0.0
move: 4.0, 1.0
DataSet: triangle
move: 0.0, -2.5
move: 1.0, -1.5
move: 2.0, -0.5
move: 3.0, 0.5
move: 4.0, 1.5
DataSet: diamond
move: 0.0, -2.0
move: 1.0, -1.0
move: 2.0, 0.0
move: 3.0, 1.0
move: 4.0, 2.0
DataSet: circle
move: 0.0, -1.5
move: 1.0, -0.5
move: 2.0, 0.5
move: 3.0, 1.5
move: 4.0, 2.5
DataSet: plus
move: 0.0, -1.0
move: 1.0, 0.0
move: 2.0, 1.0
move: 3.0, 2.0
move: 4.0, 3.0
DataSet: square
move: 0.0, -0.5
move: 1.0, 0.5
move: 2.0, 1.5
move: 3.0, 2.5
move: 4.0, 3.5
DataSet: triangle
move: 0.0, 0.0
move: 1.0, 1.0
move: 2.0, 2.0
move: 3.0, 3.0
move: 4.0, 4.0
DataSet: diamond
move: 0.0, 0.5
move: 1.0, 1.5
move: 2.0, 2.5
move: 3.0, 3.5
move: 4.0, 4.5
DataSet: dot
move: 0.0, 1.0
move: 1.0, 2.0
move: 2.0, 3.0
move: 3.0, 4.0
}

######################################################################
####
#
test Pxgraph-5.1 {Ptolemy Example} {
    pxgraphTest  -binary -t "Integrator Demo" -P \
	    -x n =800x400+0+0 -1 control -0 final \
	    ../demo/data/integrator1.plt ../demo/data/integrator2.plt
} {# Ptolemy plot, version 2.0
TitleText: Integrator Demo
XLabel: n
Marks: dots
Marks: various
DataSet: final
move: 0.0, 0.0
1.0, 1.0
2.0, 2.700000047683716
3.0, 4.889999866485596
4.0, 7.422999858856201
5.0, 5.0
6.0, 9.5
7.0, 13.649999618530273
8.0, 17.55500030517578
9.0, 21.28849983215332
10.0, 24.90195083618164
11.0, 11.0
12.0, 19.700000762939453
13.0, 26.790000915527344
14.0, 32.75299835205078
15.0, 37.927101135253906
16.0, 42.54896926879883
17.0, 17.0
18.0, 29.899999618530273
19.0, 39.93000030517578}

file delete -force pxgraphfile1 pxgraphfile2
