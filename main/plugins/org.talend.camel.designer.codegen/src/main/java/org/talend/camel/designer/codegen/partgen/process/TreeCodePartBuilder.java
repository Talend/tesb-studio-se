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
package org.talend.camel.designer.codegen.partgen.process;

import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.partgen.CamelConnectionTagGenerator;
import org.talend.camel.designer.codegen.partgen.PartGeneratorManager;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.config.CloseBlocksCodeArgument;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.config.SubTreeArgument;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class TreeCodePartBuilder extends AbstractProcessPartBuilder {

	private final NodesSubTree subTree;
	private final INode node;
	private final ECodePart part;

	public TreeCodePartBuilder(PartGeneratorManager generatorManager, NodesSubTree subTree, INode node, ECodePart part) {
		super(generatorManager);
		this.subTree = subTree;
		this.node = node;
		this.part = part;
	}

	@Override
	public AbstractProcessPartBuilder appendContent() throws CodeGeneratorException {
		SubTreeArgument subTreeArgument = new SubTreeArgument();

		// Conditional Outputs
		boolean sourceHasConditionnalBranches = node.hasConditionalOutputs() && part == ECodePart.MAIN;
		subTreeArgument.setSourceComponentHasConditionnalOutputs(sourceHasConditionnalBranches);

		// Multiplying Output Rows
		if (part == ECodePart.MAIN) {
			subTreeArgument.setMultiplyingOutputComponents(node.isMultiplyingOutputs());
		}

		for (IConnection connection : node.getOutgoingCamelSortedConnections()) {
			INode targetNode = connection.getTarget();
			if (targetNode != null && subTree != null) {
				subTreeArgument.setInputSubtreeConnection(connection);
				append(CamelConnectionTagGenerator.generateStartTag(node, connection));
				append(generateComponentsCode(targetNode, connection.getName()));
				append(CamelConnectionTagGenerator.generateEndTag(node, connection));
			}
		}

		if (part == ECodePart.MAIN && node.getBlocksCodeToClose() != null) {
			CloseBlocksCodeArgument closeBlocksArgument = new CloseBlocksCodeArgument();
			closeBlocksArgument.setBlocksCodeToClose(node.getBlocksCodeToClose());
			appendTyped(ECamelTemplate.CLOSE_BLOCKS_CODE, closeBlocksArgument);
		}
		return this;
	}

	private CharSequence generateComponentsCode(INode targetNode, String name) throws CodeGeneratorException {
		return manager.generateComponentsCode(subTree, targetNode, part, name);
	}

}
