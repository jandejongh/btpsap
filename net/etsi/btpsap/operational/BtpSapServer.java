package net.etsi.btpsap.operational;

import net.etsi.btpsap.BtpSap_DataConf;
import net.etsi.btpsap.BtpSap_DataReqContainer;

/**
 *
 */
public interface BtpSapServer
extends BtpSapEntity
{
  
  void startBtpSapServer (OperationalBtpSap btpSap);
  
  void stopBtpSapServer ();
  
  boolean isActiveBtpSapServer ();
  
  BtpSap_DataConf doRequest (BtpSapClient client, BtpSap_DataReqContainer request);
  
}
