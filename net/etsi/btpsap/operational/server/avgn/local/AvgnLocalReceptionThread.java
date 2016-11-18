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
package net.etsi.btpsap.operational.server.avgn.local;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.etsi.btpsap.BtpSapTypes;
import net.etsi.btpsap.BtpSapTypes.DefaultGnPositionVector;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.etsi.btpsap.BtpSapTypes.GnCertificateId;
import net.etsi.btpsap.BtpSapTypes.GnDestination;
import net.etsi.btpsap.BtpSapTypes.GnPermissions;
import net.etsi.btpsap.BtpSapTypes.GnPositionVector;
import net.etsi.btpsap.BtpSapTypes.GnSecurityReport;
import net.etsi.btpsap.BtpSapTypes.GnTrafficClass;
import net.etsi.btpsap.BtpSap_DataIndContainer;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.TrafficClass;

/**
 *
 *
 */
public class AvgnLocalReceptionThread
extends Thread
{

  private static final Logger LOG = Logger.getLogger (AvgnLocalReceptionThread.class.getName ());

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public AvgnLocalReceptionThread (final AvgnLocalServer server, final BtpSocket btpSocket)
  {
    super ();
    if (server == null || btpSocket == null)
      throw new IllegalArgumentException ();
    this.server = server;
    this.btpSocket = btpSocket;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AvgnLocalServer
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final AvgnLocalServer server;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSocket
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final BtpSocket btpSocket;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STOP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private volatile boolean stop = false;

  void signalStop ()
  {
    this.stop = true;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // Thread.run
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void run ()
  {
    while (! (this.stop || interrupted ()))
    {
      try
      {
        final BtpPacket btpPacket = this.btpSocket.receive ();
        LOG.log (Level.INFO, "Received BTP packet!");
        if (btpPacket == null)
          return;
        // btpSrcPort
        final Integer btpSrcPort = ((btpPacket.sourcePort () == null || ! btpPacket.sourcePort ().isPresent ())
          ? null
          : (((int) btpPacket.sourcePort ().get ()) & 0xffff));
        // btpDstPort
        final int btpDstPort = (((int) btpPacket.destinationPort ()) & 0xffff);
        // btpDstPortInfo
        final Integer btpDstPortInfo =
          ((btpPacket.destinationPortInfo () == null || ! btpPacket.destinationPortInfo ().isPresent ())
          ? null
          : (((int) btpPacket.destinationPortInfo ().get ()) & 0xffff));
        // gnDstAddress
        // XXX Should be attainable to obtain the destination address!!
        final GnDestination gnDstAddress = null; // XXX We do not know!
        // gnSrvPV
        Optional<LongPositionVector> longPositionVector = btpPacket.senderPosition ();
        final GnPositionVector gnSrcPV =
          ((btpPacket.senderPosition () == null || ! btpPacket.senderPosition ().isPresent ())
          ? null
          : new DefaultGnPositionVector (btpPacket.senderPosition ().get ().position ().lattitudeDegrees (),
            btpPacket.senderPosition ().get ().position ().longitudeDegrees ()));
        // gnSecReport
        final GnSecurityReport gnSecReport = null; // XXX We do not know!
        // gnCertId
        final GnCertificateId gnCertId = null; // XXX We do not know!
        // gnPermissions
        final GnPermissions gnPermissions = null; // XXX We do not know!
        // gnTrafficClass
        final TrafficClass trafficClass =
          ((btpPacket.trafficClass () == null || ! btpPacket.trafficClass ().isPresent ())
          ? null
          : btpPacket.trafficClass ().get ());
        final GnTrafficClass gnTrafficClass =
          ((trafficClass != null) ? new BtpSapTypes.DefaultGnTrafficClass (trafficClass.asByte ()) : null);
        // gnRemLifetime_s
        final Integer gnRemLifetime_s = null; // XXX We do not know!
        // length, data
        final byte [] data = btpPacket.payload ();
        final int length = ((data == null) ? 0 : data.length);
        this.server.doIndication (new BtpSap_DataIndContainer
          (btpSrcPort,
            btpDstPort,
            btpDstPortInfo,
            gnDstAddress,
            gnSrcPV,
            gnSecReport,
            gnCertId,
            gnPermissions,
            gnTrafficClass,
            gnRemLifetime_s,
            length,
            0,
            data));
      }
      catch (InterruptedException ie)
      {
        return;
      }
    }
  }
  
  
}