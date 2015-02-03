# Tests for the MoMLChangeRequest class
#
# @Author: Edward A. Lee, Contributor: Christopher Hylands
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
    list [[$change getContext] getFullName] [$toplevel exportMoML]
} {.top {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.1-alpha">
    </property>
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="const" class="ptolemy.actor.lib.Const">
    </entity>
</entity>
}}


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

######################################################################
####
#
test MoMLChangeRequest-1.8 {Test property deletion of a RequireVersion attribute} {
    # VersionAttribute.equals() has a bug where if we had a 
    # VersionAttribute and a RequireVersion and the RequireVersion
    # is deleted, then only the first VersionAttribute was deleted, so we
    # define equals() and hashCode().  For details, see
    # http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3984
    set VersionAttribute [java::new ptolemy.kernel.attributes.RequireVersion $toplevel "VersionAttribute"]
    set requireVersion [java::new ptolemy.kernel.attributes.RequireVersion $toplevel "RequireVersion"]
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <group>
        <deleteProperty name="RequireVersion"/>
        </group>
    }]
    set change
    $manager requestChange $change
    set moml [$toplevel exportMoML]
    # The nightly build changes the build version, so we substitute
    regsub {VersionAttribute" value="[^"]*"} $moml {VersionAttribute" value="PtVersionElided"} moml2
    regsub {RequireVersion" value="[^"]*"} $moml2 {RequireVersion" value="PtVersionElided"} moml3
    list $moml3
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="PtVersionElided">
    </property>
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <property name="VersionAttribute" class="ptolemy.kernel.attributes.RequireVersion" value="PtVersionElided">
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
    </entity>
</entity>
}}


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
set originalParserErrorHandler [java::call ptolemy.moml.MoMLParser getErrorHandler]
######################################################################
####
# Procedure used to test setReportErrorsToHandler
proc testSetReportErrorsToHandler {reportErrorsToHandler} {
    global PTII

    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $recorderErrorHandler

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

    regsub -all {\\} \
	[[[java::new java.io.File $PTII] getCanonicalFile] toString] \
	{/} ptII

    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    regsub -all {\\} $output {/} output2
    regsub -all $ptII \
		$output2 \
		{$PTII} output3

    # The error message contains the full path, so we substitute in $PTII
    regsub -all {\\} \
	        [$recorderErrorHandler getMessages] \
       	        {/} output4
    regsub -all $ptII \
	        $output4 \
		{$PTII} output5

    # The IOException differs between Windows and Solaris,
    # so we truncate the message.
    list [string range $output3 0 519] {...} \
	[string range $output5 0 330] {...}
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
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.actor.lib.XXX. In Ptolemy, classes are typically Java .class files. Entities like actors may instead be defined within a .xml file.  In any case, the class was not } ... {} ...}

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
} ... {RecorderErrorHandler: Error encountered in:
<entity name="const" class="ptolemy.actor.lib.XXX">
ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.actor.lib.XXX. In Ptolemy, classes are typically Java .class files. Entities like actors may instead be defined within a .xml file.  In any case, the class was not } ...}



# Restore the original MoMLParser Error Handler
java::call ptolemy.moml.MoMLParser setErrorHandler $originalParserErrorHandler

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
    } entities {
        {ptolemy.actor.CompositeActor {.top.instance.A} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.top.instance.A._iconDescription} attributes {
            }}
            {ptolemy.data.expr.Parameter {.top.instance.A.foo} 2}
        } ports {
        } classes {
        } entities {
        } relations {
        }}
    } relations {
    }}
} relations {
}}

######################################################################
####
# Deep deferrals

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="cA" extends="ptolemy.actor.CompositeActor">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
               <property name="p" class="ptolemy.data.expr.Parameter" value="1"/>
            </class>
            <entity name="iABC" class="cABC"/>
        </class>
        <entity name="iAB" class="cAB"/>
    </class>
    <class name="cD" extends="cA">
    </class>
</entity>
}

set moml "$header $body"
test MoMLChangeRequest-8.0 {test construction of inner class} {
    $parser reset
    set toplevel [$parser parse $moml]

	set cccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.cABC.p}]]
	set ccipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.iABC.p}]]
	set cicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.cABC.p}]]
	set ciipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.iABC.p}]]
	set cccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.cABC.p}]]
	set ccipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.iABC.p}]]
	set cicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.cABC.p}]]
	set ciipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.iABC.p}]]

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
} {1 1 1 1 1 1 1 1}

test MoMLChangeRequest-8.2 {test propagation of change from master class} {
    set toplevel [java::cast ptolemy.actor.CompositeActor $toplevel]
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.cAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="2"/>
    }]
    $context requestChange $change
    
	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
} {2 2 2 2 2 2 2 2}

