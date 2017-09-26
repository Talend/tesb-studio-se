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
package org.talend.camel.designer.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsHandler;

/**
 * created by nrousseau on Jan 16, 2013 Detailled comment
 * 
 */
public class CamelComponentsHandler implements IComponentsHandler {

    // private List<IComponent> camelComponents = new ArrayList<IComponent>();

    @Override
    public List<IComponent> filterComponents(List<IComponent> allComponents) {
        List<IComponent> camelComponents = new ArrayList<IComponent>();
        if (allComponents == null || allComponents.isEmpty()) {
            return camelComponents;
        }
        final String categoryName = extractComponentsCategory().getName();
        for (IComponent component : allComponents) {
            if (categoryName.equals(component.getPaletteType())) {
                camelComponents.add(component);
            }
        }
        return camelComponents;
    }

    @Override
    public List<IComponent> sortComponents(List<IComponent> filteredComponents) {
        return filteredComponents;
    }

    @Override
    public ComponentCategory extractComponentsCategory() {
        return ComponentCategory.CATEGORY_4_CAMEL;
    }

}
