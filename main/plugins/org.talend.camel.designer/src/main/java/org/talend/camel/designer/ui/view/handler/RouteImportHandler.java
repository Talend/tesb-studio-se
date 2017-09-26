// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.view.handler;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.runtime.model.emf.TalendXMIResource;
import org.talend.repository.items.importexport.handlers.HandlerUtil;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;

public class RouteImportHandler extends ImportRepTypeHandler {

    @Override
    protected Resource createItemResource(URI pathUri) {
        // maybe, because it's same job,joblet
        return new TalendXMIResource(pathUri);
    }

    @Override
    protected boolean copyReferenceFiles(ResourcesManager resManager, ImportItem selectedItemRecord) throws IOException {
        HandlerUtil.copyScreenshotFile(resManager, selectedItemRecord);
        return super.copyReferenceFiles(resManager, selectedItemRecord);
    }

}
