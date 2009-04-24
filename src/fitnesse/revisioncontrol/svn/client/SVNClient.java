package fitnesse.revisioncontrol.svn.client;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.revisioncontrol.*;
import fitnesse.revisioncontrol.svn.SVNState;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import java.io.File;
import java.util.*;

public class SVNClient {
  private final SVNClientManager clientManager;
  private final Map<SVNStatusType, State> states = new HashMap<SVNStatusType, State>();
  private final Map<SVNStatusType, String> errorMsgs = new HashMap<SVNStatusType, String>();

  public SVNClient(Properties properties) {
    initializeRepositories();
    clientManager = initializeClientManager(properties);
    initializeSVNStatusTypeToStateMap();
    initializeUnhandledSVNStatusTypeToErrorMsgsMap();
  }

  private void initializeRepositories() {
    // for DAV (over http and https)
    DAVRepositoryFactory.setup();
    // for svn (over svn and svn+ssh)
    SVNRepositoryFactoryImpl.setup();
    // for local (file)
    FSRepositoryFactory.setup();
  }

  private SVNClientManager initializeClientManager(Properties properties) {
    String userName = properties.getProperty("SvnUser");
    String password = properties.getProperty("SvnPassword");
    if (userName == null || password == null) {
      return SVNClientManager.newInstance();
    } else {
      return SVNClientManager.newInstance(null, userName, password);
    }
  }
  
  
  class ListResults{
	  private List<SVNDirEntry> data = new ArrayList<SVNDirEntry>();
	  public ListResults(){}
	  public void add(SVNDirEntry dir){data.add(dir);}
  }
  
  class DirHandler implements ISVNDirEntryHandler{
	  private Results results;
	  private String basePath;
	  public DirHandler(String basePath, Results results){this.results = results; this.basePath=basePath;}

	public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {
		if(dirEntry.getKind().equals(SVNNodeKind.DIR)){
			String relativePath = dirEntry.getRelativePath();
			
			HtmlTag hyperLink = (new HtmlTag("a",relativePath.equals("")?".":relativePath));
			hyperLink.addAttribute("href",basePath+"?responder=checkout&amp;button=Browse&amp;repositoryAddress="+dirEntry.getURL().toString());
			HtmlTag checkoutButton = new HtmlTag("a","CheckOut");
			checkoutButton.addAttribute("href", basePath+"?responder=checkout&amp;repositoryAddress="+dirEntry.getURL().toString());
			results.addDetail(new RevisionControlDetail(dirEntry.getRelativePath(),hyperLink,checkoutButton));
		}
	}
  }
  
  public void doTree(String path,String url,Results results) throws SVNException{
	  SVNLogClient  client = clientManager.getLogClient();
	  SVNURL svnUrl = SVNURL.parseURIEncoded(url);
	  client.doList(svnUrl,SVNRevision.HEAD,SVNRevision.HEAD,false,false,new DirHandler(path,results));
	  
  }
  
  public void doUpdate(File wcPath, NewRevisionResults results) throws SVNException {
    SVNUpdateClient client = clientManager.getUpdateClient();
    client.setIgnoreExternals(true);
    setEventHandler(results, client);

    long revision = client.doUpdate(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY, false, true);
    results.setNewRevision(revision);

    clearEventHandler(client);
  }

  public void doCheckOut(File wcPath,String url,NewRevisionResults results) throws SVNException{
	  SVNUpdateClient client = clientManager.getUpdateClient();
	  client.setIgnoreExternals(true);
	  setEventHandler(results,client);
	  SVNURL svnUrl = SVNURL.parseURIEncoded(url);
	  long revision = client.doCheckout(svnUrl,wcPath,SVNRevision.HEAD,SVNRevision.HEAD,SVNDepth.INFINITY,true);
	  results.setNewRevision(revision);
	  
	  clearEventHandler(client);
  }
  
