# Tests for the NamedObj class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

######################################################################
####
#
test ASTReflect-1.1 {} {
    set class [ java::call Class forName "ptolemy.lang.java.Skeleton"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTCompileUnitNode $class]
    list [$ast toString]
} {{CompileUnitNode
 DefTypes: list
  ClassDeclNode
   Interfaces: <empty list>
   Members: list
    ConstructorDeclNode
     Modifiers: 1
     Name: NameNode
            Ident: Skeleton
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     Params: <empty list>
     ThrowsList: <empty list>
     Body: BlockNode
            Stmts: <empty list>
           END BlockNode
     ConstructorCall: SuperConstructorCallNode
                       Args: <empty list>
                      END SuperConstructorCallNode
    END ConstructorDeclNode
    MethodDeclNode
     Modifiers: 514
     Name: NameNode
            Ident: _parseArg
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     Params: list
      ParameterNode
       DefType: TypeNameNode
                 Name: NameNode
                        Ident: String
                        Qualifier: AbsentTreeNode (leaf)
                       END NameNode
                END TypeNameNode
       Modifiers: 17
       Name: NameNode
              Ident: 
              Qualifier: AbsentTreeNode (leaf)
             END NameNode
      END ParameterNode
     END list
     ThrowsList: <empty list>
     Body: AbsentTreeNode (leaf)
     ReturnType: BoolTypeNode (leaf)
    END MethodDeclNode
    MethodDeclNode
     Modifiers: 514
     Name: NameNode
            Ident: _parseArgs
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     Params: list
      ParameterNode
       DefType: ArrayTypeNode
                 BaseType: TypeNameNode
                            Name: NameNode
                                   Ident: String
                                   Qualifier: AbsentTreeNode (leaf)
                                  END NameNode
                           END TypeNameNode
                END ArrayTypeNode
       Modifiers: 25
       Name: NameNode
              Ident: 
              Qualifier: AbsentTreeNode (leaf)
             END NameNode
      END ParameterNode
     END list
     ThrowsList: <empty list>
     Body: AbsentTreeNode (leaf)
     ReturnType: VoidTypeNode (leaf)
    END MethodDeclNode
    MethodDeclNode
     Modifiers: 513
     Name: NameNode
            Ident: main
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     Params: list
      ParameterNode
       DefType: ArrayTypeNode
                 BaseType: TypeNameNode
                            Name: NameNode
                                   Ident: String
                                   Qualifier: AbsentTreeNode (leaf)
                                  END NameNode
                           END TypeNameNode
                END ArrayTypeNode
       Modifiers: 25
       Name: NameNode
              Ident: 
              Qualifier: AbsentTreeNode (leaf)
             END NameNode
      END ParameterNode
     END list
     ThrowsList: <empty list>
     Body: AbsentTreeNode (leaf)
     ReturnType: VoidTypeNode (leaf)
    END MethodDeclNode
    FieldDeclNode
     DefType: IntTypeNode (leaf)
     Modifiers: 514
     Name: NameNode
            Ident: _fileStart
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     InitExpr: AbsentTreeNode (leaf)
    END FieldDeclNode
    FieldDeclNode
     DefType: BoolTypeNode (leaf)
     Modifiers: 514
     Name: NameNode
            Ident: _debug
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     InitExpr: AbsentTreeNode (leaf)
    END FieldDeclNode
    FieldDeclNode
     DefType: BoolTypeNode (leaf)
     Modifiers: 514
     Name: NameNode
            Ident: _eliminateImports
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
     InitExpr: AbsentTreeNode (leaf)
    END FieldDeclNode
   END list
   Modifiers: 1
   Name: NameNode
          Ident: Skeleton
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
   SuperClass: NameNode
                Ident: Object
                Qualifier: NameNode
                            Ident: lang
                            Qualifier: NameNode
                                        Ident: java
                                        Qualifier: AbsentTreeNode (leaf)
                                       END NameNode
                           END NameNode
               END NameNode
  END ClassDeclNode
 END list
 Imports: <empty list>
 Pkg: NameNode
       Ident: 
       Qualifier: AbsentTreeNode (leaf)
      END NameNode
END CompileUnitNode
}}

