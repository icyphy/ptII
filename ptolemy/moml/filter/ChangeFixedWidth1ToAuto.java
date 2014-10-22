/**
 A filter for backward compatibility with 7.2.devel or earlier models for width inference.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.moml.filter;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import ptolemy.actor.IORelation;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// ChangeFixedWidth1ToAuto

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.
 This class will filter for relations that have a fixed width of 1.
 The width value will be changed to AUto, which is the new default
 for width inference.

 @author Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */
public class ChangeFixedWidth1ToAuto extends MoMLFilterSimple {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method doesn't do anything.
     *  @param container  The container for XML element.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return A new value for the attribute, or the same value
     *   to leave it unchanged, or null to cause the current element
     *   to be ignored (unless the attributeValue argument is null).
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        return attributeValue;
    }

    /** Filter relations widths and change 1 to "Auto" and make sure still value
     * is not not stored (if not changed).
     *
     *  @param container The object defined by the element that this
     *   is the end of.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception If there is a problem modifying the
     *  specified container.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
        if (container instanceof IORelation) {
            IORelation relation = (IORelation) container;
            IntToken t = (IntToken) relation.width.getToken();

            if (t != null) {
                int width = t.intValue();

                if (width == 1) {
                    madeModification = true;
                    relation.width.setToken("Auto");
                    relation.width.setDerivedLevel(1);
                    // Make it derived to make sure it is not
                    // saved if not changed.
                }
            }
        }
    }

    /** Main function. Changes fixed relation width equal to 1 to "Auto" and save the model. The resulting width
     * won't be saved in practice.
     * @param args The arguments of the main function.
     * @exception Exception If the model can't be converted.
     */
    public static void main(String[] args) throws Exception {
        String errorMessage = "Usage: \n\tConvert one model:\n\t\tjava -classpath $PTII "
                + "ptolemy.moml.filter.ChangeFixedWidth1ToAuto model.xml\n\tConvert all models in a folder:\n\t"
                + "\tjava -classpath $PTII "
                + "ptolemy.moml.filter.ChangeFixedWidth1ToAuto -all path\n\tConvert all models in a demo folder:\n\t\tjava -classpath $PTII "
                + "ptolemy.moml.filter.ChangeFixedWidth1ToAuto -demo path";
        if (args.length != 1 && args.length != 2) {
            System.err.println(errorMessage);
            return;
        }

        if (args.length == 1) {
            _updateFile(args[0]);
        }

        if (args.length == 2) {
            if (!args[0].equals("-all") && !args[0].equals("-demo")) {
                System.err.println(errorMessage);
                return;
            }
            _updateXMLFiles(new File(args[1]), args[0].equals("-demo") ? "demo"
                    : null);
        }
    }

    /** Return a string that describes what the filter does.
     *  @return A description of the filter (ending with a newline).
     */
    @Override
    public String toString() {
        return Integer.toHexString(hashCode());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Convert the models in the folder.
     * @param folder The folder in which models need to be converted.
     * @param filter If null, no filter will be applied. On models for which
     * the path contains "/" + filter + "/" are converted.
     */
    private static void _updateXMLFiles(File folder, String filter) {
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                String filename = file.getName();
                int length = filename.length();
                if (length > 3
                        && filename.substring(length - 4, length)
                                .toLowerCase(Locale.getDefault())
                                .equals(".xml")) {
                    try {
                        if (filter == null
                                || file.toURI().toString()
                                        .toLowerCase(Locale.getDefault())
                                        .contains("/" + filter + "/")) {
                            _updateFile(file.toString());
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } else if (file.isDirectory()) {
                _updateXMLFiles(file, filter);
            }
        }
    }

    /** Convert the model with name fileName.
     * @param fileName The name (and path) of the model
     * @exception Exception If the model can't be converted
     */
    private static void _updateFile(String fileName) throws Exception {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(null);
        MoMLParser.addMoMLFilters(BackwardCompatibility.allFilters());
        ChangeFixedWidth1ToAuto filter = new ChangeFixedWidth1ToAuto();
        MoMLParser.addMoMLFilter(filter);

        URL xmlFile = ChangeFixedWidth1ToAuto.class.getClassLoader()
                .getResource(fileName);

        if (xmlFile != null) {
            InputStream input = xmlFile.openStream();
            NamedObj model = parser.parse(null, fileName, input);
            input.close();
            if (model != null && filter.madeModification) {
                System.out.println("Start updating " + fileName);
                FileWriter file = null;
                try {
                    file = new FileWriter(fileName);
                    model.exportMoML(file, 0);
                } finally {
                    if (file != null) {
                        file.close();
                    }
                }
                System.out.println("End updating " + fileName);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Keep track of modifications.*/
    private boolean madeModification = false;

}
