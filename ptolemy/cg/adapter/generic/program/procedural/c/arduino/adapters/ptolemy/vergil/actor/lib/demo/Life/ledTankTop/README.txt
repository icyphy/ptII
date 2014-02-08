$Id: README.txt 44499 2006-11-27 19:35:36Z cxh $
LED Tank Top Code from
http://craftzine.com/01/led
http://www.cs.colorado.edu/~buechley/diy/diy_tank.html


TROUBLESHOOTING

******************************************************************************
Can't communicate with STK500 programmer
******************************************************************************
- Double check the wiring on your STK500 and your USB connection

- The latest firmware (version 2.x) on the STK500 is not compatible with UISP!
If you've just ordered a new STK500, you probably need to downgrade your 
firmware to version 1.x.  To do this you will need a PC.  On your PC: Download
AVR Studio 3.5 (not AVR Studio 4.x!) from: 
http://www.atmel.com/dyn/products/tools_card.asp?tool_id=2724.  
Install AVR Studio 3.5.  Connect your STK500 to your PC.  Then, follow these 
instructions (from the STK500 user manual) to install the old firmware:
	
	1- Power off the STK500 

	2- Push the 'PROGRAM' button (located near the AT45D021 dataflash)
	while powering up the STK500 

	3- Start AVR Studio and make sure there is a serial connection between
	the PC and the STK500 DSUB9 marked 'RS232 CTRL' 

	4- Start the program 'Avr Prog' located in the AVR Studio 'Tools' menu 

	5- Locate the firmware upgrade hex-file 'stk500.ebn' by pushing the
	'Browse'-button in the 'Avr Prog Hex File'-window. 
	The path for the 'stk500.ebn' for a normal AVR Studio installation is
	"C:\Program Files\Atmel\AVR Tools\STK500".(BROWSE TO THE stk500.ebn
	FILE FROM AVR Studio 3.5)

	6- Push the 'Program'-button in the 'Avr Prog Flash'-window. A
	progress bar will now appear while showing additional information
	messages. Wait until the verify operation is finished. 

	7- Close the 'Avr Prog' program 

	8- Power off and on the STK500 PCB. The STK500 is now ready to be used
	with the new firmware. 
 
******************************************************************************
Some pins on the chip aren't working
******************************************************************************
- The ATmega16 ships with pins C2-C5 disabled for general I/O and enabled for
debugging.  To enable them as general purpose I/O pins type "make unTAG" 
from your AVR code directory.  For more information, read up on the on-chip
debugging system and the "JTAGEN" fuse bit in the ATmega16 datasheet.
