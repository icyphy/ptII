# Timing tests for the MoMLWriter class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2002 The Regents of the University of California.
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

if {[info procs jdkRuntimeStatistics] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#

# Expand a configuration and try MoMLWriter and exportMoML and report
# the times
proc compareMoMLWriter {configuration {repeatCount 3}} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set loader [[$parser getClass] getClassLoader]
    
    set URL [$loader getResource $configuration]
    puts "URL of configuration being expanded is\n [$URL toString]"
    set object [$parser {parse java.net.URL java.net.URL} $URL $URL]

    # Force everything to get expanded
    set configuration [java::cast ptolemy.kernel.CompositeEntity $object]

    # Keep track of averages
    set MoMLWriterSumElapsedTime 0
    set exportMoMLSumElapsedTime 0

    for {set i 0} {$i < $repeatCount} {incr i} {
	# First, try MoMLWriter
	set startTime [java::call System currentTimeMillis]

	set writer [java::new java.io.StringWriter]
	set mwriter [java::new ptolemy.moml.MoMLWriter $writer]
	$mwriter write $configuration

	set endTime [java::call System currentTimeMillis]
	set elapsedTime [expr {$endTime - $startTime}]
	set MoMLWriterSumElapsedTime [expr {$elapsedTime + \
		$MoMLWriterSumElapsedTime}]

	puts "MoMLWriter  total elapsed time: $elapsedTime ms"
		
	# Now, try exportMoML
	set startTime [java::call System currentTimeMillis]
	$configuration exportMoML
	set endTime [java::call System currentTimeMillis]
	set elapsedTime [expr {$endTime - $startTime}]

	set exportMoMLSumElapsedTime [expr {$elapsedTime + \
		$exportMoMLSumElapsedTime}]
	puts "exportMoML  total elapsed time: $elapsedTime ms"
	puts "[jdkRuntimeStatistics]"
    }
    puts "Average MoMLWriter elapsed time : \
	    [expr {$MoMLWriterSumElapsedTime / $repeatCount}]"

    puts "Average exportWriter elapsed time : \
	    [expr {$exportMoMLSumElapsedTime / $repeatCount}]"

}

######################################################################
####
#
test MoMLWriterTiming-1.1 {Try the DSP only configuration} {
    compareMoMLWriter "ptolemy/configs/vergilConfigurationDSP.xml"
    list 0
} {0}

######################################################################
####
#
test MoMLWriterTiming-1.2 {Try the Ptiny only configuration} {
    compareMoMLWriter "ptolemy/configs/vergilConfigurationPtiny.xml"
    list 0
} {0}

