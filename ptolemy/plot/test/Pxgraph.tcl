# Tests for the Pxgraph class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1998-2012 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1

# StringUtilities.exit() checks the ptolemy.ptII.doNotExit property
java::call System setProperty ptolemy.ptII.doNotExit true

set USER [java::call System getProperty user.name]


########################################
#### pxgraphFiles
# Create two test files pxgraphfile1 pxgraphfile2
#
proc pxgraphFiles {} {
    global pxgraphfile1 pxgraphfile2 tcl_platform USER
    if { $tcl_platform(host_platform) == "windows"} {
	set pxgraphfile1 pxgraphfile1-${USER}.plt
	set pxgraphfile2 pxgraphfile2-${USER}.plt
    } else {
	set pxgraphfile1 /tmp/pxgraphfile1-${USER}.plt
	set pxgraphfile2 /tmp/pxgraphfile2-${USER}.plt
    }

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
    global defaultPlotMLHeader
    set jargs [java::new {String[]} [llength $args] $args ]
    set pxgraph [java::new ptolemy.plot.compat.PxgraphApplication $jargs]
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    set plotFrame [java::cast ptolemy.plot.PlotFrame $pxgraph ]
    set plot [java::field $plotFrame plot]

    set toolkit [java::call java.awt.Toolkit getDefaultToolkit]
    $toolkit sync

    $plot write $printStream "Usually, the DTD would go here"
    $printStream flush
    $printStream close
    set results [$stream toString]
    set thread [java::call Thread currentThread ]
    # sleep 0.5 seconds
    $thread sleep 500
    $pxgraph dispose
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
                $results "\n" results2
    # In PlotBox, if _title is null, then write() does not emit a  <title></title>
    # This occurs on hudson as part of the nightly build:
    # Xvfb :2 -screen 0 1024x768x24 &
    # export DISPLAY=localhost:2.0
    # ant test.single -Dtest.name=ptolemy.plot.test.junit.JUnitTclTest -Djunit.formatter=plain
    regsub -all "<title></title>\n" \
                $results2 "" results3
    regsub -all {<!-- Ptolemy plot, version .* -->} $results3 "<!-- Ptolemy plot, version XXX -->" results4
    return $results4
}

########################################
#### pxgraphTest
# Test out set labeling
#
test Pxgraph-1.1 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 {''} -binary ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="''">
<m x="0.0" y="-2.0"/>
<p x="1.0" y="2.0"/>
<p x="2.0" y="0.0"/>
<p x="3.0" y="1.0"/>
<p x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-1.2 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  $pxgraphfile1 $pxgraphfile2
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
<dataset>
<m x="0.0" y="1.0"/>
<p x="1.0" y="2.0"/>
<m x="2.0" y="2.5"/>
<p x="3.0" y="1.0"/>
</dataset>
</plot>
}

test Pxgraph-1.3 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 "first data set" -1 "second data set" \
	    $pxgraphfile1 $pxgraphfile2
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="first data set">
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
<dataset name="second data set">
<m x="0.0" y="1.0"/>
<p x="1.0" y="2.0"/>
<m x="2.0" y="2.5"/>
<p x="3.0" y="1.0"/>
</dataset>
</plot>
}

# Sleep so that things can catch up
set thread [java::call Thread currentThread ]
# sleep 0.5 seconds
$thread sleep 500

test Pxgraph-1.4 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 "first data set" $pxgraphfile1 $pxgraphfile2
}  {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="first data set">
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
<dataset>
<m x="0.0" y="1.0"/>
<p x="1.0" y="2.0"/>
<m x="2.0" y="2.5"/>
<p x="3.0" y="1.0"/>
</dataset>
</plot>
}

test Pxgraph-1.5 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -1 "second data set" $pxgraphfile1 $pxgraphfile2
}  {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
<dataset name="second data set">
<m x="0.0" y="1.0"/>
<p x="1.0" y="2.0"/>
<m x="2.0" y="2.5"/>
<p x="3.0" y="1.0"/>
</dataset>
</plot>
}

