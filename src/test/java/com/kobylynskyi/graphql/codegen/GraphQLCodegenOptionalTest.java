package com.kobylynskyi.graphql.codegen;

import com.kobylynskyi.graphql.codegen.java.JavaGraphQLCodegen;
import com.kobylynskyi.graphql.codegen.model.MappingConfig;
import com.kobylynskyi.graphql.codegen.supplier.SchemaFinder;
import com.kobylynskyi.graphql.codegen.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import static com.kobylynskyi.graphql.codegen.TestUtils.assertFileContainsElements;
import static com.kobylynskyi.graphql.codegen.TestUtils.assertSameTrimmedContent;
import static com.kobylynskyi.graphql.codegen.TestUtils.getFileByName;

@ExtendWith(MaxQueryTokensExtension.class)
class GraphQLCodegenOptionalTest {

    private final File outputBuildDir = new File("build/generated");
    private final File outputJavaClassesDir = new File("build/generated");
    private final MappingConfig mappingConfig = new MappingConfig();
    private final SchemaFinder schemaFinder = new SchemaFinder(Paths.get("src/test/resources"));

    @BeforeEach
    void before() {
        mappingConfig.setUseOptionalForNullableReturnTypes(true);
    }

    @AfterEach
    void cleanup() {
        Utils.deleteDir(outputBuildDir);
    }

    @Test
    void generate_Optional() throws Exception {
        schemaFinder.setIncludePattern("github.*\\.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        // meta: GitHubMetadata!
        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/MetaQueryResolver.java.txt"),
                getFileByName(files, "MetaQueryResolver.java"));

        // node(id: ID!): Node
        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/NodeQueryResolver.java.txt"),
                getFileByName(files, "NodeQueryResolver.java"));

        // nodes(ids: [ID!]!): [Node]!
        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/NodesQueryResolver.java.txt"),
                getFileByName(files, "NodesQueryResolver.java"));

        // check root query
        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/QueryResolver.java.txt"),
                getFileByName(files, "QueryResolver.java"));
    }

    @Test
    void generate_OptionalWithCustomApiReturnType() throws Exception {
        schemaFinder.setIncludePattern("github.*\\.graphqls");
        mappingConfig.setApiReturnType("reactor.core.publisher.Mono");
        mappingConfig.setApiReturnListType("reactor.core.publisher.Flux");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        // node(id: ID!): Node
        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/" +
                        "NodeQueryResolver_mono.java.txt"),
                getFileByName(files, "NodeQueryResolver.java"));
    }

    /**
     * @see <a href="https://github.com/kobylynskyi/graphql-java-codegen/issues/556">Related issue in GitHub</a>
     */
    @Test
    void generate_OptionalFieldInInterfaceAndMandatoryInType() throws Exception {
        mappingConfig.setGenerateBuilder(true);
        mappingConfig.setGenerateToString(true);
        mappingConfig.setGenerateEqualsAndHashCode(true);
        schemaFinder.setIncludePattern("optional-vs-mandatory-types.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/" +
                        "InterfaceWithOptionalField.java.txt"),
                getFileByName(files, "InterfaceWithOptionalField.java"));
        assertSameTrimmedContent(new File("src/test/resources/expected-classes/optional/" +
                        "TypeWithMandatoryField.java.txt"),
                getFileByName(files, "TypeWithMandatoryField.java"));
    }

    /**
     * @see <a href="https://github.com/kobylynskyi/graphql-java-codegen/issues/1023">Related issue in GitHub</a>
     */
    @Test
    void generate_ObjectsInsteadOfPrimitives() throws Exception {
        mappingConfig.setCustomTypesMapping(new HashMap<>(Collections.singletonMap("Int!", "Integer")));
        schemaFinder.setIncludePattern("optional-vs-mandatory-types.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        // Integer is generated instead of int
        assertFileContainsElements(files, "TypeWithMandatoryField.java",
                "    @javax.validation.constraints.NotNull",
                "    private Integer test;");
    }

    private void generate() throws IOException {
        new JavaGraphQLCodegen(schemaFinder.findSchemas(), outputBuildDir, mappingConfig,
                TestUtils.getStaticGeneratedInfo(mappingConfig)).generate();
    }

}