# PNInterExample demo
#
# @Author: Christopher Hylands, Mudit Goel 
#
# @Version:  $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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


# Tcl Blend code to run the PNInterExample from tyjtclsh do: 
#    ptuser@kahn 1% cd $TYCHO/java/pt/domains/pn/kernel/demo
#    ptuser@kahn 2% tyjtclsh PNInterExample.tcl
#
# To run the test without Tcl Blend, do:
#    ptuser@kahn 1% cd $TYCHO/java/pt/domains/pn/demo
#    ptuser@kahn 2% setenv CLASSPATH $TYCHO/java
#    ptuser@kahn 3% java pt.domains.pn.kernel.PNInterleavingExample 50

# These commands use the Java version of this demo.
# The PNInterleavingExample Java class creates the same definitions and connections
# as this file does in Tcl.
#
# set PNInterExample [java::new pt.domains.pn.kernel.PNInterExample]
# set args [java::new {java.lang.String[]} {50}]
# $PNInterExample main $args

if [catch {package require java} err] {
    puts stderr "This file requires Tcl Blend, and will not work\
	    with Itcl2.2."
    puts stderr "'package require java' failed with:\n$err"
}

# If $TYCHO is not set, then try to set it.
if [info exists env(TYCHO)] {
    set TYCHO $env(TYCHO)
} else { 
    if [info exist env(PTOLEMY)] {
	set TYCHO $env(PTOLEMY)/tycho
    }

    if [info exist env(TYCHO)] {
	set TYCHO $env(TYCHO)
    }

    if {![info exist TYCHO]} {
	# If we are here, then we are probably running jacl and we can't
	# read environment variables
	set TYCHO [file join [pwd] .. .. .. ..]
    }
}

set stream [java::new java.io.ByteArrayOutputStream]
set printStream [java::new \
    {java.io.PrintStream java.io.OutputStream} $stream]

java::call System setOut $printStream

set out [java::field System out]
$out {println String} foo

set myUniverse [java::new pt.domains.pn.kernel.PNUniverse]
$myUniverse setName "root"

# Find the number of iterations
if ![info exists numberOfCycles] {
    set numberOfCycles 10
}

$myUniverse setCycles $numberOfCycles 

set interleave [java::new pt.domains.pn.stars.PNInterleave $myUniverse "interlea
ve"]
 
set alternate [java::new pt.domains.pn.stars.PNAlternate $myUniverse "alternate"
]
 
set redirect0 [java::new pt.domains.pn.stars.PNRedirect $myUniverse "redirect0"]
 
set redirect1 [java::new pt.domains.pn.stars.PNRedirect $myUniverse "redirect1"]


set portout [$interleave getPort "output"]
set portin [$alternate getPort "input"]
$myUniverse connect $portin $portout QX
 
set portout [$redirect0 getPort "output"]
set portin [$interleave getPort "input0"]
$myUniverse connect $portin $portout QY
 
set portout [$redirect1 getPort "output"]
set portin [$interleave getPort "input1"]
$myUniverse connect $portin $portout QZ
 
set portout [$alternate getPort "output0"]
set portin [$redirect0 getPort "input"]
$myUniverse connect $portin $portout QT1
 
set portout [$alternate getPort "output1"]
set portin [$redirect1 getPort "input"]
$myUniverse connect $portin $portout QT2
 
#NOTE THESE:
$redirect0 setInitState 0
$redirect1 setInitState 1

$myUniverse execute

puts "Bye World"

$printStream flush
puts "The output was [$stream toString]"

