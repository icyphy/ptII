# Tests for the MoMLChangeRequest class
#
# @Author: Edward A. Lee, Contributor: Christopher Hylands
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
test MoMLChangeRequest-1.1 {Test adding an entity} {
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
    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="const" class="ptolemy.actor.lib.Const">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLChangeRequest-1.2 {Test adding another entity} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="rec" class="ptolemy.actor.lib.Recorder"/>
        </entity>
    }]
    $manager requestChange $change
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "rec"]]
    $recorder getFullName
} {.top.rec}

######################################################################
####
#
test MoMLChangeRequest-1.3 {Test adding a relation} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <relation name="r" class="ptolemy.actor.TypedIORelation"/>
        </entity>
    }]
    $manager requestChange $change
    set r [$toplevel getRelation "r"]
    $r getFullName
} {.top.r}

######################################################################
####
#
test MoMLChangeRequest-1.4 {Test adding a pair of links} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <link relation="r" port="const.output"/>
            <link relation="r" port="rec.input"/>
        </entity>
    }]
    $manager requestChange $change
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {1 1}

######################################################################
####
#
test MoMLChangeRequest-1.5a {Test changing a parameter} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="const">
                <property name="value" value="2"/>
            </entity>
        </entity>
    }]
    $manager initialize
    $manager iterate
    $manager requestChange $change
    $manager iterate
    $manager wrapup
    enumToTokenValues [$recorder getRecord 0]
} {1 2}

######################################################################
####
#
test MoMLChangeRequest-1.5b {Test deleting an entity} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <deleteEntity name="const"/>
        </entity>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="rec.input" relation="r"/>
</entity>
}

######################################################################
####
#
test MoMLChangeRequest-1.6a {Test deleting a relation} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <deleteRelation name="r"/>
        </entity>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLChangeRequest-1.6b {Test deleting a port, using a new parser and context} {
    set change1 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <port name="input" class="ptolemy.actor.TypedIOPort"/>
    }]
    $manager requestChange $change1

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deletePort name="input"/>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLChangeRequest-1.7 {Test deleting a property using a lower context} {
    set rec [$toplevel getEntity "rec"]
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $rec {
        <group>
        <property name="foo" class="ptolemy.kernel.util.Attribute"/>
        <deleteProperty name="foo"/>
        </group>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
    </entity>
</entity>
}

# FIXME:  delete links

######################################################################
####
#

# Test propagation of changes from a class to instances.

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
   <class name="gen" extends="ptolemy.kernel.CompositeEntity">
   </class>
   <entity name="der" class=".top.gen"/>
</entity>
}

test MoMLChangeRequest-2.1 {Setup} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $baseModel]]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="gen" extends="ptolemy.kernel.CompositeEntity">
    </class>
    <entity name="der" class=".top.gen">
    </entity>
</entity>
}

test MoMLChangeRequest-2.2 {Test propagation} {
    set gen [$toplevel getEntity "gen"]
    # NOTE: Have to give the context as "gen" for the changes to
    # propogate to its clones.
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $gen {
        <entity name="new" class="ptolemy.kernel.ComponentEntity"/>
    }]
    # NOTE: Request is filled immediately in the toplevel context.
    $toplevel requestChange $change
    # NOTE: exportMoML won't give a full description.
    # 10/28/02: Earlier change to MoMLParser means that the description
    # output now includes URIAttribute.  Not sure why?
    $toplevel description
} {ptolemy.kernel.CompositeEntity {.top} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.top._iconDescription} attributes {
    }}
    {ptolemy.kernel.attributes.URIAttribute {.top._uri} attributes {
    }}
    {ptolemy.moml.ParserAttribute {.top._parser} attributes {
    }}
} ports {
} classes {
    {ptolemy.kernel.CompositeEntity {.top.gen} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.gen._iconDescription} attributes {
        }}
    } ports {
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.gen.new} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.gen.new._iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
} entities {
    {ptolemy.kernel.CompositeEntity {.top.der} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.der._iconDescription} attributes {
        }}
    } ports {
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.der.new} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.der.new._iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
} relations {
}}

######################################################################
####
#

# Test propagation of changes from a class to class to instances.

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
   <class name="gen" extends="ptolemy.kernel.CompositeEntity">
   </class>
   <class name="intClass" extends=".top.gen"/>
   <entity name="der" class=".top.intClass"/>
</entity>
}

