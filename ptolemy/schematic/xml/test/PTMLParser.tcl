# Tests for the PTMLParser class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test PTMLParser-2.1 {parser tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]
    set fileend [file join $PTII ptolemy schematic util test exampleIconLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set tree [$parser parse $filename]
    list [$tree toString]
} {{<iconlibrary name="SDF" version="1.0">
<description>Icons for use within SDF</description>
<icon name="LoadImage">
<description>Load an image from a file</description>
<xmlgraphic>
<rectangle color="red" coords="0 0 60 40" fill="pink"></rectangle>
<polygon color="black" coords="10 10 50 30 10 30 50 10" fill="blue"></polygon>
<ellipse color="black" coords="25 15 10 10" fill="yellow"></ellipse>
<line coords="30 20 60 20"></line>
</xmlgraphic>
</icon>
<icon name="SaveImage">
<xmlgraphic>
<rectangle color="red" coords="0 0 60 40" fill="orange"></rectangle>
<polygon color="black" coords="10 10 50 30 10 30 50 10" fill="blue"></polygon>
<ellipse color="black" coords="25 15 10 10" fill="yellow"></ellipse>
<line coords="0 20 30 20"></line>
</xmlgraphic>
</icon>
<terminalstyle name="1out">
<terminal name="output" x="64" y="20"></terminal>
</terminalstyle>
<terminalstyle name="1in">
<terminal name="input" x="-4" y="20"></terminal>
</terminalstyle>
</iconlibrary>
}}


######################################################################
####
#
test PTMLParser-2.2 {Constructor tests} {
    set fileend [file join $PTII ptolemy schematic util test exampleschematic.ptml]
    set filename "file:"
    append filename $fileend
    set tree [$parser parse $filename]
    list [$tree toString]
} {{<schematic name="SDF" version="1.0">
<description>Icons for use within SDF</description>
<entity icon="default" implementation="null" name="Load BMP File" template="SDF.LoadImage" terminalstyle="default">
<description>Load the Image that will be transmitted and stored.</description>
<parameter name="filename" type="string" value="test"></parameter>
</entity>
<entity icon="default" implementation="null" name="Save BMP File" template="SDF.SaveImage" terminalstyle="default">
<parameter name="filename" type="string" value="test"></parameter>
</entity>
<relation name="R1">
<link from="Save BMP File.input" to="Load BMP File.output"></link>
</relation>
<parameter name="domain" type="string" value="SDF"></parameter>
<parameter name="starttime" type="double" value="1.0"></parameter>
<parameter name="endtime" type="double" value="7.0"></parameter>
</schematic>
}}
    
######################################################################
####
#
test PTMLParser-3.1 {Constructor tests} {
    set fileend [file join $PTII ptolemy schematic lib ptII.ptml]
    set filename "file:"
    append filename $fileend
    set tree [$parser parse $filename]
    list [$tree toString]
} {{<domainlibrary name="Dataflow" version="1.0">
<actorpackage name="domain polymorphic" package="ptolemy.lib"></actorpackage>
<domain name="CT">
<description>Continuous Time</description>
<actorpackage name="CT default" package="ptolemy.domains.ct.lib"></actorpackage>
<director class="ptolemy.domains.ct.kernel.CTSingleSolverDirector" icon="default" implementation="null" name="Single solver"></director>
<director class="ptolemy.domains.ct.kernel.CTMultiSolverDirector" icon="default" implementation="null" name="Multiple solver"></director>
<director class="ptolemy.domains.ct.kernel.CTMixedSignalDirector" icon="default" implementation="null" name="Mixed-signal"></director>
</domain>
<domain name="DE">
<description>Discrete Event</description>
<actorpackage name="DE default" package="ptolemy.domains.de.lib"></actorpackage>
<director class="ptolemy.domains.de.kernel.DECQDirector" icon="default" implementation="null" name="Calendar queue"></director>
</domain>
<domain name="PN">
<description>Process networks</description>
<actorpackage name="PN default" package="ptolemy.domains.pn.lib"></actorpackage>
<director class="ptolemy.domains.pn.kernel.PNDirector" icon="default" implementation="null" name="Bounded memory"></director>
</domain>
<domain name="SDF">
<description>Static dataflow</description>
<actorpackage name="SDF default" package="ptolemy.domains.sdf.lib"></actorpackage>
<director class="ptolemy.domains.sdf.kernel.SDFDirector" icon="default" implementation="null" name="Multirate"></director>
</domain>
</domainlibrary>
}}

