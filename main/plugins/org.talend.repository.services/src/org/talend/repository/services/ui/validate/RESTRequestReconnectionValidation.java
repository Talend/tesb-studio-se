package org.talend.repository.services.ui.validate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.designer.core.IConnectionValidator;
import org.talend.repository.services.Messages;

public class RESTRequestReconnectionValidation implements IConnectionValidator {

	private static final String T_REST_REQUEST = "tRESTRequest";

	@Override
	public boolean canConnectToSource(INode oldSource, INode newSource,
			INode target, EConnectionType lineStyle, String connectorName,
			String connectionName) {
		if (oldSource != null
				&& newSource != null
				&& (T_REST_REQUEST.equals(newSource.getComponent().getName()) || T_REST_REQUEST
						.equals(oldSource.getComponent().getName()))) {
			ToolTip tooltip = new ToolTip(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING);
			tooltip.setText(Messages.RESTRequestReconnectionValidation_WarningMsg);
			// tooltip.setMessage("It's not allowed to reconnect to or from a tRESTRequest component");
			tooltip.setAutoHide(true);
			tooltip.setVisible(true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canConnectToTarget(INode source, INode oldTarget,
			INode newTarget, EConnectionType lineStyle, String connectorName,
			String connectionName) {
		return true;
	}

}
