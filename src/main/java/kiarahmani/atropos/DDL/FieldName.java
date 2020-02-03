package kiarahmani.atropos.DDL;

public class FieldName {

	private F_Type f_type;
	private String name;
	private boolean isPK;
	private boolean isSK;
	private boolean isDelta;
	private boolean isUUID;

	public boolean isUUID() {
		return this.isUUID;
	}

	public void setUUID() {
		this.isUUID = true;
	}

	public boolean isDelta() {
		return this.isDelta;
	}

	public void setDelta() {
		this.isDelta = true;
	}

	public boolean isPK() {
		return this.isPK;
	}

	public boolean isSK() {
		return this.isSK;
	}

	public void setSK(boolean sk) {
		this.isSK = sk;
	}

	public F_Type getType() {
		return this.f_type;
	}

	public FieldName(String name, boolean isPK, boolean isSK, F_Type f_type) {
		this.name = name;
		this.isPK = isPK;
		this.isSK = isSK;
		this.f_type = f_type;
		this.isDelta = false; // must be explicitly set later.
	}

	public String getName() {
		return this.name;
	}

	public boolean equals(FieldName other) {
		return this.name.contains(other.getName());
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String toStringWithType() {
		String pre = "", post = "";
		if (this.isPK) {
			pre += "*";
			post += "*";
		}
		if (this.isSK) {
			pre += "+";
			post += "+";
		}
		return pre + "(" + this.name + ":" + this.f_type + ")" + post;
	}
}
