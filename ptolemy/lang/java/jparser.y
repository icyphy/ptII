/* 
YACC grammar for Java. This file was based on the solution written by
Paul N. Hilfinger for a class project for CS164.

Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

Java Parser $Id$ */

/* KEYWORDS */

%token ABSTRACT
%token BOOLEAN BREAK BYTE
%token CASE CATCH CHAR CLASS CONTINUE
%token DEFAULT DO DOUBLE
%token ELSE EXTENDS
%token FINAL FINALLY FLOAT FOR
%token IF IMPLEMENTS IMPORT INSTANCEOF INT INTERFACE
%token LONG
%token NATIVE NEW _NULL
%token PACKAGE PRIVATE PROTECTED PUBLIC
%token RETURN
%token SHORT STATIC STRICTFP SUPER SWITCH SYNCHRONIZED
%token THIS THROW THROWS TRANSIENT TRY
%token VOID VOLATILE
%token WHILE


/* KEYWORDS RESERVED (ILLEGAL AS IDENTIFIERS) BUT NOT USED */

%token CONST GOTO


/* IDENTIFIERS AND LITERALS */

%token TRUE FALSE

%token<sval> IDENTIFIER
%token<sval> INT_LITERAL LONG_LITERAL
%token<sval> FLOAT_LITERAL DOUBLE_LITERAL
%token<sval> CHARACTER_LITERAL
%token<sval> STRING_LITERAL

         /* SEPARATORS */

%token '(' ')' '{' '}' '[' ']' ',' '.' ';'

/* The following represents '[' ']' with arbitrary intervening whitespace. */
/* It is reasonably cheap to have the lexer recognize this, and it helps */
/* resolve at least one awkward LALR(1) lookahead problem. */
%token EMPTY_DIM

         /* OPERATORS */

%token '=' '>' '<'  '!' '~' '?' ':'
%token '+' '-' '*' '/' '&' '|' '^' '%'

%token CAND  /* &&: conditional and */
%token COR  /* ||: conditional or */

%token EQ    /* == */
%token NE    /* != */
%token LE    /* <= */
%token GE    /* >= */

%token LSHIFTL  /* << */
%token ASHIFTR  /* >> */
%token LSHIFTR  /* >>> */

%token PLUS_ASG  /* += */
%token MINUS_ASG  /* -= */
%token MULT_ASG  /* *= */
%token DIV_ASG  /* /= */
%token REM_ASG  /* %= */
%token LSHIFTL_ASG  /* <<= */
%token ASHIFTR_ASG  /* >>= */
%token LSHIFTR_ASG  /* >>>= */
%token AND_ASG  /* &= */
%token XOR_ASG  /* ^= */
%token OR_ASG  /* |= */

%token PLUSPLUS  /* ++ */
%token MINUSMINUS  /* -- */


        /* PRECEDENCES */

/* LOWEST */

%right ELSE
%left '=' PLUS_ASG MINUS_ASG MULT_ASG DIV_ASG REM_ASG LSHIFTL_ASG LSHIFTR_ASG ASHIFTR_ASG AND_ASG OR_ASG XOR_ASG
%right '?' ':'
%left COR
%left CAND
%left '|'
%left '^'
%left '&'
%left EQ NE
%left '<' '>' LE GE INSTANCEOF
%left LSHIFTL LSHIFTR ASHIFTR
%left '+' '-'
%left '*' '/' '%'
%nonassoc PLUSPLUS MINUSMINUS

/* HIGHEST */


/* Artificial precedence rules: The rule for '.' resolves conflicts
 * with QualifiedNames, FieldAccesses, and MethodAccesses.  The result
 * is that FieldAccesses and MethodAccesses that look syntactically
 * like QualifiedNames are parsed as QualifiedNames (see the
 * production for Name from QualifiedName).  The ambiguity must be
 * resolved with static semantic information at a later stage.  The
 * rule for ')' resolves conflicts between Casts and ComplexPrimaries.
 */

%right '.' ')'

/* Artificial precedence rule to resolve conflicts with
 * InterfaceModifiers and ClassModifiers */

/*
%right ABSTRACT FINAL PUBLIC
*/

