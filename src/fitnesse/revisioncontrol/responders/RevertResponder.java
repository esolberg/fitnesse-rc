package fitnesse.revisioncontrol.responders;

import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.http.Request;
import fitnesse.revisioncontrol.OperationStatus;
import fitnesse.revisioncontrol.Results;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import fitnesse.revisioncontrol.wiki.RevisionControlledFileSystemPage;

public class RevertResponder extends RevisionControlResponder {
  public RevertResponder() {
    super(REVERT);
  }

  @Override
  protected void performOperation(RevisionControlledFileSystemPage page, HtmlTag tag, Request request) {
    Results results = page.execute(REVERT,request);
    makeResultsHtml(results, tag);
  }

  private void makeResultsHtml(Results results, HtmlTag tag) {
    if (results.getStatus() == OperationStatus.NOTHING_TO_DO) {
      tag.add("Nothing to revert");
    } else {
      HtmlTableListingBuilder table = new RevisionControlDetailsTableBuilder(results, rootPagePath);
      tag.add(table.getTable());
    }
  }
}
