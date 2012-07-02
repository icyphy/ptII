# Tests for the KeplerDocumentationAttribute class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[string compare test [info procs test]] == 1} then {
    source [file join $PTII util testsuite testDefs.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test KeplerDocumentationAttribute-1.1 {Create KeplerDocumentationAttributes} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "N1"] 
    set n2 [java::new ptolemy.kernel.util.NamedObj "N2"] 
    set sa3 [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set sa4 [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute $n1 "foo"] 
    set sa5 [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute [$n2 workspace]]
    list [$sa3 toString] [$sa4 toString] [$sa5 toString]
} {{ptolemy.vergil.basic.KeplerDocumentationAttribute {.}} {ptolemy.vergil.basic.KeplerDocumentationAttribute {.N1.foo}} {ptolemy.vergil.basic.KeplerDocumentationAttribute {.}}}

test KeplerDocumentationAttribute-2.1 {exportMoML} {
    set kda [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set stringWriter [java::new java.io.StringWriter]
    $kda exportMoML $stringWriter 2 myKDA
    set result1 [$stringWriter toString]
} {<property name="myKDA" class="ptolemy.vergil.basic.KeplerDocumentationAttribute">
<property name="description" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="author" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="version" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="userLevelDocumentation" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
</property>}


test KeplerDocumentationAttribute-3.1 {setUserLevelDocumentation} {
    set kda [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set stringWriter [java::new java.io.StringWriter]
    # Test for bogus html
    $kda setUserLevelDocumentation {<p><hello>The StringConstant actor outputs a string specified via the actor's value parameter.</p>}
    $kda exportMoML $stringWriter 2 myKDA
    set exported "<entity name=\"model3\" class=\"ptolemy.actor.TypedCompositeActor\">\n [$stringWriter toString]\n</entity>"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $exported]
    list [$toplevel exportMoML myMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="myMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
<property name="myKDA" class="ptolemy.vergil.basic.KeplerDocumentationAttribute">
<property name="description" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="author" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="version" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="userLevelDocumentation" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>&lt;p&gt;&lt;hello&gt;The StringConstant actor outputs a string specified via the actor's value parameter.&lt;/p&gt;</configure></property>
</property></entity>
}}

test KeplerDocumentatonAttribute-4.1 {addPort} {
    set kda [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set stringWriter [java::new java.io.StringWriter]
    $kda addPort portname portvalue
    $kda exportMoML $stringWriter 2 myKDA
    set result1 [$stringWriter toString]
} {<property name="myKDA" class="ptolemy.vergil.basic.KeplerDocumentationAttribute">
<property name="description" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="author" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="version" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="userLevelDocumentation" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="port:portname" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>portvalue</configure></property>
</property>}

test KeplerDocumentatonAttribute-4.2 {addPort} {
    set kda [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set stringWriter [java::new java.io.StringWriter]
    $kda addPort portname p&o<rt>value
    $kda exportMoML $stringWriter 2 myKDA
    set result1 [$stringWriter toString]
} {<property name="myKDA" class="ptolemy.vergil.basic.KeplerDocumentationAttribute">
<property name="description" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="author" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="version" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="userLevelDocumentation" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="port:portname" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>p&amp;o&lt;rt&gt;value</configure></property>
</property>}

test KeplerDocumentatonAttribute-5.1 {addProperty} {
    set kda [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set stringWriter [java::new java.io.StringWriter]
    $kda addProperty propname propvalue
    $kda exportMoML $stringWriter 2 myKDA
    set result1 [$stringWriter toString]
} {<property name="myKDA" class="ptolemy.vergil.basic.KeplerDocumentationAttribute">
<property name="description" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="author" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="version" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="userLevelDocumentation" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="prop:propname" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>propvalue</configure></property>
</property>}

test KeplerDocumentatonAttribute-5.2 {addProperty} {
    set kda [java::new ptolemy.vergil.basic.KeplerDocumentationAttribute]
    set stringWriter [java::new java.io.StringWriter]
    $kda addProperty propname p&ro<pv>alue
    $kda exportMoML $stringWriter 2 myKDA
    set result1 [$stringWriter toString]
} {<property name="myKDA" class="ptolemy.vergil.basic.KeplerDocumentationAttribute">
<property name="description" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="author" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="version" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="userLevelDocumentation" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>null</configure></property>
<property name="prop:propname" class="ptolemy.kernel.util.ConfigurableAttribute"><configure>p&amp;ro&lt;pv&gt;alue</configure></property>
</property>}
