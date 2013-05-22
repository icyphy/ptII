# Test the HLA facility
#
# @Author: Christopher Brooks, based on Clock.tcl by Edward A. Lee
#
# @Version: $Id: Sinewave.tcl 57040 2010-01-27 20:52:32Z cxh $
#
# @Copyright (c) 2013 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Run an .xml file return the toplevel.
proc runModel {modelFileName} {
    set workspace [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File $modelFileName] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]

    set manager [java::new ptolemy.actor.Manager $workspace "myManager"]
    $model setManager $manager 
    $manager execute
    return $model
}
######################################################################
#### Run two HLA models
#
test HLATest-1.0 {Run the HLA Producer Consumer} {
    # Success is when the Test actor in the consumer gets all of its values.
    set producer [runModel auto/HLAProducer.xml]
    set consumer [runModel auto/HLAConsumer.xml]
    # Return something useful as another check
    list [$producer getFullName] [$consumer getFullName]
} {.HLAProducer .HLAConsumer}

######################################################################
#### Run two more HLA models
#
test HLATest-2.0 {Run the HLA Producer Consumer} {
    # Success is when the Test actor in the consumer gets all of its values.
    set producer [runModel auto/HLAProducer.xml]
    set consumer [runModel auto/HLAConsumer.xml]
    # Return something useful as another check
    list [$producer getFullName] [$consumer getFullName]
} {.HLAProducer .HLAConsumer}



