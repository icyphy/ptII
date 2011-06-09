# Tests for the ParserAttribute class
#
# @Author: Christopher Hylands (tests only)
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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
test ParserAttribute-1.1 {Call workspace constructor, exportMoML and toString } {
    set w0 [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set n0 [java::new ptolemy.kernel.util.NamedObj $w0]
    set p1 [java::new ptolemy.moml.ParserAttribute $n0 "myParserAttribute"]
    list [$p1 toString]
} {{ptolemy.moml.ParserAttribute {..myParserAttribute}}}

test ParserAttribute-2.1 {Call clone(Workspace)} {
    set w2 [java::new ptolemy.kernel.util.Workspace "myWorkspace2"]
    set n2 [java::new ptolemy.kernel.util.NamedObj $w2]
    set p2 [java::new ptolemy.moml.ParserAttribute $n2 "myParserAttribute2"]
    set MoMLParser2 [$p2 getParser]
    # Clone the ParserAttribute	
    set p2Clone [java::cast ptolemy.moml.ParserAttribute \
	[$p2 clone \
		[java::new ptolemy.kernel.util.Workspace \
			"myWorkspace2Clone"]]]
    set MoMLParser2Clone [$p2Clone getParser]

    # Set the toplevel of the master 
    set n2b [java::new ptolemy.kernel.util.NamedObj $w2 "n2b" ]
    $MoMLParser2 setToplevel $n2b

    # The toplevel of the master should not be .n2b	
    list [[$MoMLParser2 getToplevel] equals \
	 [$MoMLParser2Clone getToplevel]]
} {0}
