# Tests for the PTMLParser class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
    set e0 [java::new ptolemy.schematic.PTMLParser "file:/users/neuendor/ptII/ptolemy/schematic/test/exampleschematic.ptml"]
    set element [$e0 parse]
    list [$element toString]
} {{<Document>
<schematic version="1.0" name="SDF">
<description>Icons for use within SDF</description>
<entity name="Load BMP File" iconlibrary="examplelibrary.ptml">
<description>Load the Image that will be transmitted and stored.</description>
<entitytype ports="2" name="loadimage"></entitytype>
<parameter value="" name="filename" type="string"></parameter>
<port multiport="false" name="image" input="false" type="doubleArray" output="true"></port>
</entity>
<entity name="Save BMP file" iconlibrary="examplelibrary.ptml">
<entitytype ports="2" name="saveimage"></entitytype>
<parameter value="" name="filename" type="string"></parameter>
<port multiport="false" name="image" input="true" type="doubleArray" output="false"></port>
</entity>
<relation name="R1" width="1">
<link name="Load BMP File.image"></link>
<link name="Save BMP File.image"></link>
</relation>
<parameter value="SDF" name="domain" type="string"></parameter>
<parameter value="1.0" name="starttime" type="double"></parameter>
<parameter value="7.0" name="endtime" type="double"></parameter>
</schematic>
</Document>
}}

######################################################################
####
#
test PTMLParser-2.2 {semantic tests:schematic} {
    # uses setup above.
    set children [$element childElements]
    set schematic [$children nextElement]
    set name [$schematic getName]
    set version [$schematic getVersion]
    set description [$schematic getDescription]
    set parameters [$schematic parameters]
    set entitynames [$schematic entityNames]
    set relationnames [$schematic relationNames]
    list $name $version $description 
} {SDF 1.0 {Icons for use within SDF}}

######################################################################
####
#
test PTMLParser-2.3 {semantic tests:schematic.parameter} {
    # uses setup above.
    set p [$parameters nextElement]
    list [$p getName] [$p getType] [$p getValue]
} {starttime double 1.0}

######################################################################
####
#
test PTMLParser-2.4 {semantic tests:schematic.entity} {
    # uses setup above.
    set entityname [$entitynames nextElement]
    set e [$schematic getEntity $entityname]
    set entitytype [$e getEntityType]
    set ps [$e parameters]
    set p [$ps nextElement]
    set ports [$e ports]
    list [$e getName] [$entitytype toString] \
[$p getName] [$p getType] [$p getValue] [$entitytype getName] 
} {{Load BMP File} {<entitytype ports="2" name="loadimage"></entitytype>
} filename string {} loadimage}

######################################################################
####
#
test PTMLParser-2.5 {semantic tests:schematic.relation} {
    # uses setup above.
    set relationname [$relationnames nextElement]
    set r [$schematic getRelation $relationname]
    set ls [$r links]
    set l [$ls nextElement]
    list [$r getName] $l

} {R1 {Save BMP File.image}}

######################################################################
####
#
test PTMLParser-2.6 {semantic tests:schematic.port} {
    # uses setup above.
    set p [$ports nextElement]
    list [$p getName] [$p isInput] [$p isOutput] [$p isMultiport] [$p getType]
} {image 0 1 0 doubleArray}

######################################################################
####
#
test PTMLParser-3.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.PTMLParser "file:/users/neuendor/ptII/ptolemy/schematic/test/examplelibrary.ptml"]
    set element [$e0 parse]
    list [$element toString]
} {{<Document>
<iconlibrary version="1.0" name="SDF">
<header>
<description>Icons for use within SDF</description>
<sublibrary file="domains/sdf/lib/icons/htvq.pti"></sublibrary>
<sublibrary file="domains/sdf/lib/icons/communication.pti"></sublibrary>
</header>
<icon name="LoadImage">
<description>Load an image from a file</description>
<entitytype ports="2" name="loadimage"></entitytype>
<graphic format="tcl">
<tclscript>

</tclscript>
</graphic>
<parameter value="" name="filename" type="string"></parameter>
<port multiport="false" name="image" input="false" type="doubleArray" output="true"></port>
</icon>
<icon name="SaveImage">
<entitytype ports="2" name="saveimage"></entitytype>
<port multiport="false" name="filename" input="true" type="string" output="false"></port>
<port multiport="false" name="image" input="true" type="doubleArray" output="false"></port>
<graphic format="xml">
<line width="5" style="dotted" points="0 0 10 10"></line>
<rect fill="hatch" points="0 0 10 10" color="blue"></rect>
<ellipse fill="blue" points="0 0 10 10"></ellipse>
<polygon points="0 0 10 10 15 10 15 0"></polygon>
<textline font="helvetica" points="0 0">
Hello!
</textline>
<textbox alignX="center" alignY="top" points="0 0 100 100">
Hello! This is a nice big text box in a ptolemy icon.
</textbox>
<image format="gif" compression="zip" points="0 0 100 100" file="icon.gif"></image>
</graphic>
</icon>
</iconlibrary>
</Document>
}}
    
        
######################################################################
####
#
test PTMLParser-3.2 {semantic tests:library} {
    # uses setup above.
    set children [$element childElements]
    set library [$children nextElement]
    set name [$library getName]
    set version [$library getVersion]
    set description [$library getDescription]
    set icons [$library icons]
    list $name $version $description 
} {SDF 1.0 {Icons for use within SDF}}

######################################################################
####
#
test PTMLParser-3.3 {semantic tests:library.icon} {
    # uses setup above.
    set i [$icons nextElement]
    set entitytype [$i getEntityType]
    set graphicformats [$i graphicFormats]
    set ports [$i ports]
    list [$i getName] [$i getDescription] [$entitytype getName]
} {SaveImage {} saveimage}

######################################################################
####
#
test PTMLParser-3.4 {semantic tests:library.icon.graphic} {
    # uses setup above.
    set graphicformat [$graphicformats nextElement]
    set graphic [$i getGraphic $graphicformat]
    list [$graphic getAttribute format] [$graphic toString]
} {xml {<graphic format="xml">
<line width="5" style="dotted" points="0 0 10 10"></line>
<rect fill="hatch" points="0 0 10 10" color="blue"></rect>
<ellipse fill="blue" points="0 0 10 10"></ellipse>
<polygon points="0 0 10 10 15 10 15 0"></polygon>
<textline font="helvetica" points="0 0">
Hello!
</textline>
<textbox alignX="center" alignY="top" points="0 0 100 100">
Hello! This is a nice big text box in a ptolemy icon.
</textbox>
<image format="gif" compression="zip" points="0 0 100 100" file="icon.gif"></image>
</graphic>
}}

######################################################################
####
#
test PTMLParser-3.5 {semantic tests:library.icon.port} {
    # uses setup above.
    set p [$ports nextElement]
    list [$p getName] [$p isInput] [$p isOutput] [$p isMultiport] [$p getType]
} {image 1 0 0 doubleArray}