%{
/*
Java parser produced by BYACC/J, with input file jparser.y

Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang.java;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
import java.io.FileInputStream;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

%}


%type<obj> CompilationUnit


%type<ival> Dims DimsOpt


%type<obj> Literal PrimaryExpression NotJustName ComplexPrimary
%type<obj> ArrayAccess MethodCall AllocationExpression
%type<obj> PostfixExpression PostIncrement PostDecrement UnaryExpression
%type<obj> PreIncrement PreDecrement UnaryExpressionNotPlusMinus
%type<obj> CastExpression ExpressionOpt Expression Assignment
%type<obj> ConstantExpression DimExpr
%type<obj> VariableInitializer ArrayInitializer Element

%type<obj> ArgumentListOpt ArgumentList
%type<obj> DimExprs ElementInitializers

%type<obj> FieldAccess 

%type<obj> Type ReferenceType PrimitiveType ClassOrInterfaceType ArrayType
%type<obj> Void SuperOpt

%type<obj> TypeNameList InterfacesOpt ThrowsOpt Throws
%type<obj> ExtendsInterfacesOpt ExtendsInterfaces

%type<obj> Block Statement
%type<obj> EmptyStatement LabeledStatement
%type<obj> SelectionStatement IterationStatement JumpStatement
%type<obj> GuardingStatement  MethodBody Finally
%type<obj> ExplicitConstructorCallStatement ExpressionStatement

%type<obj> BlockStatementsOpt BlockStatements BlockStatement SwitchBlock
%type<obj> LocalVariableDeclarationStatement
%type<obj> SwitchBlockStatementsOpt
%type<obj> ForInit ExpressionStatementsOpt ExpressionStatements
%type<obj> ForUpdateOpt

%type<ival> FieldModifiersOpt FieldModifiers FieldModifier
%type<obj> ImportStatement TypeImportStatement
%type<obj> TypeImportOnDemandStatement ClassDeclaration TypeDeclaration
%type<obj> Parameter InterfaceDeclaration
%type<obj> MethodDeclaration StaticInitializer ConstructorDeclaration
%type<obj> InstanceInitializer
%type<obj> MethodSignatureDeclaration

%type<obj> ClassBody FieldDeclarationsOpt FieldDeclarations
%type<obj> FieldDeclaration FieldVariableDeclaration
%type<obj> ConstantFieldDeclaration
%type<obj> TypeDeclarationsOpt
%type<obj> ParameterListOpt ParameterList InterfaceBody
%type<obj> ImportStatementsOpt
%type<obj> InterfaceMemberDeclaration InterfaceMemberDeclarationsOpt

%type<obj> SwitchLabel
%type<obj> SwitchLabels

%type<obj> SimpleName LabelOpt
%type<obj> PackageDeclarationOpt Name QualifiedName

%type<obj> Catch
%type<obj> Catches

%type<obj> VariableDeclarator
%type<obj> VariableDeclarators

%start Start

%%

Start :
  CompilationUnit   { _theAST = (CompileUnitNode) $1; }

        /* 1.7 LITERALS */

Literal :
    INT_LITERAL
    { $$ = new IntLitNode($1); }
  | LONG_LITERAL
    { $$ = new LongLitNode($1); }
  | FLOAT_LITERAL
    { $$ = new FloatLitNode($1); }
  | DOUBLE_LITERAL
    { $$ = new DoubleLitNode($1); }
  | TRUE
    { $$ = new BoolLitNode("true"); }
  | FALSE
    { $$ = new BoolLitNode("false"); }
  | CHARACTER_LITERAL
    { $$ = new CharLitNode($1); }
  | STRING_LITERAL
    { $$ = new StringLitNode($1); }
  ;


          /* 2. TYPES AND VALUES */

Type :
    PrimitiveType
  | ReferenceType
  ;

ReferenceType :
    ClassOrInterfaceType
  | ArrayType
  ;


/* Section 2.1 */

PrimitiveType :
    BOOLEAN
    { $$ = BoolTypeNode.instance; }
  | CHAR
    { $$ = CharTypeNode.instance; }
  | BYTE
    { $$ = ByteTypeNode.instance; }
  | SHORT
    { $$ = ShortTypeNode.instance; }
  | INT
    { $$ = IntTypeNode.instance; }
  | FLOAT
    { $$ = FloatTypeNode.instance; }
  | LONG
    { $$ = LongTypeNode.instance; }
  | DOUBLE
    { $$ = DoubleTypeNode.instance; }
  ;


/* Section 2.2 */

ClassOrInterfaceType :
    Name      %prec ')'
    { $$ = new TypeNameNode((NameNode) $1); }
  ;

ArrayType :
    Type EMPTY_DIM
    { $$ = new ArrayTypeNode((TypeNode) $1); }
  ;

           /* 5. PROGRAM STRUCTURE */

/* Section 5.4 */

CompilationUnit :
    PackageDeclarationOpt ImportStatementsOpt TypeDeclarationsOpt
    { $$ = new CompileUnitNode((TreeNode) $1, (List) $2, (List) $3);  }
  ;

PackageDeclarationOpt :
    PACKAGE Name ';'
    { $$ = $2; }
  | empty
    { $$ = AbsentTreeNode.instance; }
  ;

ImportStatementsOpt :
    empty
    { $$ = new LinkedList(); }
  | ImportStatement ImportStatementsOpt
    { $$ = cons($1, (List) $2); }

  ;

TypeDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | TypeDeclaration TypeDeclarationsOpt
    { $$ = cons($1, (List) $2); }
  | ';' TypeDeclarationsOpt
    { $$ = $2; }
  ;

TypeDeclaration :
    ClassDeclaration
  | InterfaceDeclaration
  ;


/* Section 5.7 */

ImportStatement :
    TypeImportStatement
  | TypeImportOnDemandStatement
  ;

TypeImportStatement :
    IMPORT Name ';'
    { $$ = new ImportNode((NameNode) $2); }
  ;

TypeImportOnDemandStatement :
    IMPORT Name '.' '*' ';'
    { $$ = new ImportOnDemandNode((NameNode) $2); }
  ;



      /* 6. CLASS AND INTERFACE TYPE DECLARATIONS */


/* Section 6.1 */

ClassDeclaration :
    FieldModifiersOpt CLASS SimpleName SuperOpt InterfacesOpt ClassBody
    { 
      // add a default constructor if none is found
      NameNode name = (NameNode) $3;
      List body = (List) $6;
       
      Iterator bodyItr = body.iterator();
             
      boolean constructorFound = false;
        
      while (!constructorFound && bodyItr.hasNext()) {
          Object member = bodyItr.next();
           
          if (member instanceof ConstructorDeclNode) {
             constructorFound = true;           
          }
      }
        
      if (!constructorFound) {
         body.add(new ConstructorDeclNode(Modifier.PUBLIC_MOD,         
                  new NameNode(name.getQualifier(), name.getIdent()),
                  new LinkedList(),                // params
                  new LinkedList(),                // throws 
                  new BlockNode(new LinkedList()), // body
                  new SuperConstructorCallNode(new LinkedList())));  
      }
                         
      $$ = new ClassDeclNode($1, name, (List) $5,
           (List) body, (TypeNameNode) $4);
    }
 ;

/* Section 6.1.1 */

/* Class modifiers were here. We need to verify later that the modifiers are
 * only abstract, final, or public for top-level classes.
 */

/* Section 6.1.2 */

SuperOpt :
    EXTENDS ClassOrInterfaceType
    { $$ = $2; }
  | empty
    { 
      // add the implicit subclass Object. Note this is wrong for Object itself.
      $$ = new TypeNameNode(new NameNode(AbsentTreeNode.instance, "Object")); 
    }
  ;


/* Section 6.1.3 */

InterfacesOpt :
    IMPLEMENTS TypeNameList
    { $$ = $2; }
  | empty
    { $$ = new LinkedList(); }
  ;



/* Section 6.1.4 */

ClassBody :
    '{' FieldDeclarationsOpt '}'
    {
     $$ = $2; // in the original, an ABSENT tree is added
   }
  ;

FieldDeclarationsOpt :
    FieldDeclarations  { }
  | empty
    { $$ = new LinkedList(); }
  ;

FieldDeclarations :
    FieldDeclaration
  | FieldDeclaration FieldDeclarations
    { $$ = appendLists((List) $1, (List) $2); }
  ;


/* Section 6.2 */

FieldDeclaration :
    FieldVariableDeclaration
  | MethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | StaticInitializer
    { $$ = cons($1); }
    /* NEW : Java 1.1 : D.1.3 : Instance initializers */
  | InstanceInitializer
    { $$ = cons($1); }
    /*  NEW : Java 1.1 : D.1.1 : Inner classes
     *  There shouldn't be a semicolon following
     *  the declaration, but some of the Java sources do it.
     */
  | TypeDeclaration
    { $$ = cons($1); }
  | TypeDeclaration ';'
    { $$ = cons($1); }
  ;


/* Section 6.3 */

FieldVariableDeclaration :
    FieldModifiersOpt Type VariableDeclarators ';'
   {
      Modifier.checkFieldModifiers($1);
      List result = new LinkedList();

      List varDecls = (List) $3;
      Iterator itr = varDecls.iterator();

       while (itr.hasNext()) {
         DeclaratorNode decl = (DeclaratorNode) itr.next();
         result = cons(new FieldDeclNode($1,
                        makeArrayType((TypeNode) $2, decl.getDims()),
                        decl.getName(), decl.getInitExpr()),
                       result);
       }

      $$ = result;
   }
  ;

/* Section 6.3.1, 6.4.1, 6.5.1 */
/* Note: The nonterminals ConstructorModifier, MethodModifier, and */
/* VariableModifer are consolidated here into FieldModifier to */
/* resolve the LALR(1) conflicts.  Must be distinguished in later */
/* sections of the compiler.  */

FieldModifiersOpt :
    FieldModifiers   { }
  | empty
    { $$ = Modifier.NO_MOD; }
  ;

FieldModifiers :
    FieldModifier    { $$ = $1; }
  | FieldModifiers FieldModifier
    {
     $$ = ($1 | $2);
      if (($1 & $2) != 0) {
         yyerror("repeated modifier");
      }
    }
  ;

FieldModifier :
    /* Applicable to methods, constructors, and variables (6.[345].1) */
    PUBLIC
    { $$ = Modifier.PUBLIC_MOD; }
  | PROTECTED
    { $$ = Modifier.PROTECTED_MOD;  }
  | PRIVATE
    { $$ = Modifier.PRIVATE_MOD;  }
    /* Applicable to methods and variables (6.[34].1) */
  | STATIC
    { $$ = Modifier.STATIC_MOD;  }
  | FINAL
    { $$ = Modifier.FINAL_MOD;  }
    /* Applicable to methods (6.4.1) */
  | ABSTRACT
    { $$ = Modifier.ABSTRACT_MOD;  }
  | NATIVE
    { $$ = Modifier.NATIVE_MOD;  }
  | SYNCHRONIZED
    { $$ = Modifier.SYNCHRONIZED_MOD;  }
    /* Applicable to variables (6.3.1) */
  | TRANSIENT
    { $$ = Modifier.TRANSIENT_MOD;  }
  | VOLATILE
    { $$ = Modifier.VOLATILE_MOD;  }
 | STRICTFP
   { $$ = Modifier.STRICTFP_MOD; }
  ;

/* Section 6.3.2 */

/* We build these up backwards.  The resulting lists
 * get scanned by the rules that use VariableDeclarators, and in the process
 * get reversed again. */

VariableDeclarators :
    VariableDeclarator
    { $$ = cons($1); }
  | VariableDeclarators ',' VariableDeclarator
    { $$ = cons($3, (List) $1); }
  ;

VariableDeclarator :
   SimpleName DimsOpt
    { $$ = new DeclaratorNode($2, (NameNode) $1, AbsentTreeNode.instance); }
  | SimpleName DimsOpt '=' VariableInitializer
    { $$ = new DeclaratorNode($2, (NameNode) $1, (ExprNode) $4); }
  ;


/* Section 6.3.3 */

VariableInitializer :
    Expression
  | ArrayInitializer
  ;

/* Section 6.4 */

MethodDeclaration :
    FieldModifiersOpt Type SimpleName '(' ParameterListOpt ')' DimsOpt
    ThrowsOpt MethodBody
   {
     Modifier.checkMethodModifiers($1);
      $$ = new MethodDeclNode($1, (NameNode) $3, (List) $5,
                             (List) $8, (TreeNode) $9,
                             makeArrayType((TypeNode) $2, $7));
   }
  | FieldModifiersOpt Void SimpleName '(' ParameterListOpt ')' DimsOpt
    ThrowsOpt MethodBody
   {
     Modifier.checkMethodModifiers($1);
      $$ = new MethodDeclNode($1, (NameNode) $3, (List) $5,
                             (List) $8, (TreeNode) $9,
                             makeArrayType((TypeNode) $2, $7));
   }
 ;

Void :
  VOID
    { $$ = VoidTypeNode.instance; }

/* Note: "Inlined" ResultType to avoid LALR(1) conflict. */


/* Section 6.4.3 */

ParameterListOpt :
    ParameterList    { }
  | empty
    { $$ = new LinkedList();  }
  ;

ParameterList :
    Parameter
    { $$ = cons($1); }
  | Parameter ',' ParameterList
    { $$ = cons($1, (List) $3); }
  ;

Parameter :
    FieldModifiersOpt Type SimpleName DimsOpt
    {
      Modifier.checkParameterModifiers($1); 
      $$ = new ParameterNode($1, makeArrayType((TypeNode) $2, $4),
                             (NameNode) $3);
    }
  ;

/* Section 6.4.4 */

ThrowsOpt :
    Throws       { }
  | empty
    { $$ = new LinkedList(); }
  ;

Throws :
    THROWS TypeNameList
    { $$ = $2; }
  ;

TypeNameList :
    ClassOrInterfaceType
    { $$ = cons($1); }
  | ClassOrInterfaceType ',' TypeNameList
    { $$ = cons($1, (List) $3); }
  ;


/* Section 6.4.5 */

MethodBody :
    Block
  | ';'
   { $$ = AbsentTreeNode.instance; }
  ;


/* Section 6.5 */

ConstructorDeclaration :
    FieldModifiersOpt IDENTIFIER '(' ParameterListOpt ')'  ThrowsOpt
   '{' ExplicitConstructorCallStatement BlockStatementsOpt '}'
    {
      Modifier.checkConstructorModifiers($1);
       $$ = new ConstructorDeclNode($1,
            new NameNode(AbsentTreeNode.instance, $2), (List) $4,
            (List) $6, new BlockNode((List) $9),
            (ConstructorCallNode) $8);
   }
  | FieldModifiersOpt IDENTIFIER '(' ParameterListOpt ')'  ThrowsOpt
   '{' BlockStatementsOpt '}'
    {
     Modifier.checkConstructorModifiers($1);
      $$ = new ConstructorDeclNode($1,
           new NameNode(AbsentTreeNode.instance, $2), (List) $4,
           (List) $6, new BlockNode((List) $8),
           new SuperConstructorCallNode(new LinkedList()));
    }
  ;

/* Note: We use FieldModifiersOpt to avoid a LALR(1) conflict. */

/* Section 6.5.4 */

ExplicitConstructorCallStatement :
    THIS '(' ArgumentListOpt ')' ';'
    { $$ = new ThisConstructorCallNode((List) $3); }
  | SUPER '(' ArgumentListOpt ')' ';'
    { $$ = new SuperConstructorCallNode((List) $3); }
  ;


/* Section 6.7.2 */

StaticInitializer :
    STATIC Block
    { $$ = new StaticInitNode((BlockNode) $2); }
  ;

InstanceInitializer :
     Block
       { $$ = new InstanceInitNode((BlockNode) $1); }
   ;

/* Section 6.8 */

InterfaceDeclaration :
    FieldModifiersOpt INTERFACE SimpleName ExtendsInterfacesOpt
    InterfaceBody
    {
     Modifier.checkInterfaceModifiers($1);
     $$ = new InterfaceDeclNode($1, (NameNode) $3, (List) $4, (List) $5);
    }
  ;

/* Section 6.8.1 */

/* InterfaceModifiers were here.
 * We need to verify later that the modifiers are only public and/or abstract.
 */

/* Section 6.8.2 */

ExtendsInterfacesOpt :
    ExtendsInterfaces   { }
  | empty
    { $$ = new LinkedList(); }
  ;

ExtendsInterfaces :
    EXTENDS TypeNameList
    { $$ = $2; }
  ;

/* Section 6.8.3 */

InterfaceBody :
    '{' InterfaceMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

InterfaceMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | InterfaceMemberDeclaration InterfaceMemberDeclarationsOpt
    { $$ = appendLists((List) $1, (List) $2); }
  ;

InterfaceMemberDeclaration :
    ConstantFieldDeclaration
  | MethodSignatureDeclaration
    { $$ = cons($1); }
  | TypeDeclaration
    { $$ = cons($1); }
  | TypeDeclaration ';'
    { $$ = cons($1); }
  ;

ConstantFieldDeclaration :
    FieldModifiersOpt Type VariableDeclarators ';'
    {
     int modifiers = $1;
     modifiers |= (Modifier.STATIC_MOD | Modifier.FINAL_MOD);

     Modifier.checkConstantFieldModifiers(modifiers);
     List varDecls = (List) $3;
     Iterator itr = varDecls.iterator();

      List result = new LinkedList();

      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        result = cons(new FieldDeclNode(modifiers,
                     makeArrayType((TypeNode) $2, decl.getDims()),
                      decl.getName(), decl.getInitExpr()), result);
      }

      $$ = result;
    }
  ;

