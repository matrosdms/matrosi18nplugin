package net.schwehla.matrosdms.translateplugin.part;



import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.schwehla.matrosdms.translate.model.MatrosMappingModelSingleProperty;



@Creatable
public class ItemFilter extends ViewerFilter {
	
	@Inject Logger logger;

    protected String searchString = ""; //$NON-NLS-1$
    protected boolean unmapped = false;

    public String getSearchString() {
         return searchString;
   }

   public void setSearchString(String searchString, boolean unmapped) {
         this.searchString = searchString;
         this.unmapped = unmapped;
   }


    @Override
    public boolean select(Viewer viewer, 
        Object parentElement, 
        Object element) {
      
    	try {
    		
	        
	      MatrosMappingModelSingleProperty p = (MatrosMappingModelSingleProperty) element;
	      
    		if (unmapped && p.isPropertyRootNode()) {
    			
    			if (!p.isKidsNotFilled()) {
    				return false;
    			}
    			
    		}
    		
     	      if (searchString == null || searchString.length() == 0) {
    	          return true;
    	        }

    	        
    	        if (p.getPropertyName().toLowerCase().contains(this.searchString.toLowerCase())) {
    	           return true;
    	        }
    	        
    	        return false;

    	} catch(RuntimeException e) {
    		logger.warn("Nullpointer :" + element , e); //$NON-NLS-1$
    	}
    	
    	return false;

      
    }
  } 