  public void doCommit(File wcPath, String commitMessage, NewRevisionResults results) throws SVNException {
    SVNCommitClient client = clientManager.getCommitClient();
    setEventHandler(results, client);

    SVNCommitInfo svnInfo = client.doCommit(new File[]{wcPath}, false, commitMessage,
      null, null, false, false, SVNDepth.INFINITY);

    long newRevision = svnInfo.getNewRevision();
    if (newRevision == -1)
      results.setStatus(OperationStatus.NOTHING_TO_DO);
    else
      results.setNewRevision(newRevision);

    clearEventHandler(client);
  }

  public void doAdd(File wcPath, Results results) throws SVNException {
    SVNWCClient client = clientManager.getWCClient();
    setEventHandler(results, client);

    client.doAdd(wcPath, false, false, false, SVNDepth.INFINITY, false, false);

    clearEventHandler(client);
  }

  public void doDelete(File wcPath, boolean force, Results results) throws SVNException {
    SVNWCClient client = clientManager.getWCClient();
    setEventHandler(results, client);

    client.doDelete(wcPath, force,false, false);

    clearEventHandler(client);
  }

  public List doLog(File wcPath) throws SVNException {
    LogEntryHandler handler = new LogEntryHandler();

    clientManager.getLogClient().doLog(new File[]{wcPath},
      SVNRevision.BASE, SVNRevision.HEAD,
      false, false, 100, handler);

    return handler.logEntries;
  }

  public void doRevert(File wcPath, Results results) throws SVNException {
	
    SVNWCClient client = clientManager.getWCClient();
    setEventHandler(results, client);

    client.doRevert(new File[]{wcPath}, SVNDepth.INFINITY, null);

    clearEventHandler(client);

    if (results.getDetails().size() == 0)
      results.setStatus(OperationStatus.NOTHING_TO_DO);
  }

  public void doStatus(File wcPath, SVNDepth depth, StatusResults results) throws SVNException {
    SVNStatusClient client = clientManager.getStatusClient();
    SVNStatusResultsHandler handler = new SVNStatusResultsHandler(results);
    client.doStatus(wcPath, SVNRevision.HEAD, depth,
      true, true, false, false, handler, null);
  }
  
  public String getUrl(File wcPath) throws SVNException{
	SVNStatusClient client = clientManager.getStatusClient();
	SVNStatus status = client.doStatus(wcPath,true);
	return status.getURL().toDecodedString();
	
  } 
  public String getVersion(File wcPath) throws SVNException{
		SVNStatusClient client = clientManager.getStatusClient();
		SVNStatus status = client.doStatus(wcPath,true);
		return ""+status.getRevision().getNumber();
		
	  }
  public void cleanup(File wcPath) throws SVNException{
	  SVNWCClient client = clientManager.getWCClient();
	  client.doCleanup(wcPath);
  }
  private SVNStatus doLocalStatus(File wcPath) throws SVNException {
    return clientManager.getStatusClient().doStatus(wcPath, false);
  }

  public void doLock(File wcPath, Results results) throws SVNException {
    SVNWCClient client = clientManager.getWCClient();
    setEventHandler(results, client);

    client.doLock(getPathsFromRoot(wcPath), false, null);

    clearEventHandler(client);
  }

  public void doUnlock(File wcPath) throws SVNException {
    SVNWCClient client = clientManager.getWCClient();
    client.doUnlock(getPathsFromRoot(wcPath), true);
  }

  public void doUnlock(File wcPath, Results results) throws SVNException {
    SVNWCClient client = clientManager.getWCClient();
    setEventHandler(results, client);

    client.doUnlock(getPathsFromRoot(wcPath), true);

    clearEventHandler(client);
  }

  public void doMove(File src, File dest) throws SVNException {
    clientManager.getMoveClient().doMove(src, dest);
  }

