//*****************************************************************************
//
// pin_map.h - Mapping of peripherals to pins for all parts.
//
// Copyright (c) 2007 Luminary Micro, Inc.  All rights reserved.
// 
// Software License Agreement
// 
// Luminary Micro, Inc. (LMI) is supplying this software for use solely and
// exclusively on LMI's microcontroller products.
// 
// The software is owned by LMI and/or its suppliers, and is protected under
// applicable copyright laws.  All rights are reserved.  You may not combine
// this software with "viral" open-source software in order to form a larger
// program.  Any use in violation of the foregoing restrictions may subject
// the user to criminal sanctions under applicable laws, as well as to civil
// liability for the breach of the terms and conditions of this license.
// 
// THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
// OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
// LMI SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
// CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
// 
// This is part of revision 1900 of the Stellaris Peripheral Driver Library.
//
//*****************************************************************************

#ifndef __PIN_MAP_H__
#define __PIN_MAP_H__

//*****************************************************************************
//
// LM3S101 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S101

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define 32KHZ_PORT              (GPIO_PORTB_BASE)
#define 32KHZ_PIN               (GPIO_PIN_1)

#endif // PART_LM3S101

//*****************************************************************************
//
// LM3S102 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S102

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTB_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define 32KHZ_PORT              (GPIO_PORTB_BASE)
#define 32KHZ_PIN               (GPIO_PIN_1)

#endif // PART_LM3S102

//*****************************************************************************
//
// LM3S300 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S300

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S300

//*****************************************************************************
//
// LM3S301 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S301

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S301

//*****************************************************************************
//
// LM3S308 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S308

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S308

//*****************************************************************************
//
// LM3S310 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S310

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S310

//*****************************************************************************
//
// LM3S315 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S315

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S315

//*****************************************************************************
//
// LM3S316 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S316

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S316

//*****************************************************************************
//
// LM3S317 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S317

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S317

//*****************************************************************************
//
// LM3S328 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S328

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S328

//*****************************************************************************
//
// LM3S600 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S600

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S600

//*****************************************************************************
//
// LM3S601 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S601

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PHA_PORT                (GPIO_PORTC_BASE)
#define PHA_PIN                 (GPIO_PIN_4)

#define PHB_PORT                (GPIO_PORTC_BASE)
#define PHB_PIN                 (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S601

//*****************************************************************************
//
// LM3S608 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S608

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S608

//*****************************************************************************
//
// LM3S610 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S610

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S610

//*****************************************************************************
//
// LM3S611 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S611

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S611

//*****************************************************************************
//
// LM3S612 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S612

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S612

//*****************************************************************************
//
// LM3S613 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S613

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S613

//*****************************************************************************
//
// LM3S615 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S615

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S615

//*****************************************************************************
//
// LM3S617 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S617

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S617

//*****************************************************************************
//
// LM3S618 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S618

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define IDX_PORT                (GPIO_PORTB_BASE)
#define IDX_PIN                 (GPIO_PIN_2)

#define PHA_PORT                (GPIO_PORTC_BASE)
#define PHA_PIN                 (GPIO_PIN_4)

#define PHB_PORT                (GPIO_PORTC_BASE)
#define PHB_PIN                 (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S618

//*****************************************************************************
//
// LM3S628 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S628

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S628

//*****************************************************************************
//
// LM3S800 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S800

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S800

//*****************************************************************************
//
// LM3S801 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S801

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PHA_PORT                (GPIO_PORTC_BASE)
#define PHA_PIN                 (GPIO_PIN_4)

#define PHB_PORT                (GPIO_PORTC_BASE)
#define PHB_PIN                 (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S801

//*****************************************************************************
//
// LM3S808 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S808

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S808

//*****************************************************************************
//
// LM3S811 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S811

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S811

//*****************************************************************************
//
// LM3S812 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S812

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S812

//*****************************************************************************
//
// LM3S815 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S815

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S815

