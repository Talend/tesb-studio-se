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
package org.talend.camel.designer.migration;

import java.util.ArrayList;
import java.util.List;

import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.migration.UnifyPasswordEncryption4ParametersInJobMigrationTask;

/**
 * created by ggu on Aug 25, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4ParametersInRouteMigrationTask extends UnifyPasswordEncryption4ParametersInJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(CamelRepositoryNodeType.repositoryRoutesType);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof CamelProcessItem) {
            ProcessType processType = ((CamelProcessItem) item).getProcess();
            return execute(item, processType);
        }
        return ExecutionResult.NOTHING_TO_DO;
    }
}
