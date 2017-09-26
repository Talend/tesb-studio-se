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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.maven.utils.PomUtil;

public class SyncNexusButtonController extends ConfigOptionController {

    public SyncNexusButtonController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createCommand(Button button) {
        IElementParameter parameter = (IElementParameter) button.getData();
        if (parameter != null) {
            callBeforeActive(parameter);
            // so as to invoke listeners to perform some actions.

            IElementParameter elementParameterFromField = elem.getElementParameter("DRIVER_JAR");
            IElementParameter needUpdateList = elem.getElementParameter("NEED_UPDATE_LIST");

            List needUpdateJars = (List) needUpdateList.getValue();

            if (needUpdateJars == null) {
                MessageDialog.openInformation(composite.getShell(), "Checking libraries", "No dependencies being added");
                return null;
            }

            if (needUpdateJars != null && needUpdateJars.size() == 0) {
                MessageDialog.openInformation(composite.getShell(), "Synchronizing libraries", "Everything is up-to-date");
            } else {
                List<Map<String, String>> driverJars = (List) elementParameterFromField.getValue();
                try {
                    new ProgressMonitorDialog(button.getShell()).run(true, true,
                            new RunnableWithProgress(driverJars, needUpdateJars));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            needUpdateJars.clear();

            TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get("NEED_UPDATE_LIST");

            tableViewerCreator.refresh();

            try {
                new ProgressMonitorDialog(button.getShell()).run(true, true,
                        getCheckNexusRunnableWithProgress((List) elementParameterFromField.getValue(), needUpdateJars));
            } catch (Exception e) {
                e.printStackTrace();
            }

            tableViewerCreator.refresh();

            return null;
        }
        return null;
    }

    private class RunnableWithProgress implements IRunnableWithProgress {

        private List<Map<String, String>> jars;

        private List needUpdateJars;

        public RunnableWithProgress(List jars, List needUpdateJars) {
            this.jars = jars;
            this.needUpdateJars = needUpdateJars;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            monitor.beginTask("Syncing the nexus server...", false ? IProgressMonitor.UNKNOWN : jars.size());

            for (int i = 0; i < needUpdateJars.size(); i++) {
                Map<String, Object> jar = (Map) needUpdateJars.get(i);

                String jn = TalendQuoteUtils.removeQuotes(jar.get("JAR_NAME").toString());
                String jnv = TalendQuoteUtils.removeQuotes(jar.get("JAR_NEXUS_VERSION").toString());
                String jnpv = TalendQuoteUtils.removeQuotes(jar.get("JAR_NEXUS_PRE_VERSION").toString());
                String a = jn.replaceFirst("[.][^.]+$", "");

                if (Boolean.valueOf(jar.get("JAR_SYNC").toString())) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
                        ILibrariesService librariesService = (ILibrariesService) GlobalServiceRegister.getDefault()
                                .getService(ILibrariesService.class);

                        Map driverJar = null;

                        for (Map dj : jars) {
                            if (dj.get(JAR_NAME).equals(jn)) {
                                driverJar = dj;
                                break;
                            }
                        }

                        if (jar.get("JAR_STATUS").equals("✔")) {
                            MavenArtifact ma = new MavenArtifact();
                            ma.setArtifactId(a);
                            ma.setGroupId("org.talend.libraries");
                            String djv = TalendQuoteUtils.removeQuotes(driverJar.get("JAR_NEXUS_VERSION").toString());
                            ma.setVersion(djv);
                            ma.setType("jar");

                            String p = PomUtil.getAbsArtifactPath(ma);
                            if (p != null) {
                                File f = new File(p);

                                if (f.exists()) {
                                    try {
                                        monitor.subTask("Installing local dependency ... " + jn);
                                        librariesService.deployLibrary(f.toURI().toURL(),
                                                "mvn:org.talend.libraries/" + a + "/" + jnv + "/jar");
                                        driverJar.put(JAR_NEXUS_VERSION, jnv);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        } else {
                            monitor.subTask("Donwloading" + jn + "from " + nexusServerBean.getServer());

                            InputStream is = service.getContentInputStream(nexusServerBean, "", getGroupId(), a, jnv, null);

                            File file = generateTempFile(is, jn);

                            try {

                                monitor.subTask("Installing local dependency ... " + jn);

                                librariesService.deployLibrary(file.toURI().toURL(),
                                        "mvn:org.talend.libraries/" + a + "/" + jnv + "/jar");

                                jar.put("JAR_STATUS", "✔");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        monitor.subTask("Finished syncing " + jn + " from " + nexusServerBean.getServer());
                        monitor.worked(i);
                    }
                }
            }

            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    ((IProcess2) getProcess(elem, part)).refreshProcess();
                }

            });

            monitor.done();
            if (monitor.isCanceled())
                throw new InterruptedException("The long running operation was cancelled");
        }

    }

    private void deploy(File file, String version) {

        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
                ILibrariesService service = (ILibrariesService) GlobalServiceRegister.getDefault()
                        .getService(ILibrariesService.class);

                service.deployLibrary(file.toURI().toURL(), version);
            }
        } catch (IOException ee) {
            ExceptionHandler.process(ee);
        }
    }

}
