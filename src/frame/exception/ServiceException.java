package frame.exception;

public class ServiceException extends RuntimeException {
	private static final long serialVersionUID = 5080911785890964144L;
	private Boolean ok;
	private String message;
	private String url;
	private Boolean skip;

	public ServiceException(String message) {
		// super(message);父属性message为detailMessage要保持一至，给message赋值
		this.message = message;
		ok = false;
	}

	public ServiceException(String message, String url) {
		ok = false;
		this.message = message;
		this.url = url;
	}
	
	public ServiceException(String message, String url,Boolean skip) {
		ok = false;
		this.message = message;
		this.url = url;
		this.skip = false;
	}

	public ServiceException(String message, String url, Throwable cause) {
		super(message, cause);
		ok = false;
		this.url = url;
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
		// 设定message
		this.message = message;
		ok = false;
	}
	

	public Boolean getSkip() {
	    return skip;
	}

	public void setSkip(Boolean skip) {
	    this.skip = skip;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Boolean getOk() {
		return ok;
	}

	public String getUrl() {
		return url;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setOk(Boolean ok) {
		this.ok = ok;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
