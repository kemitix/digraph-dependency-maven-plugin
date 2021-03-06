package net.kemitix.dependency.digraph.maven.plugin.digraph;

import lombok.val;
import net.kemitix.dependency.digraph.maven.plugin.DotFileFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link EdgeElement}.
 *
 * @author pcampbell
 */
public class EdgeElementTest {

    @Mock
    private EdgeEndpoint head;

    @Mock
    private EdgeEndpoint tail;

    @Mock
    private DotFileFormat dotFileFormat;

    @Mock
    private EdgeEndpoint edgeEndpoint;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldRender() {
        //given
        val edgeElement = new EdgeElement(edgeEndpoint, edgeEndpoint, dotFileFormat);
        val expected = "rendered edge element";
        given(dotFileFormat.render(edgeElement)).willReturn(expected);
        //then
        assertThat(edgeElement.render()).isEqualTo(expected);
    }

    @Test
    public void shouldSetAndReturnHeadAndTail() {
        //when
        val edgeElement = new EdgeElement(tail, head, dotFileFormat);
        //then
        assertThat(edgeElement.getTail()).isEqualTo(tail);
        assertThat(edgeElement.getHead()).isEqualTo(head);
    }
}
