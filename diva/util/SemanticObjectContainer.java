/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

/**
 * An object which is annotated with a single
 * "semantic object" which is its semantic equivalent
 * in an application.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface SemanticObjectContainer {
    /**
     * Return the semantic object.
     */
    public Object getSemanticObject();

    /**
     * Set the semantic object.
     */
    public void setSemanticObject(Object o);
}


