# Generate code for models in auto
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2006 The Regents of the University of California.
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
    set args [java::new {String[]} 1 \
			  [list $file]]
    java::new ptolemy.vergil.VergilApplication $args
}

	set parser [java::new ptolemy.moml.MoMLParser]

    if {[info vars configuration] == ""} {
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]


	set configuration [java::call ptolemy.actor.gui.MoMLApplication readConfiguration $configurationURL]
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
	    set args [java::new {String[]} 1 [list $file]]

	    set timeout 60000
	    puts "codegen.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	    set watchDog [java::new util.testsuite.WatchDog $timeout]

	    set returnValue 0
	    set vergil [java::new ptolemy.vergil.VergilApplication $args]
	    set models [$vergil models]
	    # The first model is the configuration, the second is the UserLib
	    set toplevel [java::cast ptolemy.kernel.util.NamedObj [$models get 2]]
	    set cg [java::cast ptolemy.codegen.kernel.StaticSchedulingCodeGenerator 			[$toplevel getAttribute StaticSchedulingCodeGenerator]]
	    if [catch {set returnValue [$cg generateCode]}] {
	        $watchDog cancel
	        error "$errMsg\n[jdkStackTrace]"
	    } else {
	        $watchDog cancel
	    }
	    list $returnValue
	} {{}} {KNOWN_FAILURE}
    }
}

# Open all the files in Vergil
set files [glob auto/*.xml]
set args [java::new {String[]} [llength $files] $files]
set vergil [java::new ptolemy.vergil.VergilApplication $args]

set models [[$vergil models] iterator]
while {[$models hasNext]} {
    set model [java::cast ptolemy.kernel.util.NamedObj [$models next]]
    set modelName [$model getName]
    if {$modelName == "configuration" || $modelName == "UserLibrary"} {
	continue
    }
    set uri [java::cast ptolemy.kernel.attributes.URIAttribute [$model getAttribute _uri]]
    set file [[$uri getURL] getFile]
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $file {$PTII}]

    puts "------------------ CGC testing $relativeFilename"
    test "Auto" "Automatic CGC test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
        set application [createAndExecute $file]
	set timeout 60000
	puts "codegen.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new util.testsuite.WatchDog $timeout]

	set returnValue 0

        set cg [java::cast ptolemy.codegen.kernel.StaticSchedulingCodeGenerator 			[$model getAttribute StaticSchedulingCodeGenerator]]
        if [catch {set returnValue [$cg generateCode]} errMsg] {
	    $watchDog cancel
	    error "$errMsg\n[jdkStackTrace]"
	} else {
	    $watchDog cancel
	}
	list $returnValue
    } {{}}
}

# Print out stats
doneTests
