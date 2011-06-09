/* oi.h
 *
 * Definitions for the Open Interface
 */


// Command values
#define CmdStart        128
#define CmdBaud         129
#define CmdControl      130
#define CmdSafe         131
#define CmdFull         132
#define CmdSpot         134
#define CmdClean        135
#define CmdDemo         136
#define CmdDrive        137
#define CmdMotors       138
#define CmdLeds         139
#define CmdSong         140
#define CmdPlay         141
#define CmdSensors      142
#define CmdDock         143
#define CmdPWMMotors    144
#define CmdDriveWheels  145
#define CmdOutputs      147
#define CmdSensorList   149
#define CmdIRChar       151


// Sensor byte indices - offsets in packets 0, 5 and 6
#define SenBumpDrop     0            
#define SenWall         1
#define SenCliffL       2
#define SenCliffFL      3
#define SenCliffFR      4
#define SenCliffR       5
#define SenVWall        6
#define SenOverC        7
#define SenIRChar       10
#define SenButton       11
#define SenDist1        12
#define SenDist0        13
#define SenAng1         14
#define SenAng0         15
#define SenChargeState  16
#define SenVolt1        17
#define SenVolt0        18
#define SenCurr1        19
#define SenCurr0        20
#define SenTemp         21
#define SenCharge1      22
#define SenCharge0      23
#define SenCap1         24
#define SenCap0         25
#define SenWallSig1     26
#define SenWallSig0     27
#define SenCliffLSig1   28
#define SenCliffLSig0   29
#define SenCliffFLSig1  30
#define SenCliffFLSig0  31
#define SenCliffFRSig1  32
#define SenCliffFRSig0  33
#define SenCliffRSig1   34
#define SenCliffRSig0   35
#define SenInputs       36
#define SenAInput1      37
#define SenAInput0      38
#define SenChAvailable  39
#define SenOIMode       40
#define SenOISong       41
#define SenOISongPlay   42
#define SenStreamPckts  43
#define SenVel1         44
#define SenVel0         45
#define SenRad1         46
#define SenRad0         47
#define SenVelR1        48
#define SenVelR0        49
#define SenVelL1        50
#define SenVelL0        51


// Sensor packet sizes
#define Sen0Size        26
#define Sen1Size        10
#define Sen2Size        6
#define Sen3Size        10
#define Sen4Size        14
#define Sen5Size        12
#define Sen6Size        52

// Sensor bit masks
#define WheelDropFront  0x10
#define WheelDropLeft   0x08
#define WheelDropRight  0x04
#define BumpLeft        0x02
#define BumpRight       0x01
#define BumpBoth        0x03
#define BumpEither      0x03
#define WheelDropAll    0x1C
#define ButtonAdvance   0x04
#define ButtonPlay      0x01


// LED Bit Masks
#define LEDAdvance       0x08
#define LEDPlay         0x02
#define LEDsBoth        0x0A

// OI Modes
#define OIPassive       1
#define OISafe          2
#define OIFull          3


// Baud codes
#define Baud300         0
#define Baud600         1
#define Baud1200        2
#define Baud2400        3
#define Baud4800        4
#define Baud9600        5
#define Baud14400       6
#define Baud19200       7
#define Baud28800       8
#define Baud38400       9
#define Baud57600       10
#define Baud115200      11


// Drive radius special cases
#define RadStraight     32768
#define RadCCW          1
#define RadCW           -1



// Baud UBRRx values
#define Ubrr300         3839
#define Ubrr600         1919
#define Ubrr1200        959
#define Ubrr2400        479
#define Ubrr4800        239
#define Ubrr9600        119
#define Ubrr14400       79
#define Ubrr19200       59
#define Ubrr28800       39
#define Ubrr38400       29
#define Ubrr57600       19
#define Ubrr115200      9


// Command Module button and LEDs
#define UserButton        0x10
#define UserButtonPressed (!(PIND & UserButton))

#define LED1              0x20
#define LED1Off           (PORTD |= LED1)
#define LED1On            (PORTD &= ~LED1)

#define LED2              0x40
#define LED2Off           (PORTD |= LED2)
#define LED2On            (PORTD &= ~LED2)

#define LEDBoth           0x60
#define LEDBothOff        (PORTD |= LEDBoth)
#define LEDBothOn         (PORTD &= ~LEDBoth)


// Create Port
#define RobotPwrToggle      0x80
#define RobotPwrToggleHigh (PORTD |= 0x80)
#define RobotPwrToggleLow  (PORTD &= ~0x80)

#define RobotPowerSense    0x20
#define RobotIsOn          (PINB & RobotPowerSense)


// Command Module ePorts
#define LD2Over         0x04
#define LD0Over         0x02
#define LD1Over         0x01
