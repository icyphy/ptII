/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */

package diva.graph;

/**
 * A graph package topological error.  GraphExceptions are usually
 * thrown by MutableGraphModels in response to changes which could not
 * be made for one reason or another.  For example, a composite node
 * might want to disallow a node of the wrong type to be placed inside
 * it.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class GraphException extends RuntimeException {
    public GraphException() {
        super();
    }

    public GraphException(String s) {
        super(s);
    }

    public GraphException(Exception e) {
        super(e.getMessage());
    }
}


