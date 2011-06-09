# Tests for the LabeledList class.
#
# @Author: Shuvra S. Bhattacharyya
#
# @version $Id$
#
# @Copyright (c) 2001-2005 The University of Maryland.
# All rights reserved.
# 
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
# 
# THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
# 
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#

######################################################################
####
# 
test LabeledList-2.1 {Create an labeled list} {
    set p [java::new ptolemy.graph.LabeledList]
    $p size
} {0}

######################################################################
####
# 
test LabeledList-2.2 {Add several elements to a list } {
    set e1 [java::new {java.lang.String String} element1]
    set e2 [java::new {java.lang.String String} element2]
    set e3 [java::new {java.lang.String String} element3]
    set e4 [java::new {java.lang.String String} element4]
    set e5 [java::new {java.lang.String String} element5]
    $p add $e1
    $p add $e2
    $p add $e3
    $p add $e4
    $p add $e5
    list [$p toString]
} {{element1
element2
element3
element4
element5}}

