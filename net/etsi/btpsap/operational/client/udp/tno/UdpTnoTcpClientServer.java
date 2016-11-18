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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.etsi.btpsap.operational.BtpSapClient;
import net.etsi.btpsap.operational.BtpSapServer;
import net.etsi.btpsap.operational.OperationalBtpSap;

/**
 *
 *
 */
public class UdpTnoTcpClientServer
implements Runnable
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOG = Logger.getLogger (UdpTnoTcpClientServer.class.getName ());


  public UdpTnoTcpClientServer (final OperationalBtpSap btpSap, final UdpTnoClient client, final Socket clientTcpSocket)
  {
    if (btpSap == null || client == null || clientTcpSocket == null)
      throw new IllegalArgumentException ();
    this.btpSap = btpSap;
    this.client = client;
    this.clientTcpSocket = clientTcpSocket;
  }
  
  private OperationalBtpSap btpSap;
  
  private UdpTnoClient client;
  
  private final Socket clientTcpSocket;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // THREAD
  //
  // shutdown
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private volatile Thread thread = null;
  
  public final synchronized void shutdown ()
  {
    LOG.log (Level.INFO, "UdpTnoTcpClientServer.shutdown on {0}.", this);
    if (this.thread != null && this.thread.isAlive () && this.thread != Thread.currentThread ())
    {
      this.thread.interrupt ();
    }
    if (this.clientTcpSocket != null)
      try
      {
        this.clientTcpSocket.close ();
        LOG.log (Level.INFO, "UdpTnoTcpClientServer.shutdown on {0}: Server socket closed!", this);
      }
      catch (IOException ioe)
      {
        LOG.log (Level.WARNING, "UdpTnoTcpClientServer.shutdown on {0}: IOException during server-socket close: {1}.",
          new Object[]{this, ioe.getMessage ()});
      }
    this.client.disconnect ();
  }
  
  private /* ?? synchronized */ void startUdpClient
  (final Set<BtpSapServer> units, final String remoteAddress, final int remotePort)
  {
    LOG.log (Level.WARNING, "Starting UDP client (TBI) for {0}, units={1}, removeAddress={2}, remotePort={3}.",
      new Object[]{this, units, remoteAddress, remotePort});
    for (final BtpSapServer unit : units)
      LOG.log (Level.WARNING, "  {0} => {1}:{2}.", new Object[]{unit, remoteAddress, remotePort});
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // Runnable
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public final void run ()
  {
    LOG.log (Level.INFO, "UdpTnoTcpClientServer.run on {0}: Starting!", this);
    synchronized (this)
    {
      if (this.thread != null || this.clientTcpSocket == null)
        throw new RuntimeException ();
      this.thread = Thread.currentThread ();
    }
    final int clientId = this.btpSap.getDb ().getClientId (this.client);
    if (clientId < 0)
      throw new RuntimeException ();
    try
    {
      final PrintWriter out = new PrintWriter (this.clientTcpSocket.getOutputStream (), true);
      final BufferedReader in = new BufferedReader (new InputStreamReader (this.clientTcpSocket.getInputStream ()));
      out.println ("client " + clientId);
      out.println ("$$ Welcome to " + this + "!");
      while (! (Thread.interrupted () || this.clientTcpSocket.isClosed ()))
      {
        out.print ("$$ > ");
        out.flush ();
        final String textFromClient = in.readLine ();
        if (textFromClient == null)
          break;
        cli (textFromClient, out);
      }
    }
    catch (SocketException se)
    {
      LOG.log (Level.INFO, "UdpTnoTcpClientServer.run on {0}: SocketException: {1}.", new Object[]{this, se.getMessage ()});
    }
    catch (IOException ioe)
    {
      LOG.log (Level.INFO, "UdpTnoTcpClientServer.run on {0}: IOException: {1}.", new Object[]{this, ioe.getMessage ()});
    }
    catch (Exception e)
    {
      LOG.log (Level.SEVERE, "UdpTnoTcpClientServer.run on {0}: Exception: {1}.", new Object[]{this, e.getMessage ()});
      throw new RuntimeException (e);
    }
    LOG.log (Level.INFO, "UdpTnoTcpClientServer.run on {0}: Termination!", this);
    shutdown ();
  }
    
  private void cli (final String commandString, final PrintWriter out)
  throws IOException
  { 
    if (commandString == null)
      throw new IllegalArgumentException ();
    System.err.println ("COMMAND: " + commandString + ".");
    final String splitTextFromClient[] = commandString.trim ().split ("\\s+");
    if (splitTextFromClient.length > 0 && !splitTextFromClient[0].trim ().isEmpty ())
    {
      final String command = splitTextFromClient[0];
      switch (command.toLowerCase ())
      {
        case "help":
          out.println ("help                                   - Show this help.");
          out.println ("client_id                              - Print client identification.");
          out.println ("show_units                             - Show all available units; numbering starts with unity.");
          out.println ("tc_req_routing                         - Show Traffic-Class based Request Routing (table).");
          out.println ("tc_req_routing_global                  - "
            + "Show Traffic-Class based Request Routing (table) for all clients (priviliged).");
          out.println ("tc_req_routing <tcLow> <tcHigh> <unit> - "
            + "Add unit to TC-based Request Routing for given TC range (inclusive).");
          out.println ("tc_req_routing clear [<unit|*>]        - Clear TC-based Routing for all units (or for given unit).");
          out.println ("ind_routing                            - Show Indication Routing.");
          out.println ("ind_routing_global                     - Show Indication Routing for all clients (priviliged).");
          out.println ("receive <unit|*> url                   - "
            + "Send indications from given unit (or all units) to given URL (udp://<address:port>).");
          out.println ("close <unit|*>                         - Stop sending indications from given unit (or all units).");
          out.println ("exit                                   - Exit as client.");
          break;
        case ("client_id"):
          out.println ("client " + this.btpSap.getClientId (this.client));
          break;
        case ("show_units"):
        {
          final ArrayList<BtpSapServer> servers = new ArrayList<> (this.btpSap.getDb ().getServers ());
          int i = 1;
          for (final BtpSapServer server: servers)
          {
            out.println ("" + i++ + "\t" + server);
          }
          break;
        }
        case ("tc_req_routing"):
        {
          if (splitTextFromClient.length == 1)
          {
            final NavigableMap<Integer, Set<BtpSapServer>> tcRequestRouting_client
              = this.btpSap.getDb ().getTcRequestRouting (this.client);
            if (tcRequestRouting_client == null)
              out.println ("$$ * -> DROP.");
            else
            {
              int prevKey = 0;
              Set<BtpSapServer> prevSet = Collections.emptySet ();
              for (final Entry<Integer, Set<BtpSapServer>> entry : tcRequestRouting_client.entrySet ())
              {
                if (prevSet != null && ! prevSet.isEmpty ())
                  out.println ("$$ [" + prevKey + ", " + (entry.getKey () - 1) + "] -> " + prevSet + ".");
                else if (prevKey != 0 || entry.getKey () != 0)
                  out.println ("$$ [" + prevKey + ", " + (entry.getKey () - 1) + "] -> DROP.");                  
                prevKey = entry.getKey ();
                prevSet = entry.getValue ();
              }
              if (prevKey < 64)
                out.println ("$$ [" + prevKey + ", " + 63 + "] -> DROP.");
            }
          }
          else if (splitTextFromClient.length == 2 && "clear".equalsIgnoreCase (splitTextFromClient[1].trim ()))
          {
            this.btpSap.getDb ().clearTcRequestRouting (this.client);
          }
          else if (splitTextFromClient.length == 3 && "clear".equalsIgnoreCase (splitTextFromClient[1].trim ()))
          {
            final String unitSpecString = splitTextFromClient[2].trim ();
            final BtpSapServer unit;
            if ("*".equals (unitSpecString))
              unit = null;
            else
            {
              final ArrayList<BtpSapServer> servers = new ArrayList<> (this.btpSap.getDb ().getServers ());
              final int unitNr;
              try
              {
                unitNr = Integer.parseInt (unitSpecString);
              }
              catch (NumberFormatException nfe)
              {
                out.println ("$$ Illegal unit specification: " + unitSpecString + "!");
                break;
              }
              if (unitNr < 1 || unitNr > servers.size ())
              {
                out.println ("$$ Illegal unit number (out of range): " + unitNr + "!");
                break;
              }
              unit = servers.get (unitNr - 1);
            }
            if (unit == null)
              this.btpSap.getDb ().clearTcRequestRouting (this.client);
            else
              this.btpSap.getDb ().clearTcRequestRouting (this.client, unit);
          }
          else if (splitTextFromClient.length == 4)
          {
            final String tcLowString = splitTextFromClient[1];
            final String tcHighString = splitTextFromClient[2];
            final String unitSpecString = splitTextFromClient[3];
            final int tcLow;
            try
            {
              tcLow = Integer.parseInt (tcLowString);
            }
            catch (NumberFormatException nfe)
            {
              out.println ("$$ Illegal traffic-class specification: " + tcLowString + "!");
              break;
            }
            if (tcLow < 0 || tcLow >= 64)
            {
              out.println ("$$ Illegal traffic-class specification (out of [0, 63] range): " + tcLowString + "!");
              break;
            }
            final int tcHigh;
            try
            {
              tcHigh = Integer.parseInt (tcHighString);
            }
            catch (NumberFormatException nfe)
            {
              out.println ("$$ Illegal traffic-class specification: " + tcHighString + "!");
              break;
            }
            if (tcHigh < 0 || tcHigh >= 64)
            {
              out.println ("$$ Illegal traffic-class specification (out of [0, 63] range): " + tcHighString + "!");
              break;
            }
            if (tcLow > tcHigh)
            {
              out.println ("$$ Illegal traffic-class range specification (upper boundary is stricly smaller than lower boundary): "
                + "[" + tcLowString + ", " + tcHighString + "]!");
              break;
            }            
            final int unitNr;
            try
            {
              unitNr = Integer.parseInt (unitSpecString);
            }
            catch (NumberFormatException nfe)
            {
              out.println ("$$ Illegal unit specification: " + unitSpecString + "!");
              break;
            }
            final ArrayList<BtpSapServer> servers = new ArrayList<> (this.btpSap.getDb ().getServers ());
            if (unitNr < 1 || unitNr > servers.size ())
            {
              out.println ("$$ Illegal unit number (out of range): " + unitNr + "!");
              break;
            }
            final BtpSapServer unit = servers.get (unitNr - 1);
            this.btpSap.getDb ().addTcRequestRouting (this.client, tcLow, tcHigh + 1, unit);
          }
          else
          {
            out.println ("$$ Illegal parameters!");
          }
          break;
        }
        case ("tc_req_routing_global"):
        {
          final Map<BtpSapClient, NavigableMap<Integer, Set<BtpSapServer>>> tcRequestRouting
            = this.btpSap.getDb ().getTcRequestRouting ();
          if (tcRequestRouting == null || tcRequestRouting.containsKey (null))
            throw new IllegalArgumentException ();
          for (final BtpSapClient client : tcRequestRouting.keySet ())
          {
            out.println ("$$ client: " + client + " [" + this.btpSap.getClientId (client) + "]");
            final NavigableMap<Integer, Set<BtpSapServer>> tcRequestRouting_client
              = this.btpSap.getDb ().getTcRequestRouting (this.client);
            if (tcRequestRouting_client == null)
              out.println ("$$   * -> DROP.");
            else
            {
              int prevKey = 0;
              Set<BtpSapServer> prevSet = Collections.emptySet ();
              for (final Entry<Integer, Set<BtpSapServer>> entry : tcRequestRouting_client.entrySet ())
              {
                if (prevSet != null && ! prevSet.isEmpty ())
                  out.println ("$$   [" + prevKey + ", " + (entry.getKey () - 1) + "] -> " + prevSet + ".");
                else if (prevKey != 0 || entry.getKey () != 0)
                  out.println ("$$   [" + prevKey + ", " + (entry.getKey () - 1) + "] -> DROP.");                  
                prevKey = entry.getKey ();
                prevSet = entry.getValue ();
              }
              if (prevKey < 64)
                out.println ("$$   [" + prevKey + ", " + 63 + "] -> DROP.");
            }
          }
          break;
        }
        case ("receive"):
        {
          if (splitTextFromClient.length == 3)
          {
            final ArrayList<BtpSapServer> servers = new ArrayList<> (this.btpSap.getDb ().getServers ());
            final String unitSpecString = splitTextFromClient[1].trim ();
            final String urlString = splitTextFromClient[2].trim ().toLowerCase ();
            final BtpSapServer unit;
            if ("*".equals (unitSpecString))
              unit = null;
            else
            {
              final int unitNr;
              try
              {
                unitNr = Integer.parseInt (unitSpecString);
              }
              catch (NumberFormatException nfe)
              {
                out.println ("$$ Illegal unit specification: " + unitSpecString + "!");
                break;
              }
              if (unitNr < 1 || unitNr > servers.size ())
              {
                out.println ("$$ Illegal unit number (out of range): " + unitNr + "!");
                break;
              }
              unit = servers.get (unitNr - 1);
            }
            if (urlString.startsWith ("udp://"))
            {
              final String addressPortString = urlString.substring (6);
              if (! addressPortString.contains (":"))
              {
                out.println ("$$ Illegal url: " + urlString + "!");
                break;
              }
              final String[] addressPortStringSplit = addressPortString.split (":");
              if (addressPortStringSplit.length != 2)
              {
                out.println ("$$ Illegal url: " + urlString + "!");
                break;
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
                out.println ("$$ Illegal url: " + urlString + "!");
                break;
              }
              if (port < 0 || port >= 65536)
              {
                out.println ("$$ Illegal port number (out of range): " + port + "!");
                break;
              }
              this.btpSap.getDb ().setIndicationRouting (this.client, unit, urlString);
            }
            else
            {
              out.println ("$$ Illegal url: " + urlString + "!");
              break;
            }
          }
          else
          {
            out.println ("$$ Illegal number of parameters (requires 2)!");
          }
          break;
        }
        case ("close"):
          if (splitTextFromClient.length == 2)
          {
            final ArrayList<BtpSapServer> servers = new ArrayList<> (this.btpSap.getDb ().getServers ());
            final String unitSpecString = splitTextFromClient[1].trim ();
            final BtpSapServer unit;
            if ("*".equals (unitSpecString))
              unit = null;
            else
            {
              final int unitNr;
              try
              {
                unitNr = Integer.parseInt (unitSpecString);
              }
              catch (NumberFormatException nfe)
              {
                out.println ("$$ Illegal unit specification: " + unitSpecString + "!");
                break;
              }
              if (unitNr < 1 || unitNr > servers.size ())
              {
                out.println ("$$ Illegal unit number (out of range): " + unitNr + "!");
                break;
              }
              unit = servers.get (unitNr - 1);
            }
            if (unit == null)
              this.btpSap.getDb ().removeIndicationRouting (this.client);
            else
              this.btpSap.getDb ().setIndicationRouting (this.client, unit, null);
          }
          else
            out.println ("$$ Illegal number of parameters (requires 1)!");
          break;
        case ("ind_routing"):
        {
          final Map<BtpSapServer, String> indicationRouting_client = this.btpSap.getDb ().getIndicationRouting (this.client);
          if (indicationRouting_client == null)
            out.println ("$$ * -> DROP.");
          else
          {
            for (final BtpSapServer unit : indicationRouting_client.keySet ())
              if (unit != null)
              {
                final String targetString =
                  ((indicationRouting_client.get (unit) == null) ? "DROP" : indicationRouting_client.get (unit));
                out.println ("$$ " + unit + " -> " + targetString + ".");
              }
            if (indicationRouting_client.containsKey (null))
              out.println ("$$ * -> " + indicationRouting_client.get (null) + ".");
          }
          break;
        }
        case ("ind_routing_global"):
        {
          final Map<BtpSapClient, Map<BtpSapServer, String>> indicationRouting
            = this.btpSap.getDb ().getIndicationRouting ();
          if (indicationRouting == null || indicationRouting.containsKey (null))
            throw new IllegalArgumentException ();
          for (final BtpSapClient client : indicationRouting.keySet ())
          {
            out.println ("$$ client: " + client + " [" + this.btpSap.getClientId (client) + "]");
            final Map<BtpSapServer, String> indicationRouting_client = this.btpSap.getDb ().getIndicationRouting (client);
            if (indicationRouting_client == null)
              out.println ("$$   * -> DROP.");
            else
            {
              for (final BtpSapServer unit : indicationRouting_client.keySet ())
                if (unit != null)
                {
                  final String targetString =
                    ((indicationRouting_client.get (unit) == null) ? "DROP" : indicationRouting_client.get (unit));
                  out.println ("$$ " + unit + " -> " + targetString + ".");
                }
              if (indicationRouting_client.containsKey (null))
                out.println ("$$   * -> " + indicationRouting_client.get (null) + ".");
            }
          }
          break;
        }
        case "exit":
          out.println ("$$ Exiting!");
          this.clientTcpSocket.close ();
          break;
        default:
          out.println ("$$ Unknown command " + command + "!");
          break;
      }
    }
  }

}