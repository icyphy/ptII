/* SerialBuffer manages the buffer for incoming messages over the serial port
*/
package  ptolemy.actor.lib.jjs.modules.IMUSensor;

public class SerialBuffer {
    public String Content = "";
    private String CurrentMsg, TempContent;
    private boolean available = false;
    private int LengthNeeded = 1;

    /**
     *
     * This function returns a string with a certain length from the incomin
     * messages.
     *
     * @param Length The length of the string to be returned.
     *
     */

    public synchronized String GetMsg(int Length) {
        LengthNeeded = Length;
        notifyAll();

        if (LengthNeeded > Content.length()) {
            available = false;
            while (available == false) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        CurrentMsg = Content.substring(0, LengthNeeded);
        TempContent = Content.substring(LengthNeeded);
        Content = TempContent;
        LengthNeeded = 1;
        notifyAll();
        return CurrentMsg;
    }

    /**
     *
     * This function stores a character captured from the serial port to the
     * buffer area.
     *
     * @param t The char value of the character to be stored.
     *
     */
    public synchronized void PutChar(int c) {
        Character d = Character.valueOf((char) c);
        Content = Content.concat(d.toString());
        if (LengthNeeded < Content.length()) {
            available = true;
        }
        notifyAll();
    }
}
