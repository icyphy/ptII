//*****************************************************************************
//
// lwipopts.h - Configuration file for lwIP
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
//
// NOTE:  This file has been derived from the lwIP/src/include/lwip/opt.h
// header file.  It has been reformated to Luminary coding standards, and
// most comments have been removed.
//
// For additional details, refer to the original "opt.h" file, and lwIP
// documentation.
//
//*****************************************************************************

#ifndef __LWIPOPTS_H__
#define __LWIPOPTS_H__
//*****************************************************************************
//
// ---------- System options ----------
//
//*****************************************************************************
#define ENABLE_ETHERNET_QUEUE_IF        1       // default is 0
#define SYS_LIGHTWEIGHT_PROT            1       // default is 0
#define NO_SYS                          1       // default is 0

//*****************************************************************************
//
// ---------- Memory options ----------
//
//*****************************************************************************
#define MEM_ALIGNMENT                   4       // default is 1
#define MEM_SIZE                        (4*1024)
                                                // default is 1600
//#define MEMP_SANITY_CHECK               0
//#define MEMP_NUM_PBUF                   16
//#define MEMP_NUM_RAW_PCB                4
//#define MEMP_NUM_UDP_PCB                4
#define MEMP_NUM_TCP_PCB                10
//#define MEMP_NUM_TCP_PCB_LISTEN         8
//#define MEMP_NUM_TCP_SEG                16
//#define MEMP_NUM_SYS_TIMEOUT            3
#define MEMP_NUM_NETBUF                 0       // default is 2
#define MEMP_NUM_NETCONN                0       // default is 4
#define MEMP_NUM_API_MSG                0       // default is 8
#define MEMP_NUM_TCPIP_MSG              0       // default is 8

//*****************************************************************************
//
// ---------- Pbuf options ----------
//
//*****************************************************************************
#define PBUF_POOL_SIZE                  32      // default is 16
#define PBUF_POOL_BUFSIZE               256     // default is 128
#define PBUF_LINK_HLEN                  16      // default is 14
#define ETH_PAD_SIZE                    2       // default is 0

//*****************************************************************************
//
// ---------- ARP options ----------
//
//*****************************************************************************
//#define ARP_TABLE_SIZE                  10
//#define ARP_QUEUEING                    1

//*****************************************************************************
//
// ---------- IP options ----------
//
//*****************************************************************************
//#define IP_FORWARD                      0
//#define IP_OPTIONS                      1
#define IP_REASSEMBLY                   0       // default is 1
#define IP_FRAG                         0       // default is 1

//*****************************************************************************
//
// ---------- ICMP options ----------
//
//*****************************************************************************
//#define ICMP_TTL                        255

//*****************************************************************************
//
// ---------- RAW options ----------
//
//*****************************************************************************
//#define LWIP_RAW                        1
//#define RAW_TTL                        255

//*****************************************************************************
//
// ---------- DHCP options ----------
//
//*****************************************************************************
#define LWIP_DHCP                       1       // default is 0
//#define DHCP_DOES_ARP_CHECK             1
#define DHCP_HOST_NAME                  "fury-dev"

//*****************************************************************************
//
// ---------- UDP options ----------
//
//*****************************************************************************
//#define LWIP_UDP                        1
//#define UDP_TTL                         255

//*****************************************************************************
//
// ---------- PTPD options ----------
//
//*****************************************************************************
#define LWIP_PTPD                       1       // default is 0

//*****************************************************************************
//
// ---------- TCP options ----------
//
//*****************************************************************************
//#define LWIP_TCP                        1
//#define TCP_TTL                         255
#define TCP_WND                         4096    // default is 2048
//#define TCP_MAXRTX                      12
//#define TCP_SYNMAXRTX                   6
//#define TCP_QUEUE_OOSEQ                 1
#define TCP_MSS                         1500    // default is 128
#define TCP_SND_BUF                     (6*TCP_MSS)
                                                // default is 256
//#define TCP_SND_QUEUELEN                (4 * TCP_SND_BUF/TCP_MSS)
//#define TCP_SNDLOWAT                    (TCP_SND_BUF/2)
//#define LWIP_HAVE_LOOPIF                0

