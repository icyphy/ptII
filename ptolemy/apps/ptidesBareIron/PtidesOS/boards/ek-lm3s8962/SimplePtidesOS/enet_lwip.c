//*****************************************************************************
//
// enet_lwip.c - lwIP Interface.
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

#include "../../../hw_memmap.h"
#include "../../../hw_types.h"
#include "../../../hw_ints.h"
#include "../../../src/ethernet.h"
#include "../../../src/timer.h"
#include "../../../src/interrupt.h"
#include "../../../utils/diag.h"
#include "../../../utils/ustdlib.h"
#include "../rit128x96x4.h"
#include "lwip/opt.h"
#include "lwip/api.h"
#include "lwip/tcpip.h"
#include "lwip/def.h"
#include "lwip/mem.h"
#include "lwip/pbuf.h"
#include "lwip/sys.h"
#include "lwip/stats.h"
#include "netif/etharp.h"
#include "ptpd.h"
#include "globals.h"

//*****************************************************************************
//
// External Application references.
//
//*****************************************************************************
extern err_t ethernetif_init(struct netif *netif);
extern tBoolean ethernetif_enqueue(struct netif *netif);
extern void ethernetif_dequeue(struct netif *netif);
extern void adjust_rx_timestamp(TimeInternal *tRxTime, unsigned long ulRxTime,
                                unsigned long ulNow);
extern void get_timestamp(TimeInternal *pTime);

//*****************************************************************************
//
// Counters/Timers for lwIP.
//
//*****************************************************************************
unsigned long g_ulTCPFastTimer = 0;
unsigned long g_ulTCPSlowTimer = 0;
unsigned long g_ulARPTimer = 0;
unsigned long g_ulDHCPCoarseTimer = 0;
unsigned long g_ulDHCPFineTimer = 0;
static struct netif g_sEMAC_if;
unsigned long g_ulDHCPTimeoutTimer = 0;

//*****************************************************************************
//
// Default TCP/IP Address Configuration.
// Static IP Configuration used if DHCP times out.
// Note:  This is in the Link Local address range (169.254.x.y).
//
//*****************************************************************************
//
// The Default IP address to be used.
//
#ifndef DEFAULT_IPADDR0
#define DEFAULT_IPADDR0 169
#endif

#ifndef DEFAULT_IPADDR1
#define DEFAULT_IPADDR1 254
#endif

#ifndef DEFAULT_IPADDR2
#define DEFAULT_IPADDR2 19
#endif

#ifndef DEFAULT_IPADDR3
#define DEFAULT_IPADDR3 63
#endif

//
// The Default Gateway address to be used.
//
#ifndef DEFAULT_GATEWAY_ADDR0
#define DEFAULT_GATEWAY_ADDR0 0
#endif

#ifndef DEFAULT_GATEWAY_ADDR1
#define DEFAULT_GATEWAY_ADDR1 0
#endif

#ifndef DEFAULT_GATEWAY_ADDR2
#define DEFAULT_GATEWAY_ADDR2 0
#endif

#ifndef DEFAULT_GATEWAY_ADDR3
#define DEFAULT_GATEWAY_ADDR3 0
#endif

//
// The Default Network mask to be used.
//
#ifndef DEFAULT_NET_MASK0
#define DEFAULT_NET_MASK0 255
#endif

#ifndef DEFAULT_NET_MASK1
#define DEFAULT_NET_MASK1 255
#endif

#ifndef DEFAULT_NET_MASK2
#define DEFAULT_NET_MASK2 0
#endif

#ifndef DEFAULT_NET_MASK3
#define DEFAULT_NET_MASK3 0
#endif

//*****************************************************************************
//
// Timeout for DHCP address request (in seconds).
//
//*****************************************************************************
#ifndef DHCP_EXPIRE_TIMER_SECS
#define DHCP_EXPIRE_TIMER_SECS      45
#endif

