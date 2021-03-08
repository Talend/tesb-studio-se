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
package org.talend.camel.designer.ui.view.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.runtime.model.emf.TalendXMIResource;
import org.talend.core.model.repository.ERepositoryObjectType;
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

    @Override
    public List<ImportItem> findRelatedImportItems(IProgressMonitor monitor, ResourcesManager resManager, ImportItem importItem,
            ImportItem[] allImportImportItems) throws Exception {

        // to fix cTalendJob issue TESB-21780, we need toimport job first before route, to launch
        // UpdateBuildTypeForCTalendJobMigrationTask
        List<ImportItem> jobImportItems = new ArrayList<ImportItem>();
        for (ImportItem item : allImportImportItems) {
            if (!item.isImported()) {
                if (item.getType() == ERepositoryObjectType.PROCESS) {
                    jobImportItems.add(item);
                }
            }
        }
        return jobImportItems.size() > 0 ? jobImportItems
                : super.findRelatedImportItems(monitor, resManager, importItem, allImportImportItems);
    }
}
