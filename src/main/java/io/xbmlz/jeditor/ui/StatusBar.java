package io.xbmlz.jeditor.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class StatusBar extends JPanel {

    public StatusBar() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 0, gap 0, fillx, filly"));
        add(new JLabel("Text"), "cell 0 0 5 1");
    }
}
