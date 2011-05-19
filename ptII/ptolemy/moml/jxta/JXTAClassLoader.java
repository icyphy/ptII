/* A ClassLoader that exposed the defineClass() method.

 Copyright (c) 2002-2005 The Regents of the University of California.
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
package ptolemy.moml.jxta;

//////////////////////////////////////////////////////////////////////////
//// JXTAClassLoader

/**
 A class that extends the ClassLoader, so that the protected
 defineClass() method can be called.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (ellen_zh)
 @Pt.AcceptedRating Red (cxheecs.berkeley.edu)
 */
public class JXTAClassLoader extends ClassLoader {
    // FIXME: the name of this method needs to be changed
    public Class myDefineClass(String name, byte[] b, int off, int len) {
        Class myClass = null;

        try {
            // try to turn them into a class
            myClass = defineClass(name, b, 0, len);
        } catch (java.lang.ClassFormatError e) {
            // This is not a failure!  If we reach here, it might
            // mean that we are dealing with a class in a library,
            // such as java.lang.Object
        }

        return myClass;
    }

    // FIXME: the name of this method needs to be changed
    public void myResolveClass(Class c) {
        resolveClass(c);
    }
}
