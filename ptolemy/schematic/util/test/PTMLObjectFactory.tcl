# Tests for the PTMLObjectFactory class
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
test PTMLObjectFactory-2.1 {Constructor tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]
    set xmllib [$parser parse "file:/users/neuendor/ptII/ptolemy/schematic/util/test/exampleIconLibrary.ptml"]   
    set iconlib [java::call ptolemy.schematic.util.PTMLObjectFactory \
	    createIconLibrary $xmllib]
    $iconlib toString
} {SDF({}{
ptolemy.schematic.util.Icon {LoadImage}(
....rectangle(content=,color=red,fill=red,coords=-20 -20 40 40)
....text(content=Hello!,font=helvetica,coords=0 0)
....polygon(content=,coords=0 0 30 30 0 30 30 0,fill=blue)
....ellipse(content=,fill=yellow,coords=0 0 10 10))
ptolemy.schematic.util.Icon {SaveImage}(
....rectangle(content=,outline=red,fill=blue,coords=0 0 10 10))})}

######################################################################
####
#
test PTMLObjectFactory-2.2 {Constructor tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]
    set xmllib [$parser parse "file:/users/neuendor/ptII/ptolemy/schematic/util/test/exampleRootIconLibrary.ptml"]
    set iconroot [java::call ptolemy.schematic.util.PTMLObjectFactory createIconLibrary $xmllib]
    set xmllib [$parser parse "file:/users/neuendor/ptII/ptolemy/schematic/util/test/exampleEntityLibrary.ptml"]   
    
    set entitylib [java::call ptolemy.schematic.util.PTMLObjectFactory \
	    createEntityLibrary $xmllib $iconroot]
    list [$iconroot toString] [$entitylib toString]
} {{root({SDF({}{
ptolemy.schematic.util.Icon {LoadImage}(
....rectangle(content=, color=red, fill=red, coords=-20 -20 40 40)
....text(content=Hello!, font=helvetica, coords=0 0)
....polygon(content=, coords=0 0 30 30 0 30 30 0, fill=blue)
....ellipse(content=, fill=yellow, coords=0 0 10 10))
ptolemy.schematic.util.Icon {SaveImage}(
....rectangle(content=, outline=red, fill=blue, coords=0 0 10 10))})}{})} {SDF({}{
ptolemy.schematic.util.EntityTemplate {LoadImage}(
ptolemy.schematic.util.Icon {LoadImage}(
....rectangle(content=, color=red, fill=red, coords=-20 -20 40 40)
....text(content=Hello!, font=helvetica, coords=0 0)
....polygon(content=, coords=0 0 30 30 0 30 30 0, fill=blue)
....ellipse(content=, fill=yellow, coords=0 0 10 10))
ptolemy.schematic.util.TerminalStyle {1out}(output((44.0, 44.0))))
ptolemy.schematic.util.EntityTemplate {SaveImage}(
ptolemy.schematic.util.Icon {SaveImage}(
....rectangle(content=, outline=red, fill=blue, coords=0 0 10 10))
ptolemy.schematic.util.TerminalStyle {1in}(input((-4.0, -4.0))))})}}
