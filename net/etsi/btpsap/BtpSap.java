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

/** Java binding for a BTP (Basic Transport Protocol) SAP (Service Access Point entity).
 *
 * <p>
 * After ETSI EN 302 636-5-1, V1.2.1 (2014-08).
 * 
 * @author Jan de Jongh, TNO
 *
 */
public interface BtpSap
extends BtpSap_DataReq, BtpSap_DataInd
{
  
}
