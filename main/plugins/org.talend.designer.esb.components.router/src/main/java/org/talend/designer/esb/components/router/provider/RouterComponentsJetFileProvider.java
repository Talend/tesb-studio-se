package org.talend.designer.esb.components.router.provider;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.esb.components.router.Activator;

public class RouterComponentsJetFileProvider extends AbstractJetFileProvider {

    private static Logger logger = Logger.getLogger(RouterComponentsJetFileProvider.class);

    @Override
    protected File getExternalFrameLocation() {
        try {
            URL localURL1 = FileLocator.find(Activator.getBundle(), new Path("additional"), null);
            URL localURL2 = FileLocator.toFileURL(localURL1);
            return new File(localURL2.getPath());
        } catch (Exception localException) {
            logger.error(localException);
        }
        return null;
    }

}
