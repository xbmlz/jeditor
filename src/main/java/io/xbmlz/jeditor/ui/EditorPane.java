package io.xbmlz.jeditor.ui;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.util.FontUtils;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class EditorPane extends JPanel {

    private final JPanel editorPanel;

    private final RTextScrollPane scrollPane;

    private final SyntaxTextArea textArea;

    private final ErrorStrip errorStrip;

    public EditorPane() {
        super(new BorderLayout());
        textArea = new SyntaxTextArea();
        textArea.setMarkOccurrences(true);

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

    private void updateTheme() {
        Font font = createEditorFont(0);

        textArea.setFont(font);
//        textArea.setBackground(UIManager.getColor("EditorPane.background"));
//        textArea.setCaretColor(UIManager.getColor("EditorPane.caretColor"));
//        textArea.setSelectionColor(UIManager.getColor("EditorPane.selectionBackground"));
//        textArea.setCurrentLineHighlightColor(UIManager.getColor("EditorPane.currentLineHighlight"));
//        textArea.setMarkAllHighlightColor(UIManager.getColor("EditorPane.markAllHighlightColor"));
//        textArea.setMarkOccurrencesColor(UIManager.getColor("EditorPane.markOccurrencesColor"));
//        textArea.setMatchedBracketBGColor(UIManager.getColor("EditorPane.matchedBracketBackground"));
//        textArea.setMatchedBracketBorderColor(UIManager.getColor("EditorPane.matchedBracketBorderColor"));
        textArea.setPaintMatchedBracketPair(true);
        textArea.setAnimateBracketMatching(false);
        textArea.addPropertyChangeListener(TextEditorPane.DIRTY_PROPERTY, evt -> {
            firePropertyChange(TextEditorPane.DIRTY_PROPERTY, evt.getOldValue(), evt.getNewValue());
        });

        // TODO syntax
//        textArea.setSyntaxScheme();

        // gutter
        Gutter gutter = scrollPane.getGutter();
//        gutter.setBackground(UIManager.getColor("EditorPane.gutter.background"));
//        gutter.setBorderColor(UIManager.getColor("EditorPane.gutter.borderColor"));
//        gutter.setLineNumberColor(UIManager.getColor("EditorPane.gutter.lineNumberColor"));
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

}
