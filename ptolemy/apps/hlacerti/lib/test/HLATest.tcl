# Test the HLA facility
#
# @Author: Christopher Brooks, based on Clock.tcl by Edward A. Lee
#
# @Version: $Id$
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
proc runModel {modelFileName outputStream} {
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
    set listener [java::new ptolemy.actor.StreamExecutionListener $outputStream]
    $manager addExecutionListener $listener
    $model setManager $manager 

    # Run the model in a new thread.
    $manager startRun
    return $model
}

puts "HLATest.tcl: Be sure to source certi/share/scripts/myCERTI_env.sh"

if {[java::call System getenv CERTI_HOME] == ""} {
    test HLATest-1.0-CERTI_HOME-NotSet {Skipping running HLA tests because CERTI_HOME is not set} {
        error {Skipping running HLA tests because CERTI_HOME is not set}
    } {} {Skipping running HLA tests because CERTI_HOME is not set}
} else {

    ######################################################################
    #### Run two HLA models
    #
    test HLATest-1.0 {Run the HLA MultiDataTypes} {

        # Success is when the Test actor in the consumer gets all of its values.
        set consumerListenerOutput [java::new java.io.ByteArrayOutputStream]
        set consumer [runModel MultiDataTypesConsumer.xml $consumerListenerOutput]
        sleep 1 false
        set producerListenerOutput [java::new java.io.ByteArrayOutputStream]
        set producer [runModel MultiDataTypesProducer.xml $producerListenerOutput]
        # Return something useful as another check
        sleep 5 false
        list [$consumer getFullName] [$producer getFullName] \
            [$consumerListenerOutput toString] [$producerListenerOutput toString]
    } {.MultiDataTypesConsumer .MultiDataTypesProducer {preinitializing
resolving types
initializing
} {preinitializing
resolving types
initializing
executing number 1
}}

}

######################################################################
#### Run two HLA models
#
# test HLATest-1.0 {Run the HLA MultiDataTypes} {

#     # Success is when the Test actor in the consumer gets all of its values.
#     # The models are not in the auto/ directory because we want to run
#     # them in sequence.
#     set cmdArgs [java::new {java.lang.String[]} 4 \
# 		     {{ptolemy/configs/full/configuration.xml} \
# 			  {-runThenExit} \
# 			  {MultiDataTypesConsumer.xml}
# 			 {MultiDataTypesProducer.xml}}] 

#     # ConfigurationApplication calls ptolemy.util.StringUtilities.exit(), which
#     # Check to see if the ptolemy.ptII.doNotExit property is set.
#     java::call System setProperty ptolemy.ptII.doNotExit true

#     # Run the model
#     set application [java::new ptolemy.actor.gui.ConfigurationApplication $cmdArgs]

#     puts "sleeping for 5 seconds"
#     # false means: Don't print dots
#     sleep 5 false

#     # Get some information just to be sure that we have parsed the models.
#     set models [$application models]
#     list [[$models get 0] toString] \
# 	[[$models get 1] toString] \
# 	[[$models get 2] toString] \
# } {{ptolemy.actor.gui.Configuration {.configuration}} {ptolemy.actor.TypedCompositeActor {.MultiDataTypesConsumer}} {ptolemy.actor.TypedCompositeActor {.MultiDataTypesProducer}}}
