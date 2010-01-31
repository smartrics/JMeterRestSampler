/*  Copyright 2009 Fabrizio Cannizzo
 *
 *  This file is part of JMeterRestSampler.
 *
 *  JMeterRestSampler (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  BSD License
 *
 *  You should have received a copy of the BSD License
 *  along with JMeterRestSampler.  If not, see <http://opensource.org/licenses/bsd-license.php>.
 *
 *  If you want to contact the author please see http://smartrics.blogspot.com
 */
package smartrics.jmeter.sampler.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.util.JMeterUtils;

public class SaveChartFilePanel extends HorizontalPanel implements ActionListener {
    private static final long serialVersionUID = -4969098309721273290L;

    JTextField filename = new JTextField(20);

    JLabel label = new JLabel(JMeterUtils.getResString("file_visualizer_filename")); //$NON-NLS-1$

    JButton browse = new JButton(JMeterUtils.getResString("browse")); //$NON-NLS-1$

    private static final String ACTION_BROWSE = "browse"; //$NON-NLS-1$
    private static final String ACTION_SAVE = "save"; //$NON-NLS-1$

    List<ChangeListener> listeners = new LinkedList<ChangeListener>();
    List<SaveGraphListener> sgListeners = new LinkedList<SaveGraphListener>();

    String title;

    String filetype;

    public SaveChartFilePanel(String title, String filetype) {
        this(null, title, filetype);
    }

    /**
     * Constructor for the FilePanel object.
     */
    public SaveChartFilePanel(ChangeListener l, String title, String filetype) {
        this.title = title;
        this.filetype = filetype;
        init();
        if (l != null)
            listeners.add(l);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void init() {
        setBorder(BorderFactory.createTitledBorder(title));
        add(label);
        add(Box.createHorizontalStrut(5));
        add(filename);
        add(Box.createHorizontalStrut(5));
        filename.addActionListener(this);
        add(browse);
        browse.setActionCommand(ACTION_BROWSE);
        browse.addActionListener(this);
        JButton save = new JButton("Save");
        save.setActionCommand(ACTION_SAVE);
        final SaveChartFilePanel t = this;
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("save")) {
                    if (filename != null && filename.getText() != null && !"".equals(filename.getText().trim())) {
                        t.fireSaveGraphPressed(filename.getText());
                    } else {
                        t.actionPerformed(e);
                    }
                }
            }
        });
        add(save);
    }

    private void fireSaveGraphPressed(String text) {
        Iterator<SaveGraphListener> iter = sgListeners.iterator();
        while (iter.hasNext()) {
            iter.next().saveGraphPressed(text);
        }
    }

    public void clearGui(){
        filename.setText("");
    }

    /**
     * Gets the filename attribute of the FilePanel object.
     * 
     * @return the filename value
     */
    public String getFilename() {
        return filename.getText();
    }

    /**
     * Sets the filename attribute of the FilePanel object.
     * 
     * @param f
     *            the new filename value
     */
    public void setFilename(String f) {
        filename.setText(f);
    }

    private void fireFileChanged() {
        Iterator<ChangeListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            iter.next().stateChanged(new ChangeEvent(this));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACTION_BROWSE)) {
            JFileChooser chooser;
            if(filetype == null){
                chooser = FileDialoger.promptToOpenFile();
            } else {
                chooser = FileDialoger.promptToOpenFile(new String[] { filetype });
            }
            if (chooser != null && chooser.getSelectedFile() != null) {
                filename.setText(chooser.getSelectedFile().getPath());
                fireFileChanged();
            }
        } else {
            fireFileChanged();
        }
    }

    public void addSaveGraphPressedListener(SaveGraphListener listener) {
        sgListeners.add(listener);
    }
}

