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

import net.kemitix.dependency.digraph.maven.plugin.digraph.Digraph;
import net.kemitix.dependency.digraph.maven.plugin.digraph.EdgeElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.NodeElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.NodeProperties;
import net.kemitix.dependency.digraph.maven.plugin.digraph.PropertyElement;
import net.kemitix.dependency.digraph.maven.plugin.digraph.Subgraph;

/**
 * Interface for report generators in a specific format.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public interface DotFileFormat {

    /**
     * Generates the report.
     *
     * @return the formatted report
     */
    public abstract String renderReport();

    /**
     * Render the digraph element as a string.
     *
     * @param digraph the digraph to render
     *
     * @return the rendered digraph
     */
    public abstract String render(Digraph digraph);

    /**
     * Render the edge element as a string.
     *
     * @param edgeElement the edge element to render
     *
     * @return the rendered edge element
     */
    public abstract String render(EdgeElement edgeElement);

    /**
     * Render the node element as a string.
     *
     * @param nodeElement the node element to render
     *
     * @return the rendered node element
     */
    public abstract String render(NodeElement nodeElement);

    /**
     * Render the node properties as a string.
     *
     * @param nodeProperties the node properties to render
     *
     * @return the rendered node properties
     */
    public abstract String render(NodeProperties nodeProperties);

    /**
     * Render the property element as a string.
     *
     * @param propertyElement the property element to render
     *
     * @return the rendered property element
     */
    public abstract String render(PropertyElement propertyElement);

    /**
     * Render the subgraph as a string.
     *
     * @param subgraph the subgraph to render
     *
     * @return the rendered subgraph
     */
    public abstract String render(Subgraph subgraph);
}
