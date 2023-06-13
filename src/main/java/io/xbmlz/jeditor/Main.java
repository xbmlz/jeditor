package io.xbmlz.jeditor;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import io.xbmlz.jeditor.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Hello world!
 */
public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            FlatJetBrainsMonoFont.installLazy();
//            System.setProperty("flatlaf.useWindowDecorations", "false");


            MainFrame frame = new MainFrame();
            frame.setPreferredSize(new Dimension(800, 600));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
