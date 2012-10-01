/*
 ImageWriter accepts ByteArrayTokens from its input stream, writes
 them as a temp file and passes absolute path to the file via its output to Matlab.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.actor.lib.tld;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptserver.actor.ProxySource;
import ptserver.data.ByteArrayToken;

///////////////////////////////////////////////////////////////////
//// ImageWriter
/**
 * ImageWriter accepts ByteArrayTokens from its input stream, writes
 * them as a temp file and passes absolute path to the file via its output to Matlab.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ImageWriter extends TypedAtomicActor {

    /**
     * Create new instance of the ImageWriter.
     * @param container The parent container.
     * @param name The name of the actor.
     * @throws IllegalActionException if there is a problem instantiating the object.
     * @throws NameDuplicationException if there is a problem instantiating the object.
     */
    public ImageWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.GENERAL);
        output = new TypedIOPort(this, "ouput", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    /**
     * The input port accepting byte array tokens.
     */
    public TypedIOPort input;
    /**
     * The output port outputting absolute path to a temp file.
     */
    public TypedIOPort output;

    /**
     * Read one ByteArrayToken from the input port, write the byte array to
     * a temp file and send the file name via its output port.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        ByteArrayToken token = (ByteArrayToken) input.get(0);
        ByteArrayInputStream stream = new ByteArrayInputStream(token.getArray());

        CompositeEntity container = (CompositeEntity) this.getContainer();
        ProxySource source = (ProxySource) container.getEntity("Video_remote");
        source.getProxySourceData().getTokenQueue().clear();
        try {
            File temp = File.createTempFile("predator", ".jpg");
            temp.deleteOnExit();
            System.out.println(temp.getAbsolutePath());
            FileOutputStream f = new FileOutputStream(temp);
            int val;
            while ((val = stream.read()) != -1) {
                f.write(val);
            }
            f.close();
            output.send(0, new StringToken(temp.getAbsolutePath()));
        } catch (IOException e) {
            throw new IllegalActionException(this, e,
                    "Problem writing temp file");
        }
        return super.postfire();
    }

}
