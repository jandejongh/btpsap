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
package net.etsi.btpsap.operational.server.avgn.local;

import java.io.IOException;
import net.etsi.btpsap.operational.OperationalBtpSap;
import net.etsi.btpsap.operational.BtpSapServerProtocolHandler;
import net.etsi.btpsap.BtpSapTypes.BtpType;
import net.etsi.btpsap.BtpSapTypes.GnCommunicationsProfile;
import net.etsi.btpsap.BtpSapTypes.GnDestination;
import net.etsi.btpsap.BtpSapTypes.GnSecurityProfile;
import net.etsi.btpsap.BtpSapTypes.GnTrafficClass;
import net.etsi.btpsap.BtpSapTypes.GnTransportType;
import net.etsi.btpsap.operational.AbstractBtpSapEntity;

/**
 *
 *
 */
public class AvgnLocalProtocolHandler
extends AbstractBtpSapEntity
implements BtpSapServerProtocolHandler
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public AvgnLocalProtocolHandler ()
  {
    super ("AvgnLocalProtocolHandler");
  }

  private boolean isActive = false;
  
  @Override
  public final synchronized void startBtpSapServerProtocolHandler (final OperationalBtpSap btpSap)
  {
    this.isActive = true;
  }

  @Override
  public final synchronized void stopBtpSapServerProtocolHandler ()
  {
    this.isActive = false;
  }

  @Override
  public final synchronized boolean isActiveBtpSapServerProtocolHandler ()
  {
    return this.isActive;
  }

  @Override
  public void BTPDataRequest (BtpType btpType, Integer btpSrcPort, int btpDstPort, Integer btpDstPortInfo, GnTransportType gnTransportType, GnDestination gnDstAddress, GnCommunicationsProfile gnCommProfile, GnSecurityProfile gnSecProfile, Integer gnMaxLifetime_s, Integer gnRepInterval_ms, Integer gnMaxRepTime_s, int gnMaxHopLimit, GnTrafficClass gnTrafficClass, int offset, int length, byte[] data) throws IllegalArgumentException, IOException
  {
    throw new UnsupportedOperationException ();
  }
  
}