MethodSignatureDeclaration :
    FieldModifiersOpt Type SimpleName '(' ParameterListOpt ')' DimsOpt
    ThrowsOpt ';'
    {
     Modifier.checkMethodSignatureModifiers($1);
      $$ = new MethodDeclNode($1, (NameNode) $3,
                             (List) $5, (List) $8,
                             AbsentTreeNode.instance,
                             makeArrayType((TypeNode) $2, $7));
   }
  | FieldModifiersOpt Void SimpleName '(' ParameterListOpt ')' DimsOpt
     ThrowsOpt ';'
    {
      Modifier.checkMethodSignatureModifiers($1);
      $$ = new MethodDeclNode($1, (NameNode) $3,
                             (List) $5, (List) $8,
                             AbsentTreeNode.instance,
                             makeArrayType((TypeNode) $2, $7));
    }
  ;

           /* ARRAYS */

/* Section 7.3 */

ArrayInitializer :
   '{' ElementInitializers '}'
    { $$ = new ArrayInitNode((List) $2); }
  | '{' ElementInitializers ',' '}'
    { $$ = new ArrayInitNode((List) $2); }
  | '{' '}'
    { $$ = new ArrayInitNode(new LinkedList()); }
  ;
/* Note: I'm going to assume that they didn't intend to allow "{,}". */

