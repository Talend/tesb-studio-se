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
package org.talend.designer.esb.webservice;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.esb.webservice.ui.dialog.WebServiceDialog;

public class WebServiceNode extends AbstractExternalNode {

    public int open(Display display) { // button event
        return open(display.getActiveShell());
    }

    public int open(Composite parent) {// double click in job
       return open(parent.getShell());
    }

    private int open(Shell shell) {
    	setParamValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);

        WizardDialog wizardDialog = new WizardDialog(shell, new WebServiceDialog(this));
        return (Window.OK == wizardDialog.open()) ? SWT.OK : SWT.CANCEL;
    }

    public String getParamStringValue(String key) {
        final Object parameterValue = getParamValue(key);
        if (parameterValue instanceof String) {
            return StringUtils.trimToNull((String) parameterValue);
        }
        return null;
    }

    private Object getParamValue(String key) {
        final IElementParameter parameter = getElementParameter(key);
        return parameter == null ? null : parameter.getValue();
    }

    public void setParamValue(String key, Object value) {
        final IElementParameter parameter = getElementParameter(key);
        if (parameter != null) {
            parameter.setValue(value);
        }
    }

	public boolean getBooleanValue(String key) {
		Object value = getParamValue(key);
		if(value == null) {
			return false;
		}
		if(value instanceof Boolean) {
			return (Boolean) value;
		}
		return BooleanUtils.toBoolean(value.toString());
	}

    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
    	throw new UnsupportedOperationException();
    }

    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath) {
        return null;
    }

    public void initialize() {
    }

    public void renameInputConnection(String oldName, String newName) {
    }

    public void renameOutputConnection(String oldName, String newName) {
    }

    public void setExternalData(IExternalData persistentData) {
    }

    public IExternalData getTMapExternalData() {
        return null;
    }

    public void metadataOutputChanged(IMetadataTable currentMetadata) {
    }

}
