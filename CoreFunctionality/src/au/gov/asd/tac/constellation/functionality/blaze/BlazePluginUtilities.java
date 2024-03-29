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
package au.gov.asd.tac.constellation.functionality.blaze;

import static au.gov.asd.tac.constellation.graph.visual.blaze.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author algol
 */
public class BlazePluginUtilities {

    /**
     * A helper function to get vertex ids from both a BitSet and a
     * List<Integer>.
     * <p>
     * The VERTEX_IDS_PARAMETER_ID parameter was originally BitSet, but we want
     * to allow List<Integer>, so handle both.
     *
     * @param parameters Plugin paramters.
     *
     * @return A BitSet containing vertex ids, or null if the parameter wasn't
     * specified.
     */
    static BitSet verticesParam(final PluginParameters parameters) {
        // The VERTEX_IDS_PARAMETER_ID parameter was originally BitSet, but we want to allow List<Integer>, so handle both.
        //
        final Object vParam = parameters.getObjectValue(VERTEX_IDS_PARAMETER_ID);
        final BitSet vertices;
        if (vParam == null) {
            vertices = null;
        } else if (vParam.getClass() == BitSet.class) {
            vertices = (BitSet) vParam;
        } else {
            final List<Integer> vertexList = (List<Integer>) vParam;
            vertices = new BitSet(vertexList.size());
            vertexList.stream().forEach(ix -> vertices.set(ix));
        }

        return vertices;
    }

}
