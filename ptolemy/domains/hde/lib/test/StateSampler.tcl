# Test StateSampler
#
# @Author: Christopher
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

######################################################################
####
#
test StateSampler-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set stateSamplerBase [java::new ptolemy.domains.hde.lib.StateSampler $e0 stateSampler]

    # Make sure that the type is cloned

    set t1 [[java::new ptolemy.data.IntToken] getType]
    set inputbase [java::field [java::cast ptolemy.domains.de.lib.DETransformer \
            $stateSamplerBase] input]
    $inputbase setTypeEquals $t1

    set stateSampler [java::cast ptolemy.domains.hde.lib.StateSampler \
	    [$stateSamplerBase clone [$e0 workspace]]]

    set input [java::field [java::cast ptolemy.domains.de.lib.DETransformer \
            $stateSamplerBase] input]

    set type [$input getType]
    $type toString

} {int}

