package com.kobylynskyi.graphql.codegen.kotlin;

import com.kobylynskyi.graphql.codegen.TestUtils;
import com.kobylynskyi.graphql.codegen.model.GeneratedLanguage;
import com.kobylynskyi.graphql.codegen.model.MappingConfig;
import com.kobylynskyi.graphql.codegen.supplier.SchemaFinder;
import com.kobylynskyi.graphql.codegen.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

import static com.kobylynskyi.graphql.codegen.TestUtils.assertSameTrimmedContent;
import static com.kobylynskyi.graphql.codegen.TestUtils.getFileByName;
import static java.util.Collections.singletonMap;

class GraphQLCodegenNullableTest {

    private final File outputBuildDir = new File("build/generated");
    private final File outputJavaClassesDir = new File("build/generated");
    private final MappingConfig mappingConfig = new MappingConfig();
    private final SchemaFinder schemaFinder = new SchemaFinder(Paths.get("src/test/resources"));

    @BeforeEach
    void init() {
        mappingConfig.setGeneratedLanguage(GeneratedLanguage.KOTLIN);
    }

    @AfterEach
    void cleanup() {
        Utils.deleteDir(outputBuildDir);
    }

    @Test
    void generatePrimitiveTypesResponseResolverClasses_nullable() throws Exception {
        mappingConfig.setGenerateApis(true);
        mappingConfig.setGenerateClient(true);
        schemaFinder.setIncludePattern("nullable-extend.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/Event.kt.txt"),
                getFileByName(files, "Event.kt"));

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/Null1QueryQueryResolver.kt.txt"),
                getFileByName(files, "Null1QueryQueryResolver.kt"));

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/Null2QueryQueryResolver.kt.txt"),
                getFileByName(files, "Null2QueryQueryResolver.kt"));

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/Null3QueryQueryResolver.kt.txt"),
                getFileByName(files, "Null3QueryQueryResolver.kt"));

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/Null4QueryQueryResolver.kt.txt"),
                getFileByName(files, "Null4QueryQueryResolver.kt"));

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/Null5QueryQueryResolver.kt.txt"),
                getFileByName(files, "Null5QueryQueryResolver.kt"));

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/QueryResolver.kt.txt"),
                getFileByName(files, "QueryResolver.kt"));
    }

    @Test
    void generatePrimitiveTypesResponseResolverClasses_nullable_With_Prefix_Suffix() throws Exception {
        mappingConfig.setGenerateApis(true);
        mappingConfig.setGenerateClient(true);
        mappingConfig.setModelNamePrefix("Test");
        mappingConfig.setModelNameSuffix("DTO");
        schemaFinder.setIncludePattern("nullable-extend.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());
        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/QueryResolver_WITH_Prefix_Suffix.kt.txt"),
                getFileByName(files, "QueryResolver.kt"));
    }

    /**
     * Verify Int to override Int?
     */
    @Test
    void generate_OptionalFieldInInterfaceAndMandatoryInType() throws Exception {
        mappingConfig.setGenerateBuilder(true);
        mappingConfig.setGenerateToString(true);
        mappingConfig.setGenerateEqualsAndHashCode(true);
        schemaFinder.setIncludePattern("optional-vs-mandatory-types.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/optional/InterfaceWithOptionalField.kt.txt"),
                getFileByName(files, "InterfaceWithOptionalField.kt"));
        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/optional/TypeWithMandatoryField.kt.txt"),
                getFileByName(files, "TypeWithMandatoryField.kt"));
    }

    @Test
    void generate_NullableCustomTypeWithApiReturnType() throws Exception {
        mappingConfig.setGenerateApis(true);
        mappingConfig.setApiReturnType("reactor.core.publisher.Mono");
        mappingConfig.setCustomTypesMapping(new HashMap<>(singletonMap("ZonedDateTime", "String")));

        schemaFinder.setIncludePattern("nullable-custom-type-with-api-return-type.graphqls");

        generate();

        File[] files = Objects.requireNonNull(outputJavaClassesDir.listFiles());

        assertSameTrimmedContent(
                new File("src/test/resources/expected-classes/kt/nullable/NullableCustomTypeWithApiReturnType.kt.txt"),
                getFileByName(files, "QueryResolver.kt"));

    }

    private void generate() throws IOException {
        new KotlinGraphQLCodegen(schemaFinder.findSchemas(), outputBuildDir, mappingConfig,
                TestUtils.getStaticGeneratedInfo(mappingConfig))
                .generate();
    }
}
