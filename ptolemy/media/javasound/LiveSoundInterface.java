package ptolemy.media.javasound;

import java.io.IOException;

public interface LiveSoundInterface {

    int getBufferSizeCapture();

    int getBufferSizePlayback();

    int getChannels();

    int getSampleRate();

    double[][] getSamples(Object consumer) throws IOException,
            IllegalStateException;

    int getTransferSize();

    void putSamples(Object producer, double[][] samplesArray)
            throws IOException, IllegalStateException;

    void removeLiveSoundListener(LiveSoundListener listener);

    void resetCapture();

    void resetPlayback();

    void setBitsPerSample(int bitsPerSample) throws IOException;

    void setBufferSize(int bufferSize) throws IOException;

    void setChannels(int channels) throws IOException;

    void setSampleRate(int sampleRate) throws IOException;

    void setTransferSize(int transferSize);

    void startCapture(Object consumer) throws IOException,
            IllegalStateException;

    void startPlayback(Object producer) throws IOException,
            IllegalStateException;

    void stopCapture(Object consumer) throws IOException, IllegalStateException;

    void stopPlayback(Object producer) throws IOException,
            IllegalStateException;

    void flushCaptureBuffer(Object consumer) throws IOException,
            IllegalStateException;

    void addLiveSoundListener(LiveSoundListener listener);

    int getBitsPerSample();

    int getBufferSize();

    void flushPlaybackBuffer(Object producer) throws IOException,
            IllegalStateException;

    boolean isCaptureActive();

    boolean isPlaybackActive();

}
