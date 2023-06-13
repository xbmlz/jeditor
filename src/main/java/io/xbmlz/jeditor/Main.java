package io.xbmlz.jeditor;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;

import javax.swing.*;
import java.awt.*;

/**
 * Hello world!
 */
public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            FlatJetBrainsMonoFont.installLazy();
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
            FlatLaf.registerCustomDefaultsSource( "io.xbmlz.jeditor" );
            FlatLightLaf.setup();
            MainFrame frame = new MainFrame();
            frame.setPreferredSize(new Dimension(800, 600));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
