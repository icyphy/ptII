# Tests for the LibraryBuilder class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2007 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test LibraryBuilder-1.1 {ctor, setAttribute, getAttribute} {
    set libraryBuilder [java::new ptolemy.moml.test.TestLibraryBuilder]

    # Create a list of two attributes
    set attributeList [java::new java.util.LinkedList]
    set attribute1 [java::new ptolemy.kernel.util.Attribute]
    $attribute1 setName "A"
    $attributeList add $attribute1
    set attribute2 [java::new ptolemy.kernel.util.Attribute]
    $attribute2 setName "B"
    $attributeList add $attribute2

    $libraryBuilder addAttributes $attributeList

    set list2 [$libraryBuilder getAttributes]

    list [$list2 size] \
	[[java::cast ptolemy.kernel.util.Attribute [$list2 get 0]] getName] \
	[[java::cast ptolemy.kernel.util.Attribute [$list2 get 1]] getName] 
} {2 A B}

######################################################################
####
#
test LibraryBuilder-2.0 {buildLibrary} {
    set workspace [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set libraryBuilder2 [java::new ptolemy.moml.test.TestLibraryBuilder]
    set compositeEntity [$libraryBuilder2 buildLibrary $workspace]
    list [[$compositeEntity workspace] getName]
} {myWorkspace}
