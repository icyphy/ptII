//package fsa.rcx.demos.remote;

// Changes:
// 11/18/00 - Jose ported it from TinyVM to leJOS.

import josx.platform.rcx.*;

/**
   This class provides an active listener recognising IR messages from 
	 the remote control on the IR serial port. The class has the following
	 state information: motor state and sensor state.  It provides 
	 the following defaults handlers for the buttons on the remote:
	 <ul>
	 <li>message buttons (1/2/3) : to activate/passivate sensors
	 <li>motor buttons (A/B/C, up/down): to control motor power
		  <ul>
			<li>states are: backward 7 to backward 0 - STOP - forward 0 to forward 7
		  <li>up button to go to next state (unless state is forward 7)
			<li>down button to go to previous state (unless state is backward 7)
			</ul>
	 <li>P1...P5 buttons: no default handler, for your programs when extending the class
	 <li>stop button: reset the remote state to initial state (all motors off, all sensors activated)
		   <br>[Note: will not abort a running program]
	 <li>speaker button: plays a beep
	 </ul>
	 This class can: 
	 <ul>
	 <li>be easily extended, mainly to provide behaviors
	 for P1 through P5, other handlers can also be overridden if wished.
	 <li>be run in a separate (transparent) thread if you wish
	 </ul>
		
	 @author C. Ponsard  <chp@info.ucl.ac.be>
	 @version 1/11/2000
	 
 */ 
public class Remote extends Thread {

  private byte[] p1 = new byte[10];
	private byte[] p2 = new byte[10];

  private static final char[] stop={ 'S','T','O','P' };	
  
	protected int ma,mb,mc;      // motor speed:  0=stop, +/- for forward/backward, abs(mX-1)=motor power
	protected boolean s1,s2,s3;  // sensor state: true for active, false for passive
	
	/**
	  Constructor.  Initial state : all motor off, all sensors activated.
	*/
	public Remote() {
	  reset();
	}
	
	/**
	  Resets the state of the remote to initial state 
		(all motor off, all sensors activated)
	*/
	protected void reset() {
	  ma=mb=mc=0;
		s1=s2=s3=true;
		Motor.A.stop();	
		Motor.B.stop();
		Motor.C.stop();
		Sensor.S1.activate();
		Sensor.S2.activate();
		Sensor.S3.activate();
    TextLCD.print(stop);		
	}	
	
	/**
	  Handler for the motor A up button
	*/
	public void incMotorA() {
	  ma=inc(Motor.A,ma);
	}

	/**
	  Handler for the motor B up button
	*/
	public void incMotorB() {
	  mb=inc(Motor.B,mb);
	}

	/**
	  Handler for the motor C up button
	*/
	public void incMotorC() {
	  mc=inc(Motor.C,mc);
	}

	/**
	  Handler for the motor A down button
	*/
	public void decMotorA() {
	  ma=dec(Motor.A,ma);
	}

	/**
	  Handler for the motor B down button
	*/
	public void decMotorB() {
	  mb=dec(Motor.B,mb);
	}

	/**
	  Handler for the motor C down button
	*/
	public void decMotorC() {
	  mc=dec(Motor.C,mc);
	}

	/**
	  Motor increment 
	*/
  protected int inc(Motor m, int c) {
	  c++;
		if (c>8) { c=8; return c; }
		
		if (c==0) {
		  m.stop();
		} else if (c>0) {
		  m.setPower(c-1);
			m.forward();
		} else {
		  m.setPower(-c-1);
			m.backward();	
		}
		
	  display(c);
		return c;
	}
	
  /**
	  Motor decrement 
	*/	
  protected int dec(Motor m, int c) {
	  c--;
		if (c<-8) { c=-8; return c; }
		
		if (c==0) {
		  m.stop();
		} else if (c>0) {
		  m.setPower(c-1);
			m.forward();
		} else {
		  m.setPower(-c-1);
			m.backward();	
		}
		
		display(c);
		return c;
	}	
	
	/**
	  Handler for the message 1 button
	*/
	public void message1() {
	  if (s1) Sensor.S1.passivate();
		else    Sensor.S1.activate();
	}

	/**
	  Handler for the message 2 button
	*/
	public void message2() {
	  if (s2) Sensor.S2.passivate();
		else    Sensor.S2.activate();
	}

	/**
	  Handler for the message 3 button
	*/
	public void message3() {
	  if (s3) Sensor.S3.passivate();
		else    Sensor.S3.activate();
	}
	
	/**
	  Handler for the stop button. Note: default behavior won't stop a running program !)
	*/	
	public void stopAll() {
	  reset();		
	}
	
	/**
	  Handler for the speaker button
	*/	
	public void sound() {
//	  Sound.systemSound(true,2);
    Sound.beep();
	}
	
	/**
	  Handler for the program 1 button
	*/	
	public void program1() {
	}

	/**
	  Handler for the program 2 button
	*/	
	public void program2() {
	}

	/**
	  Handler for the program 3 button
	*/	
	public void program3() {
	}

	/**
	  Handler for the program 4 button
	*/	
	public void program4() {
	}
	
	/**
	  Handler for the program 5 button
	*/		
	public void program5() {
	}

	/**
	  Display motor speed
	*/	
	private void display(int c) {
	  if (c==0) TextLCD.print(stop);
		else LCD.setNumber(0x3001,(c-1),0x3002);
  	LCD.refresh();
	try { Thread.sleep(500); } catch (InterruptedException e) { }
  }

  /**
	  Event handler polling the IR serial port for messages
		from the remote control and invoking the right command.
		Can be run in a separate thread.
	*/
	public void run() {
    for (;;) {
			
      if (Serial.isPacketAvailable())  {				
				// read packet
        Serial.readPacket(p1);
				
        // basic check
				if ((p1[0] & 255) !=210) {
				  continue;
				}
				
				LCD.clear();
				LCD.refresh();
				
				// invoke right command
				
				int c1=p1[1] & 255;
				int c2=p1[2] & 255;

        if (c1==0) {
				  if (c2==0x01) message1();
				  else if (c2==0x02) message2();
				  else if (c2==0x04) message3();
				  else if (c2==0x08) incMotorA();
				  else if (c2==0x10) incMotorB();
				  else if (c2==0x20) incMotorC();
				  else if (c2==0x40) decMotorA();
				  else if (c2==0x80) decMotorB();
					
					try { Thread.sleep(50); } catch (InterruptedException e) {}
					continue;
				}
				
				if (c2==0) {
				  if (c1==0x01) decMotorC();
				  else if (c1==0x02) program1();
				  else if (c1==0x04) program2();
				  else if (c1==0x08) program3();
				  else if (c1==0x10) program4();
				  else if (c1==0x20) program5();
				  else if (c1==0x40) stopAll();
				  else if (c1==0x80) sound();
					
					try { Thread.sleep(50); } catch (InterruptedException e) { }
          continue;
				}	
      }
    }
  }

  /**
	  Main running a Remote instance
	*/
  public static void main (String[] arg) {
	  Remote rc=new Remote();
		rc.run();  // does not start a new thread but runs in the main thread
	}
	
}

