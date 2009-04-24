package fitnesse.revisioncontrol;

import fitnesse.responders.ResponderFactory;
import static fitnesse.revisioncontrol.RevisionControlOperation.*;
import fitnesse.revisioncontrol.responders.*;
import fitnesse.revisioncontrol.widgets.DeletedPageWidget;
import fitnesse.revisioncontrol.widgets.RevisionStatusWidget;
import fitnesse.revisioncontrol.wiki.RevisionControlledFileSystemPage;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.WikiPageFactory;
//import fitnesse.revisioncontrol.svn.widgets.*;

public class RevisionControlPlugin {
  public static void registerWikiPage(WikiPageFactory wikiPageFactory) {
    wikiPageFactory.setWikiPageClass(RevisionControlledFileSystemPage.class);
  }

  public static void registerResponders(ResponderFactory responderFactory) {
    responderFactory.addResponder("revisions", RevisionsResponder.class);
    responderFactory.addResponder(ADD.getQuery(), AddResponder.class);
    responderFactory.addResponder(CHECKOUT.getQuery(), CheckoutResponder.class);
    responderFactory.addResponder(CHECKIN.getQuery(), CheckinResponder.class);
    responderFactory.addResponder(REVERT.getQuery(), RevertResponder.class);
    responderFactory.addResponder(UPDATE.getQuery(), UpdateResponder.class);
    responderFactory.addResponder(STATUS.getQuery(), StatusResponder.class);
    responderFactory.addResponder(CLEANUP.getQuery(),CleanupResponder.class);
  }

  public static void registerWikiWidgets(WidgetBuilder widgetBuilder) {
    widgetBuilder.addWidgetClass(DeletedPageWidget.class);
    widgetBuilder.addWidgetClass(RevisionStatusWidget.class);
    //widgetBuilder.addWidgetClass(CheckOutWidget.class);
  }
}
