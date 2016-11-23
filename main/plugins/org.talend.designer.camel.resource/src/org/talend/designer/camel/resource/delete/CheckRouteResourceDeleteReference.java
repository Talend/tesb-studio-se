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
package org.talend.designer.camel.resource.delete;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.AbstractCheckDeleteItemReference;
import org.talend.core.repository.model.ItemReferenceBean;
import org.talend.core.repository.ui.actions.DeleteActionCache;
import org.talend.designer.camel.resource.core.model.ResourceDependencyModel;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;
import org.talend.repository.model.IProxyRepositoryFactory;

public class CheckRouteResourceDeleteReference extends AbstractCheckDeleteItemReference {

    @Override
    protected Set<ItemReferenceBean> checkItemReferenceBeans(IProxyRepositoryFactory factory, DeleteActionCache deleteActionCache,
            IRepositoryViewObject object) {
        final Set<ItemReferenceBean> list = new HashSet<>(
                collect(factory, object, CamelRepositoryNodeType.repositoryRoutesType));
        list.addAll(collect(factory, object, CamelRepositoryNodeType.repositoryRouteletType));
        return list;
    }

    private Collection<ItemReferenceBean> collect(IProxyRepositoryFactory factory, IRepositoryViewObject object,
            ERepositoryObjectType type) {
        final Item nodeItem = object.getProperty().getItem();
        final Set<ItemReferenceBean> list = new HashSet<>();
        try {
            for (IRepositoryViewObject obj : factory.getAll(type)) {
                final Property property = obj.getProperty();
                final Item routeItem = property.getItem();
                for (ResourceDependencyModel model : RouteResourceUtil.getResourceDependencies((ProcessItem) routeItem)) {
                    if (nodeItem.getProperty().getId().equals(model.getItem().getProperty().getId())) {
                        final ItemReferenceBean bean = new ItemReferenceBean();
                        bean.setItemName(object.getProperty().getLabel());
                        bean.setItemVersion(model.getSelectedVersion());
                        bean.setItemType(CamelRepositoryNodeType.repositoryRouteResourceType);
                        bean.setItemDeleted(object.isDeleted());
                        bean.setReferenceItemName(property.getLabel());
                        bean.setReferenceItemVersion(property.getVersion());
                        bean.setReferenceItemType(type);
                        bean.setReferenceItemPath(routeItem.getState().getPath());
                        bean.setReferenceProjectName(obj.getProjectLabel());
                        bean.setReferenceItemDeleted(obj.isDeleted());
                        if (!list.add(bean)) {
                            for (ItemReferenceBean b : list) {
                                if (b.equals(bean)) {
                                    b.addNodeNum();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return list;
    }
}
