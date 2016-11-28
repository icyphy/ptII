/* simple.c
 * Designed to run on the Create module
 */




// Included files
#include <avr/interrupt.h>
#include <avr/io.h>
#include <util/delay.h>
#include "oi.h"


// Constants

#define CREATE_SONG  0
#define MODULE_SONG  1
#define USER_HW_SONG 2


// Functions
void initialize(void);
void powerOnRobot(void);
void baud28k(void);
void delay10ms(uint16_t delay_10ms);
uint8_t byteRx(void);
void flushRx(void);
void byteTx(uint8_t value);
void defineSongs(void);




int main (void)
{
  uint8_t create_play_button = 0;
  uint8_t module_button = 0;
  uint8_t added_button = 0;
  uint8_t flash_cnt = 0;
  uint8_t flash_state = 0;


  // Initialize the microcontroller
  initialize();

  // Turn on the Create power if off
  powerOnRobot();

  // Start the open interface
  byteTx(CmdStart);

  // Change to 28800 baud
  baud28k();

  // Define the songs
  defineSongs();

  // Take full control of the Create
  byteTx(CmdFull);

  // Get rid of unwanted data in the serial port receiver
  flushRx();

  for(;;)
  {
    // Request Sensors Packet 2
    byteTx(CmdSensors);
    byteTx(2);

    // Read the 6 bytes, only keep the Create Play button
    byteRx();
    create_play_button = (byteRx() & ButtonPlay);
    byteRx();
    byteRx();
    byteRx();
    byteRx();

    module_button = UserButtonPressed;
    added_button = (!(PINB & 0x02));


    if(create_play_button)
    {
      // Turn on Create Advance LED only
      byteTx(CmdLeds);
      byteTx(LEDAdvance);
      byteTx(0);
      byteTx(0);

      PORTC |= 0x02;
      LED1Off;

      byteTx(CmdPlay);
      byteTx(CREATE_SONG);
      delay10ms(263);
    }
    else if(module_button)
    {
      // Turn on module LED only
      byteTx(CmdLeds);
      byteTx(0x00);
      byteTx(0);
      byteTx(0);

      PORTC |= 0x02;
      LED1On;

      byteTx(CmdPlay);
      byteTx(MODULE_SONG);
      delay10ms(300);
    }
    else if(added_button)
    {
      // Turn on added LED only
      byteTx(CmdLeds);
      byteTx(0x00);
      byteTx(0);
      byteTx(0);

      PORTC &= ~0x02;
      LED1Off;

      byteTx(CmdPlay);
      byteTx(USER_HW_SONG);
      delay10ms(263);
    }
    else
    {
      // If no button press, flash the LEDs
      if(++flash_cnt >= 25)
      {
        flash_cnt = 0;
        flash_state = !flash_state;

        if(flash_state)
        {
          // Turn LEDs on
          byteTx(CmdLeds);
          byteTx(LEDAdvance);
          byteTx(0);
          byteTx(0);

          // User added LED
          PORTC &= ~0x02;
          LED1On;
        }
        else
        {
          // Turn LEDs off
          byteTx(CmdLeds);
          byteTx(0x00);
          byteTx(0);
          byteTx(0);

          // User Added LED
          PORTC |= 0x02;
          LED1Off;
        }
      }
    }
  }
}




void initialize(void)
{
  // Turn off interrupts
  cli();

  // Configure the I/O pins
  DDRB = 0x10;
  PORTB = 0xCF;
  DDRC = 0x02;
  PORTC = 0xFF;
  DDRD = 0xE6;
  PORTD = 0x7D;

  // Set up the serial port for 57600 baud
  UBRR0 = Ubrr57600;
  UCSR0B = (_BV(TXEN0) | _BV(RXEN0));
  UCSR0C = (_BV(UCSZ00) | _BV(UCSZ01));
}



void powerOnRobot(void)
{
  // If Create's power is off, turn it on
  if(!RobotIsOn)
  {
      while(!RobotIsOn)
      {
          RobotPwrToggleLow;
          delay10ms(50);  // Delay in this state
          RobotPwrToggleHigh;  // Low to high transition to toggle power
          delay10ms(10);  // Delay in this state
          RobotPwrToggleLow;
      }
      delay10ms(350);  // Delay for startup
  }
}



void baud28k(void)
{
  // Send the baud change command for 28800 baud
  byteTx(CmdBaud);
  byteTx(Baud28800);

  // Wait while until the command is sent
  while(!(UCSR0A & _BV(TXC0))) ;

  // Change the atmel's baud rate
  UBRR0 = Ubrr28800;

  // Wait 100 ms
  delay10ms(10);
}




void delay10ms(uint16_t delay_10ms)
{
  // Delay for (delay_10ms * 10) ms
  while(delay_10ms-- > 0)
  {
    // Call a 10 ms delay loop
    _delay_loop_2(46080);
  }
}




uint8_t byteRx(void)
{
  // Receive a byte over the serial port (UART)
  while(!(UCSR0A & _BV(RXC0))) ;
  return UDR0;
}




void flushRx(void)
{
  uint8_t temp;

  // Clear the serial port
  while(UCSR0A & _BV(RXC0))
    temp = UDR0;
}




void byteTx(uint8_t value)
{
  // Send a byte over the serial port
  while(!(UCSR0A & _BV(UDRE0))) ;
  UDR0 = value;
}



void defineSongs(void)
{
  // Create song
  byteTx(CmdSong);
  byteTx(CREATE_SONG);
  byteTx(5);
  byteTx(65);
  byteTx(24);
  byteTx(69);
  byteTx(24);
  byteTx(67);
  byteTx(48);
  byteTx(76);
  byteTx(24);
  byteTx(77);
  byteTx(48);

  // Module song
  byteTx(CmdSong);
  byteTx(MODULE_SONG);
  byteTx(6);
  byteTx(71);
  byteTx(24);
  byteTx(71);
  byteTx(24);
  byteTx(71);
  byteTx(24);
  byteTx(72);
  byteTx(24);
  byteTx(77);
  byteTx(48);
  byteTx(76);
  byteTx(48);

  // User-added hardware song
  byteTx(CmdSong);
  byteTx(USER_HW_SONG);
  byteTx(5);
  byteTx(62);
  byteTx(48);
  byteTx(65);
  byteTx(24);
  byteTx(67);
  byteTx(24);
  byteTx(62);
  byteTx(24);
  byteTx(69);
  byteTx(48);
}
