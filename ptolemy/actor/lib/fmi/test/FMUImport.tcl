# Test FMUImport
#
# @Author: Christopher Brooks
#
# @Version: $Id: Ramp.tcl 61458 2011-07-11 19:54:07Z cxh $
#
# @Copyright (c) 2012 The Regents of the University of California.
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

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

######################################################################
####
#
test FMUImport-1.1 {} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile {$CLASSPATH/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu} [java::null]]
    java::call ptolemy.actor.lib.fmi.FMUImport importFMU $e1 [$fmuFile getCanonicalPath]  $e1 100.0 100.0
    puts [$e1 exportMoML]
} {}