######################################################################
####
# more Deep deferrals

set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="cA" extends="ptolemy.actor.CompositeActor">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
               <property name="p" class="ptolemy.data.expr.Parameter" value="1"/>
            </class>
            <entity name="iABC" class="cABC"/>
        </class>
        <entity name="iAB" class="cAB"/>
    </class>
    <class name="cD" extends="cA">
    </class>
    <entity name="iA" class="cA"/>
    <entity name="iD" class="cD"/>
</entity>
}

set moml "$header $body"
test MoMLChangeRequest-9.0 {test construction of inner class} {
    $parser reset
    set toplevel [$parser parse $moml]
    set iABCp [$toplevel getAttribute {iA.iAB.iABC.p}]
    $iABCp getFullName
} {.top.iA.iAB.iABC.p}

test MoMLChangeRequest-9.1 {test values of all instances} {
	set cccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.cABC.p}]]
	set ccipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.iABC.p}]]
	set cicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.cABC.p}]]
	set ciipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.iABC.p}]]
	set cccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.cABC.p}]]
	set ccipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.iABC.p}]]
	set cicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.cABC.p}]]
	set ciipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.iABC.p}]]
	set iccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.cAB.cABC.p}]]
	set icipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.cAB.iABC.p}]]
	set iicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.iAB.cABC.p}]]
	set iiipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.iAB.iABC.p}]]
	set iccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.cAB.cABC.p}]]
	set icipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.cAB.iABC.p}]]
	set iicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.iAB.cABC.p}]]
	set iiipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.iAB.iABC.p}]]

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1}

test MoMLChangeRequest-9.1.1 {test parent relationships} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	set iABC1 [$toplevel getEntity {cA.cAB.iABC}]
	set iABC2 [$toplevel getEntity {cA.iAB.iABC}]
	set iABC3 [$toplevel getEntity {cD.cAB.iABC}]
	set iABC4 [$toplevel getEntity {cD.iAB.iABC}]
	set iABC5 [$toplevel getEntity {iA.cAB.iABC}]
	set iABC6 [$toplevel getEntity {iA.iAB.iABC}]
	set iABC7 [$toplevel getEntity {iD.cAB.iABC}]
	set iABC8 [$toplevel getEntity {iD.iAB.iABC}]

	list \
	[[$iABC1 getParent] getFullName]\
	[[$iABC2 getParent] getFullName]\
	[[$iABC3 getParent] getFullName]\
	[[$iABC4 getParent] getFullName]\
	[[$iABC5 getParent] getFullName]\
	[[$iABC6 getParent] getFullName]\
	[[$iABC7 getParent] getFullName]\
	[[$iABC8 getParent] getFullName]
} {.top.cA.cAB.cABC .top.cA.iAB.cABC .top.cD.cAB.cABC .top.cD.iAB.cABC .top.iA.cAB.cABC .top.iA.iAB.cABC .top.iD.cAB.cABC .top.iD.iAB.cABC}

test MoMLChangeRequest-9.1.2 {test parent relationships} {
	set iAB1 [$toplevel getEntity {cA.iAB}]
	set iAB2 [$toplevel getEntity {cD.iAB}]
	set iAB3 [$toplevel getEntity {iA.iAB}]
	set iAB4 [$toplevel getEntity {iD.iAB}]

	list \
	[[$iAB1 getParent] getFullName]\
	[[$iAB2 getParent] getFullName]\
	[[$iAB3 getParent] getFullName]\
	[[$iAB4 getParent] getFullName]\
} {.top.cA.cAB .top.cD.cAB .top.iA.cAB .top.iD.cAB}

test MoMLChangeRequest-9.1.3 {test parent relationships} {
	set cD [$toplevel getEntity {cD}]
	set iA [$toplevel getEntity {iA}]
	set iD [$toplevel getEntity {iD}]

	list \
	[[$cD getParent] getFullName]\
	[[$iA getParent] getFullName]\
	[[$iD getParent] getFullName]
} {.top.cA .top.cA .top.cD}

test MoMLChangeRequest-9.1.4 {test derivedLevel values} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	list \
	[[$toplevel getAttribute {cA.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.iAB.iABC.p}] getDerivedLevel] \
} {2147483647 1 2 1 3 1 2 1 3 1 2 1 3 1 2 1}

