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
package diva.graph.layout;

/**
 * An abstract implementation of the GlobalLayout interface.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public abstract class AbstractGlobalLayout implements GlobalLayout {
    LayoutTarget _layoutTarget;

    /** Create a new global layout that uses the given layout target.
     */
    public AbstractGlobalLayout(LayoutTarget target) {
        _layoutTarget = target;
    }

    /** Return the layout target.
     */
    @Override
    public LayoutTarget getLayoutTarget() {
        return _layoutTarget;
    }

    /** Set the layout target.
     */
    @Override
    public void setLayoutTarget(LayoutTarget target) {
        _layoutTarget = target;
    }

    /**
     * Layout the graph model in the viewport
     * specified by the layout target environment.
     */
    @Override
    public abstract void layout(Object composite);
}
