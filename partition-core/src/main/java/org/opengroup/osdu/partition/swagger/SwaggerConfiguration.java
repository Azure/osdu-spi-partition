package org.opengroup.osdu.partition.swagger;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.servlet.ServletContext;
import java.util.Collections;
import java.util.Map;

import org.opengroup.osdu.partition.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("!noswagger")
@PropertySource("classpath:swagger.properties")
public class SwaggerConfiguration {

    @Autowired
    private SwaggerConfigurationProperties configurationProperties;


    @Bean
    public OpenAPI openApi(ServletContext servletContext) {
        Server server = new Server().url(servletContext.getContextPath());
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(configurationProperties.getApiTitle())
                        .description(configurationProperties.getApiDescription())
                        .version(configurationProperties.getApiVersion())
                        .contact(new Contact().name(configurationProperties.getApiContactName()).email(configurationProperties.getApiContactEmail()))
                        .license(new License().name(configurationProperties.getApiLicenseName()).url(configurationProperties.getApiLicenseUrl())))
                .addTagsItem(new Tag().name("partition-api").description("Partition API"))
                .addTagsItem(new Tag().name("info").description("Version info endpoint"))
                .components(new Components()
                        .addSchemas("Property",
                                new ObjectSchema()
                                        .addProperty("sensitive", new BooleanSchema())
                                        .addProperty("value", new ObjectSchema()))
                        .addSchemas("Map",
                                new Schema<Map<String, Property>>().addProperty("< * >",
                                        new ObjectSchema().$ref("#/components/schemas/Property")))
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("Bearer")
                                        .bearerFormat("Authorization")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("Authorization"));
        if (configurationProperties.isApiServerFullUrlEnabled())
            return openAPI;
        return openAPI
                .servers(Collections.singletonList(server));
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(
                    new Parameter()
                            .in("header")
                            .required(true)
                            .description("Tenant Id")
                            .name(DpsHeaders.DATA_PARTITION_ID));
            return operation;
        };
    }
}
