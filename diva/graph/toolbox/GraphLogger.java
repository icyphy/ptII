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
package diva.graph.toolbox;

import java.util.ArrayList;
import java.util.Iterator;

import diva.graph.GraphEvent;
import diva.graph.GraphListener;

/**
 * A logger of graph events.  Every event that is sent
 * to this logger is stored in a log which can then be
 * printed out or otherwise analyzed.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class GraphLogger implements GraphListener {
    /**
     * Storage for the log.
     */
    private ArrayList _log = new ArrayList();

    /**
     * Add this event to the log.
     */
    @Override
    public void edgeHeadChanged(GraphEvent e) {
        _log.add(e);
    }

    /**
     * Add this event to the log.
     */
    @Override
    public void edgeTailChanged(GraphEvent e) {
        _log.add(e);
    }

    /**
     * Add this event to the log.
     */
    @Override
    public void nodeAdded(GraphEvent e) {
        _log.add(e);
    }

    /**
     * Add this event to the log.
     */
    @Override
    public void nodeRemoved(GraphEvent e) {
        _log.add(e);
    }

    /**
     * Add this event to the log.
     */
    @Override
    public void structureChanged(GraphEvent e) {
        _log.add(e);
    }

    /**
     * Return a sequentially-sorted iterator over the GraphEvent
     * objects contained by the log.
     */
    public Iterator iterator() {
        return _log.iterator();
    }

    /**
     * Print the contents of the log into a string.
     */
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer("LOG: \n");

        for (Iterator i = iterator(); i.hasNext();) {
            ret.append("\t" + i.next() + "\n");
        }

        return ret.toString();
    }
}