test MoMLChangeRequest-9.1.5 {test derivedLevel values} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	list \
	[[$toplevel getEntity {cA.cAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {cA.cAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {cA.iAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {cA.iAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {cD.cAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {cD.cAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {cD.iAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {cD.iAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {iA.cAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {iA.cAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {iA.iAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {iA.iAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {iD.cAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {iD.cAB.iABC}] getDerivedLevel] \
	[[$toplevel getEntity {iD.iAB.cABC}] getDerivedLevel] \
	[[$toplevel getEntity {iD.iAB.iABC}] getDerivedLevel] \
} {2147483647 2147483647 1 1 2 2 1 1 2 2 1 1 2 2 1 1}

test MoMLChangeRequest-9.1.6 {test derivedLevel values} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	list \
	[[$toplevel getEntity {cA.cAB}] getDerivedLevel] \
	[[$toplevel getEntity {cA.iAB}] getDerivedLevel] \
	[[$toplevel getEntity {cD.cAB}] getDerivedLevel] \
	[[$toplevel getEntity {cD.iAB}] getDerivedLevel] \
	[[$toplevel getEntity {iA.cAB}] getDerivedLevel] \
	[[$toplevel getEntity {iA.iAB}] getDerivedLevel] \
	[[$toplevel getEntity {iD.cAB}] getDerivedLevel] \
	[[$toplevel getEntity {iD.iAB}] getDerivedLevel] \
} {2147483647 2147483647 1 1 1 1 1 1}

test MoMLChangeRequest-9.1.7 {test derivedLevel values} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	list \
	[[$toplevel getEntity {cA}] getDerivedLevel] \
	[[$toplevel getEntity {cD}] getDerivedLevel] \
	[[$toplevel getEntity {iA}] getDerivedLevel] \
	[[$toplevel getEntity {iD}] getDerivedLevel] \
} {2147483647 2147483647 2147483647 2147483647}

test MoMLChangeRequest-9.1.8 {test getPrototypeList} {
	list \
	[listToFullNames [[$toplevel getAttribute {cA.cAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cA.cAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cA.iAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cA.iAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cD.cAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cD.cAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cD.iAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {cD.iAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iA.cAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iA.cAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iA.iAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iA.iAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iD.cAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iD.cAB.iABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iD.iAB.cABC.p}] getPrototypeList]] \
	[listToFullNames [[$toplevel getAttribute {iD.iAB.iABC.p}] getPrototypeList]] \
} {{} .top.cA.cAB.cABC.p .top.cA.cAB.cABC.p {.top.cA.iAB.cABC.p .top.cA.cAB.iABC.p} .top.cA.cAB.cABC.p {.top.cD.cAB.cABC.p .top.cA.cAB.iABC.p} {.top.cD.cAB.cABC.p .top.cA.iAB.cABC.p} {.top.cD.iAB.cABC.p .top.cD.cAB.iABC.p .top.cA.iAB.iABC.p} .top.cA.cAB.cABC.p {.top.iA.cAB.cABC.p .top.cA.cAB.iABC.p} {.top.iA.cAB.cABC.p .top.cA.iAB.cABC.p} {.top.iA.iAB.cABC.p .top.iA.cAB.iABC.p .top.cA.iAB.iABC.p} .top.cD.cAB.cABC.p {.top.iD.cAB.cABC.p .top.cD.cAB.iABC.p} {.top.iD.cAB.cABC.p .top.cD.iAB.cABC.p} {.top.iD.iAB.cABC.p .top.iD.cAB.iABC.p .top.cD.iAB.iABC.p}}

test MoMLChangeRequest-9.1.9 {test getPrototypeList} {
	list \
	[listToFullNames [[$toplevel getEntity {cA.cAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cA.cAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cA.iAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cA.iAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cD.cAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cD.cAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cD.iAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cD.iAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iA.cAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iA.cAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iA.iAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iA.iAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iD.cAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iD.cAB.iABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iD.iAB.cABC}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iD.iAB.iABC}] getPrototypeList]] \
} {{} .top.cA.cAB.cABC .top.cA.cAB.cABC {.top.cA.iAB.cABC .top.cA.cAB.iABC} .top.cA.cAB.cABC {.top.cD.cAB.cABC .top.cA.cAB.iABC} {.top.cD.cAB.cABC .top.cA.iAB.cABC} {.top.cD.iAB.cABC .top.cD.cAB.iABC .top.cA.iAB.iABC} .top.cA.cAB.cABC {.top.iA.cAB.cABC .top.cA.cAB.iABC} {.top.iA.cAB.cABC .top.cA.iAB.cABC} {.top.iA.iAB.cABC .top.iA.cAB.iABC .top.cA.iAB.iABC} .top.cD.cAB.cABC {.top.iD.cAB.cABC .top.cD.cAB.iABC} {.top.iD.cAB.cABC .top.cD.iAB.cABC} {.top.iD.iAB.cABC .top.iD.cAB.iABC .top.cD.iAB.iABC}}

test MoMLChangeRequest-9.1.10 {test getPrototypeList} {
	list \
	[listToFullNames [[$toplevel getEntity {cA.cAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cA.iAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cD.cAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {cD.iAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iA.cAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iA.iAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iD.cAB}] getPrototypeList]] \
	[listToFullNames [[$toplevel getEntity {iD.iAB}] getPrototypeList]] \
} {{} .top.cA.cAB .top.cA.cAB {.top.cD.cAB .top.cA.iAB} .top.cA.cAB {.top.iA.cAB .top.cA.iAB} .top.cD.cAB {.top.iD.cAB .top.cD.iAB}}

test MoMLChangeRequest-9.2 {test propagation of change from master class} {
    set toplevel [java::cast ptolemy.actor.CompositeActor $toplevel]
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.cAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="2"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2}

test MoMLChangeRequest-9.2.2 {test second propagation of change from master class} {
    set toplevel [java::cast ptolemy.actor.CompositeActor $toplevel]
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.cAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="0"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0}


test MoMLChangeRequest-9.3 {test changed from class of instance} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "iA.iAB.iABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="3"/>
    }]
    $context requestChange $change
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.cAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="4"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 4 4 4 4 4 4 4 4 4 4 3 4 4 4 4}

test MoMLChangeRequest-9.4 {test propagation from instance in class} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.cAB.iABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="5"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 5 4 5 4 5 4 5 4 5 4 3 4 5 4 5}

test MoMLChangeRequest-9.5 {test propagation from inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.iAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="6"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 5 6 6 4 5 6 6 4 5 6 3 4 5 6 6}

test MoMLChangeRequest-9.6 {test propagation from deeper inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cD.iAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="7"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 5 6 6 4 5 7 7 4 5 6 3 4 5 7 7}

test MoMLChangeRequest-9.7 {test shadowing on deeper inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cD.cAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="8"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 5 6 6 8 8 7 7 4 5 6 3 8 8 7 7}

test MoMLChangeRequest-9.8 {test shadowing on deeper inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA.cAB.iABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="9"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 6 3 8 8 7 7}

test MoMLChangeRequest-9.9 {test shadowing on deeper inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "iD.cAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="10"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 6 3 10 10 10 10}

test MoMLChangeRequest-9.10 {test shadowing on deeper inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "iA.iAB.iABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="11"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 6 11 10 10 10 10}

test MoMLChangeRequest-9.11 {test shadowing on deeper inner subclass} {
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "iA.iAB.cABC"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="12"/>
    }]
    $context requestChange $change

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 12 11 10 10 10 10}

######################################################################
####
# Export MoML of inner components (builds on the above).

test MoMLChangeRequest-10.1 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cA.cAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="4">
    </property>
</class>
}

test MoMLChangeRequest-10.2 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cA.cAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
    <property name="p" class="ptolemy.data.expr.Parameter" value="9">
    </property>
</entity>
}

test MoMLChangeRequest-10.3 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cA.iAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="6">
    </property>
</class>
}

test MoMLChangeRequest-10.4 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cA.iAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
</entity>
}

test MoMLChangeRequest-10.5 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cD.cAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="8">
    </property>
</class>
}

test MoMLChangeRequest-10.6 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cD.cAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
</entity>
}

test MoMLChangeRequest-10.7 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cD.iAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="7">
    </property>
</class>
}

