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
package org.talend.camel.designer.codegen.partgen.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.partgen.PartGeneratorManager;
import org.talend.camel.designer.codegen.util.NodeUtil;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.config.NodesTree;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class ProcessPartBuilder extends AbstractProcessPartBuilder {

	private final NodesTree nodesTree;

	private final List<NodesSubTree> subTrees;

	private final List<NodesSubTree> sortedFilteredSubTrees;

	public ProcessPartBuilder(PartGeneratorManager generatorManager) {
		super(generatorManager);
		this.nodesTree = manager.getArgumentBuilder().getProcessTree();
		this.subTrees = nodesTree.getSubTrees();

		if (hasSubTrees()) {
			sortedFilteredSubTrees = new ArrayList<NodesSubTree>();
			List<NodesSubTree> postPositiveTrees = new ArrayList<NodesSubTree>();
			for (NodesSubTree subTree : subTrees) {
				INode startNode = subTree.getRootNode().getSubProcessStartNode(true);
				if (subTreePostpositive(startNode)) {
					postPositiveTrees.add(subTree);
					continue;
				}
				if (subTreeNeedSkip(startNode)) {
					continue;
				}
				sortedFilteredSubTrees.add(subTree);
			}
			sortedFilteredSubTrees.addAll(postPositiveTrees);
		} else {
			sortedFilteredSubTrees = Collections.emptyList();
		}
	}

	@Override
	public AbstractProcessPartBuilder appendContent() throws CodeGeneratorException {
		appendTyped(ECamelTemplate.HEADER_ROUTE);
		if (hasSubTrees()) {
			for (NodesSubTree subTree : sortedFilteredSubTrees) {
				appendSubTree(subTree);
			}
		}

		appendTyped(ECamelTemplate.FOOTER_ROUTE);
		appendTyped(ECamelTemplate.PROCESSINFO);
		return this;
	}

	private boolean hasSubTrees() {
		return subTrees != null && !subTrees.isEmpty();
	}

//	private NodesSubTree getFirstSubTree() {
//		return subTrees.get(0);
//	}

//	private NodesSubTree getLastSubTree() {
//		NodesSubTree lastSubTree = sortedFilteredSubTrees.get(sortedFilteredSubTrees.size() - 1);
//		if (lastSubTree == null) {
//			lastSubTree = getFirstSubTree();
//		}
//		return lastSubTree;
//	}

	private static boolean subTreePostpositive(INode subProcessStartNode) {
        IElementParameter family = subProcessStartNode.getElementParameter("FAMILY"); //$NON-NLS-1$
        // https://jira.talendforge.org/browse/TESB-16530
        return subProcessStartNode.isStart() && null != family && !"Exception Handling".equals(family.getValue());
    }

	private static boolean subTreeNeedSkip(INode subProcessStartNode) {
		return NodeUtil.isConfigComponentNode(subProcessStartNode);
	}
}
