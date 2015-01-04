# Tests for the HistogramMLApplication
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
# set VERBOSE 1

# StringUtilities.exit() checks the ptolemy.ptII.doNotExit property
java::call System setProperty ptolemy.ptII.doNotExit true

########################################
#### histogramFiles
# Create two test files histogramfile1 histogramfile2
#
proc histogramFiles {} {
    global histogramfile1 histogramfile2 tcl_platform
    if { $tcl_platform(host_platform) == "windows"} {
	set histogramfile1 histogramfile1.plt
	set histogramfile2 histogramfile2.plt
    } else {
	set histogramfile1 /tmp/histogramfile1.plt
	set histogramfile2 /tmp/histogramfile2.plt
    }

    set fd [open $histogramfile1 w]
    puts $fd "TitleText: Sample histogram\n"
    puts $fd "XLabel: values\n"
    puts $fd "YLabel: count\n"
    puts $fd "YRange: 0.0,100.0\n"
    puts $fd "BarGraph: 0.5,0.15\n"
    puts $fd "BinWidth: 1.0\n"
    puts $fd "BinOffset: 0.5\n"
    puts $fd "DataSet: first\n"
    puts $fd "5.0\n"
    puts $fd "4.9999013042806855\n"
    puts $fd "4.99960522101908\n"
    close $fd

    #set fd [open $histogramfile2 w]
    #puts $fd "0 1\n		1 2\nmove:2 2.5\n 3 1"
    #close $fd
}
histogramFiles

########################################
#### histogramTest
# Pass arguments to Histogram, run it, write the output to
# a variable, sleep, dispose of the Histogram, then return the results
# 
#
proc histogramTest { args } {
    global defaultPlotMLHeader
    set jargs [java::new {String[]} [llength $args] $args ]
    set histogram [java::new ptolemy.plot.plotml.HistogramMLApplication $jargs]
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    set plotFrame [java::cast ptolemy.plot.PlotFrame $histogram ]
    set plot [java::field $plotFrame plot]
    if {[llength $args] == 0} {
	puts "histogramTest: setting seed to 1"
	[java::cast ptolemy.plot.Histogram $plot] clear true
	[java::cast ptolemy.plot.Histogram $plot] setSeed 1
	$histogram samplePlot
    }

    set toolkit [java::call java.awt.Toolkit getDefaultToolkit]
    $toolkit sync

    $plot write $printStream "Usually, the DTD would go here"
    $printStream flush
    set results [$stream toString]
    set thread [java::call Thread currentThread ]
    # sleep 0.5 seconds
    $thread sleep 500
    $histogram dispose
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


test Histogram-1.1 {Get the sample output} {
    global histogramfile1 histogramfile2
    histogramTest
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<barGraph width="0.5" offset="0.15"/>
<dataset connected="no">
<p x="0" y="65"/>
<p x="5" y="2"/>
<p x="-1" y="66"/>
<p x="4" y="204"/>
<p x="-2" y="74"/>
<p x="3" y="90"/>
<p x="-3" y="90"/>
<p x="2" y="74"/>
<p x="-4" y="205"/>
<p x="1" y="66"/>
<p x="0" y="65"/>
</dataset>
<dataset connected="no">
<p x="0" y="96"/>
<p x="-1" y="99"/>
<p x="4" y="97"/>
<p x="3" y="117"/>
<p x="-2" y="100"/>
<p x="2" y="94"/>
<p x="-3" y="107"/>
<p x="-4" y="100"/>
<p x="1" y="105"/>
<p x="0" y="86"/>
</dataset>
<dataset connected="no">
<p x="-6" y="1"/>
<p x="6" y="1"/>
<p x="5" y="4"/>
<p x="0" y="197"/>
<p x="-1" y="153"/>
<p x="4" y="18"/>
<p x="3" y="37"/>
<p x="-2" y="68"/>
<p x="-3" y="34"/>
<p x="2" y="106"/>
<p x="-4" y="17"/>
<p x="1" y="171"/>
<p x="-5" y="5"/>
<p x="0" y="189"/>
</dataset>
</plot>
}

test Histogram-2.1 {Read in an old format file} {
    global histogramfile1
    histogramTest $histogramfile1
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "Usually, the DTD would go here">
<plot>
<!-- Ptolemy plot, version XXX -->
<title>Sample histogram</title>
<xLabel>values</xLabel>
<yLabel>count</yLabel>
<yRange min="0.0" max="100.0"/>
<barGraph width="0.5" offset="0.15"/>
<dataset name="first" connected="no">
<p x="5" y="1"/>
<p x="4" y="2"/>
</dataset>
</plot>
}

test Histogram-3.1 {-help output} {
    global histogramfile1
    jdkCapture {histogramTest -help} results
    set results
} {Usage: ptplot [ options ] [file ...]

Options that take values:
 -height <pixels>
 -width <pixels>

Boolean flags:
 -help -printPDF -test -version -
}
