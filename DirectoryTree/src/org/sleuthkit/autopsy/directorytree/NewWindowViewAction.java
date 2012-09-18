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

package org.sleuthkit.autopsy.directorytree;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;
import org.sleuthkit.autopsy.corecomponents.DataContentTopComponent;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.datamodel.DataConversion;
import org.sleuthkit.datamodel.Content;

/**
 * Opens new ContentViewer pane in a detached window
 */
public class NewWindowViewAction extends AbstractAction{

    private Node contentNode ;

    public NewWindowViewAction(String title, Node contentNode){
        super(title);
        this.contentNode = contentNode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Logger.noteAction(this.getClass());
        
        String name = "DataContent";
        Content c = contentNode.getLookup().lookup(Content.class);
        if (c != null) {
            String[] filePaths = ContentUtils.getDisplayPath(c);
            name = DataConversion.getformattedPath(filePaths, 0);
        }
        String s = contentNode.getLookup().lookup(String.class);
        if (s != null)
            name = s;

        DataContentTopComponent dctc = DataContentTopComponent.createUndocked(name, this.contentNode);

        Mode m = WindowManager.getDefault().findMode("output");
        m.dockInto(dctc);
        dctc.open();

        // Undocked it (right now, I do it by pressing the "Alt+Shift+D" to undock.
        // If there's a better way, change the code below..
        dctc.requestActive();
        KeyEvent evt = new KeyEvent(dctc, 401, System.currentTimeMillis(), 585, 68, 'D');
        WindowManager.getDefault().getMainWindow().dispatchEvent(evt);
    }

    

}
