// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.IEditorInput;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.DragAndDropManager;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.model.utils.IDragAndDropServiceHandler;
import org.talend.core.repository.RepositoryComponentManager;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.process.TalendEditorDropTargetListener;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * Route builder DND listener. To handle specific exception and improve performance.
 *
 * @since 6.3.0
 */
public class CamelEditorDropTargetListener extends TalendEditorDropTargetListener {

    private AbstractTalendEditor editor;

    private boolean isContextSource = false;

    private List<Object> selectSourceList = new ArrayList<Object>();

    public CamelEditorDropTargetListener(AbstractTalendEditor editor) {
        super(editor);
        this.editor = editor;
    }

    @Override
    public boolean isEnabled(DropTargetEvent e) {
        return !this.editor.getProcess().isReadOnly();
    }

    @Override
    protected void handleDrop() {
        if (!checkSelectionSource()) {
            return;
        }

        updateTargetRequest();
        updateTargetEditPart();

        if (selectSourceList.get(0) instanceof PaletteEditPart && getTargetRequest() instanceof CreateRequest) {
            if (getTargetEditPart() instanceof ProcessPart) {

                Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                if (newObject != null) {
                    Command command = getCommand();
                    if (command != null) {
                        execCommandStack(command);
                    }
                }
            }
            return;
        }

        if (isContextSource) {
            createContext();
        } else {
            if (!(getTargetEditPart() instanceof NodeContainerPart)) {
                try {
                    createNewComponent(getCurrentEvent());
                } catch (OperationCanceledException e) {
                    return;
                }
            }

        }
        // in case after drag/drop the editor is dirty but can not get focus
        if (editor.isDirty()) {
            editor.setFocus();
        }
        this.eraseTargetFeedback();
    }

