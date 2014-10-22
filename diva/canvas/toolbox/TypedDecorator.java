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
package diva.canvas.toolbox;

import java.util.Hashtable;

import diva.canvas.CanvasComponent;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;

/**
 * A FigureDecorator implementation which simply acts as a
 * set of prototypes which can be instantiated according to
 * the type of object the decorator is applied to. Instances
 * of this class will throw an exception if they are actually
 * placed into a figure hierarchy. They can, however, be passed
 * to methods that expect a decorator instance that is to be used
 * as a prototype for other instances.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
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
    public TypedDecorator() {
        this(new BasicHighlighter());
    }

    /**
     * A typed decorator with the given decorator
     * as its default.
     */
    public TypedDecorator(FigureDecorator defaultDecorator) {
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
    @Override
    public FigureDecorator newInstance(Figure f) {
        FigureDecorator d = (FigureDecorator) _typedDecorators
                .get(f.getClass());

        if (d != null) {
            return d.newInstance(f);
        }

        return _defaultDecorator.newInstance(/* d */null);
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
    @Override
    public void setParent(CanvasComponent fc) {
        throw new UnsupportedOperationException(
                "TypedDecorator cannot be inserted into a figure tree");
    }
}
