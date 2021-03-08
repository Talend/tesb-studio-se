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
package org.talend.camel.designer.ui.wizards.export;

import java.io.IOException;
import java.util.Map;

import org.talend.core.model.properties.ProcessItem;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;

import aQute.bnd.osgi.Analyzer;

public class AdaptedJobJavaScriptOSGIForESBManager extends JobJavaScriptOSGIForESBManager {

    private String bundleName = null;
    private String bundleSymbolicName = null;
    private String osgiServiceIdSuffix = null;

	public AdaptedJobJavaScriptOSGIForESBManager(Map<ExportChoice, Object> exportChoiceMap, String contextName,
			String launcher, int statisticPort, int tracePort) {
		super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
	}

    public String getBundleName() {
    	return bundleName;
    }

    public void setBundleName(String bundleName) {
    	this.bundleName = bundleName;
    }

    public String getBundleSymbolicName() {
    	return bundleSymbolicName;
    }

    public void setBundleSymbolicName(String bundleSymbolicName) {
    	this.bundleSymbolicName = bundleSymbolicName;
    }

    public String getOsgiServiceIdSuffix() {
    	return osgiServiceIdSuffix;
    }

    public void setOsgiServiceIdSuffix(String osgiServiceIdSuffix) {
    	this.osgiServiceIdSuffix = osgiServiceIdSuffix;
    }

    @Override
    protected Analyzer createAnalyzer(ExportFileResource libResource, ProcessItem processItem) throws IOException {
        Analyzer analyzer = super.createAnalyzer(libResource, processItem);
        if (bundleName != null && bundleName.length() > 0) {
            analyzer.setProperty(Analyzer.BUNDLE_NAME, bundleName);
        }
        if (bundleSymbolicName != null && bundleSymbolicName.length() > 0) {
            analyzer.setProperty(Analyzer.BUNDLE_SYMBOLICNAME, bundleSymbolicName);
        }
        return analyzer;
    }
}
