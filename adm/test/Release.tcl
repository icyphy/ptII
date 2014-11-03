# Tests for release management
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2009-2013 The Regents of the University of California.
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

# Get rid of any previous lists of .java files etc.
exec make clean

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

test release-1.1 {Check for missing makefiles} {
    exec make --no-print-directory --silent missingMakefiles
} {./config/makefile
./doc/coding/templates/makefile
./ptolemy/actor/lib/fmi/fmipp/swig/makefile
./ptolemy/backtrack/automatic/ptolemy/actor/lib/makefile
./ptolemy/backtrack/automatic/ptolemy/domains/sdf/lib/makefile
./ptolemy/backtrack/automatic/ptolemy/math/makefile
./ptolemy/backtrack/eclipse/plugin/makefile
./ptolemy/backtrack/eclipse/plugin/actions/makefile
./ptolemy/backtrack/eclipse/plugin/actions/codestyle/makefile
./ptolemy/backtrack/eclipse/plugin/compatibility/makefile
./ptolemy/backtrack/eclipse/plugin/console/makefile
./ptolemy/backtrack/eclipse/plugin/dialogs/makefile
./ptolemy/backtrack/eclipse/plugin/editor/makefile
./ptolemy/backtrack/eclipse/plugin/preferences/makefile
./ptolemy/backtrack/eclipse/plugin/util/makefile
./ptolemy/backtrack/eclipse/plugin/widgets/makefile
./ptolemy/backtrack/ui/makefile
./ptolemy/backtrack/util/java/util/makefile
./ptolemy/plot/servlet/makefile}

test release-2.1 {Check for directories that have java files, but are not in doc/makefile} {
    exec make --no-print-directory --silent missingDocPackages
} {.
config
contrib.actor.lib.example
diva.util.java2d.svg
doc.coding.templates
doc.tutorial
doc.tutorial.domains
doc.tutorial.graph
doc.tutorial.graph.junit
doc.tutorial.gui
ptolemy.actor.corba
ptolemy.actor.corba.CoordinatorUtil
ptolemy.actor.corba.CorbaIOUtil
ptolemy.actor.corba.util
ptolemy.actor.lib.javasound.test.pitchshift
ptolemy.actor.lib.reactable
ptolemy.actor.lib.tutorial
ptolemy.backtrack.automatic.ptolemy.math
ptolemy.backtrack.eclipse.plugin.actions.codestyle
ptolemy.backtrack.eclipse.plugin.compatibility
ptolemy.backtrack.eclipse.plugin.console
ptolemy.backtrack.eclipse.plugin.dialogs
ptolemy.backtrack.eclipse.plugin.widgets
ptolemy.backtrack.test.array1
ptolemy.backtrack.test.random1
ptolemy.backtrack.test.test1
ptolemy.backtrack.test.test2
ptolemy.backtrack.util.java.util
ptolemy.caltrop.ddi.util
ptolemy.chic
ptolemy.component
ptolemy.component.domains.ptinyos
ptolemy.copernicus.interpreted
ptolemy.copernicus.kernel.fragment
ptolemy.domains.gr.lib.experimental
ptolemy.domains.gr.lib.quicktime
ptolemy.domains.wireless.lib.network
ptolemy.domains.wireless.lib.network.mac
ptolemy.domains.wireless.lib.tinyOS
ptolemy.plot.servlet}

