// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.ui.scriptmanager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.ui.ServiceMetadataDialog;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.utils.TemplateProcessor;

public class ServiceExportManager extends JobJavaScriptOSGIForESBManager {

    private static final String TEMPLATE_BLUEPRINT = "/resources/blueprint-template.xml"; //$NON-NLS-1$

    public ServiceExportManager(Map<ExportChoice, Object> exportChoiceMap) {
        super(exportChoiceMap, null, null, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
    }

    @SuppressWarnings("unchecked")
    public void createBlueprint(File outputFile, Map<ServicePort, Map<String, String>> ports, Map<String, String> additionalInfo,
            IFile wsdl, String studioServiceName) throws IOException, CoreException {

        // TODO: support multiport
        Entry<ServicePort, Map<String, String>> studioPort = ports.entrySet().iterator().next();

        Definition def = WSDLUtils.getDefinition(wsdl);
        QName serviceQName = null;
        String endpointAddress = null;
        String endpointName = null;
        Map<QName, Service> services = def.getServices();
        ServicePort servicePort = studioPort.getKey();
        for (Entry<QName, Service> serviceEntry : services.entrySet()) { // TODO: support multi-services
            Service service = serviceEntry.getValue();
            Collection<Port> servicePorts = service.getPorts().values(); // TODO: support multi-ports
            for (Port port : servicePorts) {
                if (servicePort.getName().equals(port.getBinding().getPortType().getQName().getLocalPart())) {
                    serviceQName = serviceEntry.getKey();
                    endpointName = port.getName();
                    endpointAddress = WSDLUtils.getPortAddress(port);
                    if (null != endpointAddress) {
                        // http://jira.talendforge.org/browse/TESB-3638
                        final URI uri = URI.create(endpointAddress);
                        endpointAddress = uri.getPath();
                        if (endpointAddress == null) {
                            endpointAddress = uri.getRawSchemeSpecificPart();
                            int interrogationMark = endpointAddress.indexOf('?');
                            if (interrogationMark > 0) {
                                endpointAddress = endpointAddress.substring(0, interrogationMark);
                            }
                        }

                        if (endpointAddress.equals("/services/") || endpointAddress.equals("/services")) {
                            // pass as is
                            endpointAddress = endpointAddress;
                        } else if (endpointAddress.startsWith("/services/")) {
                            // remove forwarding "/services/" context as required by runtime
                            endpointAddress = endpointAddress.substring("/services/".length() - 1); // leave
                                                                                                    // forwarding
                                                                                                    // slash
                        } else if (endpointAddress.length() == 1) { // empty path
                            endpointAddress += studioServiceName;
                        }
                    }
                    break;
                }
            }
        }

        Map<String, Object> endpointInfo = new HashMap<String, Object>();
        endpointInfo.put("namespace", serviceQName.getNamespaceURI()); //$NON-NLS-1$
        endpointInfo.put("service", serviceQName.getLocalPart()); //$NON-NLS-1$
        endpointInfo.put("port", endpointName); //$NON-NLS-1$
        endpointInfo.put("address", endpointAddress); //$NON-NLS-1$
        endpointInfo.put("studioName", studioServiceName); //$NON-NLS-1$
        endpointInfo.put("wsdlLocation", wsdl.getName()); //$NON-NLS-1$

        Map<String, String> operation2job = new HashMap<String, String>();
        for (Map.Entry<ServicePort, Map<String, String>> port : ports.entrySet()) {
            // TODO: actual port work
            for (Map.Entry<String, String> operation : port.getValue().entrySet()) {
                operation2job.put(operation.getKey(), operation.getValue());
            }
        }
        endpointInfo.put("operation2job", operation2job); //$NON-NLS-1$

        boolean isStudioEEVersion = isStudioEEVersion();

        boolean useRegistry = isStudioEEVersion
                && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_SERVICE_REGISTRY));
        boolean useSL = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_SL));
        boolean useSAM = !useRegistry && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_SAM));
        boolean useSecurityToken = !useRegistry && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.SECURITY_BASIC));
        boolean useSecuritySAML = !useRegistry && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.SECURITY_SAML));
        boolean useAuthorization = !useRegistry && isStudioEEVersion && useSecuritySAML
                && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.AUTHORIZATION));
        boolean useEncryption = !useRegistry && isStudioEEVersion && useSecuritySAML
                && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.ENCRYPTION));
        boolean logMessages = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.LOG_MESSAGES));
        boolean wsdlSchemaValidation = isStudioEEVersion
                && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.WSDL_SCHEMA_VALIDATION));
        boolean useBusinessCorrelation = !useRegistry
                && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_BUSINESS_CORRELATION));

        endpointInfo.put("useSL", useSL); //$NON-NLS-1$
        endpointInfo.put("useSAM", useSAM); //$NON-NLS-1$
        endpointInfo.put("useSecurityToken", useSecurityToken); //$NON-NLS-1$
        endpointInfo.put("useSecuritySAML", useSecuritySAML); //$NON-NLS-1$
        endpointInfo.put("useAuthorization", useAuthorization); //$NON-NLS-1$
        endpointInfo.put("useEncryption", useEncryption); //$NON-NLS-1$
        endpointInfo.put("useServiceRegistry", useRegistry); //$NON-NLS-1$
        endpointInfo.put("logMessages", logMessages); //$NON-NLS-1$
        endpointInfo.put("useWsdlSchemaValidation", wsdlSchemaValidation); //$NON-NLS-1$
        endpointInfo.put("useBusinessCorrelation", useBusinessCorrelation); //$NON-NLS-1$

        Map<String, String> slCustomProperties = new HashMap<String, String>();
        if (useSL /* && !useRegistry */) {
            for (Map.Entry<String, String> prop : additionalInfo.entrySet()) {
                if (prop.getKey().startsWith(ServiceMetadataDialog.SL_CUSTOM_PROP_PREFIX)) {
                    slCustomProperties.put(prop.getKey().substring(ServiceMetadataDialog.SL_CUSTOM_PROP_PREFIX.length()),
                            prop.getValue());
                }
            }
        }
        endpointInfo.put("slCustomProps", slCustomProperties); //$NON-NLS-1$

        endpointInfo.put("samlConfig", //$NON-NLS-1$
                serviceQName.toString().replaceAll("\\W+", "_").substring(1)); //$NON-NLS-1$

        TemplateProcessor.processTemplate("DATA_SERVICE_BLUEPRINT_CONFIG", //$NON-NLS-1$
                endpointInfo, outputFile, getClass().getResourceAsStream(TEMPLATE_BLUEPRINT));
    }

    public Manifest getManifest(String artifactName, String serviceVersion, Map<String, String> additionalInfo) {
        boolean useRegistry = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_SERVICE_REGISTRY));
        boolean logMessages = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.LOG_MESSAGES));
        boolean useSL = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_SL));
        boolean useSAM = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_SAM));
        boolean useBusinessCorrelation = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.USE_BUSINESS_CORRELATION));
        boolean useSecurityToken = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.SECURITY_BASIC));
        boolean useSecuritySAML = Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.SECURITY_SAML));
        boolean useEncryption = useSecuritySAML && Boolean.valueOf(additionalInfo.get(ServiceMetadataDialog.ENCRYPTION));

        Manifest manifest = new Manifest();
        Attributes a = manifest.getMainAttributes();
        String projectName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel().toLowerCase(Locale.US);
        a.put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-Name"), artifactName); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-SymbolicName"), projectName + "." + artifactName); //$NON-NLS-1$ //$NON-NLS-2$
        a.put(new Attributes.Name("Bundle-Version"), serviceVersion.replace("-SNAPSHOT", ".SNAPSHOT")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        a.put(new Attributes.Name("Bundle-ManifestVersion"), "2"); //$NON-NLS-1$ //$NON-NLS-2$
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        a.put(new Attributes.Name("Created-By"), brandingService.getFullProductName() + " (" + brandingService.getAcronym() + '_'
                + RepositoryPlugin.getDefault().getBundle().getVersion().toString() + ')');
        a.put(new Attributes.Name("Import-Package"), //$NON-NLS-1$
                "javax.xml.ws,org.talend.esb.job.controller" //$NON-NLS-1$
                        + ",org.osgi.service.cm;version=\"[1.3,2)\"" //$NON-NLS-1$
                        + ",org.apache.cxf,org.apache.cxf.metrics" //$NON-NLS-1$
                        + (logMessages ? ",org.apache.cxf.feature" : "") //$NON-NLS-1$
                        + (useSL ? ",org.talend.esb.servicelocator.cxf" : "") //$NON-NLS-1$
                        + (useSAM ? ",org.talend.esb.sam.agent.feature" : "") //$NON-NLS-1$
                        + (useBusinessCorrelation ? ",org.talend.esb.policy.correlation.feature" : "") //$NON-NLS-1$
                        + (useSecurityToken || useRegistry ? ",org.apache.wss4j.dom.validate" : "") //$NON-NLS-1$
                        + (useSecuritySAML || useRegistry ? ",org.talend.esb.security.saml" : "") //$NON-NLS-1$
                        + (useEncryption || useRegistry ? ",org.apache.cxf.xkms.crypto" : "") //$NON-NLS-1$
        );
        return manifest;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.
     * JobJavaScriptOSGIForESBManager#getOutputSuffix()
     */
    @Override
    public String getOutputSuffix() {
        return "/";
    }

    public static Map<ExportChoice, Object> getDefaultExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, true);
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        return exportChoiceMap;
    }

    public JobScriptsManager getJobManager(Map<ExportChoice, Object> exportChoiceMap, String parentPath,
            IRepositoryViewObject node, String groupId, String serviceVersion) {
        if (exportChoiceMap == null) {
            exportChoiceMap = getDefaultExportChoiceMap();
        }
        JobJavaScriptOSGIForESBManager manager = new JobJavaScriptOSGIForESBManager(exportChoiceMap, null, serviceVersion,
                statisticPort, tracePort);
        String artifactName = getNodeLabel(node);
        File path = getFilePath(parentPath, groupId, artifactName, serviceVersion);
        File file = new File(path, artifactName + '-' + serviceVersion + manager.getOutputSuffix());
        manager.setDestinationPath(file.getAbsolutePath());
        return manager;
    }

    public File getFilePath(String parentPath, String groupId, String artefactName, String serviceVersion) {
        File folder = new File(parentPath, "repository"); //$NON-NLS-1$
        File group = new File(folder, groupId.replace('.', File.separatorChar));
        File artefact = new File(group, artefactName);
        File version = new File(artefact, serviceVersion);
        version.mkdirs();
        return version;
    }

    public String getNodeLabel(IRepositoryViewObject node) {
        return node.getProperty().getLabel();
    }

}
