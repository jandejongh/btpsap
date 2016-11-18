package net.etsi.btpsap.operational.ui;

import java.util.ArrayList;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.etsi.btpsap.operational.BtpSapClientProtocolHandler;
import net.etsi.btpsap.operational.OperationalBtpSapServer;

/**
 *
 */
public class ClientProtocolsTableModel
extends AbstractTableModel
implements TableModel
{

  public ClientProtocolsTableModel (final OperationalBtpSapServer operationalBtpSapServer)
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
    return ClientProtocolsTableModel.COLUMNS.length;
  }

  @Override
  public String getColumnName (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    return ClientProtocolsTableModel.COLUMNS[c];
  }

  @Override
  public final Class<?> getColumnClass (final int c)
  {
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return Boolean.class;
    else if (c == 1)
      return BtpSapClientProtocolHandler.class;
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
    return this.operationalBtpSapServer.getDb ().getClientProtocolHandlers ().size ();
  }

  @Override
  public final Object getValueAt (final int r, final int c)
  {
    final Set<BtpSapClientProtocolHandler> clientProtocolHandlers
      = this.operationalBtpSapServer.getDb ().getClientProtocolHandlers ();
    if (r < 0 || r >= clientProtocolHandlers.size ())
      return null;
    if (c < 0 || c >= getColumnCount ())
      return null;
    if (c == 0)
      return new ArrayList<> (clientProtocolHandlers).get (r).isActiveBtpSapClientProtocolHandler ();
    else if (c == 1)
      return new ArrayList<> (clientProtocolHandlers).get (r);
    else
      throw new RuntimeException ();
  }
  
  @Override
  public final void setValueAt (final Object o, final int r, final int c)
  {
    final Set<BtpSapClientProtocolHandler> clientProtocolHandlers
      = this.operationalBtpSapServer.getDb ().getClientProtocolHandlers ();
    if (r < 0 || r >= clientProtocolHandlers.size ())
      return;
    if (c < 0 || c >= getColumnCount ())
      return;
    if (c == 0)
    {
      final BtpSapClientProtocolHandler handler = new ArrayList<> (clientProtocolHandlers).get (r);
      if ((Boolean) o)
        handler.startBtpSapClientProtocolHandler (this.operationalBtpSapServer);
      else
        handler.stopBtpSapClientProtocolHandler ();
    }
  }
  
}
