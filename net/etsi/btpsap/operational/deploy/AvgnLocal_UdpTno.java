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
package net.etsi.btpsap.operational.deploy;

import java.awt.HeadlessException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import net.etsi.btpsap.operational.BtpSapServer;
import net.etsi.btpsap.operational.OperationalBtpSapServer;
import net.etsi.btpsap.operational.client.udp.tno.UdpTnoClientProtocolHandler;
import net.etsi.btpsap.operational.server.avgn.local.AvgnLocalProtocolHandler;
import net.etsi.btpsap.operational.server.avgn.local.AvgnLocalServer;
import net.etsi.btpsap.operational.ui.OperationalBtpSapServerFrame;
import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.gpsdclient.GpsdClient;
import org.threeten.bp.Instant;

/**
 *
 */
public class AvgnLocal_UdpTno
{

  private static final Logger LOG = Logger.getLogger (AvgnLocal_UdpTno.class.getName ());
  
  public static void main (final String[] args)
  {
    final OperationalBtpSapServer operationalBtpSapServer = new OperationalBtpSapServer (true);
    operationalBtpSapServer.registerServerProtocolHandler (new AvgnLocalProtocolHandler ());
    final UdpTnoClientProtocolHandler udpTnoClientProtocolHandler = new UdpTnoClientProtocolHandler (operationalBtpSapServer);
    operationalBtpSapServer.registerClientProtocolHandler (udpTnoClientProtocolHandler);
    GpsdClient cchGpsdClient = null;
    try
    {
      cchGpsdClient = new GpsdClient (new InetSocketAddress ("localhost", 2947));
    }
    catch (IOException ioe)
    {
      LOG.log (Level.WARNING, "Unable to open gpsd for CCH on localhost!");
      cchGpsdClient = null;
    }
    GpsdClient sch1GpsdClient = null;
    try
    {
      sch1GpsdClient = new GpsdClient (new InetSocketAddress ("localhost", 2947));
    }
    catch (IOException ioe)
    {
      LOG.log (Level.WARNING, "Unable to open gpsd for SCH1 on localhost!");
      sch1GpsdClient = null;
    }
    LinkLayer cchll = null;
    try
    {
      cchll = new LinkLayerUdpToEthernet (1236, new InetSocketAddress ("localhost", 1235), true);
    }
    catch (SocketException se)
    {
      LOG.log (Level.WARNING, "Unable to open LinkLayer [udp2eth] for CCH on localhost!");
      cchll = null;
    }
    LinkLayer sch1ll = null;
    try
    {
      sch1ll = new LinkLayerUdpToEthernet (1238, new InetSocketAddress ("localhost", 1237), true);
    }
    catch (SocketException se)
    {
      LOG.log (Level.WARNING, "Unable to open LinkLayer [udp2eth] for SCH1 on localhost!");
      sch1ll = null;
    }
    // final BtpSapServer cchServer = new AvgnLocalServer (new StationConfig (), cchll, cchGpsdClient);
    final BtpSapServer cchServer = new AvgnLocalServer (new StationConfig (), cchll, new PositionProvider ()
    {
      @Override
      public LongPositionVector getLatestPosition ()
      {
        final Optional<Address> emptyAddress = Optional.empty ();
        return new LongPositionVector (emptyAddress, Instant.now (), new Position (52.01, 4.35), true, 0, 0);
      }
    });
    cchServer.setName (cchServer.getName () + "[CCH]");
    operationalBtpSapServer.registerServer (cchServer);
    // final BtpSapServer sch1Server = new AvgnLocalServer (new StationConfig (), sch1ll, sch1GpsdClient);
    final BtpSapServer sch1Server = new AvgnLocalServer (new StationConfig (), sch1ll, new PositionProvider ()
    {
      @Override
      public LongPositionVector getLatestPosition ()
      {
        final Optional<Address> emptyAddress = Optional.empty ();
        return new LongPositionVector (emptyAddress, Instant.now (), new Position (52.07, 4.38), true, 0, 0);
      }
    });
    sch1Server.setName (sch1Server.getName () + "[SCH1]");
    operationalBtpSapServer.registerServer (sch1Server);
    final boolean headless = true;
    if (headless)
    {
      operationalBtpSapServer.startBtpSap ();
      cchServer.startBtpSapServer (operationalBtpSapServer);
      sch1Server.startBtpSapServer (operationalBtpSapServer);
      udpTnoClientProtocolHandler.startBtpSapClientProtocolHandler (operationalBtpSapServer);
//      new Thread (new Runnable ()
//      {
//        @Override
//        public final void run ()
//        {
//          while (! Thread.interrupted ())
//          {
//            try
//            {
//              operationalBtpSapServer.doFakeRandomRequest ();
//              Thread.sleep (1000l);
//            }
//            catch (InterruptedException ie)
//            {
//              return;
//            }
//          }
//        }
//      }).start ();
      try
      {
        new Object ().wait ();      
      }
      catch (Exception e)
      {
      }
    }
    else
      SwingUtilities.invokeLater (new Runnable ()
      {
        @Override
        public final void run ()
        {
          try
          {
            new OperationalBtpSapServerFrame (operationalBtpSapServer).setVisible (true);        
          }
          catch (HeadlessException he)
          {
            LOG.log (Level.WARNING, "No GUI; proceeding in headless mode!");
          }
        }          
      });
  }
}
