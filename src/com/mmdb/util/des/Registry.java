package com.mmdb.util.des;

// ----------------------------------------------------------------------------
// $Id: Registry.java,v 1.5 2002/08/07 09:55:46 raif Exp $
//
// Copyright (C) 2001-2002, Free Software Foundation, Inc.
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License or (at your option) any
// later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
// more details.
//
// You should have received a copy of the GNU General Public License along with
// this program; see the file COPYING.  If not, write to the
//
//    Free Software Foundation Inc.,
//    59 Temple Place - Suite 330,
//    Boston, MA 02111-1307
//    USA
//
// As a special exception, if you link this library with other files to produce
// an executable, this library does not by itself cause the resulting
// executable to be covered by the GNU General Public License.  This exception
// does not however invalidate any other reasons why the executable file might
// be covered by the GNU General Public License.
// ----------------------------------------------------------------------------

/**
 * <p>A placeholder for <i>names</i> used throughout this library.</p>
 *
 * @version $Revision: 1.5 $
 */
public interface Registry {

   // Constants
   // -------------------------------------------------------------------------

   /** The name of our Provider. */
   String GNU_CRYPTO = "GNU-CRYPTO";

   // Names of properties to use in Maps when initialising primitives .........

   // Symmetric block cipher algorithms and synonyms...........................

   String ANUBIS_CIPHER =   "anubis";
   String KHAZAD_CIPHER =   "khazad";
   String RIJNDAEL_CIPHER = "rijndael";
   String SERPENT_CIPHER =  "serpent";
   String SQUARE_CIPHER =   "square";
   String TWOFISH_CIPHER =  "twofish";
   String NULL_CIPHER =     "null";

   /** AES is synonymous to Rijndael for 128-bit block size only. */
   String AES_CIPHER =      "aes";

   // Message digest algorithms and synonyms...................................

   String WHIRLPOOL_HASH = "whirlpool";
   String RIPEMD128_HASH = "ripemd128";
   String RIPEMD160_HASH = "ripemd160";
   String SHA160_HASH =    "sha-160";
   String MD5_HASH =       "md5";
   String MD4_HASH =       "md4";

   /** RIPEMD-128 is synonymous to RIPEMD128. */
   String RIPEMD_128_HASH = "ripemd-128";

   /** RIPEMD-160 is synonymous to RIPEMD160. */
   String RIPEMD_160_HASH = "ripemd-160";

   /** SHA-1 is synonymous to SHA-160. */
   String SHA_1_HASH =      "sha-1";

   /** SHA1 is synonymous to SHA-160. */
   String SHA1_HASH =      "sha1";

   /** SHA is synonymous to SHA-160. */
   String SHA_HASH =       "sha";

   // Symmetric block cipher modes of operations...............................

   /** Electronic CodeBook mode. */
   String ECB_MODE = "ecb";

   /** Counter (NIST) mode. */
   String CTR_MODE = "ctr";

   /** Integer Counter Mode (David McGrew). */
   String ICM_MODE = "icm";

   /** Output Feedback Mode (NIST). */
   String OFB_MODE = "ofb";

//   String CBC_MODE = "cbc";
//   String CFB_MODE = "cfb";

   // Padding scheme names and synonyms........................................

   /** PKCS#7 padding scheme. */
   String PKCS7_PAD = "pkcs7";

   /** Trailing Bit Complement padding scheme. */
   String TBC_PAD =   "tbc";

   // Pseudo-random number generators..........................................

   /** PRNG based on David McGrew's Integer Counter Mode. */
   String ICM_PRNG = "icm";

   /** PRNG based on a designated hash functiopn. */
   String MD_PRNG =  "md";

   /** PRNG based on UMAC's Key Derivation Function. */
   String UMAC_PRNG = "umac-kdf";

   // Asymmetric keypair generators............................................

   String DSS_KPG = "dss";
   String RSA_KPG = "rsa";

   /** DSA is synonymous to DSS. */
   String DSA_KPG = "dsa";

   // Signature-with-appendix schemes..........................................

   String DSS_SIG =     "dss";
   String RSA_PSS_SIG = "rsa-pss";

   /** DSA is synonymous to DSS. */
   String DSA_SIG =     "dsa";

   // Keyed-Hash Message Authentication Code ..................................

   /** Name prefix of every HMAC implementation. */
   String HMAC_NAME_PREFIX = "hmac-";

   // Other MAC algorithms ....................................................

   /** Message Authentication Code using Universal Hashing (Ted Krovetz). */
   String UHASH32 = "uhash32";
   String UMAC32 = "umac32";
   /** The Truncated Multi-Modular Hash Function -v1 (David McGrew). */
   String TMMH16 = "tmmh16";
//   String TMMH32 = "tmmh32";

   // Methods
   // -------------------------------------------------------------------------
}
