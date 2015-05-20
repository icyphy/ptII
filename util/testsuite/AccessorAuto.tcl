# Run tests in the auto directory and reload any JSAccessors
#
# @Author: Christopher Brooks, based on auto.tcl by Edward A. Lee
#
# @Version: $Id: auto.tcl 71213 2015-01-07 03:12:09Z cxh $
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

# Parse and execute a moml file.
# Return the top level, which is a TypedCompositeActor.
proc parseAndExecute {momlFile} {
    set parser [java::new ptolemy.moml.MoMLParser]
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]
    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
    	    ptolemy.moml.filter.RemoveGraphicalClasses]

    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parseFile $momlFile]]

    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager

    $manager execute
    return $toplevel
}

# IBM JDK 1.4.2 requires the lsort?
foreach file [lsort [glob auto/*.xml]] {
    set relativeFilename \
	    [java::call ptolemy.util.StringUtilities substituteFilePrefix \
	    $PTII $file {$PTII}]

    puts "------------------ testing Accessor model $relativeFilename"
    test "Auto" "Automatic test in Acccessor model file $relativeFilename" {
	    # FIXME: we should use $relativeFilename here, but it
	    # might have backslashes under Windows, which causes no end
	    # of trouble.
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
        if [catch {set toplevel [parseAndExecute $file]} errMsg] {
	    $watchDog cancel
	    error $errMsg
        } else {
	    $watchDog cancel
   	}
        list {}
    } {{}}
    test "Auto-rerun" "Automatic test rerun in Accessor model file $file" {
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]
        set manager [[java::cast ptolemy.actor.CompositeActor $toplevel] getManager]
        if [catch {$manager execute} errMsg] {
	    $watchDog cancel
	    error $errMsg
        } else {
	    $watchDog cancel
   	}
	# Free up memory.
        #$toplevel setContainer [java::null]
	#set toplevel [java::null]
	#java::call System gc
	list {}
    } {{}}
    test "Auto-reload1-rerun" "Automatic test reload #1 rerun in Accessor model file $file" {
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]

        # Reload.  Uses toplevel from above.
        set entityList [$toplevel entityList [java::call Class forName org.terraswarm.accessor.JSAccessor]]
	for {set i 0} {$i < [$entityList size]} {incr i} {
	    set accessor [java::cast org.terraswarm.accessor.JSAccessor [$entityList get $i]]
            $accessor reload
        }

        if [catch {$manager execute} errMsg] {
	    $watchDog cancel
	    error $errMsg
        } else {
	    $watchDog cancel
   	}
	# Free up memory.
        #$toplevel setContainer [java::null]
	#set toplevel [java::null]
	#java::call System gc
	list {}
    } {{}}
    test "Auto-reload2-rerun" "Automatic test reload #2 rerun in Accessor model file $file" {
    	set timeout 200000
        puts "auto.tcl: Setting watchdog for [expr {$timeout / 1000}]\
                  seconds at [clock format [clock seconds]]"
	set watchDog [java::new ptolemy.util.test.WatchDog $timeout]

        # Reload.  Uses toplevel from above.
        set entityList [$toplevel entityList [java::call Class forName org.terraswarm.accessor.JSAccessor]]
	for {set i 0} {$i < [$entityList size]} {incr i} {
	    set accessor [java::cast org.terraswarm.accessor.JSAccessor [$entityList get $i]]
            $accessor reload
        }

        if [catch {$manager execute} errMsg] {
	    $watchDog cancel
	    error $errMsg
        } else {
	    $watchDog cancel
   	}
	# Free up memory.
        $toplevel setContainer [java::null]
	set toplevel [java::null]
	java::call System gc
	list {}
    } {{}}
}


# Print out stats
doneTests
