

package ptolemy.media;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.media.sound.sampled.AudioStream;
import javax.media.sound.sampled.AudioSystem;
import javax.media.sound.sampled.AudioFormat;
import javax.media.sound.sampled.FileStream;
import javax.media.sound.sampled.OutputChannel;
import javax.media.sound.sampled.Mixer;
import javax.media.sound.sampled.AudioUnavailableException;

public class NewAudio implements Runnable {

    public NewAudio(double[] doublebuffer, AudioFormat af) throws IOException {
        this(_doubleToByte(doublebuffer, af), af);
    }

    public NewAudio(byte[] byteBuffer, AudioFormat af) throws IOException {
        this(new ByteArrayInputStream(byteBuffer), af, byteBuffer.length);
    }

    public NewAudio(InputStream is, AudioFormat af) throws IOException {
        this(is, af, AudioStream.UNKNOWN_LENGTH);
    }

    public NewAudio(InputStream is, AudioFormat af, long length) throws IOException {
        this(new AudioStream(is, af, length));
    }

    public NewAudio(AudioStream as) throws IOException {
        _loadAsStream(as);
    }

    public NewAudio(String fileName) throws IOException {
        _loadAsFile(fileName);
    }

    public NewAudio(String fileOrURLName, boolean isFile) throws MalformedURLException , IOException {
        if (isFile)
            _loadAsFile(fileOrURLName);
        else _loadAsURL(fileOrURLName);
    }

    public AudioStream getStream() {
        return _stream;
    }

    public AudioFormat getFormat() {
        return _stream.getFormat();
    }

    public double[] getDoubleArray() throws IOException {
        if (_doubleBuffer == null)
            if (_byteBuffer == null)
                _refreshBuffer();
            else _byteToDouble(getFormat());
        return _doubleBuffer;
    }

    public byte[] getByteArray() throws IOException {
        if (_byteBuffer == null)
            _refreshBuffer();
        return _byteBuffer;
    }

    public void saveAs(String filename, FileStream.FileType fileType) {
        if(_stream == null) {
            System.out.println("No loaded audio to save");
            return;
        }
        File file = new File( filename );
        try {
            if (AudioSystem.write(_stream, file, fileType, -1) == null)
                System.out.println("Could not save as " + fileType);
            else System.out.println("File saved.");
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
        }
    }

    public void loopPlayback() {
        _looping = true;
        startPlayback();
    }

    public void openPlayback() {
        OutputChannel channel = null;
        if (_stream == null) {
            System.out.println("AudioStream is null, cannot start playback");
            return;
        }
        if (_mixer == null)
            _mixer = AudioSystem.getMixer(null);
        try {
            channel = _mixer.getOutputChannel(_stream.getFormat(), 16384);
        } catch (AudioUnavailableException e2) {
            System.err.println("AudioUnavailableException: " + e2);
            return;
        }
        _target = channel;
        _current = _stream;
    }

    public void startPlayback() {
        openPlayback();
        if ( _pushThread != null ) {
            System.out.println("Playback thread is not null, cannot start playback : " + _pushThread);
            return;
        }
        if ( _current == null ) {
            System.out.println("InputStream not set, cannot start playback");
            return;
        }
        if ( _target == null ) {
            System.out.println("OutputChannel not set, cannot start playback");
            return;
        }
        _stopping = false;
        _stopped = false;
        _soundBuffer = new ByteArrayOutputStream();
        _pushThread = new Thread(this);
        _pushThread.start();
    }

