# Emacs mode: -*- tcl -*-
# The line above should always be the first line so Emacs is in the right mode.

# Sample .tclshrc file
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

# This is a sample ~/.tclshrc file.
# The master version is at $TYCHO/java/pt/kernel/test/tclshrc.tcl
# To use this file, either copy it to ~/.tclshrc or
# make a link:
#    cd ~
#    ln -s $TYCHO/java/pt/kernel/test/tclshrc.tcl .tclshrc
#    ln -s $TYCHO/java/pt/kernel/test/tclshrc.tcl .wishrc
#

puts "Sourcing $tcl_rcFileName"

# If $TYCHO is not set, then try to set it.
if [info exists env(TYCHO)] {
    set TYCHO $env(TYCHO)
} else { 
    if [info exist env(PTOLEMY)] {
	set TYCHO $env(PTOLEMY)/tycho
    }

    if [info exist env(TYCHO)] {
	set TYCHO $env(TYCHO)
    }

    if {![info exist TYCHO]} {
	# If we are here, then we are probably running jacl and we can't
	# read environment variables
	set TYCHO [file join [pwd] .. .. .. ..]
    }
}


if [info exists env(PTII)] {
    set PTII $env(PTII)
} else { 
    if [file exists [file join $TYCHO java makefile]] {
	set PTII [file join $TYCHO java]
	set env(PTII) $PTII
    }
}

# Source the file that contains the Tcl Blend helper procs
if [file exists [file join $PTII util testsuite init.tcl]] {
    source [file join $PTII util testsuite init.tcl]
}

puts "run ::jdk::init to load Tcl Blend. Current binary: $argv0"


#set tclblend_verbose jni,class
if [catch {::jdk::init} errmsg] {
    puts "ERROR: $errmsg"
}
