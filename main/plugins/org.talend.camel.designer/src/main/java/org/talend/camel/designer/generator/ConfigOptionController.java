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

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.INexusService;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.nexus.NexusServerBean;
import org.talend.core.nexus.TalendLibsServerManager;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;
import org.talend.utils.io.FilesUtils;
import org.talend.utils.string.MD5;

public class ConfigOptionController extends AbstractElementPropertySectionController {
    
    private static Logger log = Logger.getLogger(ConfigOptionController.class);

	protected final String JAR_NAME = "JAR_NAME";
	protected final String JAR_PATH = "JAR_PATH";
	protected final String JAR_NEXUS_VERSION = "JAR_NEXUS_VERSION";
	protected final String JAR_NEXUS_PRE_VERSION = "JAR_NEXUS_PRE_VERSION";
	
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");//20170411034415
	
	protected INexusService service;
	
    NexusServerBean nexusServerBean = TalendLibsServerManager.getInstance().getCustomNexusServer();
	
	public ConfigOptionController(IDynamicProperty dp) {
		super(dp);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(INexusService.class)) {
        	service = (INexusService) GlobalServiceRegister.getDefault().getService(
        			INexusService.class);
        }
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
	
	protected boolean isAvailable(){
	    
	    if(nexusServerBean == null){
	        MessageDialog.openError(composite.getShell(), "Checking Nexus Connection Error", "Can not initialize the nexus server, Please check the TAC.");
	    }else{
	        try {
                URL url = new URL(
                        nexusServerBean.getServer() + "/service/local/authentication/login?_dc=" + System.currentTimeMillis());
	            HttpURLConnection con = (HttpURLConnection) url.openConnection();
	            con.setConnectTimeout(3000);

                String userpass = nexusServerBean.getUserName() + ":" + nexusServerBean.getPassword();
                String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
                con.setRequestProperty("Authorization", basicAuth);

	            int state = con.getResponseCode();

	            if (state == 200) {
	                log.info("Connect to "+nexusServerBean.getServer()+" successfully!");
	                return true;
                } else if (state == 401) {
                    MessageDialog.openError(composite.getShell(), "Checking Nexus Connection Error", "Can not connect to "
                            + nexusServerBean.getServer() + "\n" + con.getResponseMessage() + " ResponseCode : " + state);
	            }
	        }catch (Exception ex) {
	            MessageDialog.openError(composite.getShell(), "Checking Nexus Connection Error", "Can not connect to "+nexusServerBean.getServer()+"\n"+ex.getMessage());
	        }finally{
	            
	        }
	    }
	    

        return false;
	}

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {

        Button theBtn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        theBtn.setBackground(subComposite.getBackground());
        if (param.getDisplayName().equals("")) { //$NON-NLS-1$
            theBtn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        } else {
            theBtn.setText(param.getDisplayName());
        }
        FormData data = new FormData();
        if (isInWizard()) {
            if (lastControl != null) {
                data.right = new FormAttachment(lastControl, 0);
            } else {
                data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
        } else {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, 0);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
            }
        }
        data.top = new FormAttachment(0, top);
        theBtn.setLayoutData(data);
        theBtn.setEnabled(!param.isReadOnly());
        theBtn.setData(param);
        hashCurControls.put(param.getName(), theBtn);
        theBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Command cmd = createCommand((Button) e.getSource());
                executeCommand(cmd);
            }
        });
        Point initialSize = theBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        if (nexusServerBean == null) {
            theBtn.setVisible(false);
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                refresh(param, true);
            }

        });

        return theBtn;
    }

	protected Command createCommand(Button source) {
		return null;
	}

	@Override
	public int estimateRowSize(Composite subComposite, IElementParameter param) {
		return 0;
	}

	@Override
	public void refresh(IElementParameter param, boolean check) {

		TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get("NEED_UPDATE_LIST");
		
        if (tableViewerCreator == null || tableViewerCreator.getTable() == null || tableViewerCreator.getTable().isDisposed()) {
            return;
        }else{
            if (nexusServerBean == null) {
                tableViewerCreator.getTable().setVisible(false);
            } else {

                TableViewerCreatorColumn blankCol = (TableViewerCreatorColumn) tableViewerCreator.getColumns().get(1);
                blankCol.getTableColumn().setWidth(0);
                blankCol.getTableColumn().setText("");
                blankCol.getTableColumn().setResizable(false);

                // refreshDynamicProperty();
                tableViewerCreator.getTableViewer().refresh();
            }
        	
        }
	}
	
    protected Map getNeedUpdateJar(String flag,String jarName,String nexusVersion,String nexusPreVersion){
		Map needUpdateJar = new HashMap();

		//needUpdateJar.put("SCHEMA_COLUMN", flag);
		
		needUpdateJar.put("JAR_STATUS", flag);
		
		needUpdateJar.put("JAR_SYNC", "true");
		
		needUpdateJar.put(JAR_NAME, jarName);
		
		needUpdateJar.put(JAR_NEXUS_VERSION, nexusVersion);
		
		needUpdateJar.put(JAR_NEXUS_PRE_VERSION, nexusPreVersion);
		
		return needUpdateJar;
    }
    
    protected File generateTempFile(InputStream is,String fileName){
        File file = new File(ExportJobUtil.getTmpFolder() + File.separator + fileName);

        try {
            FilesUtils.copyFile(is, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return file;
    }
    
    protected String getGroupId(){
        String groupId = "org.talend.libraries";
//        
//        final EMap additionalProperties = ((RouteProcess)((Node)elem).getProcess()).getProperty().getAdditionalProperties();
//        final Object groupIdValue = additionalProperties.get(MavenConstants.NAME_GROUP_ID);
//        if (groupIdValue != null && groupIdValue.toString().length() > 0) {
//            groupId = groupIdValue.toString();
//        }
        
        return groupId;
    }

    public IRunnableWithProgress getCheckNexusRunnableWithProgress(List jars, List needUpdateJars) {
        return new CheckNexusRunnableWithProgress(jars, needUpdateJars);
    }

    private class CheckNexusRunnableWithProgress implements IRunnableWithProgress {

        private List jars;

        private List needUpdateJars;

        public CheckNexusRunnableWithProgress(List jars, List needUpdateJars) {
            this.jars = jars;
            this.needUpdateJars = needUpdateJars;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            monitor.beginTask("Checking the nexus server...", false ? IProgressMonitor.UNKNOWN : jars.size());

            for (int i = 0; i < jars.size(); i++) {
                Map<String, String> jar = (Map) jars.get(i);

                String currentNexusVersion = TalendQuoteUtils.removeQuotes(jar.get(JAR_NEXUS_VERSION));
                String currentNexusPreVersion = jar.get(JAR_NEXUS_PRE_VERSION);
                String jn = TalendQuoteUtils.removeQuotes(jar.get(JAR_NAME));
                String a = jn.replaceFirst("[.][^.]+$", "");

                if (StringUtils.isBlank(currentNexusVersion)) {
                    continue;
                }

                try {

                    monitor.subTask("Checking" + jn + "from " + nexusServerBean.getServer());
                    Map metadata = service.getMavenMetadata(nexusServerBean, getGroupId(), a, currentNexusVersion);

                    if (metadata.get("Versioning.Latest").equals(currentNexusVersion)) {

                        MavenArtifact ma = new MavenArtifact();
                        ma.setArtifactId(a);
                        ma.setGroupId("org.talend.libraries");
                        ma.setVersion(currentNexusVersion);
                        ma.setType("jar");

                        String p = PomUtil.getAbsArtifactPath(ma);

                        monitor.subTask("Loading checksum file from " + nexusServerBean.getServer());

                        InputStream is = service.getContentInputStream(nexusServerBean, "", getGroupId(), a,
                                metadata.get("Versioning.Latest").toString(), "jar.md5");

                        if (p != null) {

                            if (is != null) {
                                String remoteM2FileMD5 = "";
                                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
                                    remoteM2FileMD5 = buffer.lines().collect(Collectors.joining("\n"));
                                }
                                File f = new File(p);// local file

                                if (f.exists()) {
                                    String localM2FileMD5 = MD5.getMD5(FilesUtils.getBytes(f));

                                    if (!StringUtils.equalsIgnoreCase(localM2FileMD5, remoteM2FileMD5)) {
                                        Map needUpdateJar = getNeedUpdateJar("✘", jn, currentNexusVersion,
                                                currentNexusPreVersion);
                                        needUpdateJar.put("JAR_SYNC", true);
                                        needUpdateJars.add(needUpdateJar);
                                    } else {

                                        Map needUpdateJar = getNeedUpdateJar("✔", jn, currentNexusVersion,
                                                currentNexusPreVersion);
                                        needUpdateJar.put("JAR_SYNC", "false");
                                        needUpdateJars.add(needUpdateJar);
                                    }

                                } else {
                                    Map needUpdateJar = getNeedUpdateJar("✘", jn, currentNexusVersion, currentNexusPreVersion);
                                    needUpdateJar.put("JAR_SYNC", "true");
                                    needUpdateJars.add(needUpdateJar);
                                }
                            }
                        } else {
                            if (is != null) {
                                Map needUpdateJar = getNeedUpdateJar("✘", jn, currentNexusVersion, currentNexusPreVersion);
                                needUpdateJar.put("JAR_SYNC", "true");
                                needUpdateJars.add(needUpdateJar);
                            }
                        }

                    } else {
                        Map needUpdateJar = getNeedUpdateJar("✘", jn, currentNexusVersion,
                                metadata.get("Versioning.Latest").toString());
                        needUpdateJar.put("JAR_SYNC", "true");
                        needUpdateJars.add(needUpdateJar);
                    }

                    monitor.subTask("Finished checking " + jn + " from " + nexusServerBean.getServer());
                    monitor.worked(i);

                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            monitor.done();
            if (monitor.isCanceled())
                throw new InterruptedException("The long running operation was cancelled");
        }

    }

}

