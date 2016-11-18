package net.etsi.btpsap.operational.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import net.etsi.btpsap.operational.AbstractBtpSapEntity;
import net.etsi.btpsap.operational.BtpSapEntity;
import net.etsi.btpsap.operational.OperationalBtpSapServer;

/**
 *
 */
public class ClientProtocolsPane
extends JPanel
implements BtpSapEntity.Listener
{

  public ClientProtocolsPane (final OperationalBtpSapServer operationalBtpSapServer)
  {
    if (operationalBtpSapServer == null)
      throw new IllegalArgumentException ();
    this.operationalBtpSapServer = operationalBtpSapServer;
    this.table = new JTable (new ClientProtocolsTableModel (operationalBtpSapServer));
    add (new JScrollPane (this.table));
    this.operationalBtpSapServer.registerListener (this);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONAL BTP-SAP SERVER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final OperationalBtpSapServer operationalBtpSapServer;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TABLE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final JTable table;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BtpSapEntity.Listener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public final void changed (final AbstractBtpSapEntity entity)
  {
    ((AbstractTableModel) this.table.getModel ()).fireTableDataChanged ();
  }
  
}
