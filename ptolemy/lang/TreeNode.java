/* A base type for nodes in an abstract syntax tree.

Copyright (c) 1998-2001 The Regents of the University of California.
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
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////
//// TreeNode
/**
The base type for nodes in an abstract syntax tree.  Note that the
AST can have as nodes just about anything.  If a node is a List, then
it will be interpreted as a hierarchical object.  An instance of this
class is also a hierarchical object.
@author Jeff Tsay, Shuvra S. Bhattacharyya and Steve Neuendorffer
@version $Id$
*/
public abstract class TreeNode extends TrackedPropertyMap
    implements ITreeNode {

    /** Construct a TreeNode with an unspecified number of children to
     *  be added to the child list later.
     */
    public TreeNode() {
        // The list will grow as needed, and should be trimmed after
        // all members are added.
        _childList = new ChildList(this);
    }

    /** Construct a TreeNode with the specified number of children to be
     *  added to the child list later.
     */
    public TreeNode(int numberOfChildren) {
        _childList = new ChildList(this, numberOfChildren);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /** Accept a visitor, giving the visitor an empty list as arguments.
     *  Child return values of visited nodes will be updated based on
     *  the resulting traversal.
     *  @param visitor The visitor.
     *  @return The result of the visit, which depends on the visitor.
     */
    public Object accept(IVisitor visitor) {
        return accept(visitor, nullList);
    }

    /** Accept a visitor, giving the visitor a list of arguments.
     *  Depending on the traversal method, the children of the
     *  node may be visited before or after this node is visited,
     *  or not at all.
     *  @param visitor The visitor.
     *  @param visitorArgs The arguments to pass to the visitor.
     *  @param setChildReturnValues Indicates whether or not the
     *  list of returned values for each visited node should be 
     *  placed in the CHILD_RETURN_VALUES_KEY property of the node.
     *  If this indicator is 'false,' then the current values of the
     *  CHILD_RETURN_VALUES_KEY property are left unchanged.
     *  @return The result of the visit, which depends on the visitor.
     */
    public Object accept(IVisitor visitor, LinkedList visitorArgs,
            boolean setChildReturnValues) {
        Object returnValue;
        switch (visitor.traversalMethod()) {
        case IVisitor.TM_CHILDREN_FIRST:
            {
                // Traverse the children first.
                traverseChildren(visitor, visitorArgs, setChildReturnValues);
                returnValue = _acceptHere(visitor, visitorArgs);

                // Remove the children return values to prevent
                // exponential usage of memory.
                // FIXME: a similar, appropriately-placed call for the 
                // new RETURN_VALUE_AS_ELEMENT_KEY key may be appropriate.
                if (setChildReturnValues) 
                    removeProperty(CHILD_RETURN_VALUES_KEY);
            }
            break;

        case IVisitor.TM_SELF_FIRST:
            {
                // Visit myself first.
                returnValue = _acceptHere(visitor, visitorArgs);

                // If processing of children has been turned off, 
                // simply reset the flag that disables processing of children.
                // Otherwise, traverse the children.
                if (_ignoreChildren) _ignoreChildren = false;
                else traverseChildren(visitor, visitorArgs, setChildReturnValues);
            }
            break;

        case IVisitor.TM_CUSTOM:
            {
                // Let visitor do custom traversal.
                returnValue = _acceptHere(visitor, visitorArgs);
            }
            break;

        default:
            throw new RuntimeException("Unknown traversal method for visitor");
        }

        return returnValue;
    }

    
    /** Accept a visitor, giving the visitor a list of arguments.
     *  Depending on the traversal method, the children of the
     *  node may be visited before or after this node is visited,
     *  or not at all. The  CHILD_RETURN_VALUES_KEY property of
     *  each visited node is updated during traversal.
     *  @param visitor The visitor.
     *  @param visitorArgs The arguments to pass to the visitor.
     *  @return The result of the visit, which depends on the visitor.
     */
    public Object accept(IVisitor visitor, LinkedList visitorArgs) {
        return accept(visitor, visitorArgs, true);
    }

    /** Return the list of children of this node.
     *  @return A list of children.
     */
    public List children() {
        return _childList;
    }

    /** Get the return value of the most recent
     *  visitor (that was configured to set child return values)
     *  to the <i>index</i>-th child node.
     *  @param index The index of the child whose visit result is returned.
     *  @return The most recent result of visiting the specified child
     *  with a visitation that was configured to set child return values.
     */
    public Object childReturnValueAt(int index) {
        List retList = (List) getDefinedProperty(CHILD_RETURN_VALUES_KEY);
        return retList.get(index);
    }

    /** Get the return value of the most recent
     *  visitor to this node when the node was visited as a "hierarchical"
     *  node (a list of nodes).
     *  @return The most recent result of visiting this node as an
     *  element of a hierarchical node.
     */
    public Object returnValueAsElement() {
        return getProperty(RETURN_VALUE_AS_ELEMENT_KEY);
    }

    /** Get the return value of the most recent
     *  visitor to the specified child node.
     *  @param child The child whose visit result is returned.
     *  @return The most recent result of visiting the specified child.
     */
    public Object childReturnValueFor(Object child) {
        int index = _childList.indexOf(child);
        if (index >= 0) {
            return childReturnValueAt(index);
        } else {
            throw new RuntimeException("TreeNode.childReturnValueFor(): " +
                    "Child not found");
        }
    }

    /** Return the class ID number, which is unique for each sub-type.
     *  The ID number is intended to be used in switch statements.
     *  @return A unique class ID number.
     */
    public abstract int classID();

    /** Return a deep clone of this node, which contains clones of the
     *  children of the node.  If the node identifies itself as a singleton
     *  by returning true in its isSingleton() method, then do not clone it.
     *  @return A deep copy of this node.
     */
    public Object clone() {
        // Don't clone singletons.
        if (isSingleton()) {
            return this;
        }
        TreeNode copy = (TreeNode) super.clone();
        List children = TNLManip.cloneList(_childList);
        if (children != null) copy.setChildren(children);
        return copy;
    }

    /** Return the child at the specified index in the child list.
     *  If there is no such child, return null.
     *  @param index The index of the desired child.
     *  @return The child node, or null if there is none.
     */
    public Object getChild(int index) {
        if (index < _childList.size() && index >= 0) {
            return _childList.get(index);
        } else {
            return null;
        }
    }

    /** Disable traversing children of this node the next time
     *  a visitation of this node completes during a self-first
     *  traversal. This method has no effect for other traversal
     *  modes (e.g., children first). This method allows selective pruning
     *  of subtrees, which can significantly improve performance
     *  for some applications. To ignore children in multiple self-first
     *  traversals, the method must be called multiple times.
     */
    public void ignoreChildren() {
        _ignoreChildren = true;
    }

    /** Return true if the class of this object is a singleton, i.e. there
     *  exists only one object of the subclass. This method needs to be
     *  overridden by  singleton classes.
     *  @return False, by default.
     */
    public boolean isSingleton() {
        return false;
    }

    /** Set the child at the specified index to the specified object,
     *  replacing any previous child at that index.
     *  @param index The index of the child.
     *  @param child The child to insert.
     */
    public void setChild(int index, Object child) {
        _childList.set(index, child);
        
    }

    /** Set the children of this node to the specified list.
     *  @param childList The list of children.
     */
    public void setChildren(List childList) {
        _childList = new ChildList(this);
        _childList.addAll(childList);
    }

    /** Return a String representation of this node and all its children.
     *  @return A representation of this node.
     */
    public String toString() {
        return toString("");
    }

    /** Return a String representation of this node, prefixed by prefix,
     *  and all its children.
     *  @return A representation of this node.
     */
    public String toString(String prefix) {

        StringBuffer stringBuffer = new StringBuffer();

        Class c = getClass();
        String className = StringManip.unqualifiedPart(c.getName());

        stringBuffer.append(" {" + className);

        // If the number property is defined, print the number.
        if (hasProperty(NUMBER_KEY)) {
            stringBuffer.append(" (" + getDefinedProperty(NUMBER_KEY) + ')');
        }

        Method[] methodArr = c.getMethods();

        String nextPrefix = prefix + " ";

        int matchingMethods = 0;

        for (int i = 0; i < methodArr.length; i++) {
            Method method = methodArr[i];

            String methodName = method.getName();

            if (methodName.startsWith("get") &&
                    (method.getParameterTypes().length == 0) &&
                    (!methodName.equals("getClass")) &&
                    (!methodName.equals("getParent"))) {

                matchingMethods++;
                if (matchingMethods == 1) {
                    stringBuffer.append(" { \n");
                }

                String methodLabel = methodName.substring(3);

                String totalPrefix = nextPrefix +
                    _makeSpaceString(methodLabel.length()) + "  ";

                stringBuffer.append(nextPrefix + " {" + methodLabel);

                Object returnValue = null;
                try {
                    returnValue = method.invoke(this, null);
                } catch (Exception e) {
                    throw new RuntimeException("Error invoking method " +
                            methodName);
                }

                if (returnValue instanceof TreeNode) {
                    TreeNode node = (TreeNode) returnValue;
                    stringBuffer.append(node.toString(totalPrefix) + "} \n");
                } else if (returnValue instanceof List) {
                    stringBuffer.append(" " +
                            TNLManip.toString((List) returnValue, nextPrefix) +
                            "} \n");
                } else {
		    if (returnValue == null) {
			stringBuffer.append(" null}\n");
		    } else {
			stringBuffer.append(" " + returnValue.toString() + "} \n");
		    }
                }
            }
        }

        if (matchingMethods < 1) {
            // Node has no children.
            stringBuffer.append(" {leaf}");
        } else {
            stringBuffer.append(prefix + "}") ;
        }

        stringBuffer.append("}");
        return stringBuffer.toString();
    }

    /** Recursively visit all nodes or lists in in the argument list, and 
     *  optionally place the corresponding list of returned values 
     *  in the CHILD_RETURN_VALUES_KEY property of each visited node.
     *  @param visitor The visitor to apply to the children.
     *  @param args The arguments to pass to the visitor.
     *  @param setChildReturnValues Indicates whether or not the
     *  list of returned values should be placed in the CHILD_RETURN_VALUES_KEY
     *  property of the node.
     */
    public void traverseChildren(IVisitor visitor, LinkedList args,
                                 boolean setChildReturnValues) {
        ArrayList childReturnValues =
                TNLManip.traverseList(visitor, args, _childList, 
                        setChildReturnValues);

        if (setChildReturnValues) {
            setProperty(CHILD_RETURN_VALUES_KEY, childReturnValues);
        }
                
    }
    
    /** Recursively visit all nodes or lists in in the argument list, and 
     *  place the corresponding list of returned values in the 
     *  CHILD_RETURN_VALUES_KEY property of each visited node.
     *  @param visitor The visitor to apply to the children.
     *  @param args The arguments to pass to the visitor.
     */
    public void traverseChildren(IVisitor visitor, LinkedList args) {
        traverseChildren(visitor, args, true);
    }

    /** Return the parent of a tree node. If the tree node is part of
     *  a list, then the parent is the tree node that contains the list
     *  within its list of children. If the parent of this node has
     *  not yet been set, null is returned.
     *  @param The parent of the given tree node.
     */
    public TreeNode getParent() {
        return _parent;
    }
    
    /** Set this tree node as the parent of the argument. If the argument
     *  is a list or collection (a "hierarchical tree node"), then recursively
     *  set this to be the parent of all tree nodes in the list.
     *  @param child The child (or list of children) that this is the parent of.
     */
    public void setAsParent(Object child) {
        if (child instanceof TreeNode) ((TreeNode)child)._parent = this;
        else if (child instanceof Collection) {
            Iterator children = ((Collection)child).iterator();
            while (children.hasNext()) {
                setAsParent(children.next());
            }            
        }
        else {
            // FIXME: insert an exception here?
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////
    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////


    /** Accept a visitor at this node only (not the children).
     *  This method uses reflection and
     *  therefore suffers a performance penalty. This method should be
     *  overridden in concrete subclasses of TreeNode for better performance.
     *  NOTE: Another approach would be to have a single visit method name
     *  and to use overloading to distinguish the visit methods. However,
     *  it turns out that Java doesn't provide much support for this, so
     *  the realization would require extensive use of reflection, and
     *  would probably be expensive.
     *  @param visitor The visitor to accept.
     *  @param args The arguments to pass to the visitor.
     */
    protected Object _acceptHere(IVisitor visitor, List visitArgs) {
        Class myClass = getClass();

        StringBuffer visitMethodName = new StringBuffer("visit");
        String myClassName = StringManip.unqualifiedPart(
                myClass.getName());
        visitMethodName.append(myClassName);
        
        try {
            return reflectAndInvokeMethod((Object)visitor, 
                    visitMethodName.toString(), this, visitArgs,
                    myClass, _linkedListClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.toString());
        } catch (IllegalAccessException illegalAccessException) {
            throw new RuntimeException("Illegal access exception " +
                    "invoking method " + visitMethodName);
        } catch (InvocationTargetException invocationTargetException) {
            invocationTargetException.printStackTrace();
            throw new RuntimeException(
                    "Invocation target exception invoking method "
                    + visitMethodName + " : target = " +
                    invocationTargetException.getTargetException().toString());
        }
    }

    /** Return a String of spaces, the number of which is specified by the
     *  argument.  This is used to construct indentations in toString().
     *  @param level The number of spaces to return.
     *  @return A string of spaces.
     */
    protected static String _makeSpaceString(int level) {
        if(level < 0) 
            return _makeSpaceString(0);
        synchronized(_prefixes) {
            if(_prefixes.length <= level) {
                // Expand the cache
                String[] temp = new String[2 * level];
                System.arraycopy(_prefixes, 0, temp, 0, _prefixes.length);
                _prefixes = temp;
            }
            if(_prefixes[level] == null) {
                // update the cache
                _prefixes[level] = _makeSpaceString(level - 1) + " ";
            }
            // return the value from the cache
            return _prefixes[level];
        }
    }

    /** Reflect the given methodName in the given object and invoke it
     *  with the given args.
     */
    protected static synchronized Object reflectAndInvokeMethod(Object object, 
            String methodName, Object arg1, Object arg2, 
            Class class1, Class class2)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {          
        _reflectParamTypes[0] = class1;
        _reflectParamTypes[1] = class2;            

        Method method;
        Class objectClass = object.getClass();

        method = objectClass.getMethod(methodName,
                _reflectParamTypes);

        _reflectArgs[0] = arg1;
        _reflectArgs[1] = arg2;

        return method.invoke(object, _reflectArgs);
    }


    /** The list of children. */
    protected ChildList _childList = null;

    /** The parent of this object in the AST */
    protected TreeNode _parent = null; 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Static null list.
    private static LinkedList nullList = new LinkedList();
 
    // An array of indenting spaces, indexed by the number indents.
    // Tese are cached to speed up the indenting code.
    private static String[] _prefixes = {"", " "};

    // Things that are reused for reflection.
    private static final Class[] _reflectParamTypes = new Class[2];
    private static final Object[] _reflectArgs = new Object[2];
    private static final Class _linkedListClass = nullList.getClass();

    // A flag that is used to disable traversal of children under
    // self-first traversal mode.
    private boolean _ignoreChildren = false;
}
