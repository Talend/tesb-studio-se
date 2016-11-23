// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * The Class AbstractRouteMigrationTask. Migration task extends this class restrict to only works on Route Repository
 * Node. And provide some common methods.
 */
public abstract class AbstractRouteItemMigrationTask extends AbstractItemMigrationTask {

    /** FACTORY use to save item if changed in migration task. */
    protected static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    /**
     * Restrict only works on Route Repository Node.
     * 
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#getTypes()
     */
    @Override
    public final List<ERepositoryObjectType> getTypes() {
        return Collections.singletonList(CamelRepositoryNodeType.repositoryRoutesType);
    }

    protected void saveItem(Item item) throws PersistenceException {
        FACTORY.save(item, true);
    }

    /**
     * Find component nodes equals given name.
     *
     * @param item the item
     * @param component the component
     * @return the list
     */
    protected List<NodeType> findComponentNodes(CamelProcessItem item, String component) {
        return findComponentNodes(item, component, false);
    }

    /**
     * Find component nodes match given regex.
     *
     * @param item the item
     * @param componentRegex the component regex
     * @return the list
     */
    protected List<NodeType> findComponentNodesRegex(CamelProcessItem item, String componentRegex) {
        return findComponentNodes(item, componentRegex, true);
    }

    private List<NodeType> findComponentNodes(CamelProcessItem item, String search, boolean isRegex) {
        if (search == null) {
            throw new RuntimeException("Can't search component node by \"null\" in " + this.getClass());
        }
        ProcessType processType = item.getProcess();
        if (processType == null) {
            return Collections.emptyList();
        }
        List<NodeType> returnList = new ArrayList<>();
        for (Object o : processType.getNode()) {
            if (o instanceof NodeType) {
                NodeType currentNode = (NodeType) o;
                String componentName = currentNode.getComponentName();
                if (isRegex) {
                    if (componentName.matches(search)) {
                        returnList.add(currentNode);
                    }
                } else {
                    if (componentName.equals(search)) {
                        returnList.add(currentNode);
                    }
                }
            }
        }
        return returnList;
    }

    @Override
    public final ExecutionResult execute(Item item) {
        if (item instanceof CamelProcessItem) {
            return execute((CamelProcessItem) item);
        }
        // never goes here, all Item should be CamelProcessItem.
        return ExecutionResult.FAILURE;
    }

    protected abstract ExecutionResult execute(CamelProcessItem item);
}
