# Tests for the ASTReflect class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

# Sort a list by sorting each sublist as well.
# Given a nested list like
#                    {a c b {{f {n m} p} {d e} g} z}  
# return             {a b c z {{d e} {f {m n} p} g}}
# lsort would return {a b c z {{f {n m} p} {d e} g}}
proc deeplsort {list} {
    puts "deeplsort($list)"
    # If all the elements in a list are leaves [llength <= 1], then
    # call lsort on the list
    set allLeaves 1
    foreach element $list {
	if {[llength $element] > 1} {
	    set allLeaves 0
	    break
	}
    }
    if {$allLeaves == 1} {
	puts "calling lsort $list"
	return [lsort $list]
    }
    
    # If any elements have a length greather than 1, then call deepsort on
    # each element that has a length greater than 1 and create a new list
    # and then call lsort on the new list
    foreach element $list {
	if {[llength $element] > 1} {
	    lappend newlist [deeplsort $element]
	} else {
	    lappend newlist $element
	}
    }
    return [lsort $newlist] 
}



# Compare two deeply sorted lists
# If the lists are the same return 1
# If the lists are not the same, try returning a useful message
proc deeplcompare {lista listb {indent ""}} {
    if {[llength $lista] != [llength $listb]} {
	return "The two lists are not the same length '[llength $lista]' != '[llength $listb]'\n$lista\n$listb"} {
    }
    for {set i 0} {$i< [llength $lista]} {incr i} {
	if { [lindex $lista $i ] != [lindex $listb $i ]} {
	    set a [lindex $lista $i]
	    set b [lindex $listb $i]
	    append indent " "
	    if {[llength $a] > 1 ||  [llength $b] > 1} {
		return "$indent checking '$a' '$b' \n [deeplcompare $a $b $indent]"
	    } else {
		return "$indent $a is not equal to $b"
	    }
	}
    }
}

proc lcompare {lista listb} {
    if {$lista == $listb } {
	return 1
    }
    return [deeplcompare [deeplsort $lista] [deeplsort $listb]]
}
#set a {a c b {{f {n m} p} {d e} g} z}  
#set b {a c b {{f {n m} q} {d e} g} z}  
#
#puts [lcompare $a $b]

######################################################################
####
#
test ASTReflect-2.1 {check out constructor in Object} {
    set class [ java::call Class forName "java.lang.Object"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    list [listToStrings $astList]
} {{{ {ConstructorDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident Object} 
       }}} 
  {Modifiers 1} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}}}}

######################################################################
####
#
test ASTReflect-2.2 {check out constructors} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    list [listToStrings $astList]
} {{{ {ConstructorDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident ReflectTest} 
       }}} 
  {Modifiers 0} 
  {Params { 
   {ParameterNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident } 
         }}} 
    {Modifiers 25} 
    {DefType {IntTypeNode {leaf}}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}} { {ConstructorDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident ReflectTest} 
       }}} 
  {Modifiers 0} 
  {Params { 
   {ParameterNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident } 
         }}} 
    {Modifiers 25} 
    {DefType {ArrayTypeNode { 
              {BaseType {ArrayTypeNode { 
                         {BaseType {IntTypeNode {leaf}}} 
                       }}} 
            }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}} { {ConstructorDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident ReflectTest} 
       }}} 
  {Modifiers 0} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {BlockNode { 
         {Stmts  {}} 
       }}} 
  {ConstructorCall {SuperConstructorCallNode { 
                    {Args  {}} 
                  }}} 
}}}}}

######################################################################
####
#
test ASTReflect-3.1 {check out fields} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTestFields"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    list [listToStrings $astList]
} {{{ {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myBoolean} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Boolean} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myCharacter} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Character} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myByte} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Byte} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myShort} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Short} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myInteger} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Integer} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myLong} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Long} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myFloat} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Float} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident myDouble} 
       }}} 
  {Modifiers 1} 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Qualifier {AbsentTreeNode {leaf}}} 
                   {Ident Double} 
                 }}} 
          }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myBoolean} 
       }}} 
  {Modifiers 2} 
  {DefType {BoolTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myCharacter} 
       }}} 
  {Modifiers 2} 
  {DefType {CharTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myByte} 
       }}} 
  {Modifiers 2} 
  {DefType {ByteTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myShort} 
       }}} 
  {Modifiers 2} 
  {DefType {ShortTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myInteger} 
       }}} 
  {Modifiers 2} 
  {DefType {IntTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myLong} 
       }}} 
  {Modifiers 2} 
  {DefType {LongTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myFloat} 
       }}} 
  {Modifiers 2} 
  {DefType {FloatTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident _myDouble} 
       }}} 
  {Modifiers 2} 
  {DefType {DoubleTypeNode {leaf}}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}}}}

