package org.talend.designer.esb.webservice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.wsdl.Definition;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;

public class SchemaTool {

    public static IStatus populateSchema(IWizardPage wizardPage, Definition definition) {
        return populateSchema(wizardPage.getControl().getShell(), wizardPage.getWizard().getContainer(), definition);
    }

    private static IStatus populateSchema(Shell shell, IWizardContainer container, Definition definition) {
        IRunnableWithProgress runnable;
        try {
            Class<? extends IRunnableWithProgress> forName = Class
                    .forName("org.talend.repository.services.action.PublishMetadataRunnable")
                    .asSubclass(IRunnableWithProgress.class);
            Constructor<? extends IRunnableWithProgress> constructor = forName.getConstructor(Definition.class, Shell.class);
            runnable = constructor.newInstance(definition, shell);
        } catch (Exception e) {
            String message = (null != e.getMessage()) ? e.getMessage() : e.getClass().getName();
            return WebServiceComponentPlugin.getStatus("Can't create populate action: " + message, e);
        }
        try {
            container.run(true, true, runnable);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            String message = (null != cause.getMessage()) ? cause.getMessage() : cause.getClass().getName();
            return WebServiceComponentPlugin.getStatus("Populate schema to repository: " + message, e);
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

}
