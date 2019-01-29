# Tests Configuration, to be run last
#
# @Author: Christopher Brooks
#
# @Copyright (c) 2006 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test ZZZConfiguration-100.0 {call setContainer, which calls _remoteEntity} {
    # Run this test last
    set directory [$configuration getDirectory]

    # StringUtilities.exit() checks to see if this property is has a length > 0
    java::call System setProperty ptolemy.ptII.doNotExit {}
    #java::call System clearProperty ptolemy.ptII.doNotExit

    # The Manager uses this property to help us test the Exit actor.
    java::call System setProperty ptolemy.ptII.exitAfterWrapup true

    catch {$directory setContainer [java::null]} errMsg

    # Reset the property
    java::call System setProperty ptolemy.ptII.doNotExit true

    java::new ptolemy.actor.gui.ModelDirectory $configuration [java::field ptolemy.actor.gui.Configuration _DIRECTORY_NAME]

    list $errMsg	
} {{java.lang.RuntimeException: StringUtilities.exit() was called. Normally, we would exit here because Manager.exitAfterWrapup() was called.  However, because the ptolemy.ptII.exitAfterWrapup property is set, we throw this exception instead.}}
