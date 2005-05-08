/* A tool for string manipulation.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ptolemy.backtrack.ast.UnknownASTException;

//////////////////////////////////////////////////////////////////////////
//// Strings
/**
   A tool for string manipulation.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class Strings {

    /** Combine two arrays and return the result of their combination. If any
     *  one of the two arrays is <tt>null</tt>, the other one is return. If
     *  both of them are <tt>null</tt>, <tt>null</tt> is returned. In the
     *  result, elements in <tt>array1</tt> appears before those in
     *  <tt>array2</tt>.
     * 
     *  @param array1 The first array.
     *  @param array2 The second array.
     *  @return The combination result of the two array.
     */
    public static String[] combineArrays(String[] array1, String[] array2) {
        if (array1 == null)
            return array2;
        else if (array2 == null)
            return array1;
        
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length,
                array2.length);
        return result;
    }
    
    /** Decode a string encoded with file names (or directory names) in it.
     *  Each file name in the string is separated with a path separator ({@link
     *  File#pathSeparator}) or a new line character.
     * 
     *  @param encode The string with file names encoded in it.
     *  @return The array of decoded file names.
     *  @see #encodeFileNames(String[])
     */
    public static String[] decodeFileNames(String encode) {
        StringTokenizer tokenizer =
            new StringTokenizer(encode, File.pathSeparator
                    + "\n\r");
        ArrayList list = new ArrayList();
        while (tokenizer.hasMoreElements())
            list.add(tokenizer.nextElement());
        return (String[])list.toArray(new String[list.size()]);
    }
    
    /** Encode file names (or directory names) in to a string. Each file name
     *  is separated with a path separator ({@link File#pathSeparator}).
     * 
     *  @param fileNames The array of file names.
     *  @return The string with file names encoded in it.
     *  @see #decodeFileNames(String)
     */
    public static String encodeFileNames(String[] fileNames) {
        StringBuffer path = new StringBuffer("");
        for (int i = 0; i < fileNames.length; i++) {
            path.append(fileNames[i]);
            path.append(File.pathSeparator);
        }
        return path.toString();
    }

    /** Convert an array of strings to an array of {@link URL}s with
     *  {@link File#toURL()}.
     *
     *  @param strings The array of strings.
     *  @return The array of urls.
     */
    public static URL[] stringsToUrls(String[] strings) {
        if (strings == null)
            return new URL[0];

        URL[] urls = new URL[strings.length];
        for (int i = 0; i < strings.length; i++)
            try {
                urls[i] = new File(strings[i]).toURL();
            } catch (MalformedURLException e) {
                throw new UnknownASTException();
            }
        return urls;
    }
}
