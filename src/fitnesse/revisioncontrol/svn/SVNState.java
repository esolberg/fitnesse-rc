package fitnesse.revisioncontrol.svn;

import fitnesse.revisioncontrol.RevisionControlOperation;
import static fitnesse.revisioncontrol.RevisionControlOperation.*;
import fitnesse.revisioncontrol.State;

public abstract class SVNState implements State {
  private String state;

  public static final SVNState VERSIONED = new Versioned("Versioned");
  public static final SVNState UNKNOWN = new Unknown("Unknown");
  public static final SVNState DELETED = new Deleted("Deleted");
  public static final SVNState ADDED = new Added("Added");
  public static final SVNState UNVERSIONED = new Unversioned("Unversioned");
  public static final SVNState INCOMPLETE = new Incomplete("Incomplete");
  public static final SVNState MODIFIED = new Modified("Modified");
  public static final SVNState OUTDATED = new OutDated("OutDated");
  

  protected SVNState(String state) {
    this.state = state;
  }

  public boolean isUnderRevisionControl() {
    return true;
  }

  public boolean isCheckedOut() {
    return true;
  }

  public boolean isDeleted() {
    return false;
  }

  @Override
  public String toString() {
    return state;
  }

  protected boolean contains(String msg, String searchString) {
    return msg.indexOf(searchString) != -1;
  }
}

class Versioned extends SVNState {
  protected Versioned(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, UPDATE, REVERT, STATUS,CLEANUP};
  }

  public boolean isCheckedIn() {
    return true;
  }
}

class Incomplete extends SVNState{
	protected Incomplete(String state){
		super(state);
	}

	public boolean isCheckedIn() {
		return true;
	}

	public RevisionControlOperation[] operations() {
		return new RevisionControlOperation[]{UPDATE,CLEANUP};
	}
}

class Unversioned extends SVNState{
	protected Unversioned(String state){
		super(state);
	}
	public RevisionControlOperation[] operations(){
		return new RevisionControlOperation[]{ADD,CHECKOUT};
	}
	public boolean isCheckedIn(){
		return false;
	}
	public boolean isUnderRevisionControl(){
		return false;
	}
}

class Unknown extends SVNState {
  protected Unknown(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKOUT, ADD};
	//return new RevisionControlOperation[]{ADD};
  }

  @Override
  public boolean isUnderRevisionControl() {
    return false;
  }

  public boolean isCheckedIn() {
    return false;
  }

  @Override
  public boolean isCheckedOut() {
    return false;
  }
}

class Deleted extends SVNState {
  protected Deleted(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, REVERT, STATUS,CLEANUP,UPDATE};
  }

  public boolean isCheckedIn() {
    return true;
  }

  @Override
  public boolean isDeleted() {
    return true;
  }
}

class Added extends SVNState {
  protected Added(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, REVERT, STATUS};
  }

  public boolean isCheckedIn() {
    return false;
  }
}

class Modified extends SVNState{

	protected Modified(String state) {
		super(state);
		// TODO Auto-generated constructor stub
	}

	public boolean isCheckedIn() {
		return true;
	}

	public RevisionControlOperation[] operations() {
		return new RevisionControlOperation[]{CHECKIN,REVERT,STATUS,CLEANUP};
	}
}
class OutDated extends SVNState{
	protected OutDated(String state){
		super(state);
	}
	public boolean isCheckedIn(){
		return true;
	}
	public RevisionControlOperation[] operations(){
		return new RevisionControlOperation[]{UPDATE,REVERT,STATUS,CLEANUP};
	}
}
