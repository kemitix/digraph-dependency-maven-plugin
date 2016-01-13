package net.kemitix.dependency.digraph.maven.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.NonNull;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Generates digraph.
 */
@Mojo(name = "digraph", aggregator = true)
public class DigraphMojo extends AbstractMojo {

    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    private List<MavenProject> projects;

    @Parameter(name = "includeTests", defaultValue = "false")
    private boolean includeTests;

    @NonNull
    @Parameter(name = "basePackage", required = true)
    private String basePackage;

    @Parameter(name = "debug", defaultValue = "true")
    private boolean debug;

    private final Injector injector;

    private final SourceDirectoryProvider directoryProvider;

    private final SourceFileProvider fileProvider;

    @Getter
    private final SourceFileVisitor fileVisitor;

    private final SourceFileAnalyser fileAnalyser;

    private final DependencyData dependencyData;

    private final ReportGenerator reportGenerator;

    private final ReportWriter reportWriter;

    @Getter
    private List<String> directories;

    /**
     * The file to write the report to.
     */
    private static final String REPORT_FILE = "target/digraph.dot";

    /**
     * Default constructor.
     */
    public DigraphMojo() {
        injector = Guice.createInjector(new DigraphModule(),
                new AbstractModule() {
            @Override
            protected void configure() {
                bind(DigraphMojo.class).toInstance(DigraphMojo.this);
            }
        });
        directoryProvider = injector.getInstance(SourceDirectoryProvider.class);
        fileProvider = injector.getInstance(SourceFileProvider.class);
        fileVisitor = injector.getInstance(SourceFileVisitor.class);
        fileAnalyser = injector.getInstance(SourceFileAnalyser.class);
        dependencyData = injector.getInstance(DependencyData.class);
        reportGenerator = injector.getInstance(ReportGenerator.class);
        reportWriter = injector.getInstance(ReportWriter.class);
    }

    @Override
    public void execute() {
        dependencyData.setBasePackage(basePackage);
        directories = directoryProvider.getDirectories(projects, includeTests);
        fileProvider.process(directories);
        final List<File> javaFiles = fileProvider.getJavaFiles();
        if (javaFiles != null) {
            javaFiles.forEach(fileAnalyser::analyse);
            if (debug) {
                dependencyData.debugLog(getLog());
            }
            try {
                reportWriter.write(reportGenerator.generate(
                        dependencyData.getBaseNode()), REPORT_FILE);
            } catch (IOException ex) {
                getLog().error(ex.toString());
            }
        }
    }

}
