/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.toolbox;

import diva.canvas.CanvasComponent;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;

import java.util.Hashtable;

/**
 * A FigureDecorator implementation which simply acts as a
 * set of prototypes which can be instantiated according to
 * the type of object the decorator is applied to. Instances
 * of this class will throw an exception if they are actually
 * placed into a figure hierarchy. They can, however, be passed
 * to methods that expect a decorator instance that is to be used
 * as a prototype for other instances.
 *
 * @author John Reekie  (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class TypedDecorator extends FigureDecorator {
    /**
     * The default prototype decorator.
     */
    private FigureDecorator _defaultDecorator = null;

    /**
     * The prototypes indexed by type.
     */
    private Hashtable _typedDecorators = null;

    /**
     * A typed decorator that uses a BasicHighlighter
     * as its default.
     */
    public TypedDecorator () {
        this(new BasicHighlighter());
    }

    /**
     * A typed decorator with the given decorator
     * as its default.
     */
    public TypedDecorator (FigureDecorator defaultDecorator) {
        _defaultDecorator = defaultDecorator;
        _typedDecorators = new Hashtable();
    }

    /**
     * Add a decorator which is duplicated when an object of the given
     * class is passed to the newInstance() method.
     */
    public void addDecorator(Class c, FigureDecorator d) {
        _typedDecorators.put(c, d);
    }

    /**
     * Return a new decorator, according to the type of the figure.
     */
    public FigureDecorator newInstance (Figure f) {
        FigureDecorator d = (FigureDecorator)_typedDecorators.get(f.getClass());
        if (d != null) {
            return d.newInstance(f);
        }
        return _defaultDecorator.newInstance(d);
    }

    /**
     * Remove a decorator.
     */
    public void removeDecorator(Class c) {
        _typedDecorators.remove(c);
    }

    /** Throw an exception. The exception is thrown to ensure that instances
     * of this class are not inserted into the figure hierarchy. This
     * is a bit clumsy, as this object is really a factory masquerading as
     * a figure.
     */
    public void setParent (CanvasComponent fc) {
        throw new UnsupportedOperationException(
                "TypedDecorator cannot be inserted into a figure tree");
    }
}


