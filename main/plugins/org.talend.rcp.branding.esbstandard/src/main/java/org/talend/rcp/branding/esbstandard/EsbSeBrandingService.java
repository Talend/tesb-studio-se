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
package org.talend.rcp.branding.esbstandard;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.branding.AbstractTalendBrandingService;
import org.talend.core.branding.DefaultBrandingConfiguration;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.rcp.branding.esbstandard.i18n.Messages;
import org.talend.rcp.branding.esbstandard.starting.EsbStartingBrowser;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 */
public class EsbSeBrandingService extends AbstractTalendBrandingService {

    protected IBrandingConfiguration brandingConfigure;

    @Override
    public String getShortProductName() {
        return getProductName();
    }

    @Override
    public String getCorporationName() {
        return Messages.getString("corporationname"); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getLoginVImage() {
        return Activator.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(),
            Messages.getString("loginimageleft")); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getLoginHImage() {
        return Activator.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(),
            Messages.getString("loginimagehigh")); //$NON-NLS-1$
    }

    @Override
    public URL getLicenseFile() throws IOException {
        return FileLocator.toFileURL(FileLocator.find(Activator.getDefault().getBundle(),
            new Path("resources/license.txt"), null)); //$NON-NLS-1$
    }

    @Override
    public IBrandingConfiguration getBrandingConfiguration() {
        if (brandingConfigure == null) {
            brandingConfigure = new DefaultBrandingConfiguration();
        }
        return brandingConfigure;
    }

    @Override
    public String getAcronym() {
        return "tos_esb"; //$NON-NLS-1$
    }

    @Override
    public String getJobLicenseHeader(String version) {
        return Messages.getString("TosBrandingService_job_license_header_content", getFullProductName(), version);
    }

    @Override
    public String getRoutineLicenseHeader(String version) {
        return Messages.getString("TosBrandingService_routine_license_header_content", getFullProductName(), version);
    }

    @Override
    public String getProductName() {
        return "Talend Open Studio";
    }

    @Override
    public String getOptionName() {
        return "for ESB";
    }

    @Override
    public String getStartingBrowserId() {
        return EsbStartingBrowser.ID;
    }

    @Override
    public String getUserManuals() {
        return "ESB"; //$NON-NLS-1$
    }
}
