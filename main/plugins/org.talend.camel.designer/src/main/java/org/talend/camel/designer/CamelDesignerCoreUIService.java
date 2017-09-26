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
package org.talend.camel.designer;

import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.editor.CamelMultiPageTalendEditor;
import org.talend.camel.designer.ui.editor.CamelProcessEditorInput;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.IRouteletService;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.ICamelDesignerCoreUIService;

/**
 * created by cmeng on Nov 25, 2015 Detailled comment
 *
 */
public class CamelDesignerCoreUIService implements ICamelDesignerCoreUIService {

    /**
     * use it by getRouteletService method
     */
    private IRouteletService _routeletService;

    /**
     * use it by getCamelDesignerCoreService method
     */
    private ICamelDesignerCoreService _camelDesignerCoreService;

    private IRouteletService getRouteletService() {
        if (_routeletService == null && GlobalServiceRegister.getDefault().isServiceRegistered(IRouteletService.class)) {
            _routeletService = (IRouteletService) GlobalServiceRegister.getDefault().getService(IRouteletService.class);
        }
        return _routeletService;
    }

    private ICamelDesignerCoreService getCamelDesignerCoreService() {
        if (_camelDesignerCoreService == null && GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            _camelDesignerCoreService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
        }
        return _camelDesignerCoreService;
    }

    @Override
    public boolean isInstanceofCamelRoutes(Item item) {
        return getCamelDesignerCoreService().isInstanceofCamelRoutes(item);
    }

    @Override
    public JobEditorInput getRouteEditorInput(ProcessItem processItem, boolean load, Boolean lastVersion)
            throws PersistenceException {
        if (processItem == null) {
            return null;
        }

        JobEditorInput editorInput = null;
        if (getCamelDesignerCoreService().isInstanceofCamelRoutes(processItem)) {
            // both route and routelet use CamelProcessEditorInput
            editorInput = new CamelProcessEditorInput(processItem, load, lastVersion);
        }

        return editorInput;
    }

    @Override
    public String getRouteEditorId(ProcessItem processItem) {
        CamelDesignerCoreService iCamelDesignerCoreSerice = (CamelDesignerCoreService) getCamelDesignerCoreService();
        if (processItem == null || iCamelDesignerCoreSerice.isCamelRouteProcess(processItem)) {
            return CamelMultiPageTalendEditor.ID;
        } else if (iCamelDesignerCoreSerice.isRouteletProcess(processItem)) {
            IRouteletService service = getRouteletService();
            if (service != null) {
                return service.getRouteletEditorId();
            } else {
                ExceptionHandler.process(new Throwable(Messages.getString("CamelDesignerCoreService_noRouteletServiceFound"))); //$NON-NLS-1$
            }
        }
        return null;
    }
}
