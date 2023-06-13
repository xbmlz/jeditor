package io.xbmlz.jeditor.ui;

import javax.swing.*;

public class EditorPanel extends JTabbedPane {

    public EditorPanel() {
        initComponents();
    }

    private void initComponents() {
        addTab("FileTree", new JScrollPane(new Async));
    }
}