    /**
     * Routelet and context types are validated
     *
     * @return true if validated
     */
    private boolean checkSelectionSource() {
        isContextSource = false;
        selectSourceList.clear();
        boolean isRouteletSource = false;

        LocalSelectionTransfer transfer = (LocalSelectionTransfer) getTransfer();
        IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) obj;
                if (sourceNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.PROCESS_ROUTELET) {
                    selectSourceList.add(obj);
                    isRouteletSource = true;
                }

                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ContextItem) {
                    selectSourceList.add(obj);
                    isContextSource = true;
                }
            } else if (obj instanceof PaletteEditPart) {
                selectSourceList.add(obj);
                Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                if (newObject != null && newObject instanceof INode) {
                    if (((INode) newObject).getComponent().getComponentType() == EComponentType.JOBLET) {
                        selectSourceList.add(newObject);
                        isRouteletSource = true;
                    }
                }
            }
        }

        if (selectSourceList.size() == 0) {
            return false;
        }

        if (isRouteletSource) {
            List<String> routeletList = new ArrayList<String>();
            for (INode node : editor.getProcess().getGraphicalNodes()) {
                if (node.getComponent().getComponentType() == EComponentType.JOBLET) {
                    routeletList.add(node.getComponent().getName());
                }
            }
            boolean isDuplicateRoutelet = false;
            String duplicatesName = "";
            for (Object object : selectSourceList) {
                if (object instanceof RepositoryNode) {
                    RepositoryNode sourceNode = (RepositoryNode) object;
                    if (sourceNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.PROCESS_ROUTELET) {
                        if (routeletList.contains(sourceNode.getProperties(EProperties.LABEL))) {
                            isDuplicateRoutelet = true;
                            duplicatesName += " ," + sourceNode.getProperties(EProperties.LABEL); //$NON-NLS-1$
                        }
                    }
                } else if (object instanceof INode) {
                    INode node = (INode) object;
                    if (node.getComponent().getComponentType() == EComponentType.JOBLET) {
                        if (routeletList.contains(node.getComponent().getName())) {
                            isDuplicateRoutelet = true;
                            duplicatesName += " ," + node.getComponent().getName(); //$NON-NLS-1$
                        }
                    }
                }
            }
            if (isDuplicateRoutelet) {
                MessageDialog.openInformation(editor.getEditorSite().getShell(), "Adding Routelet", //$NON-NLS-1$
                        "Do not allow duplicate Routelets\nRoutelet \"" + duplicatesName.substring(2) + "\" already exist."); //$NON-NLS-1$
                return false;
            }
        }

        return true;
    }

    private void createContext() {
        if (selectSourceList.size() == 0) {
            return;
        }
        boolean created = false;
        for (Object source : selectSourceList) {
            if (source instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) source;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ContextItem) {
                    ContextItem contextItem = (ContextItem) item;
                    EList context = contextItem.getContext();
                    Set<String> contextSet = new HashSet<String>();
                    Iterator iterator = context.iterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof ContextTypeImpl) {
                            EList contextParameters = ((ContextTypeImpl) obj).getContextParameter();
                            Iterator contextParas = contextParameters.iterator();
                            while (contextParas.hasNext()) {
                                ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas.next();
                                String name = contextParameterType.getName();
                                contextSet.add(name);
                            }
                        }
                    }
                    IEditorInput editorInput = editor.getEditorInput();
                    if (editorInput instanceof JobEditorInput) {
                        JobEditorInput jobInput = (JobEditorInput) editorInput;
                        IProcess2 process = jobInput.getLoadedProcess();
                        IContextManager contextManager = process.getContextManager();
                        Map<String, String> renamedMap = ContextUtils.getContextParamterRenamedMap(process.getProperty().getItem());
                        Set<String> addedVars = ConnectionContextHelper.checkAndAddContextVariables(contextItem, contextSet,
                                process.getContextManager(), false, renamedMap);
                        if (addedVars != null && !addedVars.isEmpty()
                                && !ConnectionContextHelper.isAddContextVar(contextItem, contextManager, contextSet)) {
                            // show
                            Map<String, Set<String>> addedVarsMap = new HashMap<String, Set<String>>();
                            addedVarsMap.put(item.getProperty().getLabel(), contextSet);
                            if (ConnectionContextHelper.showContextdialog(process, contextItem, process.getContextManager(),
                                    addedVarsMap, contextSet)) {
                                created = true;
                            }
                        } else {
                            MessageDialog.openInformation(editor.getEditorSite().getShell(), "Adding Context", //$NON-NLS-1$
                                    "Context \"" + contextItem.getProperty().getDisplayName() + "\" already exist."); //$NON-NLS-1$

                        }
                    }
                }
            }
        }
        if (created) {
            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
        }
    }

    /**
     * Used to store data temporarily. <br/>
     */
    class TempStore {

        // This is the element that user select in the repositoryView.
        RepositoryNode seletetedNode = null;

        IComponentName componentName = null;

        IComponent component;

    }

    public void createNewComponent(DropTargetEvent event1) {
        boolean quickCreateInput = event1.detail == DND.DROP_LINK;
        boolean quickCreateOutput = event1.detail == DND.DROP_COPY;
        List<TempStore> list = new ArrayList<TempStore>();
        List<IComponent> components = new ArrayList<IComponent>();
        for (Object obj : selectSourceList) {
            if (obj instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) obj;
                ERepositoryObjectType type = sourceNode.getObjectType();

                Item item = sourceNode.getObject().getProperty().getItem();
                TempStore store = new TempStore();
                store.seletetedNode = sourceNode;

                getAppropriateComponent(item, quickCreateInput, quickCreateOutput, store, type);
                if (store.component != null) {
                    list.add(store);
                } else {
                    MessageDialog.openInformation(editor.getEditorSite().getShell(),
                            Messages.getString("TalendEditorDropTargetListener.dngsupportdialog.title"), //$NON-NLS-1$
                            Messages.getString("TalendEditorDropTargetListener.dngsupportdialog.content")); //$NON-NLS-1$
                    return;
                }
            }

            org.eclipse.swt.graphics.Point swtLocation = new org.eclipse.swt.graphics.Point(event1.x, event1.y);
            Canvas canvas = (Canvas) editor.getViewer().getControl();

            /*
             * translate to Canvas coordinate
             */
            swtLocation = canvas.toControl(swtLocation);
            /*
             * translate to Viewport coordinate with zoom
             */
            org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point(swtLocation.x, swtLocation.y);

            /*
             * calcule the view port position. Take into acounte the scroll position
             */
            ProcessPart part = (ProcessPart) editor.getViewer().getRootEditPart().getRoot().getChildren().get(0);

            IFigure targetFigure = part.getFigure();
            translateAbsolateToRelative(targetFigure, draw2dPosition);

            // creates every node
            for (TempStore store : list) {
                RepositoryNode selectedNode = store.seletetedNode;
                IComponent element = store.component;
                if (!components.contains(element)) {
                    components.add(element);
                }
                Node node = new Node(element);
                IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
                if (preferenceStore.getBoolean(TalendDesignerPrefConstants.USE_REPOSITORY_NAME)) {
                    String LabelValue = null;

                    if (CorePlugin.getDefault().getDesignerCoreService()
                            .getPreferenceStore(TalendDesignerPrefConstants.DEFAULT_LABEL)
                            .equals(node.getPropertyValue(EParameterName.LABEL.getName()))) {// dnd a default
                        LabelValue = selectedNode.getObject().getLabel();
                    }
                    if (LabelValue != null) {
                        node.setPropertyValue(EParameterName.LABEL.getName(), LabelValue);
                    }
                }
                NodeContainer nc = ((Process) node.getProcess()).loadNodeContainer(node, false);

                // create the node on the design sheet
                execCommandStack(new CreateNodeContainerCommand((Process) editor.getProcess(), nc, draw2dPosition));
                // initialize the propertiesView
                CompoundCommand cc = new CompoundCommand();
                createRefreshingPropertiesCommand(cc, selectedNode, node);
                execCommandStack(cc);

                draw2dPosition = draw2dPosition.getCopy();
                draw2dPosition.x += TalendEditor.GRID_SIZE;
                draw2dPosition.y += TalendEditor.GRID_SIZE;

                node.checkNode();
            }
        }
    }

    private List<Command> createRefreshingPropertiesCommand(CompoundCommand cc, RepositoryNode selectedNode, Node node) {
        if (selectedNode.getObject().getProperty().getItem() instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) selectedNode.getObject().getProperty().getItem();
            // command used to set job
            String value = processItem.getProperty().getId();
            PropertyChangeCommand command4 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_PROCESS.getName(), value);
            cc.add(command4);
            PropertyChangeCommand command5 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_CONTEXT.getName(),
                    processItem.getProcess().getDefaultContext());
            cc.add(command5);
        }
        return null;
    }

    private void getAppropriateComponent(Item item, boolean quickCreateInput, boolean quickCreateOutput, TempStore store,
            ERepositoryObjectType type) {
        IComponentName rcSetting = RepositoryComponentManager.getSetting(item, type);

        // For handler, need check for esb
        if (rcSetting == null) {
            for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
                rcSetting = handler.getCorrespondingComponentName(item, type);
                if (rcSetting != null) {
                    break;
                }
            }
            if (rcSetting == null) {
                return;
            }
        }
        boolean isCurrentProject = true;
        String projectName = null;
        if (store.seletetedNode.getObject() != null) {
            projectName = store.seletetedNode.getObject().getProjectLabel();
            isCurrentProject = projectName.equals(ProjectManager.getInstance().getCurrentProject().getLabel());
        }

        List<IComponent> neededComponents = RepositoryComponentManager.filterNeededComponents(item, store.seletetedNode, type,
                isCurrentProject, projectName);

        neededComponents = (List<IComponent>) ComponentUtilities.filterVisibleComponents(neededComponents);

        // Check if the components in the list neededComponents have the same category that is required by Process.
        IComponent component = chooseOneComponent(extractComponents(neededComponents), rcSetting, quickCreateInput,
                quickCreateOutput);
        store.component = component;
        store.componentName = rcSetting;
    }

    /**
     * Extracts the components which have the same palette type as process. Added by Marvin Wang on Feb 27, 2013.
     *
     * @param neededComponents
     * @return
     */
    protected List<IComponent> extractComponents(List<IComponent> neededComponents) {
        if (neededComponents != null && neededComponents.size() > 0) {
            Iterator<IComponent> componentsIterator = neededComponents.iterator();
            while (componentsIterator.hasNext()) {
                IComponent component = componentsIterator.next();
                String compType = component.getPaletteType();
                if (compType != null && !compType.equals(editor.getProcess().getComponentsType())) {
                    componentsIterator.remove();
                }
            }
        }
        return neededComponents;
    }

    private IComponent chooseOneComponent(List<IComponent> neededComponents, IComponentName name, boolean quickCreateInput,
            boolean quickCreateOutput) {
        if (neededComponents.isEmpty()) {
            return null;
        }
        if (neededComponents.size() == 1) {
            return neededComponents.get(0);
        }

        IComponent inputComponent = getComponentByName(name.getInputComponentName(), quickCreateInput, neededComponents);
        if (inputComponent != null) {
            return inputComponent;
        }
        IComponent outputComponent = getComponentByName(name.getOutPutComponentName(), quickCreateOutput, neededComponents);
        if (outputComponent != null) {
            return outputComponent;
        }
        throw new OperationCanceledException(Messages.getString("TalendEditorDropTargetListener.cancelOperation")); //$NON-NLS-1$
    }

    private IComponent getComponentByName(String name, boolean loop, List<IComponent> neededComponents) {
        if (loop) {
            for (IComponent component : neededComponents) {
                if (component.getName().equals(name)) {
                    return component;
                }
            }
        }
        return null;
    }

    private void execCommandStack(Command command) {
        CommandStack cs = editor.getCommandStack();
        if (cs != null) {
            cs.execute(command);
        } else {
            command.execute();
        }
    }

    public void setEditor(AbstractTalendEditor editor) {
        this.editor = editor;
    }
}
