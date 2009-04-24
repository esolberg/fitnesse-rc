package fitnesse.revisioncontrol;

import java.util.LinkedList;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.wiki.WikiPageAction;

public abstract class RevisionControlOperation<R> {
 public static final RevisionControlOperation<Results> ADD =
    new RevisionControlOperation<Results>("Add", "addToRevisionControl", "a", "Add this sub-wiki to revision control") {

      @Override
      public Results execute(RevisionController revisionController, String pagePath, Request request) {
        return revisionController.add(pagePath);
      }
    };

  public static final RevisionControlOperation<State> SYNC =
    new RevisionControlOperation<State>("Synchronize", "syncRevisionControl", "") {

      @Override
      public State execute(RevisionController revisionController, String pagePath, Request request) {
        return revisionController.getState(pagePath);
      }
    };

  public static final RevisionControlOperation<NewRevisionResults> UPDATE =
    new RevisionControlOperation<NewRevisionResults>("Update", "update", "u", "Update this sub-wiki from version control.") {

      @Override
      public NewRevisionResults execute(RevisionController revisionController, String pagePath,Request request) {
        return revisionController.update(pagePath);
      }
    };

    public static final RevisionControlOperation<Results> BROWSE = 
    	new RevisionControlOperation<Results>("Browse","browse","b","Browse the contents of the repository"){
    	@Override
        public Results execute(RevisionController revisionController, String pagePath, Request request) {
			return null;
    		
    	}
    };
    
  public static final RevisionControlOperation<Results> CHECKOUT =
    new RevisionControlOperation<Results>("Checkout", "checkout", "c", "Checkout contents for this sub-wiki from version control.") {

      @Override
      public Results execute(RevisionController revisionController, String pagePath, Request request) {
    	  LinkedList<String> args = new LinkedList<String>();
    	  if(request.hasInput("userName")){
    		  args.addLast("userName");
    		  args.addLast((String)request.getInput("userName"));
    	  }
    	  if(request.hasInput("userPass")){
    		  args.addLast("userPass");
    		  args.addLast((String) request.getInput("userPass"));
    	  }
    	  if(request.hasInput("repositoryAddress")){
    		  args.addLast("repositoryAddress");
    		  args.addLast((String) request.getInput("repositoryAddress"));
    	  }
    	  if(request.hasInput("button")){
    		  if(request.getInput("button").equals("Browse")){
    			  return revisionController.browse(request.getResource(),(String)request.getInput("repositoryAddress"));
    		  }
    	  }
    	  String[] sArray = new String[6];
    	  return revisionController.checkout(pagePath,args.toArray(sArray));
      }
      
      public TagGroup makeHtml(String resource){
  	  	TagGroup group = makeContent(this.getName(), this.getDescription());
  	    group.add(makeForm(this.getQuery(), this.getName(),resource));
  	    group.add(HtmlUtil.HR);
  	    return group;
    }  
    private TagGroup makeContent(String header, String description) {
  	    TagGroup group = new TagGroup();
  	    group.add(new HtmlTag("h3", header));
  	    group.add(description);
  	    return group;
    }
    private HtmlTag makeForm(String responderName, String buttonCaption, String resource) {
  	    HtmlTag form = HtmlUtil.makeFormTag("post", resource);
  	    form.add(HtmlUtil.makeInputTag("hidden", "responder", responderName));
  	    form.add("Repository: ");
  	    HtmlTag repo = HtmlUtil.makeInputTag("text","repositoryAddress","");
  	    repo.addAttribute("size", "100");
  	    form.add(repo);
  	    form.add(new HtmlTag("br"));
  	  	/*form.add("User: ");
  	    form.add(HtmlUtil.makeInputTag("text", "userName",""));
  	  	form.add("Password: ");
  	    form.add(HtmlUtil.makeInputTag("text", "userPass",""));*/
  	    form.add(HtmlUtil.makeInputTag("submit", "button", buttonCaption));
  	    form.add(HtmlUtil.makeInputTag("submit","button", "Browse"));
  	    return form;
  	  }
      
    };

  public static final RevisionControlOperation<NewRevisionResults> CHECKIN =
    new RevisionControlOperation<NewRevisionResults>("Checkin", "checkin", "i", "Put changes to this sub-wiki into version control.") {

      @Override
      public NewRevisionResults execute(RevisionController revisionController, String pagePath, Request request) {
        return revisionController.checkin(pagePath);
      }
    };

  public static final RevisionControlOperation<Results> REVERT =
    new RevisionControlOperation<Results>("Revert", "revert", "", "Discard local changes to this sub-wiki.") {

      @Override
      public Results execute(RevisionController revisionController, String pagePath, Request request) {
        return revisionController.revert(pagePath);
      }
    };

  public static final RevisionControlOperation<StatusResults> STATUS =
    new RevisionControlOperation<StatusResults>("Status", "getStatus", "t", "Get the status of this sub-wiki from version control.") {

      @Override
      public StatusResults execute(RevisionController revisionController, String pagePath, Request request) {
        return revisionController.getStatus(pagePath);
      }
    };

    public static final RevisionControlOperation<Results> CLEANUP = 
    	new RevisionControlOperation<Results>("Cleanup","cleanup","z","Cleanup working copy."){
    		@Override
    		public Results execute(RevisionController revisionController, String pagePath, Request request){
    			revisionController.cleanup(pagePath);
    			return null;
    		}
    	};
  private final String query;
  private final String accessKey;
  private final String name;
  private String description;

  protected RevisionControlOperation(String name, String query, String accessKey) {
    this(name, query, accessKey, null);
  }

  public RevisionControlOperation(String name, String query, String accessKey, String description) {
    this.name = name;
    this.query = query;
    this.accessKey = accessKey;
    this.description = description;
  }

  public WikiPageAction makeAction(String pageName) {
    WikiPageAction action = new WikiPageAction(pageName, name);
    action.setQuery(query);
    action.setShortcutKey(accessKey);
    return action;
  }
  public TagGroup makeHtml(String resource){
	  	TagGroup group = makeContent(this.getName(), this.getDescription());
	    group.add(makeForm(this.getQuery(), this.getName(),resource));
	    group.add(HtmlUtil.HR);
	    return group;
  }
  private TagGroup makeContent(String header, String description) {
	    TagGroup group = new TagGroup();
	    group.add(new HtmlTag("h3", header));
	    group.add(description);
	    return group;
  }
  private HtmlTag makeForm(String responderName, String buttonCaption, String resource) {
	    HtmlTag form = HtmlUtil.makeFormTag("post", resource);
	    form.add(HtmlUtil.makeInputTag("hidden", "responder", responderName));
	    form.add(HtmlUtil.makeInputTag("submit", "", buttonCaption));
	    return form;
	  }
  
  public String getName() {
    return name;
  }

  public String getQuery() {
    return query;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return name;
  }

  public abstract R execute(RevisionController revisionController, String pagePath, Request request);
}