test ASTReflect-2.1 {check out constructor in Object} {
    set class [ java::call Class forName "java.lang.Object"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    list [listToStrings $astList]
} {{{ConstructorDeclNode
 Modifiers: 1
 Name: NameNode
        Ident: Object
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: <empty list>
 ThrowsList: <empty list>
 Body: BlockNode
        Stmts: <empty list>
       END BlockNode
 ConstructorCall: SuperConstructorCallNode
                   Args: <empty list>
                  END SuperConstructorCallNode
END ConstructorDeclNode
}}}

test ASTReflect-2.2 {check out constructors} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect constructorsASTList $class]
    list [listToStrings $astList]
} {{{ConstructorDeclNode
 Modifiers: 0
 Name: NameNode
        Ident: ReflectTest
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: <empty list>
 ThrowsList: <empty list>
 Body: BlockNode
        Stmts: <empty list>
       END BlockNode
 ConstructorCall: SuperConstructorCallNode
                   Args: <empty list>
                  END SuperConstructorCallNode
END ConstructorDeclNode
} {ConstructorDeclNode
 Modifiers: 0
 Name: NameNode
        Ident: ReflectTest
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: list
  ParameterNode
   DefType: IntTypeNode (leaf)
   Modifiers: 25
   Name: NameNode
          Ident: 
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
  END ParameterNode
 END list
 ThrowsList: <empty list>
 Body: BlockNode
        Stmts: <empty list>
       END BlockNode
 ConstructorCall: SuperConstructorCallNode
                   Args: <empty list>
                  END SuperConstructorCallNode
END ConstructorDeclNode
} {ConstructorDeclNode
 Modifiers: 0
 Name: NameNode
        Ident: ReflectTest
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: list
  ParameterNode
   DefType: ArrayTypeNode
             BaseType: ArrayTypeNode
                        BaseType: IntTypeNode (leaf)
                       END ArrayTypeNode
            END ArrayTypeNode
   Modifiers: 25
   Name: NameNode
          Ident: 
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
  END ParameterNode
 END list
 ThrowsList: <empty list>
 Body: BlockNode
        Stmts: <empty list>
       END BlockNode
 ConstructorCall: SuperConstructorCallNode
                   Args: <empty list>
                  END SuperConstructorCallNode
END ConstructorDeclNode
}}}

test ASTReflect-3.1 {check out fields} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect fieldsASTList $class]
    list [listToStrings $astList]
} {{{FieldDeclNode
 DefType: TypeNameNode
           Name: NameNode
                  Ident: NamedObj
                  Qualifier: AbsentTreeNode (leaf)
                 END NameNode
          END TypeNameNode
 Modifiers: 2
 Name: NameNode
        Ident: _publicField
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 InitExpr: AbsentTreeNode (leaf)
END FieldDeclNode
} {FieldDeclNode
 DefType: IntTypeNode (leaf)
 Modifiers: 20
 Name: NameNode
        Ident: _privateField
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 InitExpr: AbsentTreeNode (leaf)
END FieldDeclNode
}}}

