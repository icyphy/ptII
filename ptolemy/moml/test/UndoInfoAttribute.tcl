# Tests for UndoInfoAttribute
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

test UndoInfoAttribute-1.1 {Constructor} {
    set container [java::new ptolemy.kernel.util.NamedObj "container"]
    set undoInfoAttribute [java::new ptolemy.moml.UndoInfoAttribute \
	$container "undoInfoAttribute"]
    set undoEntry [$undoInfoAttribute popUndoEntry]
    set redoEntry [$undoInfoAttribute popRedoEntry]
    list [$undoInfoAttribute toString] $undoEntry $redoEntry
} {{ptolemy.moml.UndoInfoAttribute {.container.undoInfoAttribute}} java0x0 java0x0}

test UndoInfoAttribute-2.1 {pushUndoEntry, pushRedoEntry, popUndoEntry, popRedoEntry, peekUndoEntry, peekRedoEntry} {
    set container [java::new ptolemy.kernel.util.NamedObj "container"]
    set undoInfoAttribute [java::new ptolemy.moml.UndoInfoAttribute \
	$container "undoInfoAttribute"]
    set undoEntry [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo"]
    set redoEntry [java::new ptolemy.moml.MoMLUndoEntry \
	$container "redo"]
	

    $undoInfoAttribute pushUndoEntry $undoEntry
    $undoInfoAttribute pushRedoEntry $redoEntry

    list [$undoInfoAttribute peekUndoEntry] \
	[[$undoInfoAttribute popUndoEntry] getUndoMoML] \
	[$undoInfoAttribute popUndoEntry] \
	[$undoInfoAttribute peekUndoEntry] \
	[$undoInfoAttribute peekRedoEntry] \
    	[[$undoInfoAttribute popRedoEntry] getUndoMoML] \
    	[$undoInfoAttribute popRedoEntry] \
	[$undoInfoAttribute peekRedoEntry]
} {undo undo java0x0 {} redo redo java0x0 {}}


######################################################################
####
#
test UndoInfoAttribute-3.1 {mergeTopTowUndos: only one undo} {
    set container [java::new ptolemy.kernel.util.NamedObj "container"]
    set undoInfoAttribute [java::new ptolemy.moml.UndoInfoAttribute \
	$container "undoInfoAttribute"]
    set undoEntry [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo"]
    $undoInfoAttribute pushUndoEntry $undoEntry

    # Only one undo, so we have nothing to merge	
    $undoInfoAttribute mergeTopTwoUndos
    list [[$undoInfoAttribute popUndoEntry] getUndoMoML]
} {undo}

######################################################################
####
#
test UndoInfoAttribute-3.2 {mergeTopTowUndos: three undos} {
    set container [java::new ptolemy.kernel.util.NamedObj "container"]
    set undoInfoAttribute [java::new ptolemy.moml.UndoInfoAttribute \
	$container "undoInfoAttribute"]
    set undoEntry1 [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo1"]
    set undoEntry2 [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo2"]
    set undoEntry3 [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo3"]

    $undoInfoAttribute pushUndoEntry $undoEntry1
    $undoInfoAttribute pushUndoEntry $undoEntry2
    $undoInfoAttribute pushUndoEntry $undoEntry3

    $undoInfoAttribute mergeTopTwoUndos

    list [[$undoInfoAttribute popUndoEntry] getUndoMoML] \
	[[$undoInfoAttribute popUndoEntry] getUndoMoML] \
	[$undoInfoAttribute popUndoEntry]
} {{<group>
undo3undo2</group>
} undo1 java0x0}


######################################################################
####
#
test UndoInfoAttribute-3.2 {mergeTopTowUndos: three undos, middle one has a different context} {
    set container [java::new ptolemy.kernel.util.NamedObj "container"]
    set container2 [java::new ptolemy.kernel.util.NamedObj "container2"]
    set undoInfoAttribute [java::new ptolemy.moml.UndoInfoAttribute \
	$container "undoInfoAttribute"]
    set undoEntry1 [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo1"]

    # This undo has a different context, so it cannot be merged
    set undoEntry2 [java::new ptolemy.moml.MoMLUndoEntry \
	$container2 "undo2"]
    set undoEntry3 [java::new ptolemy.moml.MoMLUndoEntry \
	$container "undo3"]

    $undoInfoAttribute pushUndoEntry $undoEntry1
    $undoInfoAttribute pushUndoEntry $undoEntry2
    $undoInfoAttribute pushUndoEntry $undoEntry3

    $undoInfoAttribute mergeTopTwoUndos

    list [[$undoInfoAttribute popUndoEntry] getUndoMoML] \
	[[$undoInfoAttribute popUndoEntry] getUndoMoML] \
	[[$undoInfoAttribute popUndoEntry] getUndoMoML]
} {undo3 undo2 undo1}

######################################################################
####
#
test UndoInfoAttribute-4.1 {getUndoInfo: null argument and empty argument} {
    catch { java::call ptolemy.moml.UndoInfoAttribute getUndoInfo [java::null]} errMsg

    set container [java::new ptolemy.kernel.util.NamedObj "container"]
    set undoInfoAttribute \
	[java::call ptolemy.moml.UndoInfoAttribute getUndoInfo $container]

    list "$errMsg\n\
	[$container exportMoML]
	[$undoInfoAttribute toString]"
} {{java.lang.Exception: Unable to get undo information on a model without a named object from the model
 <?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="container" class="ptolemy.kernel.util.NamedObj">
</entity>

	ptolemy.moml.UndoInfoAttribute {.container._undoInfo}}}

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
test UndoInfoAttribute-4.2 {getUndoInfo} {
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

    set undoInfoAttribute \
	[java::call ptolemy.moml.UndoInfoAttribute getUndoInfo $toplevel]

    list [$undoInfoAttribute peekUndoEntry] \
	[$undoInfoAttribute peekRedoEntry]
} {{<entity name=".top" >
<deleteEntity name="const" />
</entity>
} {}}
