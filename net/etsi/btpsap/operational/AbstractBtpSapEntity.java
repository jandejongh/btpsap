package net.etsi.btpsap.operational;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 */
public abstract class AbstractBtpSapEntity
implements BtpSapEntity
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public AbstractBtpSapEntity (final String name)
  {
    this.name = name;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<Listener> listeners = new LinkedHashSet<> ();
  
  @Override
  public final synchronized void registerListener (final Listener listener)
  {
    if (listener == null)
      throw new IllegalArgumentException ();
    this.listeners.add (listener);
  }
  
  @Override
  public final synchronized void unregisterListener (final Listener listener)
  {
    if (listener == null)
      throw new IllegalArgumentException ();
    this.listeners.remove (listener);
  }
  
  @Override
  public final synchronized Set<Listener> getListeners ()
  {
    return new LinkedHashSet<> (this.listeners);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENER NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  protected final synchronized void fireChanged ()
  {
    for (final Listener listener : this.listeners)
      listener.changed (this);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private String name = null;
  
  @Override
  public final String getName ()
  {
    if (this.name != null)
      return this.name;
    else
      return super.toString ();
  }
  
  @Override
  public String toString ()
  {
    return getName ();
  }
  
  @Override
  public final void setName (final String name)
  {
    this.name = name;
    fireChanged ();
  }
  
}