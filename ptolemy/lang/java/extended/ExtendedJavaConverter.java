/*          
A JavaVisitor that converts Extended Java to Java by adding conversions
and calling special methods in ptolemy.math.

Copyright (c) 1998-1999 The Regents of the University of California.
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


package ptolemy.lang.java.extended;

import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A JavaVisitor that converts Extended Java to Java by adding conversions
 *  and calling special methods in ptolemy.math.
 *
 *  @author Jeff Tsay
 */ 
public class ExtendedJavaConverter extends ReplacementJavaVisitor 
     implements JavaStaticSemanticConstants {
    
    public ExtendedJavaConverter() {
        super(TM_CHILDREN_FIRST);
        _typeID = new ExtendedJavaTypeIdentifier();
        _typeVisitor = new TypeVisitor(new ExtendedJavaTypePolicy());    
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        int returnKind = _typeID.kind(_typeVisitor.type(node));
        
        return convertExprToKind(node.getExpr(), returnKind);        
    } 

    public Object visitMultNode(MultNode node, LinkedList args) {
        ExprNode expr1 = node.getExpr1();
        ExprNode expr2 = node.getExpr2();
        
        int kind1 = _typeID.kind(_typeVisitor.type(expr1));
        int kind2 = _typeID.kind(_typeVisitor.type(expr2));
        
        switch (kind1) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
            return new CandNode(expr1, expr2);            
          }
          ApplicationUtility.error("boolean * other type not supported");          
          break;
                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:      
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                    
            node.setExpr1(convertExprToKind(expr1, kind2));
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                          
            return new MethodCallNode(
             new ObjectFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "scale"), expr2),
             TNLManip.cons(convertExprToKind(expr1, 
              ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE)));
          } 
          ApplicationUtility.error("int * other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));            
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
            return node;                                                      
          } 
          ApplicationUtility.error("long * other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));
            return node;
                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "scale"), expr2),
             TNLManip.cons(convertExprToKind(expr1, 
              ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE))); 
          } 
          ApplicationUtility.error("double * other type not supported");          
          break;
                                        
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                                
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "scale"), expr1),
              TNLManip.cons(convertExprToKind(expr2, 
               ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE))); 
                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                          
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "multiply"), expr1),
             TNLManip.cons(expr2)); 
          } 
          ApplicationUtility.error("complex * other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                            
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                        
            return new MethodCallNode(
             new ObjectFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "multiply"), expr1),
             TNLManip.cons(expr2));             
          } 
          ApplicationUtility.error("fix point * other type not supported");          
          break;
                                                 
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:                 
          // nothing supported yet
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:                     
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                                      
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          {
            int returnKind = _typeID.kind(_typeVisitor.type(node));
          
            LinkedList methodArgs = new LinkedList();
            methodArgs.addLast(convertExprToKind(expr1, returnKind));
            methodArgs.addLast(convertExprToKind(expr2, returnKind));
                      
            return new MethodCallNode(
             new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "multiply"),
              matrixMathClassForKind(returnKind)),
             methodArgs);
          }                                   
        }
        ApplicationUtility.error("MultNode contains one or more unknown types");
        return null;
    }

    public Object visitDivNode(DivNode node, LinkedList args) {
        ExprNode expr1 = node.getExpr1();
        ExprNode expr2 = node.getExpr2();
        
        int kind1 = _typeID.kind(_typeVisitor.type(expr1));
        int kind2 = _typeID.kind(_typeVisitor.type(expr2));
        
        switch (kind1) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
            // what should we do here?
            break;
          }
          ApplicationUtility.error("boolean / other type not supported");          
          break;
                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:      
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                    
            node.setExpr1(convertExprToKind(expr1, kind2));
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                                                  
            return new MethodCallNode(
             new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "divide"), 
               convertExprToKind(expr1, kind2)),
             TNLManip.cons(expr2)); 
          } 
          ApplicationUtility.error("int / other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));            
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
            return node;                                                      
          } 
          ApplicationUtility.error("long / other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));
            return node;
                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
            return new MethodCallNode(
             new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "divide"), 
               convertExprToKind(expr1, ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX)),
             TNLManip.cons(expr2));                         
          } 
          ApplicationUtility.error("double * other type not supported");          
          break;
                                        
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                                
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "scale"), expr1),
              TNLManip.cons(
               new DivNode(new DoubleLitNode("1.0"),
               convertExprToKind(expr2, 
                ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE)))); 
                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                          
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "divide"), expr1),
             TNLManip.cons(expr2)); 
          } 
          ApplicationUtility.error("complex / other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                            
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                        
            return new MethodCallNode(
             new ObjectFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "divide"), expr1),
             TNLManip.cons(expr2));             
          } 
          ApplicationUtility.error("fix point / other type not supported");          
          break;
                                                 
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:                                     
          // nothing supported yet
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:                     
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                                      
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          // what is the meaning of divide in these cases?
          break;                              
        }
        ApplicationUtility.error("DivNode contains one or more unknown types");
        return null;
    }
    
    public Object visitRemNode(RemNode node, LinkedList args) {
        ExprNode expr1 = node.getExpr1();
        ExprNode expr2 = node.getExpr2();
        
        int kind1 = _typeID.kind(_typeVisitor.type(expr1));
        int kind2 = _typeID.kind(_typeVisitor.type(expr2));
        
        switch (kind1) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:             
          ApplicationUtility.error("boolean % other type not supported");          
          break;
                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:      
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                    
            node.setExpr1(convertExprToKind(expr1, kind2));
            return node;            
          } 
          ApplicationUtility.error("int % other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));            
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
            return node;                                                      
          } 
          ApplicationUtility.error("long % x not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
          ApplicationUtility.error("double % x not supported");          
          break;          
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
          ApplicationUtility.error("complex % x not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                            
          ApplicationUtility.error("fix point % x not supported");          
          break;
                                                 
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:                                     
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:                     
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                                      
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          ApplicationUtility.error("matrix % x not supported");          
          break;                              
        }
        ApplicationUtility.error("RemNode contains one or more unknown types");
        return null;            
    }
    
    public Object visitPlusNode(PlusNode node, LinkedList args) {            
        ExprNode expr1 = node.getExpr1();
        ExprNode expr2 = node.getExpr2();
        
        int kind1 = _typeID.kind(_typeVisitor.type(expr1));
        int kind2 = _typeID.kind(_typeVisitor.type(expr2));
                
        // support for string concatenation
        // convert expresssions to strings (at most one will actually be changed)       
        if ((kind1 == ExtendedJavaTypeIdentifier.TYPE_KIND_STRING) ||
            (kind2 == ExtendedJavaTypeIdentifier.TYPE_KIND_STRING)) {
           node.setExpr1(convertExprToKind(expr1, 
            ExtendedJavaTypeIdentifier.TYPE_KIND_STRING));   
           node.setExpr2(convertExprToKind(expr2, 
            ExtendedJavaTypeIdentifier.TYPE_KIND_STRING));   
           return node;            
        }
        
        switch (kind1) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
            // convert to XOR
            return new CandNode(new CorNode(expr1, expr2),
             new NotNode(new CandNode(expr1, expr2)));                        
          }
          ApplicationUtility.error("boolean + other type not supported");          
          break;
                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:      
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                    
            node.setExpr1(convertExprToKind(expr1, kind2));
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                          
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
               AbsentTreeNode.instance, "add"), expr2),
              TNLManip.cons(convertExprToKind(expr1, kind2))); 
          } 
          ApplicationUtility.error("int + other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));            
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
            return node;                        
          } 
          ApplicationUtility.error("long + other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));
            return node;
                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "add"), expr2),
             TNLManip.cons(convertExprToKind(expr1, kind2))); 
          } 
          ApplicationUtility.error("double + other type not supported");          
          break;
                                        
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                     
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                                                                                                 
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "add"), expr1),
              TNLManip.cons(convertExprToKind(expr2, kind1)));                                                  
          } 
          ApplicationUtility.error("complex + other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                            
          switch (kind2) {          
            case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                        
            return new MethodCallNode(
             new ObjectFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "add"), expr1),
             TNLManip.cons(expr2));             
          } 
          ApplicationUtility.error("fix point + other type not supported");          
          break;
                                                 
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:                 
          // nothing supported yet
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:                     
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                                      
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          {
            int returnKind = _typeID.kind(_typeVisitor.type(node));
          
            LinkedList methodArgs = new LinkedList();
            methodArgs.addLast(convertExprToKind(expr1, returnKind));
            methodArgs.addLast(convertExprToKind(expr2, returnKind));
                      
            return new MethodCallNode(
             new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "add"),
              matrixMathClassForKind(returnKind)),
             methodArgs);
          }                                   
        }
        ApplicationUtility.error("PlusNode contains one or more unknown types");
        return null; 
    }


    public Object visitMinusNode(MinusNode node, LinkedList args) {
        ExprNode expr1 = node.getExpr1();
        ExprNode expr2 = node.getExpr2();
        
        int kind1 = _typeID.kind(_typeVisitor.type(expr1));
        int kind2 = _typeID.kind(_typeVisitor.type(expr2));
                        
        switch (kind1) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
            // convert to XOR
            return new CandNode(new CorNode(expr1, expr2),
             new NotNode(new CandNode(expr1, expr2)));                        
          }
          ApplicationUtility.error("boolean - other type not supported");          
          break;
                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:      
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                                                                    
            node.setExpr1(convertExprToKind(expr1, kind2));
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                          
            return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "subtract"), 
               convertExprToKind(expr1, kind2)),
              TNLManip.cons(expr2)); 
          } 
          ApplicationUtility.error("int - other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));            
            return node;
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:     
            return node;                        
          } 
          ApplicationUtility.error("long - other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:
            node.setExpr2(convertExprToKind(expr2, kind1));
            return node;
                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
            return node;
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
            return new MethodCallNode(
              new ObjectFieldAccessNode(
               new NameNode(AbsentTreeNode.instance, "subtract"), 
               convertExprToKind(expr1, kind2)),
              TNLManip.cons(expr2)); 
          } 
          ApplicationUtility.error("double - other type not supported");          
          break;
                                        
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:  
          switch (kind2) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:                                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:                     
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                                                                                                 
            return new MethodCallNode(
             new ObjectFieldAccessNode(new NameNode(
              AbsentTreeNode.instance, "subtract"), expr1),
              TNLManip.cons(convertExprToKind(expr2, kind1)));                                                  
          } 
          ApplicationUtility.error("complex - other type not supported");          
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                            
          switch (kind2) {          
            case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:                        
            return new MethodCallNode(
             new ObjectFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "subtract"), expr1),
             TNLManip.cons(expr2));             
          } 
          ApplicationUtility.error("fix point - other type not supported");          
          break;
                                                 
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:                 
          // nothing supported yet
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:                    
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:                     
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                                      
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          {
            int returnKind = _typeID.kind(_typeVisitor.type(node));
          
            LinkedList methodArgs = new LinkedList();
            methodArgs.addLast(convertExprToKind(expr1, returnKind));
            methodArgs.addLast(convertExprToKind(expr2, returnKind));
                      
            return new MethodCallNode(
             new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "subtract"),
              matrixMathClassForKind(returnKind)),
             methodArgs);
          }                                   
        }
        ApplicationUtility.error("MinusNode contains one or more unknown types");
        return null; 
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        int returnKind = _typeID.kind(_typeVisitor.type(node));
        
        node.setExpr2(convertExprToKind(node.getExpr2(), returnKind));                
        return node;
    }

    /** Return the TypeNameNode corresponding to the class containing matrix math 
     *  methods for the argument kind.
     */ 
    public TypeNameNode matrixMathClassForKind(int kind) {
        String className = null;
        
        switch (kind) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:
          className = "BooleanMatrixMath"; // this class does not exist yet
          break;
               
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:
          className = "IntegerMatrixMath";
          break;

          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:
          className = "LongMatrixMath";
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:
          className = "DoubleMatrixMath";
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:
          className = "ComplexMatrixMath";
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          className = "FixPointMatrixMath"; // this class does not exist yet
          break;                          
          
          default:
          ApplicationUtility.error("kind is not a matrix kind");
        }
                
        return new TypeNameNode(
         new NameNode(AbsentTreeNode.instance, className));
    }

    public ExprNode convertExprToKind(ExprNode expr, int targetKind) {
        int exprKind = _typeID.kind(_typeVisitor.type(expr));
        
        // if they are the same kind, do no conversion
        if (exprKind == targetKind) return expr;
        
        switch (exprKind) {
          case ExtendedJavaTypeIdentifier.TYPE_KIND_CLASS:
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INTERFACE:
          if (targetKind == ExtendedJavaTypeIdentifier.TYPE_KIND_STRING) {
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "valueOf"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String"))),
             TNLManip.cons(expr));                      
          }
          break;
        
          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN:   
          switch (targetKind) {            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "valueOf"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String"))),
             TNLManip.cons(expr));            
          }
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT:      
          switch (targetKind) {            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:            
            return new CastNode(LongTypeNode.instance, expr);
                                                
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:   
            return new CastNode(DoubleTypeNode.instance, expr);

            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "valueOf"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String"))),
             TNLManip.cons(expr));            
            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:            
            return new AllocateNode((TypeNameNode)
              ExtendedJavaTypeIdentifier.COMPLEX_TYPE.clone(),
              TNLManip.cons(new CastNode(DoubleTypeNode.instance, expr)), 
               AbsentTreeNode.instance);                      
                         
            case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:                    
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:                     
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                                      
            case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
            // these are the cases in which dummy values of 0 and 1 are used
            // when resolving Token.zero(), Token.one(), and Token.oneRight()
            // they are replaced by a null pointer here
            System.out.println("found int -> matrix conversion");
            if (expr.classID() == INTLITNODE_ID) {
               System.out.println("found literal node in int -> matrix conversion");
                        
               IntLitNode intLitExpr = (IntLitNode) expr;
               
               String literal = intLitExpr.getLiteral();
               
               System.out.println("literal = " + literal);
               
               if (literal.equals("0") || literal.equals("1")) {
                  return new NullPntrNode();               
               }
            }
            break;                    
          }
          break;

          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE:      
          switch (targetKind) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "valueOf"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String"))),
             TNLManip.cons(expr));            
                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:            
            return new AllocateNode((TypeNameNode)
              ExtendedJavaTypeIdentifier.COMPLEX_TYPE.clone(),
              TNLManip.cons(expr), AbsentTreeNode.instance);                              
          }
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG:            
          switch (targetKind) {                        
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "valueOf"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String"))),
             TNLManip.cons(expr));            
            
          }
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX:                      
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT:            
          switch (targetKind) {            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            // we could call toString() directly if the expr is known to non-null, but..
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "valueOf"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String"))),
             TNLManip.cons(expr));            
          }
          break;

          case ExtendedJavaTypeIdentifier.TYPE_KIND_BOOLEAN_MATRIX:                 
          switch (targetKind) {            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            // not supported yet
            break;            
          }
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_INT_MATRIX:
          switch (targetKind) {            
            case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:     
            return new MethodCallNode(new TypeFieldAccessNode(
             new NameNode(AbsentTreeNode.instance, "toLongMatrix"),
             new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
              "IntegerMatrixMath"))),
             TNLManip.cons(expr));             
                                                                   
            case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:   
            return new MethodCallNode(new TypeFieldAccessNode(
             new NameNode(AbsentTreeNode.instance, "toDoubleMatrix"),
             new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
              "IntegerMatrixMath"))),
             TNLManip.cons(expr));             
                  
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "toString"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
               "IntegerMatrixMath"))),
             TNLManip.cons(expr));    
                                                     
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:            
            // need to add the toComplexMatrix() method to IntegerMatrixMath 
            return new MethodCallNode(new TypeFieldAccessNode(
             new NameNode(AbsentTreeNode.instance, "toComplexMatrix"),
             new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
              "IntegerMatrixMath"))),
             TNLManip.cons(expr));                                     
          }
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_LONG_MATRIX:           
          switch (targetKind) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "toString"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
               "LongMatrixMath"))),
             TNLManip.cons(expr));    
          }                    
          break;
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_DOUBLE_MATRIX:                            
          switch (targetKind) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "toString"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
               "DoubleMatrixMath"))),
             TNLManip.cons(expr));    
                                                
            case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:            
            // need to add the toComplexMatrix() method to IntegerMatrixMath 
            return new MethodCallNode(new TypeFieldAccessNode(
             new NameNode(AbsentTreeNode.instance, "toComplexMatrix"),
             new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
              "DoubleMatrixMath"))),
             TNLManip.cons(expr));                                     
          }
          break;
                              
          case ExtendedJavaTypeIdentifier.TYPE_KIND_COMPLEX_MATRIX:                            
          switch (targetKind) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "toString"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
               "ComplexMatrixMath"))),
             TNLManip.cons(expr));                
          }
          break; 
          
          case ExtendedJavaTypeIdentifier.TYPE_KIND_FIX_POINT_MATRIX:
          switch (targetKind) {
            case ExtendedJavaTypeIdentifier.TYPE_KIND_STRING:               
            return new MethodCallNode(new TypeFieldAccessNode(
              new NameNode(AbsentTreeNode.instance, "toString"),
              new TypeNameNode(new NameNode(AbsentTreeNode.instance, 
               "FixPointMatrixMath"))), // this class does not exist yet
             TNLManip.cons(expr));                
          }
          break;                    
        }
        ApplicationUtility.error("cannot convert kind " + exprKind + 
         " to kind " + targetKind);                  
        return null; 
    }


    protected TypeIdentifier _typeID;
    protected TypeVisitor _typeVisitor;
}