test MoMLChangeRequest-10.8 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cD.iAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
</entity>
}

test MoMLChangeRequest-10.9 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iA.cAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="4">
    </property>
</class>
}

test MoMLChangeRequest-10.10 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iA.cAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
    <property name="p" class="ptolemy.data.expr.Parameter" value="9">
    </property>
</entity>
}

test MoMLChangeRequest-10.11 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iA.iAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="12">
    </property>
</class>
}

test MoMLChangeRequest-10.12 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iA.iAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
    <property name="p" class="ptolemy.data.expr.Parameter" value="11">
    </property>
</entity>
}

test MoMLChangeRequest-10.13 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iD.cAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="10">
    </property>
</class>
}

test MoMLChangeRequest-10.14 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iD.cAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
</entity>
}

test MoMLChangeRequest-10.15 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iD.iAB.cABC"]
    $export exportMoML
} {<class name="cABC" extends="ptolemy.actor.CompositeActor">
    <property name="p" class="ptolemy.data.expr.Parameter" value="10">
    </property>
</class>
}

test MoMLChangeRequest-10.16 {test export MoML with parameter values} {
    set export [$toplevel getEntity "iD.iAB.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
    <property name="p" class="ptolemy.data.expr.Parameter" value="10">
    </property>
</entity>
}

######################################################################
####
# Export MoML one level up (builds on the above).

test MoMLChangeRequest-11.1 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cA.cAB"]
    $export exportMoML
} {<class name="cAB" extends="ptolemy.actor.CompositeActor">
    <class name="cABC" extends="ptolemy.actor.CompositeActor">
        <property name="p" class="ptolemy.data.expr.Parameter" value="4">
        </property>
    </class>
    <entity name="iABC" class="cABC">
        <property name="p" class="ptolemy.data.expr.Parameter" value="9">
        </property>
    </entity>
</class>
}

