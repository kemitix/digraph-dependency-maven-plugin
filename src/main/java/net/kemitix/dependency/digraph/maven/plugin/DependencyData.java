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

import net.kemitix.node.Node;
import org.apache.maven.plugin.logging.Log;

/**
 * Interface for storing package and class dependency data.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
interface DependencyData {

    /**
     * Records a dependency between the user class and the imported class.
     *
     * @param user     the package that is using the import
     * @param imported the package that contains the class that is being imported
     */
    public abstract void addDependency(String user, String imported);

    /**
     * Returns the base node.
     *
     * @return the base node
     */
    public abstract Node<PackageData> getBaseNode();

    /**
     * Log the statue of the dependency data.
     *
     * @param log the log to send the output
     */
    public abstract void debugLog(Log log);

    /**
     * Update names of all nodes.
     */
    public abstract void updateNames();
}