ElementInitializers :
    Element
    { $$ = cons($1); }
  | ElementInitializers ',' Element
    { $$ = append((List) $1, $3); }
  ;

Element :
    Expression
  | ArrayInitializer
  ;


         /* BLOCKS AND STATEMENTS */

/* Section 8.1 */

Block :
    '{' BlockStatementsOpt '}'
    { $$ = new BlockNode((List) $2); }
  ;

BlockStatementsOpt :
    BlockStatements     { }
  | empty
    { $$ = new LinkedList(); }
  ;

BlockStatements :
    BlockStatement
    { $$ = $1; }
  | BlockStatements BlockStatement
    { $$ = appendLists((List) $1, (List) $2); }
  ;

BlockStatement :
    LocalVariableDeclarationStatement
    { $$ = $1; }
  | Statement
    { $$ = cons($1); }
 | ClassDeclaration
   { $$ = cons(new UserTypeDeclStmtNode((UserTypeDeclNode) $1)); }
  ;


/* Section 8.2 */

LocalVariableDeclarationStatement :
   FieldModifiers Type VariableDeclarators ';'
   {
     Modifier.checkLocalVariableModifiers($1);

     List varDecls = (List) $3;
     List result = new LinkedList();

     Iterator itr = varDecls.iterator();

      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        result = cons(new LocalVarDeclNode($1,
                     makeArrayType((TypeNode) $2, decl.getDims()),
                     decl.getName(), decl.getInitExpr()), result);
     }
     $$ = result;
   }

 | Type VariableDeclarators ';'
   {
     List varDecls = (List) $2;
     List result = new LinkedList();

     Iterator itr = varDecls.iterator();

      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        result = cons(new LocalVarDeclNode(Modifier.NO_MOD,
                     makeArrayType((TypeNode) $1, decl.getDims()),
                     decl.getName(), decl.getInitExpr()), result);
     }
     $$ = result;
   }
  ;

