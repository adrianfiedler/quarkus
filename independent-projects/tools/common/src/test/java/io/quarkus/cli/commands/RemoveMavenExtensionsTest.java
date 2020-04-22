package io.quarkus.cli.commands;

import static java.util.Arrays.asList;

import io.quarkus.cli.commands.writer.FileProjectWriter;
import io.quarkus.maven.utilities.MojoUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.apache.maven.model.Model;

class RemoveMavenExtensionsTest extends AbstractRemoveExtensionsTest<Model> {

    @Override
    protected Model createProject() throws IOException, QuarkusCommandException {
        final File pom = getProjectPath().resolve("pom.xml").toFile();
        CreateProjectTest.delete(getProjectPath().toFile());
        new CreateProject(new FileProjectWriter(getProjectPath().toFile()), getPlatformDescriptor())
                .groupId("org.acme")
                .artifactId("remove-maven-extension-test")
                .version("0.0.1-SNAPSHOT")
                .execute();
        new AddExtensions(new FileProjectWriter(getProjectPath().toFile()), getPlatformDescriptor())
                .extensions(new HashSet<>(asList("quarkus-agroal", "quarkus-arc", " hibernate-validator",
                        "commons-io:commons-io:2.6", "quarkus-jdbc-postgresql", "quarkus-hibernate-search-elasticsearch",
                        "quarkus-vertx",
                        "quarkus-smallrye-openapi", "quarkus-smallrye-health", "quarkus-hibernate-orm-panache")))
                .execute();
        return MojoUtils.readPom(pom);
    }

    @Override
    protected Model readProject() throws IOException {
        return MojoUtils.readPom(getProjectPath().resolve("pom.xml").toFile());
    }

    @Override
    protected QuarkusCommandOutcome removeExtensions(List<String> extensions) throws IOException, QuarkusCommandException {
        return new RemoveExtensions(new FileProjectWriter(getProjectPath().toFile()), getPlatformDescriptor())
                .extensions(new HashSet<>(extensions))
                .execute();
    }

    @Override
    protected long countDependencyOccurrences(final Model project, final String groupId, final String artifactId,
            final String version) {
        return project.getDependencies().stream().filter(d -> d.getGroupId().equals(groupId) &&
                d.getArtifactId().equals(artifactId) &&
                Objects.equals(d.getVersion(), version)).count();
    }
}
