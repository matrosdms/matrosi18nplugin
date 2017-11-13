package net.schwehla.matrosdms.matrosmessagetranslatorplugin.handlers;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.widgets.Shell;

/** <b>Warning</b> : 
  As explained in <a href="http://wiki.eclipse.org/Eclipse4/RCP/FAQ#Why_aren.27t_my_handler_fields_being_re-injected.3F">this wiki page</a>, it is not recommended to define @Inject fields in a handler. <br/><br/>
  <b>Inject the values in the @Execute methods</b>
*/
@Singleton
public class MatrosMessageTranslateHandler {
	
	
	 MPart part ;
	 
	 
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s, EPartService partService, MApplication application,
	        EModelService modelService) {
		
		if (part == null) {
			
			
		    part = MBasicFactory.INSTANCE.createPart();
		    part.setLabel("HelloMatros");
		    part.setElementId("myPd");
		    part.setCloseable(true);
		    
		    part.setContributionURI("bundleclass://MatrosMessageTranslatorPlugin/net.schwehla.matrosdms.translateplugin.part.TranslatePart");
			
		    
		}
		
//		MessageDialog.openInformation(s, "E4 Information Dialog", "MatrosMessageTranslateHandler");
		
		// Already exists ?
		MUIElement element = modelService.find("myPd", application);

		// Add Part to primaryDataStack
		if (element == null) {
			
			MPartStack stack = (MPartStack) modelService.find("org.eclipse.e4.primaryDataStack", application);
			
			if (stack == null) {
				
			     List<MPartStack> stacks = modelService.findElements(application, null,
				            MPartStack.class, null);
			   
			     // Add add last element
			     stack = stacks.get(stacks.size()-1);
				
			}
		
			stack.getChildren().add(part);
		}

	     
	     partService.showPart(part, PartState.ACTIVATE);

	}
	



}
