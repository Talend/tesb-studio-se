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
package org.talend.repository.services.model.services.util;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * DOC dlin class global comment. Detailled comment
 */
public enum EServiceCoreImage implements IImage {
    SERVICE_ICON("/icons/services.png"), //$NON-NLS-1$
    SERVICE_WIZ("/icons/services_big.png"),
    PORT_ICON("/icons/port.gif"),
    OPERATION_ICON("/icons/operation.gif");

    private String path;

    EServiceCoreImage() {
        this.path = "/icons/unknown.png"; //$NON-NLS-1$
    }

    EServiceCoreImage(String path) {
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
        return EServiceCoreImage.class;
    }
}
