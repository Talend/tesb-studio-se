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

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class BeansJarImportHandler extends ImportRepTypeHandler {

    public BeansJarImportHandler() {
        super();
    }

    @Override
    public boolean valid(ImportItem importItem) {
        boolean valid = super.valid(importItem);
        if (valid) {
            ERepositoryObjectType itemType = importItem.getType();
            if (itemType != null && itemType == ERepositoryObjectType.BEANSJAR) {
                return true;
            }
        }
        return false;
    }

}
