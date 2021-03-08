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
package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.repository.model.RepositoryNode;

interface RouteResourceSelectionListener extends EventListener {
	void routeResourceNodeSelected(RepositoryNode resourceNode);
}

public class RouteResourcesButtonPart extends AbstractButtonPart<RouteResourceSelectionListener> {

	public RouteResourcesButtonPart(RouteResourceSelectionListener eventListener) {
		super(eventListener);
	}

	@Override
	protected String getMessageKey() {
		return "WebServiceUI.Resources";
	}

	@Override
	protected Image getImage() {
		return getImageFromBundle("org.talend.designer.camel.resource", "icons/route-resource.png");
	}

	@Override
	protected void buttonSelected(SelectionEvent e) {
		try {
			Class<?> dialogClass = Platform.getBundle("org.talend.camel.designer").loadClass(
					"org.talend.camel.designer.dialog.RouteResourceSelectionDialog");
			Dialog dialog = (Dialog) dialogClass.getConstructor(Shell.class).newInstance(getShell());
			if (dialog.open() == Dialog.OK) {
				RepositoryNode resourceNode = (RepositoryNode) dialogClass.getMethod("getResult").invoke(dialog);
				if (resourceNode != null) {
					listener.routeResourceNodeSelected(resourceNode);
				}
			}
		} catch (Exception e1) {
			ExceptionHandler.process(new IllegalStateException(
					"Can't load RouteResourceSelectionDialog from specific bundle", e1));
		}
	}
}
