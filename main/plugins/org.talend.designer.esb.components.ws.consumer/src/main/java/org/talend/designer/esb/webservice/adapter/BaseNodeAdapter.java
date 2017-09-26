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
package org.talend.designer.esb.webservice.adapter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.eclipse.swt.widgets.Display;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.util.WSDLHelper;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;

public abstract class BaseNodeAdapter {

    protected WebServiceNode node;

	public BaseNodeAdapter(WebServiceNode node) {
		this.node = node;
	}

	public Definition generateDefinition(String inputWsdlLocation) throws InvocationTargetException, WSDLException {
		String wsdl = getExpressionParamValue(inputWsdlLocation);
		
		setupSSLIfNeeded();
		Definition definition = WSDLHelper.load(wsdl, node.getUniqueName());
		return definition;
	}

	public String getExpressionParamValue(String paramValueExpression) {
		if (paramValueExpression.indexOf('"') != -1) {
			return parseContextParameter(paramValueExpression);
		}
		return TalendTextUtils.removeQuotes(paramValueExpression);
	}

	private String parseContextParameter(final String contextValue) {
		IContextManager contextManager = node.getProcess().getContextManager();
		String currentDefaultName = contextManager.getDefaultContext().getName();
		List<IContext> contextList = contextManager.getListContext();
		if (contextList != null && contextList.size() > 1) {
			currentDefaultName = ConnectionContextHelper.getContextTypeForJob(Display.getDefault().getActiveShell(), contextManager, false);
		}
		IContext context = contextManager.getContext(currentDefaultName);
		return ContextParameterUtils.parseScriptContextCode(contextValue, context);
	}

	private void setupSSLIfNeeded() {
		boolean needSSL = node.getBooleanValue("NEED_SSL_TO_TRUSTSERVER");
		if(needSSL) {
			setParamToSystemProperty("SSL_TRUSTSERVER_TRUSTSTORE", "javax.net.ssl.trustStore");
			setParamToSystemProperty("SSL_TRUSTSERVER_PASSWORD", "javax.net.ssl.trustStorePassword");
		}
	}

	private void setParamToSystemProperty(String paramK, String propertyK) {
		IElementParameter parameter = node.getElementParameter(paramK);
		Object value = parameter == null ? null : parameter.getValue();
		if (value != null) {
			String string = TalendTextUtils.removeQuotes(value.toString());
			System.setProperty(propertyK, string);
		}
	}

}
