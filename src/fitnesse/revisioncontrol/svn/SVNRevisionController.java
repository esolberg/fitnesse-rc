package fitnesse.revisioncontrol.svn;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.revisioncontrol.*;
import fitnesse.revisioncontrol.responders.RevisionControlDetailsTableBuilder;
import fitnesse.revisioncontrol.svn.client.SVNClient;
import fitnesse.revisioncontrol.svn.SVNState;

import java.io.File;
import java.util.Properties;

import org.tmatesoft.svn.core.SVNDepth;

public class SVNRevisionController implements RevisionController {
  private final SVNClient client;
  private boolean debug;

  public SVNRevisionController() {
    this(new Properties());
  }

  public SVNRevisionController(final Properties properties) {
    client = new SVNClient(properties);
    debug = Boolean.parseBoolean(properties.getProperty("RevisionControlDebug"));
  }

  public Results add(final String pagePath) {
    try {
      File file = getFileFromPath(pagePath);
      Results results = new SVNResults();
      if (!isUnderVersionControl(file)) {
        debug("add", file);
        client.doAdd(file, results);
      }
      else {
        results.setStatus(OperationStatus.NOTHING_TO_DO);
      }
      return results;
    } catch (Exception e) {
      throw revisionControlException("add", pagePath, e);
    }
  }
  
  public Results browse(final String pagePath, final String url){
	  try{
		  Results results = new SVNListResults();
		  client.doTree(pagePath,url,results);
		  
		  
	  return results;
	  }catch(Exception e){
		  throw revisionControlException("browse",url,e);
	  }
	  
  }

  public NewRevisionResults checkin(final String pagePath) {
    try {
      File file = getFileFromPath(pagePath);
      NewRevisionResults results = new SVNNewRevisionResults();
      debug("checkin", file);
      client.doCommit(file, "Auto Commit", results);
      return results;
    }
    catch (Exception e) {
      throw revisionControlException("checkin", pagePath, e);
    }
  }

  public Results checkout(final String pagePath, final String[] args) {
    try{
    	File file = getFileFromPath(pagePath);
    	NewRevisionResults results  = new SVNNewRevisionResults();
    	debug("checkout", file);
    	client.doCheckOut(file,args[1] , results);
    	client.doRevert(file, results);
    	return results;
    }
    catch(Exception e){
    	throw revisionControlException("checkout",pagePath,e);
    }
  }

  public Results delete(final String pagePath) {
    try {
      File file = getFileFromPath(pagePath);
      Results results = new SVNResults();
      debug("delete", file);
      client.doDelete(file, false, results);
      return results;
    } catch (Exception e) {
      throw revisionControlException("delete", pagePath, e);
    }
  }

  public Results revert(final String pagePath) {
    try {
      final File file = getFileFromPath(pagePath);
      Results results = new SVNResults();
      debug("revert", file);
      client.doRevert(file, results);
      //client.doUnlock(file);
      return results;
    } catch (Exception e) {
      throw revisionControlException("revert", pagePath, e);
    }
  }

  public NewRevisionResults update(final String pagePath) {
    try {
      final File file = getFileFromPath(pagePath);
      NewRevisionResults results = new SVNNewRevisionResults();
      debug("update", file);
      client.doUpdate(file, results);
      return results;
    } catch (Exception e) {
      throw revisionControlException("update", pagePath, e);
    }
  }

  public Results lock(String pagePath) {
    try {
      final File file = getFileFromPath(pagePath);
      Results results = new SVNResults();
      debug("lock", file);
      client.doLock(file, results);
      return results;
    } catch (Exception e) {
      throw revisionControlException("lock", pagePath, e);
    }
  }

  public Results unlock(String pagePath) {
    try {
      final File file = getFileFromPath(pagePath);
      Results results = new SVNResults();
      debug("unlock", file);
      client.doUnlock(file, results);
      return results;
    } catch (Exception e) {
      throw revisionControlException("unlock", pagePath, e);
    }
  }

  public StatusResults getStatus(String pagePath) {
    try {
      final File file = getFileFromPath(pagePath);
      StatusResults results = new SVNStatusResults();
      debug("status", file);
      client.doStatus(file, SVNDepth.INFINITY, results);
      return results;
    } catch (Exception e) {
      throw revisionControlException("status", pagePath, e);
    }
  }

  public OperationStatus move(final File src, final File dest) {
    try {
      client.doMove(src, dest);
      return OperationStatus.SUCCESS;
    } catch (Exception e) {
      throw new RevisionControlException("Unable to move file : " + src.getAbsolutePath() + " to location " + dest.getAbsolutePath(), e);
    }
  }

  public boolean isExternalRevisionControlEnabled() {
    return true;
  }

  private File getFileFromPath(String pagePath) {
    return new File(pagePath);
  }

  public State getState(final String pagePath) {
    File file = getFileFromPath(pagePath);
    if(!file.exists()){
    	return SVNState.UNKNOWN;
    }
    debug("get state for", file);
    return client.getState(file);
  }

  private void debug(String operation, File file) {
    if (debug)
      System.out.println("About to " + operation + " page: " + file.getAbsolutePath());
  }

  private RevisionControlException revisionControlException(String operation, String pagePath, Exception e) {
    return new RevisionControlException("Unable to " + operation + " page: " + pagePath, e);
  }

  private boolean isUnderVersionControl(final File file) {
    return getState(file.getAbsolutePath()).isUnderRevisionControl();
  }

  public String getRepositoryAddress(String pagePath){
	  try {
	      final File file = getFileFromPath(pagePath);
	      debug("getRepositoryAddress", file);
	      return client.getUrl(file);
	    } catch (Exception e) {
	      throw revisionControlException("getRepositoryLocation", pagePath, e);
	    }
  }
  
  public String getVersion(String pagePath){
	  try {
	      final File file = getFileFromPath(pagePath);
	      debug("getVersion", file);
	      return client.getVersion(file);
	    } catch (Exception e) {
	      throw revisionControlException("getVersion", pagePath, e);
	    }
  }
  
  public void cleanup(String pagePath){
	  try{
		  final File file = getFileFromPath(pagePath);
		  debug("cleanup",file);
		  client.cleanup(file);
	  }catch (Exception e){
		  throw revisionControlException("cleanup",pagePath,e);
	  }
  }
  
  class SVNResults extends Results {
    SVNResults() {
      setDetailLabels("Name", "Actions");
    }
  }

  class SVNNewRevisionResults extends NewRevisionResults {
    SVNNewRevisionResults() {
      setDetailLabels("Name", "Actions");
    }
  }

  class SVNStatusResults extends StatusResults {
    SVNStatusResults() {
      setDetailLabels("Name","Url","File", "Props", "RemoteFile", "RemoteProps",
      "LockStatus", "WCRev", "LastRev", "RemoteRev", "Author");
    }
  }
  
  class SVNListResults extends Results{
	  SVNListResults(){
		  setDetailLabels("Path","Url");
	  }
  }
  
}