package net.etsi.btpsap.operational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class OperationalBtpSapDB
extends AbstractBtpSapEntity
implements BtpSapEntity.Listener
{

  private static final Logger LOG = Logger.getLogger (OperationalBtpSapDB.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public OperationalBtpSapDB ()
  {
    super ("OperationalBtpSapDB");
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-PROTOCOL HANDLERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<BtpSapServerProtocolHandler> serverProtocolHandlers = new LinkedHashSet<> ();
  
  public final synchronized void addServerProtocolHandler (final BtpSapServerProtocolHandler handler)
  {
    if (handler == null)
      throw new IllegalArgumentException ();
    if (! this.serverProtocolHandlers.contains (handler))
    {
      this.serverProtocolHandlers.add (handler);
      handler.registerListener (this);
      fireChanged ();
    }
  }
  
  public final synchronized void removeServerProtocolHandler (final BtpSapServerProtocolHandler handler)
  {
    if (handler == null || ! this.serverProtocolHandlers.contains (handler))
      throw new IllegalArgumentException ();
    handler.unregisterListener (this);
    this.serverProtocolHandlers.remove (handler);
    fireChanged ();
  }
  
  public final synchronized Set<BtpSapServerProtocolHandler> getServerProtocolHandlers ()
  {
    return new LinkedHashSet<> (this.serverProtocolHandlers);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CLIENT-PROTOCOL HANDLERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<BtpSapClientProtocolHandler> clientProtocolHandlers = new LinkedHashSet<> ();
  
  public final synchronized void addClientProtocolHandler (final BtpSapClientProtocolHandler handler)
  {
    if (handler == null)
      throw new IllegalArgumentException ();
    if (! this.clientProtocolHandlers.contains (handler))
    {
      this.clientProtocolHandlers.add (handler);
      handler.registerListener (this);
      fireChanged ();
    }
  }
  
  public final synchronized void removeClientProtocolHandler (final BtpSapClientProtocolHandler handler)
  {
    if (handler == null || ! this.clientProtocolHandlers.contains (handler))
      throw new IllegalArgumentException ();
    handler.unregisterListener (this);
    this.clientProtocolHandlers.remove (handler);
    fireChanged ();
  }
  
  public final synchronized Set<BtpSapClientProtocolHandler> getClientProtocolHandlers ()
  {
    return new LinkedHashSet<> (this.clientProtocolHandlers);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<BtpSapServer> servers = new LinkedHashSet<> ();
  
  public final synchronized void addServer (final BtpSapServer server)
  {
    if (server == null)
      throw new IllegalArgumentException ();
    if (! this.servers.contains (server))
    {
      this.servers.add (server);
      server.registerListener (this);
      fireChanged ();
    }
  }
  
  public final synchronized void removeServer (final BtpSapServer server)
  {
    if (server == null || ! this.servers.contains (server))
      throw new IllegalArgumentException ();
    server.unregisterListener (this);
    this.servers.remove (server);
    fireChanged ();
  }
  
  public final synchronized Set<BtpSapServer> getServers ()
  {
    return new LinkedHashSet<> (this.servers);
  }
  
  public final synchronized int getServerId (final BtpSapServer server)
  {
    if (server == null || ! this.servers.contains (server))
      return -1;
    return new ArrayList<> (this.servers).indexOf (server) + 1;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CLIENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public final static int MAX_CLIENTS = 256;
  
  private final Set<BtpSapClient> clients = new LinkedHashSet<> ();
  
  private final Map<BtpSapClient, Integer> clientIdMap = new HashMap<> ();
  
  private synchronized int nextClientId ()
  {
    for (int id = 0; id < MAX_CLIENTS; id++)
      if (! this.clientIdMap.containsValue (id))
        return id;
    return -1;
  }
  
  public final synchronized boolean addClient (final BtpSapClient client)
  {
    if (client == null)
      throw new IllegalArgumentException ();
    if (this.clients.contains (client))
      return true;
    final int clientId = nextClientId ();
    if (clientId < 0)
      return false;
    this.clients.add (client);
    this.clientIdMap.put (client, clientId);
    client.registerListener (this);
    fireChanged ();
    return true;
  }
  
  public final synchronized BtpSapClient getClient (final int clientId)
  {
    if (clientId < 0 || clientId >= MAX_CLIENTS || ! this.clientIdMap.values ().contains (clientId))
      // XXX Throw exception??
      return null;
    for (final Entry<BtpSapClient, Integer> entry : this.clientIdMap.entrySet ())
      if (entry.getValue () == clientId)
        return entry.getKey ();
    throw new RuntimeException ();
  }
  
  public final synchronized int getClientId (final BtpSapClient client)
  {
    if (client == null)
      throw new IllegalArgumentException ();
    if (this.clientIdMap.containsKey (client))
      return this.clientIdMap.get (client);
    else
      return -1;
  }
  
  public final synchronized void removeClient (final BtpSapClient client)
  {
    if (client == null || ! this.clients.contains (client))
    {
      LOG.log (Level.WARNING, "Unknown or null client {0}.", client);
      return;
    }
    if (! this.clientIdMap.containsKey (client))
      throw new RuntimeException ();
    client.unregisterListener (this);
    this.clients.remove (client);
    this.clientIdMap.remove (client);
    this.tcRequestRouting.remove (client);
    this.indicationRouting.remove (client);
    fireChanged ();
  }
  
  public final synchronized Set<BtpSapClient> getClients ()
  {
    return new LinkedHashSet<> (this.clients);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TC-BASED REQUEST ROUTING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // client -> (tcLow -> list of unit or null)
  private final Map<BtpSapClient, NavigableMap<Integer, Set<BtpSapServer>>> tcRequestRouting = new LinkedHashMap<> ();
  
  public final synchronized Map<BtpSapClient, NavigableMap<Integer, Set<BtpSapServer>>> getTcRequestRouting ()
  {
    return Collections.unmodifiableMap (new LinkedHashMap<> (this.tcRequestRouting));
  }
  
  public final synchronized NavigableMap<Integer, Set<BtpSapServer>> getTcRequestRouting (final BtpSapClient client)
  {
    if (client == null)
    {
      LOG.log (Level.WARNING, "Null client!");
      return null;
    }
    if (! this.tcRequestRouting.containsKey (client))
    {
      LOG.log (Level.WARNING, "Unknown client {0}!", client);
      return null;
    }
    else
      // XXX Java 7 does not support this; shame on Java 7!...
      // return Collections.unmodifiableNavigableMap (new TreeMap<> (this.tcRequestRouting.get (client)));
      return new TreeMap<> (this.tcRequestRouting.get (client));
  }
  
  public final synchronized Set<BtpSapServer> getTcRequestRouting (final BtpSapClient client, final int tc)
  {
    if (client == null)
    {
      LOG.log (Level.WARNING, "Null client!");
      return null;
    }
    if (! this.tcRequestRouting.containsKey (client))
    {
      LOG.log (Level.WARNING, "Unknown client {0}!", client);
      return null;
    }
    if ((tc & ~0xff) != 0)
      // TC is at most 8 bits, the relevant part for routing is only (least-significant) 6 bits.
      // Issue warning otherwise...
      LOG.log (Level.WARNING, "Traffic class consists of at most 8 bits; of which only 6 bits are relevant for TC Routing;"
        + " client={0}, tc={1}.", new Object[]{client, tc});
    final Entry<Integer, Set<BtpSapServer>> floorEntry = this.tcRequestRouting.get (client).floorEntry (tc & 0x3f);
    if (floorEntry != null)
      return Collections.unmodifiableSet (floorEntry.getValue ());
    else
      return null;
  }
  
  public final synchronized void clearTcRequestRouting (final BtpSapClient client)
  {
    if (client != null && this.tcRequestRouting.containsKey (client))
    {
      this.tcRequestRouting.remove (client);
      fireChanged ();
    }
  }
  
  public final synchronized void clearTcRequestRouting (final BtpSapClient client, final BtpSapServer server)
  {
    if (client == null || server == null || ! this.tcRequestRouting.containsKey (client))
    {
      LOG.log (Level.WARNING, "One or more illegal (null) arguments, or client unknown: client={0}, server={1}.",
        new Object[]{client, server});
      return;
    }
    for (final Set<BtpSapServer> servers : this.tcRequestRouting.get (client).values ())
      servers.remove (server);
    canonicalizeTcRequestRouting (this.tcRequestRouting.get (client));
    fireChanged ();
  }
  
  public final synchronized void addTcRequestRouting
  (final BtpSapClient client, final int tcLow, final int tcHigh, final BtpSapServer server)
  {
    if (client == null || tcLow < 0 || tcLow >= 64 || tcHigh < 0 || tcHigh > 64 || tcLow >= tcHigh || server == null)
      // XXX LOG!
      return;
    if (! this.tcRequestRouting.containsKey (client))
      this.tcRequestRouting.put (client, new TreeMap<Integer, Set<BtpSapServer>> ());
    final NavigableMap<Integer, Set<BtpSapServer>> tcRequestRouting_client = this.tcRequestRouting.get (client);
    if (! tcRequestRouting_client.containsKey (tcLow))
    {
      final Entry<Integer, Set<BtpSapServer>> lowerEntry = tcRequestRouting_client.lowerEntry (tcLow);
      if (lowerEntry != null)
        tcRequestRouting_client.put (tcLow, new LinkedHashSet<> (lowerEntry.getValue ()));
      else
        tcRequestRouting_client.put (tcLow, new LinkedHashSet<BtpSapServer> ());
    }
    if (! tcRequestRouting_client.containsKey (tcHigh))
    {
      final Entry<Integer, Set<BtpSapServer>> lowerEntry = tcRequestRouting_client.lowerEntry (tcHigh);
      if (lowerEntry != null)
        tcRequestRouting_client.put (tcHigh, new LinkedHashSet<> (lowerEntry.getValue ()));
      else
        tcRequestRouting_client.put (tcHigh, new LinkedHashSet<BtpSapServer> ());      
    }
    for (Set<BtpSapServer> serverSet : tcRequestRouting_client.subMap (tcLow, tcHigh).values ())
      serverSet.add (server);
    canonicalizeTcRequestRouting (tcRequestRouting_client);
    fireChanged ();
  }
  
  private synchronized void canonicalizeTcRequestRouting ()
  {
    for (NavigableMap<Integer, Set<BtpSapServer>> tcRequestRouting_client: this.tcRequestRouting.values ())
      canonicalizeTcRequestRouting (tcRequestRouting_client);
  }
  
  private synchronized void canonicalizeTcRequestRouting (final NavigableMap<Integer, Set<BtpSapServer>> tcRequestRouting_client)
  {
    if (tcRequestRouting_client != null)
    {
      final Set<Integer> keysToRemove = new HashSet<> ();
      Set<BtpSapServer> prevSet = Collections.emptySet ();
      for (final Entry<Integer, Set<BtpSapServer>> entry: tcRequestRouting_client.entrySet ())
      {
        if (entry.getValue ().equals (prevSet))
          keysToRemove.add (entry.getKey ());
        prevSet = entry.getValue ();
      }
      for (final int keyToRemove : keysToRemove)
        tcRequestRouting_client.remove (keyToRemove);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INDICATION ROUTING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<BtpSapClient, Map<BtpSapServer, String>> indicationRouting = new LinkedHashMap<> ();
  
  public final synchronized Map<BtpSapClient, Map<BtpSapServer, String>> getIndicationRouting ()
  {
    return Collections.unmodifiableMap (new LinkedHashMap<> (this.indicationRouting));
  }
  
  public final synchronized Map<BtpSapServer, String> getIndicationRouting (final BtpSapClient client)
  {
    if (client == null)
      // XXX Do we really want to throw an Exception here???
      throw new IllegalArgumentException ();
    if (! this.indicationRouting.containsKey (client))
      return null;
    else
      return Collections.unmodifiableMap (new LinkedHashMap<> (this.indicationRouting.get (client)));
  }
  
  public final synchronized String getIndicationRouting (final BtpSapClient client, final BtpSapServer server)
  {
    if (! this.indicationRouting.containsKey (client))
      return null;
    final Map<BtpSapServer, String> indicationRoutingClient = getIndicationRouting (client);
    if (indicationRoutingClient.containsKey (server))
      return indicationRoutingClient.get (server);
    if (indicationRoutingClient.containsKey (null))
      return indicationRoutingClient.get (null);
    return null;
  }
  
  public final synchronized void setIndicationRouting
  (final BtpSapClient client, final BtpSapServer server, final String clientString)
  {
    if (client == null)
      throw new IllegalArgumentException ();
    if (! this.indicationRouting.containsKey (client))
      this.indicationRouting.put (client, new LinkedHashMap<BtpSapServer, String> ());
    this.indicationRouting.get (client).put (server, clientString);
    fireChanged ();
  }
  
  public final synchronized void removeIndicationRouting (final BtpSapClient client, final BtpSapServer server)
  {
    if (client == null || server == null)
      throw new IllegalArgumentException ();
    if (! this.indicationRouting.containsKey (client))
      this.indicationRouting.put (client, new LinkedHashMap<BtpSapServer, String> ());
    this.indicationRouting.get (client).remove (server);
    fireChanged ();
  }
  
  public final synchronized void removeIndicationRouting (final BtpSapClient client)
  {
    if (client == null)
      throw new IllegalArgumentException ();
    this.indicationRouting.remove (client);
    fireChanged ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSapEntity.Listener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public final void changed (final AbstractBtpSapEntity entity)
  {
    fireChanged ();
  }

}
