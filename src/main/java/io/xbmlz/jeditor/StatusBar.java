package io.xbmlz.jeditor;

import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class StatusBar extends JPanel {

    private JLabel fileTypeLabel;

    private JLabel endOfLineLabel;

    private JLabel encodingLabel;

    private JLabel fileInfoLabel;

    private JLabel cursorPositionLabel;


    public StatusBar() {
        initComponents();

    }

    private void initComponents() {
        Border border = BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor"));
        setBorder(border);
        setLayout(new MigLayout("insets 2", "[grow,fill][fill][fill][fill]fill", "[]")    );
        fileTypeLabel = new JLabel("Plain Text");
        endOfLineLabel = new JLabel("LF");
        encodingLabel = new JLabel("UTF-8");
        fileInfoLabel = new JLabel("Length 18, Lines 12");
        cursorPositionLabel = new JLabel("Ln 1, Col 1, Pos 0");

        add(fileTypeLabel, "cell 0 0");
        add(fileInfoLabel, "cell 1 0");
        add(cursorPositionLabel, "cell 2 0");
        add(endOfLineLabel, "cell 3 0");
        add(encodingLabel, "cell 4 0");
    }

    void setFileType(String type) {
        fileTypeLabel.setText(type);
    }
}
