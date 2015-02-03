# Tests for the StreamErrorHandler class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2009 The Regents of the University of California.
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

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set classheader {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="b" class="ptolemy.moml.test.notAClass"/>
<property name="xxx"/>
</entity>
}

set moml "$header $body"

test StreamErrorHandler-1.1 {Create a stream to stderr} {

    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set streamErrorHandler [java::new ptolemy.moml.StreamErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $streamErrorHandler
    
    # We don't actually exercise the default StreamErrorHandler
    # because it would end up clutter the output with error strings

    list [expr {$streamErrorHandler == [java::cast ptolemy.moml.StreamErrorHandler [java::call ptolemy.moml.MoMLParser getErrorHandler]]}]
    
} {1}

test StreamErrorHandler-1.2 {Create a stream that we can read} {
    # Create a stream to dump to
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set streamErrorHandler [java::new ptolemy.moml.StreamErrorHandler \
	    $printStream]

    # This method does nothing, but we call it anyway.
    $streamErrorHandler enableErrorSkipping true

    java::call ptolemy.moml.MoMLParser setErrorHandler $streamErrorHandler

    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]

    $printStream flush

    # This hack is necessary because of problems with crnl under windows
    regsub -all {\\} \
	[[[java::new java.io.File $PTII] getCanonicalFile] toString] \
	{/} ptII
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    regsub -all {\\} $output {/} output2

    regsub -all $ptII \
		$output2 {$PTII} output3

    # Windows and Solaris have different error messages
    regsub -all "No such file or directory" $output3 \
	        "The system cannot find the path specified" output4

    # so we truncate the message.

    # The IOException differs between Windows and Solaris,
    # so we truncate the message.

    list [string range $output4 0 368] {...}

} {{Error encountered in:
<entity name="b" class="ptolemy.moml.test.notAClass">
ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.notAClass. In Ptolemy, classes are typically Java .class files. Entities like actors may instead be defined within a .xml file.  In any case, the class was not found. If the class uses a third party package, then} ...}


