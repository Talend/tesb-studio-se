// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.camel.resource.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.AbstractJobItemRelationshipHandler;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.designer.camel.resource.core.model.ResourceDependencyModel;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;

/**
 * Use to add resources dependencies for route. Need to update when dependencies
 * change(add/remove resources in [Manage Route Resource Dialog].)
 *
 * @see org.talend.designer.camel.resource.ui.actions.ManageRouteResourcesAction
 */
public class ResourcesItemRelationshipHandler extends AbstractJobItemRelationshipHandler {

    @Override
    protected Set<Relation> collect(Item baseItem) {
        if (baseItem.eClass() != CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM
            && baseItem.eClass() != CamelPropertiesPackage.Literals.ROUTELET_PROCESS_ITEM) {
            return Collections.emptySet();
        }
        final Set<Relation> relationSet = new HashSet<Relation>();
        for (ResourceDependencyModel depModel : RouteResourceUtil.getResourceDependencies((ProcessItem) baseItem)) {
            final Relation addedRelation = new Relation();
            addedRelation.setId(depModel.getItem().getProperty().getId());
            addedRelation.setType(RelationshipItemBuilder.RESOURCE_RELATION);
            addedRelation.setVersion(RelationshipItemBuilder.LATEST_VERSION);
            relationSet.add(addedRelation);
        }
        return relationSet;
    }

}
