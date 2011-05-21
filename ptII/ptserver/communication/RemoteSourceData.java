package ptserver.communication;

import java.util.concurrent.ArrayBlockingQueue;

import ptserver.actor.RemoteSource;
import ptserver.data.CommunicationToken;

public class RemoteSourceData {
    private RemoteSource remoteSource;
    private ArrayBlockingQueue<CommunicationToken> tokenQueue;

    public RemoteSourceData(RemoteSource remoteSource) {
        this.setRemoteSource(remoteSource);
        setTokenQueue(new ArrayBlockingQueue<CommunicationToken>(100));
        remoteSource.setTokenQueue(tokenQueue);
    }

    public void setRemoteSource(RemoteSource remoteSource) {
        this.remoteSource = remoteSource;
    }

    public RemoteSource getRemoteSource() {
        return remoteSource;
    }

    public void setTokenQueue(
            ArrayBlockingQueue<CommunicationToken> tokenQueue) {
        this.tokenQueue = tokenQueue;
    }

    public ArrayBlockingQueue<CommunicationToken> getTokenQueue() {
        return tokenQueue;
    }

}