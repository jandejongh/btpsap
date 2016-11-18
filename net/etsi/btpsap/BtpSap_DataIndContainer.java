package net.etsi.btpsap;

import net.etsi.btpsap.BtpSapTypes.GnCertificateId;
import net.etsi.btpsap.BtpSapTypes.GnDestination;
import net.etsi.btpsap.BtpSapTypes.GnPermissions;
import net.etsi.btpsap.BtpSapTypes.GnPositionVector;
import net.etsi.btpsap.BtpSapTypes.GnSecurityReport;
import net.etsi.btpsap.BtpSapTypes.GnTrafficClass;

public class BtpSap_DataIndContainer
{
  
  private final Integer btpSrcPort;
  
  public final Integer getBtpSrcPort ()
  {
    return this.btpSrcPort;
  }
  
  private final int btpDstPort;
  
  public final int getBtpDstPort ()
  {
    return this.btpDstPort;
  }
  
  private final Integer btpDstPortInfo;

  public final Integer getBtpDstPortInfo ()
  {
    return this.btpDstPortInfo;
  }
  
  private final GnDestination gnDstAddress;
  
  public final GnDestination getGnDstAddress ()
  {
    return this.gnDstAddress;
  }
 
  private final GnPositionVector gnSrcPV;
  
  public final GnPositionVector getGnSrcPV ()
  {
    return this.gnSrcPV;
  }
  
  private final GnSecurityReport gnSecReport;

  public final GnSecurityReport getGnSecReport ()
  {
    return this.gnSecReport;
  }

  private final GnCertificateId gnCertId;

  public final GnCertificateId getGnCertId ()
  {
    return this.gnCertId;
  }
  
  private final GnPermissions gnPermissions;

  public final GnPermissions getGnPermissions ()
  {
    return this.gnPermissions;
  }
  
  private final GnTrafficClass gnTrafficClass;

  public final GnTrafficClass getGnTrafficClass ()
  {
    return this.gnTrafficClass;
  }
  
  private final Integer gnRemLifetime_s;

  public final Integer getGnRemLifetime_s ()
  {
    return this.gnRemLifetime_s;
  }
  
  private final int length;

  public final int getLength ()
  {
    return this.length;
  }

  private final int offset;
  
  public final int getOffset ()
  {
    return this.offset;
  }

  public final byte[] getData ()
  {
    return this.data;
  }
  
  private final byte data[];

  public BtpSap_DataIndContainer
  (
    final Integer btpSrcPort,
    final int btpDstPort,
    final Integer btpDstPortInfo,
    final GnDestination gnDstAddress,
    final GnPositionVector gnSrcPV,
    final GnSecurityReport gnSecReport,
    final GnCertificateId gnCertId,
    final GnPermissions gnPermissions,
    final GnTrafficClass gnTrafficClass,
    final Integer gnRemLifetime_s,
    final int length,
    final int offset,
    final byte data[]
  )
  {
    // XXX Sanity...
    this.btpSrcPort = btpSrcPort;
    this.btpDstPort = btpDstPort;
    this.btpDstPortInfo = btpDstPortInfo;
    this.gnDstAddress = gnDstAddress;
    this.gnSrcPV = gnSrcPV;
    this.gnSecReport = gnSecReport;
    this.gnCertId = gnCertId;
    this.gnPermissions = gnPermissions;
    this.gnTrafficClass = gnTrafficClass;
    this.gnRemLifetime_s = gnRemLifetime_s;
    this.length = length;
    this.offset = offset;
    this.data = data;
  }

}
