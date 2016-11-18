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
package net.etsi.btpsap;

import java.io.IOException;

/** Java binding to a BTP (Basic Transport Protocol) SAP (Service Access Point) entity.
 *
 * <p>
 * This is the BTP-Data.request part.
 * 
 * <p>
 * After ETSI EN 302 636-5-1, V1.2.1 (2014-08).
 * 
 */
public interface BtpSap_DataReq
extends BtpSapTypes
{
    
  /** BTP-Data.request.
   * 
   * <p>
   * From ETSI EN 302 636-5-1, V1.2.1 (2014-08), ANNEX A (INFORMATIVE).
   * 
   * <p>
   * The BTP-Data.request primitive is used by the ITS Facilities protocol entity to request sending a BTP-PDU.
   * Upon reception of the BTP-Data.request primitive,
   * the BTP protocol delivers the BTP-SDU to the GeoNetworking protocol entity via the GN-SAP.
   * 
   * @param btpType           The BTP type, BTP-A or BTP-B.
   * @param btpSrcPort        The BTP source port (optional), {@code btpSrcPort == null || 0 <= btpSrcPort <= 65535}.
   * @param btpDstPort        The BTP destination port, {@code btpDstPort != null && 0 <= btpDstPort <= 65535}.
   * @param btpDstPortInfo    The BTP destination port info (optional),
   *                          {@code btpDstPortInfo == null || 0 <= btpDstPortInfo <= 65535}.
   * @param gnTransportType   The Geonetworking transport type, non-null.
   * @param gnDstAddress      The Geonetworking destination address, non-null for GeoUnicast, GeoBroadcast and GeoAnycast,
   *                          null for SHB and TSB.
   * @param gnCommProfile     The Geonetworking communications profile (optional, may be null).
   * @param gnSecProfile      The Geonetworking security profile (optional, may be null).
   * @param gnMaxLifetime_ms  The maximum tolerable time in milliseconds a GeoNetworking packet can be buffered
   *                          until it reaches its destination (optional, may be null).
   *                          (Note that ETSI specifies this with seconds resolution,
   *                           whereas the GN standard allows ms resolution.)
   * @param gnRepInterval_ms  The duration in milliseconds between two consecutive transmissions
   *                          of the same GeoNetworking packet during the repetition time (optional, may be null).
   * @param gnMaxRepTime_ms   The time in milliseconds a GeoNetworking packet is repeated (optional, may be null).
   *                          (Note that ETSI specifies this with seconds resolution,
   *                           whereas the GN standard allows ms resolution.)
   * @param gnMaxHopLimit     The number of hops a packet is allowed to have in the network,
   *                          i.e. how often the packet is allowed to be forwarded, non-negative.
   * @param gnTrafficClass    The traffic class for the message, non-null.
   * @param offset            The offset into the {@code data} buffer at which the message contents start.
   * @param length            The number of bytes from the {@code data} buffer to send (non-negative).
   * @param data              The bytes to be sent as payload (starting at index {@code offset} and upto {@code length} bytes).
   * 
   */
  void BTPDataRequest
  (
    BtpType btpType,
    Integer btpSrcPort,
    int btpDstPort,
    Integer btpDstPortInfo,
    GnTransportType gnTransportType,
    GnDestination gnDstAddress,
    GnCommunicationsProfile gnCommProfile,
    GnSecurityProfile gnSecProfile,
    Integer gnMaxLifetime_ms,
    Integer gnRepInterval_ms,
    Integer gnMaxRepTime_ms,
    int gnMaxHopLimit,
    GnTrafficClass gnTrafficClass,
    int offset,
    int length,
    byte data[]
  ) throws IllegalArgumentException, IOException;
  
}
