package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;

interface FunctionSelectionListener extends EventListener {

    void functionSelected(Function function);

    ExtendedTableModel<Function> getFunctionTableModel();
}

public class FunctionTableViewPart extends AbstractTableViewPart<FunctionSelectionListener, Function> {

    private Function selectedFunction;

    public FunctionTableViewPart(FunctionSelectionListener eventListener) {
        super(eventListener);
    }

    @Override
    String getLabelKey() {
        return "WebServiceUI.Operation";
    }

    @Override
    ExtendedTableModel<Function> getTableModel() {
        return listener.getFunctionTableModel();
    }

    @Override
    String getItemLabel(Function item) {
        return item.getName();
    }

    @Override
    void itemSelected(Function function) {
        selectedFunction = function;
        listener.functionSelected(function);
    }

    public Function getSelectedFunction() {
        return selectedFunction;
    }

    public boolean selectFirstFunction() {
        return selectFirstElement();
    }

}
