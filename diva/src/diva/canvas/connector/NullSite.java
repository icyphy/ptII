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
 * @version        $Id$
 * @author         John Reekie
 */
public class NullSite extends AbstractSite {
    /** Return null
     */
    @Override
    public Figure getFigure() {
        return null;
    }

    /** Return 0
     */
    @Override
    public int getID() {
        return 0;
    }

    /** Return 0.0.
     */
    @Override
    public double getX() {
        return 0.0;
    }

    /** Return 0.0.
     */
    @Override
    public double getY() {
        return 0.0;
    }
}
