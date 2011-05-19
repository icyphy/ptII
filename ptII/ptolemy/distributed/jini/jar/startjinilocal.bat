@echo off

rem Batch script that starts jini services.
rem  
rem Author: Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
rem Version: 
rem 
rem @Copyright (c) 2005 The Regents of Aalborg University.
rem All rights reserved.
rem
rem Permission is hereby granted, without written agreement and without
rem license or royalty fees, to use, copy, modify, and distribute this
rem software and its documentation for any purpose, provided that the
rem above copyright notice and the following two paragraphs appear in all
rem copies of this software.
rem
rem IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
rem FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
rem ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
rem AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
rem SUCH DAMAGE.
rem
rem AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
rem INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
rem MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
rem PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
rem HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
rem ENHANCEMENTS, OR MODIFICATIONS.

cd %PTII%/ptolemy/distributed/jini/ 
java -Djava.security.policy=./config/jsk-all.policy -jar ./jar/start.jar ./config/start-transient-jeri-services-local.config
