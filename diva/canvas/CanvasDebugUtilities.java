/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
package diva.canvas;

import java.util.Iterator;


/** A collection of canvas utilities. These utilities perform
 * useful functions related to the structural aspects of diva.canvas
 * that do not properly belong in any one class. Some of them
 * perform utility geometric functions that are not available
 * in the Java 2D API, while others accept iterators over Figures
 * or Shapes and compute a useful composite result.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public final class CanvasDebugUtilities {
    public static String printContextTree(FigureLayer rootLayer) {
        String out = "LAYER:";
        TransformContext rootContext = rootLayer.getTransformContext();
        out = out + rootContext + "\n";

        for (Iterator i = rootLayer.figures(); i.hasNext();) {
            Figure root = (Figure) i.next();
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
            FigureSet fs = (FigureSet) root;

            for (Iterator i = fs.figures(); i.hasNext();) {
                Figure f = (Figure) i.next();
                out = out + printHelper(f, prefix + "  ", parent);
            }
        }

        return out;
    }
}