//*****************************************************************************
//
// LM3S817 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S817

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S817

//*****************************************************************************
//
// LM3S818 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S818

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define IDX_PORT                (GPIO_PORTB_BASE)
#define IDX_PIN                 (GPIO_PIN_2)

#define PHA_PORT                (GPIO_PORTC_BASE)
#define PHA_PIN                 (GPIO_PIN_4)

#define PHB_PORT                (GPIO_PORTC_BASE)
#define PHB_PIN                 (GPIO_PIN_6)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S818

//*****************************************************************************
//
// LM3S828 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S828

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTC_BASE)
#define CCP1_PIN                (GPIO_PIN_5)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2CSCL_PORT             (GPIO_PORTB_BASE)
#define I2CSCL_PIN              (GPIO_PIN_2)

#define I2CSDA_PORT             (GPIO_PORTB_BASE)
#define I2CSDA_PIN              (GPIO_PIN_3)

#define SSICLK_PORT             (GPIO_PORTA_BASE)
#define SSICLK_PIN              (GPIO_PIN_2)

#define SSIFSS_PORT             (GPIO_PORTA_BASE)
#define SSIFSS_PIN              (GPIO_PIN_3)

#define SSIRX_PORT              (GPIO_PORTA_BASE)
#define SSIRX_PIN               (GPIO_PIN_4)

#define SSITX_PORT              (GPIO_PORTA_BASE)
#define SSITX_PIN               (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S828

//*****************************************************************************
//
// LM3S1110 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1110

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1110

//*****************************************************************************
//
// LM3S1133 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1133

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1133

//*****************************************************************************
//
// LM3S1138 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1138

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTG_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTF_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1138

//*****************************************************************************
//
// LM3S1150 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1150

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTD_BASE)
#define PHA0_PIN                (GPIO_PIN_1)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_6)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1150

//*****************************************************************************
//
// LM3S1162 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1162

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTF_BASE)
#define PWM4_PIN                (GPIO_PIN_2)

#define PWM5_PORT               (GPIO_PORTF_BASE)
#define PWM5_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1162

//*****************************************************************************
//
// LM3S1165 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1165

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTB_BASE)
#define CCP6_PIN                (GPIO_PIN_5)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTF_BASE)
#define PWM4_PIN                (GPIO_PIN_2)

#define PWM5_PORT               (GPIO_PORTF_BASE)
#define PWM5_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1165

//*****************************************************************************
//
// LM3S1332 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1332

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1332

//*****************************************************************************
//
// LM3S1435 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1435

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1435

//*****************************************************************************
//
// LM3S1439 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1439

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTF_BASE)
#define PWM4_PIN                (GPIO_PIN_2)

#define PWM5_PORT               (GPIO_PORTF_BASE)
#define PWM5_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1439

//*****************************************************************************
//
// LM3S1512 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1512

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTF_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTD_BASE)
#define CCP4_PIN                (GPIO_PIN_5)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define IDX0_PORT               (GPIO_PORTD_BASE)
#define IDX0_PIN                (GPIO_PIN_0)

#define PHA0_PORT               (GPIO_PORTD_BASE)
#define PHA0_PIN                (GPIO_PIN_1)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1512

//*****************************************************************************
//
// LM3S1538 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1538

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTB_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define IDX0_PORT               (GPIO_PORTD_BASE)
#define IDX0_PIN                (GPIO_PIN_0)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1538

//*****************************************************************************
//
// LM3S1601 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1601

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1601

//*****************************************************************************
//
// LM3S1608 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1608

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1608

//*****************************************************************************
//
// LM3S1620 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1620

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_6)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1620

//*****************************************************************************
//
// LM3S1635 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1635

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTF_BASE)
#define PWM4_PIN                (GPIO_PIN_2)

#define PWM5_PORT               (GPIO_PORTF_BASE)
#define PWM5_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1635

//*****************************************************************************
//
// LM3S1637 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1637

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1637

