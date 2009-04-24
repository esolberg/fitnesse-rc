package fitnesse.revisioncontrol.responders;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKOUT;
import fitnesse.revisioncontrol.Results;
import fitnesse.revisioncontrol.RevisionControlDetail;
import fitnesse.revisioncontrol.wiki.RevisionControlledFileSystemPage;

public class CheckoutResponder extends RevisionControlResponder {
  public CheckoutResponder() {
    super(CHECKOUT);
  }

  @Override
  protected String createPageLink(String resource) throws Exception {
    return "Click " + HtmlUtil.makeLink(resource + "?edit", "here").html() + " to edit the page.";
  }

  @Override
  protected void performOperation(RevisionControlledFileSystemPage page, HtmlTag tag, Request request) {
    Results results = page.execute(CHECKOUT,request);
    if(request.hasInput("button")){
    	if(request.getInput("button").equals("Browse")){
    		String address = (String)request.getInput("repositoryAddress");
    		tag.add(new HtmlTag("h2",address));
    		HtmlTableListingBuilder table = new HtmlTableListingBuilder();
    		for( RevisionControlDetail detail : results.getDetails()){
    			table.addRow((HtmlElement[])detail.getActionTags().toArray());
    		}
    	    tag.add(table.getTable());		
    	}
    }
  }
}
