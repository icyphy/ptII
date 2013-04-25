package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;

public class MetroDebugger {

    public MetroDebugger() {
        // TODO Auto-generated constructor stub
        turnOffDebugging(); 
    }

    public boolean debugging() {
        return _debugging;
    }

    public void turnOnDebugging() {
        _debugging = true;
    }

    public void turnOffDebugging() {
        _debugging = false;
    }

    public void printTitle(String title) {
        if (!_debugging) {
            return;
        }
        System.out.println("---------- " + title);
    }

    public void printText(String text) {
        if (!_debugging) {
            return;
        }
        System.out.println("DEBUG: " + text);
    }

    public void printMetroEvent(Builder event) {
        if (!_debugging) {
            return;
        }
        String buffer = "DEBUG:";

        if (event.hasTime()) {
            buffer = buffer.concat(" Time " + event.getTime().getValue());
        }

        buffer = buffer.concat(" " + event.getStatus().toString());

        buffer = buffer.concat(" " + event.getName().toString());

        System.out.println(buffer);
    }

    public void printMetroEvents(Iterable<Builder> metroIIEventList) {
        if (!_debugging) {
            return;
        }
        printText("Event List Begins");
        for (Builder event : metroIIEventList) {
            printMetroEvent(event);
        }
        printText("Event List Ends");
    }

    private boolean _debugging = false;

}