//*****************************************************************************
//
// LM3S1751 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1751

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1751

//*****************************************************************************
//
// LM3S1850 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1850

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTD_BASE)
#define PHA0_PIN                (GPIO_PIN_1)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1850

//*****************************************************************************
//
// LM3S1911 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1911

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1911

//*****************************************************************************
//
// LM3S1918 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1918

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1918

//*****************************************************************************
//
// LM3S1937 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1937

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S1937

//*****************************************************************************
//
// LM3S1958 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1958

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTB_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1958

//*****************************************************************************
//
// LM3S1960 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1960

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTF_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTF_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define IDX0_PORT               (GPIO_PORTD_BASE)
#define IDX0_PIN                (GPIO_PIN_0)

#define IDX1_PORT               (GPIO_PORTH_BASE)
#define IDX1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTD_BASE)
#define PHA0_PIN                (GPIO_PIN_1)

#define PHA1_PORT               (GPIO_PORTG_BASE)
#define PHA1_PIN                (GPIO_PIN_6)

#define PHB0_PORT               (GPIO_PORTH_BASE)
#define PHB0_PIN                (GPIO_PIN_3)

#define PHB1_PORT               (GPIO_PORTG_BASE)
#define PHB1_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_6)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1960

//*****************************************************************************
//
// LM3S1968 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S1968

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTG_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTH_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define IDX0_PORT               (GPIO_PORTD_BASE)
#define IDX0_PIN                (GPIO_PIN_0)

#define IDX1_PORT               (GPIO_PORTF_BASE)
#define IDX1_PIN                (GPIO_PIN_1)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHA1_PORT               (GPIO_PORTG_BASE)
#define PHA1_PIN                (GPIO_PIN_6)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PHB1_PORT               (GPIO_PORTG_BASE)
#define PHB1_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTG_BASE)
#define PWM0_PIN                (GPIO_PIN_2)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTF_BASE)
#define PWM4_PIN                (GPIO_PIN_2)

#define PWM5_PORT               (GPIO_PORTF_BASE)
#define PWM5_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S1968

//*****************************************************************************
//
// LM3S2110 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2110

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2110

//*****************************************************************************
//
// LM3S2139 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2139

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2139

//*****************************************************************************
//
// LM3S2410 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2410

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2410

//*****************************************************************************
//
// LM3S2412 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2412

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2412

//*****************************************************************************
//
// LM3S2432 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2432

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2432

//*****************************************************************************
//
// LM3S2533 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2533

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2533

//*****************************************************************************
//
// LM3S2601 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2601

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2601

//*****************************************************************************
//
// LM3S2608 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2608

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2608

//*****************************************************************************
//
// LM3S2620 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2620

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTE_BASE)
#define C2O_PIN                 (GPIO_PIN_7)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTH_BASE)
#define PHB0_PIN                (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTG_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTG_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTD_BASE)
#define PWM2_PIN                (GPIO_PIN_2)

#define PWM3_PORT               (GPIO_PORTD_BASE)
#define PWM3_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2620

//*****************************************************************************
//
// LM3S2637 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2637

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2637

//*****************************************************************************
//
// LM3S2651 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2651

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2651

//*****************************************************************************
//
// LM3S2730 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2730

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2730

//*****************************************************************************
//
// LM3S2739 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2739

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2739

//*****************************************************************************
//
// LM3S2911 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2911

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2911

//*****************************************************************************
//
// LM3S2918 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2918

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S2918

//*****************************************************************************
//
// LM3S2939 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2939

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTH_BASE)
#define PHB0_PIN                (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2939

//*****************************************************************************
//
// LM3S2948 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2948

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTG_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define CCP6_PORT               (GPIO_PORTH_BASE)
#define CCP6_PIN                (GPIO_PIN_0)

#define CCP7_PORT               (GPIO_PORTH_BASE)
#define CCP7_PIN                (GPIO_PIN_1)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2948

