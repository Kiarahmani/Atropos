package kiarahmani.atropos.DDL;

public class FieldName {

	private F_Type f_type;
	private String name;
	private boolean isPK;
	private boolean isSK;

	public boolean isPK() {
		return this.isPK;
	}

	public boolean isSK() {
		return this.isSK;
	}

	public F_Type getType() {
		return this.f_type;
	}

	public FieldName(String name, boolean isPK, boolean isSK, F_Type f_type) {
		this.name = name;
		this.isPK = isPK;
		this.isSK = isSK;
		this.f_type = f_type;
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
		if (this.isPK)
			return "*(" + this.name + ":" + this.f_type + ")*";
		else
			return "(" + this.name + ":" + this.f_type + ")";
	}
}
