# Tests for the Sampler class
#
# @Author: Jie Liu, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1999-2003 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Sampler-1.1 {test constructor and clone} {
    set e0 [deModel 3.0]
    set samplerbase [java::new ptolemy.domains.de.lib.Sampler $e0 sampler]
    set sampler [java::cast ptolemy.domains.de.lib.Sampler \
		     [$samplerbase clone [$e0 workspace]]]
    $samplerbase {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $sampler {setContainer ptolemy.kernel.CompositeEntity} $e0
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Sampler in a DE model
#
test Sampler-2.1 {test with the default output values} {
    set clock1 [java::new ptolemy.actor.lib.Clock $e0 clock1]
    set clock2 [java::new ptolemy.actor.lib.Clock $e0 clock2]
    set period [java::field $clock2 period]
    $period setExpression {1.0}
    set offsets [java::field $clock2 offsets]
    $offsets setExpression {{0.0, 0.5}}
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set r0 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $clock1] output] \
       [java::field [java::cast ptolemy.domains.de.lib.DETransformer $sampler] \
       input]]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.domains.de.lib.DETransformer $sampler] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $clock2] output] \
       [java::field $sampler trigger]
    [$e0 getManager] execute
    list [enumToTokenValues [$rec getRecord 0]] \
            [enumToObjects [$rec getTimeRecord]]
} {{1 1 0 0 1 1 0} {0.0 0.5 1.0 1.5 2.0 2.5 3.0}}
