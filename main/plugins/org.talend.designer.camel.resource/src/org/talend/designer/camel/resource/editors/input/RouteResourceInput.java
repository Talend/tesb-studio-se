// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.camel.resource.editors.input;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeListener;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;
import org.talend.designer.camel.resource.ui.util.RouteResourceEditorUtil;
import org.talend.repository.model.IRepositoryNode;

public class RouteResourceInput extends RepositoryEditorInput {

	private IResourceChangeListener listener;

	protected RouteResourceInput(IFile file, Item item) {
		super(file, item);
	}

	public static RouteResourceInput createInput(IRepositoryNode node, RouteResourceItem item) {
		RouteResourceInput routeResourceInput = new RouteResourceInput(
				RouteResourceUtil.getSourceFile(item), item);
		if(node!=null) {
			routeResourceInput.setRepositoryNode(node);
			routeResourceInput.setReadOnly(RouteResourceEditorUtil.isReadOnly(node));
		}
		return routeResourceInput;
	}

	public void setListener(IResourceChangeListener listener) {
		this.listener = listener;
	}

	public IResourceChangeListener getListener() {
		return listener;
	}

	public String getName() {
		String label = (getItem() == null ? "" : getItem().getProperty()
				.getLabel());
		String version = (getItem() == null ? "0.1" : getItem().getProperty()
				.getVersion());
		return label + " " + version+ (isReadOnly()?" (ReadOnly)":"");
	}
}
