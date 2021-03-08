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
package org.talend.camel.designer.codegen.partgen;

import java.util.Arrays;
import java.util.Vector;

import org.talend.camel.designer.codegen.argument.ArgumentBuilderHolder;
import org.talend.camel.designer.codegen.argument.CodeGeneratorArgumentBuilder;
import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.jet.JetUtil;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.config.CloseBlocksCodeArgument;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class TemplatePartGenerator extends ArgumentBuilderHolder implements PartGenerator<ECamelTemplate> {

	public TemplatePartGenerator(CodeGeneratorArgumentBuilder argumentBuilder) {
		super(argumentBuilder);
	}

	public CharSequence generatePart(ECamelTemplate template, Object... params) throws CodeGeneratorException {
		switch (template) {
		case HEADER_ROUTE: {
			Object[] args = { process, VersionUtils.getVersion() };
			return generateTypedComponentCode(template, wrapToVector(args));
		}
		case FOOTER_ROUTE:
			return generateTypedComponentCode(template, wrapToVector(process, null != process.getSpringContent()));

		case PROCESSINFO:
			return generateTypedComponentCode(template, params[0]);

		case CLOSE_BLOCKS_CODE: {
			INode node = (INode) params[0];
			CloseBlocksCodeArgument closeBlocksArgument = new CloseBlocksCodeArgument();
			closeBlocksArgument.setBlocksCodeToClose(node.getBlocksCodeToClose());
			return generateTypedComponentCode(template, closeBlocksArgument);
		}

		default:
			return generateDefaultTypedComponentCode(template, params);
		}
	}

	private CharSequence generateDefaultTypedComponentCode(ECamelTemplate template, Object... params)
			throws CodeGeneratorException {
		Object argument = null;
		if (params.length > 0) {
			argument = params[0];
		}
		ECodePart part = null;
		String incomingName = null;
		NodesSubTree subProcess = null;
		if (params.length > 1) {
			for (int i = 1; i < params.length; i++) {
				if (params[i] instanceof ECodePart) {
					part = (ECodePart) params[i];
				} else if (params[i] instanceof String) {
					incomingName = (String) params[i];
				} else if (params[i] instanceof NodesSubTree) {
					subProcess = (NodesSubTree) params[i];
				}
			}
		}
		return generateTypedComponentCode(template, argument, part, incomingName, subProcess);
	}

	private CharSequence generateTypedComponentCode(ECamelTemplate template, Object argument)
			throws CodeGeneratorException {
		return generateTypedComponentCode(template, argument, null, null, null);
	}

	/**
	 * Generate Code Part for a given Component.
	 *
	 * @param type
	 *            the internal component template
	 * @param argument
	 *            the bean
	 * @param part
	 *            part of code to generate
	 * @param subProcess
	 * @return the genrated code
	 * @throws CodeGeneratorException
	 *             if an error occurs during Code Generation
	 */
	private CharSequence generateTypedComponentCode(ECamelTemplate type, Object argument, ECodePart part,
			String incomingName, NodesSubTree subProcess) throws CodeGeneratorException {
		CodeGeneratorArgument codeGenArgument = argumentBuilder.build();
		codeGenArgument.setNode(argument);
		if (subProcess != null) {
			codeGenArgument.setAllMainSubTreeConnections(subProcess.getAllMainSubTreeConnections());
			codeGenArgument.setSubTree(subProcess);
		}
		codeGenArgument.setCodePart(part);
		codeGenArgument.setIncomingName(incomingName);

		JetBean jetBean = JetUtil.createJetBean(codeGenArgument);
		jetBean.setTemplateRelativeUri(type.getTemplateURL());
		return JetUtil.jetGenerate(jetBean);
	}

	private static Vector<Object> wrapToVector(Object... objs) {
		return new Vector<Object>(Arrays.asList(objs));
	}

}
