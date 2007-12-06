# Tests for the KernelException class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2007 The Regents of the University of California.
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

if {[string compare test [info procs test]] == 1} then {
    source [file join $PTII util testsuite testDefs.tcl]
} {}

if {[string compare test [info procs jdkCaptureErr]] == 1} then {
    source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
#
test KernelException-2.1 {Create a KernelException} {
    set pe [java::new ptolemy.kernel.util.KernelException]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{} {}}

######################################################################
####
#
test KernelException-7.1 {Create a KernelException with an unnamed NamedObj \
	and an unnamed NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .<Unnamed Object> and .<Unnamed Object>}}

######################################################################
####
#
test KernelException-7.2 {Create a KernelException with a named NamedObj \
	and an unnamed NamedObj and a detail Message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .NamedObj 1 and .<Unnamed Object>}}

######################################################################
####
#
test KernelException-7.3 {Create a KernelException with an unnamed NamedObj \
	and a named NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 "Detail Message"]
    list [$pe getLocalizedMessage]
} {{Detail Message
  in .<Unnamed Object> and .NamedObj 2}}

######################################################################
####
#
test KernelException-7.4 {Create a KernelException with a named NamedObj \
	and a named NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .NamedObj 1 and .NamedObj 2}}

######################################################################
####
#
test KernelException-7.5 {Create a KernelException with a unnamed NamedObj \
	and an unnamed NamedObj and a null message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 [java::null]]
    list [$pe getMessage]
} {{  in .<Unnamed Object> and .<Unnamed Object>}}

######################################################################
####
#
test KernelException-7.6 {Create a KernelException with a null NamedObj \
	and an unnamed NamedObj and a null message} {
    set n1 [java::null]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 [java::null]]
    list [$pe getMessage]
} {{  in .<Unnamed Object>}}


######################################################################
####
#
test KernelException-7.7 {Create a KernelException with an unnamed NamedObj \
	and a null NamedObj and a null message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::null]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 [java::null]]
    list [$pe getMessage]
} {{  in .<Unnamed Object>}}

######################################################################
####
#
test KernelException-7.8 {Create a KernelException with null NamedObj \
	and a null NamedObj and a null message} {
    set n1 [java::null]
    set n2 [java::null]
    set pe [java::new ptolemy.kernel.util.KernelException $n1 $n2 [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
#

test KernelException-7.9 {Create a KernelException with a named NamedObj \
	and a named NamedObj and a detail message and a cause} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set cause [java::new Exception "Cause Exception"]
    set pe [java::new ptolemy.kernel.util.KernelException \
	    $n1 $n2 $cause "Detail Message"]

    # Try out printStackTrace(PrintStream)
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    $pe printStackTrace $printStream
    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output

    list [$pe getMessage] [[$pe getCause] toString] "\n\n" \
	    [string range $output 0 108] \
	[[$pe getNameable1] getFullName] \
	[[$pe getNameable2] getFullName]
} {{Detail Message
  in .NamedObj 1 and .NamedObj 2
Because:
Cause Exception} {java.lang.Exception: Cause Exception} {

} {ptolemy.kernel.util.KernelException: Detail Message
  in .NamedObj 1 and .NamedObj 2
Because:
Cause Exception} {.NamedObj 1} {.NamedObj 2}}


test KernelException-8.0 {printStackTrace()} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set cause [java::new Exception "Cause Exception"]
    set pe [java::new ptolemy.kernel.util.KernelException \
	    $n1 $n2 $cause "Detail Message"]
    jdkCaptureErr {$pe printStackTrace} errMsg
    list [string range $errMsg 0 108]
} {{ptolemy.kernel.util.KernelException: Detail Message
  in .NamedObj 1 and .NamedObj 2
Because:
Cause Exception}}
