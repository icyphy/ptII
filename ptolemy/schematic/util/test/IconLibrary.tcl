# Tests for the IconLibrary class
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
test IconLibrary-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.schematic.util.IconLibrary]
    set e1 [java::new ptolemy.schematic.util.IconLibrary "TestIconLibrary"]
    list [$e0 toString] [$e1 toString]
} {IconLibrary({}{}) TestIconLibrary({}{})}

test IconLibrary-2.2 {setDescription, getDescription tests} {
    # NOTE: Uses the setup above
    set r0 [$e0 getDescription]
    $e0 setDescription {Oh what a tangled web we weave,}
    set r1 [$e0 getDescription]
    $e0 setDescription {when we practice to deceive.}
    set r2 [$e0 getDescription]
    list $r0 $r1 $r2
} {{} {Oh what a tangled web we weave,} {when we practice to deceive.}}

######################################################################
####
#
test IconLibrary-3.1 {addSubLibrary} {
    set t1 [java::new ptolemy.schematic.util.IconLibrary SubLibrary1]
    set t2 [java::new ptolemy.schematic.util.IconLibrary SubLibrary2]
    $e0 addSubLibrary $t1
    $e0 toString
} {IconLibrary({SubLibrary1({}{})}{})}

test IconLibrary-3.2 {containsSubLibrary} {
    list [$e0 containsSubLibrary [$t1 getName]] \
	    [$e0 containsSubLibrary [$t2 getName]]
} {1 0}

test IconLibrary-3.3 {SubLibrarys} {
    $e0 addSubLibrary $t2
    set enum [$e0 subLibraryNames]
    set r1 [$enum hasMoreElements]
    set r2 [$enum nextElement]
    set r3 [$enum hasMoreElements]
    set r4 [$enum nextElement] 
    set r5 [$enum hasMoreElements]
    list $r1 $r2 $r3 $r4 $r5
} {1 SubLibrary2 1 SubLibrary1 0}

test IconLibrary-3.4 {removeSubLibrary} {
    $e0 removeSubLibrary [$t1 getName]
    $e0 toString
} {IconLibrary({SubLibrary2({}{})}{})}

