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
    set e0 [java::new ptolemy.schematic.xml.PTMLParser]
    set schematic [$e0 parse "file:/users/neuendor/ptII/ptolemy/schematic/test/exampleschematic.ptml"]
    list [$schematic toString]
} {{<schematic version="1.0" name="SDF">
<description>Icons for use within SDF</description>
<entity icon="examplelibrary.LoadImage" name="Load BMP File">
<description>Load the Image that will be transmitted and stored.</description>
<parameter value="" type="string" name="filename"></parameter>
<port multiport="false" input="false" type="doubleArray" output="true" name="image"></port>
</entity>
<entity icon="examplelibrary.SaveImage" name="Save BMP File">
<parameter value="" type="string" name="filename"></parameter>
<port multiport="false" output="false" type="doubleArray" input="true" name="image"></port>
</entity>
<relation name="R1">
<link name="Load BMP File.image"></link>
<link name="Save BMP File.image"></link>
</relation>
<parameter type="string" value="SDF" name="domain"></parameter>
<parameter type="double" value="1.0" name="starttime"></parameter>
<parameter type="double" value="7.0" name="endtime"></parameter>
</schematic>
}}


######################################################################
####
#
test PTMLParser-3.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.xml.PTMLParser]
    set library [$e0 parse "file:/users/neuendor/ptII/ptolemy/schematic/test/examplelibrary.ptml"]
    list [$library toString]
} {{<iconlibrary version="1.0" name="SDF">
<header>
<description>Icons for use within SDF</description>
<sublibrary url="../../domains/sdf/lib/icons/htvq.ptml" name="VQ"></sublibrary>
<sublibrary url="../../domains/sdf/lib/icons/communication.ptml" name="comm"></sublibrary>
</header>
<icon name="LoadImage">
<description>Load an image from a file</description>
<entitytype ports="2" name="loadimage"></entitytype>
<graphic format="tcl">
<tclscript>

</tclscript>
</graphic>
<parameter value="" type="string" name="filename"></parameter>
<port multiport="false" input="false" type="doubleArray" output="true" name="image"></port>
</icon>
<icon name="SaveImage">
<entitytype ports="2" name="saveimage"></entitytype>
<port multiport="false" output="false" type="string" input="true" name="filename"></port>
<port multiport="false" output="false" type="doubleArray" input="true" name="image"></port>
<graphic format="xml">
<line points="0 0 10 10" width="5" style="dotted"></line>
<rect points="0 0 10 10" fill="hatch" color="blue"></rect>
<ellipse points="0 0 10 10" fill="blue"></ellipse>
<polygon points="0 0 10 10 15 10 15 0"></polygon>
<textline points="0 0" font="helvetica">
Hello!
</textline>
<textbox points="0 0 100 100" alignY="top" alignX="center">
Hello! This is a nice big text box in a ptolemy icon.
</textbox>
<image points="0 0 100 100" file="icon.gif" compression="zip" format="gif"></image>
</graphic>
</icon>
</iconlibrary>
}}
    
######################################################################
####
#
test PTMLParser-4.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.xml.PTMLParser]
    set library [$e0 parse "file:/users/neuendor/ptII/ptolemy/schematic/lib/ptII.ptml"]
    list [$library toString]
} {{<domainlibrary version="1.0" name="Dataflow">
<actorpackage package="ptolemy.lib" name="domain polymorphic"></actorpackage>
<domain name="CT">
<description>Continuous Time</description>
<actorpackage package="ptolemy.domains.ct.lib" name="CT default"></actorpackage>
<director class="ptolemy.domains.ct.kernel.CTSingleSolverDirector" name="Single solver"></director>
<director class="ptolemy.domains.ct.kernel.CTMultiSolverDirector" name="Multiple solver"></director>
<director class="ptolemy.domains.ct.kernel.CTMixedSignalDirector" name="Mixed-signal"></director>







    </domain>
<domain name="DE">
<description>Discrete Event</description>
<actorpackage package="ptolemy.domains.de.lib" name="DE default"></actorpackage>
<director class="ptolemy.domains.de.kernel.DECQDirector" name="Calendar queue"></director>

        
        

        
    </domain>
<domain name="PN">
<description>Process networks</description>
<actorpackage package="ptolemy.domains.pn.lib" name="PN default"></actorpackage>
<director class="ptolemy.domains.pn.kernel.PNDirector" name="Bounded memory"></director>





    </domain>
<domain name="SDF">
<description>Static dataflow</description>
<actorpackage package="ptolemy.domains.sdf.lib" name="SDF default"></actorpackage>
<director class="ptolemy.domains.sdf.kernel.SDFDirector" name="Multirate"></director>





    </domain>
</domainlibrary>
}}
