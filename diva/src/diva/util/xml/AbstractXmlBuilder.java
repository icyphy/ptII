/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 * An abstract implementation of the XmlBuilder interface that gets
 * and sets a delegate, leaves the build method abstract, and doesn't
 * support the generate method.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public abstract class AbstractXmlBuilder implements XmlBuilder {
    private XmlBuilder _delegate;

    /** Given an XmlElement, create and return an internal representation
     * of it. Implementors should also provide a more
     * type-specific version of this method:
     * <pre>
     *   public Graph build (XmlElement elt, String type);
     * </pre>
     */
    @Override
    public abstract Object build(XmlElement elt, String type) throws Exception;

    /** Delegate builders can be used to build/generate for objects
     * that are unknown by the current builder, as might be the
     * case in a hierarchy of heterogeneous objects.
     *
     * @see diva.util.xml.CompositeBuilder
     */
    @Override
    public void setDelegate(XmlBuilder delegate) {
        _delegate = delegate;
    }

    /** Return the delegate set by getDelegate().
     */
    public XmlBuilder getDelegate() {
        return _delegate;
    }

    /**
     * Unable to generate XML by default.
     * @exception UnsupportedOperationException Unable to generate XML by default
     */
    @Override
    public XmlElement generate(Object in) throws Exception {
        String err = getClass().getName() + ": unable to generate XML";
        throw new UnsupportedOperationException(err);
    }
}
