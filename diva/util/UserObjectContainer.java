/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

/**
 * An object which is annotated with a single
 * "user object" which is its semantic equivalent
 * in an application.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface UserObjectContainer {
    /**
     * Return the user object.
     */
    public Object getUserObject();

    /**
     * Set the user object.
     */
    public void setUserObject(Object o);
}