//*****************************************************************************
//
// LM3S2950 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2950

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTF_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTF_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTE_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTH_BASE)
#define PHB0_PIN                (GPIO_PIN_3)

#define PWM0_PORT               (GPIO_PORTG_BASE)
#define PWM0_PIN                (GPIO_PIN_2)

#define PWM1_PORT               (GPIO_PORTG_BASE)
#define PWM1_PIN                (GPIO_PIN_3)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_6)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2950

//*****************************************************************************
//
// LM3S2965 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S2965

#define C0O_PORT                (GPIO_PORTF_BASE)
#define C0O_PIN                 (GPIO_PIN_4)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTF_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTD_BASE)
#define CCP4_PIN                (GPIO_PIN_5)

#define CCP5_PORT               (GPIO_PORTG_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define IDX1_PORT               (GPIO_PORTH_BASE)
#define IDX1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHA1_PORT               (GPIO_PORTG_BASE)
#define PHA1_PIN                (GPIO_PIN_6)

#define PHB0_PORT               (GPIO_PORTH_BASE)
#define PHB0_PIN                (GPIO_PIN_3)

#define PHB1_PORT               (GPIO_PORTG_BASE)
#define PHB1_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTG_BASE)
#define PWM0_PIN                (GPIO_PIN_2)

#define PWM1_PORT               (GPIO_PORTG_BASE)
#define PWM1_PIN                (GPIO_PIN_3)

#define PWM2_PORT               (GPIO_PORTH_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTH_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTF_BASE)
#define PWM4_PIN                (GPIO_PIN_2)

#define PWM5_PORT               (GPIO_PORTF_BASE)
#define PWM5_PIN                (GPIO_PIN_3)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S2965

//*****************************************************************************
//
// LM3S6100 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6100

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6100

//*****************************************************************************
//
// LM3S6110 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6110

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6110

//*****************************************************************************
//
// LM3S6420 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6420

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6420

//*****************************************************************************
//
// LM3S6422 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6422

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6422

//*****************************************************************************
//
// LM3S6432 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6432

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6432

//*****************************************************************************
//
// LM3S6537 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6537

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6537

//*****************************************************************************
//
// LM3S6610 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6610

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTE_BASE)
#define C2O_PIN                 (GPIO_PIN_7)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_0)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTD_BASE)
#define PHA0_PIN                (GPIO_PIN_1)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6610

//*****************************************************************************
//
// LM3S6611 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6611

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6611

//*****************************************************************************
//
// LM3S6618 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6618

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTG_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_0)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6618

//*****************************************************************************
//
// LM3S6633 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6633

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6633

//*****************************************************************************
//
// LM3S6637 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6637

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6637

//*****************************************************************************
//
// LM3S6730 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6730

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6730

//*****************************************************************************
//
// LM3S6753 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6753

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define IDX0_PORT               (GPIO_PORTD_BASE)
#define IDX0_PIN                (GPIO_PIN_0)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6753

//*****************************************************************************
//
// LM3S6911 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6911

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTE_BASE)
#define C1O_PIN                 (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6911

//*****************************************************************************
//
// LM3S6918 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6918

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTC_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTG_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_0)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S6918

//*****************************************************************************
//
// LM3S6938 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6938

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_6)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_0)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6938

//*****************************************************************************
//
// LM3S6950 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6950

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_6)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTD_BASE)
#define PHA0_PIN                (GPIO_PIN_1)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_6)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_7)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6950

//*****************************************************************************
//
// LM3S6952 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6952

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_6)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_0)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTF_BASE)
#define PHB0_PIN                (GPIO_PIN_0)

#define PWM0_PORT               (GPIO_PORTD_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6952

//*****************************************************************************
//
// LM3S6965 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S6965

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP2_PORT               (GPIO_PORTD_BASE)
#define CCP2_PIN                (GPIO_PIN_5)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define IDX0_PORT               (GPIO_PORTD_BASE)
#define IDX0_PIN                (GPIO_PIN_0)

