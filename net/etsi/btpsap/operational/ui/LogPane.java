package net.etsi.btpsap.operational.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 *
 */
public class LogPane
extends JPanel
{

  public LogPane ()
  {
    setLayout (new BorderLayout ());
    this.jtextpane = new JTextPane ();
    add (new JScrollPane (this.jtextpane), BorderLayout.CENTER);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TEXT PANE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final JTextPane jtextpane;
  
}
