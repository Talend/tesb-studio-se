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
package org.talend.designer.esb.webservice.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.esb.webservice.WebServiceComponentPlugin;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.util.RouteResourcesHelper;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;
import org.talend.repository.model.RepositoryNode;

/**
 * gcui class global comment. Detailled comment
 */
public class WebServiceUI extends WizardPage implements RouteResourceSelectionListener, ServiceSelectionListener {

	protected int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
			.getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

	private WebServiceNode webServiceNode;

	private WsdlFieldPart wsdlFieldPart;

	private ServicePortTableViewPart portViewPart;
	private FunctionTableViewPart functionViewPart;

	private Button populateCheckbox;

	private WebServiceUIPresenter presenter;

	public WebServiceUI(WebServiceNode webServiceNode) {
		super("WebServiceUI"); //$NON-NLS-1$
		setTitle("Configure component with Web Service operation");
		this.webServiceNode = webServiceNode;
		this.presenter = new WebServiceUIPresenter(this, webServiceNode);
	}

	public void createControl(Composite parent) {
		presenter.initWithCurrentSetting();

		Composite wsdlComposite = new Composite(parent, SWT.NONE);

		// WSDL URL
		// 3 columns
		createWsdlFieldControl(wsdlComposite);

		int wsdlUrlcompositeColumn = 4;
		// TESB-3590，gliu
		if (WebServiceComponentPlugin.hasRepositoryServices()) {
			wsdlUrlcompositeColumn++;
			new ServicesButtonPart(this).createControl(wsdlComposite);
		}
		//TESB-13923
		if(presenter.showResourcesButton()) {
			wsdlUrlcompositeColumn++;
			new RouteResourcesButtonPart(this).createControl(wsdlComposite);
		}
		new RefreshButtonPart(wsdlFieldPart).createControl(wsdlComposite);

		GridLayout layout = new GridLayout(wsdlUrlcompositeColumn, false);
		wsdlComposite.setLayout(layout);

		// add port name UI
		portViewPart = new ServicePortTableViewPart(presenter);
		portViewPart.createControl(wsdlComposite);

		// WSDL Operation
		if (presenter.isFunctionRequired()) {
			functionViewPart = new FunctionTableViewPart(presenter);
			functionViewPart.createControl(wsdlComposite);
		}

		if (presenter.allowPopulateSchema()) {
			populateCheckbox = new Button(wsdlComposite, SWT.CHECK);
			populateCheckbox.setText("Populate schema to repository on finish");
			populateCheckbox.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, wsdlUrlcompositeColumn, 1));
		}

		setControl(wsdlComposite);
		setPageComplete(false);
	}

	private void createWsdlFieldControl(Composite wsdlComposite) {
		wsdlFieldPart = new WsdlFieldPart(presenter);
		wsdlFieldPart.createControl(wsdlComposite);

        String initialWsdlLocation = presenter.getInitialWsdlLocation();
		wsdlFieldPart.setInitData(webServiceNode, initialWsdlLocation);
	}

	public void runWithProgress(IRunnableWithProgress runnableWithProgress) throws InvocationTargetException,
			InterruptedException {
		getContainer().run(true, false, runnableWithProgress);
	}

	public void selectFirstFunction() {
		if(functionViewPart == null) {
			//no need to select a funciton.
			setPageComplete(true);
			return;
		}
		boolean success = functionViewPart.selectFirstFunction();
		setPageComplete(success);
	}

	/**
	 * true to indicate the finish request was accepted, and false to indicate
	 * that the finish request was refused
	 */
	public boolean performFinish() {
		IStatus status = presenter.performFinishWithFunction(getSelectedFunction());
		if (!status.isOK()) {
			setErrorMessage(status.getMessage());
		}
		return status.isOK();
	}

	boolean needPopulateSchema() {
		return populateCheckbox != null && populateCheckbox.getSelection();
	}

	private Function getSelectedFunction() {
		if(functionViewPart == null) {
			return null;
		}
		return functionViewPart.getSelectedFunction();
	}

	@Override
	public void routeResourceNodeSelected(RepositoryNode resourceNode) {
		IRepositoryViewObject viewObject = resourceNode.getObject();
		String loc = RouteResourcesHelper.getRouteResourcesLocation(viewObject);
		if(loc!=null) {
			wsdlFieldPart.setRawFieldValue(loc);
			presenter.resourceNodeSelected(viewObject);
		}
		
	}

	@Override
	public void serviceNodeSelected(RepositoryNode serviceNode) {
		Item item = serviceNode.getObject().getProperty().getItem();
		IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(
				IESBService.class);
		String wsdlFilePath = service.getWsdlFilePath(item);
		if (wsdlFilePath != null) {
			wsdlFieldPart.setRawFieldValue(wsdlFilePath);
		}
	}

}
