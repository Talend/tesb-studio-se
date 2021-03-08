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
package org.talend.repository.services.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IFileEditorInput;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;

public class ServiceEditorInput extends RepositoryEditorInput {

    public ServiceEditorInput(IFile file, Item item) {
        super(file, item);
    }

    @Override
    public String getName() {
        return getFile().getName() + (isReadOnly() ? " (ReadOnly)" : "");
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof IFileEditorInput) {
            return getFile().equals(((IFileEditorInput) obj).getFile());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }
}
