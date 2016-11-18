package net.etsi.btpsap.operational.ui;

import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import net.etsi.btpsap.operational.AbstractBtpSapEntity;
import net.etsi.btpsap.operational.BtpSapEntity;
import net.etsi.btpsap.operational.OperationalBtpSapServer;

/**
 *
 */
public class OperationalBtpSapServerFrame
extends JFrame
implements BtpSapEntity.Listener
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOG = Logger.getLogger (OperationalBtpSapServerFrame.class.getName ());

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public OperationalBtpSapServerFrame (final OperationalBtpSapServer operationalBtpSapServer)
  {
    if (operationalBtpSapServer == null)
      throw new IllegalArgumentException ();
    setTitle ("BTP SAP GUI - V0.1");
    setMinimumSize (new Dimension (600, 300));
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    this.operationalBtpSapServer = operationalBtpSapServer;
    final JTabbedPane tabbedPane = new JTabbedPane ();
    this.serverProtocolsPane = new ServerProtocolsPane (this.operationalBtpSapServer);
    tabbedPane.add ("Server Protocols", this.serverProtocolsPane);
    this.clientProtocolsPane = new ClientProtocolsPane (this.operationalBtpSapServer);
    tabbedPane.add ("Client Protocols", this.clientProtocolsPane);
    this.serversPane = new ServersPane (this.operationalBtpSapServer);
    tabbedPane.add ("Servers", this.serversPane);
    this.clientsPane = new ClientsPane (this.operationalBtpSapServer);
    tabbedPane.add ("Clients", this.clientsPane);
    this.logPane = new LogPane ();
    tabbedPane.add ("Log", this.logPane);
    setContentPane (tabbedPane);
    pack ();
    setLocationRelativeTo (null);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONAL BTP-SAP SERVER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final OperationalBtpSapServer operationalBtpSapServer;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER PROTOCOLS PANE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final ServerProtocolsPane serverProtocolsPane;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CLIENT PROTOCOLS PANE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final ClientProtocolsPane clientProtocolsPane;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVERS PANE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final ServersPane serversPane;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CLIENTS PANE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final ClientsPane clientsPane;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOG PANE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final LogPane logPane;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSapEntity.Listener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public final void changed (final AbstractBtpSapEntity entity)
  {
    // XXX...
  }
  
}
