/* A representative of a file that contains one or more tokens.

 Copyright (c) 1998-2018 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;

import ptolemy.actor.lib.image.ImageTableau;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ImageTokenEffigy

/**
 An effigy for a file that contains an image.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class ImageTokenEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public ImageTokenEffigy(Workspace workspace) {
        super(workspace);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ImageTokenEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>uri</i> parameter, then read the
     *  specified URL and parse the data contained in it.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL cannot be read or
     *   if the data is malformed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // The superclass does some handling of the url attribute.
        super.attributeChanged(attribute);

        if (attribute == uri) {
            try {
                URL urlToRead = uri.getURL();

                if (urlToRead != null) {
                    read(urlToRead);
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, null, ex,
                        "Failed to read data: " + ex.getMessage());
            }
        }
    }

    /** Clear the token array associated with this effigy.
     */
    public void clear() {
        _token = null;

        // Notify the contained tableaux.
        Iterator tableaux = entityList(TokenTableau.class).iterator();

        while (tableaux.hasNext()) {
            TokenTableau tableau = (TokenTableau) tableaux.next();
            tableau.clear();
        }
    }

    /** Return the image represented by this effigy.
     *  @return An image token.
     *  @see #setImage(ImageToken)
     */
    public ImageToken getImage() {
        return _token;
    }

    /** Read the specified URL and parse the data.
     *  @param input The URL to read.
     *  @exception IOException If an error occurs while reading the URL
     *   or parsing the data.
     */
    public void read(URL input) throws IOException {
        if (input == null) {
            throw new IOException("Attempt to read from null input.");
        }
        BufferedImage image = ImageIO.read(input);
        if (image != null) {
            // Notify the contained tableaux.
            Iterator tableaux = entityList(ImageTableau.class).iterator();
            while (tableaux.hasNext()) {
                try {
                    ((ImageTableau) tableaux.next())
                            .append(new AWTImageToken(image));
                } catch (IllegalActionException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }

    /** Specify the image represented by this effigy.
     *  @param token The image represented by this effigy.
     *  @exception IllegalActionException If the token is not acceptable.
     *  @see #getImage()
     */
    public void setImage(ImageToken token) throws IllegalActionException {
        _token = token;

        // Notify the contained tableaux.
        Iterator tableaux = entityList(TokenTableau.class).iterator();

        while (tableaux.hasNext()) {
            TokenTableau tableau = (TokenTableau) tableaux.next();
            tableau.append(token);
        }
    }

    /** Write the current data of this effigy to the specified file.
     *  The filename extension is used to determine the format.
     *  Understood extensions include "jpg", "jpeg", "png", and "gif" (not
     *  case sensitive).
     *  @param file The file to write to, or null to write to standard out.
     *  @exception IOException If the write fails.
     */
    @Override
    public void writeFile(File file) throws IOException {
        // Get the filename extension.
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index > 0) {
            String extension = name.substring(index + 1).toLowerCase();
            if (extension.equals("jpeg")) {
                extension = "jpg";
            } else if (!extension.equals("jpg") && !extension.equals("png")
                    && !extension.equals("gif")) {
                throw new IOException("Unrecognized file extension: "
                        + extension + ". Should be one of jpg, png, or gif.");
            }
            if (!ImageIO.write((BufferedImage) _token.asAWTImage(), extension,
                    file)) {
                throw new IOException(
                        "No writer found for file type: " + extension);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The image token.
    private ImageToken _token;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new effigies.
     */
    public static class Factory extends EffigyFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return false, indicating that this effigy factory is not
         *  capable of creating an effigy without a URL being specified.
         *  @return False.
         */
        @Override
        public boolean canCreateBlankEffigy() {
            return false;
        }

        /** Create a new effigy in the given container by reading the
         *  specified URL. If the specified URL is null, or
         *  if the URL does not end with extension ".jpeg", ".jpg", ".png",
         *  or ".gif", then return null.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, which are
         *   ignored here, and therefore can be null.
         *  @param input The input URL.
         *  @return A new instance of ImageTokenEffigy, or null if the URL
         *   does not have a recognized extension.
         *  @exception Exception If the URL cannot be read.
         */
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base,
                URL input) throws Exception {
            if (input != null) {
                String extension = getExtension(input).toLowerCase();

                if (extension.equals("jpg") || extension.equals("jpeg")
                        || extension.equals("png") || extension.equals("gif")) {
                    ImageTokenEffigy effigy = new ImageTokenEffigy(container,
                            container.uniqueName("effigy"));
                    effigy.uri.setURL(input);
                    BufferedImage image = ImageIO.read(input);
                    ImageToken token = new AWTImageToken(image);
                    effigy.setImage(token);
                    return effigy;
                }
            }

            return null;
        }
    }
}
