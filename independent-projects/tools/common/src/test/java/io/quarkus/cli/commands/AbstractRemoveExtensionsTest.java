package io.quarkus.cli.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

abstract class AbstractRemoveExtensionsTest<T> extends PlatformAwareTestBase {

    private final Path projectPath = Paths.get("target/extensions-test");

    public Path getProjectPath() {
        return projectPath;
    }

    @Test
    void removeSomeValidExtensions() throws Exception {
        createProject();
        T project = readProject();
        hasDependency(project, "quarkus-jdbc-postgresql");
        hasDependency(project, "quarkus-agroal");
        hasDependency(project, "quarkus-arc");
        hasDependency(project, "quarkus-hibernate-validator");
        hasDependency(project, "commons-io", "commons-io", "2.6");

        removeExtensions(asList("jdbc-postgre", "agroal", "quarkus-arc", " hibernate-validator",
                "commons-io:commons-io:2.6"));

        project = readProject();
        doesNotHaveDependency(project, "quarkus-agroal");
        doesNotHaveDependency(project, "quarkus-arc");
        doesNotHaveDependency(project, "quarkus-hibernate-validator");
        doesNotHaveDependency(project, "commons-io");
        doesNotHaveDependency(project, "quarkus-jdbc-postgresql");
    }

    @Test
    void testPartialMatches() throws Exception {
        createProject();

        T project = readProject();
        hasDependency(project, "quarkus-arc");
        hasDependency(project, "quarkus-hibernate-orm-panache");
        hasDependency(project, "quarkus-jdbc-postgresql");

        removeExtensions(asList("jdbc-postgre", "hibernate-orm", "arc"));

        project = readProject();
        doesNotHaveDependency(project, "quarkus-arc");
        doesNotHaveDependency(project, "quarkus-hibernate-orm-panache");
        doesNotHaveDependency(project, "quarkus-jdbc-postgresql");
    }

    @Test
    void testRegexpMatches() throws Exception {
        createProject();

        T project = readProject();
        hasDependency(project, "quarkus-smallrye-reactive-messaging");
        hasDependency(project, "quarkus-smallrye-reactive-streams-operators");
        hasDependency(project, "quarkus-smallrye-opentracing");
        hasDependency(project, "quarkus-smallrye-metrics");
        hasDependency(project, "quarkus-smallrye-reactive-messaging-kafka");
        hasDependency(project, "quarkus-smallrye-health");
        hasDependency(project, "quarkus-smallrye-openapi");
        hasDependency(project, "quarkus-smallrye-jwt");
        hasDependency(project, "quarkus-smallrye-context-propagation");
        hasDependency(project, "quarkus-smallrye-reactive-type-converters");
        hasDependency(project, "quarkus-smallrye-reactive-messaging-amqp");
        hasDependency(project, "quarkus-smallrye-fault-tolerance");

        removeExtensions(Collections.singletonList("Sm??lRye**"));

        project = readProject();
        doesNotHaveDependency(project, "quarkus-smallrye-reactive-messaging");
        doesNotHaveDependency(project, "quarkus-smallrye-reactive-streams-operators");
        doesNotHaveDependency(project, "quarkus-smallrye-opentracing");
        doesNotHaveDependency(project, "quarkus-smallrye-metrics");
        doesNotHaveDependency(project, "quarkus-smallrye-reactive-messaging-kafka");
        doesNotHaveDependency(project, "quarkus-smallrye-health");
        doesNotHaveDependency(project, "quarkus-smallrye-openapi");
        doesNotHaveDependency(project, "quarkus-smallrye-jwt");
        doesNotHaveDependency(project, "quarkus-smallrye-context-propagation");
        doesNotHaveDependency(project, "quarkus-smallrye-reactive-type-converters");
        doesNotHaveDependency(project, "quarkus-smallrye-reactive-messaging-amqp");
        doesNotHaveDependency(project, "quarkus-smallrye-fault-tolerance");

    }

