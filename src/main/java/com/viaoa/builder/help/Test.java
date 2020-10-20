package com.viaoa.builder.help;

import java.awt.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import javax.help.*;


public class Test extends JFrame {

    JMenuItem miHelp;
    JButton cmdHelp;
    JButton cmdCSHelp;

    HelpSet mainHS = null;
    HelpBroker mainHB;
    
    static final String helpsetLabel = "Help";

    public Test() {
        try {
            // NOTE: >>>> Put help.hs and all files into help.jar
            URL url = HelpSet.findHelpSet(null, "help.hs");
	        mainHS = new HelpSet(null, url);
            mainHB = mainHS.createHelpBroker();
	        mainHB.enableHelpKey(this.getRootPane(), "index", null);
        } 
        catch (Exception ee) {
	        System.out.println ("Help Set not found");
        }
        
        miHelp = new JMenuItem("Help");
        miHelp.setIcon(new ImageIcon(this.getClass().getResource("/icons/Help.gif")));
        miHelp.setToolTipText("Test Help");
    	miHelp.addActionListener(new CSH.DisplayHelpFromSource(mainHB));


        cmdHelp = new JButton();
        cmdHelp.setIcon(new ImageIcon(this.getClass().getResource("/icons/Help.gif")));
        cmdHelp.setToolTipText("Test Help");
       	mainHB.enableHelpOnButton(cmdHelp,"index", null);

        cmdCSHelp = new JButton();
        cmdCSHelp.setIcon(new ImageIcon(this.getClass().getResource("/icons/CSHelp.gif")));
        cmdCSHelp.setToolTipText("Context Sensitive Help");
    	cmdCSHelp.addActionListener(new CSH.DisplayHelpAfterTracking(mainHB));

// -- Set id for context sensitive help
// CSH.setHelpIDString(cmdCSHelp, "vetplan.B");

// -- Add F1 help to any component
// mainHB.enableHelp(txt, "id", mainHS);

    }

    
    public static void main(String[] args) {
        Test test = new Test();

        test.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        } );
        
        test.cmdHelp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0 ,false), "xx");
        test.cmdHelp.getActionMap().put("xx", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        
        test.setBounds(1500,  200,  800,  500);
        test.getContentPane().add(test.cmdHelp, BorderLayout.NORTH);
        test.getContentPane().add(test.cmdCSHelp, BorderLayout.SOUTH);
        // test.pack();
        test.setVisible(true);
    }
}

