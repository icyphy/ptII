# Tests for missing demos in configs/doc/demos.htm
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2013-2017 The Regents of the University of California.
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

if {[string compare test [info procs jdkCapture]] == 1} then {
    source $PTII/util/testsuite/jdktools.tcl
} {}


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

test missingDemos-0.5 {Update ptolemy/configs/doc/models.txt} {
    puts "Updating $PTII/ptolemy/configs/doc/models.txt.  This could take a minute."
    exec -stderrok make -C ../doc update
    puts "Done updating $PTII/ptolemy/configs/doc/models.txt"
    file exists ../doc/models.txt
} {1}


test missingDemos-1.0 {Look for demos listed in configs/doc/demos.html that are not in models.txt} {
    # Run it once to build any missing files.  It is ok if we have output on stderr.
    exec -stderrok make -C ../doc missingDemos
    jdkCaptureOutAndErr {
	exec make -C ../doc --no-print-directory missingDemos
    } out err
    puts "Stderr for 'make -C ../doc missingDemos' was\n--start--\n$err\n--end--"
    regsub -all {make: [^']*'} $err {} err2
    list $out $err2
} {{} {}}

test missingDemos-2.0 {Run the missingDemos script.} {
    jdkCaptureOutAndErr {
	# In the installer, missingDemos might not be executable.
	exec chmod a+x $PTII/ptolemy/configs/test/missingDemos
        exec $PTII/ptolemy/configs/test/missingDemos
    } out err
    puts "Stderr for '$PTII/ptolemy/configs/test/missingDemos' was\n--start--\n$err\n--end--"
    regsub -all {make: [^']*'} $err {} err2
    list $out $err2
} {{} {}}

