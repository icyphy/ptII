/* An interface for a frame that can have multiple containers.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package org.mlc.swing.layout;

import java.awt.Container;

/** An interface for frames that can have multiple containers.
 *  Classes that implement this interface are required to be subclasses
 *  of JFrame.
 *  <p>
 *  I added this interface so that FormEditor could be contained
 *  in frames that were not instances of LayoutFrame.  LayoutFrame
 *  implements this interface.
 *
 *  @author Edward A. Lee eal&#064;eecs.berkeley.edu
@version $Id$
@since Ptolemy II 8.0
 */
public interface MultiContainerFrame {

    /** Add a container with the specified name.
     *  @param name The name of the container.
     *  @param container The container.
     */
    public void addContainer(String name, Container container);

    /** Return true if the frame has a container with the specified name.
     *  @param name The name of the container.
     */
    public boolean hasContainer(String name);

    /** Remove the container with the specified name.
     *  This may throw a RuntimeException if the container does not exist.
     *  @param name The name of the container.
     */
    public void removeContainer(String name);
}
