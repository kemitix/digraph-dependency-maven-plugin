package net.kemitix.dependency.digraph.maven.plugin;

import lombok.val;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Tests for {@link DigraphService}.
 *
 * @author pcampbell
 */
public class DefaultDigraphServiceTest {

    private DefaultDigraphService digraphService;

    @Mock
    private DigraphMojo digraphMojo;

    @Mock
    private SourceDirectoryProvider directoryProvider;

    @Mock
    private SourceFileProvider fileProvider;

    @Mock
    private FileLoader fileLoader;

    @Mock
    private SourceFileAnalyser fileAnalyser;

    @Mock
    private ReportGenerator reportGenerator;

    @Mock
    private ReportWriter reportWriter;

    @Mock
    private NodePathGenerator nodePathGenerator;

    @Mock
    private InputStream inputStream;

    @Mock
    private DotFileFormatFactory dotFileFormatFactory;

    @Mock
    private Log log;

    private List<MavenProject> mavenProjects;

    private boolean includeTests;

    private String basePackage;

    private String format;

    @Mock
    private MavenProject project;

    @Mock
    private Build build;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        digraphService =
                new DefaultDigraphService(directoryProvider, fileProvider, fileLoader, fileAnalyser, reportGenerator,
                                          reportWriter, dotFileFormatFactory
                );
        given(digraphMojo.getLog()).willReturn(log);
        basePackage = "net.kemitix";
        mavenProjects = new ArrayList<>();
        format = "simple";
        includeTests = false;
        given(digraphMojo.getBasePackage()).willReturn(basePackage);
        given(digraphMojo.getProjects()).willReturn(mavenProjects);
        given(digraphMojo.isIncludeTests()).willReturn(includeTests);
        given(digraphMojo.isDebug()).willReturn(false);
        given(digraphMojo.getProject()).willReturn(project);
        given(project.getBuild()).willReturn(build);
        given(build.getDirectory()).willReturn("target");
    }

    @Test
    public void execute() {
        //when
        digraphService.execute(digraphMojo);
    }

    @Test
    public void executeWithDebug() {
        //given
        given(digraphMojo.isDebug()).willReturn(true);
        //when
        digraphService.execute(digraphMojo);
        //then
        then(digraphMojo).should()
                         .getLog();
    }

    @Test
    public void executeWithIOException() throws Exception {
        //given
        String message = "message";
        doThrow(new IOException(message)).when(reportWriter)
                                         .write(any(), any());
        //when
        digraphService.execute(digraphMojo);
        //then
        then(log).should()
                 .error(message);
    }

    @Test
    public void createTargetDirectory() throws Exception {
        //given
        val target = folder.newFolder();
        target.delete();
        assertThat(target).doesNotExist();
        given(build.getDirectory()).willReturn(target.getAbsolutePath());
        //when
        digraphService.execute(digraphMojo);
        //then
        assertThat(target).exists()
                          .isDirectory();
    }
}
