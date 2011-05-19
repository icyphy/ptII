# Tests for the CheckModelSize class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2006 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set ptolemyPtIIDir [java::call ptolemy.util.StringUtilities getProperty ptolemy.ptII.dir]

######################################################################
####
#
test CheckModelSize-1.0 {checkModelSize} {
    set args [java::new {String[]} {1} {../../../../ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml}]
    set results \
	[java::call ptolemy.actor.gui.CheckModelSize \
		checkModelSize [java::null] $args]
    if { [regsub -all "file:/*$ptolemyPtIIDir/" $results {} results2] == 0} {
	regsub -all {file:[^>]*/ptII/} $results {} results2
    }
    list $results2
	
} {{<h1>Check Size</h1>
Below are the results from checking the sizes of and centering of models
<table>
<b>Note: after running review these results, be sure to exit, as the graphical elements of the  models will have been removed</b>
<tr>
  <td><a href="ptolemy/actor/gui/test/../../../../ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml">ptolemy/actor/gui/test/../../../../ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml</a></td>
  <td> seems to be OK.</td>
</table>
}}


######################################################################
####
#
test CheckModelSize-1.1 {checkModelSize: no _vergilSize} {
    set args [java::new {String[]} {1} {test.xml}]
    set results \
	[java::call ptolemy.actor.gui.CheckModelSize \
		checkModelSize [java::null] $args]
    if { [regsub -all "file:/*$ptolemyPtIIDir/" $results {} results2] == 0} {
	regsub -all {file:[^>]*/ptII/} $results {} results2
    }
    list $results2
	
} {{<h1>Check Size</h1>
Below are the results from checking the sizes of and centering of models
<table>
<b>Note: after running review these results, be sure to exit, as the graphical elements of the  models will have been removed</b>
<tr>
  <td><a href="ptolemy/actor/gui/test/test.xml">ptolemy/actor/gui/test/test.xml</a></td>
  <td> has no _vergilSize.</td>
</table>
}}

######################################################################
####
#
test CheckModelSize-1.2 {checkModelSize: bad moml} {
    set args [java::new {String[]} {1} {badMoML.moml}]
    set results \
	[java::call ptolemy.actor.gui.CheckModelSize \
		checkModelSize [java::null] $args]
    if { [regsub -all "file:/*$ptolemyPtIIDir/" $results {} results2] == 0} {
	regsub -all {file:[^>]*/ptII/} $results {} results2
    }
    list [string range $results2 0 290]
} {{<h1>Check Size</h1>
Below are the results from checking the sizes of and centering of models
<table>
<b>Note: after running review these results, be sure to exit, as the graphical elements of the  models will have been removed</b>
<tr>
  <td><a href="ptolemy/actor/gui/test/badMoML.moml">pto}}

######################################################################
####
#
test CheckModelSize-2.0 {main} {
    set modelFile [java::new java.io.File testCheckModelSize.xml]
    set args [java::new {String[]} {2} [list  Notafile [$modelFile toString]]]
    jdkCapture {
        java::call ptolemy.actor.gui.CheckModelSize main $args
    } results
    if { [regsub -all "file:/*$ptolemyPtIIDir/" $results {} results2] == 0} {
	regsub -all {file:[^>]*/ptII/} $results {} results2
    }
    list $results2
} {{<h1>Check Size</h1>
Below are the results from checking the sizes of and centering of models
<table>
<b>Note: after running review these results, be sure to exit, as the graphical elements of the  models will have been removed</b>
<tr>
  <td><a href="ptolemy/actor/gui/test/testCheckModelSize.xml">ptolemy/actor/gui/test/testCheckModelSize.xml</a></td>
  <td> width(9999) > 800 height(8888) > 768 Center([7777.0, 6666.0]) is not centered, should be [4999.5, 4444.0] Zoom(2.0) != 1.0</td>
</table>

}}

######################################################################
####
#
test CheckModelSize-2.1 {main: bad moml} {
    set modelFile [java::new java.io.File badMoML.moml]
    set modelURL [$modelFile toURL]
    set args [java::new {String[]} {1} $modelURL]
    jdkCapture {
        java::call ptolemy.actor.gui.CheckModelSize main $args
    } results
    list $results
} {{<h1>Check Size</h1>
Below are the results from checking the sizes of and centering of models
<table>
<b>Note: after running review these results, be sure to exit, as the graphical elements of the  models will have been removed</b>
</table>

}}

######################################################################
####
#
test CheckModelSize-2.2 {main: throw an exception} {
    set modelFile [java::new java.io.File badMoML.moml]
    set modelURL [$modelFile toURL]
    set args [java::new {String[]} {1} [java::null]]
    jdkCaptureErr {
        java::call ptolemy.actor.gui.CheckModelSize main $args
    } results
    list [string range $results 0 29]
} {java.lang.NullPointerException}
