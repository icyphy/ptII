/* Set of function definitions.
 * From the example provided, simple.c
 * Designed to run on the Create command module.
 */

// Included files
#include <avr/interrupt.h>
#include <avr/io.h>
#include <util/delay.h>
#include "oi.h"

// Declare functions here so that there can be forward references to them.
void initializeRobot(void);
void powerOnRobot(void);
void baud28k(void);
void delay10ms(uint16_t delay_10ms);
uint8_t byteRx(void);
void flushRx(void);
void byteTx(uint8_t value);




void initializeRobot(void)
{
  // Turn off interrupts (by Edward)
  cli();

  // Configure the I/O pins
  DDRB = 0x10;
  PORTB = 0xCF;
  // Make port C all input pins.
  DDRC = 0x00;
  PORTC = 0xFF;
  DDRD = 0xE6;
  PORTD = 0x7D;


  // Set up timer 1 to generate an interrupt every 1 ms
  //TCCR1A = 0x00;
  //TCCR1B = (_BV(WGM12) | _BV(CS12));
  //OCR1A = 71;
  //TIMSK1 = _BV(OCIE1A);

  // Set up the serial port for 57600 baud
  UBRR0 = Ubrr57600;
  UCSR0B = (_BV(TXEN0) | _BV(RXEN0));
  UCSR0C = (_BV(UCSZ00) | _BV(UCSZ01));

  // Turn on interrupts (by Patrick)
  sei();
}



/*
// Initialize the Mind Control's ATmega168 microcontroller
void initializeRobot(void)
{
  cli();

  // Set I/O pins
  DDRB = 0x10;
  PORTB = 0xCF;
  DDRC = 0x00;
  PORTC = 0xFF;
  DDRD = 0xE6;
  PORTD = 0x7D;

  // Set up timer 1 to generate an interrupt every 1 ms
  TCCR1A = 0x00;
  TCCR1B = (_BV(WGM12) | _BV(CS12));
  OCR1A = 71;
  TIMSK1 = _BV(OCIE1A);

  // Set up the serial port with rx interrupt
  //UBRR0 = 19;
  UBRR0 = Ubrr57600;
  UCSR0B = (_BV(RXCIE0) | _BV(TXEN0) | _BV(RXEN0));
  UCSR0C = (_BV(UCSZ00) | _BV(UCSZ01));

  // Turn on interrupts
  sei();
}

*/


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

/** Busy wait for the specified amount of
 *  time, as a multiple of 10ms.
 */
void delay10ms(uint16_t delay_10ms)
{
  // Delay for (delay_10ms * 10) ms
  while(delay_10ms-- > 0)
  {
    // Call a 10 ms delay loop
    _delay_loop_2(46080);
  }
}

/** Busy wait for the specified amount of
 *  time, as a multiple of 1ms.
 */
void delay1ms(uint16_t delay_1ms)
{
  // Delay for (delay_10ms * 10) ms
  while(delay_1ms-- > 0)
  {
    // Call a 10 ms delay loop
    _delay_loop_2(4608);
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
