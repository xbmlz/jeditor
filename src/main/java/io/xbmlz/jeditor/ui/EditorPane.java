package io.xbmlz.jeditor.ui;

import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class EditorPane extends JPanel {

    private final JPanel editorPanel;

    private final RTextScrollPane scrollPane;

    private final SyntaxTextArea textArea;

    public EditorPane() {
        super(new BorderLayout());
        textArea = new SyntaxTextArea();
        textArea.setMarkOccurrences(true);

        JLayer<SyntaxTextArea> overlay = new JLayer<>(textArea, new EditorOverlay());

        scrollPane = new RTextScrollPane(overlay);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setLineNumbersEnabled(true);

        editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(scrollPane);
        add(editorPanel, BorderLayout.CENTER);
    }
}
