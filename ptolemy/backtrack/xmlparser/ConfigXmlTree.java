/* Tree representation of XML starting from an element.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

 */
package ptolemy.backtrack.xmlparser;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

///////////////////////////////////////////////////////////////////
//// ConfigXmlTree
/**
 Tree representation of XML starting from an element.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ConfigXmlTree implements Cloneable {

    /** Construct a node in the tree with no child.
     *
     *  @param elementName The XML element name of the node.
     */
    public ConfigXmlTree(String elementName) {
        this._elementName = elementName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a child to this node. This child will be added after the existing
     *  children.
     *
     *  @param child The child to be added.
     */
    public void addChild(ConfigXmlTree child) {
        _children.add(child);
        child._setParent(this);
    }

    /** Clone the sub-tree starting from this node, and return the clone result.
     *
     *  @return A clone of the sub-tree.
     */
    public Object clone() {
        ConfigXmlTree newTree = new ConfigXmlTree(getElementName());
        newTree._attributes = new Hashtable<String, String>(_attributes);
        startTraverseChildren();

        while (hasMoreChildren()) {
            newTree.addChild((ConfigXmlTree) nextChild().clone());
        }

        return newTree;
    }

    /** Print the content of the sub-tree starting from this node to the given
     *  output stream in a text format.
     *
     *  @param stream The stream to be printed to.
     */
    public void dump(PrintStream stream) {
        _dump(0, stream);
    }

    /** Get the value of the attribute with the given name.
     *
     *  @param name The attribute name.
     *  @return The value of the attribute, or null if not found.
     *  @see #setAttribute(String, String)
     */
    public String getAttribute(String name) {
        return _attributes.get(name);
    }

    /** Get the names of all the attributes of this XML element.
     *
     *  @return The enumeration of attribute names.
     */
    public Enumeration<String> getAttributeNames() {
        return _attributes.keys();
    }

    /** Get the name of this XML element.
     *
     *  @return The element name.
     */
    public String getElementName() {
        return _elementName;
    }

    /** Get the parent of this node.
     *
     *  @return The parent, or null.
     */
    public ConfigXmlTree getParent() {
        return _parent;
    }

    /** Test whether this node has an attribute with the given name.
     *
     *  @param name The attribute name.
     *  @return true if this node has an attribute with the name; false,
     *   otherwise.
     */
    public boolean hasAttribute(String name) {
        return _attributes.get(name) != null;
    }

    /** Test whether there are more children to traverse.
     *
     *  @return true if there are more children to traverse; false, otherwise.
     *  @see #nextChild()
     *  @see #startTraverseChildren()
     */
    public boolean hasMoreChildren() {
        return _iterator.hasNext();
    }

    /** Test whether this node is a leaf in the XML tree.
     *
     *  @return true if this node is a leaf; false, otherwise.
     */
    public boolean isLeaf() {
        return _children.isEmpty();
    }

    /** Return the next child to be traversed.
     *
     *  @return The next child.
     *  @see #hasMoreChildren()
     *  @see #startTraverseChildren()
     */
    public ConfigXmlTree nextChild() {
        return _iterator.next();
    }

    /** Set the value of the attribute with the given name. Create the attribute
     *  if it does not exist yet.
     *
     *  @param name The attribute name.
     *  @param value The attribute value.
     *  @see #getAttribute(String)
     */
    public void setAttribute(String name, String value) {
        if (value != null) {
            _attributes.put(name, value);
        }
    }

    /** Start traversing the children of this node by initializing the internal
     *  iterator.
     *
     *  @see #hasMoreChildren()
     *  @see #nextChild()
     */
    public void startTraverseChildren() {
        _iterator = _children.iterator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Print the content of the sub-tree starting from this node to the given
     *  output stream in a text format. The sub-tree is indented with the
     *  specified amount.
     *
     *  @param indent The amount of indentation.
     *  @param stream The stream to be printed to.
     */
    protected void _dump(int indent, PrintStream stream) {
        _dumpString(indent, getElementName(), stream);

        Enumeration<String> attrenu = getAttributeNames();

        while (attrenu.hasMoreElements()) {
            String attr = attrenu.nextElement();
            _dumpString(indent + 2, "+A " + attr + " = " + getAttribute(attr),
                    stream);
        }

        startTraverseChildren();

        while (hasMoreChildren()) {
            ConfigXmlTree child = nextChild();
            child._dump(indent + 2, stream);
        }
    }

    /** Set the name of this XML element.
     *
     *  @param elementName The element name.
     */
    protected void _setElementName(String elementName) {
        this._elementName = elementName;
    }

    /** Set the parent of this XML node. This method does not update the
     *  parent's children list. To add this node to be one of the parent's
     *  children, use {@link #addChild(ConfigXmlTree)} of the parent.
     *
     *  @param parent The parent node.
     */
    protected void _setParent(ConfigXmlTree parent) {
        this._parent = parent;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Print a string to the given output stream in a text format. The string
     *  is indented with the specified amount.
     *
     *  @param indent The amount of indentation.
     *  @param s The string to be printed.
     *  @param stream The stream to be printed to.
     */
    private void _dumpString(int indent, String s, PrintStream stream) {
        StringBuffer buf = new StringBuffer(indent
                + ((s == null) ? 0 : s.length()));

        for (int i = 0; i < indent; i++) {
            buf.append(" ");
        }

        buf.append(s);
        stream.println(buf);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The hash table of attributes from their names to their values.
     */
    private Hashtable<String, String> _attributes = new Hashtable<String, String>();

    /** The list of children.
     */
    private Vector<ConfigXmlTree> _children = new Vector<ConfigXmlTree>();

    /** The name of this XML element.
     */
    private String _elementName;

    /** The iterator used to iterate the children of this node.
     */
    private Iterator<ConfigXmlTree> _iterator;

    /** The parent of this node. null if this node is the root node of the XML
     *  document.
     */
    private ConfigXmlTree _parent;
}
