/* A Java AST visitor that finds the most specific Token type allowable for
   variables declared as abstract Tokens (Token, ScalarToken, and MatrixToken).

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.domains.sdf.codegen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.graph.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A Java AST visitor that finds the most specific Token type allowable for
 *  variables declared as abstract Tokens (Token, ScalarToken, and MatrixToken).
 *
 *  @author Jeff Tsay
 */
public class SpecializeTokenVisitor extends ResolveVisitorBase {

    // inner classes 
    
    class ConstantTerm implements InequalityTerm {
        public ConstantTerm(ClassDecl classDecl, TypedDecl decl) {
            _classDecl = classDecl;
            _decl = decl;            
        }        
        
        public void fixValue() {}
        
        public Object getValue() { return _classDecl; }

        public Object getAssociatedObject() { return _decl; }
                
        // Constant terms do not contain any variables
        public InequalityTerm[] getVariables() { return new InequalityTerm[0]; }
        
        public void initialize(Object e) throws IllegalActionException {
            setValue(e);
        }
        
        // Constant terms are not settable
        public boolean isSettable() { return false; }
        
        public boolean isValueAcceptable() { return true; }
        
        public void setValue(Object e) throws IllegalActionException {
            throw new IllegalActionException(
             "ConstantTerm.setValue(): This term is a constant");
        }

        public String toString() {
            return "ConstantTerm: value = " + _classDecl.getName() + ", _decl = " + _decl;          
        }
        
        public void unfixValue() {}
        
        private ClassDecl _classDecl;
        private TypedDecl _decl;    
    }
    
    class VariableTerm implements InequalityTerm {
        public VariableTerm(ClassDecl classDecl, TypedDecl decl) {
            _classDecl = classDecl;
            _decl = decl;            
        }        
        
        public void fixValue() { _fixed = true; }
                
        public Object getValue() { return _classDecl; }
        
        public Object getAssociatedObject() { return _decl; }
        
        public InequalityTerm[] getVariables() { 
            return new InequalityTerm[] { this }; 
        }

        public void initialize(Object e) {
            setValue(e);
        }
        
        // Variable terms are settable
        public boolean isSettable() { return true; }

        public boolean isValueAcceptable() { return true; }        
        
        public void setValue(Object e) {
            if (!_fixed) {
               _classDecl = (ClassDecl) e;
            }
        }
        
        public String toString() {
            return "VariableTerm: decl = " + _decl + ", value = " + _classDecl.getName();          
        }
        
        public void unfixValue() { _fixed = false; }
        
        private ClassDecl _classDecl;
        private TypedDecl _decl;    
        private boolean _fixed = false;
    }
    
    public SpecializeTokenVisitor(PerActorCodeGeneratorInfo actorInfo) {    
        super(TM_CUSTOM);
        _solver = new InequalitySolver(_cpo);                        
        _actorInfo = actorInfo;
        _typeVisitor = new PtolemyTypeVisitor(actorInfo);                                
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return null;
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitArrayInitTypeNode(ArrayInitTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {    
        TNLManip.traverseList(this, node, null, node.getDefTypes());
        
        boolean ok = _solver.solveLeast();
        
        if (!ok) {
           ApplicationUtility.warn("unable to solve for Token types");
           
           Iterator unsatisfiedItr = _solver.unsatisfiedInequalities();
           
           System.out.println("unsatisfied inequalities:");
           
           while (unsatisfiedItr.hasNext()) {
               System.out.println(unsatisfiedItr.next());
           } 
           System.out.println("end of unsatisfied inequalities:");
        }
        
        HashMap declToTokenTypeMap= new HashMap();
        
        Iterator termItr = _declToTermMap.values().iterator();
        
        while (termItr.hasNext()) {
           InequalityTerm term = (InequalityTerm) termItr.next();
           
           System.out.println(term);
           
           ClassDecl value = (ClassDecl) term.getValue();
           
           if (value != PtolemyTypeVisitor.DUMMY_LOWER_BOUND) {
           
              TypeNameNode typeNode = value.getDefType();
                      
              declToTokenTypeMap.put(term.getAssociatedObject(), typeNode);        
           }
        }
        
        return declToTokenTypeMap;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {   
        return _visitUserTypeDeclNode(node);
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);                           
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        // make sure we process the parameters first
        TNLManip.traverseList(this, node, null, node.getParams());
        
        node.getBody().accept(this, null);
        
        return null;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        // make sure we process the parameters first
        TNLManip.traverseList(this, node, null, node.getParams());
        
        node.getBody().accept(this, null);
        
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node);
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        TypeNode type = node.getDefType();
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) node);
                                                      
