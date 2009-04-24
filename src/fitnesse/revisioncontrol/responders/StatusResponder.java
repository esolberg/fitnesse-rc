package fitnesse.revisioncontrol.responders;

import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.http.Request;
import static fitnesse.revisioncontrol.RevisionControlOperation.STATUS;
import fitnesse.revisioncontrol.StatusResults;
import fitnesse.revisioncontrol.wiki.RevisionControlledFileSystemPage;

public class StatusResponder extends RevisionControlResponder {
  public StatusResponder() {
    super(STATUS);
  }

  @Override
  protected void performOperation(RevisionControlledFileSystemPage page, HtmlTag tag, Request request) {
    StatusResults results = page.execute(STATUS,request);
    makeResultsHtml(results, tag);
  }

  private void makeResultsHtml(StatusResults results, HtmlTag tag) {
    HtmlTableListingBuilder table = new RevisionControlDetailsTableBuilder(results, rootPagePath);
    tag.add(table.getTable());
  }
}