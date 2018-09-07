// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.talend.camel.designer.ui.SaveAsRoutesAction;
import org.talend.camel.designer.ui.action.RoutePasteAction;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.IComponentsHandler;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.INexusService;
import org.talend.core.model.process.INode;
import org.talend.core.nexus.ArtifactRepositoryBean;
import org.talend.core.nexus.TalendLibsServerManager;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;
import org.talend.utils.io.FilesUtils;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class CamelTalendEditor extends AbstractTalendEditor {

    private Map<String,String> cConfigStoredInfo = null;

    private static CamelComponentsHandler CAMEL_COMPONENTS_HANDLER;

    public CamelTalendEditor() {
        super();
    }

    public CamelTalendEditor(boolean readOnly) {
        super(readOnly);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();

        // Set DND listener by CamelEditorDropTargetListener
        getGraphicalViewer().removeDropTargetListener(talendEditorDropTargetListener);
        talendEditorDropTargetListener.setEditor(null);
        talendEditorDropTargetListener = null;
        talendEditorDropTargetListener = new CamelEditorDropTargetListener(this);
        talendEditorDropTargetListener.setEditor(this);
        getGraphicalViewer().addDropTargetListener(talendEditorDropTargetListener);

        IAction pasteAction = new RoutePasteAction(this);
        getActionRegistry().registerAction(pasteAction);

        List<? extends INode> graphicalNodes = this.getProcess().getGraphicalNodes();

        cConfigStoredInfo = new HashMap<>();

        for (INode node : graphicalNodes) {
            if (node.getComponent().getName().equals("cConfig")){
                List<Map<String,String>> jars = (List) node.getElementParameter("DRIVER_JAR").getValue();

                for(Map<String,String> o:jars){
                    String jn = TalendQuoteUtils.removeQuotes(o.get("JAR_NAME"));
                    String jnv = TalendQuoteUtils.removeQuotes(o.get("JAR_NEXUS_VERSION"));

                    cConfigStoredInfo.put(jn, jnv);
                }
            }
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);

        if (!PluginChecker.isTIS()) {
            return;
        }

        ArtifactRepositoryBean nexusServerBean = TalendLibsServerManager.getInstance().getCustomNexusServer();


        if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {

            List<? extends INode> graphicalNodes = this.getProcess().getGraphicalNodes();
            for (INode node : graphicalNodes) {
                if (node.getComponent().getName().equals("cConfig")){
                    List<Map<String,String>> jars = (List) node.getElementParameter("DRIVER_JAR").getValue();
                    if (jars == null || jars.isEmpty()) {
                        continue;
                    }

                    boolean nexusIsAvailable = nexusServerBean != null && isAvailable(nexusServerBean);

                    try {
                        new ProgressMonitorDialog(getParent().getEditorSite().getShell()).run(true, true,
                                new RunnableWithProgress(jars, null, nexusIsAvailable));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            getProcess().refreshProcess();
                        }

                    });
                }
            }
        }

    }

    private class RunnableWithProgress implements IRunnableWithProgress {

        private List<Map<String, String>> jars;

        private List needUpdateJars;

        private boolean updateNexusJars;

        public RunnableWithProgress(List jars, List needUpdateJars, boolean updateNexusJars) {
            this.jars = jars;
            this.needUpdateJars = needUpdateJars;
            this.updateNexusJars = updateNexusJars;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            if (jars == null || jars.isEmpty()) {
                return;
            }


            ILibrariesService service = (ILibrariesService) GlobalServiceRegister.getDefault()
                    .getService(ILibrariesService.class);

            INexusService nexusService = null;
            ArtifactRepositoryBean nexusServerBean = null;

            if (updateNexusJars) {
                nexusService = (INexusService) GlobalServiceRegister.getDefault().getService(INexusService.class);
                nexusServerBean = TalendLibsServerManager.getInstance().getCustomNexusServer();
            }

            monitor.beginTask("Syncing the nexus server...", false ? IProgressMonitor.UNKNOWN : jars.size());

            for (int i = 0; i < jars.size(); i++) {

                Map<String, String> o = jars.get(i);

                String jn = TalendQuoteUtils.removeQuotes(o.get("JAR_NAME"));
                String jnv = TalendQuoteUtils.removeQuotes(o.get("JAR_NEXUS_VERSION"));
                String jv = String.valueOf(o.get("JAR_PATH"));
                String a = jn.replaceFirst("[.][^.]+$", "");

                if (StringUtils.isBlank(jnv)) {
                    continue;
                }

                if (StringUtils.isNotBlank(jv)) {
                    File jarFile = new File(jv);

                    if (jarFile.exists()) {

                        try {
                            monitor.subTask("Installing local dependency ... " + jn);

                            service.deployLibrary(jarFile.toURI().toURL(), "mvn:org.talend.libraries/" + a + "/" + jnv + "/jar", true, updateNexusJars);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        cConfigStoredInfo.put(jn, jnv);
                        o.put("JAR_PATH", "");
                    }
                }

                if (cConfigStoredInfo.get(jn) == null) {
                    cConfigStoredInfo.put(jn, jnv);
                    continue;
                }

                if (cConfigStoredInfo.get(jn).equals(jnv)) {
                    continue;
                } else {
                    MavenArtifact ma = new MavenArtifact();
                    ma.setArtifactId(a);
                    ma.setGroupId("org.talend.libraries");
                    ma.setVersion(cConfigStoredInfo.get(jn));
                    ma.setType("jar");

                    String p = PomUtil.getAbsArtifactPath(ma);

                    if (p != null) {
                        File file = new File(p);
                        try {
                            if (file.exists()) {
                                File tmp = new File(ExportJobUtil.getTmpFolder() + File.separator + jn);

                                FilesUtils.copyFile(file, tmp);

                                monitor.subTask("Installing local dependency ... " + jn);

                                service.deployLibrary(tmp.toURI().toURL(), "mvn:org.talend.libraries/" + a + "/" + jnv + "/jar");


                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (nexusServerBean == null) {
                    monitor.subTask("Finished syncing " + jn + " failed");
                } else {
                    monitor.subTask("Checking" + jn + "from " + nexusServerBean.getServer());

                    Map metadata = nexusService.getMavenMetadata(nexusServerBean, "org.talend.libraries", a, jn);

                    if (metadata.get("Versioning.Latest").equals(jn)) {
                        monitor.subTask("Finished syncing " + jn + " successfully");
                    }
                }

                monitor.worked(i);

            }


            //((ILibrariesService) GlobalServiceRegister.getDefault().getService(ILibrariesService.class))
            //        .updateModulesNeededForCurrentJob(getProcess());

            monitor.done();
            if (monitor.isCanceled())
                throw new InterruptedException("The long running operation was cancelled");
        }

    }

    @Override
    public void doSaveAs() {
        SaveAsRoutesAction saveAsAction = new SaveAsRoutesAction(this.getParent());
        saveAsAction.run();
    }

    protected IComponentsHandler initComponentsHandler() {
        if (CAMEL_COMPONENTS_HANDLER == null) {
            synchronized (CamelTalendEditor.class) {
                CAMEL_COMPONENTS_HANDLER = new CamelComponentsHandler();
            }
        }
        return CAMEL_COMPONENTS_HANDLER;
    }

    protected boolean isAvailable(ArtifactRepositoryBean nexusServerBean) {

        if (nexusServerBean == null) {
            MessageDialog.openError(getParent().getEditorSite().getShell(), "Checking Nexus Connection Error",
                    "Can not initialize the nexus server, Please check the TAC.");
        } else {
            try {
                if (nexusServerBean.getType().equals(ArtifactRepositoryBean.NexusType.NEXUS_3.name())) {
                    return isAvailableNexus3(nexusServerBean);
                } else if (nexusServerBean.getType().equals(ArtifactRepositoryBean.NexusType.NEXUS_2.name())) {
                    return isAvailableNexus2(nexusServerBean);
                } else if (nexusServerBean.getType().equals(ArtifactRepositoryBean.NexusType.ARTIFACTORY.name())) {
                    return isAvailableArtifactory(nexusServerBean);
                }
            } catch (Exception ex) {
                MessageDialog.openError(getParent().getEditorSite().getShell(), "Checking Nexus Connection Error",
                        "Can not connect to " + nexusServerBean.getServer() + "\n" + ex.getMessage()
                                + " Please upload the related jar files manually");
            } finally {

            }
        }

        return false;
    }

    protected boolean isAvailableArtifactory(ArtifactRepositoryBean nexusServerBean) throws Exception {
        String authUrl = nexusServerBean.getServer() + "api/system/ping?_dc=" + System.currentTimeMillis();
        URL url = new URL(authUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(3000);

        String userpass = nexusServerBean.getUserName() + ":" + nexusServerBean.getPassword();
        String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
        con.setRequestProperty("Authorization", basicAuth);

        int state = con.getResponseCode();

        if (state == 200) {
            return true;
        } else if (state == 401) {
            MessageDialog.openError(getParent().getEditorSite().getShell(), "Checking Nexus Connection Error",
                    "Can not connect to " + nexusServerBean.getServer() + "\n" + con.getResponseMessage() + " ResponseCode : "
                            + state + " Please upload the related jar files manually");
        }
        return false;
    }


    protected boolean isAvailableNexus2(ArtifactRepositoryBean nexusServerBean) throws  Exception {
        String authUrl = nexusServerBean.getServer() + "/service/local/authentication/login?_dc=" + System.currentTimeMillis();
        URL url = new URL(authUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(3000);

        String userpass = nexusServerBean.getUserName() + ":" + nexusServerBean.getPassword();
        String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
        con.setRequestProperty("Authorization", basicAuth);

        int state = con.getResponseCode();

        if (state == 200) {
            return true;
        } else if (state == 401) {
            MessageDialog.openError(getParent().getEditorSite().getShell(), "Checking Nexus Connection Error",
                    "Can not connect to " + nexusServerBean.getServer() + "\n" + con.getResponseMessage()
                            + " ResponseCode : " + state + " Please upload the related jar files manually");
        }
        return false;
    }

    protected boolean isAvailableNexus3(ArtifactRepositoryBean nexusServerBean) throws Exception {
        String authUrl = nexusServerBean.getServer() +  (nexusServerBean.getServer().endsWith("/") ? "" : "/") +
                            "service/rapture/session?_dc=" + System.currentTimeMillis();
        URL url = new URL(authUrl);
        String urlParameters  = "username=" + java.util.Base64.getEncoder().encodeToString(nexusServerBean.getUserName().getBytes())  +
                               "&password=" + java.util.Base64.getEncoder().encodeToString(nexusServerBean.getPassword().getBytes());
        byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int    postDataLength = postData.length;

        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        conn.setUseCaches( false );
        try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
            wr.write( postData );
        }

        int state = conn.getResponseCode();
        if (state == 204) {
            return true;
        } else if (state == 401) {
            MessageDialog.openError(getParent().getEditorSite().getShell(), "Checking Nexus Connection Error",
                "Can not connect to " + nexusServerBean.getServer() + "\n" + conn.getResponseMessage()
                        + " ResponseCode : " + state + " Please upload the related jar files manually");
        }

        return false;
    }
}
