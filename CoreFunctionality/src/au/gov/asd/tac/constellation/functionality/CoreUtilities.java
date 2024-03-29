/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.BitSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author algol
 */
public class CoreUtilities {

    private CoreUtilities() {
    }

    /**
     * Gather a graph's selected vxIds into a BitSet.
     *
     * @param rg The graph.
     *
     * @return A BitSet where selected vertex ids in the graph are set.
     */
    public static BitSet selectedVertexBits(final GraphReadMethods rg) {
        final int selectedId = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        final int vxCount = rg.getVertexCount();
        final BitSet bs = new BitSet();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = rg.getVertex(position);

            if (rg.getBooleanValue(selectedId, vxId)) {
                bs.set(vxId);
            }
        }

        return bs;
    }

    public static Action findAction(final String category, final String name) {
        final FileObject actionFolder = FileUtil.getConfigFile("Actions/" + category);
        final FileObject[] actions = actionFolder.getChildren();
        for (FileObject fo : actions) {
            if (fo.getName().contains(name)) {
                try {
                    final DataObject dob = DataObject.find(fo);
                    final InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
                    if (ic != null) {
                        final Object instance = ic.instanceCreate();
                        if (instance instanceof Action) {
                            return (Action) instance;
                        }
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                    break;
                }
            }
        }

        return null;
    }

    /**
     * Add a PreferenceChangeListener to a specified preference node.
     * <p>
     * For example, to listen for font size changes, listen to
     * "org/netbeans/core/output2".
     *
     * @param preferenceNode The preference node to listen to.
     * @param pcl A PreferenceChangeListener
     *
     * @return True if the addPreferenceChangeListener() worked, false
     * otherwise.
     */
    public static boolean addPreferenceChangeListener(final String preferenceNode, final PreferenceChangeListener pcl) {
        try {
            Preferences p = NbPreferences.root();
            if (p.nodeExists(preferenceNode)) {
                p = p.node(preferenceNode);
                p.addPreferenceChangeListener(pcl);

                return true;
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    /**
     * Remove a PreferenceChangeListener from a specified preference node.
     *
     * @param preferenceNode The preference node being listened to.
     * @param pcl A PreferenceChangeListener
     *
     * @return True if the addPreferenceChangeListener() worked, false
     * otherwise.
     */
    public static boolean removePreferenceChangeListener(final String preferenceNode, final PreferenceChangeListener pcl) {
        try {
            Preferences p = NbPreferences.root();
            if (p.nodeExists(preferenceNode)) {
                p = p.node(preferenceNode);
                p.removePreferenceChangeListener(pcl);

                return true;
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    /**
     * Return whether or not to freeze the graph view
     *
     * @return True if the graph view should be frozen, False otherwise
     */
    public static boolean isGraphViewFrozen() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        return prefs.getBoolean(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW_DEFAULT);
    }

}
