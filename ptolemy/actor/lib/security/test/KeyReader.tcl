# Tests for the KeyReader class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2013 The Regents of the University of California.
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

if {[string compare listToStrings [info procs listToStrings]] == 1} then { 
    source $PTII/util/testsuite/enums.tcl
} {}

if {[info procs jdkCapture] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
# 
test KeyReader-1.1 {Try to read in a non-existant key} {
    # We capture standard error and ignore it.
    jdkCaptureErr {
	catch {createAndExecute "KeyReaderBad.xml"} errorMessage
    } stderr
    regsub -all {URL is: '.*foo.keystore} $errorMessage {URL is: 'XXX/foo.keystore} r
    list $r
} {{ptolemy.kernel.util.IllegalActionException: Failed to get key store alias 'barf' or certificate from Keystore: 'foo.keystore', which exists and is readable,  as a URL is: 'XXX/foo.keystore'
  in .KeyReaderBad.SecretKeyReader
Because:
Failed to get certificate for alias 'barf' from  Keystore: 'foo.keystore', which exists and is readable,  as a URL is: 'XXX/foo.keystore'}}
