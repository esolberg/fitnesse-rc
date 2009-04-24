package fitnesse.revisioncontrol;

import fitnesse.http.Request;


public interface RevisionControllable {
  <R> R execute(RevisionControlOperation<R> operation, Request request);

  boolean isExternallyRevisionControlled();

  State getState();
}