test MoMLChangeRequest-3.1 {Setup} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $baseModel]]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="gen" extends="ptolemy.kernel.CompositeEntity">
    </class>
    <class name="intClass" extends=".top.gen">
    </class>
    <entity name="der" class=".top.intClass">
    </entity>
</entity>
}

test MoMLChangeRequest-3.2 {Test propagation} {
    set gen [$toplevel getEntity "gen"]
    # NOTE: Have to give the context as "gen" for the changes to
    # propogate to its clones.
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $gen {
        <entity name="new" class="ptolemy.kernel.ComponentEntity"/>
    }]
    # NOTE: Request is filled immediately in the toplevel context.
    $toplevel requestChange $change
    # NOTE: exportMoML won't give a full description.
    # 10/28/02: Earlier change to MoMLParser means that the description
    # output now includes URIAttribute.  Not sure why?
    $toplevel description
} {ptolemy.kernel.CompositeEntity {.top} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.top._iconDescription} attributes {
    }}
    {ptolemy.kernel.attributes.URIAttribute {.top._uri} attributes {
    }}
    {ptolemy.moml.ParserAttribute {.top._parser} attributes {
    }}
} ports {
} classes {
    {ptolemy.kernel.CompositeEntity {.top.gen} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.gen._iconDescription} attributes {
        }}
    } ports {
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.gen.new} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.gen.new._iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
    {ptolemy.kernel.CompositeEntity {.top.intClass} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.intClass._iconDescription} attributes {
        }}
    } ports {
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.intClass.new} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.intClass.new._iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
} entities {
    {ptolemy.kernel.CompositeEntity {.top.der} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.der._iconDescription} attributes {
        }}
    } ports {
    } classes {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.der.new} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.der.new._iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
} relations {
}}


######################################################################
####
#
set baseModel4 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top4" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir4" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="4"/>
    </property>
</entity>}

set baseModel5 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top5" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir5" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="5"/>
    </property>
</entity>}

test MoMLChangeRequest-4.1 {Call two arg constructor (Originator, request)} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set toplevel4 [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel4]]
    set manager [java::new ptolemy.actor.Manager [$toplevel4 workspace] "w"]
    $toplevel4 setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest \
	    $toplevel4 $baseModel5]
    
    # NOTE: Request is filled immediately because the model is not running.
    # Note that this call also ends up calling MoMLParser.getToplevel()
    $manager requestChange $change

    $toplevel4 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top5" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="dir5" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="allowRateChanges" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
</entity>
} {I'm not sure why the baseModel5 is not showing up?}


######################################################################
####
#
test MoMLChangeRequest-5.1 {getDeferredToParent} {
    # FIXME: This is not a real test for getDeferredToParent.
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel4]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
	     <entity name="const" class="ptolemy.actor.lib.Const"/>
        </entity>
    }]
    set r1 [expr {[java::call \
	    ptolemy.moml.MoMLChangeRequest getDeferredToParent [java::null]] \
	    == [java::null]}] 
    set r2 [expr {[java::call \
	    ptolemy.moml.MoMLChangeRequest getDeferredToParent $toplevel] \
	    == [java::null]}] 
    list $r1 $r2
} {1 1}


# Save the original MoMLParser Error Handler
set originalParserErrorHandler [$parser getErrorHandler]
######################################################################
####
# Procedure used to test setReportErrorsToHandler
proc testSetReportErrorsToHandler {reportErrorsToHandler} {

    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    $parser setErrorHandler $recorderErrorHandler

    set baseModel6 {<?xml version="1.0" standalone="no"?>
    <!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
    <entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
    </entity>
    }
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel6]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
	    <entity name="const" class="ptolemy.actor.lib.XXX"/>
        </entity>
    }]


    $change setReportErrorsToHandler $reportErrorsToHandler

    # We need a ChangeListener at the top
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    $toplevel addChangeListener $listener

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change
    $toplevel exportMoML
    $printStream flush	
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list [string range $output 0 451] \
	    [string range [$recorderErrorHandler getMessages] 0 451]
}

