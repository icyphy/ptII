# Test ColtPoisson
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007-2008 The Regents of the University of California.
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

######################################################################
####
#
test ColtPoisson-1.1 {Read in a model} {
    set workspace [java::new ptolemy.kernel.util.Workspace "ColtPoissonClassWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "ColtPoissonClass.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set manager [java::new ptolemy.actor.Manager $workspace "subaggManager"]
    #$model setManager $manager 
    #$manager execute
    set actor1 [$model getEntity ColtPoisson]
    set seed1 [getParameter $actor1 seed]

    set instance1 [java::cast ptolemy.kernel.CompositeEntity [$model getEntity InstanceOfCompositeClassDefinition]]
    set instanceActor1 [$instance1 getEntity ColtPoisson]
    set instanceSeed1 [getParameter $instanceActor1 seed]

    set composite1 [java::cast ptolemy.kernel.CompositeEntity [$model getEntity CompositeActor]]
    set compositeActor1 [$composite1 getEntity ColtPoisson]
    set compositeSeed1 [getParameter $compositeActor1 seed]

    list \
	[$seed1 getExpression] \
	[$instanceSeed1 getExpression] \
	[$compositeSeed1 getExpression]
} {100L 100L 100L}

test ColtPoisson-1.2 {Change the seed parameter of one actor.  All actors should change.} {
    # Uses 1.1 above
    $seed1 setExpression 0L
    list \
	[$seed1 getExpression] \
	[$instanceSeed1 getExpression] \
	[$compositeSeed1 getExpression]
} {0L 0L 0L}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
