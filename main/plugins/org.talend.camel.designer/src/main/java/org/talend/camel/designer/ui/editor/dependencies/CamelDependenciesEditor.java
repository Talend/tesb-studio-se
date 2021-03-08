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
package org.talend.camel.designer.ui.editor.dependencies;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.ui.editor.dependencies.controls.SearchControl;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.camel.dependencies.core.DependenciesResolver;
import org.talend.designer.camel.dependencies.core.model.BundleClasspath;
import org.talend.designer.camel.dependencies.core.model.ExportPackage;
import org.talend.designer.camel.dependencies.core.model.ImportPackage;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;
import org.talend.designer.camel.dependencies.core.model.RequireBundle;
import org.talend.designer.camel.dependencies.core.util.DependenciesCoreUtil;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;

public class CamelDependenciesEditor extends EditorPart implements IRouterDependenciesChangedListener, IMessagePart {

    private final CommandStack commandStack;
    private final boolean isReadOnly;

    private Label statusLabel;

	//show all datas
	private CamelDependenciesPanel requireBundleViewer;
	private CamelDependenciesPanel bundleClasspathViewer;
	private CamelDependenciesPanel importPackageViewer;
	private CamelDependenciesPanel exportPackageViewer;
	private ManageRouteResourcePanel manageRouteResourcePanel;

