# Tests for the PtolemyThread class 
#
# @Author: Lukito Muliadi
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test PtolemyThread-1.1 {Test the constructor} {
    set pthread1 [java::new ptolemy.kernel.util.PtolemyThread]

    set thread [java::new Thread]
    set pthread2 [java::new ptolemy.kernel.util.PtolemyThread \
	    $thread]
    set pthread3 [java::new ptolemy.kernel.util.PtolemyThread \
	    $thread "pthread2"] 

    set pthread4 [java::new ptolemy.kernel.util.PtolemyThread \
	    "pthread4"] 

    set threadGroup [java::new ThreadGroup "ptThreadGroup"]
    set pthread5 [java::new ptolemy.kernel.util.PtolemyThread \
	    $threadGroup $thread]
    set pthread6 [java::new ptolemy.kernel.util.PtolemyThread \
	    $threadGroup $thread "pthread6"]
    set pthread7 [java::new ptolemy.kernel.util.PtolemyThread \
	    $threadGroup "pthread7"]

    set threads [list $pthread1 $pthread2 $pthread3 \
	    $pthread4 $pthread5 $pthread6 $pthread7]

    set results {}
    foreach thread $threads {
	lappend results [list [$thread getReadDepth] \
		[$thread getName]]
    }
    list $results [$threadGroup activeCount]
} {{{0 Thread-0} {0 Thread-2} {0 pthread2} {0 pthread4} {0 Thread-3} {0 pthread6} {0 pthread7}} 3}
