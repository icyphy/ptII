# Test NashornAccessorHostApplication
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2016 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then { 
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


test NashornAccessorHostApplication-0.0.1 {No way to test the command line arguments} {
    list "Unfortunately, the Nashorn Host spawns a separate thread, so there" \
	"is no way to asyncronously wait to get the results back." \
	"This makes it difficult to test the command line argument processing."
} {1} {Known Failure}

# ######################################################################
# ####
# #
# test NashornAccessorHostApplication-0.1 {test with no arguments} {
#     jdkCapture {
# 	set commandArguments [java::new {java.lang.String[]} 0]
# 	set result [java::call ptolemy.actor.lib.jjs.NashornAccessorHostApplication evaluate $commandArguments]
# 	puts "result: $result"
#     } out
#     list $out 
# } {{ERROR: Usage: [-help] [-echo] [-js filename] [-timeout milliseconds] [-version] [accessorClassName] [accessorClassName ...]
# } 3}

# ######################################################################
# ####
# #
# test NashornAccessorHostApplication-1.1 {test -e -h} {
#     jdkCaptureOutAndErr {
# 	set commandArguments [java::new {java.lang.String[]} 2 \
# 				  {{-e} {-h}}]
# 	java::call ptolemy.actor.lib.jjs.NashornAccessorHostApplication evaluate $commandArguments
#     } out err
#     list $out $err
# } {{[ '-e', '-h' ]
# Usage: [-help] [-echo] [-js filename] [-timeout milliseconds] [-version] [accessorClassName] [accessorClassName ...]
# } 0}

# ######################################################################
# ####
# #
# test NashornAccessorHostApplication-2.1 {test -h} {
#     jdkCaptureOutAndErr {
# 	set commandArguments [java::new {java.lang.String[]} 1 \
# 			 {{-h}}]
# 	java::call ptolemy.actor.lib.jjs.NashornAccessorHostApplication evaluate $commandArguments
#     } out err
#     list $out $err
# } {{Usage: [-help] [-echo] [-js filename] [-timeout milliseconds] [-version] [accessorClassName] [accessorClassName ...]
# } 0}

# ######################################################################
# ####
# #
# test NashornAccessorHostApplication-3.1 {test -v} {
#     jdkCaptureOutAndErr {
# 	set commandArguments [java::new {java.lang.String[]} 1 \
# 			 {{-v}}]
# 	java::call ptolemy.actor.lib.jjs.NashornAccessorHostApplication evaluate $commandArguments
#     } out err

#     list [string range $out 0 8] $err
# } {Accessors 0}

# ######################################################################
# ####
# #
# test NashornAccessorHostApplication-4.1 {test -accessor and -timeout} {
#     # Unfortunately, -timeout invokes process.exit(), so we can't easily test this right now.
#     #jdkCaptureOutAndErr {
# 	# set commandArguments [java::new {java.lang.String[]} 4  {{-accessor} {-timeout} {2000} {hosts/nashorn/test/testNashornHost.js}}]
# 	# puts $commandArguments
# 	# catch {java::call ptolemy.actor.lib.jjs.NashornAccessorHostApplication evaluate $commandArguments} errMsg
# 	# puts $errMsg
#     #} out err

#     #list $out $err
# } {}

