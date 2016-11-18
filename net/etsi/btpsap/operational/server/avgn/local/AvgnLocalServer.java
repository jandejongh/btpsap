package net.etsi.btpsap.operational.server.avgn.local;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.etsi.btpsap.BtpSapTypes;
import net.etsi.btpsap.BtpSap_DataConf;
import net.etsi.btpsap.BtpSap_DataIndContainer;
import net.etsi.btpsap.BtpSap_DataReqContainer;
import net.etsi.btpsap.operational.AbstractBtpSapEntity;
import net.etsi.btpsap.operational.BtpSapClient;
import net.etsi.btpsap.operational.BtpSapServer;
import net.etsi.btpsap.operational.OperationalBtpSap;
import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.Area;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination;
import net.gcdc.geonetworking.GeonetData;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.TrafficClass;
import net.gcdc.geonetworking.UpperProtocolType;
import net.gcdc.geonetworking.gpsdclient.GpsdClient;

/**
 *
 */
public class AvgnLocalServer
extends AbstractBtpSapEntity
implements BtpSapServer
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOG = Logger.getLogger (AvgnLocalServer.class.getName ());

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static String constructName (final LinkLayer linkLayer, final PositionProvider positionProvider)
  {
    final String linkLayerString = "LL: " + ((linkLayer != null && (linkLayer instanceof LinkLayerUdpToEthernet))
      ? "u2p"
      : linkLayer);
    final String positionProviderString = "PP: " + ((positionProvider != null && (positionProvider instanceof GpsdClient))
      ? "gpsd://?"
      : positionProvider);
    return "AvgnLocalServer[" + linkLayerString + ", " + positionProviderString + "]";
  }
  
  public AvgnLocalServer (final StationConfig stationConfig, final LinkLayer linkLayer, final PositionProvider positionProvider)
  {
    super (constructName (linkLayer, positionProvider));
    this.stationConfig = ((stationConfig != null) ? stationConfig : new StationConfig ());
    this.linkLayer = linkLayer;
    this.positionProvider = positionProvider;
    this.btpSap = null;
    this.geonetStation = null;
    this.btpSocket = null;
    this.rxThread = null;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StationConfig
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final StationConfig stationConfig;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LinkLayer
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final LinkLayer linkLayer;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PositionProvider
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final PositionProvider positionProvider;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OperationalBtpSap
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private volatile OperationalBtpSap btpSap;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GeonetStation
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private volatile GeonetStation geonetStation;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSocket
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private volatile BtpSocket btpSocket;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AvgnLocalReceptionThread
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private volatile AvgnLocalReceptionThread rxThread;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSapServerProtocolHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  protected synchronized void stateCheck ()
  {
    if (this.btpSap == null && this.geonetStation == null && this.btpSocket == null && this.rxThread == null)
      return;
    if (this.btpSap != null && this.geonetStation != null && this.btpSocket != null && this.rxThread != null)
      return;
    throw new IllegalStateException ();
  }
  
  @Override
  public synchronized void startBtpSapServer (final OperationalBtpSap btpSap)
  {
    if (btpSap == null)
      throw new IllegalArgumentException ();
    stateCheck ();
    if (this.btpSap == null)
    {
      this.btpSap = btpSap;
      this.geonetStation = new GeonetStation (this.stationConfig, this.linkLayer, this.positionProvider);
      new Thread (this.geonetStation).start ();
      this.geonetStation.startBecon ();
      this.btpSocket = BtpSocket.on (this.geonetStation);
      this.rxThread = new AvgnLocalReceptionThread (this, this.btpSocket);
      this.rxThread.start ();
    }
  }

  @Override
  public synchronized void stopBtpSapServer ()
  {
    stateCheck ();
    if (this.btpSap != null)
    {
      this.rxThread.signalStop ();
      this.rxThread.interrupt ();
      this.rxThread = null;
      this.btpSap = null;
      this.btpSocket.close ();
      this.btpSocket = null;
      this.geonetStation = null;
    }
  }

  @Override
  public synchronized boolean isActiveBtpSapServer ()
  {
    return this.btpSap != null;
  }

  @Override
  public final BtpSap_DataConf doRequest (final BtpSapClient client, final BtpSap_DataReqContainer request)
  {
    LOG.log (Level.INFO, "Doing request from client {0}: {1}.", new Object[]{client, request});
    if (client == null /* XXX TODO || unregistered client */)
    {
      LOG.log (Level.WARNING, "Ignoring request from unknown/unregistered client {0}!", client);
      return null;
    }
    if (request == null)
    {
      LOG.log (Level.WARNING, "Ignoring empty request from client {0}!", client);
      return null;
    }
    if (this.btpSap == null)
    {
      LOG.log (Level.WARNING, "Ignoring request from client {0} on inactive server {1}!", new Object[]{client, this});
      return null;
    }
    // Gather required (and supported) fields from the request in order to construct a GeonetData object.
    final UpperProtocolType upperProtocolType;
    switch (request.getBtpType ())
    {
      case BTP_A:
        upperProtocolType = UpperProtocolType.BTP_A;
        break;
      case BTP_B:
        upperProtocolType = UpperProtocolType.BTP_B;
        break;
      default:
        LOG.log (Level.WARNING, "Ignoring request from client {0} with invalid BTP Type {1}!",
          new Object[]{client, request.getBtpType ()});      
        return null;
    }
    final Destination destination;
    switch (request.getGnTransportType ())
    {
      case GN_UC:
        if (request.getGnDestination () == null
          || request.getGnDestination ().getGnDestinationType () != BtpSapTypes.GnDestinationType.GN_DEST_UC
          || request.getGnDestination ().getGnUnicastAddress () == null)
        {
          LOG.log (Level.WARNING,
            "Ignoring request from client {0} with invalid or incompatible GnDestination {1} (should be unicast address)!",
            new Object[]{client, request.getGnDestination ()});      
          return null;
        }
        final Address address = new Address (request.getGnDestination ().getGnUnicastAddress ().toLong ());
        destination = Destination.geounicast (address);
        break;
      case GN_SHB:
        if (request.hasMaxLifetime ())
          destination = Destination.singleHop ().withMaxLifetimeSeconds (request.getMaxLifeTime_ms () / 1000.0);
        else
          destination = Destination.singleHop ();
        break;
      case GN_TSB:
        destination = Destination.toposcopedbroadcast ().withMaxHopLimit ((byte) request.getGnMaxHopLimit ());
        break;
      case GN_GBC:
      case GN_AC:
        if (request.getGnDestination () == null
          || request.getGnDestination ().getGnDestinationType () != BtpSapTypes.GnDestinationType.GN_DEST_GBC_AC
          || request.getGnDestination ().getGnArea () == null)
        {
          LOG.log (Level.WARNING,
            "Ignoring request from client {0} with invalid or incompatible GnDestination {1} (should be area-type address)!",
            new Object[]{client, request.getGnDestination ()});
          return null;
        }
        final double latitude = request.getGnDestination ().getGnArea ().getLatitude ();
        final double longitude = request.getGnDestination ().getGnArea ().getLongitude ();
        final Position center = new Position (latitude, longitude);
        final double distanceA = request.getGnDestination ().getGnArea ().getDistanceA_m ();
        final double distanceB = request.getGnDestination ().getGnArea ().getDistanceB_m ();
        final double angle = request.getGnDestination ().getGnArea ().getAngle_degrees ();
        final Area area;
        switch (request.getGnDestination ().getGnArea ().getAreaShape ())
        {
          case CIRCLE:
            area = Area.circle (center, distanceA);
            break;
          case ELLIPSE:
            area = Area.ellipse (center, distanceA, distanceB, angle);
            break;
          case RECTANGLE:
            area = Area.rectangle (center, distanceA, distanceB, angle);
            break;
          default:
            LOG.log (Level.WARNING,
              "Ignoring request from client {0} with invalid area shape in GnDestination {1}!",
              new Object[]{client, request.getGnDestination ().getGnArea ().getAreaShape ()});
            return null;
        }
        destination = (request.getGnTransportType () == BtpSapTypes.GnTransportType.GN_GBC
          ? Destination.geobroadcast (area)
          : Destination.geoanycast (area));
        break;
      default:
        LOG.log (Level.WARNING, "Ignoring request from client {0} with invalid GN Transport Type {1}!",
          new Object[]{client, request.getGnTransportType ()});      
        return null;
    }
    // XXX Optional is a Java-8 feature... Be careful not to draw us into Java-8, as it apparently does not run yet on Voyage...
    // Seems like Alex created his own Optional class in GeoNetworking!
    final Optional<TrafficClass> trafficClass;
    if (request.getGnTrafficClass () == null)
      trafficClass = Optional.empty ();
    else
      trafficClass = Optional.of (TrafficClass.fromByte (request.getGnTrafficClass ().toByte ()));
    // XXX Should we supply an LPV, or can we rely on the PositionProvider by setting it to null (empty)?
    final Optional<LongPositionVector> longPositionVector;
    longPositionVector = Optional.empty ();
    // XXX Code below could use some additional error-checking!
    final int length = request.getLength ();
    final int offset = request.getOffset ();
    final byte [] data = request.getData ();
    final byte[] payload = ((offset != 0 || length != data.length)
      ? Arrays.copyOfRange (data, offset, offset + length)
      : data);
    final byte [] btpPacketData = new byte[payload.length + 4];
    btpPacketData[0] = (byte) ((request.getBtpDestinationPort () >> 8) & 0xff);
    btpPacketData[1] = (byte) (request.getBtpDestinationPort () & 0xff);
    if (upperProtocolType == UpperProtocolType.BTP_A)
    {
      if (request.getBtpSrcPort () != null)
      {
        btpPacketData[2] = (byte) ((request.getBtpSrcPort () >> 8) & 0xff);
        btpPacketData[3] = (byte) (request.getBtpSrcPort () & 0xff);
      }
      else
      {
        btpPacketData[2] = (byte) 0;
        btpPacketData[3] = (byte) 0;
      }
    }
    else
    {
      if (request.getBtpDstPortInfo () != null)
      {
        btpPacketData[2] = (byte) ((request.getBtpDstPortInfo () >> 8) & 0xff);
        btpPacketData[3] = (byte) (request.getBtpDstPortInfo ()& 0xff);
      }
      else
      {
        btpPacketData[2] = (byte) 0;
        btpPacketData[3] = (byte) 0;
      }      
    }
    System.arraycopy (payload, 0, btpPacketData, 4, payload.length);
    final GeonetData geonetData = new GeonetData (upperProtocolType, destination, trafficClass, longPositionVector, btpPacketData);
    final BtpPacket btpPacket = BtpPacket.fromGeonetData (geonetData);
//    switch (request.getGnTransportType ())
//    {
//      case GN_UC:
//        // XXX LOG.
//        return null;
//      case GN_SHB:
//      {
//        // final BtpSapTypes.BtpType btpType = request.getBtpType ();
//        //if (btpType != BtpSapTypes.BtpType.BTP_B)
//        //{
//        //  btpPacket = null;
//        //  break;
//        //}
//        final int destinationPort = request.getBtpDestinationPort ();
//        final int gnMaxLifetime_ms = request.getMaxLifeTime_ms ();
//        if (gnMaxLifetime_ms == 0)
//          btpPacket = BtpPacket.singleHop (payload, (short) destinationPort);
//        else
//          btpPacket = BtpPacket.singleHop (payload, (short) destinationPort, ((double) gnMaxLifetime_ms) / 1000.0);
//        break;
//      }
//      case GN_TSB:
//        // XXX LOG.
//        return null;
//      case GN_GBC:
//      case GN_AC:
//      {
//        final int destinationPort = request.getBtpDestinationPort ();
//        btpPacket = BtpPacket.customDestination (payload, (short) destinationPort, destination);
//        break;
//      }
//      default:
//        btpPacket = null;
//        // XXX LOG.
//        return null;
//    }
    boolean deactivated = false;
    try
    {
      synchronized (this)
      {
        if (this.btpSocket != null)
          this.btpSocket.send (btpPacket);
        else
          deactivated = true;
      }
    }
    catch (IOException ioe)
    {
      LOG.log (Level.WARNING, "Caught IOException on BtpSocket {0}.", this.btpSocket);
    }
    if (deactivated)
    {
      LOG.log (Level.WARNING, "Ignoring request from client {0} on inactive server {1}!", new Object[]{client, this});
      return null;      
    }
    // XXX
    return null;
  }
  
  public final synchronized void doIndication (final BtpSap_DataIndContainer indication)
  {
    if (this.btpSap != null)
      this.btpSap.doIndicationFromServer (this, indication, null);
  }
  
}
