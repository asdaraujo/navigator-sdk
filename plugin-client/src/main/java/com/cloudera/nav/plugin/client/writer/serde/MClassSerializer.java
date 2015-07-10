/*
 * Copyright (c) 2015 Cloudera, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.nav.plugin.client.writer.serde;

import com.cloudera.nav.plugin.client.writer.registry.MClassRegistry;
import com.cloudera.nav.plugin.client.writer.registry.MPropertyEntry;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public abstract class MClassSerializer<T> extends StdSerializer<T> {

  private final MClassRegistry registry;

  protected MClassSerializer(Class<T> aClass, MClassRegistry registry) {
    super(aClass);
    this.registry = registry;
  }

  @Override
  public void serialize(T t, JsonGenerator jg, SerializerProvider sp)
      throws IOException, JsonGenerationException {
    jg.writeStartObject();
    writeProperties(t, jg);
    jg.writeEndObject();
  }

  protected void writeProperties(T t, JsonGenerator jg) throws IOException {
    for (MPropertyEntry p : registry.getProperties(t.getClass())) {
      jg.writeObjectField(p.getAttribute(), p.getValue(t));
    }
  }
}
