package net.etsi.btpsap.operational.ui;

import java.util.ArrayList;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.etsi.btpsap.operational.BtpSapClient;
import net.etsi.btpsap.operational.OperationalBtpSapServer;

/**
 *
 */
public class ClientsTableModel
extends AbstractTableModel
implements TableModel
{

  public ClientsTableModel (final OperationalBtpSapServer operationalBtpSapServer)
  {
    if (operationalBtpSapServer == null)
      throw new IllegalArgumentException ();
    this.operationalBtpSapServer = operationalBtpSapServer;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONAL BTP-SAP SERVER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final OperationalBtpSapServer operationalBtpSapServer;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TableModel
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final static String[] COLUMNS = {"Active", "Id", "Name"};
  
  @Override
  public final int getColumnCount ()
  {
    return ClientsTableModel.COLUMNS.length;
  }

  @Override
  public String getColumnName (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    return ClientsTableModel.COLUMNS[c];
  }

  @Override
  public final Class<?> getColumnClass (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return Boolean.class;
    if (c == 1)
      return String.class;
    else if (c == 2)
      return BtpSapClient.class;
    else
      throw new RuntimeException ();
  }
  
  @Override
  public final boolean isCellEditable (final int r, final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return false;
    return c == 0;
  }

  @Override
  public final int getRowCount ()
  {
    return this.operationalBtpSapServer.getDb ().getClients ().size ();
  }

  @Override
  public final Object getValueAt (final int r, final int c)
  {
    final Set<BtpSapClient> clients
      = this.operationalBtpSapServer.getDb ().getClients ();
    if (r < 0 || r >= clients.size ())
      return null;
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return new ArrayList<> (clients).get (r).isActiveBtpSapClient ();
    else if (c == 1)
      return this.operationalBtpSapServer.getDb ().getClientId (new ArrayList<> (clients).get (r));
    else if (c == 2)
      return new ArrayList<> (clients).get (r);
    else
      throw new RuntimeException ();
  }
  
  @Override
  public final void setValueAt (final Object o, final int r, final int c)
  {
    final Set<BtpSapClient> clients
      = this.operationalBtpSapServer.getDb ().getClients ();
    if (r < 0 || r >= clients.size ())
      return;
    if (c < 0 || c >= getColumnCount ())
      return;
    if (c == 0)
    {
      final BtpSapClient client = new ArrayList<> (clients).get (r);
      if ((Boolean) o)
        client.startBtpSapClient (this.operationalBtpSapServer);
      else
        client.stopBtpSapClient ();
    }
  }
  
}