#define IDX1_PORT               (GPIO_PORTF_BASE)
#define IDX1_PIN                (GPIO_PIN_1)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHA1_PORT               (GPIO_PORTE_BASE)
#define PHA1_PIN                (GPIO_PIN_3)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_7)

#define PHB1_PORT               (GPIO_PORTE_BASE)
#define PHB1_PIN                (GPIO_PIN_2)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTD_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S6965

//*****************************************************************************
//
// LM3S8530 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8530

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CAN2RX_PORT             (GPIO_PORTE_BASE)
#define CAN2RX_PIN              (GPIO_PIN_4)

#define CAN2TX_PORT             (GPIO_PORTE_BASE)
#define CAN2TX_PIN              (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S8530

//*****************************************************************************
//
// LM3S8538 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8538

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_6)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_0)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8538

//*****************************************************************************
//
// LM3S8630 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8630

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8630

//*****************************************************************************
//
// LM3S8730 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8730

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8730

//*****************************************************************************
//
// LM3S8733 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8733

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8733

//*****************************************************************************
//
// LM3S8738 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8738

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S8738

//*****************************************************************************
//
// LM3S8930 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8930

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S8930

//*****************************************************************************
//
// LM3S8933 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8933

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTD_BASE)
#define CCP3_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8933

//*****************************************************************************
//
// LM3S8938 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8938

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_6)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define C1O_PORT                (GPIO_PORTC_BASE)
#define C1O_PIN                 (GPIO_PIN_5)

#define C1_MINUS_PORT           (GPIO_PORTB_BASE)
#define C1_MINUS_PIN            (GPIO_PIN_5)

#define C1_PLUS_PORT            (GPIO_PORTC_BASE)
#define C1_PLUS_PIN             (GPIO_PIN_5)

#define C2O_PORT                (GPIO_PORTC_BASE)
#define C2O_PIN                 (GPIO_PIN_6)

#define C2_MINUS_PORT           (GPIO_PORTC_BASE)
#define C2_MINUS_PIN            (GPIO_PIN_7)

#define C2_PLUS_PORT            (GPIO_PORTC_BASE)
#define C2_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTE_BASE)
#define CCP1_PIN                (GPIO_PIN_3)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTE_BASE)
#define CCP3_PIN                (GPIO_PIN_0)

#define CCP4_PORT               (GPIO_PORTE_BASE)
#define CCP4_PIN                (GPIO_PIN_2)

#define CCP5_PORT               (GPIO_PORTC_BASE)
#define CCP5_PIN                (GPIO_PIN_4)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define I2C1SCL_PORT            (GPIO_PORTA_BASE)
#define I2C1SCL_PIN             (GPIO_PIN_6)

#define I2C1SDA_PORT            (GPIO_PORTA_BASE)
#define I2C1SDA_PIN             (GPIO_PIN_7)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#define U2RX_PORT               (GPIO_PORTG_BASE)
#define U2RX_PIN                (GPIO_PIN_0)

#define U2TX_PORT               (GPIO_PORTG_BASE)
#define U2TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S8938

//*****************************************************************************
//
// LM3S8962 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8962

#define C0O_PORT                (GPIO_PORTB_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTD_BASE)
#define CCP0_PIN                (GPIO_PIN_4)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define FAULT_PORT              (GPIO_PORTD_BASE)
#define FAULT_PIN               (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define IDX1_PORT               (GPIO_PORTF_BASE)
#define IDX1_PIN                (GPIO_PIN_1)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHA1_PORT               (GPIO_PORTE_BASE)
#define PHA1_PIN                (GPIO_PIN_3)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_6)

#define PHB1_PORT               (GPIO_PORTE_BASE)
#define PHB1_PIN                (GPIO_PIN_2)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTG_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTB_BASE)
#define PWM2_PIN                (GPIO_PIN_0)

