# Run tests in the auto directory.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

proc createAndExecute {file} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parseFile $file]]
    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    # FailedChangeListener throws a RuntimeException if a change request
    # fails.  If we do not add this listener, then tests that have
    # failed change requests will be counted as passing.
    set failedChangeListener \
	    [java::new util.testsuite.FailedChangeListener]
    $toplevel setManager $manager
    $toplevel addChangeListener  $failedChangeListener
    # Success is just not throwing an exception.
    $manager execute
}

foreach file [glob auto/*.xml] {
    puts "------------------ testing $file"
    test "Auto" "Automatic test in file $file" {
        createAndExecute $file
        list {}
    } {{}}
}

# Print out stats
doneTests
