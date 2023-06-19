package io.xbmlz.jeditor;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.util.FontUtils;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class EditorPane extends JPanel implements HyperlinkListener {

    private final static Logger LOGGER = Logger.getLogger(EditorPane.class.getName());

    static final String DIRTY_PROPERTY = TextEditorPane.DIRTY_PROPERTY;

    private final JPanel editorPanel;

    private final RTextScrollPane scrollPane;

    private final SyntaxTextArea textArea;

    private final ErrorStrip errorStrip;

    private static boolean findReplaceVisible;

    private static SearchContext findReplaceContext;

    private FindReplaceBar findReplaceBar;

    private File file;

    public EditorPane() {
        super(new BorderLayout());
        textArea = new SyntaxTextArea();
        LanguageSupportFactory.get().register(textArea);
        textArea.requestFocusInWindow();
        textArea.setCaretPosition(0);
        textArea.setMarkOccurrences(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.addHyperlinkListener(this);
        textArea.addPropertyChangeListener(TextEditorPane.DIRTY_PROPERTY, evt -> {
            firePropertyChange(DIRTY_PROPERTY, evt.getOldValue(), evt.getNewValue());
        });
        ToolTipManager.sharedInstance().registerComponent(textArea);

        // create text area
        scrollPane = new RTextScrollPane(textArea, true);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setLineNumbersEnabled(true);

        // create error strip
        errorStrip = new ErrorStrip(textArea);

        // create editor panel
        editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(scrollPane);
        editorPanel.add(errorStrip, BorderLayout.LINE_END);

        add(editorPanel, BorderLayout.CENTER);
        updateTheme();
    }

    void updateTheme() {
        Font font = createEditorFont(0);
        textArea.setFont(font);
        textArea.setBackground(UIManager.getColor("EditorPane.background"));
        textArea.setCaretColor(UIManager.getColor("EditorPane.caretColor"));
        textArea.setSelectionColor(UIManager.getColor("EditorPane.selectionBackground"));
        textArea.setCurrentLineHighlightColor(UIManager.getColor("EditorPane.currentLineHighlight"));
        textArea.setMarkAllHighlightColor(UIManager.getColor("EditorPane.markAllHighlightColor"));
        textArea.setMarkOccurrencesColor(UIManager.getColor("EditorPane.markOccurrencesColor"));
        textArea.setMatchedBracketBGColor(UIManager.getColor("EditorPane.matchedBracketBackground"));
        textArea.setMatchedBracketBorderColor(UIManager.getColor("EditorPane.matchedBracketBorderColor"));
        textArea.setPaintMatchedBracketPair(true);
        textArea.setAnimateBracketMatching(false);

//        textArea.setSyntaxScheme();

        // gutter
        Gutter gutter = scrollPane.getGutter();
        gutter.setBackground(UIManager.getColor("EditorPane.gutter.background"));
        gutter.setBorderColor(UIManager.getColor("EditorPane.gutter.borderColor"));
        gutter.setLineNumberColor(UIManager.getColor("EditorPane.gutter.lineNumberColor"));
        gutter.setLineNumberFont(font);

        // error strip
        errorStrip.setCaretMarkerColor(UIManager.getColor("EditorPane.errorstrip.caretMarkerColor"));
    }

    private static Font createEditorFont(int sizeIncr) {
        int size = UIManager.getFont("defaultFont").getSize() + sizeIncr;
        Font font = FontUtils.getCompositeFont(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, size);
        if (isFallbackFont(font)) {
            Font defaultFont = RTextArea.getDefaultFont();
            font = defaultFont.deriveFont((float) size);
        }
        return font;
    }

    private static boolean isFallbackFont(Font font) {
        return Font.DIALOG.equalsIgnoreCase(font.getFamily());
    }

    private String getWindowTitle() {
        Window window = SwingUtilities.windowForComponent(this);
        return (window instanceof JFrame) ? ((JFrame) window).getTitle() : null;
    }

    boolean isDirty() {
        return textArea.isDirty();
    }

    void load(File file) {
        this.file = file;
        // TODO custom charset
        try {
            textArea.load(FileLocation.create(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load '" + file.getName() + "'\n\nReason: " + e.getMessage(),
                    getWindowTitle(), JOptionPane.WARNING_MESSAGE);
        }
    }

    boolean save() {
        try {
            if (textArea.isDirty())
                textArea.save();
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save '" + textArea.getFileName() + "'\n\nReason: " + ex.getMessage(),
                    getWindowTitle(), JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    boolean saveAs(File file) {
        try {
            textArea.saveAs(FileLocation.create(file));
            this.file = file;
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save '" + file.getName() + "'\n\nReason: " + ex.getMessage(),
                    getWindowTitle(), JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    void undo() {
        textArea.undoLastAction();
    }

    void redo() {
        textArea.redoLastAction();
    }

    void cut() {
        textArea.cut();
    }

    void copy() {
        textArea.copy();
    }

    void paste() {
        textArea.paste();
    }

    void delete() {
        textArea.replaceSelection("");
    }

    void print() {
        try {
            textArea.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to print '" + textArea.getFileName() + "'\n\nReason: " + e.getMessage(),
                    getWindowTitle(), JOptionPane.WARNING_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    void updateFontSize(int sizeIncr) {
        Font font = createEditorFont(sizeIncr);
        textArea.setFont(font);
        scrollPane.getGutter().setLineNumberFont(font);
    }

    String getText() {
        return textArea.getText();
    }

    int getLineCount() {
        return textArea.getLineCount();
    }

    int getLineOfOffset(int dot) {
        try {
            return textArea.getLineOfOffset(dot) + 1;
        } catch (BadLocationException e) {
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    int getColumnOfOffset(int dot, int lineOffset) {
        try {
            return dot - textArea.getLineStartOffset(lineOffset - 1) + 1;
        } catch (BadLocationException e) {
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    String getSelectText() {
        return textArea.getSelectedText();
    }

    File getFile() {
        return file;
    }

    Document getDocument() {
        return textArea.getDocument();
    }

    SyntaxTextArea getTextArea() {
        return textArea;
    }

    void setFileEncoding(String encoding) {
        textArea.setEncoding(encoding);
    }

    void setLineSeparator(String eol) {
        textArea.setLineSeparator(eol);
    }

    void showFindReplaceBar(boolean findEditorSelection) {
        if (findReplaceBar == null) {
            findReplaceBar = new FindReplaceBar(textArea);
            findReplaceBar.addPropertyChangeListener(FindReplaceBar.PROP_CLOSED, e -> {
                findReplaceVisible = false;
                textArea.requestFocusInWindow();
            });
            editorPanel.add(findReplaceBar, BorderLayout.SOUTH);
            editorPanel.revalidate();
        }

        findReplaceVisible = true;
        if (findReplaceContext == null)
            findReplaceContext = findReplaceBar.getSearchContext();
        else
            findReplaceBar.setSearchContext(findReplaceContext);

        findReplaceBar.setVisible(true);
        findReplaceBar.activate(findEditorSelection);
    }

    void hideFindReplaceBar() {
        if (findReplaceBar != null)
            findReplaceBar.setVisible(false);
    }

    void selected() {
        if (findReplaceVisible)
            showFindReplaceBar(false);
        else
            hideFindReplaceBar();
    }

    void gotoLineColumn(String res) {
        try {
            String[] arr = res.split(":");
            if (arr.length == 1)
                // only set line
                textArea.setCaretPosition(textArea.getLineStartOffset(Integer.parseInt(arr[0]) - 1));
            if (arr.length == 2)
                // set line and column
                textArea.setCaretPosition(textArea.getLineStartOffset(Integer.parseInt(arr[0]) - 1) + Integer.parseInt(arr[1]) - 1);
        } catch (BadLocationException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    String getLineSeparator() {
        return textArea.getLineSeparator().toString();
    }

    void setCharset(Charset charset) {
        textArea.setEncoding(charset.name());
    }

    String getEncoding() {
        return textArea.getEncoding();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL url = e.getURL();
            if (url == null) {
                UIManager.getLookAndFeel().provideErrorFeedback(null);
            } else {
                // open url in default browser
                try {
                    Desktop.getDesktop().browse(url.toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return textArea.requestFocusInWindow();
    }

    public void setLanguage(String s) {
        textArea.setSyntaxEditingStyle(s);
    }
}
