# Run tests in the auto directory.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 2000-2015 The Regents of the University of California.
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

# Delete the ~/codegen/ and ~/cg/ directories
file delete -force $env(HOME)/codegen
file delete -force $env(HOME)/cg

if [ file isdirectory auto/knownFailedTests ] {
    foreach file [glob -nocomplain auto/knownFailedTests/*.xml] {
	# Get the name of the current directory relative to $PTII
	set relativeFilename \
		[java::call ptolemy.util.StringUtilities substituteFilePrefix \
		$PTII [file join [pwd] $file] {$PTII}]
	puts "------------------ testing $relativeFilename (Known Failure) "
	test "Auto" "Automatic test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
	    createAndExecute $file
	    list {}
	} {{}} {KNOWN_FAILURE}
    }
}

# Just in case the test fails before returning and setting the application.
set application [java::null]

# Run os.name and os.arch specific tests
regsub -all { } [string tolower [java::call System getProperty os.name]] "" osName
set osArch [java::call System getProperty os.arch]
set autoNameArch $osName-$osArch

# Names are macosx-x86_64, linux-amd64 etc.

if [ file isdirectory auto/$autoNameArch ] {
    foreach file [glob -nocomplain auto/$autoNameArch/*.xml] {
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $file {$PTII}]

    puts "------------------ testing $relativeFilename ($autoNameArch)"
    test "Auto" "Automatic test in file $relativeFilename ($autoNameArch)" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
        if [catch {set application [createAndExecute $file]} errMsg] {
	    $watchDog cancel
            error $errMsg
        } else {
	    $watchDog cancel
   	}
        list {}
    } {{}}
    test "Auto-rerun" "Automatic test rerun in file $file" {
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
        if {[java::isnull $application]} {
            error "application was null, there was a problem with the previous run."
        } else {
            set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
            if [catch {$application rerun} errMsg] {
                $watchDog cancel
                error $errMsg
            } else {
                $watchDog cancel
            }
        }
	list {}
    } {{}}
    }
}

if [ file isdirectory auto/nonTerminatingTests ] {
    foreach file [glob -nocomplain auto/nonTerminatingTests/*.xml] {
	# Get the name of the current directory relative to $PTII
	set relativeFilename \
		[java::call ptolemy.util.StringUtilities substituteFilePrefix \
		$PTII [file join [pwd] $file] {$PTII}]
	puts "------------------ testing $relativeFilename (Nonterminating) "
        test "Auto" "Automatic test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
    	    set timeout 10000
            puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	    set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
            if [catch {set application [java::new ptolemy.moml.MoMLSimpleTimeoutApplication $file]} errMsg] {
	        $watchDog cancel
                error $errMsg
            } else {
	        $watchDog cancel
   	    }
            list {}
        } {{}}
    }
}
# IBM JDK 1.4.2 requires the lsort?
foreach file [lsort [glob auto/*.xml]] {
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $file {$PTII}]

    puts "------------------ testing $relativeFilename"
    test "Auto" "Automatic test in file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
        if [catch {set application [createAndExecute $file]} errMsg] {
	    $watchDog cancel
            error $errMsg
        } else {
	    $watchDog cancel
   	}
        list {}
    } {{}}
    test "Auto-rerun" "Automatic test rerun in file $file" {
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
        if {[java::isnull $application]} {
            error "application was null, there was a problem with the previous run."
        } else {
            set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
            if [catch {$application rerun} errMsg] {
                $watchDog cancel
                error $errMsg
            } else {
                $watchDog cancel
            }
        }
	list {}
    } {{}}

    # If the JSAccessor class is present, and the model contains at
    # least on JSAccessor actor, then reload all the JSAccessors and
    # rerun the model.
    if {![catch {java::call Class forName org.terraswarm.accessor.JSAccessor} errMsg]} {

        if {[java::isnull $application]} {
            puts "application was null, there was a problem with the previous run, skipping reloading any accessors."
        } else {
            # Look for JSAccessor actors in the model.
            set toplevel [$application toplevel]
            set JSAccessorPresent 0
            set entityList [$toplevel allAtomicEntityList]
            for {set iterator [$entityList iterator]} {[$iterator hasNext] == 1} {} {
                set entity [$iterator next]
                if [java::instanceof $entity org.terraswarm.accessor.JSAccessor] {
                    set JSAccessorPresent 1
                }
            }

            # If a JSAccessor actor was found, then reload all the JSAccessors and rerun the model.
            if {$JSAccessorPresent} {
                # Skip reloading certain classes
                set toplevelName [[$application toplevel] getFullName]
                #if {[lsearch [list {.ContextAware}] $toplevelName] != -1} {
                #    puts "auto.tcl: Skipping reloading accessors in $toplevelName because util/testsuite/auto.tcl asked us to."
                #} else {
                    test "Auto-reload1-rerun" "Automatic test reload Accessors and rerun in model file $file" {
                        set timeout 200000
                        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}] seconds at [clock format [clock seconds]], then reloading accessor(s)"
                        set watchDog [java::new ptolemy.util.test.WatchDog $timeout]

                        set toplevel [$application toplevel]
                        java::call org.terraswarm.accessor.JSAccessor reloadAllAccessors $toplevel

                        if [catch {$application rerun} errMsg] {
                            $watchDog cancel
                            puts "While relading the accessors for [$toplevel getFullName], the rerun failed."
                            puts "This can happen if the model changes the default values after loading the accessor"
                            puts "Stack trace was: [jdkStackTrace]"
                            error $errMsg
                        } else {
                            $watchDog cancel
                        }
                        list {}
                    } {{}}
                #}
            }
        }
    }

    # Free up memory.
    if {![java::isnull $application]} {
        $application cleanup
        set application [java::null]
    }
    java::call System gc
}

# Print out stats
doneTests