######################################################################
####
#
test MoMLChangeRequest-6.1 {setReportErrorsToHandler true (the default) } {
    # Usually, reportToHandler is set to false.  This test tests
    # the default condition where the error gets popped up to
    # the listener

    testSetReportErrorsToHandler false
} {{StreamChangeRequest.changeFailed(): 
        <entity name=".top">
	    <entity name="const" class="ptolemy.actor.lib.XXX"/>
        </entity>
     failed: com.microstar.xml.XmlException: XML element "entity" triggers exception. in [external stream] at line 3 and column 47
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.actor.lib.XXX
Because:
Could not find 'ptolemy/actor/lib/XXX.xml' or 'ptolemy/actor/lib/XXX.moml} {}}



######################################################################
####
#
test MoMLChangeRequest-6.2 {setReportErrorsToHandler true} {
    testSetReportErrorsToHandler true
} {{StreamChangeRequest.changeExecuted(): 
        <entity name=".top">
	    <entity name="const" class="ptolemy.actor.lib.XXX"/>
        </entity>
     succeeded
} {RecorderErrorHandler: Error encountered in:
<entity name="const" class="ptolemy.actor.lib.XXX">
ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.actor.lib.XXX
Because:
Could not find 'ptolemy/actor/lib/XXX.xml' or 'ptolemy/actor/lib/XXX.moml' using base 'null':  in [external stream] at line 3 and column 47
Caused by:
 com.microstar.xml.XmlException: -- no protocol: ptolemy/actor/lib/XXX.xml
-- XML file not found relative to cl}}


# Restore the original MoMLParser Error Handler
$parser setErrorHandler $originalParserErrorHandler

######################################################################
####
# Sequence of tests for inner classes.

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <class name="inner" extends="ptolemy.actor.CompositeActor">
            <property name="foo" class="ptolemy.data.expr.Parameter" value="1"/>
        </class>
        <entity name="A" class="inner"/>
    </class>
    <entity name="instance" class="master"/>
</entity>
}

set moml "$header $body"
test MoMLChangeRequest-7.1 {test construction of inner class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel description
} {ptolemy.actor.CompositeActor {.top} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.top._iconDescription} attributes {
    }}
    {ptolemy.kernel.attributes.URIAttribute {.top._uri} attributes {
    }}
    {ptolemy.moml.ParserAttribute {.top._parser} attributes {
    }}
} ports {
} classes {
    {ptolemy.actor.CompositeActor {.top.master} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.master._iconDescription} attributes {
        }}
    } ports {
    } classes {
        {ptolemy.actor.CompositeActor {.top.master.inner} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.master.inner._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.master.inner.foo} 1}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } entities {
        {ptolemy.actor.CompositeActor {.top.master.A} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.master.A._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.master.A.foo} 1}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } relations {
    }}
} entities {
    {ptolemy.actor.CompositeActor {.top.instance} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance._iconDescription} attributes {
        }}
    } ports {
    } classes {
        {ptolemy.actor.CompositeActor {.top.instance.inner} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance.inner._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.instance.inner.foo} 1}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } entities {
        {ptolemy.actor.CompositeActor {.top.instance.A} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance.A._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.instance.A.foo} 1}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } relations {
    }}
} relations {
}}

test MoMLChangeRequest-7.2 {test propagation of changes} {
    set toplevel [java::cast ptolemy.actor.CompositeActor $toplevel]
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "master.inner"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="foo" class="ptolemy.data.expr.Parameter" value="2"/>
    }]
    $context requestChange $change
    $toplevel description
} {ptolemy.actor.CompositeActor {.top} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.top._iconDescription} attributes {
    }}
    {ptolemy.kernel.attributes.URIAttribute {.top._uri} attributes {
    }}
    {ptolemy.moml.ParserAttribute {.top._parser} attributes {
    }}
} ports {
} classes {
    {ptolemy.actor.CompositeActor {.top.master} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.master._iconDescription} attributes {
        }}
    } ports {
    } classes {
        {ptolemy.actor.CompositeActor {.top.master.inner} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.master.inner._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.master.inner.foo} 2}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } entities {
        {ptolemy.actor.CompositeActor {.top.master.A} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.master.A._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.master.A.foo} 2}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } relations {
    }}
} entities {
    {ptolemy.actor.CompositeActor {.top.instance} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance._iconDescription} attributes {
        }}
    } ports {
    } classes {
        {ptolemy.actor.CompositeActor {.top.instance.inner} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance.inner._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.instance.inner.foo} 2}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
        {ptolemy.actor.CompositeActor {.top.instance.A} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance.A._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.instance.A.foo} 2}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } entities {
    } relations {
    }}
} relations {
}}
