/* An actor that outputs doubles read from a URL.

 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
///////////////////////////////////////////////////////////////////
//// DoubleReader
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.io.IOException;
import java.lang.Object;
import java.util.StringTokenizer;
import ptolemy.actor.lib.URLReader;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.DoubleToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This actor reads tokens from an URL, and output them. Each entry in
 * the file corresponds to one iteration. If there are multiple fires in
 * the iteration, the same token will be repeated.  This actor has a
 * multiport, where each port corresponds to one column in the data file.
 * <p> The file format at the URL is assumed as the following.  A newline
 * character separates the rows, and a tab or a space character separates
 * the columns.
 * <p> The <i>sourceURL</i> parameter should be set to the name of the
 * file, specified as a fully qualified URL.  If the <i>sourceURL</i>
 * parameter is an empty string, then the System.in is used for input.
 * It is possible to load a file from the local file system by using the
 * prefix "file://" instead of "http://". Relative file paths are
 * allowed. To specify a file relative to the current directory, use
 * "../" or "./". For example, if the current directory contains a file
 * called "test.txt", then <i>sourceURL</i> should be set to
 * "file:./test.txt". If the parent directory contains a file called
 * "test.txt", then <i>sourceURL</i> should be set to
 * "file:../test.txt". To reference the file test.txt, located at
 * "/tmp/test.txt", <i>sourceURL</i> should be set to
 * "file:///tmp/test.txt" The default value is "file:///tmp/test.txt".
 * <p>FIXME: The type of the output ports is set to Double for now.
 * It should read a line in the prefire() and refer the type
 * from there.
 * <p>FIXME: Reader should read in expressions and serialized tokens
 * @author  Jie Liu, Christopher Hylands
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Red (liuj)
 * @Pt.AcceptedRating Red (liuj)
 * @deprecated Use ExpressionReader instead.
 */
public class DoubleReader extends URLReader implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Cache of one row.
    // FIXME: Should we clone this?
    // Valid enties in the data array.
    // FIXME: Should we clone this?
    private double[] _data;

    private int _dataSize;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public DoubleReader(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
    }

    /**
     * Output the data read in the prefire.
     * @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        for (int i = 0; i < _dataSize; i++) {
            output.send(i, new DoubleToken(_data[i]));
        }
    }

    /**
     * Open the file at the URL, and set the width of the output.
     * @exception IllegalActionException Not thrown in this base class
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_dataSize(output.getWidth());
        $ASSIGN$_data(new double[_dataSize]);
        attributeChanged(sourceURL);
    }

    /**
     * Read one row from the input and prepare for output them.
     * @exception IllegalActionException If an IO error occurs.
     */
    public boolean prefire() throws IllegalActionException  {
        try {
            $ASSIGN$_dataSize(output.getWidth());
            if (_data.length != _dataSize) {
                $ASSIGN$_data(new double[_dataSize]);
            }
            String oneRow = _reader.readLine();
            if (oneRow == null) {
                return false;
            }
            StringTokenizer tokenizer = new StringTokenizer(oneRow);
            int columnCount = tokenizer.countTokens();
            if (_dataSize > columnCount) {
                $ASSIGN$_dataSize(columnCount);
            }
            for (int i = 0; i < _dataSize; i++) {
                $ASSIGN$_data(i, Double.valueOf(tokenizer.nextToken()).doubleValue());
            }
            return super.prefire();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "prefire() failed");
        }
    }

    private final double[] $ASSIGN$_data(double[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_data.add(null, _data, $CHECKPOINT.getTimestamp());
        }
        return _data = newValue;
    }

    private final double $ASSIGN$_data(int index0, double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_data.add(new int[] {
                    index0
                }, _data[index0], $CHECKPOINT.getTimestamp());
        }
        return _data[index0] = newValue;
    }

    private final int $ASSIGN$_dataSize(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_dataSize.add(null, _dataSize, $CHECKPOINT.getTimestamp());
        }
        return _dataSize = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _data = (double[])$RECORD$_data.restore(_data, timestamp, trim);
        _dataSize = $RECORD$_dataSize.restore(_dataSize, timestamp, trim);
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
            $RESTORE(timestamp, trim);
        }
    }

    public final Checkpoint $GET$CHECKPOINT() {
        return $CHECKPOINT;
    }

    public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
        if ($CHECKPOINT != checkpoint) {
            Checkpoint oldCheckpoint = $CHECKPOINT;
            if (checkpoint != null) {
                $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                FieldRecord.pushState($RECORDS);
            }
            $CHECKPOINT = checkpoint;
            oldCheckpoint.setCheckpoint(checkpoint);
            checkpoint.addObject(this);
        }
        return this;
    }

    protected transient CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private transient FieldRecord $RECORD$_data = new FieldRecord(1);

    private transient FieldRecord $RECORD$_dataSize = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_data,
            $RECORD$_dataSize
        };

}