    @Test
    void removeMissingExtension() throws Exception {
        createProject();

        final QuarkusCommandOutcome result = removeExtensions(Collections.singletonList("missing"));

        T project = readProject();
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertFalse(result.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
    }

    @Test
    void removeExtensionTwiceInOneBatch() throws Exception {
        createProject();
        T project = readProject();
        hasDependency(project, "quarkus-agroal");

        QuarkusCommandOutcome result = removeExtensions(asList("agroal", "agroal"));
        project = readProject();
        doesNotHaveDependency(project, "quarkus-agroal");
        Assertions.assertTrue(result.isSuccess());
    }

    @Test
    void removeExtensionTwiceInTwoBatches() throws Exception {
        createProject();

        final QuarkusCommandOutcome result1 = removeExtensions(Collections.singletonList("agroal"));
        final T project1 = readProject();
        doesNotHaveDependency(project1, "quarkus-agroal");
        Assertions.assertTrue(result1.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
        Assertions.assertTrue(result1.isSuccess());

        final QuarkusCommandOutcome result2 = removeExtensions(Collections.singletonList("agroal"));
        final T project2 = readProject();
        doesNotHaveDependency(project2, "quarkus-agroal");
        Assertions.assertFalse(result2.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
        Assertions.assertTrue(result2.isSuccess());
    }

    /**
     * This test reproduce the issue we had using the first selection algorithm.
     * The `arc` query was matching ArC but also hibernate-search-elasticsearch.
     */
    @Test
    void testPartialMatchConflict() throws Exception {
        createProject();

        final QuarkusCommandOutcome result = removeExtensions(Collections.singletonList("arc"));

        Assertions.assertTrue(result.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
        Assertions.assertTrue(result.isSuccess());
        final T project = readProject();
        doesNotHaveDependency(project, "quarkus-arc");

        final QuarkusCommandOutcome result2 = removeExtensions(Collections.singletonList("elasticsearch"));

        Assertions.assertTrue(result2.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
        Assertions.assertTrue(result2.isSuccess());
        final T project2 = readProject();
        doesNotHaveDependency(project2, "quarkus-hibernate-search-elasticsearch");
    }

    @Test
    void removeExistingAndMissingExtensions() throws Exception {
        createProject();
        final QuarkusCommandOutcome result = removeExtensions(asList("missing", "agroal"));

        final T project = readProject();
        doesNotHaveDependency(project, "quarkus-missing");
        doesNotHaveDependency(project, "quarkus-agroal");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertTrue(result.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
    }

    @Test
    void removeDuplicatedExtension() throws Exception {
        createProject();

        final QuarkusCommandOutcome result = removeExtensions(asList("agroal", "jdbc", "non-exist-ent"));

        final T project = readProject();
        doesNotHaveDependency(project, "quarkus-agroal");
        doesNotHaveDependency(project, "quarkus-jdbc-postgresql");
        doesNotHaveDependency(project, "quarkus-jdbc-h2");

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertTrue(result.valueIs(RemoveExtensions.OUTCOME_UPDATED, true));
    }

    @Test
    void removeDuplicatedExtensionUsingGAV() throws Exception {
        createProject();

        removeExtensions(asList("commons-io:commons-io:2.6", "commons-io:commons-io:2.6"));

        final T project = readProject();
        doesNotHaveDependency(project, "commons-io");
    }

    @Test
    void testVertx() throws Exception {
        createProject();

        removeExtensions(Collections.singletonList("vertx"));

        final T project = readProject();
        doesNotHaveDependency(project, "quarkus-vertx");
    }

    @Test
    void testVertxWithDot() throws Exception {
        createProject();

        removeExtensions(Collections.singletonList("vert.x"));

        final T project = readProject();
        doesNotHaveDependency(project, "quarkus-vertx");
    }

    private void hasDependency(T project, String artifactId) {
        hasDependency(project, getPluginGroupId(), artifactId, null);
    }

    private void hasDependency(T project, String groupId, String artifactId, String version) {
        Assertions.assertTrue(countDependencyOccurrences(project, groupId, artifactId, version) > 0);
    }

    private void doesNotHaveDependency(T project, String artifactId) {
        Assertions.assertTrue(countDependencyOccurrences(project, getPluginGroupId(), artifactId, null) == 0);
    }

    protected abstract T createProject() throws IOException, QuarkusCommandException;

    protected abstract T readProject() throws IOException;

    protected abstract QuarkusCommandOutcome removeExtensions(List<String> extensions)
            throws IOException, QuarkusCommandException;

    protected abstract long countDependencyOccurrences(T project, String groupId, String artifactId, String version);
}
