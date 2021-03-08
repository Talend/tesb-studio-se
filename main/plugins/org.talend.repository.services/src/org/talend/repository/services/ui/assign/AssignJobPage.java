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
package org.talend.repository.services.ui.assign;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.services.action.AssignJobAction;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

public class AssignJobPage extends WizardPage {

	private AssignJobAction assignJobAction;
	private AssignJobReviewDialog dialog;

	protected AssignJobPage(String pageName, AssignJobAction assignJobAction) {
		super(pageName);
		this.assignJobAction = assignJobAction;
	}

	@Override
	public void createControl(Composite parent) {
		dialog = new AssignJobReviewDialog(
				(AssignJobWizardDialog) getContainer(), parent.getShell(),
				ERepositoryObjectType.PROCESS, "");
		dialog.setJobIDList(assignJobAction.getAllReferenceJobId());
		setControl(dialog.create(parent));
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	public boolean finish() {
		dialog.finish();
		assignJobAction.changeOldJob();
		return assignJobAction.assign(dialog.getResult());
	}

	private static class AssignJobReviewDialog extends RepositoryReviewDialog {

		private final AssignJobWizardDialog container;

		public AssignJobReviewDialog(AssignJobWizardDialog container,
				Shell parentShell, ERepositoryObjectType type,
				String repositoryType) {
			super(parentShell, type, repositoryType);
			this.container = container;
		}

		public Control create(Composite parent) {
			return createDialogArea(parent);
		}

		@Override
		protected Button getButton(int id) {
			if (id == OK) {
				return container.doGetButton(IDialogConstants.FINISH_ID);
			} else if (id == CANCEL) {
				return container.doGetButton(IDialogConstants.CANCEL_ID);
			}
			return super.getButton(id);
		}

		public void finish() {
			okPressed();
		}
	}
}
