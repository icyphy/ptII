# Tcl script that runs the backward compatibility filter on a MoML file

# Author:  Christopher Hylands
# Version: $Id$
#
# Copyright (c) 1999-2002 The Regents of the University of California.
# 	All Rights Reserved.
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

proc updateMoMLFile { {file ../../ptolemy/domains/ct/demo/CarTracking/CarTracking.xml} } {
    set outputFile "updateMoMLFiles.xml"
    file delete -force $outputFile

    puts "parsing $file"
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]
    #$parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]

    set toplevel [$parser parseFile $file]

    if  {$toplevel == [java::null]} {
	error "$file: toplevel was null"
    }
    set fileOutputStream [java::new java.io.FileOutputStream $outputFile]
    set outputStreamWriter [java::new java.io.OutputStreamWriter \
	    $fileOutputStream]
    puts "exporting $outputFile"
    $toplevel exportMoML $outputStreamWriter
    $outputStreamWriter close
		# The list of filters is static, so we reset it
		java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]    
}

if {$argc > 0 } {
    updateMoMLFile $argv
}
