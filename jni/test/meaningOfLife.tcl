# Tests for the JNI interface
#
# @Author: Christopher Hylands
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

proc testJNI {modelbase} {
    puts "Generating JNI for $modelbase" 

    # Read in the model
    set parser [java::new ptolemy.moml.MoMLParser]

    set namedObj [$parser parseFile "./$modelbase.xml"]
    set toplevel [java::cast ptolemy.actor.CompositeActor $namedObj]
    

    # Create the JNI files and compile them.
    # generateJNI also deletes relations and ports and recreates the ports
    java::call jni.JNIUtilities generateJNI $toplevel

    runModel $modelbase
}

proc runModel {modelbase} {
    puts "Running $modelbase"
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset

    # Why does parseFile fail, yet parse(URL,URL) work?
    #set namedObj [$parser parseFile "./$modelbase.xml"]
    set file [java::new java.io.File $modelbase.xml]
    set namedObj [$parser {parse java.net.URL java.net.URL} \
		      [java::null] [$file toURL]]

    set toplevel [java::cast ptolemy.actor.CompositeActor $namedObj]

    # Run the model
    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "$modelbase"]
    
    $toplevel setManager $manager
    $manager execute
}

######################################################################
####
#

# Remove the jni directory that might contain code from a previous
# run.
file delete -force jni


test meaningOfLife-1.1 {Run a simple JNI model} {
    # Create the shared library that has the code we want
    puts "Running 'make shared'"
    puts "[exec make shared SHAREDBASE=meaningOfLife]"
    testJNI meaningOfLife
} {}

test meaningOfLife-1.2 {A native function that takes an int and a float } {
    # Create the shared library that has the code we want
    puts "Running 'make shared'"
    puts "[exec make shared SHAREDBASE=testDeux]"
    testJNI testDeux
} {}

test meaningOfLife-1.3 {A native function that takes arrays of longs} {
    # Create the shared library that has the code we want
    puts "Running 'make shared'"
    puts "[exec make shared SHAREDBASE=testTrois]"
    testJNI testTrois
} {}

test meaningOfLife-1.4 {Run a model that uses both testDeux and testTrois} {
    testJNI testQuatre
} {}




