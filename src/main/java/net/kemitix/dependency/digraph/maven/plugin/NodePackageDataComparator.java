package net.kemitix.dependency.digraph.maven.plugin;

import java.io.Serializable;
import java.util.Comparator;

import net.kemitix.node.Node;

/**
 * Comparator for sorting {@link PackageData} {@link Node}s.
 */
@SuppressWarnings("serial")
class NodePackageDataComparator
        implements Comparator<Node<PackageData>>, Serializable {

    NodePackageDataComparator() {
    }

    @Override
    public int compare(
            final Node<PackageData> o1, final Node<PackageData> o2) {
        return NodeHelper.getRequiredData(o1)
                         .getName()
                         .compareTo(NodeHelper.getRequiredData(o2).getName());
    }
}
