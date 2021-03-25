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
package org.talend.camel.designer;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.BeansJarItem;
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
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.Status;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.routines.CodesJarInfo;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.AbstractResourceRepositoryContentHandler;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
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
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEANS_JAR_ITEM) {
            return create(project, (BeansJarItem) item, path, ERepositoryObjectType.BEANSJAR);
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEAN_ITEM) {
            type = /* RoutinesUtil.isInnerCodes(item.getProperty()) ? ERepositoryObjectType.BEANSJAR : */ERepositoryObjectType.BEANS;
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

    private Resource create(IProject project, BeansJarItem item, IPath path, ERepositoryObjectType type)
            throws PersistenceException {
        Resource itemResource = getXmiResourceManager().createItemResource(project, item, path, type, false);
        itemResource.getContents().add(item.getRoutinesJarType());
        return itemResource;
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
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEANS_JAR_ITEM) {
            return save((BeansJarItem) item);
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
        } else if (type == ERepositoryObjectType.BEANSJAR) {
            return ECoreImage.ROUTINESJAR_ICON;
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
            return /* RoutinesUtil.isInnerCodes(item.getProperty()) ? ERepositoryObjectType.BEANSJAR : */ERepositoryObjectType.BEANS;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.BEANS_JAR_ITEM) {
            return ERepositoryObjectType.BEANSJAR;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_RESOURCE_ITEM) {
            return CamelRepositoryNodeType.repositoryRouteResourceType;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTE_DOCUMENT_ITEM) {
            return CamelRepositoryNodeType.repositoryDocumentationType;
        }
        return null;
    }

    @Override
    public void addNode(ERepositoryObjectType type, RepositoryNode recBinNode, IRepositoryViewObject repositoryObject,
            RepositoryNode node) {
        Property property = repositoryObject.getProperty();
        if (type != ERepositoryObjectType.BEANSJAR || !(property.getItem() instanceof BeansJarItem)) {
            return;
        }
        try {
            List<IRepositoryViewObject> innerRoutinesObj = ProxyRepositoryFactory.getInstance()
                    .getAllInnerCodes(CodesJarInfo.create(property));
            for (IRepositoryViewObject innerRoutineObj : innerRoutinesObj) {
                Property innerRoutineProperty = innerRoutineObj.getProperty();
                RepositoryNode innerRoutineNode = new RepositoryNode(new RepositoryViewObject(innerRoutineProperty), node,
                        ENodeType.REPOSITORY_ELEMENT);
                innerRoutineNode.setProperties(EProperties.LABEL, innerRoutineProperty.getLabel());
                innerRoutineNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.BEANSJAR);
                node.getChildren().add(innerRoutineNode);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

}