//*****************************************************************************
//
// Ethernet Interrupt handler.
//
//*****************************************************************************
void
EthernetIntHandler(void)
{
    unsigned long ulTemp;
    tBoolean bRetcode;

    //
    // Read and Clear the interrupt.
    //
    ulTemp = EthernetIntStatus(ETH_BASE, true);
    EthernetIntClear(ETH_BASE, ulTemp);

    //
    // Check if RX Interrupt Occurred.
    //
    if(ulTemp & ETH_INT_RX)
    {
        //
        // Read the next packet from the hardware FIFO, add a timestamp and
        // enqueue it for processing in task context later.
        //
        bRetcode = ethernetif_enqueue(&g_sEMAC_if);

        if(bRetcode)
        {
            //
            // The packet was enqueued successfully so tell our task that it
            // has work to do.
            //
            HWREGBITW(&g_ulFlags, FLAG_RXPKT) = 1;
        }
    }

    //
    // Check if the TX Interrupt Occurred.
    //
    if(ulTemp & ETH_INT_TX)
    {
        //
        // Indicate that a packet has been transmitted.
        //
        HWREGBITW(&g_ulFlags, FLAG_TXPKT) = 1;

        //
        // Disable Ethernet TX Packet Interrupt.
        //
        EthernetIntDisable(ETH_BASE, ETH_INT_TX);
    }
}

//*****************************************************************************
//
// Display an lwIP type IP Address.
//
//*****************************************************************************
static void
lwip_display_address(unsigned long ipaddr, unsigned long ulCol,
                     unsigned long ulRow)
{
    char pucBuf[16];
    unsigned char *pucTemp = (unsigned char *)&ipaddr;

    //
    // Convert the "long" IP Address into a string.
    //
    usprintf(pucBuf, "%d.%d.%d.%d", pucTemp[0], pucTemp[1], pucTemp[2],
             pucTemp[3]);

    //
    // Display the string.
    //
    RIT128x96x4StringDraw(pucBuf, ulCol, ulRow, 15);
}

//*****************************************************************************
//
// Should be called by the top-level application to perform the needed lwIP
// TCP/IP initialization
//
//*****************************************************************************
void
lwip_init(void)
{
    struct ip_addr xIpAddr, xNetMast, xGateway;

    //
    // Low-Level initialization of the lwIP stack modules.
    //
    stats_init();
    sys_init();
    mem_init();
    memp_init();
    pbuf_init();
    etharp_init();
    ip_init();
    udp_init();
    tcp_init();
    netif_init();

    //
    // Create, Configure and Add the Ethernet Controller Interface.
    //
    IP4_ADDR(&xIpAddr, 0, 0, 0, 0);
    IP4_ADDR(&xNetMast, 0, 0, 0, 0);
    IP4_ADDR(&xGateway, 0, 0, 0, 0);
    netif_add(&g_sEMAC_if, &xIpAddr, &xNetMast, &xGateway, NULL,
              ethernetif_init, ip_input);
    netif_set_default(&g_sEMAC_if);
    dhcp_start(&g_sEMAC_if);
    RIT128x96x4StringDraw("Waiting for DHCP", 0, 16, 15);
    RIT128x96x4StringDraw("<                   > ", 0, 24, 15);

    //
    // Bring the interface up.
    //
    netif_set_up(&g_sEMAC_if);
}

