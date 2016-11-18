package net.etsi.btpsap.operational.ui;

import java.util.ArrayList;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.etsi.btpsap.operational.BtpSapServer;
import net.etsi.btpsap.operational.OperationalBtpSapServer;

/**
 *
 */
public class ServersTableModel
extends AbstractTableModel
implements TableModel
{

  public ServersTableModel (final OperationalBtpSapServer operationalBtpSapServer)
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
    return ServersTableModel.COLUMNS.length;
  }

  @Override
  public String getColumnName (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    return ServersTableModel.COLUMNS[c];
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
      return BtpSapServer.class;
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
    return this.operationalBtpSapServer.getDb ().getServers ().size ();
  }

  @Override
  public final Object getValueAt (final int r, final int c)
  {
    final Set<BtpSapServer> servers
      = this.operationalBtpSapServer.getDb ().getServers ();
    if (r < 0 || r >= servers.size ())
      return null;
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return new ArrayList<> (servers).get (r).isActiveBtpSapServer ();
    else if (c == 1)
      return this.operationalBtpSapServer.getDb ().getServerId (new ArrayList<> (servers).get (r));
    else if (c == 2)
      return new ArrayList<> (servers).get (r);
    else
      throw new RuntimeException ();
  }
  
  @Override
  public final void setValueAt (final Object o, final int r, final int c)
  {
    final Set<BtpSapServer> servers
      = this.operationalBtpSapServer.getDb ().getServers ();
    if (r < 0 || r >= servers.size ())
      return;
    if (c < 0 || c >= getColumnCount ())
      return;
    if (c == 0)
    {
      final BtpSapServer server = new ArrayList<> (servers).get (r);
      if ((Boolean) o)
        server.startBtpSapServer (this.operationalBtpSapServer);
      else
        server.stopBtpSapServer ();
    }
  }
  
}