  private File[] getPathsFromRoot(File file) {
    ArrayList<File> list = new ArrayList<File>();

    if (file.isDirectory())
      recurseDirectory(file, list);
    else
      list.add(file);

    return list.toArray(new File[list.size()]);
  }

  private void recurseDirectory(File root, List<File> paths) {
    File[] files = root.listFiles();
    for (File file : files) {
      if (file.getName().equals(".svn")) {
        continue;
      }

      if (file.isDirectory())
        recurseDirectory(file, paths);
      else
        paths.add(file);
    }
  }

  private static class LogEntryHandler implements ISVNLogEntryHandler {
    public List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();

    public void handleLogEntry(SVNLogEntry logEntry) {
      logEntries.add(logEntry);
    }
  }

  public State getState(File pagePath) {
    SVNStatusType localStatus;
    SVNStatusType remoteStatus;
    try {
      SVNStatus status = doLocalStatus(pagePath);
      localStatus = status.getContentsStatus();
      remoteStatus = status.getRemoteContentsStatus();
    } catch (SVNException e) {
      return SVNState.UNKNOWN;
    }

    State localState = this.states.get(localStatus);
    State remoteState = this.states.get(remoteStatus);
    if(remoteState == SVNState.MODIFIED){
    	localState = SVNState.OUTDATED;
    }
    
    if (localState != null) {
      return localState;
    }
    throwExceptionForUnhandledStatuses(localStatus, pagePath);
    throw new RevisionControlException(pagePath + " is in an unknown state. Please update the file and try again.");
  }

  private void throwExceptionForUnhandledStatuses(final SVNStatusType status, final File fileName) {
    String errorMsg = this.errorMsgs.get(status);
    if (errorMsg != null) {
      throw new RevisionControlException(fileName.getAbsolutePath() + errorMsg);
    }
  }

  private void setEventHandler(Results results, SVNBasicClient client) {
    SVNResultsHandler handler = new SVNResultsHandler(results);
    client.setEventHandler(handler);
  }

  private void clearEventHandler(SVNBasicClient client) {
    client.setEventHandler(null);
  }

  private void initializeSVNStatusTypeToStateMap() {
    this.states.put(null, SVNState.UNKNOWN);
    this.states.put(SVNStatusType.STATUS_UNVERSIONED, SVNState.UNKNOWN);
    this.states.put(SVNStatusType.STATUS_NONE, SVNState.UNKNOWN);
    this.states.put(SVNStatusType.STATUS_ADDED, SVNState.ADDED);
    this.states.put(SVNStatusType.STATUS_DELETED, SVNState.DELETED);
    this.states.put(SVNStatusType.STATUS_NORMAL, SVNState.VERSIONED);
    this.states.put(SVNStatusType.STATUS_MODIFIED, SVNState.MODIFIED);
    this.states.put(SVNStatusType.STATUS_REPLACED, SVNState.VERSIONED);
    this.states.put(SVNStatusType.MERGED, SVNState.VERSIONED);
    this.states.put(SVNStatusType.STATUS_INCOMPLETE, SVNState.INCOMPLETE);
  }

  private void initializeUnhandledSVNStatusTypeToErrorMsgsMap() {
    this.errorMsgs.put(SVNStatusType.STATUS_CONFLICTED, " has conflicts");
    this.errorMsgs.put(SVNStatusType.STATUS_MISSING, " is missing from the working copy");
    this.errorMsgs.put(SVNStatusType.STATUS_IGNORED, " is marked to be Ignored by SVN. Cannot perform SVN operations on ignored files");
    this.errorMsgs.put(SVNStatusType.STATUS_EXTERNAL, " is an SVN External File. Cannot perform local SVN operatiosn on external files");
    this.errorMsgs.put(SVNStatusType.STATUS_INCOMPLETE, " is marked as incomplete by SVN. Please update the file and try again");
    this.errorMsgs.put(SVNStatusType.STATUS_OBSTRUCTED, " is marked as obstructed by SVN. Please clean the working copy and try again");
  }
}