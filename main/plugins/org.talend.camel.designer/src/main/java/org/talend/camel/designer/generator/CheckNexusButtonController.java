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
package org.talend.camel.designer.generator;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Button;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;

public class CheckNexusButtonController extends ConfigOptionController {

    private static Logger log = Logger.getLogger(CheckNexusButtonController.class);

    public CheckNexusButtonController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createCommand(Button button) {

        if (!isAvailable()) {
            return null;
        }

        IElementParameter parameter = (IElementParameter) button.getData();
        if (parameter != null) {
            callBeforeActive(parameter);
            // so as to invoke listeners to perform some actions.

            IElementParameter elementParameterFromField = elem.getElementParameter("DRIVER_JAR");

            List needUpdateJars = null;

            List jars = (List) elementParameterFromField.getValue();

            if (jars.size() > 0) {

                // refreshDynamicProperty();

                IElementParameter needUpdateList = elem.getElementParameter("NEED_UPDATE_LIST");

                needUpdateJars = (List) needUpdateList.getValue();

                needUpdateJars.clear();

                TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get("NEED_UPDATE_LIST");

                tableViewerCreator.refresh();

                List<ModuleNeeded> updatedModules = null;

                if (elem instanceof Node) {
                    updatedModules = LibrariesManagerUtils.getNotInstalledModules(((Node) elem));
                }

                if (nexusServerBean == null) {
                    return null;
                }

                try {
                    new ProgressMonitorDialog(button.getShell()).run(true, true,
                            getCheckNexusRunnableWithProgress(jars, needUpdateJars));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                tableViewerCreator.refresh();

            }

            if (needUpdateJars == null) {
                MessageDialog.openInformation(composite.getShell(), "Checking libraries", "No dependencies being added");
            } else {
                if (needUpdateJars != null && needUpdateJars.size() == 0) {
                    MessageDialog.openInformation(composite.getShell(), "Checking libraries", "Everything is up-to-date");
                }
            }

            return null;
        }
        return null;
    }
}