/* Section 8.3 */

Statement :
    EmptyStatement
  | LabeledStatement
  | ExpressionStatement ';'
    { $$ = new ExprStmtNode((ExprNode) $1); }
  | SelectionStatement
  | IterationStatement
  | JumpStatement
  | GuardingStatement
  | Block
  ;

/* Section 8.4 */

EmptyStatement :
    ';'
    { $$ = new EmptyStmtNode(); }
  ;


/* Section 8.5 */

LabeledStatement :
    SimpleName ':' Statement
    { $$ = new LabeledStmtNode((NameNode) $1, (StatementNode) $3); }
  ;


/* Section 8.6 */

ExpressionStatement :
    Assignment
    { $$ = $1; }
  | PreIncrement
    { $$ = $1; }
  | PreDecrement
    { $$ = $1; }
  | PostIncrement
    { $$ = $1; }
  | PostDecrement
    { $$ = $1; }
  | MethodCall
    { $$ = $1; }
  | AllocationExpression
    { $$ = $1; }
  ;


/* Section 8.7 */

SelectionStatement :
    IF '(' Expression ')' Statement %prec ELSE
    { $$ = new IfStmtNode((ExprNode) $3, (StatementNode) $5, AbsentTreeNode.instance); }
  | IF '(' Expression ')' Statement ELSE Statement
    { $$ = new IfStmtNode((ExprNode) $3, (StatementNode) $5, (TreeNode) $7); }
  | SWITCH '(' Expression ')' SwitchBlock
    { $$ = new SwitchNode((ExprNode) $3, (List) $5); }
  ;

SwitchBlock :
    '{' SwitchBlockStatementsOpt '}'
    { $$ = $2; }
  ;

SwitchBlockStatementsOpt :
    empty
   { $$ = new LinkedList(); }
  | SwitchLabels BlockStatements SwitchBlockStatementsOpt
    {
     $$ = cons(new SwitchBranchNode((List) $1, (List) $2),
               (List) $3);
   }
   /* Handle labels at the end without any statements */
 | SwitchLabels
   { $$ = cons(new SwitchBranchNode((List) $1, new LinkedList())); }
 ;

SwitchLabels :
    SwitchLabel
    { $$ = cons($1); }
  | SwitchLabel SwitchLabels
    { $$ = cons($1, (List) $2); }
 ;

SwitchLabel :
    CASE ConstantExpression ':'
    { $$ = new CaseNode((TreeNode) $2); }
  | DEFAULT ':'
    { $$ = new CaseNode(AbsentTreeNode.instance); }
  ;

