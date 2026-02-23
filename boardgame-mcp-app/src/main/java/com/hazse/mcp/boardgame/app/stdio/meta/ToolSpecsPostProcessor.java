package com.hazse.mcp.boardgame.app.stdio.meta;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springaicommunity.mcp.method.tool.AbstractMcpToolMethodCallback;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Slf4j
public class ToolSpecsPostProcessor implements BeanPostProcessor {
    private final ToolMetadataTransformer metadataTransformer;

    public ToolSpecsPostProcessor() {
        this(null);
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ("toolSpecs".equals(beanName)) {
            log.info("Found tool specifications bean...");
            if (bean instanceof List<?> listBean) {
                List<McpServerFeatures.SyncToolSpecification> toolSpecs = listBean.stream()
                        .filter(it -> it instanceof McpServerFeatures.SyncToolSpecification)
                        .map(it -> (McpServerFeatures.SyncToolSpecification) it)
                        .toList();

                log.info("Found {} tool specification(s)", toolSpecs.size());
                return processToolSpecifications(toolSpecs);
            }
        }

        return bean;
    }

    private Object processToolSpecifications(List<McpServerFeatures.SyncToolSpecification> toolSpecs) {
        List<McpServerFeatures.SyncToolSpecification> retValue = new ArrayList<>();

        Field toolMethodField = ReflectionUtils.findField(AbstractMcpToolMethodCallback.class, "toolMethod");
        ReflectionUtils.makeAccessible(toolMethodField);

        for (McpServerFeatures.SyncToolSpecification toolSpec : toolSpecs) {
            McpServerFeatures.SyncToolSpecification processedToolSpec = toolSpec;

            BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> callHandler = toolSpec.callHandler();
            if (callHandler instanceof AbstractMcpToolMethodCallback) {
                Method toolMethod = (Method) ReflectionUtils.getField(toolMethodField, callHandler);
                McpToolMeta toolMetaAnnotation = getMcpToolMetaAnnotation(toolMethod);

                if (toolMetaAnnotation != null) {
                    String[] toolMetaValues = toolMetaAnnotation.metadata();
                    Map<String, Object> metadata = parseMetadata(toolMetaValues);

                    if (!metadata.isEmpty()) {
                        if (metadataTransformer != null) {
                            metadata = metadataTransformer.transformMetadata(metadata);
                        }

                        McpSchema.Tool tool = toolSpec.tool();
                        McpSchema.Tool toolWithMeta = McpSchema.Tool.builder()
                                .name(tool.name())
                                .title(tool.title())
                                .description(tool.description())
                                .inputSchema(tool.inputSchema())
                                .outputSchema(tool.outputSchema())
                                .annotations(tool.annotations())
                                .meta(metadata)
                                .build();

                        processedToolSpec = McpServerFeatures.SyncToolSpecification.builder()
                                .tool(toolWithMeta)
                                .callHandler(callHandler)
                                .build();

                        log.info("Injected tool-metadata for: {}", toolWithMeta);
                    }
                }
            }

            retValue.add(processedToolSpec);
        }

        return retValue;
    }

    private McpToolMeta getMcpToolMetaAnnotation(Method method) {
        return method.getAnnotation(McpToolMeta.class);
    }

    private Map<String, Object> parseMetadata(String[] metadataEntries) {
        Map<String, Object> retValue = new HashMap<>();

        for (String pair : metadataEntries) {
            if (StringUtils.hasText(pair)) {
                String[] entry = pair.split("=");
                if (entry.length == 2) {
                    retValue.put(entry[0].trim(), entry[1].trim());
                }
            }
        }

        return retValue;
    }
}
