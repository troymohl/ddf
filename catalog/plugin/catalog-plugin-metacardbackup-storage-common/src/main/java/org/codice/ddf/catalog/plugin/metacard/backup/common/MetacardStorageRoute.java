/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.catalog.plugin.metacard.backup.common;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.camel.component.catalog.ingest.PostIngestConsumer;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;

public abstract class MetacardStorageRoute extends RouteBuilder {
    public static final String METACARD_TRANSFORMER_ID_RTE_PROP = "transformerId";

    public static final String METACARD_BACKUP_INVALID_RTE_PROP = "shouldBackupInvalid";

    public static final String METACARD_BACKUP_KEEP_DELETED_RTE_PROP = "keepDeleted";

    public static final String TEMPLATED_STRING_HEADER_RTE_PROP = "templated_string";

    public static final String BACKUP_INVALID_PROPERTY = "backupInvalidMetacards";

    public static final String KEEP_DELETED_PROPERTY = "keepDeletedMetacards";

    public static final String TRANSFORMER_ID_PROPERTY = "metacardTransformerId";

    private static final String INVALID_TAG = "INVALID";

    protected boolean backupInvalidMetacards;

    protected boolean keepDeletedMetacards;

    protected String metacardTransformerId;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetacardStorageRoute.class);

    public MetacardStorageRoute(CamelContext camelContext) {
        super(camelContext);
    }

    public void start() {
        try {
            getContext().addRoutes(this);
        } catch (Exception e) {
            LOGGER.error("Could not start route: {}", toString(), e);
        }
    }

    public void stop(int code) {
        try {
            CamelContext context = getContext();
            for (RouteDefinition routeDefinition : context.getRouteDefinitions()) {
                context.stopRoute(routeDefinition.getId());
                context.removeRouteDefinition(routeDefinition);
                setRouteCollection(new RoutesDefinition());
            }
        } catch (Exception e) {
            LOGGER.error("Could not stop route: {}", toString(), e);
        }
    }

    public String getMetacardTransformerId() {
        return metacardTransformerId;
    }

    public boolean isBackupInvalidMetacards() {
        return backupInvalidMetacards;
    }

    public boolean isKeepDeletedMetacards() {
        return keepDeletedMetacards;
    }

    public void setBackupInvalidMetacards(boolean backupInvalidMetacards) {
        this.backupInvalidMetacards = backupInvalidMetacards;
    }

    public void setKeepDeletedMetacards(boolean keepDeletedMetacards) {
        this.keepDeletedMetacards = keepDeletedMetacards;
    }

    public void setMetacardTransformerId(String metacardTransformerId) {
        this.metacardTransformerId = metacardTransformerId;
    }

    public void refresh(Map<String, Object> properties) throws Exception {
        Object backupInvalidProp = properties.get(BACKUP_INVALID_PROPERTY);
        if (backupInvalidProp instanceof Boolean) {
            this.backupInvalidMetacards = (boolean) backupInvalidProp;
        }

        Object keepDeletedProp = properties.get(KEEP_DELETED_PROPERTY);
        if (keepDeletedProp instanceof Boolean) {
            this.keepDeletedMetacards = (Boolean) keepDeletedProp;
        }

        Object metacardTransformerIdProp = properties.get(TRANSFORMER_ID_PROPERTY);
        if (metacardTransformerIdProp instanceof String) {
            this.metacardTransformerId = (String) metacardTransformerIdProp;
        }

        stop(0);
        configure();
        start();
    }

    public static boolean shouldBackupMetacard(Metacard metacard, boolean backupInvalid) {
        if (backupInvalid) {
            return true;
        } else {
            Attribute metacardTagsAttr = metacard.getAttribute(Core.METACARD_TAGS);
            if (metacardTagsAttr != null) {
                return metacardTagsAttr.getValues()
                        .stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .noneMatch(INVALID_TAG::equalsIgnoreCase);
            }
            return true;
        }

    }

    protected Predicate getShouldBackupPredicate() {
        return exchange -> {
            Object bodyObj = exchange.getIn()
                    .getBody();
            if (bodyObj instanceof Metacard) {
                Metacard metacard = (Metacard) bodyObj;
                return MetacardStorageRoute.shouldBackupMetacard(metacard,
                        exchange.getIn()
                                .getHeader(METACARD_BACKUP_INVALID_RTE_PROP, Boolean.class));
            }
            return false;
        };
    }

    protected Predicate getCheckDeletePredicate() {
        return exchange -> {
            boolean shouldContinueRoute = true;
            String action = exchange.getIn()
                    .getHeader(PostIngestConsumer.ACTION, String.class);
            if (action.equals(PostIngestConsumer.DELETE)) {
                Boolean keepDeleted = exchange.getIn()
                        .getHeader(METACARD_BACKUP_KEEP_DELETED_RTE_PROP, Boolean.class);
                if (BooleanUtils.isTrue(keepDeleted)) {
                    shouldContinueRoute = false;
                }
            }
            return shouldContinueRoute;
        };
    }
}