    public void stopPlayback() {
        if( _target.isPaused() ) {
            _target.flush();
            _target.resume();
        }

        if ( _pushThread == null ) {
            System.out.println("Playback thread is null, cannot stop playback");
            return;
        }
        _stopping = true;
        while(!_stopped) {
            try{
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }
        _pushThread = null;
        _byteBuffer = _soundBuffer.toByteArray();
        _current = _stream = new AudioStream(new ByteArrayInputStream(_byteBuffer), _stream.getFormat(), _byteBuffer.length);
        _soundBuffer = new ByteArrayOutputStream();
        _looping = false;
    }

    public void pausePlayback() {
        if( _target == null ) {
            System.out.println("OutputChannel not set, cannot pause");
            return;
        } else {
            if( _target.isPaused() ) {
                return;
            } else {
                _target.pause();
                return;
            }
        }
    }

    public void resumePlayback() {
        if( _target == null )
            startPlayback();
        else if( _target.isPaused() )
            _target.resume();
    }

    public void run() {
        byte[] dataArray = new byte[_target.getBufferSize()/2];
        byte[] soundByte;
        int bytesRead, bytesRemaining;
        while(_stopping == false) {
            try {
                bytesRead = _current.read(dataArray);
                if (bytesRead == -1)
                    if (!_looping) {
                        _target.write(null, 0, 0);
                        break;
                    } else {
                        _byteBuffer = _soundBuffer.toByteArray();
                        System.out.println(_byteBuffer.length);
                        _current = new ByteArrayInputStream(_byteBuffer);
                        _soundBuffer = new ByteArrayOutputStream();
                        continue;
                    }
                bytesRemaining = bytesRead;
                while (bytesRemaining > 0) {
                    bytesRemaining -= _target.write(dataArray, 0, bytesRead);
                    if (getFormat().getEncoding() == AudioFormat.Encoding.PCM_SIGNED_BIG_ENDIAN)
                        _soundBuffer.write(dataArray, 0 , bytesRead);
                    else _soundBuffer.write(_reverse(dataArray, getFormat()), 0, bytesRead);
                }
            } catch (IOException ioe) {
                break;
            }
        }
        _stopped = true;
    }


    public String toString() {
        return "Ptolemy Audio\n"
            + "-------------------\n"
            + "size = " + getStream().getLength() + " bytes\n"
            + "format = " + getFormat() + "\n";
    }

    public void convert(AudioFormat af) throws IOException {
        AudioStream asold = _stream;
        _stream = AudioSystem.getAudioStream(af, asold);
        if (_stream == null) {
            _stream = asold;
            throw new IOException("Can't convert the audio stream to the following format : " + af);
        }
        _byteBuffer = null;
        _doubleBuffer = null;
    }

    public static final boolean IS_FILE = true;
    public static final boolean IS_URL = false;


    /* PRIVATE METHODS */

    private void _loadAsFile(String filename ) throws IOException {
        FileInputStream infile = new FileInputStream(filename);
        if (infile == null)
            throw new IOException ("Cannot open file: " + filename);
        else _loadAsStream(infile);
    }

    private void _loadAsURL(String urlname ) throws MalformedURLException, IOException {
        URL theurl = new URL(urlname);
        InputStream urlstream = theurl.openStream();
        if (urlstream == null)
            throw new IOException("Cannot open URL: " + urlname);
        else _loadAsStream(urlstream);
    }

    private void _loadAsStream(InputStream is) throws IOException {
        FileStream fs = AudioSystem.getFileStream(is);
        if (fs == null)
            throw new IOException("Unable to obtain audio data from file");
        AudioStream as = AudioSystem.getAudioStream( fs );
        if (as == null)
            throw new IOException("Unable to convert file data into audio data");
        _loadAsStream(as);
    }

    private void _loadAsStream(AudioStream as) throws IOException {
        if (as == null)
            throw new IOException("Can't load empty audio stream");
        AudioStream asold = as;
        if ((as.getFormat().getEncoding() == AudioFormat.Encoding.ULAW) ||
                (as.getFormat().getEncoding() == AudioFormat.Encoding.ALAW)) {
            AudioFormat af = as.getFormat();
            as = AudioSystem.getAudioStream(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED_BIG_ENDIAN,
                    af.getSampleRate(),
                    af.getSampleSizeInBits() * 2,
                    af.getChannels(),
                    af.getFrameSizeInBits() * 2,
                    af.getFrameRate())
                    , asold);
        }
        if (as == null)
            throw new IOException("Could not convert audio stream to linear format");
        _stream = as;
        _byteBuffer = null;
        _doubleBuffer = null;
    }

    private void _refreshBuffer() throws IOException {
        _byteBuffer = new byte[(int) _stream.getLength()];
        AudioFormat af = _stream.getFormat();
        _stream.read(_byteBuffer);
        _stream = new AudioStream(new ByteArrayInputStream(_byteBuffer), af, _byteBuffer.length);
        _byteToDouble(af);
    }

    private void _byteToDouble(AudioFormat af) {
        _doubleBuffer = new double[_byteBuffer.length / 2];
        long bits = 0;
        int i, j;
        int bitratio = af.getSampleSizeInBits() / 8;
        for (i = 0; i < _doubleBuffer.length ; i += 1) {
            for (j = 0, bits = 0; j < bitratio; j += 1)
                bits += (((long) _byteBuffer[bitratio * (i + 1)  - (j + 1)]) << (8 * j));
            _doubleBuffer[i] = ((double) bits) / Math.pow(2 , 8 * bitratio);
        }
    }

    private static byte[] _doubleToByte(double[] d, AudioFormat af) {
        byte[] b = new byte[2 * d.length];
        long bits;
        int bitratio = af.getSampleSizeInBits() / 8;
        int i, j;
        for (i = 0; i < d.length; i += 1) {
            bits = (long) (d[i] * Math.pow(2, 8 * bitratio));
            for (j = 0; j < bitratio; j += 1)
                b[bitratio * i + j] = (byte) ((bits << (8 * j)) >> (8 * (bitratio - 1)));
        }
        return b;
    }

    private void _doubleToByte(AudioFormat af) {
        _byteBuffer = new byte[2 * _doubleBuffer.length];
        long bits;
        int bitratio = af.getSampleSizeInBits() / 8;
        int i, j;
        for (i = 0; i < _doubleBuffer.length; i += 1) {
            bits = (long) (_doubleBuffer[i] * Math.pow(2, 8 * bitratio));
            for (j = 0; j < bitratio; j += 1)
                if (af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED_BIG_ENDIAN)
                    _byteBuffer[bitratio * i + j] = (byte) ((bits << (8 * j)) >> (8 * (bitratio - 1)));
                else  _byteBuffer[bitratio * (i + 1) - j - 1]
                          = (byte) ((bits << (8 * j)) >> (8 * (bitratio - 1)));
        }
    }

    private static byte[] _reverse(byte[] b, AudioFormat af) {
        byte[] br = new byte[b.length];
        int bytesPerSample = af.getSampleSizeInBits() / 8;
        for (int i = 0; i < (b.length / bytesPerSample); i += 1)
            for (int j = 0; j < bytesPerSample ; j += 1)
                br[i * bytesPerSample + (bytesPerSample - j - 1)] = b[i * bytesPerSample + j];
        return br;
    }

    /* PRIVATE FIELDS */

    private Mixer _mixer = null;
    private Thread _pushThread = null;
    private InputStream _current = null;
    private ByteArrayOutputStream _soundBuffer = new ByteArrayOutputStream();
    private boolean _stopped = false;
    private boolean _stopping = false;
    private boolean _looping = false;
    private OutputChannel _target = null;
    private AudioStream _stream = null;
    private byte[] _byteBuffer = null;
    private double[] _doubleBuffer = null;

}
