package ptolemy.backtrack.util.java.util;

import java.io.Serializable;
import java.lang.Object;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

public class Random implements Serializable, Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    private boolean haveNextNextGaussian;

    private double nextNextGaussian;

    private long seed;

    private static final long serialVersionUID = 3905348978240129619L;

    public Random() {
        this(System.currentTimeMillis());
    }

    public Random(long seed) {
        setSeed(seed);
    }

    public synchronized void setSeed(long seed) {
        this.$ASSIGN$seed((seed ^ 0x5DEECE66DL) & ((1L << 48) - 1));
        $ASSIGN$haveNextNextGaussian(false);
    }

    protected synchronized int next(int bits) {
        $ASSIGN$seed((seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1));
        return (int)(seed >>> (48 - bits));
    }

    public void nextBytes(byte[] bytes) {
        int random;
        int max = bytes.length & ~0x3;
        for (int i = 0; i < max; i += 4) {
            random = next(32);
            bytes[i] = (byte)random;
            bytes[i + 1] = (byte)(random >> 8);
            bytes[i + 2] = (byte)(random >> 16);
            bytes[i + 3] = (byte)(random >> 24);
        }
        if (max < bytes.length) {
            random = next(32);
            for (int j = max; j < bytes.length; j++) {
                bytes[j] = (byte)random;
                random >>= 8;
            }
        }
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive");
        if ((n & -n) == n)
            return (int)((n * (long)next(31)) >> 31);
        int bits, val;
        do {
            bits = next(31);
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    public long nextLong() {
        return ((long)next(32) << 32) + next(32);
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        return next(24) / (float)(1 << 24);
    }

    public double nextDouble() {
        return (((long)next(26) << 27) + next(27)) / (double)(1L << 53);
    }

    public synchronized double nextGaussian() {
        if (haveNextNextGaussian) {
            $ASSIGN$haveNextNextGaussian(false);
            return nextNextGaussian;
        }
        double v1, v2, s;
        do {
            v1 = 2 * nextDouble() - 1;
            v2 = 2 * nextDouble() - 1;
            s = v1 * v1 + v2 * v2;
        } while (s >= 1);
        double norm = Math.sqrt(-2 * Math.log(s) / s);
        $ASSIGN$nextNextGaussian(v2 * norm);
        $ASSIGN$haveNextNextGaussian(true);
        return v1 * norm;
    }

    private final boolean $ASSIGN$haveNextNextGaussian(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$haveNextNextGaussian.add(null, haveNextNextGaussian, $CHECKPOINT.getTimestamp());
        }
        return haveNextNextGaussian = newValue;
    }

    private final double $ASSIGN$nextNextGaussian(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$nextNextGaussian.add(null, nextNextGaussian, $CHECKPOINT.getTimestamp());
        }
        return nextNextGaussian = newValue;
    }

    private final long $ASSIGN$seed(long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$seed.add(null, seed, $CHECKPOINT.getTimestamp());
        }
        return seed = newValue;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        haveNextNextGaussian = $RECORD$haveNextNextGaussian.restore(haveNextNextGaussian, timestamp, trim);
        nextNextGaussian = $RECORD$nextNextGaussian.restore(nextNextGaussian, timestamp, trim);
        seed = $RECORD$seed.restore(seed, timestamp, trim);
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
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

    protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private FieldRecord $RECORD$haveNextNextGaussian = new FieldRecord(0);

    private FieldRecord $RECORD$nextNextGaussian = new FieldRecord(0);

    private FieldRecord $RECORD$seed = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$haveNextNextGaussian,
            $RECORD$nextNextGaussian,
            $RECORD$seed
        };
}
