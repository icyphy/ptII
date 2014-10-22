/* Test class for moml.IconLoader

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.moml.test;

import java.io.InputStream;
import java.net.URL;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.IconLoader;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// TestIconLoader

/**
 Test class for ptolemy.moml.IconLoader.
 The IconLoader class has an abstract method, so we use this class
 to define that method.
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestIconLoader implements IconLoader {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Load an icon for a class in a particular context.
     *  @param className The name of the class for which the icon is
     *  to be loaded.
     *  @param context The context in which the icon is loaded.
     *  @return true if the icon was successfully loaded.
     *  @exception Exception If there is a problem adding
     *  the icon.
     */
    @Override
    public boolean loadIconForClass(String className, NamedObj context)
            throws Exception {
        String fileName = className.replace('.', '/') + "Icon.xml";
        URL xmlFile = getClass().getClassLoader().getResource(fileName);
        if (xmlFile == null) {
            return false;
        }
        InputStream input = xmlFile.openStream();
        MoMLParser newParser = new MoMLParser();
        newParser.setContext(context);
        newParser.parse(null, fileName, input);
        return true;
    }

}
