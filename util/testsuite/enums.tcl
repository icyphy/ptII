# Utilities for dealing with Java enumerations from Tcl
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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

# This file contains Tcl procs for accessing Java enumerations.
#
# Eventually, these procs should use namespaces or be in a Itcl class.
# 

######################################################################
#### enumMethodToNames
# Invoke the first argument (the name of a method that takes no arguments)
# for each of the objects given by remaining arguments.  The result is
# assumed to be a list of lists of objects with names.
# The list of lists of names of these objects is returned.
# If any object encountered does not implement the Nameable interface,
# then its name is reported as NOT_NAMEABLE.
#
proc enumMethodToNames {enummethod args} {
    eval mapProc enumToNames [eval mapMethod $enummethod $args]
}

######################################################################
#### enumToList
# Convert an enumeration to a list.  The list contains references
# to instances of Java Object.
#
proc enumToObjects {enum} {
    set results {}
    if {$enum != [java::null]} {
        while {[$enum hasMoreElements] == 1} {
            lappend results [$enum nextElement]
	}
    }
    return $results
}

######################################################################
#### enumToNames
# Return a list of the names of the objects represented by the enumeration
# given as an argument.  This is equivalent to invoking enumToObjects followed
# by objectsToNames.
# These objects are assumed to implement the Nameable interface.  If any
# object in the enumeration does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc enumToNames {enum} {
    return [objectsToNames [enumToObjects $enum]]
}

######################################################################
#### enumToFullNames
# Return a list of the full names of the objects represented by the enumeration
# given as an argument.  This is equivalent to invoking enumToObjects followed
# by objectsToFullNames.
# These objects are assumed to implement the Nameable interface.  If any
# object in the enumeration does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc enumToFullNames {enum} {
    return [objectsToFullNames [enumToObjects $enum]]
}

######################################################################
#### mapMethod
# Invoke the first argument (the name of a method that takes no arguments)
# for each of the objects given by remaining arguments.
# The list of results is returned.
#
proc mapMethod {methodname args} {
    set results {}
    foreach arg $args {
        lappend results [$arg $methodname]
    }
    return $results
}

######################################################################
#### mapProc
# Apply the first argument (the name of a proc that takes one argument)
# to each of the items given by remaining arguments.
# The list of results is returned.
#
proc mapProc {procname args} {
    set results {}
    foreach arg $args {
        lappend results [$procname $arg]
    }
    return $results
}

######################################################################
#### objectsToNames
# Return a list of the names of the objects in the argument, which is a list.
# These objects are assumed to implement the Nameable interface.  If any
# object in the list does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc objectsToNames {objlist} {
    set results {}
    foreach obj $objlist {
        if [ java::instanceof $obj pt.kernel.Nameable] {
            lappend results [$obj getName]
        } else {
            lappend results NOT_NAMEABLE.
        }
    }
    return $results
}

######################################################################
#### objectsToFullNames
# Return a list of the full names of the objects
# in the argument, which is a list.
# These objects are assumed to implement the Nameable interface.  If any
# object in the list does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc objectsToFullNames {objlist} {
    set results {}
    foreach obj $objlist {
        if [ java::instanceof $obj pt.kernel.Nameable] {
            lappend results [$obj getFullName]
        } else {
            lappend results NOT_NAMEABLE.
        }
    }
    return $results
}
