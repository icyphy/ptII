# Tests for the PlotBox class
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

######################################################################
####
#
test PlotBox-1.1 {} {
    set frame [java::new java.awt.Frame]
    set plot [java::new ptolemy.plot.PlotBox]
    $frame pack
    $frame {add java.lang.String java.awt.Component} "Center" $plot
    $frame show
    $frame setSize 500 300
    $frame repaint
    $plot setTitle "foo"
    $plot addXTick "X tick" -10
    $plot addYTick "Y tick" 10.1
    $plot repaint
    $plot clear true
    $plot repaint
    $plot fillPlot
} {}

test PlotBox-1.2 {addLegend, getLegend} {
    $plot addLegend 1 "A Legend"
    $plot addLegend 2 "Another Legend"
    $plot addLegend 3 "3rd Legend"
    $plot getLegend 2
} {Another Legend}

test PlotBox-2.1 {setDataurl, getDataurl, getDocumentBase} {
    $plot setDataurl ../demo/data.plt
    set url [$plot getDataurl]
    set docbase [$plot getDocumentBase]
    list $url [java::isnull $docbase]
} {../demo/data.plt 1}


test PlotBox-2.2 {setDataurl, getDataurl, getDocumentBase} {
    $plot setDataurl http://notasite/bar/foo.plt
    set url [$plot getDataurl]
    set docbase [$plot getDocumentBase]
    list $url [java::isnull $docbase]
} {http://notasite/bar/foo.plt 1}

test PlotBox-3.1 {getMinimumSize getPreferredSize} {
    $frame setSize 425 600
    $frame repaint
    set minimumDimension [$plot getMinimumSize] 
    set preferredDimension [$plot getPreferredSize]
    list [java::field $minimumDimension width] \
	    [java::field $minimumDimension height] \
	    [java::field $preferredDimension width] \
	    [java::field $preferredDimension height] \
} {488 264 488 264}

test PlotBox-4.1 {parseFile} {
    $plot parseFile ../demo/data.plt
} {}

test PlotBox-5.1 {getColorByName} {
    set color [$plot getColorByName "red"]
    $color toString
} {java.awt.Color[r=255,g=0,b=0]}

