/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.toolbox;
import diva.graph.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A logger of graph events.  Every event that is sent
 * to this logger is stored in a log which can then be
 * printed out or otherwise analyzed.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class GraphLogger implements GraphListener {
    /**
     * Storage for the log.
     */
    private ArrayList _log = new ArrayList();

    /**
     * Add this event to the log.
     */
    public void edgeHeadChanged(GraphEvent e) { _log.add(e); }

    /**
     * Add this event to the log.
     */
    public void edgeTailChanged(GraphEvent e) { _log.add(e); }

    /**
     * Add this event to the log.
     */
    public void nodeAdded(GraphEvent e) { _log.add(e); }

    /**
     * Add this event to the log.
     */
    public void nodeRemoved(GraphEvent e) { _log.add(e); }

    /**
     * Add this event to the log.
     */
    public void structureChanged(GraphEvent e) { _log.add(e); }

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
    public String toString() {
        String ret = "LOG: \n";
        for(Iterator i = iterator(); i.hasNext(); ) {
            ret = ret + "\t" + i.next() + "\n";
        }
        return ret;
    }
}


