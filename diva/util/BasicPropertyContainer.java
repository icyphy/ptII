/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An object that can be annotated with arbitrary
 * objects whose keys are strings.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class BasicPropertyContainer implements PropertyContainer {
    /**
     * The default values, if applicable.
     */
    PropertyContainer _defaults;

    /**
     * The mapping from keys to values.
     */
    HashMap _mapping;

    /**
     * A property container with no defaults.
     */
    public BasicPropertyContainer() {
        this(null);
    }

    /**
     * A property container with no defaults.
     */
    public BasicPropertyContainer(PropertyContainer defaults) {
        _defaults = defaults;
    }

    /**
     * Return the property corresponding to
     * the given key, or null if no such property
     * exists.
     */
    public Object getProperty(String key) {
        Object o = null;
        if (_mapping != null) {
            o = _mapping.get(key);
        }
        if (o == null && _defaults != null) {
            return _defaults.getProperty(key);
        }
        return o;
    }

    /**
     * Set the property corresponding to
     * the given key.
     */
    public void setProperty(String key, Object value) {
        if (_mapping == null) {
            _mapping = new HashMap();
        }
        _mapping.put(key, value);
    }

    //XXX remove property
    //added by Heloise
    public void removeAllProperties() {
        _mapping.clear();
    }

    //added by Heloise
    public Iterator properties() {
        return _mapping.values().iterator();
    }

    /** Return an iteration of the names of the properties
     */
    public Iterator propertyNames() {
        return _mapping.keySet().iterator();
    }
}
