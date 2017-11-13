
package net.schwehla.matrosdms.translateplugin.part;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import net.schwehla.matrosdms.translate.model.MatrosMappingModelSingleProperty;
import net.schwehla.matrosdms.translate.model.MatrosPropertyFileWrapper;

public class TranslatePart {
	
	@Inject MDirtyable 		dirty; 
	@Inject EPartService	partService;
	@Inject UISynchronize   sync;
	@Inject Logger logger;
	@Inject  StatusReporter statusReporter;
	
	
	@Inject
    ItemFilter filter;
	
	public static final String MATROS_TRANSLATOR = "MATROS_TRANSLATOR";
	
	
	@Inject
	@Preference(nodePath = MATROS_TRANSLATOR) 
	IEclipsePreferences preferences ;
	
	
	
	MPart part;
	
	private Text projectField;

	Map<String,MatrosPropertyFileWrapper> guiModel = new HashMap<>();

	TreeViewer treeviewer;
	private Composite compositeCenter;
	private Text testSerarch;
	
	MatrosMappingModelSingleProperty rootElement;

	@PostConstruct
	public void postConstruct(MPart part, Composite parent) {
		
		this.part = part;
		
		parent.setLayout(new GridLayout(1, false));

		Composite compositeTop = new Composite(parent, SWT.NONE);
		compositeTop.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeTop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Group grpResource = new Group(compositeTop, SWT.NONE);
		grpResource.setText("Resource");
		grpResource.setLayout(new GridLayout(2, false));

		projectField = new Text(grpResource, SWT.BORDER);
		projectField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));


		Button btnRefresh = new Button(grpResource, SWT.NONE);
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				try {
				        rootElement = new MatrosMappingModelSingleProperty(null, null,  null, null,null);
						treeviewer.setInput(rootElement);
						
						buildGUI(projectField.getText());
						
						preferences.put(MATROS_TRANSLATOR, projectField.getText());
						preferences.flush();
						
				} catch(Exception ex) {
					logger.error(ex);
					statusReporter.newStatus(IStatus.ERROR, "error", ex);
					ex.printStackTrace();



				} finally {
					treeviewer.refresh(true);
				}
				
			}
		});
		btnRefresh.setBounds(0, 0, 75, 25);
		btnRefresh.setText("Refresh");

		compositeCenter = new Composite(parent, SWT.NONE);
		compositeCenter.setLayout(new GridLayout(1, false));
		compositeCenter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		treeviewer = createTreeVewer(compositeCenter);
        rootElement = new MatrosMappingModelSingleProperty(null, null,  null, null,null);

		Composite compositeBottom = new Composite(parent, SWT.NONE);
		GridLayout gl_compositeBottom = new GridLayout(2, false);
		gl_compositeBottom.verticalSpacing = 2;
		compositeBottom.setLayout(gl_compositeBottom);
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(compositeBottom, SWT.NONE);
		
		Button btnSave = new Button(compositeBottom, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				save();
			}
		});
		btnSave.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSave.setText("Save");
		

		
		String path =  preferences.get(MATROS_TRANSLATOR,""); //$NON-NLS-1$
		projectField.setText(path);
		
		if (!path.isEmpty()) {
		
			btnRefresh.setSelection(true);
		}
			
		
        
        // SideEffect auf Dirty
        
