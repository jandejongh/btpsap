package net.etsi.btpsap.operational.client.udp.tno;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 */
public class UdpTnoTestClient
extends JFrame
{

  private static final Logger LOG = Logger.getLogger (UdpTnoTestClient.class.getName ());

  private final JTabbedPane tabbedPane;
  
  private void addConnectionWindow (final String hostString, final String portString)
  {
    if (hostString == null || hostString.trim ().isEmpty ())
    {
      JOptionPane.showMessageDialog (this, "Malformed host address!", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (portString == null || portString.trim ().isEmpty ())
    {
      JOptionPane.showMessageDialog (this, "Malformed port!", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    final int port;
    try
    {
      port = Integer.parseInt (portString);
    }
    catch (NumberFormatException e)
    {
      JOptionPane.showMessageDialog (this, "Port must be an integer!", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (port < 0 || port > 65535)
    {
      JOptionPane.showMessageDialog (this, "Illegal port " + portString + "!", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    this.tabbedPane.addTab (hostString + ":" + port, new ConnectionPanel (hostString, port));
  }
  
  public UdpTnoTestClient (final String title) throws HeadlessException
  {
    super (title);
    setMinimumSize (new Dimension (800, 600));
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    final JMenuBar menuBar = new JMenuBar ();
    final JMenu fileMenu = new JMenu ("File");
    menuBar.add (fileMenu);
    final JMenuItem openMenuItem = new JMenuItem ("Open");
    openMenuItem.addActionListener (new ActionListener ()
    {
      @Override
      public final void actionPerformed (final ActionEvent ae)
      {
        final JTextField hostField = new JTextField ();
        final JTextField portField = new JTextField ();
        portField.setText ("36095");
        final Object[] message =
        {
          "Host:", hostField,
          "Port:", portField,
        };
        final int option = JOptionPane.showConfirmDialog
          (UdpTnoTestClient.this, message, "Enter Address", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
          addConnectionWindow (hostField.getText (), portField.getText ());
      }
    });
    fileMenu.add (openMenuItem);
    fileMenu.addSeparator ();
    final JMenuItem exitMenuItem = new JMenuItem ("Exit");
    exitMenuItem.addActionListener (new ActionListener ()
    {
      @Override
      public final void actionPerformed (final ActionEvent ae)
      {
        System.exit (0);
      }
    });
    fileMenu.add (exitMenuItem);
    setJMenuBar (menuBar);
    this.tabbedPane = new JTabbedPane ();
    getContentPane ().add (this.tabbedPane, BorderLayout.CENTER);
    pack ();
    setLocationRelativeTo (null);
  }

  public static void main (final String[] args)
  {
//    SwingUtilities.invokeLater (new Runnable ()
//    {
//      @Override
//      public final void run ()
//      {
        try
        {
          Thread.sleep (10000L);
        }
        catch (Exception e) { }
        new UdpTnoTestClient ("BTP SAP - TEST GUI FOR UDP/TNO - V0.1").setVisible (true);        
//      }          
//    });
  }
  
  private class ConnectionPanel
  extends JPanel
  {
    
    private final JTextPane textPane;
    
    private final StyledDocument doc;
    
    private final Style docStyle;
    
    private final Socket clientSocket;
    
    private final InputStreamReader socketIn;
    
    private final PrintWriter socketOut;
    
    private void appendString (final String string)
    {
      try
      {
        this.doc.insertString (this.doc.getLength (), string, this.docStyle);      
      }
      catch (BadLocationException ble)
      {
        LOG.log (Level.WARNING, "Caught BadLocationException in ConnectionPanel (ignored)!");
      }      
    }
    
    public ConnectionPanel (final String hostString, final int port)
    {
      setLayout (new BorderLayout ());
      this.textPane = new JTextPane ();
      add (this.textPane, BorderLayout.CENTER);
      this.doc = this.textPane.getStyledDocument ();
      this.docStyle = this.textPane.addStyle (null, null);
      Socket clientSocket = null;
      InputStreamReader socketIn = null;
      PrintWriter socketOut = null;
      try
      {
        clientSocket = new Socket (hostString, port);
        socketIn = new InputStreamReader (clientSocket.getInputStream ());
        socketOut = new PrintWriter (clientSocket.getOutputStream ());
      }
      catch (IOException e)
      {
        clientSocket = null;
        socketIn = null;
        socketOut = null;
        LOG.log (Level.WARNING, "Opening connection to {0}:{1} failed: {2}!", new Object[] { hostString, port, e.getMessage () });
        JOptionPane.showMessageDialog (this, "Could not open client!", "Error", JOptionPane.ERROR_MESSAGE);        
        appendString ("Could not open client!");
      }
      this.clientSocket = clientSocket;
      this.socketIn = socketIn;
      this.socketOut = socketOut;
      if (this.clientSocket != null)
      {
        final JTextField guiInput = new JTextField (80);
        add (guiInput, BorderLayout.SOUTH);
        guiInput.addActionListener (new ActionListener ()
        {
          @Override
          public final void actionPerformed (final ActionEvent ae)
          {
            final String command = guiInput.getText ();
            StyleConstants.setForeground (ConnectionPanel.this.docStyle, Color.red);
            appendString (command + System.getProperty ("line.separator"));
            StyleConstants.setForeground (ConnectionPanel.this.docStyle, Color.black);
            ConnectionPanel.this.socketOut.println (command);
            ConnectionPanel.this.socketOut.flush ();
            guiInput.setText ("");
          }
        });
        new Thread (new Runnable ()
        {
          @Override
          public final void run ()
          {
            while (! Thread.interrupted ())
            {
              try
              {
                final char ch = (char) ConnectionPanel.this.socketIn.read ();
                SwingUtilities.invokeLater (new Runnable ()
                {
                  @Override
                  public final void run ()
                  {
                    appendString (Character.toString (ch));
                  }
                });
              }
              catch (IOException ioe)
              {
                LOG.log (Level.WARNING, "IOException while reading connection socket!");
              }
            }
          }
        }).start ();
      }
      pack ();
    }
    
  }
  
}
