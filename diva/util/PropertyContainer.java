/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

import java.util.Iterator;

/**
 * An object that can be annotated with arbitrary
 * objects whose keys are strings.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface PropertyContainer {
    /**
     * Return the property corresponding to
     * the given key, or null if no such property
     * exists.
     */
    public Object getProperty(String key);

    /** Get an iterator over the names of the properties.
     */
    public Iterator propertyNames();

    /**
     * Set the property corresponding to
     * the given key.
     */
    public void setProperty(String key, Object value);

    // XXX removeProperty
}
