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
    set ast [java::call ptolemy.lang.java.ASTReflect ASTClass $class]
    list [$ast toString]
} {{CompileUnitNode
 Pkg: NameNode
       Qualifier: NameNode
                   Qualifier: NameNode
                               Qualifier: AbsentTreeNode (leaf)
                               Ident: ptolemy
                              END NameNode
                   Ident: lang
                  END NameNode
       Ident: java
      END NameNode
 Imports: <empty list>
 DefTypes: list
  ClassDeclNode
   Name: NameNode
          Qualifier: AbsentTreeNode (leaf)
          Ident: Skeleton
         END NameNode
   Interfaces: <empty list>
   Modifiers: 1
   Members: list
    MethodDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: main
           END NameNode
     Modifiers: 513
     Params: list
      ParameterNode
       Name: NameNode
              Qualifier: AbsentTreeNode (leaf)
              Ident: 
             END NameNode
       Modifiers: 25
       DefType: ArrayTypeNode
                 BaseType: TypeNameNode
                            Name: NameNode
                                   Qualifier: AbsentTreeNode (leaf)
                                   Ident: String
                                  END NameNode
                           END TypeNameNode
                END ArrayTypeNode
      END ParameterNode
     END list
     ThrowsList: <empty list>
     ReturnType: VoidTypeNode (leaf)
     Body: AbsentTreeNode (leaf)
    END MethodDeclNode
    MethodDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: _parseArg
           END NameNode
     Modifiers: 514
     Params: list
      ParameterNode
       Name: NameNode
              Qualifier: AbsentTreeNode (leaf)
              Ident: 
             END NameNode
       Modifiers: 17
       DefType: TypeNameNode
                 Name: NameNode
                        Qualifier: AbsentTreeNode (leaf)
                        Ident: String
                       END NameNode
                END TypeNameNode
      END ParameterNode
     END list
     ThrowsList: <empty list>
     ReturnType: BoolTypeNode (leaf)
     Body: AbsentTreeNode (leaf)
    END MethodDeclNode
    MethodDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: _parseArgs
           END NameNode
     Modifiers: 514
     Params: list
      ParameterNode
       Name: NameNode
              Qualifier: AbsentTreeNode (leaf)
              Ident: 
             END NameNode
       Modifiers: 25
       DefType: ArrayTypeNode
                 BaseType: TypeNameNode
                            Name: NameNode
                                   Qualifier: AbsentTreeNode (leaf)
                                   Ident: String
                                  END NameNode
                           END TypeNameNode
                END ArrayTypeNode
      END ParameterNode
     END list
     ThrowsList: <empty list>
     ReturnType: VoidTypeNode (leaf)
     Body: AbsentTreeNode (leaf)
    END MethodDeclNode
    FieldDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: _fileStart
           END NameNode
     Modifiers: 514
     DefType: IntTypeNode (leaf)
     InitExpr: AbsentTreeNode (leaf)
    END FieldDeclNode
    FieldDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: _debug
           END NameNode
     Modifiers: 514
     DefType: BoolTypeNode (leaf)
     InitExpr: AbsentTreeNode (leaf)
    END FieldDeclNode
    FieldDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: _eliminateImports
           END NameNode
     Modifiers: 514
     DefType: BoolTypeNode (leaf)
     InitExpr: AbsentTreeNode (leaf)
    END FieldDeclNode
    ConstructorDeclNode
     Name: NameNode
            Qualifier: AbsentTreeNode (leaf)
            Ident: Skeleton()
           END NameNode
     Modifiers: 1
     Params: <empty list>
     ThrowsList: <empty list>
     Body: BlockNode
            Stmts: <empty list>
           END BlockNode
     ConstructorCall: SuperConstructorCallNode
                       Args: <empty list>
                      END SuperConstructorCallNode
    END ConstructorDeclNode
   END list
   SuperClass: NameNode
                Qualifier: NameNode
                            Qualifier: NameNode
                                        Qualifier: AbsentTreeNode (leaf)
                                        Ident: java
                                       END NameNode
                            Ident: lang
                           END NameNode
                Ident: Object
               END NameNode
  END ClassDeclNode
 END list
END CompileUnitNode
}}

test ASTReflect-1.2 {} {
    set class [ java::call Class forName "ptolemy.lang.java.test.ReflectTest"]
    set ast [java::call ptolemy.lang.java.ASTReflect ASTClass $class]
    list [$ast toString]
} {}