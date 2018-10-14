/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Paul Campbell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.kemitix.dependency.digraph.maven.plugin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import net.kemitix.dependency.digraph.maven.plugin.digraph.Digraph;
import net.kemitix.dependency.digraph.maven.plugin.digraph.EdgeElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.EdgeEndpoint;
import net.kemitix.dependency.digraph.maven.plugin.digraph.ElementContainer;
import net.kemitix.dependency.digraph.maven.plugin.digraph.GraphElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.NodeElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.NodeProperties;
import net.kemitix.dependency.digraph.maven.plugin.digraph.PropertyElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.Subgraph;
import net.kemitix.node.Node;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Abstract base for {@link DotFileFormat} implementations.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@Immutable
@SuppressWarnings({"methodcount", "classfanoutcomplexity"})
public abstract class AbstractDotFileFormat implements DotFileFormat {

    protected static final String CLOSE_BRACE = "]";

    private static final String DOUBLE_QUOTE = "\"";

    private static final String LINE = System.lineSeparator();

    @Getter(AccessLevel.PROTECTED)
    private final Node<PackageData> base;

    private final NodePathGenerator nodePathGenerator;

    private final NodePackageDataComparator nodePackageDataComparator;

    private final Map<Node<PackageData>, GraphElement> graphElements = new HashMap<>();

    private final GraphFilter graphFilter;

    /**
     * Constructor.
     *
     * @param base              The root node
     * @param nodePathGenerator The Node Path Generator
     * @param graphFilter       The Exclusion factory
     */
    AbstractDotFileFormat(
            final Node<PackageData> base, final NodePathGenerator nodePathGenerator, final GraphFilter graphFilter
                         ) {
        this.base = base;
        this.nodePathGenerator = nodePathGenerator;
        this.graphFilter = graphFilter;
        this.nodePackageDataComparator = new NodePackageDataComparator();
    }

    @Override
    public final String renderReport() {
        Digraph digraph = createDigraph();
        getNodeInjector().injectNodes(digraph, base);
        getUsageInjector().injectUsages(digraph, base);
        return render(digraph);
    }

    @Override
    public final String render(final Digraph digraph) {
        return "digraph{" + LINE + renderElements(digraph.getElements()) + "}" + LINE;
    }

    @Override
    public final String render(final NodeProperties nodeProperties) {
        return "node[" + renderProperties(nodeProperties.getProperties()) + CLOSE_BRACE;
    }

    @Override
    public final String render(final PropertyElement propertyElement) {
        return propertyElement.getName() + "=" + quoted(propertyElement.getValue());
    }

    /**
     * Returns the ID of the node in Cluster format.
     *
     * @param node the cluster node
     *
     * @return the cluster id
     */
    final String getClusterId(final Node<PackageData> node) {
        return getPath(node, "_");
    }

    /**
     * Returns the node path for the node, using the delimiter.
     *
     * @param headNode  The node to get the path for
     * @param delimiter The delimiter to separate each path element
     *
     * @return the path to of the node
     */
    private String getPath(
            final Node<PackageData> headNode, final String delimiter
                          ) {
        return nodePathGenerator.getPath(headNode, getBase(), delimiter);
    }

    /**
     * Creates a new Digraph.
     *
     * @return the Digraph
     */
    private Digraph createDigraph() {
        return new Digraph.Builder(this).build();
    }

    private GraphUsageInjector getUsageInjector() {
        return (container, node) -> node.getChildren()
                                        .stream()
                                        .sorted(nodePackageDataComparator)
                                        .forEach(injectUsagesByChildren(container));
    }

    private Consumer<Node<PackageData>> injectUsagesByChildren(
            final ElementContainer container
                                                              ) {
        return (Node<PackageData> childNode) -> {
            childNode.findData()
                     .ifPresent(data -> data.getUses()
                                            .stream()
                                            .filter(n -> n.isDescendantOf(getBase()))
                                            .sorted(nodePackageDataComparator)
                                            .map(usedNode -> createEdgeElement(childNode, usedNode))
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .forEach(container::add));
            getUsageInjector().injectUsages(container, childNode);
        };
    }

    /**
     * Searches for the NodeElement for leaf nodes, or a Subgraph, for the node.
     *
     * @param node The node to search for
     *
     * @return the EdgeEndpoint for the node
     */
    private EdgeEndpoint findEdgeEndpoint(final Node<PackageData> node) {
        if (node.getChildren()
                .isEmpty()) {
            return findNodeElement(node);
        }
        return findSubgraph(node);
    }

