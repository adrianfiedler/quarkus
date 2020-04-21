package io.quarkus.cli.commands;

import io.quarkus.cli.commands.file.GradleBuildFile;
import io.quarkus.cli.commands.writer.FileProjectWriter;
import io.quarkus.generators.BuildTool;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

class RemoveGradleExtensionsTest extends AbstractRemoveExtensionsTest<List<String>> {

    @Override
    protected List<String> createProject() throws IOException, QuarkusCommandException {
        CreateProjectTest.delete(getProjectPath().toFile());
        final FileProjectWriter writer = new FileProjectWriter(getProjectPath().toFile());
        new CreateProject(writer, getPlatformDescriptor())
                .buildFile(new GradleBuildFile(writer))
                .groupId("org.acme")
                .artifactId("add-gradle-extension-test")
                .version("0.0.1-SNAPSHOT")
                .execute();
        new AddExtensions(new FileProjectWriter(getProjectPath().toFile()), BuildTool.GRADLE, getPlatformDescriptor())
                .extensions(new HashSet<>(asList("quarkus-agroal", "quarkus-arc", " hibernate-validator",
                        "commons-io:commons-io:2.6", "quarkus-jdbc-postgresql", "quarkus-hibernate-search-elasticsearch", "quarkus-vertx")))
                .execute();
        return readProject();
    }

    @Override
    protected List<String> readProject() throws IOException {
        return Files.readAllLines(getProjectPath().resolve("build.gradle"));
    }

    @Override
    protected QuarkusCommandOutcome removeExtensions(final List<String> extensions) throws IOException, QuarkusCommandException {
        return new RemoveExtensions(new FileProjectWriter(getProjectPath().toFile()), BuildTool.GRADLE, getPlatformDescriptor())
                .extensions(new HashSet<>(extensions))
                .execute();
    }

    @Override
    protected long countDependencyOccurrences(final List<String> buildFile, final String groupId, final String artifactId,
                                              final String version) {
        return buildFile.stream()
                .filter(d -> d.equals(getBuildFileDependencyString(groupId, artifactId, version)))
                .count();
    }

    private static String getBuildFileDependencyString(final String groupId, final String artifactId, final String version) {
        final String versionPart = version != null ? ":" + version : "";
        return "    implementation '" + groupId + ":" + artifactId + versionPart + "'";
    }
}
