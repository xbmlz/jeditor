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

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EditorPane extends JPanel implements HyperlinkListener {

    static final String DIRTY_PROPERTY = TextEditorPane.DIRTY_PROPERTY;

    private final JPanel editorPanel;

    private final RTextScrollPane scrollPane;

    private final SyntaxTextArea textArea;

    private final ErrorStrip errorStrip;

    private File file;

    public EditorPane() {
        super(new BorderLayout());
        textArea = new SyntaxTextArea();
        LanguageSupportFactory.get().register(textArea);
//        textArea.requestFocusInWindow();
//        textArea.setCaretPosition(0);
        textArea.setMarkOccurrences(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.addHyperlinkListener(this);
        textArea.addPropertyChangeListener(TextEditorPane.DIRTY_PROPERTY, evt -> {
            firePropertyChange(DIRTY_PROPERTY, evt.getOldValue(), evt.getNewValue());
        });
        ToolTipManager.sharedInstance().registerComponent(textArea);

        // create text area
        JLayer<SyntaxTextArea> overlay = new JLayer<>(textArea, new EditorOverlay());
        scrollPane = new RTextScrollPane(overlay);
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

        // TODO syntax
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

    void load(File file) throws IOException {
        this.file = file;
        // TODO custom charset
        textArea.load(FileLocation.create(file), StandardCharsets.UTF_8);
        setSyntaxStyle();
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

    void updateFontSize(int sizeIncr) {
        Font font = createEditorFont(sizeIncr);
        textArea.setFont(font);
        scrollPane.getGutter().setLineNumberFont(font);
    }

    File getFile() {
        return file;
    }

    public void setSyntaxStyle() {
        String fileName = file.getName();
        String suffix = fileName.substring(file.getName().lastIndexOf(".") + 1);
        String style;
        switch (suffix) {
            case "txt":
                style = "text/plain";
                break;
            case "pas":
                style = "text/delphi";
                break;
            case "go":
                style = "text/golang";
                break;
            case "js":
                style = "text/javascript";
                break;
            case "jsonc":
            case "json5":
                style = "text/json";
                break;
            case "kt":
                style = "text/kotlin";
                break;
            case "md":
                style = "text/markdown";
                break;
            case "py":
                style = "text/python";
                break;
            case "rb":
                style = "text/ruby";
                break;
            case "ts":
                style = "text/typescript";
                break;
            case "sh":
                style = "text/unix";
                break;
            default:
                style = "text/" + suffix;
                break;
        }
        textArea.setSyntaxEditingStyle(style);
    }


    /**
     * Called when a hypertext link is updated.
     *
     * @param e the event responsible for the update
     */
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
}
