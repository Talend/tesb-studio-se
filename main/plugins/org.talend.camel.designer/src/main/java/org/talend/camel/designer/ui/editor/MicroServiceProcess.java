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
package org.talend.camel.designer.ui.editor;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.process.LastGenerationInfo;

/**
 * created by sunchaoqun on Apr 19, 2016 Detailled comment
 *
 */
public class MicroServiceProcess extends RouteProcess {

    /**
     * DOC sunchaoqun MicroServiceProcess constructor comment.
     *
     * @param property
     */
    public MicroServiceProcess(Property property) {
        super(property);
    }

    public boolean isEnableMetrics() {
        Map<String, Object> argumentsMap = LastGenerationInfo.getInstance().getLastMainJob().getProcessor().getArguments();

        return ObjectUtils.equals(argumentsMap.get("ESB_METRICS"), new Boolean(true));

    }

}
