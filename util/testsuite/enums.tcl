# Utilities for dealing with Java enumerations from Tcl
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
#### arrayToNames
# Return a list of the names of the objects in the argument, which is an array.
# These objects are assumed to implement the Nameable interface.  If any
# object in the list does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc arrayToNames {objarray} {
    set results {}
    for {set i 0} {$i < [$objarray length]} {incr i} {
	set obj [$objarray get $i]
        if [ java::instanceof $obj ptolemy.kernel.util.Nameable] {
            lappend results [[java::cast ptolemy.kernel.util.Nameable \
                    $obj] getName]
        } else {
            lappend results NOT_NAMEABLE.
        }
    }
    return $results
}

######################################################################
#### arrayToStrings
# Return a list of the objects in the argument, which is an array.
# The toString() method is used on each object.
#
proc arrayToStrings {objarray} {
    set results {}
    for {set i 0} {$i < [$objarray length]} {incr i} {
	set obj [$objarray get $i]
        lappend results $obj
    }
    return $results
}

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
#### enumToObjects
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
#### enumToStrings
# Return a list of strings obtained by invoking the toString method
# on the objects in the argument, which is an enumeration.
#
proc enumToStrings {enum} {
    return [objectsToStrings [enumToObjects $enum]]
}

######################################################################
#### enumToTokenValues
# Invoke enumToObjects followed by objectsToTokenValues.
#
proc enumToTokenValues {iter} {
    return [objectsToTokenValues [enumToObjects $iter]]
}

######################################################################
#### iterToList
# Convert an iteration to a list.  The list contains references
# to instances of Java Object.
#
proc iterToObjects {iter} {
    set results {}
    if {$iter != [java::null]} {
        while {[$iter hasNext] == 1} {
            lappend results [$iter -noconvert next]
	}
    }
    return $results
}

######################################################################
#### iterToTokenValues
# Invoke iterToObjects followed by objectsToTokenValues.
#
proc iterToTokenValues {iter} {
    return [objectsToTokenValues [iterToObjects $iter]]
}

######################################################################
#### listToObjects
# Convert a Java List to a Tcl list.  The list contains references
# to instances of Java Object.
#
proc listToObjects {list} {
    if {$list != [java::null]} {
        return [iterToObjects [$list iterator]]
    } else {
        return {}
    }
}

######################################################################
#### listToNames
# Return a list of the names of the objects in the specified Java List.
# These objects are assumed to implement the Nameable interface.  If any
# object in the enumeration does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc listToNames {list} {
    return [objectsToNames [listToObjects $list]]
}

######################################################################
#### listToFullNames
# Return a list of the full names of the objects in the specified Java List.
# These objects are assumed to implement the Nameable interface.  If any
# object in the enumeration does not do this, then its name is reported as
# NOT_NAMEABLE.
#
proc listToFullNames {list} {
    return [objectsToFullNames [listToObjects $list]]
}

######################################################################
#### listToStrings
# Return a list of strings obtained by invoking the toString method
# on the objects in the argument, which is an enumeration.
#
proc listToStrings {list} {
    return [objectsToStrings [listToObjects $list]]
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
        if [ java::instanceof $obj ptolemy.kernel.util.Nameable] {
            lappend results [[java::cast ptolemy.kernel.util.Nameable \
                    $obj] getName]
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
        if [ java::instanceof $obj ptolemy.kernel.util.Nameable] {
            lappend results [[java::cast ptolemy.kernel.util.Nameable $obj] \
                    getFullName]
        } else {
            lappend results NOT_NAMEABLE.
        }
    }
    return $results
}

######################################################################
#### objectsToStrings
# Return a list of strings obtained by invoking the toString method
# on the objects in the argument, which is a list.
#
proc objectsToStrings {objlist} {
    set results {}
    foreach obj $objlist {
        lappend results [$obj toString]
    }
    return $results
}

######################################################################
#### objectsToTokenValues
# Return a list of the token values of the objects in the argument,
# which is a list.
# These objects are assumed to be instances of Token.  If any
# object in the list is not, then its value is reported as
# NOT_A_TOKEN.
#
proc objectsToTokenValues {objlist} {
    set results {}
    foreach obj $objlist {
        if [ java::instanceof $obj ptolemy.data.Token] {
            lappend results [[java::cast ptolemy.data.Token \
                    $obj] toString]
        } else {
            lappend results NOT_A_TOKEN.
        }
    }
    return $results
}
