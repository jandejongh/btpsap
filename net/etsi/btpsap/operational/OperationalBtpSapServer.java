package net.etsi.btpsap.operational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.etsi.btpsap.BtpSapTypes;
import net.etsi.btpsap.BtpSap_DataConf;
import net.etsi.btpsap.BtpSap_DataIndContainer;
import net.etsi.btpsap.BtpSap_DataReqContainer;
import net.etsi.btpsap.BtpSap_DataResp;

/**
 *
 */
public class OperationalBtpSapServer
extends AbstractBtpSapEntity
implements OperationalBtpSap, OperationalBtpSapDB.Listener
{

  private static final Logger LOGGER = Logger.getLogger (OperationalBtpSapServer.class.getName ());

  public OperationalBtpSapServer (final boolean useUI)
  {
    super ("BtpSapServer");
    this.useUI = useUI;
    this.db = new OperationalBtpSapDB ();
    this.db.registerListener (this);
  }
  
  private final boolean useUI;
  
  private final OperationalBtpSapDB db;
  
  @Override
  public final OperationalBtpSapDB getDb ()
  {
    return this.db;
  }
  
  private volatile boolean started = false;
  
  @Override
  public final synchronized boolean isStarted ()
  {
    return this.started;
  }

  @Override
  public final synchronized void startBtpSap ()
  {
    if (! this.started)
    {
      this.started = true;
    }
  }

  @Override
  public final synchronized void stopBtpSap ()
  {
    if (this.started)
    {
      this.started = false;
    }
  }
  
  @Override
  public final BtpSap_DataConf doRequestFromClient
  (final BtpSapClient client, final BtpSap_DataReqContainer request, final Set<BtpSapServer> servers)
  {
    if (client == null)
    {
      LOGGER.log (Level.WARNING, "Null client!");
      return null;
    }
    if (request == null)
    {
      LOGGER.log (Level.WARNING, "Null request from client {0}!", client);
      return null;
    }
    if (! getDb ().getClients ().contains (client))
    {
      LOGGER.log (Level.WARNING, "Unknown client {0}!", client);
      return null;
    }
    synchronized (this)
    {
      if (! this.started)
      {
        LOGGER.log (Level.WARNING, "BtpSap Server {0} not active!", this);
        return null;
      }
    }
    LOGGER.log (Level.INFO, "Doing request from client {0}.", client);
    final Set<BtpSapServer> serversToRequest = new LinkedHashSet<> ();
    if (servers != null && ! servers.isEmpty ())
    {
      serversToRequest.addAll (servers);
      serversToRequest.retainAll (getDb ().getServers ());
    }
    else
    {
      // serversToRequest.addAll (getDb ().getServers ());
      final BtpSapTypes.GnTrafficClass tcObject = request.getGnTrafficClass ();
      if (tcObject != null)
      {
        final Set<BtpSapServer> serversForTc = getDb ().getTcRequestRouting (client, tcObject.toByte () & 0x3f);
        if (serversForTc != null)
          serversToRequest.addAll (serversForTc);
      }
    }
    for (final BtpSapServer server : serversToRequest)
      server.doRequest (client, request);
    return null;
  }
  
  private final Random fakeRng = new Random ();
  
  private BtpSapClient randomClient ()
  {
    final List<BtpSapClient> clients = new ArrayList<> (getDb ().getClients ());
    if (clients.isEmpty ())
      return null;
    return clients.get (this.fakeRng.nextInt (clients.size ()));
  }
  
  private BtpSapServer randomServer ()
  {
    final List<BtpSapServer> servers = new ArrayList<> (getDb ().getServers ());
    if (servers.isEmpty ())
      return null;
    return servers.get (this.fakeRng.nextInt (servers.size ()));
  }
  
  private BtpSap_DataReqContainer randomRequest ()
  {
    return new BtpSap_DataReqContainer (
      BtpSapTypes.BtpType.BTP_B,                            // btpType
      500,                                                  // btpSrcPort
      792,                                                  // btpDstPort
      0,                                                    // btpDstPortInfo
      BtpSapTypes.GnTransportType.GN_SHB,                   // gnTransportType
      null,                                                 // gnDst
      BtpSapTypes.GnCommunicationsProfile.GN_COMPROF_ITSG5, // gnCommProfile
      null,                                                 // gnSecProfile
      5000,                                                 // gnMaxLifetime_ms
      null,                                                 // gnRepInterval_ms
      5000,                                                 // gnMaxRepTime_ms
      2,                                                    // gnMaxHopLimit
      new BtpSapTypes.DefaultGnTrafficClass ((byte) 45),    // gnTrafficClass
      1,                                                    // length
      0,                                                    // offset
      new byte[] { (byte) this.fakeRng.nextInt () }         // date
    );
  }
  
  public final BtpSap_DataConf doFakeRandomRequest ()
  {
    final BtpSapClient client = randomClient ();
    final BtpSapServer server = randomServer ();
    final BtpSap_DataReqContainer request = randomRequest ();
    return doRequestFromClient (client, request, Collections.singleton (server));
  }
  
  @Override
  public final BtpSap_DataResp doIndicationFromServer
  (final BtpSapServer server, BtpSap_DataIndContainer indication, Set<BtpSapClient> clients)
  {
    if (server == null || indication == null)
      return null;
    if (! getDb ().getServers ().contains (server))
      return null;
    synchronized (this)
    {
      if (! this.started)
        return null;
    }
    final Set<BtpSapClient> clientsToIndicate = new LinkedHashSet<> ();
    if (clients != null)
    {
      clientsToIndicate.addAll (clients);
      clientsToIndicate.retainAll (getDb ().getClients ());
    }
    else
      clientsToIndicate.addAll (getDb ().getClients ());
    for (final BtpSapClient client : clientsToIndicate)
      client.doIndication (server, indication);
    return null;    
  }

  @Override
  public void changed (final AbstractBtpSapEntity entity)
  {
    fireChanged ();
  }
  
  @Override
  public void registerServerProtocolHandler (final BtpSapServerProtocolHandler handler)
  {
    getDb ().addServerProtocolHandler (handler);
  }
  
  @Override
  public void unregisterServerProtocolHandler (final BtpSapServerProtocolHandler handler)
  {
    getDb ().removeServerProtocolHandler (handler);
  }
  
  @Override
  public Set<BtpSapServerProtocolHandler> getRegisteredServerProtocolHandlers ()
  {
    return getDb ().getServerProtocolHandlers ();
  }
  
  @Override
  public void registerClientProtocolHandler (final BtpSapClientProtocolHandler handler)
  {
    getDb ().addClientProtocolHandler (handler);
  }
  
  @Override
  public void unregisterClientProtocolHandler (final BtpSapClientProtocolHandler handler)
  {
    getDb ().removeClientProtocolHandler (handler);
  }
  
  @Override
  public Set<BtpSapClientProtocolHandler> getRegisteredClientProtocolHandlers ()
  {
    return getDb ().getClientProtocolHandlers ();
  }
 
  @Override
  public void registerServer (final BtpSapServer server)
  {
    getDb ().addServer (server);
  }
  
  @Override
  public void unregisterServer (final BtpSapServer server)
  {
    getDb ().removeServer (server);
  }
  
  @Override
  public Set<BtpSapServer> getRegisteredServers ()
  {
    return getDb ().getServers ();
  }
  
  @Override
  public boolean registerClient (final BtpSapClient client)
  {
    return getDb ().addClient (client);
  }
  
  @Override
  public void unregisterClient (final BtpSapClient client)
  {
    getDb ().removeClient (client);
  }
  
  @Override
  public Set<BtpSapClient> getRegisteredClients ()
  {
    return getDb ().getClients ();
  }
  
  @Override
  public int getClientId (final BtpSapClient client)
  {
    return getDb ().getClientId (client);
  }
  
}
