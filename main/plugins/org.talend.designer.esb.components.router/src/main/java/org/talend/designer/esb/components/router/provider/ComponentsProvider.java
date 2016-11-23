package org.talend.designer.esb.components.router.provider;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.designer.esb.components.router.Activator;

public class ComponentsProvider extends AbstractComponentsProvider {

    private static Logger logger = Logger.getLogger(ComponentsProvider.class);

    @Override
    protected File getExternalComponentsLocation() {
        Bundle bundle = Activator.getBundle();
        try {
            URL localURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("components"), null));
            return new File(localURL.getPath());
        } catch (Exception localException) {
            logger.error(localException);
            localException.printStackTrace();
        }
        return null;
    }

    @Override
    public String getFamilyTranslation(String paramString) {
        return null;
    }

}
