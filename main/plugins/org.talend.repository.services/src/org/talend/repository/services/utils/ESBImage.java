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
package org.talend.repository.services.utils;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * DOC hwang class global comment. Detailled comment
 */
public enum ESBImage implements IImage {
    SERVICE_ICON("/icons/unknown.gif"); //$NON-NLS-1$

    private String path;

    ESBImage() {
        this.path = "/icons/unknown.gif"; //$NON-NLS-1$
    }

    ESBImage(String path) {
        this.path = path;
    }

    /**
     * Getter for path.
     * 
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for clazz.
     * 
     * @return the clazz
     */
    public Class getLocation() {
        return ESBImage.class;
    }
}
