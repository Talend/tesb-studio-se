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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.designer.ui.view.handler.util.BeansImportHandlerUtil;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BeanImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu BeanImportHandler constructor comment.
     */
    public BeanImportHandler() {
        super();
    }

    @Override
    public boolean valid(ImportItem importItem) {
        boolean valid = super.valid(importItem);
        if (valid && RoutinesUtil.isInnerCodes(importItem.getProperty())) {
            return false;
        }
        return valid;
    }

    /*
     * Update import bean libraries version with the studio inside versions.(TESB-23162)
     *
     * @see
     * org.talend.repository.items.importexport.handlers.imports.ImportBasicHandler#afterImportingItems(org.eclipse.core
     * .runtime.IProgressMonitor, org.talend.repository.items.importexport.manager.ResourcesManager,
     * org.talend.repository.items.importexport.handlers.model.ImportItem)
     */
    @Override
    public void afterImportingItems(IProgressMonitor monitor, ResourcesManager resManager, ImportItem importItem) {
        ModulesNeededProvider.fireLibrariesChanges();
    }

    /*
     * Update import bean libraries version with the studio inside versions.(TESB-23162)
     */
    @Override
    protected void beforeCreatingItem(ImportItem importItem) {
        super.beforeCreatingItem(importItem);
        if (importItem != null && importItem.getItem() != null && importItem.getItem() instanceof BeanItem) {
            EList<IMPORTType> imports = ((BeanItem) importItem.getItem()).getImports();
            BeansImportHandlerUtil.arrangeBeansMoudulesImport(imports);
        }
    }
}
