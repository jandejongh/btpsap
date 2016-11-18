package net.etsi.btpsap.operational.ui;

import java.util.ArrayList;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.etsi.btpsap.operational.BtpSapServerProtocolHandler;
import net.etsi.btpsap.operational.OperationalBtpSapServer;

/**
 *
 */
public class ServerProtocolsTableModel
extends AbstractTableModel
implements TableModel
{

  public ServerProtocolsTableModel (final OperationalBtpSapServer operationalBtpSapServer)
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
  
  private final static String[] COLUMNS = {"Active", "Name"};
  
  @Override
  public final int getColumnCount ()
  {
    return ServerProtocolsTableModel.COLUMNS.length;
  }

  @Override
  public final String getColumnName (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    return ServerProtocolsTableModel.COLUMNS[c];
  }

  @Override
  public final Class<?> getColumnClass (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return Boolean.class;
    else if (c == 1)
      return BtpSapServerProtocolHandler.class;
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
    return this.operationalBtpSapServer.getDb ().getServerProtocolHandlers ().size ();
  }

  @Override
  public final Object getValueAt (final int r, final int c)
  {
    final Set<BtpSapServerProtocolHandler> serverProtocolHandlers
      = this.operationalBtpSapServer.getDb ().getServerProtocolHandlers ();
    if (r < 0 || r >= serverProtocolHandlers.size ())
      return null;
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return new ArrayList<> (serverProtocolHandlers).get (r).isActiveBtpSapServerProtocolHandler ();
    else if (c == 1)
      return new ArrayList<> (serverProtocolHandlers).get (r);
    else
      throw new RuntimeException ();
  }

  @Override
  public final void setValueAt (final Object o, final int r, final int c)
  {
    final Set<BtpSapServerProtocolHandler> serverProtocolHandlers
      = this.operationalBtpSapServer.getDb ().getServerProtocolHandlers ();
    if (r < 0 || r >= serverProtocolHandlers.size ())
      return;
    if (c < 0 || c >= getColumnCount ())
      return;
    if (c == 0)
    {
      final BtpSapServerProtocolHandler handler = new ArrayList<> (serverProtocolHandlers).get (r);
      if ((Boolean) o)
        handler.startBtpSapServerProtocolHandler (this.operationalBtpSapServer);
      else
        handler.stopBtpSapServerProtocolHandler ();
    }
  }
  
}
