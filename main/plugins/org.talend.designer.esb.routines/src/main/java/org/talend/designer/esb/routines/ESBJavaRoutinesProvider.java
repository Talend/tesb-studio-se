package org.talend.designer.esb.routines;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.model.routines.IRoutinesProvider;

public class ESBJavaRoutinesProvider implements IRoutinesProvider {

    public List<URL> getSystemRoutines() {
        List<URL> toReturn = FilesUtils.getFilesFromFolder(Activator.getDefault().getBundle(), "routines", ".java", false, false);
        return toReturn;
    }

    public URL getTalendRoutinesFolder() throws IOException {
        URL url = Activator.getDefault().getBundle().getEntry("routines/system"); //$NON-NLS-1$
        return FileLocator.resolve(url);
    }

    public List<URL> getTalendRoutines() {
        return FilesUtils.getFilesFromFolder(Activator.getDefault().getBundle(), "routines/system", "");
    }

}
