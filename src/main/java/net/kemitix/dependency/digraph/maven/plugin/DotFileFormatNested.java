package net.kemitix.dependency.digraph.maven.plugin;

import net.kemitix.node.Node;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a dot file dependency report as nested clusters.
 *
 * @author pcampbell
 */
class DotFileFormatNested extends AbstractDotFileFormat {

    DotFileFormatNested(
            final Node<PackageData> base,
            final NodePathGenerator nodePathGenerator) {
        super(base, nodePathGenerator);
    }

    @Override
    String renderNode(final Node<PackageData> node) {
        final StringBuilder render = new StringBuilder();
        final String clusterId = getPath(node, "_");
        final String nodeId = getPath(node, ".");
        final String nodeName = node.getData().getName();
        final String headerFormat
                = "subgraph \"cluster{0}\"'{'"
                + "label=\"{1}\";\"{2}\"[label=\"{1}\",style=dotted]\n";
        render.append(MessageFormat.format(
                headerFormat, clusterId, nodeName, nodeId));
        node.getChildren().stream()
                .sorted(new NodePackageDataComparator())
                .forEach((Node<PackageData> child) -> {
                    if (child.getChildren().size() > 0) {
                        render.append(renderNode(child));
                    } else {
                        render.append(renderLeafPackage(child));
                    }
                });
        render.append("}\n");
        return render.toString();
    }

    @Override
    String renderUsages(final Node<PackageData> node) {
        final StringBuilder usages = new StringBuilder();
        node.getChildren().stream()
                .sorted(new NodePackageDataComparator())
                .forEach((Node<PackageData> childNode) -> {
                    childNode.getData().getUses().stream()
                            .filter((Node<PackageData> n)
                                    -> n.isChildOf(getBase()))
                            .sorted(new NodePackageDataComparator())
                            .forEach((Node<PackageData> n) -> {
                                usages.append(renderUsage(childNode, n));
                            });
                    usages.append(renderUsages(childNode));
                });
        return usages.toString();
    }

    private String renderUsage(
            final Node<PackageData> tailNode,
            final Node<PackageData> headNode) {
        List<String> attributes = new ArrayList<>();
        // if tail node has children, then add ltail attribute
        if (tailNode.getChildren().size() > 0) {
            attributes.add(String.format("ltail=\"cluster%s\",",
                    getPath(tailNode, "_")));
        }
        // if head node has children, then add lhead attribute
        if (headNode.getChildren().size() > 0) {
            attributes.add(String.format("lhead=\"cluster%s\",",
                    getPath(headNode, "_")));
        }
        final StringBuilder attributeTag = new StringBuilder();
        if (attributes.size() > 0) {
            attributeTag.append("[");
            attributes.forEach(attributeTag::append);
            attributeTag.append("]");
        }

        return String.format("\"%s\"->\"%s\"%s%n",
                getPath(tailNode, "."), getPath(headNode, "."), attributeTag);
    }

    private String renderLeafPackage(final Node<PackageData> node) {
        return String.format("\"%s\"[label=\"%s\"];",
                getPath(node, "."), node.getData().getName());
    }

}
