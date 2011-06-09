# Test StringCompare
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
test StringCompare-1.1 {test PortParameter memory leak} {
    set workspace [java::new ptolemy.kernel.util.Workspace "pubWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "auto/StringCompare.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set director [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$model getDirector]]
    set scheduler [java::new ptolemy.actor.lib.string.test.SDFTestScheduler \
	$director "MySDFTestScheduler"]
    $director setScheduler $scheduler 
    #set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]
    set manager [java::new ptolemy.actor.Manager $workspace "MyManager"]
    $model setManager $manager 
    $manager execute
    set rateVariables [$scheduler getRateVariables]
    set r1 [listToNames $rateVariables]
    $manager execute
    set rateVariables [$scheduler getRateVariables]
    set r2 [listToNames $rateVariables]
    list $r1 $r2
} {{tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate} {tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate tokenConsumptionRate}}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