#define PWM3_PORT               (GPIO_PORTB_BASE)
#define PWM3_PIN                (GPIO_PIN_1)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8962

//*****************************************************************************
//
// LM3S8970 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8970

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CAN1RX_PORT             (GPIO_PORTF_BASE)
#define CAN1RX_PIN              (GPIO_PIN_0)

#define CAN1TX_PORT             (GPIO_PORTF_BASE)
#define CAN1TX_PIN              (GPIO_PIN_1)

#define CAN2RX_PORT             (GPIO_PORTE_BASE)
#define CAN2RX_PIN              (GPIO_PIN_4)

#define CAN2TX_PORT             (GPIO_PORTE_BASE)
#define CAN2TX_PIN              (GPIO_PIN_5)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define I2C0SCL_PORT            (GPIO_PORTB_BASE)
#define I2C0SCL_PIN             (GPIO_PIN_2)

#define I2C0SDA_PORT            (GPIO_PORTB_BASE)
#define I2C0SDA_PIN             (GPIO_PIN_3)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SSI1CLK_PORT            (GPIO_PORTE_BASE)
#define SSI1CLK_PIN             (GPIO_PIN_0)

#define SSI1FSS_PORT            (GPIO_PORTE_BASE)
#define SSI1FSS_PIN             (GPIO_PIN_1)

#define SSI1RX_PORT             (GPIO_PORTE_BASE)
#define SSI1RX_PIN              (GPIO_PIN_2)

#define SSI1TX_PORT             (GPIO_PORTE_BASE)
#define SSI1TX_PIN              (GPIO_PIN_3)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#define U1RX_PORT               (GPIO_PORTD_BASE)
#define U1RX_PIN                (GPIO_PIN_2)

#define U1TX_PORT               (GPIO_PORTD_BASE)
#define U1TX_PIN                (GPIO_PIN_3)

#endif // PART_LM3S8970

//*****************************************************************************
//
// LM3S8971 Port/Pin Mapping Definitions
//
//*****************************************************************************
#ifdef PART_LM3S8971

#define C0O_PORT                (GPIO_PORTC_BASE)
#define C0O_PIN                 (GPIO_PIN_5)

#define C0_MINUS_PORT           (GPIO_PORTB_BASE)
#define C0_MINUS_PIN            (GPIO_PIN_4)

#define C0_PLUS_PORT            (GPIO_PORTB_BASE)
#define C0_PLUS_PIN             (GPIO_PIN_6)

#define CAN0RX_PORT             (GPIO_PORTD_BASE)
#define CAN0RX_PIN              (GPIO_PIN_0)

#define CAN0TX_PORT             (GPIO_PORTD_BASE)
#define CAN0TX_PIN              (GPIO_PIN_1)

#define CCP0_PORT               (GPIO_PORTB_BASE)
#define CCP0_PIN                (GPIO_PIN_0)

#define CCP1_PORT               (GPIO_PORTA_BASE)
#define CCP1_PIN                (GPIO_PIN_6)

#define CCP2_PORT               (GPIO_PORTB_BASE)
#define CCP2_PIN                (GPIO_PIN_1)

#define CCP3_PORT               (GPIO_PORTC_BASE)
#define CCP3_PIN                (GPIO_PIN_6)

#define CCP4_PORT               (GPIO_PORTA_BASE)
#define CCP4_PIN                (GPIO_PIN_7)

#define CCP5_PORT               (GPIO_PORTB_BASE)
#define CCP5_PIN                (GPIO_PIN_5)

#define FAULT_PORT              (GPIO_PORTB_BASE)
#define FAULT_PIN               (GPIO_PIN_3)

#define IDX0_PORT               (GPIO_PORTB_BASE)
#define IDX0_PIN                (GPIO_PIN_2)

#define LED0_PORT               (GPIO_PORTF_BASE)
#define LED0_PIN                (GPIO_PIN_3)

