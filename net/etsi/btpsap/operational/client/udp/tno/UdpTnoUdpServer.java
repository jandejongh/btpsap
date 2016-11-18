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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
public class UdpTnoUdpServer
extends InetSocketAddress
implements Runnable
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOG = Logger.getLogger (UdpTnoUdpServer.class.getName ());

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORIES / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public UdpTnoUdpServer (final int port, final UdpTnoClientProtocolHandler handler)
  {
    super (port);
    if (handler == null)
      throw new IllegalArgumentException ();
    this.handler = handler;
  }

  public UdpTnoUdpServer (final InetAddress addr, final int port, final UdpTnoClientProtocolHandler handler)
  {
    super (addr, port);
    if (handler == null)
      throw new IllegalArgumentException ();
    this.handler = handler;
  }

  public UdpTnoUdpServer (final String hostname, final int port, final UdpTnoClientProtocolHandler handler)
  {
    super (hostname, port);
    if (handler == null)
      throw new IllegalArgumentException ();
    this.handler = handler;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (PROTOCOL) HANDLER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final UdpTnoClientProtocolHandler handler;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER SOCKET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private DatagramSocket serverSocket = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // THREAD
  //
  // shutdown
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private Thread thread = null;
  
  public final synchronized void shutdown ()
  {
    LOG.log (Level.INFO, "UdpTnoUdpServer.shutdown on {0}.", this);
    if (this.thread != null && this.thread.isAlive () && this.thread != Thread.currentThread ())
    {
      this.thread.interrupt ();
    }
    if (this.serverSocket != null)
    {
      this.serverSocket.close ();
      LOG.log (Level.INFO, "UdpTnoUdpServer.shutdown on {0}: Server socket closed!", this);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // Runnable
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public final void run ()
  {
    LOG.log (Level.INFO, "UdpTnoUdpServer.run on {0}: Starting!", this);
    synchronized (this)
    {
      if (this.thread != null)
        throw new RuntimeException ();
      this.thread = Thread.currentThread ();
      try
      {
        this.serverSocket = new DatagramSocket (this);
      }
      catch (IOException ioe)
      {
        LOG.log (Level.WARNING, "UdpTnoUdpServer.run on {0}: Cannot bind; terminating!", this);
        shutdown ();
        return;
      }
    }
    LOG.log (Level.INFO, "UdpTnoUdpServer.run on {0}: Bound!", this);
    while (! Thread.interrupted ())
    {
      try
      {
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
        this.serverSocket.receive (packet);
        LOG.log (Level.INFO, "UdpTnoUdpServer.run on {0}: Received packet.");
        this.handler.udpPacket (packet);
      }
      catch (IOException ioe)
      {
        if (Thread.interrupted ())
          break;
        else
        {
          LOG.log (Level.WARNING, "UdpTnoUdpServer.run on {0}: IOException (proceeding): {1}.",
            new Object[]{this, ioe.getMessage ()});
        }
      }
    }
    LOG.log (Level.INFO, "UdpTnoUdpServer.run on {0}: Termination!", this);
    shutdown ();
  }
  
}
