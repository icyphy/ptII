/***printBlock ($data)***/
    sprintf( $actorSymbol(cMessage), "%d [%uns]", $data, ulMaxJitter * mainNS_PER_CLOCK );
/**/

/***StringPrintBlock ($data)***/
        sprintf( $actorSymbol(cMessage), "%s [%uns]", $data, ulMaxJitter * mainNS_PER_CLOCK );
/**/


/***preinitBlock***/
        /* Dimensions the buffer into which the jitter time is written. */
        #define mainMAX_MSG_LEN                                                25
        /* The period of the system clock in nano seconds.  This is used to calculate
        the jitter time in nano seconds. */
        #define mainNS_PER_CLOCK                                        ( ( unsigned portLONG ) ( ( 1.0 / ( double ) configCPU_CLOCK_HZ ) * 1000000000.0 ) )
        #define mainCHARACTER_HEIGHT                                ( 9 )
        #define mainMAX_ROWS_128                                        ( mainCHARACTER_HEIGHT * 14 )
        #define mainMAX_ROWS_96                                                ( mainCHARACTER_HEIGHT * 10 )
        #define mainMAX_ROWS_64                                                ( mainCHARACTER_HEIGHT * 7 )
        #define mainFULL_SCALE                                                ( 15 )
        #define ulSSI_FREQUENCY                                                ( 3500000UL )

        unsigned portLONG $actorSymbol(ulY), $actorSymbol(ulMaxY);
    static portCHAR $actorSymbol(cMessage)[ mainMAX_MSG_LEN ];
    extern volatile unsigned portLONG ulMaxJitter;
    unsigned portBASE_TYPE uxUnusedStackOnEntry;
    const unsigned portCHAR *pucImage;
/**/


/***initBlock***/
    //xOLEDMessage xMessage;
    /* Functions to access the OLED.  The one used depends on the dev kit
    being used. */
    void ( *vOLEDInit )( unsigned portLONG ) = NULL;
    void ( *vOLEDStringDraw )( const portCHAR *, unsigned portLONG, unsigned portLONG, unsigned portCHAR ) = NULL;
    void ( *vOLEDImageDraw )( const unsigned portCHAR *, unsigned portLONG, unsigned portLONG, unsigned portLONG, unsigned portLONG ) = NULL;
    void ( *vOLEDClear )( void ) = NULL;
    /* Just for demo purposes. */
    uxUnusedStackOnEntry = uxTaskGetStackHighWaterMark( NULL );
    /* Map the OLED access functions to the driver functions that are appropriate
    for the 8962 evaluation kit being used. For other development kits, there are
    other sets of initialization constants. */
        vOLEDInit = RIT128x96x4Init;
        vOLEDStringDraw = RIT128x96x4StringDraw;
        vOLEDImageDraw = RIT128x96x4ImageDraw;
        vOLEDClear = RIT128x96x4Clear;
        $actorSymbol(ulMaxY) = mainMAX_ROWS_96;
        pucImage = pucBasicBitmap;

        $actorSymbol(ulY) = $actorSymbol(ulMaxY);
    /* Initialise the OLED and display a startup message. */
    vOLEDInit( ulSSI_FREQUENCY );
/**/

/***fireBlock***/
        /* Wait for a message to arrive that requires displaying. */
$get(input, 0)

        /* Write the message on the next available row. */
                $actorSymbol(ulY) += mainCHARACTER_HEIGHT;
        if( $actorSymbol(ulY) >= $actorSymbol(ulMaxY) ) {
                $actorSymbol(ulY) = mainCHARACTER_HEIGHT;
            vOLEDClear();
            vOLEDStringDraw( "OpenRTOS MonitorValue", 0, 0, mainFULL_SCALE );
        }
        $this.printBlock($ref(input#0))

        /* Display the message along with the maximum jitter time from the
        high priority time test. */

        vOLEDStringDraw( $actorSymbol(cMessage), 0, $actorSymbol(ulY), mainFULL_SCALE );

/**/


/***fireBlock($channel)***/
                /* Wait for a message to arrive that requires displaying. */
$get(input, $channel)

                /* Write the message on the next available row. */
                $actorSymbol(ulY) += mainCHARACTER_HEIGHT;
                if( $actorSymbol(ulY) >= $actorSymbol(ulMaxY) ) {
                        $actorSymbol(ulY) = mainCHARACTER_HEIGHT;
                        vOLEDClear();
                        vOLEDStringDraw( "OpenRTOS MonitorValue", 0, 0, mainFULL_SCALE );
                }
                $this.printBlock($ref(input#$channel))

                /* Display the message along with the maximum jitter time from the
                high priority time test. */

                vOLEDStringDraw( $actorSymbol(cMessage), 0, $actorSymbol(ulY), mainFULL_SCALE );

/**/


/***wrapupBlock***/
/**/


