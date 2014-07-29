# Tests for the Expression class
#
# @Author: Daniel Crawl
#
# @Version: : mktestdir 13471 2000-10-31 19:29:02Z eal $
#
# @Copyright (c) 2014 The Regents of the University of California.
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
test Expression-1.1 {test clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set matlab [java::new ptolemy.matlab.Expression $e0 Matlab]
    set clone [java::cast ptolemy.matlab.Expression \
        [$matlab clone [$e0 workspace]]]
    list {1}
} {1}

test Expression-1.2 {test clone} {
    # set the _iteration value in the original object
    set iteration [java::cast ptolemy.data.expr.Variable \
        [$matlab getAttribute "_iteration"]]
    $iteration setToken [java::new ptolemy.data.IntToken 2]

    # get _iteration in the clone to make sure it's different
    set iterationClone [java::cast ptolemy.data.expr.Variable \
        [$clone getAttribute "_iteration"]]
    [java::cast ptolemy.data.IntToken [$iterationClone getToken]] \
        intValue
} {1}
