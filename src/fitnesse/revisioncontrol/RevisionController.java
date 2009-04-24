package fitnesse.revisioncontrol;

import java.io.File;
import java.util.Collection;

import fitnesse.html.HtmlElement;
import fitnesse.revisioncontrol.RevisionControlOperation;

public interface RevisionController {
  public Results add(String pagePath);

  public NewRevisionResults checkin(String pagePath);

  public Results checkout(String pagePath, String[] args);

  public Results browse(String pagePath,String url);
  
  public Results delete(String pagePath);

  public Results revert(String pagePath);

  public NewRevisionResults update(String pagePath);

  public Results lock(String pagePath);
  
  public Results unlock(String pagePath);

  public StatusResults getStatus(String pagePath);
  
  public String getRepositoryAddress(String pagePath);
  
  public String getVersion(String pagePath);
  
  public void cleanup(String pagePath);

  public OperationStatus move(File src, File dest);

  public State getState(String pagePath);

  public boolean isExternalRevisionControlEnabled();
}
