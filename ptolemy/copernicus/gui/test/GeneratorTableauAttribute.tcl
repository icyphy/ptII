# Tests for the GeneratorTableauAttribute class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of California.
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test GeneratorTableauAttribute-1.1 {create an GTA, clone it} {
    set model [sdfModel]
    set modelAsNamedObj [java::cast ptolemy.kernel.util.NamedObj $model]
    set attribute \
	    [java::new ptolemy.copernicus.gui.GeneratorTableauAttribute \
	    $model "generatorTableauAttribute"]
    set clonedAttribute \
	    [java::cast ptolemy.copernicus.gui.GeneratorTableauAttribute \
	    [$attribute clone [$model workspace]]]
    $clonedAttribute setContainer $model
    #puts "attribute:\n[$attribute toString]\nclonedAttribute:\n[$clonedAttribute toString]"
    regsub -all "{.top}" "[$clonedAttribute toString]" "" clonedAttributeString
    if  { [$attribute toString] != $clonedAttributeString} {
	# diffText is defined in $PTII/util/testsuite/testDefs.tcl
	puts [diffText [$attribute toString] $clonedAttributeString]]
    }
    list [expr {[$attribute toString] == $clonedAttributeString}]
} {1}
