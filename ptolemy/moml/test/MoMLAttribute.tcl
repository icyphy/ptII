# Tests for the MoMLAttribute class
#
# @Author: Christopher Hylands (tests only)
#
# @Version: $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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
test MoMLAttribute-1.1 {Call workspace constructor, exportMoML and toString } {
    set w0 [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set m1 [java::new ptolemy.moml.MoMLAttribute $w0]
    set output [java::new java.io.StringWriter]
    list [$m1 toString] [$output toString]
} {{ptolemy.moml.MoMLAttribute {.}} {}}

test MoMLAttribute-1.2 {Call NamedObj, String constructor, appendMoMLDescription} {
    set n0 [java::new ptolemy.kernel.util.NamedObj "myNamedObj"]
    set m1 [java::new ptolemy.moml.MoMLAttribute $n0 "myMoMLAttribute"]
    $m1 exportMoML $output 1
    $m1 appendMoMLDescription "The quick brown fox"
    $m1 appendMoMLDescription "jumps over the lazy dog"
    set output2 [java::new java.io.StringWriter]
    $m1 exportMoML $output2 2
    list [$m1 toString] [$output toString] "\n" [$output2 toString]
} {{ptolemy.moml.MoMLAttribute {.myNamedObj.myMoMLAttribute}} {} {
} {        The quick brown fox
        jumps over the lazy dog
}}
