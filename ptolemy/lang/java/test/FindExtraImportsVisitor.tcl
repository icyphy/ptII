# Tests for the FindExtraImportsVisitor class
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
test FindExtraImportsVisitor-1.1 {} {
    set ast [java::call ptolemy.lang.java.JavaParserManip \
	    {parse String boolean} "FindExtraImportsVisitorTest.java" false]
#    set ast [java::call ptolemy.lang.java.StaticResolution \
#    	    load $ast 0]
#    set findExtraImportsVisitor \
#	    [java::new ptolemy.lang.java.FindExtraImportsVisitor true]
#    set iVisitor [java::cast ptolemy.lang.IVisitor $findExtraImportsVisitor]
#    $ast accept $iVisitor [java::null]
    lcompare [$ast toString] \
{ {CompileUnitNode { 
  {DefTypes { 
   {ClassDeclNode { 
    {Interfaces  {}} 
    {Members { 
     {ConstructorDeclNode { 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident FindExtraImportsVisitorTest} 
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
           {Ident FindExtraImportsVisitorTest} 
           {Qualifier {AbsentTreeNode {leaf}}} 
         }}} 
    {SuperClass {TypeNameNode { 
                 {Name {NameNode { 
                        {Ident NamedObj} 
                        {Qualifier {AbsentTreeNode {leaf}}} 
                      }}} 
               }}} 
  }}}} 
  {Imports { 
   {ImportNode { 
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
  }}   {ImportNode { 
    {Name {NameNode { 
           {Ident Debuggable} 
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
}   }   }   }  {1}
