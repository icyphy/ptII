# Tests for the PtParser class
#
# @Author: Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

# 
#

proc generateCode {root} {
    set typeInference [java::new ptolemy.data.expr.CParseTreeCodeGenerator]
    $typeInference generateCode $root
}
proc displayTree {root} {
    set display [java::new ptolemy.data.expr.ParseTreeDumper]
    $display displayParseTree $root
}

######################################################################
####
# 
test CParseTreeCodeGenerator-2.2 {Construct a Parser, try simple integer expressions} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "2 + 3 + 4"]
    set type [generateCode $root]

    set root [ $p1 {generateParseTree String} "2 - 3 - 4"]
    set type1 [generateCode $root]

    set root [ $p1 {generateParseTree String} "2 * 3 * 4"]
    set type2 [generateCode $root]

    set root [ $p1 {generateParseTree String} "7 % 5"]
    set type3 [generateCode $root]

    set root [ $p1 {generateParseTree String} "12 / 2 / 3"]
    set type4 [generateCode $root]

    list $type $type1 $type2 $type3 $type4
} {1 1 1 1 1} {Known failure}
