# Tests for the TotallyOrderedSet class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#

######################################################################
####  Generally used director.
#
set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector]

######################################################################
####  Test constructors.
#
test CTActor-1.1 {Construct a CTActor and get name} {
    set a1 [java::new ptolemy.domains.ct.kernel.CTActor]
    set prename [$a1 getName]
    $a1 setName A1
    list $prename [$a1 getName]
} {{} A1}

test CTActor-1.2 {Construct a CTActor in a workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set a1 [java::new ptolemy.domains.ct.kernel.CTActor $w]
    list [$a1 getFullName]
} {W.}

test CTActor-1.3 {Construct a CTActor with a container and a name} {
    set ca [java::new ptolemy.actor.TypedCompositeActor]
    $ca setName COMP
    set a1 [java::new ptolemy.domains.ct.kernel.CTActor $ca A1]
    list [$a1 getFullName]
} {.COMP.A1}
######################################################################
####  Test CTParameters
#  
test CTActor-2.1 {Create a CTParameter} {
    # Note: Use the above setup
    set zero [java::new {ptolemy.data.DoubleToken double} 0.0]
    set par [java::new ptolemy.domains.ct.kernel.CTParameter \
	    $a1 PARAM $zero]
    list [[$par getToken] doubleValue]
} {0.0}

######################################################################
####
#
