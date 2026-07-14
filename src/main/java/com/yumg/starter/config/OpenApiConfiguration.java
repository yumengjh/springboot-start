package com.yumg.starter.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(title = "Spring Boot Starter API", version = "v1", description = "模块化后端启动模板 API"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfiguration {
    @Bean
    OpenAPI starterOpenApi() {
        Schema<?> violation = new ObjectSchema()
                .addProperty("field", new StringSchema().example("username"))
                .addProperty("message", new StringSchema().example("长度必须介于 3 和 32 之间"));
        Schema<?> error = new ObjectSchema()
                .addProperty("code", new StringSchema().example("VALIDATION_FAILED"))
                .addProperty("message", new StringSchema().example("请求参数不合法"))
                .addProperty("traceId", new StringSchema().example("01jxyz..."))
                .addProperty("violations", new ArraySchema().items(violation))
                .addProperty("timestamp", new IntegerSchema().format("int64").example(1784016000000L));
        Content errorContent = new Content().addMediaType("application/json",
                new MediaType().schema(error));
        Components components = new Components()
                .addSchemas("ApiError", error)
                .addResponses("BadRequest", new ApiResponse().description("请求格式、字段或参数校验失败").content(errorContent))
                .addResponses("Unauthorized", new ApiResponse().description("缺少、失效或被撤销的访问令牌").content(errorContent))
                .addResponses("Forbidden", new ApiResponse().description("当前身份没有所需权限，或被服务安全策略拒绝").content(errorContent))
                .addResponses("NotFound", new ApiResponse().description("资源或运行时配置键不存在").content(errorContent))
                .addResponses("RateLimited", new ApiResponse().description("触发全局或接口级限流").content(errorContent));
        return new OpenAPI().components(components);
    }
}
