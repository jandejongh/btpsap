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
