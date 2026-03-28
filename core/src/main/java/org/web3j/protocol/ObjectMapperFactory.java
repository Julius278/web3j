/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.protocol;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import org.web3j.protocol.core.Response;
import org.web3j.protocol.deserializer.RawResponseDeserializer;

/** Factory for managing our ObjectMapper instances. */
public class ObjectMapperFactory {

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = buildObjectMapper(false);

    public static ObjectMapper getObjectMapper() {
        return getObjectMapper(false);
    }

    public static ObjectMapper getObjectMapper(boolean shouldIncludeRawResponses) {
        if (!shouldIncludeRawResponses) {
            return DEFAULT_OBJECT_MAPPER;
        }

        return buildObjectMapper(true);
    }

    public static ObjectReader getObjectReader() {
        return DEFAULT_OBJECT_MAPPER.reader();
    }

    private static ObjectMapper buildObjectMapper(boolean shouldIncludeRawResponses) {
        JsonMapper.Builder builder =
                JsonMapper.builder()
                        .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        if (shouldIncludeRawResponses) {
            SimpleModule module = new SimpleModule();
            module.setDeserializerModifier(
                    new ValueDeserializerModifier() {
                        @Override
                        public ValueDeserializer<?> modifyDeserializer(
                                DeserializationConfig config,
                                BeanDescription.Supplier beanDesc,
                                ValueDeserializer<?> deserializer) {
                            if (Response.class.isAssignableFrom(beanDesc.getBeanClass())) {
                                return new RawResponseDeserializer(
                                        (ValueDeserializer<?>) deserializer);
                            }

                            return deserializer;
                        }
                    });

            builder.addModule(module);
        }

        return builder.build();
    }
}
