# procs useful for Tcl Blend
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# This file contains procs that may be useful when using Tcl Blend.

namespace eval ::jdk {}

######################################################################
#### ::jdk::help
# Print a help message about the::jdk package 
#
proc ::jdk::help {} {
	puts "\
The::jdk Tcl package contains utility procs that are useful with Tcl Blend \n\
 ::jdk::init { {version 1.0}} \n\
    Print debugging information, then load Tcl Blend. \n\
 ::jdk::version {} \n\
    Print::JDK and Tcl interpreter version information. \n\
 ::jdk::properties  \n\
    Print out the JVM System properties \n\
"
}

######################################################################
####::jdk::init
# This proc is useful in debugging Tcl Blend package loading problems.  
# It first prints the CLASSPATH, PATH and auto_path variables.
# Then the Tcl Blend package is loaded
# If the load is successful, information about the::JDK and Tcl interpreters
# is printed.
#::jdkinit takes one optional argument, which is the version of
# the java package to load.  The default is 1.0
#
proc ::jdk::init { {version 1.0}} {
    global tcl_version auto_path env

    puts "-->Starting ::jdk::init"
    set envvars [list CLASSPATH LD_LIBRARY_PATH SHLIB_PATH PATH]
    foreach envvar $envvars {
	if [info exist env($envvar)] {
	    puts [format "%16s: %s" $envvar $env($envvar)]
	} else { 
	    puts [format "%16s: %s" $envvar "is not set"]
	}
    }

    puts [format "%16s: %s" "auto_path" $auto_path]

    # tclblend_init only works with::JDK1.2 and Tcl Blend 1.0 up2
    #set tclblend_init "-verbose:jni,class"

    puts "-->Initializing java"
    puts [package require java]
    puts "-->  Initialized Java"
    ::jdk::version
}


######################################################################
#### ::jdk::version
# Print information about the Java and Tcl interpreters.
#
proc ::jdk::version {} {
    global env tcl_version
    puts "env(CLASSPATH):   $env(CLASSPATH)\n
            java.class.path property:\
            [java::call System getProperty "java.class.path"]\n"
    puts "jdk version: [java::call System getProperty "java.version"] \
            tcl version: $tcl_version \
            java package: [package versions java]"
    puts "info loaded: [info loaded]"   
}


######################################################################
####::jdk::properties
# Print the value of the Java System Properties
#
proc ::jdk::properties {} {
    set props [list java.version java.vendor java.vendor.url java.home \
	    java.class.version java.class.path os.name os.arch os.version \
	    file.separator path.separator line.separator \
	    user.name user.home user.dir]
    foreach prop $props {
	puts "$prop: [java::call System getProperty $prop]"
    }
    
}


######################################################################
#### ::jdk::getStackTrace
# Return the stack trace from the e Exception.
#
# Below is an example:
#<tcl><pre>
# # Create a String
# set s [java::new {String java.lang.String} "123"]
# # Try to get a character beyond the end of the array
# catch {$s charAt 4} err
# puts "The error was:\n$err"
# # Get the reference to the exception
# set e [lindex $errorCode 1]
# puts "The stack was:\n[getStackTrace $e]"
#</pre></tcl>
#
proc ::jdk::getStackTrace {exception} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printWriter [java::new \
	    {java.io.PrintWriter java.io.OutputStream} $stream]
    $exception {printStackTrace java.io.PrintWriter} $printWriter
    $printWriter flush
    return [$stream toString]
}


######################################################################
#### ::jdk::test
# Run all the procs in the::jdk namespace
#
proc ::jdk::test {} {

    # Test all the simple procs
    set testprocs "help init properties"
    foreach testproc $testprocs {
	puts "### Now running $testproc" 
	$testproc
    }

    puts "### Now testing getStackTrace"
    # Create a String
    set s [java::new {String java.lang.String} "123"]
    # Try to get a character beyond the end of the array
    catch {$s charAt 4} err
    puts "### The error was:\n$err"
    # Get the reference to the exception
    global errorCode
    set e [lindex $errorCode 1]
    puts "### The stack was:\n[getStackTrace $e]"

}