	public CamelDependenciesEditor(final AbstractMultiPageTalendEditor editor, boolean isReadOnly) {
	    commandStack = editor.getTalendEditor().getCommandStack();
	    this.isReadOnly = isReadOnly;
    }

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setInput(input);
		setSite(site);
	}

    @Override
	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		parent.setLayout(new GridLayout());

        //create search group, hide button and refresh button
        Composite toolsPanel = toolkit.createComposite(parent);
        toolsPanel.setLayout(new GridLayout(4, false));
        toolsPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolsPanel.setBackgroundMode(SWT.INHERIT_FORCE);

        toolkit.createLabel(toolsPanel, Messages.RouterDependenciesEditor_filterLabel);

        SearchControl searchComposite = new SearchControl(toolsPanel, SWT.NONE);
        searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        searchComposite.setActiveImage(CamelDesignerPlugin
                .getImage(CamelDesignerPlugin.HIGHLIGHT_REM_ICON));
        searchComposite.setDeactiveImage(CamelDesignerPlugin
                .getImage(CamelDesignerPlugin.GRAY_REM_ICON));
        Text filterText = searchComposite.getText();

        Button hideBuiltIn = toolkit.createButton(toolsPanel,
                Messages.RouterDependenciesEditor_hideBuiltInItems, SWT.CHECK);

        Button refreshBtn = toolkit.createButton(toolsPanel, null, SWT.PUSH);
        refreshBtn.setImage(CamelDesignerPlugin.getImage(CamelDesignerPlugin.REFRESH_ICON));
        refreshBtn
                .setToolTipText(Messages.RouterDependenciesEditor_refreshDependenciesTooltip);
        refreshBtn.setEnabled(!isReadOnly());

		// create data tables
        ScrolledComposite top = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
		SashForm mainForm = new SashForm(top, SWT.VERTICAL);

        manageRouteResourcePanel =
            createResourceTableViewer(mainForm, toolkit, Messages.CamelDependenciesEditor_Resources);

        SashForm topPart = new SashForm(mainForm, SWT.HORIZONTAL);
        importPackageViewer = createTableViewer(topPart, toolkit, Messages.RouterDependenciesEditor_importPackageSec,
            ManifestItem.IMPORT_PACKAGE);
        bundleClasspathViewer = createTableViewer(topPart, toolkit, Messages.RouterDependenciesEditor_classpathSec,
            ManifestItem.BUNDLE_CLASSPATH);

        SashForm centerPart = new SashForm(mainForm, SWT.HORIZONTAL);
        exportPackageViewer = createTableViewer(centerPart, toolkit, Messages.RouterDependenciesEditor_exportPackage,
            ManifestItem.EXPORT_PACKAGE);
        requireBundleViewer = createTableViewer(centerPart, toolkit,
            Messages.RouterDependenciesEditor_requireBundleSec, ManifestItem.REQUIRE_BUNDLE);

		top.setExpandHorizontal(true);
		top.setExpandVertical(true);
        top.setContent(mainForm);
		top.setMinSize(mainForm.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		//create status
		Composite statusComposite = toolkit.createComposite(parent);
		statusComposite.setLayout(new GridLayout(2, false));
		statusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		statusLabel = toolkit.createLabel(statusComposite, null);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		if (isReadOnly()) {
//			statusLabel.setText(Messages.RouterDependenciesEditor_itemIsLockedByOther);
//		}

		Label shortCuts = toolkit.createLabel(statusComposite, Messages.RouterDependenciesEditor_KeyBindingw, SWT.SHADOW_OUT);
		shortCuts.setEnabled(false);

		// add filter listener
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final String filterString = ((Text) e.widget).getText().trim();
				importPackageViewer.setFilterString(filterString);
				requireBundleViewer.setFilterString(filterString);
				bundleClasspathViewer.setFilterString(filterString);
				exportPackageViewer.setFilterString(filterString);
				manageRouteResourcePanel.setFilterString(filterString);
			}
		});

		// add hide listener
		hideBuiltIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean show = ((Button) e.widget).getSelection();
				importPackageViewer.setShowBuiltIn(!show);
				requireBundleViewer.setShowBuiltIn(!show);
				bundleClasspathViewer.setShowBuiltIn(!show);
				exportPackageViewer.setShowBuiltIn(!show);
                manageRouteResourcePanel.setShowBuiltIn(!show);
			}
		});

		refreshBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//	          setDirty(false);
		        updateInput();
			}
		});
	}

	public void setMessage(String message){
		if(message == null){
			message = ""; //$NON-NLS-1$
		}
		statusLabel.setText(message);
		statusLabel.setToolTipText(message);
	}

    private JobEditorInput getJobEditorInput() {
        return (JobEditorInput) getEditorInput();
    }

    private void updateInput() {
        // re-calculate all datas
        final DependenciesResolver resolver = new DependenciesResolver(getJobEditorInput().getLoadedProcess());
        // set datas
        importPackageViewer.setInput(resolver.getImportPackages());
        requireBundleViewer.setInput(resolver.getRequireBundles());
        bundleClasspathViewer.setInput(resolver.getBundleClasspaths());
        exportPackageViewer.setInput(resolver.getExportPackages());

        manageRouteResourcePanel.setInput(
            RouteResourceUtil.getResourceDependencies(getJobEditorInput().getLoadedProcess()));
    }

    private CamelDependenciesPanel createTableViewer(Composite parent, FormToolkit toolkit, String title, String type) {
        Section section = toolkit.createSection(parent, Section.TITLE_BAR);
        section.setText(title);
        final CamelDependenciesPanel c = (type == ManifestItem.BUNDLE_CLASSPATH)
            ? new CheckedCamelDependenciesPanel(section, type, isReadOnly(), this, this)
            : new CamelDependenciesPanel(section, type, isReadOnly(), this, this);
        section.setClient(c);
        toolkit.adapt(c);
        return c;
    }

    private ManageRouteResourcePanel createResourceTableViewer(Composite parent, FormToolkit toolkit, String title) {
        Section section = toolkit.createSection(parent, Section.TITLE_BAR);
        section.setText(title);

        final ManageRouteResourcePanel c = new ManageRouteResourcePanel(section, isReadOnly(), this, this);

        section.setClient(c);
        toolkit.adapt(c);
        return c;
    }

    @Override
	public void dependencesChanged(Composite source) {
//		setDirty(true);
        final Command cmd;
        if (source == manageRouteResourcePanel) {
            cmd = new Command() {
                @Override
                public void execute() {
                    RouteResourceUtil.saveResourceDependency(
                        getJobEditorInput().getLoadedProcess().getAdditionalProperties(),
                        manageRouteResourcePanel.getInput());
                    RelationshipItemBuilder.getInstance().addOrUpdateItem(getJobEditorInput().getItem());
                }
            };
        } else {
            cmd = new Command() {
                @Override
                public void execute() {
                    //save all datas
                    DependenciesCoreUtil.saveToMap(
                            getJobEditorInput().getLoadedProcess().getAdditionalProperties(),
                            (Collection<BundleClasspath>) bundleClasspathViewer.getInput(),
                            (Collection<ImportPackage>) importPackageViewer.getInput(),
                            (Collection<RequireBundle>) requireBundleViewer.getInput(),
                            (Collection<ExportPackage>)exportPackageViewer.getInput());
                }
            };
        }
        commandStack.execute(cmd);
//		doSave(new NullProgressMonitor());
	}

    @Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void setFocus() {
//		importPackageViewer.getTableViewer().getTable().setFocus();
        // update all tables input
        updateInput();
        setMessage(null);
	}

    private boolean isReadOnly() {
        return isReadOnly;
    }

}
