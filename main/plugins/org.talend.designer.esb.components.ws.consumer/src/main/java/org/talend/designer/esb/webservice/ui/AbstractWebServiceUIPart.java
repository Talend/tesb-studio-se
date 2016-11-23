package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractWebServiceUIPart<T extends EventListener> {

    protected final T listener;

    public AbstractWebServiceUIPart(T eventListener) {
        this.listener = eventListener;
    }

    abstract Control createControl(Composite parent);

}
