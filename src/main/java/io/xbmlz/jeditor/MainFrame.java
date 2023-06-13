package io.xbmlz.jeditor;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class MainFrame extends JFrame {

    private JMenuBar menuBar;

    private JMenu fileMenu;

    private JMenuItem newMenuItem;

    private FlatTabbedPane fileTabbedPane;

    private JButton addTabButton;

    private int tabIndex = 1;

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("JEditor");
        setIconImages(FlatSVGUtils.createWindowIconImages("/io/xbmlz/jeditor/icons/logo.svg"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // content panel
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        // menu bar
        menuBar = new JMenuBar();
        // file menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        // new file
        newMenuItem = new JMenuItem("New");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        newMenuItem.setMnemonic('N');
        newMenuItem.addActionListener(e -> newFileAction());
        fileMenu.add(newMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        StatusBar statusBar = new StatusBar();
        contentPane.add(statusBar, BorderLayout.SOUTH);

        // tabbed pane
        fileTabbedPane = new FlatTabbedPane();
        fileTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        fileTabbedPane.setFocusable(false);
        fileTabbedPane.putClientProperty(TABBED_PANE_TAB_CLOSABLE, true);
        fileTabbedPane.putClientProperty(TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close");
        fileTabbedPane.putClientProperty(TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabPane, tabIndex) -> {
            tabCloseCallback(tabPane, tabIndex);
        });
        fileTabbedPane.addChangeListener(e -> selectedTabChanged());
        addTabButton = new JButton(new FlatSVGIcon("icons/add.svg"));
        addTabButton.setToolTipText("New File");
        addTabButton.addActionListener(e -> newFileAction());
        JToolBar trailingToolBar = new JToolBar();
        trailingToolBar.setFloatable(false);
        trailingToolBar.add(addTabButton);
        fileTabbedPane.setTrailingComponent(trailingToolBar);
        contentPane.add(fileTabbedPane, BorderLayout.CENTER);
        addEmptyTab();
    }

    private void tabCloseCallback(JTabbedPane tabPane, Integer tabIndex) {
        System.out.println("tabCloseCallback");
    }

    private void newFileAction() {
        addEmptyTab();
    }

    private void addEmptyTab() {
        addTab("Untitled " + tabIndex++, null);
    }

    private void addTab(String tabName, String filePath) {
        // TODO
        fileTabbedPane.addTab(tabName, new EditorPane());
        // focus the new tab
        fileTabbedPane.setSelectedIndex(fileTabbedPane.getTabCount() - 1);
    }

    private void selectedTabChanged() {
    }
}
