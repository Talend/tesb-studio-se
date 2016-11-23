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
package org.talend.camel.designer;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.camel.core.model.camelProperties.RouteDocumentItem;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.camel.designer.util.ECamelCoreImage;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Status;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.AbstractResourceRepositoryContentHandler;
import org.talend.core.runtime.CoreRuntimePlugin;

public class CamelRepositoryContentHandler extends AbstractResourceRepositoryContentHandler {

    @Override
    public List<Status> getPropertyStatus(Item item) {
        if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_RESOURCE_ITEM) {
            try {
                return CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().getTechnicalStatus();
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return super.getPropertyStatus(item);
    }

    @Override
    public boolean isProcess(Item item) {
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isRepObjType(ERepositoryObjectType type) {
        return type == CamelRepositoryNodeType.repositoryBeansType || type == CamelRepositoryNodeType.repositoryDocumentationType
                || type == CamelRepositoryNodeType.repositoryRouteResourceType
                || type == CamelRepositoryNodeType.repositoryRoutesType;
    }

    @Override
    public ERepositoryObjectType getProcessType() {
        return CamelRepositoryNodeType.repositoryRoutesType;
    }

    @Override
    public ERepositoryObjectType getCodeType() {
        return CamelRepositoryNodeType.repositoryBeansType;
    }

    @Override
    public Resource create(IProject project, Item item, int classifierID, IPath path) throws PersistenceException {
        ERepositoryObjectType type = null;
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            return create(project, (ProcessItem) item, path, CamelRepositoryNodeType.repositoryRoutesType);
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEAN_ITEM) {
            type = CamelRepositoryNodeType.repositoryBeansType;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_RESOURCE_ITEM) {
            type = CamelRepositoryNodeType.repositoryRouteResourceType;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_DOCUMENT_ITEM) {
            type = CamelRepositoryNodeType.repositoryDocumentationType;
        }
        if (null != type) {
            return create(project, (FileItem) item, path, type);
        }
        return null;
    }

    @Override
    public Resource createScreenShotResource(IProject project, Item item, int classifierID, IPath path)
            throws PersistenceException {
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            return createScreenShotResource(project, item, path, CamelRepositoryNodeType.repositoryRoutesType);
        }
        return null;
    }

    @Override
    public Resource save(Item item) throws PersistenceException {
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            return save((ProcessItem) item);
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEAN_ITEM) {
            return save((BeanItem) item);
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_RESOURCE_ITEM) {
            return save((RouteResourceItem) item);
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_DOCUMENT_ITEM) {
            return save((RouteDocumentItem) item);
        }
        return null;
    }

    @Override
    public Resource saveScreenShots(Item item) throws PersistenceException {
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            return saveScreenShots((ProcessItem) item);
        }
        return null;
    }

    @Override
    public IImage getIcon(ERepositoryObjectType type) {
        if (type == CamelRepositoryNodeType.repositoryRoutesType) {
            return ECoreImage.ROUTES_ICON;
        } else if (type == CamelRepositoryNodeType.repositoryBeansType) {
            return ECamelCoreImage.BEAN_ICON;
        } else if (type == CamelRepositoryNodeType.repositoryRouteResourceType) {
            return ECamelCoreImage.RESOURCE_ICON;
        } else if (type == CamelRepositoryNodeType.repositoryDocumentationType) {
            return ECoreImage.DOCUMENTATION_ICON;
        }
        return null;
    }

    @Override
    public Item createNewItem(ERepositoryObjectType type) {
        Item item = null;
        if (type == CamelRepositoryNodeType.repositoryRoutesType) {
            item = CamelPropertiesFactory.eINSTANCE.createCamelProcessItem();
        } else if (type == CamelRepositoryNodeType.repositoryBeansType) {
            item = CamelPropertiesFactory.eINSTANCE.createBeanItem();
        } else if (type == CamelRepositoryNodeType.repositoryRouteResourceType) {
            item = CamelPropertiesFactory.eINSTANCE.createRouteResourceItem();
        } else if (type == CamelRepositoryNodeType.repositoryDocumentationType) {
            item = CamelPropertiesFactory.eINSTANCE.createRouteDocumentItem();
        }
        return item;
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType(Item item) {
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            return CamelRepositoryNodeType.repositoryRoutesType;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEAN_ITEM) {
            return CamelRepositoryNodeType.repositoryBeansType;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_RESOURCE_ITEM) {
            return CamelRepositoryNodeType.repositoryRouteResourceType;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_DOCUMENT_ITEM) {
            return CamelRepositoryNodeType.repositoryDocumentationType;
        }
        return null;
    }

}
