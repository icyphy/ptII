# Tests for the undoable feature of MoMLParser class
#
# @Author: Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
     </entity>
</entity>
}

set entityTestModel "$header $entityTestModelBody"


######################################################################
####
#

test UndoPort-1.1a {Test undoing a port creation} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top.level">
            <port name="test" class="ptolemy.actor.TypedIOPort"/>
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
        <port name="test" class="ptolemy.actor.TypedIOPort">
        </port>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

##

test UndoPort-1.1b {Test undoing a port creation} {
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
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

######################################################################
####
#

test UndoPort-1.2a {Test undoing a port transition: simple name} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top.level.a">
            <port name="input" >
                <property name="input"/>
            </port>
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
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

##

test UndoPort-1.2b {Test undoing a port transition: simple name} {
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
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

######################################################################
####
#

test UndoPort-1.3a {Test undoing a port transition: complex name} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top.level">
            <port name="a.input"/>
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
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

##

test UndoPort-1.3b {Test undoing a port transition: complex name} {
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
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
            <port name="input" class="ptolemy.actor.TypedIOPort">
                <property name="input"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
    </entity>
</entity>
}

##

set twoPortAdd {
    <entity name="AddSubtract" class="ptolemy.actor.lib.AddSubtract">
    </entity>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{145, 260}">
        </property>
    </entity>
    <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="AddSubtract.plus" relation="relation"/>
    <link port="AddSubtract.plus" relation="relation2"/>
    <link port="Const.output" relation="relation2"/>
    <link port="Ramp.output" relation="relation"/>
}

set baseModel2 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
</entity>
}

test UndoPort-2.1 {Test undoing an actor with two channels} {
    set w [java::new ptolemy.kernel.util.Workspace w2_1]
    set parser [java::new ptolemy.moml.MoMLParser $w]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    set toplevel [java::cast ptolemy.actor.CompositeActor \
		      [$parser parse $baseModel2]]
    # This replicates the paste action, where we put things in a group
    set changeRequest [java::new ptolemy.moml.MoMLChangeRequest \
			   $toplevel $toplevel \
			   "<group name=\"auto\">$twoPortAdd</group>" ]

    # Mark the change as being undoable
    $changeRequest setUndoable true
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] \
		     "myManager"]
    $toplevel setManager $manager
    $manager requestChange $changeRequest

    $toplevel exportMoML

} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <entity name="AddSubtract" class="ptolemy.actor.lib.AddSubtract">
    </entity>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="_location" class="ptolemy.kernel.util.Location" value="{145, 260}">
        </property>
    </entity>
    <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="AddSubtract.plus" relation="relation"/>
    <link port="AddSubtract.plus" relation="relation2"/>
    <link port="Const.output" relation="relation2"/>
    <link port="Ramp.output" relation="relation"/>
</entity>
}


##

test UndoPort-2.2 {Test undoing port creation on two ports in a group} {
    # This replicates a bug that occurs when we have an AddSubtract
    # that has two connections on the + port and we paste it and then
    # try undo
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
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
</entity>
}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