/* Section 8.8 */

IterationStatement :
    WHILE '(' Expression ')' Statement
    { $$ = new LoopNode(new EmptyStmtNode(), (ExprNode) $3, (TreeNode) $5); }
  | DO Statement WHILE '(' Expression ')' ';'
    { $$ = new LoopNode((TreeNode) $2, (ExprNode) $5, new EmptyStmtNode()); }
  | FOR '(' ForInit Expression ';' ForUpdateOpt ')' Statement
    { $$ = new ForNode((List) $3, (ExprNode) $4,
      (List) $6, (StatementNode) $8); }
  | FOR '(' ForInit ';' ForUpdateOpt ')' Statement
    { $$ = new ForNode((List) $3, new BoolLitNode("true"), (List) $5,
      (StatementNode) $7); }
  ;

ForInit :
    ExpressionStatementsOpt ';'
    { $$ = $1; }
  | LocalVariableDeclarationStatement
   { $$ = $1; }
  ;

ForUpdateOpt :
    ExpressionStatements   { }
  | empty
    { $$ = new LinkedList(); }
  ;

ExpressionStatementsOpt :
    ExpressionStatements   { }
  | empty
    { $$ = new LinkedList(); }
  ;

ExpressionStatements :
    ExpressionStatement
    { $$ = cons($1); }
  | ExpressionStatement ',' ExpressionStatements
    { $$ = cons($1, (List) $3); }
  ;


/* Section 8.9 */

JumpStatement :
    BREAK LabelOpt ';'
    { $$ = new BreakNode((TreeNode) $2); }
  | CONTINUE LabelOpt ';'
    { $$ = new ContinueNode((TreeNode) $2); }
  | RETURN ExpressionOpt ';'
    { $$ = new ReturnNode((TreeNode) $2); }
  | THROW Expression ';'
    { $$ = new ThrowNode((ExprNode) $2); }
  ;


LabelOpt :
    SimpleName    { }
  | empty
    { $$ = AbsentTreeNode.instance; }
  ;


/* Section 8.10 */

GuardingStatement :
    SYNCHRONIZED '(' Expression ')' Statement
    { $$ = new SynchronizedNode((ExprNode) $3, (TreeNode) $5); }
  | TRY Block Finally
    { $$ = new TryNode((BlockNode) $2, new LinkedList(), (TreeNode) $3); }
  | TRY Block Catches
    { $$ = new TryNode((BlockNode) $2, (List) $3, AbsentTreeNode.instance); }
  | TRY Block Catches Finally
    { $$ = new TryNode((BlockNode) $2, (List) $3, (TreeNode) $4); }
  ;

Catches :
    Catch
    { $$ = cons($1); }
  | Catch Catches
    { $$ = cons($1, (List) $2); }
  ;

Catch :
    CATCH '(' Parameter ')' Block
    { $$ = new CatchNode((ParameterNode) $3, (BlockNode) $5); }
  ;

Finally :
    FINALLY Block
    { $$ = $2; }
  ;


        /* EXPRESSIONS */


/* Section 9.4 */

PrimaryExpression :
   Name      %prec ')'
   { $$ = new ObjectNode((NameNode) $1); }
  | NotJustName
  | Name '.' CLASS
    { $$ = new TypeClassAccessNode(new TypeNameNode((NameNode) $1)); }
  | PrimitiveType '.' CLASS
    { $$ = new TypeClassAccessNode((TypeNode) $1); }
  | Void '.' CLASS
    { $$ = new TypeClassAccessNode((TypeNode) $1); }
  | Name '.' THIS
    { $$ = new OuterThisAccessNode(new TypeNameNode((NameNode) $1)); }
  | Name '.' SUPER
    { $$ = new OuterSuperAccessNode(new TypeNameNode((NameNode) $1)); }
  ;

NotJustName :
    AllocationExpression
  | ComplexPrimary
  ;

ComplexPrimary :
    Literal
  | _NULL
    { $$ = new NullPntrNode(); }
  | THIS
    { $$ = new ThisNode(); }
  | '(' Expression ')'
    { $$ = $2; }
  | '(' Name ')'
    { $$ = new ObjectNode((NameNode) $2); }
  | ArrayAccess
  | FieldAccess
    { $$ = $1; }
  | MethodCall
    /* NEW : Java 1.1 : D.7.3 : type class access */
  | ArrayType '.' CLASS
    { $$ = new TypeClassAccessNode((TypeNode) $1); }
  ;
/* Note: The fifth production above is redundant, but helps resolve a  */
/* LALR(1) lookahead conflict arising in cases like "(T) + x" (Do we reduce  */
/* Name T to ClassOrInterfaceType on seeing the ")"?). See also  */
/* CastExpression in section 9.10.  */

Name :
    SimpleName
    { $$ = $1; }
  | QualifiedName
  ;

SimpleName :
    IDENTIFIER
    { $$ = new NameNode(AbsentTreeNode.instance, $1); }
  ;

QualifiedName :
    Name '.' IDENTIFIER
    { $$ = new NameNode((NameNode) $1, $3); }
 ;

/* Section 9.5 */

ArrayAccess :
    Name '[' Expression ']'
    { $$ = new ArrayAccessNode(new ObjectNode((NameNode) $1), (ExprNode) $3); }
  | ComplexPrimary '[' Expression ']'
    { $$ = new ArrayAccessNode((ExprNode) $1, (ExprNode) $3); }
  ;


/* Section 9.6 */

