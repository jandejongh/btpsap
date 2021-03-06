/*
 * Copyright 2016 Jan de Jongh, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.etsi.btpsap;

import java.util.Arrays;

/** Java binding for common data types in a BTP (Basic Transport Protocol) SAP (Service Access Point entity).
 *
 * <p>
 * After ETSI EN 302 636-5-1, V1.2.1 (2014-08).
 * 
 * @author Jan de Jongh, TNO
 * 
 */
public interface BtpSapTypes
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BTP TYPE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  enum BtpType
  {
    BTP_A,
    BTP_B
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN TRANSPORT TYPE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  enum GnTransportType
  {
    /** Geonetworking Unicast.
     * 
     */
    GN_UC,
    /** Geonetworking Single-Hop Broadcast.
     * 
     */
    GN_SHB,
    /** Geonetworking Topologically-Scoped Broadcast.
     * 
     */
    GN_TSB,
    /** Geonetworking Geobroadcast.
     * 
     */
    GN_GBC,
    /** Geonetworking Anycast.
     * 
     */
    GN_AC
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN AREA SHAPE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  enum GnAreaShape
  {
    CIRCLE,
    RECTANGLE,
    ELLIPSE
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN AREA
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnArea
  {
    
    GnAreaShape getAreaShape ();
    
    double getLatitude ();
    
    double getLongitude ();
    
    int getDistanceA_m ();
    
    int getDistanceB_m ();
    
    int getAngle_degrees ();
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT GN AREA
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  static class DefaultGnArea
  implements GnArea
  {
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / CLONING / FACTORY
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public DefaultGnArea (final GnAreaShape gnAreaShape,
      final double latitude, final double longitude,
      final int distanceA_m, final int distanceB_m,
      final int angle_degrees)
    {
      if (gnAreaShape == null)
        throw new IllegalArgumentException ();
      this.gnAreaShape = gnAreaShape;
      this.latitude = latitude;
      this.longitude = longitude;
      this.distanceA_m = distanceA_m;
      this.distanceB_m = distanceB_m;
      this.angle_degrees = angle_degrees;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // AREA SHAPE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final GnAreaShape gnAreaShape;

    @Override
    public final GnAreaShape getAreaShape ()
    {
      return this.gnAreaShape;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // LATITUDE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final double latitude;

    @Override
    public final double getLatitude ()
    {
      return this.latitude;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // LONGITUDE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final double longitude;

    @Override
    public final double getLongitude ()
    {
      return this.longitude;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // DISTANCE A
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final int distanceA_m;

    @Override
    public final int getDistanceA_m ()
    {
      return this.distanceA_m;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // DISTANCE B
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final int distanceB_m;
    
    @Override
    public final int getDistanceB_m ()
    {
      return this.distanceB_m;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // ANGLE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final int angle_degrees;

    @Override
    public final int getAngle_degrees ()
    {
      return this.angle_degrees;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN ADDRESS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnAddress
  {
    
    long toLong ();
    
  }
  
  static class DefaultGnAddress
  implements GnAddress
  {
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / CLONING / FACTORY
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public DefaultGnAddress (final byte[] gnAddressBytes)
    {
      if (gnAddressBytes == null || gnAddressBytes.length != 8)
        throw new IllegalArgumentException ();
      this.gnAddressBytes = Arrays.copyOf (gnAddressBytes, 8);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // GN ADDRESS BYTES
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final byte [] gnAddressBytes;
    
    public final byte [] getGnAddressBytes ()
    {
      return this.gnAddressBytes;
    }

    @Override
    public final long toLong ()
    {
      long result = 0;
      for (int i = 0; i < 8; i++)
      {
        result <<= 8;
        result |= (this.gnAddressBytes[i] & 0xff);
      }
      return result;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN DESTINATION TYPE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  enum GnDestinationType
  {
    GN_DEST_UC,
    GN_DEST_GBC_AC
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN DESTINATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnDestination
  {
    
    GnDestinationType getGnDestinationType ();
    
    GnAddress getGnUnicastAddress () throws IllegalStateException;
    
    GnArea getGnArea () throws IllegalStateException;
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT GN DESTINATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  static class DefaultGnDestination
  implements GnDestination
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / CLONING / FACTORY
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private DefaultGnDestination (final GnDestinationType gnDestinationType, final GnAddress gnUnicastAddress, final GnArea gnArea)
    {
      if (gnDestinationType == null)
        throw new IllegalArgumentException ();
      if (gnDestinationType == GnDestinationType.GN_DEST_UC && (gnUnicastAddress == null || gnArea != null))
        throw new IllegalArgumentException ();
      if (gnDestinationType == GnDestinationType.GN_DEST_GBC_AC && (gnUnicastAddress != null || gnArea == null))
        throw new IllegalArgumentException ();
      this.gnDestinationType = gnDestinationType;
      this.gnUnicastAddress = gnUnicastAddress;
      this.gnArea = gnArea;
    }

    public DefaultGnDestination (final GnAddress gnUnicastAddress)
    {
      this (GnDestinationType.GN_DEST_UC, gnUnicastAddress, null);
    }

    public DefaultGnDestination (final GnArea gnArea)
    {
      this (GnDestinationType.GN_DEST_GBC_AC, null, gnArea);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // GN DESTINATION TYPE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final GnDestinationType gnDestinationType;
    
    @Override
    public final GnDestinationType getGnDestinationType ()
    {
      return this.gnDestinationType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // GN UNICAST ADDRESS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final GnAddress gnUnicastAddress;
    
    @Override
    public final GnAddress getGnUnicastAddress () throws IllegalStateException
    {
      if (this.gnDestinationType != GnDestinationType.GN_DEST_UC)
        throw new IllegalStateException ();
      return this.gnUnicastAddress;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // GN AREA
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final GnArea gnArea;
    
    @Override
    public final GnArea getGnArea () throws IllegalStateException
    {
      if (this.gnDestinationType != GnDestinationType.GN_DEST_GBC_AC)
        throw new IllegalStateException ();
      return this.gnArea;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN COMMUNICATIONS PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  enum GnCommunicationsProfile
  {
    GN_COMPROF_ITSG5,
    GN_COMPROF_CELLULAR;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN SECURITY PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnSecurityProfile
  {
    
    byte[] getProfileBytes ();
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT GN SECURITY PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  static class DefaultGnSecurityProfile
  implements GnSecurityProfile
  {
    
    private final byte [] bytes;
    
    public DefaultGnSecurityProfile (final byte[] bytes, final int offset)
    {
      if (bytes == null || offset < 0 || offset > bytes.length - 12)
        throw new IllegalArgumentException ();
      this.bytes = Arrays.copyOfRange (bytes, offset, offset + 12);
    }
    
    public DefaultGnSecurityProfile (final byte [] bytes)
    {
      this (bytes, 0);
    }
    
    @Override
    public final byte[] getProfileBytes ()
    {
      return Arrays.copyOf (this.bytes, this.bytes.length);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN TRAFFIC CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnTrafficClass
  {
    
    byte toByte ();
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT GN TRAFFIC CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  static class DefaultGnTrafficClass
  implements GnTrafficClass
  {
    
    private final byte trafficClassByte;
    
    public final byte getTrafficClassByte ()
    {
      return this.trafficClassByte;
    }
    
    @Override
    public final byte toByte ()
    {
      return this.trafficClassByte;
    }
    
    public DefaultGnTrafficClass (final byte trafficClassByte)
    {
      this.trafficClassByte = trafficClassByte;
//      XXX AUGMENT (Default)TrafficClass as shown below...
//      final int tcReservedBit = (((int) trafficClassByte) & 0x80) >> 7;
//      final int tcRelevanceBits = (((int) trafficClassByte) & 0x70) >> 6;
//      final int tcReliabilityBits = (((int) trafficClassByte) & 0x0c) >> 2;
//      final int tcLatencyBits = ((int) trafficClassByte) & 0x03;
//      System.err.println ("TC = " + (((int) trafficClassByte) & 0xff) + ".");
//      System.err.println ("  Reserved    = " + tcReservedBit + ".");
//      System.err.println ("  Relevance   = " + tcRelevanceBits + ".");
//      System.err.println ("  Reliability = " + tcReliabilityBits + ".");
//      System.err.println ("  Latency     = " + tcLatencyBits + ".");
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN POSITION VECTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnPositionVector
  {
    
    double getLatitude ();
    
    double getLongitude ();
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT GN POSITION VECTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  static class DefaultGnPositionVector
  implements GnPositionVector
  {
    
    // XXX Aren't there more fields in an LPV (speed/heading/etc.)??
    public DefaultGnPositionVector (final double latitude, final double longitude)
    {
      // XXX Error checking / range conversion (cardinal range?).
      this.latitude = latitude;
      this.longitude = longitude;
    }
    
    private final double latitude;

    @Override
    public final double getLatitude ()
    {
      return this.latitude;
    }
    
    private final double longitude;

    @Override
    public final double getLongitude ()
    {
      return this.longitude;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN SECURITY REPORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnSecurityReport
  {
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN CERTIFICATE ID
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnCertificateId
  {
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN PERMISSIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  interface GnPermissions
  {
    
  }
  
}
