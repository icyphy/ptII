/* light.c
 * Designed to run on the Create module
 *
 * The basic architecture of this program can be re-used to easily
 * write a wide variety of Create control programs.  All sensor values
 * are polled in the background (using the serial rx interrupt) and
 * stored in the sensors array as long as the function
 * delayAndUpdateSensors() is called periodically.  Users can send commands
 * directly a byte at a time using byteTx() or they can use the
 * provided functions, such as baud() and drive().
 */



// Includes
#include <avr/interrupt.h>
#include <avr/io.h>
#include <stdlib.h>
#include "oi.h"

// Constants
#define START_SONG 0
#define ALARM_SONG 1


// Global variables
volatile uint16_t timer_cnt = 0;
volatile uint8_t timer_on = 0;
volatile uint8_t sensors_flag = 0;
volatile uint8_t sensors_index = 0;
volatile uint8_t sensors_in[Sen0Size];
volatile uint8_t sensors[Sen0Size];
volatile int16_t distance = 0;
volatile int16_t angle = 0;



// Functions
void byteTx(uint8_t value);
void delayMs(uint16_t time_ms);
void delayAndUpdateSensors(unsigned int time_ms);
void initialize(void);
void powerOnRobot(void);
void baud(uint8_t baud_code);
void drive(int16_t velocity, int16_t radius);
uint16_t randomAngle(void);
void defineSongs(void);




int main (void)
{
  int16_t turn_angle = 0;
  uint8_t turn_dir = 1;
  uint8_t turning = 0;
  uint8_t backing_up = 0;

  uint8_t leds_on = 0;
  int16_t light_start = 0;
  int16_t light_now = 0;
  uint8_t alarm = 0;


  // Set up Create and the Command Module
  initialize();
  LEDBothOn;
  powerOnRobot();
  byteTx(CmdStart);
  baud(Baud28800);
  defineSongs();
  byteTx(CmdControl);
  byteTx(CmdFull);


  for(;;)
  {
    // Turn on all the leds
    byteTx(CmdLeds);
    byteTx(LEDsBoth);
    // Make Create's power LED orange
    byteTx(128);
    byteTx(255);

    LEDBothOn;

    delayAndUpdateSensors(1);


    if(UserButtonPressed)
    {
      // Play start song and wait
      byteTx(CmdPlay);
      byteTx(START_SONG);
      delayAndUpdateSensors(2250);

      // Sample the light level
      ADCSRA |= 0x40;
      while(ADCSRA & 0x40) ;
      light_start = ADC;

      // drive around while no alarm and until cliff detected, plugged in, or button pressed
      while(!alarm
            && (!UserButtonPressed)
            && (!sensors[SenCliffL])
            && (!sensors[SenCliffFL])
            && (!sensors[SenCliffFR])
            && (!sensors[SenCliffR])
            && (!sensors[SenChAvailable]) )
      {
        // Sample the light level
        ADCSRA |= 0x40;
        while(ADCSRA & 0x40) ;
        light_now = ADC;

        // Compare and set the alarm if the light is brighter
        if(light_now > (light_start + (light_start >> 1)))
          alarm = 1;

        // Keep turning until the specified angle is reached
        if(turning)
        {
          if(backing_up)
          {
            if((-distance) > 12)
              backing_up = 0;
            drive(-200, RadStraight);
          }
          else
          {
            if(turn_dir)
            {
              if(angle > turn_angle)
                turning = 0;
              drive(200, RadCCW);
            }
            else
            {
              if((-angle) > turn_angle)
                turning = 0;
              drive(200, RadCW);
            }
          }
        }
        else if(sensors[SenBumpDrop] & BumpBoth)  // Check for a bump
        {
          // Set the turn paramters and reset the angle
          if(sensors[SenBumpDrop] & BumpLeft)
            turn_dir = 0;
          else
            turn_dir = 1;
          backing_up = 1;
          turning = 1;
          distance = 0;
          angle = 0;
          turn_angle = randomAngle();
        }
        else
        {
          // Otherwise, drive straight
          drive(300, RadStraight);
        }

        delayAndUpdateSensors(1);
      }

      // Stop
      drive(0, RadStraight);

      if(alarm)
      {
        alarm = 0;

        while(!UserButtonPressed)
        {
          // Play the alarm continuously
          byteTx(CmdPlay);
          byteTx(ALARM_SONG);

          // Flash the leds
          leds_on = !leds_on;
          if(leds_on)
          {
            byteTx(CmdLeds);
            byteTx(LEDsBoth);
            byteTx(128);
            byteTx(255);
            LEDBothOn;
          }
          else
          {
            byteTx(CmdLeds);
            byteTx(0x00);
            byteTx(0);
            byteTx(0);
            LEDBothOff;
          }

          // Delay between flashes
          delayAndUpdateSensors(100);
        }
      }

      // Turn off leds and delay before restarting
      byteTx(CmdLeds);
      byteTx(0x00);
      byteTx(0);
      byteTx(0);
      LEDBothOff;
      delayAndUpdateSensors(1500);
    }
  }
}




// Serial receive interrupt to store sensor values
SIGNAL(SIG_USART_RECV)
{
  uint8_t temp;


  temp = UDR0;

  if(sensors_flag)
  {
    sensors_in[sensors_index++] = temp;
    if(sensors_index >= Sen0Size)
      sensors_flag = 0;
  }
}




