/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import java.util.Iterator;

/** A collection of canvas utilities. These utilities perform
 * useful functions related to the structural aspects of diva.canvas
 * that do not properly belong in any one class. Some of them
 * perform utility geometric functions that are not available
 * in the Java 2D API, while others accept iterators over Figures
 * or Shapes and compute a useful composite result.
 *
 * @version $Revision$
 * @author John Reekie
 * @rating Red
 */
public final class CanvasDebugUtilities {
    public static String printContextTree(FigureLayer rootLayer) {
        String out = "LAYER:";
        TransformContext rootContext = rootLayer.getTransformContext();
        out = out + rootContext + "\n";
        for (Iterator i = rootLayer.figures(); i.hasNext(); ) {
            Figure root = (Figure)i.next();
            out = out + printHelper(root, "  ", rootContext);
        }
        return out;
    }

    private static String printHelper(Figure root, String prefix,
            TransformContext parent) {
        String out = "";
        if (root.getTransformContext() != parent) {
            out = out + prefix + root + root.getTransformContext() + "\n";
        }
        if (root instanceof FigureSet) {
            FigureSet fs = (FigureSet)root;
            for (Iterator i = fs.figures(); i.hasNext(); ) {
                Figure f = (Figure)i.next();
                out = out + printHelper(f, prefix+"  ", parent);
            }
        }
        return out;
    }
}

