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

# Return the depth of a list.  Note that tcl has some strange
# ideas about lists with single elements:
# list a returns a
# list [list a] returns a
# so, the depth of a list like {a {b}} is 1
proc ldepth {list {count 0}} {
    incr count
    set greatestDepth $count
    foreach element $list {
	set subelement $element
	if {$subelement != {} && $subelement != [lindex $subelement 0]} {
	    set temp [ldepth $subelement $count]
	    if {$greatestDepth < $temp} {
		set greatestDepth $temp
	    }
	} 
    }
    return $greatestDepth
}

# Routine to test ldepth proc
proc test_ldepth {} {
    # Test case and expected results
    set cases {
	{ {} 1}
	{ {a} 1}
	{ {{a2}} 1}
	{ {{{a3}}} 2}
	{ {{{{a3}}}} 3}
	{ {a4} 1}
	{ {a b} 1}
	{ {a {b}} 1}
	{ {a {{b}}} 2}
	{ {a {{{b}}}} 3}
	{ {a b c} 1}
	{ {a {b} c} 1}
	{ {a {b c} d} 2}
	{ {a {b {c}} d} 2}
	{{a {b c} d {e {f g {h i}}}} 4}
    }
    foreach case $cases {
	if {[ldepth [lindex $case 0]] != [lindex $case 1]} {
	    puts "'ldepth [lindex $case 0]' is [ldepth [lindex $case 0]] != [lindex $case 1]"
	}
    }
}

# Uncomment these lines to run the test for ldepth
#test_ldepth
#exit

# Sort a list by sorting each sublist as well.
# Given a nested list like
#                    {a c b {{f {n m} p} {d e} g} z}  
# return             {a b c z {{d e} {f {m n} p} g}}
# lsort would return {a b c z {{f {n m} p} {d e} g}}
proc deeplsort {list} {
    # This is _really_ slow under jacl because it uses recursion,
    # so we print dots to let the user know what is up
    puts -nonewline "."
    # If all the elements in a list are leaves [llength <= 1], then
    # call lsort on the list
    set allLeaves 1
    foreach element $list {
	if {[ldepth $element] > 1} {
	    set allLeaves 0
	    break
	}
    }
    if {$allLeaves == 1} {
	return [lsort $list]
    }
    
    # If any elements have a length greather than 1, then call deepsort on
    # each element that has a length greater than 1 and create a new list
    # and then call lsort on the new list
    foreach element $list {
	if {[ldepth $element] > 1} {
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
	    if {[ldepth $a] > 1 ||  [ldepth $b] > 1} {
		return "$indent checking '$a' '$b' \n [deeplcompare $a $b $indent]"
	    } else {
		return "$indent '$a' is not equal to '$b'"
	    }
	}
    }
    return 1
}

# Compare two lists.  If == says that they are not equal, then
# deeply sort the lists and then deeply compare then
proc lcompare {lista listb} {
    if {$lista == $listb } {
	return 1
    }
    puts -nonewline "lcompare: Doing deeplcompare "
    set deepsortedlista [deeplsort $lista]
    set deepsortedlistb [deeplsort $listb]
    puts ""
    return [deeplcompare $deepsortedlista $deepsortedlistb] 
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
    lcompare [listToStrings $astList] \
{{ {ConstructorDeclNode { 
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
}}}}
} {1}

######################################################################
####
#
test ASTReflect-2.2 {check out constructors} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    lcompare [listToStrings $astList] \
{{ {ConstructorDeclNode { 
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
}}}}
} {1}