//*****************************************************************************
//
// Should be called by the top-level application every system tick, with the
// number of MS per system tick, to run lwIP timers, etc.
//
//*****************************************************************************
void
lwip_tick(unsigned long ulTickMS)
{
    static tBoolean bDisplayIP = false;
    static tBoolean bIPDisplayed = false;

    //
    // Increment the assorted timers.
    //
    g_ulTCPFastTimer += ulTickMS;
    g_ulTCPSlowTimer += ulTickMS;
    g_ulARPTimer += ulTickMS;
    g_ulDHCPCoarseTimer += ulTickMS;
    g_ulDHCPFineTimer += ulTickMS;

    //
    // Check and process any packets received and queued by the ethernet ISR.
    //
    ethernetif_dequeue(&g_sEMAC_if);

    //
    // Check ARP Timer.
    //
    if(g_ulARPTimer >= ARP_TMR_INTERVAL)
    {
        g_ulARPTimer = 0;
        etharp_tmr();
    }

    //
    // Check TCP Slow Timer.
    //
    if(g_ulTCPSlowTimer >= TCP_SLOW_INTERVAL)
    {
        g_ulTCPSlowTimer = 0;
        tcp_slowtmr();
    }

    //
    // Check TCP Fast Timer.
    //
    if(g_ulTCPFastTimer >= TCP_FAST_INTERVAL)
    {
        g_ulTCPFastTimer = 0;
        tcp_fasttmr();
    }

    //
    // If DHCP is enabled/active, run the timers.
    //
    if(g_sEMAC_if.dhcp != NULL)
    {
        //
        // Check DCHP Coarse Timer.
        //
        if(g_ulDHCPCoarseTimer >= (DHCP_COARSE_TIMER_SECS * 1000))
        {
            g_ulDHCPCoarseTimer = 0;
            dhcp_coarse_tmr();
        }

        //
        // Check DCHP Fine Timer.
        //
        if(g_ulDHCPFineTimer >= DHCP_FINE_TIMER_MSECS)
        {
            g_ulDHCPFineTimer = 0;
            dhcp_fine_tmr();
        }

        //
        // Check to see if DHCP is bound.
        //
        if(g_sEMAC_if.dhcp->state == DHCP_BOUND)
        {
            bDisplayIP = true;
        }

        //
        // Check to see if if the DHCP process has taken too long.
        //
        else if(g_ulDHCPTimeoutTimer > (DHCP_EXPIRE_TIMER_SECS * 1000))
        {
            struct ip_addr xIpAddr, xNetMask, xGateway;

            //
            // Disable the DHPC process.
            //
            dhcp_stop(&g_sEMAC_if);

            //
            // Program the default IP settings.
            //
            IP4_ADDR(&xIpAddr, DEFAULT_IPADDR0, DEFAULT_IPADDR1,
                     DEFAULT_IPADDR2, DEFAULT_IPADDR3);
            IP4_ADDR(&xNetMask, DEFAULT_NET_MASK0, DEFAULT_NET_MASK1,
                     DEFAULT_NET_MASK2, DEFAULT_NET_MASK3);
            IP4_ADDR(&xGateway, DEFAULT_GATEWAY_ADDR0, DEFAULT_GATEWAY_ADDR1,
                    DEFAULT_GATEWAY_ADDR2, DEFAULT_GATEWAY_ADDR3);
            netif_set_ipaddr(&g_sEMAC_if, &xIpAddr);
            netif_set_gw(&g_sEMAC_if, &xGateway);
            netif_set_netmask(&g_sEMAC_if, &xNetMask);

            //
            // Prompt a display of the IP settings.
            //
            bDisplayIP = true;
        }

        //
        // Display DHCP Status.
        //
        else
        {
            static int iColumn = 6;

            //
            // Increment the DHCP timeout timer.
            //
            g_ulDHCPTimeoutTimer += ulTickMS;

            //
            // Update status bar on the display.
            //
            if(iColumn < 12)
            {
                RIT128x96x4StringDraw("< ", 0, 24, 15);
                RIT128x96x4StringDraw("*",iColumn, 24, 7);
            }
            else
            {
                RIT128x96x4StringDraw(" *",iColumn - 6, 24, 7);
            }

            //
            // Move to the next column. Note that the x coordinate must be
            // a multiple of 2.
            //
            iColumn += 2;
            if(iColumn > 114)
            {
                iColumn = 6;
                RIT128x96x4StringDraw(" >", 114, 24, 15);
            }
        }
    }

    //
    // Check if DHCP has been bound.
    //
    if(bDisplayIP && !bIPDisplayed)
    {
        HWREGBITW(&g_ulFlags, FLAG_IPADDR) = 1;
        RIT128x96x4StringDraw("                       ", 0, 16, 15);
        RIT128x96x4StringDraw("                       ", 0, 24, 15);
        RIT128x96x4StringDraw("IP:   ", 0, 16, 15);
        RIT128x96x4StringDraw("MASK: ", 0, 24, 15);
        RIT128x96x4StringDraw("GW:   ", 0, 32, 15);
        lwip_display_address(g_sEMAC_if.ip_addr.addr, 36, 16);
        lwip_display_address(g_sEMAC_if.netmask.addr, 36, 24);
        lwip_display_address(g_sEMAC_if.gw.addr, 36, 32);
        bIPDisplayed = true;
    }
}
