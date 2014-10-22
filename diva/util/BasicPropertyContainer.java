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
package diva.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * An object that can be annotated with arbitrary
 * objects whose keys are strings.
 *
 * @author Michael Shilman
 * @version $Id$
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
    @Override
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
    @Override
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
    @Override
    public Iterator propertyNames() {
        return _mapping.keySet().iterator();
    }
}
