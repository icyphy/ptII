# Tests for the URLAttribute class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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
test URLAttribute-1.1 {Call workspace constructor, exportMoML and toString } {
    set n0 [java::new ptolemy.kernel.util.NamedObj "myNamedObj"]
    set u1 [java::new ptolemy.moml.URLAttribute $n0 "myURLAttribute"]
    set output [java::new java.io.StringWriter]
    $u1 exportMoML $output 1

    $u1 setURL [java::new java.net.URL "http://ptolemy.eecs.berkeley.edu"]
    set url [$u1 getURL]
    set output2 [java::new java.io.StringWriter]
    $u1 exportMoML $output2 1
    list [$u1 toString] [$output toString] [$url toString] [$output2 toString]
} {{ptolemy.moml.URLAttribute {.myNamedObj.myURLAttribute}} {} http://ptolemy.eecs.berkeley.edu {}}
