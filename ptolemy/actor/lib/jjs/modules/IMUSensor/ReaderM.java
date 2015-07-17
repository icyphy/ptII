/* ReaderM class is a manager for reading the unique packet data from the IMU sensor

// Copyright (c) 2015 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS. 
*/
package ptolemy.actor.lib.jjs.modules.IMUSensor.node;

import ptolemy.actor.lib.jjs.modules.IMUSensor.io.SerialBean;
import ptolemy.actor.lib.jjs.modules.IMUSensor.main.CircularFifoQueue;

/////////////////////////////////////////////////////////////////////////
///// ReaderM

/**
	@author Hunter Massey and Rajesh Kuni
	@version $Id$
	@see SerialPortController
	@Pt.ProposedRating Yellow Hunter
	@Pt.AcceptedRating 
*/

public class ReaderM extends Thread {
    
    /** isStart is true when the thread has been started */
    public boolean isStart = false;
   
    char dataCount = 0; // The current count of data bytes that have been read in from a packet
    int com; // The communication port number
    int baudRate; // The baud rate of the serial port
    SerialBean sb = null; // SerialBean takes care of serial port communication on a lower level
    static final char DLE = 0x10; // "Data Link Escape" character, signals that next value is a control value
    static final char SOH = 0x01; // "Start of Header" character, signals start of non-data header value
    static final char EOT = 0x04; // "End of Transmission" character, signals end of a single    

    private int window = 0; // The window size of the circular buffer
    private int bufIndex = 0; // Holds the current index in circular buffer to which next sample will be placed
    private int nextReadBufIndex = 0; // Holds the current index from which the next unread sample is located
    private CircularFifoQueue<int[]> buf; // Holds six integer arrays of sample values - [accX, accY, accZ, gyroX, gyroY, gyroZ]

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Constructor for ReaderM, takes in a COM port integer, a baudRate, and a buffer window size 
     *  @param com The comm port number to connect to
     *  @param baudRate The baud rate at which the connection runs
     *  @param window The window size of the circular sample buffer
    */
    public ReaderM(int com, int baudRate, int window) 
    {
	// Set base values from constructor and construct circular buffer for storing samples
        super();
        this.baudRate = baudRate;
        this.com = com;
        this.window = window;
        buf = new CircularFifoQueue<int[]>(window);
    }

    
    /** Thread run method, starts a thread. Inside this thread we grab packets from the IMU and decode them */
    public void run() 
    {
        sb = new SerialBean(com); // initialize the serial port connection
        sb.baudRate = this.baudRate;
        System.out.println("before");
        sb.Initialize();
        System.out.println(this +  " after"); // If this is printed, then the connection was properly established. USE THIS FOR DEBUGGING CONNECTION ISSUES
        while (true) 
        {
            if (isStart) 
            {
		// While thread is started, decode incoming packets
                while (isStart) 
                {
		    // initialize default values
                    char cData = 0;
                    char pData = 0;
                    char packet[] = new char[256];
                    dataCount = 0;

		    // While we have not reached a control message or start of packet parse data values
		    // In this while loop we are looking for the start of a packet to begin reading in a message
                    while (((pData != DLE) || (cData != SOH))) 
                    {
                        pData = cData;

			try{
	                        cData = (char) sb.ReadPort(1).toCharArray()[0];
			} catch(Exception e){e.printStackTrace();}
                        if ((pData == DLE) && (cData == DLE)) 
                        {
                            pData = cData;
                            cData = (char) sb.ReadPort(1).toCharArray()[0];
                        }
                    }
		    // While we are not at end of transmission continue reading in data values
                    while (((pData != DLE) || (cData != EOT))) 
                    {
                        pData = cData;
                        cData = (char) sb.ReadPort(1).toCharArray()[0];

                        if (cData != DLE)
                        {
                            packet[(dataCount++) % 256] = cData;
                        }
                        else 
                        {
                            pData = cData;
                            cData = (char) sb.ReadPort(1).toCharArray()[0];
                            if (cData == DLE) 
                            {
                                packet[(dataCount++) % 256] = cData;
                            }
                        }
                    }
                    // received correct and complete data if dataCount==22 (size of data in packet)
                    if (dataCount == 22) 
                    {
                        int ax = UCharToInt(packet[0], packet[1]);
                        int ay = UCharToInt(packet[2], packet[3]);
                        int az = UCharToInt(packet[4], packet[5]);
                        int gx = UCharToInt(packet[6], packet[7]);
                        int gy = UCharToInt(packet[8], packet[9]);
                        int gz = UCharToInt(packet[10], packet[11]);
                        
                        int[] raw = {ax, ay, az, gx, gy, gz};
                        preProcess(raw);
                    }

                }
            } 
            else 
            {
                sb.ReadPort(10);
            }
        }
    }
    
    /** Grabs the individual raw sensor (accelerometer and gyroscope) values and 
     *	stores them into the circular sample buffer as a 6 integer array 
     *  @param raw The integer array containing all the sensor values (ADC values)
     */
    public void preProcess(int[] raw)
    {
	// Obtain the individual sensor readings (accelerometer, gyroscope axes) - used for testing
        //int ax = raw[0];
        //int ay = raw[1];
        //int az = raw[2];
	//int gx = raw[3];
	//int gy = raw[4];
	//int gz = raw[5];

        buf.add(raw); // add array of raw data to circular buffer

        bufIndex++;
        if(bufIndex == window)
        {
            bufIndex = window - 1;
        }
    }

    /** Returns the sample buffer, of type CircularFifoQueue<int[]> 
     *  @return The circular sample buffer
    */
    public CircularFifoQueue<int[]> getBuffer()
    {
        return buf;
    }

    /** closes the serial port and ends the connection */
    public void stopRead(){
	sb.closePort();
    }
	
    /** returns the current index in the circular buffer 
     *  @return the current write index into the circular buffer
    */	
    public int getBufIndex(){
        return bufIndex;
    }
		
    /** Gets the next unread sample from the sample buffer 
     *  @return the current read index into the circular buffer
    */
    public int[] getNextUnreadSample(){
	if(nextReadBufIndex < bufIndex) {
		nextReadBufIndex++;
	}
	return buf.get(nextReadBufIndex-1);
    }

    /** Converts two 'unsigned char's to an integer by shifting and adding. 
     *  As a note, Java does not actually have unsigned values but we treat
     *  these chars as such
     *  @param a The most significant 8 bits of the integer in char form
     *  @param b The least significant 8 bits of the integer in char form
     *  @return The integer value from the two input unsigned chars
    */
    public int UCharToInt(char a, char b) 
    {
        int myInt = ((a << 8) + b);
        if (myInt >= 32768) {
            //myInt = (~myInt)*(-1);
            myInt = myInt - 65536;
        }
        return myInt;
    }

}
    