test Pxgraph-1.6 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -2 "second data set" $pxgraphfile1 $pxgraphfile2
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
<dataset>
<m x="0.0" y="1.0"/>
<p x="1.0" y="2.0"/>
<m x="2.0" y="2.5"/>
<p x="3.0" y="1.0"/>
</dataset>
<dataset name="second data set">
</dataset>
</plot>
}

test Pxgraph-1.7 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -0 "zero" -binary ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="zero">
<m x="0.0" y="-2.0"/>
<p x="1.0" y="2.0"/>
<p x="2.0" y="0.0"/>
<p x="3.0" y="1.0"/>
<p x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-1.8 {Test set labeling} {
    global pxgraphfile1 pxgraphfile2
    pxgraphTest  -bar -0 "first data set" -1 "second data set" \
	    $pxgraphfile1 $pxgraphfile2
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no"/>
<barGraph width="0.5" offset="0.05"/>
<dataset name="first data set">
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
<dataset name="second data set">
<m x="0.0" y="1.0"/>
<m x="1.0" y="2.0"/>
<m x="2.0" y="2.5"/>
<m x="3.0" y="1.0"/>
</dataset>
</plot>
}

######################################################################
####
#
test Pxgraph-2.1 {Test out Flags in order} {
    global pxgraphfile1
    pxgraphTest  $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.2 {Flags: -bar} {
    global pxgraphfile1
    pxgraphTest  -bar $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no"/>
<barGraph width="0.5" offset="0.05"/>
<dataset>
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.3 {Flags: -bb (Ignored)} {
    global pxgraphfile1
    pxgraphTest  -bb $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.4 {Flags: -bigendian} {
    global pxgraphfile1
    pxgraphTest  -bigendian ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="Set 0">
<m x="0.0" y="-2.0"/>
<p x="1.0" y="2.0"/>
<p x="2.0" y="0.0"/>
<p x="3.0" y="1.0"/>
<p x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-2.5 {Flags: -binary} {
    global pxgraphfile1
    pxgraphTest  -binary ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="Set 0">
<m x="0.0" y="-2.0"/>
<p x="1.0" y="2.0"/>
<p x="2.0" y="0.0"/>
<p x="3.0" y="1.0"/>
<p x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-2.6 {Flags: -bar -binary} {
    global pxgraphfile1
    pxgraphTest  -bar -binary ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no"/>
<barGraph width="0.5" offset="0.05"/>
<dataset name="Set 0">
<m x="0.0" y="-2.0"/>
<m x="1.0" y="2.0"/>
<m x="2.0" y="0.0"/>
<m x="3.0" y="1.0"/>
<m x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-2.7 {Flags: -db (turn on debugging)} { 
    global pxgraphfile1
    pxgraphTest  -db $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.8 {Flags: -help} {
    global pxgraphfile1
    # FIXME: need to capture the output here
    #pxgraphTest -help $pxgraphfile1
} {}

test Pxgraph-2.9 {Flags: -littleendian} {
    global pxgraphfile1
    pxgraphTest  -littleendian ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset name="Set 0">
<m x="0.0" y="-2.0"/>
<p x="1.0" y="2.0"/>
<p x="2.0" y="0.0"/>
<p x="3.0" y="1.0"/>
<p x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-2.10 {Flags: -lnx (Log X axis)} {
    global pxgraphfile1
    pxgraphTest  -lnx $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<xLog/>
<dataset>
<m x="0.0" y="1.0"/>
<p x="0.30102999566398114" y="2.0"/>
<p x="0.4771212547196624" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.11 {Flags: -lny (Log Y axis)} {
    global pxgraphfile1
    pxgraphTest  -lny $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<yLog/>
<dataset>
<m x="1.0" y="0.0"/>
<p x="2.0" y="0.30102999566398114"/>
</dataset>
</plot>
}

test Pxgraph-2.12 {Flags: -m} {
    global pxgraphfile1
    pxgraphTest  -m $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default marks="various"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.13 {Flags -M (StyleMarkers)} {
    global pxgraphfile1
    pxgraphTest  -M $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default marks="various"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.14 {Flags: -nl (No Lines)} {
    global pxgraphfile1
    pxgraphTest  -nl $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no"/>
<dataset>
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.15 {Flags: -p (PixelMarkers) } {
    global pxgraphfile1
    pxgraphTest  -p $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default marks="points"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.16 {Flags: -p (PixelsMarkers) -nl } {
    global pxgraphfile1
    pxgraphTest  -p -nl $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no" marks="points"/>
<dataset>
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.17 {Flags: -P (LargePixels) } {
    global pxgraphfile1
    pxgraphTest  -P $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default marks="dots"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.18 {Flags: -p -nl -binary} {
    pxgraphTest  -P -nl -binary ../demo/data/bin.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no" marks="dots"/>
<dataset name="Set 0">
<m x="0.0" y="-2.0"/>
<m x="1.0" y="2.0"/>
<m x="2.0" y="0.0"/>
<m x="3.0" y="1.0"/>
<m x="4.0" y="2.0"/>
</dataset>
</plot>
}

test Pxgraph-2.19 {Flags -p -nl} {
    global pxgraphfile1
    pxgraphTest  -P -nl $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no" marks="dots"/>
<dataset>
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.20 {Flags: -rv (Reverse Video)} {
    global pxgraphfile1
    # FIXME: The write output does not capture -rv
    pxgraphTest  -rv $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-2.21 {Flags: -tk (Ticks)} {
    global pxgraphfile1
    pxgraphTest  -tk $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<noGrid/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}


test Pxgraph-2.22 {Flags: -v (Version)} {
    set args [java::new {String[]} 1 {-v}]
    
    jdkCaptureOutAndErr {
	java::new ptolemy.plot.compat.PxgraphApplication $args
    } results err2_22
    regsub {Version .*, Build.*} $results {Version XXX, Build YYY} results2 
    list $results2
} {{Version XXX, Build YYY
}}

# Sleep so that things can catch up
set thread [java::call Thread currentThread ]
# sleep 0.5 seconds
$thread sleep 500


######################################################################
####
#
test Pxgraph-3.1 {Options: -bd <color> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -bd blue $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.2 {Options: -bg <color> } {
    global $pxgraphfile1
    #FIXME: the background is not written out
    pxgraphTest  -bg red $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}


test Pxgraph-3.3 {Options: -brb <base> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -brb 1.0 -bar $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no"/>
<barGraph width="0.5" offset="0.05"/>
<dataset>
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.4 { Options -brw <width> } {
    global $pxgraphfile1
    pxgraphTest  -brw 0.8 -bar $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<default connected="no"/>
<barGraph width="0.8" offset="0.0"/>
<dataset>
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.5 {Options:  -fg <color> } {
    global $pxgraphfile1
    #FIXME: the foreground is not written out
    pxgraphTest  -fg green $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.6 {Options:  -gw <pixels> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -gw 10 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.7 {Options:  -lf <label fontname> } {
    global $pxgraphfile1
    # FIXME: the label font is not stored
    pxgraphTest  -lf helvetica-ITALIC-20 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.8 {Options:  -lx <xl,xh>} {
    global $pxgraphfile1
    pxgraphTest  -lx 0.5,1.5 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<xRange min="0.5" max="1.5"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.9 {Options:  -ly <yl,yh>} {
    global $pxgraphfile1
    pxgraphTest  -ly 0.5,1.5 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<yRange min="0.5" max="1.5"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.10 {Options:  -lx <xl,xh>  -ly <yl,yh> } {
    global $pxgraphfile1
    pxgraphTest  -lx 0.5,1.5 -ly 0.5,1.5 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<xRange min="0.5" max="1.5"/>
<yRange min="0.5" max="1.5"/>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.11 {Options: -t <title> } {
    global $pxgraphfile1
    pxgraphTest  -t "This is the Title" $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<title>This is the Title</title>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.12 {Options: -tf <fontname> } {
    global $pxgraphfile1
    # FIXME: the title font is not written out
    pxgraphTest  -tf Courier-BOLD-16 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.13 {Options: -x -y} {
    global $pxgraphfile1
    pxgraphTest  -x Years -y "$ Profit" $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<xLabel>Years</xLabel>
<yLabel>$ Profit</yLabel>
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.14 {Option: -zg <color> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -zg Yellow $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.15 {Option: -zw <width> (Unsupported)} {
    global $pxgraphfile1
    pxgraphTest  -zw 5 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

test Pxgraph-3.16 {Option: =WxH+X+Y} {
    global $pxgraphfile1
    pxgraphTest  =200x250+300+350 $pxgraphfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<dataset>
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.0"/>
<p x="3.0" y="-0.2"/>
</dataset>
</plot>
}

    # Test out stdin
    #pxgraphTest  < ../demo/data.plt

######################################################################
####
#
test Pxgraph-4.1 {Test out file args} {
    # Test out file args
    pxgraphTest  ../demo/bargraph.plt

} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<title>Software Downloads</title>
<xLabel>Year</xLabel>
<yLabel>Downloads</yLabel>
<xRange min="0.0" max="10.0"/>
<yRange min="0.0" max="10000.0"/>
<xTicks>
  <tick label="1993" position="0.0"/>
  <tick label="1994" position="1.0"/>
  <tick label="1995" position="2.0"/>
  <tick label="1996" position="3.0"/>
  <tick label="1997" position="4.0"/>
  <tick label="1998" position="5.0"/>
  <tick label="1999" position="6.0"/>
  <tick label="2000" position="7.0"/>
  <tick label="2001" position="8.0"/>
  <tick label="2002" position="9.0"/>
  <tick label="2003" position="10.0"/>
</xTicks>
<default connected="no"/>
<barGraph width="0.5" offset="0.2"/>
<dataset name="program a">
<m x="0.0" y="100.0"/>
<m x="1.0" y="300.0"/>
<m x="2.0" y="600.0"/>
<m x="3.0" y="1000.0"/>
<m x="4.0" y="4000.0"/>
<m x="5.0" y="6000.0"/>
<m x="6.0" y="3000.0"/>
<m x="7.0" y="1000.0"/>
<m x="8.0" y="400.0"/>
<m x="9.0" y="0.0"/>
<m x="10.0" y="0.0"/>
</dataset>
<dataset name="program b">
<m x="0.0" y="0.0"/>
<m x="1.0" y="0.0"/>
<m x="2.0" y="50.0"/>
<m x="3.0" y="100.0"/>
<m x="4.0" y="800.0"/>
<m x="5.0" y="400.0"/>
<m x="6.0" y="1000.0"/>
<m x="7.0" y="5000.0"/>
<m x="8.0" y="2000.0"/>
<m x="9.0" y="300.0"/>
<m x="10.0" y="0.0"/>
</dataset>
<dataset name="program c">
<m x="0.0" y="0.0"/>
<m x="1.0" y="0.0"/>
<m x="2.0" y="0.0"/>
<m x="3.0" y="10.0"/>
<m x="4.0" y="100.0"/>
<m x="5.0" y="400.0"/>
<m x="6.0" y="2000.0"/>
<m x="7.0" y="5000.0"/>
<m x="8.0" y="9000.0"/>
<m x="9.0" y="7000.0"/>
<m x="10.0" y="1000.0"/>
</dataset>
</plot>
}

test Pxgraph-4.2 {Test out file args} {
    # Test out file args
    pxgraphTest  http://ptolemy.eecs.berkeley.edu/java/ptplot/demo/data.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<title>My Plot</title>
<xLabel>X Axis</xLabel>
<yLabel>Y Axis</yLabel>
<xRange min="0.0" max="4.0"/>
<yRange min="-4.0" max="4.0"/>
<xTicks>
  <tick label="zero" position="0.0"/>
  <tick label="one" position="1.0"/>
  <tick label="two" position="2.0"/>
  <tick label="three" position="3.0"/>
  <tick label="four" position="4.0"/>
  <tick label="five" position="5.0"/>
</xTicks>
<noGrid/>
<noColor/>
<default connected="no" marks="various"/>
<dataset connected="yes" name="dot">
<m x="0.0" y="-4.0"/>
<p x="1.0" y="-3.0"/>
<p x="2.0" y="-2.0"/>
<p x="3.0" y="-1.0"/>
<p x="4.0" y="0.0"/>
</dataset>
<dataset connected="yes" name="cross">
<m x="0.0" y="-3.5"/>
<p x="1.0" y="-2.5"/>
<p x="2.0" y="-1.5"/>
<p x="3.0" y="-0.5"/>
<p x="4.0" y="0.5"/>
</dataset>
<dataset name="square">
<m x="0.0" y="-3.0"/>
<m x="1.0" y="-2.0"/>
<m x="2.0" y="-1.0"/>
<m x="3.0" y="0.0"/>
<m x="4.0" y="1.0"/>
</dataset>
<dataset name="triangle">
<m x="0.0" y="-2.5"/>
<m x="1.0" y="-1.5"/>
<m x="2.0" y="-0.5"/>
<m x="3.0" y="0.5"/>
<m x="4.0" y="1.5"/>
</dataset>
<dataset name="diamond">
<m x="0.0" y="-2.0"/>
<m x="1.0" y="-1.0"/>
<m x="2.0" y="0.0"/>
<m x="3.0" y="1.0"/>
<m x="4.0" y="2.0"/>
</dataset>
<dataset name="circle">
<m x="0.0" y="-1.5"/>
<m x="1.0" y="-0.5"/>
<m x="2.0" y="0.5"/>
<m x="3.0" y="1.5"/>
<m x="4.0" y="2.5"/>
</dataset>
<dataset name="plus">
<m x="0.0" y="-1.0"/>
<m x="1.0" y="0.0"/>
<m x="2.0" y="1.0"/>
<m x="3.0" y="2.0"/>
<m x="4.0" y="3.0"/>
</dataset>
<dataset name="square">
<m x="0.0" y="-0.5"/>
<m x="1.0" y="0.5"/>
<m x="2.0" y="1.5"/>
<m x="3.0" y="2.5"/>
<m x="4.0" y="3.5"/>
</dataset>
<dataset name="triangle">
<m x="0.0" y="0.0"/>
<m x="1.0" y="1.0"/>
<m x="2.0" y="2.0"/>
<m x="3.0" y="3.0"/>
<m x="4.0" y="4.0"/>
</dataset>
<dataset name="diamond">
<m x="0.0" y="0.5"/>
<m x="1.0" y="1.5"/>
<m x="2.0" y="2.5"/>
<m x="3.0" y="3.5"/>
<m x="4.0" y="4.5"/>
</dataset>
<dataset name="dot">
<m x="0.0" y="1.0"/>
<m x="1.0" y="2.0"/>
<m x="2.0" y="3.0"/>
<m x="3.0" y="4.0"/>
</dataset>
</plot>
}

######################################################################
####
#
test Pxgraph-5.1 {Ptolemy Example} {
    pxgraphTest  -binary -t "Integrator Demo" -P \
	    -x n =800x400+0+0 -1 control -0 final \
	    ../demo/data/integrator1.plt ../demo/data/integrator2.plt
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<title>Integrator Demo</title>
<xLabel>n</xLabel>
<default marks="dots"/>
<dataset name="final">
<m x="0.0" y="0.0"/>
<p x="1.0" y="1.0"/>
<p x="2.0" y="2.700000047683716"/>
<p x="3.0" y="4.889999866485596"/>
<p x="4.0" y="7.422999858856201"/>
<p x="5.0" y="5.0"/>
<p x="6.0" y="9.5"/>
<p x="7.0" y="13.649999618530273"/>
<p x="8.0" y="17.55500030517578"/>
<p x="9.0" y="21.28849983215332"/>
<p x="10.0" y="24.90195083618164"/>
<p x="11.0" y="11.0"/>
<p x="12.0" y="19.700000762939453"/>
<p x="13.0" y="26.790000915527344"/>
<p x="14.0" y="32.75299835205078"/>
<p x="15.0" y="37.927101135253906"/>
<p x="16.0" y="42.54896926879883"/>
<p x="17.0" y="17.0"/>
<p x="18.0" y="29.899999618530273"/>
<p x="19.0" y="39.93000030517578"/>
</dataset>
<dataset name="control">
<m x="0.0" y="2.0"/>
<p x="1.0" y="4.0"/>
<p x="2.0" y="6.0"/>
<p x="3.0" y="8.0"/>
<p x="4.0" y="10.0"/>
<p x="5.0" y="12.0"/>
<p x="6.0" y="2.0"/>
<p x="7.0" y="4.0"/>
<p x="8.0" y="6.0"/>
<p x="9.0" y="8.0"/>
<p x="10.0" y="10.0"/>
<p x="11.0" y="12.0"/>
<p x="12.0" y="2.0"/>
<p x="13.0" y="4.0"/>
<p x="14.0" y="6.0"/>
<p x="15.0" y="8.0"/>
<p x="16.0" y="10.0"/>
<p x="17.0" y="12.0"/>
<p x="18.0" y="2.0"/>
<p x="19.0" y="4.0"/>
</dataset>
</plot>
}

######################################################################
####
#
test Pxgraph-6.1 {Reusedatasets} {
    global pxgraphfile3 pxgraphfile4 tcl_platform USER
    if { $tcl_platform(host_platform) == "windows"} {
	set pxgraphfile3 pxgraphfile3-${USER}.plt
	set pxgraphfile4 pxgraphfile4-${USER}.plt
    } else {
	set pxgraphfile3 /tmp/pxgraphfile3-${USER}.plt
	set pxgraphfile4 /tmp/pxgraphfile4-${USER}.plt
    }

    set fd [open $pxgraphfile3 w]
    puts $fd "Reusedatasets: on\nDataset: first\n1 1\n2 10\n 3 25\n\nDataset: second\n1 1.5\n2 15\n3 30\n"
    close $fd

    set fd [open $pxgraphfile4 w]
    puts $fd "Dataset: first\n5 35\n6 40 \n7 42\n"
    close $fd
    pxgraphTest $pxgraphfile3 $pxgraphfile4

} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<reuseDatasets/>
<dataset name="first">
<m x="1.0" y="1.0"/>
<p x="2.0" y="10.0"/>
<p x="3.0" y="25.0"/>
<p x="5.0" y="35.0"/>
<p x="6.0" y="40.0"/>
<p x="7.0" y="42.0"/>
</dataset>
<dataset name="second">
<m x="1.0" y="1.5"/>
<p x="2.0" y="15.0"/>
<p x="3.0" y="30.0"/>
</dataset>
</plot>
}

# Clean up
if [catch {file delete -force $pxgraphfile1 $pxgraphfile2 $pxgraphfile3 $pxgraphfile4} err] {
    puts "Failed to clean up files: $err"
}

set thread [java::call Thread currentThread ]
$thread sleep 1000

#$plotFrame dispose
