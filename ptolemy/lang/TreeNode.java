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

import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public abstract class TreeNode extends PropertyMap {

  public String toString(String indent) {

      StringBuffer sb = new StringBuffer();

      sb.append(indent);

      Class c = getClass();
      String className = c.getName();

      sb.append(className);
      sb.append('\n');

      String nextIndent = indent + "  ";
      Method[] methodArr = c.getMethods();

      for (int i = 0; i < methodArr.length; i++) {
          Method method = methodArr[i];

          String methodName = method.getName();

          if (methodName.startsWith("get") &&
              (method.getParameterTypes().length == 0) &&
              !methodName.equals("getClass")) {
             sb.append(nextIndent);
             sb.append(methodName.substring(3));
             sb.append(" : ");

             Object retval = null;
             try {
                retval = method.invoke(this, null);
             } catch (Exception e) {
                throw new RuntimeException("Error invoking method " + methodName);
             }

             if (retval instanceof TreeNode) {
                TreeNode node = (TreeNode) retval;
                sb.append('\n');
                sb.append(node.toString(nextIndent));
             } else if (retval instanceof LinkedList) {
                sb.append(TNLManip.toString((LinkedList) retval, nextIndent));
             } else {
                sb.append(retval.toString());
                sb.append('\n');
             }
          }
      }

      sb.append(indent);

      sb.append("END ");
      sb.append(className);
      sb.append('\n');

      return sb.toString();
  }

  public String toString() {
    return toString("");
  }

  public Object accept(IVisitor v) {
    return accept(v, new LinkedList());
  }

  public Object accept(IVisitor v, LinkedList visitorArgs) {

    Object retval;

    switch (v.traversalMethod()) {

    case IVisitor.TM_CHILDREN_FIRST:
    {
      // Traverse the children first
      traverseChildren(v, visitorArgs);
      retval = acceptHere(v, visitorArgs);

      // remove the children return values to prevent exponential usage
      // of memory
      removeProperty("childReturnValues");
    }
    break;

    case IVisitor.TM_SELF_FIRST:
    {
      // Visit myself first
      retval = acceptHere(v, visitorArgs);
      traverseChildren(v, visitorArgs);
    }
    break;

    case IVisitor.TM_CUSTOM:
    {
       // Let visitor do custom traversal
       retval = acceptHere(v, visitorArgs);
    }
    break;

    default:
    {
       throw new RuntimeException("Unknown traversal method for visitor");
    }
    } // end switch
    return retval;
  }

  public void traverseChildren(IVisitor v, LinkedList args) {
    LinkedList retList = TNLManip.traverseList(v, this, args, _childList);

    setProperty("childReturnValues", retList);
  }

  public Object getChild(int index) {
    return _childList.get(0);
  }

  public void setChild(int index, Object child) {
    _childList.set(index, child);
  }

  public LinkedList children() { return _childList; } // change this name

  public void setChildren(LinkedList childList) {
    _childList = childList;
  }

  protected Object acceptHere(IVisitor v, LinkedList visitArgs) {
    if (_myClass == null) {
       _myClass = getClass();

       String myClassName = StringManip.unqualifiedPart(_myClass.getName());
       _visitMethodName = "visit" + myClassName;

       _visitParamTypes[0] = _myClass;
       _visitParamTypes[1] = _linkedListClass;

       _visitArgs[0] = (Object) this;
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

  public Object childReturnValueAt(int index) {
    LinkedList retList = (LinkedList) getDefinedProperty("childReturnValues");
    return retList.get(index);
  }

  public Object childReturnValueFor(Object child) {
    ListIterator itr = _childList.listIterator();
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

  protected LinkedList _childList = new LinkedList();

  protected Class _myClass = null;
  protected String _visitMethodName = null;
  protected final Class[] _visitParamTypes = new Class[2];
  protected final Object[] _visitArgs = new Object[2];

  protected static final Class _linkedListClass = (new LinkedList()).getClass();
}
