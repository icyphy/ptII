/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.util.xml;

/**
 * An XmlBuilder is an interface that can be implemented by classes
 * that convert between XmlElements and some internal
 * data representation. The main reason for doing so is to allow
 * other builders to "reuse" parts of an XML DTD. For example,
 * we could have a builder that builds Java2D objects, such as
 * "line" and "polygon". Then some other builder, that for example
 * builds libraries of graphical icons, can use an instance of the
 * Java2D builder internally -- if it does not recognize the type
 * of an XML element, it calls the Java2D builder to see if it can
 * get a low-level graphical object.
 *
 * @author John Reekie
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface XmlBuilder {
    /** Given an XmlElement, create and return an internal representation
     * of it. Implementors should also provide a more
     * type-specific version of this method:
     * <pre>
     *   public Graph build (XmlELement elt, String type);
     * </pre>
     */
    public Object build(XmlElement elt, String type) throws Exception;

    /** Delegate builders can be used to build/generate for objects
     * that are unknown by the current builder, as might be the
     * case in a hierarchy of heterogeneous objects.
     *
     * @see diva.util.xml.CompositeBuilder
     */
    public void setDelegate(XmlBuilder child);

    /** Given an object, produce an XML representation of it.
     */
    public XmlElement generate(Object obj) throws Exception;
}
