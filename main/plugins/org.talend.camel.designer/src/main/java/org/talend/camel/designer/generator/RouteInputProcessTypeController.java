package org.talend.camel.designer.generator;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.talend.camel.designer.ui.wizards.AssignJobWizard;
import org.talend.camel.designer.ui.wizards.AssignJobWizardDialog;
import org.talend.camel.designer.util.CamelDesignerUtil;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.properties.controllers.ProcessController;
import org.talend.designer.runprocess.ItemCacheManager;

public class RouteInputProcessTypeController extends ProcessController {

    public RouteInputProcessTypeController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    protected Command createButtonCommand(Button button) {
        AssignJobWizard assignJobWizard = new AssignJobWizard();
        WizardDialog wizardDialog = new AssignJobWizardDialog(button.getShell(), assignJobWizard);
        if (wizardDialog.open() == WizardDialog.OK) {
            String id = assignJobWizard.getSelectedProcessId();
            if (id != null) {
                String paramName = (String) button.getData(PARAMETER_NAME);
                return new PropertyChangeCommand(elem, paramName, id);
            }
        }
        return null;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        IElementParameter processTypeParameter = param.getChildParameters().get(EParameterName.PROCESS_TYPE_PROCESS.getName());
        if (processTypeParameter == null) {
            super.refresh(processTypeParameter, check);
            return;
        }
        Object processId = processTypeParameter.getValue();
        if (processId == null) {
            super.refresh(processTypeParameter, check);
            return;
        }
        ProcessItem pi = ItemCacheManager.getProcessItem(processId.toString());
        if (!CamelDesignerUtil.checkRouteInputExistInJob(pi)) {
            clearAll(param, processTypeParameter);
        } else {
            super.refresh(param, check);
        }
    }

    private void clearAll(IElementParameter param, IElementParameter processTypeParameter) {
        Text jobName = (Text) hashCurControls.get(param.getName() + ":" + processTypeParameter.getName()); //$NON-NLS-1$
        CCombo contextCombo = (CCombo) hashCurControls
                .get(param.getChildParameters().get(EParameterName.PROCESS_TYPE_CONTEXT.getName()).getName());
        CCombo versionCombo = (CCombo) hashCurControls
                .get(param.getChildParameters().get(EParameterName.PROCESS_TYPE_VERSION.getName()).getName());
        jobName.setText("");
        contextCombo.removeAll();
        versionCombo.removeAll();
        param.setValue("");
        Map<String, IElementParameter> childParameters = param.getChildParameters();
        Iterator<String> iterator = childParameters.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            IElementParameter nextPara = childParameters.get(next);
            nextPara.setValue("");
        }
    }
}
