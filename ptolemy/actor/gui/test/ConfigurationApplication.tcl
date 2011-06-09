# Test ConfigurationApplication
#
# @Author: Edward A. Lee
#
# @Version: $Id$
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test ConfigurationApplication-1.0 {test reading MoML file} {
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::new java.util.LinkedList]
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]
    set filterSize [[java::call ptolemy.moml.MoMLParser getMoMLFilters] size]
    set cmdArgs [java::new {java.lang.String[]} 2 \
            {{ptolemy/actor/gui/test/testConfiguration.xml} {test.xml}}]
    set app [java::new ptolemy.actor.gui.ConfigurationApplication $cmdArgs]
    set newFilterSize [[java::call ptolemy.moml.MoMLParser getMoMLFilters] size]

    puts "test 1.0: filterSize: $filterSize, newFilterSize: $newFilterSize"
    # There should be two more filters.
    list [expr {$newFilterSize - $filterSize}]
} {2}