//*****************************************************************************
//
// ---------- Task options ----------
//
//*****************************************************************************
//#define TCPIP_THREAD_PRIO               1
//#define SLIPIF_THREAD_PRIO              1
//#define PPP_THREAD_PRIO                 1
//#define DEFAULT_THREAD_PRIO             1

//*****************************************************************************
//
// ---------- Socket Options ----------
//
//*****************************************************************************
//#define LWIP_COMPAT_SOCKETS             1

//*****************************************************************************
//
// ---------- Statistics options ----------
//
//*****************************************************************************
//#define LWIP_STATS                      1
//#define LWIP_STATS_DISPLAY              0
//#define LINK_STATS                      1
//#define IP_STATS                        1
//#define IPFRAG_STATS                    1
//#define ICMP_STATS                      1
//#define UDP_STATS                       1
//#define TCP_STATS                       1
//#define MEM_STATS                       1
//#define MEMP_STATS                      1
//#define PBUF_STATS                      1
//#define SYS_STATS                       1
//#define RAW_STATS                       0

//*****************************************************************************
//
// ---------- PPP options ----------
//
//*****************************************************************************
//#define PPP_SUPPORT                     0
//#define PAP_SUPPORT                     0
//#define CHAP_SUPPORT                    0
//#define VJ_SUPPORT                      0
//#define MD5_SUPPORT                     0

//*****************************************************************************
//
// ---------- checksum options ----------
//
//*****************************************************************************
//#define CHECKSUM_GEN_IP                 1
//#define CHECKSUM_GEN_UDP                1
//#define CHECKSUM_GEN_TCP                1
//#define CHECKSUM_CHECK_IP               1
//#define CHECKSUM_CHECK_UDP              1
//#define CHECKSUM_CHECK_TCP              1

//*****************************************************************************
//
// ---------- Debugging options ----------
//
//*****************************************************************************
//#define DBG_TYPES_ON                    0
//#define ETHARP_DEBUG                    DBG_OFF
//#define NETIF_DEBUG                     DBG_OFF
//#define PBUF_DEBUG                      DBG_OFF
//#define API_LIB_DEBUG                   DBG_OFF
//#define API_MSG_DEBUG                   DBG_OFF
//#define SOCKETS_DEBUG                   DBG_OFF
//#define ICMP_DEBUG                      DBG_OFF
//#define INET_DEBUG                      DBG_OFF
//#define IP_DEBUG                        DBG_OFF
//#define IP_REASS_DEBUG                  DBG_OFF
//#define RAW_DEBUG                       DBG_OFF
//#define MEM_DEBUG                       DBG_OFF
//#define MEMP_DEBUG                      DBG_OFF
//#define SYS_DEBUG                       DBG_OFF
//#define TCP_DEBUG                       DBG_OFF
//#define TCP_INPUT_DEBUG                 DBG_OFF
//#define TCP_FR_DEBUG                    DBG_OFF
//#define TCP_RTO_DEBUG                   DBG_OFF
//#define TCP_REXMIT_DEBUG                DBG_OFF
//#define TCP_CWND_DEBUG                  DBG_OFF
//#define TCP_WND_DEBUG                   DBG_OFF
//#define TCP_OUTPUT_DEBUG                DBG_OFF
//#define TCP_RST_DEBUG                   DBG_OFF
//#define TCP_QLEN_DEBUG                  DBG_OFF
//#define UDP_DEBUG                       DBG_OFF
//#define TCPIP_DEBUG                     DBG_OFF
//#define PPP_DEBUG                       DBG_OFF
//#define SLIP_DEBUG                      DBG_OFF
//#define DHCP_DEBUG                      DBG_OFF
//#define DBG_MIN_LEVEL                   DBG_LEVEL_OFF

//*****************************************************************************
//
// ---------- Application options ----------
//
//*****************************************************************************
#define ENABLE_LMI_FS   1       // Enable the LMI File System in the
                                // sample web server application.

#endif /* __LWIPOPTS_H__ */

