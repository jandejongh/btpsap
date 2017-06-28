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
package net.etsi.btpsap.operational.client.udp.tno;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.etsi.btpsap.BtpSapTypes;
import net.etsi.btpsap.BtpSap_DataIndContainer;
import net.etsi.btpsap.BtpSap_DataResp;
import net.etsi.btpsap.operational.AbstractBtpSapEntity;
import net.etsi.btpsap.operational.BtpSapClient;
import net.etsi.btpsap.operational.BtpSapServer;
import net.etsi.btpsap.operational.OperationalBtpSap;

/**
 *
 *
 */
public class UdpTnoClient
extends AbstractBtpSapEntity
implements BtpSapClient
{

  private static final Logger LOG = Logger.getLogger (UdpTnoClient.class.getName ());

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public UdpTnoClient (final OperationalBtpSap btpSap, final UdpTnoClientProtocolHandler handler, final Socket clientTcpSocket)
  {
    super ("UdpTnoClient[" + clientTcpSocket.getInetAddress () + ":" + clientTcpSocket.getPort () + "]");
    if (btpSap == null || handler == null || clientTcpSocket == null)
      throw new IllegalArgumentException ();
    this.btpSap = btpSap;
    this.handler = handler;
    this.udpTnoTcpClientServer = new UdpTnoTcpClientServer (this.btpSap, this, clientTcpSocket);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OperationalBtpSap
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final OperationalBtpSap btpSap;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UdpTnoClientProtocolHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final UdpTnoClientProtocolHandler handler;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UdpTnoTcpClientServer
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private UdpTnoTcpClientServer udpTnoTcpClientServer = null;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UdpTnoTcpClientServer Thread
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private Thread udpTnoTcpClientServerThread = null;

  @Override
  public synchronized void startBtpSapClient (OperationalBtpSap btpSap)
  {
    if (this.udpTnoTcpClientServerThread == null)
    {
      this.udpTnoTcpClientServerThread = new Thread (this.udpTnoTcpClientServer);
      this.udpTnoTcpClientServerThread.start ();
    }
  }

  @Override
  public synchronized void stopBtpSapClient ()
  {
    if (this.udpTnoTcpClientServerThread != null)
    {
      this.udpTnoTcpClientServer.shutdown ();
      this.udpTnoTcpClientServer = null;
    }
  }

  @Override
  public boolean isActiveBtpSapClient ()
  {
    return this.udpTnoTcpClientServerThread != null;
  }
  
  protected synchronized void disconnect ()
  {
    this.udpTnoTcpClientServerThread = null;
    this.btpSap.unregisterClient (this);
    // XXX Should remove all listeners for gc!!!...
  }

  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INDICATION SOCKET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private DatagramSocket indSocket = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INDICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final Map<String, InetSocketAddress> urlMapper = new HashMap<> ();
  
  private synchronized InetSocketAddress getInetSocketAddress (final String url)
  {
    if (url == null)
    {
      LOG.log (Level.WARNING, "Null URL?");
      return null;      
    }
    if (this.urlMapper.containsKey (url.trim ().toLowerCase ()))
      return this.urlMapper.get (url.trim ().toLowerCase ());
    // URL has to start with udp://.
    if (! url.trim ().toLowerCase ().startsWith ("udp://"))
    {
      LOG.log (Level.WARNING, "Illegal URL for indications: {0}; has to start with \'udp://\'.", url);
      this.urlMapper.put (url.trim ().toLowerCase (), null);
      return null;
    }
    final String addressPortString = url.trim ().substring (6);
    if (! addressPortString.contains (":"))
    {
      LOG.log (Level.WARNING, "Illegal URL for indications: udp://{0}; No \':\' address-port delimiter!", addressPortString);
      this.urlMapper.put (url.trim ().toLowerCase (), null);
      return null;
    }
    final String[] addressPortStringSplit = addressPortString.split (":");
    if (addressPortStringSplit.length != 2)
    {
      LOG.log (Level.WARNING, "Illegal url for indications: udp://{0}; Address-port part contains multiple \':\' delimiters!");
      this.urlMapper.put (url.trim ().toLowerCase (), null);
      return null;
    }
    final String addressString = addressPortStringSplit[0];
    final String portString = addressPortStringSplit[1];
    final int port;
    try
    {
      port = Integer.parseInt (portString);
    }
    catch (NumberFormatException nfe)
    {
      LOG.log (Level.WARNING, "Illegal port number {0}!", portString);
      this.urlMapper.put (url.trim ().toLowerCase (), null);
      return null;
    }
    if (port < 0 || port >= 65536)
    {
      LOG.log (Level.WARNING, "Port number out of range {0}!", port);
      this.urlMapper.put (url.trim ().toLowerCase (), null);
      return null;
    }
    try
    {
      final InetAddress address = InetAddress.getByName (addressString);
      final InetSocketAddress inetSocketAddress = new InetSocketAddress (address, port);
      this.urlMapper.put (url.trim ().toLowerCase (), inetSocketAddress);
      return inetSocketAddress;
    }
    catch (UnknownHostException uhe)
    {
      LOG.log (Level.WARNING, "Unknown host {0}!", addressString);
      this.urlMapper.put (url.trim ().toLowerCase (), null);
      return null;
    }
  }
  
  private final static byte MAGIC_1 = (byte) (Integer.parseInt ("3d", 16) & 0xff);
  private final static byte MAGIC_2 = (byte) (Integer.parseInt ("94", 16) & 0xff);
  
  private byte [] formatIndication (final BtpSap_DataIndContainer indication)
  {
    if (indication == null)
      return null;
    final int length = indication.getLength ();
    final int unpaddedSize = 80 + length;
    // XXX Clumsy way of doing this...
    int paddedSize = unpaddedSize;
    while (paddedSize % 4 != 0)
      paddedSize++;
    final int size = paddedSize;
    final byte[] pdu = new byte[size];
    pdu[0] = MAGIC_1;
    pdu[1] = MAGIC_2;
    pdu[2] = (byte) 1;
    pdu[3] = (byte) this.btpSap.getClientId (this); // XXX What if this fails??
    pdu[4] = (byte) 0; // Units...
    pdu[5] = (byte) 0;
    pdu[6] = (byte) 0; // XXX Btp Flags...
    pdu[7] = (byte) 0; // XXX Gn SubType
    final Integer btpSrcPort = indication.getBtpSrcPort ();
    if (btpSrcPort != null)
    {
      pdu[8] = (byte) ((btpSrcPort & 0xff00) >> 8);
      pdu[9] = (byte) (btpSrcPort & 0x00ff);
    }
    else
    {
      pdu[8] = (byte) 0;
      pdu[9] = (byte) 0;      
    }
    pdu[10] = (byte) 0; // XXX Rem Lifetime...
    if (indication.getGnTrafficClass () != null && (indication.getGnTrafficClass () instanceof BtpSapTypes.DefaultGnTrafficClass))
      pdu[11] = ((BtpSapTypes.DefaultGnTrafficClass) indication.getGnTrafficClass ()).getTrafficClassByte ();
    else
      pdu[11] = (byte) 0; // We do not know...
    final int btpDstPort = indication.getBtpDstPort ();
    pdu[12] = (byte) ((btpDstPort & 0xff00) >> 8);
    pdu[13] = (byte) (btpDstPort & 0x00ff);
    final Integer btpDstPortInfo = indication.getBtpDstPortInfo ();
    if (btpDstPortInfo != null)
    {
      pdu[14] = (byte) ((btpDstPortInfo & 0xff00) >> 8);
      pdu[15] = (byte) (btpDstPortInfo & 0x00ff);
    }
    else
    {
      pdu[14] = (byte) 0;
      pdu[15] = (byte) 0;      
    }
    // XXX Dst Latitude OR DstGnUc
    pdu[16] = (byte) 0;
    pdu[17] = (byte) 0;
    pdu[18] = (byte) 0;
    pdu[19] = (byte) 0;
    pdu[20] = (byte) 0;
    pdu[21] = (byte) 0;
    pdu[22] = (byte) 0;
    pdu[23] = (byte) 0;
    // XXX Dst Longitude
    pdu[24] = (byte) 0;
    pdu[25] = (byte) 0;
    pdu[26] = (byte) 0;
    pdu[27] = (byte) 0;
    pdu[28] = (byte) 0;
    pdu[29] = (byte) 0;
    pdu[30] = (byte) 0;
    pdu[31] = (byte) 0;
    // XXX DST Distance A
    pdu[32] = (byte) 0;
    pdu[33] = (byte) 0;
    // XXX DST Distance B
    pdu[34] = (byte) 0;
    pdu[35] = (byte) 0;
    // XXX DST Angle
    pdu[36] = (byte) 0;
    pdu[37] = (byte) 0;
    // RESERVED_1
    pdu[38] = (byte) 0;
    pdu[39] = (byte) 0;
    // XXX SRC_GN_ADDRESS
    pdu[40] = (byte) 0;
    pdu[41] = (byte) 0;
    pdu[42] = (byte) 0;
    pdu[43] = (byte) 0;
    pdu[44] = (byte) 0;
    pdu[45] = (byte) 0;
    pdu[46] = (byte) 0;
    pdu[47] = (byte) 0;
    final BtpSapTypes.GnPositionVector gnSrcPV = indication.getGnSrcPV ();
    // SRC_LATITUDE
    if (gnSrcPV != null)
    {
      final long latBits = Double.doubleToLongBits (indication.getGnSrcPV ().getLatitude ());
      pdu[48] = (byte) ((latBits >> 56) & 0xff);
      pdu[49] = (byte) ((latBits >> 48) & 0xff);
      pdu[50] = (byte) ((latBits >> 40) & 0xff);
      pdu[51] = (byte) ((latBits >> 32) & 0xff);
      pdu[52] = (byte) ((latBits >> 24) & 0xff);
      pdu[53] = (byte) ((latBits >> 16) & 0xff);
      pdu[54] = (byte) ((latBits >>  8) & 0xff);
      pdu[55] = (byte) ((latBits      ) & 0xff);      
    }
    else
    {
      pdu[48] = (byte) 0;
      pdu[49] = (byte) 0;
      pdu[50] = (byte) 0;
      pdu[51] = (byte) 0;
      pdu[52] = (byte) 0;
      pdu[53] = (byte) 0;
      pdu[54] = (byte) 0;
      pdu[55] = (byte) 0;
    }
    // SRC_LONGITUDE
    if (gnSrcPV != null)
    {
      final long lonBits = Double.doubleToLongBits (indication.getGnSrcPV ().getLongitude ());
      pdu[56] = (byte) ((lonBits >> 56) & 0xff);
      pdu[57] = (byte) ((lonBits >> 48) & 0xff);
      pdu[58] = (byte) ((lonBits >> 40) & 0xff);
      pdu[59] = (byte) ((lonBits >> 32) & 0xff);
      pdu[60] = (byte) ((lonBits >> 24) & 0xff);
      pdu[61] = (byte) ((lonBits >> 16) & 0xff);
      pdu[62] = (byte) ((lonBits >>  8) & 0xff);
      pdu[63] = (byte) ((lonBits      ) & 0xff);      
    }
    else
    {
      pdu[56] = (byte) 0;
      pdu[57] = (byte) 0;
      pdu[58] = (byte) 0;
      pdu[59] = (byte) 0;
      pdu[60] = (byte) 0;
      pdu[61] = (byte) 0;
      pdu[62] = (byte) 0;
      pdu[63] = (byte) 0;
    }
    // SECURITY REPORT LENGTH
    // [+ SECURITY REPORT]
    pdu[64] = (byte) 0;
    pdu[65] = (byte) 0;
    pdu[66] = (byte) 0;
    pdu[67] = (byte) 0;
    // CERTIFICATE ID LENGTH
    // [+ CERTIFICATE ID]
    pdu[68] = (byte) 0;
    pdu[69] = (byte) 0;
    pdu[70] = (byte) 0;
    pdu[71] = (byte) 0;
    // PERMISSIONS LENGTH
    // [+ PERMISSIONS]
    pdu[72] = (byte) 0;
    pdu[73] = (byte) 0;
    pdu[74] = (byte) 0;
    pdu[75] = (byte) 0;
    // PAYLOAD
    pdu[76] = (byte) ((length & 0xff000000) >>> 24);
    pdu[77] = (byte) ((length & 0x00ff0000) >>> 16);
    pdu[78] = (byte) ((length & 0x0000ff00) >>> 8);
    pdu[79] = (byte) (length & 0x000000ff);
    final byte[] data_src = indication.getData ();
    final int offset_src = indication.getOffset ();
    System.arraycopy (data_src, offset_src, pdu, 80, length);
    // 32-bit padding.
    for (int i = 80 + length; i < size; i++)
      pdu[i] = (byte) 0;
    LOG.log (Level.INFO, "Encoded received BTP packet for client!");
    return pdu;
  }
  
  @Override
  public final BtpSap_DataResp doIndication (final BtpSapServer server, final BtpSap_DataIndContainer indication)
  {
    if (server == null)
    {
      LOG.log (Level.SEVERE, "Null server (unit)!");
      return null;
    }
    if (indication == null)
    {
      LOG.log (Level.SEVERE, "Null indication!");
      return null;
    }
    final String indDestUrl = this.btpSap.getDb ().getIndicationRouting (this, server);
    if (indDestUrl == null)
    {
      LOG.log (Level.WARNING, "Client {0} drops indication!", this);
      return null;
    }
    // At this point, we have a URL to which to send the properly formatted indication.
    // First, get the peer socket address.
    final InetSocketAddress inetSocketAddress = getInetSocketAddress (indDestUrl);
    if (inetSocketAddress == null)
    {
      LOG.log (Level.WARNING, "Unable to get socket for {0}!", indDestUrl);
      return null;      
    }
    // Then, appropriately format the data.
    final byte [] formattedInd = formatIndication (indication);
    // Create the datagram socket if needed.
    synchronized (this)
    {
      try
      {
        if (this.indSocket == null)
          this.indSocket = new DatagramSocket ();    
      }
      catch (SocketException se)
      {
        LOG.log (Level.SEVERE, "Unable to create datagram socket for indications!");
        this.indSocket = null;
        return null;
      }
    }
    try
    {
      // Subsequently, construct the datagram (packet).
      final DatagramPacket packet = new DatagramPacket (formattedInd, 0, formattedInd.length, inetSocketAddress);
      // Finally, send the datagram.
      LOG.log (Level.INFO, "Sending received BTP Packet to client {0}!", inetSocketAddress);
      this.indSocket.send (packet);
    }
    catch (SocketException se)
    {
      LOG.log (Level.SEVERE, "Unable to create datagram packet for indication!");
      return null;
    }
    catch (IOException ioe)
    {
      LOG.log (Level.SEVERE, "IOException while sending datagram packet for indication!");
      return null;
    }
    // XXX For now...
    return null;
  }
    
}
