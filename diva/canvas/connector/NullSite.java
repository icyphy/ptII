/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/** A site that doesn't do anything useful. Sometimes this is
 * needed as a placeholder for objects that expect sites, but
 * because other objects they depend on haven't been created yet,
 * can't have them.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public class NullSite extends AbstractSite {

    /** Return null
     */
    public Figure getFigure () {
        return null;
    }

    /** Return 0
     */
    public int getID () {
        return 0;
    }

    /** Return 0.0.
     */
    public double getX () {
        return 0.0;
    }

    /** Return 0.0.
     */
    public double getY () {
        return 0.0;
    }
}


