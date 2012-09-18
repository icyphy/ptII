# Tests for the StreamExec class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2009 The Regents of the University of California.
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

if {[string compare jdkCaptureErr [info procs jdkCaptureErr]] == 1} then { 
    source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test StreamExec-1.1 {Create a StreamExec, call set and get} {
    set streamExec [java::new ptolemy.util.StreamExec]
    $streamExec cancel
    $streamExec clear

    # updateStatusBar does nothing
    $streamExec updateStatusBar foo

    list [$streamExec getLastSubprocessReturnCode]
} {0}

######################################################################
####
#
test StreamExec-2.1 {call execute a few commands, get stdout} {
    set streamExec [java::new ptolemy.util.StreamExec]
    set commands [java::new java.util.LinkedList]
    $commands add "sleep 1"
    $commands add "sleep 2"
    $streamExec setCommands $commands
    jdkCapture {    
	$streamExec start
    } stdout
    list $stdout [$streamExec getLastSubprocessReturnCode]	
} {{About to execute:
    sleep 1
About to execute:
    sleep 2
All Done
} 0}

######################################################################
####
#
test StreamExec-2.3 {execute a command that does not exist, get stderr} {
    set streamExec [java::new ptolemy.util.StreamExec]
    set commands [java::new java.util.LinkedList]
    $commands add "NotACommand"
    $streamExec setCommands $commands
    jdkCapture {    
	jdkCaptureErr {
	    $streamExec start
        } stderr
    } stdout
    set result1 \
	{IOException: java.io.IOException: NotACommand: not found
}
    set result2 \
	{IOException: java.io.IOException: CreateProcess: NotACommand error=2
}
    set result3 \
	{IOException: java.io.IOException: Cannot run program "NotACommand": java.io.IOException: error=2, No such file or directory
}
    set result4 \
	{IOException: java.io.IOException: java.io.IOException: NotACommand: not found
}
    set result5 \
	{IOException: java.io.IOException: Cannot run program "NotACommand": CreateProcess error=2, The system cannot find the file specified
}

    set result6 \
	{IOException: java.io.IOException: Cannot run program "NotACommand": error=2, No such file or directory
}
    set retval 0

    if {"$stderr" == "$result1" || "$stderr" == "$result2" || "$stderr" == "$result3" || $stderr == "$result4" || $stderr == "$result5" || "$stderr" == "$result6" } {
	set retval 1
    } else {
	puts "Did not match any of the known good results:\n----"
	puts $stderr
	puts "----"
    }

    # FIXME: should the return code be non-zero?
    list $stdout $retval [$streamExec getLastSubprocessReturnCode]
} {{About to execute:
    NotACommand
All Done
} 1 0}

######################################################################
####
#
test StreamExec-2.3a {execute a command that does not exist, get stderr} {
    set streamExec [java::new ptolemy.util.StreamExec]
    set commands [java::new java.util.LinkedList]
    $commands add "grep fofofofofofo makefile"
    $streamExec setCommands $commands
    jdkCapture {    
	jdkCaptureErr {
	    $streamExec start
        } stderr
    } stdout
    # Here, the return code is 1 because grep returns 1
    list $stdout $stderr [$streamExec getLastSubprocessReturnCode]
} {{About to execute:
    grep fofofofofofo makefile
All Done
} {} 1}


######################################################################
####
#
test StreamExec-2.4 {Run the same commands twice} {
    set streamExec [java::new ptolemy.util.StreamExec]
    set commands [java::new java.util.LinkedList]
    $commands add "echo \"foo bar\""
    $commands add "sleep 1"
    $streamExec setCommands $commands

    jdkCapture {    
	jdkCaptureErr {
	    $streamExec start
	    $streamExec start
        } stderr
    } stdout

    # Success is not throwing an error	
    list 1
} {1}



######################################################################
####
#
test StreamExec-2.5 {Run commands in another thread and call cancel} {
    set commands [java::new java.util.LinkedList]
    $commands add "echo AtTop"
    $commands add "sleep 10"
    # We will never see this because we call cancel
    $commands add "echo After10"

    set threadStreamExec [java::new ptolemy.util.test.ThreadStreamExec \
	ThreadStreamExec-2.5 $commands]

    set streamExec [java::field $threadStreamExec streamExec]

    jdkCapture {    
	jdkCaptureErr {
	    $threadStreamExec start
        } stderr
    } stdout
    # Second arg of 0 means don't print dots
    sleep 2 0
    $streamExec cancel


    # Success is not throwing an error	
    list 1
} {1}

######################################################################
####
#
test StreamExec-3.0 {Test for pattern matching} {
    set commands [java::new java.util.LinkedList]
    $commands add "echo first"
    $commands add "echo second"
    $commands add "echo Failed: 1 of 2"
    $commands add "make notATarget"

    set streamExec [java::new ptolemy.util.StreamExec]
    $streamExec setCommands $commands
    $streamExec setPattern {^sec.*|.*\*\*\*.*|^Failed: [1-9].*}
    jdkCapture {    
	jdkCaptureErr {
	    $streamExec start
	    # Sleep so that we are sure to get stderr and stdout.
	    # Sleep is defined in $PTII/util/testsuite/testDefs.tcl
	    # Sleep for 2 seconds, don't print dots.
	    sleep 2 0
        } stderr
    } stdout
    # puts "StreamExec-3.0: stdout: $stdout"
    # puts "StreamExec-3.0: stderr: $stderr"
    set result [$streamExec getPatternLog]

    regsub -all {make\[.*]} $result {make} result2
    regsub -all {.  Stop.} $result2 {.} result3
    list $result3
} {{second
Failed: 1 of 2
make: *** No rule to make target `notATarget'.
}}