//        dirty.setDirty(true);
      

    /*

		ISideEffect dirtySideEffect = ISideEffect.create(viewerSelectionObservable::getValue, 
				e -> { 
					
					if (e != null) {
						
						dirty.setDirty(true);
						
					}
				}
					
		);
		
		treeviewer.getTree().addDisposeListener( e->viewerSelectionObservable.dispose() );
		treeviewer.getTree().addDisposeListener( e->dirtySideEffect.dispose() );
		
		*/
		

	}
	
	
    private void buildGUI(String text) throws Exception {

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
		
   		
			String[] pathFragment = text.split("@");
			
			
			IProject j = root.getProject(pathFragment[0]);
			IFolder folder2 = j.getFolder(pathFragment[1]);

			String mappingClassName = null;
			IFile  unmapped = null;
		

			IResource[] members = folder2.members();
			for (IResource iResource : members) {
				if (iResource instanceof IFile) {
					
					IFile file = (IFile) iResource;
					
					// Refresh
					file.refreshLocal(IResource.DEPTH_ZERO, null);

					if ("properties".equalsIgnoreCase(file.getFileExtension())) {
						
						if (file.getName().indexOf("_") < 0) {
							mappingClassName = file.getName();
							mappingClassName = mappingClassName.replaceAll(".properties", "");
							unmapped = file;
						}
						
						MatrosPropertyFileWrapper c = new MatrosPropertyFileWrapper(file);
						guiModel.put(c.getKey(), c);
						
					}

				}
			}

			
			if (folder2.getProject().isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				
				IContainer folder = unmapped.getParent();
				
				IFile path = (IFile) folder.findMember( new Path(mappingClassName + ".java") );
				ICompilationUnit unit = (ICompilationUnit) JavaCore.create(path);
				
				
				for (IType type : unit.getTypes()) {
					for (IField iField : type.getFields()) {
						
						MatrosMappingModelSingleProperty element =
								new MatrosMappingModelSingleProperty(rootElement, iField.getElementName(),  iField.getElementName(), "text" , "", false,true);

				        new MatrosMappingModelSingleProperty(element, iField.getElementName(), "class", iField.getElementName() , "constant", false,false);
				        
				     
				    	
				        guiModel.keySet().stream().sorted().forEach( e -> {
				        	
				        	MatrosMappingModelSingleProperty propModel =  new MatrosMappingModelSingleProperty(element,  iField.getElementName(), e, "" , "", true,false);
				        	guiModel.get(e).getSingleGuiProperty().put(iField.getElementName(), propModel);
				        	
				        } );

					}
				}
			
			}


		
		// Mapping values to GUI
		guiModel.values().stream().forEach( x -> {
			try {
				x.initModel();
			} catch (Exception e1) {
				logger.error(e1);
				statusReporter.newStatus(IStatus.ERROR, "error", e1);
				throw new RuntimeException(e1);
			}
		});
		
	       
        treeviewer.setInput(rootElement);
        treeviewer.expandAll();
        
        
	}




	@CanExecute
    public boolean canExecute() {
    	return dirty.isDirty();
    }

	
	@Execute
	public void execute() {
	   partService.savePart(part, false);
	}
	
	@Persist
	protected void save() {
		
		for (MatrosPropertyFileWrapper w : guiModel.values()) {
			try {
				w.save();
				dirty.setDirty(false);
			} catch (Exception e) {
				logger.error(e);
				statusReporter.newStatus(IStatus.ERROR, "error", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private TreeViewer createTreeVewer(Composite parent) {
		
		Composite composite = new Composite(compositeCenter, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		testSerarch = new Text(composite, SWT.BORDER);
		testSerarch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);
		

		
		Button btnTodo = new Button(composite, SWT.CHECK);
		btnTodo.setText("todo");

		final TreeViewer treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
	      
	      

		treeViewer.setContentProvider(new TreeContentProvider());

		// configure the inner tree

		final Tree featureTree = treeViewer.getTree();
		featureTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		featureTree.setHeaderVisible(true);

		featureTree.setLinesVisible(true);

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(

				treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));

		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(

				treeViewer) {

			protected boolean isEditorActivationEvent(

					ColumnViewerEditorActivationEvent event) {

				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;

			}

		};

		TreeViewerEditor.create(treeViewer, focusCellManager, actSupport,

						  ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(treeViewer

				.getTree());

		// create the feature column, which contains the feature tree

		TreeViewerColumn featureColumn = new TreeViewerColumn(treeViewer, SWT.NONE);

		featureColumn.getColumn().setText("Property");
		featureColumn.setLabelProvider(new TreeLabelProviderPropertyName());

		// create the count column, which contains the count values of features

		TreeViewerColumn countColumn = new TreeViewerColumn(treeViewer, SWT.NONE);

		countColumn.getColumn().setText("Source");
		countColumn.setLabelProvider(new TreeLabelProvider2());
		countColumn.setEditingSupport(new EditingSupport(treeViewer) {

			protected boolean canEdit(Object element) {
				return ((MatrosMappingModelSingleProperty) element).isEditable();
			}

			protected CellEditor getCellEditor(Object element) {

				return textCellEditor;

			}

			protected Object getValue(Object element) {

				if (element instanceof MatrosMappingModelSingleProperty) {

					return ((MatrosMappingModelSingleProperty) element).getUserTranslatedText();

				}

				return element.toString();

			}

			protected void setValue(Object element, Object value) {

				if (element instanceof MatrosMappingModelSingleProperty) {

					((MatrosMappingModelSingleProperty) element).setUserTranslatedText((String.valueOf(value)));

				}

				treeViewer.update(element, null);

			}

		});

		treeViewer.getTree().addKeyListener(new KeyAdapter() {

			@Override

			public void keyPressed(KeyEvent e) {

				if (e.keyCode == SWT.F2) {

					// TODO find right column...

					treeViewer.editElement(((IStructuredSelection) treeViewer

							.getSelection()).getFirstElement(), 0);

				}

			}

		});

		// create the feature column, which contains the feature tree

		TreeViewerColumn featureColumn2 = new TreeViewerColumn(treeViewer, SWT.NONE);
		featureColumn2.getColumn().setText("Status");
		featureColumn2.setLabelProvider(new TreeLabelProviderStatus());
		
		testSerarch.addModifyListener(new ModifyListener() {
      	  
      	  
        	@Override
        	public void modifyText(ModifyEvent e) {
        		
        		filter.setSearchString(testSerarch.getText(),btnTodo.getSelection());
        		treeViewer.refresh(false);
        			
        	}
        	

          });
          
		
		
		btnTodo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
        		filter.setSearchString(testSerarch.getText(),btnTodo.getSelection());
        		treeViewer.refresh(false);
			}
		});
		
		
	  
		treeViewer.addFilter(filter);
		treeViewer.setExpandPreCheckFilters(true);
		
		for (TreeColumn c: treeViewer.getTree().getColumns() ) {
			c.pack();
		}
		
		
		
		/*
	     Listener listener = new Listener() {

	         @Override
	         public void handleEvent( Event e ) {
	            final TreeItem treeItem = (TreeItem)e.item;
	            sync.asyncExec(new Runnable() {

	               @Override
	               public void run() {
	                  for ( TreeColumn tc : treeItem.getParent().getColumns() )
	                     tc.pack();
	               }
	            });
	         }
	      };

	      treeViewer.getTree().addListener(SWT.Collapse, listener);
	      treeViewer.getTree().addListener(SWT.Expand, listener);

		*/

		return treeViewer;

	}

	public static class TreeContentProvider implements ITreeContentProvider {

		@Override

		public Object[] getChildren(Object parentElement) {

			return ((MatrosMappingModelSingleProperty) parentElement).children.toArray();

		}

		@Override

		public Object getParent(Object element) {

			return ((MatrosMappingModelSingleProperty) element).parent;

		}

		@Override

		public boolean hasChildren(Object element) {

			return !((MatrosMappingModelSingleProperty) element).children.isEmpty();

		}

		@Override

		public Object[] getElements(Object inputElement) {

			return getChildren(inputElement);

		}

		@Override

		public void dispose() {
		}

		@Override

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	public static class TreeLabelProviderPropertyName extends ColumnLabelProvider {

		@Override

		public String getText(Object element) {

			return ((MatrosMappingModelSingleProperty) element).getText1();

		}
		
		
		public Color getBackground(final Object element) {

			if (element instanceof MatrosMappingModelSingleProperty) {
				
				MatrosMappingModelSingleProperty casted = (MatrosMappingModelSingleProperty) element;

				if ((casted.isPropertyRootNode() && casted.isKidsNotFilled() )) {
					return Display.getDefault().getSystemColor(SWT.COLOR_RED);
				}

			}

			return super.getBackground(element);

		}
		
		

	}

	public static class TreeLabelProvider2 extends ColumnLabelProvider {

		@Override

		public String getText(Object element) {

			return ((MatrosMappingModelSingleProperty) element).getUserTranslatedText();

		}

		@Override

		
		public Color getBackground(final Object element) {

			if (element instanceof MatrosMappingModelSingleProperty) {
				
				MatrosMappingModelSingleProperty casted = (MatrosMappingModelSingleProperty) element;

				if ((casted.isEditable() && casted.isNotFilled())   || casted.isKidsNotFilled() ) {
					return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
				}

			}

			return super.getBackground(element);

		}
		
		
	}

	public static class TreeLabelProviderStatus extends ColumnLabelProvider {

		@Override

        public String getText(Object element) {

            return ((MatrosMappingModelSingleProperty)element).getStatusText();

        }

	}

	
}