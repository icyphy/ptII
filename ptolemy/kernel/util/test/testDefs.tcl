# Load test bed definitions
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

if [info exist env(PTOLEMY)] {
    set TYCHO $env(PTOLEMY)/tycho
}

if [info exist env(TYCHO)] {
    set TYCHO $env(TYCHO)
}

if {![info exist TYCHO]} {
    # If we are here, then we are probably running jacl and we can't
    # read environment variables
    set TYCHO [file join [pwd] .. .. .. ..]
}

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source [file join $TYCHO kernel test testDefs.tcl]
} {}

######################################################################
#### 
# Split a string into shorter lines separated by newlines.
#
proc _splitline {str} {
    set results ""
    for {set i 0} {$i < [string length $str] } {incr i 55} {
	append results "[string range $str $i [expr {$i + 54}]]\n    "
    }
    return $results
}

######################################################################
####
# Return a string that contains all of the information for an object
# that we can retrieve with java::info
#
proc getJavaInfo {obj} {
    return "\n \
    class:         [java::info class $obj]\n \
    fields:        [_splitline [lsort [java::info fields $obj]]]\n \
    methods:       [_splitline [lsort [java::info methods $obj]]]\n \
    constructors:  [_splitline [lsort [java::info constructors $obj]]]\n \
    properties:    [_splitline [lsort [java::info properties $obj]]]\n \
    superclass:    [_splitline [java::info superclass $obj]]\n"
}

