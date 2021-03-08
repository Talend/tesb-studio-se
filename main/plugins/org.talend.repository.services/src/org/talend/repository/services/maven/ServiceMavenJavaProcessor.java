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
package org.talend.repository.services.maven;

import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.EmptyContext;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.services.model.services.impl.ServiceItemImpl;

public class ServiceMavenJavaProcessor extends MavenJavaProcessor {

    private EmptyContext emptyContext;

    /**
     * @param process always be null
     * @param property
     * @param filenameFromLabel
     */
    public ServiceMavenJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(new Process(property), property, filenameFromLabel);
        this.emptyContext = new EmptyContext();
        emptyContext.setName(IContext.DEFAULT);
        setContext(emptyContext);
    }

    @Override
    protected boolean isStandardJob() {
        return property != null && property.getItem() != null && property.getItem() instanceof ServiceItemImpl;
    }
}
