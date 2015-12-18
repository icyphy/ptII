/* An internal component to bind the runtime's class loading strategy as default for all MomlParser instances.

Copyright (c) 2015 The Regents of the University of California; iSencia Belgium NV.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.moml.activator;

import org.ptolemy.classloading.ClassLoadingStrategy;

import ptolemy.moml.MoMLParser;

/**
   An internal component to bind the runtime's class loading strategy
   as default for all MomlParser instances.

   @author ErwinDL
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (ErwinDL)
   @Pt.AcceptedRating Yellow (ErwinDL)
 */

public class MoMLParserClassLoadingStrategyBinding {

  public void init(ClassLoadingStrategy clStrategy) {
    MoMLParser.setDefaultClassLoadingStrategy(clStrategy);
  }
}
