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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.camel.dependencies.core.DependenciesResolver;
import org.talend.designer.camel.dependencies.core.model.BundleClasspath;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;
import org.talend.designer.camel.dependencies.core.util.DependenciesCoreUtil;
import org.talend.designer.camel.resource.core.model.ResourceDependencyModel;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;
import org.talend.designer.core.ICamelDesignerCoreService;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class CamelDesignerCoreService implements ICamelDesignerCoreService {

    @Override
    public String getDeleteFolderName(ERepositoryObjectType type) {
        return type.getKey();
    }

    @Override
    public ERepositoryObjectType getBeansType() {
        return CamelRepositoryNodeType.repositoryBeansType;
    }

    @Override
    public ERepositoryObjectType getRouteDocType() {
        return CamelRepositoryNodeType.repositoryDocumentationType;
    }

    @Override
    public ERepositoryObjectType getRouteDocsType() {
        return CamelRepositoryNodeType.repositoryDocumentationType;
    }

    @Override
    public boolean isInstanceofCamelRoutes(Item item) {
        if (item == null) {
            return false;
        }
        return isCamelRouteProcess(item) || isRouteletProcess(item);
    }

    public boolean isCamelRouteProcess(Item item) {
        return item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM;
    }

    public boolean isRouteletProcess(Item item) {
        return item.eClass() == CamelPropertiesPackage.Literals.ROUTELET_PROCESS_ITEM;
    }

    @Override
    public boolean isInstanceofCamelBeans(Item item) {
        if (item == null) {
            return false;
        }
        return item.eClass() == CamelPropertiesPackage.Literals.BEAN_ITEM;
    }

    @Override
    public boolean isInstanceofCamelBeansJar(Item item) {
        return item == null ? false : item.eClass() == CamelPropertiesPackage.Literals.BEANS_JAR_ITEM;
    }

    @Override
    public boolean isInstanceofCamel(Item item) {
        if (item == null) {
            return false;
        }
        return isInstanceofCamelRoutes(item) || isInstanceofCamelBeans(item)
                || item.eClass() == CamelPropertiesPackage.Literals.ROUTE_RESOURCE_ITEM;
    }

    @Override
    public void synchronizeRouteResource(ProcessItem item) {
        RouteResourceUtil.synchronizeRouteResource(item);
    }

    @Override
    public boolean isRouteBuilderNode(INode node) {
        return ComponentCategory.CATEGORY_4_CAMEL.getName().equals(node.getProcess().getComponentsType());
    }

    @Override
    public boolean canCreateNodeOnLink(IConnection connection, INode node) {
        INodeConnector connector = node.getConnectorFromType(EConnectionType.ROUTE);
        if (connector.getMaxLinkOutput() > 0) {
            return true;
        }
        connector = node.getConnectorFromType(EConnectionType.ROUTE_ENDBLOCK);
        if (connector.getMaxLinkOutput() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public EConnectionType getTargetConnectionType(INode node) {
        INodeConnector connector = node.getConnectorFromType(EConnectionType.ROUTE);
        if (connector.getMaxLinkOutput() > 0) {
            return EConnectionType.ROUTE;
        }
        connector = node.getConnectorFromType(EConnectionType.ROUTE_ENDBLOCK);
        if (connector.getMaxLinkOutput() > 0) {
            return EConnectionType.ROUTE_ENDBLOCK;
        }
        return EConnectionType.ROUTE;
    }

    @Override
    public void appendRouteInfo2Doc(Item item, Element jobElement) {
        addSpringContent(item, jobElement);
        addManifestContent(item, jobElement);
        addResourcesContent(item, jobElement);
    }

    private void addResourcesContent(Item item, Element jobElement) {
        Element resourcesElement = jobElement.addElement("RouteResources");

        for (ResourceDependencyModel resource : RouteResourceUtil.getResourceDependencies((ProcessItem) item)) {
            Element resourceElement = resourcesElement.addElement("Resource");
            resourceElement.addAttribute("name", resource.toString());
            resourceElement.addAttribute("version", resource.getSelectedVersion());
            resourceElement.addAttribute("path", resource.getClassPathUrl());
        }
    }

    private void addManifestContent(Item item, Element jobElement) {
        Element manifestElement = jobElement.addElement("RouteManifest");
        manifestElement.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");

        final DependenciesResolver resolver = new DependenciesResolver((ProcessItem) item);
        manifestElement.addElement(ManifestItem.IMPORT_PACKAGE).addText(resolver.getManifestImportPackage('\n'));
        manifestElement.addElement(ManifestItem.EXPORT_PACKAGE).addText(resolver.getManifestExportPackage('\n'));
        manifestElement.addElement(ManifestItem.REQUIRE_BUNDLE).addText(resolver.getManifestRequireBundle('\n'));
        manifestElement.addElement(ManifestItem.BUNDLE_CLASSPATH).addText(resolver.getManifestBundleClasspath('\n'));
    }

    private void addSpringContent(Item item, Element jobElement) {
        Element routeSpringElement = jobElement.addElement("RouteSpring");
        routeSpringElement.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
        String springContent = ((CamelProcessItem) item).getSpringContent();
        routeSpringElement.addText(springContent);
    }

    @Override
    public FileItem newRouteDocumentationItem() {
        return CamelPropertiesFactory.eINSTANCE.createRouteDocumentItem();
    }

    @Override
    public Collection<String> getUnselectDependenciesBundle(ProcessItem processItem) {
        Collection<String> unSelectedBundles = new ArrayList();

        DependenciesResolver resolver = new DependenciesResolver(processItem);

        Map<?, ?> additionProperties = processItem.getProperty().getAdditionalProperties().map();
        Collection<BundleClasspath> userBundleClasspaths = DependenciesCoreUtil.getStoredBundleClasspaths(additionProperties);
        Collection<BundleClasspath> bundleClasspaths = resolver.getBundleClasspaths();
        for (BundleClasspath bc : bundleClasspaths) {

            if (!userBundleClasspaths.contains(bc)) {
                unSelectedBundles.add(bc.getName());
            }
        }

        return unSelectedBundles;
    }

}
