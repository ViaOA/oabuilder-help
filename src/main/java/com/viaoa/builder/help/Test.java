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
            // qqqq Put help.hs and all files into help.jar as root files and change next line to help.hs
	        URL url = HelpSet.findHelpSet(null, "help\\help.hs");
	        mainHS = new HelpSet(null, url);
            mainHB = mainHS.createHelpBroker();
	        mainHB.enableHelpKey(this.getRootPane(), "index", null);
        } 
        catch (Exception ee) {
	        System.out.println ("Help Set not found");
        }
        
        miHelp = new JMenuItem("Help");
        miHelp.setIcon(new ImageIcon(this.getClass().getResource("icons/Help.gif")));
        miHelp.setToolTipText("VetPlan Help");
    	miHelp.addActionListener(new CSH.DisplayHelpFromSource(mainHB));


        cmdHelp = new JButton();
        cmdHelp.setIcon(new ImageIcon(this.getClass().getResource("icons/Help.gif")));
        cmdHelp.setToolTipText("VetPlan Help");
       	mainHB.enableHelpOnButton(cmdHelp,"index", null);

        cmdCSHelp = new JButton();
        cmdCSHelp.setIcon(new ImageIcon(this.getClass().getResource("icons/CSHelp.gif")));
        cmdCSHelp.setToolTipText("VetPlan Context Sensitive Help");
    	cmdCSHelp.addActionListener(new CSH.DisplayHelpAfterTracking(mainHB));

// -- Set id for context sensitive help
// CSH.setHelpIDString(cmdCSHelp, "vetplan.B");

// -- Add F1 help to any component
// mainHB.enableHelp(txt, "id", mainHS);

    }

    
    public static void main(String[] args) {
        Test test = new Test();
        test.getContentPane().add(test.cmdHelp, BorderLayout.NORTH);
        test.getContentPane().add(test.cmdCSHelp, BorderLayout.SOUTH);
        test.pack();
        test.setVisible(true);
    }
}

