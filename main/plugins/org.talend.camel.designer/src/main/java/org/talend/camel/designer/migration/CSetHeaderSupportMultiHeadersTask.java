package org.talend.camel.designer.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class CSetHeaderSupportMultiHeadersTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cSetHeader";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 9, 10, 14, 00, 00);
        return gc.getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        List<?> parameters = node.getElementParameter();
        List<ElementParameterType> valuesParams = getValuesParams(parameters);
        if (!valuesParams.isEmpty()) {
            return updateValuesParams(parameters, valuesParams);
        }
        cSetHeaderMigrate(node);
        return true;
    }

    /**
     * Gets all elementParameter with attribute name={@link #STRING_VALUES} .
     *
     * @param parameters the parameters
     * @return the values params
     */
    private List<ElementParameterType> getValuesParams(List<?> parameters) {
        List<ElementParameterType> valuesParams = new ArrayList<>();
        for (Object tmp : parameters) {
            ElementParameterType param = (ElementParameterType) tmp;
            if ("VALUES".equals(param.getName())) {
                valuesParams.add(param);
            }
        }
        return valuesParams;
    }

    /**
     * Update values params due to duplicated or no attribute field="TABLE". Shouldn't happened for normail case but in
     * order to fix [TESB-13416] still do a workaround.
     *
     * @param parameters the parameters
     * @param valuesParams the values params, can't be empty
     * @return true, if needs save
     */
    private boolean updateValuesParams(List<?> parameters, List<ElementParameterType> valuesParams) {
        ElementParameterType keep = null;
        if (valuesParams.size() == 1) {
            keep = valuesParams.get(0);
            if ("TABLE".equals(keep.getField())) {
                return false;
            }
        } else {
            // duplicated params
            for (ElementParameterType valuesParam : valuesParams) {
                if ("TABLE".equals(valuesParam.getField())) {
                    keep = valuesParam;
                    break;
                }
            }

            if (keep == null) {
                keep = valuesParams.remove(0);
            } else {
                valuesParams.remove(keep);
            }
            parameters.removeAll(valuesParams);
        }
        keep.setField("TABLE");
        return true;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void cSetHeaderMigrate(NodeType currentNode) throws PersistenceException {
        List parameters = currentNode.getElementParameter();
        String headerName = null;
        String useBean = null;
        String expression = null;
        String language = null;
        String bean = null;
        for (Object tmp : parameters) {
            ElementParameterType param = (ElementParameterType) tmp;
            String paramName = param.getName();
            if ("HEADER".equals(paramName)) {
                headerName = param.getValue();
            } else if ("USE_BEAN".equals(paramName)) {
                useBean = param.getValue();
            } else if ("BEAN".equals(paramName)) {
                bean = param.getValue();
            } else if ("LANGUAGES".equals(paramName)) {
                language = param.getValue();
            } else if ("EXPRESSION".equals(paramName)) {
                expression = param.getValue();
            }
        }

        // setName
        ElementValueType newNameValue = TalendFileFactory.eINSTANCE.createElementValueType();
        newNameValue.setElementRef("NAME");
        newNameValue.setValue(headerName);

        // set LANGUAGE
        ElementValueType newLanguageValue = TalendFileFactory.eINSTANCE.createElementValueType();
        newLanguageValue.setElementRef("LANGUAGE");
        if ("true".equals(useBean)) {
            newLanguageValue.setValue("bean");
        } else {
            newLanguageValue.setValue(language);
        }

        // set EXPRESSION
        ElementValueType newExpressionValue = TalendFileFactory.eINSTANCE.createElementValueType();
        newExpressionValue.setElementRef("EXPRESSION");
        if ("true".equals(useBean)) {
            newExpressionValue.setValue(bean);
        } else {
            newExpressionValue.setValue(expression);
        }

        ElementParameterType newParameter = TalendFileFactory.eINSTANCE.createElementParameterType();
        newParameter.setName("VALUES");
        newParameter.setField("TABLE");
        newParameter.getElementValue().add(newNameValue);
        newParameter.getElementValue().add(newLanguageValue);
        newParameter.getElementValue().add(newExpressionValue);

        parameters.add(newParameter);
    }

}