test MoMLChangeRequest-11.2 {test export MoML with parameter values} {
    set export [$toplevel getEntity "cA.iAB"]
    $export exportMoML
} {<entity name="iAB" class="cAB">
    <class name="cABC" extends="ptolemy.actor.CompositeActor">
        <property name="p" class="ptolemy.data.expr.Parameter" value="6">
        </property>
    </class>
</entity>
}

# Now paste the above MoML in the same context.
test MoMLChangeRequest-11.2.1 {test paste with inherited and overridden values} {
    set paste [$export exportMoML {iABcopy}]
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "cA"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context $paste]
    $context requestChange $change
    set export [$toplevel getEntity "cA.iABcopy.iABC"]
    $export exportMoML
} {<entity name="iABC" class="cABC">
</entity>
}

# Check what the paste MoML looks like.
test MoMLChangeRequest-11.2.2 {test paste with inherited and overridden values} {
    list $paste
} {{<entity name="iABcopy" class="cAB">
    <class name="cABC" extends="ptolemy.actor.CompositeActor">
        <property name="p" class="ptolemy.data.expr.Parameter" value="6">
        </property>
    </class>
</entity>
}}

test MoMLChangeRequest-11.2.2.1 {test paste with inherited and overridden values} {
    set export [$toplevel getEntity "cA.iABcopy"]
    set cABC [$toplevel getEntity "cA.iABcopy.cABC"]
    set iABC [$toplevel getEntity "cA.iABcopy.iABC"]
    set p1 [$toplevel getAttribute "cA.iABcopy.cABC.p"]
    set p2 [$toplevel getAttribute "cA.iABcopy.iABC.p"]
    list \
    [$export getDerivedLevel] \
    [$cABC getDerivedLevel] \
    [$iABC getDerivedLevel] \
    [$p1 getDerivedLevel] \
    [$p2 getDerivedLevel] \
} {2147483647 1 1 2 1}

test MoMLChangeRequest-11.2.2.2 {test paste with inherited and overridden values} {
    set export [$toplevel getEntity "cD.iABcopy"]
    set cABC [$toplevel getEntity "cD.iABcopy.cABC"]
    set iABC [$toplevel getEntity "cD.iABcopy.iABC"]
    set p1 [$toplevel getAttribute "cD.iABcopy.cABC.p"]
    set p2 [$toplevel getAttribute "cD.iABcopy.iABC.p"]
    list \
    [$export getDerivedLevel] \
    [$cABC getDerivedLevel] \
    [$iABC getDerivedLevel] \
    [$p1 getDerivedLevel] \
    [$p2 getDerivedLevel] \
} {1 1 1 2 1}

test MoMLChangeRequest-11.2.2.3 {test paste with inherited and overridden values} {
    set export [$toplevel getEntity "cA.iABcopy"]
    $export exportMoML
} {<entity name="iABcopy" class="cAB">
    <class name="cABC" extends="ptolemy.actor.CompositeActor">
        <property name="p" class="ptolemy.data.expr.Parameter" value="6">
        </property>
    </class>
</entity>
}

# Check propagation of the paste.
test MoMLChangeRequest-11.2.3 {test paste with inherited and overridden values} {
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="4.0-beta">
    </property>
    <class name="cA" extends="ptolemy.actor.CompositeActor">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="4">
                </property>
            </class>
            <entity name="iABC" class="cABC">
                <property name="p" class="ptolemy.data.expr.Parameter" value="9">
                </property>
            </entity>
        </class>
        <entity name="iAB" class="cAB">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="6">
                </property>
            </class>
        </entity>
        <entity name="iABcopy" class="cAB">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="6">
                </property>
            </class>
        </entity>
    </class>
    <class name="cD" extends="cA">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="8">
                </property>
            </class>
        </class>
        <entity name="iAB" class="cAB">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="7">
                </property>
            </class>
        </entity>
    </class>
    <entity name="iA" class="cA">
        <entity name="iAB" class="cAB">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="12">
                </property>
            </class>
            <entity name="iABC" class="cABC">
                <property name="p" class="ptolemy.data.expr.Parameter" value="11">
                </property>
            </entity>
        </entity>
    </entity>
    <entity name="iD" class="cD">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="10">
                </property>
            </class>
        </class>
    </entity>
</entity>
}

