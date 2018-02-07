package org.talend.designer.esb.components.ws.crest;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.designer.esb.components.ws.tools.extensions.external.IOASDecoder;
import org.talend.designer.esb.components.ws.tools.extensions.external.OASDecoderFactory;
import org.talend.designer.esb.components.ws.tools.extensions.external.TranslationException;

public class CRESTNode extends AbstractExternalNode {

    private static final String ACCEPTED_OASDECODER_ID = "org.talend.designer.esb.components.ws.tools.RWADefDecoder";

    private class OASImporter implements Runnable {

        private Path pathToOASFile;

        private IOASDecoder oasManager;

        private Shell shell;

        /**
         * DOC dsergent OASImporter constructor comment.
         * 
         * @param node
         * @param pathToOASFile
         */
        public OASImporter(Shell shell, Path pathToOASFile) {
            super();
            this.shell = shell;
            this.pathToOASFile = pathToOASFile;
        }

        @Override
        public void run() {

            Map<String, IOASDecoder> decoders;
            try {
                decoders = OASDecoderFactory.getInstance().getOASDecoders();
                if (decoders.keySet().contains(ACCEPTED_OASDECODER_ID)) {
                    oasManager = decoders.get(ACCEPTED_OASDECODER_ID);
                    oasManager.setOASSourceFile(pathToOASFile);
                }
            } catch (CoreException e) {
                MessageDialogWithLink.openError(shell, "OAS/Swagger 2.0 import error",
                        "We were unable to find a suitable decoder for API definition.");
            }
        }

        /**
         * Getter for oasManager.
         * 
         * @return the oasManager
         */
        public IOASDecoder getOasManager() {
            return this.oasManager;
        }

    }

    @Override
    public int open(Display display) {
        return open(display.getActiveShell());
    }

    @Override
    public int open(Composite parent) {
        return open(parent.getShell());
    }

    private int open(Shell shell) {
        try {
            Field field = shell.getClass().getDeclaredField("lastActive");
            field.setAccessible(true);
            if (!field.get(shell).toString().equals("Button {}")) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView("org.talend.designer.core.ui.views.properties.ComponentSettingsView");
                return SWT.CANCEL;
            }
        } catch (Exception e) {
        }

        FileDialog fileDialog = new FileDialog(shell);
        fileDialog.setFilterExtensions(new String[] { "*.json;*.yaml", "*.*" });

        String selectedFile = fileDialog.open();
        if (StringUtils.isNotBlank(selectedFile)) {

            CRESTNodeAdapter crestNodeAdapter = new CRESTNodeAdapter(this);

            try {
                OASImporter oasImporter = new OASImporter(shell, Paths.get(selectedFile));
                BusyIndicator.showWhile(shell.getDisplay(), oasImporter);
                IOASDecoder oasManager = oasImporter.getOasManager();

                if (oasManager != null && oasManager.getTranslationStatus() != null) {
                    switch (oasManager.getTranslationStatus()) {
                    case SUCCESS:
                        if (crestNodeAdapter.isNodeToDefaultValues()) {
                            if (MessageDialogWithLink.openConfirm(shell, "Confirm component initialization",
                                    "Initialize component?", "", "", false)) {
                                crestNodeAdapter.setNodeSetting(oasManager);
                                return SWT.OK;
                            } else {
                                return SWT.CANCEL;
                            }
                        } else {
                            if (MessageDialogWithLink.openConfirm(shell, "Confirm component initialization",
                                    "Initialize component?\n\nYour existing endpoint, API mappings and documentation will be overridden.",
                                    "", "", crestNodeAdapter.isEndpointNotNull())) {
                                crestNodeAdapter.setNodeSetting(oasManager);
                                return SWT.OK;
                            } else {
                                return SWT.CANCEL;
                            }
                        }
                    case SUCCESS_WITH_WARNINGS:
                        boolean confirm = MessageDialogWithLink.openConfirm(shell, "Confirm component initialization",
                                "Initialize component?\nYour existing endpoint, API mappings and documentation will be overridden.",
                                "If some parts seem missing in your initialized component, please check your OAS/Swagger 2.0 definition in <a>Restlet Studio</a>.",
                                "https://studio.restlet.com", crestNodeAdapter.isEndpointNotNull());

                        if (confirm) {
                            crestNodeAdapter.setNodeSetting(oasManager);
                            return SWT.OK;
                        } else {
                            return SWT.CANCEL;
                        }
                    case ERROR:
                        MessageDialogWithLink.openError(shell, "OAS/Swagger 2.0 import error",
                                "We were unable to initialize your component from your OAS/Swagger 2.0 definition.",
                                "Please check your OAS/Swagger 2.0 definition in <a>Restlet Studio</a>.",
                                "https://studio.restlet.com", false);
                        return SWT.CANCEL;
                    default:
                        return SWT.CANCEL;

                    }
                } else {
                    MessageDialogWithLink.openError(shell, "OAS/Swagger 2.0 import error",
                            "We were unable to find a suitable decoder for API definition.");
                    return SWT.CANCEL;
                }
            } catch (TranslationException e) {
                MessageDialogWithLink.openError(shell, "OAS/Swagger 2.0 import error",
                        "We were unable to initialize your component from your OAS/Swagger 2.0 definition.",
                        "Please check your OAS/Swagger 2.0 definition in <a>Restlet Studio</a>.", "https://studio.restlet.com",
                        false);
                return SWT.CANCEL;
            }
        } else {
            return SWT.CANCEL;
        }
    }

    public String getParamStringValue(String key) {
        final Object parameterValue = getParamValue(key);
        if (parameterValue instanceof String) {
            return StringUtils.trimToNull((String) parameterValue);
        }
        return null;
    }

    private Object getParamValue(String key) {
        final IElementParameter parameter = getElementParameter(key);
        return parameter == null ? null : parameter.getValue();
    }

    public void setParamValue(String key, Object value) {
        final IElementParameter parameter = getElementParameter(key);
        if (parameter != null) {
            parameter.setValue(value);
        }
    }

    public boolean getBooleanValue(String key) {
        Object value = getParamValue(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return BooleanUtils.toBoolean(value.toString());
    }

    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath) {
        return null;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void renameInputConnection(String oldName, String newName) {
    }

    @Override
    public void renameOutputConnection(String oldName, String newName) {
    }

    @Override
    public void setExternalData(IExternalData persistentData) {
    }

    @Override
    public IExternalData getTMapExternalData() {
        return null;
    }

    @Override
    public void metadataOutputChanged(IMetadataTable currentMetadata) {
    }

}
