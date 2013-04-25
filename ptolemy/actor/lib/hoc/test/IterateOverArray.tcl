# Test IterateOverArray
#
# @Author: Christopher Brooks, based on Ramp.tcl by Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 2012 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test IterateOverArray-1.1 {test clone} {
    set w0 [java::new ptolemy.kernel.util.Workspace "firstWorkspace"]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w0]
    set iterateOverArray [java::new ptolemy.actor.lib.hoc.IterateOverArray $e0 IterateOverArray]
    set iterateDirectorClass [java::call Class forName {ptolemy.actor.lib.hoc.IterateOverArray$IterateDirector}]
    set iterateDirectors [$iterateOverArray attributeList $iterateDirectorClass]
    set iterateDirector [java::cast {ptolemy.actor.lib.hoc.IterateOverArray$IterateDirector} [$iterateDirectors get 0]]
    set iterateDirectorWorkspace [$iterateDirector workspace]

    # Get the container and its Workspace
    set iterateDirectorContainer [$iterateDirector getContainer]
    set iterateDirectorContainerWorkspace [$iterateDirectorContainer workspace]

    # Get the outer class and its Workspace
    # Field this$0 = inner.getClass().getDeclaredField("this$0");
    set thisZeroField [$iterateDirectorClass getDeclaredField {this$0}]
    # Outer outer = (Outer) this$0.get(inner);
    $thisZeroField setAccessible true
    set iterateDirectorOuter [java::cast ptolemy.actor.lib.hoc.IterateOverArray [$thisZeroField get $iterateDirector]]
    set iterateDirectorOuterWorkspace [$iterateDirectorOuter workspace]

    # Clone
    set w1 [java::new ptolemy.kernel.util.Workspace "clonedWorkspace"]
    set clonedIterateOverArray [java::cast ptolemy.actor.lib.hoc.IterateOverArray [$iterateOverArray clone $w1]]
    set clonedIterateDirectors [$clonedIterateOverArray attributeList [java::call Class forName {ptolemy.actor.lib.hoc.IterateOverArray$IterateDirector}]]
    set clonedIterateDirector [java::cast {ptolemy.actor.lib.hoc.IterateOverArray$IterateDirector} [$clonedIterateDirectors get 0]]
    set clonedIterateDirectorWorkspace [$clonedIterateDirector workspace]

    # Get the outer class and its Workspace
    set clonedIterateDirectorOuter [java::cast ptolemy.actor.lib.hoc.IterateOverArray [$thisZeroField get $clonedIterateDirector]]
    set clonedIterateDirectorOuterWorkspace [$clonedIterateDirectorOuter workspace]

    # Get the container of the clone and the Workspace
    set clonedIterateDirectorContainer [$clonedIterateDirector getContainer]
    set clonedIterateDirectorContainerWorkspace [$clonedIterateDirectorContainer workspace]
    
    list \
	[$iterateDirectorWorkspace getName] \
	[$iterateDirectorOuterWorkspace getName] \
	[$iterateDirectorContainerWorkspace getName] \
	[$clonedIterateDirectorWorkspace getName] \
	[$clonedIterateDirectorOuterWorkspace getName] \
	[$clonedIterateDirectorContainerWorkspace getName]
 } {firstWorkspace firstWorkspace firstWorkspace clonedWorkspace clonedWorkspace clonedWorkspace}


######################################################################
####
#
test IterateOverArray-1.2 {test clone on inner class} {
    # Uses 1.1 above

    set w2 [java::new ptolemy.kernel.util.Workspace "AnotherClonedWorkspace"]
    set clonedClonedIterateDirector [java::cast {ptolemy.actor.lib.hoc.IterateOverArray$IterateDirector} [$clonedIterateDirector clone $w2]]
    set clonedClonedIterateDirectorWorkspace [$clonedClonedIterateDirector workspace]

    set clonedClonedIterateDirectorContainer [$clonedClonedIterateDirector getContainer]
    #set clonedClonedIterateDirectorContainerWorkspace [$clonedClonedIterateDirectorContainer workspace]

    list \
	[$clonedClonedIterateDirectorWorkspace getName] \
	[java::isnull $clonedClonedIterateDirectorContainer]
	#[$clonedClonedIterateDirectorContainerWorkspace getName]
} {AnotherClonedWorkspace 1}


