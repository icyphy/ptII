/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.resource;


/**
 * A class that bundles the default Diva resources contained
 * in the diva.resource package.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class DefaultBundle extends RelativeBundle {

    /** Create a new Bundle of default resources.
     */
    public DefaultBundle () {
        super("diva.resource.Defaults", null, null);
    }
}


