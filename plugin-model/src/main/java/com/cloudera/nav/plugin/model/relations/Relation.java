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
package com.cloudera.nav.plugin.model.relations;


import com.cloudera.nav.plugin.model.SourceType;
import com.cloudera.nav.plugin.model.annotations.MProperty;
import com.cloudera.nav.plugin.model.entities.Entity;
import com.cloudera.nav.plugin.model.entities.EntityType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * An abstract base class for representing relationships between custom entities
 * or between custom entities and Navigator entities. This class is not meant
 * to be subclassed by the user as there are known set of valid relationship
 * types and they are represented in the concrete subclasses in
 * com.cloudera.nav.plugin.model.relations
 */
public abstract class Relation {

  public static final String NAVIGATOR = "nav";

  public static abstract class Builder<T extends Builder<T>> {
    private final RelationType type;
    private String namespace;
    @MProperty(required=true)
    private String identity;
    @MProperty(required=true)
    private Collection<String> ep1Ids;
    @MProperty
    private EntityType ep1Type;
    @MProperty(required=true)
    private SourceType ep1SourceType;
    @MProperty
    private String ep1SourceId;
    @MProperty(required=true)
    private Collection<String> ep2Ids;
    @MProperty
    private EntityType ep2Type;
    @MProperty(required=true)
    private SourceType ep2SourceType;
    @MProperty
    private String ep2SourceId;
    private RelationIdGenerator idGenerator;

    protected Builder(RelationType type) {
      this.type = type;
    }

    protected abstract T self();

    public T namespace(String namespace) {
      this.namespace = namespace;
      return self();
    }

    public T identity(String identity) {
      this.identity = identity;
      return self();
    }

    public T idGenerator(RelationIdGenerator idGenerator) {
      this.idGenerator = idGenerator;
      return self();
    }

    public T ep1Ids(Collection<String> ep1Ids) {
      this.ep1Ids = ep1Ids;
      return self();
    }

    public T ep1Type(EntityType ep1Type) {
      this.ep1Type = ep1Type;
      return self();
    }

    public T ep2Ids(Collection<String> ep2Ids) {
      this.ep2Ids = ep2Ids;
      return self();
    }

    public T ep2Type(EntityType ep2Type) {
      this.ep2Type = ep2Type;
      return self();
    }

    public T ep1SourceType(SourceType ep1SourceType) {
      this.ep1SourceType = ep1SourceType;
      return self();
    }

    public T ep1SourceId(String ep1SourceId) {
      this.ep1SourceId = ep1SourceId;
      return self();
    }

    public T ep2SourceType(SourceType ep2SourceType) {
      this.ep2SourceType = ep2SourceType;
      return self();
    }

    public T ep2SourceId(String ep2SourceId) {
      this.ep2SourceId = ep2SourceId;
      return self();
    }

    /**
     * Must have same source and type
     * @param ep1Entities
     * @return
     */
    public T ep1(Collection<Entity> ep1Entities) {
      Collection<String> ids = prepEndpoint(ep1Entities);
      Entity proto = Iterables.getFirst(ep1Entities, null);
      Preconditions.checkNotNull(proto);
      return ep1Ids(ids).ep1SourceType(proto.getSourceType())
          .ep1Type(proto.getEntityType())
          .ep1SourceId(proto.getSourceId());
    }

    /**
     * Must have same source and type
     * @param ep2Entities
     * @return
     */
    public T ep2(Collection<Entity> ep2Entities) {
      Collection<String> ids = prepEndpoint(ep2Entities);
      Entity proto = Iterables.getFirst(ep2Entities, null);
      Preconditions.checkNotNull(proto);
      return ep2Ids(ids).ep2SourceType(proto.getSourceType())
          .ep2Type(proto.getEntityType())
          .ep2SourceId(proto.getSourceId());
    }

    private Collection<String> prepEndpoint(Collection<Entity> entities) {
      // Entities must be a non-empty collection with the same source
      // and entity type
      Preconditions.checkArgument(!CollectionUtils.isEmpty(entities));
      Collection<String> ids = Lists.newArrayList();
      Entity proto = null;
      for (Entity en : entities) {
        ids.add(en.getIdentity());
        if (proto == null) {
          proto = en;
        } else {
          Preconditions.checkArgument(
              StringUtils.equals(proto.getSourceId(), en.getSourceId()) &&
                  proto.getSourceType() == en.getSourceType() &&
                  proto.getEntityType() == en.getEntityType());
        }
      }
      return ids;
    }

    public abstract Relation build();
  }

  public static final String MTYPE = "RELATION"; // type of metadata object

  private static RelationValidator validator = new RelationValidator();

  @MProperty(required=true)
  private String namespace;
  @MProperty(required=true)
  private String identity;
  @MProperty(required=true)
  private RelationType type;
  @MProperty(required=true)
  private Collection<String> ep1Ids;
  @MProperty(required=true)
  private SourceType ep1SourceType;
  @MProperty(required=true)
  private Collection<String> ep2Ids;
  @MProperty(required=true)
  private SourceType ep2SourceType;
  @MProperty
  private EntityType ep1Type;
  @MProperty
  private String ep1SourceId;
  @MProperty
  private EntityType ep2Type;
  @MProperty
  private String ep2SourceId;

  protected Relation(Builder<?> builder) {
    Preconditions.checkState(builder.identity != null ||
        builder.idGenerator != null);
    if (builder.identity != null) {
      this.identity = builder.identity;
    } else {
      this.identity = builder.idGenerator.generateRelationIdentity(
          builder.ep1Ids, builder.ep1SourceType,
          builder.ep2Ids, builder.ep2SourceType, builder.type);
    }
    this.type = builder.type;
    this.namespace = builder.namespace;
    this.ep1Ids = builder.ep1Ids;
    this.ep1Type = builder.ep1Type;
    this.ep1SourceType = builder.ep1SourceType;
    this.ep1SourceId = builder.ep1SourceId;
    this.ep2Ids = builder.ep2Ids;
    this.ep2Type = builder.ep2Type;
    this.ep2SourceType = builder.ep2SourceType;
    this.ep2SourceId = builder.ep2SourceId;
    validator.validateRequiredMProperties(this);
  }

  /**
   * @return Navigator assigned namespace for the custom relation
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @return id for this custom relation
   */
  public String getIdentity() {
    return identity;
  }

  /**
   * @return the type of this relation
   */
  public RelationType getType() {
    return type;
  }

  /**
   * @return id's for endpoint1 of this relation
   */
  public Collection<String> getEp1Ids() {
    return ep1Ids;
  }

  public EntityType getEp1Type() {
    return ep1Type;
  }

  /**
   * @return id's for endpoint2 of this relation
   */
  public Collection<String> getEp2Ids() {
    return ep2Ids;
  }

  public EntityType getEp2Type() {
    return ep2Type;
  }

  public SourceType getEp1SourceType() {
    return ep1SourceType;
  }

  public String getEp1SourceId() {
    return ep1SourceId;
  }

  public SourceType getEp2SourceType() {
    return ep2SourceType;
  }

  public String getEp2SourceId() {
    return ep2SourceId;
  }
}
