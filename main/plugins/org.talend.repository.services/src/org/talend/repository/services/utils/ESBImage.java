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
package org.talend.repository.services.utils;

import org.talend.commons.ui.runtime.image.IImage;

public enum ESBImage implements IImage {
                                        SERVICE_ICON("/icons/unknown.gif"); //$NON-NLS-1$

    private String path;

    ESBImage() {
        this.path = "/icons/unknown.gif"; //$NON-NLS-1$
    }

    ESBImage(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public Class getLocation() {
        return ESBImage.class;
    }
}
