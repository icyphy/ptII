# Tests for all demos listed in completeDemos.htm
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Set the timeOut to two hours
set timeOutSeconds 12000

test allDemos-1.0 {} {
    set file [open $PTII/ptolemy/configs/doc/models.txt]
    set modelsFileContents [read $file]
    close $file
    set models [split $modelsFileContents "\n"]
    foreach model $models {
	if {[string length $model] < 1} {
	    continue
	}

	# We create a new parser each time and avoid leaks,
	# See ptdevel mail from 6/15/2009

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



	regsub {\$CLASSPATH} $model {$PTII} modelPath
	set modelPath [subst $modelPath]
	set modelFile [java::new java.io.File $modelPath]
	set modelPath [$modelFile getCanonicalPath]
        puts "modelPath: $modelPath"

	set startTime [java::call -noconvert System currentTimeMillis]
	set toplevel [java::cast ptolemy.kernel.CompositeEntity \
		[$parser parseFile $modelPath]]
	puts "####$modelPath\n[$toplevel getFullName] [java::call ptolemy.actor.Manager timeAndMemory [$startTime longValue]]\n[$toplevel statistics [java::null]]"
	$toplevel setContainer [java::null]
	$parser resetAll

	# Bert Rodiers writes: "Another issues seems to be
	# ptolemy.data.expr.CachedMethod. For example
	# ParseTreeEvaluator uses this object which caches things in
	# its static member, but the contents are never cleared."
	java::call ptolemy.data.expr.CachedMethod clear

	java::call System gc
    }
} {}
