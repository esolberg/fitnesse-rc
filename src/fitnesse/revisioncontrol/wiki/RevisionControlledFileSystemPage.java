package fitnesse.revisioncontrol.wiki;

import fitnesse.ComponentFactory;
import fitnesse.html.HtmlElement;
import fitnesse.html.RawHtml;
import fitnesse.http.Request;
import fitnesse.revisioncontrol.*;
import fitnesse.wiki.*;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class RevisionControlledFileSystemPage extends FileSystemPage implements RevisionControllable {
  private static final String REVISION_CONTROLLER = "RevisionController";

  private RevisionController revisioner;

  public RevisionControlledFileSystemPage(final String path, final String name, 
                                          final ComponentFactory componentFactory) throws Exception {
    super(path, name, null, new NullVersionsController());
    this.revisioner = createRevisionController(componentFactory);
  }

  protected RevisionControlledFileSystemPage(final String path, final String name, final WikiPage parent,
                                          final RevisionController revisionController) throws Exception {
    super(path, name, parent, new NullVersionsController());
    this.revisioner = revisionController;
  }

  public RevisionControlledFileSystemPage(final String path, final String name,
                                          final RevisionController revisionController) throws Exception {
    this(path, name, null, revisionController);
  }

  private static RevisionController createRevisionController(ComponentFactory factory) throws Exception {
    RevisionController revisionController = (RevisionController) factory.createComponent(REVISION_CONTROLLER);
    if (revisionController == null) {
      throw new IllegalStateException("A RevisionController must be configured in the FitNesse properties");
    }
    return revisionController;
  }

  @Override
  public void doCommit(PageData data) throws Exception {

	    super.doCommit(data);
  
	    /*if (getState().isUnderRevisionControl()) {
	    	revisioner.lock(getAbsoluteFileSystemPath());
	    }*/ // I don't think we want locking too problematic
	    
	    if(!getState().isUnderRevisionControl()){
	    	if(this.getParent() instanceof RevisionControlledFileSystemPage){
	    		RevisionControlledFileSystemPage parent = (RevisionControlledFileSystemPage)this.getParent();
	    		if(parent.getState().isUnderRevisionControl()){
	    			revisioner.add(this.getAbsoluteFileSystemPath());
	    		}
	    	}
	    }


  }

  @Override
  public PageData makePageData() throws Exception {
    PageData data = super.makePageData();
    clearCachedState();
    if (isDeleted(data))
      data.setContent("!deletedpage");   
    
    return new RevisionControlledPageData(this,data);
  }
  @Override
  public PageData getData() throws Exception {
	  PageData data = super.getData();
	
	   	return new RevisionControlledPageData(this,data.getContent());
	  
	    //return data;
	  }
  
  private boolean isDeleted(PageData data) throws Exception {
    //return data.isEmpty() && revisioner != null && getState().isDeleted();
	return revisioner != null && getState().isDeleted();
  }

  @Override
  protected WikiPage createChildPage(String name) throws Exception {
    return new RevisionControlledFileSystemPage(getFileSystemPath(), name, this, revisioner);
  }

  @Override
  public void removeChildPage(String name) throws Exception {
	  clearCachedState();
	  if(this.getState().isUnderRevisionControl()){
		  RevisionControlledFileSystemPage pageToBeDeleted = (RevisionControlledFileSystemPage) getChildPage(name);

		    if (pageToBeDeleted.getState().isUnderRevisionControl() && pageToBeDeleted.getState().isCheckedIn()) {
		      revisioner.delete(pageToBeDeleted.getAbsoluteFileSystemPath());
		      return;
		    }	  
	  }
	  super.removeChildPage(name);
  }

  @Override
  public List<WikiPageAction> getActions() throws Exception {
    WikiPagePath localPagePath = getPageCrawler().getFullPath(this);
    String localPageName = PathParser.render(localPagePath);

    List<WikiPageAction> actions = super.getActions();
//    addRevisionControlActions(localPageName, actions);
    replaceVersionsActionWithRevisionsAction(localPageName, actions);
    return actions;
  }

  private void addRevisionControlActions(String localPageName, List<WikiPageAction> actions) throws Exception {
    actions.addAll(RevisionControlActionsBuilder.getRevisionControlActions(localPageName, getData()));
  }

  private void replaceVersionsActionWithRevisionsAction(String localPageName, List<WikiPageAction> actions) {
    WikiPageAction revisionControlAction = new WikiPageAction(localPageName, "Revisions");
    int position = actions.indexOf(new WikiPageAction(localPageName, "Versions"));
    if (position > 0) {
      actions.remove(position);
      actions.add(position, revisionControlAction);
    }
    else {
      actions.add(revisionControlAction);
    }
  }

  /**
   * @see fitnesse.revisioncontrol.RevisionControlOperation
   */

  public <R> R execute(final RevisionControlOperation<R> operation, Request request) {
    return operation.execute(revisioner, getAbsoluteFileSystemPath(),request);
  }

  public boolean isExternallyRevisionControlled() {
    return revisioner.isExternalRevisionControlEnabled();
  }
  
  private State cachedState;

  public State getState() {
	  if(cachedState != null){
		  return cachedState;
	  }
	  if(revisioner == null){
		  return NullState.UNKNOWN;
	  }
	  cachedState = revisioner.getState(getAbsoluteFileSystemPath()+File.separator+"content.txt");
    return cachedState;
  }
  private void clearCachedState(){
	  cachedState = null;
  }
  
  public String getRepositoryLocation(){
	  if(revisioner == null || !this.getState().isUnderRevisionControl())
		  return "";
	  return revisioner.getRepositoryAddress(this.getAbsoluteFileSystemPath());
  }
  
  public String getVersion(){
	  if(revisioner ==null){
		  return "Unversioned";
	  }
	  return revisioner.getVersion(this.getAbsoluteFileSystemPath()+File.separator+"content.txt");	  
  }
}
