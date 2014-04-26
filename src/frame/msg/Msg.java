package frame.msg;

public class Msg {
	private boolean ok = true;
	private String message;
	private String url;
	public static final Msg OK = new Msg("操作成功！");

	public Msg(String message) {
		this.message = message;
	}

	public Msg(String message, String url) {
		this.message = message;
		this.url = url;
	}

	public String getMessage() {
		return message;
	}

	public boolean getOk() {
		return ok;
	}

	public String getUrl() {
		return url;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
