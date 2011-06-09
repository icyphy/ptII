/*** declareBufferHeader($name) ***/
        static xQueueHandle $name;
/**/

/*** initBuffer($buffer, $capacity, $type) ***/
        $buffer = xQueueCreate($capacity, sizeof( $type ));
/**/

/*** initHWBlock ***/
        prvSetupHardware();
/**/

/*** sharedBlock ***/
        /* Status LED and Push Buttons pin definitions */
        #define LED             GPIO_PIN_0 /* PF0 */
        #define SELECT          GPIO_PIN_1 /* PF1 */
        #define UP              GPIO_PIN_0 /* PE0 */
        #define DOWN            GPIO_PIN_1 /* PE1 */
        #define LEFT            GPIO_PIN_2 /* PE2 */
        #define RIGHT           GPIO_PIN_3 /* PE3 */
        #define BUTTON         (UP | DOWN | LEFT | RIGHT)

        // Dynamic allocation is not allowed.
        #define malloc(X) NULL
        #define realloc(X, Y) NULL

        void prvSetupHardware( void )
        {
                /* If running on Rev A2 silicon, turn the LDO voltage up to 2.75V.  This is
                a workaround to allow the PLL to operate reliably. */
                if( DEVICE_IS_REVA2 )
                {
                        SysCtlLDOSet( SYSCTL_LDO_2_75V );
                }
                /* Set the clocking to run from the PLL at 50 MHz */
                //SysCtlClockSet( SYSCTL_SYSDIV_4 | SYSCTL_USE_PLL | SYSCTL_OSC_MAIN | SYSCTL_XTAL_8MHZ );
                SysCtlClockSet(SYSCTL_SYSDIV_1 | SYSCTL_USE_OSC | SYSCTL_OSC_MAIN |        SYSCTL_XTAL_8MHZ);

                /*         Enable Port F for Ethernet LEDs
                LED0        Bit 3   Output
                LED1        Bit 2   Output */
                // Enable the peripherals used by this example.
                SysCtlPeripheralEnable(SYSCTL_PERIPH_UART1);        //UART1
                SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOD);        //UART1 pins
                SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOF);        //Select button


                /* Enable peripherals */
                SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOE);
                GPIODirModeSet( GPIO_PORTF_BASE, (LED | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3), GPIO_DIR_MODE_HW );
                GPIOPadConfigSet( GPIO_PORTF_BASE, (LED | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 ), GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD );
                // Enable the GPIO pin to read the select button.
                GPIODirModeSet(GPIO_PORTF_BASE, GPIO_PIN_1, GPIO_DIR_MODE_IN);
                GPIOPadConfigSet(GPIO_PORTF_BASE, GPIO_PIN_1, GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD_WPU);

                // Set GPIO D2 and D3 as UART pins.
                GPIOPinTypeUART(GPIO_PORTD_BASE, GPIO_PIN_2 | GPIO_PIN_3);
                //GPIOPinTypeUART(GPIO_PORTD_BASE, GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3);


                // Configure the UART for 57,600, 8-N-1 operation.
                UARTConfigSetExpClk(UART1_BASE, 8000000, 57600,
                (UART_CONFIG_WLEN_8 | UART_CONFIG_STOP_ONE |
                UART_CONFIG_PAR_NONE));
                //Enable the UART interrupt.
                //UARTIntEnable(UART1_BASE, UART_INT_RX | UART_INT_RT);
                UARTEnable(UART1_BASE);
                /* Configure push buttons as inputs */
                GPIOPadConfigSet(GPIO_PORTE_BASE, BUTTON, GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD_WPU);
                GPIODirModeSet  (GPIO_PORTE_BASE, BUTTON, GPIO_DIR_MODE_IN);
        }

        void init( void ){}
        void vApplicationTickHook( void ){}
        void vApplicationStackOverflowHook( xTaskHandle *pxTask, signed portCHAR *pcTaskName )
        {
                ( void ) pxTask;
                ( void ) pcTaskName;

                for( ;; );
        }
/**/


