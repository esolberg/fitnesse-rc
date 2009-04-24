package fitnesse.revisioncontrol.responders;

import fitnesse.html.HtmlTag;
import fitnesse.http.Request;
import static fitnesse.revisioncontrol.RevisionControlOperation.SYNC;
import fitnesse.revisioncontrol.wiki.RevisionControlledFileSystemPage;

public class SyncResponder extends RevisionControlResponder {
  public SyncResponder() {
    super(SYNC);
  }

  @Override
  protected void performOperation(RevisionControlledFileSystemPage page, HtmlTag tag, Request request) {
    page.execute(SYNC,request);
  }
}