######################################################################
#### Test undo of attribute deletion in the base class

test MoMLChangeRequest-12.0 {Check baseline values before deletion} {
	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 12 11 10 10 10 10}

test MoMLChangeRequest-12.1 {first check propagation of deletion} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deleteProperty name="cA.cAB.cABC.p"/>
    }]
    $change setUndoable true
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="cA" extends="ptolemy.actor.CompositeActor">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
            </class>
            <entity name="iABC" class="cABC">
            </entity>
        </class>
        <entity name="iAB" class="cAB">
        </entity>
        <entity name="iABcopy" class="cAB">
        </entity>
    </class>
    <class name="cD" extends="cA">
    </class>
    <entity name="iA" class="cA">
    </entity>
    <entity name="iD" class="cD">
    </entity>
</entity>
}

test MoMLChangeRequest-12.2 {then check undo} {
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
	# Have to get these again because we have new attributes.
	set cccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.cABC.p}]]
	set ccipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.iABC.p}]]
	set cicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.cABC.p}]]
	set ciipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.iABC.p}]]
	set cccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.cABC.p}]]
	set ccipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.iABC.p}]]
	set cicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.cABC.p}]]
	set ciipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.iABC.p}]]
	set iccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.cAB.cABC.p}]]
	set icipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.cAB.iABC.p}]]
	set iicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.iAB.cABC.p}]]
	set iiipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.iAB.iABC.p}]]
	set iccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.cAB.cABC.p}]]
	set icipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.cAB.iABC.p}]]
	set iicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.iAB.cABC.p}]]
	set iiipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.iAB.iABC.p}]]

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 12 11 10 10 10 10}

test MoMLChangeRequest-12.2.1 {check derivedLevel values} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	list \
	[[$toplevel getAttribute {cA.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.iAB.iABC.p}] getDerivedLevel] \
} {2147483647 1 2 1 3 1 2 1 3 1 2 1 3 1 2 1}

test MoMLChangeRequest-12.3 {check propagation of deletion higher up} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deleteEntity name="cA.iAB"/>
    }]
    $change setUndoable true
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="4.0-beta">
    </property>
    <class name="cA" extends="ptolemy.actor.CompositeActor">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="4">
                </property>
            </class>
            <entity name="iABC" class="cABC">
                <property name="p" class="ptolemy.data.expr.Parameter" value="9">
                </property>
            </entity>
        </class>
        <entity name="iABcopy" class="cAB">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="6">
                </property>
            </class>
        </entity>
    </class>
    <class name="cD" extends="cA">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="8">
                </property>
            </class>
        </class>
    </class>
    <entity name="iA" class="cA">
    </entity>
    <entity name="iD" class="cD">
        <class name="cAB" extends="ptolemy.actor.CompositeActor">
            <class name="cABC" extends="ptolemy.actor.CompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="10">
                </property>
            </class>
        </class>
    </entity>
</entity>
}

test MoMLChangeRequest-12.4 {then check undo} {
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
	# Have to get these again because we have new attributes.
	set cccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.cABC.p}]]
	set ccipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.cAB.iABC.p}]]
	set cicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.cABC.p}]]
	set ciipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cA.iAB.iABC.p}]]
	set cccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.cABC.p}]]
	set ccipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.cAB.iABC.p}]]
	set cicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.cABC.p}]]
	set ciipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {cD.iAB.iABC.p}]]
	set iccpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.cAB.cABC.p}]]
	set icipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.cAB.iABC.p}]]
	set iicpA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.iAB.cABC.p}]]
	set iiipA [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iA.iAB.iABC.p}]]
	set iccpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.cAB.cABC.p}]]
	set icipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.cAB.iABC.p}]]
	set iicpD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.iAB.cABC.p}]]
	set iiipD [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute {iD.iAB.iABC.p}]]

	list \
	[$cccpA getExpression] \
	[$ccipA getExpression] \
	[$cicpA getExpression] \
	[$ciipA getExpression] \
	[$cccpD getExpression] \
	[$ccipD getExpression] \
	[$cicpD getExpression] \
	[$ciipD getExpression] \
	[$iccpA getExpression] \
	[$icipA getExpression] \
	[$iicpA getExpression] \
	[$iiipA getExpression] \
	[$iccpD getExpression] \
	[$icipD getExpression] \
	[$iicpD getExpression] \
	[$iiipD getExpression] \
} {4 9 6 6 8 8 7 7 4 9 12 11 10 10 10 10}