test ASTReflect-3.1 {check out innerclasses} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect innerClassesASTList $class]
    list [listToStrings $astList]
} {{{ClassDeclNode
 Interfaces: <empty list>
 Members: list
  ConstructorDeclNode
   Modifiers: 0
   Name: NameNode
          Ident: ReflectTest$innerPublicClass
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
   Params: list
    ParameterNode
     DefType: TypeNameNode
               Name: NameNode
                      Ident: ReflectTest
                      Qualifier: AbsentTreeNode (leaf)
                     END NameNode
              END TypeNameNode
     Modifiers: 1
     Name: NameNode
            Ident: 
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
    END ParameterNode
   END list
   ThrowsList: <empty list>
   Body: BlockNode
          Stmts: <empty list>
         END BlockNode
   ConstructorCall: SuperConstructorCallNode
                     Args: <empty list>
                    END SuperConstructorCallNode
  END ConstructorDeclNode
  MethodDeclNode
   Modifiers: 1
   Name: NameNode
          Ident: innerPublicClassPublicMethod
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
   Params: list
    ParameterNode
     DefType: TypeNameNode
               Name: NameNode
                      Ident: NamedObj
                      Qualifier: AbsentTreeNode (leaf)
                     END NameNode
              END TypeNameNode
     Modifiers: 1
     Name: NameNode
            Ident: 
            Qualifier: AbsentTreeNode (leaf)
           END NameNode
    END ParameterNode
   END list
   ThrowsList: <empty list>
   Body: AbsentTreeNode (leaf)
   ReturnType: ArrayTypeNode
                BaseType: ArrayTypeNode
                           BaseType: ArrayTypeNode
                                      BaseType: IntTypeNode (leaf)
                                     END ArrayTypeNode
                          END ArrayTypeNode
               END ArrayTypeNode
  END MethodDeclNode
  FieldDeclNode
   DefType: TypeNameNode
             Name: NameNode
                    Ident: ReflectTest
                    Qualifier: AbsentTreeNode (leaf)
                   END NameNode
            END TypeNameNode
   Modifiers: 20
   Name: NameNode
          Ident: this$0
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
   InitExpr: AbsentTreeNode (leaf)
  END FieldDeclNode
 END list
 Modifiers: 1
 Name: NameNode
        Ident: ReflectTest$innerPublicClass
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 SuperClass: NameNode
              Ident: Object
              Qualifier: NameNode
                          Ident: lang
                          Qualifier: NameNode
                                      Ident: java
                                      Qualifier: AbsentTreeNode (leaf)
                                     END NameNode
                         END NameNode
             END NameNode
END ClassDeclNode
}}}

test ASTReflect-4.1 {check out methods} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set astList [java::call ptolemy.lang.java.ASTReflect methodsASTList $class]
    list [listToStrings $astList]
} {{{MethodDeclNode
 Modifiers: 513
 Name: NameNode
        Ident: publicMethod1
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: <empty list>
 ThrowsList: <empty list>
 Body: AbsentTreeNode (leaf)
 ReturnType: VoidTypeNode (leaf)
END MethodDeclNode
} {MethodDeclNode
 Modifiers: 1
 Name: NameNode
        Ident: publicMethod2
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: list
  ParameterNode
   DefType: IntTypeNode (leaf)
   Modifiers: 25
   Name: NameNode
          Ident: 
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
  END ParameterNode
 END list
 ThrowsList: <empty list>
 Body: AbsentTreeNode (leaf)
 ReturnType: VoidTypeNode (leaf)
END MethodDeclNode
} {MethodDeclNode
 Modifiers: 1
 Name: NameNode
        Ident: publicMethod2
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: list
  ParameterNode
   DefType: ArrayTypeNode
             BaseType: TypeNameNode
                        Name: NameNode
                               Ident: NamedObj
                               Qualifier: AbsentTreeNode (leaf)
                              END NameNode
                       END TypeNameNode
            END ArrayTypeNode
   Modifiers: 25
   Name: NameNode
          Ident: 
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
  END ParameterNode
 END list
 ThrowsList: <empty list>
 Body: AbsentTreeNode (leaf)
 ReturnType: VoidTypeNode (leaf)
END MethodDeclNode
} {MethodDeclNode
 Modifiers: 1
 Name: NameNode
        Ident: publicMethod2
        Qualifier: AbsentTreeNode (leaf)
       END NameNode
 Params: list
  ParameterNode
   DefType: ArrayTypeNode
             BaseType: ArrayTypeNode
                        BaseType: TypeNameNode
                                   Name: NameNode
                                          Ident: NamedObj
                                          Qualifier: AbsentTreeNode (leaf)
                                         END NameNode
                                  END TypeNameNode
                       END ArrayTypeNode
            END ArrayTypeNode
   Modifiers: 25
   Name: NameNode
          Ident: 
          Qualifier: AbsentTreeNode (leaf)
         END NameNode
  END ParameterNode
 END list
 ThrowsList: <empty list>
 Body: AbsentTreeNode (leaf)
 ReturnType: VoidTypeNode (leaf)
END MethodDeclNode
}}}
