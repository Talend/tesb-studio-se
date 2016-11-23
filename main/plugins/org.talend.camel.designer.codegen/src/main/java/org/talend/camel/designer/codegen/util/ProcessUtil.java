package org.talend.camel.designer.codegen.util;

import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;

public class ProcessUtil {

    public static ECodeLanguage getCodeLanguage() {
        RepositoryContext context = (RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY);
        Project project = context.getProject();
        return project.getLanguage();
    }

    public static String getCodeLanguageExtension() {
        return getCodeLanguage().getExtension();
    }

    public static boolean getRunInMultiThread(IProcess process) {
        boolean running = false;
        // check the mutli-thread parameter in Job Settings.
        if (process != null) {
            IElementParameter parameter = process.getElementParameter("MULTI_THREAD_EXECATION"); //$NON-NLS-1$
            if (parameter != null) {
                Object obj = parameter.getValue();
                if (obj instanceof Boolean && ((Boolean) obj).booleanValue()) {
                    running = true;
                }
            }
        }
        return running;
    }
}
