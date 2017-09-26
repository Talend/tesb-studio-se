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
package org.talend.camel.designer.codegen.jet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.codegen.jet.JETException;
import org.talend.camel.designer.codegen.Activator;
import org.talend.camel.designer.codegen.i18n.Messages;
import org.talend.camel.designer.codegen.util.NodeUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.codegen.proxy.JetProxy;

public class JetUtil {

	private static Logger log = Logger.getLogger(JetUtil.class);

	private static final long INIT_TIMEOUT = 15L * 60 * 1000; // 15s

	private static final long INIT_PAUSE = 1000L; // 1s

	/**
	 * Initialize Jet Bean to pass to the Jet Generator.
	 * 
	 * @param argument
	 *            the node to convert
	 * @return the initialized JetBean
	 */
	public static JetBean createJetBean(CodeGeneratorArgument argument) {
		final JetBean jetBean = new JetBean();
		jetBean.setArgument(argument);

		if (argument != null && argument.getArgument() instanceof INode) {
			INode node = (INode) argument.getArgument();
			String componentsPath = IComponentsFactory.COMPONENTS_LOCATION;

			IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
					IBrandingService.class);
			if (breaningService.isPoweredOnlyCamel()) {
				componentsPath = IComponentsFactory.CAMEL_COMPONENTS_LOCATION;
			}

			jetBean.setJetPluginRepository(componentsPath);

			initTemplateRelativeUri(jetBean, node, argument.getCodePart());
		}
		if (jetBean.getJetPluginRepository() == null) {
			jetBean.setJetPluginRepository(Activator.getDefault().getBundle().getSymbolicName());
		}
		return jetBean;
	}

	private static void initTemplateRelativeUri(JetBean jetBean, INode node, ECodePart codePart) {
		String templateURI = NodeUtil.getTemplateURI(node, codePart);
		jetBean.setTemplateRelativeUri(templateURI);
	}

	public static String jetGenerate(JetBean jetBean) throws CodeGeneratorException {
		JetProxy proxy = new JetProxy(jetBean);
		try {
			String codePart = proxy.generate();
			String generationError = jetBean.getGenerationError();
			if (generationError != null) {
				throw new CodeGeneratorException(generationError);
			}
			return codePart;
		} catch (JETException e) {
			log.error(e.getMessage(), e);
			CodeGeneratorArgument argument = (CodeGeneratorArgument) jetBean.getArgument();
			throw new CodeGeneratorException(e + " in " + argument.getJobName() + " route", e);
		} catch (CoreException e) {
			log.error(e.getMessage(), e);
			throw new CodeGeneratorException(e);
		}
	}

	public static String jetGenerate(CodeGeneratorArgument argument) throws CodeGeneratorException {
		JetBean jetBean = createJetBean(argument);
		return jetGenerate(jetBean);
	}

	public static void checkEmittersPoolFactoryIsReady() throws CodeGeneratorException {
		long startTimer = System.currentTimeMillis();
		long endTimer = startTimer;
		try {
			while (!CodeGeneratorEmittersPoolFactory.isInitialized() && (endTimer - startTimer) < INIT_TIMEOUT) {
				Thread.sleep(INIT_PAUSE);
				endTimer = System.currentTimeMillis();
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new CodeGeneratorException(e);
		}
		if ((endTimer - startTimer) > INIT_TIMEOUT) {
			throw new CodeGeneratorException(Messages.getString("CodeGenerator.JET.TimeOut")); //$NON-NLS-1$
		}
	}
}
