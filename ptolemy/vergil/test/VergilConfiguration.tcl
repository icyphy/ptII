# Tests for the Vergil Configuration
#
# @Author: Steve Nuendorffer, Christopher Hylands
#
# $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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

# Print memory statistics, call gc, then print memory statistics
proc memoryGCmemory {} {
    puts "Before gc: [jdkRuntimeStatistics]"
    java::call System gc    
    puts "After gc: [jdkRuntimeStatistics]"
}

# Expand a configuration
proc expandConfiguration {configuration} {
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    # Add backward compatibility filters
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    # Filter out graphical classes while inside MoMLParser
    # See ptII/util/testsuite/removeGraphicalClasses.tcl
    removeGraphicalClasses $parser

    set loader [[$parser getClass] getClassLoader]
    
    set URL [$loader getResource $configuration]
    puts "URL of configuration being expanded is\n [$URL toString]"
    set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
    # force everything to get expanded
    set configuration [java::cast ptolemy.kernel.CompositeEntity $object]
    set returnValue [catch {$configuration description} result]

    memoryGCmemory

    # If the test fails, then return result, otherwise, return 0
    if {$returnValue != 0} {
	return [list $result]
    } else {
	return [list $returnValue]
    }

}

######################################################################
####
#
test VergilConfiguration-1.1 {make sure that everything inside the DSP configuration can be expanded} {
    expandConfiguration "ptolemy/configs/dsp/configuration.xml"
} {0}

######################################################################
####
#
test VergilConfiguration-1.2 {make sure that everything inside the Ptiny configuration can be expanded} {
    expandConfiguration "ptolemy/configs/ptiny/configuration.xml"
} {0}

######################################################################
####
#
test VergilConfiguration-1.2.1 {make sure that everything inside the Hybrid configuration can be expanded} {
    expandConfiguration "ptolemy/configs/hyvisual/configuration.xml"
} {0}

######################################################################
####
#
test VergilConfiguration-1.3 {make sure that everything inside the Full configuration (with the matlab and serial actors removed) can be expanded} {
    # Remove the matlab lines from vergilConfiguration.xml

    set parser [java::new ptolemy.moml.MoMLParser]
    set loader [[$parser getClass] getClassLoader]

    set URL [$loader getResource "ptolemy/configs/full/configuration.xml"]
    puts "URL of vergilConfiguration.xml: [$URL toString]"
    if { "$tcl_platform(host_platform)" == "windows"} {
	set inFile [string range [$URL getPath] 1 end]
    } else {
	set inFile [$URL getPath]
    }

    puts "file name vergilConfiguration.xml: $inFile"

    # See if jxta is present
    set jxta 1
    if [catch {java::info superclass net.jxta.resolver.QueryHandler} \
	    err] { 
	puts "net.jxta.resolver.QueryHandler not found, skipping jxta"
	set jxta 0
    }


    set infd [open $inFile]
    set outfd [open vergilConfigurationNoMatlabNoSerialNoAppsNoJMF.xml "w"]
    while {![eof $infd]} {
	set linein [gets $infd]
	regsub -all {.*matlab.*} $linein {} lineout
	# Filter out the serial actor because it does not work under Unix,
	# which is where the nightly build is run
	regsub -all {.*comm/comm.xml.*} $lineout {} lineout2
	# Filter out apps
	regsub -all {.*apps/apps.xml.*} $lineout2 {} lineout3
	# Filter out jmf
	regsub -all {.*jmf/jmf.xml.*} $lineout3 {} lineout4
	# Filter out interactive icons
	regsub -all {.*lib/interactive.xml.*} $lineout4 {} lineout5
	# Filter out jxta
	if {$jxta == 1} {
	    set lineout6 $lineout5
	} else {
	    # Filter out jxta
	    regsub -all {.*jxta/jxta.xml.*} $lineout5 {} lineout6
	}
	puts $outfd $lineout6
    }
    close $infd
    close $outfd

    expandConfiguration "ptolemy/vergil/test/vergilConfigurationNoMatlabNoSerialNoAppsNoJMF.xml"
} {0}


######################################################################
####
#
#test VergilConfiguration-1.4 {make sure that everything inside the Full configuration can be expanded} {
#    expandConfiguration "ptolemy/configs/vergilConfiguration.xml"
#    # This is a known Failure because of matlab problems
#} {0} {Known Failure}
