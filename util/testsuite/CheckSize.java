/* Utility that checks the size of a model.

Copyright (c) 2004 The Regents of the University of California.
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
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_3
COPYRIGHTENDKEY
*/
package util.testsuite;

import java.io.File;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

//////////////////////////////////////////////////////////////////////////
//// CheckSize
/**
   Class that checks the size, zoom, and location of a model.
   @author Rowland R Johnson
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (rowland)
   @Pt.AcceptedRating Red (rowland)
*/

public class CheckSize {

    public CheckSize(String[] args) throws Exception {
        int width, height;
        double x, y, zoom;
        String fileName = args[0];
        String analysis = "";

        MoMLParser parser = new MoMLParser();
        parser.setMoMLFilters(BackwardCompatibility.allFilters());

        // Filter out any graphical classes.
        parser.addMoMLFilter(new RemoveGraphicalClasses());

        try {
            NamedObj top = parser.parse(null, new File(fileName).toURL());
            if (top instanceof CompositeActor) {
                SizeAttribute vergilSize =
                    (SizeAttribute) top.getAttribute("_vergilSize");
                ExpertParameter vergilZoom =
                    (ExpertParameter) top.getAttribute("_vergilZoomFactor");
                ExpertParameter vergilCenter =
                    (ExpertParameter) top.getAttribute("_vergilCenter");
                if (vergilSize != null) {
                    try {
                        IntMatrixToken vergilSizeToken;
                        vergilSizeToken =
                            (IntMatrixToken) vergilSize.getToken();
                        width = vergilSizeToken.getElementAt(0, 0);
                        height = vergilSizeToken.getElementAt(0, 1);
                        if (width > 800) {
                            analysis += " width(" + width + ") > 800";
                        }
                        if (height > 768) {
                            analysis += " width(" + height + ") > 768";
                        }
                        if (vergilCenter != null) {
                            try {
                                ArrayToken vergilCenterToken =
                                    (ArrayToken) vergilCenter.getToken();
                                x =
                                    ((ScalarToken) vergilCenterToken
                                        .getElement(0))
                                        .doubleValue();
                                y =
                                    ((ScalarToken) vergilCenterToken
                                        .getElement(1))
                                        .doubleValue();
                                if ((x != ((double) width) / 2.0)
                                    || (y != ((double) height) / 2.0)) {
                                    analysis += " Center(["
                                        + x
                                        + ", "
                                        + y
                                        + "]) is not centered, should be ["
                                        + (((double) width) / 2.0)
                                        + ", "
                                        + (((double) height) / 2.0)
                                        + "]";
                                }
                            } catch (IllegalActionException e) {
                                analysis += " _vergilCenter malformed";
                                e.printStackTrace();
                            }
                        }
                    } catch (IllegalActionException e) {
                        analysis += " _vergilSize malformed";
                        e.printStackTrace();
                    }

                    if (vergilZoom != null) {
                        try {
                            DoubleToken vergilZoomToken =
                                (DoubleToken) vergilZoom.getToken();
                            zoom = vergilZoomToken.doubleValue();
                            if (zoom != 1.0)
                                analysis += " Zoom(" + zoom + ") != 1.0";
                        } catch (IllegalActionException e) {
                            analysis += " _vergilZoom malformed";
                            e.printStackTrace();
                        }
                    }

                } else {
                    analysis += " has no _vergilSize.";
                }
            } else {
                analysis += " is a "
                    + top.getClassName()
                    + " not a CompositeActor.";
            }
            if (analysis.equals("")) {
                analysis += " seems to be OK.";
            }
        } catch (Throwable t) {
            analysis += " can't be parsed because " + t.getCause();
        }
        System.out.println("Check Size " + fileName + analysis);
    }

    public static void main(String[] args) {
        try {
            new CheckSize(args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
