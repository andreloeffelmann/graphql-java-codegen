package com.kobylynskyi.graphql.codegen.mapper;

import com.kobylynskyi.graphql.codegen.model.MappingContext;
import com.kobylynskyi.graphql.codegen.model.NamedDefinition;
import com.kobylynskyi.graphql.codegen.model.ParameterDefinition;
import com.kobylynskyi.graphql.codegen.model.builders.DeprecatedDefinitionBuilder;
import com.kobylynskyi.graphql.codegen.utils.Utils;
import graphql.language.InputValueDefinition;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Mapper from GraphQL's InputValueDefinition to a Freemarker-understandable format
 *
 * @author kobylynskyi
 */
public class InputValueDefinitionToParameterMapper {

    private final ValueMapper valueMapper;
    private final GraphQLTypeMapper graphQLTypeMapper;
    private final AnnotationsMapper annotationsMapper;
    private final DataModelMapper dataModelMapper;

    public InputValueDefinitionToParameterMapper(MapperFactory mapperFactory) {
        this.valueMapper = mapperFactory.getValueMapper();
        this.graphQLTypeMapper = mapperFactory.getGraphQLTypeMapper();
        this.annotationsMapper = mapperFactory.getAnnotationsMapper();
        this.dataModelMapper = mapperFactory.getDataModelMapper();
    }

    /**
     * Map input value definition to a Freemarker-understandable data model type
     *
     * @param mappingContext   Global mapping context
     * @param valueDefinitions List of GraphQL value definitions
     * @param parentTypeName   Name of the parent GraphQL type
     * @return Freemarker data model of the GraphQL input value definition
     */
    public List<ParameterDefinition> map(MappingContext mappingContext, List<InputValueDefinition> valueDefinitions,
                                         String parentTypeName) {
        return valueDefinitions.stream()
                .map(inputValueDef -> map(mappingContext, inputValueDef, parentTypeName))
                .collect(toList());
    }

    /**
     * Map GraphQL's InputValueDefinition to a Freemarker-understandable format of operation
     *
     * @param mappingContext       Global mapping context
     * @param inputValueDefinition GraphQL input value definition
     * @param parentTypeName       Name of the parent type
     * @return Freemarker-understandable format of parameter (field)
     */
    private ParameterDefinition map(MappingContext mappingContext, InputValueDefinition inputValueDefinition,
                                    String parentTypeName) {
        NamedDefinition namedDefinition = graphQLTypeMapper
                .getLanguageType(mappingContext, inputValueDefinition.getType(), inputValueDefinition.getName(),
                        parentTypeName);

        ParameterDefinition parameter = new ParameterDefinition();
        parameter.setName(dataModelMapper.capitalizeIfRestricted(mappingContext, inputValueDefinition.getName()));
        parameter.setOriginalName(inputValueDefinition.getName());
        parameter.setType(graphQLTypeMapper.getTypeConsideringPrimitive(mappingContext, namedDefinition,
                namedDefinition.getJavaName()));
        parameter.setDefaultValue(valueMapper.map(
                mappingContext, inputValueDefinition.getDefaultValue(), inputValueDefinition.getType()));
        parameter.setVisibility(Utils.getFieldVisibility(mappingContext));
        parameter.setAnnotations(annotationsMapper.getAnnotations(mappingContext, inputValueDefinition.getType(),
                inputValueDefinition, parentTypeName, false));
        parameter.setDeprecated(DeprecatedDefinitionBuilder.build(mappingContext, inputValueDefinition));
        parameter.setMandatory(namedDefinition.isMandatory());
        parameter.setSerializeUsingObjectMapper(namedDefinition.isSerializeUsingObjectMapper());
        parameter.setGetterMethodName(dataModelMapper.capitalizeMethodNameIfRestricted(mappingContext,
                "get" + Utils.capitalize(inputValueDefinition.getName())));
        return parameter;
    }

}
