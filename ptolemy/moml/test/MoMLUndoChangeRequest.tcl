# Tests for the UndoChangeRequest and RedoChangeRequest classes,
# which are in kernel.util, but the tests use MoML.
#
# @Author: Christopher Hylands, based on MoMLChangeRequest.tcl by Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
#set VERBOSE 1


######################################################################
####
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
</entity>
}

######################################################################
####
#
test MoMLUndoChangeRequest-1.1 {Test adding an entity} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="const" class="ptolemy.actor.lib.Const"/>
        </entity>
    }]
    $change setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change

    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="allowRateChanges" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
</entity>
}

######################################################################
####
#
test MoMLUndoChangeRequest-1.2 {Undo} {
    set originalMoML [$toplevel exportMoML]
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set undoChange [java::new ptolemy.kernel.undo.UndoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $undoChange 
    set undoneMoML [$toplevel exportMoML]

    set r [diffText $originalMoML $undoneMoML]

    # Unfortunately, under Windows, there are problems with end of line
    # characters that are returned by the diffText proc, so we just
    # check for key strings in the output
    list \
	[regexp  {<     <entity name="const" class="ptolemy.actor.lib.Const">} \
	     $r] \
	[regexp  {<         <port name="output" class="ptolemy.actor.TypedIOPort">} \
	     $r] \
	[regexp  {<         <port name="trigger" class="ptolemy.actor.TypedIOPort">} \
	     $r] \
} {1 1 1}

test MoMLUndoChangeRequest-1.2a {Undo again, with nothing to undo} {
    # Uses $undoneMoML from 1.2 above
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set undoChange [java::new ptolemy.kernel.undo.UndoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $undoChange 
    set undoneAgainMoML [$toplevel exportMoML]
    diffText $undoneMoML $undoneAgainMoML
} {}

######################################################################
####
#
test MoMLUndoChangeRequest-1.3 {Redo} {
    # Uses $originalMoML from 1.2 above
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set redoChange [java::new ptolemy.kernel.undo.RedoChangeRequest \
		$originator $toplevel]
    $toplevel requestChange $redoChange 
    set redoneMoML [$toplevel exportMoML]
    diffText $originalMoML $redoneMoML
} {}


######################################################################
####
#
test MoMLUndoChangeRequest-1.4 {Redo again, with nothing to redo } {
    # Uses $originalMoML from 1.2 above
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set redoChange [java::new ptolemy.kernel.undo.RedoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $redoChange 
    set redoneMoML [$toplevel exportMoML]
    diffText $originalMoML $redoneMoML
} {}

######################################################################
####
#
test MoMLUndoChangeRequest-2.1 {Make three changes, merge the first and the last, but the middle one is not undoable } {

    # Make a change that is undoable	
    set change1 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="discard1" class="ptolemy.actor.lib.Discard"/>
        </entity>
    }]
    $change1 setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change1

    # Make a change that is not undoable	
    set change2 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="discard2" class="ptolemy.actor.lib.Discard"/>
        </entity>
    }]

    #$change2 setUndoable false

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change2

    # Make a change that is undoable	
    set change3 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="discard3" class="ptolemy.actor.lib.Discard"/>
        </entity>
    }]

    $change3 setUndoable true
    $change3 setMergeWithPreviousUndo true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change3
    set threeChangeMoML [$toplevel exportMoML]

    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set undoChange [java::new ptolemy.kernel.undo.UndoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $undoChange 
    set undoneThreeChangeMoML [$toplevel exportMoML]
    set r [diffText $threeChangeMoML $undoneThreeChangeMoML]
    # Unfortunately, under Windows, there are problems with end of line
    # characters that are returned by the diffText proc, so we just
    # check  that the discard1 and discard3 actors are not present
    # in the diff output
    list \
	[regexp {<     <entity name="discard1" class="ptolemy.actor.lib.Discard">} \
	     $r] \
	[regexp {<     <entity name="discard3" class="ptolemy.actor.lib.Discard">} \
	     $r]

} {1 1}


######################################################################
####
#
test MoMLUndoChangeRequest-5.1 {getDeferredToParent} {
    # FIXME: This is not a real test for getDeferredToParent.
    set e3 [java::new ptolemy.actor.TypedCompositeActor $toplevel E3]

    # FIXME: not sure if this is right?	
    $e3 setDeferMoMLDefinitionTo $toplevel

    set r1 [expr {[java::call \
	    ptolemy.moml.MoMLChangeRequest getDeferredToParent [java::null]] \
	    == [java::null]}] 
    set r2 [expr {[java::call \
	    ptolemy.moml.MoMLChangeRequest getDeferredToParent $toplevel] \
	    == [java::null]}] 

    set a [java::call ptolemy.moml.MoMLChangeRequest getDeferredToParent $e3]
    set r3 [$a getName]	

    list $r1 $r2 $r3
} {1 0 top}

