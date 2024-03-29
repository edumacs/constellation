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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.CoreUtilities;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginExecutor;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.schema.analyticschema.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.schema.analyticschema.concept.SpatialConcept;
import au.gov.asd.tac.constellation.schema.analyticschema.concept.TemporalConcept;
import au.gov.asd.tac.constellation.visual.decorators.Decorators;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that builds a random graph using the preferential
 * attachment model.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("PreferentialAttachmentGraphBuilderPlugin=Preferential Attachment Graph Builder")
public class PreferentialAttachmentGraphBuilderPlugin extends SimpleEditPlugin {

    public static final String N_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "n");
    public static final String M_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "m");
    public static final String RANDOM_WEIGHTS_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "random_weights");
    public static final String NODE_TYPES_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "node_types");
    public static final String TRANSACTION_TYPES_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "transaction_types");

    @Override
    public String getDescription() {
        return "Builds a random graph using the preferential attachment model.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> n = IntegerParameterType.build(N_PARAMETER_ID);
        n.setName("Number of nodes");
        n.setDescription("The number of nodes on the graph");
        n.setIntegerValue(10);
        IntegerParameterType.setMinimum(n, 0);
        params.addParameter(n);

        final PluginParameter<IntegerParameterValue> m = IntegerParameterType.build(M_PARAMETER_ID);
        m.setName("Number of edges to attach");
        m.setDescription("The number of edges to attach to each node");
        m.setIntegerValue(1);
        IntegerParameterType.setMinimum(m, 0);
        params.addParameter(m);

        final PluginParameter<BooleanParameterValue> randomWeights = BooleanParameterType.build(RANDOM_WEIGHTS_PARAMETER_ID);
        randomWeights.setName("Random edge weight/direction");
        randomWeights.setDescription("Edges have a random number of transactions going in random directions");
        randomWeights.setBooleanValue(true);
        params.addParameter(randomWeights);

        final PluginParameter<MultiChoiceParameterValue> nodeTypes = MultiChoiceParameterType.build(NODE_TYPES_PARAMETER_ID);
        nodeTypes.setName("Node Types");
        nodeTypes.setDescription("Node types to add to the graph");
        params.addParameter(nodeTypes);

        final PluginParameter<MultiChoiceParameterValue> transactionTypes = MultiChoiceParameterType.build(TRANSACTION_TYPES_PARAMETER_ID);
        transactionTypes.setName("Transaction Types");
        transactionTypes.setDescription("Transaction types to add to the graph");
        params.addParameter(transactionTypes);

        return params;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> nAttributes = new ArrayList<>();
        final List<String> tAttributes = new ArrayList<>();
        final List<String> nChoices = new ArrayList<>();
        final List<String> tChoices = new ArrayList<>();
        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final List<SchemaVertexType> nodeTypes = GraphManager.getDefault().getActiveGraph().getSchema().getFactory().getRegisteredVertexTypes();

                for (int i = 0; i < nodeTypes.size(); i++) {
                    SchemaVertexType type = nodeTypes.get(i);
                    nAttributes.add(type.getName());
                }
                nAttributes.sort(String::compareTo);

                final List<SchemaTransactionType> transactionTypes = GraphManager.getDefault().getActiveGraph().getSchema().getFactory().getRegisteredTransactionTypes();
                for (int i = 0; i < transactionTypes.size(); i++) {
                    SchemaTransactionType type = transactionTypes.get(i);
                    tAttributes.add(type.getName());
                }
                tAttributes.sort(String::compareTo);
            } finally {
                readableGraph.release();
            }
            nChoices.add(nAttributes.get(0));
            tChoices.add(tAttributes.get(0));
        }

        if (parameters != null && parameters.getParameters() != null) {
            final PluginParameter nAttribute = parameters.getParameters().get(NODE_TYPES_PARAMETER_ID);
            final PluginParameter tAttribute = parameters.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);
            MultiChoiceParameterType.setOptions(nAttribute, nAttributes);
            MultiChoiceParameterType.setOptions(tAttribute, tAttributes);
            MultiChoiceParameterType.setChoices(nAttribute, nChoices);
            MultiChoiceParameterType.setChoices(tAttribute, tChoices);
        }
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        final Random r = new Random();

        final Map<String, PluginParameter<?>> params = parameters.getParameters();

        final int n = params.get(N_PARAMETER_ID).getIntegerValue();
        final int m = params.get(M_PARAMETER_ID).getIntegerValue();
        final boolean randomWeights = params.get(RANDOM_WEIGHTS_PARAMETER_ID).getBooleanValue();
        final List<String> nodeTypes = params.get(NODE_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();
        final List<String> transactionTypes = params.get(TRANSACTION_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();

        assert m >= 1 && m < n : String.format("[Number of edges to attach '%s' must be at least 1 and less than the number of nodes '%s']", m, n);

        // Random countries to put in the graph
        final List<String> countries = new ArrayList<>();
        countries.add("Australia");
        countries.add("Brazil");
        countries.add("China");
        countries.add("France");
        countries.add("Japan");
        countries.add("New Zealand");
        countries.add("South Africa");
        countries.add("United Arab Emirates");
        countries.add("United Kingdom");
        countries.add("United States");

        final int vxIdentifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vxTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);

        final int vxIsGoodAttr = graph.addAttribute(GraphElementType.VERTEX, "boolean", "isGood", null, false, null);
        final int vxCountryAttr = SpatialConcept.VertexAttribute.COUNTRY.ensure(graph);

        final int txIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int txTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        final int[] startVxIds = new int[m];

        final Decorators decorators;
        decorators = new Decorators(null, graph.getAttributeName(vxCountryAttr), null, graph.getAttributeName(vxIsGoodAttr));
        final int decoratorsAttr = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);
        graph.setObjectValue(decoratorsAttr, 0, decorators);

        int vx = 0;
        while (vx < m) {
            final int vxId = graph.addVertex();
            final String label = "Node_" + vxId;

            graph.setStringValue(vxIdentifierAttr, vxId, label);
            graph.setStringValue(vxTypeAttr, vxId, nodeTypes.get(r.nextInt(nodeTypes.size())));
            graph.setBooleanValue(vxIsGoodAttr, vxId, r.nextInt(10) == 0);
            graph.setStringValue(vxCountryAttr, vxId, countries.get(r.nextInt(countries.size())));
            if (graph.getSchema() != null) {
                graph.getSchema().completeVertex(graph, vxId);
            }

            startVxIds[vx] = vxId;
            vx++;

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }

        // Create transactions between the nodes.
        final Date d = new Date();
        final int fourDays = 4 * 24 * 60 * 60 * 1000;

        Set<Integer> destinations = new HashSet<>();
        for (int i : startVxIds) {
            destinations.add(i);
        }
        ArrayList<Integer> repeats = new ArrayList<>();
        while (vx < n) {
            final int vxId = graph.addVertex();
            final String label = "Node_" + vxId;

            graph.setStringValue(vxIdentifierAttr, vxId, label);
            graph.setStringValue(vxTypeAttr, vxId, nodeTypes.get(r.nextInt(nodeTypes.size())));
            graph.setBooleanValue(vxIsGoodAttr, vxId, r.nextInt(10) == 0);
            graph.setStringValue(vxCountryAttr, vxId, countries.get(r.nextInt(countries.size())));
            if (graph.getSchema() != null) {
                graph.getSchema().completeVertex(graph, vxId);
            }
            final int reciprocity = r.nextInt(3);
            for (int destination : destinations) {
                int numTimes = 1;
                if (randomWeights) {
                    numTimes = r.nextInt(1 + r.nextInt(100));
                }
                for (int i = 0; i < numTimes; i++) {
                    int sxId = vxId;
                    int dxId = destination;
                    if (randomWeights) {
                        switch (reciprocity) {
                            case 0: {
                                boolean random = r.nextBoolean();
                                if (random) {
                                    sxId = destination;
                                    dxId = vxId;
                                }
                                break;
                            }
                            case 1: {
                                int random = r.nextInt(5);
                                if (random == 0) {
                                    sxId = destination;
                                    dxId = vxId;
                                }
                                break;
                            }
                            default: {
                                int random = r.nextInt(5);
                                if (random != 0) {
                                    sxId = destination;
                                    dxId = vxId;
                                }
                                break;
                            }
                        }
                    }
                    final int e = graph.addTransaction(sxId, dxId, true);
                    graph.setLongValue(txDateTimeAttr, e, d.getTime() - r.nextInt(fourDays));
                    graph.setStringValue(txTypeAttr, e, transactionTypes.get(r.nextInt(transactionTypes.size())));
                    graph.setIntValue(txIdAttr, e, e);
                    if (graph.getSchema() != null) {
                        graph.getSchema().completeTransaction(graph, e);
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
                repeats.add(vxId);
                repeats.add(destination);
            }
            destinations = generateDestinations(repeats, m, r);
            vx++;

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }

        if (!CoreUtilities.isGraphViewFrozen()) {
            if (n < 10000) {
                // Do a trees layout.
                try {
                    PluginExecutor.startWith(ArrangementPluginRegistry.TREES).followedBy(CorePluginRegistry.RESET).executeNow(graph);
                } catch (PluginException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // Do a grid layout.
                try {
                    PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE).followedBy(CorePluginRegistry.RESET).executeNow(graph);
                } catch (PluginException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            PluginExecution.withPlugin(CorePluginRegistry.RESET).executeNow(graph);
        }

        interaction.setProgress(1, 0, "Completed successfully", true);
    }

    /**
     * Returns a random set of destinations
     *
     * @param repeats A list of possible destinations
     * @param nVxStart The number of destinations to return.
     * @param r Random
     *
     * @return A random set of destinations
     */
    private static Set<Integer> generateDestinations(ArrayList<Integer> repeats, int nVxStart, Random r) {
        final Set<Integer> destinations = new HashSet<>();
        while (destinations.size() < nVxStart) {
            destinations.add(repeats.get(r.nextInt(repeats.size())));
        }
        return destinations;
    }
}
