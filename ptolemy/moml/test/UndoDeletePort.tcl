# Tests for the undoable feature of MoMLParser class
#
# @Author: Neil Smyth
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
     <entity name="level" class="ptolemy.actor.TypedCompositeActor" >
        <port name="upper">
            <property name="input" />
            <property name="output" />
        </port>
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
        <link port="upper" relation="r1"/>
     </entity>
     <relation name="r" class="ptolemy.actor.TypedIORelation" />
     <link port="level.upper" relation="r"/>
</entity>
}

set entityTestModel "$header $entityTestModelBody"

######################################################################
####
#

test UndoDeletePort-1.1 {Test deleting a port is undoable: without entity attribute, simple name} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    # NOTE: the links will not be recreated
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top.level.a">
            <deletePort name="input"/>
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
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        </entity>
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

##

test UndoDeletePort-1.1b {Test deleting a port is undoable: without entity attribute, simple name} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model.
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
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
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="a.input" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

######################################################################
####
#

test UndoDeletePort-1.2a {Test deleting a port is undoable: without entity attribute, complex name} {
   set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <deletePort name="level.a.input" />
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
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        </entity>
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

##

test UndoDeletePort-1.2b {Test deleting a port is undoable: without entity attribute, complex name} {
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
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
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
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="a.input" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

######################################################################
####
#

test UndoDeletePort-1.3a {Test deleting a port is undoable: with entity attribute, simple name, no inside links} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top.level">
            <deletePort name="input" entity="a" />
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
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        </entity>
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

##

test UndoDeletePort-1.3b {Test deleting a port is undoable: with entity attribute, simple name, no inside links} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model minus some links
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
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
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="a.input" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

######################################################################
####
#

test UndoDeletePort-1.4a {Test deleting a port is undoable: with entity attribute, complex name, no inside links} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <deletePort name="input" entity="level.a" />
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
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
        <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        </entity>
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

##

test UndoDeletePort-1.4b {Test deleting a port is undoable: with entity attribute, complex name, no inside links} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model minus some links
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
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
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="a.input" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}

######################################################################
####
#

test UndoDeletePort-1.5a {Test deleting a port is undoable: with entity
                        attribute, with inside and outside links} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $entityTestModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <deletePort name="upper" entity="level" />
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
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
            <port name="output" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
            </port>
        </entity>
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="a.input" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
</entity>
}

##

test UndoDeletePort-1.5b {Test deleting a port is undoable: with entity attribute, with inside links} {
    # Now create the MoMLUndoChangeRequest which will undo the change
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $undochange

    # Should be back to the base model.
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="level" class="ptolemy.actor.TypedCompositeActor">
        <port name="upper" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="output"/>
        </port>
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
        <relation name="r1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="upper" relation="r1"/>
        <link port="a.input" relation="r1"/>
        <link port="b.output" relation="r1"/>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="level.upper" relation="r"/>
</entity>
}
