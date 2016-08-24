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
package org.talend.designer.esb.webservice.util;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;

import org.talend.utils.wsdl.WSDLLoader;
import org.xml.sax.InputSource;

/**
 * class WSDLHelper
 * 
 * @author amarkevich
 */
public class WSDLHelper {

    private WSDLHelper() {
    }

    public static Definition load(String wsdlLocation, String filenamePrefix) throws InvocationTargetException, WSDLException {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader newWSDLReader = wsdlFactory.newWSDLReader();

        newWSDLReader.setExtensionRegistry(wsdlFactory.newPopulatedExtensionRegistry());
        newWSDLReader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        return newWSDLReader.readWSDL(new InMemoryWSDLLocator(wsdlLocation, new WSDLLoader().load(wsdlLocation, filenamePrefix
                + "%d.wsdl")));
    }

    private static class InMemoryWSDLLocator implements WSDLLocator {

        private final Map<String, InputStream> definitions;

        private String latestImportURI;

        private String wsdlLocation;

        public InMemoryWSDLLocator(String wsdlLocation, Map<String, InputStream> definitions) {
            this.definitions = definitions;
            this.wsdlLocation = wsdlLocation;
        }

        @Override
        public void close() {
        }

        @Override
        public InputSource getBaseInputSource() {
            return new InputSource(definitions.get(WSDLLoader.DEFAULT_FILENAME));
        }

        @Override
        public String getBaseURI() {
            return wsdlLocation;
        }

        @Override
        public InputSource getImportInputSource(String parent, String importLocation) {
            latestImportURI = importLocation;
            return new InputSource(definitions.get(importLocation));
        }

        @Override
        public String getLatestImportURI() {
            return latestImportURI;
        }

    }
}
