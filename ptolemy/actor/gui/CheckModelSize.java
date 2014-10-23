/* Utility that checks the size of a model.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

//////////////////////////////////////////////////////////////////////////
//// CheckModelSize

/**
 Class that checks the size, zoom, and location of a model.
 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public class CheckModelSize {
    /** Check the size, zoom and location of the models named
     *  by the args.
     *  @param configuration The optional configuration to check.
     *  @param args An array of Strings naming the models to be checked.
     *  @return HTML that describes possible problems with the models.
     *  @exception Exception If there is a problem reading a model.
     */
    public static String checkModelSize(Configuration configuration,
            String[] args) throws Exception {
        StringBuffer results = new StringBuffer();
        Set sizeProblemSet = new HashSet();
        if (configuration != null) {
            List entityList = configuration.entityList(CompositeEntity.class);
            Iterator entities = entityList.iterator();
            while (entities.hasNext()) {
                Object entity = entities.next();
                if (entity instanceof TypedCompositeActor
                        && !sizeProblemSet.contains(entity)) {
                    String checkSizeOutput = _checkSize(
                            (TypedCompositeActor) entity, false);
                    if (!checkSizeOutput.equals("")) {
                        results.append("<tr>\n  <td>"
                                + ((TypedCompositeActor) entity).getFullName()
                                + "</td>\n  <td>" + checkSizeOutput + "</td>\n");
                    }
                    sizeProblemSet.add(entity);
                }
            }

            List classList = configuration.classDefinitionList();
            entities = classList.iterator();
            while (entities.hasNext()) {
                Object entity = entities.next();
                System.out.println("CheckModelSize: " + entity + " "
                        + (entity instanceof TypedCompositeActor));
                if (entity instanceof TypedCompositeActor
                        && !sizeProblemSet.contains(entity)) {
                    String checkSizeOutput = _checkSize(
                            (TypedCompositeActor) entity, false);
                    if (!checkSizeOutput.equals("")) {
                        results.append("<tr>\n  <td><b>Class</b> "
                                + ((TypedCompositeActor) entity).getFullName()
                                + "</td>\n  <td>" + checkSizeOutput + "</td>\n");
                    }
                    sizeProblemSet.add(entity);
                }
            }

        }

        for (String arg : args) {
            String fileName = arg;
            //             if (fileName.endsWith("ENM_11_18_04.xml")
            //                     || fileName.endsWith("IPCC_Base_Layers.xml")
            //                     || fileName.endsWith("dataFrame_R.xml")
            //                     || fileName.endsWith("eml_Table_as_Record_R.xml")
            //                     || fileName.endsWith("emlToRecord_R.xml")
            //                     || fileName.endsWith("eml-simple-linearRegression-R.xml")
            //                     || fileName.endsWith("eml-pairs-R.xml")) {
            //                 System.out.println("CheckModelSize: skipping " + fileName);
            //                 continue;
            //             }
            StringBuffer analysis = new StringBuffer();

            MoMLParser parser = new MoMLParser();
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            RemoveGraphicalClasses removeGraphicalClasses = new RemoveGraphicalClasses();

            // Remove StaticSchedulingCodeGenerator from Butterfly
            // because during the nightly build the codegenerator is
            // not yet built when the tests in actor.gui are run.
            removeGraphicalClasses.put(
                    "ptolemy.codegen.kernel.StaticSchedulingCodeGenerator",
                    null);
            // We need the _vergilSize attribute, which is removed by
            // removeGraphicalClasses, so we modify removeGraphicalClasses
            // so that it no longer removes SizeAttributes.
            removeGraphicalClasses.remove("ptolemy.actor.gui.SizeAttribute");

            // Filter out any graphical classes.
            MoMLParser.addMoMLFilter(removeGraphicalClasses);

            if (!(fileName.endsWith(".xml") || fileName.endsWith(".moml"))) {
                continue;
            }

            try {
                NamedObj top = null;
                try {
                    top = parser
                            .parse(null, new File(fileName).toURI().toURL());
                } catch (Exception ex) {
                    try {
                        top = parser.parse(null, new URL(fileName));
                    } catch (Exception ex2) {
                        throw new Exception("Failed to parse \"" + fileName
                                + "\". First exception:\n" + ex, ex2);
                    }
                }
                String checkSizeOutput = _checkSize(top, false);
                if (checkSizeOutput.equals("")) {
                    analysis.append(" seems to be OK.");
                } else {
                    analysis.append(checkSizeOutput);
                }

                if (top instanceof CompositeEntity) {
                    List entityList = ((CompositeEntity) top).deepEntityList();
                    Iterator entities = entityList.iterator();
                    while (entities.hasNext()) {
                        Object entity = entities.next();
                        if (entity instanceof TypedCompositeActor
                                && !sizeProblemSet.contains(entity)) {
                            checkSizeOutput = _checkSize(
                                    (TypedCompositeActor) entity, false);
                            if (!checkSizeOutput.equals("")) {
                                sizeProblemSet.add(entity);
                                results.append("<tr>\n  <td>"
                                        + ((TypedCompositeActor) entity)
                                        .getFullName()
                                        + "</td>\n  <td>" + checkSizeOutput
                                        + "</td>\n");
                            }
                            sizeProblemSet.add(entity);
                        }
                    }
                }

            } catch (Throwable throwable) {
                analysis.append(" can't be parsed because ");
                analysis.append(KernelException.stackTraceToString(throwable));
            }
            String fileURL = new File(fileName).toURI().toURL().toString();
            results.append("<tr>\n  <td><a href=\"" + fileURL + "\">" + fileURL
                    + "</a></td>\n  <td>" + analysis + "</td>\n");

        }

        return "<h1>Check Size</h1>\nBelow are the results from checking the "
        + "sizes of and centering of models\n<table>\n"
        + "<b>Note: after running review these results, be"
        + " sure to exit, as the graphical elements of the "
        + " models will have been removed</b>\n" + results.toString()
        + "</table>\n";
    }

    /** Check the size, zoom and location of the models named
     *  by the args.
     *  @param args An array of Strings naming the models to be checked.
     *
     */
    public static void main(String[] args) {
        try {
            System.out.println(CheckModelSize.checkModelSize(null, args));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /** Check the size and centering of the model.
     *  @param top The NamedObj to check
     *  @return A string describing size problems associated with the model.
     */
    private static String _checkSize(NamedObj top,
            boolean ignoreMissingVergilSize) {
        StringBuffer analysis = new StringBuffer();
        if (top instanceof CompositeActor) {
            SizeAttribute vergilSize = (SizeAttribute) top
                    .getAttribute("_vergilSize");
            ExpertParameter vergilZoom = (ExpertParameter) top
                    .getAttribute("_vergilZoomFactor");
            ExpertParameter vergilCenter = (ExpertParameter) top
                    .getAttribute("_vergilCenter");

            if (vergilSize != null) {
                try {
                    IntMatrixToken vergilSizeToken;
                    vergilSizeToken = (IntMatrixToken) vergilSize.getToken();

                    if (vergilSizeToken == null) {
                        throw new IllegalActionException(top,
                                "_vergilSize token was null?");
                    }
                    int width = vergilSizeToken.getElementAt(0, 0);
                    int height = vergilSizeToken.getElementAt(0, 1);

                    if (width > 800) {
                        analysis.append(" width(" + width + ") > 800");
                    }

                    if (height > 768) {
                        analysis.append(" height(" + height + ") > 768");
                    }

                    if (vergilCenter != null) {
                        try {
                            ArrayToken vergilCenterToken = (ArrayToken) vergilCenter
                                    .getToken();
                            double x = ((ScalarToken) vergilCenterToken
                                    .getElement(0)).doubleValue();
                            double y = ((ScalarToken) vergilCenterToken
                                    .getElement(1)).doubleValue();

                            // Avoid comparing floats.
                            if (Math.abs(x - width / 2.0) > 0.1
                                    || Math.abs(y - height / 2.0) > 0.1) {
                                analysis.append(" Center([" + x + ", " + y
                                        + "]) is not centered, should be ["
                                        + width / 2.0 + ", " + height / 2.0
                                        + "]");
                            }
                        } catch (IllegalActionException ex) {
                            analysis.append(" _vergilCenter malformed");
                            analysis.append(KernelException
                                    .stackTraceToString(ex));
                        }
                    }
                } catch (IllegalActionException ex) {
                    analysis.append(" _vergilSize malformed");
                    analysis.append(KernelException.stackTraceToString(ex));
                }

                if (vergilZoom != null) {
                    try {
                        DoubleToken vergilZoomToken = (DoubleToken) vergilZoom
                                .getToken();
                        double zoom = vergilZoomToken.doubleValue();

                        if (zoom != 1.0) {
                            analysis.append(" Zoom(" + zoom + ") != 1.0");
                        }
                    } catch (IllegalActionException ex) {
                        analysis.append(" _vergilZoom malformed");
                        analysis.append(KernelException.stackTraceToString(ex));
                    }
                }
            } else {
                if (!ignoreMissingVergilSize) {
                    analysis.append(" has no _vergilSize.");
                }
            }
        } else {
            analysis.append(" is a " + top.getClassName()
                    + " not a CompositeActor.");
        }

        return analysis.toString();
    }
}
