package org.talend.repository.services.ui.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.ui.export.ArchiveFileExportOperationFullPath;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.ui.scriptmanager.ServiceExportWithMavenManager;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.OSGIJavaScriptForESBWithMavenManager;
import org.talend.resources.util.EMavenBuildScriptProperties;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class ExportServiceWithMavenAction extends ExportServiceAction {

    private ServiceExportWithMavenManager manager;

    private boolean addMavenScript;

    /**
     * DOC ycbai ExportServiceWithMavenAction constructor comment.
     * 
     * @param exportChoiceMap
     * @param node
     * @param targetPath
     * @throws InvocationTargetException
     */
    public ExportServiceWithMavenAction(ServiceExportWithMavenManager manager, Map<ExportChoice, Object> exportChoiceMap,
            ServiceItem serviceItem, String targetPath, boolean addMavenScript) throws InvocationTargetException {
        super(serviceItem, targetPath, exportChoiceMap);
        this.addMavenScript = addMavenScript;

        if (manager == null) {
            this.manager = new ServiceExportWithMavenManager(exportChoiceMap, IContext.DEFAULT, JobScriptsManager.LAUNCHER_ALL,
                    IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
        } else {
            this.manager = manager;
        }
        this.manager.setMavenGroupId(this.getGroupId());
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        super.run(monitor);
        try {
            addServiceFilesToExport();
            addJobFilesToExport(monitor);
            addMavenFilesToExport(monitor);
            super.processFinalResult(serviceManager.getDestinationPath());
            if (!addMavenScript) {
                ITalendProcessJavaProject javaProject = createServiceJavaProject();
                String path = javaProject.getProject().getLocation().toPortableString();
                ZipToFile.unZipFile(serviceManager.getDestinationPath(), path);
                new File(serviceManager.getDestinationPath()).delete();

                final Map<String, Object> argumentsMap = new HashMap<String, Object>();
                argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, TalendMavenConstants.GOAL_PACKAGE);
                // argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, exportChoiceMap);
                javaProject.buildModules(monitor, null, argumentsMap);
                
                IFolder targetFolder = javaProject.getTargetFolder();
                IFile jobFile = null;
                try {
                    targetFolder.refreshLocal(IResource.DEPTH_ONE, null);
                    // we only build one zip at a time, so just get the zip file to be able to manage some pom customizations.
                    for (IResource resource : targetFolder.members()) {
                        if (resource instanceof IFile) {
                            IFile file = (IFile) resource;
                            if ("kar".equals(file.getFileExtension())) {
                                jobFile = file;
                                break;
                            }
                        }
                    }
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
                FilesUtils.copyFile(jobFile.getLocation().toFile(), new File(serviceManager.getDestinationPath())); //$NON-NLS-1$
            }
        } catch (Throwable e) {
            throw new InvocationTargetException(e);
        } finally {
            super.clean();
        }
    }

    private void addMavenFilesToExport(IProgressMonitor monitor) throws Throwable {
        manager.setDestinationPath(serviceManager.getDestinationPath());
        String tempDestinationPath = getTempDestinationValue();
        List<ExportFileResource> resourcesToExport = manager
                .getExportResources(new ExportFileResource[] { new ExportFileResource(serviceItem, "") }); //$NON-NLS-1$
        ArchiveFileExportOperationFullPath exporterOperation = new ArchiveFileExportOperationFullPath(resourcesToExport,
                tempDestinationPath);
        manager.setTopFolder(resourcesToExport);
        executeExportOperation(exporterOperation, monitor);
        ZipToFile.unZipFile(tempDestinationPath, tempFolder);
        FilesUtils.removeFile(new File(tempDestinationPath));
    }

    private void addServiceFilesToExport() throws Exception {
        String resourcesPath = "src/main/resources"; //$NON-NLS-1$
        String repositoryPath = "repository"; //$NON-NLS-1$
        // control bundle file
        ZipToFile.unZipFile(getControlBundleFilePath(), tempFolder + PATH_SEPERATOR + resourcesPath);
        // feature file
        FilesUtils.copyFile(getFeatureFile(), new File(tempFolder + PATH_SEPERATOR + resourcesPath + "/feature/feature.xml")); //$NON-NLS-1$
        FilesUtils.removeFolder(tempFolder + PATH_SEPERATOR + repositoryPath, true);
    }

    private void addJobFilesToExport(IProgressMonitor monitor) throws Exception {
        String directoryName = serviceManager.getRootFolderName(tempFolder);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        for (IRepositoryViewObject node : nodes) {
            JobScriptsManager osgiManager = new OSGIJavaScriptForESBWithMavenManager(exportChoiceMap, IContext.DEFAULT,
                    JobScriptsManager.LAUNCHER_ALL, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES) {

                @Override
                protected Map<String, String> getMainMavenProperties(Item item) {
                    Map<String, String> mavenPropertiesMap = super.getMainMavenProperties(item);
                    mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemGroupName.getVarScript(), getGroupId());
                    return mavenPropertiesMap;
                }

                @Override
                protected void setMavenBuildScriptProperties(Document pomDocument, Map<String, String> mavenPropertiesMap) {
                    super.setMavenBuildScriptProperties(pomDocument, mavenPropertiesMap);
                    String itemName = mavenPropertiesMap.get(EMavenBuildScriptProperties.ItemName.getVarScript());
                    if (itemName != null && pomDocument != null) {
                        Element rootElement = pomDocument.getRootElement();
                        // Because re-use the osgi bundle for service, but for artifactId, there is no "-bundle"
                        // suffix. TDI-23491
                        Element artifactIdEle = rootElement.element("artifactId"); //$NON-NLS-1$
                        if (artifactIdEle != null) {
                            artifactIdEle.setText(itemName);
                        }
                    }
                }

            };
            String artefactName = serviceManager.getNodeLabel(node);
            String version = node.getVersion();
            // String fileName = artefactName + "-" + version;
            File destFile = new File(tempFolder + PATH_SEPERATOR + artefactName + osgiManager.getOutputSuffix());
            String destinationPath = destFile.getAbsolutePath();
            osgiManager.setDestinationPath(destinationPath);
            JobExportAction job = new JobExportAction(
                    Collections.singletonList(new RepositoryNode(node, null, ENodeType.REPOSITORY_ELEMENT)), version, osgiManager,
                    directoryName);
            job.run(monitor);
            ZipToFile.unZipFile(destinationPath,
                    tempFolder + PATH_SEPERATOR + ServiceExportWithMavenManager.OPERATIONS_PATH + artefactName);
            FilesUtils.removeFile(destFile);
        }
    }

    /**
     * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a
     * boolean indicating success.
     * 
     * @throws Throwable
     */
    private void executeExportOperation(ArchiveFileExportOperationFullPath op, IProgressMonitor monitor) throws Throwable {
        op.setCreateLeadupStructure(true);
        op.setUseCompression(true);

        try {
            op.run(monitor);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (InterruptedException e) {
            return;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "", null, //$NON-NLS-1$
                    status);
        }
    }

    protected String getTempDestinationValue() {
        String idealSuffix = manager.getOutputSuffix();
        String destinationText = manager.getDestinationPath();
        String tempDestination = getTmpFolderPath();
        destinationText = destinationText.replace("\\", PATH_SEPERATOR); //$NON-NLS-1$
        if (destinationText.indexOf(PATH_SEPERATOR) != -1) {
            String fileName = destinationText.substring(destinationText.lastIndexOf(PATH_SEPERATOR) + 1);
            tempDestination = tempDestination + PATH_SEPERATOR + fileName;
        }
        if (tempDestination.length() != 0 && !tempDestination.endsWith(File.separator)) {
            int dotIndex = tempDestination.lastIndexOf('.');
            if (dotIndex != -1) {
                int pathSepIndex = tempDestination.lastIndexOf(PATH_SEPERATOR);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    tempDestination += idealSuffix;
                }
            } else {
                tempDestination += idealSuffix;
            }
        }

        return tempDestination;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.services.ui.action.ExportServiceAction#processFinalResult(java.lang.String)
     */
    @Override
    protected void processFinalResult(String destinationPath) throws IOException {
        destinationPath = destinationPath.replace("\\", PATH_SEPERATOR); //$NON-NLS-1$
        if (destinationPath.indexOf(PATH_SEPERATOR) != -1) {
            destinationPath = destinationPath.substring(destinationPath.lastIndexOf(PATH_SEPERATOR));
        }
        destinationPath = tempFolder + destinationPath;
        super.processFinalResult(destinationPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.services.ui.action.ExportServiceAction#clean()
     */
    @Override
    protected void clean() {
    }

}
