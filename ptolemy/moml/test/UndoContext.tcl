# Tests for UndoContext: information about the current undo context
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

# The XML header entry to use
set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#

# The base model to use for the vertex tests
set entityTestModelBody {
  <entity name="top" class="ptolemy.actor.TypedCompositeActor">
     <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <relation name="r1" class="ptolemy.actor.TypedIORelation">
		<vertex name="vv" value="22, 33" />
            </relation>
        </entity>
        <relation name="r" class="ptolemy.actor.TypedIORelation"/>
     </entity>
</entity>
}

set entityTestModel "$header $entityTestModelBody"


######################################################################
####
#

test UndoContext-1.1 {Constructor} {
    set undoContext [java::new ptolemy.moml.UndoContext true]
    list [$undoContext toString]
} {{UndoContext: are undoable and does not have undoable children
undoMoML: 
closingUndoMoML: 
}}


test UndoContext-2.1 {Call various methods  } {
    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext appendClosingUndoMoML "closing 1\n"
    $undoContext appendClosingUndoMoML "closing 2\n"
    $undoContext appendUndoMoML "undo 1\n"
    $undoContext appendUndoMoML "undo 2\n"
    $undoContext setChildrenUndoable true;
    $undoContext setUndoable false


    list \
	[$undoContext getUndoMoML] \
	[$undoContext hasUndoMoML] \
	[$undoContext hasUndoableChildren] \
	[$undoContext isUndoable] \
	[$undoContext toString]  
} {{undo 1
undo 2
} 1 1 0 {UndoContext: are not undoable and has undoable children
undoMoML: undo 1
undo 2

closingUndoMoML: closing 2
closing 1

}}

test UndoContext-3.1 {applyRename with no undoMoML } {
    set undoContext [java::new ptolemy.moml.UndoContext true]
    catch {$undoContext applyRename foo} errMsg
    list $errMsg
} {{java.lang.Exception: Cannot rename an element whose parent undo context does not have any undo MoML. Requested new name: foo}}

test UndoContext-3.2 {applyRename with no "name=" in undoMoML } {
    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext appendUndoMoML "name = bar\n"
    catch {$undoContext applyRename foo} errMsg
    list $errMsg
} {{java.lang.Exception: Cannot rename an element whose parent undo context does not have a name attribute in its undo MoML. Requested new name: foo}}

test UndoContext-3.3 {applyRename with "name=", but no closing double quote in undoMoML } {
    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext appendUndoMoML "name=\"bar\n"
    catch {$undoContext applyRename foo} errMsg
    list $errMsg
} {{java.lang.Exception: Cannot rename an element whose parent undo context does not have a valid name attribute in its undo MoML. Requested new name: foo}}

test UndoContext-3.4 {applyRename} {
    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext appendUndoMoML "name=\"bar\"\n"
    $undoContext applyRename foo
    $undoContext appendUndoMoML "name=\"bar\"\n"
    list [$undoContext toString]
} {{UndoContext: are undoable and does not have undoable children
undoMoML: name="foo"
name="bar"

closingUndoMoML: 
}}

test UndoContext-3.4 {moveContextStart: use example from method documentation } {
    set workspace [java::new ptolemy.kernel.util.Workspace workspace]
    set container [java::new ptolemy.kernel.CompositeEntity $workspace] 
    $container setName top
    set a [java::new ptolemy.kernel.CompositeEntity $container a] 
    set b [java::new ptolemy.kernel.CompositeEntity $a b] 
    set c [java::new ptolemy.kernel.CompositeEntity $b c] 
    set containee [java::new ptolemy.kernel.CompositeEntity $c d] 

    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext moveContextStart $container $containee
    $undoContext getUndoMoML
} {<entity name="a.b.c" >
}

test UndoContext-3.4.1 {moveContextStart: use a containee that is already immediately contained} {
    # Use container and a from 3.4 above	
    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext moveContextStart $container $a
    $undoContext getUndoMoML
} {}

test UndoContext-3.5 {moveContextEnd: use example from method documentation } {
    # Use container and containee from 3.4 above	
    set undoContext [java::new ptolemy.moml.UndoContext true]
    $undoContext moveContextEnd $container $containee
    $undoContext getUndoMoML
} {</entity>
}
