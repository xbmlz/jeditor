package io.xbmlz.jeditor;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.*;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.fonts.roboto_mono.FlatRobotoMonoFont;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Year;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class MainFrame extends JFrame {

    static final String PREFS_ROOT_PATH = "/jeditor";

    private static final String KEY_LAF = "laf";

    private static final String KEY_FONT_SIZE_INCR = "fontSizeIncr";

    private static final String KEY_WINDOW_BOUNDS = "windowBounds";

    private JMenuItem exitMenuItem;

    private JMenuItem aboutMenuItem;

    private FlatTabbedPane fileTabbedPane;

    private JLabel languageLabel;

    private JLabel lineSeparatorLabel;

    private JLabel encodingLabel;

    private JLabel fileInfoLabel;

    private JLabel cursorPositionLabel;

    private Preferences state;

    private EditorPane selectedEditorPane;

    private JPanel statusBarPanel;

    private int emptyTabIndex = 1;

    public MainFrame(File file) {
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

        // macOS  (see https://www.formdev.com/flatlaf/macos/)
        if (SystemInfo.isMacOS) {
            // hide menu items that are in macOS application menu
            exitMenuItem.setVisible(false);
            aboutMenuItem.setVisible(false);

            if (SystemInfo.isMacFullWindowContentSupported) {
                // expand window content into window title bar and make title bar transparent
                getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
                getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);

                // hide window title
                if (SystemInfo.isJava_17_orLater)
                    getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
                else
                    setTitle(null);

            }

            // enable full screen mode for this window (for Java 8 - 10; not necessary for Java 11+)
            if (!SystemInfo.isJava_11_orLater)
                getRootPane().putClientProperty("apple.awt.fullscreenable", true);
        }

        // integrate into macOS screen menu
        FlatDesktop.setAboutHandler(this::about);
        FlatDesktop.setQuitHandler(response -> {
            if (!saveAll()) {
                response.cancelQuit();
                return;
            }
            saveWindowBounds();
            response.performQuit();
        });

        if (file != null) {
            EditorPane editorPane = new EditorPane();
            editorPane.load(file);
            addTab(file.getName(), editorPane, file.getAbsolutePath());
        } else {
            addEmptyTab();
        }
    }

    private boolean saveAll() {
        for (int i = 0; i < fileTabbedPane.getTabCount(); i++) {
            EditorPane editorPane = (EditorPane) fileTabbedPane.getComponentAt(i);
            if (!editorPane.save())
                return false;
        }
        return true;
    }

    public static void launch(String[] args) {
        File file = (args.length > 0)
                ? new File(args[0])
                : null;

        Locale.setDefault(Locale.ENGLISH);
        System.setProperty("user.language", "en");
        SwingUtilities.invokeLater(() -> {
            FlatInterFont.installLazy();
            FlatJetBrainsMonoFont.installLazy();
            FlatRobotoFont.installLazy();
            FlatRobotoMonoFont.installLazy();
            FlatLaf.registerCustomDefaultsSource("themes");
            try {
                String laf = Preferences.userRoot().node(PREFS_ROOT_PATH).get(KEY_LAF, FlatLightLaf.class.getName());
                UIManager.setLookAndFeel(laf);
            } catch (Exception ex) {
                FlatLightLaf.setup();
            }
            FlatInspector.install("ctrl alt shift X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
            MainFrame frame = new MainFrame(file);
            frame.setVisible(true);
        });
    }


    private void initComponents() {
        // content panel
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        // file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        // new file
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newMenuItem.setMnemonic('N');
        newMenuItem.addActionListener(e -> newFile());
        fileMenu.add(newMenuItem);
        // open file
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        openMenuItem.setMnemonic('O');
        openMenuItem.addActionListener(e -> openFile());
        fileMenu.add(openMenuItem);
        // save file
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.addActionListener(e -> saveFile());
        fileMenu.add(saveMenuItem);
        // save as file
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.addActionListener(e -> saveAsFile());
        fileMenu.add(saveAsMenuItem);
        // separator
        fileMenu.addSeparator();
        // print file
        JMenuItem printMenuItem = new JMenuItem("Print");
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        printMenuItem.setMnemonic('P');
        printMenuItem.addActionListener(e -> printFile());
        fileMenu.add(printMenuItem);
        // separator
        fileMenu.addSeparator();
        // exit
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.addActionListener(e -> exit());
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        // undo ctrl+z
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        undoMenuItem.setMnemonic('U');
        undoMenuItem.addActionListener(e -> undo());
        editMenu.add(undoMenuItem);
        // redo ctrl+shift+z
        JMenuItem redoMenuItem = new JMenuItem("Redo");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        redoMenuItem.setMnemonic('R');
        redoMenuItem.addActionListener(e -> redo());
        editMenu.add(redoMenuItem);
        // separator
        editMenu.addSeparator();
        // cut ctrl+x
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.addActionListener(e -> cut());
        editMenu.add(cutMenuItem);
        // copy ctrl+c
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        copyMenuItem.setMnemonic('C');
        copyMenuItem.addActionListener(e -> copy());
        editMenu.add(copyMenuItem);
        // paste ctrl+v
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.addActionListener(e -> paste());
        editMenu.add(pasteMenuItem);
        // delete delete
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setMnemonic('D');
        deleteMenuItem.addActionListener(e -> delete());
        editMenu.add(deleteMenuItem);
        // separator
        editMenu.addSeparator();
        // find and replace ctrl+f
        JMenuItem findAndReplaceMenuItem = new JMenuItem("Find/Replace");
        findAndReplaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        findAndReplaceMenuItem.setMnemonic('F');
        findAndReplaceMenuItem.addActionListener(e -> find());
        editMenu.add(findAndReplaceMenuItem);
        menuBar.add(editMenu);

        // view menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        JMenu themeMenuItem = new JMenu("Theme");
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
        if (UIManager.getLookAndFeel() instanceof FlatDarkLaf)
            flatDarkMenuItem.setSelected(true);
        themeMenuItem.add(flatDarkMenuItem);
        viewMenu.add(themeMenuItem);

        // separator
        viewMenu.addSeparator();
        // zoom in ctrl+mouse wheel up or ctrl++
        JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
        zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        zoomInMenuItem.setMnemonic('I');
        zoomInMenuItem.addActionListener(e -> zoomIn());
        viewMenu.add(zoomInMenuItem);
        // zoom out ctrl+mouse wheel down or ctrl+-
        JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
        zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        zoomOutMenuItem.setMnemonic('O');
        zoomOutMenuItem.addActionListener(e -> zoomOut());
        viewMenu.add(zoomOutMenuItem);
        // reset zoom ctrl+0
        JMenuItem resetZoomMenuItem = new JMenuItem("Reset");
        resetZoomMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        resetZoomMenuItem.setMnemonic('R');
        resetZoomMenuItem.addActionListener(e -> resetZoom());
        viewMenu.add(resetZoomMenuItem);
        menuBar.add(viewMenu);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        // about
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.addActionListener(e -> about());
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // tabbed pane
        fileTabbedPane = new FlatTabbedPane();
        fileTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        fileTabbedPane.setFocusable(false);
        fileTabbedPane.putClientProperty(TABBED_PANE_TAB_CLOSABLE, true);
        fileTabbedPane.putClientProperty(TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close");
        fileTabbedPane.putClientProperty(TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) this::removeTab);
        fileTabbedPane.addChangeListener(e -> selectedTabChanged());
        JButton addTabButton = new JButton(new FlatSVGIcon("icons/add.svg"));
        addTabButton.setToolTipText("New File");
        addTabButton.addActionListener(e -> newFile());
        JToolBar trailingToolBar = new JToolBar();
        trailingToolBar.setFloatable(false);
        trailingToolBar.add(addTabButton);
        fileTabbedPane.setTrailingComponent(trailingToolBar);
        contentPane.add(fileTabbedPane, BorderLayout.CENTER);

        // status bar
        statusBarPanel = new JPanel(new MigLayout("insets 4", "15[grow,fill][fill]20[fill]20[fill]15", "[]"));
        Border border = BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor"));
        statusBarPanel.setBorder(border);
        languageLabel = new JLabel("Plain Text");
        lineSeparatorLabel = new JLabel("CRLF");
        encodingLabel = new JLabel("UTF-8");
        fileInfoLabel = new JLabel("Length 0, Lines 0");
        cursorPositionLabel = new JLabel("Ln 0, Col 0, Pos 0");
        statusBarPanel.add(languageLabel, "cell 0 0");
        statusBarPanel.add(fileInfoLabel, "cell 1 0");
        statusBarPanel.add(cursorPositionLabel, "cell 2 0");
        statusBarPanel.add(lineSeparatorLabel, "cell 3 0");
        statusBarPanel.add(encodingLabel, "cell 4 0");
        contentPane.add(statusBarPanel, BorderLayout.SOUTH);

        // goto line:column
        cursorPositionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gotoLineColumn();
            }
        });

        // line separator menu
        buildLineSeparatorPopupMenu();
        // line separator
        buildLineSeparatorPopupMenu();
        // encoding
        buildEncodingPopupMenu();
        // language
        buildLanguagePopupMenu();
    }

    private void buildTabPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem closeMenuItem = new JMenuItem("Close");
        closeMenuItem.addActionListener(e -> removeTab(fileTabbedPane, fileTabbedPane.getSelectedIndex()));
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        popupMenu.add(closeMenuItem);
        JMenuItem closeOthersMenuItem = new JMenuItem("Close Others");
        closeOthersMenuItem.addActionListener(e -> removeOtherTabs());
        popupMenu.add(closeOthersMenuItem);
        JMenuItem closeAllMenuItem = new JMenuItem("Close All");
        closeAllMenuItem.addActionListener(e -> removeAllTabs());
        popupMenu.add(closeAllMenuItem);
        fileTabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(e);
                }
            }

            private void showPopupMenu(MouseEvent e) {
                int index = fileTabbedPane.indexAtLocation(e.getX(), e.getY());
                if (index != -1) {
                    fileTabbedPane.setSelectedIndex(index);
                    popupMenu.show(fileTabbedPane, e.getX(), e.getY());
                }
            }
        });
    }

    private void removeAllTabs() {
        int tabCount = fileTabbedPane.getTabCount();
        for (int i = tabCount - 1; i >= 0; i--) {
            removeTab(fileTabbedPane, i);
        }
    }

    private void removeOtherTabs() {
        int tabCount = fileTabbedPane.getTabCount();
        for (int i = tabCount - 1; i >= 0; i--) {
            if (i != fileTabbedPane.getSelectedIndex()) {
                removeTab(fileTabbedPane, i);
            }
        }
    }

    private void buildLineSeparatorPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu("Line Separator");
        ButtonGroup buttonGroup = new ButtonGroup();
        for (Map.Entry<String, String> entry : Constants.LINE_SEPARATOR_MAP.entrySet()) {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(entry.getKey());
            menuItem.addActionListener(e -> setLineSeparator(entry.getValue()));
            if (entry.getKey().equals("Windows (\\r\\n)")) menuItem.setSelected(true);
            buttonGroup.add(menuItem);
            popupMenu.add(menuItem);
        }
        addLabelPopupMenuMouseEvent(lineSeparatorLabel, popupMenu, statusBarPanel);
        lineSeparatorLabel.setComponentPopupMenu(popupMenu);
    }

    private void buildEncodingPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu("Encoding");
        ButtonGroup buttonGroup = new ButtonGroup();
        for (String encoding : Constants.ENCODINGS) {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(encoding);
            menuItem.addActionListener(e -> setEncoding(Charset.forName(encoding)));
            if (encoding.equals("UTF-8")) menuItem.setSelected(true);
            buttonGroup.add(menuItem);
            popupMenu.add(menuItem);
        }
        addLabelPopupMenuMouseEvent(encodingLabel, popupMenu, statusBarPanel);
        encodingLabel.setComponentPopupMenu(popupMenu);
    }

    private void buildLanguagePopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu("Language");
        ButtonGroup buttonGroup = new ButtonGroup();

        // 按照首字母分组
        Map<String, java.util.List<String>> map = new TreeMap<>();
        for (Map.Entry<String, String> entry : Constants.LANGUAGE_SYNTAX_MAP.entrySet()) {
            String key = entry.getKey().substring(0, 1);
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(entry.getKey());
        }
        // 添加菜单项
        for (Map.Entry<String, java.util.List<String>> entry : map.entrySet()) {
            JMenu menu = new JMenu(entry.getKey());
            for (String language : entry.getValue()) {
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(language);
                menuItem.addActionListener(e -> setLanguage(Constants.LANGUAGE_SYNTAX_MAP.get(language)));
                if (language.equals("Plain Text")) menuItem.setSelected(true);
                buttonGroup.add(menuItem);
                menu.add(menuItem);
            }
            popupMenu.add(menu);
        }
        addLabelPopupMenuMouseEvent(languageLabel, popupMenu, statusBarPanel);
        languageLabel.setComponentPopupMenu(popupMenu);
    }

    private void setLanguage(String val) {
        selectedEditorPane.setLanguage(val);
        languageLabel.setText(Utils.getMapFirstKey(Constants.LANGUAGE_SYNTAX_MAP, val));
    }

    private void setEncoding(Charset charset) {
        selectedEditorPane.setCharset(charset);
        encodingLabel.setText(charset.name());
    }

    private void gotoLineColumn() {
        int line = selectedEditorPane.getTextArea().getCaretLineNumber() + 1;
        int column = selectedEditorPane.getTextArea().getCaretOffsetFromLineStart() + 1;
        String res = (String) JOptionPane.showInputDialog(
                this,
                "[Line][:Column]",
                "Go to Line:Column",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                line + ":" + column);
        if (res != null)
            selectedEditorPane.gotoLineColumn(res);
    }

    private void find() {
        selectedEditorPane.showFindReplaceBar(true);
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
        selectedEditorPane.print();
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
            editorPane.load(file);
            addTab(file.getName(), editorPane, file.getAbsolutePath());
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

    private void setLineSeparator(String eof) {
        lineSeparatorLabel.setText(Utils.getMapFirstKey(Constants.LINE_SEPARATOR_MAP, eof));
        selectedEditorPane.setLineSeparator(eof);
    }

    private void addEmptyTab() {
        String tabName = "Untitled " + emptyTabIndex++;
        addTab(tabName, new EditorPane(), tabName);
    }

    private void removeTab(JTabbedPane tabPane, Integer tabIndex) {
        EditorPane editorPane = (EditorPane) tabPane.getComponentAt(tabIndex);
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
                if (editorPane.getFile() == null) this.emptyTabIndex--;
                if (tabPane.getTabCount() == 0) addEmptyTab();
            }
        } else {
            tabPane.remove(tabIndex);
            if (editorPane.getFile() == null) this.emptyTabIndex--;
            if (tabPane.getTabCount() == 0) addEmptyTab();
        }
    }

    private void addTab(String tabName, EditorPane editorPane, String tip) {
        Supplier<String> titleFun = () -> (editorPane.isDirty() ? "* " : "") + tabName;
        editorPane.addPropertyChangeListener(EditorPane.DIRTY_PROPERTY, e -> {
            int index = fileTabbedPane.indexOfComponent(editorPane);
            if (index >= 0) {
                fileTabbedPane.setTitleAt(index, titleFun.get());
            }
        });
        // document listener
        editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setFileInfoLabel(editorPane.getText().length(), editorPane.getLineCount());
            }
        });
        // caret listener
        editorPane.getTextArea().addCaretListener(e -> {
            int dot = e.getDot();
            int lineOfOffset = editorPane.getLineOfOffset(dot);
            int columnOfOffset = editorPane.getColumnOfOffset(dot, lineOfOffset);
            int pos = dot + 1;
            boolean isSelection = e.getMark() != dot;
            if (isSelection) pos = editorPane.getSelectText().length();
            setCursorPositionLabel(lineOfOffset, columnOfOffset, pos, isSelection);
        });
        // drag
        editorPane.getTextArea().setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                evt.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<File> droppedFiles = (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        EditorPane editorPane = new EditorPane();
                        editorPane.load(file);
                        addTab(file.getName(), editorPane, file.getAbsolutePath());

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        fileTabbedPane.addTab(tabName, null, editorPane, tip);
        fileTabbedPane.setSelectedComponent(editorPane);
        setLanguage(getLanguageSyntax(tabName));
        buildTabPopupMenu();
    }

    private String getLanguageSyntax(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return switch (suffix) {
            case "pas" -> "text/delphi";
            case "go" -> "text/golang";
            case "js" -> "text/javascript";
            case "jsonc", "json5" -> "text/json";
            case "kt" -> "text/kotlin";
            case "md" -> "text/markdown";
            case "py" -> "text/python";
            case "rb" -> "text/ruby";
            case "ts" -> "text/typescript";
            case "sh" -> "text/unix";
            default -> "text/plain";
        };
    }

    private void setFileInfoLabel(int length, int lines) {
        String info = String.format("Length %d, Lines %d", length, lines);
        fileInfoLabel.setText(info);
    }

    private void setCursorPositionLabel(int line, int column, int position, boolean isSelection) {
        String info = String.format("Ln %d, Col %d, Pos %d", line, column, position);
        if (isSelection) {
            info = String.format("Ln %d, Col %d, Sel %d", line, column, position);
        }
        cursorPositionLabel.setText(info);
    }

    private void selectedTabChanged() {
        this.selectedEditorPane = (EditorPane) fileTabbedPane.getSelectedComponent();
        String lineSeparator = System.getProperty("line.separator");
        String encoding = "UTF-8";
        if (selectedEditorPane.getFile() != null) {
            lineSeparator = this.selectedEditorPane.getLineSeparator();
            encoding = selectedEditorPane.getEncoding();
        }
        // set encoding
        setEncoding(Charset.forName(encoding));
        // set line separator
        setLineSeparator(lineSeparator);
        // set length and lines
        setFileInfoLabel(selectedEditorPane.getText().length(), selectedEditorPane.getLineCount());
        // set language
        setLanguage(getLanguageSyntax(fileTabbedPane.getTitleAt(fileTabbedPane.getSelectedIndex())));
        // set cursor position
        int dot = selectedEditorPane.getTextArea().getCaret().getDot();
        int lineOfOffset = selectedEditorPane.getLineOfOffset(dot);
        int columnOfOffset = selectedEditorPane.getColumnOfOffset(dot, lineOfOffset);
        int pos = dot + 1;
        boolean isSelection = selectedEditorPane.getTextArea().getCaret().getMark() != dot;
        if (isSelection) {
            pos = selectedEditorPane.getSelectText().length();
        }
        setCursorPositionLabel(lineOfOffset, columnOfOffset, pos, isSelection);
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

    private void about() {
        JLabel titleLabel = new JLabel("JEditor");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");

        String link = "https://github.com/xbmlz/jeditor";
        JLabel linkLabel = new JLabel("<html><a href=\"#\">" + link + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (IOException | URISyntaxException ex) {
                    JOptionPane.showMessageDialog(linkLabel,
                            "Failed to open '" + link + "' in browser.",
                            "About", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        JOptionPane.showMessageDialog(this,
                new Object[]{
                        titleLabel,
                        "A modern text editor for developers. Support Windows, Linux and Mac.",
                        " ",
                        "Copyright 2019-" + Year.now() + " xbmlz",
                        linkLabel,
                },
                "About", JOptionPane.PLAIN_MESSAGE);
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

    private void addLabelPopupMenuMouseEvent(JLabel label, JPopupMenu menu, JPanel container) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopupMenu();
            }

            private void showPopupMenu() {
                int x = label.getX() - menu.getPreferredSize().width / 2;
                int y = label.getY() - menu.getPreferredSize().height;
                menu.setMaximumSize(new Dimension(200, 50));
                menu.show(container, x, y);
            }
        });
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
