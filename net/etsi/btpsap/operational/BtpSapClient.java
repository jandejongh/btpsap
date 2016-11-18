package net.etsi.btpsap.operational;

import net.etsi.btpsap.BtpSap_DataIndContainer;
import net.etsi.btpsap.BtpSap_DataResp;

/**
 *
 */
public interface BtpSapClient
extends BtpSapEntity
{
  
  void startBtpSapClient (OperationalBtpSap btpSap);
  
  void stopBtpSapClient ();
  
  boolean isActiveBtpSapClient ();
 
  BtpSap_DataResp doIndication (BtpSapServer server, BtpSap_DataIndContainer indication);
  
}
