# Tests for the PtolemyModelFactory class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
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
test PtolemyModelFactory-2.1 {Constructor tests} {
    set parser [java::new ptolemy.schematic.xml.PTMLParser]
    set fileend [file join $PTII ptolemy schematic lib rootIconLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set iconroot [java::call ptolemy.schematic.util.PTMLObjectFactory createIconLibrary $xmllib]

    set fileend [file join $PTII ptolemy schematic lib rootEntityLibrary.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set entityroot [java::call ptolemy.schematic.util.PTMLObjectFactory \
	    createEntityLibrary $xmllib $iconroot]

    set fileend [file join $PTII ptolemy schematic editor testschematic.ptml]
    set filename "file:"
    append filename $fileend
    set xmllib [$parser parse $filename]
    set schematic [java::call ptolemy.schematic.util.PTMLObjectFactory createSchematic $xmllib $entityroot]

    set modelfactory [java::new ptolemy.schematic.util.PtolemyModelFactory]
    set model [$modelfactory createPtolemyModel $schematic]
    
    $model description
} {ptolemy.actor.TypedCompositeActor {.hello world} attributes {
    {ptolemy.data.expr.Parameter {.hello world.domain} ptolemy.data.StringToken(SDF)}
    {ptolemy.data.expr.Parameter {.hello world.starttime} ptolemy.data.DoubleToken(1.0)}
    {ptolemy.data.expr.Parameter {.hello world.endtime} ptolemy.data.DoubleToken(7.0)}
} ports {
} entities {
    {ptolemy.actor.lib.Ramp {.hello world.Ramp1} attributes {
        {ptolemy.data.expr.Parameter {.hello world.Ramp1.firingCountLimit} ptolemy.data.IntToken(0)}
        {ptolemy.data.expr.Parameter {.hello world.Ramp1.init} ptolemy.data.IntToken(0)}
        {ptolemy.data.expr.Parameter {.hello world.Ramp1.step} ptolemy.data.IntToken(4)}
    } ports {
        {ptolemy.actor.TypedIOPort {.hello world.Ramp1.output} attributes {
        } links {
            {ptolemy.actor.TypedIORelation {.hello world.R1} attributes {
            } configuration {width 1 fixed}}
        } insidelinks {
        } configuration {output opaque {width 1}} receivers {
        } remotereceivers {
            {
            }
        } type {declared null resolved null}}
        {ptolemy.actor.TypedIOPort {.hello world.Ramp1.trigger} attributes {
        } links {
        } insidelinks {
        } configuration {input multiport opaque {width 0}} receivers {
        } remotereceivers {
        } type {declared ptolemy.data.Token resolved ptolemy.data.Token}}
    }}
    {ptolemy.actor.lib.Recorder {.hello world.Recorder1} attributes {
    } ports {
        {ptolemy.actor.TypedIOPort {.hello world.Recorder1.input} attributes {
        } links {
            {ptolemy.actor.TypedIORelation {.hello world.R1} attributes {
            } configuration {width 1 fixed}}
        } insidelinks {
        } configuration {input multiport opaque {width 1}} receivers {
            {
            }
        } remotereceivers {
        } type {declared ptolemy.data.StringToken resolved ptolemy.data.StringToken}}
    }}
} relations {
    {ptolemy.actor.TypedIORelation {.hello world.R1} attributes {
    } links {
        {ptolemy.actor.TypedIOPort {.hello world.Recorder1.input} attributes {
        } configuration {input multiport opaque {width 1}} receivers {
            {
            }
        } remotereceivers {
        } type {declared ptolemy.data.StringToken resolved ptolemy.data.StringToken}}
        {ptolemy.actor.TypedIOPort {.hello world.Ramp1.output} attributes {
        } configuration {output opaque {width 1}} receivers {
        } remotereceivers {
            {
            }
        } type {declared null resolved null}}
    } configuration {width 1 fixed}}
} director {
    {ptolemy.domains.sdf.kernel.SDFDirector {.hello world.director} attributes {
        {ptolemy.data.expr.Parameter {.hello world.director.iterations} ptolemy.data.IntToken(3)}
    }}
} executivedirector {
}}

test PtolemyModelFactory-2.2 {Constructor tests} {
    set manager [$model getManager]
    $manager run
    set recorder [java::cast ptolemy.actor.lib.Recorder [$model getEntity Recorder1]]
    enumToTokenValues [$recorder getRecord 0]
} {0 4 8}