FieldAccess :
    /* The following never matches Name '.' IDENTIFIER */
    PrimaryExpression '.' SimpleName
    { $$ = new ObjectFieldAccessNode((ExprNode) $1, (NameNode) $3); }
  | SUPER '.' SimpleName
    { $$ = new SuperFieldAccessNode((NameNode) $3); }
  ;


/* Section 9.7 */

MethodCall :
    Name '(' ArgumentListOpt ')'
    { $$ = new MethodCallNode(new ObjectNode((NameNode) $1), (List) $3); }
  | FieldAccess '(' ArgumentListOpt ')'
    { $$ = new MethodCallNode((FieldAccessNode) $1, (List) $3); }
  ;

ArgumentListOpt :
    ArgumentList   {  }
  | empty
    { $$ = new LinkedList(); }
  ;

ArgumentList :
    Expression
    { $$ = cons($1); }
  | Expression ',' ArgumentList
    { $$ = cons($1, (List) $3); }
  ;


/* Section 9.8 */

AllocationExpression :
   NEW ClassOrInterfaceType '(' ArgumentListOpt ')'
   { $$ = new AllocateNode((TypeNameNode) $2, (List) $4, AbsentTreeNode.instance); }
   /* NEW: Java 1.1 : D.2.1 Anonymous classes */
 | NEW ClassOrInterfaceType '(' ArgumentListOpt ')' ClassBody
   {
     $$ = new AllocateAnonymousClassNode((TypeNameNode) $2,
               (List) $4, (List) $6, AbsentTreeNode.instance);
   }
 | NEW ClassOrInterfaceType DimExprs DimsOpt
   {
     $$ = new AllocateArrayNode((TypeNode) $2, (List) $3, $4,
           AbsentTreeNode.instance);
   }
   /* NEW: Java 1.1 : D.2.1 Anonymous arrays */
 | NEW ClassOrInterfaceType DimsOpt ArrayInitializer
   {
     $$ = new AllocateArrayNode((TypeNode) $2, new LinkedList(), $3,
          (TreeNode) $4);
   }
 | NEW PrimitiveType DimExprs DimsOpt
   {
     $$ = new AllocateArrayNode((TypeNode) $2, (List) $3, $4,
           AbsentTreeNode.instance);
   }
   /* NEW: Java 1.1 : D.2.1 Anonymous arrays */
 | NEW PrimitiveType DimsOpt ArrayInitializer
   {
     $$ = new AllocateArrayNode((TypeNode) $2, new LinkedList(), $3,
           (TreeNode) $4);
   }
   /* NEW: Java 1.1 : qualified class creation */
 | PrimaryExpression '.' NEW IDENTIFIER '(' ArgumentListOpt ')'
   {
     $$ = new AllocateNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, $4)),
           (List) $6, (ExprNode) $1);
   }
   /* NEW: Java 1.1 : qualified anonymous class creation */
 | PrimaryExpression '.' NEW IDENTIFIER '(' ArgumentListOpt ')' ClassBody
   {
     $$ = new AllocateAnonymousClassNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, $4)),
           (List) $6, (List) $8, (ExprNode) $1);
   }
   /* Redundant productions to handle Name . new */
   /* NEW: Java 1.1 : qualified class creation */
 | Name '.' NEW IDENTIFIER '(' ArgumentListOpt ')'
   {
     $$ = new AllocateNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, $4)),
           (List) $6, new ObjectNode((NameNode) $1));
   }
   /* NEW: Java 1.1 : qualified anonymous class creation */
 | Name '.' NEW IDENTIFIER '(' ArgumentListOpt ')' ClassBody
   {
     $$ = new AllocateAnonymousClassNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, $4)),
           (List) $6, (List) $8, new ObjectNode((NameNode) $1));
   }
 ;

DimExprs :
    DimExpr
    { $$ = cons($1); }
  | DimExpr DimExprs
    { $$ = cons($1, (List) $2); }
  ;

DimExpr :
  '[' Expression ']'
  { $$ = $2; }
  ;

DimsOpt :
    Dims  { }
  | empty
    { $$ = 0; }
  ;

Dims :
    EMPTY_DIM
    { $$ = 1; }
  | Dims EMPTY_DIM
    { $$ = $1 + 1; }
  ;


/* Section 9.9 */

PostfixExpression :
    PrimaryExpression
  | PostIncrement
  | PostDecrement
  ;

PostIncrement :
    PostfixExpression PLUSPLUS
    { $$ = new PostIncrNode((ExprNode) $1); }
  ;

PostDecrement :
    PostfixExpression MINUSMINUS
    { $$ = new PostDecrNode((ExprNode) $1); }
  ;


/* Section 9.10 */

UnaryExpression :
    PreIncrement
  | PreDecrement
  | '+' UnaryExpression
    { $$ = new UnaryPlusNode((ExprNode) $2); }
  | '-' UnaryExpression
    { $$ = new UnaryMinusNode((ExprNode) $2); }
  | UnaryExpressionNotPlusMinus
  ;

PreIncrement :
    PLUSPLUS UnaryExpression
    { $$ = new PreIncrNode((ExprNode) $2); }
  ;

PreDecrement :
    MINUSMINUS UnaryExpression
    { $$ = new PreDecrNode((ExprNode) $2); }
  ;

UnaryExpressionNotPlusMinus :
    PostfixExpression
  | '~' UnaryExpression
    { $$ = new ComplementNode((ExprNode) $2); }
  | '!' UnaryExpression
    { $$ = new NotNode((ExprNode) $2); }
  | CastExpression
  ;