        InequalityTerm term = _makeVariableTerm(type, typedDecl);                                      
                
        if (term != null) {        
           // add to the map from decls to inequality terms
           _declToTermMap.put(typedDecl, term);           
           
           // constrain the type (it must be added the inequalities)          
           _solver.addInequality(new Inequality(term, _makeConstantTerm(type, null)));
        }                                                 
        return null;
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return _visitVariableNode(node);    
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        InequalityTerm retval = (InequalityTerm) _visitExprNode(node);
        List argTerms = TNLManip.traverseList(this, node, null, node.getArgs());
        
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();
        
        ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);
        
        // if this is a static method call, we can't do any more.
        if (accessedObj == null) {
           return node;
        }
        
        int numArgs = argTerms.size();

        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);
        
        String methodName = methodDecl.getName();
        
        int accessedObjKind = _typeVisitor.kind(_typeVisitor.type(accessedObj));
                
        if (_typeVisitor.isSupportedTokenKind(accessedObjKind)) {
           if (numArgs == 1) {
              if (methodName.equals("add") || methodName.equals("addReverse") ||
                  methodName.equals("subtract") || methodName.equals("subtractReverse") ||
                  methodName.equals("multiply") || methodName.equals("multiplyReverse") || 
                  methodName.equals("divide") || methodName.equals("divideReverse") ||
                  methodName.equals("modulo") || methodName.equals("moduloReverse")) {
                 // constrain the return value to be >= both the accessedObject and
                 // the first argument
              
                 // make sure we constrain a node that appears in the original parse tree
                 // so that we can run this visitor on it
                 if (fieldAccessNode.classID() == OBJECTFIELDACCESSNODE_ID) {
                    Object accessedObjTermObj = accessedObj.accept(this, null);
                 
                    if (accessedObjTermObj instanceof InequalityTerm) {
                      _solver.addInequality(new Inequality(
                       (InequalityTerm) accessedObjTermObj, retval));                 
                    }                 
                 }
                
                 Object firstArgTermObj = argTerms.get(0);
                 if (firstArgTermObj instanceof InequalityTerm) {
                    InequalityTerm firstArgTerm = (InequalityTerm) firstArgTermObj;              
                    _solver.addInequality(new Inequality(firstArgTerm, retval));
                 }
                 
              } else if (methodName.equals("convert")) {
                 // constrain the return value to be the >= than the accessedObject 
                 // also constain the first argument to be <= the accessedObject              

                 // make sure we constrain a node that appears in the original parse tree
                 // so that we can run this visitor on it
                 if (fieldAccessNode.classID() == OBJECTFIELDACCESSNODE_ID) {
                    Object accessedObjTermObj = accessedObj.accept(this, null);
                 
                    if (accessedObjTermObj instanceof InequalityTerm) {
                       InequalityTerm accessedObjTerm = (InequalityTerm) accessedObjTermObj;
                       _solver.addInequality(new Inequality(accessedObjTerm, retval));                 

                       Object firstArgTermObj = argTerms.get(0);
                       if (firstArgTermObj instanceof InequalityTerm) {
                          InequalityTerm firstArgTerm = (InequalityTerm) firstArgTermObj;              
                         _solver.addInequality(new Inequality(firstArgTerm, accessedObjTerm));
                       }                                              
                    }                 
                 }                 
              }
           } // numArgs == 1
                                 
        } else if (_typeVisitor.isSupportedPortKind(accessedObjKind)) {                      
          // use the resolved type of ports to do type inference
          switch (accessedObj.classID()) {
              case THISFIELDACCESSNODE_ID:
              case SUPERFIELDACCESSNODE_ID: // CHECKME : is this right?
              {
                TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
                String varName = typedDecl.getName();
                
                TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);                 
                TypeNameNode portTypeNode = 
                 _typeVisitor.typeNodeForTokenType(port.getType()); 
                
                if (methodName.equals("get")) {                                                   
                   return _makeConstantTerm(portTypeNode, null);
                } else if (methodName.equals("send")) {
                   // second argument is a token, constrain it
                   Object termObj = argTerms.get(1);
                   if (termObj instanceof InequalityTerm) {
                      _solver.addInequality(new Inequality((InequalityTerm) termObj,
                       _makeConstantTerm(portTypeNode, null)));
                      return null; // return type is null
                   }                
                }
                // support getArray ...                                                                
              }                        
          }                                                   
        } else if (accessedObjKind == PtolemyTypeVisitor.TYPE_KIND_PARAMETER) {
          // use the tokens returned by parameters to do type inference
          switch (accessedObj.classID()) {
              case THISFIELDACCESSNODE_ID:
              case SUPERFIELDACCESSNODE_ID: // CHECKME : is this right?
              {
                if (methodName.equals("getToken")){
                   TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
                   String varName = typedDecl.getName();
                
                   Token token = (Token) _actorInfo.parameterNameToTokenMap.get(varName);
                  
                   TypeNameNode tokenTypeNode = 
                    _typeVisitor.typeNodeForTokenType(token.getType());
                                 
                   return _makeConstantTerm(tokenTypeNode, null); 
                }
              }                  
          }
        }
        
        return retval;
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitComplementNode(ComplementNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitNotNode(NotNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        // assume that all casts succeed
                
        InequalityTerm term = (InequalityTerm) node.getExpr().accept(this, null);
        
        InequalityTerm retval = _makeVariableTerm(node.getDtype(), null);
        
        if (term != null) {
           _solver.addInequality(new Inequality(term, retval));                           
        }        
        return retval;           
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitCandNode(CandNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitCorNode(CorNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        InequalityTerm leftTerm = (InequalityTerm) node.getExpr1().accept(this, null);               
        InequalityTerm rightTerm = (InequalityTerm) node.getExpr2().accept(this, null);               
        
        if ((leftTerm != null) && (rightTerm != null)) {
           _solver.addInequality(new Inequality(rightTerm, leftTerm));
        }
    
        return leftTerm;    
    }

    protected InequalityTerm _visitUserTypeDeclNode(UserTypeDeclNode node) {
        // visit the fields first to initialize the decl -> inequality term map
        
        List memberList = node.getMembers();
        
        Iterator memberItr = memberList.iterator();
        
        while (memberItr.hasNext()) {
            TreeNode member = (TreeNode) memberItr.next();
            
            if (member.classID() == FIELDDECLNODE_ID) {
               visitFieldDeclNode((FieldDeclNode) member, null);               
            }        
        }

        // visit the rest of the members 
        
        memberItr = memberList.iterator();
        
        while (memberItr.hasNext()) {
            TreeNode member = (TreeNode) memberItr.next();
            
            if (member.classID() != FIELDDECLNODE_ID) {
               member.accept(this, null);               
            }        
        }
        return null;
    }    

    protected InequalityTerm _visitExprNode(ExprNode node) {
        _defaultVisit(node, null);
        return _makeVariableTerm(_typeVisitor.type(node), null);
    }
    
    protected InequalityTerm _visitVariableNode(NamedNode node) {
        _defaultVisit((TreeNode) node, null);

        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl(node);
        
        InequalityTerm term = (InequalityTerm) _declToTermMap.get(typedDecl); // may be null
        return term;    
    }
    
    protected Object _visitVarInitDeclNode(VarInitDeclNode node) {
        TypeNode type = node.getDefType();
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) node);
                                                      
        InequalityTerm term = _makeVariableTerm(type, typedDecl);                                      
        InequalityTerm initExprTerm = (InequalityTerm) node.getInitExpr().accept(this, null);
                
        if (term != null) {        
           // add to the map from decls to inequality terms
           _declToTermMap.put(typedDecl, term);           
           
           // constrain the type (it must be added the inequalities)          
           _solver.addInequality(new Inequality(term, _makeConstantTerm(type, null)));
                      
            if (initExprTerm != null) {
               _solver.addInequality(new Inequality(initExprTerm, term));
            }                            
        }                                                 
        return null;
    }
    
    /** Return an inequality term corresponding to the given type, with
     *  the associated decl, which may be null. If type is not a supported
     *  token type, return null.
     */
    protected VariableTerm _makeVariableTerm(TypeNode type, TypedDecl decl) {
        int kind = _typeVisitor.kind(type);
        if (!_typeVisitor.isSupportedTokenKind(kind)) {
           return null;
        } 
        /*
        if (_typeVisitor.isConcreteTokenType(kind)) {
           // concrete token
           return new ConstantTerm((TypeNameNode) type, decl);                            
        } 
        // abstract token
        */
        
        return new VariableTerm((ClassDecl) JavaDecl.getDecl((NamedNode) type), decl);       
    }
    
    protected ConstantTerm _makeConstantTerm(TypeNode type, TypedDecl decl) {
        int kind = _typeVisitor.kind(type);
        if (!_typeVisitor.isSupportedTokenKind(kind)) {
           return null;
        } 
        
        return new ConstantTerm((ClassDecl) JavaDecl.getDecl((NamedNode) type), decl);       
    }
        
    protected PerActorCodeGeneratorInfo _actorInfo;
    protected PtolemyTypeVisitor _typeVisitor;
        
    /** A Map from TypeDecls to InequalityTerms. */
    private HashMap _declToTermMap = new HashMap();
    
    
    private static final DirectedAcyclicGraph _cpo; 
    private final InequalitySolver _solver;
    
    static {
        // construct the type lattice
        _cpo = new DirectedAcyclicGraph(18);
           
        _cpo.add(PtolemyTypeVisitor.DUMMY_LOWER_BOUND); 
     
	    _cpo.add(PtolemyTypeVisitor.BOOLEAN_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.BOOLEAN_MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.COMPLEX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.COMPLEX_MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.DOUBLE_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.DOUBLE_MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.FIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.FIX_MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.INT_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.INT_MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.LONG_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.LONG_MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.MATRIX_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.OBJECT_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.SCALAR_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.STRING_TOKEN_DECL);
	    _cpo.add(PtolemyTypeVisitor.TOKEN_DECL);

	    _cpo.addEdge(PtolemyTypeVisitor.OBJECT_TOKEN_DECL, PtolemyTypeVisitor.TOKEN_DECL);	    
	    _cpo.addEdge(PtolemyTypeVisitor.STRING_TOKEN_DECL, PtolemyTypeVisitor.TOKEN_DECL);
	    _cpo.addEdge(PtolemyTypeVisitor.BOOLEAN_TOKEN_DECL, PtolemyTypeVisitor.TOKEN_DECL);	    
	    _cpo.addEdge(PtolemyTypeVisitor.SCALAR_TOKEN_DECL, PtolemyTypeVisitor.TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeVisitor.MATRIX_TOKEN_DECL, PtolemyTypeVisitor.TOKEN_DECL);	    
	    	    	    		        	                    
	    _cpo.addEdge(PtolemyTypeVisitor.LONG_TOKEN_DECL, PtolemyTypeVisitor.SCALAR_TOKEN_DECL);
	    _cpo.addEdge(PtolemyTypeVisitor.COMPLEX_TOKEN_DECL, PtolemyTypeVisitor.SCALAR_TOKEN_DECL);	    
        _cpo.addEdge(PtolemyTypeVisitor.FIX_TOKEN_DECL, PtolemyTypeVisitor.SCALAR_TOKEN_DECL);	    
	    
	    _cpo.addEdge(PtolemyTypeVisitor.DOUBLE_TOKEN_DECL, PtolemyTypeVisitor.COMPLEX_TOKEN_DECL);						                        	    
	    
	    _cpo.addEdge(PtolemyTypeVisitor.INT_TOKEN_DECL, PtolemyTypeVisitor.LONG_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeVisitor.INT_TOKEN_DECL, PtolemyTypeVisitor.DOUBLE_TOKEN_DECL);	    

	    _cpo.addEdge(PtolemyTypeVisitor.BOOLEAN_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.MATRIX_TOKEN_DECL);
	    _cpo.addEdge(PtolemyTypeVisitor.LONG_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.MATRIX_TOKEN_DECL);
	    _cpo.addEdge(PtolemyTypeVisitor.COMPLEX_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.MATRIX_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeVisitor.FIX_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.MATRIX_TOKEN_DECL);
	    
	    _cpo.addEdge(PtolemyTypeVisitor.DOUBLE_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.COMPLEX_MATRIX_TOKEN_DECL);
	    	    	    
	    _cpo.addEdge(PtolemyTypeVisitor.INT_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.DOUBLE_MATRIX_TOKEN_DECL);
	    
	    _cpo.addEdge(PtolemyTypeVisitor.INT_MATRIX_TOKEN_DECL, PtolemyTypeVisitor.LONG_MATRIX_TOKEN_DECL);
	    	    	  	    
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.OBJECT_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.STRING_TOKEN_DECL);        
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.BOOLEAN_TOKEN_DECL);                
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.INT_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.FIX_TOKEN_DECL);        
        
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.BOOLEAN_MATRIX_TOKEN_DECL);                
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.INT_MATRIX_TOKEN_DECL);        
        _cpo.addEdge(PtolemyTypeVisitor.DUMMY_LOWER_BOUND, PtolemyTypeVisitor.FIX_MATRIX_TOKEN_DECL);
        
        /*                        
        TypeNameNode[] nodes = new TypeNameNode[] 
        { PtolemyTypeVisitor.BOOLEAN_TOKEN_DECL, PtolemyTypeVisitor.BOOLEAN_MATRIX_TOKEN_DECL,
	      PtolemyTypeVisitor.COMPLEX_TOKEN_DECL, PtolemyTypeVisitor.COMPLEX_MATRIX_TOKEN_DECL,
	      PtolemyTypeVisitor.DOUBLE_TOKEN_DECL, PtolemyTypeVisitor.DOUBLE_MATRIX_TOKEN_DECL,	      
	      PtolemyTypeVisitor.FIX_TOKEN_DECL, PtolemyTypeVisitor.FIX_MATRIX_TOKEN_DECL,
	      PtolemyTypeVisitor.INT_TOKEN_DECL, PtolemyTypeVisitor.INT_MATRIX_TOKEN_DECL,
	      PtolemyTypeVisitor.LONG_TOKEN_DECL, PtolemyTypeVisitor.LONG_MATRIX_TOKEN_DECL, 
   	      PtolemyTypeVisitor.MATRIX_TOKEN_DECL, PtolemyTypeVisitor.OBJECT_TOKEN_DECL,
   	      PtolemyTypeVisitor.SCALAR_TOKEN_DECL, PtolemyTypeVisitor.STRING_TOKEN_DECL,
	      PtolemyTypeVisitor.TOKEN_DECL };
	    
	    
	    for (int i = 0; i < nodes.length; i++) {
	        for (int j = i + 1; j < nodes.length; j++) {	        
	            System.out.println("GLB for " + nodes[i].getName() +
	             " and " + nodes[j].getName() + " is " +
	            _cpo.greatestLowerBound(nodes[i], nodes[j]));
	            
                System.out.println("LUB for " + nodes[i].getName() +
	             " and " + nodes[j].getName() + " is " +
	            _cpo.leastUpperBound(nodes[i], nodes[j]));
	            
	        }
	    } 
        */
                                                                                       	    	    	    	    
	    if (!_cpo.isLattice()) {	    
  		   throw new RuntimeException("SpecializeTokenVisitor: The " +
			  "type hierarchy is not a lattice.");
	    }             
    }    
}
