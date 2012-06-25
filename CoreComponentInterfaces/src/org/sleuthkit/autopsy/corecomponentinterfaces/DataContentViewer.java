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

package org.sleuthkit.autopsy.corecomponentinterfaces;

import java.awt.Component;
import org.openide.nodes.Node;

/**
 * Responsible for a tab in the {@link DataContent} component. Displays the
 * contents of the node passed to {@link setNode(Node)}.
 */
public interface DataContentViewer {
    /**
     * Sets the node to display in the viewer. When called with null, must
     * clear all references to previous nodes.
     */
    public void setNode(Node selectedNode);
    
    /**
     * Returns the title of this viewer. 
     */
    public String getTitle();
    
    /**
     * Returns a short description of this viewer to use as a tool tip for
     * its tab. 
     */
    public String getToolTip();

    /**
     * Get new DataContentViewer instance. (This method is weird. We use the
     * instance returned by the Lookup as a factory for the instances that
     * are actually used.)
     */
    // TODO: extract the factory method out into a seperate interface that is used for the Lookup.
    public DataContentViewer getInstance();
    
    /**
     * Get Component to display this DataContentViewer
     */
    public Component getComponent();

    /**
     * Resets the component in this viewer.
     */
    public void resetComponent();

    /**
     * Checks whether the given node is supported by the viewer
     * @param node Node to check for support
     * @return True if supported, else false
     */
    public boolean isSupported(Node node);
    
     /**
     * Checks whether the given viewer is preferred for the Node
     * @param node Node to check for preference
     * @param isSupported, true if the viewer is supported by the node
     * as determined by a previous check
     * @return an int (0-10) higher return means the viewer has higher priority
     * 0 means not supported
     * 1/2 means will display all supported
     * 3-10 are prioritized by Content viewer developer
     */
    public int isPreferred(Node node, boolean isSupported);

}