CastExpression :
    '(' PrimitiveType ')' UnaryExpression
    { $$ = new CastNode((TypeNode) $2, (ExprNode) $4); }
  | '(' ReferenceType ')' UnaryExpressionNotPlusMinus
    { $$ = new CastNode((TypeNode) $2, (ExprNode) $4); }
  | '(' Name ')' UnaryExpressionNotPlusMinus
    { $$ = new CastNode(new TypeNameNode((NameNode) $2), (ExprNode) $4); }
  ;
/* Note: The last production is redundant, but helps resolve a LALR(1) */
/* lookahead conflict arising in cases like "(T) + x" (Do we reduce Name  */
/* T to ClassOrInterfaceType on seeing the ")"?). See also ComplexPrimary */
/* in section 9.4.  */


/* Sections 9.11 to 9.19 */

ExpressionOpt :
    Expression  { }
  | empty
    { $$ = AbsentTreeNode.instance; }
  ;

Expression :
    UnaryExpression
        | Expression '*' Expression
    { $$ = new MultNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '/' Expression
    { $$ = new DivNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '%' Expression
    { $$ = new RemNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '+' Expression
    { $$ = new PlusNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '-' Expression
    { $$ = new MinusNode((ExprNode) $1, (ExprNode) $3); }
  | Expression LSHIFTL Expression
    { $$ = new LeftShiftLogNode((ExprNode) $1, (ExprNode) $3); }
  | Expression LSHIFTR Expression
    { $$ = new RightShiftLogNode((ExprNode) $1, (ExprNode) $3); }
  | Expression ASHIFTR Expression
    { $$ = new RightShiftArithNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '<' Expression
    { $$ = new LTNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '>' Expression
    { $$ = new GTNode((ExprNode) $1, (ExprNode) $3); }
  | Expression LE Expression
    { $$ = new LENode((ExprNode) $1, (ExprNode) $3); }
  | Expression GE Expression
    { $$ = new GENode((ExprNode) $1, (ExprNode) $3); }
  | Expression INSTANCEOF ReferenceType
    { $$ = new InstanceOfNode((ExprNode) $1, (TypeNode) $3); }
  | Expression EQ Expression
    { $$ = new EQNode((ExprNode) $1, (ExprNode) $3); }
  | Expression NE Expression
    { $$ = new NENode((ExprNode) $1, (ExprNode) $3); }
  | Expression '&' Expression
    { $$ = new BitAndNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '|' Expression
    { $$ = new BitOrNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '^' Expression
    { $$ = new BitXorNode((ExprNode) $1, (ExprNode) $3); }
  | Expression CAND Expression
    { $$ = new CandNode((ExprNode) $1, (ExprNode) $3); }
  | Expression COR Expression
    { $$ = new CorNode((ExprNode) $1, (ExprNode) $3); }
  | Expression '?' Expression ':' Expression
    { $$ = new IfExprNode((ExprNode) $1, (ExprNode) $3, (ExprNode) $5); }
  | Assignment
  ;


/* Section 9.20 */

Assignment :
    UnaryExpression '=' Expression
    { $$ = new AssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression MULT_ASG Expression
    { $$ = new MultAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression DIV_ASG Expression
    { $$ = new DivAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression REM_ASG Expression
    { $$ = new RemAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression PLUS_ASG Expression
    { $$ = new PlusAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression MINUS_ASG Expression
    { $$ = new MinusAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression LSHIFTL_ASG Expression
    { $$ = new LeftShiftLogAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression LSHIFTR_ASG Expression
    { $$ = new RightShiftLogAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression ASHIFTR_ASG Expression
    { $$ = new RightShiftArithAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression AND_ASG Expression
    { $$ = new BitAndAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression XOR_ASG Expression
    { $$ = new BitXorAssignNode((ExprNode) $1, (ExprNode) $3); }
  | UnaryExpression OR_ASG Expression
    { $$ = new BitOrAssignNode((ExprNode) $1, (ExprNode) $3); }
  ;


/* Section 9.22 */

ConstantExpression :
    Expression
  ;

       /* MISCELLANEOUS */

empty :
      ;

%%

protected void init(String filename) throws IOException {
  _filename = filename;
  _lexer = new Yylex(new FileInputStream(_filename));
}

protected int yylex()
{
  int retval;

  try {
    retval = _lexer.yylex();

    yylval = _lexer.getParserVal();

  } catch (IOException e) {
    throw new RuntimeException("lexical error");
  }

  return retval;
}

protected static final List cons(Object obj)
{
  return cons(obj, new LinkedList());
}

protected static final List cons(Object obj, List list)
{
  if ((obj != null) && (obj != AbsentTreeNode.instance)) {
     list.add(0, obj);
  }

  return list;
}

protected static final List append(List list, Object obj)
{
  list.add(obj);
  return list;
}


protected static final Object appendLists(List list1, List list2)
{
  list1.addAll(list2);
  return list1;
}

/** Place to put the finished AST. */
protected CompileUnitNode _theAST;

public CompileUnitNode getAST() { return _theAST; }

protected void yyerror(String msg)
{
  String errMsg = "parse error in " + _filename + ": " + msg;
  if (_lexer != null) {
     errMsg += " on line " + _lexer.lineNumber();
  }
  ApplicationUtility.error(errMsg);
}

/** An array type with given ELEMENTTYPE and DIMS dimensions.  When
 *  DIMS=0, equals ELEMENTTYPE.
 */
protected static TypeNode makeArrayType(TypeNode elementType, int dims)
{
  while (dims > 0) {
     elementType = new ArrayTypeNode(elementType);
     dims -= 1;
  }
  return elementType;
}

protected String _filename = null;
protected Yylex _lexer = null;
