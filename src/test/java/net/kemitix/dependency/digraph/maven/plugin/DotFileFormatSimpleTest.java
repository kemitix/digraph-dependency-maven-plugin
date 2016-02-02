package net.kemitix.dependency.digraph.maven.plugin;

import net.kemitix.node.Node;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

/**
 * Tests for {@link DotFileFormatSimple}.
 *
 * @author pcampbell
 */
public class DotFileFormatSimpleTest {

    /**
     * Class under test.
     */
    private DotFileFormat dotFileFormat;

    private DependencyData dependencyData;

    @Mock
    private NodePathGenerator nodePathGenerator;

    /**
     * Prepare each test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dependencyData = new NodeTreeDependencyData();
        dependencyData.setBasePackage("test");
        dotFileFormat = new DotFileFormatSimple(
                dependencyData.getBaseNode(), nodePathGenerator);
    }

    /**
     * Test that the intermediary node "test" is created.
     */
    @Test
    public void shouldCreateTestNode() {
        //given
        dependencyData.addDependency("test.nested", "test.other");
        //when
        Node<PackageData> baseNode = dependencyData.getBaseNode();
        //then
        assertThat(baseNode, is(not(nullValue())));
        assertThat(baseNode.getData().getName(), is("test"));
    }

    /**
     * Test that the report is created as expected.
     */
    @Test
    public void shouldGenerateReport() {
        //given
        dependencyData.addDependency("test.nested", "test.other");
        final Node<PackageData> baseNode = dependencyData.getBaseNode();

        doReturn("test").when(nodePathGenerator)
                .getPath(eq(baseNode), eq(baseNode), any(String.class));

        Node<PackageData> nestedNode = getChildNodeByName(baseNode, "nested");
        doReturn("nested").when(nodePathGenerator)
                .getPath(eq(nestedNode), eq(baseNode), any(String.class));

        Node<PackageData> otherNode = getChildNodeByName(baseNode, "other");
        doReturn("other").when(nodePathGenerator)
                .getPath(eq(otherNode), eq(baseNode), any(String.class));

        final String expected = "digraph{compound=true;node[shape=box]\n"
                + "\"nested\";\"other\";"
                + "\"nested\"->\"other\";"
                + "}";
        //when
        String report = dotFileFormat.renderReport();
        //then
        assertThat(report, is(expected));
    }

    /**
     * Test that the report only includes expected package when used package is
     * outside base package.
     */
    @Test
    public void shouldOnlyIncludeUsingPackage() {
        //given
        dependencyData.addDependency("test.nested", "tested.other");
        final Node<PackageData> baseNode = dependencyData.getBaseNode();

        doReturn("test").when(nodePathGenerator)
                .getPath(eq(baseNode), eq(baseNode), any(String.class));

        Node<PackageData> nestedNode = getChildNodeByName(baseNode, "nested");
        doReturn("nested").when(nodePathGenerator)
                .getPath(eq(nestedNode), eq(baseNode), any(String.class));

        final String expected = "digraph{compound=true;node[shape=box]\n"
                + "\"nested\";"
                + "}";
        //when
        String report = dotFileFormat.renderReport();
        //then
        assertThat(report, is(expected));
    }

    /**
     * Test that the report only includes expected package when using package is
     * outside base package.
     */
    @Test
    public void shouldOnlyIncludeUsedPackage() {
        //given
        dependencyData.addDependency("tested.nested", "test.other");
        final Node<PackageData> baseNode = dependencyData.getBaseNode();

        doReturn("test").when(nodePathGenerator)
                .getPath(eq(baseNode), eq(baseNode), any(String.class));

        Node<PackageData> otherNode = getChildNodeByName(baseNode, "other");
        doReturn("other").when(nodePathGenerator)
                .getPath(eq(otherNode), eq(baseNode), any(String.class));

        final String expected = "digraph{compound=true;node[shape=box]\n"
                + "\"other\";"
                + "}";
        //when
        String report = dotFileFormat.renderReport();
        //then
        assertThat(report, is(expected));
    }

    /**
     * Test that nested packages are included.
     */
    @Test
    public void shouldHandleNestedPackages() {
        //given
        dependencyData.addDependency("test.nested", "test.other");
        dependencyData.addDependency("test.nested", "test.other.more");
        dependencyData.addDependency("test.other", "test.yetmore");
        final Node<PackageData> baseNode = dependencyData.getBaseNode();

        doReturn("test").when(nodePathGenerator)
                .getPath(eq(baseNode), eq(baseNode), any(String.class));

        Node<PackageData> nestedNode = getChildNodeByName(baseNode, "nested");
        doReturn("nested").when(nodePathGenerator)
                .getPath(eq(nestedNode), eq(baseNode), any(String.class));

        Node<PackageData> yetmoreNode = getChildNodeByName(baseNode, "yetmore");
        doReturn("yetmore").when(nodePathGenerator)
                .getPath(eq(yetmoreNode), eq(baseNode), any(String.class));

        Node<PackageData> otherNode = getChildNodeByName(baseNode, "other");
        doReturn("other").when(nodePathGenerator)
                .getPath(eq(otherNode), eq(baseNode), any(String.class));

        Node<PackageData> moreNode = getChildNodeByName(otherNode, "more");
        doReturn("other.more").when(nodePathGenerator)
                .getPath(eq(moreNode), eq(baseNode), any(String.class));

        final String expected = "digraph{compound=true;node[shape=box]\n"
                + "\"nested\";\"other\";\"other.more\";\"yetmore\";"
                + "\"nested\"->\"other.more\";"
                + "\"nested\"->\"other\";"
                + "\"other\"->\"yetmore\";"
                + "}";
        //when
        String report = dotFileFormat.renderReport();
        //then
        assertThat(report, is(expected));
    }

    private Node<PackageData> getChildNodeByName(
            final Node<PackageData> baseNode,
            final String name) {
        final Optional<Node<PackageData>> nestedOptional
                = baseNode.getChild(new PackageData(name));
        if (!nestedOptional.isPresent()) {
            fail("Child node not found");
        }
        return nestedOptional.get();
    }
}