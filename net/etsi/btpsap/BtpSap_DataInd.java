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
 *
 */
package net.etsi.btpsap;

/** Java binding for a BTP (Basic Transport Protocol) SAP (Service Access Point entity) Data Indication.
 *
 * <p>
 * After ETSI EN 302 636-5-1, V1.2.1 (2014-08).
 * 
 * @author Jan de Jongh, TNO
 * 
 */
public interface BtpSap_DataInd
extends BtpSapTypes
{
    
  /** BTP-Data.indication.
   * 
   * <p>
   * From ETSI EN 302 636-5-1, V1.2.1 (2014-08), ANNEX A (INFORMATIVE).
   * 
   * <p>
   * The BTP-Data.indication primitive indicates to an ITS facilities layer protocol entity that a ITS-FSDU has been received.
   * The ITS-FSDU is processed as determined by the receiving upper protocol entity.
   * 
   * @param btpSrcPort        The BTP source port (optional), {@code btpSrcPort == null || 0 <= btpSrcPort <= 65535}.
   * @param btpDstPort        The BTP destination port, {@code btpDstPort != null && 0 <= btpDstPort <= 65535}.
   * @param btpDstPortInfo    The BTP destination port info (optional), {@code btpDstPort == null || 0 <= btpDstPortInfo <= 65535}.
   * @param gnDstAddress      The Geonetworking destination address, non-null for GeoUnicast, GeoBroadcast and GeoAnycast,
   *                          null for SHB and TSB.
   * @param gnSrcPV           The source position vector: The GeoNetworking address, geographical position and optionally
   *                          other parameters of the source of the received GeoNetworking packet. 
   * @param gnSecReport       The security report contains result information from
   *                          the security operations for decryption and verification (optional, may be null).
   * @param gnCertId          The certificate id contains the identification of source certificate,
   *                          for example the certificate hash (optional, may be null).
   * @param gnPermissions     The GN Permissions parameter contains the sender permissions (optional, may be null).
   * @param gnTrafficClass    The traffic class for the message, non-null.
   * @param gnRemLifetime_s   The remaining lifetime of the packet in seconds (optional, may be null).
   * @param length            The number of bytes in the {@code data} buffer received (non-negative).
   * @param data              The bytes received as payload (starting at index zero and upto {@code length} bytes).
   * 
   * @throws IllegalArgumentException If one or more actual arguments has an illegal value.
   * 
   */
  void BTPDataIndication
  (
    Integer btpSrcPort,
    int btpDstPort,
    Integer btpDstPortInfo,
    GnDestination gnDstAddress,
    GnPositionVector gnSrcPV,
    GnSecurityReport gnSecReport,
    GnCertificateId gnCertId,
    GnPermissions gnPermissions,
    GnTrafficClass gnTrafficClass,
    Integer gnRemLifetime_s,
    int length,
    byte data[]
  ) throws IllegalArgumentException;
  
}