#define LED1_PORT               (GPIO_PORTF_BASE)
#define LED1_PIN                (GPIO_PIN_2)

#define PHA0_PORT               (GPIO_PORTC_BASE)
#define PHA0_PIN                (GPIO_PIN_4)

#define PHB0_PORT               (GPIO_PORTC_BASE)
#define PHB0_PIN                (GPIO_PIN_7)

#define PWM0_PORT               (GPIO_PORTF_BASE)
#define PWM0_PIN                (GPIO_PIN_0)

#define PWM1_PORT               (GPIO_PORTF_BASE)
#define PWM1_PIN                (GPIO_PIN_1)

#define PWM2_PORT               (GPIO_PORTD_BASE)
#define PWM2_PIN                (GPIO_PIN_2)

#define PWM3_PORT               (GPIO_PORTD_BASE)
#define PWM3_PIN                (GPIO_PIN_3)

#define PWM4_PORT               (GPIO_PORTE_BASE)
#define PWM4_PIN                (GPIO_PIN_0)

#define PWM5_PORT               (GPIO_PORTE_BASE)
#define PWM5_PIN                (GPIO_PIN_1)

#define SSI0CLK_PORT            (GPIO_PORTA_BASE)
#define SSI0CLK_PIN             (GPIO_PIN_2)

#define SSI0FSS_PORT            (GPIO_PORTA_BASE)
#define SSI0FSS_PIN             (GPIO_PIN_3)

#define SSI0RX_PORT             (GPIO_PORTA_BASE)
#define SSI0RX_PIN              (GPIO_PIN_4)

#define SSI0TX_PORT             (GPIO_PORTA_BASE)
#define SSI0TX_PIN              (GPIO_PIN_5)

#define SWCLK_PORT              (GPIO_PORTC_BASE)
#define SWCLK_PIN               (GPIO_PIN_0)

#define SWDIO_PORT              (GPIO_PORTC_BASE)
#define SWDIO_PIN               (GPIO_PIN_1)

#define SWO_PORT                (GPIO_PORTC_BASE)
#define SWO_PIN                 (GPIO_PIN_3)

#define TCK_PORT                (GPIO_PORTC_BASE)
#define TCK_PIN                 (GPIO_PIN_0)

#define TDI_PORT                (GPIO_PORTC_BASE)
#define TDI_PIN                 (GPIO_PIN_2)

#define TDO_PORT                (GPIO_PORTC_BASE)
#define TDO_PIN                 (GPIO_PIN_3)

#define TMS_PORT                (GPIO_PORTC_BASE)
#define TMS_PIN                 (GPIO_PIN_1)

#define TRST_PORT               (GPIO_PORTB_BASE)
#define TRST_PIN                (GPIO_PIN_7)

#define U0RX_PORT               (GPIO_PORTA_BASE)
#define U0RX_PIN                (GPIO_PIN_0)

#define U0TX_PORT               (GPIO_PORTA_BASE)
#define U0TX_PIN                (GPIO_PIN_1)

#endif // PART_LM3S8971

//*****************************************************************************
//
// Pin Mapping Functions
//
// This section describes the code that is responsible for handling the
// mapping of peripheral functions to their physical location on the pins of
// a device.
//
//*****************************************************************************

//*****************************************************************************
//
// Definitions to support mapping GPIO Ports and Pins to their function.
//
//*****************************************************************************