    /**
     * Creates a new NodeElement for the Node PackageData.
     *
     * @param node The node to create the NodeElement for
     *
     * @return the NodeElement for the node
     */
    private NodeElement createNodeElement(
            final Node<PackageData> node
                                         ) {
        return new NodeElement(node, getNodeId(node), node.getData()
                                                          .getName(), this);
    }

    /**
     * Creates a new EdgeElement linking the tail node to the head node.
     *
     * @param tail The node at the tail of the link
     * @param head The node at the head of the link
     *
     * @return the EdgeElement linking tail to head
     */
    private Optional<EdgeElement> createEdgeElement(
            final Node<PackageData> tail, final Node<PackageData> head
                                                   ) {
        if (graphFilter.filterNodes(tail) || graphFilter.filterNodes(head)) {
            return Optional.of(new EdgeElement(findEdgeEndpoint(tail), findEdgeEndpoint(head), this));
        }
        return Optional.empty();
    }

    /**
     * Finds the NodeElement for the given node in the graphElements, adding a
     * new one if one is not found.
     *
     * @param node The node to find the equivalent NodeElement for
     *
     * @return the NodeElement
     */
    private NodeElement findNodeElement(final Node<PackageData> node) {
        if (!graphElements.containsKey(node)) {
            graphElements.put(node, createNodeElement(node));
        }
        return (NodeElement) graphElements.get(node);
    }

    /**
     * Finds the Subgraph for the given node in the graphElements, adding a new
     * one if one is not found.
     *
     * @param node The node to find the equivalent Subgraph for
     *
     * @return the Subgraph
     */
    private Subgraph findSubgraph(final Node<PackageData> node) {
        if (!graphElements.containsKey(node)) {
            graphElements.put(node, createSubgraph(node));
        }
        return (Subgraph) graphElements.get(node);
    }

    private Subgraph createSubgraph(final Node<PackageData> node) {
        return new Subgraph(node, getClusterId(node), node.getData()
                                                          .getName(), this);
    }

    private GraphNodeInjector getNodeInjector() {
        return new GraphNodeInjector() {
            @Override
            public void injectNodes(final ElementContainer container, final Node<PackageData> node) {
                val children = node.getChildren();
                if (children.isEmpty()) {
                    container.add(findNodeElement(node));
                } else {
                    val subgraph = findSubgraph(node);
                    children.stream()
                            .sorted(nodePackageDataComparator)
                            .forEach(c -> this.injectNodes(subgraph, c));
                    container.add(subgraph);
                }
            }
        };
    }

    /**
     * Returns the ID of the node in dot node format.
     *
     * @param node the node
     *
     * @return the dotnode id
     */
    final String getNodeId(final Node<PackageData> node) {
        return getPath(node, ".");
    }

    /**
     * Renders the GraphElements.
     *
     * @param elements The GraphElements to be rendered
     *
     * @return the rendered GraphElements
     */
    final String renderElements(final Collection<GraphElement> elements) {
        return elements.stream()
                       .map(GraphElement::render)
                       .collect(Collectors.joining(LINE));
    }

    /**
     * Renders the PropertyElements.
     *
     * @param properties The PropertyElements to be rendered
     *
     * @return the rendered PropertyElements
     */
    private String renderProperties(final Set<PropertyElement> properties) {
        return properties.stream()
                         .map(GraphElement::render)
                         .collect(Collectors.joining(";" + LINE));
    }

    /**
     * Wraps the string in double quotes.
     *
     * @param text The string to be surrounded
     *
     * @return The string wrapped in double quotes
     */
    final String quoted(final String text) {
        return DOUBLE_QUOTE + text + DOUBLE_QUOTE;
    }

    /**
     * Functional Interface for rendering a node.
     */
    @FunctionalInterface
    interface GraphNodeInjector {

        /**
         * Adds the node and its child nodes to the container.
         *
         * @param container The container to add the node and its children to
         * @param node      The node to add to the container
         */
        public abstract void injectNodes(
                ElementContainer container, Node<PackageData> node
                        );

    }

    /**
     * Functional Interface for rendering a usage.
     */
    @FunctionalInterface
    interface GraphUsageInjector {

        /**
         * Adds uses of packages be the node's children to the container.
         *
         * @param container The container to add the usages to
         * @param node      The node to scan to find uses
         */
        public abstract void injectUsages(
                ElementContainer container, Node<PackageData> node
                         );

    }

}
