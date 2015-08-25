package com.sim.random;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

public class MenuActionExample extends JPanel {

  public JMenuBar menuBar;

  public JToolBar toolBar;

  public MenuActionExample() {
    super(true);

    // Create a menu bar and give it a bevel border.
    menuBar = new JMenuBar();
    menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

    // Create a menu and add it to the menu bar.
    JMenu menu = new JMenu("Menu");
    menuBar.add(menu);

    // Create a toolbar and give it an etched border.
    toolBar = new JToolBar();
    toolBar.setBorder(new EtchedBorder());

    // Instantiate a sample action with the NAME property of
    // "Download" and the appropriate SMALL_ICON property.
    SampleAction exampleAction = new SampleAction(null,
        new ImageIcon("icons/new_32.png"));

    // Finally, add the sample action to the menu and the toolbar.
    // These methods are no longer preferred:
    // menu.add(exampleAction);
    // toolBar.add(exampleAction);
    // Instead, you should create actual menu items and buttons:
    JMenuItem exampleItem = new JMenuItem(exampleAction);
    JButton exampleButton = new JButton(exampleAction);
    menu.add(exampleItem);
    toolBar.add(exampleButton);
  }

  class SampleAction extends AbstractAction {
    // This is our sample action. It must have an actionPerformed() method,
    // which is called when the action should be invoked.
    public SampleAction(String text, Icon icon) {
      super(text, icon);
    }

    public void actionPerformed(ActionEvent e) {
      System.out.println("Action [" + e.getActionCommand()
          + "] performed!");
    }
  }

  public static void main(String s[]) {
    MenuActionExample example = new MenuActionExample();
    JFrame frame = new JFrame("Action Example");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setJMenuBar(example.menuBar);
    frame.getContentPane().add(example.toolBar, BorderLayout.NORTH);
    frame.setSize(200, 200);
    frame.setVisible(true);
  }
}