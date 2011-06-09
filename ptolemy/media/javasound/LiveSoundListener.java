/* An interface implemented by objects that are interested in being kept
 informed about changes in the audio parameter values of LiveSound.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.media.javasound;

////////////////////////////////////////////////////////////////
//// LiveSoundListener

/**
 This interface is implemented by objects that are interested
 in being kept informed about changes in the audio parameters
 of LiveSound. The listeners register their interest through the
 addLiveSoundListener() method of LiveSound, and are informed of
 the changes by receiving instances of the LiveSoundEvent in the
 liveSoundChanged() method.

 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red
 @see LiveSoundEvent
 @see ptolemy.media.javasound.LiveSound
 */
public interface LiveSoundListener {
    /** Notify that the an audio parameter of LiveSound has
     *  changed.
     *
     *  @param event The live sound change event.
     *
     */
    public void liveSoundChanged(LiveSoundEvent event);
}
