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

import java.io.IOException;

import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.model.IRouteProcess;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.editor.process.Process;

public class RouteProcess extends Process implements IRouteProcess {

    private String springContent = null;

    public RouteProcess(Property property) {
        super(property);
        loadSpringContent();
        setComponentsType(ComponentCategory.CATEGORY_4_CAMEL.getName());
    }

    @Override
    protected void createJobSettingsParameters() {
        // no job settings for routes
    }

    @Override
    public String getBaseHelpLink() {
        return "org.talend.esb.help.";
    }

    @Override
    public String getSpringContent() {
        return springContent;
    }

    @Override
    public void setSpringContent(String springContent) {
        this.springContent = springContent;
    }

    /*
     * used to load spring content when opening Editor
     */
    private final void loadSpringContent() {
        if (getProperty().getItem() instanceof CamelProcessItem) {
            springContent = ((CamelProcessItem) getProperty().getItem()).getSpringContent();
        }
    }

    @Override
    public ProcessType saveXmlFile() throws IOException {
        if (getProperty().getItem() instanceof CamelProcessItem) {
            ((CamelProcessItem) getProperty().getItem()).setSpringContent(springContent);
        }
        return super.saveXmlFile();
    }
    // END ADDED for TESB-7887

    @Override
    public String getElementName() {
        if (getProperty().getItem() instanceof CamelProcessItem) {
            return Messages.getString("MultiPageTalendEditor.Route"); //$NON-NLS-1$
        } else {
            return Messages.getString("MultiPageTalendEditor.Routelet"); //$NON-NLS-1$
        }
    }

    @Override
    public boolean isSubjobEnabled() {
        return false;
    }

}