test MoMLChangeRequest-12.4.1 {check derivedLevel values} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	list \
	[[$toplevel getAttribute {cA.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cA.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {cD.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iA.iAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.cAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.cAB.iABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.iAB.cABC.p}] getDerivedLevel] \
	[[$toplevel getAttribute {iD.iAB.iABC.p}] getDerivedLevel] \
} {2147483647 1 2 1 3 1 2 1 3 1 2 1 3 1 2 1}

test MoMLChangeRequest-12.4.2 {test parent relationships} {
	set toplevel [java::cast ptolemy.kernel.CompositeEntity $toplevel]
	set iABC1 [$toplevel getEntity {cA.cAB.iABC}]
	set iABC2 [$toplevel getEntity {cA.iAB.iABC}]
	set iABC3 [$toplevel getEntity {cD.cAB.iABC}]
	set iABC4 [$toplevel getEntity {cD.iAB.iABC}]
	set iABC5 [$toplevel getEntity {iA.cAB.iABC}]
	set iABC6 [$toplevel getEntity {iA.iAB.iABC}]
	set iABC7 [$toplevel getEntity {iD.cAB.iABC}]
	set iABC8 [$toplevel getEntity {iD.iAB.iABC}]

	list \
	[[$iABC1 getParent] getFullName]\
	[[$iABC2 getParent] getFullName]\
	[[$iABC3 getParent] getFullName]\
	[[$iABC4 getParent] getFullName]\
	[[$iABC5 getParent] getFullName]\
	[[$iABC6 getParent] getFullName]\
	[[$iABC7 getParent] getFullName]\
	[[$iABC8 getParent] getFullName]
} {.top.cA.cAB.cABC .top.cA.iAB.cABC .top.cD.cAB.cABC .top.cD.iAB.cABC .top.iA.cAB.cABC .top.iA.iAB.cABC .top.iD.cAB.cABC .top.iD.iAB.cABC}

