/* Interface for actors.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.LinkedList;

import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// Reception
/**
A class that defines the data structure being passed between actors in
a composite channel.

FIXME: I am not sure whether this is the sturcture we want. For example,
should it contains a receivers list or not? I think, the advantage for "yes"
is it makes the ChannelOutput actor's design easy and clear (the token in a
reception is put to all the receives in the list); the disadvantage is it
causes actors manipulate on a reception to be specific for this domain...

@author Yang
@version $ $
@since Ptolemy II 3.0
*/
public class Reception {
    public Token token;
    public WirelessIOPort sender;
    public Token properties;
    public LinkedList receivers;
}
