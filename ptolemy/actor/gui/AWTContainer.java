/*
 The portable container that wraps java.awt.Container.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.actor.gui;

import java.awt.Component;
import java.awt.Container;

import ptolemy.actor.injection.PortableContainer;

///////////////////////////////////////////////////////////////////
//// AWTContainer

/**
 * The portable container that wraps java.awt.Container.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class AWTContainer implements PortableContainer {

    /**
     * Create a new instance of the object by wrapping the provided container.
     * @param container The container to wrap.
     */
    public AWTContainer(Container container) {
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the component to the container.
     * @param component the component to be added to the container.
     * @see ptolemy.actor.injection.PortableContainer#add(java.lang.Object)
     */
    @Override
    public void add(Object component) {
        _container.add((Component) component);
    }

    /**
     * Return the AWT container that this instance wraps.
     * @see ptolemy.actor.injection.PortableContainer#getPlatformContainer()
     * @return the AWT container that this instance wraps.
     */
    @Override
    public Container getPlatformContainer() {
        return _container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The AWT container that this instance wraps.
     */
    private final Container _container;

}
