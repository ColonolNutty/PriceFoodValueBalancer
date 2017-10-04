package com.company.ui;

import com.company.CNLog;
import com.company.SettingsWriter;
import com.company.balancer.ValueBalancer;
import com.company.models.ConfigSettings;
import com.company.models.MessageBundle;
import com.company.models.RecipeCreatorSettings;
import com.company.ui.balancer.ConfigSettingsDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * User: Jack's Computer
 * Date: 09/27/2017
 * Time: 10:18 AM
 */
public class MainWindow {
    private JFrame _mainFrame;
    private ConfigSettings _configSettings;
    private RecipeCreatorSettings _recipeCreatorSettings;
    private ValueBalancer _valueBalancer;
    private CNLog _log;
    private SettingsWriter _settingsWriter;
    private OutputDisplay _outputDisplay;
    private ConfigSettingsDisplay _configSettingsDisplay;

    public MainWindow(ConfigSettings settings,
                      RecipeCreatorSettings recipeCreatorSettings,
                      SettingsWriter settingsWriter) {
        _configSettings = settings;
        _recipeCreatorSettings = recipeCreatorSettings;
        _settingsWriter = settingsWriter;
    }

    public void start() {
        setup();
        _mainFrame.setVisible(true);
    }

    private void setup() {
        if(_mainFrame != null) {
            return;
        }
        _mainFrame = new JFrame("Ingredient Balancer");
        _mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                _log.dispose();
                _mainFrame = null;
            }
        });
        _mainFrame.setLayout(new BorderLayout());
        _mainFrame.getContentPane().setBackground(Color.BLUE);
        JPanel mainPanel = setupMainPanel(_mainFrame.getSize());
        _mainFrame.add(mainPanel);
        _mainFrame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameDimensions = _mainFrame.getSize();
        int newX = screenSize.width/2 - frameDimensions.width/2;
        if(newX < 0) {
            newX = 0;
        }
        int newY = screenSize.height/2 - frameDimensions.height/2;
        if(newY < 0) {
            newY = 0;
        }
        _mainFrame.setLocation(newX, newY);
    }

    private JPanel setupMainPanel(Dimension size) {
        JPanel mainPanel = new JPanel();
        mainPanel.setSize(size);
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        _configSettingsDisplay = new ConfigSettingsDisplay();

        _outputDisplay = new OutputDisplay();
        JPanel outputDisplayPanel = _outputDisplay.get();
        if(_log != null) {
            _log.dispose();
        }
        _log = new CNLog(_outputDisplay, _configSettings);
        mainPanel.setVisible(true);
        JPanel settingsPanel = _configSettingsDisplay.setup(_configSettings, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JButton source = (JButton) e.getSource();
                source.setEnabled(false);
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            _log.clear();
                            _configSettingsDisplay.disable();
                            _configSettingsDisplay.updateConfigSettings(_configSettings);
                            _settingsWriter.write(_configSettings);
                            _valueBalancer = new ValueBalancer(_configSettings, _log);
                            _valueBalancer.run();
                            Hashtable<String, MessageBundle> messages = _log.getMessages();
                            _outputDisplay.updateTreeDisplay(messages);
                        }
                        catch(Exception e1) {
                            e1.printStackTrace();
                        }
                        finally {
                            source.setEnabled(true);
                            _configSettingsDisplay.enable();
                        }
                    }
                };
                thread.start();
            }
        });
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                .addComponent(settingsPanel)
                .addComponent(outputDisplayPanel)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(settingsPanel)
                .addComponent(outputDisplayPanel)
        );
        return mainPanel;
    }
}
