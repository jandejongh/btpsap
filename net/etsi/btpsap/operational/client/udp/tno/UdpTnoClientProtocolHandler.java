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
 */
package net.etsi.btpsap.operational.client.udp.tno;

import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.etsi.btpsap.operational.OperationalBtpSap;
import net.etsi.btpsap.operational.BtpSapClientProtocolHandler;
import net.etsi.btpsap.BtpSapTypes.GnCertificateId;
import net.etsi.btpsap.BtpSapTypes.GnDestination;
import net.etsi.btpsap.BtpSapTypes.GnPermissions;
import net.etsi.btpsap.BtpSapTypes.GnPositionVector;
import net.etsi.btpsap.BtpSapTypes.GnSecurityReport;
import net.etsi.btpsap.BtpSapTypes.GnTrafficClass;
import net.etsi.btpsap.BtpSap_DataReqContainer;
import net.etsi.btpsap.operational.AbstractBtpSapEntity;
import net.etsi.btpsap.operational.BtpSapClient;
import net.etsi.btpsap.operational.BtpSapServer;

/**
 *
 *
 */
public class UdpTnoClientProtocolHandler
extends AbstractBtpSapEntity
implements BtpSapClientProtocolHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOG = Logger.getLogger (UdpTnoClientProtocolHandler.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public UdpTnoClientProtocolHandler (final OperationalBtpSap btpSap, final int tcpAcceptPort, final int udpServerPort)
  {
    super ("BTP/UDP[TNO][tcp:" + tcpAcceptPort + ", udp:" + udpServerPort + "]");
    if (btpSap == null)
      throw new IllegalArgumentException ();
    this.btpSap = btpSap;
    this.tcpAcceptPort = tcpAcceptPort;
    this.udpServerPort = udpServerPort;
  }

  public UdpTnoClientProtocolHandler (final OperationalBtpSap btpSap)
  {
    this (btpSap, UdpTnoClientProtocolHandler.DEFAULT_TCP_ACCEPT_PORT, UdpTnoClientProtocolHandler.DEFAULT_UDP_SERVER_PORT);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OperationalBtpSap
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final OperationalBtpSap btpSap;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT TCP ACCEPT PORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public final static int DEFAULT_TCP_ACCEPT_PORT = 36095;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TCP ACCEPT PORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int tcpAcceptPort;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT UDP SERVER PORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public final static int DEFAULT_UDP_SERVER_PORT = 36095;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UDP SERVER PORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int udpServerPort;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TCP ACCEPT SERVER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private UdpTnoTcpAcceptServer tcpAcceptServer = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UDP SERVER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private UdpTnoUdpServer udpServer = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // HEX REPRESENTATION UTILITY METHODS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final static char[] hexArray = "0123456789abcdef".toCharArray ();

  public static String bytesToHex (byte[] bytes)
  {
    final char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++)
    {
      final int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String (hexChars);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSapClientProtocolHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public final void startBtpSapClientProtocolHandler (final OperationalBtpSap btpSap)
  {
    synchronized (this)
    {
      if (this.tcpAcceptServer == null)
      {
        this.tcpAcceptServer = new UdpTnoTcpAcceptServer (this.tcpAcceptPort, this);
        new Thread (this.tcpAcceptServer).start ();
      }
      if (this.udpServer == null)
      {
        this.udpServer = new UdpTnoUdpServer (this.udpServerPort, this);
        new Thread (this.udpServer).start ();
      }
    }
  }

  @Override
  public final void stopBtpSapClientProtocolHandler ()
  {
    synchronized (this)
    {
      if (this.tcpAcceptServer != null)
      {
        this.tcpAcceptServer.shutdown ();
        this.tcpAcceptServer = null;
      }
      if (this.udpServer != null)
      {
        this.udpServer.shutdown ();
        this.udpServer = null;
      }
    }
  }

  @Override
  public final synchronized boolean isActiveBtpSapClientProtocolHandler ()
  {
    return this.tcpAcceptServer != null;
  }

  protected final boolean newClient (final Socket clientTcpSocket)
  {
    final UdpTnoClient client = new UdpTnoClient (this.btpSap, this, clientTcpSocket);
    if (! this.btpSap.registerClient (client))
      return false;
    client.startBtpSapClient (this.btpSap);
    return true;
  }
  
  private Set<BtpSapServer> createUnitsSet (final int unitsHigh, final int unitsLow)
  {
    final List<BtpSapServer> allServers = new ArrayList<> (this.btpSap.getRegisteredServers ());
    final Set<BtpSapServer> unitsSelected = new LinkedHashSet<> ();
    if ((unitsLow & 0x01) != 0 && allServers.size () >= 1)
      unitsSelected.add (allServers.get (0));
    if ((unitsLow & 0x02) != 0 && allServers.size () >= 2)
      unitsSelected.add (allServers.get (1));
    if ((unitsLow & 0x04) != 0 && allServers.size () >= 3)
      unitsSelected.add (allServers.get (2));
    if ((unitsLow & 0x08) != 0 && allServers.size () >= 4)
      unitsSelected.add (allServers.get (3));
    if ((unitsLow & 0x10) != 0 && allServers.size () >= 5)
      unitsSelected.add (allServers.get (4));
    if ((unitsLow & 0x20) != 0 && allServers.size () >= 6)
      unitsSelected.add (allServers.get (5));
    if ((unitsLow & 0x40) != 0 && allServers.size () >= 7)
      unitsSelected.add (allServers.get (6));
    if ((unitsLow & 0x80) != 0 && allServers.size () >= 8)
      unitsSelected.add (allServers.get (7));
    if ((unitsHigh & 0x01) != 0 && allServers.size () >= 9)
      unitsSelected.add (allServers.get (8));
    if ((unitsHigh & 0x02) != 0 && allServers.size () >= 10)
      unitsSelected.add (allServers.get (9));
    if ((unitsHigh & 0x04) != 0 && allServers.size () >= 11)
      unitsSelected.add (allServers.get (10));
    if ((unitsHigh & 0x08) != 0 && allServers.size () >= 12)
      unitsSelected.add (allServers.get (11));
    if ((unitsHigh & 0x10) != 0 && allServers.size () >= 13)
      unitsSelected.add (allServers.get (12));
    if ((unitsHigh & 0x20) != 0 && allServers.size () >= 14)
      unitsSelected.add (allServers.get (13));
    if ((unitsHigh & 0x40) != 0 && allServers.size () >= 15)
      unitsSelected.add (allServers.get (14));
    if ((unitsHigh & 0x80) != 0 && allServers.size () >= 16)
      unitsSelected.add (allServers.get (15));
    return unitsSelected;
  }
  
  private final static byte MAGIC_1 = (byte) (Integer.parseInt ("3d", 16) & 0xff);
  private final static byte MAGIC_2 = (byte) (Integer.parseInt ("93", 16) & 0xff);
  
  protected final void udpPacket (final DatagramPacket udpPacket)
  {
    if (udpPacket == null || udpPacket.getData () == null)
    {
      LOG.log (Level.SEVERE, "Received null UDP packet, or one with null data buffer!");
      return;
    }
    final byte[] data = udpPacket.getData ();
    final int offset = udpPacket.getOffset ();
    if (udpPacket.getLength () < 60)
    {
      LOG.log (Level.WARNING, "Received UDP packet with invalid size!");
      return;
    }
    // Check magic: 0x3d93.
    if (data[offset] != MAGIC_1 || data[offset + 1] != MAGIC_2)
    {
      LOG.log (Level.WARNING, "Received UDP packet with magic mismatch (!= 0x3d93)!");
      return;
    }
    // Check version: only 0x01 supported.
    if (data[offset + 2] != 1)
    {
      LOG.log (Level.WARNING, "Received UDP packet with unsupported version number: {0}.", data[offset + 2]);
      return;
    }
    // Check client.
    final int clientId = data[offset + 3] & 0xff;
    final BtpSapClient client = this.btpSap.getDb ().getClient (clientId);
    if (client == null)
    {
      LOG.log (Level.WARNING, "Received UDP packet from unregistered client (number): {0}.", clientId);
      return;
    }
    LOG.log (Level.FINE, "Received data from client {0}.", clientId);
    this.btpSap.getDb ().getClient (clientId);
    // Extract set of units (servers) to which the request applies.
    final int unitsHigh = data[offset + 4] & 0xff;
    final int unitsLow  = data[offset + 5] & 0xff;
    final Set<BtpSapServer> units = createUnitsSet (unitsHigh, unitsLow);
    boolean error = false;
    final List<String> errorMessages = new ArrayList<> ();
    final byte btpFlags = data[offset + 6];
    final int communicationsProfileBits = (((int) btpFlags) & 0xff) >> 4;
    final GnCommunicationsProfile gnCommunicationsProfile;
    switch (communicationsProfileBits)
    {
      case 0:
        gnCommunicationsProfile = GnCommunicationsProfile.GN_COMPROF_ITSG5;
        break;
      case 1:
        gnCommunicationsProfile = GnCommunicationsProfile.GN_COMPROF_CELLULAR;
        break;
      default:
        gnCommunicationsProfile = null;
        error = true;
        errorMessages.add ("Unknown Communications Profile: " + communicationsProfileBits + ".");
        break;
    }
    final int btpTypeBits = (((int) btpFlags) & 0x0f);
    final BtpType btpType;
    switch (btpTypeBits)
    {
      case 0:
        btpType = BtpType.BTP_A;
        break;
      case 1:
        btpType = BtpType.BTP_B;
        break;
      default:
        btpType = null;
        error = true;
        errorMessages.add ("Unknown BTP Type: " + btpTypeBits + ".");
        break;
    }
    final byte gnTypeByte = data[offset + 7];
    final int gnTypeNibble = (((int) gnTypeByte) & 0xff) >> 4;
    final GnTransportType gnTransportType;
    switch (gnTypeNibble)
    {
      case 0:
        gnTransportType = GnTransportType.GN_UC;
        break;
      case 1:
        gnTransportType = GnTransportType.GN_SHB;
        break;
      case 2:
        gnTransportType = GnTransportType.GN_TSB;
        break;
      case 3:
        gnTransportType = GnTransportType.GN_GBC;
        break;
      case 4:
        gnTransportType = GnTransportType.GN_AC;
        break;
      default:
        gnTransportType = null;
        error = true;
        errorMessages.add ("Unknown GeoNetworking Transport Type: " + gnTypeNibble + ".");
        break;
    }
    final int sourcePort = ((data[offset + 8] & 0xff) << 8) + (data[offset + 9] & 0xff);
    final byte lifeTimeByte = data[offset + 10];
    final int lifeTimeMultiplier = (((int) lifeTimeByte) & 0xff) >> 2;
    final int lifeTimeBaseBits = ((int) lifeTimeByte) & 0x03;
    final int lifeTimeBase_ms;
    switch (lifeTimeBaseBits)
    {
      case 0:
        lifeTimeBase_ms = 50;
        break;
      case 1:
        lifeTimeBase_ms = 1000;
        break;
      case 2:
        lifeTimeBase_ms = 10000;
        break;
      case 3:
        lifeTimeBase_ms = 100000;
        break;
      default:
        lifeTimeBase_ms = 0;
        error = true;
        errorMessages.add ("Unknown Lifetime Base: " + lifeTimeBaseBits + ".");
        break;
    }
    final int lifetime_ms = lifeTimeMultiplier * lifeTimeBase_ms;
    final byte tcByte = data[offset + 11];
    final GnTrafficClass gnTrafficClass = new DefaultGnTrafficClass (tcByte);
    final int destinationPort = ((data[offset + 12] & 0xff) << 8) + (data[offset + 13] & 0xff);
    final int destinationPortInfo = ((data[offset + 14] & 0xff) << 8) + (data[offset + 15] & 0xff);
    final int hopLimit = data[offset + 16] & 0xff;
    final byte repIntervalByte = data[offset + 17];
    final int repIntervalMultiplier = (((int) repIntervalByte) & 0xff) >> 2;
    final int repIntervalBaseBits = ((int) repIntervalByte) & 0x03;
    final int repIntervalBase_ms;
    switch (repIntervalBaseBits)
    {
      case 0:
        repIntervalBase_ms = 50;
        break;
      case 1:
        repIntervalBase_ms = 1000;
        break;
      case 2:
        repIntervalBase_ms = 10000;
        break;
      case 3:
        repIntervalBase_ms = 100000;
        break;
      default:
        repIntervalBase_ms = 0;
        error = true;
        errorMessages.add ("Unknown Repitition Interval Base: " + repIntervalBaseBits + ".");
        break;
    }
    final int repInterval_ms = repIntervalMultiplier * repIntervalBase_ms;
    final byte repTimeByte = data[offset + 18];
    final int repTimeMultiplier = (((int) repTimeByte) & 0xff) >> 2;
    final int repTimeBaseBits = ((int) repTimeByte) & 0x03;
    final int repTimeBase_ms;
    switch (repTimeBaseBits)
    {
      case 0:
        repTimeBase_ms = 50;
        break;
      case 1:
        repTimeBase_ms = 1000;
        break;
      case 2:
        repTimeBase_ms = 10000;
        break;
      case 3:
        repTimeBase_ms = 100000;
        break;
      default:
        repTimeBase_ms = 0;
        error = true;
        errorMessages.add ("Unknown Repitition Time Base: " + repTimeBaseBits + ".");
        break;
    }
    final int repTime_ms = repTimeMultiplier * repTimeBase_ms;
    // final byte reserved1Byte = data[offset + 19];
    // System.err.println ("RESERVED_1: " + (((int) reserved1Byte) & 0xff) + ".");
    final GnDestination gnDestination;
    if (gnTransportType == null)
      gnDestination = null;
    else
      switch (gnTransportType)
      {
        case GN_UC:
          final byte[] gnAddressBytes = Arrays.copyOfRange (data, 20, 28);
          // XXX Error checking on gnAddressBytes??
          final GnAddress gnAddress = new DefaultGnAddress (gnAddressBytes);
          gnDestination = new DefaultGnDestination (gnAddress);
          break;
        case GN_SHB:
          gnDestination = null;
          break;
        case GN_TSB:
          gnDestination = null;
          break;
        case GN_GBC:
        case GN_AC:
          final int gnSubTypeNibble = (((int) gnTypeByte) & 0x0f);
          final GnAreaShape gnAreaShape;
          switch (gnSubTypeNibble)
          {
            case 0:
              gnAreaShape = GnAreaShape.CIRCLE;
              break;
            case 1:
              gnAreaShape = GnAreaShape.RECTANGLE;
              break;
            case 2:
              gnAreaShape = GnAreaShape.ELLIPSE;
              break;
            default:
              gnAreaShape = null;
              error = true;
              errorMessages.add ("Unknown GeoNetworking SubType: " + gnSubTypeNibble + ".");
              break;
          }
          final long latBits = ((long) (data[offset + 20] & 0xff) << 56)
                             + ((long) (data[offset + 21] & 0xff) << 48)
                             + ((long) (data[offset + 22] & 0xff) << 40)
                             + ((long) (data[offset + 23] & 0xff) << 32)
                             + ((long) (data[offset + 24] & 0xff) << 24)
                             + ((long) (data[offset + 25] & 0xff) << 16)
                             + ((long) (data[offset + 26] & 0xff) << 8)
                             + ((long) (data[offset + 27] & 0xff));
          final double latitude = Double.longBitsToDouble (latBits);
          final long lonBits = ((long) (data[offset + 28] & 0xff) << 56)
                             + ((long) (data[offset + 29] & 0xff) << 48)
                             + ((long) (data[offset + 30] & 0xff) << 40)
                             + ((long) (data[offset + 31] & 0xff) << 32)
                             + ((long) (data[offset + 32] & 0xff) << 24)
                             + ((long) (data[offset + 33] & 0xff) << 16)
                             + ((long) (data[offset + 34] & 0xff) << 8)
                             + ((long) (data[offset + 35] & 0xff));
          final double longitude = Double.longBitsToDouble (lonBits);
          final int dA = ((data[offset + 36] & 0xff) << 8) + (data[offset + 37] & 0xff);
          final int dB = ((data[offset + 38] & 0xff) << 8) + (data[offset + 39] & 0xff);
          final int angle = ((data[offset + 40] & 0xff) << 8) + (data[offset + 41] & 0xff);
          if (error)
            gnDestination = null;
          else
            gnDestination = new DefaultGnDestination (new DefaultGnArea (gnAreaShape, latitude, longitude, dA, dB, angle));
          break;
        default:
          gnDestination = null;
          break;
      }
    // final int reserved2Int = ((data[offset + 42] & 0xff) << 8) + (data[offset + 43] & 0xff);
    // System.err.println ("RESERVED_2: " + reserved2Int + ".");
    final GnSecurityProfile gnSecurityProfile = new DefaultGnSecurityProfile (data, 44);
    final long payloadLength = ((long) (data[offset + 56] & 0xff) << 24)
                             + ((long) (data[offset + 57] & 0xff) << 16)
                             + ((long) (data[offset + 58] & 0xff) << 8)
                             + ((long) (data[offset + 59] & 0xff));
    if (udpPacket.getLength () != 60 + payloadLength)
    {
      error = true;
      errorMessages.add ("UDP Packet Length and Payload Length MISMATCH: UDP packet lenght = " + udpPacket.getLength ()
        + ", payload length encoded in packet = " + payloadLength + " [SHOULD BE EXACTLY 60 LESS THAN UDP PACKET SIZE]!");
    }
    // System.err.println ("Payload:");
    // System.err.println (Arrays.toString (Arrays.copyOfRange (data, (int) 60, (int) (60 + payloadLength))));
    if (error)
    {
      LOG.log (Level.WARNING, "Error decoding BTP/UDP[TNO] packet from client {0}:", client);
      for (final String errorMessage : errorMessages)
        LOG.log (Level.WARNING, errorMessage);
    }
    else
    {
      final BtpSap_DataReqContainer reqContainer;
      try
      {
        reqContainer = new BtpSap_DataReqContainer
          (btpType, sourcePort, destinationPort, destinationPortInfo,
           gnTransportType, gnDestination, gnCommunicationsProfile, gnSecurityProfile,
           lifetime_ms, repInterval_ms, repTime_ms, hopLimit, gnTrafficClass,
           (int) payloadLength, 60, data);
      }
      catch (IllegalArgumentException iae)
      {
        LOG.log (Level.WARNING, "Error creating BTP Request Container: Illegal Argument (dropping request from client {0}): {1}.",
          new Object[] {client, iae.getMessage ()});
        return;
      }
      this.btpSap.doRequestFromClient (client, reqContainer, units);
    }
  }
  
  @Override
  public void BTPDataIndication
  (final Integer btpSrcPort,
    final int btpDstPort,
    final Integer btpDstPortInfo,
    final GnDestination gnDstAddress,
    final GnPositionVector gnSrcPV,
    final GnSecurityReport gnSecReport,
    final GnCertificateId gnCertId,
    final GnPermissions gnPermissions,
    final GnTrafficClass gnTrafficClass,
    final Integer gnRemLifetime_s,
    final int length,
    final byte[] data) throws IllegalArgumentException
  {
    throw new UnsupportedOperationException ();
  }
    
}
