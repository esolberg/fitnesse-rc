package fitnesse.revisioncontrol.responders;

import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.http.Request;
import fitnesse.revisioncontrol.NewRevisionResults;
import fitnesse.revisioncontrol.OperationStatus;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import fitnesse.revisioncontrol.wiki.RevisionControlledFileSystemPage;

public class CheckinResponder extends RevisionControlResponder {
  public CheckinResponder() {
    super(CHECKIN);
  }

  @Override
  protected void performOperation(RevisionControlledFileSystemPage page, HtmlTag tag, Request request) {
    NewRevisionResults results = page.execute(CHECKIN,request);
    makeResultsHtml(results, tag);
  }

  private void makeResultsHtml(NewRevisionResults results, HtmlTag tag) {
    if (results.getDetails().size() == 0) {
      tag.add("At revision " + results.getNewRevision());
    }
    else {
      HtmlTableListingBuilder table = new RevisionControlDetailsTableBuilder(results, rootPagePath);
      tag.add(table.getTable());

      tag.add("Checked in as revision " + results.getNewRevision());
    }
  }

}
