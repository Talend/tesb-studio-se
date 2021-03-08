// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.generator;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.ComboController;

/**
 * Camel language combo list controller
 */
public class LanguageComboController extends ComboController {

    private static String[] displayCodeNames = {
	    "NONE",
	    "BEAN",
	    "CONSTANT",
	    "CORRELATIONID",
	    "EL",
	    "GROOVY",
	    "HEADER",
	    "JAVASCRIPT",
	    "JOSQL",
	    "JSONPATH",
	    "JXPATH",
	    "MVEL",
	    "OGNL",
	    "PHP",
	    "PROPERTY",
	    "PYTHON",
	    "RUBY",
	    "SIMPLE",
	    "SPEL",
	    "SQL",
	    "XPATH",
	    "XQUERY"
	  };

    private static String[] itemValues = {
	    "none",
	    "bean",
	    "constant",
	    "correlation",
	    "el",
	    "groovy",
	    "header",
	    "javaScript",
	    "josql",
	    "jsonpath",
	    "jxpath",
	    "mvel",
	    "ognl",
	    "php",
	    "property",
	    "python",
	    "ruby",
	    "simple",
	    "spel",
	    "sql",
	    "xpath",
	    "xquery"
	  };

    private static String[] displayNames = {
	    "None",
	    "Bean",
	    "Constant",
	    "CorrelationID",
	    "EL",
	    "Groovy",
	    "Header",
	    "JavaScript",
	    "JoSQL",
	    "JSonPath",
	    "JXPath",
	    "MVEL",
	    "OGNL",
	    "PHP",
	    "Property",
	    "Python",
	    "Ruby",
	    "Simple",
	    "SpEl",
	    "SQL",
	    "XPath",
	    "XQuery"
	  };

    public LanguageComboController(IDynamicProperty dp) {
        super(dp);
        for (IElementParameter param : elem.getElementParameters()) {
            if (EParameterFieldType.LANGUAGE_CLOSED_LIST.equals(param.getFieldType())) {
                addLanguageList(param);
            } else if (EParameterFieldType.TABLE.equals(param.getFieldType())) {
                for (Object item : param.getListItemsValue()) {
                    if (item instanceof IElementParameter) {
                        IElementParameter itemParam = (IElementParameter) item;
                        if ("LANGUAGE".equals(itemParam.getName())) {
                            addLanguageList(itemParam);
                        }
                    }
                }
            }
        }
    }

    private void addLanguageList(IElementParameter param) {
        if (param.getListItemsValue() == null || param.getListItemsValue().length == 0) {
            param.setListItemsValue(itemValues);
            param.setListItemsDisplayCodeName(displayCodeNames);
            param.setListItemsDisplayName(displayNames);
            param.setListRepositoryItems(new String[itemValues.length]);
            param.setListItemsShowIf(new String[itemValues.length]);
            param.setListItemsNotShowIf(new String[itemValues.length]);
            param.setListItemsReadOnlyIf(new String[itemValues.length]);
            param.setListItemsNotReadOnlyIf(new String[itemValues.length]);

            if (param.getValue() == null || "".equals(param.getValue())) {
                param.setDefaultClosedListValue(itemValues[2]); // constant
            } else {
                for (int i = 0; i < displayCodeNames.length; i++) {
                    if (displayCodeNames[i].equals(param.getValue())) {
                        param.setValue(itemValues[i]);
                        break;
                    }
                }
            }
        }
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
    }
}
