# Tests for the Query class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Query-1.1 {} {
    set query [java::new ptolemy.gui.Query]
    set options [java::new {String[]} 4 [list "water" "soda" "juice" "none"]]
    $query addRadioButtons "radio" "Radio buttons" $options "water"
    set dialog [java::new ptolemy.gui.ComponentDialog \
	    [java::null] "Query-1.1: Press OK" $query]
    set results [$dialog buttonPressed]
} {OK}

test Query-1.2 {Lots of text} {
    set query [java::new ptolemy.gui.Query]
    set stringBuffer [java::new StringBuffer]
    for {set i 0} {$i < 30} {incr i} {
	$stringBuffer append "Line $i\n"
    } 
    $query setMessage [$stringBuffer toString] 
    $query addRadioButtons "radio" "Radio buttons" $options "water"
    set dialog [java::new ptolemy.gui.ComponentDialog \
	    [java::null] "Query-1.2: Press OK" $query]
    set results [$dialog buttonPressed]
} {OK}


test Query-1.3 {Lots of checkboxes} {
    # Uses 1.2 from above
    set query [java::new ptolemy.gui.Query]

    $query setMessage [$stringBuffer toString] 
    for {set i 0} {$i < 10} {incr i} {
	set options [java::new {String[]} 4 \
		[list "line $i" "soda" "juice" "none"]]
	$query addCheckBox "cb $i" "cb $i" true
	$query addChoice "choice $i" "choice $i" $options "choice $i"
	$query addDisplay "display $i" "display $i" "$i"
	$query addLine "line $i" "line $i" "$i"
	$query addRadioButtons "radio" "Radio buttons" $options "line $i"

	set initiallySelected [java::new java.util.HashSet]
	$initiallySelected add "line $i"
	$query addSelectButtons "select" "select buttons" $options \
	    $initiallySelected
	$query addSlider "slider $i" "slider $i"  $i 0 40 
    } 
    set dialog [java::new ptolemy.gui.ComponentDialog \
	    [java::null] "Query-1.3: Press OK" $query]
    set results [$dialog buttonPressed]
} {OK}


test Query-1.4 {Test out Password} {
    set query [java::new ptolemy.gui.Query]
    set options [java::new {String[]} 4 [list "water" "soda" "juice" "none"]]
    $query addPassword "password" "password" foo
    set dialog [java::new ptolemy.gui.ComponentDialog \
	    [java::null] "Query-1.4: Press OK" $query]
    set results [$dialog buttonPressed]
} {OK}