/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

/**
 * A multi-page document contains an ordered set of pages. A page
 * generally contains a single object, the "model," that contains part
 * of the document's data.
 *
 * @author Heloise Hse  (hwawen@eecs.berkeley.edu)
 * @author John Reekie  (johnr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface Page {
    /** Return the model of the page.
     */
    public Object getModel();

    /** Return the title of the page.
     */
    public String getTitle();

    /** Set the model of the page.
     */
    public void setModel(Object model);

    /** Set the title of the page.
     */
    public void setTitle(String title);
}


