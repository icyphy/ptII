# Tests for the MessageHandler class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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
test MessageHandler-1.1 {Create a MessageHandler, call set and get} {
    set handler [java::new ptolemy.util.MessageHandler]
    java::call ptolemy.util.MessageHandler setMessageHandler $handler
    set handler2 [java::call ptolemy.util.MessageHandler getMessageHandler] 
    $handler equals $handler2
} {1}

######################################################################
####
#
test MessageHandler-2.1 {error(String) } {
    jdkCaptureErr {
	java::call ptolemy.util.MessageHandler error "This is an error"
    } results
    list $results
} {{This is an error
}}

######################################################################
####
#
test MessageHandler-2.1 {error(String, Throwable) } {
    jdkCaptureErr {
	set throwable [java::new Throwable "A throwable"]
	java::call ptolemy.util.MessageHandler \
		error "This is another error" $throwable
    } results
    # Truncate to get rid of platform dependent stack trace.
    list [string range $results 0 53]
} {{This is another error
java.lang.Throwable: A throwable}}

######################################################################
####
#
test MessageHandler-3.1 {shortDescription} {
    set throwable [java::new Throwable "A throwable"]
    set error [java::new Error "An error"]
    set exception [java::new Exception "An exception"]

    list \
	    [java::call ptolemy.util.MessageHandler \
	    shortDescription $throwable] \
	    [java::call ptolemy.util.MessageHandler \
	    shortDescription $error] \
	    [java::call ptolemy.util.MessageHandler \
	    shortDescription $exception]
} {Throwable Error Exception}

######################################################################
####
#
test MessageHandler-4.1 {message(String) } {
    jdkCaptureErr {
	java::call ptolemy.util.MessageHandler message "This is a message"
    } results
    list $results
} {{This is a message
}}


######################################################################
####
#
test MessageHandler-5.1 {warning(String) } {
    jdkCaptureErr {
	java::call ptolemy.util.MessageHandler warning "This is a warning"
    } results
    list $results
} {{This is a warning
}}

######################################################################
####
#
test MessageHandler-6.1 {warning(String, Throwable) } {
    jdkCaptureErr {
	set throwable [java::new Throwable "Another throwable"]
	java::call ptolemy.util.MessageHandler \
		warning "This is another warning" $throwable
    } results
    # Truncate to get rid of platform dependent stack trace.
    list [string range $results 0 80]
} {{This is another warning: Another throwable
java.lang.Throwable: Another throwable}}

######################################################################
####
test MessageHandler-7.1 {yesNoQuestion(String), answer is yes} {
    set stdin [java::field System in]
    # set the byteArray to "yes" 
    set byteArray [java::new {byte[]} {3} {121 101 115}]
    set stream [java::new java.io.ByteArrayInputStream $byteArray]
    java::call System setIn $stream
    jdkCapture {
 	set answer [java::call ptolemy.util.MessageHandler \
 		yesNoQuestion "Is this test working?"]
    } results
    java::call System setIn $stdin
    list $answer $results
} {1 {Is this test working? (yes or no) }}

######################################################################
####
test MessageHandler-7.2 {yesNoQuestion(String), answer is yes} {
    set stdin [java::field System in]
    # set the byteArray to "no" 
    set byteArray [java::new {byte[]} {2} {110 111}]
    set stream [java::new java.io.ByteArrayInputStream $byteArray]
    java::call System setIn $stream
    jdkCapture {
 	set answer [java::call ptolemy.util.MessageHandler \
 		yesNoQuestion "Is this other test working?"]
    } results
    java::call System setIn $stdin
    list $answer $results
} {0 {Is this other test working? (yes or no) }}
