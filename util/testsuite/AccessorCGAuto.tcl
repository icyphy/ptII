# Run "auto" tests
#
# @Author: Bert Rodiers
#
# @Version: $Id$
#
# @Copyright (c) 2009-2010 The Regents of the University of California.
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

# Get the value of ptolemy.ptII.isRunningNightlyBuild and save it,
# then reset the property to the empty string.
# If we are running as the nightly build, we usually want to
# throw an exception if the trainingMode parameter is set to true.
# However, while testing the Test actor itself, we want to 
# be able to set the trainingMode parameter to true

set oldIsRunningNightlyBuild \
    [java::call ptolemy.util.StringUtilities getProperty \
     "ptolemy.ptII.isRunningNightlyBuild"]
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" ""

if {[info procs testAccessorCG] == "" } then {
    source [file join $PTII util testsuite testAccessorCG.tcl]
}

foreach file [glob auto/*.xml] {
    testAccessorCG $file
}

# Reset the isRunningNightlyBuild property
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" \
    $oldIsRunningNightlyBuild 

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    
# Print out stats
doneTests
