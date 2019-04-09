# digraph-dependency-maven-plugin

Digraph generator for package dependencies within a project

Build Plugin to create a [DOT](https://en.wikipedia.org/wiki/DOT_(graph_description_language))
format diagram showing the dependencies between the packages in a project.

It works by scanning the source code and processing import statements.

Java parsing is handled using the [JavaParser](https://github.com/javaparser/javaparser)
project.

## Usage

Add the following build plugin to your pom.xml:

            <plugin>
                <groupId>net.kemitix</groupId>
                <artifactId>digraph-dependency-maven-plugin</artifactId>
                <version>${digraph.version}</version>
                <configuration>
                    <basePackage>com.example</basePackage>
                    <!-- <includeTests>false</includeTests> -->
                    <!-- <debug>true</debug> -->
                    <!-- <format>nested</format> -->
                    <!-- <exclude>string</exclude> -->
                    <!-- <include>string</include> -->
                </configuration>
            </plugin>

The `basePackage` is the only required parameter.

### basePackage

Only dependencies where both the using package and the package being used are
within the `basePackage` are included in the graph.

### includeTests

Whether or not to include the test sources in the graph. Default is `false`.

### debug

Whether to include a debug output. Currently that mean printing the list of
nested packages that were scanned. Default is `true`.

### format

Which style of digraph to create. Default is `nested` which attempts to cluster
packages within their parent package. The alternative is `simple`.

### exclude

Any package that matches the exclude string will be excluded.

### include

Only packages that match the include string will be included. Packages
that are used by or use such included packages directly will also be
included.
