# Tests for the FilterOutGraphicalClasses class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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
test FilterOutGraphicalClasses-1.1 {filterAttributeValue} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    set toplevel [$parser parseFile "./FilterOutGraphicalClasses.xml"]
    set newMoML [$toplevel exportMoML]
    set tmpFile "newMoML.xml"
    set fd [open $tmpFile "w"]
    puts $fd $newMoML
    close $fd
    # Use catch in case diff cannot be found
    catch {exec diff ./FilterOutGraphicalClasses.xml $tmpFile} results

    file delete -force $tmpFile
    list $results
} {{26c26
<         <property name="_controllerFactory" class="ptolemy.vergil.basic.NodeControllerFactory">
---
>         <property name="_controllerFactory" class="ptolemy.kernel.util.Attribute">
47c47
<         <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
---
>         <property name="_icon" class="ptolemy.kernel.util.Attribute">
80c80
<         <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
---
>         <property name="_icon" class="ptolemy.kernel.util.Attribute">
115a116
> }}
