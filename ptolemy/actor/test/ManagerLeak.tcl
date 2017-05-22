# Used for leak detection: Create a Manager, run a model.
#
# @Author: Yuhong Xiong, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2014 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test Manager-10.1 {Test execution listener with one arg} {
    set e0 [sdfModel 2]
    set manager [$e0 getManager]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    jdkCapture {
	set listener [java::new ptolemy.actor.StreamExecutionListener \
		[java::field System out]]

	$manager addExecutionListener $listener
	$manager run
	set exception [java::new ptolemy.actor.NoRoomException \
		"This exception is testing the execution listener"]
	# While we are here, test StreamExecutionListener.executionError()
	$listener executionError $manager $exception
	$manager removeExecutionListener $listener
    } stdoutResults

    # Strip out the time in ms, which will vary between runs  
    regsub {[0-9]* ms.*$} $stdoutResults "xxx ms" stdoutResultsWithoutTime

    # Strip out the stack frames
    regsub -all {	at .*$\n} $stdoutResultsWithoutTime "" \
	    stdoutResultsWithoutStackTrace	   
    #puts "------- result: [enumToTokenValues [$rec getRecord 0]]"
    #9/02 - 'processing mutations' moved from line 4 to line 2
    list $stdoutResultsWithoutStackTrace
} {{preinitializing
resolving types
initializing
executing number 1
wrapping up
idle
xxx ms
Completed execution with 2 iterations
Execution error.
ptolemy.actor.NoRoomException: This exception is testing the execution listener
}}

######################################################################
####
#
test Manager-11.0 {Test execution by execute method} {
    $manager execute
    enumToTokenValues [$rec getRecord 0]
} {0 1}
