# Run tests in the auto directory.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

proc createAndExecute {file} {
    java::new ptolemy.actor.gui.MoMLSimpleApplication $file
}

if [ file isdirectory auto/knownFailedTests ] {
    foreach file [glob -nocomplain auto/knownFailedTests/*.xml] {
	# Get the name of the current directory relative to $PTII
	set relativeFilename \
		[java::call ptolemy.util.StringUtilities substituteFilePrefix \
		$PTII [file join [pwd] $file] {$PTII}]
	puts "------------------ testing $relativeFilename (Known Failure) "
	test "Auto" "Automatic test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
	    createAndExecute $file
	    list {}
	} {{}} {KNOWN_FAILURE}
    }
}

foreach file [glob auto/*.xml] {
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $file {$PTII}]

    puts "------------------ testing $relativeFilename"
    test "Auto" "Automatic test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
        set application [createAndExecute $file]
        list {}
    } {{}}
    test "Auto-rerun" "Automatic test rerun in file $file" {
	$application rerun
	list {}
    } {{}}
}


# Print out stats
doneTests
