package net.etsi.btpsap;

import net.etsi.btpsap.BtpSapTypes.BtpType;
import net.etsi.btpsap.BtpSapTypes.GnCommunicationsProfile;
import net.etsi.btpsap.BtpSapTypes.GnDestination;
import net.etsi.btpsap.BtpSapTypes.GnSecurityProfile;
import net.etsi.btpsap.BtpSapTypes.GnTrafficClass;
import net.etsi.btpsap.BtpSapTypes.GnTransportType;

public class BtpSap_DataReqContainer
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public BtpSap_DataReqContainer
  (final BtpType btpType,
   final Integer btpSrcPort,
   final int btpDstPort,
   final Integer btpDstPortInfo,
   final GnTransportType gnTransportType,
   final GnDestination gnDst,
   final GnCommunicationsProfile gnCommProfile,
   final GnSecurityProfile gnSecProfile,
   final Integer gnMaxLifetime_ms,
   final Integer gnRepInterval_ms,
   final Integer gnMaxRepTime_ms,
   final int gnMaxHopLimit,
   final GnTrafficClass gnTrafficClass,
   final int length,
   final int offset,
   final byte data[]
  )
  {
    if (btpType == null)
      throw new IllegalArgumentException ();
    this.btpType = btpType;
    if (btpSrcPort != null && (btpSrcPort < 0 || btpSrcPort >= 65536))
      throw new IllegalArgumentException ();
    this.btpSrcPort = btpSrcPort;
    if (btpDstPort < 0 || btpDstPort >= 65536)
      throw new IllegalArgumentException ();
    this.btpDstPort = btpDstPort;
    if (btpDstPortInfo != null && (btpDstPortInfo < 0 || btpDstPortInfo >= 65536))
      throw new IllegalArgumentException ();
    this.btpDstPortInfo = btpDstPortInfo;
    if (gnTransportType == null)
      throw new IllegalArgumentException ();
    this.gnTransportType = gnTransportType;
    switch (this.gnTransportType)
    {
      case GN_SHB:
      case GN_TSB:
        if (gnDst != null)
          throw new IllegalArgumentException ();
        break;
      case GN_UC:
        if (gnDst == null || gnDst.getGnDestinationType () != BtpSapTypes.GnDestinationType.GN_DEST_UC)
          throw new IllegalArgumentException ();
        break;        
      case GN_GBC:
      case GN_AC:
        if (gnDst == null || gnDst.getGnDestinationType () != BtpSapTypes.GnDestinationType.GN_DEST_GBC_AC)
          throw new IllegalArgumentException ();
        break;        
      default:
        throw new IllegalArgumentException ();
    }
    this.gnDst = gnDst;
    this.gnCommProfile = gnCommProfile;
    this.gnSecProfile = gnSecProfile;
    if (gnMaxLifetime_ms != null && (gnMaxLifetime_ms < 0 || gnMaxLifetime_ms > 6300000))
      throw new IllegalArgumentException ();
    this.gnMaxLifetime_ms = gnMaxLifetime_ms;
    if (gnRepInterval_ms != null && (gnRepInterval_ms < 0 || gnRepInterval_ms > 6300000))
      throw new IllegalArgumentException ();
    this.gnRepInterval_ms = gnRepInterval_ms;
    if (gnMaxRepTime_ms != null && (gnMaxRepTime_ms < 0 || gnMaxRepTime_ms > 6300000))
      throw new IllegalArgumentException ();
    this.gnMaxRepTime_ms = gnMaxRepTime_ms;
    if (gnMaxHopLimit < 0 || gnMaxHopLimit >= 256)
      throw new IllegalArgumentException ();
    this.gnMaxHopLimit = gnMaxHopLimit;
    if (gnTrafficClass == null)
      throw new IllegalArgumentException ();
    this.gnTrafficClass = gnTrafficClass;
    if (length < 0 || offset < 0)
      throw new IllegalArgumentException ();
    if (data == null && (length > 0 || offset > 0))
      throw new IllegalArgumentException ();
    if (data != null && offset + length > data.length)
      throw new IllegalArgumentException ();
    this.length = length;
    this.offset = offset;
    this.data = data;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BTP TYPE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final BtpType btpType;
  
  public final BtpType getBtpType ()
  {
    return this.btpType;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BTP SOURCE PORT [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Integer btpSrcPort;

  public final Integer getBtpSrcPort ()
  {
    return this.btpSrcPort;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BTP DESTINATION PORT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int btpDstPort;
  
  public final int getBtpDestinationPort ()
  {
    return this.btpDstPort;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BTP DESTINATION PORT INFO [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Integer btpDstPortInfo;

  public final Integer getBtpDstPortInfo ()
  {
    return this.btpDstPortInfo;
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN TRANSPORT TYPE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final GnTransportType gnTransportType;
  
  public final GnTransportType getGnTransportType ()
  {
    return this.gnTransportType;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN DESTINATION [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final GnDestination gnDst;

  public final GnDestination getGnDestination ()
  {
    return this.gnDst;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN COMMUNICATIONS PROFILE [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final GnCommunicationsProfile gnCommProfile;
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN SECURITY PROFILE [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  private final GnSecurityProfile gnSecProfile;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN MAXIMUM LIFETIME [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Integer gnMaxLifetime_ms;
  
  public final boolean hasMaxLifetime ()
  {
    return this.gnMaxLifetime_ms != null;
  }
  
  public final int getMaxLifeTime_ms ()
  {
    if (this.gnMaxLifetime_ms == null)
      return 0;
    else
      return this.gnMaxLifetime_ms;
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN REPITITION INTERVAL [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Integer gnRepInterval_ms;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN MAXIMUM REPITITION TIME [OPTIONAL]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Integer gnMaxRepTime_ms;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN MAXIMUM HOP LIMIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int gnMaxHopLimit;

  public final int getGnMaxHopLimit ()
  {
    return this.gnMaxHopLimit;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GN TRAFFIC CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final GnTrafficClass gnTrafficClass;

  public final GnTrafficClass getGnTrafficClass ()
  {
    return this.gnTrafficClass;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DATA / PAYLOAD
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The length of the payload to be taken from the data array (i.e., this is NOT necessarily the length of the array itself).
   * 
   */
  private final int length;
  
  public final int getLength ()
  {
    return this.length;
  }
  
  /** The offset into the data array at which the payload (data) starts.
   * 
   */
  private final int offset;
  
  public final int getOffset ()
  {
    return this.offset;
  }
  
  private final byte data[];
  
  public final byte[] getData ()
  {
    return this.data;
  }
  
}
