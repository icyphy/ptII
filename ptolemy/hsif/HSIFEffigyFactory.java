/* An object that can create a new Effigy from an HSIF file.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.hsif;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// EffigyFactory
/**
An object that can create a new Effigy from an HSIF file.
An HSIF filename can end with either .xml or .hsif
@author Haiyang Zheng, Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.2
@see Configuration
@see Effigy
*/
public class HSIFEffigyFactory extends EffigyFactory {

    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public HSIFEffigyFactory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false, indicating that this effigy factory is not
     *  capable of creating an effigy without a URL being specified.
     *  @return False.
     */
    public boolean canCreateBlankEffigy() {
        return false;
    }

    /** Create a new effigy in the given container by reading the
     *  specified URL, which must end with either .xml or .hsif.  If
     *  the first 20 lines of the file contain the string "HSIF.dtd",
     *  then the specified URL refers to an HSIF file.  If the URL is
     *  an HSIF file, then invok the HSIF to MoML translator to create
     *  a MoML temporary file, and then delegate to the container of
     *  this effigy factory to open that file. If the specified file
     *  is not HSIF, return null.
     
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @return A new effigy.
     *  @exception Exception If the stream cannot be read, or if the data
     *   is malformed in some way.
     */
    public Effigy createEffigy(CompositeEntity container, URL base, URL input)
            throws Exception {
        if (_inCreateEffigy) {
            return null;
        }

        // Check whether the URL ends with .xml or .hsif
        if (input != null) {
            String extension = EffigyFactory.getExtension(input);
            if (!extension.equals("xml")
                && !extension.equals("hsif") ) {
                return null;
            }
        }

        if (_isHSIF(input)) {
            try {
                _inCreateEffigy = true;

                // We need to operate on urls here in case we
                // are operating under Web Start and the model
                // is a JAR URL that starts with jar:file:

                // Generate a MoML file with a name 'xxx_moml.xml'
                String inputFileName = input.toString();
                // The directory and base name
                String inputDirectoryBaseName = inputFileName;

                int index = inputFileName.lastIndexOf(".");
                if (index >= 0) {
                    inputDirectoryBaseName = inputFileName.substring(0, index);
                }
                String temporaryOutputFileName =
                    inputDirectoryBaseName + "_moml.xml";


                // Try to open the output file before we go through
                // the trouble of ding the conversion.
                FileWriter outputFileWriter = null;
                try {
                    outputFileWriter = new FileWriter(temporaryOutputFileName);
                } catch (IOException ex) {
                    // Try to open up a temporary file.
                    // If we are running under Web Start, then
                    // temporaryOutputFileName is likely a jar url, and
                    // it cannot be written to
                    String baseName = inputDirectoryBaseName;
                    // Under Windows, the separator will always be a /
                    // because we converted a URL to a string.
                    index = inputDirectoryBaseName.lastIndexOf("/");
                    if (index > 0) {
                        baseName = inputDirectoryBaseName
                            .substring(index,
                                    inputDirectoryBaseName.length());
                    }

                    File temporaryOutputFile;
                    try {
                        temporaryOutputFile =
                            File.createTempFile(baseName, ".xml");
                    } catch (IOException ex2) {
                        // JDK1.4.1_01 is so lame that it might not report
                        // what the problem was, instead it reports:
                        // "The filename, directory name, or volume label
                        // syntax is incorrect"
                        // FIXME: IOException does not take a cause argument?
                        throw new Exception("Could not create a temporary "
                                + "file based on '" + baseName
                                + "'", ex2);
                    }

                    // Save the new name of the file so we can
                    // tell the user about it and open the resulting model.
                    temporaryOutputFileName =
                        temporaryOutputFile.toString();
                    try {
                        outputFileWriter = new FileWriter(temporaryOutputFile);
                    } catch (IOException ex3) {
                        // FIXME: IOException does not take a cause argument?
                        throw new Exception("Could not open '"
                                + temporaryOutputFile
                                + "', also tried '"
                                + temporaryOutputFileName
                                + "' where the exception was:",
                                ex);
                    }
                }

                System.out.print("Converting HSIFToMoML ('"
                        + inputFileName + "' to '"
                        + temporaryOutputFileName + "'");

                // Read in from the URL so that Web Start works.
                HSIFUtilities.HSIFToMoML(input.toString(),
                        outputFileWriter);
                outputFileWriter.close();
                System.out.println(" Done");

                URL temporaryOutputURL =
                    MoMLApplication.specToURL(temporaryOutputFileName);

                // Note that createEffigy might end up substituting %20
                // for spaces.
                Effigy effigy = ((EffigyFactory)getContainer())
                    .createEffigy(container,
                            temporaryOutputURL, temporaryOutputURL);

                effigy.identifier.setExpression(temporaryOutputURL.toString());
                return effigy;
            } finally {
                _inCreateEffigy = false;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return true if the input file is a HSIF file.
    private static boolean _isHSIF(URL inputURL) throws IOException {

        InputStream inputStream = null;
        try {
            inputStream = inputURL.openStream();
        } catch (FileNotFoundException ex) {
            // Try it as a jar URL
            try {
                URL jarURL =
                    JNLPUtilities.jarURLEntryResource(inputURL.toString());
                if (jarURL == null) {
                    throw new Exception("'" + inputURL + "' was not a jar "
                            + "URL, or was not found");
                }
                inputStream = jarURL.openStream();
            } catch (Exception ex2) {
                // FIXME: IOException does not take a cause argument
                throw ex;
            }
        }

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(inputStream));

        String inputLine;

        int lineCount = 0;
        try {
            while ((inputLine = reader.readLine()) != null) {
                // FIXME:  all we are doing is looking for the
                // string HSIF.dtd in the first 20 lines
                if (inputLine.indexOf("HSIF.dtd") != -1) {
                    return true;
                }
                if (lineCount++ > 20) {
                    return false;
                }
            }
            return false;
        } finally {
            reader.close();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _inCreateEffigy;
}
