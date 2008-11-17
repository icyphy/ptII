//*****************************************************************************
//
// The clock rate for the SysTick interrupt.  All events in the application
// occur at some fraction of this clock rate.
//
//*****************************************************************************
#define SYSTICKHZ               100
#define SYSTICKMS               (1000 / SYSTICKHZ)
#define SYSTICKUS               (1000000 / SYSTICKHZ)
#define SYSTICKNS               (1000000000 / SYSTICKHZ)

//*****************************************************************************
//
// Define the system clock rate here.  One of the following must be defined to
// choose the system clock rate.
//
//*****************************************************************************
//#define SYSTEM_CLOCK_8MHZ
//#define SYSTEM_CLOCK_20MHZ
//#define SYSTEM_CLOCK_25MHZ
#define SYSTEM_CLOCK_50MHZ

//*****************************************************************************
//
// Clock and PWM dividers used depend on which system clock rate is chosen.
//
//*****************************************************************************
#if defined(SYSTEM_CLOCK_8MHZ)
#define SYSDIV      SYSCTL_SYSDIV_1
#define PWMDIV      SYSCTL_PWMDIV_1
#define CLKUSE      SYSCTL_USE_OSC
#define TICKNS      125

#elif defined(SYSTEM_CLOCK_20MHZ)
#define SYSDIV      SYSCTL_SYSDIV_10
#define PWMDIV      SYSCTL_PWMDIV_2
#define CLKUSE      SYSCTL_USE_PLL
#define TICKNS      50

#elif defined(SYSTEM_CLOCK_25MHZ)
#define SYSDIV      SYSCTL_SYSDIV_8
#define PWMDIV      SYSCTL_PWMDIV_2
#define CLKUSE      SYSCTL_USE_PLL
#define TICKNS      40

#elif defined(SYSTEM_CLOCK_50MHZ)
#define SYSDIV      SYSCTL_SYSDIV_4
#define PWMDIV      SYSCTL_PWMDIV_2
#define CLKUSE      SYSCTL_USE_PLL
#define TICKNS      20

#else
#error "System clock speed is not defined properly!"

#endif


//*****************************************************************************
//
// Select button GPIO definitions. The GPIO defined here is assumed to be
// attached to a button which, when pressed during application initialization,
// signals that Ethernet packet timestamping hardware is not to be used.  If
// the button is not pressed, the hardware timestamp feature will be used if
// it is available on the target IC.
//
//*****************************************************************************
#define SEL_BTN_GPIO_PERIPHERAL SYSCTL_PERIPH_GPIOF
#define SEL_BTN_GPIO_BASE       GPIO_PORTF_BASE
#define SEL_BTN_GPIO_PIN        GPIO_PIN_1

//*****************************************************************************
//
// Pulse Per Second (PPS) Output Definitions
//
//*****************************************************************************
#define PPS_GPIO_PERIPHERAL     SYSCTL_PERIPH_GPIOB
#define PPS_GPIO_BASE           GPIO_PORTB_BASE
#define PPS_GPIO_PIN            GPIO_PIN_0

//*****************************************************************************
//
// The following group of labels define the priorities of each of the interrupt
// we use in this example.  SysTick must be high priority and capable of
// preempting other interrupts to minimize the effect of system loading on the
// timestamping mechanism.
//
// The application uses the default Priority Group setting of 0 which means
// that we have 8 possible preemptable interrupt levels available to us using
// the 3 bits of priority available on the Stellaris microcontrollers with
// values from 0xE0 (lowest priority) to 0x00 (highest priority).
//
//*****************************************************************************
#define SYSTICK_INT_PRIORITY  0x00



