// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.XSDSchemaExtensibilityElement;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;
import org.eclipse.wst.wsdl.ui.internal.text.WSDLModelLocatorAdapterFactory;
import org.eclipse.wst.xsd.ui.internal.util.XSDSchemaLocationResolverAdapterFactory;
import org.eclipse.xsd.XSDSchema;
import org.talend.datatools.xml.utils.XSDPopulationUtil2;

public class WSDLPopulationUtil extends XSDPopulationUtil2 {

    private boolean loadFromWSDL;

    public WSDLPopulationUtil() {
        resourceSet = new ResourceSetImpl();
        resourceSet.getAdapterFactories().add(new WSDLModelLocatorAdapterFactory());
        resourceSet.getAdapterFactories().add(new XSDSchemaLocationResolverAdapterFactory());
    }

    @Override
    public XSDSchema getXSDSchema(String fileName, boolean forceReload) throws URISyntaxException, MalformedURLException {
        return getXSDSchemaFromNamespace(fileName);
    }

    @Override
    public XSDSchema getXSDSchemaFromNamespace(String namespace) {
        if (loadFromWSDL) {
            if (resourceSet.getResources().size() == 1) {
                Resource resource = resourceSet.getResources().get(0);
                if (resource.getContents().size() == 1) {
                    Object oDef = resource.getContents().get(0);
                    if (oDef instanceof Definition) {
                        Definition definition = (Definition) oDef;
                        for (Object o : definition.getETypes().getEExtensibilityElements()) {
                            XSDSchemaExtensibilityElement schema = (XSDSchemaExtensibilityElement) o;
                            if (schema.getSchema().getTargetNamespace().equals(namespace)) {
                                return schema.getSchema();
                            }
                        }
                    }
                }
            }
            return null;
        }
        return super.getXSDSchemaFromNamespace(namespace);
    }

    @Override
    public void loadWSDL(String wsdlFile) throws IOException {
        WSDLResourceFactoryImpl resourceFactory = new WSDLResourceFactoryImpl();
        Resource resource = resourceFactory.createResource(URI.createURI(wsdlFile));
        resourceSet.getResources().add(resource);
        resource.load(null);
        loadFromWSDL = true;
    }
}
