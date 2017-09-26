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
package org.talend.camel.designer.codegen.config;

import org.apache.commons.lang.StringUtils;
import org.talend.camel.designer.codegen.util.ProcessUtil;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.TemplateUtil;

/**
 * Internal Templates for Code Generator, must be always available to
 * encapsulate rela parts of code.
 * 
 * $Id$
 * 
 */
public enum ECamelTemplate {
	FOOTER_ROUTE("footer_route"), //$NON-NLS-1$ 
	HEADER_ROUTE("header_route"), //$NON-NLS-1$ 
	CAMEL_SPECIALLINKS("camel_speciallinks"), //$NON-NLS-1$ 

	// ref templates.
	CONTEXT(EInternalTemplate.CONTEXT), 
	CLOSE_BLOCKS_CODE(EInternalTemplate.CLOSE_BLOCKS_CODE),
	PART_HEADER(EInternalTemplate.PART_HEADER),
	PART_FOOTER(EInternalTemplate.PART_FOOTER),
	PROCESSINFO(EInternalTemplate.PROCESSINFO),
	ITERATE_SUBPROCESS_HEADER(EInternalTemplate.ITERATE_SUBPROCESS_HEADER),
	ITERATE_SUBPROCESS_FOOTER(EInternalTemplate.ITERATE_SUBPROCESS_FOOTER);

	private final String templateName;

	/**
	 * 
	 * @param templateName
	 */
	private ECamelTemplate(String templateName) {
		this.templateName = templateName;
	}

	private ECamelTemplate(EInternalTemplate refTemplate) {
		this.templateName = refTemplate.getTemplateName();
	}

	@Override
	public String toString() {
		return getTemplateURL();
	}

	public String getTemplateFileName() {
		String[] fileNameParts = {templateName,
				  TemplateUtil.EXT_SEP,
				  ProcessUtil.getCodeLanguageExtension(),
				  TemplateUtil.TEMPLATE_EXT};
		return StringUtils.join(fileNameParts);
	}

	public String getTemplateURL() {
		String[] paths = { /*bundleName,*/
						   "resources",
						   getTemplateFileName() };
		return StringUtils.join(paths, TemplateUtil.DIR_SEP);
	}
}