set currentDirectory [pwd]
test release-3.1 {Run svn status and look for files that should be checked in.  See ptII/adm/bin/svnignoreupdate for a script to fix this} {

    cd "$PTII"
    if {[glob -nocomplain {*.class}] != {}} {
	exec rm [glob -nocomplain {*.class}]
    } 
    puts "Removing doc/books/systems because building jnlp files creates files and directories."
    exec rm -rf doc/books/systems papers
    exec svn update doc/books/systems
    # Other places to update the book location: $PTII/adm/gen-X.Y/{makefile,ptIIX_Y_devel_setup_windows.xml,shortcutSpec.xml}
    set bookURL http://ptolemy.eecs.berkeley.edu/books/Systems/PtolemyII_DigitalV1_02.pdf
    puts "Getting the Ptolemy book $bookURL"
    exec -stderrok wget $bookURL
    exec mv PtolemyII_DigitalV1_02.pdf doc/books/systems/
    puts "Removing \$PTII/index.html and \$PTII/toc.htm, which can be created while exporting HTML for the book."
    exec rm -f index.html toc.htm

    # Remove hs_err_pid* files created by ScaleC crashing
    if {[glob -nocomplain {ptolemy/cg/lib/test/hs_err_pid*}] != {}} {
	puts "Removing hs_err_pid* files created by ScaleC crashing"
	foreach file [glob -nocomplain {ptolemy/cg/lib/test/hs_err_pid*}] { 
	    file delete $file
	}
    } 

    set result {}
    set status [exec svn status]
    set data [split $status "\n"]
    foreach line $data {
	# Skip directories in /demo/ because they were created by exporting MoML
	if [regexp {/demo/} $line] {
	    set fields [split $line " "]
	    set directory [lindex $fields [expr {[llength $fields] - 1}]]
	    if [file isdirectory $directory] {
		# puts "$directory is a directory"
	    } else {
		lappend $result $line
	    }
	} else {
	    # Skip junit3213853918795049946.properties
	    if {![regexp {junit.*.properties} $line]} {
	        lappend result "\n$line"
   	    }
	}
    }

    set result [lsort $result]
    set result1 \
{{
?       .maven} {
?       cobertura.ser} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.bmp} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.jpg} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.pgm} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.tif} {
?       ptolemy/configs/doc/ClassesIllustrated} {
?       ptolemy/matlab/META-INF} {
?       ptolemy/matlab/matlabLinux.jar} {
?       reports}}
    if { $result == $result1 } {
	puts "Result was:\n$result\nWhich is ok"
        set resultMessage {}
    } else {
	set result2 \
{{
!       adm/dists/README.txt} {
!       ptolemy/vergil/basic/layout/kieler/test/layoutPerformance.xml} {
!       ptolemy/vergil/basic/layout/kieler/test/layoutPerformance2.xml} {
?       .maven} {
?       cobertura.ser} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.bmp} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.jpg} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.pgm} {
?       ptolemy/actor/lib/jai/test/auto/PtolemyII.tif} {
?       ptolemy/actor/lib/jai/test/auto/file.png} {
?       ptolemy/actor/lib/test/cobertura.ser} {
?       ptolemy/configs/doc/ClassesIllustrated} {
?       ptolemy/vergil/basic/export/html/test/Butterfly.gif} {
?       ptserver/test/PtolemyServer.log} {
?       reports} {
?       vendors/misc/javamail/javax.mail.jar} {
M       lib/diva.jar}}
        if { $result == $result2 } {
	    puts "Result was:\n$result\nWhich is ok"
            set resultMessage {}
        } else {
	    puts "This test is annoying, it runs svn status on the build directory to
                look for files that are not checked in.  There are two states:
                1) Running \"cd \$PTII/adm/test; \$PTII/bin/ptjacl Release.tcl\" on a regular, non-nightly build machine
	        2) Running the same command by hand on the nightly build machine.
	        The result of (cd \$PTII; svn status) was:\n $result \
                \nWhich is not the same as the result on a regular, non-nightly build machine.
                $result1 \nHere's the diff:\n [diffText $result $result1] \
                or the command on the nightly build machine\n $result2 Here's the diff:\n [diffText $result $result2]
                The fix is to run (cd \$PTII; svn status)
                and for each modified file, either remove it, check it in or add it to svn:ignore by running
                \$PTII/adm/bin/svnignoreupdate in the directory above the file to be ignored or by using
                \"svn propertyset svn:ignore filename; svn commit -m 'Added filename to svn:ignore.'\""
	    set resultMessage $result
        }
    }
    list $resultMessage
} {{}}
cd "$currentDirectory"

test release-4.1 {Check for makefiles in directories that have a test/ directory, but the makefile does not list test in the DIRS = line} {
    exec make --no-print-directory --silent chktestdir
} {./ptolemy/backtrack/test/../makefile:DIRS =		$(PTBACKTRACK_ECLIPSE_DIR) automatic manual util xmlparser demo}


test release-5.1 {Check for models that have $PTII in them.  They should use $CLASSPATH so that they work with jar files.} {
    exec make --no-print-directory --silent dollarPTII
} {}
