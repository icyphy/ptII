# Tests for the HSIFUtilities class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

test HSIFEffigy.1.1 {Convert the Thermostat demo using the configuration} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset	
    set inURL [java::call ptolemy.actor.gui.MoMLApplication specToURL \
	"ptolemy/configs/hyvisual/configuration.xml"]
    set toplevel [$parser parse $inURL [$inURL openStream]]
    set config [java::cast ptolemy.actor.gui.Configuration $toplevel]

    set inURL [java::call ptolemy.actor.gui.MoMLApplication specToURL \
	"../demo/Thermostat/Thermostat.xml"]
	
    set key [$inURL toExternalForm]	
    catch {$config openModel $inURL $inURL $key	} errMsg
    # It is ok to get a NoClassDefFoundError because we are trying to
    # start vergil	
    list $errMsg
} {java.lang.NoClassDefFoundError}
