package jpacker.exception;

public class JpackerException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private Exception e;
	public JpackerException(Exception e){
		super(e);
	}
	
	public Exception getDetails(){
		return e;
	}
	
}
