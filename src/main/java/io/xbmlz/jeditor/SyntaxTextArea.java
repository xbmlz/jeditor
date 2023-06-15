package io.xbmlz.jeditor;

import org.fife.ui.rsyntaxtextarea.TextEditorPane;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class SyntaxTextArea extends TextEditorPane {

    public SyntaxTextArea() {
        setForeground(UIManager.getColor("TextArea.foreground"));
        // remove Ctrl+Tab and Ctrl+Shift+Tab focus traversal keys to allow tabbed pane to process them
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());
    }
}
