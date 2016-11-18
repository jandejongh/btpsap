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
package net.etsi.btpsap.operational;

import java.util.Set;
import net.etsi.btpsap.BtpSap_DataConf;
import net.etsi.btpsap.BtpSap_DataIndContainer;
import net.etsi.btpsap.BtpSap_DataReqContainer;
import net.etsi.btpsap.BtpSap_DataResp;

/**
 *
 *
 */
public interface OperationalBtpSap
{
  
  void startBtpSap ();
  
  void stopBtpSap ();

  boolean isStarted ();
  
  OperationalBtpSapDB getDb ();

  BtpSap_DataConf doRequestFromClient (BtpSapClient client, BtpSap_DataReqContainer request, Set<BtpSapServer> servers);
  
  BtpSap_DataResp doIndicationFromServer (BtpSapServer server, BtpSap_DataIndContainer indication, Set<BtpSapClient> clients);
  
  void registerServerProtocolHandler (BtpSapServerProtocolHandler handler);
  
  void unregisterServerProtocolHandler (BtpSapServerProtocolHandler handler);
  
  Set<BtpSapServerProtocolHandler> getRegisteredServerProtocolHandlers ();
  
  void registerClientProtocolHandler (BtpSapClientProtocolHandler handler);
  
  void unregisterClientProtocolHandler (BtpSapClientProtocolHandler handler);
  
  Set<BtpSapClientProtocolHandler> getRegisteredClientProtocolHandlers ();
 
  void registerServer (BtpSapServer server);
  
  void unregisterServer (BtpSapServer server);
  
  Set<BtpSapServer> getRegisteredServers ();
  
  boolean registerClient (BtpSapClient client);
  
  void unregisterClient (BtpSapClient client);
  
  Set<BtpSapClient> getRegisteredClients ();
  
  int getClientId (BtpSapClient client);
  
}
