/* An application that executes non-graphical
 models specified on the command line.

 Copyright (c) 2001-2014 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// MoMLSimpleApplication

/** A simple application that reads in a .xml file as a command
 line argument and runs it.

 <p>MoMLApplication sets the look and feel, which starts up Swing,
 so we can't use MoMLApplication for non-graphical simulations.

 <p>We implement the ChangeListener interface so that this
 class will get exceptions thrown by failed change requests.

 For example to use this class, try:
 <pre>
 java -classpath $PTII ptolemy.actor.gui.MoMLSimpleApplication ../../../ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml
 </pre>

 @deprecated Use {@link ptolemy.moml.MoMLSimpleApplication} instead.
 MoMLSimpleApplication does not depend on anything in actor.gui
 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (eal)
 */
@Deprecated
public class MoMLSimpleApplication extends ptolemy.moml.MoMLSimpleApplication {
    /** A Nullary constructor is necessary so that we can extends this
     *  base class with a subclass.
     *  @exception Exception Not thrown in this base class
     */
    public MoMLSimpleApplication() throws Exception {
    }

    /** Parse the xml file and run it.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public MoMLSimpleApplication(String xmlFileName) throws Throwable {
        super(xmlFileName);
    }
}