// Timer 1 interrupt to time delays in ms
SIGNAL(SIG_OUTPUT_COMPARE1A)
{
  if(timer_cnt)
    timer_cnt--;
  else
    timer_on = 0;
}




// Transmit a byte over the serial port
void byteTx(uint8_t value)
{
  while(!(UCSR0A & _BV(UDRE0))) ;
  UDR0 = value;
}




// Delay for the specified time in ms without updating sensor values
void delayMs(uint16_t time_ms)
{
  timer_on = 1;
  timer_cnt = time_ms;
  while(timer_on) ;
}




// Delay for the specified time in ms and update sensor values
void delayAndUpdateSensors(uint16_t time_ms)
{
  uint8_t temp;

  timer_on = 1;
  timer_cnt = time_ms;
  while(timer_on)
  {
    if(!sensors_flag)
    {
      for(temp = 0; temp < Sen0Size; temp++)
        sensors[temp] = sensors_in[temp];

      // Update running totals of distance and angle
      distance += (int)((sensors[SenDist1] << 8) | sensors[SenDist0]);
      angle += (int)((sensors[SenAng1] << 8) | sensors[SenAng0]);

      byteTx(CmdSensors);
      byteTx(0);
      sensors_index = 0;
      sensors_flag = 1;
    }
  }
}




// Initialize the Mind Control's ATmega168 microcontroller
void initialize(void)
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
  UBRR0 = 19;
  UCSR0B = (_BV(RXCIE0) | _BV(TXEN0) | _BV(RXEN0));
  UCSR0C = (_BV(UCSZ00) | _BV(UCSZ01));

  // Set up the ADC on pin C5
  DIDR0 |= 0x20;  // disable digital input on C5
  PRR &= ~_BV(PRADC); // Turn off ADC power save
  ADCSRA = (_BV(ADEN) | _BV(ADPS2) | _BV(ADPS1) | _BV(ADPS0)); // Enabled, prescaler = 128
  ADMUX = (_BV(REFS0) | 0x05); // set voltage reference, select channel C5

  // Turn on interrupts
  sei();
}



void powerOnRobot(void)
{
  // If Create's power is off, turn it on
  if(!RobotIsOn)
  {
      while(!RobotIsOn)
      {
          RobotPwrToggleLow;
          delayMs(500);  // Delay in this state
          RobotPwrToggleHigh;  // Low to high transition to toggle power
          delayMs(100);  // Delay in this state
          RobotPwrToggleLow;
      }
      delayMs(3500);  // Delay for startup
  }
}



// Switch the baud rate on both Create and module
void baud(uint8_t baud_code)
{
  if(baud_code <= 11)
  {
    byteTx(CmdBaud);
    UCSR0A |= _BV(TXC0);
    byteTx(baud_code);
    // Wait until transmit is complete
    while(!(UCSR0A & _BV(TXC0))) ;

    cli();

    // Switch the baud rate register
    if(baud_code == Baud115200)
      UBRR0 = Ubrr115200;
    else if(baud_code == Baud57600)
      UBRR0 = Ubrr57600;
    else if(baud_code == Baud38400)
      UBRR0 = Ubrr38400;
    else if(baud_code == Baud28800)
      UBRR0 = Ubrr28800;
    else if(baud_code == Baud19200)
      UBRR0 = Ubrr19200;
    else if(baud_code == Baud14400)
      UBRR0 = Ubrr14400;
    else if(baud_code == Baud9600)
      UBRR0 = Ubrr9600;
    else if(baud_code == Baud4800)
      UBRR0 = Ubrr4800;
    else if(baud_code == Baud2400)
      UBRR0 = Ubrr2400;
    else if(baud_code == Baud1200)
      UBRR0 = Ubrr1200;
    else if(baud_code == Baud600)
      UBRR0 = Ubrr600;
    else if(baud_code == Baud300)
      UBRR0 = Ubrr300;

    sei();

    delayMs(100);
  }
}




// Send Create drive commands in terms of velocity and radius
void drive(int16_t velocity, int16_t radius)
{
  byteTx(CmdDrive);
  byteTx((uint8_t)((velocity >> 8) & 0x00FF));
  byteTx((uint8_t)(velocity & 0x00FF));
  byteTx((uint8_t)((radius >> 8) & 0x00FF));
  byteTx((uint8_t)(radius & 0x00FF));
}




// Return an angle value in the range 53 to 180 (degrees)
uint16_t randomAngle(void)
{
    return (53 + ((uint16_t)(random() & 0xFF) >> 1));
}



// Define songs to be played later
void defineSongs(void)
{
  // Start song
  byteTx(CmdSong);
  byteTx(START_SONG);
  byteTx(5);
  byteTx(60);
  byteTx(24);
  byteTx(79);
  byteTx(24);
  byteTx(75);
  byteTx(24);
  byteTx(72);
  byteTx(24);
  byteTx(76);
  byteTx(48);

  // Alarm song
  byteTx(CmdSong);
  byteTx(ALARM_SONG);
  byteTx(7);
  byteTx(86);
  byteTx(24);
  byteTx(83);
  byteTx(24);
  byteTx(86);
  byteTx(24);
  byteTx(83);
  byteTx(24);
  byteTx(86);
  byteTx(24);
  byteTx(83);
  byteTx(24);
  byteTx(0);
  byteTx(96);
}
