/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.xml;

/**
 * An abstract implementation of the XmlBuilder interface that gets
 * and sets a delegate, leaves the build method abstract, and doesn't
 * support the generate method.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public abstract class AbstractXmlBuilder implements XmlBuilder {
    private XmlBuilder _delegate;

    /** Given an XmlElement, create and return an internal representtion
     * of it. Implementors should also provide a more
     * type-specific version of this method:
     * <pre>
     *   public Graph build (XmlELement elt, String type);
     * </pre>
     */
    public abstract Object build (XmlElement elt, String type)
            throws Exception;

    /** Delegate builders can be used to build/generate for objects
     * that are unknown by the current builder, as might be the
     * case in a hierarchy of heterogeneous objects.
     *
     * @see diva.util.xml.CompositeBuilder
     */
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
    public XmlElement generate(Object in) throws Exception {
        String err = getClass().getName() + ": unable to generate XML";
        throw new UnsupportedOperationException(err);
    }
}


