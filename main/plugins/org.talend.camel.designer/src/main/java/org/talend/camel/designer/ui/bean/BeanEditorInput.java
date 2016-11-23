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
package org.talend.camel.designer.ui.bean;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IFileEditorInput;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;

public class BeanEditorInput extends RepositoryEditorInput {

    public BeanEditorInput(IFile file, Item item) {
        super(file, item);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IFileEditorInput)) {
            return false;
        }
        IFileEditorInput other = (IFileEditorInput) obj;

        if (getFile().equals(other.getFile())) {
            return true;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }
}
