package io.xbmlz.jeditor;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class MainFrame extends JFrame {

    static final String PREFS_ROOT_PATH = "/jeditor";

    private static final String KEY_LAF = "laf";

    private static final String KEY_FONT_SIZE_INCR = "fontSizeIncr";

    private static final String KEY_WINDOW_BOUNDS = "windowBounds";

    private JMenuBar menuBar;

    private JMenu fileMenu;

    private JMenuItem newMenuItem;

    private JMenuItem openMenuItem;

    private JMenuItem saveMenuItem;

    private JMenuItem saveAsMenuItem;

    private JMenuItem printMenuItem;

    private JMenuItem exitMenuItem;

    private JMenu editMenu;

    private JMenuItem undoMenuItem;

    private JMenuItem redoMenuItem;

    private JMenuItem cutMenuItem;

    private JMenuItem copyMenuItem;

    private JMenuItem pasteMenuItem;

    private JMenuItem deleteMenuItem;

    private JMenu viewMenu;

    private JMenu themeMenuItem;

    private JMenuItem zoomInMenuItem;

    private JMenuItem zoomOutMenuItem;

    private JMenuItem resetZoomMenuItem;

    private FlatTabbedPane fileTabbedPane;

    private JButton addTabButton;

    private Preferences state;

    private EditorPane selectedEditorPane;

    private int tabIndex = 1;

    public MainFrame() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MainFrame.this.windowClosing();
            }
        });
        restoreState();
        restoreWindowBounds();
        setTitle("JEditor");
        setIconImages(FlatSVGUtils.createWindowIconImages("/icons/logo.svg"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        initComponents();
    }

    public static void launch() {
        Locale.setDefault(Locale.ENGLISH);
        System.setProperty("user.language", "en");
        SwingUtilities.invokeLater(() -> {
            FlatJetBrainsMonoFont.installLazy();
            FlatLaf.registerCustomDefaultsSource("themes");
            try {
                String laf = Preferences.userRoot().node(PREFS_ROOT_PATH).get(KEY_LAF, FlatLightLaf.class.getName());
                UIManager.setLookAndFeel(laf);
            } catch (Exception ex) {
                FlatLightLaf.setup();
            }
            FlatInspector.install("ctrl alt shift X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }


    private void initComponents() {

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
        newMenuItem.addActionListener(e -> newFile());
        fileMenu.add(newMenuItem);
        // open file
        openMenuItem = new JMenuItem("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openMenuItem.setMnemonic('O');
        openMenuItem.addActionListener(e -> openFile());
        fileMenu.add(openMenuItem);
        // save file
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.addActionListener(e -> saveFile());
        fileMenu.add(saveMenuItem);
        // save as file
        saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK));
        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.addActionListener(e -> saveAsFile());
        fileMenu.add(saveAsMenuItem);
        // separator
        fileMenu.addSeparator();
        // print file
        printMenuItem = new JMenuItem("Print");
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        printMenuItem.setMnemonic('P');
        printMenuItem.addActionListener(e -> printFile());
        fileMenu.add(printMenuItem);
        // separator
        fileMenu.addSeparator();
        // exit
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.addActionListener(e -> exit());
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // edit menu
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        // undo ctrl+z
        undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        undoMenuItem.setMnemonic('U');
        undoMenuItem.addActionListener(e -> undo());
        editMenu.add(undoMenuItem);
        // redo ctrl+shift+z
        redoMenuItem = new JMenuItem("Redo");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK));
        redoMenuItem.setMnemonic('R');
        redoMenuItem.addActionListener(e -> redo());
        editMenu.add(redoMenuItem);
        // separator
        editMenu.addSeparator();
        // cut ctrl+x
        cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.addActionListener(e -> cut());
        editMenu.add(cutMenuItem);
        // copy ctrl+c
        copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        copyMenuItem.setMnemonic('C');
        copyMenuItem.addActionListener(e -> copy());
        editMenu.add(copyMenuItem);
        // paste ctrl+v
        pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.addActionListener(e -> paste());
        editMenu.add(pasteMenuItem);
        // delete delete
        deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setMnemonic('D');
        deleteMenuItem.addActionListener(e -> delete());
        editMenu.add(deleteMenuItem);
        menuBar.add(editMenu);

        // view menu
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        themeMenuItem = new JMenu("Theme");
        themeMenuItem.setMnemonic('T');
        // theme
        ButtonGroup themeButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem flatLightMenuItem = new JRadioButtonMenuItem("Flat Light");
        flatLightMenuItem.setMnemonic('L');
        flatLightMenuItem.setSelected(true);
        flatLightMenuItem.addActionListener(e -> setTheme("Flat Light"));
        themeButtonGroup.add(flatLightMenuItem);
        themeMenuItem.add(flatLightMenuItem);
        JRadioButtonMenuItem flatDarkMenuItem = new JRadioButtonMenuItem("Flat Dark");
        flatDarkMenuItem.setMnemonic('D');
        flatDarkMenuItem.addActionListener(e -> setTheme("Flat Dark"));
        themeButtonGroup.add(flatDarkMenuItem);
        if( UIManager.getLookAndFeel() instanceof FlatDarkLaf )
            flatDarkMenuItem.setSelected( true );
        themeMenuItem.add(flatDarkMenuItem);
        viewMenu.add(themeMenuItem);

        // separator
        viewMenu.addSeparator();
        // zoom in ctrl+mouse wheel up or ctrl++
        zoomInMenuItem = new JMenuItem("Zoom In");
        zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        zoomInMenuItem.setMnemonic('I');
        zoomInMenuItem.addActionListener(e -> zoomIn());
        viewMenu.add(zoomInMenuItem);
        // zoom out ctrl+mouse wheel down or ctrl+-
        zoomOutMenuItem = new JMenuItem("Zoom Out");
        zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        zoomOutMenuItem.setMnemonic('O');
        zoomOutMenuItem.addActionListener(e -> zoomOut());
        viewMenu.add(zoomOutMenuItem);
        // reset zoom ctrl+0
        resetZoomMenuItem = new JMenuItem("Reset");
        resetZoomMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        resetZoomMenuItem.setMnemonic('R');
        resetZoomMenuItem.addActionListener(e -> resetZoom());
        viewMenu.add(resetZoomMenuItem);
        menuBar.add(viewMenu);

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
            removeTab(tabPane, tabIndex);
        });
        fileTabbedPane.addChangeListener(e -> selectedTabChanged());
        addTabButton = new JButton(new FlatSVGIcon("icons/add.svg"));
        addTabButton.setToolTipText("New File");
        addTabButton.addActionListener(e -> newFile());
        JToolBar trailingToolBar = new JToolBar();
        trailingToolBar.setFloatable(false);
        trailingToolBar.add(addTabButton);
        fileTabbedPane.setTrailingComponent(trailingToolBar);
        contentPane.add(fileTabbedPane, BorderLayout.CENTER);
        addEmptyTab();
    }

    private void setTheme(String light) {
        if (light.equals("Flat Light")) {
            applyLookAndFeel(FlatLightLaf.class.getName());
        } else {
            applyLookAndFeel(FlatDarkLaf.class.getName());
        }
    }

    private void applyLookAndFeel(String lafClassName) {
        if (UIManager.getLookAndFeel().getClass().getName().equals(lafClassName))
            return;
        try {
            UIManager.setLookAndFeel(lafClassName);
            FlatLaf.updateUI();
            for (EditorPane editorPane : getEditorPanes())
                editorPane.updateTheme();
            state.put(KEY_LAF, lafClassName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void windowClosing() {
        exit();
    }

    private void exit() {
        // TODO is saved
        saveWindowBounds();
        System.exit(0);
    }

    private void zoomIn() {
        applyFontSizeIncr(getFontSizeIncr() + 1);
    }

    private void zoomOut() {
        applyFontSizeIncr(getFontSizeIncr() - 1);
    }

    private void resetZoom() {
        applyFontSizeIncr(0);
    }

    private void delete() {
        selectedEditorPane.delete();
    }

    private void paste() {
        selectedEditorPane.paste();
    }

    private void copy() {
        selectedEditorPane.copy();
    }

    private void cut() {
        selectedEditorPane.cut();
    }

    private void redo() {
        selectedEditorPane.undo();
    }

    private void undo() {
        selectedEditorPane.undo();
    }

    private void printFile() {
        // TODO
    }

    private void newFile() {
        addEmptyTab();
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Open File");
        chooser.setApproveButtonText("Open");
        chooser.setApproveButtonToolTipText("Open File");
        chooser.setFileHidingEnabled(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            EditorPane editorPane = new EditorPane();
            try {
                editorPane.load(file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            addTab(file.getName(), editorPane, null, file.getAbsolutePath(), true);
        }
    }

    private void saveFile() {
        if (selectedEditorPane.getFile() != null)
            selectedEditorPane.save();
        else
            saveAsFile();
    }

    private void saveAsFile() {
        // save as
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Save As");
        chooser.setApproveButtonText("Save");
        chooser.setApproveButtonToolTipText("Save File");
        chooser.setFileHidingEnabled(true);
        // add all file types
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            selectedEditorPane.saveAs(file);
            fileTabbedPane.setTitleAt(fileTabbedPane.getSelectedIndex(), file.getName());
            fileTabbedPane.setToolTipTextAt(fileTabbedPane.getSelectedIndex(), file.getAbsolutePath());
        }
    }

    private void addEmptyTab() {
        String tabName = "Untitled " + tabIndex++;
        addTab(tabName, new EditorPane(), null, tabName, true);
    }

    private void removeTab(JTabbedPane tabPane, Integer tabIndex) {
        EditorPane editorPane = (EditorPane) tabPane.getComponentAt(tabIndex);
        System.out.println(editorPane.isDirty());
        if (editorPane.isDirty()) {
            int result = JOptionPane.showConfirmDialog(this, "Save " + tabPane.getTitleAt(tabIndex) + "?",
                    "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                if (editorPane.getFile() != null)
                    editorPane.save();
                else
                    saveAsFile();
            } else if (result == JOptionPane.NO_OPTION) {
                tabPane.remove(tabIndex);
                if (editorPane.getFile() == null) this.tabIndex--;
                if (tabPane.getTabCount() == 0) addEmptyTab();
            }
        } else {
            tabPane.remove(tabIndex);
            if (editorPane.getFile() == null) this.tabIndex--;
            if (tabPane.getTabCount() == 0) addEmptyTab();
        }
    }

    private void addTab(String tabName, EditorPane editorPane, Icon icon, String tip, boolean isSelect) {
        Supplier<String> titleFun = () -> (editorPane.isDirty() ? "* " : "") + tabName;
        editorPane.addPropertyChangeListener(EditorPane.DIRTY_PROPERTY, e -> {
            int index = fileTabbedPane.indexOfComponent(editorPane);
            if (index >= 0) {
                fileTabbedPane.setTitleAt(index, titleFun.get());
            }
        });
        fileTabbedPane.addTab(tabName, icon, editorPane, tip);
        if (isSelect) fileTabbedPane.setSelectedComponent(editorPane);
    }

    private void selectedTabChanged() {
        this.selectedEditorPane = (EditorPane) fileTabbedPane.getSelectedComponent();
    }

    private void applyFontSizeIncr(int sizeIncr) {
        if (sizeIncr < -5)
            sizeIncr = -5;
        if (sizeIncr == getFontSizeIncr())
            return;

        for (EditorPane editorPane : getEditorPanes())
            editorPane.updateFontSize(sizeIncr);
        state.putInt(KEY_FONT_SIZE_INCR, sizeIncr);
    }

    private EditorPane[] getEditorPanes() {
        EditorPane[] result = new EditorPane[fileTabbedPane.getTabCount()];
        for (int i = 0; i < result.length; i++)
            result[i] = (EditorPane) fileTabbedPane.getComponentAt(i);
        return result;
    }

    private int getFontSizeIncr() {
        return state.getInt(KEY_FONT_SIZE_INCR, 0);
    }

    private void restoreState() {
        state = Preferences.userRoot().node(PREFS_ROOT_PATH);
    }

    private void saveState() {

    }

    private void restoreWindowBounds() {
        String windowBoundsStr = state.get(KEY_WINDOW_BOUNDS, null);
        if (windowBoundsStr != null) {
            java.util.List<String> list = StringUtils.split(windowBoundsStr, ',');
            if (list.size() >= 4) {
                try {
                    int x = UIScale.scale(Integer.parseInt(list.get(0)));
                    int y = UIScale.scale(Integer.parseInt(list.get(1)));
                    int w = UIScale.scale(Integer.parseInt(list.get(2)));
                    int h = UIScale.scale(Integer.parseInt(list.get(3)));

                    // limit to screen size
                    GraphicsConfiguration gc = getGraphicsConfiguration();
                    if (gc != null) {
                        Rectangle screenBounds = gc.getBounds();
                        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                        Rectangle r = FlatUIUtils.subtractInsets(screenBounds, screenInsets);

                        w = Math.min(w, r.width);
                        h = Math.min(h, r.height);
                        x = Math.max(Math.min(x, r.width - w), r.x);
                        y = Math.max(Math.min(y, r.height - h), r.y);
                    }

                    setBounds(x, y, w, h);
                    return;
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }
        }

        // default window size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(UIScale.scale(800), screenSize.width),
                screenSize.height - UIScale.scale(100));
        setLocationRelativeTo(null);
    }

    private void saveWindowBounds() {
        Rectangle r = getBounds();
        int x = UIScale.unscale(r.x);
        int y = UIScale.unscale(r.y);
        int width = UIScale.unscale(r.width);
        int height = UIScale.unscale(r.height);
        state.put(KEY_WINDOW_BOUNDS, x + "," + y + ',' + width + ',' + height);
    }
}
