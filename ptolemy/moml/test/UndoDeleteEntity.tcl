# Tests for the undoable feature of MoMLParser class
#
# @Author: Neil Smyth, Christopher Hylands
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

# The XML header entry to use
set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#

# The base model to use for the entity tests
set entityTestModelBody {
  <entity name="top" class="ptolemy.actor.TypedCompositeActor">
     <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
              <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
              </port>
        </entity>
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation" />
        <link port="b.output" relation="r1"/>
        <link port="a.input" relation="r1"/>
     </entity>
</entity>
}

set entityTestModel "$header $entityTestModelBody"


######################################################################
####
#

test UndoDeleteEntity-1.1a {Test undoing an entity deletion: simple name} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top.level">
            <deleteEntity name="a" />
        </entity>
    }]
    # Mark the change as being undoable
    $change setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change

    # Export the modified MoML
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="b.output" relation="r1"/>
    </entity>
</entity>
}

##

test UndoDeleteEntity-1.1b {Test undoing an entity deletion: simple name} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model...
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="b.output" relation="r1"/>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

######################################################################
####
#

test UndoDeleteEntity-1.2a {Test undoing an entity deletion: complex name} {
   set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <deleteEntity name="level.a" />
        </entity>
    }]
    # Mark the change as being undoable
    $change setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change

    # Export the modified MoML
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="b.output" relation="r1"/>
    </entity>
</entity>
}

##

test UndoDeleteEntity-1.2b {Test undoing an entity deletion: complex name} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model...
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="b.output" relation="r1"/>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

##

test UndoDeleteEntity-1.2c {Call undo again, which should do nothing} {
    # Create another undo
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange


    # Create another undo
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model...
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="b.output" relation="r1"/>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}



######################################################################
####
#
test UndoDeleteEntity-1.3a {Delete an entity in a composite actor} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
		      [$parser parseFile UndoDeleteEntityComposite.xml]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".UndoDeleteEntityComposite.composite actor">
	       <deleteEntity name="AddSubtract" />
        </entity>
    }]
    # Mark the change as being undoable
    $change setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change

    # Export the modified MoML
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="UndoDeleteEntityComposite" class="ptolemy.actor.TypedCompositeActor">
    <property name="annotation" class="ptolemy.kernel.util.Attribute">
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue">Delete the add actor inside and the redo</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="165.0, 95.0">
        </property>
    </property>
    <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={104, 126, 815, 516}}">
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[600, 400]">
    </property>
    <entity name="composite actor" class="ptolemy.actor.TypedCompositeActor">
        <property name="_location" class="ptolemy.kernel.util.Location" value="245.0, 195.0">
        </property>
        <port name="port" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="_location" class="ptolemy.kernel.util.Location" value="20.0, 200.0">
            </property>
        </port>
        <port name="port2" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="_location" class="ptolemy.kernel.util.Location" value="575.0, 190.0">
            </property>
        </port>
        <relation name="relation" class="ptolemy.actor.TypedIORelation">
        </relation>
        <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="port" relation="relation2"/>
        <link port="port2" relation="relation"/>
    </entity>
</entity>
}

test UndoDeleteEntity-1.3b {Delete an entity in a composite actor: Now call undo} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model...
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="UndoDeleteEntityComposite" class="ptolemy.actor.TypedCompositeActor">
    <property name="annotation" class="ptolemy.kernel.util.Attribute">
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue">Delete the add actor inside and the redo</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="165.0, 95.0">
        </property>
    </property>
    <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={104, 126, 815, 516}}">
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[600, 400]">
    </property>
    <entity name="composite actor" class="ptolemy.actor.TypedCompositeActor">
        <property name="_location" class="ptolemy.kernel.util.Location" value="245.0, 195.0">
        </property>
        <port name="port" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="_location" class="ptolemy.kernel.util.Location" value="20.0, 200.0">
            </property>
        </port>
        <port name="port2" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="_location" class="ptolemy.kernel.util.Location" value="575.0, 190.0">
            </property>
        </port>
        <entity name="AddSubtract" class="ptolemy.actor.lib.AddSubtract">
            <property name="_location" class="ptolemy.kernel.util.Location" value="235.0, 190.0">
            </property>
        </entity>
        <relation name="relation" class="ptolemy.actor.TypedIORelation">
        </relation>
        <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="port" relation="relation2"/>
        <link port="port2" relation="relation"/>
        <link port="AddSubtract.plus" relation="relation2"/>
        <link port="AddSubtract.output" relation="relation"/>
    </entity>
</entity>
}


if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

######################################################################
####
#
test UndoDeleteEntity-1.4a {Call undo on a TypedCompositeActor that has not yet saved.  Note that this model does not have a name or a _parser attribute}  {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # For some reason the stack trace is dumped to stderr for us,
    # perhaps because of the cancel facility
    jdkCaptureErr {
	# NOTE: Request is filled immediately because the model is not running.
	catch {$manager requestChange $undochange} errMsg
    } message
    # This test returns nothing because there is no _parser attribute 
    # This test is here to increase basic block coverage
    list [string range $message 0 500 ]
} {{}}
