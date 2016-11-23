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
package org.talend.camel.designer.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsHandler;

public class CamelComponentsHandler implements IComponentsHandler {

    @Override
    public List<IComponent> filterComponents(List<IComponent> allComponents) {
        List<IComponent> camelComponents = new ArrayList<>();
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