test MoMLChangeRequest-12.4.3 {test children relationships} {
	list \
	[listToFullNames [[$toplevel getEntity {cA.cAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {cA.iAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {cD.cAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {cD.iAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {iA.cAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {iA.iAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {iD.cAB.cABC}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {iD.iAB.cABC}] getChildren]] \
} {.top.cA.cAB.iABC .top.cA.iAB.iABC .top.cD.cAB.iABC .top.cD.iAB.iABC .top.iA.cAB.iABC .top.iA.iAB.iABC .top.iD.cAB.iABC .top.iD.iAB.iABC}

test MoMLChangeRequest-12.4.4 {test children relationships} {
	list \
	[listToFullNames [[$toplevel getEntity {cA.cAB}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {cD.cAB}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {iA.cAB}] getChildren]] \
	[listToFullNames [[$toplevel getEntity {iD.cAB}] getChildren]] \
} {{.top.cA.iABcopy .top.cA.iAB} {.top.cD.iABcopy .top.cD.iAB} {.top.iA.iABcopy .top.iA.iAB} {.top.iD.iABcopy .top.iD.iAB}}

######################################################################
####
# more Deep deferrals

set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="BaseClass" extends="ptolemy.actor.CompositeActor">
        <class name="InnerClass" extends="ptolemy.actor.CompositeActor">
            <property name="p" class="ptolemy.data.expr.Parameter" value="1"/>
        </class>
        <class name="SubclassOfInnerClass" extends="InnerClass">
            <property name="p" class="ptolemy.data.expr.Parameter" value="2"/>
        </class>
    </class>
    <class name="DerivedClass" extends="BaseClass">
    </class>
</entity>
}

set moml "$header $body"
test MoMLChangeRequest-13.0 {test construction of inner class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="BaseClass" extends="ptolemy.actor.CompositeActor">
        <class name="InnerClass" extends="ptolemy.actor.CompositeActor">
            <property name="p" class="ptolemy.data.expr.Parameter" value="1">
            </property>
        </class>
        <class name="SubclassOfInnerClass" extends="InnerClass">
            <property name="p" class="ptolemy.data.expr.Parameter" value="2">
            </property>
        </class>
    </class>
    <class name="DerivedClass" extends="BaseClass">
    </class>
</entity>
}

test MoMLChangeRequest-13.1 {test propagation of change from master class} {
    set toplevel [java::cast ptolemy.actor.CompositeActor $toplevel]
    set context [java::cast ptolemy.actor.CompositeActor [$toplevel getEntity "BaseClass.InnerClass"]]
    set change [java::new ptolemy.moml.MoMLChangeRequest $context $context {
        <property name="p" value="3"/>
    }]
    $context requestChange $change

	set p1 [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute "BaseClass.InnerClass.p"]]
	set p2 [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute "BaseClass.SubclassOfInnerClass.p"]]
	set p3 [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute "DerivedClass.InnerClass.p"]]
	set p4 [java::cast ptolemy.kernel.util.Settable [$toplevel getAttribute "DerivedClass.SubclassOfInnerClass.p"]]

	list \
	[$p1 getExpression] \
	[$p2 getExpression] \
	[$p3 getExpression] \
	[$p4 getExpression] \
} {3 2 3 2}


######################################################################
####
# Test exportMoML from a derived instance.

set body {
<entity name="cls2" class="ptolemy.actor.TypedCompositeActor">
    <class name="C0" extends="ptolemy.actor.TypedCompositeActor">
        <entity name="I0" class="ptolemy.actor.TypedCompositeActor">
            <property name="p" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </entity>
    </class>
    <class name="C1" extends="ptolemy.actor.TypedCompositeActor">
        <entity name="IC0" class="C0">
            <entity name="I0" class="ptolemy.actor.TypedCompositeActor">
                <property name="p" class="ptolemy.data.expr.Parameter" value="10">
                </property>
            </entity>
        </entity>
    </class>
    <entity name="IC1" class="C1">
    </entity>
</entity>
}

set moml "$header $body"
test MoMLChangeRequest-14.0 {test export moml from a derived instance} {
    $parser reset
    set toplevel [$parser parse $moml]
    set top [java::cast ptolemy.kernel.CompositeEntity $toplevel]
    set p0 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute "C0.I0.p"]]
    set p1 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute "C1.IC0.I0.p"]]
    set p2 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute "IC1.IC0.I0.p"]]
	set i1 [java::cast ptolemy.kernel.util.NamedObj [$top getEntity "IC1.IC0"]]
    list \
    [[$p0 getToken] toString] \
    [[$p1 getToken] toString] \
    [[$p2 getToken] toString] \
    [$i1 exportMoML]
} {0 10 10 {<entity name="IC0" class="C0">
    <entity name="I0" class="ptolemy.actor.TypedCompositeActor">
        <property name="p" class="ptolemy.data.expr.Parameter" value="10">
        </property>
    </entity>
</entity>
}}

set body {
<entity name="cls3" class="ptolemy.actor.TypedCompositeActor">
    <class name="C0" extends="ptolemy.actor.TypedCompositeActor">
        <property name="p" class="ptolemy.data.expr.Parameter" value="0">
        </property>
    </class>
    <class name="C1" extends="ptolemy.actor.TypedCompositeActor">
        <class name="SC0" extends="C0">
        </class>
        <entity name="A0" class="ptolemy.actor.TypedCompositeActor">
            <entity name="ISC0" class="SC0">
                <property name="p" class="ptolemy.data.expr.Parameter" value="10">
                </property>
            </entity>
        </entity>
    </class>
    <class name="SC1" extends="C1">
        <class name="SC0" extends="C0">
            <property name="p" class="ptolemy.data.expr.Parameter" value="20">
            </property>
        </class>
    </class>
</entity>
}

set moml "$header $body"
test MoMLChangeRequest-14.1 {test export moml from a derived instance} {
    $parser reset
    set toplevel [$parser parse $moml]
    set top [java::cast ptolemy.kernel.CompositeEntity $toplevel]
    set p0 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute "C1.A0.ISC0.p"]]
    set p1 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute "SC1.SC0.p"]]
    set p2 [java::cast ptolemy.data.expr.Parameter [$toplevel getAttribute "SC1.A0.ISC0.p"]]
	set i1 [java::cast ptolemy.kernel.util.NamedObj [$top getEntity "C1.A0.ISC0"]]
	set i2 [java::cast ptolemy.kernel.util.NamedObj [$top getEntity "SC1.A0.ISC0"]]
    list \
    [[$p0 getToken] toString] \
    [[$p1 getToken] toString] \
    [[$p2 getToken] toString] \
    [$i1 exportMoML] \
    [$i2 exportMoML]
} {10 20 20 {<entity name="ISC0" class="SC0">
    <property name="p" class="ptolemy.data.expr.Parameter" value="10">
    </property>
</entity>
} {<entity name="ISC0" class="SC0">
</entity>
}}

