/*
  Transform Actors using Soot and generate C code.

  Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.c;

import ptolemy.copernicus.kernel.WatchDogTimer;
import soot.Pack;
import soot.PackManager;


//////////////////////////////////////////////////////////////////////////
//// Main

/** Read in a MoML model, generate .c files with very few
    dependencies on Ptolemy II.

    @author Shuvra S. Bhattacharyya, Michael Wirthlin, Stephen Neuendorffer,
    Edward A. Lee, Christopher Hylands
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class Main extends ptolemy.copernicus.java.Main {
    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        // super.addTransforms();
        addStandardTransforms(_toplevel);

        Pack pack = PackManager.v().getPack("wjtp");

        addTransform(pack, "wjtp.finalSnapshotC", CWriter.v());

        addTransform(pack, "wjtp.watchDogCancel", WatchDogTimer.v(),
                "cancel:true");
    }
}
