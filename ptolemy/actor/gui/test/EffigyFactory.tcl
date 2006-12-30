# Tests EffigyFactory
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
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


######################################################################
####
#
test EffigyFactory-1.0 {} {
    set workspace [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set effigyFactory1 [java::new ptolemy.actor.gui.EffigyFactory $workspace]
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    $e1 setName E1
    set effigyFactory2 [java::new ptolemy.actor.gui.EffigyFactory $e1 EF1]
    $effigyFactory2 exportMoML 
} {<entity name="EF1" class="ptolemy.actor.gui.EffigyFactory">
</entity>
}

######################################################################
####
#
test EffigyFactory-2.0 {} {

} {}

######################################################################
####
#
test EffigyFactory-7.0 {getExtension} {
    set file1 [java::new java.io.File test.xml]
    set url1 [$file1 toURL]
    set file2 [java::new java.io.File makefile]
    set url2 [$file2 toURL]
    list [java::call ptolemy.actor.gui.EffigyFactory getExtension $url1] \
	[java::call ptolemy.actor.gui.EffigyFactory getExtension $url2]
} {xml {}}

######################################################################
####
#
test EffigyFactory-7.1 {getExtension with . in path name} {
    set file1 [java::new java.io.File foo.bar/makefile]
    set url1 [$file1 toURL]
    list [java::call ptolemy.actor.gui.EffigyFactory getExtension $url1]
} {{}}

