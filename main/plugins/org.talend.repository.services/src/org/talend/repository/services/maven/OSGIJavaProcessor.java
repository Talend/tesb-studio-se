// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.maven;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.utils.JobContextUtils;

public class OSGIJavaProcessor extends MavenJavaProcessor {

    /**
     * DOC sunchaoqun OSGIJavaProcessor constructor comment.
     * 
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public OSGIJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    @Override
    public void generateCode(boolean statistics, boolean trace, boolean javaProperties, int option) throws ProcessorException {
        super.generateCode(statistics, trace, javaProperties, option);

        IProgressMonitor monitor = new NullProgressMonitor();
        ProcessItem processItem = (ProcessItem) property.getItem();
        FeaturesModel featuresModel = new FeaturesModel(PomIdsHelper.getJobGroupId(processItem.getProperty()),
                processItem.getProperty().getDisplayName(), PomIdsHelper.getJobVersion(processItem.getProperty()));
        featuresModel.setConfigName(this.context.getName());
        featuresModel.setContexts(JobContextUtils.getContextsMap(processItem));
        BundleModel bundleModel = new BundleModel(PomIdsHelper.getJobGroupId(processItem.getProperty()),
                processItem.getProperty().getDisplayName(), PomIdsHelper.getJobVersion(processItem.getProperty()));
        featuresModel.addBundle(bundleModel);
        IFile feature = getTalendJavaProject().createSubFolder(monitor, getTalendJavaProject().getResourcesFolder(), "feature")
                .getFile("feature.xml");

        PomIdsHelper.getJobVersion(processItem.getProperty());
        try {
            if (feature.exists()) {
                feature.setContents(featuresModel.getContent(), 0, monitor);
            } else {
                feature.create(featuresModel.getContent(), 0, monitor);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        // Delete microservice launcher for OSGi type running in studio
        String packageFolder = JavaResourcesHelper.getJobClassPackageFolder(property.getItem(), true);
        IFolder srcFolder = getTalendJavaProject().getSrcSubFolder(null, packageFolder);
        IFile app = srcFolder.getFile("App.java");
        if (app.exists()) {
            try {
                app.delete(true, monitor);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> getArguments() {
        Map<String, Object> argumentsMap = super.getArguments();
        if (argumentsMap == null) {
            argumentsMap = new HashMap<String, Object>();
        }
        if (!argumentsMap.containsKey(TalendProcessArgumentConstant.ARG_BUILD_TYPE)) {
            argumentsMap.put(TalendProcessArgumentConstant.ARG_BUILD_TYPE, "OSGI");
        }
        return argumentsMap;
    }
}
