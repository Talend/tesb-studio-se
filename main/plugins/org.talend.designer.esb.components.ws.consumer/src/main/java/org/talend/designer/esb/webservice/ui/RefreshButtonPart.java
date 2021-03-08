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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.EImage;

interface RefreshEventListener extends EventListener{
	void refreshTriggered();
}

public class RefreshButtonPart extends AbstractButtonPart<RefreshEventListener> {
	public RefreshButtonPart(RefreshEventListener eventListener) {
		super(eventListener);
	}

	@Override
	protected void buttonSelected(SelectionEvent e) {
		listener.refreshTriggered();
	}

	@Override
	protected String getMessageKey() {
		return null;
	}

	@Override
	protected Image getImage() {
		return getImageFromIImage(EImage.REFRESH_ICON);
	}
}
