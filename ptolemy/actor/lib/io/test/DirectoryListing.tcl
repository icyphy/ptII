# Test DirectoryListing
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

file mkdir emptyDirectory

######################################################################
####
#
test DirectoryListing-1.1 {Test for empty directory} {
    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "DirectoryListingEmptyDirectory.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    $model setManager $manager 
    $manager execute
} {}

file delete -force emptyDir

######################################################################
####
#
test DirectoryListing-1.1 {Test for empty directory} {
    set directoryListing [java::cast ptolemy.actor.lib.io.DirectoryListing [$model getEntity DirectoryListing]]
    set allowEmptyDirectory [java::cast ptolemy.data.expr.Parameter [$directoryListing getAttribute allowEmptyDirectory]]
    $allowEmptyDirectory setExpression "false"
    $directoryListing attributeChanged $allowEmptyDirectory

    set pattern [java::cast ptolemy.data.expr.Parameter [$directoryListing getAttribute pattern]]
    $pattern setExpression ""
    $directoryListing attributeChanged $pattern

    catch {$manager execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: No files or directories that match the pattern.
  in .DirectoryListingEmptyDirectory.DirectoryListing}}