//*****************************************************************************
//
// This function configures the specified CAN pin to function as a CAN pin.
//
// \param ulName is one of the valid names for the CAN pins.
//
// This function takes one of the valid names for a CAN pin and configures
// the pin for its CAN functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b CAN0RX, \b CAN0TX,
// \b CAN1RX, \b CAN1TX, \b CAN2RX, or \b CAN2TX.
//
// \sa GPIOPinTypeCAN() in order to configure multiple CAN pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeCAN(ulName)      GPIOPinTypeCAN(ulName##_PORT, ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified comparator pin to function as a
// comparator pin.
//
// \param ulName is one of the valid names for the Comparator pins.
//
// This function takes one of the valid names for a comparator pin and
// configures the pin for its comparator functionality depending on the part
// that is defined.
//
// The valid names for the pins are as follows: \b C0_MINUS, \b C0_PLUS,
// \b C1_MINUS, \b C1_PLUS, \b C2_MINUS, or \b C2_PLUS.
//
// \sa GPIOPinTypeComparator() in order to configure multiple comparator pins
// at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeComparator(ulName)                                    \
                                GPIOPinTypeComparator(ulName##_PORT, \
                                                      ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified I2C pin to function as an I2C pin.
//
// \param ulName is one of the valid names for the I2C pins.
//
// This function takes one of the valid names for an I2C pin and configures
// the pin for its I2C functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b I2C0SCL, \b I2C0SDA,
// \b I2C1SCL, or \b I2C1SDA.
//
// \sa GPIOPinTypeI2C() in order to configure multiple I2C pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeI2C(ulName)      GPIOPinTypeI2C(ulName##_PORT, ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified PWM pin to function as a PWM pin.
//
// \param ulName is one of the valid names for the PWM pins.
//
// This function takes one of the valid names for a PWM pin and configures
// the pin for its PWM functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b PWM0, \b PWM1, \b PWM2,
// \b PWM3, \b PWM4, \b PWM5, or \b FAULT.
//
// \sa GPIOPinTypePWM() in order to configure multiple PWM pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypePWM(ulName)      GPIOPinTypePWM(ulName##_PORT, ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified QEI pin to function as a QEI pin.
//
// \param ulName is one of the valid names for the QEI pins.
//
// This function takes one of the valid names for a QEI pin and configures
// the pin for its QEI functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b PHA0, \b PHB0, \b IDX0,
// \b PHA1, \b PHB1, or \b IDX1.
//
// \sa GPIOPinTypeQEI() in order to configure multiple QEI pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeQEI(ulName)      GPIOPinTypeQEI(ulName##_PORT, ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified SSI pin to function as an SSI pin.
//
// \param ulName is one of the valid names for the SSI pins.
//
// This function takes one of the valid names for an SSI pin and configures
// the pin for its SSI functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b SSI0CLK, \b SSI0FSS,
// \b SSI0RX, \b SSI0TX, \b SSI1CLK, \b SSI1FSS, \b SSI1RX, or \b SSI1TX.
//
// \sa GPIOPinTypeSSI() in order to configure multiple SSI pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeSSI(ulName)      GPIOPinTypeSSI(ulName##_PORT, ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified Timer pin to function as a Timer
// pin.
//
// \param ulName is one of the valid names for the Timer pins.
//
// This function takes one of the valid names for a Timer pin and configures
// the pin for its Timer functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b CCP0, \b CCP1, \b CCP2,
// \b CCP3, \b CCP4, \b CCP5, \b CCP6, or \b CCP7.
//
// \sa GPIOPinTypeTimer() in order to configure multiple CCP pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeTimer(ulName)    GPIOPinTypeTimer(ulName##_PORT, ulName##_PIN)

//*****************************************************************************
//
// This function configures the specified UART pin to function as a UART pin.
//
// \param ulName is one of the valid names for the UART pins.
//
// This function takes one of the valid names for a UART pin and configures
// the pin for its UART functionality depending on the part that is defined.
//
// The valid names for the pins are as follows: \b U0RX, \b U0TX, \b U1RX,
// \b U1TX, \b U2RX, or \b U2TX.
//
// \sa GPIOPinTypeUART() in order to configure multiple UART pins at once.
//
// \return None.
//
//*****************************************************************************
#define PinTypeUART(ulName)     GPIOPinTypeUART(ulName##_PORT, ulNAME##_PIN)

#endif // __PIN_MAP_H__