######################################################################
####
#
test ASTReflect-4.1 {check out innerclasses} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect innerClassesASTList $class]
    list [listToStrings $astList]
} {{{ {ClassDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident ReflectTest$innerPublicClass} 
       }}} 
  {Interfaces  {}} 
  {Modifiers 1} 
  {Members { 
   {ConstructorDeclNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident ReflectTest$innerPublicClass} 
         }}} 
    {Modifiers 0} 
    {Params { 
     {ParameterNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident } 
           }}} 
      {Modifiers 1} 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Qualifier {AbsentTreeNode {leaf}}} 
                       {Ident ReflectTest} 
                     }}} 
              }}} 
    }}}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}   {MethodDeclNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident innerPublicClassPublicMethod} 
         }}} 
    {Modifiers 1} 
    {Params { 
     {ParameterNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident } 
           }}} 
      {Modifiers 1} 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Qualifier {AbsentTreeNode {leaf}}} 
                       {Ident NamedObj} 
                     }}} 
              }}} 
    }}}} 
    {ThrowsList  {}} 
    {ReturnType {ArrayTypeNode { 
                 {BaseType {ArrayTypeNode { 
                            {BaseType {ArrayTypeNode { 
                                       {BaseType {IntTypeNode {leaf}}} 
                                     }}} 
                          }}} 
               }}} 
    {Body {AbsentTreeNode {leaf}}} 
  }}   {FieldDeclNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident this$0} 
         }}} 
    {Modifiers 20} 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Qualifier {AbsentTreeNode {leaf}}} 
                     {Ident ReflectTest} 
                   }}} 
            }}} 
    {InitExpr {AbsentTreeNode {leaf}}} 
  }}}} 
  {SuperClass {NameNode { 
               {Qualifier {NameNode { 
                           {Qualifier {NameNode { 
                                       {Qualifier {AbsentTreeNode {leaf}}} 
                                       {Ident java} 
                                     }}} 
                           {Ident lang} 
                         }}} 
               {Ident Object} 
             }}} 
}}}}}

######################################################################
####
#
test ASTReflect-5.1 {check out methods} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    list [listToStrings $astList]
} {{{ {MethodDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident publicMethod1} 
       }}} 
  {Modifiers 513} 
  {Params  {}} 
  {ThrowsList  {}} 
  {ReturnType {VoidTypeNode {leaf}}} 
  {Body {AbsentTreeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident publicMethod2} 
       }}} 
  {Modifiers 1} 
  {Params { 
   {ParameterNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident } 
         }}} 
    {Modifiers 25} 
    {DefType {ArrayTypeNode { 
              {BaseType {TypeNameNode { 
                         {Name {NameNode { 
                                {Qualifier {AbsentTreeNode {leaf}}} 
                                {Ident NamedObj} 
                              }}} 
                       }}} 
            }}} 
  }}}} 
  {ThrowsList  {}} 
  {ReturnType {VoidTypeNode {leaf}}} 
  {Body {AbsentTreeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident publicMethod2} 
       }}} 
  {Modifiers 1} 
  {Params { 
   {ParameterNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident } 
         }}} 
    {Modifiers 25} 
    {DefType {ArrayTypeNode { 
              {BaseType {ArrayTypeNode { 
                         {BaseType {TypeNameNode { 
                                    {Name {NameNode { 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                           {Ident NamedObj} 
                                         }}} 
                                  }}} 
                       }}} 
            }}} 
  }}}} 
  {ThrowsList  {}} 
  {ReturnType {VoidTypeNode {leaf}}} 
  {Body {AbsentTreeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Name {NameNode { 
         {Qualifier {AbsentTreeNode {leaf}}} 
         {Ident publicMethod2} 
       }}} 
  {Modifiers 1} 
  {Params { 
   {ParameterNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident } 
         }}} 
    {Modifiers 25} 
    {DefType {IntTypeNode {leaf}}} 
  }}}} 
  {ThrowsList  {}} 
  {ReturnType {VoidTypeNode {leaf}}} 
  {Body {AbsentTreeNode {leaf}}} 
}}}}}