/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

import java.io.Reader;

/**
 * ModelParser is an interface that should be extended by application
 * specified model parsers.  It's job is to parse data into an
 * application specific data structure.
 *
 * @author Heloise Hse  (hwawen@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface ModelParser {
    /**
     * Parse the data in the given charater stream into a data
     * structure and return the data structure.
     */
    public Object parse(Reader reader) throws java.lang.Exception;
}


