/* Base class to handle differences between Graphics class in JDK1.1 and 1.2

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.plot;

import java.awt.Graphics;

//////////////////////////////////////////////////////////////////////////
//// PtolemyGraphics.java
/** The sole purpose of this class is to workaround a monstrously poor
 design decision at Sun to add a method to the java.awt.Graphics class
 between JDK1.1 and JDK1.2.  Usually, adding a method to an abstract
 class is not a problem, derived classes can easily add dummy methods
 to handle both the old base class and the new base class.

 Unfortunately, the method that was added takes an argument of 
 a type that is only present in JDK1.2, so we cannot just add
 a method in the derived class and expect everything to compile
 under both JDK1.1 and JDK1.2.

 The evil workaround is to have two files, PtolemyGraphics1_1.java
 and PtolemyGraphics1_2.java and to copy the appropriate file to
 PtolemyGraphics.java, depending on the version of the JDK.

 This is an evil, unportable, and nasty hack, which would not be
 necessary if Java had a preprocessor, or Sun had not added this 
 bogus method. :-)


@author Christopher Hylands
@version $Id$
 */
public abstract class PtolemyGraphics extends Graphics {
}
