/*
The common base type for nodes in an abstract syntax tree.

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


package ptolemy.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 *  The common base type for nodes in an abstract syntax tree.
 *
 *  @author Jeff Tsay
 */
public abstract class TreeNode extends TrackedPropertyMap {

    /** Return the class ID number, which is unique for each sub-type. */    
    public abstract int classID();

    /** Return a String representation of this node, indented by ident. 
     *  Call the toString() method of all child nodes.
     */
    public String toString(String indent) {

        StringBuffer sb = new StringBuffer();

        Class c = getClass();
        String className = StringManip.unqualifiedPart(c.getName());

        sb.append(className);
      
        // If the number property is defined, print the number.
        if (hasProperty(NUMBER_KEY)) {
           sb.append(" (" + getDefinedProperty(NUMBER_KEY) + ')');
        }
           
        Method[] methodArr = c.getMethods();
          
        String nextIndent = indent + " ";        
      
        int matchingMethods = 0;

        for (int i = 0; i < methodArr.length; i++) {
            Method method = methodArr[i];

            String methodName = method.getName();
          
            if (methodName.startsWith("get") &&
                (method.getParameterTypes().length == 0) &&
                !methodName.equals("getClass")) {
              
               matchingMethods++; 
               if (matchingMethods == 1) {
                  sb.append('\n'); 
               } 
              
               String methodLabel = methodName.substring(3);
          
               String totalIndent = nextIndent + _makeSpaceString(methodLabel.length()) + "  ";
                       
               sb.append(nextIndent + methodLabel + ": ");

               Object retval = null;
               try {
                  retval = method.invoke(this, null);
               } catch (Exception e) {
                  throw new RuntimeException("Error invoking method " + methodName);
               }

               if (retval instanceof TreeNode) {
                  TreeNode node = (TreeNode) retval;
                  sb.append(node.toString(totalIndent));
               } else if (retval instanceof List) {
                  sb.append(TNLManip.toString((List) retval, nextIndent));
               } else {
                  sb.append(retval.toString() + '\n');
               }
             
            } // if (methodName.startsWith("get") ...
        } // for 
                      
        if (matchingMethods < 1) {
           sb.append(" (leaf)"); // Node has no children
        } else {
           sb.append(indent + "END " + className);           
        }      
      
        sb.append('\n');

        return sb.toString();
    }

    /** Return a String representation of this node.
     *  Call the toString() method of all child nodes.
     */  
    public String toString() {
        return toString("");
    }

    /** Accept a visitor, giving the visitor a list of zero arguments. */
    public Object accept(IVisitor v) {
        return accept(v, new LinkedList());
    }

    /** Accept a visitor, giving the visitor a list of arguments. Depending on
     *  the traversal method, the children of the node may be visited before or
     *  after this node is visited, or not at all.
     */
    public Object accept(IVisitor v, LinkedList visitorArgs) {

      Object retval;

      switch (v.traversalMethod()) {

        case IVisitor.TM_CHILDREN_FIRST:
        {
        // Traverse the children first
        traverseChildren(v, visitorArgs);
        retval = _acceptHere(v, visitorArgs);

        // remove the children return values to prevent exponential usage
        // of memory
        removeProperty(CHILD_RETURN_VALUES_KEY);
        }
        break;

        case IVisitor.TM_SELF_FIRST:
        {
        // Visit myself first
        retval = _acceptHere(v, visitorArgs);
        traverseChildren(v, visitorArgs);
        }
        break;

        case IVisitor.TM_CUSTOM:
        {
        // Let visitor do custom traversal
        retval = _acceptHere(v, visitorArgs);
        }
        break;

        default:
        throw new RuntimeException("Unknown traversal method for visitor");
      } // end switch
      
      return retval;
    }

    /** Visit all nodes or lists in in the argument list, and place the list of 
     *  return values in the CHILD_RETURN_VALUES_KEY property of the node.
     */
    public void traverseChildren(IVisitor v, LinkedList args) {
        List retList = TNLManip.traverseList(v, this, args, _childList);

        setProperty(CHILD_RETURN_VALUES_KEY, retList);
    }

    /** Return the child at the specified index in the child list. */
    public Object getChild(int index) {
        return _childList.get(0);
    }

    public void setChild(int index, Object child) {
        _childList.set(index, child);
    }

    /** Return the list of all direct children of this node. */
    public List children() { return _childList; } // change this name

    public void setChildren(List childList) {
        _childList = childList;
    }

    public Object childReturnValueAt(int index) {
        List retList = (List) getDefinedProperty(CHILD_RETURN_VALUES_KEY);
        return retList.get(index);
    }

    public Object childReturnValueFor(Object child) {
        Iterator itr = _childList.iterator();
        int index = 0;

        while (itr.hasNext()) {
          if (child == itr.next()) {
             return childReturnValueAt(index);
          }
          index++;
        }
        ApplicationUtility.error("Child not found");
        return null;
    }

    /** Return true iff this subclass of TreeNode is a singleton, i.e. there exists only
     *  one object of the subclass. This method needs to be overridden by 
     *  singleton classes.
     */
    public boolean isSingleton() { return false; }


    protected Object _acceptHere(IVisitor v, LinkedList visitArgs) {      
        if (_myClass == null) {
           _myClass = getClass();

           String myClassName = StringManip.unqualifiedPart(_myClass.getName());
           _visitMethodName = "visit" + myClassName;

           _visitParamTypes[0] = _myClass;
           _visitParamTypes[1] = _linkedListClass;

           _visitArgs[0] = this;
        } 

        Method method;
        Class visitorClass = v.getClass();

        try {
          method = visitorClass.getMethod(_visitMethodName, _visitParamTypes);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e.toString());
        }

        _visitArgs[1] = (Object) visitArgs;

        try {
          return method.invoke(v, _visitArgs);
        } catch (IllegalAccessException iae) {
          ApplicationUtility.error("Illegal access exception invoking method "
           + _visitMethodName);
        } catch (InvocationTargetException ite) {
          ApplicationUtility.error("Invocation target exception invoking method "
           + _visitMethodName + " : target = " +
           ite.getTargetException().toString());
        }
        return null;
    }
  
    // protected methods
  
    /** Return a String of spaces, the number of which is specified by the argument. */
    protected static String _makeSpaceString(int spaces) {
        StringBuffer sb = new StringBuffer();
    
        for (int i = 0; i < spaces; i++) {
            sb.append(' ');
        }
  
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public variables                 ////
    
    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    protected Class  _myClass = null;    
    protected List _childList = new ArrayList();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private String _visitMethodName = null;
    private final Class[] _visitParamTypes = new Class[2];
    private final Object[] _visitArgs = new Object[2];

    private static final Class _linkedListClass = (new LinkedList()).getClass();
}
