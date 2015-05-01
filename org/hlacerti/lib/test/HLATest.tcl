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

# We keep a list of listeners so that they don't get garbaged collected and then the logging messages do not appear.
# This is because Manager keeps a list that is a WeakReference to the listeners.
set listeners [java::new java.util.LinkedList]

# Run an .xml file return the toplevel.
proc runModel {modelFileName outputStream} {
    global listeners
    set workspace [java::new ptolemy.kernel.util.Workspace "myWorkspace$modelFileName"]
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
    $listeners add $listener
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
        $consumerListenerOutput write [[java::new String "consumer start\n"] getBytes]
        set consumer [runModel MultiDataTypesConsumer.xml $consumerListenerOutput]
        sleep 2 false

        set producerListenerOutput [java::new java.io.ByteArrayOutputStream]
        $producerListenerOutput write [[java::new String "producer start\n"] getBytes]
        set producer [runModel MultiDataTypesProducer.xml $producerListenerOutput]
        sleep 5 false

        # Return something useful as another check
        $consumerListenerOutput write [[java::new String "consumer end\n"] getBytes]
        $producerListenerOutput write [[java::new String "producer end\n"] getBytes]

        $consumerListenerOutput flush
        $producerListenerOutput flush

        list [$consumer getFullName] [$producer getFullName] \
            [$consumerListenerOutput toString] [$producerListenerOutput toString]
    } {.MultiDataTypesConsumer .MultiDataTypesProducer {consumer start
preinitializing
infering widths
preinitializing
resolving types
initializing
infering widths
initializing
resolving types
executing number 1
wrapping up
idle
Completed execution with 5 iterations
consumer end
} {producer start
preinitializing
resolving types
initializing
executing number 1
wrapping up
idle
Completed execution with 6 iterations
producer end
}}
}

# # Let the listeners be gc'd.
# set listeners [java::null]

