# Tests for Yylex
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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
test Yylex-1.1 {Parse Simple.java} {
    set javaParser [java::new ptolemy.lang.java.JavaParser]
    $javaParser init Simple.java
    $javaParser parse
    set ast [$javaParser getAST]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {DefTypes { 
   {ClassDeclNode { 
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
    {SuperClass {AbsentTreeNode {leaf}}} 
  }}}} 
  {Imports  {}} 
  {Pkg {NameNode { 
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
} {1}

test Yylex-1.2 {Parse JavaScope.java} {
    set javaParser [java::new ptolemy.lang.java.JavaParser]
    $javaParser init JavaScope.java
    $javaParser parse
    set ast [$javaParser getAST]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {DefTypes { 
   {ClassDeclNode { 
    {Interfaces  {}} 
    {Members { 
     {FieldDeclNode { 
      {DefType {IntTypeNode {leaf}}} 
      {Modifiers 516} 
      {Name {NameNode { 
             {Ident js$t0} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {InitExpr {MethodCallNode { 
                 {Args { 
                  {StringLitNode { 
                   {Literal /home/eecs/cxh/jsdatabase} 
                 }}}} 
                 {Method {ObjectNode { 
                          {Name {NameNode { 
                                 {Ident setDatabase} 
                                 {Qualifier {NameNode { 
                                             {Ident js$} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                        }}} 
               }}} 
    }}     {FieldDeclNode { 
      {DefType {ArrayTypeNode { 
                {BaseType {TypeNameNode { 
                           {Name {NameNode { 
                                  {Ident String} 
                                  {Qualifier {AbsentTreeNode {leaf}}} 
                                }}} 
                         }}} 
              }}} 
      {Modifiers 516} 
      {Name {NameNode { 
             {Ident js$p} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {InitExpr {ArrayInitNode { 
                 {Initializers { 
                  {StringLitNode { 
                   {Literal ptolemy} 
                 }}                  {StringLitNode { 
                   {Literal lang} 
                 }}                  {StringLitNode { 
                   {Literal java} 
                 }}                  {StringLitNode { 
                   {Literal test} 
                 }}}} 
               }}} 
    }}     {FieldDeclNode { 
      {DefType {TypeNameNode { 
                {Name {NameNode { 
                       {Ident CoverageUnit} 
                       {Qualifier {AbsentTreeNode {leaf}}} 
                     }}} 
              }}} 
      {Modifiers 516} 
      {Name {NameNode { 
             {Ident js$c} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {InitExpr {MethodCallNode { 
                 {Args { 
                  {ObjectNode { 
                   {Name {NameNode { 
                          {Ident js$p} 
                          {Qualifier {AbsentTreeNode {leaf}}} 
                        }}} 
                 }}                  {StringLitNode { 
                   {Literal JavaScope} 
                 }}                  {StringLitNode { 
                   {Literal /export/maury/maury2/cxh/src/ptII/ptolemy/lang/java/test/jsoriginal/JavaScope.java} 
                 }}                  {LongLitNode { 
                   {Literal 4869} 
                 }}                  {MethodCallNode { 
                   {Args  {}} 
                   {Method {ObjectNode { 
                            {Name {NameNode { 
                                   {Ident js$n} 
                                   {Qualifier {AbsentTreeNode {leaf}}} 
                                 }}} 
                          }}} 
                 }}}} 
                 {Method {ObjectNode { 
                          {Name {NameNode { 
                                 {Ident c} 
                                 {Qualifier {NameNode { 
                                             {Ident js$} 
                                             {Qualifier {AbsentTreeNode {leaf}}} 
                                           }}} 
                               }}} 
                        }}} 
               }}} 
    }}     {FieldDeclNode { 
      {DefType {ArrayTypeNode { 
                {BaseType {IntTypeNode {leaf}}} 
              }}} 
      {Modifiers 528} 
      {Name {NameNode { 
             {Ident js$a} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {InitExpr {ObjectNode { 
                 {Name {NameNode { 
                        {Ident counters} 
                        {Qualifier {NameNode { 
                                    {Ident js$c} 
                                    {Qualifier {AbsentTreeNode {leaf}}} 
                                  }}} 
                      }}} 
               }}} 
    }}     {ConstructorDeclNode { 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident JavaScope} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
      {ThrowsList  {}} 
      {Body {BlockNode { 
             {Stmts { 
              {ExprStmtNode { 
               {Expr {MethodCallNode { 
                      {Args { 
                       {ObjectNode { 
                        {Name {NameNode { 
                               {Ident js$a} 
                               {Qualifier {NameNode { 
                                           {Ident JavaScope} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                      }}                       {IntLitNode { 
                        {Literal 1} 
                      }}}} 
                      {Method {ObjectNode { 
                               {Name {NameNode { 
                                      {Ident g} 
                                      {Qualifier {NameNode { 
                                                  {Ident js$} 
                                                  {Qualifier {AbsentTreeNode {leaf}}} 
                                                }}} 
                                    }}} 
                             }}} 
                    }}} 
             }}              {ExprStmtNode { 
               {Expr {MethodCallNode { 
                      {Args { 
                       {ObjectNode { 
                        {Name {NameNode { 
                               {Ident js$a} 
                               {Qualifier {NameNode { 
                                           {Ident JavaScope} 
                                           {Qualifier {AbsentTreeNode {leaf}}} 
                                         }}} 
                             }}} 
                      }}                       {IntLitNode { 
                        {Literal 0} 
                      }}}} 
                      {Method {ObjectNode { 
                               {Name {NameNode { 
                                      {Ident g} 
                                      {Qualifier {NameNode { 
                                                  {Ident js$} 
                                                  {Qualifier {AbsentTreeNode {leaf}}} 
                                                }}} 
                                    }}} 
                             }}} 
                    }}} 
             }}}} 
           }}} 
      {ConstructorCall {SuperConstructorCallNode { 
                        {Args  {}} 
                      }}} 
    }}     {FieldDeclNode { 
      {DefType {IntTypeNode {leaf}}} 
      {Modifiers 1} 
      {Name {NameNode { 
             {Ident anInteger} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {InitExpr {IntLitNode { 
                 {Literal 1} 
               }}} 
    }}     {MethodDeclNode { 
      {Modifiers 516} 
      {Name {NameNode { 
             {Ident js$n} 
             {Qualifier {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
      {ThrowsList  {}} 
      {Body {BlockNode { 
             {Stmts { 
              {ReturnNode { 
               {Expr {IntLitNode { 
                      {Literal 2} 
                    }}} 
             }}}} 
           }}} 
      {ReturnType {IntTypeNode {leaf}}} 
    }}}} 
    {Modifiers 1} 
    {Name {NameNode { 
           {Ident JavaScope} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {SuperClass {AbsentTreeNode {leaf}}} 
  }}}} 
  {Imports { 
   {ImportNode { 
    {Name {NameNode { 
           {Ident js$} 
           {Qualifier {NameNode { 
                       {Ident database} 
                       {Qualifier {NameNode { 
                                   {Ident javascope} 
                                   {Qualifier {NameNode { 
                                               {Ident suntest} 
                                               {Qualifier {NameNode { 
                                                           {Ident sun} 
                                                           {Qualifier {NameNode { 
                                                                       {Ident COM} 
                                                                       {Qualifier {AbsentTreeNode {leaf}}} 
                                                                     }}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
  }}   {ImportNode { 
    {Name {NameNode { 
           {Ident CoverageUnit} 
           {Qualifier {NameNode { 
                       {Ident database} 
                       {Qualifier {NameNode { 
                                   {Ident javascope} 
                                   {Qualifier {NameNode { 
                                               {Ident suntest} 
                                               {Qualifier {NameNode { 
                                                           {Ident sun} 
                                                           {Qualifier {NameNode { 
                                                                       {Ident COM} 
                                                                       {Qualifier {AbsentTreeNode {leaf}}} 
                                                                     }}} 
                                                         }}} 
                                             }}} 
                                 }}} 
                     }}} 
         }}} 
  }}}} 
  {Pkg {NameNode { 
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
} {1}
