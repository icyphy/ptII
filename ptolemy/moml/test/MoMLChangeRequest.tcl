# Tests for the MoMLChangeRequest class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </director>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-1.1 {Test adding an entity} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <entity name="const" class="ptolemy.actor.lib.Const"/>
        </model>
    }]
    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </director>
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
</model>
}

#----------------------------------------------------------------------
test MoMLParser-1.2 {Test adding another entity} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <entity name="rec" class="ptolemy.actor.lib.Recorder"/>
        </model>
    }]
    $manager requestChange $change
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "rec"]]
    $recorder getFullName
} {.top.rec}

#----------------------------------------------------------------------
test MoMLParser-1.3 {Test adding a relation} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <relation name="r" class="ptolemy.actor.TypedIORelation"/>
        </model>
    }]
    $manager requestChange $change
    set r [$toplevel getRelation "r"]
    $r getFullName
} {.top.r}

#----------------------------------------------------------------------
test MoMLParser-1.4 {Test adding a pair of links} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <link relation="r" port="const.output"/>
            <link relation="r" port="rec.input"/>
        </model>
    }]
    $manager requestChange $change
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {1 1}

#----------------------------------------------------------------------
test MoMLParser-1.5 {Test changing a parameter} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <entity name="const">
                <property name="value" value="2"/>
            </entity>
        </model>
    }]
    $manager initialize
    $manager iterate
    $manager requestChange $change
    $manager iterate
    $manager wrapup
    enumToTokenValues [$recorder getRecord 0]
} {1 2}

#----------------------------------------------------------------------
test MoMLParser-1.5 {Test deleting an entity} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <deleteEntity name="const"/>
        </model>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </director>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="rec.input" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-1.6 {Test deleting a relation} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $parser {
        <model name=".top">
            <deleteRelation name="r"/>
        </model>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </director>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-1.6 {Test deleting a port, using a new parser} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel {
        <model name=".top">
            <deletePort name="rec.input"/>
        </model>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </director>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
    </entity>
</model>
}

# FIXME:  delete links