######################################################################
####
#
test ASTReflect-3.1 {check out fields} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTestFields"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    lcompare [listToStrings $astList] \
{{ {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Boolean} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myBoolean} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Character} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myCharacter} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Byte} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myByte} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Short} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myShort} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Integer} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myInteger} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Long} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myLong} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Float} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myFloat} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {TypeNameNode { 
            {Name {NameNode { 
                   {Ident Double} 
                   {Qualifier {NameNode { 
                               {Ident lang} 
                               {Qualifier {NameNode { 
                                           {Ident java} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                 }}} 
          }}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident myDouble} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {BoolTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myBoolean} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {CharTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myCharacter} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {ByteTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myByte} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {ShortTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myShort} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {IntTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myInteger} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {LongTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myLong} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {FloatTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myFloat} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}} { {FieldDeclNode { 
  {DefType {DoubleTypeNode {leaf}}} 
  {Modifiers 2} 
  {Name {NameNode { 
         {Ident _myDouble} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}   }   }   }   
} {1}

######################################################################
####
#
test ASTReflect-4.1 {check out innerclasses} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect innerClassesASTList $class]
    lcompare [listToStrings $astList] \
{{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident ReflectTest$innerPublicClass} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params { 
     {ParameterNode { 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Ident ReflectTest} 
                       {Qualifier {NameNode { 
                                   {Ident test} 
                                   {Qualifier {NameNode { 
                                               {Ident java} 
                                               {Qualifier {NameNode { 
                                                           {Ident lang} 
                                                           {Qualifier {NameNode { 
                                                                       {Ident ptolemy} 
                                                                       {Qualifier {AbsentTreeNode {leaf}}} 
                                                                     }}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
              }}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident } 
             {Qualifier {AbsentTreeNode {leaf}}} 
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
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident innerPublicClassPublicMethod} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params { 
     {ParameterNode { 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Ident NamedObj} 
                       {Qualifier {NameNode { 
                                   {Ident util} 
                                   {Qualifier {NameNode { 
                                               {Ident kernel} 
                                               {Qualifier {NameNode { 
                                                           {Ident ptolemy} 
                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
              }}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident } 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
    }}}} 
    {ThrowsList  {}} 
    {Body {AbsentTreeNode {leaf}}} 
    {ReturnType {ArrayTypeNode { 
                 {BaseType {ArrayTypeNode { 
                            {BaseType {ArrayTypeNode { 
                                       {BaseType {IntTypeNode {leaf}}} 
                                     }}} 
                          }}} 
               }}} 
  }}   {FieldDeclNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident ReflectTest} 
                     {Qualifier {NameNode { 
                                 {Ident test} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {NameNode { 
                                                         {Ident lang} 
                                                         {Qualifier {NameNode { 
                                                                     {Ident ptolemy} 
                                                                     {Qualifier {AbsentTreeNode {leaf}}} 
                                                                   }}} 
                                                       }}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 20} 
    {Name {NameNode { 
           {Ident this$0} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {InitExpr {AbsentTreeNode {leaf}}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ReflectTest$innerPublicClass} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}}
} {1} 

######################################################################
####
#
test ASTReflect-5.1 {check out methods} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 513} 
  {Name {NameNode { 
         {Ident publicMethod1} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident publicMethod2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident publicMethod2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {TypeNameNode { 
                         {Name {NameNode { 
                                {Ident NamedObj} 
                                {Qualifier {NameNode { 
                                            {Ident util} 
                                            {Qualifier {NameNode { 
                                                        {Ident kernel} 
                                                        {Qualifier {NameNode { 
                                                                    {Ident ptolemy} 
                                                                    {Qualifier {AbsentTreeNode {leaf}}} 
                                                                  }}} 
                                                      }}} 
                                          }}} 
                              }}} 
                       }}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident publicMethod2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {ArrayTypeNode { 
                         {BaseType {TypeNameNode { 
                                    {Name {NameNode { 
                                           {Ident NamedObj} 
                                           {Qualifier {NameNode { 
                                                       {Ident util} 
                                                       {Qualifier {NameNode { 
                                                                   {Ident kernel} 
                                                                   {Qualifier {NameNode { 
                                                                               {Ident ptolemy} 
                                                                               {Qualifier {AbsentTreeNode {leaf}}} 
                                                                             }}} 
                                                                 }}} 
                                                     }}} 
                                         }}} 
                                  }}} 
                       }}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}   }   }   }
} {1}

######################################################################
####
#
test ASTReflect-6.1 {check out Anonymous classes} {
    set class [ java::call Class forName "ptolemy.lang.java.test.Anon"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident foo} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ugh} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-7.1 {check out Array Length} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ArrayLength"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] {
{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ack} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident Cloneable} 
                     {Qualifier {NameNode { 
                                 {Ident lang} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 9} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident oof} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {DoubleTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident String} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident oof} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident String} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident ugh} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident Object} 
                     {Qualifier {NameNode { 
                                 {Ident lang} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {DoubleTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident yo} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {ArrayTypeNode { 
              {BaseType {DoubleTypeNode {leaf}}} 
            }}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
#test ASTReflect-8.1 {check out characters - ignored - signature is unchanged } {
#    set class [ java::call Class forName "ptolemy.lang.java.test.CharTest"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    list [listToStrings $astList]
#} {1}

######################################################################
####
#
test ASTReflect-9.1 {check out class access} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ClassAccess"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 512} 
  {Name {NameNode { 
         {Ident class$} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {TypeNameNode { 
              {Name {NameNode { 
                     {Ident String} 
                     {Qualifier {NameNode { 
                                 {Ident lang} 
                                 {Qualifier {NameNode { 
                                             {Ident java} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                   }}} 
            }}} 
    {Modifiers 17} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Class} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}}
} {1}

######################################################################
####
#
#test ASTReflect-10.1 {check out exceptions - ignored - signature is unchanged } {
#    set class [ java::call Class forName "ptolemy.lang.java.test.ExceptionTest"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    list [listToStrings $astList]
#} {}

######################################################################
####
#
#test ASTReflect-11.1 {check out for loops - ignored - signature is unchanged } {
#    set class [ java::call Class forName "ptolemy.lang.java.test.For"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    list [listToStrings $astList]
#} {}
######################################################################
####
#

test ASTReflect-12.1 {check out interfaces} {
    set class [ java::call Class forName "ptolemy.lang.java.test.IFace"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {Pkg {NameNode { 
        {Qualifier {NameNode { 
                    {Qualifier {NameNode { 
                                {Qualifier {NameNode { 
                                            {Qualifier {AbsentTreeNode {leaf}}} 
                                            {Ident ptolemy} 
                                          }}} 
                                {Ident lang} 
                              }}} 
                    {Ident java} 
                  }}} 
        {Ident test} 
      }}} 
  {Imports  {}} 
  {DefTypes { 
   {InterfaceDeclNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident IFace} 
         }}} 
    {Interfaces  {}} 
    {Modifiers 8} 
    {Members  {}} 
  }}}} 
}   }   }   } {1}


######################################################################
####
#
test ASTReflect-13.1 {check out inner classes} {
    set class [ java::call Class forName "ptolemy.lang.java.test.InnerClass"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {Pkg {NameNode { 
        {Qualifier {NameNode { 
                    {Qualifier {NameNode { 
                                {Qualifier {NameNode { 
                                            {Qualifier {AbsentTreeNode {leaf}}} 
                                            {Ident ptolemy} 
                                          }}} 
                                {Ident lang} 
                              }}} 
                    {Ident java} 
                  }}} 
        {Ident test} 
      }}} 
  {Imports  {}} 
  {DefTypes { 
   {ClassDeclNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident InnerClass} 
         }}} 
    {Interfaces  {}} 
    {Modifiers 0} 
    {Members { 
     {ConstructorDeclNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident InnerClass} 
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
    }}     {FieldDeclNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident x} 
           }}} 
      {Modifiers 1} 
      {DefType {IntTypeNode {leaf}}} 
      {InitExpr {AbsentTreeNode {leaf}}} 
    }}     {ClassDeclNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident InnerClass$InnerInnerClass} 
           }}} 
      {Interfaces  {}} 
      {Modifiers 1} 
      {Members { 
       {ConstructorDeclNode { 
        {Name {NameNode { 
               {Qualifier {AbsentTreeNode {leaf}}} 
               {Ident InnerClass$InnerInnerClass} 
             }}} 
        {Modifiers 1} 
        {Params { 
         {ParameterNode { 
          {Name {NameNode { 
                 {Qualifier {AbsentTreeNode {leaf}}} 
                 {Ident } 
               }}} 
          {Modifiers 0} 
          {DefType {TypeNameNode { 
                    {Name {NameNode { 
                           {Qualifier {NameNode { 
                                       {Qualifier {NameNode { 
                                                   {Qualifier {NameNode { 
                                                               {Qualifier {NameNode { 
                                                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                                                           {Ident ptolemy} 
                                                                         }}} 
                                                               {Ident lang} 
                                                             }}} 
                                                   {Ident java} 
                                                 }}} 
                                       {Ident test} 
                                     }}} 
                           {Ident InnerClass} 
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
      }}       {MethodDeclNode { 
        {Name {NameNode { 
               {Qualifier {AbsentTreeNode {leaf}}} 
               {Ident meth} 
             }}} 
        {Modifiers 1} 
        {Params  {}} 
        {ThrowsList  {}} 
        {ReturnType {VoidTypeNode {leaf}}} 
        {Body {AbsentTreeNode {leaf}}} 
      }}       {FieldDeclNode { 
        {Name {NameNode { 
               {Qualifier {AbsentTreeNode {leaf}}} 
               {Ident _y} 
             }}} 
        {Modifiers 2} 
        {DefType {IntTypeNode {leaf}}} 
        {InitExpr {AbsentTreeNode {leaf}}} 
      }}       {FieldDeclNode { 
        {Name {NameNode { 
               {Qualifier {AbsentTreeNode {leaf}}} 
               {Ident this$0} 
             }}} 
        {Modifiers 20} 
        {DefType {TypeNameNode { 
                  {Name {NameNode { 
                         {Qualifier {NameNode { 
                                     {Qualifier {NameNode { 
                                                 {Qualifier {NameNode { 
                                                             {Qualifier {NameNode { 
                                                                         {Qualifier {AbsentTreeNode {leaf}}} 
                                                                         {Ident ptolemy} 
                                                                       }}} 
                                                             {Ident lang} 
                                                           }}} 
                                                 {Ident java} 
                                               }}} 
                                     {Ident test} 
                                   }}} 
                         {Ident InnerClass} 
                       }}} 
                }}} 
        {InitExpr {AbsentTreeNode {leaf}}} 
      }}}} 
      {SuperClass {TypeNameNode { 
                   {Name {NameNode { 
                          {Qualifier {NameNode { 
                                      {Qualifier {NameNode { 
                                                  {Qualifier {AbsentTreeNode {leaf}}} 
                                                  {Ident java} 
                                                }}} 
                                      {Ident lang} 
                                    }}} 
                          {Ident Object} 
                        }}} 
                 }}} 
    }}}} 
    {SuperClass {TypeNameNode { 
                 {Name {NameNode { 
                        {Qualifier {NameNode { 
                                    {Qualifier {NameNode { 
                                                {Qualifier {AbsentTreeNode {leaf}}} 
                                                {Ident java} 
                                              }}} 
                                    {Ident lang} 
                                  }}} 
                        {Ident Object} 
                      }}} 
               }}} 
  }}}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-14.1 {check out } {
    set class [ java::call Class forName "ptolemy.lang.java.test.InnerIFace"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {Pkg {NameNode { 
        {Qualifier {NameNode { 
                    {Qualifier {NameNode { 
                                {Qualifier {NameNode { 
                                            {Qualifier {AbsentTreeNode {leaf}}} 
                                            {Ident ptolemy} 
                                          }}} 
                                {Ident lang} 
                              }}} 
                    {Ident java} 
                  }}} 
        {Ident test} 
      }}} 
  {Imports  {}} 
  {DefTypes { 
   {ClassDeclNode { 
    {Name {NameNode { 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Ident InnerIFace} 
         }}} 
    {Interfaces  {}} 
    {Modifiers 0} 
    {Members { 
     {ConstructorDeclNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident InnerIFace} 
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
    }}     {ClassDeclNode { 
      {Name {NameNode { 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Ident InnerIFace$IFace} 
           }}} 
      {Interfaces  {}} 
      {Modifiers 520} 
      {Members { 
       {MethodDeclNode { 
        {Name {NameNode { 
               {Qualifier {AbsentTreeNode {leaf}}} 
               {Ident ack} 
             }}} 
        {Modifiers 9} 
        {Params  {}} 
        {ThrowsList  {}} 
        {ReturnType {IntTypeNode {leaf}}} 
        {Body {AbsentTreeNode {leaf}}} 
      }}       {MethodDeclNode { 
        {Name {NameNode { 
               {Qualifier {AbsentTreeNode {leaf}}} 
               {Ident ugh} 
             }}} 
        {Modifiers 9} 
        {Params { 
         {ParameterNode { 
          {Name {NameNode { 
                 {Qualifier {AbsentTreeNode {leaf}}} 
                 {Ident } 
               }}} 
          {Modifiers 25} 
          {DefType {CharTypeNode {leaf}}} 
        }}}} 
        {ThrowsList  {}} 
        {ReturnType {CharTypeNode {leaf}}} 
        {Body {AbsentTreeNode {leaf}}} 
      }}}} 
      {SuperClass {TypeNameNode { 
                   {Name {NameNode { 
                          {Qualifier {AbsentTreeNode {leaf}}} 
                          {Ident Object} 
                        }}} 
                 }}} 
    }}}} 
    {SuperClass {TypeNameNode { 
                 {Name {NameNode { 
                        {Qualifier {NameNode { 
                                    {Qualifier {NameNode { 
                                                {Qualifier {AbsentTreeNode {leaf}}} 
                                                {Ident java} 
                                              }}} 
                                    {Ident lang} 
                                  }}} 
                        {Ident Object} 
                      }}} 
               }}} 
  }}}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-15.1 {check out one field, one method classes } {
    set class [ java::call Class forName "ptolemy.lang.java.test.OneFM"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident get} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params  {}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {IntTypeNode {leaf}}} 
}}} { {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident set} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {VoidTypeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-16.1 {check out one field classes} {
    set class [ java::call Class forName "ptolemy.lang.java.test.OneField"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    lcompare [listToStrings $astList] {
{{FieldDeclNode { 
  {DefType {IntTypeNode {leaf}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident x} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {InitExpr {AbsentTreeNode {leaf}}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-17.1 {check out a simple class} {
    set classDeclNode [java::call ptolemy.lang.java.ASTReflect  lookupClassDeclNode "ptolemy.lang.java.test.Simple"]
    lcompare [$classDeclNode toString] \
{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident Simple} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident Simple} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-18.1 {check out a simple class with an import} {
    set classDeclNode [java::call ptolemy.lang.java.ASTReflect  lookupClassDeclNode "ptolemy.lang.java.test.Simple2"]
    lcompare [$classDeclNode toString] \
{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident Simple2} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident Simple2} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}
} {1}

######################################################################
####
#
#test ASTReflect-19.1 {check out switches - ignored - signature is unchanged } {} {
#    set class [ java::call Class forName "ptolemy.lang.java.test.Switch"]
#    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
#    lcompare [listToStrings $astList] \
#} {} 

######################################################################
####
#
test ASTReflect-20.1 {check out superclasses} {
    set classDeclNode [java::call ptolemy.lang.java.ASTReflect  lookupClassDeclNode "ptolemy.lang.java.test.SuperChild"]
    lcompare [$classDeclNode toString] \
{ {ClassDeclNode { 
  {Interfaces  {}} 
  {Members { 
   {ConstructorDeclNode { 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident SuperChild} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {BlockNode { 
           {Stmts  {}} 
         }}} 
    {ConstructorCall {SuperConstructorCallNode { 
                      {Args  {}} 
                    }}} 
  }}   {MethodDeclNode { 
    {Modifiers 513} 
    {Name {NameNode { 
           {Ident test} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {Params  {}} 
    {ThrowsList  {}} 
    {Body {AbsentTreeNode {leaf}}} 
    {ReturnType {VoidTypeNode {leaf}}} 
  }}}} 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident SuperChild} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {SuperClass {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}
} {1}

######################################################################
####
#
test ASTReflect-21.1 {check out field use} {
    set class [ java::call Class forName "ptolemy.lang.java.test.UseFields"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    lcompare [listToStrings $astList] {}
} {1}

######################################################################
####
#
test ASTReflect-22.1 {check out method Use} {
    set class [ java::call Class forName "ptolemy.lang.java.test.UseMethods"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    lcompare [listToStrings $astList] \
{{ {MethodDeclNode { 
  {Modifiers 1} 
  {Name {NameNode { 
         {Ident use} 
         {Qualifier {AbsentTreeNode {leaf}}} 
       }}} 
  {Params { 
   {ParameterNode { 
    {DefType {IntTypeNode {leaf}}} 
    {Modifiers 25} 
    {Name {NameNode { 
           {Ident } 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
  }}}} 
  {ThrowsList  {}} 
  {Body {AbsentTreeNode {leaf}}} 
  {ReturnType {TypeNameNode { 
               {Name {NameNode { 
                      {Ident Object} 
                      {Qualifier {NameNode { 
                                  {Ident lang} 
                                  {Qualifier {NameNode { 
                                              {Ident java} 
                                              {Qualifier {AbsentTreeNode {leaf}}} 
                                            }}} 
                                }}} 
                    }}} 
             }}} 
}}}}
} {1}

######################################################################
####
#
test ASTReflect-23.1 {pathnameToClass} {
    set fileSeparator [java::call System getProperty file.separator]
    set fileList [list java lang Object.java]
    set path [join $fileList $fileSeparator]
    set myclass [java::call ptolemy.lang.java.ASTReflect pathnameToClass $path]
    list [$myclass getName]
} {java.lang.Object}


######################################################################
####
#
test ASTReflect-23.2 {pathnameToClass} {
    set fileSeparator [java::call System getProperty file.separator]
    set fileList [list ptII ptolemy lang java ASTReflect]
    set path [join $fileList $fileSeparator]
    set myclass [java::call ptolemy.lang.java.ASTReflect pathnameToClass $path]
    list [$myclass getName]
} {ptolemy.lang.java.ASTReflect}


######################################################################
####
#
test ASTReflect-23.3 {pathnameToClass on a non-existant class} {
    set myclass [java::call ptolemy.lang.java.ASTReflect pathnameToClass "ptolemy/lang/java/NOTAClass"]
    list [java::isnull $myclass]
} {1}

######################################################################
####
#
test ASTReflect-23.4 {pathnameToClass} {
    set myclass [java::call ptolemy.lang.java.ASTReflect pathnameToClass "NOTAClass"]
    list [java::isnull $myclass]
} {1}

