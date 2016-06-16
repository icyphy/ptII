# Test GDPManager
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2016 The Regents of the University of California.
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

######################################################################
####
#
test GDPManager-1.1 {clean and then build the GDP} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set gdpSource [java::new ptolemy.data.expr.FileParameter $e gdpSource ]
    $gdpSource setExpression {$PTII/vendors/gdp}
    java::call ptolemy.actor.lib.jjs.modules.gdp.GDPManager downloadAndBuild $gdpSource true true
    list [file isfile [glob $PTII/vendors/gdp/gdp/gdp/gdp_api.o]] \
        [file isfile [glob $PTII/vendors/gdp/gdp/lang/java/org/terraswarm/gdp/GDP_GCL.class]]
} {1 1}

