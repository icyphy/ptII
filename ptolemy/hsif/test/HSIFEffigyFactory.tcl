# Tests for the HSIFEffigyFactory class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

if {[string compare removeGraphicalClasses [info procs removeGraphicalClasses]] != 0} \
        then {
    source [file join $PTII util testsuite removeGraphicalClasses.tcl]
} {}

test HSIFEffigyFactory-1.1 {canCreateBlankEffigy} {
    set compositeEntity [java::new ptolemy.kernel.CompositeEntity]
    set hsifEffigyFactory [java::new ptolemy.hsif.HSIFEffigyFactory $compositeEntity foo]
    $hsifEffigyFactory canCreateBlankEffigy
} {0}

test HSIFEffigyFactory-2.1 { createEffigy with a non .xml or .hsif file} {
    # Uses 1.1 above
    set top [java::new ptolemy.kernel.CompositeEntity]
    set effigy [$hsifEffigyFactory createEffigy $top [java::null] \
	[java::new java.net.URL "file:/C:/foo.bar"]]
    java::isnull $effigy
} {1}

test HSIFEffigyFactory-2.2 { createEffigy with a non .xml or .hsif file} {
    # Uses 1.1 above
    set top [java::new ptolemy.kernel.CompositeEntity]
    set input [java::new java.io.File HSIFConfiguration.xml]
    set effigy [$hsifEffigyFactory createEffigy $top [java::null] \
	[$input toURL]]
    java::isnull $effigy
} {1}

test HSIFEffigyFactory-2.2 { createEffigy with a non-existant .xml file} {
    # Uses 1.1 above
    set top [java::new ptolemy.kernel.CompositeEntity]
    catch {set effigy [$hsifEffigyFactory createEffigy $top [java::null] \
	[java::new java.net.URL "file:/C:/DoesNotExist.xml"]]} msg
    list $msg	
} {{java.io.FileNotFoundException: /C:/DoesNotExist.xml (No such file or directory)}}
