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
import org.talend.commons.CommonsPlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.utils.RoutineUtils;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.utils.ProductUtils;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class BeansJarImportHandler extends ImportRepTypeHandler {

    public BeansJarImportHandler() {
        super();
    }

    /**
     * override to use this handler import BeansJar and inner Beans
     */
    @Override
    public boolean valid(ImportItem importItem) {
        if (importItem == null || importItem.getItem() == null || importItem.getType() == null) {
            return false;
        }
        ERepositoryObjectType itemType = importItem.getType();
        if (itemType == ERepositoryObjectType.BEANSJAR || RoutinesUtil.isInnerCodes(importItem.getProperty())) {
            // if in studio, check the product.
            if (!CommonsPlugin.isHeadless() && isEnableProductChecking()) {
                String currentPerspectiveId = CoreRuntimePlugin.getInstance().getActivedPerspectiveId();
                String[] products = itemType.getProducts();
                if (products != null) {
                    for (String product : products) {
                        String perspectiveId = ProductUtils.getPerspectiveId(product);
                        if (currentPerspectiveId != null && currentPerspectiveId.equals(perspectiveId)) {
                            return true; // match the product and perspective.
                        }
                    }
                    return false; // if enable product check, have set the product.
                }
            }
            return true;
        }

        return false;
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
        // inner Beans to change package name
        if (RoutinesUtil.isInnerCodes(importItem.getProperty())) {
            RoutineUtils.changeInnerCodePackage(importItem.getItem(), true);
            return;
        }

        // import jar for BeansJar
//        if (importItem != null && importItem.getItem() != null && importItem.getItem() instanceof BeansJarItem) {
//            EList<IMPORTType> imports = ((BeansJarItem) importItem.getItem()).getRoutinesJarType().getImports();
//            BeansImportHandlerUtil.arrangeBeansMoudulesImport(imports);
//        }
    }

}
