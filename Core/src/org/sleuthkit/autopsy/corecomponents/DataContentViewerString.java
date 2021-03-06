/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.corecomponents;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import org.sleuthkit.autopsy.coreutils.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.coreutils.StringExtract;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractResult;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractUnicodeTable;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractUnicodeTable.SCRIPT;
import org.sleuthkit.autopsy.datamodel.StringContent;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.FsContent;
import org.sleuthkit.datamodel.LayoutFile;
import org.sleuthkit.datamodel.TskException;

/**
 * Viewer displays strings extracted from contents.
 */
@ServiceProvider(service = DataContentViewer.class, position=2)
public class DataContentViewerString extends javax.swing.JPanel implements DataContentViewer {

    private static long currentOffset = 0;
    private static final long pageLength = 16384;
    private final byte[] data = new byte[(int)pageLength];
    private static int currentPage = 1;
    private Content dataSource;
    // for error handling
    private String className = this.getClass().toString();
    
    //string extract utility
    private final StringExtract stringExtract = new StringExtract();

    /** Creates new form DataContentViewerString */
    public DataContentViewerString() {
        initComponents();
        customizeComponents();
        this.resetComponent();
    }
    
    private void customizeComponents(){
        outputViewPane.setComponentPopupMenu(rightClickMenu);
        ActionListener actList = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                JMenuItem jmi = (JMenuItem) e.getSource();
                if(jmi.equals(copyMenuItem))
                    outputViewPane.copy();
                else if(jmi.equals(selectAllMenuItem))
                    outputViewPane.selectAll();
            }
        };
        copyMenuItem.addActionListener(actList);
        selectAllMenuItem.addActionListener(actList);
        
        List<SCRIPT> supportedScripts = StringExtract.getSupportedScripts();
        for (SCRIPT s : supportedScripts) {
            languageCombo.addItem(s);
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rightClickMenu = new javax.swing.JPopupMenu();
        copyMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputViewPane = new javax.swing.JTextPane();
        totalPageLabel = new javax.swing.JLabel();
        ofLabel = new javax.swing.JLabel();
        currentPageLabel = new javax.swing.JLabel();
        pageLabel = new javax.swing.JLabel();
        nextPageButton = new javax.swing.JButton();
        pageLabel2 = new javax.swing.JLabel();
        prevPageButton = new javax.swing.JButton();
        goToPageLabel = new javax.swing.JLabel();
        goToPageTextField = new javax.swing.JTextField();
        languageCombo = new javax.swing.JComboBox();
        languageLabel = new javax.swing.JLabel();

        copyMenuItem.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.copyMenuItem.text")); // NOI18N
        rightClickMenu.add(copyMenuItem);

        selectAllMenuItem.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.selectAllMenuItem.text")); // NOI18N
        rightClickMenu.add(selectAllMenuItem);

        outputViewPane.setEditable(false);
        outputViewPane.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        outputViewPane.setPreferredSize(new java.awt.Dimension(700, 400));
        jScrollPane1.setViewportView(outputViewPane);

        totalPageLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.totalPageLabel.text_1")); // NOI18N

        ofLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.ofLabel.text_1")); // NOI18N

        currentPageLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.currentPageLabel.text_1")); // NOI18N
        currentPageLabel.setMaximumSize(new java.awt.Dimension(18, 14));
        currentPageLabel.setMinimumSize(new java.awt.Dimension(18, 14));
        currentPageLabel.setPreferredSize(new java.awt.Dimension(18, 14));

        pageLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.pageLabel.text_1")); // NOI18N
        pageLabel.setMaximumSize(new java.awt.Dimension(33, 14));
        pageLabel.setMinimumSize(new java.awt.Dimension(33, 14));
        pageLabel.setPreferredSize(new java.awt.Dimension(33, 14));

        nextPageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_forward.png"))); // NOI18N
        nextPageButton.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.nextPageButton.text")); // NOI18N
        nextPageButton.setBorderPainted(false);
        nextPageButton.setContentAreaFilled(false);
        nextPageButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_forward_disabled.png"))); // NOI18N
        nextPageButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        nextPageButton.setPreferredSize(new java.awt.Dimension(55, 23));
        nextPageButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_forward_hover.png"))); // NOI18N
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        pageLabel2.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.pageLabel2.text")); // NOI18N
        pageLabel2.setMaximumSize(new java.awt.Dimension(29, 14));
        pageLabel2.setMinimumSize(new java.awt.Dimension(29, 14));
        pageLabel2.setPreferredSize(new java.awt.Dimension(29, 14));

        prevPageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_back.png"))); // NOI18N
        prevPageButton.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.prevPageButton.text")); // NOI18N
        prevPageButton.setBorderPainted(false);
        prevPageButton.setContentAreaFilled(false);
        prevPageButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_back_disabled.png"))); // NOI18N
        prevPageButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        prevPageButton.setPreferredSize(new java.awt.Dimension(55, 23));
        prevPageButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/corecomponents/btn_step_back_hover.png"))); // NOI18N
        prevPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevPageButtonActionPerformed(evt);
            }
        });

        goToPageLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.goToPageLabel.text")); // NOI18N

        goToPageTextField.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.goToPageTextField.text")); // NOI18N
        goToPageTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToPageTextFieldActionPerformed(evt);
            }
        });

        languageCombo.setToolTipText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.languageCombo.toolTipText")); // NOI18N
        languageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageComboActionPerformed(evt);
            }
        });

        languageLabel.setText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.languageLabel.text")); // NOI18N
        languageLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DataContentViewerString.class, "DataContentViewerString.languageLabel.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(currentPageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ofLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(totalPageLabel)
                .addGap(50, 50, 50)
                .addComponent(pageLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prevPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(nextPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(goToPageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(goToPageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(languageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(currentPageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ofLabel)
                        .addComponent(totalPageLabel))
                    .addComponent(pageLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nextPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prevPageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(goToPageLabel)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(goToPageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(languageLabel)))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void prevPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevPageButtonActionPerformed
        //@@@ this is part of the code dealing with the data viewer. could be copied/removed to implement the scrollbar
        currentOffset -= pageLength;
        currentPage = currentPage - 1;
        currentPageLabel.setText(Integer.toString(currentPage));
        setDataView(dataSource, currentOffset, false);
    }//GEN-LAST:event_prevPageButtonActionPerformed

    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageButtonActionPerformed
        //@@@ this is part of the code dealing with the data viewer. could be copied/removed to implement the scrollbar
        currentOffset += pageLength;
        currentPage = currentPage + 1;
        currentPageLabel.setText(Integer.toString(currentPage));
        setDataView(dataSource, currentOffset, false);
    }//GEN-LAST:event_nextPageButtonActionPerformed

    private void goToPageTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToPageTextFieldActionPerformed
        String pageNumberStr = goToPageTextField.getText();
        int pageNumber;
        int maxPage = Math.round((dataSource.getSize()-1) / pageLength) + 1;
        try {
            pageNumber = Integer.parseInt(pageNumberStr);
        } catch (NumberFormatException ex) {
            pageNumber = maxPage + 1;
        }
        if (pageNumber > maxPage || pageNumber < 1) {
            JOptionPane.showMessageDialog(this, "Please enter a valid page number between 1 and " + maxPage,
                    "Invalid page number", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentOffset = (pageNumber-1) * pageLength;
        currentPage = pageNumber;
        currentPageLabel.setText(Integer.toString(currentPage));
        setDataView(dataSource, currentOffset, false);
    }//GEN-LAST:event_goToPageTextFieldActionPerformed

    private void languageComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageComboActionPerformed

        if (dataSource != null) {
            setDataView(dataSource, currentOffset, false);
        }
    }//GEN-LAST:event_languageComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JLabel currentPageLabel;
    private javax.swing.JLabel goToPageLabel;
    private javax.swing.JTextField goToPageTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JButton nextPageButton;
    private javax.swing.JLabel ofLabel;
    private javax.swing.JTextPane outputViewPane;
    private javax.swing.JLabel pageLabel;
    private javax.swing.JLabel pageLabel2;
    private javax.swing.JButton prevPageButton;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JLabel totalPageLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Sets the DataView (The tabbed panel)
     *
     * @param dataSource  the content that want to be shown
     * @param offset      the starting offset
     * @param reset       whether to reset the dataView or not
     */
    public void setDataView(Content dataSource, long offset, boolean reset) {
        // change the cursor to "waiting cursor" for this operation
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            try {
                this.dataSource = dataSource;

                int bytesRead = 0;
                if (!reset && dataSource.getSize() > 0) {
                    bytesRead = dataSource.read(data, offset, pageLength); // read the data
                } 


                // set the data on the bottom and show it
                String text = "";
                Boolean setVisible = false;

                if (bytesRead > 0) {
                    //text = DataConversion.getString(data, bytesRead, 4);
                    final SCRIPT selScript = (SCRIPT) languageCombo.getSelectedItem();
                    stringExtract.setEnabledScript(selScript);
                    StringExtractResult res = stringExtract.extract(data, bytesRead, 0);
                    text = res.getText();
                    if (text.trim().isEmpty()) {
                        text = "(offset " + currentOffset + "-" + (currentOffset + pageLength) 
                                + " contains no text)";
                    }
  
                    setVisible = true;
                }

                // disable or enable the next button
                if (!reset && offset + pageLength < dataSource.getSize()) {
                    nextPageButton.setEnabled(true);
                } else {
                    nextPageButton.setEnabled(false);
                }

                if (offset == 0) {
                    prevPageButton.setEnabled(false);
                    currentPage = 1; // reset the page number
                } else {
                    prevPageButton.setEnabled(true);
                }

                if (setVisible) {
                    int totalPage = Math.round((dataSource.getSize()-1) / pageLength) + 1;
                    totalPageLabel.setText(Integer.toString(totalPage));
                    currentPageLabel.setText(Integer.toString(currentPage));
                    outputViewPane.setText(text); // set the output view
                    setComponentsVisibility(true); // shows the components that not needed
                } else {
                    // reset or hide the labels
                    totalPageLabel.setText("");
                    currentPageLabel.setText("");
                    outputViewPane.setText(""); // reset the output view
                    setComponentsVisibility(false); // hides the components that not needed
                }
                outputViewPane.moveCaretPosition(0);
            } catch (TskException ex) {
                Logger logger = Logger.getLogger(this.className);
                logger.log(Level.WARNING, "Error while trying to show the String content.", ex);
            }
        } finally {
            this.setCursor(null);
        }
    }

    /**
     * To set the visibility of specific components in this class.
     *
     * @param isVisible  whether to show or hide the specific components
     */
    private void setComponentsVisibility(boolean isVisible) {
        currentPageLabel.setVisible(isVisible);
        totalPageLabel.setVisible(isVisible);
        ofLabel.setVisible(isVisible);
        prevPageButton.setVisible(isVisible);
        nextPageButton.setVisible(isVisible);
        pageLabel.setVisible(isVisible);
        pageLabel2.setVisible(isVisible);
        goToPageTextField.setVisible(isVisible);
        goToPageLabel.setVisible(isVisible);
        languageCombo.setVisible(isVisible);
        languageLabel.setVisible(isVisible);
        
    }

    @Override
    public void setNode(Node selectedNode) {
        if(!isSupported(selectedNode)) {
            setDataView(null, 0, true);
            return;
        }
        if (selectedNode != null) {
            Lookup lookup = selectedNode.getLookup();
            Content content = lookup.lookup(Content.class);
            if (content != null) {
                this.setDataView(content, 0, false);
                return;
            }
            else{
                StringContent scontent = selectedNode.getLookup().lookup(StringContent.class);
                if(scontent != null){
                    this.setDataView(scontent);
                    return;
                }
            }
        }

        this.setDataView(null, 0, true);
    }

    @Override
    public String getTitle() {
        return "String View";
    }
    
    @Override
    public String getToolTip() {
        return "Displays ASCII strings extracted from the file.";
    }

    @Override
    public DataContentViewer getInstance() {
        return new DataContentViewerString();
    }

    @Override
    public void resetComponent() {
        // clear / reset the fields
        currentPage = 1;
        currentOffset = 0;
        this.dataSource = null;
        currentPageLabel.setText("");
        totalPageLabel.setText("");
        prevPageButton.setEnabled(false);
        nextPageButton.setEnabled(false);
        setComponentsVisibility(false); // hides the components that not needed
    }

    @Override
    public boolean isSupported(Node node) {
        if(node == null) {
            return false;
        }
        FsContent fsContent = node.getLookup().lookup(FsContent.class);
        LayoutFile lc = node.getLookup().lookup(LayoutFile.class);
        if(fsContent != null && fsContent.getSize() != 0)
            return true;
        if(lc != null && lc.getSize() != 0)
            return true;
        return false;
    }
    
    @Override
    public int isPreferred(Node node, boolean isSupported) {
        if(node != null && isSupported){
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    private void setDataView(StringContent dataSource) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
                this.dataSource = null;

                // set the data on the bottom and show it
                String text = dataSource.getString();

                nextPageButton.setEnabled(false);

                prevPageButton.setEnabled(false);
                currentPage = 1;

                int totalPage = 1;
                totalPageLabel.setText(Integer.toString(totalPage));
                currentPageLabel.setText(Integer.toString(currentPage));
                outputViewPane.setText(text); // set the output view
                setComponentsVisibility(true); // shows the components that not needed
                outputViewPane.moveCaretPosition(0);
        } finally {
            this.setCursor(null);
        }
    }
    
    /* Show the right click menu only if evt is the correct mouse event */
    private void maybeShowPopup(java.awt.event.MouseEvent evt){
        if(evt.isPopupTrigger()){
            rightClickMenu.setLocation(evt.getLocationOnScreen());
            rightClickMenu.setVisible(true);
            copyMenuItem.setEnabled(outputViewPane.getSelectedText() != null);
        }else
            rightClickMenu.setVisible(false);
    }
}
