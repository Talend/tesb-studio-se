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
package org.talend.repository.services.relation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.AbstractJobItemRelationshipHandler;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.utils.ESBRepositoryNodeType;

/**
 * Add operation job relation with SOAP service
 */
public class ServiceRelationshipHandler extends AbstractJobItemRelationshipHandler {

    @Override
    public boolean valid(Item baseItem) {
        if (baseItem instanceof ProcessItem) {
            return true;
        }
        return false;
    }

    @Override
    public String getBaseItemType(Item baseItem) {
        if (baseItem instanceof ProcessItem) {
            return RelationshipItemBuilder.JOB_RELATION;
        }
        return null;
    }

    protected ProcessType getProcessType(Item baseItem) {
        if (baseItem instanceof ProcessItem) {
            return ((ProcessItem) baseItem).getProcess();
        }
        return null;
    }

    @Override
    protected Set<Relation> collect(Item baseItem) {
        if (!(baseItem instanceof ProcessItem)) {
            return Collections.emptySet();
        }

        List<IRepositoryViewObject> serviceRepoList = null;
        try {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            serviceRepoList = factory.getAll(ESBRepositoryNodeType.SERVICES);

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        Set<Relation> relationSet = new HashSet<Relation>();
        for (IRepositoryViewObject obj : serviceRepoList) {
            ServiceItem serviceItem = (ServiceItem) obj.getProperty().getItem();
            List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(serviceItem.getProperty().getId(),
                    RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.SERVICES_RELATION);

            for (Relation repId : relations) {
                if (repId.getId().equals(baseItem.getProperty().getId())) {
                    Relation addedRelation = new Relation();
                    addedRelation.setId(serviceItem.getProperty().getId());
                    addedRelation.setType(RelationshipItemBuilder.SERVICES_RELATION);
                    addedRelation.setVersion(serviceItem.getProperty().getVersion());
                    relationSet.add(addedRelation);
                }
            }
        }
        return relationSet;
    }

}
