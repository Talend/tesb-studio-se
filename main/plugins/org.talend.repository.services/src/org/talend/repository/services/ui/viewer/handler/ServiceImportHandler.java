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
package org.talend.repository.services.ui.viewer.handler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.migration.IMigrationTask.ExecutionResult;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ServiceImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu ServiceImportHandler constructor comment.
     */
    public ServiceImportHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see org.talend.repository.items.importexport.handlers.imports.AbstractImportExecutableHandler#isPriorImportRelatedItem()
     */
    @Override
    public boolean isPriorImportRelatedItem() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.talend.repository.items.importexport.handlers.imports.ImportBasicHandler#afterImportingItems(org.eclipse.core.runtime.IProgressMonitor, org.talend.repository.items.importexport.manager.ResourcesManager, org.talend.repository.items.importexport.handlers.model.ImportItem)
     */
    @Override
    public void afterImportingItems(IProgressMonitor monitor, ResourcesManager resManager, ImportItem importItem) {
        super.afterImportingItems(monitor, resManager, importItem);
        if (importItem.getItem() instanceof ServiceItem) {

            ServiceItem serviceItem = (ServiceItem) importItem.getItem();

            ServiceConnection connection = (ServiceConnection) serviceItem.getConnection();

            EList<ServicePort> listPort = connection.getServicePort();

            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

            try {
                for (ServicePort port : listPort) {
                    List<ServiceOperation> listOperation = port.getServiceOperation();
                    for (ServiceOperation operation : listOperation) {
                        if (StringUtils.isNotEmpty(operation.getReferenceJobId())) {
                            IRepositoryViewObject node;
                            node = factory.getLastVersion(operation.getReferenceJobId());
                            if (node != null) {
                                // check if need to remove from parent pom
                                IFile jobPom = AggregatorPomsHelper.getItemPomFolder(node.getProperty())
                                        .getFile(TalendMavenConstants.POM_FILE_NAME);
                                AggregatorPomsHelper.removeFromParentModules(jobPom);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }
}
