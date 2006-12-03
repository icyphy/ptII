-- --------------------------------------------------------------------
--Copyright©2006 by the Institute of Electrical and Electronics Engineers, Inc.
-- Three Park Avenue
-- New York, NY 10016-5997, USA
-- All rights reserved.
-- 
-- This document is an unapproved draft of a proposed IEEE Standard. As such,
-- this document is subject to change. USE AT YOUR OWN RISK! Because this
--  is an unapproved draft, this document must not be utilized for any 
-- conformance/compliance purposes. Permission is hereby granted for IEEE 
-- Standards Committee participants to reproduce this document for purposes 
-- of IEEE standardization activities only. Prior to submitting this document 
-- to another standards development organization for standardization 
-- activities, permission must first be obtained from the Manager, Standards 
-- Licensing and Contracts, IEEE Standards Activities Department. Other 
-- entities seeking permission to reproduce this document, in whole or in 
-- part, must obtain permission from the Manager, Standards Licensing and 
-- Contracts, IEEE Standard Activities Department.
--
-- IEEE Standards Activities Department
-- Standards Licensing and Contracts
-- 445 Hoes Lane, P.O. Box 1331
-- Piscataway, NJ 08855-1331, USA
--
--   Title      :  Math utility package
--
--   Library   :  This package shall be compiled into a library 
--                symbolically named IEEE. 
--
--   Developers:  IEEE DASC P1076
--
--   Purpose   :  Definitions for ue in fixed point and floating point
--                arithmetic packages
--
--   Limitation:  
--
-- --------------------------------------------------------------------
-- Version    : $Revision$
-- Date       : $Date$
-- --------------------------------------------------------------------

package math_utility_pkg is

  -- Types used for generics of fixed_generic_pkg
  
  type fixed_round_style_type is (fixed_round, fixed_truncate);
  
  type fixed_overflow_style_type is (fixed_saturate, fixed_wrap);

  -- Type used for generics of float_generic_pkg

  -- These are the same as the C FE_TONEAREST, FE_UPWARD, FE_DOWNWARD,
  -- and FE_TOWARDZERO floating point rounding macros.

  type round_type is (round_nearest,    -- Default, nearest LSB '0'
                      round_inf,        -- Round toward positive infinity
                      round_neginf,     -- Round toward negative infinity
                      round_zero);      -- Round toward zero (truncate)

end package math_utility_pkg;
